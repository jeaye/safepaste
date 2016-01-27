(ns safepaste.core
  (:gen-class)
  (:require [buddy.core.crypto :as crypto]
            [buddy.core.codecs :as codecs]
            [buddy.core.nonce :as nonce]
            [buddy.core.hash :as hash]

            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.response :refer [render]]
            [clojure.java.io :as io]

            [clojure.data.json :as json]))

(defn hexify [b]
  (apply str (map #(format "%02x" %) b)))

(defn unhexify [s]
  (let [bytes (into-array
                Byte/TYPE
                (map (fn [[x y]]
                       (unchecked-byte (Integer/parseInt (str x y) 16)))
                     (partition 2 s)))]
    bytes))

(def iv (codecs/str->bytes "1234567890123456"))

(defn encrypt [data]
  (let [random-iv iv
        random-key (nonce/random-bytes 64)]
    [random-key
     (crypto/encrypt (codecs/str->bytes data)
                    random-key
                    random-iv
                    {:algorithm :aes256-cbc-hmac-sha512})]))

(defn decrypt [file]
  (-> (crypto/decrypt (unhexify (slurp file))
                      (unhexify file)
                      iv
                      {:algorithm :aes256-cbc-hmac-sha512})
      (codecs/bytes->str)))

;(defn -main [& args]
;  (if (some #(= % "-d") args)
;    (println (decrypt (second args)))
;    (let [[random-key encrypted-data] (encrypt (first args))]
;      (println (hexify random-key))
;      (spit (hexify random-key) (hexify encrypted-data)))))

(defn home [req]
  (render (io/resource "index.html") req))

(defroutes app-routes
  (GET "/" [] home)
  (GET "/api/:id" [id]
    (str "this is from the server for id " id))
  (POST "/api/new" {body :body}
    ; TODO: Generate new key; save to file; return key as json
    (println "new body" (json/read-str (slurp body)))
    body)
  (route/files "/" {:root "target"})
  (route/resources "/" {:root "target"})
  (route/not-found home))

(def app (wrap-defaults
           app-routes
           ; TODO: Turn this back on
           (assoc-in site-defaults [:security :anti-forgery] false)))
