# Minton — Icon Library Reference

> Reference for finding and using icons in IntelligentUI. Seven sources are
> available: six icon fonts shipped via Minton's SCSS bundle (already loaded
> in `npm run dev` — no extra install required) + antd's icon library. This
> doc lists each, when to prefer it, and where to browse the catalogue.

---

## Quick decision: which icon source?

```
┌──────────────────────────────────────────────────────────────────────┐
│  Are you matching a legacy UI icon (`<i class="fa fa-…">`,           │
│  `<i class="md md-…">`)?                                              │
│                                                                       │
│   YES → use the SAME icon font as legacy (FontAwesome / MDI / etc.)  │
│   NO  → keep reading…                                                 │
│                                                                       │
├──────────────────────────────────────────────────────────────────────┤
│  Is the icon part of an antd component (Button icon, Form prefix,    │
│  Table action) or a React state-driven element?                      │
│                                                                       │
│   YES → use @ant-design/icons (React components)                     │
│   NO  → keep reading…                                                 │
│                                                                       │
├──────────────────────────────────────────────────────────────────────┤
│  Static decorative icon in your own TSX (sidebar, card header,       │
│  footer)?                                                             │
│                                                                       │
│   Prefer:  Material Design Icons (MDI) — largest set, ~7000 icons    │
│   Or:      Feather — minimal stroke style, ~290 icons (already       │
│            used for the topbar hamburger: <i className="fe-menu">)   │
└──────────────────────────────────────────────────────────────────────┘
```

---

## The seven sources

| # | Source | Type | Class / Component | Catalogue URL |
|---|---|---|---|---|
| 1 | **@ant-design/icons** | React components | `<SettingOutlined />` | https://ant.design/components/icon |
| 2 | **Material Design Icons (MDI)** | Icon font | `<i className="mdi mdi-NAME" />` | https://pictogrammers.com/library/mdi/ |
| 3 | **BoxIcons** | Icon font | `<i className="bx bx-NAME" />` or `<i className="bx bxs-NAME" />` (solid) | https://boxicons.com/ |
| 4 | **Feather** | Icon font | `<i className="fe-NAME" />` | https://feathericons.com/ |
| 5 | **Remix Icon** | Icon font | `<i className="ri-NAME" />` | https://remixicon.com/ |
| 6 | **FontAwesome 5 (free)** | Icon font | `<i className="fa fa-NAME" />` | https://fontawesome.com/v5/search?o=r&m=free |
| 7 | **Weather Icons** | Icon font | `<i className="wi wi-NAME" />` | https://erikflowers.github.io/weather-icons/ |

All 6 icon fonts (#2-7) are loaded globally via `src/assets/scss/icons.scss`
(imported in `main.tsx` during Phase 1). No per-page import is needed.

Total icon font weight in your bundle: ~875 KB raw CSS includes the
`@font-face` declarations + all 6 font binary URLs. Already in production
bundle since Phase 1 — **adding new icons from these libraries costs zero**
KB.

---

## When to use each

### 1. `@ant-design/icons` (React components)

**Use for:** anything inside an antd component, anywhere React state controls
the icon, when you need a different icon on focus/hover/active and don't
want to swap class names manually.

```tsx
import { SettingOutlined, SaveOutlined, DeleteOutlined } from '@ant-design/icons';

<Button icon={<SaveOutlined />}>Save</Button>
<SettingOutlined style={{ fontSize: 18 }} />
```

**Already used in IntelligentUI:**
- `MainLayout.tsx` — `UserOutlined` (avatar), `SettingOutlined` (theme customizer trigger)
- `ListPageShell.tsx` — `PlusOutlined`, `DeleteOutlined`, `DownloadOutlined`, `ReloadOutlined`, `EditOutlined` (toolbar buttons)
- `SearchPanel.tsx` — `SearchOutlined`, `ReloadOutlined` (search action buttons)
- Many `Async*Section.tsx` files

**Three style variants per icon** (suffix on the component name):
- `Outlined` — `<SettingOutlined />` — line/stroke style (default Minton vibe)
- `Filled` — `<SettingFilled />` — solid
- `TwoTone` — `<SettingTwoTone />` — two-color

Browse: https://ant.design/components/icon — search box on the page filters live.

### 2. Material Design Icons (MDI) — largest set

**Use for:** generic decorative icons, sidebar nav, status indicators. ~7000
icons covering almost any concept.

```tsx
<i className="mdi mdi-content-paste" />        // basic
<i className="mdi mdi-content-paste mdi-24px" />  // size modifier
<i className="mdi mdi-spin mdi-loading" />     // animation modifier
```

**Already used in IntelligentUI:**
- Legacy section headers use `<i className="md md-content-paste content-portlet-title" />` — note: `md` (legacy short prefix), not `mdi`. The legacy short `md` prefix is provided by a separate Materialize Icons font (different from MDI).
- Check `AsyncEditSection.tsx`'s `comTitleIcon` default for examples.

**Modifier classes** (combine with `mdi-NAME`):
- Sizes: `mdi-18px` / `mdi-24px` / `mdi-36px` / `mdi-48px`
- Rotate: `mdi-rotate-45` / `mdi-rotate-90` / `mdi-rotate-180` / `mdi-rotate-270`
- Flip: `mdi-flip-h` / `mdi-flip-v`
- Spin: `mdi-spin`

Browse: https://pictogrammers.com/library/mdi/ — instant search, click an icon to copy its name.

### 3. BoxIcons

**Use for:** when you want a more illustrated/friendly look than MDI's
flat-minimal style. ~1500 icons in 3 variants:
- Regular (line): `bx bx-NAME`
- Solid: `bx bxs-NAME`
- Logos: `bx bxl-NAME` (Twitter, GitHub, etc.)

```tsx
<i className="bx bx-home" />
<i className="bx bxs-bell" />            // solid bell
<i className="bx bxl-github" />          // GitHub logo
```

Browse: https://boxicons.com/ — search + filter by variant.

### 4. Feather — already in use for the hamburger

**Use for:** clean stroke style. ~290 icons. Smaller catalogue but very
consistent visual weight. Already loaded; we use `fe-menu` for the
hamburger in `MainLayout.tsx` after Phase 3 rounds.

```tsx
<i className="fe-menu" />
<i className="fe-search" />
<i className="fe-user" />
```

Note: Feather classes are **un-prefixed** — just `fe-NAME`, not `fe fe-NAME`.

Browse: https://feathericons.com/

### 5. Remix Icon

**Use for:** large modern set, open-source. ~2500 icons. Outlined + Filled
variants.

```tsx
<i className="ri-home-line" />            // outlined
<i className="ri-home-fill" />            // filled
```

Browse: https://remixicon.com/

### 6. FontAwesome 5 (free tier)

**Use for:** when matching legacy UI that already uses FA. Already in use in
IntelligentUI's legacy-styled process buttons:

```tsx
<i className="fa fa-sign-out content-peach-red" />
<i className="fa fa-rotate-left content-green" />
<i className="fa fa-flag-checkered content-green" />
<i className="fa fa-save content-green" />
```

**Class prefix variants:**
- `fa fa-NAME` — solid (default in FA 5 free)
- `far fa-NAME` — regular (FA 5 has limited free regular icons)
- `fab fa-NAME` — brands

Browse: https://fontawesome.com/v5/search?o=r&m=free (filter to FA 5 free).

### 7. Weather Icons

**Use for:** weather-specific (sun, clouds, rain, snow, temperature). ~220
icons. Rarely needed for an admin tool — present in Minton's bundle for
completeness.

```tsx
<i className="wi wi-day-sunny" />
<i className="wi wi-rain" />
```

Browse: https://erikflowers.github.io/weather-icons/

---

## Common recipes

### Recipe 1 — Add an icon to a sidebar nav item

`src/router/menuConfig.ts` already uses antd icon components:

```tsx
import { ShoppingCartOutlined, FileTextOutlined } from '@ant-design/icons';

{
  key: 'procurement',
  label: t('procurement'),
  icon: React.createElement(ShoppingCartOutlined),
  // …
}
```

To use a Minton font icon (e.g. MDI) instead:

```tsx
{
  key: 'procurement',
  label: t('procurement'),
  icon: React.createElement('i', { className: 'mdi mdi-cart' }),
  // …
}
```

The `SidebarMenu` component in `MainLayout.tsx` renders `<span className="side-nav-icon">{item.icon}</span>` regardless of icon source.

### Recipe 2 — Color an icon

```tsx
{/* antd icon */}
<SettingOutlined style={{ color: 'var(--bs-primary)', fontSize: 18 }} />

{/* font icon */}
<i className="mdi mdi-bell" style={{ color: 'var(--bs-warning)', fontSize: 20 }} />
```

Always use `var(--bs-*)` for color so dark mode tracks automatically. Hex colors lock you out of dark mode.

### Recipe 3 — Match a legacy UI icon

Step 1: find the legacy class string. Legacy IntelligentUI uses lots of FontAwesome:

```html
<i class="fa fa-save content-green"></i>
```

Step 2: use the same class verbatim. Minton ships the same FontAwesome 5 free font, so this just works:

```tsx
<i className="fa fa-save content-green" />
```

If a legacy class uses `md md-NAME` (Materialize legacy short prefix, not MDI), the equivalent is usually `mdi mdi-NAME` from MDI — search MDI by the icon's name.

**Complete substitution table** (verified 2026-07-01 during the icon-migration
sweep — Minton does NOT ship Ionicons v1, Materialize `md`, or the custom
`nmd` font, so every legacy class using those prefixes renders as an
invisible/broken icon and needs substitution):

| Legacy class (broken in Minton bundle) | Minton equivalent (`mdi mdi-*`) |
|---|---|
| `ion-refresh` | `mdi mdi-refresh` |
| `ion-close-round` | `mdi mdi-close` |
| `ion-minus-round` | `mdi mdi-minus` |
| `ion-plus-round` | `mdi mdi-plus` |
| `ion-list` | `mdi mdi-format-list-bulleted` |
| `ion-arrow-down-b` | `mdi mdi-chevron-down` |
| `ion-arrow-return-left` | `mdi mdi-keyboard-return` |
| `md md-chat` | `mdi mdi-chat` |
| `md md-close` | `mdi mdi-close` |
| `md md-create` | `mdi mdi-pencil` |
| `md md-history` | `mdi mdi-history` |
| `md md-perm-contact-cal` | `mdi mdi-account-box` |
| `md md-content-paste` | `mdi mdi-content-paste` |
| `md md-done-all` | `mdi mdi-checkbox-multiple-marked-outline` |
| `md md-add` | `mdi mdi-plus` |
| `md md-bookmark-outline` | `mdi mdi-bookmark-outline` |
| `md md-info-outline` | `mdi mdi-information-outline` |
| `md md-restore` | `mdi mdi-restore` |
| `md md-security` | `mdi mdi-security` |
| `md md-spellcheck` | `mdi mdi-spellcheck` |
| `md md-remove-circle-outline` | `mdi mdi-minus-circle-outline` |
| `nmd nmd-add-shopping-cart` | `mdi mdi-cart-plus` |
| `nmd nmd-find-replace` | `mdi mdi-find-replace` |
| `nmd nmd-chat-bubble-outline` | `mdi mdi-chat-outline` |
| `nmd nmd-play-circle-outline` | `mdi mdi-play-circle-outline` |
| `nmd nmd-format-color-text` | `mdi mdi-format-color-text` |
| `nmd nmd-portrait` | `mdi mdi-account` |
| `nmd nmd-shopping-cart` | `mdi mdi-cart` |
| `nmd nmd-playlist-add-check` | `mdi mdi-playlist-check` |

**Which legacy classes DO work in Minton unchanged:**

| Class prefix | Font | Ships in Minton? |
|---|---|---|
| `fa fa-*` / `far fa-*` / `fab fa-*` | FontAwesome 5 free | ✅ Yes — Minton ships it |
| `fe-*` | Feather | ✅ Yes — used unchanged for the topbar hamburger |
| `mdi mdi-*` | Material Design Icons | ✅ Yes — biggest set, 7000 icons |
| `bx bx-*` / `bx bxs-*` / `bx bxl-*` | BoxIcons | ✅ Yes |
| `ri-*` | Remix Icon | ✅ Yes |
| `wi wi-*` | Weather Icons | ✅ Yes |
| `ion-*` | Ionicons v1 | ❌ NO — needs substitution |
| `md md-*` | Materialize Icons (legacy short prefix, NOT MDI) | ❌ NO — needs substitution |
| `nmd nmd-*` | Legacy custom nmd font | ❌ NO — needs substitution |

**How to find broken icons in your code:** run this grep from the
IntelligentUI root:

```bash
grep -rEn '(className|class)=["\`][^"\`]*\b(ion-|md md-|nmd nmd-)[a-z][a-z0-9-]*[^"\`]*["\`]' src/ --include="*.tsx" --include="*.ts"
```

Any hit in a `.tsx` file that ISN'T a JS `//` comment is a broken icon —
substitute per the table above.

Legacy `.js` files under `src/components/**/legacy/` or files not imported
by any `.tsx` are dead code — their icon references are harmless because
they never render.

### Recipe 4 — Spinning loader icon

**antd:**
```tsx
import { LoadingOutlined } from '@ant-design/icons';
<LoadingOutlined spin style={{ fontSize: 24 }} />
```

**MDI:**
```tsx
<i className="mdi mdi-loading mdi-spin mdi-24px" />
```

---

## How to browse from inside IntelligentUI (optional)

If you'd like an in-app icon browser later, Minton ships 6 icon demo pages
already as React TSX files under:

```
template/Minton_v10.1.0/React.js/TS/src/app/(admin)/icons/
  ├── boxicons/page.tsx
  ├── feather/page.tsx
  ├── font-awesome/page.tsx
  ├── mdi/page.tsx
  ├── remix/page.tsx
  └── weather/page.tsx
```

These could be copied into `src/pages/dev/icons/*` and wired into
`router/index.tsx` under `/dev/icons/{boxicons,feather,…}` routes. Out of
scope for now — see official catalogues (URLs above) instead.

---

## What NOT to do

- **Don't pull a third-party icon library** (`react-icons`, `lucide-react`,
  etc.) when one of the 7 sources above already covers your need. Adding
  another library bloats the bundle without visual benefit.
- **Don't hex-color an icon** — use `var(--bs-*)` CSS custom properties so
  dark mode keeps working.
- **Don't mix icon styles in a row** — pick one stroke weight per UI region
  (e.g. all topbar icons Outlined antd; all sidebar icons MDI). Mixed
  weights look uneven.
- **Don't reach for FontAwesome's paid tier** — the free tier shipped with
  Minton covers most needs. If you genuinely need a Pro icon, use MDI or
  BoxIcons first.

---

## See also

- [`MINTON_CUSTOMIZATION_GUIDE.md`](./MINTON_CUSTOMIZATION_GUIDE.md) — change colors, fonts, spacing
- [`MINTON_TEMPLATE_INDEX.md`](./MINTON_TEMPLATE_INDEX.md) — Minton template structure
- [`plan-bootstrap-integration.md`](./plan-bootstrap-integration.md) — integration plan
