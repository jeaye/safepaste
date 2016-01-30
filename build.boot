(set-env!
  :source-paths #{"src/clj" "src/cljs" "src/js"}

  ; TODO: harden everything!
  ;   CSP for XSS protection
  ; TODO: prevent just anyone from using the api?
  ; TODO: minify crypto-js
  :dependencies '[[org.clojure/clojure "1.8.0"]
                  [org.clojure/clojurescript "1.7.228"]

                  ; Front end
                  [adzerk/boot-cljs "1.7.228-1"] ; CLJS compiler
                  [adzerk/boot-reload "0.4.4"] ; Automatic reloading
                  [cljs-http "0.1.39"] ; Communication with back end
                  [prismatic/dommy "1.1.0"] ; DOM events

                  ; REPL
                  [adzerk/boot-cljs-repl "0.3.0"]
                  [com.cemerick/piggieback "0.2.1"]
                  [weasel "0.7.0"]
                  [org.clojure/tools.nrepl "0.2.12"]

                  ; Back end
                  [pandeiro/boot-http "0.7.0"] ; HTTP server
                  [compojure "1.4.0"] ; Routing
                  [org.clojure/data.json "0.2.6"] ; Reading client json
                  [buddy/buddy-core "0.9.0"] ; Encryption
                  [hiccup "1.0.5"] ; HTML generation
                  [garden "1.3.0"] ; CSS generation

                  ; HTTP
                  [ring/ring-core "1.4.0"]
                  [ring/ring-servlet "1.4.0"]
                  [ring/ring-defaults "0.1.5"]])

(require '[safepaste.core]
         '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])

(deftask dev
  "Start dev environment"
  []
  (comp
    (serve :handler 'safepaste.core/app
           :reload true
           :resource-root "target")
    (watch)
    ;(reload)
    ;(cljs-repl) ; Before cljs task
    (cljs :compiler-options {:optimizations :none})
    (target :dir #{"target"})))
