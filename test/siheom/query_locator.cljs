(ns siheom.query-locator)


(defn query [role name]
  {:role role
   :name name})

;; TODO: add document
(defn query-from [role name from]
  {:role role
   :name name
   :from from})

(defn query-first [role name]
  {:first true
   :role  role
   :name  name})

(defn query-last [role name]
  {:last true
   :role role
   :name name})

(defn banner [name]
  {:role "banner"
   :name name})

(defn main [name]
  {:role "main"
   :name name})

(defn navigation [name]
  {:role "navigation"
   :name name})

(defn button [name]
  {:role "button"
   :name name})

(defn link [name]
  {:role "link"
   :name name})

(defn form [name]
  {:role "form"
   :name name})

(defn textbox [name]
  {:role "textbox"
   :name name})

(defn combobox [name]
  {:role "combobox"
   :name name})

(defn option [name]
  {:role "option"
   :name name})

(defn checkbox [name]
  {:role "checkbox"
   :name name})

(defn radio [name]
  {:role "radio"
   :name name})

(defn radiogroup [name]
  {:role "radiogroup"
   :name name})

(defn status [name]
  {:role "status"
   :name name})


(defn role-list [name]
  {:role "list"
   :name name})

(defn listbox [name]
  {:role "listbox"
   :name name})

(defn listitem [name]
  {:role "listitem"
   :name name})

(defn tablist [name]
  {:role "tablist"
   :name name})

(defn tab [name]
  {:role "tab"
   :name name})

(defn tabpanel [name]
  {:role "tabpanel"
   :name name})

(defn dialog [name]
  {:role "dialog"
   :name name})

(defn alertdialog [name]
  {:role "alertdialog"
   :name name})

(defn heading [name]
  {:role "heading"
   :name name})

(defn paragraph [name]
  {:role "paragraph"
   :name name})

(defn region [name]
  {:role "region"
   :name name})

(defn group [name]
  {:role "group"
   :name name})

(defn menu [name]
  {:role "menu"
   :name name})

(defn menuitem [name]
  {:role "menuitem"
   :name name})

(defn menuitemradio [name]
  {:role "menuitemradio"
   :name name})

(defn menuitemcheckbox [name]
  {:role "menuitemcheckbox"
   :name name})

(defn table [name]
  {:role "table"
   :name name})

(defn columnheader [name]
  {:role "columnheader"
   :name name})

(defn row [name]
  {:role "row"
   :name name})

(defn cell [name]
  {:role "cell"
   :name name})

(defn alert [name]
  {:role "alert"
   :name name})

(defn img [name]
  {:role "img"
   :name name})

;; role이 아니지만 role처럼 쓰는 경우
(defn text [name]
  {:role "text"
   :name name})

(defn label [name]
  {:role "label"
   :name name})
