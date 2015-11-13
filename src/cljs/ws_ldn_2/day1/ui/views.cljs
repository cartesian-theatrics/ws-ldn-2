(ns ws-ldn-2.day1.ui.views
  (:require-macros
   [reagent.ratom :refer [reaction]]
   [cljs-log.core :refer [debug info warn]])
  (:require
   [ws-ldn-2.day1.ui.state :as state]
   [reagent.core :as r]
   [thi.ng.geom.svg.core :as svg]
   [cljsjs.codemirror :as cm]
   [cljsjs.codemirror.addon.edit.matchbrackets]
   [cljsjs.codemirror.addon.edit.closebrackets]
   [cljsjs.codemirror.mode.clojure]))

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

(defn query-editor
  [route]
  (let [query (reaction (:query @state/app-state))
        polys (reaction (:polygons @state/app-state))]
    [:div.container
     [:div.row
      [:div.col-xs-12
       [:h1 "Query editor"]
       [cm-editor
        {:mode              "text/x-clojure"
         :theme             "material"
         :on-change         #(state/set-query %)
         :default-value      @query
         :matchBrackets     true
         :autoCloseBrackets true
         :lineNumbers       true
         :autofocus         true}]]]
     [:div.row
      [:div.col-xs-12
       [:button.btn.btn-primary
        {:on-click state/submit-query}
        "Submit"] " "
       [:button.btn.btn
        {:on-click state/set-viz-query}
        "Visualize"]]]
     [:div.row
      [:div.col-xs-12 (svg/svg {:width 960 :height 720} @polys)]]
     [query-viz]]))
