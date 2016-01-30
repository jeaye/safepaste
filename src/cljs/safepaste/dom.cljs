(ns safepaste.dom
  (:require [dommy.core :as dommy :refer-macros [sel1]]))

(def title js/window.title)

(defn set-url! [path]
  (.replaceState js/window.history nil title path))

(defn viewing? []
  (not= "/" js/window.location.pathname))

(def editing? (comp not viewing?))

(defn update-input! []
  (let [input (sel1 :#input)]
    (if (viewing?)
      (dommy/set-attr! input :readonly)
      (dommy/remove-attr! input :readonly))))

(defn reset-input! []
  (dommy/set-value! (sel1 :#input) "")
  (update-input!))

(defn reset-page! [e]
  (set-url! "/")
  (reset-input!))
