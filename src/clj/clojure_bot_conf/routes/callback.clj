(ns clojure-bot-conf.routes.callback
  (:require [clojure-bot-conf.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [clojure-bot-conf.config :refer [env]]
            [clojure.core.async :as a :refer [<! >! <!! >!! go thread chan]]
            [messenger.workflow :as w :refer [make-message make-conversation workflow!]]))

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

(defn webhook-router
  "Given facebook entries, create a thread which dispatch message to their
  actions."
  [out entries]
  (let [in (a/to-chan (:entry entries))]
    (thread
      (try
        (when-some [val (<!! in)]
          (println "Here is some data !!!" val)
          (>!! out val))
        (catch Throwable ex
          (println "Manage exception here !!!" ex)))))
  (response/ok))

;; Incoming HTTP requests -> Thread manage the reading and stack all the data in a channel
;; -> a go channel manage the input and execute the workflow method
;; -> maybe stuff to do

(defonce out (chan (a/sliding-buffer 1024)))

(def webhook (partial webhook-router out))

(defroutes callback-routes
           (GET "/callback" {params :query-params} (validate-webhook-token params))
           (POST "/callback" {params :body} (webhook params)))

;; Example
(def mess1 (make-message
             :mess1
             (fn [input]
               (println "mess 1"))
             (fn [input]
               (println "called")
               :mess2)
             (fn [input]
               true)
             (fn [input]
               (println "error 1"))))

(def mess2 (make-message
             :mess2
             (fn [input]
               (println "mess 2"))
             (fn [input]
               :mess1)
             (fn [input]
               true)
             (fn [input]
               (println "error 2"))
             :default true))

(def conv (make-conversation :conv1 [mess1 mess2]))

(def conv-storage (w/->ConversationAtomStorage (atom {})))

(def user-storage (w/->UserAtomStorage (atom [])))
;; end of Example

(go (loop []
      (when-some [entry (<! out)]
        (workflow! conv :1 conv-storage user-storage entry))
      (recur)))
