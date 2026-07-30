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