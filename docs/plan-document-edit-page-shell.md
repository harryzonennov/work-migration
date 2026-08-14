# Generic `DocumentEditPage` Shell — Migration Plan / Record

**Status:** Implemented (2026-08-02)

## Problem

Every document editor page in IntelligentUI must render the same composition around the
`AsyncEditorPage`:

1. `AsyncEditorPage` — form/section renderer
2. `DocumentItemMultiSelectModal` — cross-document item-selection modal
3. `DocActionModal` — workflow action confirmation modal
4. an optional doc-specific `***MaterialItemPanel` — quick-edit bottom sidebar

This composition **does not exist in the legacy Vue UI**. There, each `<xxx-editor>` template
inlined `<document-item-multi-select-factory>`, `<doc-action-modal>`, and the material-item
panel as child tags wired through Vue `$refs` / `$emit`. In React those wiring points — the
imperative factory → modal-state bridge, the panel-registry registration, and the refresh
callbacks — are much more verbose, so they were copy-pasted into `PurchaseContractEditPage`.

The controller/lifecycle glue was already generic (`useDocumentEditController`), but the
**page JSX was not**, and would have been duplicated for every migrated document type.

## Solution

A single generic shell owns the rendering half of the split; each concrete editor page becomes
a ~15-line call site.

### `src/components/page/DocumentEditPage.tsx` (new)

Generic component, parameterized by the quick-edit panel handle type `TPanel`.

- Wraps `PanelRegistryProvider` around an inner body (so the quick-edit magnifier in
  `AsyncEmbeddedListSection` can resolve the panel by `refItemName`).
- Body calls the injected `useController(processMode)` hook and renders, inside
  `ControllerVmContext.Provider`:
  - `AsyncEditorPage` (pageMeta/initialValues under the `!loading` gate),
  - `DocumentItemMultiSelectModal` (driven by `activeMultiSelect` + handlers),
  - `DocActionModal`,
  - `renderItemPanel?.(ref, uuid, onSaved)` — optional; the caller returns the concrete panel.

Props:

```ts
// Shared base — every edit page's route prop. Exported for concrete pages to reuse.
interface EditPageProps {
  processMode: ProcessMode;
}

interface DocumentEditPageProps<TPanel extends RegisteredPanelHandle = RegisteredPanelHandle>
  extends EditPageProps {
  useController: (processMode: ProcessMode) => DocumentEditControllerResult<TPanel>;
  renderItemPanel?: (
    ref: React.RefObject<TPanel | null>,
    uuid: string | undefined,
    onSaved: () => void,
  ) => React.ReactNode;
}
```

`EditPageProps` is the generic props base for any edit page (just `processMode`, which every
editor receives from its route). Concrete pages type against `EditPageProps` directly instead of
re-declaring a local `interface Props` — e.g. `PurchaseContractEditPage: React.FC<EditPageProps>`.

**Typing notes (non-obvious):**
- `DocumentEditControllerResult` = `ReturnType<typeof useDocumentEditController<…, TPanel>>`
  with `controller` narrowed to a structural `DocumentEditControllerContract & ControllerVm`.
  The `& ControllerVm` is required so `controller` still satisfies
  `ControllerVmContext.Provider value=` (which wants `ServiceBaseController | DocumentItemMultiSelect`).
- `handleFinish` in the contract is written as a **method signature** (not an arrow property)
  so its parameter is checked bivariantly — a concrete controller's `handleFinish(values: TForm)`
  (where `TForm` is narrower than `Record<string, unknown>`) stays assignable. This replaces the
  `as (values: Record<string, unknown>) => Promise<boolean>` cast the page used before.

### `src/pages/logistics/purchaseContract/PurchaseContractEditPage.tsx` (refactored)

Collapsed from ~86 lines to a thin call site: passes `usePurchaseContractEditController` and a
`renderItemPanel` returning `<PurchaseContractMaterialItemPanel>`. All the wiring + comments
moved into the shell.

## Migration-contract compliance

This is new-UI framework glue, not a migrated legacy class — Rules 1–4 (faithful port / no new
methods) do not apply. No business logic added; `refreshEditView` and all controller methods are
called, never defined here.

## Verification

- `npx tsc --noEmit` — no errors in `DocumentEditPage.tsx` or `PurchaseContractEditPage.tsx`
  (35 pre-existing project-wide errors elsewhere are unrelated).
- `npx vitest run src/pages/logistics/purchaseContract/PurchaseContractEditPage.test.tsx` —
  passes; exercises controller hook → AsyncEditorPage → right-bar through the new shell.
- Manual: `/logistics/purchaseContract/<uuid>/edit` — form + item table render, action button
  opens DocActionModal, cross-doc select opens DocumentItemMultiSelectModal, quick-edit magnifier
  opens the panel, saving refreshes the item table.

## Future consumers

Each new document editor (SalesContract, PurchaseOrder, InboundDelivery, …) becomes the same
~15-line call site: supply its `use***EditController` + `renderItemPanel`, or omit
`renderItemPanel` when the editor has no quick-edit panel.
