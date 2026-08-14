# Plan — Extract the generic right-bar wiring out of the edit pages into a shared hook

**Status:** IMPLEMENTED — 2026-07-15 (hook, both pages). During implementation the root page
proved to have a genuinely different lifecycle (ungated help load in create mode; edit-only
docContext), so the hook gained a `helpLoadGate: 'mount' | 'uuid'` option to keep each page's
exact behavior. Verified: 9/9 tests pass (incl. a new right-bar wiring test), `vite build` clean.
**Date:** 2026-07-15
**Target project:** `/Users/I043125/work2/IntelligentUI/`

---

## 1. Goal

`PurchaseContractItemEditPage.tsx:42-112` (and the sibling `PurchaseContractEditPage.tsx:66-106`)
contain a block of **right-sidebar wiring** that is not PurchaseContract-item–specific — it is
the generic "document editor page ↔ RightSideBar" contract:

- set `tab1Mode` on mount, reset on unmount;
- on `uuid`, register `docContext`, load the help document (+ optionally the doc-flow list),
  then clear all right-bar state on cleanup;
- a `resolveKey` that namespaces field keys against the module's i18n namespace.

Only a handful of **values** differ per page (docType, help-doc name(s), i18n path, doc-flow URL,
the tab1 mode, and whether the doc-flow list is fetched). Extract the generic effect logic into a
shared hook so each page passes only that config — mirroring the `useDocumentEditController` /
`useDocumentListController` extractions already done.

> Scope note: the target is a **shared hook** (`useDocumentEditRightBar`), NOT a base *class*.
> These pages are React function components; their shared logic is `useEffect` lifecycle + context
> writers, which composes as a hook, not as class inheritance. "Super general class" isn't the
> right vehicle here — a hook is. (If the user specifically wants the *config* to live on a class,
> it already can: most values are derivable from the Manager/controller — see §4.)

---

## 2. Current state (verified from source)

### Item page (`PurchaseContractItemEditPage.tsx`)
- `useEffect []` → `setTab1Mode('docFlow')`; cleanup `setTab1Mode('actionLog')` (lines 42-51).
- `useEffect [uuid]` → `setDocContext({docType:'purchaseContractMaterialItem', uuid})` →
  `initHelpDocumentWithDocFlow({ helpDocumentName:'PurchaseContractMaterialItemHelpDocument',
  i18nPath:'supplyChain', labelResolver:resolveKey, statusLabelMap:Manager.getStatusLabelMap(),
  getDocFlowListURL:'purchaseContractMaterialItem/getDocFlowList', errorHandle, writers })` →
  cleanup clears helpList/docFlowList/activeKey/docContext (lines 53-109).

### Root page (`PurchaseContractEditPage.tsx`)
- `useEffect []` → `setTab1Mode('actionLog')` (lines 70-73).
- `useEffect []` → `loadHelpDocument(['PurchaseContractHelpDocument',
  'PurchaseContractMaterialItemHelpDocument'], 'supplyChain')` → `setHelpList(buildHelpList(...))`;
  cleanup `setHelpList([])` (lines 81-98). **No** doc-flow fetch (help-only).
- `useEffect [uuid]` → `setDocContext({docType:'purchaseContract', uuid})`; cleanup clears it
  (lines 101-106).

### What's already reusable (do NOT rebuild)
- `initHelpDocumentWithDocFlow(settings)` + `loadHelpDocument`/`buildHelpList` in
  `RightBarPanelService.ts` / `HelpDocumentService.ts` — the actual fetch/build is already generic.
- `useRightBarContent()` context writers.
- Config values already owned by lower layers:
  - `getDocFlowListURL` — a `readonly` on `PurchaseContractMaterialItemController` (:98).
  - `getStatusLabelMap()`, `getRootNodeInstId()` / `getItemNodeInstId()` — on the Manager.
  - i18n namespace — the module's `addResourceBundle` registration.

**So the only thing missing is the effect orchestration**; the pieces it calls are all generic.

---

## 3. The generic vs. per-page split

| Concern | Generic (→ hook) | Per-page value |
|---|---|---|
| set/reset `tab1Mode` on mount/unmount | ✅ effect | `'docFlow'` (item) / `'actionLog'` (root) |
| `setDocContext({docType, uuid})` on uuid | ✅ effect | `docType` |
| load help doc (+ optional doc-flow) | ✅ effect | `helpDocumentName(s)`, `i18nPath`, `getDocFlowListURL?` |
| `resolveKey` (namespace + i18next + fallback) | ✅ helper | i18n namespace (e.g. `purchaseContract`) |
| `statusLabelMap` | ✅ passthrough | `Manager.getStatusLabelMap()` |
| cleanup: clear helpList/docFlowList/activeKey/docContext | ✅ effect | — |
| `errorHandle` | ✅ default (console.warn) | optional override |

---

## 4. Design

### New hook: `src/composables/useDocumentEditRightBar.ts`

```ts
export interface DocumentEditRightBarConfig {
  uuid: string | undefined;
  /** 'actionLog' (root editor) | 'docFlow' (item editor). */
  tab1Mode: 'actionLog' | 'docFlow';
  docType: string;
  /** i18n namespace for resolveKey + help entry titles (e.g. 'purchaseContract'). */
  i18nNamespace: string;
  helpDocumentName: string | string[];
  /** Help-document asset path segment (e.g. 'supplyChain'). */
  i18nPath: string;
  statusLabelMap?: Record<string, Record<string, string>>;
  /** When set, also fetch the doc-flow list (item editor). Omit for help-only (root editor). */
  getDocFlowListURL?: string;
  errorHandle?: (error: unknown) => void;
}

/**
 * Generic RightSideBar wiring for a document/item editor page. Encapsulates the
 * tab1Mode set/reset, docContext registration, help-document (+ optional doc-flow) load,
 * and full cleanup — the contract shared by every AsyncEditorPage. Per-page code passes
 * only the config values that differ.
 */
export function useDocumentEditRightBar(config: DocumentEditRightBarConfig): void { … }
```

The hook internally:
- pulls `useRightBarContent()` writers,
- runs the `tab1Mode` mount/reset effect,
- runs the `[uuid]` effect: builds `resolveKey` from `i18nNamespace`, calls
  `initHelpDocumentWithDocFlow` (which no-ops the doc-flow branch when `getDocFlowListURL` is
  absent — verify at `RightBarPanelService.ts:197`), then cleans up.

### Item page becomes:
```ts
useDocumentEditRightBar({
  uuid,
  tab1Mode: 'docFlow',
  docType: 'purchaseContractMaterialItem',
  i18nNamespace: 'purchaseContract',
  helpDocumentName: 'PurchaseContractMaterialItemHelpDocument',
  i18nPath: 'supplyChain',
  statusLabelMap: PurchaseContractManager.getStatusLabelMap(),
  getDocFlowListURL: 'purchaseContractMaterialItem/getDocFlowList',
});
```
…deleting ~60 lines of effect boilerplate (lines 42-109).

### Root page (optional, same PR or follow-up):
```ts
useDocumentEditRightBar({
  uuid,
  tab1Mode: 'actionLog',
  docType: 'purchaseContract',
  i18nNamespace: 'purchaseContract',
  helpDocumentName: ['PurchaseContractHelpDocument', 'PurchaseContractMaterialItemHelpDocument'],
  i18nPath: 'supplyChain',
  statusLabelMap: PurchaseContractManager.getStatusLabelMap(),
  // no getDocFlowListURL → help-only, matches current behavior
});
```
…replacing lines 66-106. (The root page keeps all its *other* wiring — panel registry,
multiSelect modal, DocActionModal — untouched.)

**Optional refinement (decide in review):** most config is derivable — `docType` =
`Manager.getItemNodeInstId()`/`getRootNodeInstId()`; `getDocFlowListURL` = the controller's
`readonly`; `statusLabelMap`/namespace = the Manager. The hook could take
`(controller, { tab1Mode, helpDocumentName, i18nPath, mode })` and read the rest off the
controller/Manager, shrinking the call site further. Deferred as a follow-up to avoid widening
the surface in one step.

---

## 5. Steps
1. Add `src/composables/useDocumentEditRightBar.ts` (hook + config type) — port the effect
   logic verbatim from the item page (the more complete of the two).
2. Rewrite `PurchaseContractItemEditPage.tsx` to call the hook; delete the inlined effects +
   now-unused imports (`useEffect`, `useRightBarContent`, `initHelpDocumentWithDocFlow`, `i18n`).
3. (Same PR or follow-up) Rewrite the root page's right-bar effects (66-106) via the hook,
   help-only mode.
4. Verify: `tsc` clean on touched files; `vite build` clean; existing tests green; add a mount
   test asserting both pages still mount and register right-bar state (spy the RightBar writers /
   `initHelpDocumentWithDocFlow`).

## 6. What this does NOT change
- `initHelpDocumentWithDocFlow` / help-document services — reused as-is.
- The root page's panel-registry, multiSelect-modal, DocActionModal wiring — untouched.
- Any controller/Manager API — the hook consumes existing values.

## 7. Open questions for review
1. **Hook, not class** — confirm a shared hook is the intended vehicle (the request said "super
   general class"; for RC function-component effect logic a hook is the idiomatic equivalent —
   see §1 scope note). If a *class* is truly wanted, the only class-shaped part is the *config*,
   which can move onto the Manager/controller (the §4 refinement).
2. **Scope:** item page only, or both pages in one PR? (Recommend both — proves the generic hook
   against its two real shapes.)
3. **Config-from-controller refinement** now or later? (Recommend later.)
