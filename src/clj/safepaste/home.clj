(ns safepaste.home
  (:require [safepaste.css :as css]
            [hiccup.page :as page]))

(def default-expiry "day")

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
          [:option {:value "burn"} "Burn after reading"]
          (for [o ["hour" "day" "week" "month"]]
            [:option
             (cond-> {:value o}
               (= o default-expiry)
               (assoc :selected "selected"))
             (str "Expires after 1 " o)])]]
        [:nav
         (for [a ["new" "about" "donate" "post"]]
           [:a {:id a} a])]]
       [:div {:class "input"}
        [:textarea#input {:placeholder placeholder}]]])))
