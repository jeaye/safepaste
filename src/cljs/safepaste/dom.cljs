(ns safepaste.dom
  (:require [dommy.core :as dommy :refer-macros [sel1]]))

(def title js/window.title)

(defn set-url!
  "Uses an HTML5 feature to change the browser's URL without reloading."
  [path]
  (.replaceState js/window.history nil title path))

(defn viewing? []
  (not= "/" js/window.location.pathname))
(def editing? (comp not viewing?))

(defn status [key-id]
  (condp #(= %1 %2) key-id
    :editing "Your post will be encrypted using AES-256."
    :encrypting "Encrypting paste..."
    :uploading "Uploading paste..."
    :uploaded "Your encrypted paste has been uploaded."
    :uploaded-burn "Your encrypted paste will only be readable once."
    :downloading "Downloading paste..."
    :decrypting "Decrypting paste..."
    :viewing "This paste is encrypted for your eyes only."
    :viewing-burned "This paste is encrypted and will never be readable again."))

(defn error [key-id]
  (condp #(= %1 %2) key-id
    :invalid-key "Invalid secret key."
    :too-large "Post is too large."
    :unable-to-decrypt "Unable to decrypt."))

(defn set-status! [key-id]
  (let [item (sel1 :#status)]
    (dommy/set-text! item (status key-id))
    (dommy/remove-class! item :status-error)))

(defn reset-status! []
  (set-status! :editing))

; May take a string, as given by the server
(defn set-error! [error-key-or-msg]
  (let [item (sel1 :#status)]
    (dommy/set-text!
      item
      (if (keyword? error-key-or-msg)
        (error error-key-or-msg)
        error-key-or-msg))
    (dommy/add-class! item :status-error)))

(defn update-inputs!
  "Updates the UI to enable or disable input, based on viewing mode."
  []
  (let [input (sel1 :#input)
        expiry (sel1 :#expiry)]
    (if (viewing?)
      (do
        (dommy/set-attr! input :readonly)
        (dommy/set-attr! expiry :style "display:none;"))
      (do
        (dommy/remove-attr! input :readonly)
        (dommy/remove-attr! expiry :style)))))

(defn reset-input! []
  (dommy/set-value! (sel1 :#input) "")
  (update-inputs!))

(defn reset-page! [e]
  (set-url! "/")
  (reset-input!)
  (reset-status!))
