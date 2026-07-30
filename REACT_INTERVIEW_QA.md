# React Interview Q&A

Grounded in the actual code in this repo, for use as interview prep.

## Components & libraries used

**Q: What components did you use in this project?**
A: No external UI library (no MUI/AntD/etc.) — three hand-rolled, reusable form components in `src/components/`:
- `Select.tsx` — async-loading dropdown; takes a `load()` function, fetches options on focus/mount, supports deps-based re-fetching (like a query key), disabled/placeholder states, and an `onPicked` callback to surface the selected option's label to the parent.
- `NumberInput.tsx` — numeric input with formatting control: suffixes (inline or masked as text), max decimal places, max integer digits, integer-only mode, leading-zero preservation.
- `DateInput.tsx` — segmented mm/dd/yyyy input (three separate text fields) synced with a hidden native `<input type="date">` so you get a calendar picker icon while keeping full control over typed input/formatting.

Pages: `LfwPage.tsx` (main worksheet form), `CalculatePage.tsx` (calculation results), `HelpPage.tsx` (static help), `App.tsx` (routing shell).

**Q: What third-party libraries are actually in use?**
A: `react-router-dom` (routing) and `axios` (API client). `formik`, `yup`, and `recharts` are listed in `package.json` but not imported anywhere in `src/` — worth flagging as unused/leftover dependencies rather than claiming they're in active use.

**Q: What's the build tooling?**
A: Vite (dev/build), TypeScript, ESLint with `@typescript-eslint`. No test framework is currently configured (no `test` script, no Vitest/Jest, no `*.test.*`/`*.spec.*` files).

---

## State management

**Q: What state management did you use — Redux, Context API?**
A: Plain `useState` in the page components, no Redux/Context. `LfwPage.tsx` alone has ~30 `useState` calls for form fields, labels, and UI flags. For a form-heavy single-flow app like this, prop drilling through Context or a store wasn't worth the overhead — state lives where it's used and gets passed down to the three shared input components as props/callbacks.

**Q: How did you persist form data across navigation (e.g., user goes to the calculate page and comes back)?**
A: `sessionStorage`, not a global store — see `src/utils/persist.ts` (`loadLfwForm`/`saveLfwForm`/`clearLfwForm`). A flag (`plv-lfw-ui:return-from-calc`) in `sessionStorage` tells `LfwPage` on mount whether to rehydrate from the saved snapshot or start fresh. This runs in `useLayoutEffect` rather than `useEffect` specifically to restore values *before* paint and avoid a flash of empty fields.

**Q: Why `useLayoutEffect` there instead of `useEffect`?**
A: `useEffect` fires after the browser paints, so restoring saved values there would show a flicker of empty inputs first. `useLayoutEffect` runs synchronously after DOM mutations but before paint, so the rehydrated values are on screen in the first frame.

---

## Routing

**Q: How do you pass data between pages/routes?**
A: Via `react-router-dom`'s `navigate(path, { state })` and reading it back with `useLocation().state` — see `CalculatePage.tsx`. `LfwPage` navigates to `/glvlfw-web/calculateELCandLF` with `criteria`/`meta`/`results` in `state`; `CalculatePage` reads that state, and if it's missing (e.g., user hit the URL directly), redirects back with `navigate(..., { replace: true })` inside a `useEffect` guard.

---

## Component design

**Q: How are your reusable input components designed?**
A: As controlled components — `Select`, `NumberInput`, `DateInput` all take `value`/`onChange` from the parent and hold no source-of-truth state themselves (aside from transient UI state like a text "draft" while typing). This keeps the parent form the single source of truth, which is what makes the sessionStorage persistence and derived-value logic in `LfwPage` possible.

**Q: `Select` does async data loading — walk through how.**
A: It takes a `load: () => Promise<Opt[]>` prop and a `deps` array. On mount (and whenever `deps` changes) it calls `load()` and stores results in local `useState`. It also lazy-loads on first focus if nothing's loaded yet (`handleFocus`), and reports the currently-matched option back to the parent via an `onPicked` callback so the parent can capture the human-readable label, not just the id — that's how `LfwPage` keeps `brandLabel`, `divisionLabel`, etc., in sync with `brandId`, `divisionId`.

**Q: How do you handle cascading/dependent dropdowns (Brand → Division → Department → Class)?**
A: Each `Select`'s `load` closes over the parent id (e.g., Division's loader depends on `brandId`) and its `deps` array includes that parent id, so it refetches when the parent changes. Selecting a new Brand also manually resets every downstream field (`setDivisionId(undefined)`, `setDepartmentId(undefined)`, etc.) in the `onChange` handler — cascading resets are explicit, not automatic.

**Q: I saw a `tpResetKey` state used as a React `key`. What's that for?**
A: It's the "change `key` to force remount" trick. The Transfer Point `Select` is keyed on `` `transferPoint-${tpResetKey}` ``; bumping that counter unmounts and remounts the component instead of just changing props, which fully clears its internal `options`/`loading` state rather than relying on `useEffect` cleanup to do it.

---

## Hooks & effects

**Q: How do you avoid infinite loops / stale state with useEffect?**
A: A few patterns: `useRef` flags to track whether a value is "ready" before reacting to changes (`stockDateReadyRef`), comparing new vs. previous values via refs before calling `setState` again, and functional `setState` updates (`setCartonType(prev => ...)`) to avoid needing the current value in the dependency array. A couple of effects intentionally suppress `react-hooks/exhaustive-deps` where re-running on every referenced value would cause unwanted loops — a deliberate trade-off, not an oversight.

**Q: What's `useMemo` used for here?**
A: Derived/computed values that shouldn't trigger their own re-render cycle — e.g., `derivedPackFactor` and `derivedAgentCommissionPercent` fall back from an explicit override to a guidance value looked up from a map, and `isHongKong` derives a boolean from country label/code. Keeps derivation logic out of `useEffect` + extra state, which would just add another render.

---

## TypeScript & bootstrapping

**Q: Any TypeScript patterns worth mentioning?**
A: Component props are defined as inline typed object literals (not separate `interface`/`type` declarations in most cases), optional props via `?:`, and `React.CSSProperties`/`React.FocusEventHandler` types are reused directly from React's types rather than redefined. `NumberInput` has a fairly wide prop surface (`onlyIntegerDigits`, `maxDecimalPlaces`, `preserveLeadingZeros`, etc.) to cover several input-formatting variants without needing multiple components.

**Q: Did you use `formik`/`yup`/`recharts`?**
A: They're in `package.json` but not currently imported anywhere in `src/` — worth being upfront about if asked, since form validation here is done manually (inline checks like `isValidDateInput`) rather than through a schema library. Good to flag as a possible cleanup/leftover-dependency item.

**Q: Any React 18 / bootstrapping specifics?**
A: `createRoot` (React 18 API) wrapped in `<React.StrictMode>`, and `main.tsx` does an async `bootstrap()` before the first render — it fetches `/ui-config.json` at runtime to set the API base URL, so the app supports environment-specific config without a rebuild, then renders once that resolves.

---

## Going deeper: how would you refactor state management?

**Q: `LfwPage.tsx` has ~30 `useState` calls. How would you clean that up?**
A: Two realistic options, depending on scope:
- **Extract a custom hook** (`useLfwForm()`) that owns all the field state, the derived `useMemo` values, and the effects (pack-factor guidance, HK duty override, rehydration). `LfwPage.tsx` would then just call the hook and render — separates "what the form does" from "what it looks like," and makes the logic unit-testable without rendering anything.
- **Consolidate related fields into `useReducer`.** The cascading resets (`onChange` for Brand resets Division/Department/Class/PackFactor/etc. in one handler) are really a single state transition today expressed as 8-10 separate `setState` calls. A reducer action like `{ type: 'BRAND_CHANGED', brandId }` would express that as one atomic transition and remove the risk of forgetting to reset a field when a new cascading dependency is added later.

I would *not* reach for Context or Redux here — this is a single page with no siblings needing the same state, so a store would add indirection without solving a real problem.

**Q: Would you replace the manual form handling with `formik`/`yup` since they're already installed?**
A: Possibly, but I'd weigh it against the current design first. Formik centralizes validation/touched/error state, which this form currently does manually and ad hoc (e.g., `isValidDateInput`). The tradeoff: the current `Select`/`NumberInput`/`DateInput` components already have bespoke behaviors (async option loading, label callbacks, decimal-place enforcement) that don't map cleanly onto Formik's field model, so adopting it would mean adapting those components, not just wrapping the page. If validation rules grow more complex, yup's already-installed schema validation is the more incremental win — I'd add it before pulling in all of Formik.

---

## Going deeper: how would you introduce tests?

**Q: There's no test setup currently. What would you add?**
A: Vitest + React Testing Library, since Vite is already the build tool (Vitest shares its config/transform pipeline, so there's no separate webpack/babel setup to maintain). Confirmed there's currently no `test` script and no `*.test.*`/`*.spec.*` files in the repo.

**Q: Where would you start, given limited time?**
A: Highest ROI first, roughly in this order:
1. **Pure function unit tests** — `src/utils/format.ts`, `persist.ts`, and the inline logic in `LfwPage.tsx` like `isValidDateInput`, `normalizePackFactor`, `toIso`/`toDisplay` in `DateInput.tsx`. No rendering needed, fast, and these are exactly the kind of edge-case-heavy logic (date parsing, decimal truncation) that's easy to regress silently.
2. **Component-level tests for the shared inputs** — `Select`, `NumberInput`, `DateInput` in isolation via RTL: assert they're properly controlled (value in → same value reflected out), that `NumberInput`'s decimal/integer constraints are enforced, that `DateInput` correctly converts between ISO and display formats.
3. **Integration tests on `LfwPage`** — mock `src/api/endpoints.ts`, then assert cascading-reset behavior (changing Brand clears Division/Department/Class) and the sessionStorage rehydration flow, since those are the parts most likely to break silently during future edits.
4. **E2E (Playwright)** for the full happy path — fill the worksheet, submit, land on `CalculatePage` with the right values — since the app relies on `react-router` `state` passed between routes, which unit/integration tests won't fully exercise end-to-end.

---

## Deep dive: Select.tsx walkthrough

**Q: Walk through `Select.tsx` line by line — what does each prop do?**
A:
- `label` / `hideLabel` — text shown above the select; `hideLabel` suppresses it when the parent renders its own label.
- `value` — the controlled selected value (string or number).
- `onChange(v)` — fires with the new value, or `undefined` when cleared back to the placeholder.
- `load: () => Promise<Opt[]>` — async function that fetches the option list.
- `deps` — dependency array; changing it triggers a reload, same idea as a query key.
- `disabled` — force-disables regardless of load state.
- `onPicked(opt)` — optional callback that hands back the *whole* matched `{label, value}` object, not just the id, so the parent can capture a human-readable label alongside the id.
- `selectStyle` / `placeholder` — style override and placeholder text; passing `placeholder={null}` removes the placeholder `<option>` entirely.

**Q: How does the async loading work?**
A: `reload()` guards against overlapping fetches (`if (loading) return`), calls `load()`, stores the result in `options` state, and resets to `[]` on error — failures are swallowed silently rather than surfaced to the parent. It runs once on mount and again whenever `deps` changes (line 31-36), plus lazily on first focus if nothing has loaded yet (`handleFocus`, line 45-49) — so a `Select` that isn't focused never fetches until the user interacts with it.

**Q: What's the `onPicked` effect for (line 38-44)?**
A: Whenever `options` or `value` changes, it looks up the option matching the current `value` (comparing as strings, so `"1"` matches `1`) and reports it via `onPicked`. This is how cascading dropdowns in `LfwPage` (Brand → Division → Department → Class) keep a `*Label` state in sync with a `*Id` state without duplicating lookup logic in the parent.

**Q: Why is the select sometimes disabled even when `disabled` is false?**
A: Line 52: `isEffectivelyDisabled = disabled || (options.length === 0 && !loading)`. An unloaded select (no options yet, not currently loading) is disabled until something triggers a load — either a `deps` change or the user focusing it. This prevents opening an empty dropdown.

**Q: Any bugs worth flagging in this component?**
A: Yes — the `mounted` flag in the mount effect (line 32/34) is set to `true` and to `false` on cleanup, but it's never actually *read* before the `setOptions`/`setLoading` calls inside `reload()`. It looks like a guard against "set state after unmount," but it doesn't do anything — `reload()` will happily call `setOptions` even after the component has unmounted. It's dead code masquerading as a safety check, not a real fix.

---

## Deep dive: NumberInput.tsx walkthrough

**Q: Why does this component keep its own `draft` string state instead of just formatting `value` directly?**
A: `value` is a `number`, but a lot of valid *in-progress* typing isn't a valid number yet — a trailing `.` (`"12."`), a lone `-`, or a leading zero you want to preserve (`"007"`). If the input's displayed value were derived straight from `Number`, those intermediate states would get silently reformatted or stripped while the user is mid-keystroke. `draft` holds the literal string the user typed; the numeric `value` is only what gets reported to the parent via `onChange`.

**Q: What do `isTextMasked`, `showDraftForIntegers`, and `showSuffix` each control?**
A:
- `isTextMasked` (`suffix && textWithSuffix`) — the suffix (e.g., `"%"`) is baked directly into the input's text value (`"12.5%"`) and the field becomes `type="text"`, rather than a real numeric input with a suffix floating next to it.
- `showDraftForIntegers` (`preserveLeadingZeros && onlyIntegerDigits`) — uses the raw `draft` string as the displayed value so something like `"007"` isn't collapsed to `7` while typing/showing.
- `showSuffix` — renders the suffix as a separate `<span>` next to the input, but only when *not* masked (masked mode already has the suffix baked into the text).

**Q: Walk through `enforceMaxDecimals`.**
A: It strips everything except digits and `.` (line 40), splits on the first `.` into whole/decimal parts, truncates the whole part to `maxIntegerDigits` and the decimal part to `maxDecimalPlaces` (both optional caps), then reassembles — re-adding the `-` prefix if the original input was negative and re-adding the `.` only if the original had one and decimals aren't capped at exactly `0`. It's a pure string transform, not a numeric round — so `"12.999"` with `maxDecimalPlaces=2` becomes `"12.99"` (truncation, not rounding).

**Q: Explain the sync `useEffect` at line 63-77 — why does it sometimes *not* overwrite `draft` when `value` changes?**
A: Two deliberate skip conditions:
1. **`isPartial`** — if the user currently has a trailing `.` or a lone `-`/`-.` typed, don't stomp on it just because a re-render happened; there's nothing meaningful to sync yet since that input isn't a complete number.
2. **Numeric equivalence** — if `draft` already represents the same number as `value` (e.g., draft `"12.50"` vs value `12.5`), leave `draft` alone. Without this check, every re-render would collapse the user's typed formatting (trailing zeros, etc.) back to `String(value)`, fighting the user while they type.

**Q: Why does `onChange` sometimes get called with `NaN`?**
A: This component treats `NaN` as the "no valid number entered yet" sentinel — e.g., clearing the field or leaving only a `-` sets `onChange(NaN)` rather than `onChange(undefined)`. It's a slightly unusual API choice (most codebases use `undefined` for "empty"), but it's consistent throughout the component (`hasValue` checks `Number.isFinite(value)`, which is `false` for `NaN` too) — worth knowing so callers don't accidentally treat `NaN` as a truthy/valid value.

**Q: How does the masked-suffix input round-trip user typing back out correctly?**
A: On `onChange` (line 90-115), if `isTextMasked`, it does `rawValue.replace(suffix, '').trim()` to strip the suffix back off what the user sees before parsing it as a number — since the input's actual DOM value includes the suffix text (e.g., typing next to `"12.5%"` produces a raw value that still contains `%`). Note this uses a plain string `replace`, not a regex, so it only strips the *first* literal occurrence of `suffix` — fine for a fixed suffix string like `"%"`, but worth flagging if `suffix` could ever contain regex-special characters or appear twice.

---

## Deep dive: DateInput.tsx walkthrough

**Q: Why three separate text inputs instead of one `<input type="date">`?**
A: A native `type="date"` input's typing/formatting behavior varies by browser/locale and doesn't give full control over segment-by-segment input. This component renders three plain `mm` / `dd` / `yyyy` text fields as the actual UI (fully controlled, consistent across browsers), and keeps a *hidden* native date input in sync purely to get the OS/browser calendar-picker UI (`showPicker()`) for click-to-pick — see line 153-161, styled with `opacity: 0`/`pointerEvents: none` and `aria-hidden`.

**Q: What's stored in `parts` vs `value` vs `displayValue`?**
A: `parts` (`{month, day, year}`) is the actual source of truth for the three visible fields. `value` is the ISO string (`yyyy-mm-dd`) reported to the parent via `onChange` — always zero-padded (`toIso`, line 24-29). `displayValue`/`onDisplayChange` is an optional *second* channel that carries the raw, non-padded `mm/dd/yyyy` string the user actually typed (`toRawDisplay`, line 38-41) — useful when the parent wants to persist/display exactly what was typed (e.g., `"7/4/2026"`) separately from the normalized ISO value used for calculations.

**Q: Explain the `lastEmittedValue`/`lastEmittedDisplay` refs and the sync effect at line 58-66.**
A: This component both *receives* `value`/`displayValue` as props and *emits* new ones via `onChange`/`onDisplayChange` — a classic setup for a feedback loop, since a parent that just stores whatever it's given would hand the same value straight back down as a prop. The refs record what this component itself last emitted; the effect only re-parses incoming props into local `parts` if the incoming value *differs* from what was last emitted. That breaks the loop and, just like `NumberInput`'s draft-sync effect, avoids clobbering in-progress typing when a prop changes for unrelated reasons.

**Q: Walk through `handleChange`.**
A: Each of the three fields calls `handleChange(key, maxLength)`, which strips non-digits and truncates to that field's max length (2 for month/day, 4 for year), merges it into `parts`, and updates local state immediately (so every keystroke is visible). It only calls `onChange`/`onDisplayChange` once `toIso(next)` produces a non-empty string — i.e., once year is 4 digits and month/day are non-empty (`isComplete`, line 20-22) — so partial typing (e.g., just a month) updates the visible fields but doesn't yet notify the parent. If the user clears all three fields back to empty, it explicitly emits `''` to both callbacks.

**Q: How does clicking the calendar icon open the native picker, and why the try/catch and fallback?**
A: `openNativePicker` (line 91-104) calls the hidden input's `showPicker()` if the browser supports it (a newer API, not universally available); otherwise it falls back to `.focus()` + `.click()`. The whole thing is wrapped in try/catch because `showPicker()` can throw (e.g., if called on a non-visible/disabled element or without a recent user gesture in some browsers) — the catch just swallows that and does nothing further, so the fallback only triggers on missing API support, not on a thrown error.

**Q: There's both an `onInput` handler and a manually-attached native `'change'` listener on the hidden date input — why two mechanisms?**
A: `handleNativeInput` (React's `onInput`, line 125-129) fires continuously as the user interacts with the native picker and just updates local `parts` for a live preview — it doesn't call `onChange` yet. The separate `useEffect` (line 106-123) attaches a raw DOM `'change'` listener via `addEventListener`, which is what actually commits the picked date — it fires once when the picker selection is finalized, updates `parts`, and calls `onChange`/`onDisplayChange`. It's done with a manual listener rather than a React `onChange` prop, likely because relying on React's synthetic change-event normalization for a `type="date"` input driven by `showPicker()` isn't fully consistent across browsers — attaching directly to the DOM node sidesteps that.

**Q: Anything in here that looks like dead code, similar to the `mounted` flag in `Select`?**
A: The `<input type="hidden" value={hiddenDisplay} />` at line 162 has no `name` attribute, so if this were ever inside a native form `submit`, it wouldn't actually contribute a field — it's not wired to anything and doesn't feed back into React state either. It reads like a leftover/vestigial "hidden field for form submission" pattern that was never fully connected — worth flagging rather than assuming it's load-bearing.

---

## Deep dive: LfwPage.tsx walkthrough

**Q: Give me the high-level shape of this component.**
A: It's the main worksheet form — ~1000 lines, no child form components beyond the three shared inputs (`Select`, `NumberInput`, `DateInput`). It owns roughly 30 `useState` calls for field values *and* their display labels, a handful of `useMemo`/`useRef`s for derived values and timing guards, persists itself to `sessionStorage` on every change, and on submit (`onCalculate`) validates, calls the `calculateLFW` API, and navigates to `CalculatePage` with the results in router `state`.

**Q: How do the cascading dropdowns (Brand → Division → Department → Class) actually reset each other?**
A: Explicitly, inside each `Select`'s `onChange` — e.g., picking a new Brand (line 604) resets `divisionId`, `departmentId`, `classId`, `packFactor`, `packFactorFromGuidance`, `pfOverrideConfirmed`, `classPackFactorById`, `channelId`, `marketId`, `destCtry`, `transferPoint`, `tpEnabled`, bumps `tpResetKey`, and clears `dutyPercent` — all in one handler. There's no generic "reset everything downstream of X" mechanism; each level's handler manually lists what it invalidates, so adding a new cascading dependency means remembering to add it to every upstream handler that should clear it.

**Q: What is `tpResetKey` for, and why does Transfer Point need it but the others don't?**
A: Transfer Point's `Select` is rendered with `key={\`transferPoint-${tpResetKey}\`}` (line 663). Bumping `tpResetKey` forces React to unmount/remount that specific `Select`, fully wiping its internal `options` state, rather than relying on the `deps` array to trigger a reload. The other cascading `Select`s just rely on `deps` + `disabled` to naturally show as empty/disabled when their parent id clears — Transfer Point apparently needed the harder reset (possibly because its options depend on 4 different parent values and a stale option could otherwise briefly remain selectable).

**Q: Walk through the Pack Factor logic — `derivedPackFactor`, `packFactorFromGuidance`, `pfOverrideConfirmed`.**
A: Pack factor can come from two places: a value the user typed (`packFactor`), or "guidance" looked up from the selected Class (`classPackFactorById[classId]`). `derivedPackFactor` (line 115-122) prefers the explicit `packFactor` state and falls back to the class guidance value. `packFactorFromGuidance` tracks whether the *current* `packFactor` value came from guidance rather than being manually typed. When the user edits Pack Factor while a guidance value is showing and hasn't already confirmed an override (line 903-924), it pops a `window.confirm` ("override the guidance?"); confirming sets `pfOverrideConfirmed` and commits the new value, declining reverts to the previous value. Once confirmed, or once the value didn't come from guidance in the first place, edits just apply directly. `pfOverrideConfirmed` gets reset to `false` whenever Class changes (line 140) or Stock Date changes post-hydration (line 145-156), since a new Class/date means new guidance to potentially reconfirm against.

**Q: Why does the Pack Factor / Class `Select`'s `load` function also call `setClassPackFactorById` and `setPackFactor` itself, instead of just returning the option list?**
A: The Class dropdown's `load` (line 620-636) is doing double duty: besides fetching+returning `{label, value}` options for the `Select` to render, it also captures each class's `packFactor` from the API response into a side-table (`classPackFactorById`) and, if the currently selected class's guidance wasn't set yet, seeds `packFactor` right there. This couples data-fetching with side-effect state updates inside a callback that `Select` treats as a pure loader — a pattern that works but means `Select`'s "just calls `load()` and renders options" contract is quietly relied on here to also drive unrelated business state.

**Q: How does the form survive navigating to `CalculatePage` and back?**
A: `sessionStorage`, via `src/utils/persist.ts`. A `useLayoutEffect` on mount (line 232-273) checks a `glv-lfw-ui:return-from-calc` flag: if absent, it's a fresh load and the persisted snapshot is cleared; if present, it loads the snapshot and restores every field (including labels) before first paint, using `useLayoutEffect` specifically to avoid a flash of empty fields. A separate effect (line 285-325) re-saves the full snapshot to `sessionStorage` on *any* tracked field change, but only after `rehydrated` is `true` — otherwise the initial restore would immediately overwrite the very snapshot it just loaded with blank/default values.

**Q: What's the `returnedFromCalc` / `window.history.pushState` effect for (line 276-282)?**
A: When the user came back from `CalculatePage` via the browser Back button (rather than clicking "Back"), this pushes a duplicate history entry for the current page. That effectively disables the browser's Forward button from taking them back to `CalculatePage` with stale results — a manual patch for React Router not naturally handling the "don't let Forward re-show a result computed from now-possibly-changed criteria" case.

**Q: Walk through `onCalculate`'s validation — why check exchange rate availability twice (line 421-427 and again 460-466)?**
A: The first check is a pre-flight validation to give the user an error before setting `loading` and building the payload. The second happens again right before the actual `calculateLFW` call, inside the `try` block. Since nothing async happens between the two checks except the earlier `alert`-based validations (which are synchronous), this second check is redundant — the currency/date/rate relationship can't have changed in between. It's defensive duplication rather than a real race condition being guarded against.

**Q: Any leftover debug code worth flagging?**
A: Yes — the Pack Factor `NumberInput`'s `onChange` (line 904-905) has two `console.log` calls dumping the before/after pack-factor state on every keystroke. That's debugging output left in from development, not something intended for production.

**Q: Is `HELP_FEEDBACK_TEXT` (imported from `@/utils/constants`) actually used?**
A: No — it's imported (line 13) but never referenced; the "For help/feedback..." text and `mailto:` link at the bottom of the form (line 1001-1004) are hard-coded inline instead of using the constant. Same unused-import pattern shows up in `CalculatePage.tsx`.

---

## Deep dive: CalculatePage.tsx walkthrough

**Q: What happens if a user navigates straight to `/calculateELCandLF` (e.g., pastes the URL, or hits refresh)?**
A: `criteria`/`results` come from `useLocation().state`, which is only populated when `navigate(..., { state })` was called from `LfwPage`. A direct hit has no `state`, so `missingState` is `true`; a `useEffect` (line 19-24) redirects back to the landing page with `replace: true` (so the broken URL doesn't stay in history), and the component renders `null` (line 25) until that redirect happens — avoiding a crash from destructuring `undefined` results.

**Q: Walk through the exchange-rate effect (line 63-99). Why does it fetch the rate range even for USD?**
A: It always calls `fetchExchangeRate` to populate `sprDateRange` (the "rate used is effective for stocking dates between X-Y" note shown regardless of currency), then branches: if currency is `USD`, it short-circuits `sprRate=1` and `firstCostUsd=firstCost` without a second API call; otherwise it calls `fetchFirstCostUsd` to get the actual conversion rate and computes `firstCostUsd = firstCost * rate`. Both calls fail silently into `null` state on error/catch, which the render then displays as `'--'` via `sprRate != null ? fmt(sprRate) : '--'`.

**Q: How are the ship-mode rows and terms-of-sale columns built, given `results` is a nested `Record<string, Record<string, any>>` from the API?**
A: Two `useMemo`s derive the table shape dynamically rather than hardcoding columns. `shipModes` (line 103-109) takes `Object.keys(results)` and sorts them by a fixed preferred order (`['Air','Ocean','Truck']`), with anything unrecognized sorted last. `termsList` (line 110-127) unions every term key found across *all* ship modes into a `Set` (so it's robust to a ship mode missing a term the others have), then sorts by a similar priority-predicate list (FOB, CFR, Split/Air-Ocean, FCA, DDU, DDP). This means the table's columns/rows are entirely response-shaped — if the backend adds a new term or ship mode, it appears without a code change, as long as the sort predicates recognize it (unrecognized ones just sort to the end, they aren't dropped).

**Q: What do `**` and `NA` mean in a results cell (`renderCell`, line 338-344)?**
A: For a given ship mode + term combination: no entry at all (`!r`) renders `**`; `r.notApplicable` renders `NA`; otherwise it shows the formatted value only if `r.hasAValidFreightRate` is true, and `**` again if not. So `**` is overloaded to mean both "no data for this combination" and "data exists but the freight rate isn't valid" — visually identical to the user, but semantically different reasons.

**Q: `formatTermLabel` has some fairly specific regex — walk through what it's doing.**
A: It splits a raw term key like `"FOBPurchaserPays"` into a known prefix (`FOB|FCA|CFR|DDU|DDP`) and the remainder, then reformats the remainder for display: `"Air Ocean"`/`"AirOcean"` → `"Air/Ocean"`, and `"PurchaserPays"`/`"VendorPays"` → `"Purchaser pays"`/`"Vendor pays"` (space inserted, lowercased "pays"). It wraps the remainder in parens if it mentions "Split" or "Air/Ocean" (e.g., `FOB/(CFR Air/Ocean Split)`-style labels), otherwise just joins with a `/`. This is presentation logic translating backend enum-ish keys into the legacy UI's exact label format.

**Q: Is `pickPrimaryTermKey` (line 40-55) actually used anywhere?**
A: No — it's defined but never called in the component. It looks like it was meant to pick a single "primary" term per ship mode (maybe for an earlier version of the table, or a planned single-column summary), but the current render iterates *all* terms via `termsList` instead. It's dead code that should either be removed or wired in if there's a pending use for it.

**Q: `onPrint` swaps `document.title` before calling `window.print()` and restores it after a `setTimeout(300)` — why?**
A: Browsers typically use `document.title` as the default filename/header when printing or saving as PDF. Setting it to `printHeading` ("Landed Factor Worksheet") just before printing makes the printed output's filename/header meaningful instead of showing the app's normal route title; the `setTimeout` restores the real title shortly after, once the print dialog has read it, so the visible tab title doesn't stay wrong. Since `window.print()` is synchronous-blocking in some browsers but not others, the fixed 300ms delay is a heuristic rather than a guarantee the dialog has already captured the title.

**Q: `fmtZeroAware` vs `fmtTwoDp` vs the imported `fmt` — what's the difference?**
A: `fmtZeroAware` (line 130-136) shows `0` as literally `"0"` but otherwise allows up to 2 decimals with none required (`minimumFractionDigits: 0`) — used for criteria fields like Duty %/Agent commission where a bare `0` shouldn't be blanked out. `fmtTwoDp` (line 137-142) always shows exactly 2 decimals — used for currency amounts like First Cost. The imported `fmt` (from `@/utils/format`) is used for the actual ELC/LF result values in the table. All three return `''` for `undefined`/`null`/non-finite input, but differ in decimal-place enforcement for valid numbers.

**Q: Same unused-import pattern as `LfwPage` — is `HELP_FEEDBACK_TEXT` used here either?**
A: No — imported at line 7 alongside `INVALID_NOTE_TEXT` (which *is* used, line 315), but `HELP_FEEDBACK_TEXT` itself is never referenced; the help/feedback email text at line 377 is hard-coded inline instead.