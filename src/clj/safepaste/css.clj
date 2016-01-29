(ns safepaste.css
  (:require [garden.core :refer [css]]))

; Colors from http://ethanschoonover.com/solarized
(defn main []
  (let [header-height "30px"
        input-height "calc(100% - 30px)"
        header-background-color "#eee8d5"
        header-color "586e75"
        content-background-color "#fdf6e3"
        content-color "#657b83"]
    (css
      {:pretty-print? false}
      [:html :body {:height "100%"}]
      [:body
       {:color content-color
        :width "100%"
        :margin "0%"
        :padding "0%"}
       [:.header {:background-color header-background-color
                  :height header-height
                  :max-height header-height
                  :width "100%"}
        [:nav
         {:color header-color
          :text-align "right"
          :padding-right "10px"
          :padding-top "5px"}]]
       [:.input {:background-color content-background-color
                 :height input-height
                 :min-height input-height
                 :width "100%"}]]
      [:.input :textarea {:background-color content-background-color
                          :color content-color
                          :border "0px"
                          :margin "0px"
                          :padding "0px"
                          :overflow "auto"
                          :width "100%"
                          :height "100%"}])))
