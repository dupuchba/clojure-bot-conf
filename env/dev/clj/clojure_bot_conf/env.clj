(ns clojure-bot-conf.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [clojure-bot-conf.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[clojure-bot-conf started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[clojure-bot-conf has shut down successfully]=-"))
   :middleware wrap-dev})
