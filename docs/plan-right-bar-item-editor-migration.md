# Right-Bar Migration Plan — Item Editor DocFlow variant

Generated: 2026-07-08

## Trigger

Legacy `PurchaseContractMaterialItemEditor.html` renders a right sidebar with tab-1 = **DocMatItemFlowTab** (related-document card list) and tab-2 = **DocumentLineTab** (field help). This is different from the root `PurchaseContractEditor.html` which uses tab-1 = **ActionCodeTab** (action code history).

The new React UI has the sidebar scaffolding (`layouts/RightSideBar/*`) but only tab-1=ActionLog + tab-2=HelpDocument. The item-editor variant with a doc-flow tab-1 is **not yet migrated**.

---

## Legacy right-bar architecture

Three Vue components under `admin/js/component/template/`:

| File | Lines | Role |
|---|---|---|
| `RightBarTemplate.js` | 235 | Base "shell": `<div class="side-bar right-bar">` with 4-slot tab bar (`tab1..tab4`) + 4 `<slot>`s |
| `RightBarTimeline.js` | 111 | Wraps RightBarTemplate: tab1=ActionCodeTab, tab2=DocumentLineTab, tab3=TimeLineTab. Used by AsyncEditor (document root editors) |
| `RightBarDocFlow.js` | 105 | Wraps RightBarTemplate: tab1=**DocMatItemFlowTab**, tab2=DocumentLineTab, tab3=TimeLineTab. Used by AsyncItemEditor (item editors) |

**The 4-tab layout is always rendered** by `RightBarTemplate` (`:136-197`). Each `<li class="tab tabN">` uses `v-if="label.tabNTitle"`. When no title, renders `<a><span>&nbsp;</span></a>` (the empty placeholder in the DOM the user showed).

**Tab icons** hardcoded in wrapper `data()`:
- `RightBarDocFlow.js:32-36`:
  - tab1: `ion-merge content-lightblue` (doc flow)
  - tab2: `md md-info-outline content-orange` (help)
  - tab3: `md md-security content-orange` (empty)
- `RightBarTimeline.js:34-38`:
  - tab1: `nmd nmd-play-circle-outline content-red` (action code)
  - tab2/tab3 same as above

---

## How each tab is rendered

### Tab 1 (item editor) — `DocMatItemFlowTab` @ `DocMatItemTab.js` (137 lines)
- CSS class: `docMatItem-3` (line 77) — matches runtime DOM
- Data source: static `DocMatItemFlowTab.loadDocFlowList(oSettings)` @ `:117-135`
- URL: `pageMeta.getDocFlowListURL` → `../purchaseContractMaterialItem/getDocFlowList.html`
- Wired by: `ServiceRightBarPanelHelper.initHelpDocumentWithDocFlow` @ `ServiceHttpRequestHelper.js:6450-6483`
- Item shape (from template binding): `{uuid, documentType, documentTypeValue, id, name, referenceDate, updatedById, updatedByName}`
- `docFlowList[i].uuid === activeKey` → adds class `item-active`
- Click behavior: `navigateToDocMatItem` @ `:62-73` — opens `<targetPage>?uuid=<uuid>&processMode=EDIT` in new tab

### Tab 1 (root editor) — `ActionCodeTab` (~200 lines) — CSS class `actionCode-3`

### Tab 2 (both variants) — `DocumentLineTab` @ `DocumentLineTab.js` (283 lines)
- CSS class: `timeline-3` (line 78) — matches runtime DOM
- `x_anc<fieldName>` id generation: `DocumentLineTab.genAnchorKey(key)` @ `:280-283` returns `'x_anc' + key`
- Data source: static JSON at `admin/i18n/supplyChain/<HelpDocumentName>_<lan>.json`
- Loaded by: `DocumentLineTab.loadI18nDocument({name, path, language, callback})` @ `:105-126`
- Merged by: `mergeToFieldDocument` @ `:194`, `processFieldConfiguration` @ `:216`
- Item shape: `{head: {key, title, iconClass?}, paras: [{key?, text, iconClass?}]}`

### Tab 3 (security) / Tab 4 — empty placeholders. `label.rightBar.tab3Title` / `tab4Title` never set by `setNodeI18nPropertiesCore` @ `ServiceHttpRequestHelper.js:6177-6181, 6303-6307`.

---

## Legacy controller declarations

### Item editor — `PurchaseContractMaterialItemControl.js:12, 71-131`
```js
data: {
    getDocFlowListURL: '../purchaseContractMaterialItem/getDocFlowList.html'
}
getDefaultPageMeta: {
    i18nPath: 'supplyChain/',
    helpDocumentName: ['PurchaseContractMaterialItemHelpDocument'],
}
```
Rendered via `<async-item-editor>` → `HelpDocFlowTemplate()` @ `AsyncPageElement.js:5416, 3711-3718`.
Load trigger: `ServiceItemEditorHelper.initHelpDocumentList` @ `ServiceUiController.js:2383-2389` → `initHelpDocumentWithDocFlow`.

### Root editor — `PurchaseContractEditor.js:198-222`
```js
getDefaultPageMeta: {
    getDocActionNodeListURL: '../purchaseContract/getDocActionNodeList.html',
    helpDocumentName: ['PurchaseContractHelpDocument', 'PurchaseContractMaterialItemHelpDocument'],
}
```
Rendered via `<async-editor-page>` → `HelpDocBarHocTemplate()` @ `AsyncPageElement.js:4478, 3702-3709`.
Load trigger: `ServiceEditorControlHelper` @ `ServiceUiController.js:1725` → `initHelpDocumentWithAction`.

### Difference summary

| Aspect | Root editor | Item editor |
|---|---|---|
| Wrapper | `right-bar-timeline` | `right-bar-doc-flow` |
| Tab-1 icon | `nmd nmd-play-circle-outline content-red` | `ion-merge content-lightblue` |
| Tab-1 content | Action code history | Related-document card list |
| Tab-1 URL | `getDocActionNodeListURL` | `getDocFlowListURL` |
| Tab-2 files | 2 (root + item help) | 1 (item help only) |
| Helper mixin | `ServiceEditorControlHelper` → `initHelpDocumentWithAction` | `ServiceItemEditorHelper` → `initHelpDocumentWithDocFlow` |

---

## Help document JSON shape

Files:
- `admin/i18n/supplyChain/PurchaseContractMaterialItemHelpDocument_{en,zh}.json`
- `admin/i18n/supplyChain/PurchaseContractHelpDocument_{en,zh}.json`

Both `_en` and `_zh` contain Chinese text — not truly localized.

Shape:
```json
{
  "fieldConfiguration": {
    "purchaseContractMaterialItem.unitPrice": {
      "paras": [
        {"text": "物料采购单价 内部价格"},
        {"text": "物料采购单价 为内部价格 只有特定权限的用户才能看到..."}
      ]
    },
    "purchaseContractMaterialItem.itemStatus": {
      "metaParas": {
        "1":   {"text": "初始状态"},
        "2":   {"text": "完成审核..."},
        "299": {"text": "已提交审核"}
      }
    }
  }
}
```

Two entry shapes:
- **`paras[]`** — plain paragraphs
- **`metaParas{code}`** — status-code paragraphs; `code` looked up in `selectMeta.data[fieldKey]` for status labels

---

## Current state in new React UI

### Existing infrastructure (partial scaffold already exists)

| File | Lines | Purpose |
|---|---|---|
| `src/layouts/RightSideBar/index.tsx` | 71 | AntD `<Drawer>` with 2 tabs: `actionLog` + `helpDocument`. Icons mirror legacy |
| `src/layouts/RightSideBar/ActionLogTab.tsx` | 152 | Port of RightBarTimeline tab1. CSS class `actionCode-3` |
| `src/layouts/RightSideBar/HelpDocumentTab.tsx` | 73 | Renders `timeline-3` list from `helpList`. Uses `id={"x_anc" + item.elementId}` @ `:46` |
| `src/layouts/RightSideBar/RightBarContext.tsx` | 48 | React context: `helpList`, `docContext`, `activeKey`, `openHelpDocument(key)` |
| `src/layouts/RightSideBar/types.ts` | 34 | `ElementHelpItem`, `ElementHelpParagraph`, `ActionLogItem`, `DocContext` |
| `src/services/HelpDocumentService.ts` | 60 | `loadHelpDocument(names, module)` → `/i18n/help/<module>/<name>.json`. `buildHelpList` |
| `src/services/ActionLogService.ts` | 67 | Port of `ActionCodeTab.loadActionList` |

### MainLayout wiring — `src/layouts/MainLayout.tsx`
- Line 30: `import RightSideBar` 
- Line 31: `import { RightBarContentProvider }`
- Line 98: `<RightBarContentProvider>` wraps children
- Line 228: `<RightSideBar />` mounted globally

### Root editor wiring — `PurchaseContractEditPage.tsx:23-40`
- Loads both `PurchaseContractHelpDocument` + `PurchaseContractMaterialItemHelpDocument`
- Sets `docContext.docType = 'purchaseContract'`

### Item editor — NOT wired
`PurchaseContractItemEditPage.tsx` — no `useRightBarContent`, no `setHelpList`, no `setDocContext`. This is why the sidebar shows stale root-editor content on the item page.

### Gaps vs legacy `RightBarDocFlow`
1. **No `DocMatItemFlowTab` equivalent** — item editor's "related documents" list missing
2. `RightSideBar/index.tsx` doesn't switch tab-1 content based on page type (root vs item)
3. `AsyncItemEditor.tsx:145` has `{/* TODO: right-bar-doc-flow — not migrated */}` — explicit gap
4. Empty tabs 3/4 not implemented (minor visual gap)
5. `buildHelpList` uses `labelResolver: key => key` — shows raw keys instead of labels
6. `HelpDocumentService.loadHelpDocument` fetches `/i18n/help/<module>/<name>.json` — legacy uses `<name>_<lan>.json`
7. `statusLabelMap`/`metaParas` supported in `buildHelpList` but never populated

---

## Migration plan — phased

### Phase A — Port `DocMatItemFlowTab`

**New file**: `src/layouts/RightSideBar/DocMatItemFlowTab.tsx`
**Legacy source**: `admin/js/component/template/DocMatItemTab.js` (137 lines)

Method-by-method mapping:

| Legacy | Line | New TS | Strategy |
|---|---|---|---|
| `data() → {coreUUID: ''}` | 15-20 | `useState<string>('')` | port verbatim |
| `created() → initCoreUUID()` | 22-28 | `useEffect(() => setCoreUUID(genRamdomPostIndex()), [])` | idiom translation |
| `initCoreUUID()` | 31-34 | inline setter | port verbatim |
| `getParaIconClass(iconClass)` | 36-42 | function | port verbatim |
| `formatItemInfoClass(docMatItem)` | 44-48 | function | port verbatim |
| `formatDocTypeIcon(docMatItem)` | 50-52 | function → `DocumentManagerFactory.formatDocTypeIconClass` | port verbatim |
| `formatDocumentId(docMatItem)` | 55-60 | function | port verbatim |
| `navigateToDocMatItem(docMatItem)` | 62-73 | function → `window.open(...)` | port verbatim |
| template (`docMatItem-3`) | 76-101 | JSX with same class names | template translation |
| static `loadDocFlowList(oSettings)` | 117-135 | Move to `DocFlowService.fetchDocFlowList` | port |

**Props**:
```ts
docFlowList: DocFlowItem[]     // from RightBarContext
activeKey: string              // highlights current doc
```

### Phase B — Extend `RightBarContext` for the doc-flow variant

Edit `RightBarContext.tsx`:
```ts
docFlowList: DocFlowItem[];
setDocFlowList: (list: DocFlowItem[]) => void;
tab1Mode: 'actionLog' | 'docFlow';
setTab1Mode: (mode: 'actionLog' | 'docFlow') => void;
```

Edit `RightSideBar/index.tsx` — switch tab-1 by `tab1Mode`:
- `'actionLog'` → `<ActionLogTab />` (existing), icon `mdi mdi-play-circle-outline content-red`
- `'docFlow'` → `<DocMatItemFlowTab />` (new), icon `mdi mdi-source-merge content-lightblue`

### Phase C — Port `initHelpDocumentWithDocFlow` glue

**New helper**: `src/services/RightBarPanelService.ts`
**Legacy source**: `admin/js/ServiceHttpRequestHelper.js:6450-6483`

Methods:
| Legacy | Line | New TS |
|---|---|---|
| `initHelpDocumentWithDocFlow(oSettings)` | 6450 | port verbatim |
| `initHelpDocumentWithAction(oSettings)` | 6384 | port verbatim |
| `_initHelpDocumentCore(oSettings)` | 6420 | port verbatim |
| `mergeHelpDocConfigure(oSettings, docConfigList)` | 6485 | port verbatim |

### Phase D — Wire `PurchaseContractItemEditPage`

Mirror `PurchaseContractEditPage.tsx:23-40` for the item variant:
```ts
- setHelpList(buildHelpList(doc, key => resolveLabel(labelObject, key)))
    with names=['PurchaseContractMaterialItemHelpDocument'], module='supplyChain'
- setDocContext({ docType: 'purchaseContractMaterialItem', uuid })
- setTab1Mode('docFlow')
- DocFlowService.fetchDocFlowList('/api/purchaseContractMaterialItem/getDocFlowList', uuid)
- setDocFlowList(list)
- Cleanup on unmount
```

Also **fix `buildHelpList` label resolver** in both pages: replace `key => key` with `key => fetchObjValueByPath(labelObject, key)` — matches `DocumentLineTab.processFieldConfiguration:218`.

### Phase E — Fix help-document file path in `HelpDocumentService`

Edit `HelpDocumentService.ts:16-26`:
- Add `language` parameter (default `getLan()` short-code)
- URL: `/i18n/<module>/<name>_<lan>.json` — matches legacy directory structure

Copy JSON help files into new UI static assets or serve from same `/i18n/` path.

### Phase F — Populate `statusLabelMap` for `metaParas`

Edit callers in both edit pages — pass third arg:
```ts
{ 'purchaseContractMaterialItem.itemStatus': {
    '1': labelObject.itemStatus1, '2': labelObject.itemStatus2, ...
} }
```

Legacy: `selectMeta.data[fieldKey]` populated by `updateSelectMetaData` @ `ServiceHttpRequestHelper.js:6183-6190`.

---

## Files to create

| Path | Purpose |
|---|---|
| `src/layouts/RightSideBar/DocMatItemFlowTab.tsx` | 1:1 port of `DocMatItemTab.js` (docFlow tab-1 content) |
| `src/services/RightBarPanelService.ts` | 1:1 port of `ServiceRightBarPanelHelper` methods |
| `src/services/DocFlowService.ts` | 1:1 port of `DocMatItemFlowTab.loadDocFlowList` |
| `src/i18n/help/supplyChain/PurchaseContractMaterialItemHelpDocument_{zh,en}.json` | Copy of legacy |
| `src/i18n/help/supplyChain/PurchaseContractHelpDocument_{zh,en}.json` | Copy of legacy |

## Files to edit

| Path | Change |
|---|---|
| `src/layouts/RightSideBar/RightBarContext.tsx` | Add `docFlowList`, `setDocFlowList`, `tab1Mode`, `setTab1Mode` |
| `src/layouts/RightSideBar/types.ts` | Add `DocFlowItem` type |
| `src/layouts/RightSideBar/index.tsx` | Switch tab-1 by `tab1Mode`; update icon |
| `src/services/HelpDocumentService.ts` | Add language suffix `<name>_<lan>.json` |
| `src/pages/logistics/purchaseContract/PurchaseContractItemEditPage.tsx` | Wire `setHelpList`, `setDocContext`, `setDocFlowList`, `setTab1Mode('docFlow')` |
| `src/pages/logistics/purchaseContract/PurchaseContractEditPage.tsx` | `setTab1Mode('actionLog')`; real label resolver; `statusLabelMap` |
| `src/components/page/AsyncItemEditor.tsx` | Remove TODO comment at :145 |

## Vue 2 → React idiom translations

| Vue 2 | React |
|---|---|
| `Vue.extend({data, methods, template})` | `React.FC` |
| `this.$set(parentCache, 'docFlowList', v)` | `setDocFlowList(v)` via RightBarContext |
| `this.$refs.rightbarDocFlow.initTabTitle()` | `useEffect` on tab-title labels |
| `$nextTick(fn)` | `setTimeout(fn, 0)` or `useEffect` |
| Static `DocMatItemFlowTab.loadDocFlowList` | Module-level function in `DocFlowService` |
| `Vue.component("right-bar-doc-flow", ...)` | Direct import |
| `RightBarTemplate.openSideBar(tab, key)` | `openHelpDocument(key)` (already present) |

## Verification steps

1. `tsc --noEmit` clean in `IntelligentUI/`
2. Open a `PurchaseContract` root editor → tab-1 = action log (red play-circle icon), tab-2 = help with root+item entries
3. Open a `PurchaseContractMaterialItem` editor → tab-1 = doc flow (source-merge icon), tab-2 = help with item entries only
4. Click a doc-flow card → new tab opens with target document's editor page
5. Click a help-icon on a field → sidebar opens, tab-2 activates, `#x_anc<key>` scrolls into view with `item-active` highlight
6. Confirm tab-2 titles resolve to i18n labels (not raw fieldConfiguration keys)
7. Confirm `itemStatus` help entry renders status labels for codes 1/2/4/299/310

## Known gaps (out of scope)

- Empty tab 3 (security) + tab 4 in `RightBarTemplate` — not being ported (2-tab Drawer is practical equivalent)
- Chinese-only help text in `_en.json` files — legacy has same drift; preserved as-is
- Backend `/api/purchaseContractMaterialItem/getDocFlowList` — assumes endpoint exists
- Global `openRightSideBar(tab, key)` invocations from field labels — outside this task
