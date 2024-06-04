(ns re-search.components
  (:require
   [re-frame.core :as re-frame]
   [re-search.subs :as subs]
   [re-search.events :as events]
   ["@heroicons/react/solid" :as icons]
   ["@heroicons/react/outline" :as hero-outline]))

(defn set-hash!
  "Set the location hash of a js/window object."
  ([v] (set-hash! (.-location js/window) v))
  ([loc v] (aset loc "hash" v)))

(defn get-search-input-value [e]
  (.preventDefault e)
  (aget e "target" 0 "value"))

(defn set-search-string [category search]
  (cond
    (= :all category) (set-hash! (str "/search/" search))
    (not category) (set-hash! (str "/search/" search))
    :else (set-hash! (str "/search/" (name category) "/" search))))

(defn set-category [category search]
  (if (= "all" category)
    (set-hash! (str "/search/" search))
    (set-hash! (str "/search/" category "/" search))))

(defn autocompleter []
  (let [category (re-frame/subscribe [::subs/category])
        results (re-frame/subscribe [::subs/searx-autocompleter-results])]
    [:div {:class "flex flex-wrap justify-center items-center w-full mt-5 mb-3"}
     (for [result (second @results)]
       [:button
        {:on-click #(set-search-string @category result)
         :key (str "autocomplete-link" result)
         :class "flex-shrink bg-white hover:bg-gray-100 mr-2 mb-2 text-gray-600 hover:text-blue-600 text-sm  py-2 px-4 rounded shadow"}
        result])]))

(defn avatar [{:keys [url class]}]
  [:img {:loading "lazy"
         :class (str  "h-11 rounded-full cursor-pointer
     transition duration-150 transform hover:scale-110 " class)
         :src url
         :alt "profile pic"}])

(defn header-option [Icon title selected]
  (let [category (-> title clojure.string/lower-case)
        search (re-frame/subscribe [::subs/search])
        class (cond-> "flex items-center space-x-1 border-b-2 border-transparent hover:text-blue-400
    hover:border-blue-400 pb-2 cursor-pointer"
                selected (str " text-blue-500 boarder-blue-400"))]
    [:div {:on-click #(set-category category @search)
           :class class}
     [:> Icon {:class "h-5"}]
     [:p {:class "hidden sm:inline-flex"} title]]))

(defn header-options []
  (let [category (re-frame/subscribe [::subs/category])]
    [:div {:class "flex w-full text-gray-700 justify-center text-sm lg:text-base lg:space-x-36 boarder-b font-OpenSans"}
     [:div {:class "flex space-x-6"}
      [header-option icons/SearchIcon "All" (or (not @category) (= @category :all))]
      [header-option icons/PhotographIcon "Images" (= @category :images)]
      [header-option icons/PlayIcon "Videos" (= @category :videos)]
      [header-option icons/NewspaperIcon "News" (= @category :news)]
      [header-option icons/MapIcon "Maps" (= @category :maps)]]]))

(defn header []
  [:header {:class "flex w-full p-5 justify-between text-sm text-gray-800"}])

(defn search-header [category]
  (let [search (re-frame/subscribe [::subs/search])
        category (re-frame/subscribe [::subs/category])]
    [:header {:class "sticky w-full top-0 bg-white/95 p-2 rounded"}
     [:div {:class "flex w-full p-6 items-center justify-center"}
      [:a {:href "/"}
       [:img {:src "/doodle.png"
              :height 60
              :width 120
              :class "cursor-pointer rounded"}]]
      [:form {:on-submit #(set-search-string @category (get-search-input-value %))
              :class "flex flex-grow px-5 py-3 ml-10 mr-5 border boder-gray-200 rounded-full shadow-lg max-w-3xl justify-center"}
       [:input {:on-input #(re-frame/dispatch [::events/search-input
                                               (aget % "target" "value")])
                :class "flex-grow w-full focus:outline-none"
                :placeholder @search
                :type "text"}]
       [:> icons/XIcon {:class "h-7 sm:mr-3 text-gray-500 cursor-pointer tarnsition duration-100 transform hover:scale-125"}]
       [:button {:type "submit"}
        [:> icons/SearchIcon {:class "mb-1 h-5 hidden sm:inline-flex text-gray-500 tarnsition duration-100 transform hover:scale-125"}]]]]
     [autocompleter]
     [header-options]]))

(defn footer []
  [:footer {:class "grid w-full divide-y-[1px] divide-gray-400 bg-gray-100 text-gray-500 font-OpenSans"}
   [:div {:class "px-8 py-3"}
    "France"]])
