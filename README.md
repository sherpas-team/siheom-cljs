# 시험 (Siheom)

_A data first Web test interpreter_

> "Siheom transforms testing from imperative scripts into declarative stories. No more async hell, no more implementation details... just human readable log and error messages" – Taehee Kim, Sherpas

[![Clojars Project](https://img.shields.io/clojars/v/siheom.svg)](https://clojars.org/siheom)

**Siheom** (시험, meaning "test" in Korean) is a ClojureScript web testing interpreter that treats tests as declarative data interpreted by a smart runtime (like testing-library, playwright, webdriverio etc...). It eliminates async boilerplate and hide implementation details, letting you focus on expressing user behavior in accessible web.

- 📊 **Tests as data** - No `await`, `act`, `waitFor`, just write scenario as data
- ♿ **Accessibility-first** - Semantic queries using ARIA roles and accessible names
- 🔧 **Extensible runtime** - Add custom actions for project specific feature
- 🌍 **Framework agnostic** - Support React/Reagent as default. Based on Web standard, easily extensible to other frameworks like solid, vue, svelte...

## How to start a new project with Siheom

We don't publish siheom to npm or mvn yet. But...

### Clone
You can clone our repo as template.

### Existing project
You can copy just `test/siheom`, `test/vitest` directories, `test/setupTest.js`, `vitest.config.js` and `scripts/vitest/generate_modules_hook.clj`

then apply our build hook script in `shadow-cljs.edn`
```
{
    ...
    :build-hooks        [(vitest.generate-modules-hook/hook)]
}
```

then install dev dependencies

```sh
pnpm add -D vitest playwright accname @vitest/browser @testing-library/jest-dom @testing-library/react @testing-library/user-event
```

## Usage

In cljs, async testing was frustrating

### Traditional Testing (Before Siheom)
```clojure
(ns siheom.examples.form-story-test
  (:require
   ["@testing-library/react" :as rtl]
   ["@testing-library/user-event" :default user-event]
   [shadow.cljs.modern :refer [js-await]]
   [siheom.examples.form-story :refer [form-story]]
   [uix.core :refer [$]]
   [vitest.clojure-testing :refer [is testing]]))

(defn type-input [target value]
  (.type (.setup user-event) target value))

(defn click [target]
  (.click rtl/fireEvent target))

(def result-2-atom (atom nil))

(testing "should submit form : uix official docs example"
  (let [_ (rtl/render ($ form-story {:submit-message! (fn [value]
                                                                (reset! result-2-atom value)
                                                                (js/Promise.resolve))}))
        input (.getByTestId screen "form-input")
        button (.getByTestId screen "form-submit-button")]
    (js-await [_ (type-input input "hello world")]
              (click button)
              ;; findByTestId is async operation
              (js-await [message (.findByTestId screen "form-submitted-message")]
                        (is (= "Submitted!" (.-textContent message)))
                        (is (= @result-2-atom "hello world"))))))
```

When test fail, Error messages with html can be complex. Stacktrace is useless.

```html
TestingLibraryElementError: Unable to find an element by: [data-testid="form-submit-button"]

Ignored nodes: comments, script, style
<form class="flex flex-col gap-8">
   <div class="flex flex-col gap-6">
      <div class="flex flex-col gap-x-8 gap-y-6 sm:flex-row">
         <div data-input-wrapper="true" class="group flex h-max w-full flex-col items-start justify-start gap-1.5" data-rac="" data-required="true">
            <label class="flex cursor-default items-center gap-0.5 text-sm font-medium text-secondary" id="react-aria-_R_15j9bsnpfiv7bH1_" for="react-aria-_R_15j9bsnpfiv7b_" data-label="true">First name<span class="hidden text-brand-tertiary group-required:block">*</span></label>
            <div role="presentation" class="relative flex w-full flex-row place-content-center place-items-center rounded-lg bg-primary shadow-xs ring-1 ring-primary transition-shadow duration-100 ease-linear ring-inset group-disabled:cursor-not-allowed group-disabled:bg-disabled_subtle group-disabled:ring-disabled group-invalid:ring-error_subtle flex-1" data-rac=""><input type="text" required="" placeholder="First name" tabindex="0" id="react-aria-_R_15j9bsnpfiv7b_" aria-labelledby="react-aria-_R_15j9bsnpfiv7bH1_" class="m-0 w-full bg-transparent text-md text-primary ring-0 outline-hidden placeholder:text-placeholder autofill:rounded-lg autofill:text-primary px-3.5 py-2.5" data-rac="" name="firstName" value="" title=""></div>
         </div>
// ...
 ```

### With Siheom (After)

With siheom, you can easily test the same scenario.

```clojure
(ns siheom.examples.form-story-test
  (:require
   [promesa.core :as p]
   [siheom.default-runtime-impl :refer [resolve-story]]
   [siheom.examples.form-story :refer [form-story]]
   [siheom.query-locator :as q]
   [siheom.siheom :as sh]
   [uix.core :refer [$]]
   [vitest.clojure-testing :refer [is testing]]))

(def result-atom (atom nil))
(defmethod resolve-story :Examples/form-story [_]
  ($ form-story {:submit-message! (fn [value]
                                    (reset! result-atom value)
                                    (js/Promise.resolve))}))

;; using siheom
(testing
 "should submit form : siheom"
  (p/do (sh/run-siheom!
         (sh/render :Examples/form-story)
         (sh/type! (q/textbox "Message") "hello world")
         (sh/click! (q/button "Submit"))
         (sh/visible? (q/text "Submitted!")))

        (is (= @result-atom "hello world"))))
```

When test fail, We provide human readable logs and snapshots

```sh
TestingLibraryElementError: 

[Action Logs]

render: :Examples/form-story               
type! : "hello world" to textbox "Message"  <- !! FAILED !!


[Original Error Message]

Unable to find role="textbox" and name "Message"


[A11y Snapshot]

form 
  presentation 
    textbox: "First name*" [value=]
  presentation 
    textbox: "Last name*" [value=]
  presentation 
    textbox: "Email*" [value=]
  region 
  presentation 
    textbox: "Phone number Phone number*" [value=]
  textbox: "Message*" 
  checkbox: "You agree to our friendly privacy policy." [checked=false]
    "You agree to our friendly privacy policy."
  button: "Send message"
```

It is not just for human, also for [your LLM agent](https://x.com/stelo_kim/status/1945052079657206102/photo/1) (like cursor, claude code etc...)


## How does it work?

### Tests as Data
In Siheom, test is just data. `sh/click!`, `sh/visible?` and `q/button` is just utilities for creating data

```clojure
(defn render [story-title]
  {:action-key :render
   :args {:story-title story-title}})

(defn click! [locator]
  {:action-key :click
   :args {:locator locator}})

(defn button [name]
  {:role "button"
   :name name})

(defn visible?
  ([locator] (visible? locator true))
  ([locator expected]
   {:action-key :visible?
    :args {:locator locator :expected expected}}))
```

```clojure
[(sh/render :Examples/Counter)
 (sh/click! (q/button "Count: 0"))
 (sh/visible? (q/button "Count: 1"))]
;; => As EDN
[{:action-key :render
  :args       {:story-title :Examples/counter}}
 {:action-key :click
  :args       {:locator {:role "button"
                         :name "click: 0"}}}
 {:action-key :visible?
  :args       {:locator {:role "button"
                         :name "click: 1"}
               :expected true}}]
```

`sh/run-siheom!` is the interpreter for the test plan. It find proper implementation for each action. We provide `test/siheom/default_runtime_impl.cljs`, and `test/siheom/testing_library_impl.cljs` with `@testing-library`.

```clojure
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
  }})
```

But you can extend it, or even create your own implementation with playwright, webdriverio etc... see [Custom Siheom Runtime Example](./test/siheom/examples/custom_siheom.cljs)


## Documentation

In progress

## Who's using Siheom?

- [Sherpas](https://blog.smply.one/) - We're creating IT Asset Management system for fast growing teams with fullstack clojure/script.
- *Your team here!*


## Thanks to

- [Testing Library](https://testing-library.com/) for accessibility-focused testing principles
- [Playwright](https://playwright.dev/) for locator and web-first assertion concepts  
- [Cucumber](https://cucumber.io/) for human readable, natural language testing inspiration
- [Maestro](https://maestro.mobile.dev/) for test as data, DSL using yaml
