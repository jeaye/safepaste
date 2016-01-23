(ns safepaste.core
  (:gen-class)
  (:require [buddy.core.crypto :as crypto]
            [buddy.core.codecs :as codecs]
            [buddy.core.nonce :as nonce]
            [buddy.core.hash :as hash]))

(defn hexify [b]
  (apply str (map #(format "%02x" %) b)))

(defn unhexify [s]
  (let [bytes (into-array Byte/TYPE
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

(defn -main [& args]
  (if (some #(= % "-d") args)
    (println (decrypt (second args)))
    (let [[random-key encrypted-data] (encrypt (first args))]
      (println (hexify random-key))
      (spit (hexify random-key) (hexify encrypted-data)))))
