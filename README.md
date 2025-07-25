# 시험 (Siheom)

_A data first Web test interpreter_

> "Siheom transforms testing from imperative scripts into declarative stories. No more async hell, no more implementation details... just human readable log and error messages" – Taehee Kim, Sherpas


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
            <div class="relative flex w-full flex-row place-content-center place-items-center rounded-lg bg-primary shadow-xs ring-1 ring-primary transition-shadow duration-100 ease-linear ring-inset group-disabled:cursor-not-allowed group-disabled:bg-disabled_subtle group-disabled:ring-disabled group-invalid:ring-error_subtle flex-1" data-rac=""><input type="text" required="" placeholder="First name" tabindex="0" id="react-aria-_R_15j9bsnpfiv7b_" aria-labelledby="react-aria-_R_15j9bsnpfiv7bH1_" class="m-0 w-full bg-transparent text-md text-primary ring-0 outline-hidden placeholder:text-placeholder autofill:rounded-lg autofill:text-primary px-3.5 py-2.5" data-rac="" name="firstName" value="" title=""></div>
         </div>
         <div data-input-wrapper="true" class="group flex h-max w-full flex-col items-start justify-start gap-1.5" data-rac="" data-required="true">
            <label class="flex cursor-default items-center gap-0.5 text-sm font-medium text-secondary" id="react-aria-_R_25j9bsnpfiv7bH1_" for="react-aria-_R_25j9bsnpfiv7b_" data-label="true">Last name<span class="hidden text-brand-tertiary group-required:block">*</span></label>
            <div class="relative flex w-full flex-row place-content-center place-items-center rounded-lg bg-primary shadow-xs ring-1 ring-primary transition-shadow duration-100 ease-linear ring-inset group-disabled:cursor-not-allowed group-disabled:bg-disabled_subtle group-disabled:ring-disabled group-invalid:ring-error_subtle flex-1" data-rac=""><input type="text" required="" placeholder="Last name" tabindex="0" id="react-aria-_R_25j9bsnpfiv7b_" aria-labelledby="react-aria-_R_25j9bsnpfiv7bH1_" class="m-0 w-full bg-transparent text-md text-primary ring-0 outline-hidden placeholder:text-placeholder autofill:rounded-lg autofill:text-primary px-3.5 py-2.5" data-rac="" name="lastName" value="" title=""></div>
         </div>
      </div>
      <div data-input-wrapper="true" class="group flex h-max w-full flex-col items-start justify-start gap-1.5" data-rac="" data-required="true">
         <label class="flex cursor-default items-center gap-0.5 text-sm font-medium text-secondary" id="react-aria-_R_9j9bsnpfiv7bH1_" for="react-aria-_R_9j9bsnpfiv7b_" data-label="true">Email<span class="hidden text-brand-tertiary group-required:block">*</span></label>
         <div class="relative flex w-full flex-row place-content-center place-items-center rounded-lg bg-primary shadow-xs ring-1 ring-primary transition-shadow duration-100 ease-linear ring-inset group-disabled:cursor-not-allowed group-disabled:bg-disabled_subtle group-disabled:ring-disabled group-invalid:ring-error_subtle" data-rac=""><input type="email" required="" placeholder="you@company.com" tabindex="0" id="react-aria-_R_9j9bsnpfiv7b_" aria-labelledby="react-aria-_R_9j9bsnpfiv7bH1_" class="m-0 w-full bg-transparent text-md text-primary ring-0 outline-hidden placeholder:text-placeholder autofill:rounded-lg autofill:text-primary px-3.5 py-2.5" data-rac="" name="email" value="" title=""></div>
      </div>
      <div data-input-wrapper="true" class="group flex h-max w-full flex-col items-start justify-start gap-1.5" data-rac="">
         <label class="flex cursor-default items-center gap-0.5 text-sm font-medium text-secondary" data-label="true" id="react-aria-_R_dj9bsnpfiv7bH1_" for="react-aria-_R_dj9bsnpfiv7b_">Phone number<span class="hidden text-brand-tertiary">*</span></label>
         <div data-input-size="md" class="group relative flex h-max w-full flex-row justify-center rounded-lg bg-primary transition-all duration-100 ease-linear has-[&amp;&gt;select]:shadow-xs has-[&amp;&gt;select]:ring-1 has-[&amp;&gt;select]:ring-border-primary has-[&amp;&gt;select]:ring-inset has-[&amp;&gt;select]:has-[input:focus]:ring-2 has-[&amp;&gt;select]:has-[input:focus]:ring-border-brand">
            <section data-leading="true">
               <div class="w-full in-data-input-wrapper:w-max">
                  <div class="relative grid w-full items-center">
                     <select aria-label="Country code" id="select-native-_R_ddj9bsnpfiv7b_" aria-describedby="select-native-hint-_R_ddj9bsnpfiv7b_" aria-labelledby="select-native-_R_ddj9bsnpfiv7b_" class="appearance-none rounded-lg bg-primary px-3.5 py-2.5 text-md font-medium text-primary shadow-xs ring-1 ring-primary outline-hidden transition duration-100 ease-linear ring-inset placeholder:text-fg-quaternary focus-visible:ring-2 focus-visible:ring-brand disabled:cursor-not-allowed disabled:bg-disabled_subtle disabled:text-disabled in-data-input-wrapper:flex in-data-input-wrapper:h-full in-data-input-wrapper:gap-1 in-data-input-wrapper:bg-inherit in-data-input-wrapper:px-3 in-data-input-wrapper:py-2 in-data-input-wrapper:font-normal in-data-input-wrapper:text-tertiary in-data-input-wrapper:shadow-none in-data-input-wrapper:ring-transparent in-data-input-wrapper:group-disabled:pointer-events-none in-data-input-wrapper:group-disabled:cursor-not-allowed in-data-input-wrapper:group-disabled:bg-transparent in-data-input-wrapper:group-disabled:text-disabled in-data-input-wrapper:in-data-leading:rounded-r-none in-data-input-wrapper:in-data-trailing:rounded-l-none in-data-input-wrapper:in-data-[input-size=md]:py-2.5 in-data-input-wrapper:in-data-leading:in-data-[input-size=md]:pl-3.5 in-data-input-wrapper:in-data-[input-size=sm]:py-2 in-data-input-wrapper:in-data-[input-size=sm]:pl-3 in-data-input-wrapper:in-data-leading:in-data-[input-size=md]:pr-4.5 in-data-input-wrapper:in-data-leading:in-data-[input-size=sm]:pr-4.5 in-data-input-wrapper:in-data-trailing:in-data-[input-size=md]:pr-8 in-data-input-wrapper:in-data-trailing:in-data-[input-size=sm]:pr-7.5">
                        <option value="AF">AF</option><option value="AL">AL</option><option value="DZ">DZ</option><option value="AD">AD</option><option value="AO">AO</option><option value="AG">AG</option><option value="AR">AR</option><option value="AM">AM</option><option value="AU">AU</option><option value="AT">AT</option><option value="AZ">AZ</option><option value="BS">BS</option><option value="BH">BH</option><option value="BD">BD</option><option value="BB">BB</option><option value="BY">BY</option><option value="BE">BE</option><option value="BZ">BZ</option><option value="BJ">BJ</option><option value="BT">BT</option><option value="BO">BO</option><option value="BA">BA</option><option value="BW">BW</option><option value="BR">BR</option><option value="BN">BN</option><option value="BG">BG</option><option value="BF">BF</option><option value="BI">BI</option><option value="KH">KH</option><option value="CM">CM</option><option value="CA">CA</option><option value="CF">CF</option><option value="TD">TD</option><option value="CL">CL</option><option value="CN">CN</option><option value="CO">CO</option><option value="KM">KM</option><option value="CR">CR</option><option value="HR">HR</option><option value="CU">CU</option><option value="CY">CY</option><option value="CZ">CZ</option><option value="CD">CD</option><option value="DK">DK</option><option value="DJ">DJ</option><option value="DM">DM</option><option value="DO">DO</option><option value="TL">TL</option><option value="EC">EC</option><option value="EG">EG</option><option value="SV">SV</option><option value="GQ">GQ</option><option value="ER">ER</option><option value="EE">EE</option><option value="SZ">SZ</option><option value="ET">ET</option><option value="FJ">FJ</option><option value="FI">FI</option><option value="FR">FR</option><option value="GA">GA</option><option value="GM">GM</option><option value="GE">GE</option><option value="DE">DE</option><option value="GH">GH</option><option value="GR">GR</option><option value="GD">GD</option><option value="GT">GT</option><option value="GN">GN</option><option value="GW">GW</option><option value="GY">GY</option><option value="HT">HT</option><option value="HN">HN</option><option value="HU">HU</option><option value="IS">IS</option><option value="IN">IN</option><option value="ID">ID</option><option value="IR">IR</option><option value="IQ">IQ</option><option value="IE">IE</option><option value="IL">IL</option><option value="IT">IT</option><option value="JM">JM</option><option value="JP">JP</option><option value="JO">JO</option><option value="KZ">KZ</option><option value="KE">KE</option><option value="KI">KI</option><option value="KW">KW</option><option value="KG">KG</option><option value="LA">LA</option><option value="LV">LV</option><option value="LB">LB</option><option value="LS">LS</option><option value="LR">LR</option><option value="LY">LY</option><option value="LI">LI</option><option value="LT">LT</option><option value="LU">LU</option><option value="MG">MG</option><option value="MW">MW</option><option value="MY">MY</option><option value="MV">MV</option><option value="ML">ML</option><option value="MT">MT</option><option value="MH">MH</option><option value="MR">MR</option><option value="MU">MU</option><option value="MX">MX</option><option value="FM">FM</option><option value="MD">MD</option><option value="MC">MC</option><option value="MN">MN</option><option value="ME">ME</option><option value="MA">MA</option><option value="MZ">MZ</option><option value="MM">MM</option><option value="NA">NA</option><option value="NR">NR</option><option value="NP">NP</option><option value="NL">NL</option><option value="NZ">NZ</option><option value="NI">NI</option><option value="NE">NE</option><option value="NG">NG</option><option value="KP">KP</option><option value="MK">MK</option><option value="NO">NO</option><option value="OM">OM</option><option value="PK">PK</option><option value="PW">PW</option><option value="PA">PA</option><option value="PG">PG</option><option value="PY">PY</option><option value="PE">PE</option><option value="PH">PH</option><option value="PL">PL</option><option value="PT">PT</option><option value="QA">QA</option><option value="RO">RO</option><option value="RU">RU</option><option value="RW">RW</option><option value="KN">KN</option><option value="LC">LC</option><option value="VC">VC</option><option value="WS">WS</option><option value="SM">SM</option><option value="ST">ST</option><option value="SA">SA</option><option value="SN">SN</option><option value="RS">RS</option><option value="SC">SC</option><option value="SL">SL</option><option value="SG">SG</option><option value="SK">SK</option><option value="SI">SI</option><option value="SB">SB</option><option value="SO">SO</option><option value="ZA">ZA</option><option value="KR">KR</option><option value="SS">SS</option><option value="ES">ES</option><option value="LK">LK</option><option value="SR">SR</option><option value="SE">SE</option><option value="CH">CH</option><option value="SY">SY</option><option value="TJ">TJ</option><option value="TZ">TZ</option><option value="TH">TH</option><option value="TG">TG</option><option value="TO">TO</option><option value="TT">TT</option><option value="TN">TN</option><option value="TR">TR</option><option value="TM">TM</option><option value="TV">TV</option><option value="UG">UG</option><option value="UA">UA</option><option value="AE">AE</option><option value="GB">GB</option><option value="US" selected="">US</option><option value="UY">UY</option><option value="UZ">UZ</option><option value="VU">VU</option><option value="VE">VE</option><option value="VN">VN</option><option value="YE">YE</option><option value="ZM">ZM</option><option value="ZW">ZW</option>
                     </select>
                     <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" class="pointer-events-none absolute right-3.5 size-5 text-fg-quaternary in-data-input-wrapper:right-0 in-data-input-wrapper:size-4 in-data-input-wrapper:stroke-[2.625px] in-data-input-wrapper:in-data-trailing:in-data-[input-size=sm]:right-3"><path d="m6 9 6 6 6-6"></path></svg>
                  </div>
               </div>
            </section>
            <div class="relative flex w-full flex-row place-content-center place-items-center rounded-lg bg-primary shadow-xs ring-1 ring-primary transition-shadow duration-100 ease-linear ring-inset group-disabled:cursor-not-allowed group-disabled:bg-disabled_subtle group-disabled:ring-disabled group-invalid:ring-error_subtle z-10 rounded-l-none group-has-[&amp;&gt;select]:bg-transparent group-has-[&amp;&gt;select]:shadow-none group-has-[&amp;&gt;select]:ring-0 group-has-[&amp;&gt;select]:focus-within:ring-0 group-disabled:group-has-[&amp;&gt;select]:bg-transparent" data-rac=""><input aria-label="Phone number" type="tel" placeholder="+1 (000) 000-0000" tabindex="0" id="react-aria-_R_dj9bsnpfiv7b_" class="m-0 w-full bg-transparent text-md text-primary ring-0 outline-hidden placeholder:text-placeholder autofill:rounded-lg autofill:text-primary px-3.5 py-2.5 group-has-[&amp;&gt;select]:px-3 group-has-[&amp;&gt;select]:pl-3" data-rac="" name="phone" value="" title="" aria-labelledby="react-aria-_R_dj9bsnpfiv7b_ react-aria-_R_dj9bsnpfiv7bH1_"></div>
         </div>
      </div>
      <div data-input-wrapper="true" class="group flex h-max w-full flex-col items-start justify-start gap-1.5" data-rac="" data-required="true"><label class="flex cursor-default items-center gap-0.5 text-sm font-medium text-secondary" id="react-aria-_R_hj9bsnpfiv7bH1_" for="react-aria-_R_hj9bsnpfiv7b_" data-label="true">Message<span class="text-brand-tertiary block">*</span></label><textarea required="" placeholder="Leave us a message..." tabindex="0" id="react-aria-_R_hj9bsnpfiv7b_" aria-labelledby="react-aria-_R_hj9bsnpfiv7bH1_" rows="4" style="--resize-handle-bg:url(data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTgiIGhlaWdodD0iMTgiIHZpZXdCb3g9IjAgMCAxOCAxOCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cGF0aCBkPSJNMTAgMkwyIDEwIiBzdHJva2U9IiNENUQ3REEiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPjxwYXRoIGQ9Ik0xMSA3TDcgMTEiIHN0cm9rZT0iI0Q1RDdEQSIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIi8+PC9zdmc+);--resize-handle-bg-dark:url(data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTgiIGhlaWdodD0iMTgiIHZpZXdCb3g9IjAgMCAxOCAxOCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cGF0aCBkPSJNMTAgMkwyIDEwIiBzdHJva2U9IiMzNzNBNDEiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPjxwYXRoIGQ9Ik0xMSA3TDcgMTEiIHN0cm9rZT0iIzM3M0E0MSIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIi8+PC9zdmc+)" class="w-full scroll-py-3 rounded-lg bg-primary px-3.5 py-3 text-md text-primary shadow-xs ring-1 ring-primary transition duration-100 ease-linear ring-inset placeholder:text-placeholder autofill:rounded-lg autofill:text-primary focus:outline-hidden [&amp;::-webkit-resizer]:bg-(image:--resize-handle-bg) [&amp;::-webkit-resizer]:bg-contain dark:[&amp;::-webkit-resizer]:bg-(image:--resize-handle-bg-dark)" data-rac="" title=""></textarea></div>
      <label data-react-aria-pressable="true" class="flex items-start gap-3" data-rac="">
         <span style="border:0;clip:rect(0 0 0 0);clip-path:inset(50%);height:1px;margin:-1px;overflow:hidden;padding:0;position:absolute;width:1px;white-space:nowrap"><input type="checkbox" data-react-aria-pressable="true" tabindex="0" name="privacy" title=""></span>
         <div class="flex shrink-0 cursor-pointer appearance-none items-center justify-center bg-primary ring-1 ring-primary ring-inset size-5 rounded-md mt-0.5">
            <svg aria-hidden="true" viewBox="0 0 14 14" fill="none" class="pointer-events-none absolute text-fg-white opacity-0 transition-inherit-all size-3.5"><path d="M2.91675 7H11.0834" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path></svg>
            <svg aria-hidden="true" viewBox="0 0 14 14" fill="none" class="pointer-events-none absolute text-fg-white opacity-0 transition-inherit-all size-3.5"><path d="M11.6666 3.5L5.24992 9.91667L2.33325 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path></svg>
         </div>
         <div class="inline-flex flex-col gap-0.5">
            <span class="text-tertiary text-md">You agree to our friendly<!-- --> <a href="#" class="rounded-sm underline underline-offset-3 outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2">privacy policy.</a></span>
         </div>
      </label>
   </div>
   <button class="group relative inline-flex h-max cursor-pointer items-center justify-center whitespace-nowrap outline-brand transition duration-100 ease-linear focus-visible:outline-2 focus-visible:outline-offset-2 in-data-input-wrapper:shadow-xs in-data-input-wrapper:focus:!z-50 in-data-input-wrapper:in-data-leading:-mr-px in-data-input-wrapper:in-data-leading:rounded-r-none in-data-input-wrapper:in-data-leading:before:rounded-r-none in-data-input-wrapper:in-data-trailing:-ml-px in-data-input-wrapper:in-data-trailing:rounded-l-none in-data-input-wrapper:in-data-trailing:before:rounded-l-none disabled:cursor-not-allowed disabled:text-fg-disabled disabled:*:data-icon:text-fg-disabled_subtle *:data-icon:pointer-events-none *:data-icon:size-5 *:data-icon:shrink-0 *:data-icon:transition-inherit-all gap-1.5 rounded-lg px-4.5 py-3 text-md font-semibold before:rounded-[7px] data-icon-only:p-3.5 bg-brand-solid text-white shadow-xs-skeumorphic ring-1 ring-transparent ring-inset hover:bg-brand-solid_hover data-loading:bg-brand-solid_hover before:absolute before:inset-px before:border before:border-white/12 before:mask-b-from-0% disabled:bg-disabled disabled:shadow-xs disabled:ring-disabled_subtle *:data-icon:text-button-primary-icon hover:*:data-icon:text-button-primary-icon_hover" data-rac="" type="submit" tabindex="0" data-react-aria-pressable="true" id="react-aria-_R_2j9bsnpfiv7b_"><span data-text="true" class="transition-inherit-all px-0.5">Send message</span></button>
</form>
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
         (sh/click! (q/button "Send message"))
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
  textbox: "First name*" [value=] 
  textbox: "Last name*" [value=] 
  textbox: "Email*" [value=]
  region  
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
