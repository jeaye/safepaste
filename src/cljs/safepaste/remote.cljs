(ns safepaste.remote
  (:require [safepaste.dom :as dom]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [dommy.core :as dommy :refer-macros [sel1]]
            [crypto-js.aes])
  (:require-macros [cljs.core.async.macros :refer [go]]))

; Sane default; provided by the server during login
(def max-paste-bytes (atom (* 2 1024 1024)))
(def key-size 64)

; Unique session token required for pasting
(def csrf-token (atom ""))

(def error-from-status
  {400 :bad-request
   410 :invalid-id
   413 :too-large
   429 :banned
   503 :pasting-disabled})

(defn check-error!
  "Checks the :status code of the reply and updates the dom to convey
   errors. Returns whether or not an error was handled."
  [reply]
  (let [status (:status reply)
        error? (not= 200 status)]
    (when error?
      (dom/set-error! (error-from-status status :bad-request)))
    error?))

(defn login! []
  (go
    (let [reply (<! (http/get "/api/login"))
          reply-json (.parse js/JSON (:body reply))]
      (when (not (check-error! reply))
        (swap! csrf-token (fn [_] (get (:headers reply) "x-csrf-token")))
        (swap! max-paste-bytes (fn [_] (.-max reply-json)))))))

(defn paste! [e]
  (let [data (dommy/value (sel1 :#input))
        expiry (dommy/value (sel1 :#expiry))]
    (if (>= (count data) @max-paste-bytes)
      (dom/set-error! :too-large)
      (when (and (not-empty data) (not (dom/viewing?)))
        (dom/set-status! :encrypting)
        (let [safe-key (.toString (.random js/CryptoJS.lib.WordArray 32))
              encrypted (.encrypt js/CryptoJS.AES (str data "\n") safe-key)
              encoded (.toString encrypted)]
          (dom/set-status! :uploading)

          ; This is an async post; we won't block.
          (go
            (let [reply (<! (http/post "/api/new"
                                       {:json-params {:data encoded
                                                      :expiry expiry}
                                        :headers {"X-CSRF-Token" @csrf-token}}))]
              (when (not (check-error! reply))
                (let [reply-json (.parse js/JSON (:body reply))]
                  (dom/set-url! (str "/" (.-id reply-json) "#" safe-key))
                  (dom/update-inputs!)
                  (if (= expiry "burn")
                    (dom/set-status! :uploaded-burn)
                    (dom/set-status! :uploaded)))))))))))

(defn get! []
  (dom/set-status! :downloading)
  (let [id (.substring js/window.location.pathname 1)
        safe-key (.substring js/window.location.hash 1)]
    (if (not= key-size (count safe-key))
      (dom/set-error! :invalid-key)

      ; This is an async get; we won't block.
      (go
        (let [reply (<! (http/get (str "/api/" id)))]
          (when (not (check-error! reply))
            ; crytpo-js doesn't always throw, even when things are blatantly
            ; wrong. Given the same wrong data, it throws about 10% of the time.
            (try
              (dom/set-status! :decrypting)
              (let [reply-json (.parse js/JSON (:body reply))
                    decrypted (.decrypt js/CryptoJS.AES
                                        (.-data reply-json)
                                        safe-key)
                    decrypted-str (.toString decrypted js/CryptoJS.enc.Utf8)]
                (when (empty? decrypted-str)
                  (throw (js/Error.)))
                (dommy/set-value! (sel1 :#input) decrypted-str)
                (if (.-burned reply-json)
                  (dom/set-status! :viewing-burned)
                  (dom/set-status! :viewing)))
              (catch js/Error e
                (dom/set-error! :unable-to-decrypt)))))))))
