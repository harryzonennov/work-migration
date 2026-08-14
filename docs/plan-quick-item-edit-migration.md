# Quick-Item-Edit (Bottom Sidebar) Migration Plan

Generated: 2026-07-09

## Trigger

Screenshot: `screenshots/PurchaseContract-QuickItemEdit.png`.

Every document editor page in the legacy UI (e.g. `PurchaseContractEditor.html`) renders a **material items table** with two action icons in the first column of each row:

| Icon | Class | Behavior |
|---|---|---|
| Green pencil | `md md-mode-edit content-green editDetail` | **Full-page edit** — navigates to `<Doc>MaterialItemEditor.html?uuid=…&processMode=EDIT` |
| Orange magnifier | `nmd nmd-search content-orange quickEdit` | **Quick edit** — slides open a bottom panel showing the same item-editor UI *without* leaving the parent page |

The orange-magnifier icon opens a **pop-up bottom sidebar** that mounts the *same* controller/component used by the full-page item editor, but converted to a compact panel form: tabs are collapsed into an embedded tab row inside a single card, and any section marked `pageOnly: true` (notably the DocFlow section) is stripped. The user can save-and-navigate to the prev/next row, expand back to the full page, or close the panel — all without a page load.

This document plans the faithful port of that "quick-item-edit" panel to the new React/TypeScript IntelligentUI.

---

## Legacy architecture — file-by-file

All paths are under `/Users/I043125/work/ThorSalesDistributionUI/admin/`.

### 1. Table action-icon column — `js/component/ServiceDataTableFrame.js`

The row-action buttons are built by `TableFirstRowRender` (`:540-655`). Two of the three canonical buttons matter here:

```js
// :592-603 — full-page edit (pencil)
if (vm.editModule) {
    btnMetaList.push({
        index: 1,
        iconClass: TableFirstRowRender.ICON_CONSTANTS.editButton,   // 'md md-mode-edit content-green editDetail'
        buttonId:  TableFirstRowRender.BUTTON_ID.editButton,         // 'edit-detail'
        callback:  vm.editModule,      // takes uuid
        ...
    });
}
// :604-616 — quick edit (magnifier)
if (vm.editModuleModal) {
    btnMetaList.push({
        index: 2,
        iconClass: TableFirstRowRender.ICON_CONSTANTS.editModalButton, // 'nmd nmd-search content-orange quickEdit'
        buttonId:  TableFirstRowRender.BUTTON_ID.editModalButton,       // 'quick-edit'
        callback:  vm.editModuleModal, // takes (uuid, $event)
        ...
    });
}
```

The `editModule` and `editModuleModal` functions are **injected**, not implemented here — the parent embedded-list section passes them in.

### 2. Function factory — `js/component/basicElements/AsyncPageElement.js`

`AsyncPage.generateSubNodeFunctionMatrix(oSettings)` (`:4652-4707`) builds both handlers from the section meta:

```js
if (refItemName) {                         // any panel-capable section
    result.editModuleModal      = AsyncPage.genFuncEditModuleModal({...});
    result.newModuleModal       = AsyncPage.genFuncNewModuleModal({...});
    result.newModuleFromParentModal = AsyncPage.genFuncNewModuleModalFromParent({...});
}
if (detailedPageUrl) {                     // full-page navigation
    result.editModule           = AsyncPage.genFuncEditModule(detailedPageUrl);
    result.newModule            = AsyncPage.genFuncNewModule({...});
    result.newModuleFromParent  = AsyncPage.genFuncNewModuleFromParent(detailedPageUrl);
}
```

Full-page edit (`:4713-4718`):
```js
AsyncPage.genFuncEditModule = function (oSettings) {
    var detailedPageUrl = oSettings.detailedPageUrl ? oSettings.detailedPageUrl : oSettings;
    return function (uuid) {
        window.location.href = genCommonEditURL(detailedPageUrl, uuid);
    };
};
```

Quick edit — **the key function** (`:4766-4784`):
```js
AsyncPage.genFuncEditModuleModal = function (oSettings) {
    var refItemName             = oSettings.refItemName;              // e.g. 'contractMaterialItemPanel'
    var parentVue               = oSettings.parentVue;                // the editor page vm
    var comRefPanelCompSection  = oSettings.comRefPanelCompSection;   // spacer node the panel pushes above content
    var postRefresh             = oSettings.postRefresh;              // reload the whole editor page after save
    var postRefreshContent      = oSettings.postRefreshContent;       // reload only this section after save
    return function (uuid, $event) {
        var refItemPanel = parentVue.$refs[refItemName];              // handle to the pop-bottom-panel Vue instance
        refItemPanel.loadPanel({
            baseUUID: uuid,
            processMode: PROCESSMODE_EDIT,
            errorHandle: parentVue.errorHandle,
            $event: $event,
            compensateSection: comRefPanelCompSection,
            postRefreshContent: postRefreshContent,
            postRefresh: postRefresh
        });
    };
};
```

Same file: `genFuncNewModuleModal` (`:4797-4817`) is the counterpart for the **"add new" button** inside the embedded-list-section header (`embedProcessButtonMeta.newModuleModalFlag: true` — used e.g. by `PurchaseContractEditor.js:341-395`). It calls `refItemPanel.loadPanel({processMode: PROCESSMODE_NEW, baseUUID: getBaseUUIDCallback(), ...})`.

`genFuncNewModuleModalFromParent` (`:4830-…`) exists for tree-hierarchies (parent → new-child inside panel) and is out of scope for the material-item flow.

Wiring in the embedded-list section (`AsyncPageElement.js:603-642` — `initTableConfig`): when `sectionMeta.editModuleModalFlag === true`, it fills `editModuleModal` from the section meta or, if absent, from `defFunctionMatrix.editModuleModal`. This means each editor only needs to set `refItemName` + `editModuleModalFlag: true` and the panel opener is auto-generated.

### 3. Bottom-sidebar shell — `js/component/basicElements/PopBottomPanel.js` (252 lines)

`<pop-bottom-panel>` is a fixed `<footer class="foot-wrapper …">` element positioned `position:fixed; bottom:0`. Its own template only renders:
- a busy-loader
- an "expand-block" arrow icon (`ion-arrow-down-b`) used to close the panel
- a `<slot name="content">` that holds whatever payload the caller drops in
- an internal render-loop (`renderPopButton`, `setTimeoutRenderPopButtonWrapper`) that repositions the button 20× at 100 ms intervals as content mounts

Key public methods:

| Method | What it does |
|---|---|
| `showPanel({elementClass, compensateSection, postLoadPanel})` | Closes all other open `.foot-wrapper` panels (`closeOtherPanel`), toggles `hide-display` → `show-display`, invokes `postLoadPanel` |
| `hidePanel()` | Toggles `show-display` → `hide-display`, calls `compensateSection.resetToDefault()` |
| `showBusyLoading` / `hideBusyLoading` | Delegates to embedded `<busy-loader>` |
| `refreshPanel()` | Kicks the reposition loop |

Also in that file: `PopPanelCompensateSection` — a hidden spacer rendered inside the main content stream. When the panel opens, the panel measures its own height and sets `line-height` on `.compensate` so the parent scroll area is padded, preventing the bottom rows from being hidden behind the panel.

### 4. Item-panel mixin — `js/ServiceHttpRequestHelper.js:6905-7041`

`ServicePopBottomPanelHelper.defPopButtomPanelMinxin` is a Vue mixin every `<Xxx>Panel` component consumes. Public API used by external code:

```js
loadPanel(oSettings)             // Entry point — called by genFuncEditModuleModal
  ├─ openPanel(oSettings)        // stores postPanel, shows loader, calls popBottomPanel.showPanel
  └─ loadPanelCore(oSettings)
       ├─ openPanel(oSettings)
       └─ parentControl.loadModule({
              baseUUID, $event, processMode,
              pageCategory: PANEL_DOWN,             // ← constant that forces panel mode downstream
              errorHandle, postRefresh, postRefreshContent, postPanel, paras,
              postLoadData: (data) => { hideBusyLoading(); setTimeout(refreshPanel, 500); },
              promiseAllCallback
          })
hidePanel() / showBusyLoading / hideBusyLoading / refreshPanel / changeUIHandler / controlErrorHandle
```

`pageCategory: PANEL_DOWN` is `DocumentConstants.StandardProperty.PageCategory.PANEL_DOWN = 2` (`DocumentConstants.js:263`). This constant is what the downstream code checks to strip page-only sections.

### 5. Panel-form item component — `js/component/basicElements/ServiceUiController.js:2889-3067`

`ServiceItemControlHelper.defEditorPanelMinxin` is the mixin every `<Xxx>MaterialItemPanel.js` uses. Each panel component is just:

```js
// e.g. PurchaseContractMaterialItemPanel.js (14 lines total)
var PurchaseContractMaterialItemPanel = Vue.extend({
    name: "purchase-contract-material-item-panel",
    mixins: [
        ServiceItemControlHelper.defEditorPanelMinxin,   // adds pop-bottom-panel + loadPanel + convertToPanelPageMeta
        PurchaseContractMaterialItemControl              // same controller used by the full item editor page
    ]
});
```

Two things `defEditorPanelMinxin` does that make it different from the full-page `defControlMinxin`:

**5a. Reports pageCategory = EDITPANEL** (`:2928-2930`):
```js
getPageCategory: function () { return AsyncPage.pageCategory.EDITPANEL; },  // = 5
```

This value flows into `pageMeta.pageCategory` and is used later by `AsyncSectionFactory.checkForSectionCore` to skip page-only sections at render time (`AsyncPageElement.js:3597-3614`):
```js
if (pageCategory * 1 === AsyncPage.pageCategory.EDITPANEL) {
    if (ServiceUtilityHelper.checkEqualsTrue(pageOnly)) return;   // skip
}
```

**5b. Flattens tabs into an embedded tab row** — `convertToPanelPageMeta` (`:2970-2998`):
```js
convertToPanelPageMeta: function(oSettings) {
    var pageMeta = oSettings.pageMeta;
    // Filter out pageOnly sections BEFORE flattening
    var rawSectionMetaList = AsyncPage.getOverallSectionList({
        pageMeta: pageMeta, panelFilter: true                     // ← the filter switch
    });
    var baseSectionMeta = ServiceUtilityHelper.cloneObj(vm.getFirstSectionMeta(pageMeta));
    if (rawSectionMetaList.length === 1) {                        // single-section shortcut
        pageMeta.tabMetaList = undefined;
        pageMeta.sectionMetaList = [baseSectionMeta];
        return pageMeta;
    }
    baseSectionMeta.embeddedTabMetaList = rawSectionMetaList;     // ≥2 sections → 1 card with an embedded tab bar
    ServiceCollectionsHelper.traverseListInterrupt(baseSectionMeta.embeddedTabMetaList, function(embeddedTab) {
        embeddedTab.tabId = embeddedTab.tabId ? embeddedTab.tabId : 'tab' + embeddedTab.sectionId;
        AsyncSectionFactory.fillSectionProperty(embeddedTab);
    });
    baseSectionMeta.fieldMetaList = undefined;
    pageMeta.tabMetaList = undefined;
    pageMeta.sectionMetaList = [baseSectionMeta];
    return pageMeta;
}
```

And the corresponding filter (`AsyncPageElement.js:4560-4590`):
```js
AsyncPage.getOverallSectionList = function (oSettings) {
    var pageMeta = oSettings.pageMeta;
    var panelFilter = oSettings.panelFilter;
    var sectionMetaList = [];
    // For every section under every tab (or top-level), drop item.pageOnly if panelFilter=true
    …
};
```

**5c. Template** (`:3056-3066`):
```html
<div class="serviceItemPanel">
    <pop-bottom-panel ref="popBottomPanel">
        <template v-slot:content>
            <div :id="coreControlId">
                <async-editor-control ref="corePage" :page-meta="meta.pageMeta"
                    @changeUI="changeUIHandler" @save="saveModule" @navToEdit="navToEdit">
                </async-editor-control>
            </div>
        </template>
    </pop-bottom-panel>
</div>
```

`exitModule` is overridden to `hidePanel()` (`:2960-2964`) so the panel's "close" button reuses the same handler as the full-page "exit" button but does not navigate.

**5d. `navToEdit` — the "expand" button** (`:3047-3053`):
```js
navToEdit: function () {
    var vm = this;
    vm.navToEditAPI({
        uuid: vm.getBaseUUID(),
        parentNodeUUID: vm.getParentUUID(),
    });
},
```
`navToEditAPI` (in `defControlMinxin`) navigates to `<vm.getEditPageURL()>?uuid=…&processMode=EDIT`, moving the user from the compact panel to the full-page editor with the same record loaded.

### 6. Item nav toolbar — `js/component/basicElements/ItemQuickAction.js` (223 lines)

Renders inside the panel header as `<span class="item-quick-action">…</span>` (see the "展开" area in the screenshot). Three action icons controlled by `processMode`:

- **`PROCESSMODE_NEW`** — single `ion-plus-round` "save & new" icon (calls `updateHandler` → `newModuleHandler(baseUUID)`)
- **`PROCESSMODE_EDIT`** — two arrows: `ion-arrow-left-a` (save & prev) + `ion-arrow-right-a` (save & next)

The prev/next arrows work by DOM traversal — from the panel's own `itemEvent.target` they walk up `parents("table tr")`, then `.prev()` / `.next()`, then trigger `click()` on the sibling row's `i.quickEdit` icon. This chains: save → close → sibling's quickEdit opens → panel refreshes with the sibling's data.

`initConfig(oSettings)` accepts `{processMode, seperatorFlag, baseUUID, updateHandler, itemEvent, newModuleHandler}` — these are wired by the item panel when the user clicks "save".

### 7. Where the panel actually renders — `PurchaseContractEditor.html:47`

The pattern every doc-editor HTML uses: mount the panel component **once, inside `<async-editor-page>`'s `v-slot:tab-header>`** slot at page level, next to the other factories:

```html
<async-editor-page ref="corePage" :page-meta="meta.pageMeta">
    <template v-slot:tab-header>
        <document-item-multi-select-factory ref="multiSelectFactory"></document-item-multi-select-factory>
        <split-item-model ref="splitItemModel"></split-item-model>
        <material-serial-id-input ref="serialIdInput"></material-serial-id-input>
        <purchase-contract-material-item-panel ref="contractMaterialItemPanel">
        </purchase-contract-material-item-panel>              <!-- ← this is the bottom-sidebar host -->
    </template>
</async-editor-page>
```

Then the section meta refers to it by name (`PurchaseContractEditor.js:347`): `refItemName: 'contractMaterialItemPanel'`. This is exactly the string `genFuncEditModuleModal` looks up on `parentVue.$refs`.

---

## Section-meta contract driving the panel

Two flags in the embedded-list section meta control panel wiring:

| Flag / property | Effect |
|---|---|
| `refItemName: 'contractMaterialItemPanel'` | Vue-ref name of the panel component mounted on the editor page |
| `detailedPageUrl: 'PurchaseContractMaterialItemEditor.html'` | Enables the **pencil (full-page edit)** icon — `editModule` becomes `location.href = url + '?uuid=…'` |
| `editModuleFlag: true` | Show the pencil |
| `editModuleModalFlag: true` | Show the magnifier — enables the **quick-edit panel** |
| `embedProcessButtonMeta.newModuleModalFlag: true` | The section's "Add" button opens the panel in `PROCESSMODE_NEW` instead of navigating |

And on each **section meta inside the panel's own page meta**:

| Flag | Effect |
|---|---|
| `pageOnly: true` | Section is skipped in the panel (`AsyncPage.getOverallSectionList({panelFilter: true})`) — this is how `PurchaseContractMaterialItemControl.js:112-116` hides the `refDocumentSection` (DOCFLOW) when opened as a panel |

Concrete example — `PurchaseContractMaterialItemControl.js` `getDefaultPageMeta` (`:71-132`):
```js
tabMetaList: [{
    tabId: 'purchaseContractMaterialItem',
    sectionMetaList: [
        PurchaseContractManager.getItemControlConfig(),         // basic info — shown in panel
        PurchaseContractManager.getItemControlPriceSection(),   // pricing — shown in panel
        {
            sectionId: 'refDocumentSection',
            pageOnly: true,                                      // ← HIDDEN in panel
            sectionCategory: AsyncSection.sectionCategory.DOCFLOW,
            parentContentPath: 'purchaseContractMaterialItemUIModel',
            titleLabelKey: 'refDocumentSection'
        }
    ]
}, {
    tabId: 'purchaseContractMaterialItemAttachment',             // attachment tab — flattened into embedded-tab-list
    ...
}]
```

When opened as full page: all three sections render across two tabs.
When opened as panel: DocFlow is stripped, the two remaining tabs (main + attachment) become an `embeddedTabMetaList` under a single card.

---

## Current state of the new UI

Grep + directory scan (`/Users/I043125/work2/IntelligentUI/src/`) confirms:

- ✅ `AsyncEmbeddedListSection.tsx` **already renders a three-icon actions column** in an editor context (`:172-203`) — a magnifier (`mdi-pencil-box-outline content-orange`, tooltip `commonElements:quickEditTitle`), a pencil (`mdi-pencil content-green`, tooltip `editDetailTitle`), and a delete icon. But the magnifier is currently wired to `action.startEditable(itemId)` — i.e. **inline row-editing** via `EditableProTable`, not to a bottom panel.
- ✅ `AsyncItemEditor.tsx` exists (151 lines) and consumes `pageMeta.tabMetaList` / `pageMeta.sectionMetaList` — same shape as legacy. Would need to consume `pageMeta.sectionMetaList[0].embeddedTabMetaList` to render the flattened panel form.
- ✅ `PurchaseContractItemEditPage.tsx` renders the full-page item editor via `AsyncEditorPage`. Its controller (`usePurchaseContractMaterialItemController.ts`) — reuse this **1:1** as the panel's data controller.
- ✅ `AsyncPage.pageCategory` — need to verify constant exists; if not, add `EDITPANEL: 5` to match legacy.
- ❌ **No `PopBottomPanel` / bottom-sidebar shell** — must be built.
- ❌ **No `defEditorPanelMinxin` equivalent** — the panel controller wrapper that flattens `pageMeta` and injects `pageCategory=EDITPANEL`.
- ❌ **No `ItemQuickAction` (prev/next/save-and-new toolbar)**.
- ❌ **The magnifier icon in `AsyncEmbeddedListSection.tsx:186-190` currently triggers inline editing, not the panel** — needs to be re-wired to open the panel when the parent editor declares one.

---

## Migration plan

### Rule alignment

Contract Rules 1–4 apply. Each new TS module must map 1:1 to a legacy JS source; every method/property in the legacy file must be ported (renaming / adding logic is forbidden). Where the migration produces framework-adapter callback properties (e.g. `onOpen`, `onClose`), they replace Vue `$refs`/`$emit` and are acceptable as adapter hooks per Rule 2's last bullet.

### File map — legacy → new

| Legacy JS | New TS/TSX | Purpose |
|---|---|---|
| `PopBottomPanel.js` (252 L) | `src/components/page/PopBottomPanel.tsx` | The sliding footer shell + spacer node |
| `ItemQuickAction.js` (223 L) | `src/components/control/ItemQuickAction.tsx` | Prev/Next/SaveNew nav row |
| `ServicePopBottomPanelHelper.defPopButtomPanelMinxin` @ `ServiceHttpRequestHelper.js:6905-7041` | `src/composables/usePopBottomPanel.ts` (hook) + `src/components/page/PopBottomPanelHost.tsx` (host wrapper) | Mixin equivalent — `loadPanel` / `openPanel` / `hidePanel` / `showBusyLoading` |
| `ServiceItemControlHelper.defEditorPanelMinxin` @ `ServiceUiController.js:2889-3067` | `src/pages/…/PurchaseContractMaterialItemPanel.tsx` + `src/hooks/useItemPanelController.ts` | The panel-form wrapper around the existing item controller (`getPageCategory`, `convertToPanelPageMeta`, `exitModule=hidePanel`) |
| `PurchaseContractMaterialItemPanel.js` (14 L) | `src/pages/logistics/purchaseContract/PurchaseContractMaterialItemPanel.tsx` | Thin composition — reuse `usePurchaseContractMaterialItemController` + item-panel wrapper |
| `AsyncPage.genFuncEditModuleModal` @ `AsyncPageElement.js:4766-4784` | Method on the panel host: `openForEdit(uuid, event)` | Called from `AsyncEmbeddedListSection`'s magnifier icon |
| `AsyncPage.genFuncNewModuleModal` @ `:4797-4817` | Method on the panel host: `openForNew(baseUUID, event)` | Called from the "Add" button in `embedProcessButtonMeta` |
| `AsyncPage.getOverallSectionList({panelFilter:true})` @ `:4560-4590` | `AsyncPageHelpers.getOverallSectionList` (extend) | Add the `panelFilter` argument if not already present |
| `AsyncSectionFactory.checkForSectionCore` `pageOnly` check @ `:3597-3614` | `AsyncSectionFactory.tsx` — mirror the check | Ensures `pageOnly: true` sections never render when `pageCategory=EDITPANEL` |

### Phase A — bottom-sidebar shell (foundation, no per-doc code)

**A1. `PopBottomPanel.tsx`** — 1:1 port of `PopBottomPanel.js`.
- Public API preserved: `showPanel({ elementClass?, compensateSection?, postLoadPanel?})`, `hidePanel()`, `showBusyLoading()`, `hideBusyLoading()`, `refreshPanel()`, `closeOtherPanel()`, `renderPopButton()`, `expandCallback()`.
- Vue `$refs.refBusyLoader.showBusyLoading()` → local React state `busyLoading` (or forwarded to an `AbsBusyLoader` component if one exists).
- `initLayoutEvents` (attach `click` handler on `.content .nav-tab-row.content-page-nav .nav.nav-tabs li.tab` → close panel) → port literally to a `useEffect`; do **not** invent a nicer event bus.
- `renderPopButton`/`setTimeoutRenderPopButtonWrapper` reposition loop — port as a `useEffect` + `setInterval` that runs 20× at 100 ms exactly as legacy does.
- CSS classes `foot-wrapper`, `fixed-foot`, `hide-display`/`show-display`, `expand-wrapper`, `footer-content`, `pop-panel-compensate-section` — preserved verbatim. Add corresponding rules to `styles/legacy-panel.css` (or extend existing `legacy` bucket) so the sliding animation matches. Position: `position: fixed; bottom: 0; z-index: 90`.
- **Ref API**: expose `showPanel`/`hidePanel`/`showBusyLoading`/`hideBusyLoading`/`refreshPanel` via `useImperativeHandle` — this is the direct analog of legacy `$refs.popBottomPanel.showPanel(...)`.

**A2. `PopPanelCompensateSection.tsx`** — 1:1 port of the second Vue class in `PopBottomPanel.js` (`:200-253`). Spacer node with `resetToDefault()` / `setLineheight(px)`. Mount inside the editor page's scroll flow (near the bottom of `AsyncEditorPage`'s content area) so opening the panel doesn't hide the last table row.

**A3. `usePopBottomPanel` hook (or `PopBottomPanelHost` wrapper)** — port `ServicePopBottomPanelHelper.defPopButtomPanelMinxin`:
- Wraps a `PopBottomPanel` ref + a payload slot.
- `loadPanel(oSettings)` — entry from external callers.
- `loadPanelCore(oSettings)` — calls `openPanel(oSettings)`, then `parentControl.loadModule({baseUUID, processMode, pageCategory: PANEL_DOWN, errorHandle, postRefresh, postRefreshContent, postPanel, postLoadData: () => { hideBusyLoading(); setTimeout(refreshPanel, 500); }})`.
- `openPanel(oSettings)` — stores `postPanel`, calls `showBusyLoading()`, then `popBottomPanel.showPanel(...)` with `postLoadPanel = ServicePopBottomPanelHelper.buildDefProcessLabel(label)`.
- `openRightSideBar(key)`, `changeUIHandler`, `controlErrorHandle`, `saveModule` — all ported literally as adapter methods.

**Definition of done — Phase A**: `<PopBottomPanel>` can be mounted in a Storybook page and controlled via ref to slide up/down. The compensate spacer resizes correctly.

### Phase B — item-panel controller wrapper (per-doc scaffold)

**B1. Constants** — verify / add:
```ts
// src/components/page/AsyncPage.ts (or PageMeta.ts)
export const PageCategory = {
    EDIT: 1, LIST: 2, TREE: 3, INIT: 4, EDITPANEL: 5,      // from AsyncPageElement.js:4484-4490
} as const;
```
And in `DocumentConstants.ts`:
```ts
StandardProperty.PageCategory = { PANEL_DOWN: 2, PANEL_RIGHT: 3, ... }  // DocumentConstants.js:263
```

**B2. `AsyncPageHelpers.getOverallSectionList`** — port the `panelFilter` argument (`AsyncPageElement.js:4560-4590`). Drop sections where `pageOnly === true` when `panelFilter === true`.

**B3. `AsyncSectionFactory` `pageOnly` check** — add the EDITPANEL guard from `AsyncPageElement.js:3597-3614` so that even if a caller forgets to filter, individual sections respect their own `pageOnly` flag when `pageCategory === PageCategory.EDITPANEL`.

**B4. `useItemPanelController` hook** — the reusable panel-controller wrapper. Given:
- an existing per-doc controller factory (e.g. `useContractItemEditController`),
- a `PopBottomPanelHost` ref,

it exposes:
- `loadPanel({baseUUID, processMode, errorHandle, $event, compensateSection, postRefreshContent, postRefresh})`
- `buildAsyncPageMeta()` which reads the existing item controller's `buildAsyncPageMeta` output, then applies `convertToPanelPageMeta` (see B5) and overrides `pageCategory` to `EDITPANEL`.
- `exitModule()` = `hidePanel()`
- `navToEdit()` → `navigate(<detailedPageUrl>?uuid=<getBaseUUID()>)` — moves user to full-page editor with same record.

**B5. `convertToPanelPageMeta(pageMeta)`** — port `defEditorPanelMinxin.convertToPanelPageMeta` @ `ServiceUiController.js:2970-2998` literally:
1. Call `getOverallSectionList({pageMeta, panelFilter: true})` — strips `pageOnly` sections.
2. If 1 section remains: `pageMeta.tabMetaList = undefined; pageMeta.sectionMetaList = [firstSection]`.
3. Else: clone first section, set `embeddedTabMetaList = filteredSections`, fill missing `tabId`, blank out `fieldMetaList`, blank out `pageMeta.tabMetaList`, put the wrapper section as the sole `sectionMetaList[0]`.

**B6. `AsyncItemEditor.tsx` — render `embeddedTabMetaList`**. When a section carries `embeddedTabMetaList`, render an inner tab bar (Ant `Tabs`) inside the section's card, with one tab per `embeddedTabMetaList[i]`. Each tab renders that entry's own section content via `AsyncSectionFactory`.

**Definition of done — Phase B**: given the existing `usePurchaseContractMaterialItemController`, a fake harness can call `panelHost.loadPanel({baseUUID: 'abc', processMode: 'EDIT'})` and see the panel slide up with two tabs (basic + attachment) — DocFlow section is hidden.

### Phase C — per-doc panel components (thin composition, one file per doc)

**C1. `PurchaseContractMaterialItemPanel.tsx`** — the 1:1 port of the 14-line `PurchaseContractMaterialItemPanel.js`. Because we don't have Vue mixins, it composes:
- `useItemPanelController(useContractItemEditController(), popBottomPanelRef)` — the reusable wrapper
- A ref exposing `{ loadPanel, hidePanel, showBusyLoading, hideBusyLoading, refreshPanel, navToEdit }` via `useImperativeHandle` so parent editors can trigger it exactly as Vue's `$refs.contractMaterialItemPanel.loadPanel(...)` does.
- Renders `<PopBottomPanelHost>` wrapping an `<AsyncItemEditor pageMeta={panelPageMeta}>`.

**Definition of done — Phase C, per doc**: opening the panel from a table row in `PurchaseContractEditPage` displays the same fields as the full-page item editor, minus the DocFlow tab, with an "expand" button that navigates to the full page.

**C2. Repeat for every doc-type material item panel**:
- `PurchaseRequestMaterialItemPanel`, `PurchaseReturnMaterialItemPanel`, `InquiryMaterialItemPanel`, `QualityInspectMatItemPanel`, `InboundDeliveryItemPanel`, `OutboundDeliveryItemPanel`, `InventoryCheckItemPanel`, `InventoryTransferItemPanel`, `WasteProcessMaterialItemPanel`, `WarehouseStoreItemPanel`, plus non-supplyChain: `SalesContractMaterialItemPanel`, `SalesReturnOrderItemPanel`, `FinAccountMaterialItemPanel`, `ProductionOrderItemPanel`, various BOM item panels.
- Each is a 30–50-line composition file — no per-doc business logic; reuses the same controller as the full-page editor.

### Phase D — wire the magnifier icon in `AsyncEmbeddedListSection`

Current code (`AsyncEmbeddedListSection.tsx:186-190`):
```tsx
<Tooltip key="inline-edit" title={i18n.t('commonElements:quickEditTitle')} placement="top">
    <a onClick={() => action?.startEditable?.(itemId)}>
        <i className="mdi mdi-pencil-box-outline content-orange" />
    </a>
</Tooltip>
```

Rewire so that when the section meta declares `editModuleModalFlag: true` **and** `refItemName` (i.e. a panel is mounted on the parent editor), the magnifier calls `parentController.openItemPanel(refItemName, uuid, event)` instead of `startEditable`. Fallback (no `refItemName` or no `editModuleModalFlag`) → keep inline-edit behavior (or hide the icon if the section didn't opt in to inline-edit either).

**D1. Extend the section-meta contract** — mirror legacy:
```ts
interface EmbeddedListSectionMeta {
    ...
    refItemName?: string;
    detailedPageUrl?: string;
    editModuleFlag?: boolean;       // show pencil (navigate to page)
    editModuleModalFlag?: boolean;  // show magnifier (open panel)
    embedProcessButtonMeta?: {
        newModuleModalFlag?: boolean;  // Add button opens panel in NEW mode
        ...
    };
    ...
}
```

**D2. Extend `parentController` API (pageMeta.parentController) with `openItemPanel(refItemName, uuid, event)`** — resolves the panel ref by name from a per-page panel-registry context (see D3), then calls `.loadPanel({baseUUID: uuid, processMode: 'EDIT', $event: event, errorHandle, compensateSection, postRefreshContent, postRefresh})`. Directly mirrors `AsyncPage.genFuncEditModuleModal`.

**D3. Panel registry context** — since React has no `$refs.<name>`, add `PanelRegistryContext` at the editor-page level:
```ts
// src/pages/context/PanelRegistryContext.tsx
type PanelRefs = Record<string, ItemPanelRef>;  // { contractMaterialItemPanel: <ref>, ... }
// provide `register(name, ref)` / `resolve(name): ItemPanelRef | null`
```
Each `<PurchaseContractMaterialItemPanel>` mounted inside the editor page calls `useEffect(() => register('contractMaterialItemPanel', ref), [])`. The section's magnifier calls `resolve('contractMaterialItemPanel').loadPanel(...)`.

**D4. Rewire the "Add" button** — same idea for `embedProcessButtonMeta.newModuleModalFlag`. When set, the section header's Add button calls `panelRef.loadPanel({baseUUID: parentContent.uuid, processMode: 'NEW', ...})` instead of navigating to `<detailedPageUrl>?processMode=NEW`.

**Definition of done — Phase D**: on `PurchaseContractEditPage`, clicking a row's orange magnifier slides up the compact editor; the pencil still navigates to the full page; the Add button opens the panel in NEW mode.

### Phase E — ItemQuickAction toolbar (prev / next / save-and-new)

**E1. `ItemQuickAction.tsx`** — 1:1 port of `ItemQuickAction.js` (223 L). Preserve method names literally: `initConfig`, `initCoreUUID`, `setI18nProperties`, `setI18nActionProperties`, `displayForNextItem`, `saveNavToNew`, `updateSync`, `saveNavToNext`, `saveNavToPrev`, `saveNavToSiblings`, `getSiblingsItem`, `displayForPrevItem`, `displayInNew`, `displayInUpdate`. `Vue.$set` → plain assignment (Contract §6). jQuery DOM traversal (`$(itemEvent.target).parents("table tr").prev()`) → port literally with `event.target.closest('tr')` + `previousElementSibling`. The sibling row's magnifier is discovered by class selector `.quickEdit` — preserve that class name on the icon in `AsyncEmbeddedListSection`. Trigger click via `HTMLElement.click()`.

**E2. Mount inside the item-panel header** — the panel's toolbar area (matching legacy `defEditorPanelMinxin` template's default content) shows `<ItemQuickAction :display-flag="…">` alongside the "Save" / "Exit" / "Expand" buttons. Wire `updateHandler = () => saveModule()` and `newModuleHandler = (baseUUID) => panelRef.loadPanel({baseUUID, processMode: 'NEW'})`.

**Definition of done — Phase E**: the prev/next arrows save the current row, close the panel, and immediately open the sibling row's panel — matching legacy chain-editing UX.

### Phase F — verify per-doc `pageOnly` markers are ported

For each item controller in the new UI, verify the `pageOnly: true` flag is preserved on any section that must be hidden in panel form. Grep legacy sources:
```
grep -rn "pageOnly: true" /Users/I043125/work/ThorSalesDistributionUI/admin/js
```
Expected hits: every `<Xxx>MaterialItemControl.js` `refDocumentSection` (the DocFlow section). Also check `attachmentSection` on some doc types — some may need `pageOnly` too depending on legacy behavior; port whatever the legacy source says.

### Phase G — CSS / class alignment

- `.foot-wrapper.fixed-foot` — fixed positioning, sliding transition. Copy CSS from legacy `pages.css` / `components.css` for these selectors.
- `.hide-display` / `.show-display` — legacy toggle; keep exact class names so `PopBottomPanel`'s jQuery-style toggle port works.
- `.expand-wrapper.footer-style` with `.ion-arrow-down-b.expand-block` — the drop-down arrow used as close.
- `.pop-panel-compensate-section .compensate` — spacer.
- `.item-quick-action` / `.item-quick-action-icon` / `.saveNavToNext` / `.saveNavToPrev` / `.saveNavToNew` — nav icons.
- `.serviceItemPanel` — the panel content wrapper; anchor for `.serviceItemPanel .nav.nav-pills li.tab` click handler.

### Phase H — smoke tests / verification

1. Open a purchase contract → material item table → click magnifier on row 1 → panel slides up with basic + attachment tabs (no DocFlow) → save → panel closes and table row refreshes → click next-arrow instead → panel closes, then reopens with row 2's data.
2. Open a purchase contract → material item table → click "Add" (with `newModuleModalFlag: true`) → panel slides up in NEW mode → save-new arrow saves and reopens blank panel for another item.
3. Click "Expand" button (`navToEdit`) → user lands on `PurchaseContractMaterialItemEditor` full page with the same record.
4. Switch top-level tabs on the parent editor → panel closes automatically (legacy `initLayoutEvents` behavior — verify port).
5. Open a purchase contract, open the panel, switch to `refDocumentSection` — verify tab is not present.
6. Open full-page item editor directly (`/logistics/purchaseContract/:parent/items/:uuid`) — verify `refDocumentSection` (DocFlow) IS present.

---

## Open decisions / callouts

- **Panel registry vs prop drilling**: the legacy code uses Vue's `$refs` by-name lookup (`parentVue.$refs[refItemName]`). React idiom is context. Recommend `PanelRegistryContext` (D3) — it's the smallest surface that keeps section-meta strings (`refItemName: 'contractMaterialItemPanel'`) working literally, so per-doc migration keeps parity with legacy source.
- **Inline-edit vs quick-edit**: the current `AsyncEmbeddedListSection` uses the magnifier for `EditableProTable.startEditable`. That's a *deviation* from legacy. Two options:
  - **(A)** magnifier = quick-panel (matches legacy); use a different icon for inline row-edit if that feature is retained.
  - **(B)** magnifier does both: inline-edit when `!refItemName`, panel when `refItemName` set.
  - Recommend **(A)** — the whole point of this migration is fidelity to legacy; if inline-editing is wanted, add a fourth icon rather than overload the existing one.
- **Multiple panels on one page**: legacy handles this via `PopBottomPanel.closeOtherPanel()` — it toggles every `.foot-wrapper` when opening. Port literally so only one panel is visible at a time. This also mirrors the legacy invariant that switching parent tabs closes all panels.
- **`postRefresh` vs `postRefreshContent`**: legacy passes both; after save, the panel invokes `postRefreshContent` to reload the specific section's data, then optionally `postRefresh` for a whole-page reload. Preserve both hook points; wire them to the parent controller's existing "reload list" and "reload page" methods respectively.
- **Skipping panel migration for doc-types that don't declare `editModuleModalFlag`**: some doc-types in the legacy code do not enable the magnifier icon (only pencil). No panel component is required for those. Grep `editModuleModalFlag: true` across `admin/js/**/*Editor.js` to build the exact per-doc scope.

---

## Line inventory (for effort estimation)

Legacy source to be ported:

| File | Lines |
|---|---|
| `PopBottomPanel.js` | 252 |
| `ItemQuickAction.js` | 223 |
| `ServiceHttpRequestHelper.js:6905-7041` (`defPopButtomPanelMinxin`) | ~140 |
| `ServiceUiController.js:2889-3067` (`defEditorPanelMinxin`) | ~180 |
| `AsyncPageElement.js` panel-related (`genFuncEditModuleModal`, `genFuncNewModuleModal`, `genFuncNewModuleModalFromParent`, `getOverallSectionList` panelFilter branch, `checkForSectionCore` EDITPANEL branch) | ~120 |
| Per-doc panel components (`<Xxx>MaterialItemPanel.js`, ~14 lines each × ~15 doc-types) | ~210 |
| **Total legacy** | **~1125** |

Estimated new React code (net-new, controllers reused): `PopBottomPanel.tsx` + `PopPanelCompensateSection.tsx` + `usePopBottomPanel.ts` + `useItemPanelController.ts` + `ItemQuickAction.tsx` + `PanelRegistryContext.tsx` + one thin panel component per doc-type + amendments to `AsyncEmbeddedListSection`, `AsyncItemEditor`, `AsyncSectionFactory`, `AsyncPageHelpers`. Roughly the same order of magnitude — ~1200 new lines.
