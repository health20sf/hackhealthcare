(ns app.core
  (:require-macros
   [cljs.core.async.macros
    :refer [go go-loop]])
  (:require
   [cljs.core.async :as async
    :refer [<! chan close! alts! timeout put!]]
   [goog.dom :as dom]
   [goog.events :as events]
   [taoensso.timbre :as timbre]
   [reagent.core :as reagent
    :refer [atom]]
   [re-frame.core :as rf]
   [reagent.dom.server
    :refer [render-to-string]]
   [app.state :as state]
   [app.view.views
    :refer [view page html5]]))

(enable-console-print!)

(def scripts [{:src "/js/out/app.js"}
              "main_cljs_fn()"])

(def logo
  [:div.logo "c"])

(defn static-page []
    (let [out (chan 1)
          state {}]
        (timbre/debug "[STATIC]" state)
        (put! out
              (->  state
                   (page :scripts scripts
                         :title "Urban Rheuma Care"
                         :forkme false)
                   (render-to-string)
                   (html5)))
        out))

(defn activate []
  (timbre/debug "[ACTIVATE]")
  (state/init)
  (let [el (dom/getElement "canvas")
        mode (rf/subscribe [:page])
        state (rf/subscribe [:state])]
    (reagent/render [#(view @mode @state)] el)))
