(ns safepaste.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(enable-console-print!)

(println "sending...")

(go (let [response (<! (http/get "http://localhost:5050"
                                 {:with-credentials? false
                                  :query-params {"since" 135}}))]
      (prn response)))

(println "sent!")
