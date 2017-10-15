(ns app.lib
  (:require-macros
   [cljs.core.async.macros :as m
    :refer [go go-loop alt!]])
  (:require
   [cljs.nodejs :as nodejs]
   [cljs.core.async :as async
    :refer [chan close! <! timeout put!]]))

(defn process [f ch]
  (go-loop []
    (when-let [item (<! ch)]
      (f item)
      (recur))))

(defn echo
  ([ch]
   (process println ch))
  ([label ch]
   (process #(println label %) ch)))
