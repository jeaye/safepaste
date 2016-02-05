(set-env!
  :source-paths #{"src/clj" "src/cljs" "src/js"}

  :dependencies '[[org.clojure/clojure "1.8.0"]
                  [org.clojure/clojurescript "1.7.228"]

                  ; Front end
                  [adzerk/boot-cljs "1.7.228-1"] ; CLJS compiler
                  [adzerk/boot-reload "0.4.5"] ; Automatic reloading
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
                  [me.raynes/fs "1.4.6"] ; Filesystem work

                  ; HTTP
                  [ring/ring-core "1.4.0"]
                  [ring/ring-servlet "1.4.0"]
                  [ring/ring-defaults "0.1.5"]
                  [ring/ring-anti-forgery "1.0.0"]])

(require '[safepaste core api]
         '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[me.raynes.fs :as fs]
         '[clojure.java.shell :as shell])

(deftask minify
  "Minify the compiled JS"
  []
  (with-post-wrap fileset
    (when (not (fs/exists? "./node_modules"))
      (println "Installing uglify-js...")
      (shell/sh "npm" "install" "uglify-js"))

    (println "Minifying JS...")
    (shell/sh "./node_modules/uglify-js/bin/uglifyjs"
              "target/js/main.js"
              "--screw-ie8"
              "-c" "-m" "--stats"
              "-o" "target/js/main.min.js")))

(deftask dev
  "Start dev environment"
  []
  (fs/mkdir safepaste.api/output-dir)
  (comp
    (serve :handler 'safepaste.core/app
           :reload true
           :resource-root "target")
    (watch)
    (cljs :compiler-options {:optimizations :advanced})
    (target :dir #{"target"})
    (minify)))
