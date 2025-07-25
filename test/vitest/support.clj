(ns vitest.support)

(defmacro before-each
  [& body]
  `(js/beforeEach
    (fn []
      ~@body)))

(defmacro before-all
  [& body]
  `(js/beforeAll
    (fn []
      ~@body)))

(defmacro after-each
  [& body]
  `(js/afterEach
    (fn []
      ~@body)))

(defmacro describe
  ([title & body]
   `(js/describe
     ~title
     (fn []
       ~@body))))

(defmacro describe-only
  ([title & forms]
   `(js/describe.only
     ~title
     (fn []
       ~@forms))))

(defmacro it
  ([title & body]
   `(js/it
     ~title
     (fn []
       ~@body))))

(defmacro xit
  [title & body]
  `(js/it.skip
    ~title
    (fn []
      ~@body)))

(defmacro it-only
  ([title & body]
   `(js/it.only
     ~title
     (fn []
       ~@body))))