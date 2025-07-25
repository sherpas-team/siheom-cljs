(ns siheom.examples.counter-test
  (:require
   ["@testing-library/react" :as tlr :refer [screen waitFor]]
   ["@testing-library/user-event" :default user-event]
   [promesa.core :as p]
   [reagent.core :as r]
   [siheom.examples.counter :refer [counter]]
   [siheom.query-locator :as q]
   [siheom.siheom :as sh]
   [siheom.util :as util]
   [vitest.clojure-testing :refer [deftest testing]]))


(deftest counter-test
  (testing
   "click counter then 1 up : plain interop"
    (p/do
      (waitFor (fn []
                 (tlr/render (r/as-element [counter]))))
      (waitFor (fn []
                 (.click user-event (.getByRole screen "button" #js{:name "click: 0"}))))
      (waitFor (fn []
                 (.click user-event (.getByRole screen "button" #js{:name "click: 1"}))))
      (waitFor (fn []
                 (-> (.findByRole screen "button" #js{:name "click: 2"})
                     (js/expect)
                     (.-resolves)
                     (.toBeVisible))))
      nil))

  (testing
   "click counter then 1 up : util"
    (p/do
      (util/render-cp [counter])
      ;; click and wait as promise
      (util/click! #(util/query "button" "click: 0"))
      (util/click! #(util/query "button" "click: 1"))
      (util/visible? #(util/query "button" "click: 2") true)
      nil))

  (testing
   "click counter then 1 up : siheom"
    ;; test as data. with playwright like locator
    (sh/run-siheom
     (sh/render [counter])
     (sh/click! (q/button "click: 0"))
     (sh/click! (q/button "click: 1"))
     (sh/visible? (q/button "click: 2")))))
