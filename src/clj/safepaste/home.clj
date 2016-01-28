(ns safepaste.home
  (:use [hiccup.core]))

(defn render [request]
  (html
    [:head
     [:script {:src "main.js"}]
     [:title "safepaste"]]
    [:body]))
