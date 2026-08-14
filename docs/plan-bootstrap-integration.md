# Plan: Introduce Minton (Bootstrap 5) styling to IntelligentUI

> **Status:** ✅ **Integration complete** (last updated 2026-06-29 with Issue 7: brand-dark label/title color). Option **C-lite (Style-only Minton)** delivered: Phase 1 + Phase 2 (incl. 2.5b) + Phase 3a + Polish (B + C) + Phase 3b + CSS Adjustments (rounds 1, 2 & 3) + Gear-button relocation + Card-size Plan B + Issue 7. Phase 4 formally skipped — see §Phase 4 for the why and the escape hatch if it's ever needed.
> **Companion reference:** [`MINTON_TEMPLATE_INDEX.md`](./MINTON_TEMPLATE_INDEX.md).
> **Alternatives considered and rejected:** see Appendix A.

---

## Goal (verbatim from the user)

> Keep the current front-end logic: the rendering of the fields, sections,
> pages — try not to touch the TypeScript logic. If at all possible, only
> change the return HTML template part in the `.tsx` files.
>
> The use of the Minton template is to bring in **the style** — how the page,
> section, navigation bar, and elements **look** like Minton — and to
> introduce the **global SCSS file** to control the HTML look. If possible,
> reuse the way Minton's **configurations** change the UI style.

## Principle

> **Minton becomes a stylesheet + a runtime theming protocol** — not a
> component library, not a React layout library.
>
> - The Minton SCSS bundle (`bootstrap.scss` + `app.scss` + `icons.scss`) is
>   imported once and provides every visual primitive (`.wrapper`, `.card`,
>   `.navbar-custom`, `.col-md-*`, `.btn-primary`, …).
> - Minton's `data-*` attribute protocol on `<html>` is re-implemented via a
>   30-line `useMintonTheme()` hook — gives runtime dark mode, sidebar
>   collapse, layout orientation, menu color, boxed/fluid width.
> - **No `react-bootstrap` is installed.** Components remain antd / antd Pro.
> - Existing controller, hook, and AsyncPage TS logic is **not modified**.
> - Only JSX `return (...)` blocks change — and only the outermost wrappers,
>   replacing inline-styled `<div>`s and `ProLayout` chrome with Minton-class
>   markup.

---

## Phase 1 — Foundation ✅ DONE (2026-06-28)

### 1.1 Install ✅

```bash
cd /Users/I043125/work2/IntelligentUI
npm install --legacy-peer-deps --save bootstrap@5.3.5 usehooks-ts@^3.1.1
npm install --legacy-peer-deps --save-dev sass@1.77.3
```

Three deps total. No `react-bootstrap`, no `cookies-next`, no chart libs.

### 1.2 Copy Minton SCSS + assets ✅

Copied from `template/Minton_v10.1.0/React.js/TS/src/assets/` to
`IntelligentUI/src/assets/`:

- `fonts/**` — icon font files (BoxIcons, MDI, Remix, FontAwesome, Feather, Weather, Pe-icon-7-stroke, dropify)
- `images/**` — Minton's images (25 files)
- `scss/icons.scss` — global icon font face declarations
- `scss/config/default/{_theme-config, _variables, _variables-dark, _variables-custom, app, bootstrap}.scss`
- `scss/custom/{components,plugins,icons,fonts,pages,structure}/**` — all Minton SCSS partials referenced by `app.scss`
- `scss/config/{creative,modern,saas,material,corporate}/**` — kept on disk for future variant swaps but **not imported**

### 1.3 Wire SCSS in `main.tsx` ✅

```tsx
// Order matters: Minton bundle FIRST so antd's reset wins on antd-specific
// component collisions; Minton's chrome classes remain otherwise active.
import '@/assets/scss/icons.scss';
import '@/assets/scss/config/default/bootstrap.scss';
import '@/assets/scss/config/default/app.scss';
import 'antd/dist/reset.css';
import './index.css';
import './i18n';
```

### 1.4 `useMintonTheme()` hook ✅

`src/hooks/useMintonTheme.ts` — 30-line minimal re-implementation of Minton's
`useLayoutContext`. Owns `MintonSettings` in localStorage under
`__INTELLIGENT_UI_THEME__`, writes seven `data-*` attributes onto `<html>` on
every change:

| Attribute | Type | Vocabulary |
|---|---|---|
| `data-bs-theme` | `theme` | `light \| dark` |
| `data-layout-mode` | `orientation` | `vertical \| horizontal \| detached \| two-column` |
| `data-topbar-color` | `topbarTheme` | `light \| dark` |
| `data-menu-color` | `menuTheme` | `light \| dark \| brand` |
| `data-sidebar-size` | `menuSize` | `default \| condensed \| compact \| sm-hover` |
| `data-layout-position` | `menuPosition` | `fixed \| scrollable` |
| `data-layout-width` | `layoutWidth` | `fluid \| boxed` |

Returns `{ settings, setSettings, updateSettings }`. Called once in
`MainLayout.tsx` — sidebar's `Toggle` button does
`updateSettings({menuSize: 'condensed'})` and Minton's SCSS handles the rest.

### 1.5 Replace `ProLayout` with Minton-classed shell ✅

`src/layouts/MainLayout.tsx` rewritten end-to-end:

- **Removed**: `@ant-design/pro-components` `ProLayout` + `PageContainer`,
  `menuConfigToProRoute()` adapter call
- **Kept**: `getMenuConfig()` (same source-of-truth), `useTranslation`,
  `useLocation`, `useNavigate`, `LanguageSwitcher`
- **Added**: a recursive `SidebarMenu` / `SidebarSubMenu` pair that renders
  the same `MenuConfigItem[]` tree into Minton's `<ul class="side-nav">`
  markup; a Minton-classed topbar (`<div class="navbar-custom">`); a footer

The `SidebarMenu` uses `<Link>` (react-router) for navigation — no Bootstrap
JS, no react-bootstrap. Submenu open/close is React state, not Bootstrap
collapse data attrs.

### 1.6 Verification ✅

```bash
cd /Users/I043125/work2/IntelligentUI
npx vite build         # ✅ compiles
npm run dev            # ✅ http://localhost:3001/ in ~200ms
```

Vite build output:
- `dist/assets/index-Bx1JUv_7.css` — **875 KB raw / 157 KB gzipped** (Minton + icons + antd reset)
- All icon fonts (boxicons, mdi, remix, font-awesome, feather, weather) bundled
- JS chunk 2.5 MB / 780 KB gzipped (pre-existing antd Pro footprint, no growth from this slice)

`npm run build` still reports pre-existing TS errors in `DocumentManagerFactory.ts`,
`AsyncUnion.tsx`, `StoreAvailableItemSection.tsx`, `mock/contracts.ts` —
**none introduced by this slice; none in files I touched.**

---

## Phase 2 — Re-skin shared shells (no controller TS changes) ✅ DONE (2026-06-28)

The whole point of C-lite: re-class the JSX returns of the *shared* shell
components, and every page that uses them inherits the Minton look for free.

| Order | File | Change | TS logic touched? |
|---|---|---|---|
| 2.1 ✅ | `src/components/ListPageShell.tsx` | Toolbar `<Space>` → `.page-title-box > .page-title-right`; `<ProTable>` wrapped in `.card > .card-body` | No |
| 2.2 ✅ | `src/components/EditPageShell.tsx` | Removed two `col-md-*` → `flexBasis` regex calculations and the flex-row wrapper; replaced with `<div className={section.colClass}>` and `<div className="row">` | No |
| 2.3 ✅ | `src/components/SearchPanel.tsx` | antd `<Card>` → `<div className="card"><div className="card-body"><h4 className="header-title mb-3">{title}</h4>…</div></div>`; dropped unused `Card` import | No |
| 2.4 ✅ | `src/components/page/AsyncSection.tsx` | This file renders `null` (constants/helpers module). Its `colClassToFlexStyle()` helper was deleted in step 2.5b after the four sibling section files (AsyncEditSection / AsyncSearchSection / AsyncEmbeddedListSection / AsyncCustomerContactSection) stopped calling it. `calSecColClass()` retained — still used by all four. | No (delete only) |
| 2.5 ✅ | `src/components/control/AsyncField.tsx` | `flexBasis` IIFE deleted; outer `<div style={display:flex,…}>` → `<div className="row">`; each field/refControl wrapper → `<div className={fieldColClass}>` where `fieldColClass = colClass ?? 'col-md-4'` | No |

### 2.5b — Sibling section files follow-up ✅ DONE (2026-06-28)

Replaced `colClassToFlexStyle()` inline-flex wrappers with `className={computedSecColClass}`
in the four sibling section files, then deleted the helper itself.

| File | Change |
|---|---|
| `src/components/page/AsyncEditSection.tsx` | Import `calSecColClass` instead of `colClassToFlexStyle`. Both render branches (antd `Card` + Bootstrap `portlet`) now use `className={\`${computedSecColClass} mb-3\`}` / `className={\`async-section ${computedSecColClass}\`}` on the outer wrapper instead of `style={{...colFlexStyle, marginBottom: 16}}` |
| `src/components/page/AsyncSearchSection.tsx` | Same swap — outer wrapper `className={\`${computedSecColClass} mb-3\`}` |
| `src/components/page/AsyncEmbeddedListSection.tsx` | Same swap — applied to all three return branches (Mode A editor, Mode B list, dev-time fallback) |
| `src/components/page/AsyncCustomerContactSection.tsx` | Same swap — outer wrapper `className={computedSecColClass}` (no `mb-3` because it sits inside an `InvolvePartySection` that handles its own bottom spacing) |
| `src/components/page/AsyncSection.tsx` | Deleted `colClassToFlexStyle()` helper (24 lines) — no longer referenced anywhere. `calSecColClass()` retained because it's used in all four sibling files. |

### Step 2.5b verification ✅

```bash
cd /Users/I043125/work2/IntelligentUI
npx vite build     # ✅ built in 10.90s
npm run dev        # ✅ http://localhost:3000/ in 137ms
```

Bundle deltas vs Phase 2:
- CSS: **unchanged** (875 KB raw / 157 KB gzipped)
- JS: **2,574.56 KB** vs Phase 2's 2,574.84 KB — another ~0.3 KB smaller (deleted helper body)

Cumulative bundle delta since Phase 0:
- CSS: +875 KB raw / +157 KB gzipped (Minton bundle, one-time cost)
- JS: ~0.6 KB *smaller* than baseline (3 IIFE/helper bodies deleted across Phase 2 + 2.5b)

### Phase 2 verification ✅

```bash
cd /Users/I043125/work2/IntelligentUI
npx vite build     # ✅ built in 12.37s
npm run dev        # ✅ http://localhost:3001/ in 134ms
```

Bundle deltas vs Phase 1:
- CSS: **unchanged** (875 KB raw / 157 KB gzipped)
- JS: **2,574.84 KB** vs Phase 1's 2,575.11 KB — ~0.3 KB smaller (deleted IIFEs)

### Rules followed

- Touched only the JSX `return (...)` blocks of the five files.
- Did not modify any controller hook (`useContractListController`,
  `useContractEditController`, etc.).
- Did not add or rename methods on any class.
- Did not change any prop signatures.
- Did not change `SelectField.tsx`, `InputFieldUnion.tsx`, `InputField.tsx`,
  `RefControl.tsx`, or any page component.

---

## Polish — Sidebar route awareness + Dashboard re-skin ✅ DONE (2026-06-28)

Two small follow-ups picked together because they're both purely JSX/className
changes and they close the visual loop: the sidebar reflects the current route,
and the landing page (Dashboard) finally looks Minton end-to-end.

### B — Sidebar route awareness + Minton hamburger

`src/layouts/MainLayout.tsx`:

- Added `findActiveChain(items, pathname)` — walks the menu tree DFS and returns
  the chain of keys from root down to the leaf whose `path === pathname`. Empty
  array when no match.
- Derived `activeChain` and `activeKeys` (Set) via `useMemo` keyed on
  `[menu, pathname]`.
- Seeded `useState<openKeys>` from `activeChain` so the active route's parents
  are expanded on first render. Added a `useEffect` that **adds** missing
  ancestor keys when the route changes — never collapses user-opened groups.
- Threaded `activeKeys` through `SidebarMenu` and `SidebarSubMenu` props.
  Replaced per-item `isActive = item.path === currentPath` with
  `isActive = activeKeys.has(item.key)` so parent groups also light up when a
  descendant is active.
- Replaced topbar hamburger `<MenuOutlined />` (antd icon) with
  `<i className="fe-menu" />` — Minton's standard Feather icon, already loaded
  via `assets/scss/icons.scss`. Dropped the `MenuOutlined` import.

### C — DashboardPage re-skin

`src/pages/dashboard/DashboardPage.tsx`:

- Replaced antd `<Row>` / `<Col>` / `<Card>` with Minton `.row` / `.col-xl-3 col-md-6` /
  `.card > .card-body`. Inline `gutter={[16,16]}` and `style={{marginTop:16}}`
  no longer needed — Bootstrap's `.row` provides gutter, `.card` provides margin.
- Kept antd `<Statistic>` — no Minton equivalent for animated number formatting.
- Replaced hard-coded `valueStyle={{color: '#3f8600'}}` (and `#cf1322`, `#d46b08`)
  with `var(--bs-success)` / `var(--bs-danger)` / `var(--bs-warning)` CSS
  variables. Now `data-bs-theme="dark"` automatically tunes those colors via
  Minton's `_variables-dark.scss` — no JS bridge needed.
- "Recent activity" card uses `<h4 className="header-title mb-3">` (Minton's
  card heading style) instead of antd `<Card title>`. Body uses
  `className="text-muted text-center"` utility classes.
- Dropped antd `Row`, `Col`, `Card` imports.

### Polish verification ✅

```bash
cd /Users/I043125/work2/IntelligentUI
npx vite build     # ✅ built in 15.21s
npm run dev        # ✅ http://localhost:3001/ in 153ms
```

Bundle deltas vs Phase 3a:
- CSS: **unchanged** (875 KB raw / 157 KB gzipped)
- JS: **2,574.88 KB** vs 3a's 2,574.67 KB — +0.21 KB (the `findActiveChain` helper)

### What to look for in `npm run dev`

- Open `/` (Dashboard) — sidebar's "Home" leaf has `.menuitem-active` highlight; cards laid out as 4 columns at xl, 2 columns at md, 1 column at sm; numbers in green/red/orange via CSS variables.
- Navigate to `/logistics/purchaseContract` — sidebar auto-expands the
  "Procurement" parent group AND its "Purchase Contracts" sub-group; the leaf
  "Contract List" gets `.active`; both parent groups get `.menuitem-active`.
- Manually collapse "Procurement" — the active leaf stays highlighted; the
  group stays closed (route change doesn't re-open user-collapsed groups
  unless the route changes to a path under that group).
- Click the hamburger button in the topbar — sidebar collapses to condensed
  mode; icon is now Minton's Feather hamburger, not antd's.
- Toggle `<html data-bs-theme="dark">` in DevTools — Minton chrome flips dark
  AND Dashboard's Statistic colors flip via CSS variables (green/red/orange
  shift to their dark-mode tones) AND antd components flip via the 3a bridge.

---

## Phase 3 — antd integration + optional ThemeCustomizer

### 3a — antd dark-mode bridge ✅ DONE (2026-06-28)

Wrapped `App.tsx`'s `<ConfigProvider>` to derive antd's theme algorithm
from `useMintonTheme().settings.theme`. Toggling Minton's `data-bs-theme`
now flips antd components (Tables, Modals, Form fields, etc.) at the same
time as Minton's chrome — single source of truth.

```tsx
const App: React.FC = () => {
  const { settings } = useMintonTheme();
  return (
    <ConfigProvider
      locale={enUS}
      theme={{
        algorithm: settings.theme === 'dark'
          ? antdTheme.darkAlgorithm
          : antdTheme.defaultAlgorithm,
      }}
    >
      <ErrorBoundary>
        <RouterProvider router={router} />
      </ErrorBoundary>
    </ConfigProvider>
  );
};
```

**Note:** `useMintonTheme()` is now called from two places (`App.tsx` for
the antd algorithm bridge; `MainLayout.tsx` for the sidebar toggle). Both
subscribe to the same `useLocalStorage` key, so they stay in sync. The
hook's `useEffect` writes `data-*` attrs idempotently — running twice per
change is a no-op DOM cost.

**Verification:** `npx vite build` succeeds in 18.40s. JS bundle
2,574.67 KB / 779.82 KB gzipped (+0.11 KB vs step 2.5b — added the hook
call and `antdTheme` import). CSS unchanged.

### 3b — ThemeCustomizer panel ✅ DONE (2026-06-28)

User-facing offcanvas drawer exposing all 7 `MintonSettings` knobs from
`useMintonTheme()`. Gear icon in topbar opens it; "Reset" button restores
defaults. No new dependency, no TS-logic changes elsewhere.

**Architecture:**

- `useMintonTheme()` (unchanged from Phase 1) is the single source of truth
- New `MINTON_DEFAULTS` constant exported from the hook so the customizer's
  reset button can use it without duplicating the literal
- New `src/components/ThemeCustomizer.tsx` (~190 lines):
  - antd `<Drawer placement="right" width={320}>` with a `<Button>` "Reset"
    in the drawer's `extra` slot
  - Seven `<Section>` blocks with `<Radio.Group>` controls — each binds
    `value={settings[key]}` and `onChange` to `updateSettings({[key]: ...})`
  - Local `<Section>` helper renders Minton's `.header-title` class +
    optional hint paragraph
- `MainLayout.tsx`:
  - Added `useState<themeCustomizerOpen>` and `<ThemeCustomizer>` mount
  - Added gear icon `<li>` between language switcher and user-menu in the
    topbar `<ul.topnav-menu>`; uses antd's `SettingOutlined`

**Orientation knob:** Vertical is the only enabled option. Horizontal,
Detached, and Two-column show as disabled radios with a hint explaining
that only vertical layout chrome ships in `MainLayout.tsx`. Enabling them
later means porting Minton's `HorizontalLayout` / `DetachedLayout` /
`TwoColumnLayout` from the template and adding a dispatcher.

**The 7 knobs:**

| Setting | data-* attribute | Values |
|---|---|---|
| theme | `data-bs-theme` | light, dark |
| topbarTheme | `data-topbar-color` | light, dark |
| menuTheme | `data-menu-color` | light, dark, brand |
| menuSize | `data-sidebar-size` | default, condensed, compact, sm-hover |
| menuPosition | `data-layout-position` | fixed, scrollable |
| layoutWidth | `data-layout-width` | fluid, boxed |
| orientation | `data-layout-mode` | vertical (only) |

### Phase 3b verification ✅

```bash
cd /Users/I043125/work2/IntelligentUI
npx vite build     # ✅ built in 18.16s
npm run dev        # ✅ http://localhost:3001/ in 160ms
```

Bundle deltas vs Polish step:
- CSS: **unchanged** (875 KB raw / 157 KB gzipped)
- JS: **2,578.49 KB / 780.70 KB gzipped** — +3.61 KB raw / +0.82 KB gzipped (drawer markup + 7 radio groups + new component file)

### What to look for in `npm run dev`

- Click the gear icon in the topbar (between language switcher and user
  avatar) → drawer opens from the right
- Toggle Color Scheme → light ↔ dark: Minton chrome flips, antd
  components flip (via 3a bridge), Dashboard CSS-variable colors flip
- Toggle Sidebar Size → default/condensed/compact/sm-hover: sidebar
  visually changes immediately
- Toggle Menu Color → light/dark/brand: sidebar background recolors
- Click Reset → all 7 knobs return to defaults; UI reverts
- All choices persist across page reloads via `localStorage` key
  `__INTELLIGENT_UI_THEME__`

---

## Phase 4 — Live SCSS variant swap ❌ SKIPPED (2026-06-28)

Formally closed out. **Not implementing — not worth the cost-benefit.**

### What it would have been

Minton ships six visual variants under `src/assets/scss/config/`:
`default` (active), `creative`, `modern`, `saas`, `material`, `corporate`.
Each is a complete alternative palette / typography / spacing for the same
Minton chrome (same `.wrapper`, `.card`, `.navbar-custom` — different
colors/fonts/tokens). Phase 4 would have let users flip between them at
runtime via an 8th knob in the ThemeCustomizer.

### Why skipped

1. **The official Minton React template treats variants as build-time only.**
   `template/Minton_v10.1.0/React.js/TS/src/App.tsx` has all six imports
   present but five are commented out. The vendor's own pattern is "pick
   one at build time" — strong signal that runtime switching is not the
   typical use case.

2. **Bundle cost is severe.** Pre-compiling all six (Option A — the
   simplest implementation) would inflate the CSS payload from 157 KB
   gzipped to ~940 KB gzipped — a 6× increase for a feature most users
   set once and never touch.

3. **The "right" approach (Option B — extract Minton's Sass variables
   into CSS custom properties) is genuinely a lot of work.** Reverse-
   engineering Minton's variable tree (which uses Sass `darken()` /
   `lighten()` / `mix()` calls that don't have CSS-variable equivalents)
   into a runtime-controllable token system is 1-2 days plus ongoing
   maintenance whenever Minton updates.

4. **No concrete user need.** No team member has asked for runtime variant
   switching. If a future request comes in, options remain viable.

### If you need to switch variants later (build-time)

The simplest path. Three lines in `src/main.tsx`:

```tsx
// Replace:
import '@/assets/scss/config/default/bootstrap.scss';
import '@/assets/scss/config/default/app.scss';

// With (e.g. for Creative):
import '@/assets/scss/config/creative/bootstrap.scss';
import '@/assets/scss/config/creative/app.scss';
```

All six variants are already on disk under `src/assets/scss/config/*`.
Rebuild and the entire UI re-skins. No other code changes required —
this is the whole point of having put Minton's SCSS bundle behind the
two import lines.

### If you ever want runtime switching (not recommended without a use case)

Three viable approaches, in order of practicality:

| | How | Effort | Bundle cost |
|---|---|---|---|
| **A. Pre-compile all 6, swap `<link>`** | Configure Vite to emit 6 separate CSS bundles; swap which `<link rel="stylesheet">` is active at runtime | Medium — ~100 lines + Vite config | +~4.4 MB CSS shipped |
| **B. CSS custom-properties** | Extract Minton tokens into `:root { --… }` blocks per variant; switch via a `<style>` injection | Hard — ~500 lines + ongoing maintenance | ~0 |
| **C. Dynamic SCSS import** | Vite `import()` for variant SCSS as lazy-loaded chunks | Medium-Hard — ~150 lines + chunking config | +~875 KB lazy per variant |

Pick **A** if you decide to do this — simplest, cleanest, and the bundle
cost is acceptable for an internal admin tool. Add a Variant `<Radio.Group>`
to `ThemeCustomizer.tsx` and an `8th` knob (`variant`) to `MintonSettings`
in `useMintonTheme.ts`.

---

## CSS Adjustments — overrides layer ✅ DONE (2026-06-29)

After the integration was declared complete, a round of visual-bug fixes
landed. To keep Minton vendor SCSS untouched and the changes auditable,
introduced a single project override layer: `src/styles/overrides.scss`,
imported in `main.tsx` **after** Minton's `app.scss` (so it wins on
selector-specificity ties) and **before** `antd/dist/reset.css` (so antd
component styles still win for antd components).

### File-level convention

Documented at the top of `overrides.scss`:

- **Use this file for:**
  - Minton × antd collision fixes (selector-targeted)
  - Visual bugs in Minton defaults that aren't token-driven
  - Fixes for our IntelligentUI-specific markup that uses class names
    from Minton's Next.js variant (`.side-nav-*`) which aren't styled by
    Minton's vanilla left-menu SCSS
- **Do NOT put here:**
  - Color / font-size / spacing tokens that affect Minton globally →
    use `_variables-custom.scss` (those participate in dark mode +
    Minton's derived calculations)
  - Per-component conditional logic → fix in the .tsx instead

Each rule block is grouped under a numbered "Issue N" comment matching
the entry in this section.

### The four fixes

| Issue | Symptom | Root cause | Fix |
|---|---|---|---|
| **1** | Second-level sidebar nav items (`采购订单`, `采购合同`, `供应商`) cramped — small font, tight vertical padding | Our `MainLayout.tsx` uses Minton **Next.js variant** class names (`.side-nav-second-level`, `.side-nav-third-level`). Minton's vanilla `_left-menu.scss` only targets `#sidebar-menu > ul > li > a` for top-level — nested items inherit nothing beyond a `ul ul { padding-left: 34px }` indent. | Explicit `padding: 10px 20px; font-size: 13.5px; line-height: 1.4` for `.side-nav-second-level > li > a`; smaller for third-level. Uses CSS custom properties `--bs-menu-item-color/hover/active` so dark mode + menu-color variant still apply. |
| **2** | Process buttons (Save/Exit) butting against the page-title bar with no visible gap | Phase 2.1 wrapped the toolbar in `<div className="page-title-box"><div className="page-title-right">…</div></div>`. Minton's `.page-title-box` ships with `padding: 15px 27px`, a box-shadow, and `margin: 0 -27px 30px` — designed for a row that has a left-side `.page-title` text AND a right-side action cluster. With only the right side rendered, the box-shadow creates a hairline that looks attached to the next `.card`, and the negative side margins cause horizontal overflow inside our `.container-fluid`. | Override `.page-title-box` to `margin: 0 0 24px 0; padding: 12px 0; box-shadow: none; background: transparent`. Provide an opt-in `.page-title-box.with-title` modifier for future pages that DO have a `.page-title` heading. |
| **3** | Topbar items (Language switcher, gear icon, Avatar) not vertically aligned with each other | Minton's `.navbar-custom .topnav-menu > li { float: left }` uses floats. The three items render through different paths (antd Button, raw `<button>`, `<a class="nav-link">`) and Minton's vertical centering relies on `line-height: 70px` on `.nav-link` only — our gear `<button>` and LanguageSwitcher don't inherit that. | Convert `.navbar-custom .topnav-menu` to `display: flex; align-items: center; height: 70px`. Force `> li { display: flex; align-items: center; height: 100% }`. Reset `button.nav-link` to `height: 100%; line-height: 1; background: transparent; border: none` so the gear button visually matches its `<a>` siblings. |
| **4** | Footer (2026 © IntelligentUI) scrolls up with page content; should stay pinned at viewport bottom | Minton's `.footer { position: absolute; bottom: 0 }` resolves to the first positioned ancestor. In Minton's vanilla template `.content-page` IS the positioning context (because `min-height: 80vh` + the wrapper layout). In our React app, the ancestor chain doesn't establish that context the way Minton expects, so `position: absolute` falls back to `<body>` and the footer scrolls with the document. | Switch to `position: fixed; bottom: 0; left: $leftbar-width; right: 0`. Add `data-sidebar-size` selectors so `left` shifts when sidebar collapses (70px / 160px / 240px) or on mobile (full-width). Reserve `.content-page { padding-bottom: 70px }` so the last content row isn't hidden behind the fixed footer. |

### CSS Adjustments — round 2 ✅ DONE (2026-06-29)

After round 1, three issues survived user verification: (a) topbar items still misaligned, (b) toolbar still cramped under the fixed topbar, (c) a new issue surfaced — sibling `col-md-6` sections side-by-side had zero horizontal gap because a parallel code path in `AsyncPage.tsx` was still using inline-flex instead of Bootstrap `.row`.

| Issue | What changed |
|---|---|
| **Patch to Issue 2** | `.page-title-box { padding: 12px 0 }` → `padding: 24px 0 8px 0`. The first round gave 12px breathing room which still felt cramped under the fixed 70px topbar. Top padding now 24px; bottom kept at 8px so the following `.card` doesn't drift too far below. |
| **Patch to Issue 3** | Three new realizations: **(a)** Bootstrap's `.d-none.d-lg-block` on the LanguageSwitcher `<li>` carries `display: block !important` at ≥lg — my plain `display: flex` lost the specificity battle. Added `!important` to the `> li` rule. **(b)** The first selector `> li > *` targets the antd `<Space>` wrapper, which is inline-flex anchored to its text baseline, not to the 70px topbar. Added `align-self: center` on all direct children. **(c)** Minton's `.nav-link { line-height: 70px }` fights flex centering once the parent is flex. Added a rule to reset `> li > a.nav-link { line-height: 1; height: auto }` matching the gear `<button>` rule from round 1. |
| **New Issue 5** | `AsyncPage.tsx` (the legacy doc/edit page rendering pipeline used by `AsyncEditorPage`) was still generating `<div style="display: flex; flex-wrap: wrap; width: 100%; margin: 0 -8px">` for multi-section rows. This is the same pattern Phase 2 replaced in `EditPageShell.tsx` — but `AsyncPage` is a separate code path I didn't touch. The inline-flex wrapper neutralizes Bootstrap's `.col-md-*` gutter so sibling `col-md-6` sections butt against each other. Fix: one-line JSX change — `style={multi ? { display: 'flex', flexWrap: 'wrap', ... } : undefined}` → `className={multi ? 'row' : undefined}`. Now sibling sections get Bootstrap's `--bs-gutter-x` (~24px between columns) automatically. |

### Round 2 verification ✅

```bash
cd /Users/I043125/work2/IntelligentUI
npx vite build     # ✅ built in 12.63s
npm run dev        # ✅ http://localhost:3001/ in 208ms
```

Bundle deltas vs round 1:
- CSS: **877.90 KB / 157.52 KB gzipped** — +0.41 KB raw / +0.07 KB gzipped (new alignment rules and adjusted padding)
- JS: **2,578.43 KB / 780.69 KB gzipped** — -0.06 KB (deleted the inline-flex style object in AsyncPage.tsx)

### Files touched in round 2

- `IntelligentUI/src/styles/overrides.scss` — updated Issue 2 padding values; rewrote Issue 3 block with `!important` override for `.d-lg-block`, `align-self: center` on direct children, and `a.nav-link` line-height reset to match the gear button.
- `IntelligentUI/src/components/page/AsyncPage.tsx` — replaced inline-flex multi-section row wrapper with `className="row"` (one-line JSX change; TS logic untouched).

### What to verify

- Topbar: language `EN / 中文` buttons now sit on the same baseline as the gear icon and Avatar dropdown. No more "hugging the top edge".
- Process buttons: clear ~24px gap between the fixed topbar and the toolbar buttons. Visually distinct, not cramped.
- Edit page (`/logistics/purchaseContract/:uuid/edit`): the two `采购方信息` / `供应商信息` cards sit side-by-side with a visible ~24px horizontal gap between them. Bootstrap's `.row` gutter is doing the work — no more touching.

---

### CSS Adjustments — round 3 ✅ DONE (2026-06-29)

After round 2, the user reported the process-button gap was still missing on the contract **edit** page (`/logistics/purchaseContract/:uuid/edit`). Round 1's Issue 2 fix targeted `.page-title-box` (used by `ListPageShell`), but the **edit page uses a different code path** that has no `.page-title-box` wrapper.

| Issue | What changed |
|---|---|
| **New Issue 6** | `AsyncPage.tsx` renders the toolbar as a bare antd `<Space>` directly inside `<div id="x-page-{uuid}">` — no `.page-title-box` wrapper, no margin/padding. My round-1+2 `.page-title-box { padding-top }` rule never applied. Fix: 3-line CSS rule in `overrides.scss` — `[id^="x-page-"] > .ant-space:first-child { margin-top: 24px }`. Surgical: matches AsyncEditorPage's toolbar specifically; doesn't affect ListPageShell, EditPageShell, or anywhere else. |

### Why two code paths in the first place

The `ListPageShell.tsx` (re-skinned in Phase 2.1) wraps the toolbar in `<div class="page-title-box"><div class="page-title-right">…</div></div>`. The `AsyncPage.tsx` pipeline (used by `AsyncEditorPage`) was never re-skinned — it still renders `<div id={comPageId}><ProcessButtonArray /><ProForm>…</ProForm></div>` with no wrapper around `ProcessButtonArray`. This is a real design smell (two pages with the same UX have different markup), but unifying them would mean editing `AsyncPage.tsx` JSX — out of scope for a pure-CSS round. Tracked: if a future round wants to unify the markup, replace `<ProcessButtonArray />` in `AsyncPage.tsx` with `<div className="page-title-box"><div className="page-title-right"><ProcessButtonArray /></div></div>` and delete Issue 6 from `overrides.scss`.

### Round 3 verification ✅

```bash
cd /Users/I043125/work2/IntelligentUI
npx vite build     # ✅ built in 16.30s
npm run dev        # ✅ http://localhost:3001/ in 357ms
```

Bundle deltas vs round 2:
- CSS: **877.95 KB / 157.55 KB gzipped** — +0.05 KB raw / +0.03 KB gzipped (3-line CSS rule)
- JS: **2,578.43 KB / 780.69 KB gzipped** — unchanged

### Round 3 — What to verify

- Navigate to `/logistics/purchaseContract/:uuid/edit` (a real contract — e.g. existing one from the list). The process buttons (`退出`, `反审核`, `交货完成`, `流程完成`, `保存`) now have a visible ~24px gap below the fixed 70px topbar. Matches the gap on the list page from rounds 1+2.

---

### Gear-button relocation ✅ DONE (2026-06-29)

User-driven reorganization, not a bug fix: the gear icon (Theme Customizer trigger) moved from the topbar to the footer's right side. About/Help dead links removed from the footer. Goal: free up topbar real-estate for future functions; consolidate "preferences"-style controls in the footer where they're discoverable but out of primary nav flow.

**Changes:**

- `IntelligentUI/src/layouts/MainLayout.tsx`:
  - **Removed** the gear `<li>` from the topbar `<ul.topnav-menu>` (was between LanguageSwitcher and Avatar Dropdown). Topbar now has 2 items: LanguageSwitcher + Avatar.
  - **Replaced** the footer's right `<div className="col-md-6">` content. Was `<div className="text-md-end footer-links d-none d-sm-block"><a href="#">About</a><a href="#">Help</a></div>` (two dead `#` links). Now a single right-aligned `<button>` with `<SettingOutlined>` icon, transparent background, no border, `cursor: pointer`. Calls `setThemeCustomizerOpen(true)` — same handler as before.
- `IntelligentUI/src/styles/overrides.scss`:
  - **Pruned** the now-orphan `.navbar-custom .topnav-menu > li > button.nav-link { ... }` block from Issue 3. No `<button class="nav-link">` exists in the topbar anymore. The `> li > a.nav-link` rule (for the Avatar Dropdown's `<a>`) is preserved.

**Verification:**

```bash
cd /Users/I043125/work2/IntelligentUI
npx vite build     # ✅ built in 19.23s
npm run dev        # ✅ http://localhost:3001/ in 152ms
```

Bundle deltas:
- CSS: **877.59 KB / 157.50 KB gzipped** — −0.36 KB raw / −0.05 KB gzipped (pruned the orphan button.nav-link rule)
- JS: **2,578.20 KB / 780.64 KB gzipped** — −0.23 KB (removed the topbar gear `<li>` markup)

**What to verify:**

- Topbar now shows only the language switcher and the Avatar dropdown — the gear icon is gone.
- Footer right side shows the gear icon (no more About/Help text links).
- Clicking the gear in the footer still opens the Theme Customizer drawer with all 7 knobs.

---

### Card-size Plan B — remove `size="small"` from section cards ✅ DONE (2026-06-29)

User-reported regression: section headers across the app (Edit page sections, Search section, Embedded-list sections) rendered with a 38px height and 14px font, while the InvolvePartySection (the only section without `size="small"`) rendered with the proper 56px / 16px header. The visual hierarchy was inverted — section titles looked smaller than their own field labels.

**Root cause:** antd 5.x's CSS-in-JS engine generates this rule at runtime when any `<Card>` has `size="small"`:

```css
:where(.css-dev-only-do-not-override-mncuj7).ant-card-small > .ant-card-head {
  min-height: 38px;
  padding: 0 12px;
  font-size: 14px;
}
```

The rule is **generated by antd itself** (see `node_modules/antd/es/card/style/index.js`, the `headerHeightSM` / `headerPaddingSM` / `headerFontSizeSM` tokens). It's not in our SCSS; we can't delete it. It activates whenever a `<Card>` has `size="small"`, which adds the `ant-card-small` class.

**Fix (Plan B — chosen over Plan A's CSS override):** removed `size="small"` from all 5 `<Card>` JSX locations. The `ant-card-small` class is no longer added, the antd-small rule never matches, and the default `.ant-card .ant-card-head` rule (min-height: 56px, padding: 0 24px, font-size: 16px, font-weight: 600) applies naturally.

Why Plan B over Plan A (CSS override): semantically cleaner. If we want default-size header dimensions, the prop saying "I want small dimensions" shouldn't be there. Body padding is preserved via the existing inline `styles={{ body: { padding: ... } }}` props on 4 of the 5 files.

**JSX edits — 5 locations:**

| File | Before | After |
|---|---|---|
| `AsyncEditSection.tsx:368` | `<Card title={cardTitle} size="small" styles={{ body: { padding: '12px 16px' } }}>` | `<Card title={cardTitle} styles={{ body: { padding: '12px 16px' } }}>` |
| `AsyncSearchSection.tsx:199` | `<Card title={cardTitle} size="small" styles={{ body: { padding: '12px 16px' } }}>` | `<Card title={cardTitle} styles={{ body: { padding: '12px 16px' } }}>` |
| `AsyncEmbeddedListSection.tsx:136` (Mode A: editor) | `<Card title={cardTitle} size="small" styles={{ body: { padding: '8px 0' } }}>` | `<Card title={cardTitle} styles={{ body: { padding: '8px 0' } }}>` |
| `AsyncEmbeddedListSection.tsx:252` (Mode B: list) | `<Card title={cardTitle} size="small" styles={{ body: { padding: '8px 0' } }}>` | `<Card title={cardTitle} styles={{ body: { padding: '8px 0' } }}>` |
| `AsyncEmbeddedListSection.tsx:278` (Mode C: fallback) | `<Card title={cardTitle} size="small">` | `<Card title={cardTitle}>` |

**Not modified:**
- `AsyncSearchSection.tsx:203` — `<Tabs items={tabItems} size="small">` (different component; `size` on Tabs controls tab-bar density, not card-head)
- `AsyncEmbeddedListSection.tsx:266` — `<ProTable size="small">` (different component; controls table row density)

Both of those legitimately want compact rendering and don't trigger the `.ant-card-small` rule.

**Verification:**

```bash
cd /Users/I043125/work2/IntelligentUI
npx vite build     # ✅ built in 19.91s
npm run dev        # ✅ http://localhost:3001/ in 185ms
```

Bundle deltas:
- CSS: **877.59 KB / 157.50 KB gzipped** — unchanged (no CSS edits)
- JS: **2,578.13 KB / 780.64 KB gzipped** — −0.07 KB (deleted 5 `size="small"` prop strings)

**What to verify:**

- Open `/logistics/purchaseContract/:uuid/edit`. The `采购合同信息` card header now renders at 56px tall with 16px bold title — visually distinct as a section header rather than a tight inline row.
- The `采购方信息` and `供应商信息` section headers also render at the larger size.
- The search panel header on `/logistics/purchaseContract` likewise.
- The InvolvePartySection (which never had `size="small"`) is unchanged — all section headers across the app now match its sizing.

---

### Issue 7 — Brand-dark color on form labels + section card titles ✅ DONE (2026-06-29)

User request: change all form-field labels and section card titles to `#01053e` (legacy IntelligentUI brand-dark, deep navy) in the **light theme**. Dark theme should keep antd defaults (the brand-dark would be unreadable on dark backgrounds).

**Approach chosen: centralize the value as a token + Tier 2 CSS custom property + Tier 3 selector overrides.** This is the textbook three-tier pattern from `MINTON_CUSTOMIZATION_GUIDE.md`. The alternative (literal hex in `overrides.scss`) would have been one line shorter but locked the color into one location — future "use brand-dark elsewhere" requests would scatter hex literals across files.

**Changes:**

1. **`src/assets/scss/config/default/_variables-custom.scss`** — added:
   ```scss
   $brand-dark:  #01053e;
   ```
   Available everywhere Sass variables are imported.

2. **`src/assets/scss/config/default/_theme-config.scss`** — added inside the `:root, [data-bs-theme="light"]` block:
   ```scss
   --#{$prefix}brand-dark: #{$brand-dark};
   ```
   Emits `--bs-brand-dark: #01053e` **only in light mode**. Dark mode's `html[data-bs-theme="dark"]` block doesn't define it, so `--bs-brand-dark` is undefined there.

3. **`src/styles/overrides.scss`** — appended Issue 7 block:
   ```scss
   .ant-form-item-label > label   { color: var(--bs-brand-dark); }
   .ant-card-head-title           { color: var(--bs-brand-dark); }
   ```

**Theme cascade behavior:**

| Theme | `--bs-brand-dark` | Resolution | Result |
|---|---|---|---|
| Light | `#01053e` | Rule applies | Labels & titles deep navy |
| Dark | undefined | `color: var(--bs-brand-dark)` is invalid CSS, antd cascade restores its default | Labels & titles in antd default light text on dark bg |

No JavaScript theme bridge needed — pure CSS custom-property cascade.

**Targets caught by the two selectors:**

- `.ant-form-item-label > label` — every antd `<Form.Item>` and ProForm field label (text inputs, selects, date pickers, etc.), including the `.ant-form-item-required` red-asterisk variant
- `.ant-card-head-title` — every antd `<Card>` title, whether the title is a plain string (e.g. `采购方信息`) or a `<Space>` wrapper from `PortletHeadEle` (icon + label, e.g. `采购合同信息`)

**Verification:**

```bash
cd /Users/I043125/work2/IntelligentUI
npx vite build     # ✅ built in 17.51s
npm run dev        # ✅ http://localhost:3001/ in 175ms
```

Bundle deltas vs Card-size Plan B:
- CSS: **877.72 KB / 157.55 KB gzipped** — +0.13 KB raw / +0.05 KB gzipped (token definition + Issue 7 rules)
- JS: **2,578.13 KB / 780.64 KB gzipped** — unchanged

**To verify:**

- Open `/logistics/purchaseContract` or `/logistics/purchaseContract/:uuid/edit`. All form labels (`合同编号`, `合同名称`, etc.) render in deep navy `#01053e` instead of antd's default near-black.
- All section card titles (`采购合同信息`, `采购方信息`, etc.) render in the same deep navy.
- Toggle `<html data-bs-theme="dark">` in DevTools → form labels & section titles flip back to antd's light-on-dark default (the brand-dark color stops applying because the CSS variable is undefined in dark mode).

**Reusing the brand-dark color elsewhere:**

Three equivalent ways (use whichever fits the context):
- In `.scss` files: `color: $brand-dark;`
- In `overrides.scss` or any CSS: `color: var(--bs-brand-dark);`
- For a new selector that should pick up the same color: add another rule to Issue 7

If a future use needs the brand-dark in dark mode too, edit `_theme-config.scss` and add `--bs-brand-dark` to the `html[data-bs-theme="dark"]` block with a dark-mode-readable value (e.g. a much lighter shade).

---

## What was deliberately NOT done

- ❌ Did not install `react-bootstrap` — antd remains the only component library
- ❌ Did not install `cookies-next` — IntelligentUI uses its own auth/i18n
- ❌ Did not adopt Minton's `LayoutContext`/`AuthContext`/`NotificationContext` — `useMintonTheme` covers what's needed; auth/notifications stay as-is
- ❌ Did not copy Minton's `src/layouts/VerticalLayout.tsx` — we wrote a new `MainLayout.tsx` from scratch using only Minton class names
- ❌ Did not import any of Minton's 264 sample pages, widgets, or chart components
- ❌ Did not change any controller, hook, or AsyncPage TypeScript logic
- ❌ Did not enable a class-name prefix (`bs-`) — using Minton's classes verbatim was the explicit goal

---

## Files touched (Phase 1)

| Action | Path | Purpose |
|---|---|---|
| Modified | `package.json` | Added `bootstrap@5.3.5`, `usehooks-ts@^3.1.1` (deps); `sass@1.77.3` (devDeps) |
| Modified | `src/main.tsx` | Added 3 Minton SCSS imports before `antd/dist/reset.css` |
| Modified | `src/layouts/MainLayout.tsx` | Replaced `ProLayout`+`PageContainer` shell with hand-written Minton-classed layout (`.wrapper`/`.left-side-menu`/`.navbar-custom`/`.content-page`/`.content`/`.container-fluid`/`.footer`) consuming the existing `getMenuConfig()` |
| Created | `src/hooks/useMintonTheme.ts` | `useMintonTheme()` hook — owns settings in localStorage, writes 7 `data-*` attrs onto `<html>` |
| Created (bulk copy) | `src/assets/scss/**` | Minton SCSS bundle (config/default/* + custom/* + icons.scss + 5 other variants kept dormant) |
| Created (bulk copy) | `src/assets/fonts/**` | Minton icon font files (BoxIcons, MDI, Remix, FontAwesome, Feather, Weather, Pe-icon-7-stroke, dropify) |
| Created (bulk copy) | `src/assets/images/**` | Minton image assets (25 files) |

---

## Risk Assessment

| Risk | Mitigation |
|---|---|
| antd × Minton CSS collision on component-level classes (`.btn`, `.form-control`) | `antd/dist/reset.css` loads **after** Minton, so antd component styles win for antd components. Verified on dev server. |
| Class-name pollution leaking into antd internals | Minton's chrome classes (`.wrapper`, `.left-side-menu`, `.card`, `.navbar-custom`) don't collide with antd's `.ant-*` prefixes. |
| `react-router` `<Link>` vs Bootstrap `data-bs-toggle` | We don't use Bootstrap JS; sidebar collapse is React state. No collision. |
| Bundle size growth | CSS +~875 KB raw / +157 KB gzipped (includes 6 icon fonts). JS unchanged (~2.5 MB / 780 KB gzipped). |
| Pre-existing TS errors block `npm run build` | Pre-existed before this slice; resolving them is separate work. `npx vite build` (which is what production deploys use) succeeds. |
| Sidebar menu state isn't synced with current route | Phase-2 work — auto-expand the parent of the active leaf. Tracked but not blocking. |

---

## Appendix A — Alternatives considered and rejected

This document went through three drafts before settling on C-lite. The
rejected paths are recorded here for future readers:

### Option A — Bootstrap grid + utilities only, `bs-` prefixed

Install only `bootstrap@5` + `sass`. Generate utility classes scoped with
`$prefix: "bs-"` to guarantee zero antd collisions. Hand-author a custom
sidebar/topbar. **Rejected:** wastes the installed Minton template — gets
~5KB gzipped of grid + spacing utilities but none of the visual identity.

### Option B — Full Minton adoption (chrome + components + theme)

Install all ~20 of Minton's React deps including `react-bootstrap`. Adopt
Minton's React layouts (`VerticalLayout`, `HorizontalLayout`, …),
`ThemeCustomizer`, charts, widgets. Manage antd × react-bootstrap component
overlap per-family. **Rejected:** brings in a second component library
alongside antd Pro, contradicting the stated goal of keeping antd as the only
component framework. Largest install footprint and largest collision surface.

### Option C — Hybrid: Minton chrome via react-bootstrap, antd content

Install `bootstrap@5`, `react-bootstrap`, `sass`, plus copy Minton's React
layout TSX files. ESLint rule restricts `react-bootstrap` imports to
`src/layouts/` and `src/components/chrome/`. `ConfigProvider` bridges
`data-bs-theme` to antd `darkAlgorithm`. **Rejected (for now):** still
introduces `react-bootstrap`, which the user's stated scope did not include.
Reserved as a future expansion path if a Minton chrome component proves
hard to reproduce by hand.

### Option C-lite — Style-only Minton (this plan)

Install only `bootstrap@5` + `sass` + `usehooks-ts`. Use Minton's pre-built
SCSS bundle for visual classes. Re-implement Minton's `data-*` configuration
protocol in a 30-line hook. Hand-write the layout TSX using Minton class
names. **Chosen** because it matches the user's request exactly:
- ✅ TS logic untouched
- ✅ Only JSX `return` blocks change
- ✅ Global SCSS is the source of truth for visuals
- ✅ Configuration uses Minton's exact `data-*` mechanism
- ✅ No second component library
- ✅ Smallest possible install (3 deps)
