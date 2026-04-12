# Draft Migration Contract — PurchaseContract Module
## Status: EXPLORING (first case — patterns still being validated)

*This is the working contract for the PurchaseContract migration. Once all items are verified,
it becomes the canonical example in `MIGRATION_PATTERN.md`.*

---

## Module Identity

| Property | Value |
|---|---|
| Module | `logistics` / `supplyChain` |
| Entity | `PurchaseContract` |
| Old URL prefix | `/purchaseContract/` |
| New route | `/logistics/purchaseContracts` |
| Old source folder | `admin/js/supplyChain/` |
| New source folder | `src/pages/logistics/purchaseContract/` |
| Has sub-items | Yes — `PurchaseContractMaterialItem` |
| Has party sections | Yes — supplier (`purchaseFromSupplierUIModel`) + buying org (`purchaseToOrgUIModel`) |

---

## Output File Contract

### ✅ = verified done | ⚠️ = partial | ❌ = pending

| File | Status | Notes |
|---|---|---|
| `src/types/logistics/PurchaseContractContent.ts` | ✅ | All field names correct; `ContractStatus` extended union (1–8, 100, 200, 790) |
| `src/services/logistics/PurchaseContractManager.ts` | ✅ | `CONTRACT_STATUS`, `CONTRACT_DOC_ACTION`, `CONTRACT_STATUS_BADGE`; i18n self-registers |
| `src/api/purchaseContractApi.ts` | ⚠️ | `listContracts`, `getContract`, `getContractMaterialItem` done; **missing: `createContract`, `updateContract`, `deleteContract`, `executeDocAction`, `deleteItem`, `exitEditor`** |
| `src/i18n/locales/en/supplyChain/PurchaseContract.json` | ✅ | 124+ keys; status 1–8/100/200/790 (status/priority labels — options loaded from backend, not i18n) |
| `src/i18n/locales/zh/supplyChain/PurchaseContract.json` | ✅ | ZH equivalent |
| `src/i18n/locales/en/foundation/CommonElements.json` | ❌ | Missing `fields.taxRate` + `fields.discount` — broken reference in EditController |
| `src/i18n/locales/zh/foundation/CommonElements.json` | ❌ | Same |
| `src/pages/logistics/purchaseContract/PurchaseContractListController.tsx` | ✅ | Calls real `listContracts()`; status `valueEnum` uses integer codes |
| `src/pages/logistics/purchaseContract/PurchaseContractEditController.tsx` | ⚠️ | `getDefaultPageMeta()` done; `executeAction()` still mock; `buildPayload()` done |
| `src/pages/logistics/purchaseContract/PurchaseContractMaterialItemController.tsx` | ⚠️ | Basic load done; save/delete not wired |
| `src/pages/logistics/purchaseContract/PurchaseContractListPage.tsx` | ✅ | Wired to controller |
| `src/pages/logistics/purchaseContract/PurchaseContractEditPage.tsx` | ✅ | `WorkflowToolbar` + `DocStatusTag` + contract number wired |
| `src/pages/logistics/purchaseContract/PurchaseContractItemEditPage.tsx` | ✅ | Basic page wired |
| `src/router/index.tsx` | ✅ | List + edit + item routes registered |

---

## API Contract

### Implemented
| Function | Endpoint | Method |
|---|---|---|
| `listContracts(query)` | `/purchaseContract/searchTableService` | POST |
| `getContract(uuid)` | `/purchaseContract/loadModuleEditService` | GET |
| `getContractMaterialItem(uuid)` | `/purchaseContractMaterialItem/loadModuleEditService` | GET |

### Missing (must complete before module is "done")
| Function | Endpoint | Method | Priority |
|---|---|---|---|
| `createContract(data)` | `/purchaseContract/saveModuleService` | POST | High |
| `updateContract(data)` | `/purchaseContract/saveModuleService` | POST | High |
| `executeDocAction(uuid, actionCode)` | `/purchaseContract/executeDocAction` | POST | High |
| `deleteContract(uuid)` | `/purchaseContract/deleteModule` | GET | Medium |
| `exitEditor(uuid)` | `/purchaseContract/exitEditor` | POST | Medium |
| `createItem(baseUUID)` | `/purchaseContractMaterialItem/newModuleService` | POST | Medium |
| `updateItem(data)` | `/purchaseContractMaterialItem/saveModuleService` | POST | Medium |
| `deleteItem(uuid)` | `/purchaseContractMaterialItem/deleteModule` | GET | Medium |
| `checkDuplicateId(id)` | `/purchaseContract/checkDuplicateID` | POST | Low |

---

## Select Metadata Pattern (Backend-Driven Dropdowns)

### How it worked in the legacy UI

The legacy `PurchaseContractEditor.js` defined URL properties on the editor class:

```js
getStatusURL:      '../purchaseContract/getStatusMap.html',
getPriorityCodeURL: '../purchaseContract/getPriorityMap.html',
```

`AsyncControlElement.loadMetaData()` called `ServiceUtilityHelper.loadMetaRequest({ url, $http, element, ... })`
on mount, which fetched the backend, parsed `[{id, text}]` or `{content:[{id,text}]}`, and populated a Select2 element.

### How it works in the new UI

The same backend endpoints exist in `PurchaseContractEditorController.java`. The pipeline is:

```
PurchaseContractEditController.getDefaultPageMeta()
  │  fieldType: 'select', settings: { getMetaDataUrl: this.getStatusURL }
  ▼
ServiceEditController._convertLegacyField()
  │  emits: { type: 'select', settings: { getMetaDataUrl: url } }
  ▼
editDescriptorResolver.resolveField()
  │  builds: request = () => loadMetaRequestForSelect(url)
  ▼
EditPageShell → <ProFormSelect request={...} />
  │  ProFormSelect calls request() on mount via SWR (no user click needed)
  ▼
loadMetaRequestForSelect(url)   ← ServiceUtilityHelper.ts
  │  apiGet(url) → unwrap → map {id,text} → {label,value}
  ▼
Ant Design Select options populated
```

### Backend endpoints for this module

| Property on controller | URL | Spring mapping |
|---|---|---|
| `getStatusURL` | `purchaseContract/getStatusMap` | `@RequestMapping("/getStatusMap")` |
| `getPriorityCodeURL` | `purchaseContract/getPriorityMap` | `@RequestMapping("/getPriorityMap")` |

Both return `[{id: <integer>, text: <string>}]` — a bare JSON array, no `{result,content}` envelope.

> **Important:** Spring Boot 3 / Spring MVC 6 **removed suffix pattern matching**.
> The old `.html` suffix URLs (`getStatusMap.html`) return **404** in the new backend.
> All `getMetaDataUrl` values must be specified **without `.html`**.

### Controller properties (authoritative)

```ts
// PurchaseContractEditController.tsx
readonly getStatusURL      = 'purchaseContract/getStatusMap';
readonly getPriorityCodeURL = 'purchaseContract/getPriorityMap';
```

These are referenced directly in `getDefaultPageMeta()`:

```ts
{ fieldName: 'status',       fieldType: 'select', settings: { getMetaDataUrl: this.getStatusURL },       readonly: true },
{ fieldName: 'priorityCode', fieldType: 'select', settings: { getMetaDataUrl: this.getPriorityCodeURL } },
```

### `loadMetaRequestForSelect` — universal utility

`ServiceUtilityHelper.loadMetaRequestForSelect` is the React/Ant Design equivalent of
`ServiceUtilityHelper.loadMetaRequest` from the legacy UI. It is the **only function** that
should be used to load backend-driven select options across the entire IntelligentUI project.

```ts
// Simple case (status/priority maps)
request: () => loadMetaRequestForSelect('purchaseContract/getStatusMap')

// Model list (idField/textField/listSubPath)
request: () => loadMetaRequestForSelect({
  url: 'material/getTypeList',
  idField: 'uuid',
  textField: 'name',
  listSubPath: 'materialTypeUIModel',
})

// With empty option (search forms)
request: () => loadMetaRequestForSelect({
  url: 'purchaseContract/getStatusMap',
  addEmptyFlag: true,
})

// Filtered list
request: () => loadMetaRequestForSelect({
  url: 'purchaseContract/getStatusMap',
  filteredKeyList: [1, 2, 4],
})
```

All five legacy features of `_renderSelectResultList` are preserved:

| Feature | Option |
|---|---|
| `{id,text}` mapping (default) | — |
| Model-object mapping | `idField` + `textField` + `listSubPath` |
| `{content:[...]}` envelope unwrap | automatic |
| Keep-only filter | `filteredKeyList` |
| Exclude filter | `excludeKeyList` |
| Prepend blank option | `addEmptyFlag` / `emptyLabel` / `emptyId` |
| Custom mapping | `processSelectOptions` |

### Static options (no backend fetch)

Fields whose options never change are declared inline with `options: [{id, text}]`
in `getDefaultPageMeta()`. `_convertLegacyField` converts them to `{label,value}` pairs.
No `request` function is attached — `ProFormSelect` uses `options` directly.

```ts
// Example: currencyCode
{ fieldName: 'currencyCode', fieldType: 'select',
  options: ['USD', 'EUR', 'CNY', 'GBP'].map(c => ({ id: c, text: c })) }
```

---

## Page Rendering Pipeline

The edit page layout is **fully driven by data returned from `getDefaultPageMeta()`** on the
controller class. No tabs, sections, or form fields are hard-coded in the page component itself.
The page (`PurchaseContractEditPage.tsx`) renders only a loading gate and `<EditPageShell>`;
everything inside the shell is generated from the descriptor returned by the controller.

### Layer overview

```
PurchaseContractEditController.getDefaultPageMeta()
  │
  │  Returns: PageMeta { tabMetaList, processButtonMeta }
  │  — legacy format, plain objects, i18n keys not yet resolved
  │  — one TabConfig per tab; each TabConfig has N SectionConfig entries;
  │    each SectionConfig has N FieldConfig entries
  ▼
ServiceEditController.convertPageMetaToSectionsJson()    (called inside buildSections())
  │
  │  Produces: EditSectionConfigJson[]  +  SubmitterButtonJson[]
  │  — still serialisable JSON: all strings are i18n keys (*Key)
  │  — multi-tab: each section gets  tab:'basic'  tabLabelKey:'...'
  │  — single-tab: tab / tabLabelKey omitted → flat card-stack layout
  │  — select with getMetaDataUrl → { type:'select', settings:{getMetaDataUrl} }
  │  — select with options[]      → { type:'select', options:[{label,value}] }
  │  — CUSTOMERCONTACT section    → { sectionCategory, parentContentPath, fields:[] }
  │  — EMBEDLIST section          → single { type:'items-table', columns:[...] } field
  ▼
editDescriptorResolver.resolveSection() / resolveButton()
  │
  │  Produces: EditSectionConfig[]  +  SubmitterButton[]
  │  — all *Key strings resolved via i18n.t() → translated labels
  │  — iconKey string → React node via ICON_REGISTRY
  │  — getMetaDataUrl → request: () => loadMetaRequestForSelect(url)
  │    (ProFormSelect fires request() on mount automatically via SWR)
  ▼
EditPageShell (React component)
  │
  │  Receives: sections[], buttons[], controller
  │  — hasTabs = sections.some(s => s.tab !== undefined)
  │  — Tab layout:  buildTabEntries() groups sections by tab key
  │                 <Tabs> + one <TabPane> per group
  │  — Flat layout: sections rendered as stacked cards (no Tabs)
  │  — renderSection():
  │      CUSTOMERCONTACT → <InvolvePartySection> (party selector component)
  │      standard       → partitionFields() → inline groups in ProForm.Group
  │                        + block fields (textarea/upload/items-table) standalone
  │  — renderField():
  │      text    → <ProFormText>
  │      select  → <ProFormSelect request={...}> or <ProFormSelect options={...}>
  │      date    → <ProFormDatePicker>
  │      digit   → <ProFormDigit>
  │      textarea→ <ProFormTextArea>
  │      upload  → <ProForm.Item> + <Upload>
  │      items-table → <EditableProTable> (state delegated to controller)
  │  — Side-by-side cards: colClass 'col-md-6' → flexBasis 50%, guttered flex row
  ▼
ProForm
  │  initialValues = controller.buildInitialValues()
  │    edit mode:   loads record, ISO date strings → dayjs instances
  │    create mode: calls buildCreateDefaults()
  │  onFinish      = controller.handleFinish()
  │    dayjs instances → ISO strings, then buildPayload(), API call, navigate to list
```

### Field name path construction

`parentContentPath` on a `SectionConfig` causes the converter to prefix all bare field names,
producing the nested path ProForm needs to read and write the wrapped API envelope:

```
parentContentPath: 'purchaseContractUIModel'
fieldName:         'signDate'
─────────────────────────────────────────
name: ['purchaseContractUIModel', 'signDate']
```

ProForm reads `initialValues.purchaseContractUIModel.signDate` and writes back to the same path.
`buildPayload()` spreads the full form values (which already mirror `PurchaseContractServiceUIModel`)
and merges in the items list and attachment list from controller state.

### Tab vs. flat layout rule

| `tabMetaList` length | Effect |
|---|---|
| 1 | No `tab` property on any section → `EditPageShell` renders a flat card stack (no Tabs component) |
| > 1 | Each section gets `tab: tabId` → `EditPageShell` builds `<Tabs>` with one pane per unique tab key |

### Specialized section categories

| `sectionCategory` | Behaviour |
|---|---|
| `SectionCategory.EDIT` (default) | Standard ProForm fields rendered from `fieldMetaList` |
| `SectionCategory.CUSTOMERCONTACT` | `fieldMetaList` ignored; renders `<InvolvePartySection>` party selector, using `parentContentPath` + `accountObjectType` |
| `SectionCategory.EMBEDLIST` | `fieldMetaList` converted to table columns; emits a single `items-table` field consumed by `EditableProTable` |
| `SectionCategory.ATTACHMENT` | `fieldType:'upload'` field in `fieldMetaList` renders `<Upload>` via `ProForm.Item` wrapper |

---

## Workflow Action Buttons Pattern

### How it worked in the legacy UI

The `placeholder: { category: ProcessButtonConstants.placeholderCategory.DOC_ACTION_BTN }` entry
in `getDefaultPageMeta().processButtonMeta` is a **slot marker** — it tells the framework to
dynamically expand that slot into the real workflow action buttons at runtime.

The expansion happens in two phases:

**Phase 1 — fetch action configuration from backend (on component `created`)**

`SerDocumentControlHelper` calls `initDocActionConfigureList()`, which GETs
`getDocActionConfigureListURL` (e.g. `../purchaseContract/getDocActionConfigureList.html`).
The backend returns an array of `DocActionConfigure` objects — one per possible action:

```json
[
  { "actionCode": 2,  "targetStatus": 2,   "preStatusList": [1, 6], "authorActionCode": "edit"     },
  { "actionCode": 5,  "targetStatus": 1,   "preStatusList": [2],    "authorActionCode": "edit"     },
  { "actionCode": 2,  "targetStatus": 3,   "preStatusList": [2],    "authorActionCode": "auditDoc" },
  { "actionCode": 6,  "targetStatus": 6,   "preStatusList": [2],    "authorActionCode": "auditDoc" },
  { "actionCode": 3,  "targetStatus": 1,   "preStatusList": [3],    "authorActionCode": "auditDoc" },
  { "actionCode": 5,  "targetStatus": 200, "preStatusList": [3],    "authorActionCode": "edit"     },
  { "actionCode": 6,  "targetStatus": 100, "preStatusList": [3,200],"authorActionCode": "edit"     }
]
```

`preStatusList` = the document statuses in which this button should be **visible**.
`authorActionCode` = the authorization key checked against the logged-in user's action codes.

**Phase 2 — generate button descriptors (after promise resolves)**

`ProcessButtonArray.genDocActionProcessButtonMeta()` cross-references the backend list with
the controller's `getActionCodeMatrix()`.  `getActionCodeMatrix()` maps human-readable
*action headers* (e.g. `'submit'`, `'approve'`) to their numeric action codes:

```js
// PurchaseContractEditor.js — getActionCodeMatrix()
{
  submit:         { actionCode: PurchaseContractActionNode.DOC_ACTION_SUBMIT      },
  revokeSubmit:   { actionCode: PurchaseContractActionNode.DOC_ACTION_REVOKE_SUBMIT },
  approve:        { actionCode: PurchaseContractActionNode.DOC_ACTION_APPROVE      },
  rejectApprove:  { actionCode: PurchaseContractActionNode.DOC_ACTION_REJECT_APPROVE },
  countApprove:   { actionCode: PurchaseContractActionNode.DOC_ACTION_COUNTAPPROVE  },
  processDone:    { actionCode: PurchaseContractActionNode.DOC_ACTION_PROCESS_DONE  },
  deliveryDone:   { actionCode: PurchaseContractActionNode.DOC_ACTION_DELIVERY_DONE,
                    docItemMultiSelectConfig: { ... } },   // opens cross-doc selector
}
```

For each item in the backend list, the framework:
1. Finds the matching action header in the matrix (reverse lookup by `actionCode`)
2. Generates a button `formatClass` function: calls `displayForActionCodeCore(header)` on each
   render cycle → returns a CSS display/hide class based on `currentStatus ∈ preStatusList`
   AND user's `authorActionCode` permission
3. Generates a button `callback` function: calls `executeDocActionCore(header)`

**`executeDocActionCore(header)`** resolves the action and:
- If `docItemMultiSelectConfig` is set → opens a cross-document multi-select popup
  (e.g. `deliveryDone` for PurchaseContract → creates InboundDelivery)
- Otherwise → initialises the standard `DocActionNode` modal (confirm dialog → POST to
  `executeDocActionURL`)

### Backend endpoint (authoritative source)

```
GET /purchaseContract/getDocActionConfigureList
```

Implemented in `PurchaseContractEditorController.java` via
`serviceBasicUtilityController.getDocActionConfigureListCore(purchaseContractActionExecutionProxy)`.

The action list is **hardcoded** in `PurchaseContractActionExecutionProxy.getDefDocActionConfigureList()` —
it is **not** database-driven. The full list for PurchaseContract:

| Action header    | `actionCode` | `targetStatus` | `preStatusList` | `authorActionCode` |
|---|---|---|---|---|
| `submit`         | 2  | 2 (Submitted)    | [1, 6]      | `edit`     |
| `revokeSubmit`   | 5  | 1 (Initial)      | [2]         | `edit`     |
| `approve`        | 2  | 3 (Approved)     | [2]         | `auditDoc` |
| `rejectApprove`  | 6  | 6 (RejectApproval)| [2]        | `auditDoc` |
| `countApprove`   | 3  | 1 (Initial)      | [3]         | `auditDoc` |
| `deliveryDone`   | 5  | 200 (DeliveryDone)| [3]        | `edit`     |
| `processDone`    | 6  | 100 (ProcessDone) | [3, 200]   | `edit`     |

Both the legacy and new backend (`IntelligentPlatform`) have **identical** implementations
of `PurchaseContractActionExecutionProxy` — the new backend already has this endpoint.

### Action code integer constants (authoritative)

Defined in `PurchaseContractActionNode.java` → delegates to `SystemDefDocActionCodeProxy`:

| Constant                    | Integer value |
|---|---|
| `DOC_ACTION_UPDATE`         | 1  |
| `DOC_ACTION_APPROVE`        | 2  |
| `DOC_ACTION_COUNTAPPROVE`   | 3  |
| `DOC_ACTION_DELIVERY_DONE`  | 5  |
| `DOC_ACTION_PROCESS_DONE`   | 6  |
| `DOC_ACTION_SUBMIT`         | = `STATUS_SUBMITTED` = 2 |
| `DOC_ACTION_REVOKE_SUBMIT`  | = `STATUS_REVOKE_SUBMIT` = 5 |
| `DOC_ACTION_REJECT_APPROVE` | = `STATUS_REJECT_APPROVAL` = 6 |

### How it works in the new UI

The legacy dynamic expansion mechanism (fetch → placeholder → generate) has been replaced with
a **static, status-driven** React component: `WorkflowToolbar`.

```
PurchaseContractEditPage
  │  status = controller.getStatus()          // integer code from loaded record
  │  onAction = controller.executeAction      // stub — see Known Issues I3
  ▼
<WorkflowToolbar status={status} onAction={onAction} />
  │
  │  visibleCodes = STATUS_TO_ACTIONS[status]     (hardcoded map, mirrors preStatusList)
  │  visible = allActions.filter(a => visibleCodes.has(a.actionCode))
  ▼
For each visible action:
  │  requiresConfirm = true  →  <Popconfirm title={t(confirmTitleKey)}>
  │                                onConfirm={() => onAction(actionCode)}
  │                              </Popconfirm>
  │  requiresConfirm = false →  <Button onClick={() => onAction(actionCode)}>
  ▼
controller.executeAction(actionCode)
  │  Currently: mock toast (see Known Issues I3)
  │  Target:    call executeDocAction(uuid, actionCode) API → POST /purchaseContract/executeDocAction
```

#### Status → visible actions map (current implementation)

| Status | Visible action codes |
|---|---|
| 1 — Initial       | `SUBMIT` |
| 2 — Submitted     | `REVOKE_SUBMIT`, `APPROVE`, `REJECT_APPROVE` |
| 3 — Approved      | `PROCESS_DONE`, `DELIVERY_DONE`, `ARCHIVE` |
| 100 — ProcessDone | `DELIVERY_DONE`, `ARCHIVE` |
| 200 — DeliveryDone| `ARCHIVE` |

This map is the React equivalent of `preStatusList` from the backend configure list.
The new UI does **not** call `getDocActionConfigureList` — visibility is computed client-side
from `DOC_STATUS` constants. This is intentional: the backend list only adds value when the
action matrix is user-role-configurable (future enhancement — see Known Issues below).

#### `CONTRACT_DOC_ACTION` string constants (new UI)

Defined in `PurchaseContractManager.ts`; used as action code keys in `WorkflowToolbar`:

| Key              | String value              |
|---|---|
| `SUBMIT`         | `'ACTION_SUBMIT'`         |
| `REVOKE_SUBMIT`  | `'ACTION_REVOKE_SUBMIT'`  |
| `APPROVE`        | `'ACTION_APPROVE'`        |
| `REJECT_APPROVE` | `'ACTION_REJECT_APPROVE'` |
| `COUNTAPPROVE`   | `'ACTION_COUNTAPPROVE'`   |
| `DELIVERY_DONE`  | `'ACTION_DELIVERY_DONE'`  |
| `PROCESS_DONE`   | `'ACTION_PROCESS_DONE'`   |
| `ARCHIVE`        | `'ACTION_ARCHIVE'`        |

These string values are passed to `executeDocAction(uuid, actionCode)` and then to the backend
`/purchaseContract/executeDocAction` endpoint — the backend maps them back to integer codes.

### Known gaps vs. legacy (open items)

| Gap | Detail |
|---|---|
| `executeAction` is mock | `PurchaseContractEditController.executeAction` logs and shows a toast instead of calling `executeDocAction(uuid, actionCode)` — see Known Issues I3 |
| `getDocActionConfigureList` not called | New UI computes visibility from local `DOC_STATUS` constants; no API call to `/purchaseContract/getDocActionConfigureList` — works correctly but doesn't support per-role overrides |
| `executeDocActionURL` / `getDocActionConfigureListURL` module prefix | ✅ Fixed — now correctly set to `purchaseContract/executeDocAction` and `purchaseContract/getDocActionConfigureList` |
| `countApprove` not in WorkflowToolbar | Legacy matrix included `countApprove` (reverts Approved → Initial); omitted from `WorkflowToolbar` — add if needed |

---

## Data Shape Contract

### `PurchaseContractServiceUIModel` (API envelope)
```ts
{
  purchaseContractUIModel: PurchaseContractUIModel       // header fields
  purchaseToOrgUIModel: InvolvePartyUIModel              // buying org
  purchaseFromSupplierUIModel: InvolvePartyUIModel       // supplier
  purchaseContractMaterialItemUIModelList: PurchaseContractMaterialItemUIModel[]
  serviceUIMeta: Record<string, unknown>                 // server flags, read-only
}
```

### Key field names (exact — do not rename)
| Field | Type | Notes |
|---|---|---|
| `uuid` | string | PK |
| `id` | string | Display contract number |
| `name` | string | Contract name |
| `status` | number | Integer: 1=Initial, 2=Submitted, 3=Approved, 100=ProcessDone, 200=DeliveryDone |
| `signDate` | string | ISO date — NOT `signingDate` |
| `requireExecutionDate` | string | ISO date |
| `priorityCode` | number | 1–4 |
| `currencyCode` | string | NOT `currency` |
| `grossPrice` | number | Total |
| `contractDetails` | string | Description — NOT `details` |
| `note` | string | |

### Status codes (authoritative)
| Code | Meaning | Workflow actions available |
|---|---|---|
| 1 | Initial | Submit |
| 2 | Submitted | RevokeSubmit, Approve, RejectApprove |
| 3 | Approved | ProcessDone, DeliveryDone, Archive |
| 4–8 | Extended backend codes | See DocStatusTag fallback |
| 100 | ProcessDone | DeliveryDone, Archive |
| 200 | DeliveryDone | Archive |
| 790 | Legacy archived | — |

---

## i18n Contract

### Namespace mapping
| Namespace key | File | Covers |
|---|---|---|
| `purchaseContract:` | `supplyChain/PurchaseContract.json` | All entity-specific labels |
| `commonElements:` | `foundation/CommonElements.json` | Shared: save, cancel, exit, fields.taxRate *(pending)* |
| `docActionNode:` | `foundation/DocActionNode.json` | Workflow action labels |
| `docInvolveParty:` | `foundation/DocInvolveParty.json` | Party section labels |
| `docFlowNode:` | `foundation/DocFlowNode.json` | Doc chain labels |
| `docMatItem:` | `foundation/DocMatItem.json` | Material item shared labels |

### Required keys in `PurchaseContract.json`
```
pageTitle, messages.saveSuccess, messages.deleteConfirm,
toolbar.newContract, toolbar.export,
status.1 … status.8, status.100, status.200, status.790,
purchaseContract.id, purchaseContract.name, purchaseContract.signDate,
purchaseContract.requireExecutionDate, purchaseContract.priorityCode,
purchaseContract.currencyCode, purchaseContract.grossPrice,
purchaseContract.contractDetails, purchaseContract.note,
purchaseContract.signDate, purchaseContract.corporateCustomerId,
purchaseContractSection, purchaseContractDetailsSection, purchaseContractMaterialItemSection,
purchaseContractHeaderSection, purchaseFromSupplierSection, purchaseToOrgSection,
purchaseContractMaterialItem.id, purchaseContractMaterialItem.name,
purchaseContractMaterialItem.amount, purchaseContractMaterialItem.unitPrice,
purchaseContractMaterialItem.itemPrice, purchaseContractMaterialItem.note
```

---

## Known Issues / Open Decisions

| # | Issue | Decision |
|---|---|---|
| I1 | `CommonElements.json` missing `fields.taxRate` + `fields.discount` | Add `fields` sub-object — 2-line fix |
| I2 | `handleFinish` logs instead of calling API | Wire `createContract`/`updateContract` after API functions added |
| I3 | `executeAction` shows mock toast | Wire `executeDocAction` API call |
| I4 | Delete is in-memory only | Wire `deleteContract` API call |
| I5 | `exitEditor` not called on unmount | Add to EditPage `useEffect` cleanup |
| I6 | Status `ContractStatus` union wider than `DOC_STATUS` | Intentional — see G2 note in plan doc |

---

## Lessons Learned (feed into MIGRATION_PATTERN.md)

1. **Read the old Manager.js before the Editor.js** — Manager.js has the authoritative field names, action codes, and URL prefixes. Editor.js just uses them.

2. **`_en.properties` files contain Chinese unicode escapes** — the file suffix `_en` is misleading. Always decode with `.decode('unicode_escape')`.

3. **`loadModuleEditService` vs `loadModuleViewService`** — Edit acquires a pessimistic lock; View is read-only. Always call `exitEditor` on unmount when using the edit endpoint.

4. **Material items are NOT in the main ServiceUIModel content** — they are loaded and saved separately via their own endpoints. Merge them into `buildPayload()` from `dataSource` state.

5. **Status codes from real data are wider than the workflow codes** — `DOC_STATUS` (5 values) is for workflow logic; `ContractStatus` union (all backend codes) is for type safety. Both are needed.

6. **`CUSTOMERCONTACT` section type handles party panels** — no individual i18n field keys needed for supplier/org fields; `DocInvolveParty.json` covers them.

7. **Backend-driven selects: drop `.html` suffix** — Spring Boot 3 removed suffix pattern matching. Legacy URLs like `getStatusMap.html` return 404. All `getMetaDataUrl` values must be written without `.html` (e.g. `purchaseContract/getStatusMap`). This applies to every module, not just PurchaseContract.

8. **Use `loadMetaRequestForSelect` for all backend-driven selects** — this is the single canonical utility in `ServiceUtilityHelper.ts` that replaces the legacy `loadMetaRequest`. Never call `apiGet` directly from a controller or resolver for select options. Declare `readonly get*URL` properties on the controller class, reference them in `settings: { getMetaDataUrl: this.get*URL }` inside `getDefaultPageMeta()`, and the framework wires the `ProFormSelect.request` function automatically.

9. **`ProFormSelect.request` fires on mount, not on click** — Ant Design Pro uses SWR internally. The fetch happens immediately when the form renders, exactly like the legacy `AsyncControlElement.loadMetaData()` called on mount. No manual `useEffect` or `useState` for options is needed.
