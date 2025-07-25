(ns vitest.generate-modules-hook
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]))

(def test-dir "test")

(defn path->namespace [path]
  (-> path
      (string/replace-first (re-pattern (str "^" test-dir "[/\\\\]")) "")
      (string/replace #"\.cljs$" "")
      (string/replace "_" "-")
      (string/replace "/" ".")))

(defn ns->module-name [ns]
  (some-> ns
          (string/replace "smply." "")
          (string/replace "_" "-")
          (string/replace "." "-")))

(defn- find-test-file-paths [dir]
  (->> (io/file dir)
       file-seq
       (filter #(and (.isFile %) (string/ends-with? (.getName %) "_test.cljs")))
       (map #(.getPath %))))

(defn generate-test-modules [old-modules]
  (let [test-modules (->> (find-test-file-paths test-dir)
                          (map (fn [path]
                                 (let [ns          (path->namespace path)
                                       module-name (ns->module-name ns)]
                                   (when module-name
                                     [(keyword module-name)
                                      {:entries    [(symbol ns)]
                                       :depends-on #{:siheom}}]))))
                          (remove nil?)
                          (into {}))]

    (println "[:test - generate-test-modules-hook]" (count test-modules) "tests created!")

    (merge old-modules
           test-modules)))

(defn hook
  {:shadow.build/stage :configure}
  [build-state & _args]
  (-> build-state
      (update-in [:shadow.build/config :modules] generate-test-modules)))
