(ns siheom.default-runtime-impl
  (:require
   ["@testing-library/react" :refer [cleanup waitFor]]
   [reagent.core :as r]
   [siheom.query-locator :as q]
   [siheom.testing-library-impl :as test-util]))

(defmulti resolve-story identity)

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

(def wait-duration 0)

(defn wait [duration]
  (new js/Promise (fn [resolve]
                    (js/setTimeout (fn [] (resolve nil)) duration))))

(def ko-dict
  {:logs-title "액션 로그"
   :failed "실패"
   :original-error-message "원본 에러 메시지"
   :a11y-snapshot "접근성 스냅샷"})

(def default-runtime-impl
  {:actions {:render {:run (fn [{:keys [story-title]}]
                             (cleanup)
                             (test-util/render-cp (resolve-story story-title)))
                      :log (fn [{:keys [story-title]}]
                             (str "render: " story-title))}
             :click {:run (fn [{:keys [locator]}]
                            (test-util/click! (fn [] (get-element locator))))
                     :log (fn [{:keys [locator]}]
                            (str "click! " (:role locator) " " (:name locator)))}
             :visible? {:run (fn [{:keys [locator expected]}]
                               (test-util/visible? #(get-element locator (not expected)) expected))
                        :log (fn [{:keys [locator expected]}]
                               (str (if expected "보이는지?: " "안 보이는지?: ") (:role locator) " " (:name locator)))}
             :value? {:run (fn [{:keys [locator expected]}]
                             (test-util/value? #(get-element locator) expected))
                      :log (fn [{:keys [locator expected]}]
                             (str "value? : " (:role locator) " " (:name locator) " [value=" expected "]"))}
             :file? {:run (fn [{:keys [locator expected]}]
                            (test-util/file? #(get-element locator) expected))
                     :log (fn [{:keys [locator expected]}]
                            (str "파일이 " (if expected
                                          (str (.-name expected) " 인지?: ")
                                          "없는지?: ") (:role locator) " " (:name locator)))}
             :count? {:run (fn [{:keys [locator expected]}]
                             (test-util/count? #(get-elements locator) expected))
                      :log (fn [{:keys [locator expected]}]
                             (str expected "개 인지?: " (:role locator) " " (:name locator)))}
             :match-a11y-snapshot? {:run (fn [{:keys [locator file-name]}]
                                           (-> (test-util/match-a11y-snapshot? #(get-element locator) file-name)
                                               (.then (fn [] nil))))
                                    :log (fn [{:keys [locator]}]
                                           (str "접근성 스냅샷과 일치하는지?: " (:role locator) " " (:name locator)))}
             :match-table-snapshot? {:run (fn [{:keys [locator file-name]}]
                                            (-> (test-util/match-table-snapshot? #(get-element locator) file-name)
                                                (.then (fn [] nil))))
                                     :log (fn [{:keys [locator]}]
                                            (str "테이블이 스냅샷과 일치하는지?: " (:role locator) " " (:name locator)))}
             :pressed? {:run (fn [{:keys [locator expected]}]
                               (test-util/pressed? (get-element locator) expected))
                        :log (fn [{:keys [locator expected]}]
                               (str (if expected "눌림?: " "눌리지 않음?: ") (:role locator) " " (:name locator)))}
             :readonly? {:run (fn [{:keys [locator expected]}]
                                (test-util/readonly? (get-element locator) expected))
                         :log (fn [{:keys [locator expected]}]
                                (str "읽기 전용 " expected "? : " (:role locator) " " (:name locator)))}
             :current? {:run (fn [{:keys [locator expected]}]
                               (test-util/current? (get-element locator) expected))
                        :log (fn [{:keys [locator expected]}]
                               (str "현재 " expected "? : " (:role locator) " " (:name locator)))}
             :have-accessible-name? {:run (fn [{:keys [locator expected]}]
                                            (test-util/have-accessible-name? #(get-elements locator) expected))
                                     :log (fn [{:keys [locator expected]}]
                                            (str "라벨 " expected "? : " (:role locator) " " (:name locator)))}
             :have-text-content? {:run (fn [{:keys [locator expected]}]
                                         (test-util/have-text-content? #(get-elements locator) expected))
                                  :log (fn [{:keys [locator expected]}]
                                         (str "텍스트 " expected "? : " (:role locator) " " (:name locator)))}
             :disabled? {:run (fn [{:keys [locator expected]}]
                                (test-util/disabled? #(get-element locator) expected))
                         :log (fn [{:keys [locator expected]}]
                                (str (if expected "비활성화?: " "활성화?: ") (:role locator) " " (:name locator)))}
             :read-only? {:run (fn [{:keys [locator expected]}]
                                 (test-util/read-only? #(get-element locator) expected))
                          :log (fn [{:keys [locator expected]}]
                                 (str (if expected "읽기 전용: " "쓰기 가능?: ") (:role locator) " " (:name locator)))}
             :checked? {:run (fn [{:keys [locator expected]}]
                               (test-util/checked? #(get-element locator) expected))
                        :log (fn [{:keys [locator expected]}]
                               (str (if expected "체크?: " "체크되지 않음?: ") (:role locator) " " (:name locator)))}
             :selected? {:run (fn [{:keys [locator expected]}]
                                (test-util/selected? #(get-element locator) expected))
                         :log (fn [{:keys [locator expected]}]
                                (str (if expected "선택?: " "선택되지 않음?: ") (:role locator) " " (:name locator)))}
             :expanded? {:run (fn [{:keys [locator expected]}]
                                (test-util/expanded? #(get-element locator) expected))
                         :log (fn [{:keys [locator expected]}]
                                (str (if expected "펼쳐짐?: " "닫힘?: ") (:role locator) " " (:name locator)))}
             :error-message? {:run (fn [{:keys [locator expected]}]
                                     (test-util/error-message? #(get-element locator) expected))
                              :log (fn [{:keys [locator expected]}]
                                     (str (if expected (str "에러 메시지가 \"" expected "\" 인지?: ") (str "에러 메세지가 " expected "가  아닌지?: ")) (:role locator) " " (:name locator) " " expected))}
             :have-href? {:run (fn [{:keys [locator expected]}]
                                 (test-util/have-href? #(get-element locator) expected))
                          :log (fn [{:keys [locator expected]}]
                                 (str "href=\"" expected "\" ?: " (:role locator) " " (:name locator)))}
             :dbl-click! {:run (fn [{:keys [locator]}]
                                 (test-util/dbl-click! (fn [] (get-element locator))))
                          :log (fn [{:keys [locator]}]
                                 (str "더블 클릭한다!: " (:role locator) " " (:name locator)))}
             :right-click! {:run (fn [{:keys [locator]}]
                                   (test-util/right-click! (fn [] (get-element locator))))
                            :log (fn [{:keys [locator]}]
                                   (str "우클릭한다!: " (:role locator) " " (:name locator)))}
             :select! {:run (fn [{:keys [locator option-label]}]
                              (-> (test-util/click! (fn [] (get-element locator)))
                                  (.then #(test-util/click! (fn [] (get-element (q/option option-label)))))))
                       :log (fn [{:keys [locator option-label]}]
                              (str "선택한다!: " (:role locator) " " (:name locator) " -> " option-label))}
             :upload! {:run (fn [{:keys [locator file]}]
                              (test-util/upload! (fn [] (get-element locator)) file))
                       :log (fn [{:keys [locator file]}]
                              (str (.-name file) " 파일을 업로드한다: " (:role locator) " " (:name locator)))}
             :screenshot? {:run (fn [{:keys [locator]}]
                                  (test-util/screenshot? (fn [] (get-element locator))))
                           :log (fn [{:keys [locator]}]
                                  (str "스크린샷?: " (:role locator) " " (:name locator)))}
             :hover! {:run (fn [{:keys [locator]}]
                             (test-util/hover! (fn [] (get-element locator))))
                      :log (fn [{:keys [locator]}]
                             (str "호버한다!: " (:role locator) " " (:name locator)))}
             :have-logs? {:run (fn [{:keys [expected]}]
                                 (test-util/have-logs? expected))
                          :log (fn [{:keys [expected]}]
                                 (str "데이터 레이어로그?: " expected))}
             :clear! {:run (fn [{:keys [locator]}]
                             (test-util/clear! (fn [] (get-element locator))))
                      :log (fn [{:keys [locator]}]
                             (str "지운다!: " (:role locator) " " (:name locator)))}
             :type! {:run (fn [{:keys [locator text]}]
                            (test-util/type! (fn [] (get-element locator)) text))
                     :log (fn [{:keys [locator text]}]
                            (str "type! : \"" text "\" to " (:role locator) " \"" (:name locator) "\""))}
             :fill! {:run (fn [{:keys [locator text]}]
                            (test-util/fill! (fn [] (get-element locator)) text))
                     :log (fn [{:keys [locator text]}]
                            (str "\"" text "\"를 채운다!: " (:role locator) " " (:name locator)))}
             :paste! {:run (fn [{:keys [locator text]}]
                             (test-util/paste! (fn [] (get-element locator)) text))
                      :log (fn [{:keys [locator text]}]
                             (str "\"" text "\"를 붙여넣는다!: " (:role locator) " " (:name locator)))}
             :keyboard! {:run (fn [{:keys [text]}]
                                (test-util/keyboard! text))
                         :log (fn [{:keys [text]}]
                                (str "\"" text "\" 키를 누른다!"))}}
   :hooks   {:after-action (fn [{:keys [_name _args]}]
                             (waitFor (fn []
                                        (r/flush)
                                        (wait wait-duration))))}
   :i18n    ko-dict})

(defn add-action [runtime-impl action-key {:keys [run log]}]
  (assert (some? run))
  (assert (some? log))
  (assoc-in runtime-impl [:actions action-key] {:run run :log log}))