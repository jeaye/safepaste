(ns com.jeaye.safepaste.core
  (:require [com.jeaye.safepaste.remote :as remote]
            [com.jeaye.safepaste.dom :as dom]
            [cljs-promises.async]
            [dommy.core :as dommy :refer-macros [sel1]]))

(enable-console-print!)

(cljs-promises.async/extend-promises-as-pair-channels!)

(def about-paste
  "/14e3c3ce#j57yDcJIVkDgsoz6bP2oxT9PWUjz2xaiAhnoGQssz1Y.")

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
  ;(dommy/listen! (sel1 :#about) :click (fn [_] (view-paste! about-paste)))
  (dommy/listen! (sel1 :#paste) :click remote/paste!))

(aset js/window "onload" onload)
