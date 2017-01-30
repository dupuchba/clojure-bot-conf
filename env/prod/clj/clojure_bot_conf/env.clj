(ns clojure-bot-conf.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[clojure-bot-conf started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[clojure-bot-conf has shut down successfully]=-"))
   :middleware identity})
