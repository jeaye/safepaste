(ns safepaste.api
  (:require [safepaste.expiry :as expiry]
            [buddy.core
             [nonce :as nonce]
             [codecs :as codecs]]
            [me.raynes.fs :as fs]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))

(def disable-pasting false)
(def output-dir "paste/")
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

(defn random-id []
  (codecs/bytes->hex (nonce/random-bytes id-size)))

(defn delete!
  "Deletes a paste and all of its corresponding files. Only used when burning."
  [path]
  (doseq [p [path (str path ".expire") (str path ".burn")]]
    (fs/delete p)))

(defn login []
  {:status 200
   :headers {"X-CSRF-Token" *anti-forgery-token*}
   ; TODO: This isn't the same as the other rendering
   :body (json/write-str {:max_paste_size max-paste-bytes})})

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
        expiry (get json-body "expiry")]
    (cond
      disable-pasting
      (do
        (print "Pasting is disabled.")
        {:status 503})

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
        (println "Paste" id "from" ip "is valid.")

        ; Each paste file also gets a .expire file which is dated for
        ; when it should be deleted.
        ; There's a bug where the modification time isn't set when it's
        ; creating a new file. I've opened an issue here:
        ; https://github.com/Raynes/fs/issues/101
        (dotimes [_ 2]
          (fs/touch (str output-dir id ".expire") (expiry/offset expiry)))

        ; If the file needs to be burned after reading, a .burn file is
        ; also created.
        (when (= expiry "burn")
          (fs/touch (str output-dir id ".burn")))

        (spit-bytes (str output-dir id) (codecs/base64->bytes data))
        (json/write-str {:id id})))))
