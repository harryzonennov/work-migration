# Migration Plan — DOCFLOW section (`refDocumentSection` / `关联业务凭证信息`)

Generated: 2026-07-08

## Trigger

Legacy declaration in `admin/js/supplyChain/PurchaseContractMaterialItemControl.js:110-116`:
```js
{ sectionId: 'refDocumentSection', pageOnly: true,
  sectionCategory: AsyncSection.sectionCategory.DOCFLOW,  // 8
  parentContentPath: 'purchaseContractMaterialItemUIModel',
  titleLabelKey: 'refDocumentSection' }
```

Controller-level `getDocFlowListURL: '../purchaseContractMaterialItem/getDocFlowList.html'` (line 12).

## MIGRATION CONTRACT (enforced)

1. Port ALL methods, properties, computed, statics — same names, same signatures.
2. No new methods / no rename / no new business logic.
3. Grep the legacy file before writing every method.
4. One TS file = one legacy JS file.
5. jQuery/Bootstrap/Vue-only idioms → `// TODO: legacy <desc>` stubs but the method must exist.

---

## Legacy source map

### Section dispatch
- `AsyncSection.sectionCategory.DOCFLOW = 8` — `AsyncPageElement.js:1455`
- Dispatch in `AsyncSectionFactory` — `AsyncPageElement.js:3196-3298, 3364-3519`
  - `computedRefDocFlowSection: 'docFlowSection' + coreUUID` (:3196-3198)
  - `Vue.component("doc-flow-section", DocFlowSection)` (:3239)
  - `checkForDocFlowSection(sectionCategory)` (:3295-3298, :3364-3365)
  - Template `<doc-flow-section v-if="checkForDocFlowSection(...)"/>` (:3519)

### DocFlowSection — `AsyncPageElement.js:2041-2236`
- **data** (:2045-2054): `cache.flowDocFieldList=[]`, `meta.flowDocConfigure=''`, `getDocumentTypeMapURL='../serviceDocumentSetting/getDocumentTypeMap.html'`
- **computed** (:2058-2091):
  - `comRefDocFlow` — `"ref-doc-flow-" + coreUUID`
  - `comTitleIcon` — defaults to `ion-merge content-portlet-title`
  - `comHiddenReservedDoc / comHiddenPrevDoc / comHiddenPrevProfDoc / comHiddenNextDoc / comHiddenNextProfDoc` — `convertFlowDocConfigureProperty(...)` for each
  - `comHiddenDocFlow` — AND of the 5 above
- **lifecycle** (:2095-2108): `mounted` → `initFlowDocFields(sectionMeta.flowDocConfigure)` + `getRefDocFlowArray().initLoadDocFlow({...})`
- **methods** (:2112-2206):
  - `initSubComponents` — registers `doc-flow-widget-array`
  - `convertFlowDocConfigureProperty(propertyName)` — returns `true` unless truthy
  - `getBaseUUID()` — reads `pageMeta.parentVue.getBaseUUID()` or `?uuid=`
  - `getDocFlowListURL()` — `pageMeta.parentVue.getDocFlowListURL`
  - `getRefDocFlowArray()` — `$refs[comRefDocFlow]`
  - `initFlowDocFields(flowDocConfigure)` — sets meta + populates fieldList
  - `getSingleRowDocFields(oSettings)` — 3-field row (Type/Id/Name) per direction
  - `getDefFlowDocFields()` — concats 5 rows: RESERVED_BY, DIREC_PREV, DIREC_PREV_PROF, DIREC_NEXT, DIREC_NEXT_PROF
- **template** (:2210-2234): portlet + async-field row + `docFlowContainer` + `doc-flow-widget-array`

### DocFlowUnionWidget — `DocFlowWidget.js:4-168`
- **props** (:6-33): `baseUid, processIndex, updatedByName, targetPage, updatedDate, activeFlag, refUiModel, docId, docIdPath, documentType, documentTypeValue, pullLeftFieldArray, pullRightFieldArray`
- **data** (:35-42): `cache={}, coreUUID=''`
- **computed** (:44-80): `comControlId` (`widget-union-<uuid>`), `comDocId`, `comDocTypeIcon`, `comDocTypeValue`, `comActiveClass`, `comActiveTitleIconClass`
- **lifecycle** (:82-84): `created` → `initCoreUUID()`
- **methods** (:86-108): `initCoreUUID`, `getDocId`, `getFieldValue`
- **template** (:111-167): matches the DOM the user attached — `.widget-simple.card-box` + `.pull-left.popItem-label` + doc type icon + `<a target="_blank">` + updater/timestamp + `.lean-hr-seperate.border-linkblue` + left/right field columns

### DocFlowWidgetArray — `DocFlowWidget.js:174-503`
- **props** (:176-181): `colClass, baseUid`
- **data** (:183-190): `cache.docFlowArray=[], coreUUID=''`
- **computed** (:193-199): `comControlId` (`widget-array-<uuid>`), `comColClass` (default `col-md-3`)
- **lifecycle** (:202-205): `created` → `initCoreUUID()` + `initSubComponents()`
- **methods** (:212-487):
  - `initCoreUUID`, `initSubComponents`
  - `initLoadDocFlow(oSettings)` — HTTP GET, envelope `{content:[]}`
  - `getDocFlowFieldConfigureWrapper(rawDocFlow)` — dispatches to `DocumentManagerFactory.getDocumentManagerDef(docType).getDefaultDocFlowFieldConfigure(rawDocFlow)`
  - `processDocFlowList(oSettings)` — build promises, then `cache.docFlowArray = genUnionRowArray(...)`
  - `genUnionRowArray(docFlowList)` — chunk by `calculateRowSize(comColClass)` (default 4)
  - `checkNewRow(index)`, `getToDetailedPage(oSettings)`, `getDocLabelObject(...)`
  - `genPromiseToConvert(oSettings)` — load i18n, walk fieldMetaList
  - `addDefaultFieldMeta(oSettings)` — prepend `parentDocId` + `documentType`
  - `processFieldMeta(oSettings)` — resolve label/value/icon, split left/right
  - `getIconClassMap(oSettings)`, `getI18nConfig(oSettings)`
  - `checkDefPullLeft(fieldMeta)`, `getDefPullLeftFieldNameList()` — fixed 10-name list

### API endpoint (legacy backend)
- URL: `GET ../purchaseContractMaterialItem/getDocFlowList.html?uuid=<uuid>`
- Controller: `ThorsteinLogistics/.../PurchaseContractMaterialItemEditorController.java:225-229`
- Core: `ThorsteinPlatform/.../ServiceDocumentComProxy.java:810-868` — returns sorted `List<ServiceDocumentExtendUIModel>`

---

## What's already migrated

| New file | Line | Coverage | State |
|---|---|---|---|
| `src/components/page/AsyncSection.tsx` | 34 | `SectionCategory.DOCFLOW = 8` | ✅ done |
| `src/components/page/AsyncSectionFactory.tsx` | 6, 110-112 | Imports + dispatches DocFlowSection | ✅ done |
| `src/components/page/DocFlowSection.tsx` | whole file (97 lines) | Skeleton: `coreUUID`, `flowDocFieldList`, `comRefDocFlow`, `comTitleIcon`, portlet + AsyncField + placeholder `<div data-component="doc-flow-widget-array">` | ⚠️ partial — TODOs 46-49, 70, 86-87 |
| `src/i18n/locales/{zh,en}/foundation/DocFlowNode.json` | 23 keys | Matches legacy `ServiceDocFlowHelper.defComLabelObj` | ✅ done |
| `src/services/DocumentManagerFactory.ts` | 315, 421, 221 | `formatDocTypeIconClass`, `getDocumentManagerDef`, `getDocumentTypeIconArray` | ✅ done |
| `src/services/SystemStandrdMetadataProxy.ts` | 157, 168 | `formatDocFlowDirectionIconClass`, `formatDocFlowDirectionPrefix` | ✅ done |
| `src/services/ServiceStringHelper.ts` | 18 | `headerToUpperCase` | ✅ done |
| `src/services/logistics/PurchaseContractManager.ts` | 139+ | `DOC_ACTION_CODE`, status icons, `getI18nItemConfig` | ⚠️ missing `getDefaultDocFlowFieldConfigure` + `getDefaultDocumentItemEditorPage` |
| `src/pages/logistics/purchaseContract/PurchaseContractMaterialItemController.tsx` | 118-272 | 3 tabs (identity/pricing/attachment) | ⚠️ DOCFLOW section not added |

## What's missing / partial

1. `DocFlowUnionWidget.tsx` — entire file missing
2. `DocFlowWidgetArray.tsx` — entire file missing
3. `DocFlowSection.tsx` — 7 methods + 6 computed stubbed
4. `PurchaseContractManager.getDefaultDocFlowFieldConfigure` + `.getDefaultDocumentItemEditorPage` — not ported
5. Wiring in `PurchaseContractMaterialItemController.getDefaultPageMeta()` — no `refDocumentSection` entry
6. `getDocFlowListURL` on controller — not exposed
7. Backend endpoint `POST/GET purchaseContractMaterialItem/getDocFlowList` — missing from IntelligentPlatform
8. i18n key `refDocumentSection` in PurchaseContract locale
9. CSS classes verification in `overrides.scss`

---

## Migration plan — phased

### Phase A — Widget primitives (bottom-up)

**A.1** Create `src/components/page/DocFlowUnionWidget.tsx` — 1:1 port of `DocFlowWidget.js:4-168`.
- Preserve all 13 props verbatim
- Preserve `coreUUID`, `comControlId`, `comDocId`, `comDocTypeIcon`, `comDocTypeValue` (empty body kept), `comActiveClass`, `comActiveTitleIconClass`
- Preserve `initCoreUUID`, `getDocId`, `getFieldValue`
- JSX template: all class names verbatim (`widget-simple card-box`, `popItem-label`, `popover-info`, `cs-admin-union`, `lean-hr-seperate border-linkblue`, `pull-left/right`, `m-l-15`, `m-r-10`, `m-t-20`, `value-content content-darkblue`, `md md-chat/perm-contact-cal/history`)

**A.2** Create `src/components/page/DocFlowWidgetArray.tsx` — 1:1 port of `DocFlowWidget.js:174-503`.
- Class-based (like `DocumentItemMultiSelect.ts`) so `initLoadDocFlow` is callable via ref
- Preserve every member: `initCoreUUID`, `initSubComponents` (stub), `initLoadDocFlow`, `getDocFlowFieldConfigureWrapper`, `processDocFlowList`, `genUnionRowArray`, `checkNewRow`, `getToDetailedPage`, `getDocLabelObject`, `genPromiseToConvert`, `addDefaultFieldMeta`, `processFieldMeta`, `getIconClassMap`, `getI18nConfig`, `checkDefPullLeft`, `getDefPullLeftFieldNameList`
- HTTP call: `ServiceUtilityHelper.httpRequest({...})` — response envelope `{content:[]}`

Verify: `tsc --noEmit` clean.

### Phase B — DocFlowSection full port

Edit `src/components/page/DocFlowSection.tsx`:
- Remove TODO placeholders (lines 46-49, 70, 86-87)
- Add all 6 missing computed: `comHiddenReservedDoc`, `comHiddenPrevDoc`, `comHiddenPrevProfDoc`, `comHiddenNextDoc`, `comHiddenNextProfDoc`, `comHiddenDocFlow`
- Add all 7 missing methods: `initSubComponents` (stub), `convertFlowDocConfigureProperty`, `getBaseUUID`, `getDocFlowListURL`, `getRefDocFlowArray`, `initFlowDocFields`, `getSingleRowDocFields`, `getDefFlowDocFields`
- `mounted` (via `useEffect`): call `initFlowDocFields` + `getRefDocFlowArray().initLoadDocFlow(...)`
- Wire `useRef<DocFlowWidgetArrayHandle>` to `<DocFlowWidgetArray/>`

### Phase C — PurchaseContractManager statics

Edit `src/services/logistics/PurchaseContractManager.ts`:
- Add `static getDefaultDocumentItemEditorPage(): string` — port from `PurchaseContractManager.js:392-405`
- Add `static getDefaultDocFlowFieldConfigure(refUIModel)` — port from `:542-563`

### Phase D — Controller wiring

Edit `src/pages/logistics/purchaseContract/PurchaseContractMaterialItemController.tsx`:
- Add `getDocFlowListURL: 'purchaseContractMaterialItem/getDocFlowList'` (drop legacy `.html`)
- In `getDefaultPageMeta()`, append to `identity` tab's `sectionMetaList`:
  ```ts
  {
    sectionId: 'refDocumentSection',
    pageOnly: true,
    sectionCategory: SectionCategory.DOCFLOW,
    parentContentPath: 'purchaseContractMaterialItemUIModel',
    titleLabelKey: 'refDocumentSection'
  }
  ```
- Expose `getDocFlowListURL` on `pageMeta` top-level

### Phase E — Backend (flag only)

Endpoint missing on `IntelligentPlatform`:
- `POST/GET /purchaseContractMaterialItem/getDocFlowList?uuid=<uuid>` returning `{content: ServiceDocumentExtendUIModel[]}`
- Legacy reference: `ThorsteinLogistics/.../PurchaseContractMaterialItemEditorController.java:225-229` + `ServiceDocumentComProxy.java:810-868`
- Separate migration ticket

### Phase F — i18n

Edit `src/i18n/locales/{zh,en}/logistics/PurchaseContract.json`:
- Add under `purchaseContractMaterialItem`: `refDocumentSection` = `关联业务凭证信息` / `Related Business Documents`
- Decode from `admin/i18n/supplyChain/PurchaseContract_zh.properties`

### Phase G — Styles

Edit `src/styles/overrides.scss`:
- Verify + add (as TODO stubs) if missing: `widget-simple`, `card-box`, `popItem-label`, `popover-info`, `cs-admin-union`, `lean-hr-seperate`, `border-linkblue`, `docFlowContainer`, `content-{link,light,dark,grey}blue`, `content-orange`, `pull-{left,right}`, `m-{l,r,t}-{15,10,20}`, `value-content`

---

## Vue 2 → React idiom translations

| Vue 2 | React equivalent |
|---|---|
| `Vue.extend({mixins:[AsyncEditSection]})` | Function component reusing `PortletHeadEle`, `calSecColClass`, `getSectionElementId` |
| `data()` | `useState` per field |
| `computed` | `useMemo` or plain derived const |
| `mounted` | `useEffect(() => ..., [])` |
| `created` | `useState(() => genRamdomPostIndex())` or class constructor |
| `vm.$set(vm.meta, k, v)` | `setMeta(prev => ({...prev, [k]: v}))` |
| `vm.$refs[key]` | `useRef` + `forwardRef`/`useImperativeHandle` |
| `vm.$http.get` | `ServiceUtilityHelper.httpRequest(...)` |
| `Vue.component(...)` | stub method + direct JSX import |
| `v-for` / `v-if` / `v-show` | `.map` / conditional JSX |

## API envelope

- Request: `GET purchaseContractMaterialItem/getDocFlowList?uuid=<uuid>`
- Response: `{content: ServiceDocumentExtendUIModel[]}`
- Each element: `{uuid, id, documentType, documentTypeValue, targetPage, refUIModel: {uuid, id, updatedByName, updatedDate, parentDocId, parentDocName, refMaterialSKUId, refMaterialSKUName, amount, parentDocStatus, parentDocStatusValue, itemStatus, ...}}`

## Verification steps

1. `tsc --noEmit` clean
2. Navigate to purchase contract item editor — "关联业务凭证信息" portlet renders on identity tab
3. Header shows i18n label + refresh/collapse/close buttons
4. Inline field row of prev/next/reserved triplets rendered disabled
5. Fetches `getDocFlowList?uuid=<itemUUID>` on mount
6. Response cards render 4-per-row (`col-md-3`), each `.widget-simple.card-box`
7. Active card gets `.active` class + filled bookmark icon
8. No console errors from missing helpers
9. Diff test: legacy JS vs new TS shows only type annotations + Vue→React idiom rows

## Known gaps (out of scope)

1. `getDefaultDocFlowFieldConfigure` for other 8 doc managers (InboundDelivery, PurchaseRequest, QualityInspectOrder, OutboundDelivery, InventoryTransferOrder, WasteProcessOrder, WarehouseStore, RepairProdOrder) — needed only when their cards appear in the DOCFLOW list. Fall-through: `undefined` → card skipped (legacy behavior preserved at `DocFlowWidget.js:245`).
2. Backend endpoint on IntelligentPlatform — separate ticket
3. Card popover/hover behavior — Bootstrap popover markup handled elsewhere
4. `RightBarDocFlow` help-document drawer — different view, not exercised by `refDocumentSection`

## File paths

- **Legacy sources**:
  - `/Users/I043125/work/ThorSalesDistributionUI/admin/js/component/basicElements/AsyncPageElement.js`
  - `/Users/I043125/work/ThorSalesDistributionUI/admin/js/component/basicElements/DocFlowWidget.js`
  - `/Users/I043125/work/ThorSalesDistributionUI/admin/js/supplyChain/PurchaseContractMaterialItemControl.js`
  - `/Users/I043125/work/ThorSalesDistributionUI/admin/js/supplyChain/PurchaseContractManager.js`

- **New UI to edit**:
  - `/Users/I043125/work2/IntelligentUI/src/components/page/DocFlowSection.tsx`
  - `/Users/I043125/work2/IntelligentUI/src/services/logistics/PurchaseContractManager.ts`
  - `/Users/I043125/work2/IntelligentUI/src/pages/logistics/purchaseContract/PurchaseContractMaterialItemController.tsx`
  - `/Users/I043125/work2/IntelligentUI/src/i18n/locales/{zh,en}/logistics/PurchaseContract.json`

- **New files to create**:
  - `/Users/I043125/work2/IntelligentUI/src/components/page/DocFlowUnionWidget.tsx`
  - `/Users/I043125/work2/IntelligentUI/src/components/page/DocFlowWidgetArray.tsx`

- **Backend (flagged)**:
  - Missing: `/Users/I043125/work2/IntelligentPlatform/src/main/java/com/company/IntelligentPlatform/logistics/**/PurchaseContractMaterialItemController.java` endpoint
  - Legacy ref: `/Users/I043125/work/ThorsteinLogistics/src/main/java/net/thorstein/logistics/Controller/Model/SupplyChain/PurchaseContractMaterialItemEditorController.java:225-229` + `/Users/I043125/work/ThorsteinPlatform/src/main/java/platform/foundation/LogicManager/Common/ServiceDocumentComProxy.java:810-868`
