# Purchase Contract Editor — Migration Plan
## Legacy `PurchaseContractEditor` (Vue 2 / jQuery) → `IntelligentUI` (React / Ant Design)

*Last refreshed: 2026-04-06 — reflects actual IntelligentUI codebase state*

---

## Overview

This document is a step-by-step migration plan for bringing the legacy `PurchaseContractEditor`
into the new `IntelligentUI` project (`/Users/I043125/work2/IntelligentUI`).

### Key Principles

> 1. **Keep the methods, patterns, and structure in business controller files as unchanged as
>    possible.** There are hundreds of business controllers across the codebase. Every method
>    signature or pattern that must change in each one multiplies the migration cost by that
>    count. The adaptation work belongs in the **shared framework layer**, not in downstream files.

> 2. **Use existing i18n property files as the source of truth.** Convert them to JSON.
>    Merge related business files (e.g., `PurchaseContract` + `PurchaseContractItem`) into one
>    per-module file. Keep all shared labels (save, cancel, exit…) in a single `commonElements`
>    namespace. Business-specific labels only go in the module file.

> 3. **Keep data, field definitions, and action logic from old JS files as the source of truth.**
>    Express them in the new framework patterns — do not rewrite business logic, translate it.

---

## Architecture Comparison

| Concern | Legacy (`PurchaseContractEditor.js`) | New (`IntelligentUI`) |
|---|---|---|
| Framework | Vue 2 + jQuery mixins | React 19 + TypeScript |
| UI Library | Custom `AsyncField`/`AsyncSection` components | Ant Design 5 + `@ant-design/pro-components` |
| Form engine | `AsyncEditorPage` consuming `getDefaultPageMeta()` JSON | `EditPageShell` consuming `buildSections()` via `ServiceEditController` |
| Form layout definition | `getDefaultPageMeta()` in each business controller | `getDefaultPageMeta()` in each business controller (same pattern, `PageMeta` type) |
| Form state | Vue `data.content` — single reactive object | ProForm internal state + `buildInitialValues()` |
| Lifecycle | `created()` → `mounted()` → `setModuleToUI()` | `loadRecord()` → `buildInitialValues()` (sync) |
| i18n | Runtime `.properties` files (unicode-escaped Chinese) | Build-time `i18next` JSON, namespace per module |
| i18n namespaces | `label.purchaseContract`, `label.actionNode`, `label` (common) | `purchaseContract:`, `docActionNode:`, `commonElements:`, etc. |
| i18n registration | `getI18nRootConfig()` per module | `PurchaseContractManager.ts` calls `i18n.addResourceBundle()` on import |
| Workflow buttons | Server-driven `getDocActionConfigureList` + `getActionCodeMatrix()` | Status-computed visibility in `WorkflowToolbar.tsx`, direct REST calls |
| Save | POST to `saveModuleService.html` | `handleFinish()` → REST `POST/PUT` |
| Navigation | `NavigationPanelIns.initNavigation('logistics','PurchaseContract')` | Automatic via `react-router-dom` + `MainLayout` active route |
| Sub-items | `AsyncEmbeddedListSection` loading separate editor page | `EditableProTable` inline in the form + separate item edit page |
| Party info | `purchaseToOrgUIModel` + `purchaseFromSupplierUIModel` | `CUSTOMERCONTACT` section type in `getDefaultPageMeta()`, rendered by `AsyncSection` |

---

## Current State (as of 2026-04-06)

### What is now done ✅

| File | Status | Notes |
|---|---|---|
| `src/types/logistics/PurchaseContractContent.ts` | **Done** | `PurchaseContractUIModel`, `PurchaseContractMaterialItemUIModel`, `PurchaseContractServiceUIModel`, `InvolvePartyUIModel`; all backend field names correct (`signDate`, `currencyCode`, `grossPrice`, `contractDetails`, `note`, `priorityCode`); `ContractStatus` union 1\|2\|3\|4\|5\|6\|7\|8\|100\|200\|790 |
| `src/mock/contracts.ts` | **Done** | `PurchaseContractListItem`, `MOCK_CONTRACTS` with real field names, `MOCK_SUPPLIERS`, `MOCK_MANAGERS`, `generateContractNo()` |
| `src/constants/docStatus.ts` | **Done** | `DOC_STATUS` = `{INITIAL:1, SUBMITTED:2, APPROVED:3, PROCESS_DONE:100, DELIVERY_DONE:200}`, `DOC_STATUS_COLOR`, `DOC_STATUS_I18N_KEY` — all correct backend values |
| `src/services/logistics/PurchaseContractManager.ts` | **Done** | `CONTRACT_STATUS` integer map (1,2,3,100,200), `ITEM_STATUS`, `CONTRACT_DOC_ACTION`, `CONTRACT_STATUS_BADGE` with correct keys; `getI18nRootConfig()`, `getI18nItemConfig()`; self-registers all i18n bundles on import |
| `src/i18n/index.ts` | **Done** | Bootstraps i18next; registers only `menu` + `dashboard`; all module namespaces registered by module managers |
| `src/i18n/locales/en/foundation/CommonElements.json` | **Done** | 176-key flat file — shared labels; note: no `fields` sub-object (see G3) |
| `src/i18n/locales/zh/foundation/CommonElements.json` | **Done** | ZH equivalent |
| `src/i18n/locales/en/foundation/DocInvolveParty.json` | **Done** | All party-role label keys |
| `src/i18n/locales/zh/foundation/DocInvolveParty.json` | **Done** | ZH equivalent |
| `src/i18n/locales/en/foundation/DocActionNode.json` | **Done** | All workflow action label keys |
| `src/i18n/locales/zh/foundation/DocActionNode.json` | **Done** | ZH equivalent |
| `src/i18n/locales/en/foundation/DocFlowNode.json` | **Done** | All doc-chain field label keys |
| `src/i18n/locales/zh/foundation/DocFlowNode.json` | **Done** | ZH equivalent |
| `src/i18n/locales/en/foundation/DocMatItem.json` | **Done** | Material item shared labels |
| `src/i18n/locales/en/supplyChain/PurchaseContract.json` | **Done** | `pageTitle`, `messages`, `toolbar`, `status` (1–8, 100, 200, 790), `purchaseContract.*` section/field keys, `form.priorityOptions` 1–4, `purchaseContractMaterialItem.*` |
| `src/i18n/locales/zh/supplyChain/PurchaseContract.json` | **Done** | ZH equivalent |
| `src/controllers/ServiceEditController.ts` | **Done** | `buildSections()`, `convertPageMetaToSectionsJson()`, `convertProcessButtonsToJson()`, `handleFinish()`, `navigateToList()`, `buildInitialValues()`, `serializeForm()`, `buildPayload()` |
| `src/controllers/ServiceListController.ts` | **Done** | `request()`, `handleDelete()`, `handleBulkDelete()`, search panel hooks, `extendDocSearchTabFieldMeta()` |
| `src/controllers/DocumentEditController.ts` | **Done** | Abstract `getStatus()`, `getDocumentId()` — sits between `ServiceEditController` and domain controllers |
| `src/controllers/PageMetaTypes.ts` | **Done** | `FieldConfig`, `SectionConfig`, `TabConfig`, `PageMeta`, `ButtonMeta` — full legacy page-meta type definitions |
| `src/controllers/EditPageTypes.ts` | **Done** | `EditFieldConfig`, `EditSectionConfig`, `SubmitterButton`, `ItemsTableController` |
| `src/controllers/EditPageJsonTypes.ts` | **Done** | `EditFieldConfigJson`, `EditSectionConfigJson`, `SubmitterButtonJson` |
| `src/controllers/ListPageTypes.ts` | **Done** | `ToolbarItem`, `ListColumnJson`, `ListPageConfig` |
| `src/controllers/editDescriptorResolver.ts` | **Done** | `ICON_REGISTRY`, `resolveSection()`, `resolveButton()` |
| `src/components/EditPageShell.tsx` | **Done** | Tabbed layout, inline grouping, `items-table` delegation |
| `src/components/ListPageShell.tsx` | **Done** | ProTable with toolbar, search panel, row selection |
| `src/components/SearchPanel.tsx` | **Done** | Tabbed search fields, `dateRange` with transform |
| `src/components/StatusTag.tsx` | **Done** | Ant Design Tag from per-contract status map; note colour mapping for status 3 diverges from `DocStatusTag` (see G2 note) |
| `src/components/LanguageSwitcher.tsx` | **Done** | EN / ZH toggle |
| `src/components/doc/WorkflowToolbar.tsx` | **Done** | Status-driven workflow buttons mirroring `getActionCodeMatrix()`; INITIAL→Submit, SUBMITTED→Revoke/Approve/Reject, APPROVED→ProcessDone/DeliveryDone/Archive, PROCESS_DONE→DeliveryDone/Archive, DELIVERY_DONE→Archive |
| `src/components/doc/DocStatusTag.tsx` | **Done** | Reusable status tag driven by `DOC_STATUS_COLOR` / `DOC_STATUS_I18N_KEY` |
| `src/api/purchaseContractApi.ts` | **Done** | `listContracts()`, `getContract()`, `getContractMaterialItem()` — mapped to real legacy URLs; `apiPost`/`apiGet` from `apiClient.ts` |
| `src/pages/logistics/purchaseContract/PurchaseContractListController.tsx` | **Done** | `getDefaultPageMeta()` with SEARCH + EMBEDLIST sections; `request()` calls real API; `valueEnumKeys` uses 1–8, 100, 200, 790 status codes |
| `src/pages/logistics/purchaseContract/PurchaseContractEditController.tsx` | **Done** | 3-tab layout (basic/details/items); `CUSTOMERCONTACT` sections for supplier + org; `getStatus()`, `getDocumentId()`, `executeAction()`; `buildPayload()` merges dataSource |
| `src/pages/logistics/purchaseContract/PurchaseContractMaterialItemController.tsx` | **Done** | Item editor controller |
| `src/pages/logistics/purchaseContract/PurchaseContractEditPage.tsx` | **Done** | `WorkflowToolbar` + `DocStatusTag` + contract number wired above `EditPageShell` |
| `src/pages/logistics/purchaseContract/PurchaseContractListPage.tsx` | **Done** | List page wired to controller |
| `src/pages/logistics/purchaseContract/PurchaseContractItemEditPage.tsx` | **Done** | Item edit page |
| `src/router/index.tsx` | **Done** | `/logistics/purchaseContract` routes |
| `src/layouts/MainLayout.tsx` | **Done** | Sidebar nav |

---

## Gap Status

| # | Gap | Status | Notes |
|---|---|---|---|
| G1 | **Field name misalignment** — `signingDate`→`signDate`, `currency`→`currencyCode`, etc. | ✅ Done | `PurchaseContractContent.ts` uses all correct backend field names. `PurchaseContractEditController` uses them throughout. |
| G2 | **Status integer codes** — backend values 1,2,3,100,200 vs invented 1–8 range | ⚠️ Partial | `DOC_STATUS` and `CONTRACT_STATUS` use correct values. However `ContractStatus` union was *extended* (1–8 + 100 + 200 + 790) to match actual backend data from the legacy system. `StatusTag.tsx` maps status 3 → `'error'` (Reject/Review Rejected per legacy data). The plan's original prescription (5-value narrow union) does not match the actual backend data shape discovered in the JSON fixtures. The extended union is intentional — see note below. |
| G3 | **`commonElements` missing `fields.taxRate` and `fields.discount`** | ❌ Pending | `PurchaseContractEditController` references `commonElements:fields.taxRate` but `CommonElements.json` has no `fields` sub-object. The key will fall back to the raw string at runtime. `discount` is not yet referenced anywhere. |
| G4 | **No `priorityCode` field** in form or type | ✅ Done | `PurchaseContractUIModel.priorityCode` present; select field in `getDefaultPageMeta()` with options 1–4; `form.priorityOptions` in i18n. |
| G5 | **`purchaseContract.json` missing supplier/buyingOrg i18n keys** | ✅ Done | The supplier and buying-org sections now use `SectionCategory.CUSTOMERCONTACT` (party-selector sections with no individual field keys). Section title keys `purchaseFromSupplierSection` and `purchaseToOrgSection` are present. `requireExecutionDate` is present. |
| G6 | **No Workflow Toolbar** | ✅ Done | `WorkflowToolbar.tsx` exists and is wired in `PurchaseContractEditPage.tsx`. |
| G7 | **No status header in edit form** | ✅ Done | `DocStatusTag.tsx` + contract number displayed above form in `PurchaseContractEditPage.tsx`. |
| G8 | **No real API calls** | ✅ Done | `purchaseContractApi.ts` maps to real legacy backend URLs. `PurchaseContractListController.request()` calls `listContracts()`. Edit controller accepts pre-loaded `record` from hook. `handleFinish()` calls `serializeForm()` and logs payload (real save call is the only remaining wiring in the hook). |
| G9 | **Adapter layer for `getDefaultPageMeta()`** | ✅ Done | `ServiceEditController.convertPageMetaToSectionsJson()` converts the legacy page-meta format automatically. `PageMetaTypes.ts` defines all types (renamed from `LegacyPageMetaTypes.ts`). Every future controller that implements `getDefaultPageMeta()` works in `EditPageShell` with zero extra changes. |

### Note on G2 — Extended Status Union

The original plan prescribed a narrow 5-value `ContractStatus = 1 | 2 | 3 | 100 | 200` union.
The actual backend data (from real JSON fixtures and the legacy `PurchaseContractManager.js`) uses
additional codes:
- `4` = Goods Received, `5` = Cancelled, `6` = Archived, `7` = Completed, `8` = In Execution
- `790` = Legacy archived state

`ContractStatus` is intentionally extended to match actual backend data.
`DOC_STATUS` remains the narrow 5-value constant for workflow logic (toolbar visibility, etc.).
`StatusTag.tsx` covers all values with its per-module colour map.
The `DOC_STATUS_COLOR` / `DOC_STATUS_I18N_KEY` maps in `docStatus.ts` cover only the 5 workflow-relevant
values — they fall back gracefully for the additional codes via `DocStatusTag`'s
`purchaseContract:status.${status}` fallback.

---

## Remaining Work

### R1 — Fix `commonElements:fields.taxRate` key  *(Gap G3)*

**Priority: High** — this is a broken i18n reference currently in production code.

**File:** `src/i18n/locales/en/foundation/CommonElements.json` and `zh/` equivalent

Add a `fields` sub-object:
```json
"fields": {
  "taxRate":  "Tax Rate",
  "discount": "Discount"
}
```

ZH: `"taxRate": "税率"`, `"discount": "折扣"`

This is a one-line addition to each locale file. The `taxRate` field in
`PurchaseContractEditController.tsx` (tab 2, financial section) already references
`commonElements:fields.taxRate` — this fix resolves the broken key immediately.

---

### R2 — Wire real save/update calls in `handleFinish`  *(G8 partial)*

**Priority: Medium** — the API module exists; the hook wrapper does not yet call it on submit.

**File:** `src/pages/logistics/purchaseContract/usePurchaseContractEditController.ts` (or
the edit controller's `buildPayload`/`afterSubmit` override)

Replace the `console.log` in `ServiceEditController.handleFinish` with actual
`purchaseContractApi.create()` / `purchaseContractApi.update()` calls.

Pattern from the plan (unchanged):
```ts
// In the hook or via a buildPayload override:
const uuid = record?.purchaseContractUIModel.uuid;
return uuid
  ? purchaseContractApi.update(uuid, payload)
  : purchaseContractApi.create(payload);
```

Note: `purchaseContractApi` currently only has `listContracts`, `getContract`, and
`getContractMaterialItem`. The `create` and `update` endpoints need to be added to match
the legacy `saveModuleService.html` endpoint.

**Add to `purchaseContractApi.ts`:**
```ts
// POST /api/purchaseContract/saveModuleService.html (create)
export async function createContract(data: PurchaseContractServiceUIModel): Promise<unknown> {
  return apiPost('purchaseContract/saveModuleService.html', data);
}

// POST /api/purchaseContract/saveModuleService.html (update — same endpoint, uuid in body)
export async function updateContract(data: PurchaseContractServiceUIModel): Promise<unknown> {
  return apiPost('purchaseContract/saveModuleService.html', data);
}

// POST /api/purchaseContract/executeDocAction.html (workflow action)
export async function executeDocAction(uuid: string, actionCode: string): Promise<unknown> {
  return apiPost('purchaseContract/executeDocAction.html', { uuid, actionCode });
}
```

Wire `executeDocAction` into `PurchaseContractEditController.executeAction()` to replace the
mock `console.log`.

---

## Execution Order for Remaining Work

| Order | Item | Gap(s) | Files Affected | Complexity |
|---|---|---|---|---|
| 1 | R1: Add `fields.taxRate` to `commonElements` | G3 | `i18n/locales/*/foundation/CommonElements.json` | Low |
| 2 | R2: Add create/update/action endpoints to API + wire in controller | G8 partial | `api/purchaseContractApi.ts`, `PurchaseContractEditController.tsx` | Low–Medium |

---

## Current File Structure

```
src/
├── api/
│   ├── apiClient.ts                      ← DONE ✅
│   └── purchaseContractApi.ts            ← DONE ✅ (list + get; create/update/action pending R2)
├── constants/
│   └── docStatus.ts                      ← DONE ✅
├── controllers/
│   ├── ServiceEditController.ts          ← DONE ✅ (includes convertPageMetaToSectionsJson adapter)
│   ├── ServiceListController.ts          ← DONE ✅
│   ├── DocumentEditController.ts         ← DONE ✅
│   ├── EditPageTypes.ts                  ← DONE ✅
│   ├── EditPageJsonTypes.ts              ← DONE ✅
│   ├── ListPageTypes.ts                  ← DONE ✅
│   ├── PageMetaTypes.ts                  ← DONE ✅ (was LegacyPageMetaTypes.ts in plan)
│   └── editDescriptorResolver.ts         ← DONE ✅
├── components/
│   ├── EditPageShell.tsx                 ← DONE ✅
│   ├── ListPageShell.tsx                 ← DONE ✅
│   ├── SearchPanel.tsx                   ← DONE ✅
│   ├── StatusTag.tsx                     ← DONE ✅
│   ├── LanguageSwitcher.tsx              ← DONE ✅
│   └── doc/
│       ├── WorkflowToolbar.tsx           ← DONE ✅
│       └── DocStatusTag.tsx              ← DONE ✅
├── types/logistics/
│   └── PurchaseContractContent.ts        ← DONE ✅ (was types/contract.ts in plan)
├── mock/
│   └── contracts.ts                      ← DONE ✅
├── services/logistics/
│   └── PurchaseContractManager.ts        ← DONE ✅
├── i18n/
│   ├── index.ts                          ← DONE ✅
│   └── locales/
│       ├── en/
│       │   ├── foundation/
│       │   │   ├── CommonElements.json   ← UPDATE (R1): + fields.taxRate, fields.discount
│       │   │   ├── DocInvolveParty.json  ← DONE ✅
│       │   │   ├── DocActionNode.json    ← DONE ✅
│       │   │   ├── DocFlowNode.json      ← DONE ✅
│       │   │   └── DocMatItem.json       ← DONE ✅
│       │   └── supplyChain/
│       │       └── PurchaseContract.json ← DONE ✅
│       └── zh/
│           └── (mirrors en/ structure)
└── pages/logistics/purchaseContract/
    ├── PurchaseContractListPage.tsx       ← DONE ✅
    ├── PurchaseContractEditPage.tsx       ← DONE ✅ (WorkflowToolbar + DocStatusTag wired)
    ├── PurchaseContractItemEditPage.tsx   ← DONE ✅
    ├── PurchaseContractListController.tsx ← DONE ✅
    ├── PurchaseContractEditController.tsx ← DONE ✅ (getStatus/getDocumentId/executeAction present)
    ├── PurchaseContractMaterialItemController.tsx ← DONE ✅
    ├── usePurchaseContractListController.ts  ← DONE ✅
    ├── usePurchaseContractEditController.ts  ← DONE ✅ (async record load; save wiring pending R2)
    └── usePurchaseContractItemEditController.ts ← DONE ✅
```

---

## Key Design Decisions

### 1. Module managers self-register i18n (implemented)

`src/i18n/index.ts` bootstraps only the i18next engine plus `menu` + `dashboard`. Each module's
manager file (`PurchaseContractManager.ts`) calls `i18n.addResourceBundle()` on import, loading
its own foundation + business namespaces. Adding a new module never requires touching `index.ts`.

### 2. camelCase namespace names + sub-folder structure (implemented)

Namespaces: `commonElements`, `docInvolveParty`, `docActionNode`, `docFlowNode`, `docMatItem`,
`purchaseContract`.
Files: `foundation/CommonElements.json`, `supplyChain/PurchaseContract.json`.
Matches the module/feature hierarchy and avoids kebab-case parsing issues.

### 3. Adapter in the framework, zero changes in business controllers (implemented)

`ServiceEditController.convertPageMetaToSectionsJson()` lives in the framework base class.
A business controller that implements `getDefaultPageMeta()` works in `EditPageShell` with no
additional changes — the base class converts it automatically. `PageMetaTypes.ts` defines all
legacy type shapes.

### 4. Extended ContractStatus union (intentional)

`DOC_STATUS` constant covers the 5 workflow-relevant codes (1,2,3,100,200) used for toolbar
visibility logic. `ContractStatus` union covers all codes that appear in real backend data
(1–8, 100, 200, 790) to prevent TypeScript errors when processing live records. The two are
complementary, not contradictory.

### 5. Workflow buttons outside `EditPageShell` (implemented)

`EditPageShell`'s submitter footer is for save/cancel/draft operations only. Workflow
actions (submit/approve/etc.) live in `WorkflowToolbar.tsx` — reusable across all document
types without changes to the shell.

### 6. Foundation i18n namespaces shared across all modules (implemented)

`docActionNode`, `docInvolveParty`, `docFlowNode`, `docMatItem`, `commonElements` are foundation
namespaces — not specific to purchase contracts. Every future module manager will
`addResourceBundle` the same foundation files, then add its own business namespace.

### 7. CUSTOMERCONTACT section type for party info (implemented)

Rather than flat supplier/buying-org fields (as originally planned in the gap list), the
party sections use `SectionCategory.CUSTOMERCONTACT` in `getDefaultPageMeta()`. This matches
the legacy `AsyncSection` category and enables the party-selector component
(currently rendered as a placeholder by `AsyncSection` in the new UI).

---

## Source Reference Files

| Reference | Path | What to extract |
|---|---|---|
| Field list + content model | `ThorSalesDistributionUI/admin/js/supplyChain/PurchaseContractManager.js` | `content.purchaseContractUIModel`, `DOC_ACTION_CODE`, `getStatusIconArray()`, `documentTab` |
| Form layout + field meta | `ThorSalesDistributionUI/admin/js/supplyChain/PurchaseContractEditor.js` | `getDefaultPageMeta()` → `tabMetaList` → `sectionMetaList` → `fieldMetaList` |
| i18n labels (contract) | `ThorSalesDistributionUI/admin/i18n/supplyChain/PurchaseContract_en.properties` | 126 keys (unicode → Chinese decode) |
| i18n labels (item) | `ThorSalesDistributionUI/admin/i18n/supplyChain/PurchaseContractMaterialItem_en.properties` | item-level labels |
| i18n labels (common) | `ThorSalesDistributionUI/admin/i18n/foundation/ComElements_en_US.properties` | shared action/message labels |
| API endpoints | `work-migration/docs/PURCHASE_CONTRACT_EDITOR_MIGRATION_CASE.md` (§ API Mapping) | REST URL patterns |
| Backend field names | `IntelligentPlatform/src/main/java/…/PurchaseContract.java` | Java field names (camelCase) |
