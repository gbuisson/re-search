(ns re-search.views
  (:require
   [goog.object :as goog-object]
   [re-frame.core :as re-frame]
   [re-search.subs :as subs]
   [re-search.events :as events]
   [secretary.core :as secretary]
   [re-search.components
    :refer [header
            search-header
            footer
            set-search-string
            get-search-input-value
            autocompleter]]
   ["@heroicons/react/solid" :as icons]
   ["@heroicons/react/outline" :as hero-outline]))

(def title-suffix " - re-search")

(defn set-page-title! [title]
  (set! (. js/document -title) title))

(defn home-body []
  (let [category (re-frame/subscribe [::subs/category])]
    [:form {:on-submit #(set-search-string @category
                                           (get-search-input-value %))
            :class "flex flex-col items-center pt-3 flex-grow w-4/5"}
     [:img {:class "rounded"
            :src "/doodle.png"
            :height 207
            :width 700}]
     [:div {:class "flex w-full mt-5 hover:shadow-lg focus-within:shadow-lg max-w-md rounded-full
      border border-gray-200 px-5 py-3 items-center sm:max-w-xl lg:max-w-2xl"}
      [:> hero-outline/SearchIcon {:class "h-5 mr-3 text-gray-700"}]
      [:input {:type "text"
               :on-input #(re-frame/dispatch
                           [::events/search-input (aget % "target" "value")])
               :class "flex-grow focus:outline-none"}]]

     [autocompleter]]))


(defn structured-url [url parsed_url]
  [:span {:class "w-64"}
   [:a {:href url
        :class "truncate text-xs font-OpenSans text-stone-900"}
    (let [[proto domain & parts] parsed_url
          rendered-parts (->> (remove #(= "" %) parts)
                              (clojure.string/join " > "))]
      (cond-> (str proto "://" domain)
        (not= "/" rendered-parts) (str " › "
                                       (clojure.string/replace
                                        rendered-parts #"/" "/"))))]])

(defn search-body []
  (let [{:keys [results
                number_of_results
                infoboxes
                suggestions]}
        @(re-frame/subscribe [::subs/searx-search-results])]
    [:div {:class "grid w-full max-w-5xl font-OpenSans grid-cols-10 ml-5"}
     [:div {:class "order-last sm:order-first md:order-first lg:order-first col-span-10 sm:col-span-10 md:col-span-6 lg:col-span-6 mt-8 mr-8"}
      [:p {:class "text-gray-500 text-md mb-5 mt-3"}
       (str "About " number_of_results " results")]
      (for [{:keys [url title content img_src parsed_url]} results]
        [:div {:key url :class "mb-8 font-sans"}
         [:div {:class "group clear-both"}
          [structured-url url parsed_url]
          [:a {:href url}
           [:h2 {:class "text-xl text-blue-700 group-hover:underline font-OpenSans"}
            title]]]

         (when img_src
           [:a {:href url}
            [:img {:class "float-end rounded h-24"
                   :src img_src}]])
         [:p {:class "line-clamp-10 text-gray-900 font-OpenSans"}
          (if (seq content)
            content "No content")]])]

     (when (or (seq infoboxes)
               (seq suggestions))
       [:div {:class "order-first sm:order-last md:order-last lg:order-last col-span-10 sm:col-span-10 md:col-span-4 lg:col-span-4 mt-8 mr-8"}

        (when (seq infoboxes)
          (for [{:keys [infobox
                        content
                        img_src
                        id
                        urls]} infoboxes]
            [:div {:class "group mt-8"}
             [:a {:href id}
              [:h2 {:class "text-xl text-blue-700 group-hover:underline font-OpenSans"}
               infobox]]
             (when img_src
               [:img {:class "rounded w-full pb-3"
                      :src img_src}])
             (when content
               [:p {:class "line-clamp-10 text-gray-900 font-OpenSans"}
                content])

             (when urls
               [:ul {:class "pt-3"}
                (for [{:keys [title url]} urls]
                  [:a {:href url
                       :class "text-blue-700 hover:underline font-OpenSans"}
                   [:li {:class "list-none"} title]])])]))

        (when (seq suggestions)
          [:div {:class "group mt-8"}
           [:h2 {:class "text-xl"} "See Also"]
           [:ul {:class "pt-3"}
            (for [suggestion suggestions]
              [:a {:on-click #(set-search-string :all suggestion)
                   :class "text-blue-700 hover:underline font-OpenSans cursor-pointer"}
               [:li {:class "list-none"} suggestion]])]])
        ])]))

(defn images-search-body []
  (let [{:keys [results
                number_of_results]}
        @(re-frame/subscribe [::subs/searx-search-results])]
    [:div {:class "mx-auto w-full px-3 sm:pl-[5%] md:pl-[5%] lg:pl-5 font-OpenSans"}
     [:p {:class "text-gray-500 text-md mb-5 mt-3"}
      (str "About " number_of_results " results")]

     [:div {:class "columns-2 md:columns-3 lg:columns-4"}
      (for [{:keys [url
                    title
                    content
                    img_src
                    img_format
                    thumbnail_src
                    thumbnail]} results]
        [:a {:href url}
         [:div {:class "relative mb-4 before:content-[''] before:rounded-md before:absolute before:inset-0 before:bg-black before:bg-opacity-20"}
          [:img {:key url
                 :class "w-full rounded-md"
                 :src (or thumbnail_src thumbnail)}]]])]]))

(defn home []
  (set-page-title! "re-search")
  [:div {:class "flex flex-col items-center h-screen"}
   [header]
   [home-body]
   [footer]])

(defn search []
  (let [search (re-frame/subscribe [::subs/search])
        category (re-frame/subscribe [::subs/category])]
    (set-page-title! (str @search title-suffix))
    [:div {:class "flex flex-col items-center h-screen"}
     [search-header category]
     (cond (= :images @category) [images-search-body]
           (= :videos @category) [images-search-body]
           :else [search-body])
     [footer]]))

(defn main-panel []
  (let [active-page
        (re-frame/subscribe [::subs/active-page])
        searx-config
        (re-frame/subscribe [::subs/searx-config])
        search-results (re-frame/subscribe [::subs/searx-search-results])]

    (cond
      (= :home @active-page) [home]
      (= @active-page :search) [search]
      :else [home])))
