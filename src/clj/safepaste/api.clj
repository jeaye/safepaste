(ns safepaste.api
  (:require [buddy.core
             [nonce :as nonce]
             [codecs :as codecs]]
            [me.raynes.fs :as fs]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))

; TODO: make sure this exists
(def output-dir "post/")

; https://stackoverflow.com/questions/23018870/how-to-read-a-whole-binary-file-nippy-into-byte-array-in-clojure/26372677#26372677
(defn slurp-bytes
  "Slurp the bytes from a slurpable thing."
  [path]
  (with-open [out (java.io.ByteArrayOutputStream.)]
    (io/copy (io/input-stream path) out)
    (.toByteArray out)))

(defn spit-bytes
  "Opposite of slurp-bytes, opening f as a writer and writing all byte content."
  [f content]
  (with-open [w (io/output-stream f)]
    (.write w content)))

(defn view [id]
  ; TODO: Test that the path can't be something like ../../../secret-data
  (let [path (str output-dir id)]
    (json/write-str
      (if (fs/exists? path)
        {:data (codecs/bytes->base64 (slurp-bytes path))}
        {:error "Invalid post ID."}))))

(defn post [body]
  ; TODO: Input validation
  (let [id (codecs/bytes->hex (nonce/random-bytes 4)) ; TODO: improve
        json-body (json/read-str (slurp body))]
    (spit-bytes (str output-dir id)
                (codecs/base64->bytes (get json-body "data")))
    (json/write-str {:id id})))
