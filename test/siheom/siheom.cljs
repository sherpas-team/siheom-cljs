(ns siheom.siheom
  (:require
   ["@testing-library/react" :refer [cleanup]]
   [clojure.string :as string]
   [promesa.core :as p]
   [siheom.query-locator :as q]
   [siheom.util :as test-util]
   [vitest.support :refer [get-a11y-snapshot]]))

(defn render [react-element]
  {:run (fn []
          (cleanup)
          (test-util/render-cp react-element))
   :log "컴포넌트 렌더"})

(defn get-element
  ([locator]
   (get-element locator false))
  ([locator optional?]
   (cond
     (:first locator)
     (first (test-util/query-all (:role locator) (:name locator) optional?))
     (:last locator)
     (last (test-util/query-all (:role locator) (:name locator) optional?))
     (:from locator)
     (let [from   (:from locator)
           parent (cond (:dialog from)        (test-util/query "dialog" (:dialog from))
                        (:alertdialog from)   (test-util/query "alertdialog" (:alertdialog from))
                        (:region from)        (test-util/query "region" (:region from))
                        (:group from)         (test-util/query "group" (:group from))
                        (:radiogroup from)    (test-util/query "radiogroup" (:radiogroup from))
                        (:cell from)          (test-util/query "cell" (:cell from))
                        (:menu from)          (test-util/query "menu" (:menu from))
                        (:table from)         (test-util/query "table" (:table from))
                        (:row from)           (test-util/query "row" (:row from))
                        (:list from)          (test-util/query "list" (:list from))
                        (:listitem from)      (test-util/query "listitem" (:listitem from))
                        (:tablist from)       (test-util/query "tablist" (:tablist from))
                        (:rowgroup from)      (test-util/query "rowgroup" (:rowgroup from))
                        (:main from)          (test-util/query "main" (:main from)))]
       (test-util/query-within parent (:role locator) (:name locator) optional?))
     :else
     (test-util/query (:role locator) (:name locator) optional?))))

(defn get-elements [locator]
  (cond (:from locator)
        (let [from   (:from locator)
              parent (cond (:dialog from)        (test-util/query "dialog" (:dialog from))
                           (:alertdialog from)   (test-util/query "alertdialog" (:alertdialog from))
                           (:region from)        (test-util/query "region" (:region from))
                           (:group from)         (test-util/query "group" (:group from))
                           (:radiogroup from)    (test-util/query "radiogroup" (:radiogroup from))
                           (:cell from)          (test-util/query "cell" (:cell from))
                           (:menu from)          (test-util/query "menu" (:menu from))
                           (:table from)         (test-util/query "table" (:table from))
                           (:row from)           (test-util/query "row" (:row from))
                           (:list from)          (test-util/query "list" (:list from))
                           (:listitem from)      (test-util/query "listitem" (:listitem from))
                           (:tablist from)       (test-util/query "tablist" (:tablist from))
                           (:rowgroup from)      (test-util/query "rowgroup" (:rowgroup from))
                           (:main from)          (test-util/query "main" (:main from)))]
          (test-util/query-all-within parent (:role locator) (:name locator)))
        :else (test-util/query-all (:role locator) (:name locator) true)))

(defn value?
  [locator expected]
  {:run (fn [] (test-util/value? #(get-element locator) expected))
   :log (str "값이 " expected " 인지?: " (:role locator) " " (:name locator))})

(defn file?
  [locator expected]
  {:run (fn [] (p/do  (test-util/file? #(get-element locator) expected)
                      nil))
   :log (str "파일이 " (if expected
                      (str (.-name expected) " 인지?: ")
                      "없는지?: ")  (:role locator) " " (:name locator))})

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
  {:run (fn [] (test-util/count? #(get-elements locator) expected))
   :log (str expected "개 인지?: " (:role locator) " " (:name locator))})

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
   {:run (fn [] (test-util/visible? #(get-element locator (not expected)) expected))
    :log (str (if expected "보이는지?: " "안 보이는지?: ") (:role locator) " " (:name locator))}))


(defn match-a11y-snapshot?
  ([locator file-name]
   {:run (fn [] (-> (test-util/match-a11y-snapshot? #(get-element locator) file-name)
                    (.then (fn [] nil))))
    :log (str "접근성 스냅샷과 일치하는지?: " (:role locator) " " (:name locator))}))


(defn match-table-snapshot?
  ([locator file-name]
   {:run (fn [] (-> (test-util/match-table-snapshot? #(get-element locator) file-name)
                    (.then (fn [] nil))))
    :log (str "테이블이 스냅샷과 일치하는지?: " (:role locator) " " (:name locator))}))

(defn pressed?
  ([locator]
   (pressed? locator true))
  ([locator expected]
   {:run (fn [] (p/do  (test-util/pressed? (get-element locator) expected)
                       nil))
    :log (str (if expected "눌림?: " "눌리지 않음?: ") (:role locator) " " (:name locator))}))

(defn readonly?
  "input 같은 요소가 읽기 전용(readonly)인지 검사하고, 아니면 에러를 던집니다.
      
   ```clojure
   ;; <input readonly=\"true\" ... />

   (readonly? (q/textbox #\"회사 이메일*\"))
   ```"
  ([locator expected]
   {:run (fn [] (p/do (test-util/readonly? (get-element locator) expected)
                      nil))
    :log (str "읽기 전용 " expected "? : " (:role locator) " " (:name locator))}))

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
   {:run (fn [] (p/do (test-util/current? (get-element locator) expected)
                      nil))
    :log (str "현재 " expected "? : " (:role locator) " " (:name locator))}))

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
   {:run (fn [] (test-util/have-accessible-name? #(get-elements locator) expected))
    :log (str "라벨 " expected "? : " (:role locator) " " (:name locator))}))


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
   {:run (fn [] (test-util/have-text-content? #(get-elements locator) expected))
    :log (str "텍스트 " expected "? : " (:role locator) " " (:name locator))}))

(defn disabled?
  ([locator]
   (disabled? locator true))
  ([locator expected]
   {:run (fn [] (p/do  (test-util/disabled? #(get-element locator) expected)
                       nil))
    :log (str (if expected "비활성화?: " "활성화?: ") (:role locator) " " (:name locator))}))


(defn read-only?
  ([locator]
   (read-only? locator true))
  ([locator expected]
   {:run (fn [] (p/do  (test-util/read-only? #(get-element locator) expected)
                       nil))
    :log (str (if expected "읽기 전용: " "쓰기 가능?: ") (:role locator) " " (:name locator))}))

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
   {:run (fn [] (p/do  (test-util/checked? #(get-element locator) expected)
                       nil))
    :log (str (if expected "체크?: " "체크되지 않음?: ") (:role locator) " " (:name locator))}))

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
   {:run (fn [] (p/do  (test-util/selected? #(get-element locator) expected)
                       nil))
    :log (str (if expected "선택?: " "선택되지 않음?: ") (:role locator) " " (:name locator))}))

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
   {:run (fn [] (p/do  (test-util/expanded? #(get-element locator) expected)
                       nil))
    :log (str (if expected "펼쳐짐?: " "닫힘?: ") (:role locator) " " (:name locator))}))

(defn error-message?
  "input이 기대하는 에러메시지를 가지고 있는지 검사합니다."
  [locator expected]
  {:run (fn [] (p/do  (test-util/error-message? #(get-element locator) expected)
                      nil))
   :log (str (if expected (str "에러 메시지가 \"" expected "\" 인지?: ") (str "에러 메세지가 " expected "가  아닌지?: ")) (:role locator) " " (:name locator) " " expected)})

(defn have-href?
  "주어진 link 요소가 해당 url을 가지고 있는지 확인합니다.
   ```clojure
   [link {:route (routes/device-detail \"oubcyxzlhapr-01\")]
   ;; ...
   (have-href (q/link \"2402-C001\") \"/device-detail/oubcyxzlhapr-01\")
   ```
  "
  [locator expected]
  {:run (fn [] (p/do (test-util/have-href? #(get-element locator) expected)
                     nil))
   :log (str "href=\"" expected "\" ?: " (:role locator) " " (:name locator))})

(defn click!
  "요소를 클릭합니다. hover 등 클릭을 하면서 일어나야 하는 이벤트도 같이 일어납니다.
   ```clojure
   (sh/click!(q/radio \"평균 비용\"))
   ```
  "
  [locator]
  {:run (fn []
          (test-util/click! (fn [] (get-element locator))))
   :log (str "클릭한다!: " (:role locator) " " (:name locator))})

(defn dbl-click!
  "요소를 클릭합니다. hover 등 클릭을 하면서 일어나야 하는 이벤트도 같이 일어납니다.
   ```clojure
   (dblclick! (q/radio \"평균 비용\"))
   ```
  "
  [locator]
  {:run (fn []
          (test-util/dbl-click! (fn [] (get-element locator))))
   :log (str "더블 클릭한다!: " (:role locator) " " (:name locator))})

(defn right-click!
  "요소를 우클릭합니다. 보통 컨텍스트 메뉴를 띄우기 위해 사용합니다
   ```clojure
   (sh/right-click! (q/tab \"사용 중\"))
   ```
  "
  [locator]
  {:run #(test-util/right-click! (fn [] (get-element locator)))
   :log (str "우클릭한다!: " (:role locator) " " (:name locator))})


(defn select!
  "select 컴포넌트 등을 클릭해서 열고, option을 선택합니다."
  [locator option-label]
  {:run (fn []
          (-> (test-util/click! (fn [] (get-element locator)))
              (.then #(test-util/click! (fn [] (get-element (q/option option-label)))))))
   :log (str "선택한다!: " (:role locator) " " (:name locator) " -> " option-label)})


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
  {:run #(test-util/upload! (fn [] (get-element locator)) file)
   :log (str (.-name file) " 파일을 업로드한다: " (:role locator) " " (:name locator))})


(defn screenshot?
  [locator]
  {:run #(test-util/screenshot? (fn [] (get-element locator)))
   :log (str "스크린샷?: " (:role locator) " " (:name locator))})

(defn hover!
  "요소를 호버합니다.
   ```clojure
   (hover! (q/link \"자세히\"))
   ```
  "
  [locator]
  {:run #(test-util/hover! (fn [] (get-element locator)))
   :log (str "호버한다!: " (:role locator) " " (:name locator))})

(defn have-logs?
  [expected]
  {:run #(test-util/have-logs? expected)
   :log (str "데이터 레이어로그?: " expected)})

(defn clear!
  "input이나 textarea 등에 입력된 값을 지웁니다
   ```clojure
   (sh/clear! (q/input \"이메일\"))
   ```
  "
  [locator]
  {:run (fn []
          (test-util/clear! (fn [] (get-element locator))))
   :log (str "지운다!: " (:role locator) " " (:name locator))})

(defn type!
  "input이나 textarea 등의 요소에 텍스트를 입력합니다.
   ```clojure
   (sh/type! (q/input \"이메일\") \"admin@sherpas.team\")
   ```
  "
  [locator text]
  {:run #(test-util/type! (fn [] (get-element locator)) text)
   :log (str "\"" text "\"를 입력한다!: " (:role locator) " " (:name locator))})

(defn fill!
  "input이나 textarea 등의 요소에 기존 텍스트를 지우고 새 텍스트를 채웁니다."
  [locator text]
  {:run #(test-util/fill! (fn [] (get-element locator)) text)
   :log (str "\"" text "\"를 채운다!: " (:role locator) " " (:name locator))})

(defn paste!
  "input이나 textarea 등의 요소에 텍스트를 붙여넣습니다.
   ```clojure
   (sh/paste! (q/input \"이메일\") \"admin@sherpas.team\")
   ```
  "
  [locator text]
  {:run #(test-util/paste! (fn [] (get-element locator)) text)
   :log (str "\"" text "\"를 붙여넣는다!: " (:role locator) " " (:name locator))})

(defn log!
  [text]
  {:run (fn []
          (js/Promise.resolve nil))
   :log text})

(defn run-custom!
  [run log]
  {:run run
   :log log})

(defn keyboard!
  "주어진 키를 칩니다
   ```clojure
   (sh/keyboard! \"{Enter}\")
   ```
  "
  [text]
  {:run #(test-util/keyboard! text)
   :log (str "\"" text "\"를 입력한다!")})

(defn run-siheom [& lines]
  (let [logs     (atom [])
        dispatch (fn [{:keys [log run]}]
                   (swap! logs conj log)
                   (p/do (run)))]
    (-> (reduce (fn [promise line]
                  (assert (instance? js/Promise promise) (str "promise가 아닙니다 : " @logs))

                  (-> promise
                      (.then (fn []
                               (dispatch line)))))
                (js/Promise.resolve nil) lines)
        (.then (fn []
                 (cleanup)))
        (p/catch (fn [error]
                   (let [max-length       (apply max (map (fn [log] (.-length log)) @logs))
                         original-message (if (string? error) error (.-message error))
                         index            (or (string/index-of original-message "Here are the accessible roles:")
                                              (string/index-of original-message "Ignored nodes:")
                                              (count original-message))]
                     (set! (.-message (if (string? error) (new js/Error error) error))
                           (str "\n[시험 로그]\n" (string/join "\n" (map #(.padEnd % (inc max-length) " ") @logs)) " <- !!여기서 실패!!\n\n"
                                "[원본 에러 메세지]\n"
                                (subs original-message 0 index)
                                "\n[접근성 스냅샷]\n"
                                (get-a11y-snapshot js/document.body)
                                "\n\n")))
                   (throw error))))))
