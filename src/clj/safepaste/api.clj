(ns safepaste.api
  (:require [safepaste.expiry :as expiry]
            [buddy.core
             [nonce :as nonce]
             [codecs :as codecs]]
            [me.raynes.fs :as fs]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [clojure.data.json :as json]
            [clojure.java
             [io :as io]]))

(def output-dir "paste/")
(def ban-file "/var/tmp/safepaste.ban")
(def id-size 4)

(def max-paste-bytes (* 2 1024 1024))

; https://stackoverflow.com/questions/23018870/how-to-read-a-whole-binary-file-nippy-into-byte-array-in-clojure/26372677#26372677
(defn slurp-bytes
  "Slurps the bytes from a slurpable thing."
  [path]
  (with-open [out (java.io.ByteArrayOutputStream.)]
    (io/copy (io/input-stream path) out)
    (.toByteArray out)))

(defn spit-bytes
  "Opposite of slurp-bytes, opening f as a writer and writing all byte content."
  [f content]
  (with-open [w (io/output-stream f)]
    (.write w content)))

(defn random-id
  "Generates a random id which will be the means of accessing a given paste."
  []
  (codecs/bytes->hex (nonce/random-bytes id-size)))

(defn delete!
  "Deletes a paste and all of its corresponding files. Only used when burning."
  [path]
  (doseq [p [path (str path ".burn")]]
    (fs/delete p)))

(defn banned?
  "Returns whether or not the given ip has been banned for rate limiting.
   This reads a file which is managed by fail2ban."
  [ip]
  (if (fs/exists? ban-file)
    (with-open [reader (clojure.java.io/reader ban-file)]
      (some #(= ip %) (line-seq reader)))
    false))

(defn login []
  {:status 200
   :headers {"X-CSRF-Token" *anti-forgery-token*}
   :body (json/write-str {:max-size max-paste-bytes})})

(defn view [id]
  (let [path (str output-dir id)]
    (if (fs/exists? path)
      (let [data (codecs/bytes->base64 (slurp-bytes path))
            burn (fs/exists? (str path ".burn"))]
        ; If a .burn file exists, we'll delete the paste immediately
        (when burn
          (delete! path))
        (json/write-str {:data data :burned burn}))
      {:status 410})))

(defn paste! [body ip]
  (let [id (first (remove fs/exists? (repeatedly random-id)))
        json-body (json/read-str (slurp body))
        data (get json-body "data")
        expiry (get json-body "expiry")
        disable-pasting (fs/exists? "paste/.disable")
        output-file (str output-dir id)]
    (cond
      disable-pasting
      (do
        (println "Paste from" ip "is disabled.")
        {:status 503})

      (banned? ip)
      (do
        (println "Paste from" ip "is banned.")
        {:status 429})

      (>= (count data) max-paste-bytes)
      (do
        (println "Paste from" ip "is too large.")
        {:status 413})

      (not (expiry/valid? expiry))
      (do
        (println "Paste from" ip "has invalid expiry.")
        {:status 400})

      :else
      (do
        (println "Paste from" ip "for" id "is valid.")

        ; If the file needs to be burned after reading, a .burn file is
        ; also created.
        (when (= expiry "burn")
          (fs/touch (str output-file ".burn")))

        (spit-bytes output-file (codecs/base64->bytes data))

        ; Date the paste for its expiration
        (fs/touch output-file (expiry/offset expiry))
        (json/write-str {:id id})))))
