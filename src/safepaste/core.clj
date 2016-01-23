(ns safepaste.core
  (:gen-class)
  (:require [buddy.core.crypto :as crypto]
            [buddy.core.codecs :as codecs]
            [buddy.core.nonce :as nonce]
            [buddy.core.hash :as hash]))

(def message
  (codecs/str->bytes "Hello World."))

(def iv (nonce/random-bytes 16))
(def secret-key (nonce/random-bytes 64))

(def encrypted (crypto/encrypt message
                               secret-key
                               iv
                               {:algorithm :aes256-cbc-hmac-sha512}))

(def decrypted
  (-> (crypto/decrypt encrypted
                      secret-key
                      iv
                      {:algorithm :aes256-cbc-hmac-sha512})
      (codecs/bytes->str)))

(defn -main [& args]
  (println (str encrypted))
  (println decrypted))
