(ns siheom.testing-library-impl
  (:require
   ["@testing-library/react" :refer [fireEvent render screen waitFor within]]
   ["@testing-library/user-event" :default user-event]
   ["accname" :refer [getAccessibleName]]
   [clojure.string :as string]
   [reagent.core :as r]
   [vitest.support :refer [get-a11y-snapshot rows->markdown table->markdown]]))

(def wait-duration 0)

(defn wait [duration]
  (waitFor (fn []
             (new js/Promise (fn [resolve]
                               (js/setTimeout (fn [] (resolve nil)) duration))))))

(defn render-cp [element]
  (let [el (r/as-element element)]
    (-> (waitFor (fn []
                   (render el)
                   (js/Promise.resolve nil)))
        (.then #(wait wait-duration)))))

(defn query-within
  ([parent role name]
   (query-within parent role name false))
  ([parent role name optional?]
   (let [p (within parent)]
     (if (= role "text")
       (if optional?
         (.queryByText p name)
         (.getByText p name))
       (if optional?
         (.queryByRole p role #js{:name name})
         (.getByRole p role #js{:name name}))))))

(defn query-all-within
  ([parent role name]
   (query-all-within parent role name false))
  ([parent role name optional?]
   (let [p (within parent)]
     (if (= role "text")
       (if optional?
         (.queryAllByText p name)
         (.getAllByText p name))
       (if optional?
         (.queryAllByRole p role #js{:name name})
         (.getAllByRole p role #js{:name name}))))))

(defn query
  ([role name]
   (query role name false))
  ([role name optional?]
   (case role
     "text"
     (if optional?
       (.queryByText screen name)
       (.getByText screen name))
     "label"
     (if optional?
       (.queryByLabelText screen name)
       (.getByLabelText screen name))
     (if optional?
       (.queryByRole screen role #js{:name name})
       (.getByRole screen role #js{:name name})))))

(defn query-all
  ([role name]
   (query-all role name false))
  ([role name optional?]
   (if optional?
     (or (.queryAllByRole screen role #js{:name name}) [])
     (.getAllByRole screen role #js{:name name}))))

(defn visible?
  [get-element expected]
  (waitFor (fn []
             (-> (wait 0)
                 (.then (fn []
                          (if expected
                            (get-element)
                            (assert (= (get-element) nil) "element is visible"))
                          nil))
                 (.then #(wait wait-duration))))
           #js{:timeout 3000}))

(defn disabled?
  [get-element expected]
  (waitFor #(if expected
              (assert (or (= (.getAttribute (get-element) "disabled") "")
                          (= (.getAttribute (get-element) "aria-disabled") "true"))
                      (.-outerHTML (get-element)))
              (assert (and (not= (.getAttribute (get-element) "disabled") "")
                           (not= (.getAttribute (get-element) "aria-disabled") "true"))
                      (.-outerHTML (get-element))))))

(defn click!
  [get-element]
  (waitFor (fn []
             (let [element (get-element)]
               (-> (visible? get-element true) ;; 보일 때까지 기다린다
                   (.then #(disabled? get-element false))
                   (.then #(.click user-event element))
                   (.then (fn []
                            (if (= (.-tagName element) "A")
                              (wait 100)
                              (wait wait-duration))))
                   (.then #(wait wait-duration)))))
           #js{:timeout 3000}))

(defn dbl-click!
  [get-element]
  (waitFor (fn []
             (let [element (get-element)]
               (-> (visible? get-element true) ;; 보일 때까지 기다린다
                   (.then #(disabled? get-element false))
                   (.then #(.dblClick user-event element))
                   (.then (fn []
                            (if (= (.-tagName element) "A")
                              (wait 100)
                              (wait wait-duration))))
                   (.then #(wait wait-duration)))))
           #js{:timeout 3000}))

(defn right-click!
  [get-element]
  (waitFor (fn []
             (-> (visible? get-element true) ;; 보일 때까지 기다린다
                 (.then #(disabled? get-element false))
                 (.then #(.click user-event (get-element) #js{:button 2}))
                 (.then #(.contextMenu fireEvent (get-element)))
                 (.then #(wait wait-duration))))
           #js{:timeout 3000}))

(defn upload!
  [get-element file]
  (waitFor (fn []
             (-> (visible? get-element true) ;; 보일 때까지 기다린다
                 (.then #(.upload user-event (get-element) file))
                 (.then #(wait wait-duration))))
           #js{:timeout 3000}))


(defn match-a11y-snapshot? [get-element file-name]
  (-> (visible? get-element true) ;; 보일 때까지 기다린다
      (.then (fn []
               (waitFor (fn []
                          (-> (js/expect (get-a11y-snapshot (get-element)))
                              (.toMatchFileSnapshot file-name)))
                        #js{:timeout 3000})))))

(defn match-table-snapshot? [get-element file-name]
  (-> (visible? get-element true) ;; 보일 때까지 기다린다
      (.then (fn []
               (waitFor (fn []
                          (-> (js/expect (table->markdown (get-element)))
                              (.toMatchFileSnapshot file-name)))
                        #js{:timeout 1000})))))

(defn have-logs? [expected]
  (waitFor (fn []
             (-> (js/expect js/dataLayer)
                 (.toStrictEqual (clj->js expected))))))


(defn screenshot? [get-element]
  (-> (visible? get-element true) ;; 보일 때까지 기다린다
      (.then (fn []
               #_(-> (js/expect (get-element))
                     (.toMatchImageSnapshot #js{:failureThreshold     0.1
                                                :failureThresholdType "percent"}))))))

(defn hover!
  [get-element]
  (waitFor (fn []
             (-> (visible? get-element true) ;; 보일 때까지 기다린다
                 (.then #(.hover user-event (get-element)))
                 (.then #(wait wait-duration))))))

(defn clear!
  "input이나 textarea 등의 요소를 지웁니다."
  [get-element]
  (waitFor (fn []
             (-> (visible? get-element true)
                 (.then #(.clear user-event (get-element)))
                 (.then #(wait wait-duration))))))

(defn keyboard!
  "주어진 키를 누릅니다."
  [key]
  (waitFor (fn []
             (let [promise (.keyboard user-event key)]
               (.then promise (fn [] (r/flush)))))))

(defn type!
  "input이나 textarea 등의 요소에 텍스트를 입력합니다."
  [get-element text]
  (waitFor (fn []
             (-> (visible? get-element true)
                 (.then #(.type user-event (get-element) text))
                 (.then #(wait wait-duration))))
           #js{:timeout 3000}))


(defn fill!
  "input이나 textarea 등의 요소에 기존 텍스트를 지우고 새 텍스트를 입력합니다."
  [get-element text]
  (-> (visible? get-element true) ;; 보일 때까지 기다린다
      (.then #(disabled? get-element false))
      (.then (fn []
               (waitFor (fn []
                          (-> (.clear user-event (get-element))
                              (.then #(.focus (get-element)))
                              (.then #(.paste user-event text))
                              (.then #(wait wait-duration)))))))))


(defn paste!
  "input이나 textarea 등의 요소에 텍스트를 붙여넣습니다."
  [get-element text]
  (waitFor (fn []
             (-> (visible? get-element true)
                 (.then #(.focus (get-element)))
                 (.then #(.paste user-event text))
                 (.then #(wait wait-duration))))
           #js{:timeout 3000}))

(defn value?
  [get-element expected]
  (-> (visible? get-element true)
      (.then (fn []
               (waitFor (fn []
                          (if (= (.-tagName (get-element)) "INPUT")
                            (if (nil? (.-value (get-element)))
                              (-> (js/expect (get-element))
                                  (.toHaveAttribute "value" expected))
                              (-> (js/expect (get-element))
                                  (.toHaveValue expected)))
                            (-> (js/expect (get-element))
                                (.toHaveTextContent expected)))))))))

(defn file?
  [get-element expected]
  (waitFor #(if (nil? expected)
              (-> (js/expect (.-files (get-element)))
                  (.toHaveLength 0))
              (-> (js/expect (.item (.-files (get-element)) 0))
                  (.toBe expected)))))

(defn count?
  [get-elements expected]
  (waitFor (fn []
             (-> (js/expect (get-elements))
                 (.toHaveLength expected)))))

(defn checked?
  [get-element expected]
  (-> (js/Promise.resolve)
      (.then (fn []
               (waitFor (fn []
                          (if expected
                            (-> (js/expect (get-element))
                                (.toBeChecked))
                            (-> (js/expect (get-element))
                                (.-not)
                                (.toBeChecked))))
                        #js{:timeout 3000})))))

(defn selected?
  [get-element expected]
  (waitFor #(if expected
              (assert (or (= (.getAttribute (get-element) "aria-selected") "")
                          (= (.getAttribute (get-element) "aria-selected") "true")))
              (and (not= (.getAttribute (get-element) "aria-selected") "")
                   (not= (.getAttribute (get-element) "aria-selected") "true")))))


(defn read-only?
  [get-element expected]
  (waitFor #(if expected
              (-> (js/expect (get-element))
                  (.toHaveAttribute "readonly" ""))
              (-> (js/expect (get-element))
                  (.-not)
                  (.toHaveAttribute "readonly" "")))))

(defn current?
  [element expected]
  (waitFor #(-> (js/expect element)
                (.toHaveAttribute "aria-current" expected))))

(defn pressed?
  [element expected]
  (waitFor #(if expected
              (-> (js/expect element)
                  (.toHaveAttribute "aria-pressed" "true"))
              (-> (js/expect element)
                  (.-not)
                  (.toHaveAttribute  "aria-pressed" "true")))))

(defn readonly?
  [element expected]
  (waitFor (fn []
             (if expected
               (-> (js/expect element)
                   (.toHaveAttribute "readonly"))
               (-> (js/expect element)
                   (.-not)
                   (.toHaveAttribute "readonly"))))))

(defn expanded?
  [get-element expected]
  (waitFor #(if expected
              (-> (js/expect (get-element))
                  (.toHaveAttribute "aria-expanded" "true"))
              (-> (js/expect (get-element))
                  (.-not)
                  (.toHaveAttribute  "aria-expanded" "true")))))

(defn error-message?
  [get-element expected]
  (waitFor (fn []
             (-> (js/expect (get-element))
                 (.toHaveAttribute "aria-invalid" "true"))
             (-> (js/expect (get-element))
                 (.toHaveAccessibleErrorMessage expected)))))

(defn have-href?
  [get-element expected]
  (waitFor #(-> (js/expect (get-element))
                (.toHaveAttribute "href" expected))))

(defn format-vect-vertical [vect]
  (str "[\n   "
       (->> vect
            (map (fn [item]
                   (cond
                     (string? item)
                     (str "\"" item "\"")
                     (regexp? item)
                     (str item)
                     :else item)))
            (string/join "\n    "))

       "\n  ]"))

(defn have-accessible-name?
  [get-elements expected]
  (waitFor (fn []
             (let [actual (vec (map getAccessibleName
                                    (get-elements)))]
               (assert (= (count actual) (count expected))
                       (str "(not= \n  " (format-vect-vertical actual) " \n  " (format-vect-vertical expected) " )"))
               (assert (every? (fn [[b a]]
                                 (if (regexp? b)
                                   (some? (re-find b a))
                                   (= a b))) (map vector expected actual))
                       (str "(not= \n  " (format-vect-vertical actual) " \n  " (format-vect-vertical expected) " )"))))
           #js{:timeout 3000}))


(defn have-text-content?
  [get-elements expected]
  (waitFor (fn []
             (let [actual (vec (map #(.-textContent %)
                                    (get-elements)))]
               (assert (= (count actual) (count expected))
                       (str "(not= \n  " (format-vect-vertical actual) " \n  " (format-vect-vertical expected) " )"))
               (assert (every? (fn [[b a]]
                                 (if (regexp? b)
                                   (some? (re-find b a))
                                   (= a b))) (map vector expected actual))
                       (str "(not= \n" (format-vect-vertical actual) " \n  " (format-vect-vertical expected) " )")))
             (wait 0))
           #js{:timeout 3000}))

(defn assert-equal [a b]
  (.toStrictEqual (js/expect (clj->js a)) (clj->js b))
  (js/Promise.resolve nil))

(defn dict->markdown [dict]
  (->> dict
       (map (fn [[sheet-name {:keys [columns data]}]]

              (str sheet-name
                   "\n"
                   (rows->markdown (clj->js (concat [columns]
                                                    (mapv (fn [row]
                                                            (->> columns
                                                                 (mapv (fn [col]
                                                                         (str (or (get row col) ""))))))
                                                          data)))))))
       (string/join "\n")))

(defn try-error-message [f]
  (try
    (f)
    (catch js/Error e
      (.-message e))))
