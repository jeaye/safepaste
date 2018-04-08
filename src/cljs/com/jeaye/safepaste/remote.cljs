(ns com.jeaye.safepaste.remote
  (:require [com.jeaye.safepaste.dom :as dom]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [dommy.core :as dommy :refer-macros [sel1]]
            [cljs-promises.async :refer-macros [<?]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

; Sane default; provided by the server during login
(def max-paste-bytes (atom (* 2 1024 1024)))
(def key-size 64)

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
                 false ; Not extractable
                 ["encrypt" "decrypt"])
       (.then (fn [generated-key]
                (println "my generated key" generated-key)
                generated-key))
       (.catch (fn [error]
                 (println "error" error)))))

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

(defn encode [encrypted]
;var base64 = btoa(
;  new Uint8Array(arrayBuffer)
;    .reduce((data, byte) => data + String.fromCharCode(byte), '')
;)
  (-> (js/Uint8Array. encrypted)
      (.reduce (fn [acc e]
                 (+ acc (.fromCharCode js/String e)))
               "")
      js/btoa))

(defn paste! [e]
  (let [data (dommy/value (sel1 :#input))
        expiry (dommy/value (sel1 :#expiry))]
    (if (>= (count data) @max-paste-bytes)
      (dom/set-error! :too-large)
      (when (and (not-empty data) (not (dom/viewing?)))
        (dom/set-status! :encrypting)
        (go
          (let [key-promise (generate-key!)
                encrypted (<? (encrypt! (<? key-promise) (str data "\n")))
                decrypted (<? (decrypt (<? key-promise) encrypted))]
            (println "encoded key" (encode (<? key-promise)))
            (println "my encoded" (encode encrypted))
            (println "my decrypted" decrypted)))
        #_(let [safe-key (.toString (.random js/CryptoJS.lib.WordArray 32))
              encrypted (.encrypt js/CryptoJS.AES (str data "\n") safe-key)
              encoded (.toString encrypted)]
          (println "encoded" encoded)
          (dom/set-status! :uploading)

          ; This is an async post; we won't block.
          (go
            (let [reply (<! (http/post "/api/new"
                                       {:json-params {:data encoded
                                                      :expiry expiry}
                                        :headers {"X-CSRF-Token" @csrf-token}}))]
              (when (not (check-error! reply))
                (let [reply-json (.parse js/JSON (:body reply))]
                  (dom/set-url! (str "/" (aget reply-json "id") "#" safe-key))
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

      ; This is an async get; we won't block.
      (go
        (let [reply (<! (http/get (str "/api/" id)))]
          (when (not (check-error! reply))
            ; crypto-js doesn't always throw, even when things are blatantly
            ; wrong. Given the same wrong data, it throws about 10% of the time.
            (try
              (dom/set-status! :decrypting)
              #_(let [reply-json (.parse js/JSON (:body reply))
                    decrypted (.decrypt js/CryptoJS.AES
                                        (aget reply-json "data")
                                        safe-key)
                    decrypted-str (.toString decrypted js/CryptoJS.enc.Utf8)]
                (when (empty? decrypted-str)
                  (throw (js/Error. reply)))
                (dommy/set-value! (sel1 :#input) decrypted-str)
                (if (aget reply-json "burned")
                  (dom/set-status! :viewing-burned)
                  (dom/set-status! :viewing)))
              (catch js/Error e
                (dommy/set-value! (sel1 :#input) (str e "\n\n" reply))
                (dom/set-error! :unable-to-decrypt)))))))))
