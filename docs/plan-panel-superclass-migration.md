# Panel Super-Class Migration Plan — Generic `EditPanel` base

Generated: 2026-07-10
Updated: 2026-07-10 — renamed base to `EditPanel.tsx`; documented the full legacy
inheritance chain and why the deeper control super-classes are NOT needed as panel
classes (they already exist as the controller hierarchy).
Updated: 2026-07-17 — added the `EditPanelHandle` vs `RegisteredPanelHandle` reference
section (see end of file).

## Legacy inheritance chain (full depth)

```
ServiceBasicControlHelper.defControlMixin            (ServiceUiController.js:24)
  └─ ServiceEditorControlHelper.defControlMinxin      (:1566)
       └─ ServiceItemEditorHelper.defControlMinxin    (:2329)
            └─ ServiceItemControlHelper.defControlMinxin (:2434)
                 └─ ServiceItemControlHelper.defEditorPanelMinxin (:2889)
                       + ServicePopBottomPanelHelper.defPopButtomPanelMinxin (ServiceHttpRequestHelper.js:6905)
```

`defEditorPanelMinxin` multiple-inherits TWO branches:
- **Control branch** (`defControlMinxin` chain) — record load/save, validation,
  page-meta, status, doc-actions, help docs.
- **Panel-presentation branch** (`defPopButtomPanelMinxin`) — the sliding panel:
  loadPanel/openPanel/hidePanel/saveModule/busy/refresh.

### The control branch is ALREADY migrated — do NOT recreate it as panel classes

The `defControlMinxin` chain maps 1:1 onto the new-UI controller classes that
already exist:

| Legacy mixin level | New-UI class (already exists) |
|---|---|
| `ServiceBasicControlHelper.defControlMixin` | `ServiceBaseController` |
| `ServiceEditorControlHelper.defControlMinxin` | `ServiceEditController` |
| `ServiceItemEditorHelper.defControlMinxin` | `DocItemEditController` |
| `ServiceItemControlHelper.defControlMinxin` | (item methods folded into `DocItemEditController`) |
| concrete `PurchaseContractMaterialItemControl` | `PurchaseContractMaterialItemController` |

So the deep super-classes are **not needed as new panel classes** — the panel's
`config.buildController` returns a `PurchaseContractMaterialItemController`, which
already carries every method the legacy control chain contributed
(`getBaseUUID`, `getStatus`, `getServiceManager`, `getDefaultPageMeta`,
`handleFinish`/save, validation, etc.).

### Only the panel-presentation branch needs a base — that base is `EditPanel`

`EditPanel.tsx` = the analog of `defEditorPanelMinxin`'s OWN methods
(`convertToPanelPageMeta`, `getPageCategory=EDITPANEL`, `exitModule=hidePanel`,
`navToEdit`, template) + `defPopButtomPanelMinxin` (via `usePopBottomPanel`).
Adding further intermediate panel super-classes above `EditPanel` would only
duplicate the controller hierarchy — **explicitly out of scope**.

---

## Trigger

The concrete `PurchaseContractMaterialItemPanel.tsx` (308 lines) currently inlines
**all** panel behavior — state, fetch orchestration, save wiring, imperative
handle, header, and render. In the legacy UI this behavior lives in a shared
super-mixin, and each concrete panel is a **14-line** file overriding only a
handful of methods. We should mirror that split so the remaining ~14 doc-type
panels are thin, and the shared logic lives in one place.

---

## Legacy architecture (the model to mirror)

### Concrete class — `PurchaseContractMaterialItemPanel.js` (14 lines)

```js
var PurchaseContractMaterialItemPanel = Vue.extend({
    name: "purchase-contract-material-item-panel",
    mixins: [
        ServiceItemControlHelper.defEditorPanelMinxin,   // ← super: all panel behavior
        PurchaseContractMaterialItemControl              // ← the doc's controller (data + a few overrides)
    ]
});
```

### Super class — `ServiceItemControlHelper.defEditorPanelMinxin` (`ServiceUiController.js:2889-3067`)

Itself mixes in `defPopButtomPanelMinxin` (`ServiceHttpRequestHelper.js:6905-7041`)
+ `defControlMinxin`. Owns **all** generic panel logic:

| Legacy method (super) | Role |
|---|---|
| `initSubComponentsItemPanel` | register `pop-bottom-panel` |
| `initPanelLayoutEvents` | close panel on parent-tab click; refresh on inner nav-pills click |
| `getPageCategory` | returns `EDITPANEL` |
| `getPageMeta` | `getDefaultPageMeta()` → `convertToPanelPageMeta()` |
| `getFirstSectionMeta` | first non-`pageOnly` section |
| `convertToPanelPageMeta` | strip `pageOnly`, flatten tabs |
| `exitModule` | = `hidePanel` |
| `postUpdateUIModel` | after-load lifecycle (hide busy, refresh, help docs, layout events) |
| `navToEdit` | expand → full-page editor |
| `loadPanel/openPanel/loadPanelCore/hidePanel/saveModule/showBusyLoading/hideBusyLoading/refreshPanel/changeUIHandler/controlErrorHandle` | (from `defPopButtomPanelMinxin`) |
| `template` | `<pop-bottom-panel><async-editor-control .../></pop-bottom-panel>` |

### Concrete overrides — `PurchaseContractMaterialItemControl.js`

Only these are doc-specific (everything else inherited):

| Override | What it supplies |
|---|---|
| `data` (`label`, `content`, `*URL`) | the doc's UI model + endpoint URLs |
| `getEditPageURL()` | `"PurchaseContractMaterialItemEditor.html"` |
| `getBaseUUID()` | `content.purchaseContractMaterialItemUIModel.uuid` |
| `getParentUUID()` | `content...parentNodeUUID` |
| `getServiceManager()` | `PurchaseContractManager` |
| `getStatus()` | `content...itemStatus` |
| `setModuleToUI(content)` | assign fetched model → `content`, then `postUpdateUIModel()` |
| `getDefaultPageMeta()` | the doc's tab/section/field layout |

**Net split: ~90% shared, ~10% custom.**

---

## Current React state (the problem)

`PurchaseContractMaterialItemPanel.tsx` (308 lines) inlines the shared 90%:
- state (`record`, `currentUuid`, `currentProcessMode`), `panelShellRef`, `formRef`
- `fetchAndApply` (= legacy `loadModule`/`setModuleToUI`)
- `saveModule` (validate + `handleFinish`)
- `usePopBottomPanel` + `useItemPanelController` wiring
- imperative `useImperativeHandle`
- `panelPageMeta` / `initialValues` memos + guards
- `handlePanelFinish`
- the full render: `<PopBottomPanel>` + header (Save / 展开 / `ItemQuickAction`) + `<AsyncPage>`

If we copy this per doc-type, each new panel is ~300 lines of duplicated glue. That
violates the legacy 14-line-subclass model and Migration Contract §1 (faithful port
of structure).

The shared hooks (`usePopBottomPanel`, `useItemPanelController`) already exist, but the
**component-level glue is not shared**. This plan extracts that glue into a base
component.

---

## Proposed React design

React idiom for "super class + a few overrides" = **one generic base component +
a per-doc config object** (the config object is the analog of the concrete
mixin's method overrides). No class inheritance; the config supplies the doc-specific
callbacks, exactly the set the legacy subclass overrode.

### New file 1 — `src/components/page/EditPanel.tsx` (the "super class")

A generic `forwardRef` component. Owns everything the legacy `defEditorPanelMinxin`
owned. Props = the doc-specific overrides:

```ts
export interface EditPanelConfig<TRecord> {
  /** Fetch the record by UUID (EDIT mode). Legacy setModuleToUI's data source. */
  fetchRecord: (uuid: string) => Promise<TRecord | undefined>;
  /** Build the controller instance for the current record/mode. Legacy: the concrete Control mixin. */
  buildController: (args: {
    processMode: ProcessMode; uuid?: string; parentUuid?: string;
    navigate: NavigateFn; record?: TRecord;
  }) => ItemPanelControllerBase & {
    handleFinish: (values: never) => Promise<boolean>;
    buildInitialValues: () => Record<string, unknown>;
  };
  /** Full-page editor route for the Expand button. Legacy getEditPageURL(). */
  getEditPageURL: (args: { parentUuid?: string; uuid?: string }) => string;
}

export interface EditPanelHandle {
  loadPanel(o: LoadPanelSettings): void;
  hidePanel(): void;
  navToEdit(): void;
  getBaseUUID(): string | undefined;
  getProcessMode(): ProcessMode | undefined;
  getItemEvent(): MouseEvent | React.MouseEvent | undefined;
}

export interface EditPanelProps<TRecord> {
  config: EditPanelConfig<TRecord>;
  parentUuid?: string;
  onSaved?: () => void;
}
```

Internals moved verbatim from the current concrete file:
- state, `panelShellRef`, `formRef`
- `fetchAndApply` → calls `config.fetchRecord`
- `saveModule` (validate + `controller.handleFinish`)
- `usePopBottomPanel` + `useItemPanelController` wiring
- `useImperativeHandle`
- `panelPageMeta` / `initialValues` memos + guards
- `handlePanelFinish`
- render: `<PopBottomPanel>` + header (Save / 展开 / `ItemQuickAction`) + `<AsyncPage>`

This is the single home for the legacy super-class logic.

### New file 2 (rewrite) — `PurchaseContractMaterialItemPanel.tsx` (the "concrete class", ~30 lines)

Shrinks to the analog of the 14-line legacy file — just the config + a passthrough
ref:

```tsx
const config: EditPanelConfig<PurchaseContractMaterialItemUIModel> = {
  fetchRecord: async (uuid) => {
    const r = await getContractMaterialItem(uuid);
    return (r as PurchaseContractMaterialItemServiceUIModel | undefined)
      ?.purchaseContractMaterialItemUIModel;
  },
  buildController: (args) => new PurchaseContractMaterialItemController(args),
  getEditPageURL: ({ parentUuid, uuid }) =>
    `/logistics/purchaseContract/${parentUuid}/items/${uuid}/edit`,
};

const PurchaseContractMaterialItemPanel = forwardRef<
  EditPanelHandle, { parentUuid?: string; onSaved?: () => void }
>((props, ref) => <EditPanel ref={ref} config={config} {...props} />);
```

The concrete file names ONLY the three doc-specific things (fetch endpoint,
controller class, editor route) — mirroring the legacy overrides
`setModuleToUI` (fetch), the Control mixin (controller), and `getEditPageURL`.

`PurchaseContractMaterialItemPanelHandle` becomes a re-export alias of
`EditPanelHandle` so `PurchaseContractEditPage`'s ref import is unaffected.

### Unchanged

- `PopBottomPanel.tsx`, `PopPanelCompensateSection.tsx`, `usePopBottomPanel.ts`,
  `useItemPanelController.ts`, `ItemQuickAction.tsx`, `PanelRegistryContext.tsx`
  — already the correct shared primitives; the base component composes them.
- `PurchaseContractEditPage.tsx` — imports the same handle type + component name;
  no change needed (the concrete file keeps its export name + handle alias).
- `PurchaseContractMaterialItemController.tsx` — unchanged (it IS the analog of the
  legacy Control mixin's data + overrides).

---

## Method-by-method mapping (legacy super → new base component)

| Legacy super method | New base component location |
|---|---|
| `data.postPanel` etc. | `useState` in `EditPanel` |
| `initSubComponentsItemPanel` | n/a (React imports, no global registration) |
| `initPanelLayoutEvents` | already in `PopBottomPanel`'s `useEffect` (tab-click close) |
| `getPageCategory` | `useItemPanelController.getPageCategory` |
| `getPageMeta` / `convertToPanelPageMeta` / `getFirstSectionMeta` | `useItemPanelController` |
| `exitModule` | base component (`popPanel.hidePanel`) |
| `postUpdateUIModel` | base component's `fetchAndApply` postLoadData path |
| `navToEdit` | `useItemPanelController.navToEdit` (uses `config.getEditPageURL`) |
| `loadPanel`/`openPanel`/`hidePanel`/`saveModule`/busy/refresh | `usePopBottomPanel` (already shared) |
| `template` | base component's JSX |

## Doc-specific overrides (legacy subclass → new config)

| Legacy override | New config key |
|---|---|
| `setModuleToUI` (fetch source) | `config.fetchRecord` |
| the Control mixin (controller class) | `config.buildController` |
| `getEditPageURL` | `config.getEditPageURL` |
| `getBaseUUID`/`getParentUUID`/`getStatus`/`getServiceManager`/`getDefaultPageMeta` | already on the doc's `...Controller` class (unchanged) |

---

## Steps

1. **Create `EditPanel.tsx`** — move all shared glue out of the current
   `PurchaseContractMaterialItemPanel.tsx` into this generic base, parameterized by
   `EditPanelConfig<TRecord>`. Generic over the record type.
2. **Rewrite `PurchaseContractMaterialItemPanel.tsx`** to a ~30-line config +
   passthrough, re-exporting `EditPanelHandle` as
   `PurchaseContractMaterialItemPanelHandle` for source compatibility.
3. **Verify `PurchaseContractEditPage.tsx`** compiles unchanged (same import names).
4. **`tsc --noEmit`** — expect zero new errors on touched files (baseline 164).
5. **`vite build`** — expect success.
6. Update `docs/MIGRATION_CONVERSATIONS.md` per the after-update rule.

## Non-goals

- Not migrating the other ~14 doc-type panels in this task — but after this refactor
  each becomes a ~30-line config file, so they're trivial follow-ups.
- No behavior change: the panel looks and acts identically; this is a pure
  structural extraction (the legacy super/sub split).

## Risk / notes

- `buildController` must return an object exposing `buildAsyncPageMeta`,
  `buildInitialValues`, `handleFinish`, `getBaseUUID` — the `PurchaseContractMaterialItemController`
  already has all of these (it extends `DocItemEditController`).
- The generic `TRecord` keeps `fetchRecord`/controller typing tight per doc-type.
- Keep the concrete file's `displayName` and export names identical so no other
  imports break.

---

## Reference — `EditPanelHandle` vs `RegisteredPanelHandle` (why two similar interfaces)

Added 2026-07-17. These two interfaces look near-identical; they are intentionally the
**two ends of one decoupled contract**, not duplication.

- **`EditPanelHandle`** (`components/page/EditPanel.tsx`) — the **full** imperative API a
  quick-edit panel exposes via `ref` (the producer side):
  `loadPanel`, `hidePanel`, `navToEdit`, `getBaseUUID`, `getProcessMode`, `getItemEvent`.
- **`RegisteredPanelHandle`** (`pages/context/PanelRegistryContext.tsx`) — the **minimal**
  slice the generic panel registry needs to drive *any* panel by name (the consumer side):
  `loadPanel` + `hidePanel` (required), `getBaseUUID?` + `getProcessMode?` (optional).

`EditPanelHandle` is a **structural superset** of `RegisteredPanelHandle`, so every
`EditPanelHandle` is a valid `RegisteredPanelHandle`.

### How they connect (verified in source)
```
EditPanelHandle                             EditPanel.tsx — the contract EditPanel implements
   │ = (type alias)
PurchaseContractMaterialItemPanelHandle     PurchaseContractMaterialItemPanel.tsx:40
   │                                          (literally `export type … = EditPanelHandle`)
   │ materialItemPanelRef.current
   ▼
register('contractMaterialItemPanel', ref)  PurchaseContractEditPage.tsx:62
   │ register expects RegisteredPanelHandle | null
   ▼
RegisteredPanelHandle                       PanelRegistryContext.tsx — the minimal slice
```
Neither interface imports the other — they meet purely via TypeScript **structural
(duck) typing** at the `register()` call site.

### Why the split exists (dependency direction)
`PanelRegistryContext` is generic infrastructure (`AsyncEmbeddedListSection`'s quick-edit
magnifier resolves any panel by name). It must **not** depend on the concrete `EditPanel`,
so it declares its own minimal handle describing only what a caller invokes. `EditPanel`
declares the full handle it implements, including panel-only methods (`navToEdit`,
`getItemEvent`) the registry has no business knowing. Collapsing them into one interface
would force the generic registry to depend on the specific component — the exact coupling
the split avoids.

### Optional (not done)
Could make the relationship explicit via `EditPanelHandle extends RegisteredPanelHandle`
(documents intent, keeps the two optional getters consistent). Trade-off: that reverses the
dependency direction (EditPanel would import the registry's type). Left as-is — the
structural match already works and keeps the registry dependency-free.
