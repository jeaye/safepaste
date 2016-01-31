(ns safepaste.api
  (:require [buddy.core
             [nonce :as nonce]
             [codecs :as codecs]]
            [clojure.data.json :as json]))

; TODO: make sure this exists
(def output-dir "post/")

(defn view [id]
  ; TODO: Input validation
  (slurp (str output-dir id)))

(defn post [body]
  ; TODO: Input validation
  (let [id (codecs/bytes->hex (nonce/random-bytes 4)) ; TODO: improve
        json-body (json/read-str (slurp body))]
    (spit (str output-dir id)
          (codecs/base64->bytes (get json-body "data")))
    id))
