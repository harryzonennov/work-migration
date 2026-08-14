# Material UI Migration Plan

**Date:** 2026-08-12
**Status:** Plan — awaiting approval

---

## 1. Goal

Migrate the **Material** master-data UI (list + editor + unit-item editor + quick-edit panel) into the new React `IntelligentUI`, reusing the Document UI framework — the same pattern as PurchaseContract/PurchaseRequest.

Material is **not** a `DocumentContent` in the backend — it's a plain `ServiceEntityNode`. But it still has **status**, **action codes** (activate/archive lifecycle), an **item-level node** (MaterialUnit), and **attachments** — so it rides the same Document UI framework. This is the "dummy document" case: the framework treats it as a document even though the backend entity isn't one.

---

## 2. Scope decision — Material only (not the whole family)

The legacy Material family is **several independent flat ServiceEntities**, not one hierarchy:

| Entity | Item node | Backend | Relationship |
|---|---|---|---|
| **Material** (master) | MaterialUnit | `/material` | root |
| **MaterialStockKeepUnit** (SKU) | MaterialSKUUnit | `/materialStockKeepUnit` | references Material via `refMaterialUUID` (FK, not child) |
| MaterialType | — | `/materialType` | referenced by Material |
| MaterialConfigureTemplate | — | — | separate |
| StandardMaterialUnit | — | — | separate |

**This plan covers `Material` (+ its MaterialUnit item) only.** MaterialStockKeepUnit is a parallel sibling that can follow as a second migration using the same recipe — I flag it but keep it out of scope to keep this reviewable. (The Material editor's 4th tab is a read-only SKU cross-list; I'll stub it initially, since it depends on the SKU entity.)

---

## 3. Readiness confirmation

| Area | Status |
|---|---|
| **Backend entity** | ✅ `Material.java` extends `ServiceEntityNode` (borrows `DocumentContent.STATUS_*` constants only) |
| **Backend controllers** | ✅ `MaterialEditorController` + `MaterialListController`, both `@RequestMapping("/material")` |
| **Backend endpoints** | ✅ Full doc-action surface: `searchTableService`, `loadModuleEditService`/`ViewService`/`loadModule`, `saveModuleService`, `newModuleService`, `executeDocAction`, `getActionCodeMap`, `getDocActionConfigureList`, `getDocActionNodeList`, attachments, `preLock`, + enum endpoints (`/getStatus`, `/getSupplyType`, `/getMaterialCategory`, `/getOperationMode`, `/getCargoType`, `/getStandardUnit`) |
| **⚠️ Status endpoint** | Material serves status via **`/material/getStatus`**, NOT `getStatusMap`. Field select `getMetaDataUrl` must point at `material/getStatus`. |
| **UIModel** | ✅ `MaterialUIModel.java` (589 lines, ~55 fields), `MaterialServiceUIModel.java` (action nodes: `activeBy, approvedBy, submittedBy, reInitBy, archivedBy`) |
| **`DocumentConstants` (FE)** | ✅ `ServiceModuleConstants.Material`, `DummyDocumentType.Material` (string `'Material'`), and a full `Material` block (status incl. ACTIVE, operationMode, supplyType, materialCategory) — **no edit needed** |
| **Action codes (FE)** | ✅ `ACTION_ACTIVE` (305), `ACTION_REINIT` (350), `ACTION_ARCHIVE` (980), plus standard submit/approve — all in `SystemDefDocActionCodeProxy` |
| **Status icons (FE)** | ✅ `statusIcon.ACTIVE`, `.ARCHIVED`, `.INITIAL`, `.APPROVED`, `.SUBMITTED` all present |
| **Factory scaffolding** | ✅ `DocumentManagerFactory` already routes `DummyDocumentType.Material` → `MaterialManager` (currently a `declare const … : any` stub to be replaced with the real import) + icon config |

**Conclusion:** Backend ready; the FE constants/enums/factory scaffolding already exist. Pure frontend migration.

---

## 4. Key deltas: Material vs PurchaseContract/PurchaseRequest

| # | Aspect | PurchaseContract/Request | **Material** |
|---|---|---|---|
| 1 | Backend base | `DocumentContent` | **`ServiceEntityNode`** (dummy document) |
| 2 | Doc type | numeric (20 / 34) | **string `'Material'`** (`DummyDocumentType.Material`) |
| 3 | Root model key | `purchaseContractUIModel` | `materialUIModel` |
| 4 | Item model key | `purchaseContractMaterialItemUIModel` | **`materialUnitUIModel`** (item node = MaterialUnit) |
| 5 | Action codes | submit/approve/deliver | **submit/approve/countApprove/reInit/active/archive** (master-data lifecycle) |
| 6 | Statuses | +DELIVERY/PROCESS_DONE | **INIT→SUBMITTED→APPROVED→ACTIVE→ARCHIVED** |
| 7 | Involve-party sections | supplier + org (2 CUSTOMERCONTACT sections) | **NONE** — supplier is a flat scalar field (`mainSupplierName`) |
| 8 | Status endpoint | `getStatusMap` | **`getStatus`** |
| 9 | Node inst ids | purchaseContract / …MaterialItem | **material / materialUnit** |
| 10 | i18n namespace / path | purchaseContract / supplyChain | **material / coreFunction** |
| 11 | Editor tabs | basic / details(or attach) / items | **basic+size+attach / production+price / units(items) / SKU-list(stub)** |
| 12 | Domain metadata | priority icons | materialCategory / supplyType / cargoType / operationMode icon arrays |

The **biggest simplification**: no involve-party sections (no supplier/org CUSTOMERCONTACT). The **biggest additions**: the ACTIVE/REINIT/ARCHIVE lifecycle actions and several domain enum fields.

---

## 5. Files to CREATE (Material-specific)

### 5A. Types & i18n
| # | File | Mirrors | Notes |
|---|---|---|---|
| 1 | `src/types/platform/MaterialContent.ts` | `logistics/PurchaseRequestContent.ts` | `MaterialUIModel` (materialCategory, supplyType, operationMode, cargoType, price fields, `mainSupplierName`, dimensions), `MaterialUnitUIModel`, `MaterialUnitServiceUIModel`, `MaterialServiceUIModel` (action nodes: activeBy/approvedBy/submittedBy/reInitBy/archivedBy) |
| 2 | `src/i18n/locales/en/coreFunction/Material.json` | `supplyChain/PurchaseRequest.json` | decode `admin/i18n/coreFunction/Material_en.properties` + `MaterialUnit_en.properties` |
| 3 | `src/i18n/locales/zh/coreFunction/Material.json` | same | Chinese labels |

Note: `coreFunction/` is a new locale sub-folder (matches legacy `i18nPath: 'coreFunction/'`).

### 5B. Service
| # | File | Mirrors | Notes |
|---|---|---|---|
| 4 | `src/services/platform/MaterialManager.ts` | `logistics/PurchaseRequestManager.ts` | node inst ids `material`/`materialUnit`; `getDocumentType → DummyDocumentType.Material` (string); `getResourceId → ServiceModuleConstants.Material`; DOC_ACTION_CODE = submit/approve/countApprove/reInit/active/archive; status icon array (INIT/APPROVED/ACTIVE/SUBMITTED/ARCHIVED); i18n bundle registration for `material` namespace; **status URL = `material/getStatus`** |

New folder `src/services/platform/` (Material is a platform entity, not logistics).

### 5C. Pages — `src/pages/platform/material/`
| # | File | Mirrors | Notes |
|---|---|---|---|
| 5 | `MaterialListController.tsx` | `PurchaseRequestListController.tsx` | searchContent (headerModel, createdUpdate, materialType, materialCategory, mainSupplierName, packageStandard, action nodes submittedBy/approvedBy/activeBy/reInitBy/archivedBy); columns id/name/status/materialCategory/materialTypeName/packageStandard; docPopConfig → Material / MaterialType |
| 6 | `MaterialListPage.tsx` | `PurchaseRequestListPage.tsx` | rename |
| 7 | `useMaterialListController.ts` | `usePurchaseRequestListController.ts` | rename |
| 8 | `MaterialEditController.tsx` | `PurchaseRequestEditController.tsx` | **NO involve-party sections**; 4 tabs (basic+size+attachment / production+price / units embed-list / SKU stub); getActionCodeMatrix = submit/revokeSubmit/approve/reInit/rejectApprove/countApprove/active/archive; status field select → `material/getStatus` |
| 9 | `MaterialEditPage.tsx` | `PurchaseRequestEditPage.tsx` | rename |
| 10 | `useMaterialEditController.ts` | `usePurchaseRequestEditController.ts` | rightBar help `MaterialHelpDocument`, i18nPath `coreFunction`, panel `materialUnitPanel` |
| 11 | `MaterialUnitController.tsx` | `PurchaseRequestMaterialItemController.tsx` | `getItemUIModelKey → materialUnitUIModel`; item fields unitName/ratioToStandard/retailPrice/netWeight/note |
| 12 | `MaterialUnitEditPage.tsx` | `PurchaseRequestItemEditPage.tsx` | rename |
| 13 | `useMaterialUnitController.ts` | `usePurchaseRequestMaterialItemController.ts` | `loadDocMatItem` → `materialUnit`; item rightBar |
| 14 | `MaterialUnitPanel.tsx` | `PurchaseRequestMaterialItemPanel.tsx` | quick-edit panel; getEditPageURL `/platform/materialUnit/...` |

**Note:** Material's item node is `materialUnit` but the backend serves it under the `/materialUnit` prefix via `getItemNodeInstId`. Confirm `materialUnit` load/save endpoints exist during implementation (the SKU controller had `/getMaterialSKUUnitList`; Material's unit endpoints need verification — if absent, the unit editor is view-only initially).

### 5D. Cross-doc select
Material is a **popover target** (dummy type), not a cross-doc source/target for batch generation — so **no `SelectInput`/`MultiSelect` components needed** (unlike PurchaseContract). The popover config lives in the Manager's `getDocumentPopoverContent()`.

---

## 6. Files to EDIT (shared registration)

| # | File | Edit |
|---|---|---|
| 15 | `src/router/index.tsx` | 3 imports + 4 routes: `platform/material`, `/new`, `/:uuid/edit`, `platform/materialUnit/:uuid/edit` |
| 16 | `src/router/menuConfig.ts` | Add a Material menu entry (e.g. under a "Master Data" / systemResource group → `/platform/material`) |
| 17 | `src/i18n/locales/en/Menu.json`, `zh/Menu.json` | Add `material` / `materialList` keys |
| 18 | `src/services/DocumentManagerFactory.ts` | Replace `declare const MaterialManager: any;` stub with real import; keep the two existing routing lines (they already reference `MaterialManager`); icon config already present |

**No edit needed:** `DocumentConstants.ts` — Material SEName, DummyDocumentType, status/enum blocks already present.

---

## 7. Shared framework — reused AS-IS (zero changes)

Identical reuse list to PurchaseRequest: `ServiceListController`, `DocumentEditController`, `ServiceEditController`, `DocItemEditController`, `ServiceManager`, all composables (`useDocumentEditController`, `useDocumentListController`, `useItemEditController`, `useServiceEntityEditController`), all page shells (`DocumentEditPage`, `AsyncListPage`, `AsyncEditorPage`, `EditPanel`, `AsyncAttachmentSection`, `AsyncEmbeddedListSection`), `DocActionModalController`, and the error-handling / full-model-save fixes. Material inherits all of it by subclassing.

**Confirms the "dummy document" thesis:** because Material rides `DocumentEditController` / `ServiceEditController` (which were deliberately built to support non-DocumentContent entities — see the `ServiceEditController.ts:763` comment "dummy document editors e.g. CorporateSupplier, Material"), Material needs the **same 14 thin files + 4 edits** as a real document. No framework changes.

---

## 8. Summary counts

| Category | Count |
|---|---|
| **New files** | **14** (1 type + 2 i18n + 1 manager + 10 pages) |
| **Shared files edited** | **4** (router, menuConfig, 2 Menu.json, DocumentManagerFactory) |
| **Cross-doc select components** | **0** (popover-only, not a batch source/target) |
| **Backend changes** | **0** |

Slightly fewer than PurchaseRequest (16) because Material has no cross-doc select components.

---

## 9. Implementation order

1. **Types** — `MaterialContent.ts`
2. **i18n** — `Material.json` (en + zh) under new `coreFunction/` folder
3. **Manager** — `MaterialManager.ts` (registers i18n, status/action/icon arrays, `getStatus` URL)
4. **List** — controller + page + hook → verify search + table against `/material/searchTableService`
5. **Editor** — controller + page + hook → verify load/save/activate/archive; **no involve-party sections**
6. **Unit item + panel** — controller + page + hook + panel (verify `/materialUnit` endpoints; view-only fallback if save absent)
7. **Registration** — router, menu, Menu.json, DocumentManagerFactory (replace stub with real import)
8. **Verify** — `tsc --noEmit`, run app, exercise list search / editor save / activate action / unit quick-edit

---

## 10. Risks / watch-items

1. **Status endpoint is `/getStatus`** not `getStatusMap` — the status field's `getMetaDataUrl` and any status-map fetch must use `material/getStatus`. Verify the response shape matches what SelectField expects (`{id, text}` list).
2. **ACTIVE / REINIT actions** are the Material-specific lifecycle. Wire them into `getActionCodeMatrix()` (`active`, `reInit`). They already exist as `ACTION_ACTIVE`/`ACTION_REINIT` constants.
3. **No involve-party** — do NOT copy the two `CUSTOMERCONTACT` sections from the PurchaseContract editor. Material's supplier is a flat text field. This is the main structural divergence.
4. **SKU cross-list tab (tab 4)** depends on the MaterialStockKeepUnit entity (out of scope). Stub it (empty section or omit the tab) until SKU is migrated.
5. **MaterialUnit item endpoints** — the backend research confirmed Material's editor controller but did not enumerate a standalone `/materialUnit` editor controller. If MaterialUnit has no independent load/save endpoint, the unit item is edited inline in the embed-list only (no full-page item editor) — adjust files #11-14 accordingly (may reduce to just the inline embed + panel).
6. **New folder conventions** — `src/services/platform/`, `src/pages/platform/material/`, `src/i18n/locales/{en,zh}/coreFunction/` are new. Confirm no build/alias config needs updating (Vite `@/` alias covers `src/**`, so fine).
7. **Domain enum fields** (materialCategory, supplyType, cargoType, operationMode) — render as selects with static options from `DocumentConstants.Material.*`, OR fetch from the enum endpoints (`/getMaterialCategory` etc.). Prefer static options from the already-present `DocumentConstants.Material` block (simpler, matches how PurchaseContract handles priority via a static icon array).

---

## 11. Open question for confirmation

**Scope:** This plan migrates **Material + MaterialUnit** only. Do you want:
- (a) Material only now (SKU tab stubbed), MaterialStockKeepUnit as a follow-up? — **recommended**, keeps it reviewable
- (b) Material + MaterialStockKeepUnit together (roughly doubles the file count: +14 files for the SKU family)?
