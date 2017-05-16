(defproject clojure-bot-conf "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[bouncer "1.0.0"]
                 [ch.qos.logback/logback-classic "1.1.7"]
                 [compojure "1.5.2"]
                 [cprop "0.1.10"]
                 [luminus-nrepl "0.1.4"]
                 [luminus/ring-ttl-session "0.3.1"]
                 [markdown-clj "0.9.92"]
                 [metosin/ring-http-response "0.8.1"]
                 [mount "0.1.11"]
                 [org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.webjars.bower/tether "1.4.0"]
                 [org.webjars/bootstrap "4.0.0-alpha.5"]
                 [org.webjars/font-awesome "4.7.0"]
                 [org.webjars/jquery "3.1.1"]
                 [ring-middleware-format "0.7.0"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-core "1.5.1"]
                 [ring/ring-defaults "0.2.2"]
                 [ring/ring-servlet "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [selmer "1.10.5"]

                 ;; custom libs for messenger
                 [clj-http "3.4.1"]
                 [cheshire "5.7.0"]
                 [org.clojure/core.async "0.2.395"]]

  :min-lein-version "2.0.0"

  :jvm-opts ["-server" "-Dconf=.lein-env"]
  :source-paths ["src/clj" "modules/bots"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main clojure-bot-conf.core

  :plugins [[lein-cprop "1.0.1"]
            [lein-kibit "0.1.2"]
            [lein-uberwar "0.2.0"]]
  :uberwar
  {:handler clojure-bot-conf.handler/app
   :init clojure-bot-conf.handler/init
   :destroy clojure-bot-conf.handler/destroy
   :name "clojure-bot-conf.war"}


  :profiles
  {:uberjar {:omit-source true
             :aot :all
             :uberjar-name "clojure-bot-conf.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:dependencies [[prone "1.1.4"]
                                 [ring/ring-mock "0.3.0"]
                                 [ring/ring-devel "1.5.1"]
                                 [luminus-http-kit "0.1.4"]
                                 [pjstadig/humane-test-output "0.8.1"]
                                 [directory-naming/naming-java "0.8"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.18.1"]]

                  :source-paths ["env/dev/clj" "test/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:resource-paths ["env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})
