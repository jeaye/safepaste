(set-env!
  :source-paths #{"src/clj" "src/cljs" "src/js"}

  ; TODO: Compress posts?
  ;   lz-string (takes 1s for 2MB)

  ; TODO: Use base85, not base64

  ; TODO: core.typed?

  ; TODO: harden everything!
  ;   CSP for XSS protection
  ; TODO: prevent just anyone from using the api?
  ; TODO: minify crypto-js
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
                  [cljsjs/boot-cljsjs "0.5.1"] ; Minification

                  ; HTTP
                  [ring/ring-core "1.4.0"]
                  [ring/ring-servlet "1.4.0"]
                  [ring/ring-defaults "0.1.5"]
                  [ring/ring-anti-forgery "1.0.0"]])

(require '[safepaste core api]
         '[boot.pod :as pod]
         '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[me.raynes.fs :as fs]
         '[cljsjs.boot-cljsjs.packaging :refer [minify]])

(defn minifier-pod []
  (pod/make-pod (assoc-in (get-env)
                          [:dependencies]
                          '[[asset-minifier "0.1.7"]])))

(defn minify [js-file]
  (pod/with-eval-in min-pod
    (require 'asset-minifier.core)
    (asset-minifier.core/minify-js ~in-path ~out-path {})))

(deftask minify-js
  "Minify all JS sources"
  []
  (with-pre-wrap fileset
    (loop [files (by-ext [".js"] (ls fileset))
           file (first files)
           new-fileset fileset]
      (if (some? file)
        (let [path (tmp-path file)
              out-path (str "js/main.out/" (.getName (tmp-file file)))]
          (println path "=>" out-path)
          (recur (rest files)
                 (first (rest files))
                 (minify :in path :out out-path)))
        new-fileset))))

(deftask dev
  "Start dev environment"
  []
  (fs/mkdir safepaste.api/output-dir)
  (comp
    (serve :handler 'safepaste.core/app
           :reload true
           :resource-root "target")
    (minify :in ".*inc.js" :out "js/main.out/")
    (watch)
    (cljs :compiler-options {:optimizations :none})
    (target :dir #{"target"})))
