(ns safepaste.css
  (:require [garden.core :refer [css]]))

(defn main []
  (let [header-height "30px"
        input-height "calc(100% - 30px)"]
    (css
      [:html :body {:height "100%"}]
      [:body
       {:width "100%"
        :margin "0%"
        :padding "0%"}
       [:.header {:background-color "#428bca"
                  :height header-height
                  :max-height header-height
                  :width "100%"}]
       [:.input {:background-color "white"
                 :height input-height
                 :min-height input-height
                 :width "100%"}]]
      [:.input :textarea {:border "0px"
                          :margin "0px"
                          :padding "0px"
                          :overflow "auto"
                          :width "100%"
                          :height "100%"}])))
