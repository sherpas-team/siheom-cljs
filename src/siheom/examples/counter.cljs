(ns siheom.examples.counter
  (:require
   [reagent.core :as r]))
(defn counter []
  (let [counter-atom (r/atom 0)]
    (fn []
      [:button
       {:on-click #(swap! counter-atom inc)}
       "click: " @counter-atom])))