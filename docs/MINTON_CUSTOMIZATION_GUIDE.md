# Minton Theme — Customization Quickstart

> Reference for changing colors, fonts, spacing, and other visual tokens in
> IntelligentUI's Minton-based theme. Companion to
> [`plan-bootstrap-integration.md`](./plan-bootstrap-integration.md) and
> [`MINTON_TEMPLATE_INDEX.md`](./MINTON_TEMPLATE_INDEX.md).

---

## TL;DR — decision tree

You want to change something visual. Ask yourself:

```
┌──────────────────────────────────────────────────────────────────────┐
│  Is it a color, font, or numeric token used in many places?          │
│                                                                       │
│   YES → tier 1 (_variables.scss / _variables-custom.scss)            │
│   NO  → keep reading…                                                 │
│                                                                       │
├──────────────────────────────────────────────────────────────────────┤
│  Does it need different values in light vs. dark mode?               │
│                                                                       │
│   YES → tier 2 (_theme-config.scss — CSS custom properties)          │
│   NO  → keep reading…                                                 │
│                                                                       │
├──────────────────────────────────────────────────────────────────────┤
│  Is it a one-off rule for specific selectors?                        │
│                                                                       │
│   YES → tier 3 (overrides.scss)                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## The three tiers

| Tier | File | What it controls |
|---|---|---|
| **1** | `src/assets/scss/config/default/_variables.scss` | Bootstrap 5 SCSS variables (colors, fonts, spacing, breakpoints) — what every Bootstrap/Minton component reads at compile time |
| **1** | `src/assets/scss/config/default/_variables-custom.scss` | Minton-specific SCSS variables (sidebar width, topbar height, menu sizes) |
| **2** | `src/assets/scss/config/default/_theme-config.scss` | CSS custom properties — the runtime variant switches (`data-bs-theme="dark"`, `data-menu-color="dark/brand"`, `data-topbar-color="dark"`) |
| **3** | `src/styles/overrides.scss` | One-off selector overrides — not token-driven |

After any change in tiers 1-2, **Vite hot-reloads** in `npm run dev`; for production you need `npx vite build`.

---

## Tier 1 — Bootstrap & Minton SCSS variables

### Color tokens (`_variables.scss`, lines 12-99)

**Gray scale:**

```scss
$white:    #fff;
$gray-100: #f1f5f7;   // subtle bg
$gray-200: #f7f7f7;   // card backgrounds (light mode)
$gray-300: #dee2e6;   // borders
$gray-400: #ced4da;   // disabled
$gray-500: #adb5bd;
$gray-600: #98a6ad;   // text-muted
$gray-700: #6c757d;   // body-color
$gray-800: #343a40;
$gray-900: #323a46;   // headings
$black:    #000;
```

**Brand palette:**

```scss
$blue:    #3bafda;    // (also $primary)
$indigo:  #675aa9;
$purple:  #6559cc;
$pink:    #f672a7;
$red:     #f1556c;
$orange:  #fd7e14;
$yellow:  #f7b84b;
$green:   #1abc9c;
$teal:    #02a8b5;
$cyan:    #37cde6;
```

**Semantic tokens** (derive from the brand palette):

```scss
$primary:   $blue;     // buttons, links, focus rings, active states
$secondary: $gray-700;
$success:   $green;    // Dashboard's "Active Contracts" stat color
$info:      $cyan;
$warning:   $yellow;   // Dashboard's "Expiring Soon" stat color
$danger:    $red;      // Dashboard's "Pending Approval" stat color
$light:     $gray-100;
$dark:      $gray-900;
```

**To change the brand color globally:** edit `$primary` → all derived states (hover via `darken($primary, 7.5%)`, active via `darken($primary, 10%)`, focus ring via `rgba($primary, 0.5)`, etc.) recompute automatically.

### Layout dimensions (`_variables-custom.scss`)

```scss
$leftbar-width:              240px;   // sidebar default
$leftbar-width-sm:           160px;   // sidebar compact mode
$leftbar-width-condensed:    70px;    // sidebar icon-only mode
$topbar-height:              70px;    // topbar height
$boxed-layout-width:         1300px;  // when data-layout-width="boxed"
$menu-item-size:             0.95rem; // sidebar top-level font
$menu-sub-item-size:         0.9rem;  // sidebar sub-level font
```

Changing `$leftbar-width` here: also update `src/styles/overrides.scss`'s
Issue 4 footer rule (`.footer { left: 240px }`) to match — that one's hard-coded because Sass `$leftbar-width` doesn't reach CSS at runtime.

### Font

```scss
// _variables-custom.scss
@import url('https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap');
$font-family-secondary: 'Roboto', sans-serif;
```

To change the base font, replace the `@import` URL and update
`$font-family-base` in `_variables.scss` (search for `$font-family-base`).

---

## Tier 2 — CSS custom properties (`_theme-config.scss`)

This file defines runtime-switchable values keyed off `<html>` attributes. The
structure:

```scss
:root, [data-bs-theme="light"] { … light defaults … }

html[data-bs-theme="dark"] { … dark overrides … }

// menu color variants (controlled by data-menu-color attribute)
html[data-menu-color="light"]  { … }
html[data-menu-color="dark"]   { … }   ← default
html[data-menu-color="brand"]  { … }

// topbar color variants
html[data-topbar-color="light"] { … }   ← default
html[data-topbar-color="dark"]  { … }
```

### The most useful CSS custom properties

These map to Minton's `data-*` configuration knobs from `useMintonTheme.ts`:

| CSS variable | What it controls | Light / Dark default |
|---|---|---|
| `--bs-body-bg` | App background behind content | `#fff` / `#36404a` |
| `--bs-body-color` | Default body text | `#6c757d` / `#dee2e6` |
| `--bs-card-bg` | Card backgrounds | `#fff` / `#3d4752` |
| `--bs-border-color` | Default borders | `#dee2e6` / `#5d7186` |
| `--bs-menu-bg` | Sidebar background | `#fff` (light) / `#39444e` (dark) / `#3bafda` (brand) |
| `--bs-menu-item-color` | Sidebar item text | `#6c757d` (light) / `#9097a7` (dark) |
| `--bs-menu-item-hover` | Sidebar item hover | `#3bafda` (light) / `#fff` (dark) |
| `--bs-menu-item-active` | Sidebar active item text | `#3bafda` / `#3bafda` |
| `--bs-topbar-bg` | Topbar background | `#fff` / `#3d4752` |
| `--bs-topbar-item-color` | Topbar item text | `#6c757d` / `rgba(255,255,255,.6)` |
| `--bs-topbar-item-hover-color` | Topbar item hover | `#3bafda` / `#bccee4` |
| `--bs-primary` | Brand color | `#3bafda` |
| `--bs-success` / `--bs-danger` / `--bs-warning` / `--bs-info` | Semantic colors | … |
| `--bs-gray-100` … `--bs-gray-900` | Gray scale | (see Tier 1) |

### How to add a custom value for dark mode

Want the sidebar to use a different green only in dark mode? Edit `_theme-config.scss`:

```scss
html[data-bs-theme="dark"] {
  --bs-menu-item-active: #2ecc71;  // your custom dark-mode active color
}
```

The change applies the moment `<html data-bs-theme="dark">` is set (via the ThemeCustomizer or DevTools). No JavaScript change needed.

---

## Tier 3 — `overrides.scss` (selector-targeted fixes)

For things that aren't tokens. Already in active use — current contents:

| Block | What it fixes |
|---|---|
| Issue 1 | Sidebar `.side-nav-second-level` / `.side-nav-third-level` padding & font |
| Issue 2 | `.page-title-box` margin/padding/shadow when used without a `.page-title` |
| Issue 3 | `.navbar-custom .topnav-menu` flex alignment (Minton×antd collision) |
| Issue 4 | `.footer` position fixed + sidebar-width-aware `left` |
| Issue 6 | `[id^="x-page-"] > .ant-space:first-child` toolbar gap |

The convention is documented at the top of `overrides.scss`. Add new blocks with a numbered "Issue N" header explaining root cause + fix. Use CSS custom properties (`var(--bs-…)`) wherever a color is involved so dark mode keeps working.

---

## Project tokens (IntelligentUI-specific)

In addition to Bootstrap & Minton's tokens above, IntelligentUI adds its own
project-specific tokens in `_variables-custom.scss`. These follow the same
three-tier pattern: Sass variable → CSS custom property in `_theme-config.scss`
→ consumed by `overrides.scss` rules.

| Sass variable | CSS custom property | Value (light) | Value (dark) | Used by |
|---|---|---|---|---|
| `$brand-dark` | `--bs-brand-dark` | `#01053e` | (undefined) | `overrides.scss` Issue 7 — form labels and section card titles |

**Pattern for adding a new project token:**

1. Add `$my-token: #value;` to `_variables-custom.scss`
2. Add `--#{$prefix}my-token: #{$my-token};` to the appropriate `_theme-config.scss` block (either `:root, [data-bs-theme="light"]` for light-only, or both blocks for both themes)
3. Reference `var(--bs-my-token)` in `overrides.scss` rules

The token's value becomes the single source of truth — changing it once propagates everywhere it's referenced.

---

## Common recipes

### Recipe 1 — Change the brand color (primary) globally

**Edit:** `src/assets/scss/config/default/_variables.scss`

```scss
// Line ~42 — change $blue (the source for $primary):
$blue: #5b73e8;        // your new brand color

// OR change $primary directly (line ~73):
$primary: #5b73e8;
```

**Effect:** every button, link, focus ring, active sidebar item, antd `colorPrimary` (via the Phase 3a bridge), Dashboard `var(--bs-primary)` reference flips. Includes light AND dark mode because both modes derive from the same `$primary`.

### Recipe 2 — Change the body background color

**Edit:** `src/assets/scss/config/default/_theme-config.scss`

```scss
:root, [data-bs-theme="light"] {
  --bs-body-bg: #f5f7fa;     // your new light-mode background
}

html[data-bs-theme="dark"] {
  --bs-body-bg: #1a2231;     // your new dark-mode background
}
```

Both light and dark mode get their own value. No rebuild loop — `npm run dev` hot-reloads.

### Recipe 3 — Change the body text color

Same file, same idea:

```scss
:root, [data-bs-theme="light"] {
  --bs-body-color: #4a4a6a;
}

html[data-bs-theme="dark"] {
  --bs-body-color: #d8e0e8;
}
```

### Recipe 4 — Change the sidebar background

**Edit:** `_theme-config.scss`, the `html[data-menu-color="dark"]` block (since `dark` is the default menu color):

```scss
html[data-menu-color="dark"] {
  --bs-menu-bg: #1e2733;       // your new sidebar background
  --bs-menu-item-color: #aab8c5;
  --bs-menu-item-hover: #ffffff;
}
```

If you want the change to apply to ALL menu-color variants, edit each of the three blocks (`light`, `dark`, `brand`).

### Recipe 5 — Change the font

**Edit:** `_variables-custom.scss`

```scss
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;700&display=swap');
$font-family-secondary: 'Inter', sans-serif;
```

**Also edit:** `_variables.scss` (find `$font-family-base`):

```scss
$font-family-base: 'Inter', system-ui, sans-serif;
```

### Recipe 6 — Make the sidebar narrower

**Edit:** `_variables-custom.scss`

```scss
$leftbar-width: 220px;     // was 240px
```

**Also edit:** `src/styles/overrides.scss` Issue 4 footer rule (line `.footer { left: 240px }` → `220px`). The footer's `left` is hard-coded because Sass `$leftbar-width` isn't available as a CSS variable at runtime.

### Recipe 7 — Change the card background or border

```scss
// _theme-config.scss
:root, [data-bs-theme="light"] {
  --bs-card-bg: #fafbfd;          // new card background
  --bs-theme-card-border-color: #dde3e8;
}
```

`--bs-card-bg` is used by antd Cards too (via the Phase 3a bridge, antd's `colorBgContainer` resolves from this) — one edit, two libraries.

### Recipe 8 — Change a specific selector that isn't a token

When tier 1 or 2 don't fit, **add to `overrides.scss`**:

```scss
// Issue 7 — Make the sidebar logo larger
.left-side-menu .logo-box .logo-lg span {
  font-size: 20px;
  letter-spacing: 1px;
}
```

Add a numbered "Issue N" header per the existing convention. Use `var(--bs-*)` for any color.

---

## How to know which token a UI element uses

Two ways:

### A — search the SCSS source

```bash
cd /Users/I043125/work2/IntelligentUI/src/assets/scss
grep -rn "menu-bg\|menu-item-color" custom/
```

Tells you which CSS rule reads the variable and what selectors it applies to.

### B — DevTools inspection

1. Open the page in `npm run dev`
2. Right-click the element → "Inspect"
3. In the Styles panel, find the rule that styles it
4. Hover over CSS variables like `var(--bs-menu-item-color)` — DevTools shows the resolved value and where it's defined

The "Computed" tab shows the final value with the full cascade.

---

## What NOT to do

- **Don't edit anything under `src/assets/scss/custom/`** — those are Minton vendor SCSS files. Treat them as read-only. If you need to override their behavior, put the override in `overrides.scss` or change the token they consume in `_variables*.scss`.
- **Don't add hex colors directly to `overrides.scss`** — use `var(--bs-*)` so dark mode keeps working. Hex breaks dark-mode coherence.
- **Don't sprinkle inline `style={{...}}` in TSX for colors/fonts** — those don't participate in dark-mode or theming. Always go through a CSS variable.
- **Don't change Minton's `_theme-config.scss` structure** — adding new tokens is fine, but keep the `:root, [data-bs-theme="light"]` / `html[data-bs-theme="dark"]` block structure intact. The ThemeCustomizer's `data-*` protocol depends on it.

---

## When you've made a change

After editing any tier 1/2 file:

```bash
cd /Users/I043125/work2/IntelligentUI
npm run dev    # hot-reloads — no manual rebuild needed
```

For production:

```bash
npx vite build
```

Bundle delta from a typical token change: ~0 bytes (the rules already exist in the bundle, only their values change).

---

## See also

- [`plan-bootstrap-integration.md`](./plan-bootstrap-integration.md) — full integration plan
- [`MINTON_TEMPLATE_INDEX.md`](./MINTON_TEMPLATE_INDEX.md) — Minton template structure reference
- [`MINTON_ICON_REFERENCE.md`](./MINTON_ICON_REFERENCE.md) — icon library guide (BoxIcons, Feather, MDI, FontAwesome, Remix, Weather, antd icons)
