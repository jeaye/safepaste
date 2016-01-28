(ns safepaste.home
  (:use [hiccup.core]))

(defn render [request]
  (html
    [:head
     [:link {:rel "stylesheet" :type "test/css" :href "/main.css"}]
     [:script {:src "main.js"}]
     [:title "safepaste"]]
    [:body]))
