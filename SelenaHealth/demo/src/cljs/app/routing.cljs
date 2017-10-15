(ns app.routing
  (:require-macros
   [cljs.core.async.macros :as m
    :refer [go go-loop alt!]])
  (:require
   [cljs.core.async :as async
    :refer [chan close! <! timeout put!]]
   [goog.dom :as dom]
   [goog.events :as events]
   [goog.history.EventType :as EventType]
   [taoensso.timbre :as timbre]
   [re-frame.core :as rf]
   [secretary.core :as secretary
    :refer-macros [defroute]]
   [sdk.facebook.fbme :as fbme])
  (:import
   [goog History]))

(secretary/set-config! :prefix "#")

(def history
  (memoize #(History.)))

(defn navigate! [token & {:keys [stealth]}]
  (if stealth
    (secretary/dispatch! token)
    (.setToken (history) token)))

(defn hook-browser-navigation! []
  (doto (history)
    (events/listen EventType/NAVIGATE
                   (fn [event]
                     (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroute "/" []
  nil)

(defroute "/checkin" []
  (timbre/debug "CHECKIN")
  (go-loop [id (<! (fbme/fetch-user-id))]
    (timbre/debug "CHECKIN" id)
    (rf/dispatch [:page :checkin (str (or id "unknown"))])))

(defroute "/checkin/:id" [id]
  (timbre/debug "CHECKIN" id)
  (rf/dispatch [:page :checkin id]))
