(ns safepaste.home
  (:require [safepaste.css :as css]
            [hiccup.page :as page]))

(defn render [request]
  (page/html5
    [:head
     [:style (css/main)]
     (page/include-js "/main.js")
     [:title "safepaste"]]
    [:body
     [:div {:class "header"}
      [:p "Your post will be encrypted using AES-256."]
      [:nav
       ; TODO: cleanup
       [:a {:id "new"} "new"]
       [:a {:id "about"} "about"]
       [:a {:id "donate"} "donate"]
       [:a {:id "post"} "post"]]]
     [:div {:class "input"}
      [:textarea#input {:placeholder "Enter your paste here…"}]]]))
