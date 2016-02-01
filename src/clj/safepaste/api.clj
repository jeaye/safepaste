(ns safepaste.api
  (:require [buddy.core
             [nonce :as nonce]
             [codecs :as codecs]]
            [me.raynes.fs :as fs]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))

(def output-dir "post/")
(def id-size 4)

; XXX: repeated in the client
(def max-post-bytes (* 2 1024 1024))

(def second-ms 1000)
(def hour-ms (* 3600 second-ms))
(def day-ms (* 24 hour-ms))
(def week-ms (* 7 day-ms))
(def month-ms (* 30 day-ms))

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

(defn random-id []
  (codecs/bytes->hex (nonce/random-bytes id-size)))

(defn valid-expiry? [expiry]
  (some #(= % expiry) ["burn" "hour" "day" "week" "month"]))

(defn future-ms [offset]
  (+ (System/currentTimeMillis) offset))

(defn expiry-time [expiry]
  (case expiry
    "burn" (future-ms day-ms)
    "hour" (future-ms hour-ms)
    "day" (future-ms day-ms)
    "week" (future-ms week-ms)
    "month" (future-ms month-ms)))

(defn view [id]
  (let [path (str output-dir id)]
    (json/write-str
      (if (fs/exists? path)
        {:data (codecs/bytes->base64 (slurp-bytes path))}
        {:error "Invalid post ID."}))))

(defn post [body]
  (let [id (first (remove fs/exists? (repeatedly random-id)))
        json-body (json/read-str (slurp body))
        data (get json-body "data")
        expiry (get json-body "expiry")]
    (cond
      (>= (count data) max-post-bytes)
      (json/write-str {:error "Post is too large."})

      (not (valid-expiry? expiry))
      (json/write-str {:error "Invalid expiry."})

      :else
      (do
        ; There's a bug where the modification time isn't set when it's
        ; creating a new file. I've opened an issue here:
        ; https://github.com/Raynes/fs/issues/101
        (dotimes [_ 2]
          (fs/touch (str output-dir id ".expire") (expiry-time expiry)))

        (spit-bytes (str output-dir id) (codecs/base64->bytes data))
        (json/write-str {:id id})))))
