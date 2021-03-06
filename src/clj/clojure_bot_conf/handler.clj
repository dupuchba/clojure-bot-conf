(ns clojure-bot-conf.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [clojure-bot-conf.layout :refer [error-page]]
            [clojure-bot-conf.routes.callback :refer [callback-routes]]
            [compojure.route :as route]
            [clojure-bot-conf.env :refer [defaults]]
            [mount.core :as mount]
            [clojure-bot-conf.middleware :as middleware]
            [clojure.tools.logging :as log]
            [clojure-bot-conf.config :refer [env]]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (doseq [component (:started (mount/start))]
    (log/info component "started")))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (shutdown-agents)
  (log/info "clojure-bot-conf has shut down!"))

(def app-routes
  (routes
    (-> #'callback-routes
        (wrap-routes middleware/wrap-json))
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))


(def app (middleware/wrap-base #'app-routes))
