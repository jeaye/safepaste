(defproject safepaste "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [buddy/buddy-core "0.9.0"]

                 [compojure "1.4.0"]

                 [ring/ring-core "1.4.0" :exclusions [javax.servlet/servlet-api]]
                 [ring/ring-servlet "1.4.0" :exclusions [javax.servlet/servlet-api]]
                 [ring/ring-defaults "0.1.5" :exclusions [javax.servlet/servlet-api]]

                 [cc.qbits/jet "0.5.4"]]
  :main ^:skip-aot safepaste.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
