(ns safepaste.core
  (:require [safepaste.remote :as remote]
            [safepaste.dom :as dom]
            [dommy.core :as dommy :refer-macros [sel1]]))

(enable-console-print!)

(def about-post
  "/about---#439d738e6e11e2583c9d2414541d7cae6b612a524c4af930a4fdedaeabccd31b")
(def donate-post
  "/donate--#6930b5545b073d1345d03cc6adf5409d58ba9ecc49b5b4fabb72fcaed0c8c1e8")

(defn view-post! [path]
  (dom/set-url! path)
  (dom/update-input!)
  (remote/get!))

(defn onload [e]
  (dom/update-input!)
  (when (dom/viewing?)
    (remote/get!))
  (dommy/listen! (sel1 :#new) :click dom/reset-page!)
  (dommy/listen! (sel1 :#about) :click (fn [_] (view-post! about-post)))
  (dommy/listen! (sel1 :#donate) :click (fn [_] (view-post! donate-post)))
  (dommy/listen! (sel1 :#post) :click remote/post!))

; TODO: listen to browser back/forward and refresh everything
(dommy/listen! js/window :load onload)
