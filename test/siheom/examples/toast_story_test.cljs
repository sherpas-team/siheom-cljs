(ns siheom.examples.toast-story-test
  (:require
   [siheom.default-runtime-impl :refer [resolve-story]]
   [siheom.examples.custom-siheom :as csh]
   [siheom.examples.toast-story :refer [toast-story]]
   [siheom.query-locator :as q]
   [siheom.siheom :as sh]
   [vitest.clojure-testing :refer [testing]]))

(defmethod resolve-story :Examples/toast-story [_]
  [toast-story])

(testing
 "toast test with custom runtime"
  (csh/run-custom-siheom!
   (sh/render :Examples/toast-story)
   (sh/click! (q/button "show me toast!"))
   (csh/toast-log? "hello~ i am toast!")))