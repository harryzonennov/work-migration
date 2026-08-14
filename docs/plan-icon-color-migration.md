# Plan: Legacy Icon + Color Class Migration to Minton

> **Status:** Planning document. No code changes yet.
> **Created:** 2026-07-01
> **Companion docs:** [`MINTON_ICON_REFERENCE.md`](./MINTON_ICON_REFERENCE.md), [`MINTON_CUSTOMIZATION_GUIDE.md`](./MINTON_CUSTOMIZATION_GUIDE.md)

---

## 1. What went wrong (both problems)

The user reported two symptoms:

1. **Some icons don't display** — even ones using `fa fa-*` (which we thought Minton shipped fully) fail. Example: `fa fa-rotate-left` broken, `fa fa-save` works.
2. **`content-*` color classes don't apply** — icons that should be green, red, orange, etc. render in the default text color.

Both are real problems with the same underlying nature: **the migration to Minton copied icon fonts and structural CSS, but skipped project-specific class definitions**.

## 2. Root cause analysis

### 2.1 Problem 1 — Icon classes broken

The earlier "Broken legacy icons substituted with Minton equivalents" round (2026-07-01, earlier today) fixed only **12 hard-coded icon references in TSX files**. It missed the far larger surface: **~80 unique icon classes referenced from data files** (`DocumentConstants.ts`, `DocumentManagerFactory.ts`, `SystemStandrdMetadataProxy.ts`) that get rendered through controllers into buttons and cards.

Of those ~80 classes, three sub-problems:

- **Ionicons `ion-*`** — Minton doesn't ship the Ionicons v1 font. Every `ion-*` reference is broken.
- **Materialize `md md-*`** — Minton doesn't ship Materialize Icons (it ships MDI which uses `mdi mdi-*`, a different font). Every `md md-*` reference is broken.
- **Custom `nmd nmd-*`** — Legacy custom font. Not shipped. Every reference broken.
- **FontAwesome `fa fa-*`** — Minton **does** ship FontAwesome 5 free. BUT the FA4-to-FA5 upgrade **renamed several icons**. Some FA4 names simply don't exist in FA5:

| Legacy FA4 name | Shipped in FA5? | FA5 replacement |
|---|---|---|
| `fa fa-rotate-left` | ❌ | `fa fa-undo` |
| `fa fa-gear` | ❌ | `fa fa-cog` |
| `fa fa-gears` | ❌ | `fa fa-cogs` |
| `fa fa-file-excel-o` | ❌ (FA4 `-o` outlined suffix dropped) | `far fa-file-excel` |
| `fa fa-plus-square-o` | ❌ | `far fa-plus-square` |
| `fa fa-building-o` | ❌ | `far fa-building` |
| `fa fa-warning` | ❌ (renamed) | `fa fa-exclamation-triangle` |
| `fa fa-flag-checkered` | ✅ | (same) |
| `fa fa-save` | ✅ | (same) |
| `fa fa-print`, `fa fa-search`, `fa fa-plus`, `fa fa-key`, etc. | ✅ | (same) |

That's why `fa fa-save` displays but `fa fa-rotate-left` doesn't — same prefix, different fate.

### 2.2 Problem 2 — Color classes don't work

Grepped all SCSS under `src/assets/scss/` for `.content-<color>` definitions. **Found zero.**

Legacy IntelligentUI defined these color utilities in its own project CSS (probably `admin/css/style.css` or similar), never bundled with Minton. When we adopted the Minton bundle, we brought **structural** classes (`.card`, `.navbar-custom`, `.side-nav-*`, `.portlet`) but not **legacy project-specific utility classes** like `.content-green`, `.content-red`, etc.

**14 color classes are used but undefined:**

| Class | Legacy value (from ThorSalesDistributionUI/admin/css) |
|---|---|
| `content-green` | (green — success semantic) |
| `content-red` | (red — danger semantic) |
| `content-orange` | (orange — warning semantic) |
| `content-pink` | (pink — accent) |
| `content-grey` | (grey — muted) |
| `content-greyblue` | (muted blue-grey) |
| `content-darkblue` | (deep blue) |
| `content-darkblue1` | (variant 1 of deep blue) |
| `content-darkblue2` | (variant 2 of deep blue) |
| `content-lightblue` | (light blue) |
| `content-linkblue` | (blue for links) |
| `content-peach-red` | (peach-red / coral) |
| `content-portlet-title` | (portlet header title color) |
| `content-list` (used with icons for lists?) | (unclear — verify per usage) |

Legacy CSS defined each as a `color:` rule (and sometimes background variants). All 14 need to be either:
- Reproduced in `overrides.scss` as `color:` utilities, OR
- Migrated to Minton's existing utility classes (`.text-success`, `.text-danger`, etc.)

## 3. Strategic options

### For Problem 1 (broken icons): three approaches

| Option | Description | Effort | Ongoing cost |
|---|---|---|---|
| **1A. Global substitute in data files** | Grep + Edit every `ion-*`, `md md-*`, `nmd nmd-*`, and FA4-legacy-name to Minton equivalent across all TS/TSX data files | ~2-3 hours | Zero ongoing cost — data files are self-contained after |
| **1B. Runtime icon-class mapper** | Write a `normalizeIconClass(legacy)` function that translates old class strings to Minton equivalents. Wrap it around any place that consumes `iconClass` from data files | ~1 hour | +1 function call per icon render (negligible perf). Legacy strings survive in data files forever. |
| **1C. Load the missing legacy fonts** | Bundle Ionicons v1, Materialize, and custom nmd fonts alongside Minton's | ~2 hours | +~200 KB fonts, three more `@font-face`s, keeps legacy debt indefinitely |

**Recommended: 1A.** It's the durable answer. Once data files are migrated, the codebase is fully on Minton icons with no debt. 1B adds a runtime layer that outlives itself. 1C keeps the migration incomplete.

### For Problem 2 (missing color classes): two approaches

| Option | Description | Effort |
|---|---|---|
| **2A. Add `.content-*` utilities to `overrides.scss`** | Define the 14 classes as `color:` rules using CSS variables (`var(--bs-success)` for green, etc. — so dark mode adapts) | ~30 min |
| **2B. Substitute `.content-*` with Minton's `.text-*` utilities in data files** | Every `content-green` → `text-success`, `content-red` → `text-danger`, etc. — uses Minton's Bootstrap-derived text-color utilities | ~1-2 hours (bigger surface than 2A) |

**Recommended: 2A.** Much less invasive. Data files stay unchanged. Legacy color-class semantics preserved. Dark mode works because the `.content-*` utilities reference `--bs-*` variables which switch per theme.

If 2A ever feels dirty (keeping legacy class names alive forever), a future round can migrate to `text-*` utilities via 2B. But for now, 2A ships in 30 minutes and both problems resolve cleanly.

### Combining: recommended path

**2A + 1A together.**

Sequence matters: **do 2A FIRST** (add color classes → immediately fixes half the visual bugs even before touching icons), **then 1A** (substitute icon names — now icons render AND they get the right colors).

## 3.5 Confirmed legacy color values (from `ThorSalesDistributionUI/admin/assets/css/core.css`)

Grepped the legacy source for the ground-truth values. Each `.content-*` class was defined as a `color:` rule:

| Class | Legacy hex | Semantic role | Notes |
|---|---|---|---|
| `content-red` | `#c81827` | Danger (foreground) | Legacy had `/*color: #ee6e73;*/` commented out — final was the darker `#c81827` |
| `content-peach-red` | `#e91e63` (legacy) → **`#f77e8e` (user override, temporary)** | Danger variant / status color | User explicitly said "use #f77e8e temporary" |
| `content-green` | `rgb(0, 177, 157)` = `#00b19d` | Success (foreground) | Teal-green, close to but not the same as Bootstrap's `--bs-success` `#1abc9c` |
| `content-yellow` | `#f9cd48` | Warning variant | Not yet cataloged in Phase A sketch — add |
| `content-orange` | `#ffaa00` | Warning (foreground) | Slightly more saturated than Bootstrap's `--bs-warning` `#f7b84b` |
| `content-lightblue` | `#0372ea` | Info (foreground) | Blue link-adjacent |
| `content-linkblue` | `#0073ea` | Link blue | Nearly identical to lightblue — separate class kept |
| `content-darkblue` | **`#0854a0`** | Foreground blue (heading) | **NOT the same as `--bs-brand-dark`** (which is `#01053e`) |
| `content-darkblue1` | **`#01053e`** | Deepest foreground blue | Same hex as `$brand-dark`, but distinct role (foreground/text, not background) |
| `content-darkblue2` | **`#253b61`** | Mid-tone foreground blue | Between darkblue and darkblue1 |
| `content-greyblue` | `#314e87` | Muted heading foreground | |
| `content-portlet-title` | `#073c71` | Portlet title text | |
| `content-grey` | `#777` | Muted foreground | |
| `content-grey2` | `#9eb2c8` | Lighter muted foreground | Not yet in Phase A sketch — add |
| `content-pink` | `#f76397` | Accent (foreground) | |
| `content-white` | `#fff` | White foreground | Not yet in Phase A sketch — add |
| `content-lightwhite` | `#eff4f9` | Faint bg-tinted foreground | Match to `--bs-topbar-bg` (already used for topbar), but semantically foreground here |
| `content-lightGrey` | `#dce6f7` | Very pale foreground | Note: legacy uses inconsistent capitalization `lightGrey` |

## 3.6 Naming decision — `content-darkblue` vs `--bs-brand-dark`

The user explicitly clarified: **do NOT collapse `content-darkblue*` into `--bs-brand-dark`** even where they share `#01053e`. Reasoning:

- `--bs-brand-dark` = **background color role**, used in Issue 7 for form labels + section titles (where it's a color-of-text-on-light-bg role)
- `content-darkblue*` = **foreground/text color role**, used for icons and inline text

Same hex value, different semantic role. Keeping them separate means either can change without dragging the other.

**Two ways to implement this cleanly:**

- **Option X (recommended for Phase A):** define `.content-darkblue1` etc. with the literal legacy hex directly — don't try to unify with any token. Explicit, no coupling.
- **Option Y (future refactor):** promote to a project-token `$content-fg-darkblue` / `$content-fg-darkblue1` / `$content-fg-darkblue2` in `_variables-custom.scss`, emit as CSS variables in `_theme-config.scss`. Same pattern as `$brand-dark` from Issue 7. Overhead only worthwhile if the values need light/dark theme variants or if new consumers appear beyond the `.content-*` utilities.

## 4. Phased plan

### Phase A — Add the `.content-*` color classes to `overrides.scss` ✅ DONE (2026-07-02)

**Deliverable:** New "Issue 9" block in `overrides.scss` defining all legacy color utilities as `color:` rules with literal legacy hex values, plus `!important` for antd cascade tie-breaking. No CSS-variable tokens for foreground colors (per §3.6 Option X); dark mode does NOT re-tint these — they're intentional legacy foreground hex values.

**Sketch of what goes in (final version, with confirmed legacy hex values):**

```scss
// ─────────────────────────────────────────────────────────────────────────
// Issue 9 — Legacy `.content-*` color utility classes (not shipped by Minton).
//
// IntelligentUI's legacy code (TSX files + data-file iconClass strings)
// uses `.content-green`, `.content-red`, `.content-darkblue1`, etc. as
// icon FOREGROUND color modifiers. These classes were defined in legacy
// `ThorSalesDistributionUI/admin/assets/css/core.css` but were NOT
// copied over during the Minton adoption. Every legacy icon consequently
// renders in default text color instead of its intended color.
//
// Values below are the exact legacy hex from core.css (confirmed 2026-07-02),
// with one intentional override: `content-peach-red` uses `#f77e8e` per
// user's temporary preference (legacy was `#e91e63`).
//
// Semantic note: `content-darkblue1` shares its hex `#01053e` with
// `$brand-dark` (from Issue 7), but the roles differ — brand-dark is a
// BACKGROUND-adjacent foreground for headings/labels; content-darkblue1
// is an ICON foreground. Kept as separate literal values so future theme
// tuning of one doesn't affect the other.
//
// All rules use `!important` because antd may inject inline `color`
// through its component tokens. Precedent: Issue 8 (card-head bg).
// ─────────────────────────────────────────────────────────────────────────

// Semantic status colors
.content-red        { color: #c81827  !important; }
.content-peach-red  { color: #f77e8e  !important; }  // user override; legacy was #e91e63
.content-green      { color: #00b19d  !important; }
.content-yellow     { color: #f9cd48  !important; }
.content-orange     { color: #ffaa00  !important; }
.content-pink       { color: #f76397  !important; }

// Blues / links
.content-lightblue  { color: #0372ea  !important; }
.content-linkblue   { color: #0073ea  !important; }
.content-darkblue   { color: #0854a0  !important; }
.content-darkblue1  { color: #01053e  !important; }
.content-darkblue2  { color: #253b61  !important; }
.content-greyblue   { color: #314e87  !important; }
.content-portlet-title { color: #073c71 !important; }

// Neutrals
.content-grey       { color: #777     !important; }
.content-grey2      { color: #9eb2c8  !important; }
.content-white      { color: #fff     !important; }
.content-lightwhite { color: #eff4f9  !important; }
.content-lightGrey  { color: #dce6f7  !important; }  // legacy camelCase preserved
```

`content-list` and `content-paste` classes seen in the grep are NOT color classes — they're component-role classes (see AsyncEditSection.tsx). Do NOT add them here. Component classes stay handled by their component's own rules.

**Total: 19 utility rules** covering every legacy `.content-*` foreground color class. Includes 4 that were missing from the earlier sketch (`content-yellow`, `content-grey2`, `content-white`, `content-lightwhite`, `content-lightGrey`).

**Verification:** open dev server, refresh a page with existing broken color classes. Icons that WERE broken because of colors should now render in their intended color (assuming the underlying font is loaded — the font problem is Phase B).

### Phase B — Icon substitution across data files ✅ DONE (2026-07-02)

**Sub-phases per data source (each independently verifiable):**

#### B.1 — `src/services/DocumentConstants.ts` (~30 icons)

The document-action icon registry. Contains:
- `ACTION_*` iconClasses used by ProcessButtonArray (buttons like 反审核 / 保存 / 交货完成)
- `DOCFLOW_*` iconClasses used by document flow section
- `STATUS_*` iconClasses used by document status badge

**Substitution strategy per class:**
- FA4 renames (`fa-rotate-left`, `fa-gear`, `fa-gears`, `-o` suffix drops) → correct FA5 name
- All `md md-*` → `mdi mdi-*` equivalent (biggest set, most matches)
- All `nmd nmd-*` → `mdi mdi-*` equivalent (nmd extended Materialize, so MDI usually covers it)
- All `ion-*` → `mdi mdi-*` equivalent

The color half of each string (`content-green`, etc.) stays untouched — Phase A already defined them.

#### B.2 — `src/services/DocumentManagerFactory.ts` (~50 icons)

Document-type icon registry — one entry per DocumentType.

Same substitution strategy as B.1.

#### B.3 — `src/services/SystemStandrdMetadataProxy.ts` (~5 icons)

System metadata iconClasses.

Same substitution.

#### B.4 — `src/services/legacy/DocumentManagerFactory.legacy.js` — **DO NOT TOUCH**

This is under `legacy/` and not imported by any TS code (verified during earlier icon round). Leave as-is per the "legacy JS untouched" principle.

#### B.5 — Any remaining `.tsx` hardcoded icons

- `DragDropDesignSection.tsx:203` — `fa fa-gear` → `mdi mdi-cog`
- `DragSideCard.tsx:57` — `fa fa-gear` → `mdi mdi-cog`
- `DragSideWidget.tsx:28` — `fa fa-bookmark` → works, but consider unifying to `mdi mdi-bookmark` for consistency

### Phase C — Update `MINTON_ICON_REFERENCE.md` [~15 min]

Two additions to the substitution table:

1. **FA4→FA5 rename table** — new subsection listing the 6-8 FA class renames (gear→cog, rotate-left→undo, etc.). Documents WHY some `fa fa-*` classes still break.

2. **`.content-*` color-class reference** — new table listing the 14 classes with their SCSS definition file, resolved color, and dark-mode value.

### Phase D — Optional cleanup [deferred]

If the team later wants to eliminate legacy class names entirely:

- **Phase D.1** — Substitute `.content-*` calls in TSX/data files with Bootstrap `.text-*` utilities (`content-green` → `text-success`, etc.). Requires touching 100+ sites. Not required — Phase A's utility layer works forever.
- **Phase D.2** — Substitute remaining `fa fa-*` classes with `mdi mdi-*` for consistency. Not required — FA5 works alongside MDI.
- **Phase D.3** — Investigate whether `content-list` and `content-paste` are icon-related utility classes (color/spacing) or component-structural classes. Currently ambiguous.

## 5. Risk assessment

| Risk | Likelihood | Mitigation |
|---|---|---|
| A substituted MDI icon doesn't visually match its Materialize/Ionicons original | Medium | Verify visually in dev after each sub-phase. MDI has 7000 icons — usually a close match exists. If not, note in the substitution table for future review. |
| The `content-portlet-title` class isn't just a color — may need extra styling | Medium | Check in browser DevTools first; adjust rule if compound. |
| `!important` on the color utilities collides with someone's later override | Low | Only necessary because antd's runtime `:where()`-neutralized rules may set an `inline color` that outweighs a class rule. Precedent from Issue 8 — antd doesn't use `!important` itself so ours wins. |
| Some FA5 aliases silently render a different icon than legacy expected | Low | FA5 aliased many FA4 names. If a substitution "works" but looks slightly different, it's the alias behavior; not a bug per se. |
| Data files still contain `nmd nmd-` or `ion-` strings after Phase B, missed because they're constructed dynamically | Low | Final grep after B.5 catches remnants: `grep -rEn "(nmd nmd-|ion-|md md-)"` in `src/services/` and `src/components/` should return zero after Phase B. |

## 6. Effort estimate

| Phase | Time | Bundle impact | Verification |
|---|---|---|---|
| A — `.content-*` utilities | ~30 min | +14 rules ~0.3 KB CSS | Refresh a page — colors return |
| B.1 — DocumentConstants | ~45 min | 0 | ProcessButtonArray icons render + colored |
| B.2 — DocumentManagerFactory | ~1 hr | 0 | Document type icons everywhere colored |
| B.3 — SystemStandrdMetadataProxy | ~15 min | 0 | System-level icons |
| B.5 — TSX hardcodes | ~10 min | 0 | Design-mode icons |
| C — Doc update | ~15 min | 0 | (documentation) |
| **Total** | **~3-4 hours** | **~0.3 KB CSS, 0 JS** | End-to-end |

## 7. Open questions

Before executing, one thing left to confirm with the user:

1. ~~**Color mapping** — `content-darkblue` maps to `--bs-brand-dark`~~ **RESOLVED (2026-07-02):** legacy `content-darkblue` = `#0854a0` (medium blue, NOT `#01053e`), `content-darkblue1` = `#01053e` (same hex as `$brand-dark` but distinct semantic role — foreground vs background), `content-darkblue2` = `#253b61`. Confirmed from `ThorSalesDistributionUI/admin/assets/css/core.css`. All 3 classes kept as separate literal hex rules per user's naming decision (§3.6). The `--bs-brand-dark` token is untouched.

2. ~~**`content-peach-red` value**~~ **RESOLVED (2026-07-02):** user directive: `#f77e8e` (temporary override; legacy was `#e91e63`). Kept as literal hex.

3. **Scope agreement (open):** proceed A + B fully, or Phase A only first (color classes) so we can see how much of the visual improves before committing to the data-file icon substitution work?

## 8. Recommendation

**Phase A first, standalone.** ~30 minutes of work.

Rationale: Phase A **fixes half the visible problem immediately** by making legacy color classes work. Any icon whose FONT class was already correct (mostly `fa fa-*` icons) will now display in its intended color. That's a meaningful improvement without touching a single data file.

After Phase A, the visual state clarifies:
- Icons using working fonts (FA5-shipped classes) + working colors (Phase A adds these) → **fully rendered**
- Icons using broken fonts (`md md-*`, `nmd nmd-*`, `ion-*`, FA4-legacy names) → **still broken color-only** or **still fully broken**

Then Phase B (the big one) becomes the follow-up round(s), possibly split by data file so each sub-phase is independently reviewable.

## 9. Next steps

1. **User reviews this plan.** Decide:
   - Phase A only first? Or A + B all-in?
   - Answers to the three open questions in §7?
   - Any pushback on the recommended approach (2A + 1A) vs alternatives (1B runtime mapper, 1C loading legacy fonts)?

2. **If proceeding with Phase A:** ~30 minutes, one commit — I write Issue 9 into `overrides.scss`, verify build, update log.

3. **If proceeding with B afterwards:** likely done as 3-4 sub-commits (one per data file) to keep review manageable.

No code changes are made by this plan — it's a planning document only. The next user message determines what ships.
