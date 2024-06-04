(ns re-search.events
  (:require
   [re-search.config :as config]
   [re-frame.core :as re-frame]
   [re-search.db :as db]
   [goog.string :as gstring]
   [ajax.core :as ajax]))

(def search-api-url
  (str config/searx-api-base-url "/search?q=%s&categories=%s&format=json"))

(def autocomplete-api-url
  (str config/searx-api-base-url "/autocompleter?q=%s&autocomplete=google"))

(def config-api-url
  (str config/searx-api-base-url "/config"))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-fx
 ::set-active-page
 (fn [{:keys [db]} [_ {:keys [page category search]}]]
   (let [set-page (assoc db :active-page page)]
     (case page
       :home {:db set-page}
       :search {:http-xhrio {:method :get
                             :uri (gstring/format search-api-url search (name category))
                             :response-format (ajax/json-response-format {:keywords? true})
                             :on-success      [::searx-search-success]
                             :on-failure      [::searx-search-failure]}
                :db (assoc set-page
                           :search search
                           :searx-search-results nil
                           :searx-autocompleter-results nil
                           :category category)}))))

(re-frame/reg-event-fx
 ::searx-get-config
 (fn [{:keys [db]} _]
   {:http-xhrio {:method :get
                 :uri config-api-url
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::searx-get-config-success]
                 :on-failure      [::searx-get-config-failure]}}))

(re-frame/reg-event-fx
 ::search-input
 (fn [{:keys [db]} [_ search]]
   {:http-xhrio {:method          :get
                 :uri (gstring/format autocomplete-api-url search)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::searx-autocompleter-success]
                 :on-failure      [::searx-autocompleter-failure]}}))

(re-frame/reg-event-db
 ::searx-get-config-success
 (fn [db [_ result]]
   (assoc db :searx-config result)))

(re-frame/reg-event-db
 ::searx-search-success
 (fn [db [_ result]]
   (assoc db :searx-search-results result)))

(re-frame/reg-event-db
 ::searx-search-failure
 (fn [db [_ result]]
   (assoc db :searx-search-results nil)))

(re-frame/reg-event-db
 ::searx-autocompleter-success
 (fn [db [_ result]]
   (assoc db :searx-autocompleter-results result)))
