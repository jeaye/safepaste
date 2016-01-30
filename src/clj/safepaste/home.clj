(ns safepaste.home
  (:require [safepaste.css :as css]
            [hiccup.page :as page]))

(defn render [id request]
  (let [placeholder "Enter your paste here…"]
    (page/html5
      [:head
       [:style (css/main)]
       (page/include-js "/js/main.js")
       [:title "safepaste"]]
      [:body
       [:div {:class "header"}
        [:p {:id "status"} "Your post will be encrypted using AES-256."]
        [:nav
         (for [a ["new" "about" "donate" "post"]]
           [:a {:id a} a])]]
       [:div {:class "input"}
        [:textarea#input {:placeholder placeholder}]]])))
