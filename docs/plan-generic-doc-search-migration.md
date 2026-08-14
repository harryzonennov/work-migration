# Migration Plan — Decouple Generic Document Search from PurchaseContract

**Date:** 2026-07-14
**Scope:** Extract the reusable document-search infrastructure from PurchaseContract business logic in `IntelligentUI`, consolidate the two competing search systems, and close the known wiring gaps.
**Legacy source:** `admin/js/component/basicElements/ServiceUiController.js`, `admin/js/ServiceHttpRequestHelper.js`, `admin/js/supplyChain/PurchaseContractList.js`
**Target:** `/Users/I043125/work2/IntelligentUI/src/`

---

## 0. Key finding — the decoupling is already 90% done

The generic/business split you identified in the legacy code **already exists in the ported React code**. This plan is **consolidation + gap-fill**, not a fresh port.

### What already exists (verified in source)

| Concern | Location | State |
|---|---|---|
| Generic tab/field builder `extendDocSearchTabFieldMeta` + all `_getTemplate*TabMeta` helpers | `src/controllers/ServiceListController.ts:904-967` (+ `_getTemplate*` above) | **Ported, faithful** |
| PurchaseContract business config (the ONLY business surface) | `src/pages/logistics/purchaseContract/PurchaseContractListController.tsx:141-163` | **Correct** — passes `helpKeyPrefix`, `docFlowIdList`, `docPartyIdList`, `docActionLogIdList`, `headerPostMetaList` |
| Search runtime (`searchModule`, `handleReset`, `setPanelParams`, `panelParams`) | `ServiceListController.ts:414-434` | Exists |
| Meta→config parsers (`buildSearchFields`, `buildSearchTabs`) | `ServiceListController.ts:235-286` | Exists |
| Search form renderer (System A, routed) | `src/components/page/AsyncSearchSection.tsx` | Renders; **wiring stub** |
| Search form renderer (System B, orphaned) | `src/components/SearchPanel.tsx` + `ListPageShell.tsx` | Complete, **not routed** |

### The business surface is already this small (`PurchaseContractListController.tsx:141`)
```ts
ServiceListController.extendDocSearchTabFieldMeta({
  vm: this,
  helpKeyPrefix: 'purchaseContract',
  docActionLogIdList: ['submittedBy', 'approvedBy', 'deliveryDoneBy'],
  docFlowIdList: ['prevProfDoc', 'nextDoc', 'nextProfDoc', 'reservedByDoc'],
  docPartyIdList: ['purchaseFromSupplier', 'purchaseToOrg'],
  headerPostMetaList: [ signDate range, requireExecutionDate range ],
})
```
Everything else (basic/createUpdate/material tabs, field shapes, i18n structure) is generic. **This is exactly the decoupling target.** The plan protects and completes it rather than rebuilding it.

---

## 1. Problems to fix (the actual work)

1. **Two competing search systems.** Only `AsyncListPage` → `AsyncSearchSection` (System A) is routed. `ListPageShell`/`SearchPanel` (System B) is complete but orphaned. Must pick one and delete/quarantine the other.
2. **Search→table wiring is a stub.** `AsyncListPage.tsx:9-11` just delegates to `AsyncPage` unchanged; the header comment admits `searchModule` isn't wired. In the routed path `pageMeta.listTable` reaches `AsyncSearchSection` but the ProTable reload path needs verification end-to-end.
3. **Select fields render empty.** Both systems produce `options: []` for status/priority/docType selects — `settings.getMetaDataUrl` → option-loading is unimplemented (`AsyncSearchSection.tsx:128-137`, `ServiceListController.ts:279`).
4. **i18n key-shape mismatch.** Code queries nested `commonElements:actions.*`, `fields.*`, `search.panelTitle` but `commonElements.json` is entirely flat → these resolve to fallbacks. `AsyncSearchSection` tab labels use flat keys (correct); `ServiceListController` field labels use `fields.*` (broken). The two disagree.
5. **`extendDocSearchTabFieldMeta` uses `any`.** Generic infra should carry a typed options contract so business callers get checking.
6. **`AsyncSearchSection` re-renders fields inline instead of delegating to `AsyncField` (the fidelity root cause).** `AsyncSearchSection.tsx:106-147` reimplements text/RangePicker/Select from scratch and imports **none** of `AsyncField`/`AsyncEditUnion`/`InputFieldUnion` (verified — no import). Legacy had exactly **one** field renderer; the React side accidentally grew a second, thinner one that silently drops meta flags.

---

## 1b. Fidelity audit — builders faithful, render layer not (verified line-by-line)

The generic **meta builders** are a near-verbatim port and are faithful:

- `_getTemplateHeaderTabMeta`, `_getTemplateParentHeaderTabMeta`, `_getTemplateAccountContactTabMeta`, `_getTemplateCreateUpdateTabMeta`, `_getTemplateMaterialTabMeta`, `_getTemplateDocPartyTabMeta`, `_getTemplateDocFlowTabMeta`, `_getTemplateDocActionLogTabMeta`, `_getActionCodeKey` — all match legacy `ServiceUiController.js:1050-1409` field-for-field (paths, `labelKey`, `newRow`/`collapseAble`/`datetime`, `lowFieldName`/`highFieldName`, `helpKey`, the party/flow/actionLog loops, action-code mapping).
- Cosmetic-only diffs: icon fonts modernized (`md md-home`→`mdi mdi-home`, `ion-merge`→`mdi mdi-source-merge`); `AbsInput.FIELDTYPE.Select2`→`'select'`, `TextArea`→`'textarea'` (expected framework swap).

**Conclusion: the meta the form is built from is faithful. The form the user sees is not — the gap is entirely in the render layer.** Verified state of the meta flags:

| Meta flag | Read by ported `.tsx` field elements? | Honored in current search render? |
|---|---|---|
| `leftIcon` (docFlow/actionLog icons) | ✅ `AbsInputEle.tsx:99`, `TextAreaEle.tsx:68`, `TypeAheadEle.tsx:13`, `LabelEle.tsx` | ❌ dropped (inline renderer ignores it) |
| `collapseAble` (material tab) | ✅ `TextAreaEle.tsx:39` | ❌ dropped |
| `newRow` (layout breaks) | ⚠️ only a constant `AsyncSection.tsx:46` — no `.tsx` renderer honors it yet | ❌ dropped |
| select **options** (`getMetaDataUrl`) | (loader-dependent) | ❌ empty `options: []` |

So three of four fidelity flags are **already supported by `AsyncField`'s field elements** — they're lost only because `AsyncSearchSection` bypasses them. This is why the backbone of the fix is *delegation*, not re-implementing each flag in the search renderer.

---

## 2. Decisions required before implementation

These are genuine forks — see the accompanying questions. Defaults assumed below:
- **System A (`AsyncListPage`/`AsyncSearchSection`)** is the survivor (it's the routed legacy port and matches the meta-driven architecture in memory).
- **i18n:** nest the JSON to match code (`actions.*`, `fields.*`) rather than flatten the code.
- Select option-loading is **in scope** (a real search that can't filter by status is not shippable).

---

## 3. Implementation steps

### Step 0 — First checkpoint: compare-component pass (do before any edit)
**Tool:** `/compare-component` on legacy render path vs new `AsyncSearchSection`

Before touching code, run a focused compare so the plan rests on a verified render-layer diff, not this document's read:
- **Legacy:** the `AsyncSearchSection` / `AsyncField` render path in `admin/js/component/basicElements/AsyncPageElement.js` + `admin/js/component/control/AsyncControlElement.js` (how it turns `fieldMetaList` + `newRow`/`collapseAble`/`leftIcon`/`fieldType` into DOM).
- **New:** `src/components/page/AsyncSearchSection.tsx` (inline renderer) and the ported `src/components/control/AsyncField.tsx` + `*Ele.tsx` elements.
- **Output:** confirm exactly which meta flags the legacy renderer acts on, and confirm `AsyncField` already honors `leftIcon`/`collapseAble` (and whether it honors `newRow` — current read says no). Adjust Steps 3–4 if the compare contradicts the table in §1b.

This is a **gate**: Steps 3+ proceed only after the compare confirms the delegation target.

#### Step 0 RESULT (2026-07-14) — gate PASSED, target sharpened

Verified by reading `AsyncField.tsx`, `AsyncEditUnion.tsx`, `AsyncEditSection.tsx`, `InputFieldUnion.tsx`:

- **`InputFieldUnion` maps `fieldType` → real components** (`InputFieldUnion.tsx:49-57`): `Select`/`Select2`→`SelectField`, `TextArea`→`TextAreaField`, `Date`→`DateField`, etc. Delegating yields correct widgets for free; the inline renderer's text/RangePicker/empty-Select is strictly inferior.
- **`leftIcon` ✅ and `collapseAble` ✅** honored by field elements (confirmed §1b).
- **`newRow` ❌** honored nowhere in the `.tsx` path (only the constant `AsyncSection.tsx:46`); neither `AsyncField` nor `AsyncForeField` acts on it. Genuine gap in the **shared** renderer — fixing it in `AsyncField` benefits editors too.
- **Key finding:** `AsyncEditSection` **already** renders `embeddedTabMetaList` as `nav nav-pills` tabs AND **already routes `SectionCategory.SEARCH` tabs through `AsyncEditUnion` → `AsyncField`** (`AsyncEditSection.tsx:212-218` `checkForNormalSubSection` includes SEARCH; tab body `259-324`). So `AsyncSearchSection` is **duplicating an existing, more-correct path.**

**Consequence for Step 3:** the target is not "hand-wire `AsyncField` into the inline renderer" — it is "make `AsyncSearchSection` build its tab/field body via `AsyncEditUnion` (the same machinery `AsyncEditSection` uses for SEARCH tabs), keeping ONLY search-specific concerns: the Search/Reset buttons, the dateRange `[start,end]`→`Low/High` flatten, and `listTable` wiring." Step 3 rewritten accordingly below.

### Step 1 — Formalize the generic options contract (types only)
**File:** `src/controllers/ListPageTypes.ts` (add), `ServiceListController.ts:904` (apply)

Replace the `any` on `extendDocSearchTabFieldMeta` / `extendDocSearchFieldMeta` / `extendAccountSearchTabFieldMeta` with a typed options interface — this *is* the decoupled contract, named explicitly:

```ts
export interface DocSearchTabOptions {
  vm: unknown;
  helpKeyPrefix: string;
  nodeCategory?: string;
  docPartyIdList?: string[];
  docFlowIdList?: string[];
  docActionLogIdList?: string[];
  headerPostMetaList?: EmbeddedTabFieldMeta[];
  createUpdatePreMetaList?: EmbeddedTabFieldMeta[];
  materialPreMetaList?: EmbeddedTabFieldMeta[];
  headerPostTabMeta?: EmbeddedTabMeta[];
  createUpdatePostTabMeta?: EmbeddedTabMeta[];
  materialPostTabMeta?: EmbeddedTabMeta[];
}
```
No behavior change — locks down the seam so future doc types can only pass business config, never touch generic structure. **Migration Rule 2 compliant** (no new methods; only a type annotation on an existing faithful port).

### Step 2 — Consolidate on one search system
**Files:** `src/components/SearchPanel.tsx`, `src/components/ListPageShell.tsx`, `ListPageTypes.ts`

- Confirm no route imports `ListPageShell` (audit says none do).
- Move System B files to `src/components/_legacy-unused/` (quarantine, don't delete yet) OR delete if git history is the only reference needed. Keep `SearchFieldConfig`/`SearchTabConfig` types **only if** `buildSearchFields`/`buildSearchTabs` still parse into them for another consumer; otherwise remove those parsers too.
- Net: one search rendering path (`AsyncSearchSection`).

### Step 3 — Backbone: `AsyncSearchSection` builds its body via `AsyncEditUnion`
**Files:** `src/components/page/AsyncSearchSection.tsx`, `src/components/control/AsyncField.tsx`

Per the Step 0 gate result: `AsyncEditSection` already renders `embeddedTabMetaList` tabs and already routes `SectionCategory.SEARCH` tabs through `AsyncEditUnion` → `AsyncField` → `InputFieldUnion` (correct `fieldType` mapping, `leftIcon`, `collapseAble`). So `AsyncSearchSection`'s inline `renderField` (`AsyncSearchSection.tsx:106-147`) is a duplicate, thinner path.

Rewrite `AsyncSearchSection` to render each tab's `fieldMetaList` through **`AsyncEditUnion`** (the same machinery editors use), keeping ONLY search-specific concerns:
- the `Card` + tab-nav shell (or reuse the `nav nav-pills` pattern from `AsyncEditSection`),
- the Search/Reset buttons,
- the dateRange `[start,end]` → `{name}Low/High` flattening on submit,
- `pageMeta.listTable.searchModule / handleReset` wiring (already present, `AsyncSearchSection.tsx:70,93-102`).

Recovered *for free* by delegating (gate-verified): correct `fieldType` widgets (`Select`/`TextArea`/`Date`), `leftIcon`, `collapseAble`. Select **options** come via Step 4 (into `AsyncField`).

**`newRow` sub-task (real gap):** gate confirmed `newRow` is honored **nowhere** in the `.tsx` path (`AsyncSection.tsx:46` is only a constant; neither `AsyncField` nor `AsyncForeField` acts on it). Add `newRow` layout handling to the shared `AsyncField` grid logic so **every** meta-driven form (search AND editor) gains it — faithful to legacy `AsyncControlElement.js:3125-3126`. Do not special-case it in the search renderer.

> Note: `AsyncField` renders a Bootstrap `div.row` + `col-md-*` grid, not antd `Row/Col`. The search shell must live inside that grid convention (matches `AsyncEditSection`), not fight it — one reason to reuse `AsyncEditUnion` rather than keep antd `Row/Col` in the search renderer.

### Step 4 — Load select options from metadata URLs (into `AsyncField`, not the search renderer)
**Files:** `src/components/control/AsyncField.tsx` (or the Select element it renders), new/existing metadata loader

- Field meta carries `settings.getMetaDataUrl` (e.g. `getStatusURL`, `getPriorityCodeURL`, the docType map URL) + `formatMeta` + `addEmptyFlag` — all already in the ported meta.
- Implement option loading where `AsyncField` renders a `'select'`, so **both** search and editor selects populate. **Grep first** — reuse `SystemStandrdMetadataProxy.ts` / `ServicePageMetaProxy.ts` if they already fetch status/priority/docType maps; do not duplicate.
- Honor `addEmptyFlag` (legacy adds an empty option) and `formatMeta`. Cache per URL; render loading state until resolved.

### Step 5 — Close the search→table wiring
**Files:** `src/components/page/AsyncListPage.tsx`, verify against `ServiceListController.searchModule`

- `buildAsyncListPageMeta()` already exposes `listTable: this` (`ServiceListController.ts:403`); `AsyncSearchSection` already reads `pageMeta.listTable.searchModule/handleReset` (`AsyncSearchSection.tsx:70,93-102`).
- Verify the EMBEDLIST section (`AsyncEmbeddedListSection`) shares the same `actionRef` that `searchModule` reloads (`ServiceListController.ts:427`). If `AsyncListPage` must relay a shared `actionRef` between the SEARCH and EMBEDLIST sections, implement that relay here — the one place `AsyncListPage` legitimately extends `AsyncPage`. Remove the stub comment (`AsyncListPage.tsx:6-8`) once wired.

### Step 6 — Fix i18n key shapes
**Files:** `src/i18n/locales/{en,zh}/foundation/commonElements.json`

- **Nest the JSON** (decision confirmed): add `actions.{search,reset,newModule,...}`, `fields.{id,name,status,...}`, `search.panelTitle` objects to match what the code already queries. Keep existing flat keys during transition (both resolve).
- Verify `AsyncSearchSection` tab labels (`commonElements:basicSection` etc., flat — already present) still resolve.
- Add PurchaseContract search-specific keys if missing (`signDate`, `requireExecutionDate` labels exist; check tab help keys).

### Step 7 — Verify end-to-end on the routed PurchaseContract page
**Route:** `/logistics/purchaseContract`
- Search form renders all 6 tabs (basic + signDate/requireExecutionDate, createUpdate, party×2, docFlow×4, actionLog×3, material).
- **Fidelity checks (the point of the revision):** `leftIcon` icons appear on docFlow/actionLog fields; material textareas are collapsible; `newRow` fields break to a new row — matching legacy layout.
- Status/priority/docType selects populate.
- Entering a value + Search reloads the ProTable with `panelParams` merged (`ServiceListController.ts:499` / `PurchaseContractListController.tsx:242`). Reset clears and reloads.
- Confirm the 5-vs-4 doc-flow asymmetry is preserved (searchContent had 5 flow slots incl. `prevDoc`; form renders 4 — legacy-faithful).

---

## 4. What this plan explicitly does NOT do

- **Does not** add methods/business logic to migrated classes (Migration Rule 2).
- **Does not** rebuild the generic tab builders — they're already faithful ports.
- **Does not** touch the EMBEDLIST column logic beyond confirming shared `actionRef`.
- **Does not** invent a new "generic search framework" — the framework exists; we type it, wire it, and de-duplicate it.
- **Does not** add fidelity flags (`newRow`/`collapseAble`/`leftIcon`/options) to the search renderer specifically — they are recovered by delegating to `AsyncField`, so search AND editor forms benefit equally.

---

## 5. File-change summary (planned)

| File | Change |
|---|---|
| — (Step 0) | `/compare-component` gate — legacy render path vs `AsyncSearchSection`/`AsyncField`; no edit |
| `src/controllers/ListPageTypes.ts` | + `DocSearchTabOptions` interface |
| `src/controllers/ServiceListController.ts` | Type the 3 `extend*SearchTabFieldMeta` signatures; fix `fields.*` i18n key |
| `src/components/page/AsyncSearchSection.tsx` | **Backbone:** replace inline `renderField` with delegation to `AsyncField`; keep only Card+Tabs shell, buttons, dateRange flatten |
| `src/components/control/AsyncField.tsx` (+ `AsyncEditUnion`/Select element) | Honor `newRow` in shared grid; load select options from `getMetaDataUrl` (honor `addEmptyFlag`/`formatMeta`) |
| metadata loader | Reuse `SystemStandrdMetadataProxy.ts`/`ServicePageMetaProxy.ts` if present; else add — **grep first** |
| `src/components/page/AsyncListPage.tsx` | Wire shared `actionRef` relay; remove stub comment |
| `src/components/SearchPanel.tsx`, `ListPageShell.tsx` | Quarantine/remove (System B) |
| `src/i18n/locales/{en,zh}/foundation/commonElements.json` | Nest `actions.*`, `fields.*`, `search.*` |
| `PurchaseContractListController.tsx` | No change — already correct business config |

---

## 6. Confirmed decisions
- Survivor: **System A** (`AsyncListPage`/`AsyncSearchSection`).
- i18n: **nest the JSON** to match code.
- Select option-loading: **in scope**.
- Backbone: **`AsyncSearchSection` delegates to `AsyncField`** (not re-implementing flags in the search renderer).
- Step 0 **compare-component pass is the first implementation checkpoint** (a gate before any edit).
