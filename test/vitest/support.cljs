(ns vitest.support
  #_{:clj-kondo/ignore [:unused-referred-var]}
  (:require-macros [vitest.support :refer [after-each before-each before-all describe describe-only it it xit]]))

(defn get-a11y-snapshot [dom-element]
  (js/window.getA11ySnapshot dom-element))

(defn table->markdown [table-element]
  (js/tableToMarkdown table-element))

(defn rows->markdown [rows]
  (js/renderRowsToMarkdown rows))