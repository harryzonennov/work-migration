# PurchaseRequest UI Migration Plan

**Date:** 2026-08-10
**Author:** Migration (Architect)
**Status:** Plan — awaiting approval

---

## 1. Goal

Migrate the **PurchaseRequest** document (list + editor + material-item editor + quick-edit panel + cross-doc select) into the new React `IntelligentUI` project, mirroring **exactly** the structure already built for **PurchaseContract**.

The heavy lifting lives in the shared framework (base controllers, hooks, page shells, `ServiceManager`). PurchaseRequest needs only its own thin per-document files plus a handful of shared-registration edits — the same 1:1 pattern PurchaseContract follows.

---

## 2. Readiness confirmation

| Area | Status |
|---|---|
| **Backend entities** | ✅ `PurchaseRequest.java`, `PurchaseRequestMaterialItem.java` + party/attachment/action-node |
| **Backend services** | ✅ `PurchaseRequestManager.java`, `PurchaseRequestMaterialItemManager.java` + proxies |
| **Backend controllers** | ✅ Split into 4 (List/Editor/ItemList/ItemEditor), base paths `/purchaseRequest` + `/purchaseRequestMaterialItem` |
| **Backend UIModels** | ✅ `PurchaseRequestServiceUIModel`, `PurchaseRequestUIModel`, `PurchaseRequestMaterialItem*UIModel` |
| **Endpoints** | ✅ All FE-expected URLs present (searchTableService, loadModuleEditService, saveModuleService, getStatusMap, getPriorityMap, getDocActionConfigureList, executeDocAction, newModuleService …) |
| **`DocumentConstants` (FE)** | ✅ Already has `PurchaseRequest` SEName, `DocumentType.PURCHASEREQUEST = 34`, and a `PurchaseRequest.status` block **incl. INPROCESS** — no edit needed |
| **Backend gap** | ⚠️ `/purchaseRequest` has **no** `/getItemStatusMap` or `/getActionCodeMap` (PurchaseContract has them). Only relevant if the item status select needs a runtime map — mitigate by reusing the status icon array in the Manager (static), matching how PC item status renders. |

**Conclusion:** Backend is ready. This is a pure frontend migration.

---

## 3. Key deltas: PurchaseRequest vs PurchaseContract

These are the ONLY substantive differences to carry into the port. Everything else is a mechanical `PurchaseContract`→`PurchaseRequest` rename.

| # | Aspect | PurchaseContract | PurchaseRequest |
|---|---|---|---|
| 1 | **Doc type constant** | `PURCHASECONTRACT = 20` | `PURCHASEREQUEST = 34` (already in DocumentConstants) |
| 2 | **Root model key** | `purchaseContractUIModel` | `purchaseRequestUIModel` |
| 3 | **Item model key** | `purchaseContractMaterialItemUIModel` | `purchaseRequestMaterialItemUIModel` |
| 4 | **Root fields** | `signDate, requireExecutionDate, currencyCode, contractDetails, refFinAccount*` | `taxRate, planExecutionDate` (no currency/signDate/contractDetails) |
| 5 | **Action codes** | no INPROCESS; has `deliveryDoneBy` | **adds `INPROCESS`** + `inProcessBy` action node |
| 6 | **Statuses** | +PROCESS_DONE | +INPROCESS, +CANCELED, +ARCHIVED |
| 7 | **Editor tabs** | `purchaseContractSection / purchaseContractDetailsSection / purchaseContractMaterialItemSection` | `purchaseRequestSection / refDocumentSection (attachment) / purchaseRequestMaterialItemSection` |
| 8 | **Search date fields** | `signDateLow/High`, `requireExecutionDateLow/High` | `planExecutionDateLow/High` |
| 9 | **Search action nodes** | `submittedBy, approvedBy, deliveryDoneBy` | `submittedBy, approvedBy, inProcessBy` |
| 10 | **Search extras** | `reservedByDoc` | (none) |
| 11 | **Cross-doc create** | from Request / Inquiry (`newFromRequest`/`newFromInquiry`) | creates PurchaseContract *from* Request (`newPurchaseContract`); item-level `mergePurchaseBatch` |
| 12 | **i18n namespace** | `purchaseContract` | `purchaseRequest` |
| 13 | **URL prefix / node inst id** | `purchaseContract` / `purchaseContractMaterialItem` | `purchaseRequest` / `purchaseRequestMaterialItem` |

**⚠️ Do NOT replicate the PurchaseContract copy-paste bug:** `PurchaseContractManager.getItemControlConfig` legacy references `purchaseReturnMaterialItemUIModel`. The PurchaseRequest port must use `purchaseRequestMaterialItemUIModel`.

---

## 4. Files to CREATE (PurchaseRequest-specific)

Mirror each PurchaseContract file, renaming symbols and swapping the deltas from §3.

### 4A. Page directory — `src/pages/logistics/purchaseRequest/`

| # | New file | Mirrors (PurchaseContract) | ~Lines | Notes / deltas to apply |
|---|---|---|---|---|
| 1 | `PurchaseRequestListController.tsx` | `PurchaseContractListController.tsx` | ~320 | searchContent: swap date fields → `planExecutionDate`, action nodes → `inProcessBy`, drop `reservedByDoc`. List columns: show `planExecutionDate`. Process-button group: `newPurchaseContract` (cross-doc). |
| 2 | `PurchaseRequestListPage.tsx` | `PurchaseContractListPage.tsx` | ~12 | Pure rename. |
| 3 | `usePurchaseRequestListController.ts` | `usePurchaseContractListController.ts` | ~17 | Pure rename; import `PurchaseRequestManager`. |
| 4 | `PurchaseRequestEditController.tsx` | `PurchaseContractEditController.tsx` | ~560 | Basic tab fields: `taxRate`, `planExecutionDate` (drop signDate/currency/contractDetails). `getActionCodeMatrix()`: add `inProcess`. Tabs: basic / attachment (`refDocumentSection`) / items. `buildPayload()`: `purchaseRequestMaterialItemUIModelList`. |
| 5 | `PurchaseRequestEditPage.tsx` | `PurchaseContractEditPage.tsx` | ~32 | Pure rename. |
| 6 | `usePurchaseRequestEditController.ts` | `usePurchaseContractEditController.ts` | ~53 | rightBar help docs → `PurchaseRequestHelpDocument`; panel name → `purchaseRequestMaterialItemPanel`. |
| 7 | `PurchaseRequestMaterialItemController.tsx` | `PurchaseContractMaterialItemController.tsx` | ~300 | `getItemUIModelKey()` → `purchaseRequestMaterialItemUIModel`. Item fields per PR item UIModel. |
| 8 | `PurchaseRequestItemEditPage.tsx` | `PurchaseContractItemEditPage.tsx` | ~31 | Pure rename. |
| 9 | `usePurchaseRequestMaterialItemController.ts` | `usePurchaseContractMaterialItemController.ts` | ~41 | `loadDocMatItem` typed to PR item service model; item rightBar help doc. |
| 10 | `PurchaseRequestMaterialItemPanel.tsx` | `PurchaseContractMaterialItemPanel.tsx` | ~80 | config: loadModule → PR item wrapper; getEditPageURL → `/logistics/purchaseRequestMaterialItem/...`. |

### 4B. Service / types / cross-doc components

| # | New file | Mirrors | ~Lines | Notes |
|---|---|---|---|---|
| 11 | `src/services/logistics/PurchaseRequestManager.ts` | `PurchaseContractManager.ts` | ~560 | `getRootNodeInstId → 'purchaseRequest'`, `getItemNodeInstId → 'purchaseRequestMaterialItem'`, `getDocumentType → PURCHASEREQUEST`, `getResourceId → ServiceModuleConstants.PurchaseRequest`. DOC_ACTION_CODE adds INPROCESS. Status/badge arrays per PR statuses. i18n bundles: `purchaseRequest` (en+zh) + foundation namespaces. |
| 12 | `src/types/logistics/PurchaseRequestContent.ts` | `PurchaseContractContent.ts` | ~130 | `PurchaseRequestUIModel` (taxRate, planExecutionDate), `PurchaseRequestMaterialItemUIModel`, `PurchaseRequestServiceUIModel` (action nodes incl. `inProcessBy`), `PurchaseRequestMaterialItemServiceUIModel`. |
| 13 | `src/components/doc/supplyChain/PurchaseRequestSelectInput.ts` | `PurchaseContractSelectInput.ts` | ~68 | Cross-doc source-select config. |
| 14 | `src/components/doc/supplyChain/PurchaseRequestMultiSelect.ts` | `PurchaseContractMultiSelect.ts` | ~59 | Cross-doc target multi-select config. |

### 4C. i18n locale files

| # | New file | Mirrors | Source (decode from) |
|---|---|---|---|
| 15 | `src/i18n/locales/en/supplyChain/PurchaseRequest.json` | `.../PurchaseContract.json` | `admin/i18n/supplyChain/PurchaseRequest_en.properties` + `PurchaseRequestMaterialItem_en.properties` |
| 16 | `src/i18n/locales/zh/supplyChain/PurchaseRequest.json` | `.../PurchaseContract.json` | `PurchaseRequest_zh.properties` + `PurchaseRequestMaterialItem_zh.properties` |

---

## 5. Files to EDIT (shared registration)

Add PurchaseRequest entries alongside PurchaseContract's — no full recreation.

| # | File | Edit |
|---|---|---|
| 17 | `src/router/index.tsx` | Add 3 lazy imports + 4 routes: `logistics/purchaseRequest`, `/new`, `/:uuid/edit`, `logistics/purchaseRequestMaterialItem/:uuid/edit`. |
| 18 | `src/router/menuConfig.ts` | Add a `purchase-requests` menu group (or child under procurement) → path `/logistics/purchaseRequest`. |
| 19 | `src/i18n/locales/en/Menu.json` | Add `purchaseRequests` + `requestList` keys. |
| 20 | `src/i18n/locales/zh/Menu.json` | Same, Chinese. |
| 21 | `src/services/DocumentManagerFactory.ts` | Register `PurchaseRequestManager` against `DocumentType.PURCHASEREQUEST` in both the static lookup (~L428) and the cached-instance factory (~L503). Add the import + a doc-type icon entry (~L224). |

**No edit needed:** `src/services/DocumentConstants.ts` — PurchaseRequest SEName, DocumentType, and status block already present.

---

## 6. Shared framework — reused AS-IS (zero changes)

These are document-type-agnostic; PurchaseRequest subclasses/parameterizes them exactly as PurchaseContract does:

- **Controllers:** `ServiceListController`, `DocumentEditController`, `ServiceEditController`, `DocItemEditController`, `ServiceBaseController`, `ServiceUIConstants`, `DocActionModalController`
- **Services:** `ServiceManager`, `DocumentConstants`, `ServiceUtilityHelper`, `SystemStandrdMetadataProxy`, `Commons`, help/action-log/right-bar services
- **API:** `docActionApi`, `attachmentApi`
- **Composables:** `useDocumentEditController`, `useDocumentListController`, `useItemEditController`, `useServiceEntityEditController`
- **Page components:** `DocumentEditPage`, `AsyncListPage`, `AsyncEditorPage`, `EditPanel`, `AsyncSection`, `AsyncAttachmentSection`, `AsyncEmbeddedListSection`, `AsyncSearchSection`, `AsyncEditSection`, `ProcessButtonArray`
- **Doc components:** `SrcSelectInputUnion`, `DocumentItemMultiSelect`, `DocumentItemMultiSelectFactory`
- **Types:** `DocumentUIModel`, `DocMatItemUIModel`, `DocInvolvePartyUIModel` (platform bases)
- **i18n:** engine + foundation namespaces (CommonElements, DocInvolveParty, DocActionNode, DocFlowNode, DocMatItem)

---

## 7. Summary counts

| Category | Count |
|---|---|
| **New files to create** | **16** (10 page + 2 service/type + 2 cross-doc + 2 i18n) |
| **Shared files to edit** | **5** (router, menuConfig, 2 Menu.json, DocumentManagerFactory) |
| **Shared files reused unchanged** | ~40 (entire framework) |
| **Backend changes** | **0** (ready) |

---

## 8. Suggested implementation order

1. **Types** (#12) — `PurchaseRequestContent.ts` (foundation for everything else)
2. **i18n** (#15, #16) — decode `.properties` → JSON (needed by Manager import)
3. **Manager** (#11) — `PurchaseRequestManager.ts` (registers i18n, provides URLs/status/icons)
4. **List** (#1–3) — controller + page + hook → verify search + table render against backend
5. **Editor** (#4–6) — controller + page + hook → verify load/save/exit/actions
6. **Item editor + panel** (#7–10) — item controller + page + hook + quick-edit panel
7. **Cross-doc select** (#13, #14) — source-select + multi-select
8. **Registration** (#17–21) — router, menu, DocumentManagerFactory
9. **Verify** — `tsc --noEmit`, run app, exercise list search / editor save / item quick-edit

Each step is independently testable — the list can be verified before the editor exists, etc.

---

## 9. Risks / watch-items

1. **INPROCESS action** — the one genuinely new workflow action. Must be added to the Manager's `DOC_ACTION_CODE`, the editor's `getActionCodeMatrix()`, and the status icon array. `DocumentConstants.PurchaseRequest.status.INPROCESS` already exists.
2. **Missing `/getItemStatusMap`** backend endpoint — if the PR item-status select needs a runtime option map, it's absent. Mitigation: render item status via the static icon array in the Manager (same as PC), not a runtime fetch.
3. **Attachment tab** — PR's middle tab is `refDocumentSection` (attachment) rather than a details form. Reuse the shared `AsyncAttachmentSection` (already used by PC's item attachment).
4. **Cross-doc `newPurchaseContract`** — list-level "create PurchaseContract from this Request" button. Can be a stub initially (like PC's `newFromInquiry`/`newFromRequest` placeholders) and wired later.
5. **Do not copy the `purchaseReturnMaterialItemUIModel` typo** from PC's manager.
