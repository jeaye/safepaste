(ns safepaste.remote
  (:require [safepaste.dom :as dom]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [dommy.core :as dommy :refer-macros [sel1]]
            [crypto-js.aes])
  (:require-macros [cljs.core.async.macros :refer [go]]))

; XXX: repeated in the server
(def max-post-bytes (* 2 1024 1024))
(def key-size 64)

(defn post! [e]
  (let [data (dommy/value (sel1 :#input))
        expiry (dommy/value (sel1 :#expiry))]
    (if (>= (count data) max-post-bytes)
      (dom/set-error! :too-large)
      (when (and (not-empty data) (not (dom/viewing?)))
        (dom/set-status! :encrypting)
        (let [safe-key (.toString (.random js/CryptoJS.lib.WordArray 32))
              encrypted (.encrypt js/CryptoJS.AES data safe-key)
              encoded (.toString encrypted)]
          (dom/set-status! :uploading)

          ; This is an async post; we won't block.
          (go
            (let [reply (<! (http/post "/api/new"
                                       {:json-params {:data encoded
                                                      :expiry expiry}}))
                  reply-json (.parse js/JSON (:body reply))]
              (if-let [error (.-error reply-json)]
                (dom/set-error! error)
                (do
                  (dom/set-url! (str "/" (.-id reply-json) "#" safe-key))
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
        (let [get-reply (<! (http/get (str "/api/" id)))
              reply-json (.parse js/JSON (:body get-reply))] ; TODO: Use transit?
          (if-let [error (.-error reply-json)]
            (dom/set-error! error)

            ; crytpo-js doesn't always throw, even when things are blatantly
            ; wrong. Given the same wrong data, it throws about 10% of the time.
            (try
              (dom/set-status! :decrypting)
              (let [decrypted (.decrypt js/CryptoJS.AES
                                        (.-data reply-json)
                                        safe-key)
                    decrypted-str (.toString decrypted js/CryptoJS.enc.Utf8)]
                (when (empty? decrypted-str)
                  (throw (js/Error.)))
                (dommy/set-value! (sel1 :#input) decrypted-str)
                (if (.-burned reply-json)
                  (dom/set-status! :viewing-burned)
                  (dom/set-status! :viewing)))
              (catch js/Error e
                (dom/set-error! :unable-to-decrypt)))))))))
