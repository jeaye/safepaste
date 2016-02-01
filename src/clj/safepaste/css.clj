(ns safepaste.css
  (:require [garden.core :refer [css]]))

; Colors from http://ethanschoonover.com/solarized
(def base00    "#657b83") (def base0     "#839496")
(def base01    "#586e75") (def base1     "#93a1a1")
(def base02    "#073642") (def base2     "#eee8d5")
(def base03    "#002b36") (def base3     "#fdf6e3")
(def yellow    "#b58900") (def orange    "#cb4b16")
(def red       "#dc322f") (def magenta   "#d33682")
(def violet    "#6c71c4") (def blue      "#268bd2")
(def cyan      "#2aa198") (def green     "#859900")

; TODO: Break these bits into individual functions
(defn main []
  (let [header-height "3em"
        input-height (str "calc(100% - " header-height ")")
        header-background-color base2
        header-color base00
        header-font-size "2em"
        content-background-color base3
        content-color base00
        content-font-size "1.5em"
        status-color green
        status-error-color red
        link-hover-color orange]
    (css
      {:pretty-print? false}
      [:html :body {:height "100%"}]
      [:body
       {:background-color content-background-color
        :color content-color
        :width "100%"
        :min-height "100%"
        :margin "0%"
        :padding "0%"}
       [:a:hover {:color link-hover-color}]
       [:.header {:background-color header-background-color
                  :height header-height
                  :max-height header-height
                  :width "100%"
                  :display "flex"
                  :justify-content "center"}
        [:p {:color status-color
             :width "33%"
             :font-weight :bold
             :margin "0px"
             :margin-top "1em"
             :margin-left "1em"
             :padding "0px"}]
        [:nav
         {:width "33%"
          :color header-color
          :text-align "right"
          :padding-right "10px"
          :padding-top "5px"}
         [:a
          {:padding ".1em .1em"
           :margin-left ".5em"
           :font-size header-font-size}]]
        [:.expiry
         {:width "33%"
          :padding-top ".5em"
          :text-align "center"}
         [:select
          {:color header-color
           :font-size "1em"
           :font-weight "bold"
           :border "0px"
           :background-color content-background-color
           :appearance "none"
           :-webkit-appearance "none"
           :-moz-appearance "none"
           :padding "5px 35px 5px 5px"
           :margin "0px"}]]]
       [:.input {:height input-height
                 :min-height input-height
                 :width "100%"}]]
      [:.status-error {:color (str status-error-color " !important")}]
      [:textarea {:background-color content-background-color
                  :color content-color
                  :border "0px"
                  :margin "0px"
                  :padding "0px"
                  :width "100%"
                  :height "100%"
                  :min-height "100%"
                  :font-size content-font-size}])))
