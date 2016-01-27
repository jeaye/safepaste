(ns safepaste.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(enable-console-print!)

(println "sending...")

(go (let [response (<! (http/get "/test/meow"
                                 {:with-credentials? false}))]
      (prn response)))

(println "sent!")
