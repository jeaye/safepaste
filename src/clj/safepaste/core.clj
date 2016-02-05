(ns safepaste.core
  (:gen-class)
  (:require [safepaste
             [home :as home]
             [css :as css]
             [api :as api]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [compojure
             [core :refer :all]
             [route :as route]]
            [clojure.java.io :as io]))

(use 'ring.middleware.anti-forgery
     'ring.middleware.session)

(defroutes app-routes
  (GET "/api/login" [] (api/login))
  ; TODO: Use the same regex for the API
  (GET "/:id{(?:[0-9a-fA-F]{8})?}" [id] (partial home/render id))
  (GET "/api/:id" [id] (api/view id))
  (POST "/api/new" {body :body} (api/post body))
  (route/files "/js" {:root "target/js"})
  (route/not-found (partial home/render nil)))

(def app (-> app-routes
             ; TODO: secure-site-defaults
             (wrap-defaults site-defaults)))
