(ns safepaste.core
  (:require [safepaste.remote :as remote]
            [safepaste.dom :as dom]
            [dommy.core :as dommy :refer-macros [sel1]]))

(enable-console-print!)

(def about-post
  "/about---#c92f48c6631086b2c193556ac639c186d195e029b0569cdcca49dd31b2a54ffa")
(def donate-post
  "/donate--#634397c795e76b77627a356f5eb5d92e336ce1dc552f0c7bb28e2b306891f24f")

(defn view-post! [path]
  (dom/set-url! path)
  (dom/update-input!)
  (remote/get!))

(defn onload [e]
  (dom/update-input!)
  (if (dom/viewing?)
    (view-post! (+ js/window.location.pathname js/window.location.hash))
    (dom/reset-status!))
  (dommy/listen! (sel1 :#new) :click dom/reset-page!)
  (dommy/listen! (sel1 :#about) :click (fn [_] (view-post! about-post)))
  (dommy/listen! (sel1 :#donate) :click (fn [_] (view-post! donate-post)))
  (dommy/listen! (sel1 :#post) :click remote/post!))

(dommy/listen! js/window :load onload)
