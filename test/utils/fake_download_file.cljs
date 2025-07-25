(ns utils.fake-download-file)

(def fake-download-dir (atom []))

(defn download-file
  "파일을 다운로드 합니다."
  [file filename]
  (assert (instance? js/File file) (str "file이 아닙니다 : " file))
  (swap! fake-download-dir conj {:file     file
                                 :filename filename}))
