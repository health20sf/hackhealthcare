(ns sdk.facebook.fbme
  (:require-macros
    [cljs.core.async.macros :as m :refer [go go-loop alt!]])
  (:require
    [cljs.core.async :as async
       :refer [chan <! >! put! close! timeout]]
    [taoensso.timbre :as timbre]))

(defn express-get-handler [{:keys [facebook-token]}]
  ; see https://developers.facebook.com/docs/messenger-platform/guides/quick-start
  (fn [req res]
    (let [query (js->clj (.-query req))
          mode (get query "hub.mode")
          check-token #(assert (= facebook-token
                                  (get query "hub.verify_token")))]
      (timbre/info "[FLAREBOT] GET:" query (js->clj req))
      (case mode
        "subscribe" (do (check-token)
                      (timbre/debug "Validated webhook")
                      (.send res (get query "hub.challenge")))
        (do (timbre/error "Failed validation. Make sure the validation tokens match. mode:" mode)
          (.sendStatus res 403))))))

(defn express-post-handler [& [message-handler]]
  ; see https://developers.facebook.com/docs/messenger-platform/guides/quick-start
 (fn [req res]
  (timbre/debug "[FBME]" (.keys js/Object req))
  (let [data (.-body req)
        object (.-object data)]
    (timbre/debug "[FBME] post " object)
    (case object
      "page" (doseq [entry (.-entry data)]
               (let [id (.-id entry)
                     time (.-time entry)]
                 (timbre/debug "[FB] entry:" entry)
                 (doseq [event (.-messaging entry)]
                   (if (.-sender event)
                     (if message-handler
                       (message-handler (js->clj event :keywordize-keys true))
                       (timbre/warn "[FB] no handler for event"
                                    (js->clj event)))
                     (timbre/warn "[FB] unknown event "
                                  (js->clj event))))))
      (timbre/warn "[FLAREBOT] unknown object "
                   (js->clj object)))
    (.sendStatus res 200))))

(defn fetch-user-id
      "get id to identify and authenticate the user and personalize the resulting experience"
      ;; https://developers.facebook.com/docs/messenger-platform/webview/userid
      ([] (fetch-user-id println))
      ([report]
       (let [out (chan)
             extract #(.-psid ^js/Facebook.Messenger %)]
         (.getUserID ^js/Facebook.Messenger js/MessengerExtensions
                     (fn success [uids]
                       (try
                         (put! out (extract uids))
                         (catch :default e
                           (report "Error:" (pr-str e))))
                       (close! out))
                     (fn [err]
                       (timbre/error err)
                       (close! out)))
         out)))

(defn close-browser []
  (.requestCloseBrowser ^js/Facebook.Messenger js/MessengerExtensions
                      (fn [& _]
                        (timbre/debug "Closed browser"))
                      (fn [& errors]
                        (timbre/error "Failed closing browser:" errors))))
