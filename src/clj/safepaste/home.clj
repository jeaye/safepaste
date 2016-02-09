(ns safepaste.home
  (:require [safepaste.css :as css]
            [hiccup.page :as page]))

(def default-expiry "day")

(defn render [id request]
  (let [placeholder "Enter your paste here…"]
    (page/html5
      [:head
       ; Have search engines ignore everything but the home page
       (when (not-empty id)
         [:meta {:name "robots" :content "noindex"}])
       [:style (css/main)]
       (page/include-js "/js/main.min.js")
       [:title "safepaste"]]
      [:body
       [:div.header
        [:p#status.status-error
         [:noscript
          "JavaScript is required for client-side encryption."]]
        [:div.expiry
         [:select#expiry
          [:option {:value "burn"} "Burn after reading"]
          (for [o ["hour" "day" "week" "month"]]
            [:option
             (conj {:value o}
                   (when (= o default-expiry)
                     {:selected "selected"}))
             (str "Expires after 1 " o)])]]
        [:nav
         (for [a ["new" "about" "donate" "paste"]]
           [:a {:id a} a])]]
       [:div.input
        [:textarea#input {:placeholder placeholder
                          :autofocus "autofocus"}]]])))
