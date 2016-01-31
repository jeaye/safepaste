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
        [:p {:id "status"}]
        [:div {:class "expiry"}
         [:select
          (for [o ["Burn after reading" "One hour" "One day" "One week" "One month"]]
            [:option {:value o} o])]]
        [:nav
         (for [a ["new" "about" "donate" "post"]]
           [:a {:id a} a])]]
       [:div {:class "input"}
        [:textarea#input {:placeholder placeholder}]]])))
