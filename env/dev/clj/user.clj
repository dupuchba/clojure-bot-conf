(ns user
  (:require [mount.core :as mount]
            clojure-bot-conf.core))

(defn start []
  (mount/start-without #'clojure-bot-conf.core/http-server
                       #'clojure-bot-conf.core/repl-server))

(defn stop []
  (mount/stop-except #'clojure-bot-conf.core/http-server
                     #'clojure-bot-conf.core/repl-server))

(defn restart []
  (stop)
  (start))


