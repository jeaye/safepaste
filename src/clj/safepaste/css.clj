(ns safepaste.css
  (:require [garden.core :refer [css]]))

; https://jsfiddle.net/eu8vjzds/
(defn render [request]
  (css
    [:html {:background-color "black"}]))
