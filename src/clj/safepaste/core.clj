(ns safepaste.core
  (:gen-class)
  (:require [safepaste
             [home :as home]
             [css :as css]
             [api :as api]]
            [ring.middleware
             [defaults :refer [wrap-defaults secure-site-defaults]]
             [gzip :as gzip]]
            [compojure
             [core :refer :all]
             [route :as route]]
            [clojure.java.io :as io]))

(use 'ring.middleware.anti-forgery
     'ring.middleware.session)

(def id-regex "{(?:[0-9a-fA-F]{8})?}")

(defroutes app-routes
  (GET "/api/login" [] (api/login))
  (GET (str "/:id" id-regex) [id] (partial home/render id))
  (GET (str "/api/:id" id-regex) [id] (api/view id))
  (POST "/api/new" {body :body ip :remote-addr} (api/paste! body ip))
  (route/files "/" {:root "resources/public/"})
  (route/not-found (partial home/render nil)))

(def app (-> app-routes
             (wrap-defaults
               (-> secure-site-defaults
                   (assoc :cookies false)
                   (assoc :session {})
                   (assoc :proxy true)
                   (assoc-in [:static :resources] "/")
                   (assoc-in [:security :hsts] false)
                   (assoc-in [:security :ssl-redirect] false)))
             (gzip/wrap-gzip)))
