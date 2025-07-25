(ns siheom.examples.form-story
  (:require
   [uix.core :as uix :refer [$ defui]]))

(defui form-story [{:keys [submit-message!]}]
  (let [[value set-value] (uix/use-state "")
        [submitted? set-submitted] (uix/use-state false)
        on-submit (fn []
                    (-> (submit-message! value)
                        (.then (fn []
                                 (set-submitted true)))))]
    ($ :form {:on-submit (fn [e]
                           (.preventDefault e)
                           (on-submit))}
       ($ :label {}
          "Message"
          ($ :input {:value value
                     :data-testid "form-input"
                     :on-change #(set-value (.. % -target -value))}))
       ($ :button {:type "submit"
                   :data-testid "form-submit-button"}
          "Submit")
       (when submitted?
         ($ :p {:data-testid "form-submitted-message"}
            "Submitted!")))))