(ns safepaste.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [dommy.core :as dommy :refer-macros [sel1]]
            crypto-js.aes))

(enable-console-print!)

(def title js/window.title)

(defn push-history! [path]
  (.pushState js/window.history nil title path))

(defn reset [e]
  (push-history! "/"))

(defn post [e]
  (let [sha-key (.substring js/window.location.hash 1)
        data (dommy/value (sel1 :#input))
        safe-key (.toString (.random js/CryptoJS.lib.WordArray 32))
        encrypted (.encrypt js/CryptoJS.AES data safe-key)
        encoded (.toString encrypted)]
    ; TODO: input validation
    (go (let [post-reply (<! (http/post "/api/new"
                                        {:json-params {:data encoded}}))
              post-reply-body (:body post-reply)]
          ; TODO: reply validation
          (push-history! (str "/" post-reply-body "#" safe-key))))))

(defn onload [e]
  ; TODO: Setup other events: new/about/donate
  (dommy/listen! (sel1 :#new) :click reset)
  (dommy/listen! (sel1 :#post) :click post))

(dommy/listen! js/window :load onload)

;      (println "requesting")
;      (go (let [get-reply (<! (http/get (str "/api/" (:body post-reply))))]
;            (println "encoded response:" (:body get-reply))
;            (println "decrypting")
;            (def decrypted (.decrypt js/CryptoJS.AES (.toString (:body get-reply)) safe-key))
;            (println "decrypted:" (.toString decrypted js/CryptoJS.enc.Utf8))
;            ))))
;
; TODO: Read text box; encrypt data
;(go (let [post-reply (<! (http/post "/api/new"
;                                    {:json-params {:data "meow"}}))]
;      ; TODO: Verify json; show url
;      (go (let [get-reply (<! (http/get (str "/api/" (:body post-reply))))]
;            (println (:body get-reply))))))
