(ns sdk.facebook.messenger
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [cljs.core.async :as async
    :refer [chan <! >! put! close! timeout]]
   [clojure.spec.alpha :as s]
   [taoensso.timbre :as timbre]
   [goog.net.XhrIo :as xhr]
   [camel-snake-kebab.core
    :refer [->snake_case_keyword ->kebab-case-keyword]]
   [camel-snake-kebab.extras
    :refer [transform-keys]]))

;;    Docs:
;;    https://developers.facebook.com/docs/messenger-platform/send-api-reference

(def fb-endpoint "https://graph.facebook.com/v2.10/")
(def fb-messages-endpoint (str fb-endpoint "me/messages"))
(def fb-profile-endpoint (str fb-endpoint "me/messenger_profile"))

(def fb-access-secret (aget js/process "env" "FACEBOOK_ACCESS_TOKEN"))

(let [snake_case (memoize ->snake_case_keyword)]
  (defn snake_case_keys [m]
    (into {} (for [[k v] m] [(snake_case k) v]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SPEC
;;
;; https://developers.facebook.com/docs/messenger-platform/send-api-reference

(defn check
  ([type value]
   (if-not (s/valid? type value)
      (do (timbre/error (s/explain-str type value)) false)
      true)))

(defn check-event [value]
  (check ::event value))

;; --------------------------------------
;; EVENT is the object received from facebook via the webhook
;; See https://developers.facebook.com/docs/messenger-platform/webhook-reference

(s/def ::event (s/and (s/keys :req-un [::sender ::recipient ::timestamp])
                      (s/or
                       :postback (s/keys :req-un [::postback])
                       :any (s/keys :opt-un []))))

;; --------------------------------------
;; REQUEST

(s/def ::request (s/or
                   :whitelist (s/keys :req-un [::whitelisted_domains])
                   :home-url (s/keys :req-un [::home_url])
                   :get-started (s/keys :req-un [::get_started])
                   :persistent-menu (s/keys :req-un [::persistent_menu])
                   :greeting (s/keys :req-un [::setting_type ::greeting])
                   :envelope
                   (s/keys :req-un [::recipient]
                           :opt-un [::sender_action ::message ::notification_type])))

(s/def ::whitelisted_domains (s/coll-of ::url :max-count 10))

(s/def ::home_url (s/keys :req-un [::url ::webview_height_ratio ::in_test]
                          :opt-un [::webview_share_button]))

(s/def ::get_started (s/keys :req-un [::payload]))

(s/def ::persistent_menu (s/coll-of (s/keys :opt-un [])))

(s/def ::url string?)

(s/def ::recipient (s/keys :req-un []
                           ;; phone_number or id must be set
                           :opt-un [::id ::phone_number ::name]))

(s/def ::id string?)

;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/sender-actions

(s/def ::sender-action #{:mark_seen :typing_on :typing_off})

(s/def ::message (s/keys :req-un []
                         :opt-un [::text ::attachment ::quick_replies ::metadata]))

(s/def ::metadata string?)  ;; limited to 1000 chars

;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/quick-replies

(s/def ::quick_replies (s/coll-of ::quick-reply))

(defmulti quick-reply-type (comp vector :content_type))

(s/def ::quick-reply (s/multi-spec quick-reply-type :content_type))

(s/def ::content-type #{:location :text})

(defmethod quick-reply-type [:location] [_]
  (s/keys :req-un [::content_type]
          :opt-un [::title ::payload ::image_url]))

(defmethod quick-reply-type [:text] [_]
  (s/keys :req-un [::content_type ::title ::payload]
          :opt-un [::image_url]))

;;;;

(s/def :attachment/type #{:audio :file :image :video :template})

(defmulti attachment-type (comp vector :type))

(s/def ::attachment (s/multi-spec attachment-type :type))

;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/audio-attachment

(defmethod attachment-type [:audio] [_]
  (s/keys :req-un [:attachment/type :audio-attachment/payload]))

(s/def :audio-attachment/payload (s/keys :req-un [::url]))

;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/file-attachment

(defmethod attachment-type [:file] [_]
  (s/keys :req-un [:attachment/type :file-attachment/payload]))

(s/def :file-attachment/payload (s/keys :req-un [::url]))

;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/image-attachment

(defmethod attachment-type [:image] [_]
  (s/keys :req-un [:attachment/type :image-attachment/payload]))

(s/def :image-attachment/payload (s/keys :req-un [::url]))

;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/video-attachment

(defmethod attachment-type [:video] [_]
  (s/keys :req-un [:attachment/type :video-attachment/payload]))

(s/def :video-attachment/payload (s/keys :req-un [::url]))

;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/templates

(defmethod attachment-type [:template] [_]
  (s/keys :req-un [:attachment/type :template-attachment/payload]))

(defmulti payload-type (comp vector :template_type))

(s/def :template-attachment/payload (s/multi-spec payload-type :template_type))

(s/def ::template_type #{:button :generic :list :receipt :airline_boardingpass
                         :airline_checkin :airline_itinerary :airline_update})

;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/button-template

(defmethod payload-type [:button] [_]
  (s/keys :req-un [::template_type ::text ::buttons]))

(s/def ::buttons (s/coll-of ::message-button :min-count 1 :max-count 3))

(s/def ::message-button (s/and ::button))

;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/generic-template

(defmethod payload-type [:generic] [_]
  (s/keys :req-un [::template_type :generic/elements]
          :opt-un [:generic/image-aspect-ratio]))

(s/def :generic/image-aspect-ratio #{:horizontal :square})

(s/def :generic/elements (s/coll-of :generic/element :max-count 10))

(s/def :generic/element
  (s/keys :req-un [::title]
          :opt-un [::subtitle ::image_url ::default_action :generic/buttons]))

(s/def :generic/buttons (s/coll-of :generic/button :max-count 3))

(s/def :generic/button (s/and ::button))

;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/list-template

(defmethod payload-type [:list] [_]
  (s/keys :req-un [::template_type :list/elements]
          :opt-un [:list/top_element_style :list/buttons]))

(s/def :list/elements (s/coll-of :generic/element :min-count 2 :max-count 4))

(s/def :list/top_element_style #{:large :compact})

(s/def :list/buttons (s/coll-of :list/button :max-count 1))

(s/def :list/button (s/and ::button))

;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/buttons

(s/def :button/type #{:web_url :postback :phone_number :element_share :payment
                      :account_link :account_unlink})

(defmulti button-type (comp vector :type))

(s/def ::button (s/multi-spec button-type :type))

;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/url-button

(defmethod button-type [:web_url] [_]
  (s/keys :req-un [:button/type ::title ::url]
          :opt-un [::webview-height-ratio ::messenger-extensions ::fallback_url]))

(defmethod button-type [:postback] [_]
  (s/keys :req-un [:button/type ::title ::payload])

  (defmethod button-type [:element_share] [_]
    (s/keys :req-un [:button/type])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; TRANSFER

(defn result-handler [result-chan & [kebab-case?]]
  (fn [e]
    (if (-> e .-target .isSuccess)
      (put! result-chan
            (if kebab-case?
              (->> e .-target .getResponseJson js->clj
                  (transform-keys ->kebab-case-keyword))
              (-> e .-target .getResponseJson
                  (js->clj :keywordize-keys true))))
      (timbre/error (-> e .-target .getLastError)
                    (-> e .-target .getResponseJson
                       (js->clj :keywordize-keys true))))
    (close! result-chan)))

(defn api-post [{:as event} uri & [kebab-case?]]
   (go-loop [method "POST"
             content  (->> event
                           (transform-keys ->snake_case_keyword)
                           (clj->js)
                           (goog.json.serialize))
             headers #js {"Content-Type" "application/json"}
             result (chan)
             cb (result-handler result kebab-case?)]
     (xhr/send uri cb method content headers)
     (<! result)))

(defn api-get [uri & [kebab-case?]]
  (let [result (chan)
        cb (result-handler result kebab-case?)]
     (xhr/send uri cb)
     result))

(defn send
  ([{:as request} endpoint]
   {:pre [(check ::request request)]}
   (api-post request (str endpoint "?access_token=" fb-access-secret)))
  ([request]
   (send request fb-messages-endpoint)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DESTRUCTURING messages

(defn get-sender-id [event]
  {:post [(check ::id %)]}
  (get-in event [:sender :id]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; COMPOSING MESSAGES

(defrecord Request [])

(defn set-recipient [root id]
  [:pre [(check ::id id)]]
  (assoc-in root [:recipient :id] id))

(defn create-request
  ([id]
   (set-recipient (create-request) id))
  ([] (->Request)))

(def create-message create-request) ;; deprecated

(defn create-reply [event]
  {:pre [(check ::event event)]}
  (-> (get-sender-id event)
      (create-request)))

(defn set-action [root action]
  (assoc-in root [:sender_action] action))

(defn set-message-text [root text]
      (assoc-in root [:message :text] text))

(defn set-message-replies [root options]
  (assoc-in root [:message :quick_replies] options))

(defn fb-wrap-quick-replies [root replies]
      (assoc-in root [:message :quick_replies]
                     (for [item replies
                           :let [item (if (map? item) item {:title (str item)})]]
                       (-> item
                           (update :content_type #(or % "text"))
                           (update :payload
                                   #(or % (if (map? item)(:title item)item)))))))

(defn set-attachment-type [root type]
  (assoc-in root [:message :attachment :type] type))

(defn set-payload-template-type [root type] ;; #eliminate?
  (assoc-in root [:message :attachment :payload :template_type] type))

(defn set-payload-elements [root elements] ;; #eliminate
  (assoc-in root [:message :attachment :payload :elements] elements))

(defn set-message-metadata [root metadata]
  (assoc-in root [:message :metadata] metadata))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PAYLOAD

(defn as-buttons-payload [text buttons]
  (assoc {}
         :template_type :button
         :text text
         :buttons buttons))

(defn as-generic-payload [elements & {:keys [top_element_style buttons] :as opt}]
  (assoc opt
         :template_type :generic
         :elements elements))

(defn as-list-payload [elements]
     {:template_type :list
      :elements elements})

;; buttons

(defn url-button [title & {:keys [url webview-height-ratio messenger-extensions fallback-url]
                           :as options}]
  (merge {:type :web_url :title title}
         (snake_case_keys options)))

(defn postback-button [title & {:keys [payload]}]
  {:type :postback :title title :payload payload})

(defn share-button [& {:keys [share_contents]}]
  {:type :element_share :share_contents share_contents})

;; reply

(defn text-reply [title payload & {:keys [image_url] :as opt}]
  (assoc opt
         :content_type :text
         :title title
         :payload payload))

(defn location-reply []
  {:content_type :location})

;;

(defn create-element [title & {:keys [subtitle image-url
                                      default-action buttons] :as options}]
  (->>
   {:title title}
   (merge options)
   (remove (comp nil? second))
   (into {})
   (snake_case_keys)))

(defn set-element-buttons [element buttons]
  (assoc element :buttons buttons))

(defn set-template-payload
    ([root payload]
     {:pre [(check :template-attachment/payload payload)]}
     (-> (set-attachment-type root :template)
         (assoc-in [:message :attachment :payload] payload)))
    ([root type payload1 & payload]
     (let [generator (case type
                       :generic as-generic-payload)]
       (->> (apply generator payload1 payload)
            (set-template-payload root)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SENDERS

(defn send-text [id text]
  (-> (create-message id)
      (set-message-text text)
      (send)))

(defn send-action [id action]
  (-> (create-message id)
      (set-action action)
      (send)))

(defn post-profile [type data]
  (send {type data} fb-profile-endpoint))

;; https://developers.facebook.com/docs/messenger-platform/webview/extensions))

(defn send-whitelist-domains
  "To use Messenger Extensions in your bot, you must first whitelist the domain the page is served from"
  ([[:as domains]]
   (-> {:whitelisted_domains (vec domains)}
       (send fb-profile-endpoint))))

;; https://developers.facebook.com/docs/messenger-platform/messenger-profile/home-url/

(defn send-home-url
  "enable a Chat Extension in the composer drawer in Messenger. It controls what the user sees when your bot's chat extension is invoked via the composer drawer in Messenger."
  ([{:keys [url webview_height_ratio webview_share_button in_test]
     :or {in_test true webview_height_ratio "tall"} :as home-url}]
   (timbre/debug "URL=" home-url)
   (post-profile :home_url home-url)))

;; https://developers.facebook.com/docs/messenger-platform/user-profile

(defn send-get-started ;; ue post-profile instead
  ([{:keys [payload] :as get-started}]
   (post-profile :get_started get-started)))

(defn fetch-user-profile [id]
  ; first_name,last_name,profile_pic,locale,timezone,gender
  (api-get (str fb-endpoint id "?access_token=" fb-access-secret) true))

(defn fetch-user-pageids [id]
  (api-get (str fb-endpoint id "/ids_for_pages" "?access_token=" fb-access-secret)))

(defn send-echo [event]
  ;; send message text back to sender as is (useful for testing)
  (-> (create-reply event)
      (set-message-text (or (get-in event [:message :text])
                            (get-in event [:message :message :text])))
      (send)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PERSISTENT MENU

(defn send-persistent-menu [[:as menu-items]]
  (post-profile :persistent_menu menu-items))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GREETING

(defn add-greeting [text]
  (send {:setting_type "greeting"
         :greeting {:text text}}
        (str fb-endpoint "me/thread_settings")))

#_
(add-greeting
 "Welcome to CoCoCare.
  How can I help you?")


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CONVERSATION
;;
;; https://developers.facebook.com/docs/graph-api/reference/v2.9/conversation

(defn fetch-conversation [conversation-id]
  (api-get (str fb-endpoint conversation-id
                "?access_token=" fb-access-secret)))

(defn fetch-conversation-messages [conversation-id]
  (api-get (str fb-endpoint conversation-id "/messages"
                "?access_token=" fb-access-secret)))
