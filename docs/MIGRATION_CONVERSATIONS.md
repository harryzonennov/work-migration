# Migration Conversations

A running log of technical questions, discussions, and decisions raised during the migration.
Each entry is a self-contained Q&A or open discussion item.

---

## Project Reference

| Role | Path | Tech |
|---|---|---|
| **Legacy backend — Platform** | `/Users/I043125/work/ThorsteinPlatform/` | Spring 4 + Hibernate 5, Java source root: `src/main/java/platform/` |
| **Legacy backend — Finance** | `/Users/I043125/work/ThorsteinFinance/` | Spring 4 + Hibernate 5, Java source root: `src/main/java/net/thorstein/finance/` |
| **Legacy backend — Logistics** | `/Users/I043125/work/ThorsteinLogistics/` | Spring 4 + Hibernate 5, Java source root: `src/main/java/net/thorstein/logistics/` |
| **Legacy backend — SalesDistribution** | `/Users/I043125/work/ThorsteinSalesDistribution/` | Spring 4 + Hibernate 5, Java source root: `src/main/java/net/thorstein/salesDistribution/` |
| **Legacy backend — Production** | `/Users/I043125/work/ThorsteinProduction/` | Spring 4 + Hibernate 5, Java source root: `src/main/java/net/thorstein/production/` |
| **Legacy UI** | `/Users/I043125/work/ThorSalesDistributionUI/` | jQuery + Vue 2, admin root: `admin/`, JS modules: `admin/js/`, i18n: `admin/i18n/` |
| **New backend** | `/Users/I043125/work2/IntelligentPlatform/` | Spring Boot 3.2 + Hibernate 6 + Java 17, Java source root: `src/main/java/com/company/IntelligentPlatform/` |
| **New UI** | `/Users/I043125/work2/IntelligentUI/` | React 18 + Vite 5 + TypeScript + Ant Design Pro, src root: `src/` |

---

## Conversations

---

### C1

**Topic:** How are document action buttons (Submit, Approve, etc.) dynamically rendered in the legacy UI via `placeholder: { category: DOC_ACTION_BTN }` in `getDefaultPageMeta()`?

**Answer:**

The placeholder is a two-phase runtime expansion mechanism.

**Phase 1 — backend fetch (on component `created`)**

`SerDocumentControlHelper.initDocActionConfigureList()` GETs `getDocActionConfigureListURL`
(e.g. `purchaseContract/getDocActionConfigureList`). The backend returns an array of
`DocActionConfigure` objects — one per possible workflow action:

```json
[
  { "actionCode": 2, "targetStatus": 2,   "preStatusList": [1, 6], "authorActionCode": "edit"     },
  { "actionCode": 5, "targetStatus": 1,   "preStatusList": [2],    "authorActionCode": "edit"     },
  { "actionCode": 2, "targetStatus": 3,   "preStatusList": [2],    "authorActionCode": "auditDoc" },
  { "actionCode": 6, "targetStatus": 6,   "preStatusList": [2],    "authorActionCode": "auditDoc" },
  { "actionCode": 3, "targetStatus": 1,   "preStatusList": [3],    "authorActionCode": "auditDoc" },
  { "actionCode": 5, "targetStatus": 200, "preStatusList": [3],    "authorActionCode": "edit"     },
  { "actionCode": 6, "targetStatus": 100, "preStatusList": [3,200],"authorActionCode": "edit"     }
]
```

`preStatusList` — document statuses in which the button is visible.
`authorActionCode` — authorization key checked against the logged-in user's permissions.

**Phase 2 — button generation (after promise resolves)**

`ProcessButtonArray.genDocActionProcessButtonMeta()` cross-references the backend list with
the controller's `getActionCodeMatrix()`, which maps human-readable action headers to integer
action codes:

```js
// PurchaseContractEditor.js
{
  submit:        { actionCode: PurchaseContractActionNode.DOC_ACTION_SUBMIT },
  revokeSubmit:  { actionCode: PurchaseContractActionNode.DOC_ACTION_REVOKE_SUBMIT },
  approve:       { actionCode: PurchaseContractActionNode.DOC_ACTION_APPROVE },
  rejectApprove: { actionCode: PurchaseContractActionNode.DOC_ACTION_REJECT_APPROVE },
  countApprove:  { actionCode: PurchaseContractActionNode.DOC_ACTION_COUNTAPPROVE },
  processDone:   { actionCode: PurchaseContractActionNode.DOC_ACTION_PROCESS_DONE },
  deliveryDone:  { actionCode: PurchaseContractActionNode.DOC_ACTION_DELIVERY_DONE,
                   docItemMultiSelectConfig: { ... } },
}
```

For each configure item, the framework: reverse-looks up the action header by `actionCode`,
generates a `formatClass` function (checks `currentStatus ∈ preStatusList` + user auth on
every render), and generates a `callback` function (`executeDocActionCore(header)`).

**New UI equivalent:**

The entire mechanism is replaced by `<WorkflowToolbar>` — a static React component that
computes visible actions from a hardcoded `status → actionCodes` map (mirrors `preStatusList`).
No call to `getDocActionConfigureList` is made; visibility is computed client-side from
`DOC_STATUS` constants. `controller.executeAction(actionCode)` is the single callback.

See `DRAFT_MIGRATION_CONTRACT_PURCHASE_CONTRACT.md § Workflow Action Buttons Pattern` for
the full pipeline diagram and action code table.

---

### C2

**Topic:** How are page elements (tabs, sections, fields) rendered from `getDefaultPageMeta()` — the method returns JSON data, not React elements?

**Answer:**

The edit page layout is driven entirely by the JSON descriptor returned from
`getDefaultPageMeta()`. The page component itself (`PurchaseContractEditPage.tsx`) renders
only a loading gate and `<EditPageShell>` — no tabs, sections, or fields are hard-coded there.

The descriptor goes through four layers before reaching the DOM:

```
1. getDefaultPageMeta()   → PageMeta { tabMetaList, processButtonMeta }
                             Legacy format. Plain objects. i18n keys unresolved.

2. convertPageMetaToSectionsJson()  → EditSectionConfigJson[]
                             Still JSON-serialisable. Keys are *Key strings.
                             Multi-tab: each section gets tab:'basic', tabLabelKey:'...'
                             Single-tab: tab/tabLabelKey omitted → flat card-stack.
                             CUSTOMERCONTACT section → { sectionCategory, fields:[] }
                             EMBEDLIST section       → single items-table field

3. resolveSection()       → EditSectionConfig[]
                             i18n keys → translated strings via i18n.t()
                             iconKey   → React node via ICON_REGISTRY
                             getMetaDataUrl → request: () => loadMetaRequestForSelect(url)

4. EditPageShell          → <ProForm> + <Tabs> + ProFormText / ProFormSelect / ...
                             hasTabs = sections.some(s => s.tab !== undefined)
                             CUSTOMERCONTACT → <InvolvePartySection>
                             items-table     → <EditableProTable>
```

`parentContentPath` on a `SectionConfig` causes all bare field names to be prefixed,
producing nested array paths that ProForm uses to read/write the API envelope:

```
parentContentPath: 'purchaseContractUIModel' + fieldName: 'signDate'
  → name: ['purchaseContractUIModel', 'signDate']
```

See `DRAFT_MIGRATION_CONTRACT_PURCHASE_CONTRACT.md § Page Rendering Pipeline` for the full
layer diagram, tab/flat layout rule, and specialized section category table.

---

### C3

**Topic:** How do backend-driven select options (status, priority dropdowns) work — from `getMetaDataUrl` in `getDefaultPageMeta()` through to the rendered `<Select>`?

**Answer:**

The pipeline mirrors the legacy `AsyncControlElement.loadMetaData()` pattern but uses React/SWR
instead of jQuery. Key points:

1. The controller declares `readonly getStatusURL = 'purchaseContract/getStatusMap'` and
   references it in `getDefaultPageMeta()` via `settings: { getMetaDataUrl: this.getStatusURL }`.

2. `editDescriptorResolver.resolveField()` converts `getMetaDataUrl` into a `request` function:
   `request: () => loadMetaRequestForSelect(url)`.

3. `ProFormSelect` calls `request()` automatically on mount via SWR — no `useEffect` or
   `useState` needed. This mirrors `loadMetaData()` firing on mount in the legacy system.

4. `loadMetaRequestForSelect` (in `ServiceUtilityHelper.ts`) is the single canonical utility
   for all backend-driven selects. It accepts `string | SelectMetaOptions` and handles:
   `{id,text}` mapping, `{content:[...]}` envelope unwrap, model-object mapping
   (`idField`/`textField`/`listSubPath`), `filteredKeyList`, `excludeKeyList`, `addEmptyFlag`.

5. **Spring Boot 3 breaking change:** `.html` suffix URLs (e.g. `getStatusMap.html`) return
   404. All `getMetaDataUrl` values must be written without `.html`.

See `DRAFT_MIGRATION_CONTRACT_PURCHASE_CONTRACT.md § Select Metadata Pattern` for the full
pipeline diagram, feature table, and usage examples.

---

### C4

**Topic:** Where should the `test/` folder live — inside `src/` or at the project root? And should it mirror `src/`'s subdirectory structure?

**Answer:**

Place `test/` at the project root, parallel to `src/`. Mirror `src/`'s subdirectory structure
exactly so that every test file sits at the same relative path as the source file it tests:

```
src/services/ServiceUtilityHelper.ts
→ test/services/loadMetaRequestForSelect.test.ts

src/components/page/EditPageShell.tsx
→ test/components/editPageShell.test.tsx

src/pages/logistics/purchaseContract/PurchaseContractEditPage.tsx
→ test/pages/logistics/purchaseContract/purchaseContractEditPage.test.tsx
```

Rationale: root-level `test/` is the convention for Vite/Vitest projects (matches where
`vite.config.ts` lives, avoids polluting `src/` with non-production files, consistent with
how `vitest` discovers test files by default). Update `vite.config.ts` `setupFiles` to
`['test/setup.ts']` when moving.

---

### C5

**Topic:** Why do `getStatusMap` and `getPriorityMap` backend endpoints return 404 in the new backend when the legacy UI used `.html` suffix URLs?

**Answer:**

Spring Boot 3 / Spring MVC 6 **removed suffix pattern matching**, which was enabled by default
in Spring 4 via `<mvc:annotation-driven>`. In Spring 4, a controller mapped to `/getStatusMap`
also matched `/getStatusMap.html`, `/getStatusMap.json`, etc. In Spring Boot 3 that suffix
behaviour is gone entirely — only the exact path matches.

Fix: strip `.html` from all `get*URL` properties on the controller class. The correct values
for PurchaseContract are:

```ts
readonly getStatusURL      = 'purchaseContract/getStatusMap';
readonly getPriorityCodeURL = 'purchaseContract/getPriorityMap';
```

This applies to **every module** migrated from the legacy system — all `getMetaDataUrl` values,
`getDocActionConfigureListURL`, `executeDocActionURL`, and any other URL property must be
written without `.html`.

---

### C6

**Topic:** (open)

**Topic:** <!-- describe the question or discussion point here -->

**Answer:** <!-- fill in when resolved; leave blank while open -->

---
