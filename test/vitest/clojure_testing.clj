(ns vitest.clojure-testing)

;; compile time
(def test? (= (System/getenv "APP_ENV") "test"))

(defmacro deftest
  "A macro that mimics clojure.test/deftest but uses Vitest's describe.
   Usage: (deftest test-name & body)"
  [name & body]
  (if test?
    `(js/describe ~(str name)
                  (fn []
                    ~@body))
    `(cljs.test/deftest ~name ~@body)))

(defmacro testing
  "A macro that mimics clojure.test/testing but uses Vitest's it.
   This is mainly used within deftest blocks.
   Usage: (testing description & body)"
  [description & body]
  (if test?
    `(js/it ~description
            (fn []
              ~@body))
    `(cljs.test/testing ~description
       ~@body)))


(defmacro is
  "Generic assertion macro that uses assert-equal. 'form' is any predicate test.
   'msg' is an optional message to attach to the assertion.
   
   Example: (is (= 4 (+ 2 2)) \"Two plus two should be 4\")"
  ([form] `(is ~form nil))
  ([form msg]
   (let [[operator a b] form
         a-str          (str a)]
     (if test?
       `(cond (= ~operator =)
              (do (.toStrictEqual (js/expect (cljs.core/clj->js ~a)) (cljs.core/clj->js ~b)) nil)
              (= ~operator not=)
              (do (.not.toStrictEqual (js/expect (cljs.core/clj->js ~a)) (cljs.core/clj->js ~b)) nil)
              (= ~operator true?)
              (assert (= ~a true) (str "(not= " ~a-str " true)"))
              (= ~operator false?)
              (assert (= ~a false) (str "(not= " ~a-str " false)"))
              :else
              (throw (js/Error. (str "Unsupported operator: " ~operator))))
       `(cljs.test/is ~form ~msg)))))