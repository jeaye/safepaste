(ns safepaste.home
  (:require [safepaste.css :as css]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]))

(defn render [request]
  (page/html5
    [:head
     [:style (css/main)]
     (page/include-js "/main.js")
     [:title "safepaste"]]
    [:body
     [:div {:class "header"}]
     [:div {:class "input"}]]))
