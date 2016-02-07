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
                  [ring/ring-jetty-adapter "1.4.0"] ; HTTP in jar
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

(def target-dir "target/")

(require '[safepaste core api]
         '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[me.raynes.fs :as fs]
         '[clojure.java
           [shell :as shell]
           [io :as io]])

(deftask minify
  "Minify the compiled JS"
  []
  ; This is an awful hack which brings in an npm package to do the job.
  ; Oddly enough, its mangling and minifying shaves 20% off Closure's
  ; advanced compilation. So... huge wins. I'm ok with this.
  (fn [next-task]
    (fn [fileset]
      (let [tmp (tmp-dir!)
            old-file (tmp-get fileset "js/main.js")
            old-file-path (-> old-file tmp-file .getPath)
            new-file (io/file tmp "js/main.min.js")
            new-file-path (.getPath new-file)
            node-modules "./node_modules"]
        (try
          (io/make-parents new-file)
          (println)
          (when (not (fs/exists? node-modules))
            (println "Installing uglify-js...")
            (shell/sh "npm" "install" "uglify-js"))

          (println "Minifying JS...")
          (shell/sh (str node-modules "/uglify-js/bin/uglifyjs")
                    old-file-path
                    "--screw-ie8"
                    "-c" "-m"
                    "-o" new-file-path)

          (let [original-size (fs/size old-file-path)
                new-size (fs/size new-file-path)]
            (println
              (format "Shaved off %.2f%%\n"
                      (float (* 100 (- 1 (/ new-size original-size)))))))
          (catch Exception _
            (println "npm isn't working; not minifying...")
            (fs/copy old-file-path new-file-path)))
        (next-task (-> fileset
                       (add-resource tmp)
                       (rm [old-file])
                       commit!))))))

(deftask dev
  "Start dev environment"
  []
  (doseq [dir [safepaste.api/output-dir target-dir]]
    (fs/mkdir dir))
  (comp
    (serve :handler 'safepaste.core/app
           :reload true
           :resource-root target-dir)
    (watch)
    (cljs :compiler-options {:optimizations :none})
    (minify)
    (target :dir #{target-dir})))

(deftask build []
  (comp
    (cljs :compiler-options {:optimizations :advanced})
    (minify)
    (aot :namespace '#{safepaste.core})
    (pom :project 'safepaste
         :version "0.1.0")
    (uber)
    (jar :main 'safepaste.core
          :manifest {"Description" "TODO"
                     "Url" "https://github.com/jeaye/safepaste"}
         :file "safepaste.jar")
    (target :dir #{target-dir})))
