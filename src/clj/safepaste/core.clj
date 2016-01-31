(ns safepaste.core
  (:gen-class)
  (:require [safepaste
             [home :as home]
             [css :as css]
             [api :as api]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [compojure
             [core :refer :all]
             [route :as route]
             [response :refer [render]]]
            [clojure.java.io :as io]))

(defroutes app-routes
  (GET "/:id{(?:.{8})?}" [id] (partial home/render id))
  (GET "/api/:id" [id] (api/view id))
  (POST "/api/new" {body :body} (api/post body))
  (route/files "/js" {:root "target/js"})
  (route/not-found (partial home/render nil)))

(def app (wrap-defaults
           app-routes
           ; TODO: Turn this back on
           (assoc-in site-defaults [:security :anti-forgery] false)))
