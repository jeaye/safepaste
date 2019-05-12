(ns com.jeaye.safepaste.remote
  (:require [com.jeaye.safepaste.dom :as dom]
            [goog.crypt.base64 :as b64]
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
      (when-not (check-error! reply)
        (reset! csrf-token (get (:headers reply) "x-csrf-token"))
        (reset! max-paste-bytes (aget reply-json "max_size"))))))

(defn generate-key! []
   (-> (.generateKey js/window.crypto.subtle
                 #js{:name "AES-CBC" :length 256}
                 true ; Extractable
                 ["encrypt" "decrypt"])
       (.then (fn [generated-key]
                generated-key))
       (.catch (fn [error]
                 (println "error" error)))))

(defn export-key [key-data]
  (println "exporting key" key-data)
  (-> (.exportKey js/window.crypto.subtle
                  "raw"
                  key-data)
      (.then (fn [exported-key]
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
               encrypted))
      (.catch (fn [error]
               (println "encryption error" error)))))

(defn decrypt [aes-key data]
  (-> (.decrypt js/window.crypto.subtle
                #js{:name "AES-CBC"
                    :iv (.from js/Uint8Array (range 16))}
                aes-key
                data)
      (.then (fn [decrypted]
               (let [decoder (js/TextDecoder. "utf-8")]
                 (.decode decoder (js/Uint8Array. decrypted)))))
      (.catch (fn [error]
               (println "decryption error" error)))))

(defn encode [data]
  (b64/encodeByteArray data true))

(defn decode [encoded]
  (b64/decodeStringToUint8Array encoded true))

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
                encoded-key (encode (js/Uint8Array. exported-key))
                encrypted (<? (encrypt! (<? key-promise) (str data "\n")))
                encoded (encode (js/Uint8Array. encrypted))]

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
                    imported-key (<? (import-key (decode safe-key)))
                    encrypted (decode (aget reply-json "data"))
                    decrypted (<? (decrypt imported-key encrypted))]
                (when (empty? decrypted)
                  (throw (js/Error. reply)))
                (dommy/set-value! (sel1 :#input) decrypted)
                (if (aget reply-json "burned")
                  (dom/set-status! :viewing-burned)
                  (dom/set-status! :viewing)))
              (catch js/Error e
                (dommy/set-value! (sel1 :#input) (str e "\n\n" reply))
                (dom/set-error! :unable-to-decrypt)))))))))
