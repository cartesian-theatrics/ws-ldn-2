(ns ws-ldn-2.day1.ui.views
  (:require-macros
   [reagent.ratom :refer [reaction]]
   [cljs-log.core :refer [debug info warn]])
  (:require
   [ws-ldn-2.day1.ui.state :as state]
   [reagent.core :as r]
   [thi.ng.geom.svg.core :as svg]
   [thi.ng.color.core :as col]
   [thi.ng.color.gradients :as grad]
   [thi.ng.math.core :as m]
   [cljsjs.codemirror :as cm]
   [cljsjs.codemirror.addon.edit.matchbrackets]
   [cljsjs.codemirror.addon.edit.closebrackets]
   [cljsjs.codemirror.mode.clojure]))

(def tonemap
  (->> :orange-blue
       (grad/cosine-schemes)
       (apply grad/cosine-gradient 100)
       (reverse)
       (vec)))

(defn home
  [route]
  [:div.container
   [:h1 "Welcome"]])

(defn cm-editor
  [opts]
  (r/create-class
   {:component-did-mount
    (fn [this]
      (let [editor (.fromTextArea js/CodeMirror (r/dom-node this) (clj->js opts))]
        (.on editor "change" #((:on-change opts) (.getValue %)))))
    :reagent-render
    (fn [_]
      [:textarea {:default-value (:default-value opts)}])}))

(defn query-viz
  []
  (let [src (reaction (:query-viz-uri @state/app-state))]
    (fn []
      (when @src
        [:div.row
         [:div.col-xs-12
          [:img.fullwidth {:src @src}]]]))))

(defn map-view
  []
  (let [boroughs (reaction (:boroughs @state/app-state))
        sel      (reaction (:selected-borough @state/app-state))
        {:keys [avg-min avg-max]} @state/app-state]
    (fn []
      @sel
      [:div.row
       [:div.col-xs-12
        (svg/svg
         {:width 960 :height 720}
         (doall
          (map
           (fn [borough]
             (svg/polygon
              (borough '?apoly)
              {:key           (borough '?boroughID)
               :stroke        "red"
               :fill          (if (= (borough '?boroughID)
                                     (:selected-borough @state/app-state))
                                "yellow"
                                (-> (m/map-interval (borough '?avg) avg-min avg-max 0 99)
                                    int
                                    tonemap
                                    col/as-css
                                    deref))
               :on-mouse-over #(state/select-borough (borough '?boroughID))}))
           @boroughs)))]])))

(defn query-editor
  [route]
  (let [query    (reaction (:query @state/app-state))]
    [:div.container
     [:div.row
      [:div.col-xs-12
       [:h1 "Query editor"]
       [cm-editor
        {:mode              "text/x-clojure"
         :theme             "material"
         :on-change         #(state/set-query %)
         :default-value     @query
         :matchBrackets     true
         :autoCloseBrackets true
         :lineNumbers       true
         :autofocus         true}]]]
     [:div.row
      [:div.col-xs-12
       [:button.btn.btn-primary
        {:on-click #(state/submit-query @query state/parse-query-response)}
        "Submit"] " "
       [:button.btn.btn
        {:on-click state/set-viz-query}
        "Visualize"]]]
     [map-view]
     [query-viz]]))
