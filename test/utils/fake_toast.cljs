(ns utils.fake-toast
  (:require
   [vitest.support :refer [before-each]]))

(def toast-log-atom (atom []))

(before-each
 (reset! toast-log-atom []))

(defn toast! [text]
  (swap! toast-log-atom conj text))