(ns app.view.views
  (:require-macros
   [kioo.reagent
    :refer [defsnippet deftemplate snippet]])
  (:require
   [reagent.core :as reagent
     :refer [atom]]
   [re-frame.core :as rf]
   [taoensso.timbre :as timbre]
   [kioo.reagent :as kioo
    :refer [html-content content append after set-attr do->
            substitute listen unwrap]]
   [kioo.core
    :refer [handle-wrapper]]
   [cljsjs.material-ui]
   [cljs-react-material-ui.core :as material
     :refer [get-mui-theme color]]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.icons :as ic]
   [goog.string :as gstring]))

(defn raw-view [attr & items]
  (into [:div attr]
     (for [item items]
          [:div {:style {:color "black" :border "thin solid red"}}
            (pr-str item)])))

(defn time-button [var content]
  [:button.btn
   {:type "button"
    :class (if (= @var content) "btn-primary" "btn-default")
    :on-click #(reset! var content)
    :style {:margin-left "0.5em"
            :margin-bottom "0.5em"}}
   content])

(defn panel [data]
  (let [time (reagent/atom nil)
        date "Thursday, October 15, 2017"]
   (fn [data]
    [ui/paper {:style {:padding-left "1em"
                       :padding-right "1em"}}
     [:h3
      [ui/font-icon {:class-name "material-icons"
                     :style {:font-size "40"}}
       [ic/action-perm-contact-calendar {:color (color :grey600)
                                         :font-size "8em"}]
       [:span {:style {:margin-left "0.5em"}}
        "Schedule your appointment"]]]
     [ui/paper {:style {:padding "0.5em"}}
      [:h3 "Available spots:"]
      [:h4 "Thursday, October 15, 2017"]
      [:div {:style {:margin-left "1em" :margin-top "1em"}}
       [time-button time "10:30 AM"]
       [time-button time "1:15 PM"]
       [time-button time "2:00 PM"]
       [time-button time "3:00 AM"]
       [time-button time "4:00 AM"]]
      [:div {:style {:margin-top "1em"}}
       [:button.btn.btn-primary
        {:style {:margin-left "0%" :width "100%"}
         :on-click #(rf/dispatch
                     [:schedule (str @time " on " date)])}
        "Schedule Your Appointment"]]]])))

(defn view [mode session]
  [ui/mui-theme-provider ;; ## TODO: factor this into widget...
       {:mui-theme (get-mui-theme
                    {:palette
                     {:primary1-color "#661775"
                      :primary2-color (color :deep-purple700)
                      :primary3-color (color :deep-purple200)
                      :alternate-text-color (color :white) ;; used for appbar text
                      :primary-text-color (color :light-black)}})}
       [ui/paper {:style {:height "auto"}}
        [panel session]]])

(defsnippet page "template.html" [:html]
  [state & {:keys [scripts title forkme]}]
  {[:head :title] (if title (content title) identity)
   [:nav :.navbar-brand] (if title (content title) identity)
   [:main] (content [view nil state])
   [:.refresh-activator] (kioo/set-style :display "none")
   [:#forkme] (if forkme identity (content nil))
   [:body] (append [:div (for [src scripts]
                           ^{:key (gstring/hashCode (pr-str src))}
                           [:script src])])})

(defn html5 [data]
  (str "<!DOCTYPE html>\n" data))
