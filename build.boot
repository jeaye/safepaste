(set-env!
  :source-paths #{"src/clj" "src/cljs" "src/js"}
  :resource-paths #{"html"}

  ; TODO: garden and hiccup
  ; TODO: harden everything!
  :dependencies '[[org.clojure/clojure "1.8.0"]
                  [org.clojure/clojurescript "1.7.228"]

                  ; Front end
                  [adzerk/boot-cljs "1.7.228-1"] ; CLJS compiler
                  [pandeiro/boot-http "0.7.0"] ; HTTP server
                  [adzerk/boot-reload "0.4.4"] ; Automatic reloading
                  [cljs-http "0.1.39"] ; Communication with back end
                  [cljsjs/boot-cljsjs "0.5.1"] ; JS minification

                  ; REPL
                  [adzerk/boot-cljs-repl "0.3.0"]
                  [com.cemerick/piggieback "0.2.1"]
                  [weasel "0.7.0"]
                  [org.clojure/tools.nrepl "0.2.12"]

                  ; Back end
                  [compojure "1.4.0"] ; Routing
                  [org.clojure/data.json "0.2.6"] ; Reading client json
                  [buddy/buddy-core "0.9.0"] ; Encryption

                  ; HTTP
                  [ring/ring-core "1.4.0"]
                  [ring/ring-servlet "1.4.0"]
                  [ring/ring-defaults "0.1.5"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[safepaste.core]
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
