(ns app.start
  (:require
   [app.core :as app]
   [app.routing :as routing]))

(enable-console-print!)

(defn ^:export main []
  (app/activate)
  (routing/hook-browser-navigation!))

(defn loaded-messenger-extensions [& args]
  nil)

(set! js/loaded_messenger_extensions_sdk
      loaded-messenger-extensions)

(set! js/main-cljs-fn main)
