(ns server.core
  (:require-macros
   [cljs.core.async.macros :as m
    :refer [go go-loop alt!]])
  (:require
   [cljs.core.async :as async
    :refer [chan close! <! timeout put!]]
   [polyfill.compat]
   [cljs.nodejs :as nodejs]
   [taoensso.timbre :as timbre]
   [reagent.core :as reagent
    :refer [atom]]
   [app.core :as app
    :refer [static-page]]
   [sdk.facebook.fbme :as fbme]
   [sdk.facebook.messenger :as messenger]
   [sdk.slack :as slack]
   [server.fbhook :as fbhook]))

(enable-console-print!)

(def express (nodejs/require "express"))

(def body-parser (nodejs/require "body-parser"))

(def ^{:doc "used as verify token when setting up webhooksecret-facebook-token"}
     secret-facebook-token
    (aget js/process "env" "FACEBOOK_VERIFY_TOKEN"))

(when-not secret-facebook-token
  (timbre/warn "Need to set the FACEBOOK_VERIFY_TOKEN environment var"))

(def slack-api-token
  (or (aget js/process "env" "SLACK_API_TOKEN")
      (timbre/warn "Need to set SLACK_API_TOKEN environment var")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SLACK

#_
(slack/echo
 (slack/fetch-channels {:token slack-api-token}))

(def slack-channel
  {:general "C7JDWRXNX"})

(def schedule-channel {:token slack-api-token
                       :channel-id (slack-channel :general)})


#_
(slack/send-message "Hello"
                    {:token slack-api-token
                     :channel-id (slack-channel :general)})


(defn schedule-handler [req res]
  (timbre/debug "Schedule:" (.keys js/Object req)(js->clj req))
  (let [query (.-query req)
        time (.-time query)]
    (timbre/debug "Scheduling Appointment:" time)
    (slack/send-message (str "*Angela Martinez* is scheduled at "
                             time)
                        schedule-channel)
    (fbhook/send-appointment-message time)
    (.set res "Content-Type" "text/html")
    (.send res "OK")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn handler [req res]
  (if (= "https" (aget (.-headers req) "x-forwarded-proto"))
    (.redirect res (str "http://" (.get req "Host") (.-url req)))
    (go
      (.set res "Content-Type" "text/html")
      (.send res (<! (static-page))))))

(defn debug-redirect [req res]
   (let [local (aget js/process "env" "REDIRECT")]
     (when-not (empty? local)
       (timbre/debug "REDIRECT:" (str local (.-url req)))
       (.redirect res 307 (str local (.-url req)))
       true)))

(defn wrap-intercept [handler]
  (fn [req res]
    (timbre/debug "REDIRECT?")
    (or (debug-redirect req res)
        (handler req res))))

(defn server [port success]
  (doto (express)
    (.use (.urlencoded body-parser
                 #js {:extended false}))
    (.use (.json body-parser))
    (.get "/" handler)
    (.get "/fbme/webhook" (fbme/express-get-handler
                           {:facebook-token secret-facebook-token}))
    (.post "/fbme/webhook" (wrap-intercept fbhook/fbme-handler))
    (.get "/schedule" schedule-handler)
    (.use (.static express "resources/public"))
    (.listen port success)))

(defn -main [& mess]
  (assert (= (aget js/React "version")
             (aget (reagent.dom.server/module) "version")))
  (let [port (or (.-PORT (.-env js/process)) 1337)]
    (server port
            #(println (str "Server running at http://127.0.0.1:" port "/")))))

(set! *main-cli-fn* -main)
