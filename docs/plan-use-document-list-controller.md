# Plan — Extract `useDocumentListController` from `usePurchaseContractListController`

**Status:** IMPLEMENTED — 2026-07-15 (recommended defaults: `useDocumentListController`, one
tier, `dataSource` promoted). Verified: 8/8 tests pass (incl. a new page-mount test), `vite
build` clean. The mount test also surfaced + fixed a latent i18n regression (flat
`commonElements:search` had been nested away in a prior step, breaking the process-button label).
**Date:** 2026-07-15
**Target project:** `/Users/I043125/work2/IntelligentUI/`

---

## 1. Goal

`usePurchaseContractListController` (27 lines) is almost entirely **generic list-page
state plumbing**: it wires `navigate`, an `actionRef`, `selectedRowKeys` state, and an
(unused) `dataSource` pair into `ServiceListControllerDeps`, then constructs the controller
inside a `useMemo`. The **only** PurchaseContract-specific things are:

- which controller class to `new` (`PurchaseContractListController`)
- the i18n side-effect import (`@/services/logistics/PurchaseContractManager`)

Mirror what we did on the edit side (`usePurchaseContractEditController` → thin adapter over
the generic `useDocumentEditController` → `useServiceEntityEditController`) so future list
pages drop the boilerplate and only supply a controller factory.

---

## 2. Current state (verified from source)

`usePurchaseContractListController.ts:7-27` — holds `useNavigate`, `useRef<ActionType>`,
`useState<React.Key[]>(selectedRowKeys)`, and a `useMemo` (keyed on `selectedRowKeys`) that
constructs the controller with `ServiceListControllerDeps`:

```ts
new PurchaseContractListController({
  actionRef, dataSource: [], setDataSource: () => {},
  selectedRowKeys, setSelectedRowKeys, navigate,
})
```

`ServiceListControllerDeps<T>` (`ServiceListController.ts:42-55`) requires exactly:
`actionRef`, `dataSource`, `setDataSource`, `selectedRowKeys`, `setSelectedRowKeys`, `navigate`.

**Honest scoping notes**
- **Only ONE list hook exists today** (PurchaseContract). This extraction removes no current
  duplication — it pays off when the *next* list module is migrated. It is a forward-looking
  consolidation, consistent with the edit-side precedent, not an immediate dedup win.
- The list hook is **simpler than the edit hook**: no route `:uuid`, no async fetch-on-mount,
  no `loading` gate. It is pure state plumbing. So it maps to the *middle* tier, not the full
  `useServiceEntity*` lifecycle core.
- `dataSource`/`setDataSource` are passed but **unused** by `PurchaseContractListController`
  (its `request()` hits the real API via `PurchaseContractManager.listDocuments`). They remain
  in the generic deps for in-memory list controllers that still use `filterData()`.

---

## 3. Two-tier question (mirror the edit side?)

The edit side has two layers: `useServiceEntityEditController` (universal core) and
`useDocumentEditController` / `useItemEditController` (per-shape wrappers). For lists there is
currently **one shape** (a ProTable list page), so a second tier buys nothing yet.

**Recommendation:** create **one** generic hook, `useDocumentListController`, now. Only split
out a lower `useServiceEntityListController` if/when a genuinely different list shape appears
(e.g. a tree-list or a nested sub-list with different state). Naming it `useDocumentList*`
keeps it parallel to `useDocumentEditController`. (The user floated
`useServiceEntityListController` as the alternative name — see §6 open question.)

---

## 4. Design

### New file: `src/composables/useDocumentListController.ts`

```ts
import { useRef, useState, useMemo } from 'react';
import type { ActionType } from '@ant-design/pro-components';
import { useNavigate } from 'react-router-dom';
import type { ServiceListControllerDeps } from '@/controllers/ServiceListController';

/** Per-entity config injected by each concrete list hook. */
export interface DocumentListControllerConfig<T, TController> {
  /** Construct the concrete ServiceListController subclass from the resolved deps. */
  buildController: (deps: ServiceListControllerDeps<T>) => TController;
}

/**
 * Generic React-lifecycle hook for a document list page — the entity-agnostic shell
 * shared by every ServiceListController subclass. Owns the ProTable actionRef, the
 * selected-row-keys state, and an (optional) in-memory dataSource pair, and constructs
 * the concrete controller from ServiceListControllerDeps.
 *
 * Mirrors useDocumentEditController on the list side. Per-entity code supplies only the
 * controller factory; the i18n namespace registration stays in the concrete hook (a
 * side-effect import), same as the edit wrappers.
 */
export function useDocumentListController<T, TController>(
  config: DocumentListControllerConfig<T, TController>,
) {
  const navigate = useNavigate();
  const actionRef = useRef<ActionType>(undefined);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [dataSource, setDataSource] = useState<T[]>([]);

  const controller = useMemo(
    () => config.buildController({
      actionRef, dataSource, setDataSource, selectedRowKeys, setSelectedRowKeys, navigate,
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [selectedRowKeys, dataSource],
  );

  return { controller };
}
```

**Deviation from the current hook (deliberate, verify in review):**
- The current hook hardcodes `dataSource: []` + `setDataSource: () => {}` (no-ops) because
  PurchaseContract fetches from the API. The generic hook promotes these to real
  `useState<T[]>` so in-memory list controllers (that use `filterData`/`handleDelete`/
  `handleBulkDelete`) work too. This is a superset — PurchaseContract simply won't read them.
  Adds `dataSource` to the `useMemo` deps so state-changing deletes re-instantiate correctly.
  - *Risk:* if any current behavior relies on `dataSource` being a frozen `[]`, this changes
    it. PurchaseContract's `request()` ignores `deps.dataSource`, so no behavior change expected
    — but this is the one thing to confirm at runtime (Step 4).

### Rewrite: `usePurchaseContractListController.ts` (thin adapter, ~10 lines)

```ts
import '@/services/logistics/PurchaseContractManager'; // registers purchaseContract i18n namespace
import { useDocumentListController } from '@/composables/useDocumentListController';
import { PurchaseContractListController } from './PurchaseContractListController';
import type { PurchaseContractListItem } from '@/mock/contracts';

export function useContractListController() {
  return useDocumentListController<PurchaseContractListItem, PurchaseContractListController>({
    buildController: (deps) => new PurchaseContractListController(deps),
  });
}
```

Return shape stays `{ controller }` — `PurchaseContractListPage.tsx:6` is unchanged.

---

## 5. Steps

1. **Add** `src/composables/useDocumentListController.ts` (generic hook + config type).
2. **Rewrite** `usePurchaseContractListController.ts` as the thin adapter (keep the i18n
   side-effect import and the `useContractListController` name + `{ controller }` return).
3. **Verify:** `PurchaseContractListPage.tsx` unchanged; `tsc` clean on touched files;
   `vite build` clean; existing `PurchaseContractSearch.test.tsx` (7 tests) still green
   (its `makeController` builds the controller directly, so it's unaffected — good
   independent check that the deps shape didn't drift).
4. **Runtime check:** load `/logistics/purchaseContract` — search + table still work; row
   selection + any bulk action still work (confirms the `dataSource` state promotion is benign).

---

## 6. Open questions for review

1. **Name:** `useDocumentListController` (parallels `useDocumentEditController`) vs the
   user-floated `useServiceEntityListController`. Recommendation: `useDocumentListController`
   now; reserve `useServiceEntityListController` for a future lower tier only if a second list
   shape needs it. Which do you want?
2. **`dataSource` promotion:** promote to real `useState` (superset, supports in-memory list
   controllers) — or keep the current no-op `[]` to guarantee zero behavior change for
   PurchaseContract and let in-memory controllers override later? Recommendation: promote.
3. **Two tiers now, or one?** Recommendation: one (`useDocumentListController`) until a second
   list shape justifies a shared core.
