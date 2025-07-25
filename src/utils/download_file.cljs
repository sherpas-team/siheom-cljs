(ns utils.download-file)

(defn download-file
  "파일을 다운로드 합니다."
  [file filename]
  (let [object-url   (js/URL.createObjectURL file)
        link-element (js/document.createElement "a")
        _            (.setAttribute link-element "href" object-url)
        _            (.setAttribute link-element "download" filename)
        _            (.click link-element)
        _            (.remove link-element)]
    (js/URL.revokeObjectURL object-url)))
