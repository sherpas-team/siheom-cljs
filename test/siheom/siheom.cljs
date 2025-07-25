(ns siheom.siheom
  (:require
   ["@testing-library/react" :refer [cleanup]]
   [clojure.string :as string]
   [siheom.default-runtime-impl :refer [default-runtime-impl]]
   [vitest.support :refer [get-a11y-snapshot]]))

(defn render [story-title]
  {:action-key :render
   :args {:story-title story-title}})

(defn click!
  "요소를 클릭합니다. hover 등 클릭을 하면서 일어나야 하는 이벤트도 같이 일어납니다.
   ```clojure
   (sh/click!(q/radio \"평균 비용\"))
   ```
  "
  [locator]
  {:action-key :click
   :args {:locator locator}})

(defn value?
  [locator expected]
  {:action-key :value?
   :args {:locator locator :expected expected}})

(defn file?
  [locator expected]
  {:action-key :file?
   :args {:locator locator :expected expected}})

(defn count?
  "해당하는 요소의 개수가 기대하는 숫자와 같은지 검사하고, 아니면 에러를 던집니다.
  ```clojure
  (sh/click!(q/radio \"제외된 구성원\"))
  ;; 제외된 구성원은 1명
  (sh/count? (q/checkbox #\"구성원\") 1)

  ;; 전체 구성원은 4명
  (sh/click!(q/radio \"전체 구성원\"))
  (sh/count? (q/checkbox #\"구성원\") 4)
  ```
  "
  [locator expected]
  {:action-key :count?
   :args {:locator locator :expected expected}})

(defn visible?
  "요소가 보이는지 체크하고, 보이지 않으면 기다리고, 타임아웃이 지나면 에러를 던집니다.
    
    ```clojure
    (sh/click!(q/checkbox #\"김상현\"))
    ;; 구성원을 제외했어요 토스트가 뜨기를 기다립니다
    (sh/visible? (q/alert \"구성원을 제외했어요\"))
    ```
    "
  ([locator] (visible? locator true))
  ([locator expected]
   {:action-key :visible?
    :args {:locator locator :expected expected}}))

(defn match-a11y-snapshot?
  ([locator file-name]
   {:action-key :match-a11y-snapshot?
    :args {:locator locator :file-name file-name}}))

(defn match-table-snapshot?
  ([locator file-name]
   {:action-key :match-table-snapshot?
    :args {:locator locator :file-name file-name}}))

(defn pressed?
  ([locator]
   (pressed? locator true))
  ([locator expected]
   {:action-key :pressed?
    :args {:locator locator :expected expected}}))

(defn readonly?
  "input 같은 요소가 읽기 전용(readonly)인지 검사하고, 아니면 에러를 던집니다.
      
   ```clojure
   ;; <input readonly=\"true\" ... />

   (readonly? (q/textbox #\"회사 이메일*\"))
   ```"
  ([locator expected]
   {:action-key :readonly?
    :args {:locator locator :expected expected}}))

(defn current?
  "요소가 현재(aria-current) 선택되거나 위치하는 요소인지 검사하고, 아니면 에러를 던집니다.
      
   ```clojure
   ;; <a aria-current=\"false\" ... />
   (sh/current? (q/link #\"김상현\") \"false\")
      
   (sh/click!(q/link #\"김상현\"))

   ;; <a aria-current=\"true\" ... />
   (sh/current? (q/link #\"김상현\") \"true\")
   ```"
  ([locator expected]
   {:action-key :current?
    :args {:locator locator :expected expected}}))

(defn have-accessible-name?
  "여러 요소들의 접근 가능한 이름이 기대하는 패턴이나 문자열 목록에 매칭되는지 검사하고, 아니면 에러를 던집니다. 개수와 순서도 동일해야 합니다.
  
    ```clojure
    (sh/have-accessible-name? (q/checkbox #\"구성원\")
                        [#\"김태희\" #\"김상현\" #\"김현영\" #\"정웅기\"])
  
    (sh/type! (q/combobox \"사람 및 이메일을 입력해주세요\") \"우\")
    (sh/have-accessible-name? (q/checkbox #\"구성원\")
                        [#\"정웅기\" #\"김태희\" #\"김상현\" #\"김현영\"])
     ```
     접근 가능한 이름은 aria-label이나 aria-labelledby, alt 등으로 설정할 수 있습니다.
     button이나 link role은 기본적으로 textContent를 접근 가능한 이름으로 가집니다.
     
     @see https://developer.mozilla.org/ko/docs/Glossary/Accessible_name
     "
  ([locator expected]
   {:action-key :have-accessible-name?
    :args {:locator locator :expected expected}}))

(defn have-text-content?
  "여러 요소들의 textContent가 기대하는 패턴이나 문자열 목록에 매칭되는지 검사하고, 아니면 에러를 던집니다. 개수와 순서도 동일해야 합니다.
  
    ```clojure
    (sh/have-text-content? (q/checkbox #\"구성원\")
                        [#\"김태희\" #\"김상현\" #\"김현영\" #\"정웅기\"])
  
    (sh/type! (q/combobox \"사람 및 이메일을 입력해주세요\") \"우\")
    (sh/have-text-content? (q/checkbox #\"구성원\")
                        [#\"정웅기\" #\"김태희\" #\"김상현\" #\"김현영\"])
     ```"
  ([locator expected]
   {:action-key :have-text-content?
    :args {:locator locator :expected expected}}))

(defn disabled?
  ([locator]
   (disabled? locator true))
  ([locator expected]
   {:action-key :disabled?
    :args {:locator locator :expected expected}}))

(defn read-only?
  ([locator]
   (read-only? locator true))
  ([locator expected]
   {:action-key :read-only?
    :args {:locator locator :expected expected}}))

(defn checked?
  "요소가 체크되었는지 검사하고, 아니면 에러를 던집니다. 두 번째 인자로 true를 넘겨도 동일합니다.
   
   ```html
   <input type=\"radio\" checked=\"true\" />
   ```
   
   ```clojure
   ;; checked가 true인지?
   (sh/checked? (q/radio \"실제 결제\"))
   (sh/checked? (q/radio \"실제 결제\") true)
   ```

   체크되어 있지 않은지를 검사하려면 두 번째 인자로 false를 넘깁니다.
   ```clojure
   (sh/checked? (q/radio \"실제 결제\") false)
   ```
  "
  ([locator]
   (checked? locator true))
  ([locator expected]
   {:action-key :checked?
    :args {:locator locator :expected expected}}))

(defn selected?
  "tab이나 option 등의 role을 가진 요소가 선택되었는지 검사하고, 아니면 에러를 던집니다. 두 번째 인자로 true를 넘겨도 동일합니다.
   
   ```html
   <div role=\"tab\" aria-selected=\"true\" />
   ```
   
   ```clojure
   ;; selected가 true인지?
   (sh/selected? (q/tab \"결제 정보\"))
   (sh/selected? (q/tab \"결제 정보\") true)
   ```

   선택되어 있지 않은지를 검사하려면 두 번째 인자로 false를 넘깁니다.
   ```clojure
   (sh/selected? (q/tab \"결제 정보\") false)
   ```
  "
  ([locator]
   (selected? locator true))
  ([locator expected]
   {:action-key :selected?
    :args {:locator locator :expected expected}}))

(defn expanded?
  "combobox나 accordion 등의 요소가 펼쳐져 있는지, 즉 aria-expanded가 true인지 검사하고, 아니면 에러를 던집니다. 두 번째 인자로 true를 넘겨도 동일합니다.
    
   ```clojure
   (sh/expanded? (q/combobox \"1개월 비용\"))
   (sh/expanded? (q/combobox \"1개월 비용\") true)
   ```

   닫혀있는지를 검사하려면 두 번째 인자로 false를 넘깁니다.
   ```clojure
   (sh/expanded? (q/combobox \"1개월 비용\") false)
   ```
  "
  ([locator]
   (expanded? locator true))
  ([locator expected]
   {:action-key :expanded?
    :args {:locator locator :expected expected}}))

(defn error-message?
  "input이 기대하는 에러메시지를 가지고 있는지 검사합니다."
  [locator expected]
  {:action-key :error-message?
   :args {:locator locator :expected expected}})

(defn have-href?
  "주어진 link 요소가 해당 url을 가지고 있는지 확인합니다.
   ```clojure
   [link {:route (routes/device-detail \"oubcyxzlhapr-01\")]
   ;; ...
   (have-href (q/link \"2402-C001\") \"/device-detail/oubcyxzlhapr-01\")
   ```
  "
  [locator expected]
  {:action-key :have-href?
   :args {:locator locator :expected expected}})

(defn dbl-click!
  "요소를 클릭합니다. hover 등 클릭을 하면서 일어나야 하는 이벤트도 같이 일어납니다.
   ```clojure
   (dblclick! (q/radio \"평균 비용\"))
   ```
  "
  [locator]
  {:action-key :dbl-click!
   :args {:locator locator}})

(defn right-click!
  "요소를 우클릭합니다. 보통 컨텍스트 메뉴를 띄우기 위해 사용합니다
   ```clojure
   (sh/right-click! (q/tab \"사용 중\"))
   ```
  "
  [locator]
  {:action-key :right-click!
   :args {:locator locator}})

(defn select!
  "select 컴포넌트 등을 클릭해서 열고, option을 선택합니다."
  [locator option-label]
  {:action-key :select!
   :args {:locator locator :option-label option-label}})

(defn create-test-file [{:keys [content file-name type size]}]
  (let [file (new js/File
                  (or content (if size
                                #js[(repeat size "h")]
                                #js["hello"]))
                  file-name
                  #js{:type         type
                      :lastModified (new js/Date "2025-06-13T00:00:00.000Z")})]
    file))

(defn upload!
  "이미지나 문서 등의 파일을 업로드합니다.
   ```clojure

   (let [file (create-test-file {:file-name \"hello.png\"
                                 :type \"image/png\"})]
     (sh/upload! (q/radio \"평균 비용\") file))
   ```
  "
  [locator file]
  {:action-key :upload!
   :args {:locator locator :file file}})

(defn screenshot?
  [locator]
  {:action-key :screenshot?
   :args {:locator locator}})

(defn hover!
  "요소를 호버합니다.
   ```clojure
   (hover! (q/link \"자세히\"))
   ```
  "
  [locator]
  {:action-key :hover!
   :args {:locator locator}})

(defn have-logs?
  [expected]
  {:action-key :have-logs?
   :args {:expected expected}})

(defn clear!
  "input이나 textarea 등에 입력된 값을 지웁니다
   ```clojure
   (sh/clear! (q/input \"이메일\"))
   ```
  "
  [locator]
  {:action-key :clear!
   :args {:locator locator}})

(defn type!
  "input이나 textarea 등의 요소에 텍스트를 입력합니다.
   ```clojure
   (sh/type! (q/input \"이메일\") \"admin@sherpas.team\")
   ```
  "
  [locator text]
  {:action-key :type!
   :args {:locator locator :text text}})

(defn fill!
  "input이나 textarea 등의 요소에 기존 텍스트를 지우고 새 텍스트를 채웁니다."
  [locator text]
  {:action-key :fill!
   :args {:locator locator :text text}})

(defn paste!
  "input이나 textarea 등의 요소에 텍스트를 붙여넣습니다.
   ```clojure
   (sh/paste! (q/input \"이메일\") \"admin@sherpas.team\")
   ```
  "
  [locator text]
  {:action-key :paste!
   :args {:locator locator :text text}})

(defn keyboard!
  "주어진 키를 칩니다
   ```clojure
   (sh/keyboard! \"{Enter}\")
   ```
  "
  [text]
  {:action-key :keyboard!
   :args {:text text}})

(defn init-siheom [{:keys [actions hooks i18n]}]
  (fn [lines]
    (let [logs     (atom [])
          after-action (get hooks :after-action)
          dispatch (fn [{:keys [action-key args]}]
                     (let [action (get actions action-key)
                           run! (partial (:run action) args)
                           action-log (:log action)
                           log (if (fn? action-log)
                                 (action-log args)
                                 action-log)]
                       (swap! logs conj log)
                       (-> (js/Promise.resolve)
                           (.then (fn []
                                    (run!))))))]
      (-> (reduce (fn [promise line]
                    (assert (instance? js/Promise promise) (str "promise가 아닙니다 : " @logs))

                    (-> promise
                        (.then (fn []
                                 (dispatch line)))
                        (.then (fn []
                                 (after-action line)))))
                  (js/Promise.resolve nil) lines)
          (.then (fn []
                   (cleanup)))
          (.catch (fn [error]
                    (let [max-length       (apply max (map (fn [log] (.-length log)) @logs))
                          original-message (if (string? error) error (.-message error))
                          index            (or (string/index-of original-message "Here are the accessible roles:")
                                               (string/index-of original-message "Ignored nodes:")
                                               (count original-message))]
                      (set! (.-message (if (string? error) (new js/Error error) error))
                            (str "\n\n[" (get i18n :logs-title) "]\n\n" (string/join "\n" (map #(.padEnd % (inc max-length) " ") @logs)) " <- !! " (get i18n :failed) " !!\n\n\n"
                                 "[" (get i18n :original-error-message) "]\n\n"
                                 (subs original-message 0 index)
                                 "\n[" (get i18n :a11y-snapshot) "]\n\n"
                                 (get-a11y-snapshot js/document.body)
                                 "\n\n")))
                    (throw error)))))))

(defn run-siheom! [& lines]
  (let [run-siheom-impl! (init-siheom default-runtime-impl)]
    (run-siheom-impl! lines)))
