(ns safepaste.core
  (:gen-class)
  (:require [buddy.core.crypto :as crypto]
            [buddy.core.codecs :as codecs]
            [buddy.core.nonce :as nonce]
            [buddy.core.hash :as hash]))

(def original-text
  (codecs/str->bytes "Hello World."))

(def iv (nonce/random-bytes 16))   ;; 16 byte random iv
(def secret-key (hash/sha512 "mysecret")) ;; 64 byte key

;; Encrypt the original-text content using previously
;; declared iv and key.
(def encrypted (crypto/encrypt original-text secret-key iv
                               {:algorithm :aes256-cbc-hmac-sha512}))

;; And now, decrypt it using the same parameters:
(def decrypted
  (-> (crypto/decrypt encrypted secret-key iv {:algorithm :aes256-cbc-hmac-sha512})
      (codecs/bytes->str)))

(defn -main [& args]
  (println (str encrypted))
  (println decrypted))
