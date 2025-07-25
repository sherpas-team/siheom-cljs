
# Siheom Test Guidelines

## 0. Basic Configuration and Structure

### 0.1 Test Namespace Setup
- Test namespaces follow the `*-test` naming convention.
- Import `deftest` and `testing` functions from the `vitest.clojure-testing` namespace for testing.
- Import actual test action and query functions (`render`, `run-siheom`, `click!`, `type!`, `query`, `visible?`, `navigate!`, etc.) from `siheom.siheom`.

```clojure
(ns app.example-test
  (:require
   [app.example :refer [example-component]]
   [siheom.siheom :as sh] ; Siheom functions
   [vitest.clojure-testing :refer [deftest testing is]])) ; Vitest-compatible testing functions
```

## 1. Test Setup

### 1.1 Creating Fixtures
- Define test data in separate `fixtures.cljs` files to improve reusability. (Example: `stories/app.fixtures.fixture-member.cljs` [fixtures.cljs](stories/app.fixtures.fixture-member.cljs))
- Name fixtures meaningfully and descriptively (e.g., `base-member`, `deactivated-member`).
- Share the same fixtures between tests and Storybook to maintain consistency.

### 1.2 Creating and Utilizing Storybook
- Create Storybook stories that visually represent various states of components.
- Use Storybook components directly in tests to ensure consistency between visual representation and test scenarios.
- Instead of mocking for testing dynamic behavior, create simple fake implementations using atoms and other state management in Storybook.

```clojure
(defn member-detail-header-story [{:keys [member]}]
  (let [deactivated-atom (r/atom (:deactivated? member))]
    (fn []
      [tab-provider {:selected-id "saas"}
       [:div.base-layout
        [:div.base-body {}
         [member-detail-header {:member             (assoc member
                                                               :deactivated? @deactivated-atom)
                                  :saas-count           1
                                  :device-count         1
                                  :deactivate-member! (fn [_member-id deactivated?]
                                                          (reset! deactivated-atom deactivated?))}]]]])))

```

## 2. Test Writing Rules

### 2.1 Utilizing Story Components
- Create reusable test story components for complex components.
- Stories should include both component state and behavior.
- Use story components directly in tests to maintain consistency.

```clojure
;; Good
(sh/render [category-field-setting-form-story])

;; Bad - Don't create logic directly in test
(let [state-atom (r/atom initial-state)]
  (sh/render [category-field-setting-form {:state atom}]))
```

### 2.2 Utilizing WAI-ARIA Roles
- Use WAI-ARIA roles instead of CSS selectors when querying components.
- You can also use regex patterns to match dynamic text.
- Examples: `(q/tab "All")`, `(q/combobox #"User")`, `(q/option #"Kim Sanghyun")`

```clojure
;; Good
(sh/disabled? (q/button "Save"))

;; Bad - Don't use CSS selectors
(sh/visible? (q/query "button[disabled]" "Save"))
```

The list of WAI-ARIA roles we use is as follows:

```
alert
alertdialog
article
banner
button
cell
checkbox
columnheader
combobox
dialog
form
grid
gridcell
heading
link
list
listbox
mark
math
menu
menubar
menuitem
menuitemcheckbox
menuitemradio
navigation
option
progressbar
radio
radiogroup
region
row
rowgroup
rowheader
scrollbar
search
searchbox
tab
table
tablist
tabpanel
textbox
toolbar
tooltip
```

We do not use the following abstract roles:
```
command, composite, input, landmark, range, roletype, section, sectionhead, select, structure, widget, window
```

Select components use the combobox role:
```clojure
(sh/select! (q/combobox #"Position") "CEO")
```

### 2.3 Avoiding Unnecessary visible? Checks
- Functions like click!, checked?, etc. already implicitly test element visibility.
- Avoid unnecessary visible? checks.

```clojure
;; Good
(sh/click! (q/query-from "checkbox" "RAM" {:group "Laptop"}))

;; Bad - Redundant visibility check
(sh/visible? (q/text "Laptop"))
(sh/click! (q/query-from "checkbox" "RAM" {:group "Laptop"}))
```

### 2.4 Testing Grouped Fields
- Use field labels instead of field names when testing form fields.
- When using query-from, use actual labels for the group attribute.

```clojure
;; Good
(sh/checked? (q/query-from "checkbox" "OS" {:group "Laptop"}))

;; Bad - Don't use field names
(sh/checked? (q/query-from "checkbox" "OS" {:group "category-fields.Laptop"}))
```

### 2.5 User Interaction Testing
- Primary interactions are `click!` and `type!`.
- Test context menus with `right-click!`.
- Represent special key inputs in {key} format: `"text{Enter}"`, `"text{Escape}"`
- Use `clear!` to reset input fields.

```clojure
(sh/right-click! (q/button "tab2")) ;; Opens context menu
(sh/clear! (q/textbox "Tab name")) ;; Clears the field input
(sh/type! (q/textbox "Tab name") "NewName{Enter}") ;; Types "NewName" and presses Enter
```

### 2.6 Visibility Testing
- The `visible?` function can take false as a second argument to test invisibility.
- When testing that elements are not visible, use `(sh/visible? element false)` instead of `not-visible?`.

```clojure
(sh/visible? (q/tab "New Tab") false)  ;; Recommended
(sh/not-visible? (q/tab "New Tab"))    ;; Not recommended
```

### 2.7 Side Effect Testing
- Test closing behaviors of UI elements like context menus or modals.
- Verify state changes from external clicks or Escape key inputs.

```clojure
(testing "Context menu can be closed by clicking elsewhere on the screen"
    (sh/run-siheom
     (sh/render [tabs-story])
     (sh/right-click! (q/button "tab2"))
     (sh/visible? (q/menu "") true)
     (sh/click! (q/button "Outside"))
     (sh/visible? (q/menu "") false)))
```

### 2.8 Using Test Doubles
- Stub: Objects that provide pre-prepared responses, mainly used for state verification.
- Fake: Objects that simplify actual implementations. Example: Fake APIs with state defined in [handlers.cljs](src/app/msw/handlers.cljs), fake in-memory routers.
- Whenever possible, use Storybook components and fake implementations with atoms instead of mocks to test state changes.

### 2.9 Link Testing

There are two ways to test links:

- Render pages according to routes for mocking, actually click links and navigate
- Verify that links have the expected href

```clojure
;; Actually clicking links
(sh/click! (q/link "Details"))

;; Verifying href
(sh/have-href? (q/link "Details") (rfe/href :member-detail {:member-id (member/id gw-deleted-member)}))
```

### 2.10 Page Loading and Routing Testing
- When testing page components that depend on specific routes and parameters, use the `navigate!` function from `siheom.siheom` to set routing state.
- `navigate!` executes the router and related loaders to ensure pages render in an environment similar to actual usage scenarios.
- `path-params-track` is read-only, so it cannot be modified directly like an atom, making it impossible to test from a user perspective.

```clojure
;; Good - Use navigate! to set route and params
(testing "Displays approved message for approved invitation"
  (sh/run-siheom
   (sh/navigate! :invitation-detail {:path-params {:invitation-id "approved-id"}})
   (sh/render [invitation-detail])
   (sh/visible? (q/heading "Your invitation has been approved."))))

;; Bad - Don't manipulate internal atoms directly
(testing "Displays approved message (Incorrect Setup)"
  (reset! path-params-track {:invitation-id "approved-id"}) ; Avoid this
  (sh/run-siheom
   ;; Loader might not run correctly, leading to inconsistent state
   (sh/render [invitation-detail])
   (sh/visible? (q/heading "Your invitation has been approved."))))
```

## 3. Examples

The following is an actual test code example from `new_member_form_test.cljs`:

```clojure
(deftest your-component-test
  (testing "Shows success toast when form is submitted"
      (sh/run-siheom
       (sh/render [your-form-component])
       ;; (sh/visible? (q/label "Name"))

       ;; Test error cases
       (sh/click! (q/button "Submit"))
       (sh/visible? (q/alert "Please enter a name"))

       ;; Test correct cases that cause side effects
       (sh/type! (q/textbox "Name") "Kim Sanghyun")
       (sh/type! (q/textbox "Email") "tlonist@sherpas.team")
       
       (sh/click! (q/button "Submit"))
       ;; Verify success toast
       (sh/toast-log? "Form submitted successfully!")
       ;; Code like (wait-for #(sh/visible? (q/alert "Form submitted successfully!"))) is not needed.
)))
```

## 4. Precautions

### 4.1 MSW Setup and Multi-case Fake API Creation
- **API Mocking:** Do not mock APIs with `with-redefs` in individual tests. To test API behavior, create fake API implementations in `src/app/msw/handlers.cljs` [handlers.cljs](src/app/msw/handlers.cljs).
- **Multi-case:** When a single API endpoint needs to return different responses (success, failure, specific states, etc.) based on parameters (e.g., ID), manage state using `reagent.core/atom` within `handlers.cljs`.
    - Store multiple case data in map format in the atom (e.g., `{"id-1" {:status "ok"}, "id-2" {:status "error"}}`).
    - Within API handler functions, use request parameters to query appropriate data from the atom and return corresponding responses (data or error status codes).

```clojure
;; Example: Multi-case data definition in handlers.cljs
(ns app.msw.handlers
  (:require [reagent.core :as r] ...))

(def fake-invitations-atom
  (r/atom {"approved-id" {:id "approved-id" :status "approved" :expired? false}
           "expired-id"  {:id "expired-id" :status "pending" :expired? true}}))

(def handlers
  (create-msw-handlers
    [... ;; Other handlers
     ["/invitations/:invitation-id"
      {:get {:parameters {:path [:map [:invitation-id string?]]}
             :handler (fn [{:keys [parameters]}]
                        (let [invitation-id (get-in parameters [:path :invitation-id])]
                          (if-let [invitation (get @fake-invitations-atom invitation-id)]
                            {:status 200 :body {:invitation invitation}} ; Return data matching ID
                            {:status 404 :body {:error "Not Found"}})))}}]
     ...]))
```

### 4.2 Using Correct ARIA Roles
- There is no 'modal' role when testing modal windows. Use "dialog", "alertdialog", or "alert" roles instead.
- dropdown, combobox, searchbar, select, etc. all use the "combobox" role.

### 4.3 Use siheom Functions, Not Utils
- Do not use functions from the `app.util` namespace directly in tests; use functions from the `siheom.siheom` namespace instead.

### 4.4 Don't Duplicate Tests for Screen Rendering

- Functions like click! already operate under the assumption that elements are rendered on screen, so don't write redundant `visible?` function tests to check if elements are rendered.

## 5. Test Focus

### 5.1 Single Test Case Optimization
- Each test focuses on one main scenario.
- Don't unnecessarily split into multiple cases.
- Integrate related validations into a single test case.

```clojure
;; Good - Single focused test case
(testing "Can configure fields by category"
    (sh/run-siheom
     ;; given
     (sh/render [form-story])

     (sh/checked? (q/query-from "checkbox" "OS" {:group "Laptop"}))
     (sh/disabled? (q/button "Save"))

     ;; when
     (sh/click! (q/query-from "checkbox" "RAM" {:group "Air Purifier"}))
     (sh/click! (q/button "Save"))

     ;; then
     (sh/toast-log? "Saved!")))

;; Bad - Unnecessarily split test cases
(testing "Default values are selected" ...)
(testing "Save button is disabled" ...)
(testing "Can select fields and save" ...)
```

### 5.2 Test Order
Tests generally follow this order:
1. Given: Verify initial state
2. When: Perform interactions
3. Then: Verify results

