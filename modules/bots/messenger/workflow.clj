(ns messenger.workflow)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Objectif à rappeler => savoir faire un button à la sortie de la conférence !

;; Internals
(defprotocol IParse
  (parse [this]))

(defprotocol IConversation
  (get-default-message [this])
  (get-message [this msg-id]))

(defprotocol IMessage
  (default-message? [this]))

;; Storage protocol
(defprotocol IUserStorage
  "Protocol used to manage user's informations"
  (new-user? [this ^User user] "Return true if user already exist in a given storage, false otherwise.")
  (save-user! [this ^User user] "Adds user to the storage and returns it."))

(defprotocol IConversationStorage
  "Protocol that manages Conversations storage"
  (save-conversation! [this ^User user] [this ^User user ^BotMessage msg input])
  (get-conversation [this ^User user])
  (has-conversation? [this ^User user])
  (get-current-state [this ^User user]))

(declare bot-message? bot-conversation?)

(defrecord BotMessage [id action next-route valid error default]
  IMessage
  (default-message? [this]
    (true? default))
  IParse
  (parse [this]
    (hash-map id (hash-map :action action :next-route next-route :valid valid :error error :default default))))

(defrecord BotConversation [id elements]
  IConversation
  (get-message [this msg-id]
    (if-let [message
             (some #(when (= msg-id (key %)) %) (parse this))]
      (let [{:keys [id action next-route valid error default]} (into {:id (first message)} (second message))]
        (->BotMessage id action next-route valid error default))))
  (get-default-message [this]
    (let [message
          (some #(when (get (val %) :default false)
                   %) (parse this))]
      (if message
        (let [{:keys [id action next-route valid error default]} (into {:id (first message)} (second message))]
          (->BotMessage id action next-route valid error default)))))
  IParse
  (parse [this]
    (let [result
          (cond
            (bot-message? elements) (parse elements)
            (bot-conversation? elements) (loop [[first & rest] (:elements elements)
                                                res {}]
                                           (if (empty? rest)
                                             (merge res (parse first))
                                             (recur rest (merge res (parse first)))))
            (coll? elements) (loop [[first & rest] elements
                                    res {}]
                               (if (empty? rest)
                                 (merge res (parse first))
                                 (recur rest (merge res (parse first))))))]
      result)))

(defn bot-message? [bot-msg]
  (instance? BotMessage bot-msg))

(defn bot-conversation? [bot-conv]
  (instance? BotConversation bot-conv))

(defrecord User [id])

;; Serving examples
(defrecord ConversationAtomStorage [atom])
(extend-type ConversationAtomStorage
  IConversationStorage
  (get-conversation [this ^User user]
    (some (fn [coll]
            (let [id (-> (:id user) hash Math/abs str keyword)]
              (when (= id (key coll))
                (peek coll))))
          (deref (.-atom this))))
  (has-conversation? [this ^User user]
    (let [id (-> (:id user) hash Math/abs str keyword)]
      (contains? (deref (.-atom this)) id)))
  (save-conversation!
    ([this ^User user]
     (swap! (.-atom this)
            assoc
            (-> (:id user) hash Math/abs str keyword)
            []))
    ([this ^User user ^BotMessage msg input]
     (when-not (has-conversation? this user)
       (save-conversation! this user))
     (swap! (.-atom this)
            update
            (-> (:id user) hash Math/abs str keyword)
            conj
            {(:id msg) input})))
  (get-current-state [this ^User user]
    (when (has-conversation? this user)
      (let [conversation (get-conversation this user)]
        (apply key (peek conversation))))))

(defrecord UserAtomStorage [atom])
(extend-type UserAtomStorage
  IUserStorage
  (new-user? [this ^User user]
    (if (some #(= user %) (deref (.-atom this)))
      false
      true))
  (save-user! [this ^User user]
    (swap! (.-atom this) conj user))
  (get-user [this id]
    (some (fn [user]
            (when (= id (:id user))
              user))
          (deref (.-atom this)))))

;; Workflow method
(defn make-message [id action next-route valid error & opts]
  (let [{:keys [default] :or {default false}} opts]
    (->BotMessage id action next-route valid error default)))

(defn make-conversation [id elements]
  (->BotConversation id elements))

(defn make-user [id]
  (->User id))

(defmulti get-user
          "Build a user from the entry based on the platform type."
          (fn [entry]
            (cond
              (not (nil? (get-in entry [:sender :id]))) :facebook))
          :default :platform-not-supported)

(defmethod get-user :facebook
  [entry]
  (make-user (get-in entry [:sender :id])))

(defmethod get-user :platform-not-supported
  [entry]
  (throw (Throwable. "This is some shit dude !")))

;; If conv-storage satis
(defn
  workflow!
  [^BotConversation conversation conv-storage user-storage input]
  (if (and (satisfies? IConversationStorage conv-storage)
           (satisfies? IUserStorage user-storage))
    (let [user (get-user input)]
      (when (new-user? user-storage user)
        (save-user! user-storage user))
      (if-let [user-conversation (get-conversation conv-storage user)]
        (let [msg-id (get-current-state conv-storage user)
              bot-msg (get (parse conversation) msg-id)
              valid-fn (:valid bot-msg)]
          (if (valid-fn input)
            (let [next-state-fn (:next-route bot-msg)
                  next-msg-id (next-state-fn input)
                  next-bot-msg (get-message conversation next-msg-id)
                  action-fn (:action next-bot-msg)]
              (println "NOT DOOOOOOOOO")
              (action-fn input)
              (save-conversation! conv-storage user next-bot-msg input))
            (let [error-fn (:error bot-msg)]
              (error-fn input)
              (save-conversation! conv-storage user bot-msg input))))
        (do
          (println "DOOOOOOOOO")
          (save-conversation! conv-storage user (get-default-message conversation) input)
          (workflow! conversation conv-storage user-storage input))))))


