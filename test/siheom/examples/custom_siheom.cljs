(ns siheom.examples.custom-siheom
  (:require
   [siheom.default-runtime-impl :refer [add-action default-runtime-impl]]
   [siheom.siheom :as sh]
   [utils.fake-download-file :refer [fake-download-dir]]
   [utils.fake-toast :refer [toast-log-atom]]
   [vitest.clojure-testing :refer [is]]))

(defn toast-log? [expected]
  {:action-key :toast-log?
   :args {:expected expected}})

(def toast-log?-impl
  {:run (fn [{:keys [expected]}]
          (is (= expected (last @toast-log-atom))))
   :log (fn [{:keys [expected]}]
          (str "toast-log? : " expected))})

(defn download-file? [expected]
  {:action-key :download-file?
   :args {:expected expected}})

(def download-file?-impl
  {:run (fn [{:keys [expected]}]
          (is (= expected (last @fake-download-dir))))
   :log (fn [{:keys [expected]}]
          (str "download-file? : " expected))})

(def custom-runtime (-> default-runtime-impl
                        (assoc :i18n {:logs-title "Action Logs"
                                      :failed "FAILED"
                                      :original-error-message "Original Error Message"
                                      :a11y-snapshot "A11y Snapshot"})
                        (add-action :toast-log? toast-log?-impl)
                        (add-action :download-file? download-file?-impl)))

(defn run-custom-siheom! [& lines]
  (let [run-siheom-impl! (sh/init-siheom custom-runtime)]
    (run-siheom-impl! lines)))
