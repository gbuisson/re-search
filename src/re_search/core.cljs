(ns re-search.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [day8.re-frame.http-fx]
   [re-search.events :as events]
   [re-search.views :as views]
   [re-search.config :as config]
   [re-search.router :as router]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)
    (router/register-service-worker "js/service-worker.js")))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (router/routes)
  #_(re-frame/dispatch-sync [::events/searx-get-config])
  (dev-setup)
  (mount-root))
