(ns safepaste.remote
  (:require [safepaste.dom :as dom]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [dommy.core :as dommy :refer-macros [sel1]]
            [crypto-js.aes])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn post! [e]
  (let [sha-key (.substring js/window.location.hash 1)
        data (dommy/value (sel1 :#input))
        safe-key (.toString (.random js/CryptoJS.lib.WordArray 32))
        encrypted (.encrypt js/CryptoJS.AES data safe-key)
        encoded (.toString encrypted)]
    ; TODO: input validation
    (when (and (not-empty data) (not (dom/viewing?)))
      (go (let [post-reply (<! (http/post "/api/new"
                                          {:json-params {:data encoded}}))
                post-reply-body (:body post-reply)]
            ; TODO: reply validation
            (dom/set-url! (str "/" post-reply-body "#" safe-key))
            (dom/update-input!))))))

(defn get! []
  (dom/set-status! "Downloading paste...")
  (let [id (.substring js/window.location.pathname 1)
        safe-key (.substring js/window.location.hash 1)]
    ; TODO: input validation
    (go (let [get-reply (<! (http/get (str "/api/" id)))
              _ (dom/set-status! "Decrypting paste...")
              decrypted (.decrypt js/CryptoJS.AES
                                  (.toString (:body get-reply))
                                  safe-key)]
          ; TODO: error checking
          (dommy/set-value! (sel1 :#input)
                            (.toString decrypted js/CryptoJS.enc.Utf8))
          (dom/set-status! "This paste is encrypted for your eyes only.")))))

