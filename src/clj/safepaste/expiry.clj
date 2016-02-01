(ns safepaste.expiry)

(def second-ms 1000)
(def hour-ms (* 3600 second-ms))
(def day-ms (* 24 hour-ms))
(def week-ms (* 7 day-ms))
(def month-ms (* 30 day-ms))

(defn valid? [expiry]
  (some #(= % expiry) ["burn" "hour" "day" "week" "month"]))

(defn future-ms [offset]
  (+ (System/currentTimeMillis) offset))

(defn offset [expiry]
  (case expiry
    "burn" (future-ms day-ms)
    "hour" (future-ms hour-ms)
    "day" (future-ms day-ms)
    "week" (future-ms week-ms)
    "month" (future-ms month-ms)))
