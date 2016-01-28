(ns safepaste.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            js.crypto))

(enable-console-print!)

(def sha-key (.substring js/window.location.hash 1))

(println "cryptojs:" (.toString (.random js/CryptoJS.lib.WordArray 8)))

;(defn hexify [b]
;  (apply str (map #(format "%02x" %) b)))
;
;(defn unhexify [s]
;  (let [bytes (into-array
;                Byte/TYPE
;                (map (fn [[x y]]
;                       (unchecked-byte (Integer/parseInt (str x y) 16)))
;                     (partition 2 s)))]
;    bytes))
;
;(def iv (codecs/str->bytes "1234567890123456"))

;(defn encrypt [data]
;  (let [random-iv iv
;        random-key (nonce/random-bytes 64)]
;    [random-key
;     (crypto/encrypt (codecs/str->bytes data)
;                    random-key
;                    random-iv
;                    {:algorithm :aes256-cbc-hmac-sha512})]))
;
;(defn decrypt [file]
;  (-> (crypto/decrypt (unhexify (slurp file))
;                      (unhexify file)
;                      iv
;                      {:algorithm :aes256-cbc-hmac-sha512})
;      (codecs/bytes->str)))

; TODO: Read text box; encrypt data
(go (let [post-reply (<! (http/post "/api/new"
                                    {:json-params {:data "meow"}}))]
      ; TODO: Verify json; show url
      (go (let [get-reply (<! (http/get (str "/api/" (:body post-reply))))]
            (println (:body get-reply))))))
