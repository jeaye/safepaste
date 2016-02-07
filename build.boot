(set-env!
  :source-paths #{"src/clj" "src/cljs" "src/js"}

  :dependencies '[[org.clojure/clojure "1.8.0"]
                  [org.clojure/clojurescript "1.7.228"]

                  ; Front end
                  [adzerk/boot-cljs "1.7.228-1"] ; CLJS compiler
                  [cljs-http "0.1.39"] ; Communication with back end
                  [prismatic/dommy "1.1.0"] ; DOM events

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
         '[me.raynes.fs :as fs]
         '[clojure.java.shell :as shell])

(deftask minify
  "Minify the compiled JS"
  []
  ; This is an awful hack which brings in an npm package to do the job.
  ; Oddly enough, its mangling and minifying shaves 20% off Closure's
  ; advanced compilation. So... huge wins. I'm ok with this.
  (with-post-wrap fileset
    (let [old-file "target/js/main.js"
          new-file "target/js/main.min.js"
          node-modules "./node_modules"]
      (if (= 1 (:exit (shell/sh "npm" "--version")))
        (do
          (println "npm isn't installed; not minifying...")
          (fs/copy old-file new-file))
        (do
          (when (not (fs/exists? node-modules))
            (println "Installing uglify-js...")
            (shell/sh "npm" "install" "uglify-js"))

          (println "\nMinifying JS...")
          (shell/sh (str node-modules "/uglify-js/bin/uglifyjs")
                    old-file
                    "--screw-ie8"
                    "-c" "-m"
                    "-o" new-file)

          (let [original-size (fs/size old-file)
                new-size (fs/size new-file)]
            (println
              (format "Shaved off %.2f%%\n"
                      (float (* 100 (- 1 (/ new-size original-size))))))))))))

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
