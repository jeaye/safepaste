(ns safepaste.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(enable-console-print!)

(println "hash:" (.substring js/window.location.hash 1))

(println "sending...")

;(go (let [response (<! (http/get "/api/42"))]
;      (prn response)))

(go (let [response (<! (http/post "/api/new"
                                  {:json-params {:data "meow"}}))]
      (prn response)))

(println "sent!")
