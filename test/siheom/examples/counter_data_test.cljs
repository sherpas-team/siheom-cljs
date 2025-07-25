(ns siheom.examples.counter-data-test
  (:require
   [clojure.edn :as edn]
   [shadow.resource :as rc]
   [siheom.siheom :as sh]
   [vitest.clojure-testing :refer [testing]]))

;; siheom test is just a data
(def test-suite-edn (rc/inline "siheom/examples/counter_test.edn"))
(def test-suite-json (rc/inline "siheom/examples/counter_test.json"))

(defn run-siheom-edn! [test-suite-edn]
  (apply sh/run-siheom! (edn/read-string test-suite-edn)))

(testing
 "click counter then 1 up : siheom with edn"
  (run-siheom-edn! test-suite-edn))


(defn run-siheom-json! [test-suite-json]
  (->> (js->clj (js/JSON.parse test-suite-json) {:keywordize-keys true})
       (map (fn [test-case]
              (let [action-key (keyword (get test-case :action-key))
                    args (get test-case :args)]
                {:action-key action-key
                 :args (if (= action-key :render)
                         (let [story-title (get args :story-title)]
                           {:story-title (keyword story-title)})
                         args)})))
       (apply sh/run-siheom!)))

(testing
 "click counter then 1 up : siheom with json"
  (run-siheom-json! test-suite-json))
