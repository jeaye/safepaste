(ns com.jeaye.safepaste.remote
  (:require [com.jeaye.safepaste.dom :as dom]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [dommy.core :as dommy :refer-macros [sel1]]
            [cljs-promises.async :refer-macros [<?]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

; Sane default; provided by the server during login
(def max-paste-bytes (atom (* 2 1024 1024)))
(def key-size 44) ;  TODO: Right?

; Unique session token required for pasting
(def csrf-token (atom ""))

(def error-from-status
  {400 :bad-request
   410 :invalid-id
   413 :too-large
   429 :banned
   503 :pasting-disabled})

(defn check-error!
  "Checks the :status code of the reply and updates the dom to convey
   errors. Returns whether or not an error was handled."
  [reply]
  (let [status (:status reply)
        error? (not= 200 status)]
    (when error?
      (dom/set-error! (error-from-status status :bad-request)))
    error?))

(defn login!
  "By logging in, we get some updated info from the server.
   Anyone can login and it doesn't require credentials."
  []
  (go
    (let [reply (<! (http/get "/api/login"))
          reply-json (.parse js/JSON (:body reply))]
      (when (not (check-error! reply))
        (swap! csrf-token (fn [_] (get (:headers reply) "x-csrf-token")))
        (swap! max-paste-bytes (fn [_] (aget reply-json "max_size")))))))

(defn generate-key! []
   (-> (.generateKey js/window.crypto.subtle
                 #js{:name "AES-CBC" :length 256}
                 true ; Extractable
                 ["encrypt" "decrypt"])
       (.then (fn [generated-key]
                (println "my generated key" generated-key)
                generated-key))
       (.catch (fn [error]
                 (println "error" error)))))

(defn export-key [key-data]
  (println "exporting key" key-data)
  (-> (.exportKey js/window.crypto.subtle
                  "raw"
                  key-data)
      (.then (fn [exported-key]
               (println "my key length" (.-byteLength exported-key))
               (println "my exported key" exported-key)
               exported-key))
      (.catch (fn [error]
                (println "key export error" error)))))

(defn import-key [raw-key]
  (println "importing key" raw-key)
  (-> (.importKey js/window.crypto.subtle
                  "raw"
                  raw-key
                  #js{:name "AES-CBC"}
                  true ; Extractable
                  ["encrypt" "decrypt"])
      (.then (fn [imported-key]
               (println "my imported key" imported-key)
               imported-key))
      (.catch (fn [error]
                (println "key import error" error)))))

(defn encrypt! [aes-key data]
  (-> (.encrypt js/window.crypto.subtle
                #js{:name "AES-CBC"
                    :iv (.from js/Uint8Array (range 16))
                    ; TODO: Random ivs
                    #_:iv #_(.getRandomValues js/window.crypto
                                          (js/Uint8Array. 16))}
                aes-key
                (.encode (js/TextEncoder.) data))
      (.then (fn [encrypted]
               (println "my encrypted raw" encrypted)
               (println "my encrypted" (js/Uint8Array. encrypted))
               encrypted))
      (.catch (fn [error]
               (println "encryption error" error)))))

(defn decrypt [aes-key data]
  (-> (.decrypt js/window.crypto.subtle
                #js{:name "AES-CBC"
                    :iv (.from js/Uint8Array (range 16))
                    }
                aes-key
                data)
      (.then (fn [decrypted]
               (println "my decrypted length" (.-byteLength decrypted))
               (println "my decrypted raw" decrypted)
               (println "my decrypted" (js/Uint8Array. decrypted))
               (println "my test" (.apply js/String.fromCharCode nil (js/Uint8Array. decrypted)))
               (let [decoder (js/TextDecoder. "utf-8")]
                 (.decode decoder (js/Uint8Array. decrypted)))))
      (.catch (fn [error]
               (println "decryption error" error)))))

(defn encode [data]
  (-> data
      #_(js/Uint8Array. data)
      #_(.reduce (fn [acc e]
                 (+ acc (.fromCharCode js/String e)))
               "")
      js/btoa))

(defn decode [encoded]
  (-> encoded
      #_(js/Uint8Array. encoded)
      #_(.reduce (fn [acc e]
                 (+ acc (.fromCharCode js/String e)))
               "")
      js/atob))

(defn paste! [e]
  (let [data (dommy/value (sel1 :#input))
        expiry (dommy/value (sel1 :#expiry))]
    (if (>= (count data) @max-paste-bytes)
      (dom/set-error! :too-large)
      (when (and (not-empty data) (not (dom/viewing?)))
        (dom/set-status! :encrypting)
        (go
          (let [key-promise (generate-key!)
                exported-key (<? (export-key (<? key-promise)))
                encoded-key (encode exported-key)
                encrypted (<? (encrypt! (<? key-promise) (str data "\n")))
                encoded (encode encrypted)]
            (println "encoded" encoded)

            (dom/set-status! :uploading)
            (let [reply (<! (http/post "/api/new"
                                       {:json-params {:data encoded
                                                      :expiry expiry}
                                        :headers {"X-CSRF-Token" @csrf-token}}))]
              (when (not (check-error! reply))
                (let [reply-json (.parse js/JSON (:body reply))]
                  (dom/set-url! (str "/" (aget reply-json "id") "#" encoded-key))
                  (dom/update-inputs!)
                  (if (= expiry "burn")
                    (dom/set-status! :uploaded-burn)
                    (dom/set-status! :uploaded)))))))))))

(defn get! []
  (dom/set-status! :downloading)
  (let [id (.substring js/window.location.pathname 1)
        safe-key (.substring js/window.location.hash 1)]
    (if (not= key-size (count safe-key))
      (dom/set-error! :invalid-key)
      (go
        (let [reply (<! (http/get (str "/api/" id)))]
          (when (not (check-error! reply))
            (try
              (dom/set-status! :decrypting)
              (let [reply-json (.parse js/JSON (:body reply))
                    imported-key (<? (import-key (js/Uint8Array. (decode safe-key))))
                    decrypted (<? (decrypt imported-key (aget reply-json "data")))]
                (when (empty? decrypted)
                  (throw (js/Error. reply)))
                (dommy/set-value! (sel1 :#input) decrypted)
                (if (aget reply-json "burned")
                  (dom/set-status! :viewing-burned)
                  (dom/set-status! :viewing)))
              (catch js/Error e
                (dommy/set-value! (sel1 :#input) (str e "\n\n" reply))
                (dom/set-error! :unable-to-decrypt)))))))))
