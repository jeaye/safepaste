(ns safepaste.css
  (:require [garden.core :refer [css]]))

; https://jsfiddle.net/eu8vjzds/
(defn main []
  (let [header-height "20%"
        input-height "80%"]
    (css
      [:html :body {:height "100%"}]
      [:body
       {:width "100%"
        :margin "0%"
        :padding "0%"}
       [:.header {:background-color "blue"
                  :height header-height
                  :max-height header-height
                  :width "100%"}]
       [:.input {:background-color "green"
                 :height input-height
                 :min-height input-height
                 :width "100%"}]])))
