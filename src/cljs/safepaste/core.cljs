(ns safepaste.core
  (:require [safepaste.remote :as remote]
            [safepaste.dom :as dom]
            [dommy.core :as dommy :refer-macros [sel1]]))

(enable-console-print!)

(def about-post
  "/a828b669#cfc9d7219aaebf10454b5ca196a99f7bac90e4e44fb829bc552ccbb2cab72d4b")
(def donate-post
  "/99c14048#623e8078679c1d53d141bab759380e2fbc4689c95861971c5d5c2517516b96aa")

(defn view-post!
  "Puts the UI in readonly mode and changes the URL to load the post in place."
  [path]
  (dom/set-url! path)
  (dom/update-inputs!)
  (remote/get!))

(defn onload [e]
  (remote/login!)
  (dom/update-inputs!)
  (if (dom/viewing?)
    (view-post! (+ js/window.location.pathname js/window.location.hash))
    (dom/reset-status!))
  (dommy/listen! (sel1 :#new) :click dom/reset-page!)
  (dommy/listen! (sel1 :#about) :click (fn [_] (view-post! about-post)))
  (dommy/listen! (sel1 :#donate) :click (fn [_] (view-post! donate-post)))
  (dommy/listen! (sel1 :#post) :click remote/post!))

; XXX: There's an issue with dommy which breaks this when :advanced
; optimizations are enabled. It seems to be affecting only :load.
; To resolve it, we'll just use a more primitive approach for now.
; https://github.com/plumatic/dommy/issues/101
;(dommy/listen! (sel1 :body) :load onload)

(aset js/window "onload" onload)
