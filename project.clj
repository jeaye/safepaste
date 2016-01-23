(defproject safepaste "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [buddy/buddy-core "0.9.0"]]
  :main ^:skip-aot safepaste.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
