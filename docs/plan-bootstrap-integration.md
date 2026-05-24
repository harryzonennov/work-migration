# Plan: Introduce Bootstrap Template to IntelligentUI

## Context

The legacy UI (`ThorSalesDistributionUI/admin/`) uses the **Minton Admin Dashboard Template** built on **Bootstrap 3.3.6**. It provides a polished admin layout (fixed sidebar, top navbar, content area) plus grid, utilities, and dozens of Bootstrap plugins.

The new IntelligentUI project (React + TypeScript + Ant Design) currently has:
- No CSS framework for layout (inline flexbox only)
- No Bootstrap CSS loaded — `col-md-*` strings are metadata converted to inline styles at runtime
- Ant Design for all UI components (buttons, forms, tables, modals, etc.)

**Goal**: Bring Bootstrap grid + utilities into IntelligentUI to enable proper layout classes in JSX, while keeping Ant Design for components and avoiding style conflicts.

---

## Legacy Template Reference

| Aspect | Legacy (ThorSalesDistributionUI) |
|--------|----------------------------------|
| Template | Minton Admin Dashboard (by CoderThemes) |
| Bootstrap version | 3.3.6 |
| CSS load order | `bootstrap.min.css` → `core.css` → `icons.css` → `components.css` → `pages.css` → `menu.css` |
| Layout | Fixed left sidebar (`#navigation-panel`) + top navbar (`.topbar`) + content area (`.content-page > .content`) |
| Color scheme | Primary dark `#01053e`, accent `#00b19d`, light bg `#f9fbfd`, text `#304e87` |
| Grid usage | Standard Bootstrap 3 grid: `.row` + `.col-md-*` |
| Key plugins | Select2, DataTables, Bootstrap-datepicker, Summernote, SweetAlert |

---

## Recommended Approach

### Upgrade to Bootstrap 5 (not 3)

Bootstrap 3.3.6 is EOL. Bootstrap 5 is the current stable release with:
- No jQuery dependency (fits React project)
- CSS custom properties (theming)
- Improved utilities API
- `$prefix` option for class name scoping

We will use Bootstrap 5's SCSS source to import **only grid + utilities**, prefixed with `bs-` to guarantee zero antd conflicts.

---

## Implementation Steps

### Phase 1: Install & Configure (Foundation)

#### 1.1 Install packages

```bash
cd /Users/I043125/work2/IntelligentUI
npm install bootstrap@5
npm install -D sass
```

- `bootstrap@5` — SCSS source files (no JS needed for grid/utilities)
- `sass` — Vite has native SCSS support once `sass` is installed

#### 1.2 Create scoped Bootstrap SCSS

**Create** `src/styles/bootstrap-custom.scss`:

```scss
// ═══════════════════════════════════════════════════════════════════
// Bootstrap 5 — Grid + Utilities ONLY (scoped with bs- prefix)
// 
// WHY prefixed: Ant Design uses class names like .row in its internals.
// The bs- prefix ensures zero CSS collisions.
// ═══════════════════════════════════════════════════════════════════

// 1. Prefix all generated class names
$prefix: "bs-";

// 2. Import Bootstrap foundation (no CSS output, just variables/mixins)
@import "bootstrap/scss/functions";
@import "bootstrap/scss/variables";
@import "bootstrap/scss/variables-dark";
@import "bootstrap/scss/maps";
@import "bootstrap/scss/mixins";
@import "bootstrap/scss/utilities";

// 3. Root & Reboot — SKIP entirely (antd has its own reset)
// @import "bootstrap/scss/root";       ← SKIP
// @import "bootstrap/scss/reboot";     ← SKIP

// 4. Grid system (containers, rows, columns)
@import "bootstrap/scss/containers";
@import "bootstrap/scss/grid";

// 5. Utilities API (generates all utility classes: spacing, display, flex, text, etc.)
@import "bootstrap/scss/utilities/api";
```

This produces classes like:
- Layout: `bs-container`, `bs-row`, `bs-col-md-6`, `bs-col-lg-4`
- Spacing: `bs-m-3`, `bs-p-2`, `bs-mt-4`, `bs-px-3`
- Display: `bs-d-flex`, `bs-d-none`, `bs-d-md-block`
- Flex: `bs-flex-wrap`, `bs-justify-content-between`, `bs-align-items-center`
- Text: `bs-text-center`, `bs-text-start`, `bs-fw-bold`
- Sizing: `bs-w-100`, `bs-h-auto`
- Gaps: `bs-gap-2`, `bs-row-gap-3`

#### 1.3 Create theme variables (matching legacy Minton colors)

**Create** `src/styles/theme-variables.scss`:

```scss
// ═══════════════════════════════════════════════════════════════════
// Theme Variables — aligned with legacy Minton color scheme
// ═══════════════════════════════════════════════════════════════════

$color-primary-dark: #01053e;
$color-accent: #00b19d;
$color-bg-light: #f9fbfd;
$color-bg-section: #dce6f7;
$color-text-primary: #304e87;
$color-text-secondary: #4c5667;

// Override Bootstrap variables before import (if needed)
// $primary: $color-accent;
// $body-bg: $color-bg-light;
```

These can be imported before the Bootstrap variables in `bootstrap-custom.scss` to customize the generated utilities.

#### 1.4 Import in main.tsx

**Modify** `src/main.tsx`:

```tsx
import 'antd/dist/reset.css';           // Ant Design reset (first)
import './styles/bootstrap-custom.scss'; // Bootstrap grid + utilities (second)
import './index.css';                    // Project-specific overrides (last)
```

---

### Phase 2: Layout Structure (Sidebar + Navbar + Content)

Mirror the legacy Minton layout structure in React. This is for **future use** — the current app already has basic routing. When ready, add:

#### 2.1 Layout wrapper component

**Create** `src/layouts/AdminLayout.tsx`:

```tsx
// Fixed left sidebar + top navbar + scrollable content area
// Uses bs-* grid classes for responsive layout
<div className="admin-layout">
  <aside className="admin-sidebar">
    {/* Navigation component */}
  </aside>
  <div className="admin-main">
    <header className="admin-topbar">
      {/* Top navigation */}
    </header>
    <main className="admin-content">
      <Outlet />  {/* React Router outlet */}
    </main>
  </div>
</div>
```

#### 2.2 Layout CSS

**Create** `src/styles/layout.scss`:

```scss
// Admin layout — mirrors legacy Minton fixed-left pattern
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.admin-sidebar {
  width: 240px;
  background: $color-primary-dark;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
}

.admin-main {
  margin-left: 240px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.admin-topbar {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  position: sticky;
  top: 0;
  z-index: 99;
}

.admin-content {
  padding: 24px;
  background: $color-bg-light;
  flex: 1;
}
```

---

### Phase 3: Gradual Migration of Inline Flex → Bootstrap Classes

Once Bootstrap grid classes are available, components can gradually replace inline flex styles with class names. This is **optional and incremental** — not a forced migration.

#### Before (current):
```tsx
<div style={{ display: 'flex', flexWrap: 'wrap', gap: '0 16px' }}>
  <div style={{ flex: '0 0 calc(50% - 8px)', minWidth: 0 }}>
    <Field />
  </div>
</div>
```

#### After (with Bootstrap):
```tsx
<div className="bs-row bs-g-3">
  <div className="bs-col-md-6">
    <Field />
  </div>
</div>
```

#### Migration priority:
1. `AsyncField.tsx` — replace `flexBasis` IIFE with `className={colClass replaced with bs- prefix}`
2. `EditPageShell.tsx` — section row wrappers
3. `AsyncSection.tsx` — section-level column sizing
4. Individual page components

---

### Phase 4: Optional — Add Legacy Plugin Equivalents

The legacy Minton template includes plugins that have React/antd equivalents:

| Legacy Plugin | React Equivalent | Status |
|---------------|-----------------|--------|
| Select2 | antd `Select` / `ProFormSelect` | Already migrated |
| DataTables | antd `Table` / `ProTable` | Already migrated |
| Bootstrap-datepicker | antd `DatePicker` | Already migrated |
| SweetAlert | antd `Modal.confirm` / `message` | Already migrated |
| Summernote (WYSIWYG) | React-Quill or antd mentions | Future |
| Bootstrap-treeview | antd `Tree` | Future |

No action needed here — just for reference.

---

## Files Summary

| File | Action | Purpose |
|------|--------|---------|
| `package.json` | Modify | Add `bootstrap@5` + `sass` |
| `src/styles/bootstrap-custom.scss` | Create | Scoped BS5 grid + utilities import |
| `src/styles/theme-variables.scss` | Create | Legacy Minton color scheme as SCSS vars |
| `src/styles/layout.scss` | Create | Admin layout structure (sidebar/navbar/content) |
| `src/main.tsx` | Modify | Import the new SCSS after antd reset |
| `src/layouts/AdminLayout.tsx` | Create (Phase 2) | Layout wrapper component |

---

## What NOT to Change

- **Ant Design components** — keep using antd for buttons, forms, tables, modals, etc.
- **Existing inline styles** — keep working; migrate gradually if desired
- **`colClassToFlexStyle()` utility** — remains available as fallback for components not yet migrated to BS classes
- **No Bootstrap JS** — not needed (React handles interactivity; antd handles UI components)

---

## Verification

1. `npm run dev` — app builds and starts without errors
2. Browser DevTools: verify `.bs-col-md-6` has `flex: 0 0 auto; width: 50%` from Bootstrap grid
3. Confirm antd components (Modal, Select, Table, Button) are visually unchanged
4. Test: add `<div className="bs-row"><div className="bs-col-md-6">A</div><div className="bs-col-md-6">B</div></div>` in any page and confirm 2-column layout
5. Test spacing utilities: `<div className="bs-p-3 bs-mb-2">` has correct padding/margin

---

## Risk Assessment

| Risk | Mitigation |
|------|-----------|
| antd CSS conflict | `$prefix: "bs-"` ensures no class name overlap |
| Bundle size increase | Grid + utilities only ≈ 25-40KB CSS (gzipped ~5KB) |
| Bootstrap Reboot conflict | Explicitly skipped — only grid + utilities imported |
| Maintenance burden | Bootstrap 5 is actively maintained; SCSS imports are explicit |
| Developer confusion (two systems) | Document that `bs-*` = layout/spacing, antd = components |