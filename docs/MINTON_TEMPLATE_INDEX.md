# Minton v10.1.0 — React TypeScript Template Index

Reference document for evaluating Bootstrap/Minton integration into `IntelligentUI`.
The template ships pre-built at:
`/Users/I043125/work2/IntelligentUI/template/Minton_v10.1.0/React.js/TS/`

This index focuses **only** on the React.js/TS variant — the only one whose stack
(Vite + React 19 + TS) lines up with IntelligentUI. Other variants
(ASP.Net, Angular, Laravel, Next.js, HTML, …) are present but out of scope.

---

## 1. Stack & build

| Layer | Choice | Version |
|---|---|---|
| Runtime | React + react-dom | ^19.1.0 |
| Router | react-router-dom | ^7.5.2 |
| Bundler | Vite + @vitejs/plugin-react | ^6.3.1 / ^4.3.4 |
| Language | TypeScript (composite project) | ~5.7.2 |
| UI framework | Bootstrap + react-bootstrap | ^5.3.5 / ^2.10.9 |
| Styling | Sass | 1.77.3 |
| Forms | react-hook-form + yup + @hookform/resolvers | ^7.56 / ^1.6 / 2.8 |
| Tables | react-table | 7.7.0 |
| Charts | apexcharts, chart.js (+ react wrappers) | ^4.6 / ^4.4 |
| Calendar | @fullcalendar/* (react, daygrid, timegrid, list, bootstrap, interaction) | ^6.1.17 |
| Drag/drop | @dnd-kit/core, sortable, utilities, react-nestable | ^6.3 / ^10 / ^3.2 / ^3.0 |
| Rich text | react-quill-new, easymde + react-simplemde-editor | ^3.4 / ^2.2 / ^5.2 |
| HTTP | axios + axios-mock-adapter | ^1.9 / ^2.1 |
| Auth/session | cookies-next | ^5.1 |
| Alerts | sweetalert2 + sweetalert2-react-content | ^11.19 / ^5.1 |
| Maps | jsvectormap | 1.3.3 |
| Misc | usehooks-ts, simplebar-react, react-countdown, react-countup, react-dropzone, react-datepicker, react-select, react-bootstrap-typeahead, react-use-wizard, @react-input/mask, yet-another-react-lightbox | various |

**Scripts**: `dev` (vite), `build` (`tsc -b && vite build`), `lint`, `preview`.

**TypeScript**: composite project, `@/*` alias → `src/*`, strict mode on. ESLint
config relaxes `no-explicit-any`, `no-unused-vars`, `exhaustive-deps`.

**Notable absences** (vs IntelligentUI current stack): no Ant Design, no
@ant-design/pro-components, no Redux / Zustand, no i18n library, no Pinia/Vuex equivalent.

---

## 2. Top-level layout

```
template/Minton_v10.1.0/React.js/TS/
├── index.html                # mounts /src/main, root div#root
├── package.json
├── vite.config.ts            # minimal: react plugin + @/* alias
├── tsconfig.{json,app,node}.json
├── eslint.config.js
├── config.env                # VITE_API_URL: process.env.REACT_APP_API_URL
├── public/favicon.ico
└── src/
    ├── main.tsx              # createRoot → <StrictMode><BrowserRouter><App/>
    ├── App.tsx               # AppWrapper + AppRouter + theme SCSS imports
    ├── AppWrapper.tsx        # Auth > Layout > Notification providers (re-export)
    ├── app/                  # 264 page.tsx files in (admin) (auth) (error) groups
    ├── routes/
    │   ├── index.tsx         # lazy-loaded route arrays
    │   └── router.tsx        # AppRouter — auth guard + Routes
    ├── layouts/              # MainLayout, Default, Vertical, Horizontal,
    │                         # Detached, TwoColumn, Topbar, LeftSidebar,
    │                         # Footer, Menu, RightSidebar/
    ├── components/           # ~28 widgets + Form/, ThemeCustomizer/, Topbar/, VectorMap/
    ├── context/              # useAuthContext, useLayoutContext, useNotificationContext
    ├── constants/menu.ts     # MENU_ITEMS, HORIZONTAL_MENU_ITEMS, TWO_COl_MENU_ITEMS
    ├── helpers/              # fake-backend.ts, httpClient.ts, layout.ts, menu.ts
    ├── hooks/                # useViewPort.ts, index.ts
    ├── utils/                # array.ts, chart.ts, layout.ts, index.ts
    ├── types/                # LayoutState, MenuType, AuthType, …
    ├── assets/
    │   ├── fonts/  images/
    │   ├── scss/icons.scss
    │   └── scss/config/{default,creative,modern,saas,material,corporate}/
    │         ├── _theme-config.scss
    │         ├── _variables.scss
    │         ├── _variables-dark.scss
    │         ├── _variables-custom.scss
    │         ├── bootstrap.scss
    │         └── app.scss
    ├── module-shims.d.ts
    ├── react-table-config.d.ts
    └── vite-env.d.ts
```

---

## 3. Entry chain

```
index.html  (div#root + <script src="/src/main">)
   ↓
main.tsx
   createRoot(...).render(
     <StrictMode><BrowserRouter><App/></BrowserRouter></StrictMode>
   )
   ↓
App.tsx
   import 'react-datepicker/dist/react-datepicker.css'
   import 'jsvectormap/dist/css/jsvectormap.min.css'
   import '@/assets/scss/icons.scss'
   import '@/assets/scss/config/default/bootstrap.scss'   ← active theme
   import '@/assets/scss/config/default/app.scss'         ← active theme
   // (5 alternate themes commented out)
   configureFakeBackend()
   <AppWrapper><AppRouter/></AppWrapper>
   ↓
AppWrapper.tsx
   <AuthProvider><LayoutProvider><NotificationProvider>
     {children}
   ↓
routes/router.tsx (AppRouter)
   authRoutes  → wrapped in <DefaultLayout> (no guard)
   appRoutes   → wrapped in <MainLayout> if isAuthenticated, else <Navigate to=/auth/login>
```

`isAuthenticated` is derived from a cookie via `useAuthContext`
(cookies-next), not from any external IdP. The bundled `fake-backend.ts` +
`axios-mock-adapter` intercept HTTP calls so the template runs standalone.

---

## 4. Routing

Routes are flat arrays of `{ path, name, element }` in `src/routes/index.tsx`
with `React.lazy(() => import('@/app/(admin)/.../page'))` for each.

Two exported arrays consumed by `AppRouter`:

- **`authRoutes`** — `/auth/*`, `/auth2/*`, `/404`, `/maintenance`, `/error-500`,
  `/coming-soon`. Rendered inside `DefaultLayout` (no sidebar/topbar, used for
  login & error screens). Two parallel auth styles ("auth" vs "auth2").
- **`appRoutes`** — everything else. Rendered inside `MainLayout` only when
  authenticated, otherwise redirect to `/auth/login?redirectTo=…`.
  Composed as:
  `initialRoutes ∪ generalRoutes ∪ layoutRoutes ∪ appsRoutes ∪ customRoutes ∪
   baseUIRoutes ∪ advancedUIRoutes ∪ chartsNMapsRoutes ∪ formsRoutes ∪
   tableRoutes ∪ iconRoutes ∪ authRoutes`.

Top-level URL families:

| Prefix | Purpose | Examples |
|---|---|---|
| `/dashboard/*` | Sample dashboards | `analytics`, `crm`, `sales` |
| `/apps/*` | Domain apps | `ecommerce/*` (10 pages), `chat`, `email/*`, `companies`, `tasks/*`, `tickets`, `contacts/*`, `file-manager`, `calendar` |
| `/pages/*` | Static-style pages | `starter`, `faq`, `sitemap`, `invoice`, `search-results`, `timeline`, `pricing`, `gallery`, `profile`, `404-alt` |
| `/widgets` | Widget catalogue | — |
| `/ui/*` | Base Bootstrap UI samples | `tabs-accordions`, `avatars`, `buttons`, `cards`, `carousel`, `dropdowns`, `grid`, `images`, `modals`, `offcanvas`, `placeholders`, `portlets`, `progress`, `ribbons`, `spinners`, `tooltips-popovers`, `typography`, `list-group`, `notifications`, `general`, `embedvideo` |
| `/extended-ui/*` | Advanced widgets | `sweet-alert`, `nestable` |
| `/charts/*`, `/maps/*` | Chart & map samples | `apex`, `chartjs`, `vectormaps` |
| `/forms/*` | Form variants | `basic`, `advanced`, `validation`, `upload`, `editors` |
| `/tables/*` | Tables | `basic`, `advanced` |
| `/icons/*` | Icon catalogues | `boxicons`, `feather`, `remix`, `mdi`, `font-awesome`, `weather` |
| `/layouts/*` | Live layout previews | `dark`, `vertical`, `horizontal`, `detached`, `two-column` |
| `/auth/*`, `/auth2/*` | Login / register / lock / etc. | two parallel styles |

Default landing: `/` → `Navigate to=/dashboard/analytics`.
Catch-all `*` → `NotFoundAdmin`.

---

## 5. Layout system

`MainLayout` is a thin dispatcher that picks one of these based on
`useLayoutContext().orientation` + sidebar width:

| Layout component | Folder/File | What it renders |
|---|---|---|
| `VerticalLayout` | `layouts/VerticalLayout.tsx` | Topbar + collapsible left sidebar + content + footer |
| `HorizontalLayout/` | `layouts/HorizontalLayout/` | Topbar with menubar across the top, no sidebar |
| `TwoColumnLayout/` | `layouts/TwoColumnLayout/` | Two-column sidebar (icon rail + secondary panel) + content |
| `DetachedLayout` | `layouts/DetachedLayout.tsx` | Floating "detached" sidebar variant |
| `DefaultLayout` | `layouts/Default.tsx` | Bare wrapper for auth/error pages |

Shared chrome: `Topbar.tsx`, `LeftSidebar.tsx`, `Menu.tsx`, `Footer.tsx`,
`RightSidebar/` (theme customizer panel).

**Layout state lives in `LayoutContext`** (`src/context/useLayoutContext.tsx`):

```ts
INIT_STATE: LayoutState = {
  theme: 'light',                  // light | dark
  orientation: 'vertical',         // vertical | horizontal | detached | two-column
  topbarTheme: 'light',
  menu: { theme: 'dark', size: 'default', position: 'fixed' },
  width: 'fluid',                  // fluid | boxed
  showUserInfo: false,
}
```

Persisted in `localStorage` under key `__MINTON_NEXT_CONFIG__` via
`usehooks-ts`. On every settings change, an effect writes a set of
`data-*` attributes onto `<html>`:

```
data-bs-theme         ← settings.theme
data-layout-width     ← settings.width
data-topbar-color     ← settings.topbarTheme
data-menu-color       ← settings.menu.theme
data-sidebar-size     ← settings.menu.size
data-layout-position  ← settings.menu.position
data-layout-mode      ← settings.orientation
```

The SCSS selectors key off these attributes — that is the entire
runtime theming mechanism. **No CSS bundle swap at runtime.**

The `RightSidebar` ("ThemeCustomizer") is the user-facing UI that calls
`changeTheme / changeLayoutOrientation / changeMenuTheme / …` to mutate this
state.

---

## 6. Theming (SCSS)

Six theme variants ship under `src/assets/scss/config/<variant>/`:

```
default/   creative/   modern/   saas/   material/   corporate/
```

Each variant contains the same five files:

- `_theme-config.scss` — entry partial pulling the rest together
- `_variables.scss` — Bootstrap SCSS variable overrides (light)
- `_variables-dark.scss` — same, for dark mode
- `_variables-custom.scss` — Minton-specific tokens (sidebar widths, etc.)
- `bootstrap.scss` — `@import` Bootstrap with this variant's variables
- `app.scss` — Minton component styles compiled on top of Bootstrap

Switching variants is **build-time only** — uncomment a different pair of
imports in `App.tsx`:

```tsx
// For Default
import '@/assets/scss/config/default/bootstrap.scss'
import '@/assets/scss/config/default/app.scss'

// For Creative (currently commented)
// import '@/assets/scss/config/creative/bootstrap.scss'
// import '@/assets/scss/config/creative/app.scss'
// … etc
```

Within a chosen variant, **light/dark** and all layout permutations are pure
CSS driven by the `data-*` attributes set by `LayoutContext`.

Plus `src/assets/scss/icons.scss` — icon font bundle (BoxIcons, Feather, Remix,
MDI, FontAwesome, Weather) imported once globally.

---

## 7. Menu / navigation

Static data, not derived from routes.

`src/constants/menu.ts` exports three trees:

- `MENU_ITEMS` — vertical sidebar
- `HORIZONTAL_MENU_ITEMS` — top menubar
- `TWO_COl_MENU_ITEMS` — two-column layout

Shape:

```ts
interface MenuItemTypes {
  key: string
  label: string
  parentKey?: string
  url?: string
  icon?: string
  badge?: { ... }
  children?: MenuItemTypes[]
  isTitle?: boolean
  isDisabled?: boolean
}
```

`src/helpers/menu.ts` is a tiny API around them (`getMenuItems`, recursive
`findMenuItem`, `findAllParent`, `getMenuItemFromURL`) with a comment hinting
that "you can fetch from server" — but the template never does. Routes in
`routes/index.tsx` are entirely independent of menu data — keeping them in
sync is the integrator's responsibility.

---

## 8. State, auth, networking

| Concern | Mechanism |
|---|---|
| Global state | React Context only — no Redux/Zustand/Pinia |
| Auth | `useAuthContext` reads/writes session cookie via `cookies-next` |
| Layout | `useLayoutContext` + `usehooks-ts` `useLocalStorage` |
| Notifications | `useNotificationContext` |
| HTTP | `helpers/httpClient.ts` wraps axios; `configureFakeBackend()` registers `axios-mock-adapter` handlers at boot |
| Forms | react-hook-form + yup resolvers (see `components/Form/`) |
| Tables | react-table v7 (legacy hooks API, not v8 `@tanstack/react-table`) |

---

## 9. Components inventory (high-level)

`src/components/` (flat unless noted):

| Component | Notes |
|---|---|
| `AppWrapper.tsx` | Re-export of provider stack |
| `AuthLayout.tsx`, `AuthLayout2.tsx` | Two styles for `/auth` vs `/auth2` |
| `PageBreadcrumb.tsx` | Standard breadcrumbs |
| `Portlet.tsx`, `BasicPortlet.tsx` | Card-with-toolbar widget |
| `Table.tsx` | react-table wrapper |
| `Pagination.tsx` | Page nav |
| `FileUploader.tsx` | react-dropzone wrapper |
| `CustomDatePicker.tsx` | react-datepicker wrapper |
| `Rating.tsx`, `Spinner.tsx`, `Loader.tsx`, `Preloader.tsx` | UI atoms |
| `StatisticsWidget.tsx`, `StatisticsChartWidget.tsx`, `ChartStatistics.tsx` | Dashboard cards |
| `PricingCard.tsx`, `FAQs.tsx`, `SocialLinks.tsx` | Marketing-style blocks |
| `Chats.tsx`, `ChatList.tsx`, `MessageList.tsx`, `MessageItem.tsx`, `Messages.tsx` | Chat module |
| `Tasks.tsx`, `TodoList.tsx` | Task widgets |
| `Form/` | `VerticalForm.tsx`, `FormInput.tsx` — RHF + yup helpers |
| `ThemeCustomizer/` | Right-side offcanvas for live layout tweaks |
| `Topbar/` | Topbar subcomponents (user menu, notifications, search) |
| `VectorMap/` | jsvectormap React wrapper |

---

## 10. Gotchas worth knowing before integration

1. **React 19**, not 18. Any IntelligentUI dep that pins React 18 will need a
   peer-deps audit.
2. **react-router-dom v7**, not v6. API mostly compatible but worth confirming
   for any custom router code in IntelligentUI.
3. **react-table v7** is the legacy hooks API; IntelligentUI's Ant Design
   ProTable is its own table system — these don't compose, they're alternatives.
4. **Bootstrap 5.3 + Ant Design coexistence** is the central risk. Both ship
   their own normalize/reset, their own grid, their own button/form styles, and
   their own JS for modals/dropdowns/tooltips. Integrating both means deciding
   per-area which framework "wins."
5. **SCSS theme is build-time switchable, not runtime.** If the integration
   plan wants live theme variant switching (default → creative), that needs
   either CSS variable extraction or shipping all six bundles with conditional
   `<link>` tags — neither is what the template does.
6. **No i18n library.** IntelligentUI's existing migration uses keyed JSON
   (`src/locales/zh-CN/...`) — the template has no equivalent and pages embed
   English text directly.
7. **Static menu vs router source-of-truth.** IntelligentUI's existing menu
   pattern (per memory) is derived/declared with the route; Minton keeps them
   completely separate. Picking one model matters.
8. **Cookies-next is a Next.js-leaning lib.** Works in Vite/SPA but its naming
   reveals the template's Next.js origin — the `(admin)` / `(auth)` / `(error)`
   folder grouping in `src/app/` is Next.js route-group syntax repurposed as
   plain filesystem organization here.
9. **264 page files**, almost all decorative samples. The actual code worth
   pulling into IntelligentUI is a small subset (the layout chrome, theme
   customizer, form/table primitives) — most of `src/app/` is demo content.
10. **Composite tsconfig** — `tsc -b` is required for type checking; a plain
    `tsc` won't traverse references correctly.

---

## 11. Stack diff vs IntelligentUI

| Concern | Minton | IntelligentUI (current) |
|---|---|---|
| UI framework | Bootstrap 5.3 + react-bootstrap | Ant Design + @ant-design/pro-components |
| Forms | react-hook-form + yup | ProForm (antd-based) |
| Tables | react-table v7 | ProTable (antd-based) |
| Charts | apexcharts + chart.js | (none specified yet) |
| State | React Context only | (project-specific; legacy used Vuex/Pinia patterns) |
| i18n | none in template | i18next-style JSON locales |
| Theming | data-attr + SCSS | antd `ConfigProvider` |
| Routing | react-router-dom v7 | react-router-dom |
| Bundler | Vite | Vite |
| TS | 5.7, composite | (assumed similar) |

The two stacks overlap on **Vite + React + TS + react-router** and diverge on
**every UI-rendering layer**.

---

## 12. Suggested reading order when reviewing plan-bootstrap-integration.md

1. **§1, §11** — stack & diff vs current IntelligentUI
2. **§5–6** — layout & theming (this is where Bootstrap and Ant Design fight)
3. **§4, §7** — routing and menu (where structure decisions live)
4. **§10** — gotchas — every one of these is a likely "what's the plan for X?" line in the integration doc
