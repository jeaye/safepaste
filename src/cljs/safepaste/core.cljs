(ns safepaste.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [dommy.core :as dommy :refer-macros [sel1]]
            crypto-js.aes))

(enable-console-print!)

(def title js/window.title)

(defn push-history! [path]
  (.replaceState js/window.history nil title path))

(defn viewing? []
  (not= "/" js/window.location.pathname))

(defn lock-input! []
  (let [input (sel1 :#input)]
    (if (viewing?)
      (dommy/set-attr! input :readonly)
      (dommy/remove-attr! input :readonly))))

(defn reset-input! []
  (dommy/set-value! (sel1 :#input) "")
  (lock-input!))

(defn reset-page! [e]
  (push-history! "/")
  (reset-input!))

(defn post! [e]
  ; TODO: Don't post empty content
  (let [sha-key (.substring js/window.location.hash 1)
        data (dommy/value (sel1 :#input))
        safe-key (.toString (.random js/CryptoJS.lib.WordArray 32))
        encrypted (.encrypt js/CryptoJS.AES data safe-key)
        encoded (.toString encrypted)]
    ; TODO: input validation
    (when (not-empty data)
      (go (let [post-reply (<! (http/post "/api/new"
                                          {:json-params {:data encoded}}))
                post-reply-body (:body post-reply)]
            ; TODO: reply validation
            (push-history! (str "/" post-reply-body "#" safe-key))
            (lock-input!))))))

(defn get! []
  (println "requesting")
  (let [id (.substring js/window.location.pathname 1)
        safe-key (.substring js/window.location.hash 1)]
    ; TODO: input validation
    (go (let [get-reply (<! (http/get (str "/api/" id)))
              decrypted (.decrypt js/CryptoJS.AES
                                  (.toString (:body get-reply))
                                  safe-key)]
          ; TODO: error checking
          (println "encoded response:" (:body get-reply))
          (dommy/set-value! (sel1 :#input)
                            (.toString decrypted js/CryptoJS.enc.Utf8))))))

(defn onload [e]
  ; TODO: Setup other events: about/donate
  (when (viewing?)
    (get!))
  (dommy/listen! (sel1 :#new) :click reset-page!)
  (dommy/listen! (sel1 :#post) :click post!))

; TODO: listen to browser back/forward and refresh everything
(dommy/listen! js/window :load onload)
