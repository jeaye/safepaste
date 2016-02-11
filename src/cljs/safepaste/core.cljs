(ns safepaste.core
  (:require [safepaste.remote :as remote]
            [safepaste.dom :as dom]
            [dommy.core :as dommy :refer-macros [sel1]]))

(enable-console-print!)

(def about-paste
  "/14e3c3ce#371864573d2a445868316521cc3cc374413f3eb04978281c964527cdc79a64bb")
(def donate-paste
  "/f1a8f535#31bcdb56b77528a3c1b540bc460ed07d5b74fcf65eb91733bc4d10884e764caf")

(defn view-paste!
  "Puts the UI in readonly mode and changes the URL to load the paste in place."
  [path]
  (dom/set-url! path)
  (dom/update-inputs!)
  (remote/get!))

(defn fork!
  "Puts the UI in edit mode with the contents of the current paste."
  [e]
  (let [input (sel1 :#input)
        data (dommy/value input)]
    (dom/reset-page! e)
    (dommy/set-value! input data)))

(defn onload [e]
  (remote/login!)
  (dom/update-inputs!)
  (if (dom/viewing?)
    (view-paste! (+ js/window.location.pathname js/window.location.hash))
    (dom/reset-status!))
  (dommy/listen! (sel1 :#new) :click dom/reset-page!)
  (dommy/listen! (sel1 :#fork) :click fork!)
  (dommy/listen! (sel1 :#about) :click (fn [_] (view-paste! about-paste)))
  (dommy/listen! (sel1 :#donate) :click (fn [_] (view-paste! donate-paste)))
  (dommy/listen! (sel1 :#paste) :click remote/paste!))

; XXX: There's an issue with dommy which breaks this when :advanced
; optimizations are enabled. It seems to be affecting only :load.
; To resolve it, we'll just use a more primitive approach for now.
; https://github.com/plumatic/dommy/issues/101
;(dommy/listen! (sel1 :body) :load onload)

(aset js/window "onload" onload)
