(ns server.fbhook
  (:require-macros
   [cljs.core.async.macros :as m
    :refer [go go-loop alt!]])
  (:require
   [cljs.core.async :as async
    :refer [chan close! <! timeout put!]]
   [cljs.nodejs :as nodejs]
   [cljs-http.client :as http]
   [taoensso.timbre :as timbre]
   [sdk.facebook.fbme :as fbme]
   [sdk.facebook.messenger :as messenger]
   [app.lib :as lib]))

(def fb-patient-id (aget js/process "env" "PATIENT_FACEBOOK_ID"))

(def fb-endpoint "https://graph.facebook.com/v2.9/")

(def fb-access-token (aget js/process "env" "FACEBOOK_ACCESS_TOKEN"))

(defn send-message [message]
  (http/post (str fb-endpoint "me/messages")
             {:query-params {:access_token fb-access-token}
              :json-params message}))

(defn extension-button-payload [text & {:keys [label]}]
  {:template_type "button"
   :text text
   :buttons [(messenger/url-button
              label
              :url "https://cococare.herokuapp.com/"
              :webview-height-ratio "tall"
              :messenger-extensions true)]})

(defn send-payload [recipient payload]
  (send-message
   {:recipient {:id recipient}
    :message {:attachment
               {:type "template"
                :payload payload}}}))

(defn send-schedule-message [target-id]
  (->> (extension-button-payload
        "Hi! According to our records, we suggest scheduling an appointment due to risk of a potential flare up."
        :label "Schedule Appointment")
    (send-payload target-id)))

#_
(lib/echo (send-schedule-message fb-patient-id))

(defn send-appointment-message [time]
  (->> time
   (str "Your appointment has been successfully scheduled at ")
   (messenger/send-text fb-patient-id)))

#_
(lib/echo (send-appintment-message "Tuesday 10 AM"))

(def fbme-handler
   (-> (fn [request]
         (timbre/debug "FBME->" request)
         (when-let [text (get-in request [:message :text])]
           (when-not (get-in request [:message :is_echo])
             (timbre/debug "FBME=>" text request)
             (send-schedule-message (messenger/get-sender-id request)))))
       fbme/express-post-handler))
