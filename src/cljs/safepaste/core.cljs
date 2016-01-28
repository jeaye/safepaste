(ns safepaste.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            crypto-js.aes))

(enable-console-print!)

(def sha-key (.substring js/window.location.hash 1))
(def data ">3< Misa")

(println "generating key")
(def safe-key (.toString (.random js/CryptoJS.lib.WordArray 32)))
(println "encrypting with key:" safe-key)
(def encrypted (.encrypt js/CryptoJS.AES data safe-key))
(println "encoding in base64")
(def encoded (.toString encrypted))

(println "posting")
(go (let [post-reply (<! (http/post "/api/new"
                                    {:json-params {:data encoded}}))]
      (println "requesting")
      (go (let [get-reply (<! (http/get (str "/api/" (:body post-reply))))]
            (println "encoded response:" (:body get-reply))
            (println "decrypting")
            (def decrypted (.decrypt js/CryptoJS.AES (.toString (:body get-reply)) safe-key))
            (println "decrypted:" (.toString decrypted js/CryptoJS.enc.Utf8))
            ))))

; TODO: Read text box; encrypt data
;(go (let [post-reply (<! (http/post "/api/new"
;                                    {:json-params {:data "meow"}}))]
;      ; TODO: Verify json; show url
;      (go (let [get-reply (<! (http/get (str "/api/" (:body post-reply))))]
;            (println (:body get-reply))))))
