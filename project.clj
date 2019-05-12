(defproject com.jeaye/safepaste "0.1.0"
  :description ""
  :url ""
  :license {:name "Jank license"
            :url "https://upload.jeaye.com/jank-license"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [org.clojure/core.async  "0.4.490"]

                 ; Front end
                 [cljs-http "0.1.46"] ; Communication with back end
                 [prismatic/dommy "1.1.0"] ; DOM events

                 ; Back end
                 [ring/ring-jetty-adapter "1.7.1"] ; HTTP in jar
                 [compojure "1.6.1"] ; Routing
                 [org.clojure/data.json "0.2.6"] ; Reading client json
                 [buddy/buddy-core "0.9.0"] ; Encryption utils
                 [hiccup "1.0.5"] ; HTML generation
                 [garden "1.3.9"] ; CSS generation
                 [me.raynes/fs "1.4.6"] ; Filesystem work
                 [bk/ring-gzip "0.3.0"] ; On-the-fly gzipping

                 ; HTTP
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [ring/ring-anti-forgery "1.3.0"]]

  :plugins [[lein-figwheel "0.5.18"]
            [lein-cljsbuild "1.1.7" :exclusions [org.clojure/clojure]]
            [lein-ring "0.12.5" :exclusions [org.clojure/clojure]]]

  ;; Ring
  :ring {:handler safepaste.core/app
         ;:stacktraces? false
         :auto-reload? true}

  :cljsbuild {:builds {:base {:source-paths ["src/cljs/"
                                             "lib/cljs-promises/src/"]
                              :compiler {:asset-path "js/out/"
                                         :output-to "resources/public/js/safepaste.js"
                                         :output-dir "resources/public/js/out"
                                         :pretty-print false
                                         :elide-asserts false
                                         :parallel-build true
                                         :npm-deps false}}}}

  :clean-targets ^{:protect false} ["resources/public/js/"
                                    :target-path]

  :source-paths ["src/clj/"]

  :profiles {:dev {:figwheel {:http-server-root "public/"
                              :css-dirs ["resources/public/css/"]}
                   :cljsbuild {:builds {:base {:source-paths ["dev/"]
                                               :figwheel {:on-jsload "com.jeaye.safepaste.core/on-js-reload"}
                                               :compiler {:main com.jeaye.safepaste.core
                                                          :source-map-timestamp true
                                                          :closure-defines {"goog.DEBUG" false}}}}}}
             :prod {:cljsbuild {:builds {:base {:compiler {:infer-externs true
                                                           :static-fns true
                                                           :optimize-constants true
                                                           :optimizations :advanced
                                                           :pseudo-names false ; false for smallest build
                                                           :global-vars {*warn-on-infer* true}
                                                           :closure-defines {"goog.DEBUG" false}}}}}}
             :uberjar {:aot :all}})
