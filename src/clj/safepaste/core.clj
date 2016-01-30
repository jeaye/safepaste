(ns safepaste.core
  (:gen-class)
  (:require [safepaste.home :as home]
            [safepaste.css :as css]

            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.response :refer [render]]
            [clojure.java.io :as io]
            [buddy.core.nonce :as nonce]

            [clojure.data.json :as json]
            [clojure.pprint :refer [pprint]]))

(defn hexify [b]
  (apply str (map #(format "%02x" %) b)))

; TODO: make sure this exists
(def output-dir "post/")

(defroutes app-routes
  (GET "/:id{(?:.{8})?}" [id] (partial home/render id))
  (GET "/api/:id" [id]
    ; TODO: Input validation
    (slurp (str output-dir id)))
  (POST "/api/new" {body :body}
    ; TODO: Input validation
    (let [id (hexify (nonce/random-bytes 4)) ; TODO: improve
          json-body (json/read-str (slurp body))]
      (spit (str output-dir id) (get json-body "data"))
      id))
  (route/files "/js" {:root "target/js"})
  ; TODO: Call into home with an error string (red)
  (route/not-found (partial home/render nil "Unknown paste.")))

(def app (wrap-defaults
           app-routes
           ; TODO: Turn this back on
           (assoc-in site-defaults [:security :anti-forgery] false)))
