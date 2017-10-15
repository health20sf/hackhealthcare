(ns app.state
  (:require-macros
   [cljs.core.async.macros
    :refer [go go-loop]])
  (:require
   [cljs.core.async :as async
    :refer [<!]]
   [cljs-http.client :as http]
   [taoensso.timbre :as timbre]
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [sdk.facebook.fbme :as fbme]))

(defn init [& [initial]]

  (timbre/debug "[INIT]")

  (rf/reg-event-db
   :initialize
   (fn [db _] initial))

  (rf/reg-event-db
   :schedule
   (fn [db [_ time]]
     (timbre/debug "click schedule")
     (http/get (str "/schedule?time=" time) {})
     (fbme/close-browser)
     (assoc db :schedule true)))

  (rf/reg-sub
   :state
   (fn [db [_ & [path]]]
     (get-in db path)))

  (rf/reg-event-db
   :state
   (fn [db [_ path value]]
     (assoc-in db path value)))

  (rf/reg-sub
   :user
   (fn [db _] (:user db)))

  (rf/reg-event-db
   :page
   (fn [db [_ value & [user]]]
     (assoc db :page value
               :user (keyword (str user)))))

  (rf/reg-sub
   :page
   (fn [db _] (:page db))))
