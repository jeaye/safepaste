(ns safepaste.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(enable-console-print!)

(println "hash:" (.substring js/window.location.hash 1))

(println "sending...")

;(go (let [response (<! (http/get "/api/42"))]
;      (prn response)))

; TODO: Read text box; encrypt data
(go (let [post-reply (<! (http/post "/api/new"
                                    {:json-params {:data "meow"}}))]
      ; TODO: Verify json; show url
      (go (let [get-reply (<! (http/get (str "/api/" (:body post-reply))))]
            (println (:body get-reply))))))

(println "sent!")
