(ns sdk.slack
  (:require-macros
   [cljs.core.async.macros :as m
    :refer [go go-loop alt!]])
  (:require
   [cljs.nodejs :as nodejs]
   [cljs.core.async :as async
    :refer [chan close! timeout put! <!]]
   [cljs-http.client :as http]
   [taoensso.timbre :as timbre]))

(enable-console-print!)

(defn echo [in & [fn]]
  (go-loop []
    (when-let [msg (<! in)]
      (timbre/debug ((or fn identity) msg))
      (recur))))

(def debug false)

;; https://github.com/slackhq/node-slack-client

(defonce slack-client (nodejs/require "@slack/client"))

(defn errfn [out] ;; ##TODO: Eliminate
  (fn [err info] (put! out (or err info))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; WEB API
;; https://api.slack.com/web
;; for argument details see
;; https://github.com/slackhq/node-slack-client/blob/master/lib/clients/web/facets

(defn web-client
  ([{:keys [token]}]
   {:pre [(string? token)]}
   (let [client (.-WebClient slack-client)]
     (new client token #js {:logLevel "debug"}))))

(defn result-handler [out & [extract]]
  (fn [err result]
    (if-not err
      (when-let [value (js->clj ((or extract identity) result) :keywordize-keys true)]
        (put! out value))
      (timbre/error err))
    (close! out)))

(defn error-handler [out]
  (fn [err result]
    (timbre/debug "RES:" (js->clj result))
    (timbre/debug "ERR:" (js->clj err))
    (if (some? err)
      ; per the spec https://github.com/slackapi/node-slack-sdk
      (timbre/error err)
      ; for .setTopic err will be nil so handle result instead
      (if-not (.-ok result)
        (timbre/error (.-error result))
        (if-let [warning (.-warning result)]
          (timbre/warn warning))))
    (close! out)))

(defn fetch-response [bot call]
  {:pre [(:token bot)]}
  (let [out (chan)
        client (web-client bot)]
    (call client out)
    out))

(defn api-response [bot path & [args]]
  {:pre [(some? bot)]}
  (go-loop [url (str "https://slack.com/api/" path "?"
                     (http/generate-query-string
                      (assoc args :token (:token bot))))
            {:keys [body error-code] :as response}
            (<! (http/get url {:with-credentials? false
                               :timeout (* 15 1000)}))]
    (case error-code
      :no-error body
      :timeout (do (timbre/warn "Timeout retriving" url) nil)
      (do (timbre/warn "Retrieve json failed with code"
                       error-code url)
        nil))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PERMISSIONS

(defn fetch-permissions-info [bot]
  {:pre [(some? bot)]}
  (api-response bot "apps.permissions.info"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CHANNELS

(defn fetch-channels
  "Group channels only"
  ([bot]
   {:pre [(some? bot)]}
   (fetch-response bot
    (fn [client out]
      (.list (.-channels client) 1 (result-handler out #(.-channels %)))))))

(defn fetch-multiparty-channels [bot]
  (fetch-response bot
   (fn [client out]
     (.list (.-mpim client) (result-handler out #(.-groups %))))))

(defn fetch-private-channels [bot]
  (fetch-response bot
   (fn [client out]
    (.list (.-groups client) 1 (result-handler out #(.-groups %))))))

(defn %fetch-history [interface out id]
  ;; ## split out the message streaming to make this more like the others?
  (let [batch 10
        control (async/merge [(async/to-chan [{:latest nil}])  ;; ## TODO: use spool ?
                              (chan)])
        fetch (fn [{:keys [latest]} result]
                #_(timbre/debug "fetch history:" latest)
                (.history interface id #js {:count batch
                                            :latest latest}
                          (fn [err info]
                            (if-not err
                              (go
                                (<! (async/onto-chan result (map #(js->clj % :keywordize-keys true)
                                                                 (.-messages info))))
                                (if (.-has_more info)
                                  (put! control {:latest (.-ts (last (.-messages info)))})
                                  (close! control)))
                              (do
                                (timbre/error "[SLACK]err:" err)
                                (close! result)
                                (close! control)))))
                result)]
    ;; Ensures result is in original order and is fetched on demand only:
    (async/pipeline-async 1 out fetch control)))

(defn fetch-channel-history
  ([bot channel-id]
   (fetch-response bot
                   (fn [client out]
                     (%fetch-history (.-channels client) out channel-id)))))

(defn fetch-channel-info [bot channel-id]
  (fetch-response bot
   (fn [client out]
     (.info (.-channels client) channel-id
            (result-handler out #(.-channel %))))))

(defn create-channel [bot name]
  (->> (fn [client out]
         (.create (.-channels client) name false
                  (result-handler out #(.-channel %))))
       (fetch-response bot)))

(defn join-channel [bot name]
  {:pre [(some? bot)]}
  (api-response bot "channels.create" {:name name}))

(defn set-topic!
   ([bot channel topic]
    {:pre [(some? bot)]}
    (->> (fn [client out]
           (.setTopic (.-channels client) channel topic (error-handler out)))
         (fetch-response bot))))

(defn set-purpose!
   ([bot channel purpose]
    {:pre [(some? bot)]}
    (->> (fn [client out]
           (.setPurpose (.-channels client) channel purpose (error-handler out)))
         (fetch-response bot))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FILES

(defn files-upload [bot {:keys [content filename filetype title channels] :as args}]
  {:pre [content]}
  (fetch-response bot
   (fn [client out]
     (.upload (.-files client) (or filename "Untitled")
              (clj->js args)
              (result-handler out #(.-file %))))))

#_
(echo (files-upload {:content "hello" :filename "u"}))

(defn fetch-files [bot & [{:keys [user channel]}]]
  ;; Cannot be called by bot users so what's the point?
  ;; TODO: implement remaining args
  (fetch-response bot
   (fn [client out]
     (let [control (async/merge [(async/to-chan [{:page 1}])  ;; ## TODO: use spool ?
                                 (chan)])
           fetch (fn [{:keys [page]} result]
                   (.list (.-files client)
                          #js {:user user
                               :channel channel
                               :page page}
                          (fn [err info]
                            (if (and (not err) (:ok info))
                              (go
                                (<! (async/onto-chan result #_(map #(js->clj % :keywordize-keys true)
                                                                (.-files info))
                                                            (vector (js->clj info :keywordize-keys true))))
                                (close! control))
                              (do
                                (timbre/error (or err (.-error info) "unknown error"))
                                (close! result)
                                (close! control))))))]
        (async/pipeline-async 1 out fetch control)))))

;;; IM

(defn fetch-direct-channels [bot]
  (fetch-response bot
   (fn [client out]
     (.list (.-im client) (result-handler out #(.-ims %))))))

(defn fetch-im-history
  ([{:keys [token channel-id] :as channel}]
   ;; channel id starting with "D"
   (fetch-response channel
                   (fn [client out]
                     (%fetch-history (.-im client) out channel-id)))))

;;; TEAM

(defn fetch-team-info [bot]
  (let [out (chan)
        client (web-client bot)]
    (.info (.-team client) (errfn out))
    out))

;; USERS

(defn fetch-user-info [bot user-id]
  (let [out (chan)
        client (web-client bot)]
    (.info (.-users client) user-id
           (result-handler out #(.-user %)))
    out))

;; (echo (fetch-user-info "U0PA52X6W"))

(defn fetch-users [bot]
  (let [out (chan)
        client (web-client bot)]
    (.list (.-users client) 1
           (result-handler out #(.-members %)))
    out))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; RTM

(def CLIENT_EVENTS (.-CLIENT_EVENTS slack-client))

(defn rtm-client
  ([{:keys [token]}] ;; # memoize? better of just plain token?
   {:pre [(string? token)]}
   (let [client (.-RtmClient slack-client)
         rtm (new client token (if debug #js {:logLevel "debug"} #js {}))]
     (.on rtm (.-RTM_CONNECTION_OPENED (.-RTM CLIENT_EVENTS))
          (fn [rtmStartData]
            (timbre/info "Logged in to slack")))
     ;; ## slack recommends using .connect instead of .start
     ;;     but not working well yet per aug 2017 slack/client 3.11
     ;;     although required to use workspace token instead of bot token
     (.start rtm)
     rtm)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INCOMING

(def RTM_EVENTS (.-RTM_EVENTS slack-client))

(defn subscribe-chan [bot]
   (let [out (chan)
         rtm (rtm-client bot)]
     (.on rtm (.-MESSAGE RTM_EVENTS)
          (fn [message]
            (put! out (js->clj message :keywordize-keys true))))
     out))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; OUTGOING

(def RTM_CLIENT_EVENTS (.-RTM (.-CLIENT_EVENTS slack-client)))

(defn transmit-message ;; only call internally
  ([bot rtm message channel-id]
   (timbre/debug "transmit-message:" message channel-id)
   (let [out (chan)]
     (if (string? message)
       ;; message formatted according to https://api.slack.com/docs/message-formatting
       (.sendMessage rtm message channel-id
                     (fn [err msg]
                       (put! out (or err msg))
                       (if err
                         (timbre/error "[SLACK] Fail transmit" channel-id ":" err)
                         (timbre/debug "[SLACK] Sent" channel-id ":" msg))))
       ;; structure according to https://api.slack.com/methods/chat.postMessage
       (.postMessage (.-chat (web-client bot)) channel-id (:text message)
                     (clj->js message)
                     (result-handler out)))
     out)))

;; (.sendMessage rtm "hello" "C0P9SREKD" (fn [] nil))

(def transmit-chan
  (memoize
   (fn [token]
     (let [incoming (chan)
           rtm (rtm-client {:token token})]
       (.on rtm
            (.-RTM_CONNECTION_OPENED RTM_CLIENT_EVENTS)
            (fn []
              (timbre/debug "[SLACK] connected")
              (go-loop [i 0]
                (when-let [{:keys [message channel-id] :as msg}
                           (<! incoming)]
                  (let [result (<! (transmit-message {:token token} rtm message channel-id))]
                    (timbre/debug "[SLACK]response: " result))
                  (<! (timeout 1200)) ;; throttle - may no longer be needed?
                  (recur (inc i))))))
       incoming))))

(defn send-message
  ([message {:keys [token channel-id] :as target}]
   (put! (transmit-chan token)
         {:message message :channel-id channel-id})))

(defn publish-chan
  ([{:keys [token channel-id] :as target}]
   (let [in (chan)]
     (go-loop []
       (when-let [message (<! in)]
         (send-message message target)
         (recur)))
     in)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn echo-response [msg]
  (str "->" (pr-str msg)))

(defn test-bot [bot response]
  (let [in (subscribe-chan bot)
        out (publish-chan {:token (:token bot)
                           :channel-id (get-in bot [:channels :general])})]
    (go-loop []
      (when-let [msg (<! in)]
        (<! (timeout 2000))
        (when-let [reply (response msg)]
          (put! out reply))
        (recur)))))
