(ns siheom.examples.form-story-test
  (:require
   ["@testing-library/react" :as rtl]
   ["@testing-library/user-event" :default user-event]
   [promesa.core :as p]
   [shadow.cljs.modern :refer [js-await]]
   [siheom.default-runtime-impl :refer [resolve-story]]
   [siheom.examples.form-story :refer [form-story]]
   [siheom.query-locator :as q]
   [siheom.siheom :as sh]
   [uix.core :refer [$]]
   [vitest.clojure-testing :refer [is testing]]))

(def result-atom (atom nil))
(defmethod resolve-story :Examples/form-story [_]
  ($ form-story {:submit-message! (fn [value]
                                    (reset! result-atom value)
                                    (js/Promise.resolve))}))

;; using siheom
(testing
 "should submit form : siheom"
  (p/do (sh/run-siheom!
         (sh/render :Examples/form-story)
         (sh/type! (q/textbox "Message") "hello world")
         (sh/click! (q/button "Submit"))
         (sh/visible? (q/text "Submitted!")))

        (is (= @result-atom "hello world"))))

;; uix official docs example
;; @see https://github.com/pitch-io/uix/blob/master/docs/testing.md
;; I fixed the false negative test case.

(defn type-input [target value]
  (.type (.setup user-event) target value))

(defn click [target]
  (.click rtl/fireEvent target))

(def result-2-atom (atom nil))

(testing "should submit form : uix official docs example"
  (let [container (rtl/render ($ form-story {:submit-message! (fn [value]
                                                                (reset! result-2-atom value)
                                                                (js/Promise.resolve))}))
        input (.getByTestId container "form-input")
        button (.getByTestId container "form-submit-button")]
    (js-await [_ (type-input input "hello world")]
              (click button)
              ;; findByTestId is async operation
              (js-await [message (.findByTestId container "form-submitted-message")]
                        (is (= "Submitted!" (.-textContent message)))
                        (is (= @result-2-atom "hello world"))))))