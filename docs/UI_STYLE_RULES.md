# New UI Style Rules

A living reference for style decisions made during the Vue 2 → React migration.
Each entry records the **problem**, the **legacy pattern**, the **correct new UI approach**, and the **rule** to follow going forward.

---

## Rule 1 — Button icon colour: let it inherit, never override

### Problem
A green button (`backgroundColor: '#00b19d'`, `color: '#fff'`) was given a **separate red colour override** on its icon:

```tsx
// WRONG
<Button style={{ backgroundColor: '#00b19d', color: '#fff' }}
  icon={<DeleteOutlined style={{ color: '#c81827' }} />}
/>
```

This produced a red trash icon on a green button — visually inconsistent and semantically misleading (red suggests destructive delete, not a clear/reset action).

### Legacy pattern
The legacy CSS used two classes on the button element:
- `btn-green` → `background-color: #00b19d; color: #fff`
- `content-red` → `color: #c81827` applied to the **`<i>` icon element** inside the button

The `content-red` class on the icon was a legacy artefact — the icon was intended to be white (matching the button text) but got `content-red` by mistake. In the new Ant Design stack the icon renders as an SVG with `fill="currentColor"` — it always inherits `color` from the parent button.

### Correct approach
Do **not** set a separate colour on the icon. Let it inherit the button's `color`:

```tsx
// CORRECT — icon inherits #fff from the button
<Button style={{ backgroundColor: '#00b19d', borderColor: '#00b19d', color: '#fff' }}
  icon={<DeleteOutlined />}
/>
```

### Rule
> **Never add a `style` prop to an Ant Design icon inside a Button unless the icon must visually differ from the button's own text colour.** For action buttons where the icon IS the button content, always let `color` inherit.

---

## Rule 2 — Green action button colour: use `btn-green` (`#00b19d`), not `btn-success-reverse` (`#007e70`)

### Problem
The search section reset button was initially styled with `#007e70` (the darker teal from `btn-success-reverse`). This does not match the standard green used throughout the legacy UI for action buttons and icons.

### Legacy CSS values
```css
/* Standard green — use this for action buttons */
.btn-green {
  border: 1px solid #00b19d !important;
  background-color: #00b19d !important;
  color: #fff !important;
}

/* content-green — same hue, used for icons and text */
.content-green {
  color: rgb(0, 177, 157);   /* = #00b19d */
}

/* btn-success-reverse — darker teal, NOT the standard green */
.btn-success-reverse {
  background-color: #007e70;
  color: #eff4f9;
}
```

`btn-green` and `content-green` share the same green (`#00b19d` / `rgb(0, 177, 157)`).
`btn-success-reverse` is a **darker teal** (`#007e70`) used only for inverted-style secondary actions.

### Correct approach
Use `#00b19d` with `className="btn-rounded-embed-search embed-secHeaderLeft embed-secHeader"` for the leftmost embedded header button:

```tsx
// CORRECT — matches btn-green (#00b19d) + btn-rounded-embed-search sizing + left spacing
<Button
  size="small"
  className="btn-rounded-embed-search embed-secHeaderLeft embed-secHeader"
  style={{
    backgroundColor: '#00b19d',
    borderColor: '#00b19d',
    color: '#fff',
  }}
  icon={<SomeIcon />}
/>
```

The CSS classes (defined in `src/styles/overrides.scss`):
- `.btn-rounded-embed-search` — `border-radius: 5px; padding: 1px 10px; margin: 4px 0; min-width: 40px`
- `.embed-secHeaderLeft.embed-secHeader` — `margin-left: 1rem` (compound selector, specificity 0,2,0) — adds left spacing only when **both** classes are present, separating the button from the section title. Use `embed-secHeaderLeft` only on the **leftmost** embedded button in a section header; omit it for subsequent buttons if more than one is present.

For hover inversion, add a CSS class — inline styles cannot express `:hover`.

### When to use
Compact, non-destructive utility buttons embedded in section headers (e.g. clear-search). **Not** for primary CTA buttons (use `type="primary"`) or destructive delete actions (use `danger`).

---

## Rule 3 — Search section title: always show `advancedSearchCondition`

### Problem
The search card had no title — the card header showed only the reset button with no label.

### Legacy pattern
Every legacy `AsyncSearchSection` resolves its title via `sectionMeta.titleLabelKey = 'advancedSearchCondition'` → `labelObject` lookup:
- **zh**: `高级搜索条件`
- **en**: `Advanced Search`

### Correct approach
In `AsyncSearchSection`, always resolve a title — prefer `sectionMeta.titleLabelKey` → `labelObject` lookup, fall back to `i18n.t('commonElements:advancedSearchCondition')`:

```tsx
const searchTitleLabel = (() => {
  const key = sectionMeta?.titleLabelKey as string | undefined;
  if (key && labelObject?.[key]) return labelObject[key] as string;
  return i18n.t('commonElements:advancedSearchCondition');
})();
```

### Rule
> **The search section card must always display `advancedSearchCondition` as its title.** Individual list controllers do not need to set `titleLabelKey` — `AsyncSearchSection` provides the default automatically.

---

## Rule 4 — Do not nest `PortletHeadEle` inside a `<Space>` when adding sibling elements

### Problem
`PortletHeadEle` renders its own `<Space>` wrapper internally. Placing it inside another `<Space>` creates **double-wrapped flex items**, which breaks vertical alignment between the title and any sibling buttons.

```tsx
// WRONG — PortletHeadEle's inner Space is wrapped by an outer Space
// → title and button are not vertically centred relative to each other
<Space size={8}>
  <PortletHeadEle titleLabel="..." titleIcon="..." />  {/* renders <Space> internally */}
  {resetButton}
</Space>
```

### Correct approach
Flatten the title and button into **direct siblings** in a single `<Space align="center">`. Render the icon and label inline — skip `PortletHeadEle`:

```tsx
// CORRECT — all items are direct Space children, vertically centred
<Space size={8} align="center">
  <i className="mdi mdi-magnify-scan content-portlet-title" />
  <span>{searchTitleLabel}</span>
  {resetButton}
</Space>
```

### Rule
> **When building a card `title` node that mixes a section heading with action buttons, render all elements as direct `<Space align="center">` children.** Never nest a component that returns `<Space>` inside another `<Space>` — it creates alignment bugs that cannot be fixed with CSS alone.

---

## Rule 5 — Reset button: position left of title, not right

### Problem
The reset button was placed at the far right of the card title bar using `justify-content: space-between`.

### Legacy pattern
The legacy `embed-secHeaderLeft embed-secHeader` classes positioned the button **immediately after** the section title text, with `margin-left: 1rem`.

### Rule
> **Embedded section-header utility buttons belong immediately after the title, as a direct `<Space>` sibling.** Only use `justify-content: space-between` when there are genuinely two independent groups on opposite ends of a header.

---

## Rule 6 — Reset button: always confirm before clearing search fields

### Legacy pattern
The legacy trash button called `clearValue()` directly with no confirmation dialog.

### New UI policy (deliberate improvement)
Show `Modal.confirm` before executing the reset, so users cannot accidentally wipe all search conditions:

```tsx
const handleResetClick = () => {
  Modal.confirm({
    title: i18n.t('commonElements:actions.resetSearchTitle'),
    content: i18n.t('commonElements:actions.resetSearchContent'),
    okText: i18n.t('commonElements:actions.confirm'),
    cancelText: i18n.t('commonElements:actions.cancel'),
    onOk: executeReset,
  });
};
```

### Rule
> **Always gate irreversible UI state changes (clear all fields, bulk reset) behind a `Modal.confirm`.** This is a deliberate UX improvement over the legacy UI — do not remove it to match legacy exactly.

---

## Rule 7 — Icon font substitution: replace `nmd` / `md` / `ion-` with `mdi`

### Problem
Legacy UI uses several icon fonts that are **not loaded** in the new UI:
- `nmd nmd-*` — custom Material icon variant (legacy `icons.css`)
- `md md-*` — older Material Design icons
- `ion-*` — Ionicons

Using these classes in the new UI renders the `<i>` element but shows **nothing** — the glyph is missing because the font file is not loaded.

### Available icon fonts in the new UI
Only **MDI (Material Design Icons)** is loaded, via:
```scss
// src/assets/scss/icons.scss
@import "custom/icons/materialdesignicons";
```
Classes use the pattern `mdi mdi-<name>`.

### Substitution table (known mappings)

| Legacy class | New UI class | Context |
|---|---|---|
| `nmd nmd-youtube-searched-for` | `mdi mdi-magnify-scan` | Search section title icon |
| `mdi mdi-cart` | `mdi mdi-cart` | Already MDI — no change needed |

### General substitution steps
1. Find what the legacy icon looks like (check `icons.css` glyph code or the rendered legacy UI)
2. Find the closest MDI equivalent at [materialdesignicons.com](https://materialdesignicons.com)
3. Replace with `mdi mdi-<name>` — keep colour classes (e.g. `content-portlet-title`, `content-green`) unchanged; they are defined in `overrides.scss`

### Rule
> **Never use `nmd`, `md md-*`, or `ion-*` icon classes in new UI code.** Always use `mdi mdi-<name>`. When porting legacy `sectionMeta.titleIcon` or `comTitleIcon` strings, substitute to the MDI equivalent and add the mapping to the table above.

---

## Rule 8 — Modal dialog style: portlet-heading header + message warn bar + rounded footer buttons

### Case: DocActionModal style update (2026-08-10)

#### What was wrong (new UI before fix)

The `DocActionModal` had these visual differences from the legacy:

| Element | New UI (before) | Legacy |
|---|---|---|
| Header background | Plain white | Light grey `#eff4f9` (`bg-lightgrey`) |
| Title icon | Dynamic action icon or nothing | `nmd nmd-format-color-text content-orange` always present as default |
| Modal width | Fixed `640px` | `50%` of viewport |
| Warn bar | Ant Design `<Alert type="warning">` | `message-title-box background-messageWarn` div with `md md-warning` icon + `content-darkblue` text |
| Footer buttons | Plain text, no icons | `btn-rounded-embedded` class + `md md-close` / `md md-check` icons |

#### Legacy HTML (key parts)

```html
<!-- Header: light grey portlet heading -->
<div class="portlet-heading bg-lightgrey">
  <h3 class="portlet-title">
    <i class="nmd nmd-format-color-text content-orange"></i> 确定提交该凭证
  </h3>
</div>

<!-- Warn bar: message-title-box with warning icon + darkblue text -->
<div class="message-title-box background-messageWarn">
  <i class="md md-warning"></i>
  <span class="content-darkblue">确定基本内容无误，可以提交审核？</span>
</div>

<!-- Footer: rounded buttons with icons -->
<button class="btn btn-nonAction btn-rounded-embedded">
  <i class="md md-close"></i> 关闭
</button>
<button class="btn btn-action btn-rounded-embedded">
  <i class="md md-check"></i> 确定
</button>
```

#### What was changed in `DocActionModal.tsx`

**1. Header background** — via Ant Design `Modal.styles.header`:
```tsx
// BEFORE
<Modal width={640} ...>

// AFTER
<Modal
  width="50%"
  styles={{ header: { backgroundColor: '#eff4f9', padding: '12px 16px', borderRadius: '8px 8px 0 0' } }}
  ...>
```

**2. Title icon** — always show a default orange icon, override with action-specific:
```tsx
// BEFORE
const iconClass = controller.formatActionCodeIcon(controller.cache.actionCode);
// → could be empty string, rendering no icon at all

// AFTER
const iconClass = controller.formatActionCodeIcon(controller.cache.actionCode)
    || 'mdi mdi-file-document-edit content-orange';
// → always has an icon; mdi mdi-file-document-edit = MDI equivalent of nmd nmd-format-color-text
```

**3. Warn bar** — replaced `<Alert>` with legacy-style div:
```tsx
// BEFORE
<Alert type="warning" message={controller.warnBarText} showIcon style={{ marginBottom: 16 }} />

// AFTER
<div className="message-title-box background-messageWarn" style={{ marginBottom: 16 }}>
  <i className="mdi mdi-alert" style={{ marginRight: 8 }} />
  <span className="content-darkblue">{controller.warnBarText}</span>
</div>
// Do NOT use <Alert> — it does not match the legacy message-title-box visual style
```

**4. Footer buttons** — added `btn-rounded-embedded` class and MDI icons:
```tsx
// BEFORE
<Button onClick={onCancel}>{controller.label.close}</Button>
<Button type="primary" onClick={onOk}>{controller.label.confirm}</Button>

// AFTER
<Button className="btn-rounded-embedded" icon={<i className="mdi mdi-close" />} onClick={onCancel}>
  {controller.label.close}
</Button>
<Button type="primary" className="btn-rounded-embedded" icon={<i className="mdi mdi-check" />} onClick={onOk}>
  {controller.label.confirm}
</Button>
```

#### CSS added to `overrides.scss`

```css
/* Matches legacy components.css .btn-rounded-embedded — used for modal footer buttons */
.btn-rounded-embedded {
  border-radius: 10px;
  padding: 3px 16px;
}
```

#### Icon substitution table (this modal)

| Legacy class | MDI equivalent | Usage |
|---|---|---|
| `nmd nmd-format-color-text` | `mdi mdi-file-document-edit` | Modal title default icon |
| `md md-warning` | `mdi mdi-alert` | Warn bar icon |
| `md md-close` | `mdi mdi-close` | Close/Cancel button icon |
| `md md-check` | `mdi mdi-check` | Confirm/OK button icon |

#### Rule
> For modal dialogs porting from the legacy `DocActionModal` style:
> - Header: `Modal.styles.header` with `backgroundColor: '#eff4f9'`
> - Title: always provide a default icon; use `mdi mdi-file-document-edit content-orange` as fallback
> - Warn bar: `background-messageWarn` div + `mdi mdi-alert` icon + `content-darkblue` text — **not** `<Alert>`
> - Footer buttons: `btn-rounded-embedded` class + MDI icon on each button
> - Width: `"50%"` not a fixed pixel value
The new `DocActionModal` used a plain white Ant Design modal header, a yellow `<Alert>` warn bar, plain text footer buttons with odd spacing, and no icon in the title — all differing from the legacy style.

### Legacy pattern (from `DocActionModal` HTML)
- **Header**: `<div class="portlet-heading bg-lightgrey">` — light grey background (`#eff4f9`)
- **Title icon**: `<i class="nmd nmd-format-color-text content-orange">` — orange icon before the title text
- **Warn bar**: `<div class="message-title-box background-messageWarn">` with `<i class="md md-warning">` and `<span class="content-darkblue">` text
- **Footer buttons**: `btn-rounded-embedded` class — `border-radius: 10px; padding: 3px 16px`
  - Close: `<i class="md md-close">` icon
  - Confirm: `<i class="md md-check">` icon

### Correct approach

**Modal header background** — via Ant Design `styles.header`:
```tsx
<Modal
  styles={{ header: { backgroundColor: '#eff4f9', padding: '12px 16px', borderRadius: '8px 8px 0 0' } }}
  width="50%"
  ...
>
```

**Title icon** — default to `mdi mdi-file-document-edit content-orange` (MDI equivalent of `nmd nmd-format-color-text`), override with action-specific icon from `formatActionCodeIcon()`:
```tsx
const iconClass = controller.formatActionCodeIcon(controller.cache.actionCode)
    || 'mdi mdi-file-document-edit content-orange';
```

**Warn bar** — use the existing `background-messageWarn` CSS class with `mdi mdi-alert` icon and `content-darkblue` text (matches `md md-warning`):
```tsx
<div className="message-title-box background-messageWarn" style={{ marginBottom: 16 }}>
  <i className="mdi mdi-alert" style={{ marginRight: 8 }} />
  <span className="content-darkblue">{controller.warnBarText}</span>
</div>
```
Do NOT use Ant Design `<Alert>` — it does not match the legacy visual style.

**Footer buttons** — use `btn-rounded-embedded` class + MDI icons:
```tsx
<Button className="btn-rounded-embedded" icon={<i className="mdi mdi-close" />} onClick={onCancel}>
  {label.close}
</Button>
<Button type="primary" className="btn-rounded-embedded" icon={<i className="mdi mdi-check" />} onClick={onOk}>
  {label.confirm}
</Button>
```

### CSS classes added to `overrides.scss`
```css
/* Matches legacy components.css .btn-rounded-embedded */
.btn-rounded-embedded {
  border-radius: 10px;
  padding: 3px 16px;
}
```

### Icon substitution (this modal)
| Legacy | New UI (MDI) |
|---|---|
| `nmd nmd-format-color-text` | `mdi mdi-file-document-edit` |
| `md md-warning` | `mdi mdi-alert` |
| `md md-close` | `mdi mdi-close` |
| `md md-check` | `mdi mdi-check` |

### Rule
> For modal dialogs: use `styles.header` with `backgroundColor: '#eff4f9'` for the portlet-heading look; use `background-messageWarn` + `content-darkblue` for warn bars (not `<Alert>`); use `btn-rounded-embedded` class on footer buttons with MDI icons.

---

## Rule 9 — Item Quick-Edit Panel (PopBottomPanel / EditPanel) layout

### Case: Panel style alignment with legacy (2026-08-13)

#### What was wrong (new UI before fix)

| Element | New UI (before) | Legacy |
|---|---|---|
| Panel width | Full viewport `left: 0` — covered the left sidebar | Content area only: starts at `left: 240px` (sidebar edge) |
| Fold/unfold button | Sticky `.row-expand` bar inside the panel body | Absolutely positioned on the **top border edge** of the panel, straddling the border line |
| Header / section title | Plain `div` with Save + Expand buttons, no title | `portlet-heading bg-lightgrey` with section icon + title text + buttons |

#### Legacy HTML (key structure)

```html
<!-- Panel container — starts after sidebar -->
<footer class="foot-wrapper hide-display" style="left: 240px">
  <!-- Fold button on the top border edge -->
  <div class="panel-fold-handle">▼</div>

  <!-- Portlet header: bg-lightgrey, section title + embedded buttons -->
  <div class="portlet-heading bg-lightgrey">
    <h3 class="portlet-title">
      <i class="mdi mdi-texture content-portlet-title"></i> 物料项目基本信息
    </h3>
    <div class="portlet-widgets">
      <button class="btn-rounded-embed-search">保存</button>
      <button class="btn-rounded-embed-search">展开</button>
      <!-- ItemQuickAction prev/next -->
    </div>
  </div>

  <!-- Panel body (AsyncPage renders here) -->
  <div class="footer-content">...</div>
</footer>
```

#### Fix 1 — Width (CSS `pop-bottom-panel.css`)

```css
/* BEFORE */
.foot-wrapper { left: 0; }

/* AFTER — starts at main-content edge, skipping the 240px sidebar */
.foot-wrapper { left: 240px; /* = $leftbar-width */ }

/* Condensed/small sidebar variants */
body.sidebar-condensed .foot-wrapper { left: 70px; }
body.sidebar-sm .foot-wrapper         { left: 160px; }
```

Also changed `border-top` from `1px solid #e6e6e6` to `2px solid #4a9fd4` (blue top border matching legacy).

#### Fix 2 — Fold/unfold button position (CSS + `PopBottomPanel.tsx`)

**CSS — new `.panel-fold-handle` class:**
```css
.foot-wrapper .panel-fold-handle {
  position: absolute;
  top: -14px;          /* straddles the top border: half height above */
  left: 50%;
  transform: translateX(-50%);
  z-index: 3;
  width: 56px; height: 28px;
  background: #fff;
  border: 2px solid #4a9fd4;
  border-radius: 6px 6px 0 0;
}
```

**`PopBottomPanel.tsx`** — replaced the old sticky `.row-expand` bar with the new border-edge handle:
```tsx
/* BEFORE — sticky inside panel body */
<div className="row row-expand">
  <div className="expand-wrapper footer-style" onClick={hidePanel}>
    <i className="mdi mdi-chevron-down" />
  </div>
</div>

/* AFTER — absolute on border edge */
<div className="panel-fold-handle" onClick={hidePanel}>
  <i className="mdi mdi-chevron-down content-green" />
</div>
```

Removed the old `.row-expand`, `.expand-wrapper` CSS rules (replaced by `.panel-fold-handle`).

#### Fix 3 — Portlet header with section title (`EditPanel.tsx`)

Replaced the plain button row with a full portlet-style header:

```tsx
/* BEFORE — plain row of buttons, no title */
<div className="panel-header-row" style={{ display: 'flex', ... }}>
  <Button>保存</Button>
  <Button>展开</Button>
  <ItemQuickAction ... />
</div>

/* AFTER — portlet header: title + separator + buttons, ALL LEFT-ALIGNED */
<div className="panel-portlet-header"> {/* bg-lightgrey, sticky top */}
  <div className="panel-portlet-title">
    <i className={panelSectionIcon} />   {/* from panelPageMeta first section */}
    <span>{resolvedTitle}</span>          {/* i18n-resolved sectionTitle */}
  </div>
  {/* Thin vertical divider between title and buttons */}
  <div className="panel-portlet-separator" />
  <div className="panel-portlet-actions">
    <Button className="btn-rounded-embed-search">保存</Button>
    <Button className="btn-rounded-embed-search">展开</Button>
    <ItemQuickAction ... />
  </div>
</div>
```

**Layout rule**: title and buttons are ALL LEFT-ALIGNED. Do NOT give `panel-portlet-title` `flex: 1` — that would push the buttons to the far right. All items are natural-width flex children in a left-to-right row.

The `panelSectionIcon` and `resolvedTitle` are derived at render time from `panelPageMeta.sectionMetaList[0]` — so the header always shows the correct section name and icon for the current panel type (material unit, contract item, etc.).

**CSS for portlet header** (in `pop-bottom-panel.css`):
```css
.foot-wrapper .panel-portlet-header {
  background: #eff4f9;  /* bg-lightgrey */
  border-bottom: 1px solid #e0e8f0;
  position: sticky; top: 0; z-index: 2;
  display: flex; align-items: center; gap: 8px; padding: 8px 12px;
}
.foot-wrapper .panel-portlet-title {
  /* NO flex: 1 — title is natural width so buttons stay left-adjacent */
  display: flex; align-items: center; gap: 6px;
  font-size: 14px; font-weight: 600;
}
.foot-wrapper .panel-portlet-separator {
  width: 1px; height: 16px; background: #c8d8e8; flex-shrink: 0;
}
.foot-wrapper .panel-portlet-actions {
  display: flex; align-items: center; gap: 6px;
}
```

Buttons use the existing `.btn-rounded-embed-search` class (Rule 2) so they match the compact embedded-button style.

#### Fix: buttons left-aligned (follow-up correction, 2026-08-13)

**Problem:** buttons were pushed to the far right because `.panel-portlet-title` had `flex: 1`.

**Fix** — remove `flex: 1` from `.panel-portlet-title` and `flex-shrink: 0` from `.panel-portlet-actions`. Add a thin separator `<div class="panel-portlet-separator">` between title and buttons. All items are natural-width flex children — left-to-right, all left-aligned.

```css
/* WRONG — flex:1 pushes buttons to the right */
.foot-wrapper .panel-portlet-title { flex: 1; ... }

/* CORRECT — natural width, buttons stay adjacent */
.foot-wrapper .panel-portlet-title { display: flex; align-items: center; gap: 6px; }
.foot-wrapper .panel-portlet-separator { width: 1px; height: 16px; background: #c8d8e8; }
.foot-wrapper .panel-portlet-actions { display: flex; align-items: center; gap: 6px; }
```

#### Fix: fold handle clipped by overflow (follow-up correction, 2026-08-13)

**Problem:** `overflow-y: auto` on `.foot-wrapper` clipped the `position: absolute; top: -14px` fold handle — the top half was hidden.

**Root cause:** Any `overflow` value other than `visible` on a containing block clips absolutely-positioned children that extend outside the box boundary.

**Fix** — move `overflow-y: auto` and `max-height` from `.foot-wrapper` to `.footer-content` (the body slot). The wrapper itself becomes `overflow: visible` so the fold handle is never clipped:

```css
/* WRONG — clips the fold handle */
.foot-wrapper { overflow-y: auto; max-height: 70vh; }

/* CORRECT — wrapper visible, only body scrolls */
.foot-wrapper { overflow: visible; }
.foot-wrapper .footer-content { overflow-y: auto; max-height: 60vh; }
```

The portlet header (`panel-portlet-header`) is also moved outside `.footer-content` in the DOM so it stays fixed at the panel top while only the body content scrolls.

#### Rule
> For the item quick-edit panel (PopBottomPanel / EditPanel):
> - **Width**: `left: 240px` to confine the panel to the main content area (not cover the sidebar)
> - **Fold button visibility**: `.foot-wrapper` must be `overflow: visible` — never `overflow-y: auto` — so the `top: -14px` fold handle is not clipped. Put `overflow-y: auto` on `.footer-content` instead.
> - **Fold button position**: absolute at `top: -14px; left: 50%`, straddling the top border line
> - **Header layout**: title + separator + buttons ALL LEFT-ALIGNED — do NOT give the title `flex: 1`
> - **Header**: `panel-portlet-header` with `bg-lightgrey` background, section title (from `panelPageMeta`), thin separator, action buttons using `btn-rounded-embed-search`

---

## Rule 10 — Section card portlet: full border + shadow treatment

### Case: Section card style alignment with legacy portlet (2026-08-13, updated 2026-08-13)

#### What was wrong
Ant Design `<Card>` components used as section wrappers had:
- No top border accent on the header
- No side/bottom borders on the header strip
- No full-box border (just antd's default thin border)
- No box shadow — sections looked flat
- Rounded corners — legacy portlet boxes are sharp rectangles
- No bottom margin between sections

#### Legacy pattern
```css
/* core.css — .portlet box */
.portlet {
  border-top: 2px solid #073c71;   /* dark-blue accent */
  border-radius: 0;
  margin-bottom: 20px;
}

/* core.css / components.css — .card-box shadow */
.card-box {
  box-shadow: 0px 1px 3px 1px rgba(8, 84, 160, 0.4);
}

/* core.css — .portlet-body side/bottom borders */
.portlet-body {
  border: 1px solid rgb(158, 178, 200);
}
```

#### Complete fix (`src/styles/overrides.scss`)

```scss
/* Section card header (.ant-card-head) — portlet header strip */
html[data-bs-theme="light"] .ant-card-head {
  background: #eff4f9;                       /* bg-lightgrey */
  border-top: 2px solid #073c71;             /* dark-blue portlet accent */
  border-bottom: 1px solid rgb(158, 178, 200);
  border-left: 1px solid rgb(158, 178, 200);
  border-right: 1px solid rgb(158, 178, 200);
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
}

/* Section card box (.ant-card) — portlet wrapper */
.ant-card {
  border-radius: 0 !important;               /* sharp corners */
  border: 1px solid rgb(158, 178, 200);      /* full box border */
  box-shadow: 0px 1px 3px 1px rgba(8, 84, 160, 0.4);  /* blue glow */
  margin-bottom: 20px;                       /* vertical rhythm */
}

.ant-card-head {
  border-radius: 0 !important;
}
```

#### Why `!important` on border-radius
Antd applies `borderRadiusLG` (8px) as an inline token-driven CSS variable at the component level. Scoping the override to `.ant-card` / `.ant-card-head` surgically targets only section cards without affecting modals, dropdowns, or popovers that share the same token.

#### Value reference
| Value | Source | Meaning |
|---|---|---|
| `#073c71` | `core.css .portlet border-top` | Dark-blue portlet header accent |
| `rgb(158, 178, 200)` | `core.css .portlet-body border` | Grey-blue side/bottom borders |
| `rgba(8, 84, 160, 0.4)` | `core.css .card-box box-shadow` | Blue glow shadow |
| `#eff4f9` | `core.css .bg-lightgrey` | Header background |

#### Rule
> All section cards (Ant Design `<Card>` used as section wrappers) must have the full portlet treatment:
> - **Header** (`.ant-card-head`): `border-top: 2px solid #073c71`, `border-left/right/bottom: 1px solid rgb(158,178,200)`, `border-radius: 0`, `background: #eff4f9`
> - **Box** (`.ant-card`): `border: 1px solid rgb(158,178,200)`, `box-shadow: 0 1px 3px 1px rgba(8,84,160,0.4)`, `margin-bottom: 20px`, `border-radius: 0`
