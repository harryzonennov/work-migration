# Plan: Migrate the legacy "Action Guidance" (Step Tutorial) to IntelligentUI

> **Status:** 🟡 Planned — not yet started
> **Created:** 2026-07-07
> **Companion docs:** [`plan-bootstrap-integration.md`](./plan-bootstrap-integration.md), [`plan-right-bar-migration.md`](./plan-right-bar-migration.md), [`MIGRATION_CONVERSATIONS.md`](./MIGRATION_CONVERSATIONS.md)

---

## 1. What is Action Guidance?

On every Document Editor page (Purchase Contract, Sales Contract, Delivery, …) two visual affordances tell the user what to do next based on the current document status:

1. **Hint Message Bar** at the top of the page — shallow-blue-green background with a bell icon and a dismissible X button. The wording depends on the current status.
   Example (Purchase Contract, status = INITIAL, `zh_CN`):
   > 🔔 提示:确认信息编辑完成后,可以提交到下一个流程    ✕

2. **Focused Process Button** — one of the workflow action buttons in the process button row (保存 / 提交审核 / 退出 …) is visually highlighted with a bright glow (`focus-info` CSS class), telling the user which button is the recommended next step.

Both pieces are driven from the **same config**: `getDefActionCodeMatrix()` marks certain action headers with `focusButtonInGuide: true` + `notifyLabelKeyInGuide: '<i18n-key>'`, and the runtime picks whichever headers are currently visible/enabled for the document's status.

The legacy feature is a **generic framework** called **Step Tutorial** — the same pipeline supports arbitrary custom tutorials (e.g. `QualityInspectOrderEditor` declares a full `stepTutorialConfig` object).

---

## 2. Legacy pipeline — how it actually works

### 2.1 Three-part architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│ Part A — Config source                                               │
│   ServiceUiController.js:673  getDefActionCodeMatrix()               │
│   PurchaseContractEditor.js:107  getActionCodeMatrix()               │
│     ↓                                                                │
│ Part B — Config assembler                                            │
│   ServiceUiController.js:2153  initDocumentStepTutorial()            │
│     merges per-doc matrix with default matrix, produces              │
│     stepTutorialConfig with configureUnit[] entries                  │
│     ↓                                                                │
│ Part C — Runtime renderer                                            │
│   ServiceHttpRequestHelper.js:8918  ServiceStepTutorialHelper        │
│     .initStepTutorial()                                              │
│       ↓                                                              │
│   ServiceHttpRequestHelper.js:8565  ServiceMessageBarHelper          │
│     .generateMessageBar()  →  DOM: .message-title-box                │
│                                                                      │
│   $("[id^='button-core-<header>']").addClass("focus-info")           │
└──────────────────────────────────────────────────────────────────────┘
```

### 2.2 Part A — Config source

**Base defaults** — `ServiceUiController.js:673-735` `ServiceBasicControlHelper.defControlMixin.methods.getDefActionCodeMatrix()`:

Four action headers carry the two special fields that trigger a tutorial:

| Action header    | `focusButtonInGuide` | `notifyLabelKeyInGuide`      | Source lines |
|------------------|----------------------|------------------------------|--------------|
| `submit`         | `true`               | `'submitNotifyMessage'`      | 680-681      |
| `approve`        | `true`               | `'approveNotifyMessage'`     | 692-693      |
| `deliveryDone`   | `true`               | `'deliveryDoneNotifyMessage'`| 709-710      |
| `active`         | `true`               | `'approveNotifyMessage'`     | 721-722      |

**Per-doc overrides** — each editor's `getActionCodeMatrix()` declares which headers apply. `PurchaseContractEditor.js:107-134` declares `submit`, `revokeSubmit`, `approve`, `rejectApprove`, `countApprove`, `processDone`, `deliveryDone` — of these, only `submit`, `approve`, `deliveryDone` inherit the default guide fields. The other four (`revokeSubmit`, `rejectApprove`, `countApprove`, `processDone`) have no `notifyLabelKeyInGuide` — they never trigger a tutorial.

### 2.3 Part B — Config assembler

**`SerDocumentControlHelper.initDocumentStepTutorial()`** — `ServiceUiController.js:2153-2194`:

```js
methods.initDocumentStepTutorial = function () {
    var vm = this;
    var actionCodeMatrix = vm.getActionCodeMatrix();
    var defMatrix = vm.getDefActionCodeMatrix();
    var keys = Object.keys(actionCodeMatrix);
    var configs = [];
    for (var i = 0; i < keys.length; i++) {
        var unit = _.merge({}, defMatrix[keys[i]] || {}, actionCodeMatrix[keys[i]]);
        if (!unit.focusButtonInGuide) { continue; }
        var configureUnit = {};
        configureUnit.actionCodeHeader = keys[i];                                 // :2169
        configureUnit.activeCallback = function () {                              // :2170
            var checkResult = vm.displayForActionCodeCore(this.actionCodeHeader, {});
            return checkResult === DocumentManagerFactory.DISPLAY_CLASS.DISPLAY;
        };
        configureUnit.notificationMessages = [{                                   // :2177
            labelKey: unit.notifyLabelKeyInGuide,
            context: unit.notifyLabelKeyInGuide
        }];
        configureUnit.focusButtonArray = [ keys[i] ];                             // :2183
        configs.push(configureUnit);
    }
    ServiceStepTutorialHelper.initStepTutorial({                                  // :2188
        stepTutorialConfig: { configs: configs },
        labelObj: vm.label.actionNode,
        getStatus: vm.getStatus,
        defaultMessageContainer: $(vm.getDefMessageContainer())
    });
};
```

Called from `postUpdateUIModel` at `ServiceUiController.js:2199-2205` and `:3030-3045`, i.e. after every data reload.

### 2.4 Part C — Runtime renderer

**`ServiceStepTutorialHelper.initStepTutorial`** — `ServiceHttpRequestHelper.js:8918-8938`:
Iterates `stepTutorialConfig.configs`, calls `checkTutorialActiveCondition` (which evaluates `activeCallback` and/or `activeByStatus[]`), and for each active one calls `loadTutorialUnion`.

**`ServiceStepTutorialHelper.loadTutorialUnion`** — `ServiceHttpRequestHelper.js:8961-9010`:
- For each `notificationMessages[]` → resolves `labelKey` from `labelObj` and calls `ServiceMessageBarHelper.generateMessageBar({ msgCategory: INFO, message, container, context })`.
- For each `focusButtonArray[]` → `$("[id^='button-core-<header>']").addClass("focus-info")` (line 8991-8997).
- For each `focusTableActionArray[]` → same, wrapped in `setTimeout(fn, 1000)` (line 8999-9008) to wait for embedded tables to mount.

**`ServiceMessageBarHelper.generateMessageBar`** — `ServiceHttpRequestHelper.js:8565-8627`:
Builds this DOM and prepends it to `oSettings.container` (which is `$(vm.getDefMessageContainer())` → `.main.message-container`):

```html
<div class="row message-title <context>">
  <div class="col-sm-12">
    <div class="message-title-box background-actionGreen">
      <i class="nmd nmd-notifications-active"></i>
      <span class="page-title content-darkblue">{message}</span>
      <div class="portlet-widgets">
        <button type="button"><i class="ion-close-round close-messageBar"></i></button>
      </div>
    </div>
  </div>
</div>
```

`$(".close-messageBar").on("click", ServiceMessageBarHelper.removeMessage)` (line 8575-8577) wires dismissal. `removeMessage` (line 8551-8555) removes the `.row.message-title` node — **no persistence**. On save/refresh the bar is re-rendered.

Duplicate suppression: `generateMessageBar` first calls `removeMessageBar` with the same `context` (line 8569-8572), so re-firing the tutorial for the same status doesn't stack duplicate bars.

**CSS** — `.btn.focus-info` at `admin/assets/css/components.css:1971-1975`:

```css
.btn.focus-info {
    box-shadow: 0 0 20px 4px rgba(10, 110, 209, 0.7);
}
```

The perceived "yellow" focus effect is actually a **blue box-shadow glow** overlaid on the button's base color. `submit`/`approve` buttons are `btn-bigblue`, so the halo reads bright.

### 2.5 Part D — Complementary base path

For non-document editors (or documents with a custom tutorial), `ServiceBasicControlHelper.initStepTutorial()` at `ServiceUiController.js:1732-1741` reads `pageMeta.stepTutorialConfig` directly (see `QualityInspectOrderEditor.js:221-232`). This path coexists with `initDocumentStepTutorial` — both call `ServiceStepTutorialHelper.initStepTutorial`.

---

## 3. Purchase Contract concrete mapping

Purchase Contract inherits everything — zero per-doc overrides.

| Status              | `displayForActionCodeCore` = DISPLAY for | Focused button     | Hint `labelKey`               | Chinese (zh_CN)                                        | English (en, new UI)                              |
|---------------------|------------------------------------------|--------------------|-------------------------------|--------------------------------------------------------|---------------------------------------------------|
| INITIAL (1)         | `submit`                                 | Submit             | `submitNotifyMessage`         | 提示:确认信息编辑完成后,可以提交到下一个流程          | Note: Complete editing, then submit to next step  |
| SUBMITTED (2)       | `approve`                                | Approve            | `approveNotifyMessage`        | 提示:确认信息正确后,可以提交审核状态                  | Note: Confirm content before approving            |
| APPROVED (3)        | `deliveryDone`                           | Delivery Done      | `deliveryDoneNotifyMessage`   | 提示:可以点击按钮执行交货                              | Note: Click button to execute delivery            |
| PROCESS_DONE (100)  | `deliveryDone`                           | Delivery Done      | `deliveryDoneNotifyMessage`   | 提示:可以点击按钮执行交货                              | Note: Click button to execute delivery            |
| DELIVERY_DONE (200) | — (only `archive` visible, no guide)     | —                  | —                             | —                                                      | —                                                 |
| REJECTED / ARCHIVED | —                                        | —                  | —                             | —                                                      | —                                                 |

i18n source of truth:
- `admin/i18n/foundation/DocActionNode_zh_CN.properties:10, 35, 76, 86`
- `admin/i18n/foundation/DocActionNode.properties` (English)

Already ported to new UI:
- `IntelligentUI/src/i18n/locales/zh/foundation/DocActionNode.json`
- `IntelligentUI/src/i18n/locales/en/foundation/DocActionNode.json`

All five `*NotifyMessage` keys confirmed present. **No new i18n keys needed.**

---

## 4. New UI audit — what exists, what's missing

### 4.1 Already in place ✅

| Item                                                                                       | Location                                                          | Notes                                                                       |
|--------------------------------------------------------------------------------------------|-------------------------------------------------------------------|-----------------------------------------------------------------------------|
| `getDefActionCodeMatrix()` port (base)                                                     | `src/controllers/ServiceEditController.ts:658-711`                | ⚠ **`focusButtonInGuide` and `notifyLabelKeyInGuide` fields were dropped** — must be restored |
| `executeDocActionCore` / process button dispatch                                           | `src/controllers/ServiceEditController.ts:727-`                   |                                                                             |
| `refreshEditView`                                                                          | `src/controllers/ServiceEditController.ts:916-920`                | Hard-refreshes route; will re-trigger `postUpdateUIModel`                   |
| Process button `id` pattern `button-core-<header>`                                         | `src/components/control/ProcessButtonArray.tsx:185-193`           | Selector anchor point already exists                                        |
| i18n keys (`submitNotifyMessage`, `approveNotifyMessage`, `deliveryDoneNotifyMessage`, …)  | `src/i18n/locales/{zh,en}/foundation/DocActionNode.json`          | All 5 keys present                                                          |
| Editor page shell where MessageBar can mount                                               | `src/components/page/AsyncEditorPage.tsx`, `AsyncPage.tsx`        | Needs a stable container anchor                                             |

### 4.2 Missing — must be created/ported ❌

| Legacy file                                                          | New UI file (to create)                                    | Port scope                                        |
|----------------------------------------------------------------------|------------------------------------------------------------|---------------------------------------------------|
| `admin/js/ServiceHttpRequestHelper.js:8482-8703` (`ServiceMessageBarHelper`) | `src/controllers/ServiceMessageBarHelper.ts`               | 1:1 — every static method with legacy signatures  |
| `admin/js/ServiceHttpRequestHelper.js:8904-9040` (`ServiceStepTutorialHelper`) | `src/controllers/ServiceStepTutorialHelper.ts`             | 1:1 — every static method with legacy signatures  |
| `admin/js/component/basicElements/EmbeddedProcessButtonCore.js:114-116` (`ButtonCore.generateButtonIdPrefix`) | Add to `src/components/control/ProcessButtonArray.tsx` or its own `EmbeddedProcessButtonCore.ts` | 2-line helper                                     |
| DOM built by `generateMessageBarCore` (line 8580-8627)                | `src/components/control/MessageBar.tsx`                    | Presentation shim — React equivalent of the DOM, preserving CSS class names |
| CSS `.btn.focus-info` block (`components.css:1963-2001`)             | `src/styles/legacy-focus.css`                              | Verbatim copy                                     |

### 4.3 Missing — must be edited ✏

| File                                                                    | Change                                                                                                        |
|-------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| `src/controllers/ServiceEditController.ts`                              | Restore `focusButtonInGuide: true` + `notifyLabelKeyInGuide: '...'` on `submit`, `approve`, `deliveryDone`, `active` in `getDefActionCodeMatrix()`. Add `initStepTutorial()` method (mirror of `ServiceUiController.js:1732-1741`). |
| `src/controllers/DocumentEditController.ts` (wherever `SerDocumentControlHelper` was ported) | Add `initDocumentStepTutorial()` method (mirror of `ServiceUiController.js:2153-2194`). Call it from `postUpdateUIModel` after data reload (mirror of `:2199-2205` and `:3030-3045`). |
| `src/components/page/AsyncEditorPage.tsx` (and/or `AsyncPage.tsx`)      | Mount `<MessageBar />` as the first child inside the page container — this is the anchor for `defaultMessageContainer`, replacing the legacy `.main.message-container`. |
| `src/components/control/ProcessButtonArray.tsx`                         | Accept `focusedButtonIds?: string[]` prop (subscribed from the same store `ServiceStepTutorialHelper` publishes to). Add `focus-info` to `className` when a button's header matches. |
| `src/main.tsx` / global styles                                          | Import `src/styles/legacy-focus.css`.                                                                          |

### 4.4 Nothing to change

Per **Migration Contract Rule 2** ("Never create new methods in migrated classes") and Rule 4 ("One class = one legacy file"):

- **`PurchaseContractEditController.tsx` / editor controller** — no change. Legacy `PurchaseContractEditor.js` overrides neither `initDocumentStepTutorial` nor the guide fields; it inherits everything. Do not add anything.
- **All other document editors** — same. As long as they extend the base document controller, they get the feature for free once the base is ported.

---

## 5. Vue 2 → React/TS idiom translations

Per the CLAUDE.md conversion table:

| Legacy idiom                                                                             | Where                                                | New UI translation                                                                                                            |
|------------------------------------------------------------------------------------------|------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| `vm.$nextTick(fn)` — `ServiceUiController.js:2201, 3032`                                 | `postUpdateUIModel`                                  | `setTimeout(fn, 0)`                                                                                                           |
| `$(vm.getDefMessageContainer())` — `ServiceUiController.js:1739, 2192`                   | passed as `defaultMessageContainer`                  | Opaque token (e.g. string id `'main-message-container'`) that `MessageBar` resolves via a subscription store.                 |
| `$("[id^='button-core-<h>']").addClass("focus-info")` — `ServiceHttpRequestHelper.js:8993-8996` | `loadTutorialUnion` focus loop                       | `// TODO: legacy jQuery focus` — publish the header to a `focusedButtonIds` store; `ProcessButtonArray` subscribes.           |
| `$(".close-messageBar").on("click", …)` — `ServiceHttpRequestHelper.js:8575-8577`         | end of `generateMessageBar`                          | React `onClick={() => ServiceMessageBarHelper.removeMessage(event)}` on the MessageBar close button.                          |
| `setTimeout(fn, 1000)` — `ServiceHttpRequestHelper.js:9002-9007`                          | table-action focus                                   | Preserved verbatim.                                                                                                           |
| `label.actionNode.<key>` (Vue reactive label) — `ServiceUiController.js:2190`             | tutorial `labelObj`                                  | i18n resolver: `i18n.t('docActionNode:<labelKey>')`. Because `labelKey` is a string path, it drops in unchanged.              |
| `_.merge({}, ...)` — `ServiceUiController.js:2161`                                       | matrix merge                                         | `{ ...defMatrix[key], ...perDocMatrix[key] }` (shallow is sufficient — no nested objects on these entries).                    |
| `Vue.extend`, `methods: { … }`                                                            | throughout                                           | Already handled by existing controller class ports.                                                                            |

---

## 6. Migration phases

### Phase A — Base infrastructure (blocks Phase B)

1. Port `ServiceMessageBarHelper` → `src/controllers/ServiceMessageBarHelper.ts` — 1:1 static class port.
2. Port `ServiceStepTutorialHelper` → `src/controllers/ServiceStepTutorialHelper.ts` — 1:1 static class port.
3. Port `ButtonCore.generateButtonIdPrefix` — 2 lines, wherever button-id helpers live.
4. Create `src/components/control/MessageBar.tsx` — presentation-only component that reads the descriptor store and renders one `.message-title-box.background-actionGreen` row per active entry. Preserve legacy CSS class names.
5. Copy `.btn.focus-info` selector block from `components.css:1963-2001` → `src/styles/legacy-focus.css`; import globally.

### Phase B — Controller wiring

6. Restore `focusButtonInGuide` + `notifyLabelKeyInGuide` fields on `submit`, `approve`, `deliveryDone`, `active` in `ServiceEditController.getDefActionCodeMatrix()`. Field-for-field match with `ServiceUiController.js:673-735`.
7. Add `initStepTutorial()` to `ServiceEditController` (base path, mirrors `ServiceUiController.js:1732-1741`).
8. Add `initDocumentStepTutorial()` to `DocumentEditController` (document path, mirrors `ServiceUiController.js:2153-2194`).
9. Call `initDocumentStepTutorial()` from `postUpdateUIModel` — same call site as legacy (`:2199-2205` and `:3030-3045`).

### Phase C — Page + button wiring

10. Mount `<MessageBar />` inside `AsyncEditorPage.tsx` (and/or `AsyncPage.tsx`) at the top of the page body.
11. Add `focusedButtonIds` prop + `focus-info` class application to `ProcessButtonArray.tsx`.
12. Wire the `focusedButtonIds` store to the same publish stream `ServiceStepTutorialHelper` writes to.

### Phase D — Verification

For each of the 5 relevant Purchase Contract statuses (per the table in §3), open the editor and confirm:

| Status         | Hint bar text                              | Focused button   |
|----------------|--------------------------------------------|------------------|
| INITIAL        | 提示:确认信息编辑完成后,可以提交到下一个流程 | Submit           |
| SUBMITTED      | 提示:确认信息正确后,可以提交审核状态         | Approve          |
| APPROVED       | 提示:可以点击按钮执行交货                     | Delivery Done    |
| PROCESS_DONE   | 提示:可以点击按钮执行交货                     | Delivery Done    |
| DELIVERY_DONE  | (none)                                     | (none)           |

Also:
- Click the X on the hint bar → bar disappears. Save the document → bar reappears at the new status. **Do not persist dismissal** — legacy does not, and we match that.
- Toggle browser language to `en` → English hint text renders.
- Multiple saves at the same status → no duplicate bar (duplicate suppression via `removeMessageBar` before `generateMessageBar`).

---

## 7. Out of scope for this migration

- **`focusTableActionArray`** (embedded-table action button focus) — port the code path (with its `setTimeout(fn, 1000)`), but no wiring; only activates when a `stepTutorialConfig` explicitly declares it. Legacy usage: `admin/js/supplyChain/QualityInspectMatItemControl.js:168`.
- **Message URL / icon substitution** — `generateMessageElement` line 8629-8683 and `_preProcessUrlArray` line 9012-9020 handle `{icon}`, `{url}` tokens inside messages. Port verbatim but no Purchase Contract usage exists; untested until another doc type exercises it.
- **Custom `stepTutorialConfig` per doc type** (e.g. `QualityInspectOrderEditor.js:221-232`) — the base `initStepTutorial()` path handles it once ported, but no port work for those specific doc types is included here.
- **`WorkflowToolbar.tsx`** (older alternate toolbar with hard-coded per-doc visibility) — not on the current Purchase Contract render path. Decide separately whether to keep or delete; not required for this feature.

---

## 8. Migration Contract compliance checklist

Per `/Users/I043125/work-migration/CLAUDE.md`:

- ✅ **Rule 1** — All methods of `ServiceMessageBarHelper` (line 8482-8703) and `ServiceStepTutorialHelper` (line 8904-9040) are ported, not just the ones used by Purchase Contract. Same names, same parameter names. jQuery / DOM parts become `// TODO: legacy` stubs but the method still exists with the same signature.
- ✅ **Rule 2** — No new methods added to migrated classes. `initStepTutorial` and `initDocumentStepTutorial` exist in the legacy source at the cited lines.
- ✅ **Rule 3** — Every ported method above is line-referenced to the legacy source.
- ✅ **Rule 4** — Each new TS file maps 1:1 to a legacy JS file:

| New TS file                                        | Legacy JS file                                                                            | Lines      |
|----------------------------------------------------|-------------------------------------------------------------------------------------------|------------|
| `src/controllers/ServiceMessageBarHelper.ts`       | `admin/js/ServiceHttpRequestHelper.js` (ServiceMessageBarHelper class)                    | 8482-8703  |
| `src/controllers/ServiceStepTutorialHelper.ts`     | `admin/js/ServiceHttpRequestHelper.js` (ServiceStepTutorialHelper class)                  | 8904-9040  |
| `src/components/control/MessageBar.tsx`            | DOM emitted by `ServiceMessageBarHelper.generateMessageBarCore`                           | 8580-8627  |

---

## 9. Referenced files (absolute paths)

**Legacy**
- `/Users/I043125/work/ThorSalesDistributionUI/admin/js/ServiceHttpRequestHelper.js`
  — `ServiceMessageBarHelper` @ 8482-8703, `ServiceStepTutorialHelper` @ 8904-9040
- `/Users/I043125/work/ThorSalesDistributionUI/admin/js/component/basicElements/ServiceUiController.js`
  — `getDefActionCodeMatrix` @ 673-735, `displayForActionCodeCore` @ 523-543, `initStepTutorial` @ 1732-1741, `initDocumentStepTutorial` @ 2153-2194, `postUpdateUIModel` @ 2199-2205 & 3030-3045
- `/Users/I043125/work/ThorSalesDistributionUI/admin/js/component/basicElements/EmbeddedProcessButtonCore.js`
  — `ButtonCore.generateButtonIdPrefix` @ 114-116
- `/Users/I043125/work/ThorSalesDistributionUI/admin/js/supplyChain/PurchaseContractEditor.js`
  — `getStatus` @ 78, `getActionCodeMatrix` @ 107-134
- `/Users/I043125/work/ThorSalesDistributionUI/admin/js/supplyChain/QualityInspectOrderEditor.js`
  — custom `stepTutorialConfig` example @ 221-232
- `/Users/I043125/work/ThorSalesDistributionUI/admin/i18n/foundation/DocActionNode_zh_CN.properties`
- `/Users/I043125/work/ThorSalesDistributionUI/admin/i18n/foundation/DocActionNode.properties`
- `/Users/I043125/work/ThorSalesDistributionUI/admin/assets/css/components.css` — `.btn.focus-info` @ 1963-2001

**New UI**
- `/Users/I043125/work2/IntelligentUI/src/controllers/ServiceEditController.ts` — `getDefActionCodeMatrix` @ 658-711 (needs guide fields restored)
- `/Users/I043125/work2/IntelligentUI/src/controllers/DocumentEditController.ts` — target for `initDocumentStepTutorial`
- `/Users/I043125/work2/IntelligentUI/src/components/control/ProcessButtonArray.tsx` — target for `focusedButtonIds` prop
- `/Users/I043125/work2/IntelligentUI/src/components/page/AsyncEditorPage.tsx`, `AsyncPage.tsx` — target for `<MessageBar />` mount
- `/Users/I043125/work2/IntelligentUI/src/i18n/locales/zh/foundation/DocActionNode.json` — keys already present
- `/Users/I043125/work2/IntelligentUI/src/i18n/locales/en/foundation/DocActionNode.json` — keys already present
