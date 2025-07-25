(ns siheom.examples.counter-test
  (:require
   ["@testing-library/react" :as tlr :refer [screen waitFor]]
   ["@testing-library/user-event" :default user-event]
   [promesa.core :as p]
   [reagent.core :as r]
   [siheom.default-runtime-impl :refer [resolve-story]]
   [siheom.examples.counter :refer [counter]]
   [siheom.query-locator :as q]
   [siheom.siheom :as sh]
   [siheom.testing-library-impl :as util]
   [vitest.clojure-testing :refer [testing]]))

(testing
 "click counter then 1 up : plain testing-library interop"
  (p/do
    (waitFor (fn []
               (tlr/render (r/as-element [counter]))))
    (waitFor (fn []
               (.click user-event (.getByRole screen "button" #js{:name "click: 0"}))))
    (waitFor (fn []
               (-> (.findByRole screen "button" #js{:name "click: 1"})
                   (js/expect)
                   (.-resolves)
                   (.toBeVisible))))
    nil))

(testing
 "click counter then 1 up : with siheom runtime"
  (p/do
    (util/render-cp [counter])
    ;; click and wait as promise
    (util/click! #(util/query "button" "click: 0"))
    (util/visible? #(util/query "button" "click: 1") true)
    nil))

(defmethod resolve-story :Examples/counter [_]
  [counter])

(testing
 "click counter then 1 up : siheom"
  (sh/run-siheom!
   (sh/render :Examples/counter)
   (sh/click! (q/button "click: 0"))
   (sh/click! (q/button "click: 1"))
   (sh/visible? (q/button "click: 2"))))