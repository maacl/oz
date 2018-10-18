(ns ^:figwheel-always oz.core
  (:require [reagent.core :as r]
            [cljsjs.vega]
            [cljsjs.vega-lite]
            [cljsjs.vega-embed]
            [cljsjs.vega-tooltip]))

(defn render-vega-lite
  ([spec elem]
   (when spec
     (let [spec (clj->js spec)
           opts {:renderer "canvas"
                 :mode "vega-lite"}
           vega-spec (. js/vl (compile spec))]
       (log "Vega-lite translates to:")
       (log vega-spec)
       (-> (js/vegaEmbed elem spec (clj->js opts))
           (.then (fn [res]
                    #_(log res)
                    (. js/vegaTooltip (vegaLite (.-view res) spec))))
           (.catch (fn [err]
                     (log err))))))))

(defn render-vega [spec elem]
  (when spec
    (let [spec (clj->js spec)
          opts {:renderer "canvas"
                :mode "vega"}]
      (-> (js/vegaEmbed elem spec (clj->js opts))
          (.then (fn [res]
                   #_(log res)
                   (. js/vegaTooltip (vega (.-view res) spec))))
          (.catch (fn [err]
                    (log err)))))))

(defn vega-lite
  "Reagent component that renders vega-lite."
  [spec]
  (r/create-class
   {:display-name "vega-lite"
    :component-did-mount (fn [this]
                           (render-vega-lite spec (r/dom-node this)))
    :component-will-update (fn [this [_ new-spec]]
                             (render-vega-lite new-spec (r/dom-node this)))
    :reagent-render (fn [spec]
                      [:div#vis])}))


(defn vega
  "Reagent component that renders vega"
  [spec]
  (r/create-class
   {:display-name "vega"
    :component-did-mount (fn [this]
                           (render-vega spec (r/dom-node this)))
    :component-will-update (fn [this [_ new-spec]]
                             (render-vega new-spec (r/dom-node this)))
    :reagent-render (fn [spec]
                      [:div#vis])}))


(defn view-spec
  ;; should handle sharing data with nodes that need it?
  [spec]
  ;; prewalk spec, rendering special hiccup tags like :vega and :vega-lite, and potentially other composites,
  ;; rendering using the components above. Leave regular hiccup unchanged).
  ;; TODO finish writing; already hooked in below so will break now
  (clojure.walk/prewalk
    (fn [x] (if (and (coll? x) (#{:vega :vega-lite} (first x)))
              [(case (first x) :vega vega :vega-lite vega-lite)
               (reduce merge (rest x))]
              x))
    spec))


(defn application [app-state]
  (when-let [spec (:view-spec @app-state)]
    [view-spec spec]))

(r/render-component [application app-state]
                    (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
