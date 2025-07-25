(ns siheom.examples.toast-story
  (:require
   [utils.toast :refer [toast!]]))

(defn toast-story []
  [:button {:on-click (fn []
                        (toast! "hello~ i am toast!"))}
   "show me toast!"])