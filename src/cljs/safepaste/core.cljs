(ns safepaste.core
  (:require [safepaste.remote :as remote]
            [safepaste.dom :as dom]
            [dommy.core :as dommy :refer-macros [sel1]]))

(enable-console-print!)

(defn onload [e]
  ; TODO: Setup other events: about/donate
  (dom/update-input!)
  (when (dom/viewing?)
    (remote/get!))
  (dommy/listen! (sel1 :#new) :click dom/reset-page!)
  (dommy/listen! (sel1 :#post) :click remote/post!))

; TODO: listen to browser back/forward and refresh everything
(dommy/listen! js/window :load onload)
