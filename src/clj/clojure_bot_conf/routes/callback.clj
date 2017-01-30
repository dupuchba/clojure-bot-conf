(ns clojure-bot-conf.routes.callback
  (:require [clojure-bot-conf.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [clojure-bot-conf.config :refer [env]]))

;; ========================== WebToken validation =============================
(defn validate-webhook-token
  "Validate query-params map according to user's defined webhook-token.
  Return hub.challenge if valid, error message else."
  [params]
  (println)
  (if (and (= (params "hub.mode") "subscribe")
           (= (params "hub.verify_token") (:webhooks-verify-token env)))
    (params "hub.challenge")
    (response/bad-request! "Verify token not valid")))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
           (GET "/callback" {params :query-params} (validate-webhook-token params))
  (GET "/about" [] (about-page)))

