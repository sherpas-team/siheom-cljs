(ns utils.toast)

(defn toast! [text]
  (js/alert text))