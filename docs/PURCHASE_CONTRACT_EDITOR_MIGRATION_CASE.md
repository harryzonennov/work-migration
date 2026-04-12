# PurchaseContractEditor — Vue 2 to Vue 3 Migration Case Document

## Overview

This document describes the full architecture of the legacy `PurchaseContractEditor` (Vue 2) and maps every key concept to its Vue 3 equivalent. The goal is to guide a **faithful migration** — preserving the same data flow, lifecycle, and rendering patterns while replacing deprecated Vue 2/jQuery APIs with Vue 3 Composition API and Element Plus.

---

## File Inventory

| Legacy File | Role |
|---|---|
| `PurchaseContractEditor.html` | Page shell; mounts Vue instance |
| `PurchaseContractEditor.js` | Vue instance definition; extends mixins; overrides lifecycle methods |
| `PurchaseContractManager.js` | Static data: labels, content defaults, URL helpers, i18n config, DOC_ACTION_CODEs |
| `ServiceUiController.js` | Base mixins: `ServiceBasicControlHelper`, `ServiceEditorControlHelper`, `SerDocumentControlHelper` |
| `AsyncPageElement.js` | Page/section/tab components: `AsyncPage`, `AsyncEditorPage`, `AsyncSection`, `AsyncEditSection`, `AsyncEmbeddedListSection`, etc. |
| `AsyncControlElement.js` | Field components: `AsyncField`, `AsyncForeField`, `AsyncEditUnion`, `InputField`, `Select2Field`, etc. |

---

## Mixin Inheritance Chain

```
ServiceBasicControlHelper.defControlMixin           (base utilities, author, navigation)
  └── ServiceEditorControlHelper.defControlMinxin   (load/save/exit lifecycle, page meta)
        └── SerDocumentControlHelper.defControlMinxin  (doc workflow: submit/approve/revokeSubmit/etc.)
              └── PurchaseContractEditor (Vue instance)
                    mixins: [PurchaseContractManager.labelTemplate, SerDocumentControlHelper.defControlMinxin]
```

`PurchaseContractManager.labelTemplate` is a mixin that injects `label` data into the Vue instance.

---

## Vue Instance Structure (`PurchaseContractEditor.js`)

### `data()`

```js
data: {
  // From PurchaseContractManager.labelTemplate mixin:
  label: PurchaseContractManager.label.purchaseContract,
  // {uuid, id, name, signDate, currencyCode, priorityCode, status, grossPrice,
  //  grossPriceDisplay, contractDetails, note, corporateCustomerId, ...}

  // Defined locally in PurchaseContractEditor.js:
  content: {
    purchaseContractUIModel:   PurchaseContractManager.content.purchaseContractUIModel,
    purchaseToOrgUIModel:      ServiceInvolvePartyHelper.defContentObj (clone),
    purchaseFromSupplierUIModel: ServiceInvolvePartyHelper.defContentObj (clone),
    serviceUIMeta:             {},
    purchaseContractAttachmentUIModelList: []
  },

  // URL strings (set in initDefaultExecutionURL from base mixin):
  loadModuleEditURL:    '../purchaseContract/loadModuleEditService.html',
  loadModuleViewURL:    '../purchaseContract/loadModuleViewService.html',
  newModuleServiceURL:  '../purchaseContract/newModuleService.html',
  saveModuleURL:        '../purchaseContract/saveModuleService.html',
  executeDocActionURL:  '../purchaseContract/executeDocAction.html',
  // ...plus upload/download/split URLs
}
```

### `content` — The Central Data Object

`content` is the single source of truth for all form data. Every field in every tab binds to a sub-model within `content`.

| Sub-model | Contains |
|---|---|
| `purchaseContractUIModel` | Main contract header fields: id, name, signDate, currencyCode, priorityCode, status, grossPrice, grossPriceDisplay, contractDetails, note |
| `purchaseToOrgUIModel` | Party info: the buying organization (internal party) |
| `purchaseFromSupplierUIModel` | Party info: the vendor/supplier (external party) |
| `serviceUIMeta` | Server-side metadata (e.g., excelType, flags) — read-only |
| `purchaseContractAttachmentUIModelList` | List of attachment objects |

The material items tab is NOT stored in `content` — it is loaded separately as an `EMBEDLIST` section and managed by `PurchaseContractMaterialItemEditor.js` as a sub-component.

---

## Lifecycle Flow

### `created()`
```
PurchaseContractEditor.created()
  → SerDocumentControlHelper.created()
      → initSubComponents()        — registers Vue.component("async-editor-page", ...) globally
      → initDefaultDocExecutionURL() — sets executeDocActionURL, getDocActionConfigureListURL
      → initDocActionConfigureList() — loads doc action config from server
  → ServiceEditorControlHelper.created()
      → initCoreUUID()
      → initSubComponents()
      → initAuthorResourceCheck()  — checks access rights
      → initDefaultExecutionURL()  — sets all URL strings
      → getPageMeta()              — calls this.getDefaultPageMeta(), stores in this.meta.pageMeta
  → PurchaseContractEditor.created()
      → initSubComponentsController() — registers sub-component Vue instances
      → NavigationPanelIns.initNavigation('logistics', 'PurchaseContract')
```

### `mounted()`
```
ServiceEditorControlHelper.mounted()
  → setI18nProperties(vm.setI18nCallback)   — loads i18n JSON files, sets label values
  → loadModuleEdit()                          — reads uuid from URL, calls backend
      if processMode == NEW: newModuleService.html → setModuleToUI(content)
      if processMode == EDIT: loadModuleEditService.html → setModuleToUI(content)
```

### `setModuleToUI(content)` — Core Data Hydration
Called after backend response. Uses `vm.$set` for reactivity:

```js
setModuleToUI: function(content) {
  var vm = this;
  vm.$set(vm.content, 'purchaseContractUIModel', content.purchaseContractUIModel);
  vm.$set(vm.content, 'purchaseToOrgUIModel', content.purchaseToOrgUIModel);
  vm.$set(vm.content, 'purchaseFromSupplierUIModel', content.purchaseFromSupplierUIModel);
  vm.$set(vm.content, 'serviceUIMeta', content.serviceUIMeta);
  vm.$set(vm.content, 'purchaseContractAttachmentUIModelList', content.purchaseContractAttachmentUIModelList);
  vm.postUpdateUIModel();  // triggers postUpdate hooks on all page sections
}
```

### `saveModule()`
Defined in `ServiceEditorControlHelper`:
1. Validates via `validateSave()` → `getPageRef().checkValidateSave()` (cascades through all sections)
2. Calls `ServiceUtilityHelper.defSaveModuleWrapper` → POSTs to `saveModuleURL`
3. On success: refreshes the editor view (`refreshEditView`)

### `exitModule()`
Calls `defaultExitEditor(baseUUID, exitModuleURL, exitURL, UIFLAG_STANDARD)` — navigates back to list.

---

## Page Meta System (`getDefaultPageMeta()`)

The entire page layout is defined by this JSON object returned from `getDefaultPageMeta()` in `PurchaseContractEditor.js`. It is consumed by `AsyncEditorPage` / `AsyncPage` to dynamically render tabs, sections, and fields.

### Top-Level Shape

```js
{
  pageCategory: AsyncPage.pageCategory.EDIT,   // 'EDIT' or 'LIST'
  processButtonMeta: { /* toolbar buttons */ },
  tabMetaList: [ /* tabs */ ],
  // optional:
  stepTutorialConfig: { ... }
}
```

### `processButtonMeta` — Toolbar Buttons

```js
processButtonMeta: {
  saveModuleFlag: 'checkEditInInit',        // method name → show/hide Save button
  exitModuleFlag: true,                     // always show Exit button
  processButtonArray: [                     // doc-action buttons injected here
    { actionCodeHeader: 'submit', ... },
    { actionCodeHeader: 'revokeSubmit', ... },
    // ...
  ]
}
```

### `tabMetaList` — Tabs

```js
tabMetaList: [
  {
    tabId: 'purchaseContractSection',
    titleKey: 'purchaseContractSection',   // i18n key
    sectionMetaList: [ /* sections in this tab */ ]
  },
  {
    tabId: 'purchaseContractDetailsSection',
    ...
  },
  {
    tabId: 'purchaseContractMaterialItemSection',
    ...
  }
]
```

Tab indices are defined as constants in `PurchaseContractManager.documentTab`:
```js
documentTab: { purchaseContractSection: 0, purchaseContractDetailsSection: 1, purchaseContractMaterialItemSection: 2 }
```

### `sectionMetaList` — Sections within a Tab

Each section has:
```js
{
  sectionId: 'purchaseContractHeaderSection',
  sectionCategory: 'EDIT',              // 'EDIT', 'EMBEDLIST', 'ATTACHMENT', 'CUSTOMERCONTACT'
  contentPath: 'purchaseContractUIModel', // path into content object
  titleKey: 'purchaseContractSection',
  colClass: 'col-md-12',
  fieldMetaList: [ /* fields */ ],
  // for EMBEDLIST sections:
  embedListConfig: { ... },
  embedProcessButtonMeta: { addCallback: 'addMaterialItem', addLabel: '...', ... }
}
```

### `fieldMetaList` — Fields within a Section

Each field meta entry:
```js
{
  fieldName: 'id',                      // maps to content.purchaseContractUIModel.id
  fieldType: AbsInput.FIELDTYPE.Input,  // Input, Select2, TypeAhead, TextArea, Date, Number, ...
  titleKey: 'id',                       // i18n label key
  required: true,                       // optional
  disabled: 'disableNotInInit',         // method name (string) or boolean
  hidden: false,
  colClass: 'col-md-4',
  settings: { ... }                     // field-type-specific settings
}
```

---

## Action Code Matrix (`getActionCodeMatrix()`)

Defines which document workflow buttons appear on the toolbar. Merged with default definitions from `SerDocumentControlHelper.getDefActionCodeMatrix()`.

```js
getActionCodeMatrix: function() {
  return {
    submit:        { actionCode: DOC_ACTION_CODE.submit },
    revokeSubmit:  { actionCode: DOC_ACTION_CODE.revokeSubmit },
    approve:       { actionCode: DOC_ACTION_CODE.approve },
    countApprove:  { actionCode: DOC_ACTION_CODE.countApprove },
    deliveryDone:  {
      actionCode: DOC_ACTION_CODE.deliveryDone,
      docItemMultiSelectConfig: {
        useCase: DocumentItemMultiSelectFactory.USE_CASE.NEW_DOC_FROM_DOC,
        targetDocType: 'InboundDelivery',
        ...
      }
    },
    processDone:   { actionCode: DOC_ACTION_CODE.processDone },
  };
}
```

Each action button's visibility is controlled by `displayForActionCodeCore(actionCodeHeader, settings)` which checks:
1. Current document status matches the allowed statuses for that action
2. User has the required access role

When a button is clicked → `executeDocActionCore(actionCodeHeader)` → POSTs to `executeDocActionURL`.

### Action Codes (DOC_STATUS integers)
| Action Header | Trigger Status | Result Status |
|---|---|---|
| submit | INITIAL (1) | SUBMITTED (2) |
| revokeSubmit | SUBMITTED (2) | INITIAL (1) |
| approve | SUBMITTED (2) | APPROVED (4) |
| rejectApprove | SUBMITTED (2) | INITIAL (1) |
| countApprove | APPROVED (4) | INITIAL (1) |
| deliveryDone | APPROVED (4) | DELIVERY_DONE |
| processDone | APPROVED (4) or DELIVERY_DONE | PROCESS_DONE |

---

## Component Rendering System

### HTML Template Root
```html
<div id="x_data">
  <async-editor-page ref="corePage" :page-meta="meta.pageMeta">
    <template slot="tab-header">
      <!-- Custom sub-components injected as slot content -->
      <document-item-multi-select-factory ref="multiSelectFactory" ... />
      <split-item-model ref="splitItemModel" ... />
      <material-serial-id-input ref="serialIdInput" ... />
      <purchase-contract-material-item-panel ref="contractMaterialItemPanel" ... />
    </template>
  </async-editor-page>
</div>
```

### Component Hierarchy
```
AsyncEditorPage (async-editor-page)
  └── AsyncPage (async-page)
        ├── Toolbar: processButtonMeta → doc-action buttons
        ├── Tab strip: tabMetaList
        └── For each tab → AsyncSectionFactory
              ├── EDIT → AsyncEditSection
              │         └── AsyncEditUnion
              │               └── AsyncField → renders each fieldMeta
              ├── EMBEDLIST → AsyncEmbeddedListSection
              │               └── AsyncEmbeddedListUnion → ServiceDataTableFrame
              └── ATTACHMENT → AsyncAttachmentSection
```

### Data Flow Through Component Tree
- `pageMeta` prop: passed from root Vue instance (`meta.pageMeta`) down through every layer
- `pageMeta.parentVue`: reference back to root Vue (allows children to call `getBaseUUID()`, `getStatus()`, etc.)
- `parentContent`: the relevant sub-object of `content` for each section (e.g., `content.purchaseContractUIModel`)
- `labelObject`: the i18n label object for the section

Each `AsyncField` reads `fieldMeta.fieldName` and binds to `parentContent[fieldMeta.fieldName]` using `v-model`.

---

## Vue 3 Migration Strategy

### Architecture Decisions

The old system uses a **data-driven rendering engine**: `getDefaultPageMeta()` returns JSON that drives the entire UI. The new system should also be data-driven, but implemented with Vue 3 components.

Two options:
1. **Faithful meta-driven**: Recreate `AsyncPage`, `AsyncSection`, `AsyncField` as Vue 3 components that consume the same pageMeta JSON. Maximum reuse across all editor pages.
2. **Explicit template per module**: Write a direct Vue 3 template for each editor (what was done initially). Simpler but not scalable.

**Recommended: Option 1 (faithful meta-driven)** for long-term migration of 30+ editor pages.

### Component Mapping

| Legacy Component | Vue 3 Equivalent |
|---|---|
| `AsyncEditorPage` | `EditorPage.vue` |
| `AsyncSection` (EDIT) | `EditSection.vue` |
| `AsyncSection` (EMBEDLIST) | `EmbedListSection.vue` |
| `AsyncField` | `FormField.vue` |
| `InputField` | Element Plus `<el-input>` |
| `Select2Field` | Element Plus `<el-select>` |
| `DateField` | Element Plus `<el-date-picker>` |
| `NumberField` | Element Plus `<el-input-number>` |
| `TextAreaField` | Element Plus `<el-input type="textarea">` |
| `DocumentItemMultiSelectFactory` | `CrossDocSelector.vue` (future) |

### Composable Mapping

| Legacy | Vue 3 Composable |
|---|---|
| `ServiceEditorControlHelper.defControlMinxin` | `useDocumentEditor.ts` |
| `ServiceListControlHelper.defControlMixin` | `useDocumentList.ts` |
| `SerDocumentControlHelper.defControlMinxin` | Extended into `useDocumentEditor.ts` |
| `PurchaseContractManager.content` | TypeScript interface + initial value constant |
| `PurchaseContractManager.label` | `src/locales/zh-CN/.../PurchaseContract.json` |
| `PurchaseContractManager.getI18nRootConfig()` | `src/locales/index.ts` registration |
| `getDefaultPageMeta()` | `usePageMeta()` composable or inline `pageMeta` constant |
| `setModuleToUI(content)` | `content.value = loaded` (reactive ref assignment) |
| `getBaseUUID()` | `uuid` from `useRoute().params.uuid` |
| `getStatus()` | `computed(() => content.value.purchaseContractUIModel.status)` |
| `checkEditInInit()` | `computed(() => status.value === DOC_STATUS.INITIAL)` |
| `saveModule()` | `saveModule()` from `useDocumentEditor` |
| `exitModule()` | `exitModule()` from `useDocumentEditor` |
| `executeDocActionCore(code)` | `executeAction(code, title, text)` from `useDocumentEditor` |

### `content` Model in Vue 3

```ts
interface PurchaseContractContent {
  purchaseContractUIModel: PurchaseContract
  purchaseToOrgUIModel: InvolveParty
  purchaseFromSupplierUIModel: InvolveParty
  serviceUIMeta: Record<string, unknown>
  purchaseContractAttachmentUIModelList: Attachment[]
}
```

`content` becomes a `ref<PurchaseContractContent>` in the composable. Template sections bind to `content.purchaseContractUIModel.xxx` directly.

### API Mapping

| Legacy URL | New REST Endpoint |
|---|---|
| `purchaseContract/loadModuleEditService.html` | `GET /api/v1/logistics/purchaseContracts/{uuid}/detail` |
| `purchaseContract/newModuleService.html` | (use initial empty content, POST on save) |
| `purchaseContract/saveModuleService.html` | `POST /api/v1/logistics/purchaseContracts` (new) or `PUT /api/v1/logistics/purchaseContracts/{uuid}` (update) |
| `purchaseContract/executeDocAction.html` | `PUT /api/v1/logistics/purchaseContracts/{uuid}/status/{status}` |
| `purchaseContract/searchTableService.html` | `GET /api/v1/logistics/purchaseContracts?client=xxx` |

### Backend Response Shape

The new `/detail` endpoint returns:
```json
{
  "contract": { ...PurchaseContract fields... },
  "materialItems": [ ...PurchaseContractMaterialItem[] ]
}
```

Note: In the old system, `loadModuleEditService.html` returned the full nested `PurchaseContractServiceUIModel` including `purchaseToOrgUIModel` and `purchaseFromSupplierUIModel`. The new backend does NOT yet return party info — that section needs to be implemented when party info is added to the backend.

---

## Implementation Plan for PurchaseContractEditorView.vue

### Phase 1: Faithful Static Template (current state, needs refinement)
- 3 tabs directly in Vue template (no meta-driven)
- Composable `useDocumentEditor` handles load/save/exit/executeAction
- Content model: `{ contract, materialItems }` (simplified, skips party models)
- Status-based toolbar buttons via computed properties

### Phase 2: Full Content Model
Add `purchaseToOrgUIModel` and `purchaseFromSupplierUIModel` to both frontend and backend when party section is needed.

### Phase 3: Meta-driven (Long Term)
Implement `EditorPage.vue` + `EditSection.vue` + `FormField.vue` that consume `pageMeta` JSON. Then `PurchaseContractEditorView.vue` becomes:

```vue
<EditorPage :page-meta="pageMeta" :content="content" />
```

and `pageMeta` is a constant defined per-module, equivalent to `getDefaultPageMeta()`.

---

## Key Patterns to Preserve

1. **Single `content` ref** — all form data in one reactive object, not split across multiple refs
2. **Status-driven button visibility** — computed properties per action code, checking status
3. **Load → setModuleToUI → postUpdateUIModel** — three-stage hydration after API response
4. **Validate before save** — `validateSave()` cascades through sections before calling backend
5. **Exit returns to list** — `router.push({ name: 'PurchaseContractList' })`
6. **Tab index constants** — `documentTab.purchaseContractSection = 0` etc.
7. **New vs Edit detection** — check `content.purchaseContractUIModel.uuid` is empty for new mode

---

---

## Q & A

### Q1: How are the buttons on the PurchaseContractEditor rendered?

The toolbar buttons are rendered by a dedicated Vue component called `ProcessButtonArray` (`process-button-array`). It is declared in the page template via `AsyncTemplateConstant.PageHeaderTemplate`:

```html
<div v-if="pageMeta.processButtonMeta" class="row process-model">
  <div class="col-lg-12">
    <process-button-array ref="processButtonArray"
      :process-button-meta-array="pageMeta.processButtonMeta"
      :label="pageMeta.labelObject">
    </process-button-array>
  </div>
</div>
```

`ProcessButtonArray` receives `processButtonMeta` — an object map where each key is a button ID (e.g., `save`, `exit`, `placeholder`) — as a prop. It holds an internal `cache.buttonMetaArray` which is the final flat array used to render the buttons.

---

### Q2: What does `processButtonMeta` look like, and who defines it?

`processButtonMeta` is defined inside `getDefaultPageMeta()` in `PurchaseContractEditor.js`. It is a **key-value object** (not an array). The order of keys determines the display order.

Here is the actual definition from `PurchaseContractEditor.js`:

```js
processButtonMeta: {
  save: {
    formatClass: 'displayForEdit',   // method name → controls visibility
    callback: 'saveModule'           // method name → click handler
  },
  placeholder: {
    category: ProcessButtonConstants.placeholderCategory.DOC_ACTION_BTN
    // This is a special marker — gets expanded into all doc-action buttons at runtime
  },
  exit: {
    callback: 'exitModule'
  }
}
```

The `save` and `exit` buttons are **static** — they are always declared and their visibility is controlled by `formatClass` (a method name string that resolves to `displayForEdit`). The doc-action buttons (`submit`, `approve`, etc.) are **not declared explicitly** — instead the special `placeholder` entry with `category: DOC_ACTION_BTN` acts as a **slot** that will be expanded at runtime.

---

### Q3: How are the document action buttons (submit, approve, revokeSubmit, etc.) generated dynamically?

The doc-action buttons go through a **two-stage pipeline**:

**Stage 1 — Fetch action codes from the server (async)**

When the mixin `SerDocumentControlHelper` initializes (`created()`), it calls `initDocActionConfigureList()`, which POSTs to `../purchaseContract/getDocActionConfigureList.html`. The server returns an `actionCodeList` array — the list of action codes that are *allowed for the current document and user*, based on the document's current workflow configuration:

```js
actionCodeList = [
  { actionCode: 102 },  // SUBMIT
  { actionCode: 104 },  // APPROVE
  { actionCode: 107 },  // DELIVERY_DONE
  // ...
]
```

This Promise is stored and later passed into `initProcessButtonFromPageMeta()`.

**Stage 2 — Expand the PLACEHOLDER into concrete buttons**

After i18n is loaded (`setI18nCallback` → `initProcessButtonFromPageMeta()`), `AsyncPage.initProcessButtonFromPageMeta()` is called on the page component (`$refs.corePage`). It waits for the `actionConfigurePromise` to resolve, then calls:

```js
vm.$refs.processButtonArray.convertButtonMetaToArray({
  processButtonMetaArray: pageMeta.processButtonMeta,
  actionCodeList: actionCodeList,        // from server
  actionCodeMatrix: getActionCodeMatrix(), // from PurchaseContractEditor.js
  parentVue: vm.pageMeta.parentVue,
  labelObject: vm.getLabelObject()
});
```

Inside `ProcessButtonArray.convertButtonMetaToArray()`, when it encounters the `placeholder` entry with `category: DOC_ACTION_BTN`, it calls `ProcessButtonArray.genDocActionProcessButtonMeta()`. This function:

1. **Iterates `actionCodeList`** (server-provided)
2. For each `actionCode`, calls `filterActionCodeMatrixByCode(actionCodeMatrix, actionCode)` to find the matching entry in `getActionCodeMatrix()` (declared in `PurchaseContractEditor.js`)
3. Generates a button meta object with:
   - `id`: the action header key (e.g., `"submit"`, `"approve"`)
   - `formatClass`: a closure calling `parentVue.displayForActionCodeCore(actionCodeHeader, ...)` — controls visibility
   - `callback`: a closure calling `parentVue.executeDocActionCore(actionCodeHeader)` — click handler
4. Applies i18n label via `convertButtonMetaCore()` (looks up `submitTitle`, `approveTitle`, etc.)
5. Pushes the button meta into the array

The final `cache.buttonMetaArray` contains the merged list: `[save button, submit button, approve button, ..., exit button]` in declaration order.

---

### Q4: How does each doc-action button know whether to show or hide based on document status?

Each doc-action button's `formatClass` is a **live function** (not a static CSS class). It is generated as:

```js
formatClass: function () {
  var oSettings = {
    renderModel: { [actionCodeHeader]: {} },
    involveTaskStatus: parentVue.involveTaskStatus
  };
  return parentVue.displayForActionCodeCore(actionCodeHeader, oSettings);
}
```

`ProcessButtonArray` calls `itemFormatClass(buttonMeta.formatClass)` in its template to evaluate it each render cycle. The value returned is a CSS class string — either `DISPLAY` (visible) or `HIDDEN` (invisible), as determined by `DocumentManagerFactory.formatDisplayClass()`.

The logic chain for `displayForActionCodeCore(actionCodeHeader, oSettings)` is:

```
displayForActionCodeCore(actionCodeHeader)
  → actionCodeUnit = getActionCodeMatrix()[actionCodeHeader]
  → displayForActionCodeConfig({
      targetActionCode: actionCodeUnit.actionCode,
      renderModel: ...,
      involveTaskStatus: ...
    })
      → ServiceUtilityHelper.displayForActionCodeConfig({
          currentStatus: getStatus() * 1,    // e.g., status = 2 (SUBMITTED)
          docActionConfigureList: [...],      // server-fetched config
          targetActionCode: actionCodeUnit.actionCode,
          accessActionCodeModel: author.actionCode,  // user permissions
          renderModel: ...,
          involveTaskStatus: ...
        })
```

Inside `displayForActionCodeConfig`, the `docActionConfigureList` (fetched from `getDocActionConfigureList.html`) encodes which action codes are valid at each status transition. The current document status is compared against each configure entry to determine if the button should display.

**Summary of factors controlling button visibility:**
| Factor | Source |
|---|---|
| Current document status | `getStatus()` → `content.purchaseContractUIModel.status` |
| Allowed action codes per status | `docActionConfigureList` (from server, loaded once on init) |
| User access rights | `author.actionCode` (Edit, Approve, etc.) |
| Involve task status | `involveTaskStatus` (for approval task flow) |
| Custom render model | `renderModel` (e.g., `{approve: {}}` means approval role required) |

---

### Q5: What is the complete flow from button click to server execution?

When the user clicks a doc-action button (e.g., "Submit"):

```
User clicks "Submit"
  → buttonMeta.callback()                          // closure in ProcessButtonArray
      → parentVue.executeDocActionCore('submit')   // in ServiceBasicControlHelper
          → validateSubmit()                        // cascades validation through all sections
          → getDefActionCodeMatrix()['submit']       // get default warnTitle/warnText
          → getActionCodeMatrix()['submit']          // get actionCode from PurchaseContractEditor.js
          → check: actionCodeUnit.callback?          // custom callback override? no
          → check: actionCodeUnit.docItemMultiSelectConfig?  // multi-select mode? no (for submit)
          → vm.getDocActionModel().initLoad({        // open confirmation modal (DocActionModal)
              warnTitle: 'xxx',
              warnText: 'xxx',
              actionCode: PurchaseContractManager.DOC_ACTION_CODE.SUBMIT
            })
              → user confirms in modal
                → POST to ../purchaseContract/executeDocAction.html
                    → on success: refreshEditView() → reloads page
```

For `deliveryDone` (which has `docItemMultiSelectConfig`), the flow takes the alternative path:
```
  → check: actionCodeUnit.docItemMultiSelectConfig? yes
      → executeDocItemSelectWrapper(actionCodeUnit)
          → docItemMultiSelectConfig.multiSelectFactory.initBatchSelection(...)
              → opens DocumentItemMultiSelectFactory dialog
              → user selects InboundDelivery items
              → on done: fnExecutionDone() → refreshEditView()
```

---

### Q6: How should all of this be replicated in Vue 3?

In Vue 3, there is no dynamic button generation pipeline. The equivalent pattern is **reactive computed properties** per button, which Vue 3 re-evaluates automatically when `status` changes.

**Vue 3 equivalent of the full button system:**

```ts
// composable: useDocumentEditor.ts already has executeAction(actionCode, title, text)

// In PurchaseContractEditorView.vue:

const status = computed(() => content.value.contract.status)

// Button visibility — equivalent of formatClass / displayForActionCodeCore
const canSubmit       = computed(() => status.value === DOC_STATUS.INITIAL)
const canRevokeSubmit = computed(() => status.value === DOC_STATUS.SUBMITTED)
const canApprove      = computed(() => status.value === DOC_STATUS.SUBMITTED)
const canRejectApprove = computed(() => status.value === DOC_STATUS.SUBMITTED)
const canCountApprove = computed(() => status.value === DOC_STATUS.APPROVED)
const canDeliveryDone = computed(() => status.value === DOC_STATUS.APPROVED)
const canProcessDone  = computed(() =>
  status.value === DOC_STATUS.DELIVERY_DONE || status.value === DOC_STATUS.APPROVED)
```

```html
<!-- Template — equivalent of ProcessButtonArray rendering cache.buttonMetaArray -->
<el-button v-if="isEditable" @click="saveModule">保存</el-button>

<!-- doc-action buttons, each controlled by its computed visibility flag -->
<el-button v-if="canSubmit"
  @click="executeAction(DOC_STATUS.SUBMITTED, '提交合同', '确认提交？')">提交</el-button>
<el-button v-if="canRevokeSubmit"
  @click="executeAction(DOC_STATUS.INITIAL, '撤回提交', '确认撤回？')">撤回</el-button>
<el-button v-if="canApprove" type="success"
  @click="executeAction(DOC_STATUS.APPROVED, '审批合同', '确认审批？')">审批</el-button>
<el-button v-if="canCountApprove"
  @click="executeAction(DOC_STATUS.INITIAL, '反审批', '确认反审批？')">反审批</el-button>
<el-button v-if="canDeliveryDone" type="success"
  @click="executeAction(DOC_STATUS.DELIVERY_DONE, '交货完成', '确认交货完成？')">交货完成</el-button>
<el-button v-if="canProcessDone"
  @click="executeAction(DOC_STATUS.PROCESS_DONE, '流程完成', '确认设置流程完成？')">流程完成</el-button>

<el-button @click="exitModule">返回列表</el-button>
```

**Key differences from the legacy system:**

| Aspect | Legacy (Vue 2) | Vue 3 |
|---|---|---|
| Button list source | Server `getDocActionConfigureList.html` + `getActionCodeMatrix()` | Hardcoded per-module computed properties |
| Visibility control | `formatClass()` function returning CSS display class | `v-if` on computed boolean |
| Click handler | Closure calling `executeDocActionCore(header)` then shows modal | Direct `@click` calling `executeAction(code, title, text)` |
| Confirmation dialog | `DocActionModal` (custom component, loaded via `getDocActionModel()`) | `ElMessageBox.confirm()` from Element Plus |
| Dynamic expansion | `PLACEHOLDER` + `genDocActionProcessButtonMeta()` runtime expansion | All buttons statically declared in template |
| Button order | Determined by `processButtonMeta` key order + server code order | Determined by template order |

The Vue 3 approach sacrifices the server-driven dynamic button list (which allowed the backend to control which action codes are available) in favor of simpler, explicit template declarations. This is appropriate since the new REST API uses explicit status-based PUT endpoints rather than a generic `executeDocAction` handler.

---

### Q7: How is the first and second layer navigation bar rendered in the legacy editor, and how does that translate to Vue 3?

#### Legacy: Two-layer navigation architecture

The legacy system renders navigation completely **outside** the Vue editor instance. It is a separate, standalone Vue component mounted on a different DOM element — not inside `#x_data` at all.

**HTML structure in `PurchaseContractEditor.html`:**

```html
<div id="wrapper">
  <!-- Layer 1 + Layer 2 navigation: a SEPARATE Vue instance mounted here -->
  <div id="navigation-panel">
    <navigation-panel></navigation-panel>
  </div>

  <!-- The editor Vue instance is mounted here, independently -->
  <div class="content-page">
    <div id="x_data" class="content init">
      <async-editor-page ref="corePage" :page-meta="meta.pageMeta">
        ...
      </async-editor-page>
    </div>
  </div>
</div>
```

Two entirely independent Vue 2 instances are mounted on the same page: `NavigationPanelIns` on `#navigation-panel` and `dataVar` (the editor) on `#x_data`.

---

#### Layer 1: Top bar (horizontal navigation groups)

The top bar is rendered by the `NavigationGroup` sub-component inside `NavigationPanel`. It displays the **module group icons** (Logistics, Production, Finance, etc.) as horizontal tab icons. The data source is a `navigation.json` file (a top-level group config) loaded via `$.getJSON('js/navigation.json')`.

Each entry in that file maps a `groupId` (e.g., `"logistics"`) to a URL pointing to the second-layer JSON (e.g., `"js/navigationLogistics.json"`).

**Navigation groups config shape:**
```json
{
  "navigationList": [
    { "id": "logistics",   "url": "navigationLogistics.json",   "icon": "...", "idTitle": "logistics" },
    { "id": "production",  "url": "navigationProduction.json",  "icon": "...", "idTitle": "production" },
    { "id": "finance",     "url": "navigationFinance.json",     "icon": "...", "idTitle": "finance" },
    ...
  ]
}
```

The currently active group is highlighted by matching against `groupId` — the first argument passed to `NavigationPanelIns.initNavigation()`.

---

#### Layer 2: Left sidebar (vertical module menu)

The left sidebar is rendered by the `SideBarNavigation` sub-component. Once the active group is known, `NavigationPanel` loads the corresponding navigation JSON — for `"logistics"` that is `js/navigationLogistics.json`.

**`navigationLogistics.json` shape (abridged):**
```json
{
  "navigationList": [
    {
      "id": "PurchaseContract",
      "icon": "nmd nmd-shopping-cart",
      "subNavigationList": [
        { "id": "Inquiry",           "resourceId": "Inquiry",          "url": "InquiryList.html" },
        { "id": "PurchaseContract",  "resourceId": "PurchaseContract", "url": "PurchaseContractList.html" },
        { "id": "PurchaseReturnOrder","resourceId": "PurchaseContract","url": "PurchaseReturnOrderList.html" }
      ]
    },
    {
      "id": "WarehouseManagement",
      "icon": "md md-now-widgets",
      "subNavigationList": [ ... ]
    }
  ]
}
```

Each `navigationList` entry becomes a collapsible **first-level group** in the sidebar (with an icon and group title). Each `subNavigationList` entry becomes a **second-level link** under it.

The currently active sub-item is highlighted by matching against `navigationId` — the second argument to `NavigationPanelIns.initNavigation()`.

---

#### The initialization call in each editor/list page

Every editor page calls this **in `mounted()`**:

```js
// PurchaseContractEditor.js
mounted: function () {
  NavigationPanelIns.initNavigation('logistics', 'PurchaseContract');
}
```

`'logistics'` → selects the active top-bar module group (Layer 1)
`'PurchaseContract'` → highlights the active sidebar item and sub-items (Layer 2)

`NavigationPanelIns` is a globally accessible instance created at the bottom of `NavigationPanel.js`:
```js
var NavigationPanelIns = new NavigationPanel({ el: 'navigation-panel' });
```

The call to `initNavigation(groupId, navigationId)`:
1. Sets `vm.groupId` and `vm.navigationId` on the panel instance
2. Loads i18n labels for the navigation panel
3. Calls `initNavigationCore()` which concurrently:
   - Fetches `navigation.json` → sends `groupId` to `$refs.navigationGroup.initGroupConfigure()` to highlight the active top group icon
   - Loads `navigationLogistics.json` → sends `navigationId` to `$refs.sideBarNavigation.initNaviConfigure()` to expand and highlight the active sidebar item

Authorization filtering is also applied: items with a `resourceId` that the current user has no access to are hidden.

---

#### Vue 3: Navigation is already implemented in `AppLayout.vue`

In the Vue 3 project, both navigation layers are already handled by `AppLayout.vue` (`src/components/common/AppLayout.vue`), which wraps all authenticated routes via the router:

```
/ (AppLayout)
  ├── sidebar (Layer 1 + Layer 2 combined in el-menu)
  ├── header (top bar with user info and logout)
  └── <RouterView /> ← editor views render here
```

The `el-menu` with `router` mode automatically marks the active route — no explicit `initNavigation()` call is needed. Element Plus `<el-menu>` handles both layers:

- **Layer 1 (first layer)**: `<el-sub-menu>` entries — Logistics, Finance, Production, etc.
- **Layer 2 (second layer)**: `<el-menu-item>` entries inside each sub-menu — Purchase Contracts, Inquiries, etc.

```ts
// AppLayout.vue — menuItems definition (the equivalent of navigationLogistics.json + navigation.json)
const menuItems = [
  {
    title: 'Logistics',
    icon: 'Van',
    children: [
      { title: 'Purchase Contracts', route: '/logistics/purchaseContracts' },
      { title: 'Purchase Requests',  route: '/logistics/purchaseRequests' },
      { title: 'Inquiries',          route: '/logistics/inquiries' },
      ...
    ],
  },
  ...
]
```

**Comparison summary:**

| Aspect | Legacy (Vue 2) | Vue 3 |
|---|---|---|
| Navigation mount point | Separate `<navigation-panel>` DOM node, independent Vue instance | `AppLayout.vue` wrapping all routes via Vue Router |
| Layer 1 (top/group) | `NavigationGroup` component, loaded from `navigation.json` | `<el-sub-menu>` entries in `AppLayout.vue` |
| Layer 2 (sidebar items) | `SideBarNavigation` component, loaded from `navigationLogistics.json` | `<el-menu-item>` children inside each sub-menu |
| Active item highlighting | `NavigationPanelIns.initNavigation(groupId, navigationId)` called in each page's `mounted()` | `<el-menu :router="true">` — active route matched automatically by Vue Router |
| Data source | JSON files loaded at runtime via `$.getJSON` | Static `menuItems` array in `AppLayout.vue` |
| Authorization filtering | Server-side ACL filtered from navigation items | Not yet implemented (to be added) |
| Collapsible sidebar | Hamburger button toggling CSS | `collapsed` ref toggling `<el-aside>` width + `<el-menu :collapse>` |

**Key migration insight:** The `initNavigation()` pattern in every legacy editor/list file has **no equivalent call needed** in Vue 3. The navigation bar stays mounted inside `AppLayout.vue` and Vue Router's active-route matching handles highlighting automatically. Each editor view simply needs to be a child route of the layout — which the router config already enforces.

---

## Q8: How is i18n configured and how does it render labels in the PurchaseContractEditor?

### Legacy: Runtime `.properties` loading

**Step 1 — Label shell (`PurchaseContractManager.label`)**

All label keys are pre-declared as empty strings via `ServiceUtilityHelper.getComLabelObject()`:

```js
PurchaseContractManager.label = {
  purchaseContract: ServiceUtilityHelper.getComLabelObject({
    id: '', name: '', status: '', signDate: '', contractDetails: '',
    purchaseContractSection: '', purchaseContractDetailsSection: '',
    purchaseContractMaterialItemSection: '', approveContract: '',
    deliveryDone: '', processDone: '', ...
  }),
  purchaseContractMaterialItem: ServiceUtilityHelper.getComLabelObject({ ... })
}
```

**Step 2 — i18n config (`getI18nRootConfig()`)**

```js
PurchaseContractManager.getI18nRootConfig = function() {
  return {
    i18nPath: 'supplyChain/',          // folder under /admin/i18n/
    mainName: 'PurchaseContract',       // → loads PurchaseContract_en.properties
    labelObjectParent: PurchaseContractManager.label,
    labelObjectMainPath: 'purchaseContract',   // fills label.purchaseContract
    configList: [
      { name: 'PurchaseContractMaterialItem', subLabelPath: 'purchaseContractMaterialItem' },
      { involvePartyPath: 'docInvolveParty' },
      { actionNodePath: 'actionNode' },
      { docFlowNodePath: 'docFlowNode' },
    ]
  }
}
```

**Step 3 — Runtime loading (`mounted()`)**

`ServiceEditorControlHelper.mounted()` calls `setI18nProperties(vm.setI18nCallback)`, which:
1. Fetches `admin/i18n/supplyChain/PurchaseContract_en.properties` via `$.i18n.properties()`
2. Parses each `key=\uXXXX` line and resolves unicode escapes to Chinese characters (despite the `_en` filename, the file contains Chinese)
3. Writes each resolved value into the corresponding empty slot in `PurchaseContractManager.label.purchaseContract`
4. Then fetches `PurchaseContractMaterialItem_en.properties` → fills `label.purchaseContractMaterialItem`
5. Also loads shared configs: `docInvolveParty`, `actionNode`, `docFlowNode`
6. On completion, calls `vm.setI18nCallback()` → triggers `initProcessButtonFromPageMeta()` to build the toolbar

**Step 4 — Mixin injection (`labelTemplate`)**

`PurchaseContractManager.labelTemplate` mixin injects label sub-objects into the Vue instance `data()`:

```js
// labelTemplate mixin
data: function() {
  return {
    label: {
      purchaseContractMaterialItem: PurchaseContractManager.label.purchaseContractMaterialItem,
      actionNode: ServiceActionCodeNodeHelper.defLabelObj,
      docFlowNode: ServiceUtilityHelper.cloneObj(ServiceDocFlowHelper.defComLabelObj),
      docInvolveParty: ServiceUtilityHelper.cloneObj(ServiceInvolvePartyHelper.defComLabelObj),
    }
  }
}
// label.purchaseContract is injected separately by the base mixin via getDefaultPageMeta() → labelObject
```

**Step 5 — Template rendering**

Fields reference labels via the `labelObject` prop passed down through the component tree:
- `AsyncField` uses `labelObject[fieldMeta.titleKey]` to render each field label
- Tab titles use `label.purchaseContract[tab.titleKey]`
- Section titles use `label.purchaseContract[section.titleLabelKey]`

---

### Vue 3: Build-time JSON locale files

**`src/locales/index.ts`**

```ts
import { createI18n } from 'vue-i18n'
import zhCNPurchaseContract from './zh-CN/supplyChain/PurchaseContract.json'
import enPurchaseContract from './en/supplyChain/PurchaseContract.json'

export const i18n = createI18n({
  legacy: false,        // Composition API mode (required for <script setup>)
  locale: 'zh-CN',      // default locale
  fallbackLocale: 'en', // fallback if key missing in zh-CN
  messages: {
    'zh-CN': { purchaseContract: zhCNPurchaseContract },
    en:      { purchaseContract: enPurchaseContract },
  },
})
```

All keys live under the `purchaseContract` namespace. In the editor:

```ts
const { t } = useI18n()
t('purchaseContract.id')               // → "合同编号" (zh-CN) / "Contract No." (en)
t('purchaseContract.approveContract')  // → "审核合同" / "Approve Contract"
```

---

### i18n Keys Used in `PurchaseContractEditorView.vue`

All `t()` calls in the editor and their resolved values:

| i18n Key | zh-CN | en |
|---|---|---|
| `purchaseContract.purchaseContractSection` | 采购合同信息 | Purchase Contract Info |
| `purchaseContract.purchaseContractDetailsSection` | 合同细节 | Contract Details |
| `purchaseContract.purchaseContractMaterialItemSection` | 合同物料项目 | Contract Material Items |
| `purchaseContract.purchaseContractASection` | 甲方信息(本公司) | Party A Info (Our Company) |
| `purchaseContract.purchaseContractBSection` | 乙方信息(供应商) | Party B Info (Supplier) |
| `purchaseContract.id` | 合同编号 | Contract No. |
| `purchaseContract.name` | 合同名称 | Contract Name |
| `purchaseContract.priorityCode` | 优先级别 | Priority |
| `purchaseContract.signDate` | 签署时间 | Sign Date |
| `purchaseContract.requireExecutionDate` | 计划执行时间 | Planned Execution Date |
| `purchaseContract.currencyCode` | 结算货币 | Currency |
| `purchaseContract.note` | 备注信息 | Remarks |
| `purchaseContract.grossPrice` | 总成交价格 | Total Price |
| `purchaseContract.grossPriceDisplay` | 展示总价格 | Display Total Price |
| `purchaseContract.contractDetails` | 合同细节 | Contract Details |
| `purchaseContract.status` | 合同状态 | Contract Status |
| `purchaseContract.refItemId` | 项目编号 | Item No. |
| `purchaseContract.refMaterialSKUId` | 物料编号 | Material No. |
| `purchaseContract.refMaterialSKUName` | 物料名称 | Material Name |
| `purchaseContract.packageStandard` | 物料规格 | Material Spec |
| `purchaseContract.requireShippingTime` | 交货时间 | Delivery Time |
| `purchaseContract.addPurchaseContractMaterialItem` | 新增物料项目 | Add Material Item |
| `purchaseContract.splitItemButtonTitle` | 拆分当前采购项目 | Split Current Item |
| `purchaseContract.approveContract` | 审核合同 | Approve Contract |
| `purchaseContract.approveContractWarnTitle` | 审核合同 | *(missing — falls back to zh-CN)* |
| `purchaseContract.approveContractWarnText` | 合同审核后，基本信息无法更改，确认审核合同？ | *(missing — falls back to zh-CN)* |
| `purchaseContract.countApproveContract` | 反审核 | Counter-Approve |
| `purchaseContract.countApproveContractWarnTitle` | 反审核合同 | *(missing — falls back to zh-CN)* |
| `purchaseContract.countApproveContractWarnText` | 确认反审核该合同，重新进入编辑？ | *(missing — falls back to zh-CN)* |
| `purchaseContract.deliveryDone` | 交货完成 | Delivery Done |
| `purchaseContract.deliveryDoneTitle` | 设置交货完成和入库状态 | Set Delivery Done & Inbound |
| `purchaseContract.processDone` | 流程完成 | Process Done |
| `purchaseContract.processDoneTitle` | 设置流程完成状态 | Set Process Done Status |

**Note:** The 4 warn/confirm dialog keys (`approveContractWarnTitle`, `approveContractWarnText`, `countApproveContractWarnTitle`, `countApproveContractWarnText`) exist in `zh-CN` but are absent from the English locale file. Vue i18n's `fallbackLocale: 'en'` will not help here since the missing keys are in `en` — they will render the zh-CN text in English mode. These should be added to `src/locales/en/supplyChain/PurchaseContract.json`.

---

### Key Differences: Legacy vs Vue 3 i18n

| Aspect | Legacy | Vue 3 |
|---|---|---|
| File format | `.properties` with `\uXXXX` unicode escapes | `.json` with decoded Unicode characters |
| Load timing | Runtime async XHR on `mounted()` | Bundled at build time via Vite static import |
| Namespace | Label object paths: `label.purchaseContract.id` | `vue-i18n` namespaced: `t('purchaseContract.id')` |
| Multi-language | Single file (`_en.properties` = Chinese unicode, no real EN) | Separate `zh-CN/` and `en/` folders |
| Sub-models | Separate `.properties` files per sub-entity | All keys in one `PurchaseContract.json` per locale |
| Fallback | None (empty string if key missing at runtime) | `fallbackLocale: 'en'` — but only covers missing zh-CN keys |
| Reactivity | Labels mutated into empty shell object after async fetch | Reactive via vue-i18n's `t()` composable |
| Decode command | `jquery.i18n.properties.js` runtime decode | Pre-decoded; source decode script in MEMORY.md |

---

## Files to Create/Modify (Summary)

### Frontend
- `src/views/logistics/PurchaseContractEditorView.vue` — main editor (Phase 1 already done, needs party models in Phase 2)
- `src/composables/useDocumentEditor.ts` — already done, covers lifecycle
- `src/locales/zh-CN/supplyChain/PurchaseContract.json` — already done
- `src/locales/en/supplyChain/PurchaseContract.json` — already done

### Backend
- `PurchaseController.java` — `GET /purchaseContracts/{uuid}/detail` — already done
- `PurchaseContractDetail.java` — already done
- Future: add `purchaseToOrgUIModel`, `purchaseFromSupplierUIModel` to detail endpoint when needed
