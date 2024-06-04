(ns re-search.subs
  (:require
   [ajax.core :as ajax]
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::searx-config
 (fn [db]
   (:searx-config db)))

(re-frame/reg-sub
 ::active-page
 (fn [db]
   (:active-page db)))

(re-frame/reg-sub
 ::search
 (fn [db]
   (:search db)))

(re-frame/reg-sub
 ::category
 (fn [db]
   (:category db)))

(re-frame/reg-sub
 ::searx-search-results
 (fn [db]
   (:searx-search-results db)))

(re-frame/reg-sub
 ::searx-autocompleter-results
 (fn [db]
   (:searx-autocompleter-results db)))
