(defproject com.jeaye/safepaste "0.1.0"
  :description ""
  :url ""
  :license {:name "Jank license"
            :url "https://upload.jeaye.com/jank-license"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.191"]
                 [org.clojure/core.async  "0.4.474"]

                 ; Front end
                 [cljs-http "0.1.44"] ; Communication with back end
                 [prismatic/dommy "1.1.0"] ; DOM events

                 ; Back end
                 [ring/ring-jetty-adapter "1.6.3"] ; HTTP in jar
                 [compojure "1.6.0"] ; Routing
                 [org.clojure/data.json "0.2.6"] ; Reading client json
                 [buddy/buddy-core "0.9.0"] ; Encryption
                 [hiccup "1.0.5"] ; HTML generation
                 [garden "1.3.4"] ; CSS generation
                 [me.raynes/fs "1.4.6"] ; Filesystem work
                 [bk/ring-gzip "0.3.0"] ; On-the-fly gzipping

                 ; HTTP
                 [ring/ring-core "1.6.3"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [ring/ring-anti-forgery "1.2.0"]]

  :plugins [[lein-figwheel "0.5.15"]
            [lein-cljsbuild "1.1.7" :exclusions [org.clojure/clojure]]
            [lein-ring "0.12.3" :exclusions [org.clojure/clojure]]]

  :source-paths ["src/clj/"]

  ;; Ring
  :ring {:handler safepaste.core/app
         ;:stacktraces? false
         :auto-reload? true}

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljs/"
                               "lib/cljs-promises/src/"]

                ;; The presence of a :figwheel configuration here
                ;; will cause figwheel to inject the figwheel client
                ;; into your build
                :figwheel {:on-jsload "com.jeaye.safepaste.core/on-js-reload"}

                :compiler {:main com.jeaye.safepaste.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/safepaste.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           ;; To console.log CLJS data-structures make sure you enable devtools in Chrome
                           ;; https://github.com/binaryage/cljs-devtools
                           :preloads [devtools.preload]}}
               ;; This next build is a compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src/cljs/"]
                :compiler {:output-to "resources/public/js/compiled/safepaste.js"
                           :main com.jeaye.safepaste.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             :server-port 3455
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.9"]
                                  [figwheel-sidecar "0.5.15"]
                                  [com.cemerick/piggieback "0.2.2"]]
                   :source-paths ["src/cljs/" "dev"]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})
