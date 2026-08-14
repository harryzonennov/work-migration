# StandardMaterialUnit UI Migration Plan

**Date:** 2026-08-13  
**Status:** Plan — awaiting approval

---

## 1. What is StandardMaterialUnit?

A **plain ServiceEntity** — a simple CRUD master-data record with:
- No workflow actions, no status lifecycle, no `DOC_ACTION_CODE`
- No embedded child list (no item-level node)
- A flat two-section editor: unit definition fields + a reference-unit FK lookup section
- Three backend enum dropdown maps: `getUnitTypeMap`, `getUnitCategoryMap`, `getSystemCategoryMap`

This is simpler than Material (dummy document) — it needs no `DocItemEditController`, no item panel, no `DocumentEditController`. It uses a **plain `ServiceEditController`** directly (the base of the document controller hierarchy).

**Legacy base:** `ServiceEditorControlHelper.defControlMinxin` (NOT `SerDocumentControlHelper`) — confirmed plain service entity, not a document.

---

## 2. Backend readiness

All endpoints confirmed present under `@RequestMapping("/standardMaterialUnit")`:

| Endpoint | Purpose |
|---|---|
| `searchTableService` (POST) | List page data |
| `loadModuleListService` (GET) | Dropdown list (for reference unit selector) |
| `loadModuleEditService` (GET ?uuid) | Editor load |
| `loadModuleViewService` (GET ?uuid) | Popover/view load |
| `saveModuleService` (POST) | Save |
| `newModuleService` (POST) | Create new (defaults: systemCategory=TRADING) |
| `exitEditor` (POST) | Release edit lock |
| `getUnitTypeMap` | Enum dropdown: STANDARD/SELF/SYSTEM |
| `getUnitCategoryMap` | Enum dropdown: PACKAGE/WEIGHT/VOLUME/LENGTH |
| `getSystemCategoryMap` | Enum dropdown: PHYSICAL/TRADING |

**No** `executeDocAction`, `getDocActionConfigureList`, `getStatusMap`. Zero workflow.

---

## 3. Existing FE scaffolding (no edit needed)

`DocumentConstants.ts` already has:
- `ServiceModuleConstants.StandardMaterialUnit` = `'StandardMaterialUnit'`
- `DummyDocumentType.StandardMaterialUnit` = `ServiceModuleConstants.StandardMaterialUnit`
- `DocumentConstants.StandardMaterialUnit.unitCategory` = `{ PACKAGE:1, WEIGHT:2, VOLUME:3, LENGTH:4 }`

`DocumentManagerFactory.ts` already has:
- `declare const StandardMaterialUnitManager: any` stub (line ~65) → replace with real import
- Lookup lines already wired at lines ~457 and ~571

**Missing from `DocumentConstants.ts`** — add to the `StandardMaterialUnit` block:
```ts
StandardMaterialUnit: {
    unitCategory: { PACKAGE: 1, WEIGHT: 2, VOLUME: 3, LENGTH: 4 },  // ← already present
    unitType:     { STANDARD: 1, SELF: 2, SYSTEM: 3 },               // ← add
    systemCategory: { PHYSICAL: 1, TRADING: 2 },                      // ← add
},
```

---

## 4. Fields (from `StandardMaterialUnitUIModel.java`)

| Field | Type | Notes |
|---|---|---|
| `id` | string | required, unique (duplicate check on save) |
| `name` | string | required |
| `note` | string | textarea |
| `languageCode` | string | |
| `unitType` | int | enum: `getUnitTypeMap` → STANDARD/SELF/SYSTEM |
| `unitTypeValue` | string | display value (auto-populated) |
| `unitCategory` | int | enum: `getUnitCategoryMap` → PACKAGE/WEIGHT/VOLUME/LENGTH |
| `unitCategoryValue` | string | display value |
| `systemCategory` | int | enum: `getSystemCategoryMap` → PHYSICAL/TRADING |
| `systemCategoryValue` | string | display value |
| `referUnitUUID` | string | FK to another StandardMaterialUnit |
| `refMaterialUnitId` | string | display: populated via FK lookup, read-only |
| `refMaterialUnitName` | string | display: populated via FK lookup, read-only |
| `toReferUnitFactor` | double | conversion factor to the reference unit |
| `toReferUnitOffset` | double | conversion offset |

**ServiceUIModel wrapper:**
```ts
interface StandardMaterialUnitServiceUIModel {
    standardMaterialUnitUIModel: StandardMaterialUnitUIModel;
    serviceUIMeta: Record<string, unknown>;
}
```
Note: **no** `MaterialItemUIModelList`, **no** action nodes, **no** involve-party — the thinnest possible wrapper.

---

## 5. Editor sections (from `StandardMaterialUnitControl.js`)

**Tab 1 (only tab):**

**Section 1 — `standardMaterialUnitSection`** (parentContentPath: `standardMaterialUnitUIModel`)
- `systemCategory` — select, `getSystemCategoryMap`
- `unitCategory` — select, `getUnitCategoryMap`
- `unitType` — select, `getUnitTypeMap`
- `id` — text, required
- `name` — text, required
- `languageCode` — text
- `note` — textarea, 5 rows

**Section 2 — `refMaterialUnitSection`** (parentContentPath: `standardMaterialUnitUIModel`)
- `refMaterialUnitId` — select (loads from `standardMaterialUnit/loadModuleListService`), populates `referUnitUUID`
- `refMaterialUnitName` — text, read-only (auto-populated)
- `toReferUnitFactor` — number
- `toReferUnitOffset` — number

**Process buttons:** `exit` + `save` (formatClass: `displayForEdit`) only — no workflow buttons.

---

## 6. Architecture: which controller base to use

StandardMaterialUnit does NOT need `DocumentEditController` — it uses a **plain `ServiceEditController`** directly.

This means:
- **No** `useDocumentEditController` hook (that hook is for documents with action-configure lists and item data sources)
- **No** `DocItemEditController`
- **No** `EditPanel`
- Use **`useServiceEntityEditController`** directly (the shared core), wrapped in a thin doc-specific hook

The `ServiceEditController` already provides `handleFinish` → `saveDocument`, `exitModule`, `saveModule`, `buildInitialValues`, `serializeForm`, and `refreshEditView` — everything StandardMaterialUnit needs.

The main difference from a full document: `loadActionConfigureList` is omitted, `extractItems` is omitted, and `buildPayload` is simpler (no item list to merge).

---

## 7. Files to CREATE (10 files)

### 7A. Types + i18n + Manager

| # | File | Notes |
|---|---|---|
| 1 | `src/types/platform/StandardMaterialUnitContent.ts` | `StandardMaterialUnitUIModel` (15 fields above), `StandardMaterialUnitServiceUIModel` (wrapper, no item list) |
| 2 | `src/i18n/locales/en/coreFunction/StandardMaterialUnit.json` | English labels |
| 3 | `src/i18n/locales/zh/coreFunction/StandardMaterialUnit.json` | Chinese labels from decoded `.properties` |
| 4 | `src/services/platform/StandardMaterialUnitManager.ts` | Node inst id `standardMaterialUnit`, **no** `getDocumentType()` numeric return needed (use `getDummyDocumentType()` only), **no** DOC_ACTION_CODE, **no** `getStatusIconArray()`, three enum URL methods, i18n registration |

### 7B. Pages — `src/pages/platform/standardMaterialUnit/`

| # | File | Notes |
|---|---|---|
| 5 | `StandardMaterialUnitListController.tsx` | Search fields: id/name/unitType/unitCategory/systemCategory; columns: name(docPop)/id/unitTypeValue/unitCategoryValue/systemCategoryValue/refMaterialUnitName |
| 6 | `StandardMaterialUnitListPage.tsx` | Pure rename of pattern |
| 7 | `useSMUListController.ts` | Hook wrapper |
| 8 | `StandardMaterialUnitEditController.tsx` | Extends `ServiceEditController` directly (NOT DocumentEditController); two sections; process buttons exit+save only; no action code matrix |
| 9 | `StandardMaterialUnitEditPage.tsx` | Uses `AsyncEditorPage` (not `DocumentEditPage` — no panel needed) |
| 10 | `useSMUEditController.ts` | Uses `useServiceEntityEditController` directly (not `useDocumentEditController`) |

**No item editor, no panel, no cross-doc select components.**

---

## 8. Files to EDIT (4 shared files)

| # | File | Edit |
|---|---|---|
| 11 | `src/services/DocumentConstants.ts` | Add `unitType` and `systemCategory` to the `StandardMaterialUnit` block |
| 12 | `src/router/index.tsx` | 2 imports + 3 routes: list / new / :uuid/edit |
| 13 | `src/router/menuConfig.ts` | Add under "Master Data" group |
| 14 | `src/services/DocumentManagerFactory.ts` | Replace `declare const StandardMaterialUnitManager: any` stub with real import + `as unknown as DocumentManagerInstance` casts |

**No `Menu.json` changes if the menu key already exists** (check first).

---

## 9. Key architectural differences vs Material/PurchaseRequest

| Aspect | PurchaseRequest / Material | StandardMaterialUnit |
|---|---|---|
| Controller base | `DocumentEditController` | **`ServiceEditController` directly** |
| Hook | `useDocumentEditController` | **`useServiceEntityEditController`** |
| Page shell | `DocumentEditPage` (has panel slot) | **`AsyncEditorPage`** (simpler) |
| `loadActionConfigureList` | Yes (for workflow buttons) | **No** |
| `extractItems` | Yes (for item list) | **No** |
| `buildPayload` | Merges item list + attachments | **Just the UIModel wrapper** |
| Process buttons | exit + DOC_ACTION_BTN + save | **exit + save only** |
| Right sidebar | actionLog tab | **Minimal or none** |
| `ServiceUIModel` wrapper | Many sub-models | **Single `standardMaterialUnitUIModel` field** |
| Files created | 16 | **10** |
| Backend changes | 0 | **0** |

---

## 10. Implementation order

1. Add `unitType` + `systemCategory` to `DocumentConstants.ts` `StandardMaterialUnit` block
2. Create `StandardMaterialUnitContent.ts` (types)
3. Create `StandardMaterialUnit.json` i18n (en + zh)
4. Create `StandardMaterialUnitManager.ts`
5. Create List (controller + page + hook)
6. Create Editor (controller + page + hook)
7. Register: router + menuConfig + DocumentManagerFactory
8. `tsc --noEmit` + verify in browser

---

## 11. Risks / watch-items

1. **`refMaterialUnitId` select field** — the legacy editor uses a Select2 that loads from `standardMaterialUnit/loadModuleListService` and populates `referUnitUUID` on selection. The new UI equivalent is a select field with `settings.getMetaDataUrl`. The FK write-back (`referUnitUUID`) needs to be handled via `onValuesChange` in the edit controller or via a field-level `postUpdate` callback.

2. **No `Menu.json` edit if key exists** — check `en/Menu.json` and `zh/Menu.json` for existing `standardMaterialUnit` / `smuList` keys before adding.

3. **`useServiceEntityEditController` has no `loadActionConfigureList`** — confirm the hook signature accepts `undefined` for the `loadExtra` field (it does — `loadExtra` is optional).

4. **`newModuleService` defaults** — the backend sets `systemCategory = TRADING` on new records. The `buildCreateDefaults()` method should reflect this so the form pre-selects the correct option: `{ standardMaterialUnitUIModel: { systemCategory: DocumentConstants.StandardMaterialUnit.systemCategory.TRADING } }`.

5. **No `DocumentManagerFactory` edit needed for lookup** if only the `DummyDocumentType` string path is used (lines ~457, ~571 already reference the stub). Replacing the stub with the real import covers both.

6. **⚠️ CRITICAL — searchContent must exactly match the backend SearchModel fields (confirmed in implementation)**

   Before writing `searchContent`, always read the backend `*SearchModel.java` to confirm which fields it accepts. For plain ServiceEntity entities:
   - The backend SearchModel has **only flat fields** — no sub-objects
   - Sending `headerModel` or `createdUpdateModel` sub-objects causes `UnrecognizedPropertyException` on the backend
   - **Do NOT use `ServiceUIConstants.getDocSearchHeaderModel()` or `ServiceUIConstants.getCreateUpdateSearchModel()`** for plain ServiceEntity list controllers

   The correct `searchContent` for StandardMaterialUnit (matching `StandardMaterialUnitSearchModel.java`):
   ```ts
   readonly searchContent = { id: '', name: '', unitType: '', unitCategory: '', systemCategory: '' };
   ```

   **⚠️ IMPORTANT — flat fieldNames apply to BOTH search section AND list columns for plain ServiceEntityNode:**

   For plain ServiceEntityNode, the backend `searchTableService` returns rows as flat UIModel fields (no wrapper object). So BOTH the search tab and the list column `fieldName` values must be flat:
   ```ts
   // CORRECT for both search fields AND list columns — all flat
   { fieldName: 'id', labelKey: 'id' }
   { fieldName: 'name', labelKey: 'name' }
   { fieldName: 'unitTypeValue', labelKey: 'unitType' }
   // WRONG — the backend does not return a wrapper sub-object
   { fieldName: 'standardMaterialUnitUIModel.name' }
   ```
   Same for `rowKey`, `getRowId`, `docPopConfig.uuidFieldName` — all use the flat field name directly (`'uuid'`, not `'standardMaterialUnitUIModel.uuid'`).

   **Contrast with standard documents / dummy documents:** Their `searchTableService` returns wrapped rows (`{ purchaseContractUIModel: {...}, purchaseFromSupplierUIModel: {...} }`), so their list columns correctly use dot-path `fieldName` values like `'purchaseContractUIModel.name'`.

7. **List `request()` must have try/catch with message bar** — if the backend returns an error (e.g. because of a bad searchContent shape), the list page should show the error in the message bar instead of silently failing. Always wrap the `listDocuments` call:
   ```ts
   request = async (params) => {
       try {
           const result = await Manager.listDocuments({ content, ... });
           return { data: result.data, success: true, total: result.total };
       } catch (err) {
           pushErrorMessageBar(err instanceof Error ? err.message : String(err), { context: 'list-error' });
           return { data: [], success: false, total: 0 };
       }
   };
   ```
