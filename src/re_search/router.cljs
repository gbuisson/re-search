(ns re-search.router
  (:require
   [re-search.events :as events]
   [goog.events :as goog-events]
   [re-frame.core :as re-frame]
   [secretary.core
    :as secretary :refer-macros [defroute]])
  (:import [goog History]
           [goog.history EventType]))

(defn is-service-worker-supported?
  []
  (and
   (exists? js/navigator.serviceWorker)
   (= js/location.protocol "https:")))

(defn register-service-worker
  [path-to-sw]
  (when (is-service-worker-supported?)
    (-> js/navigator
        .-serviceWorker
        (.register path-to-sw))))

(defn routes
  []
  (set! (.-hash js/location) "/") ;; on app startup set location to "/"
  (secretary/set-config! :prefix "#") ;; and don't forget about "#" prefix
  (defroute "/" [] (re-frame/dispatch [::events/set-active-page
                                       {:page :home}]))
  (defroute "/search/:search" [search]
    (re-frame/dispatch
     [::events/set-active-page {:page :search
                                :category :all
                                :search search}]))

  (defroute "/search/images/:search" [search]
    (re-frame/dispatch
     [::events/set-active-page {:page :search
                                :category :images
                                :search search}]))

  (defroute "/search/videos/:search" [search]
    (re-frame/dispatch
     [::events/set-active-page {:page :search
                                :category :videos
                                :search search}]))

  (defroute "/search/news/:search" [search]
    (re-frame/dispatch
     [::events/set-active-page {:page :search
                                :category :news
                                :search search}]))

  (defroute "/search/maps/:search" [search]
    (re-frame/dispatch
     [::events/set-active-page {:page :search
                                :category :maps
                                :search search}])))

(defn dispatch-token [e]
  (do (secretary/dispatch! (aget e "token"))))

(doto (History.)
  (goog-events/listen EventType.NAVIGATE dispatch-token)
  (.setEnabled true))
