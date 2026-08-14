# Plan — Merge PurchaseContractApi into PurchaseContractManager (record-load simplification)

**Status:** FULLY IMPLEMENTED (Steps 1–6 + the `listDocuments` move) — 2026-07-14.
`purchaseContractApi.ts` is deleted; all its logic now lives on `PurchaseContractManager` /
`ServiceManager`. Verified: 7/7 tests pass, `vite build` clean.
**Date:** 2026-07-14
**Target project:** `/Users/I043125/work2/IntelligentUI/`

---

## 1. Goal

The current Purchase Contract UI stack has, per page, a `use***Controller` hook, a
`***Controller` class, plus a single shared `PurchaseContractApi` and a single
`PurchaseContractManager`. The record-load path is more indirect than it needs to be:

- `purchaseContractApi.getContract(uuid)` / `getContractMaterialItem(uuid)` do **not**
  contain any fetch logic themselves. They build a **throwaway controller instance**
  (via the `fetchOnlyDeps()` hack) purely so they can call the instance method
  `loadModuleEdit()` on `ServiceBaseController`.
- The real work — resolve a URL prefix, call `apiGet('<prefix>/loadModuleEditService')`
  — is generic and already lives on `ServiceBaseController`, keyed off the Manager's
  `getRootNodeInstId()` / `getItemNodeInstId()`.

**Objective:** let `PurchaseContractManager` load a document / doc-material-item directly,
delete the throwaway-controller machinery, and reduce the number of moving parts — without
introducing a fragile new layer.

---

## 2. Current state (verified from source)

### 2.1 The load path today

```
usePurchaseContractEditController.ts
  fetchRecord: getContract                          ← purchaseContractApi.ts:184
      └─ new PurchaseContractEditController(fetchOnlyDeps<...>())   ← throwaway instance
             └─ .loadModuleEdit<T>(uuid)             ← ServiceBaseController.ts:143
                    └─ getPrefixURL()                ← ServiceBaseController.ts:126
                           └─ getServiceManager().getRootNodeInstId()  ← PurchaseContractManager.ts:422
                    └─ apiGet('<prefix>/loadModuleEditService', {uuid})
```

The item path is identical except the controller is `PurchaseContractMaterialItemController`
and `getPrefixURL()` is overridden in `DocItemEditController.ts:58` to use
`getItemNodeInstId()` (`PurchaseContractManager.ts:430`).

### 2.2 The `loadModuleEdit` implementation (already fully generic)

`src/controllers/ServiceBaseController.ts:143-147`

```ts
loadModuleEdit<T>(uuid: string): Promise<T | undefined> {
    const prefixURL = this.getPrefixURL();
    if (!prefixURL) return Promise.resolve(undefined);
    return apiGet<T>(`${prefixURL}/loadModuleEditService`, { uuid });
}
```

There is **nothing module-specific** here — it only needs a prefix string.

### 2.3 The `fetchOnlyDeps` hack (the real complication to remove)

`src/api/purchaseContractApi.ts:38-50`

```ts
function fetchOnlyDeps<D>(): D {
    return { processMode: PROCESSMODE_EDIT, uuid: undefined, navigate: () => {} } as unknown as D;
}
```

Both `getContract` and `getContractMaterialItem` construct a full controller instance
with these inert deps just to reach `loadModuleEdit`. This is the indirection worth deleting.

### 2.4 The five exports in `purchaseContractApi.ts`

| Export | Lines | Belongs on Manager? | Notes |
|---|---|---|---|
| `listDocuments` (was `listContracts`) | 95–174 | ✅ move → `ServiceManager.listDocuments` | After the `content` pass-through decouple this is now *generic*: DataTables protocol + `content` passed straight through; the only module-specific bit is the URL prefix, which the Manager already owns via `getRootNodeInstId()`. Move as a generic `static listDocuments<T>()` on the base `ServiceManager` (inherited by every module). Renamed `listContracts`→`listDocuments` + `ContractListQuery`→`DocumentListQuery` (the "contract" here is the root-level *document*) |
| `getContract` | 184–187 | ✅ move → `loadDocument` | uses `fetchOnlyDeps` hack |
| `getContractMaterialItem` | 195–198 | ✅ move → `loadDocMatItem` | uses `fetchOnlyDeps` hack |
| `getDocActionConfigureList` | 208–210 | ⬜ optional | thin wrapper over `docActionApi` generic |
| `executeDocAction` | 228–239 | ⬜ optional | thin wrapper over `docActionApi` generic |

---

## 3. Assessment of the original proposal

| Proposed step | Verdict | Reason |
|---|---|---|
| Merge Api into Manager | ✅ Do it (load methods + `listDocuments`) | `listDocuments` moves too — now generic after the decouple; doc-action wrappers optional |
| `getContract` → `loadDocument` | ✅ Correct | |
| `getContractMaterialItem` → `loadDocMatItem` | ✅ Correct | |
| Move `loadModuleEdit` logic from `ServiceBasicController` → `ServiceManager` | ⚠️ Adjust | The logic is already generic and already on `ServiceBaseController`. Nothing to "move". Instead **add a parallel static path** on the Manager; **keep** the instance method for the live editor page. |
| Make `getPrefixURL` static on each **Controller**, then feed it into `loadDocument` | ❌ Drop (consume prefix in the **Manager** instead) | Wrong layer. The prefix already exists as static Manager data (`getRootNodeInstId` / `getItemNodeInstId`). **Decision:** the Manager's own methods — `listDocuments`, `loadDocument`, `loadDocMatItem` — consume `getRootNodeInstId()` / `getItemNodeInstId()` directly to build their URLs (`${getRootNodeInstId()}/searchTableService`, etc.). The controller is never involved in prefix resolution. The root-vs-item split rides on which node-id the Manager method reads, not on an instance override. |
| Same for `getRootNodeInstId` / `getItemNodeInstId` static in each Manager | ✅ Already true | Both are already `static` on `PurchaseContractManager` (`ServiceManager.ts:180,187`). No change needed. |

**Answer to the embedded question — "can `getPrefixURL` be a static method in each controller?"**
Technically yes (TS allows static methods), but it's the wrong place. The root/item prefix
distinction is Manager data that already exists as `getRootNodeInstId()` / `getItemNodeInstId()`.
Have the Manager's own load methods pick the right one; the controller never needs to be involved.

---

## 4. Refined plan

### Step 1 — Add static load methods to `ServiceManager`
`src/services/ServiceManager.ts`

```ts
import { apiGet } from '@/api/apiClient';

// ── Record load (static) ──────────────────────────────────────────────
/** Load a root document record by uuid. Keyed off getRootNodeInstId(). */
static loadDocument<T>(uuid: string): Promise<T | undefined> {
    return this.loadByPrefix<T>(this.getRootNodeInstId(), uuid);
}

/** Load a doc-material-item record by uuid. Keyed off getItemNodeInstId(). */
static loadDocMatItem<T>(uuid: string): Promise<T | undefined> {
    return this.loadByPrefix<T>(this.getItemNodeInstId(), uuid);
}

/** Shared: resolve prefix → GET <prefix>/loadModuleEditService?uuid=... */
protected static loadByPrefix<T>(prefix: string, uuid: string): Promise<T | undefined> {
    if (!prefix) return Promise.resolve(undefined);
    return apiGet<T>(`${prefix}/loadModuleEditService`, { uuid });
}
```

- Uses the two static node-id methods that already exist — no new prefix logic.
- Every subclass Manager inherits `loadDocument` / `loadDocMatItem` for free.

### Step 2 — Delete `getContract` / `getContractMaterialItem` and the `fetchOnlyDeps` hack
`src/api/purchaseContractApi.ts`

- Remove `getContract` (184–187), `getContractMaterialItem` (195–198), `fetchOnlyDeps` (38–50),
  and the now-unused imports of the two controller classes + `PROCESSMODE_EDIT`.

### Step 3 — Point the two hook call sites at the Manager
- `usePurchaseContractEditController.ts:27` — `fetchRecord: getContract`
  → `fetchRecord: (uuid) => PurchaseContractManager.loadDocument(uuid)`
- `usePurchaseContractMaterialItemController.ts:26` — `fetchRecord: getContractMaterialItem`
  → `fetchRecord: (uuid) => PurchaseContractManager.loadDocMatItem(uuid)`

(Verify the `fetchRecord` signature in
`useDocumentEditController.ts:51` / `useItemEditController.ts:31` accepts this shape.)

### Step 4 — Keep `ServiceBaseController.loadModuleEdit` (instance) as-is
The live editor page loads its record through its real controller instance. Do **not** delete
the instance method — only the fake-instance call sites in the API go away.

### Step 5 — Do NOT add static `getPrefixURL` to controllers
No controller change required for the load path.

### Step 6 (OPTIONAL) — Move the doc-action wrappers onto the Manager
Only if you want the fuller merge. `getDocActionConfigureList` (208–210) and
`executeDocAction` (228–239) become `PurchaseContractManager` statics that call the
`docActionApi` generics with the module URL. Then update:
- `PurchaseContractEditController.tsx:10,223` (`executeDocAction` import + call)
- `usePurchaseContractEditController.ts:2,28` (`getDocActionConfigureList`)

Move `listDocuments` onto `ServiceManager` as a generic `static listDocuments<T>(query)`:
it builds the DataTables body (`draw`/`start`/`length`), POSTs to
`${getRootNodeInstId()}/searchTableService`, and unwraps the response. It is generic after the
`content` pass-through decouple, so every module Manager inherits it. Delete the API function.

---

## 5. Blast radius (grep-verified call sites)

| File | Line | Reference | Action |
|---|---|---|---|
| `usePurchaseContractEditController.ts` | 2, 27 | `getContract` | repoint to `Manager.loadDocument` |
| `usePurchaseContractEditController.ts` | 2, 28 | `getDocActionConfigureList` | Step 6 only |
| `usePurchaseContractMaterialItemController.ts` | 2, 26 | `getContractMaterialItem` | repoint to `Manager.loadDocMatItem` |
| `PurchaseContractEditController.tsx` | 10, 223 | `executeDocAction` | Step 6 only |
| `PurchaseContractListController.tsx` | 12, 282 | `listDocuments` (was `listContracts`) | repoint to `PurchaseContractManager.listDocuments` (inherited from `ServiceManager`) |
| `PurchaseContractSearch.test.tsx` | 122–124 | `listDocuments` spy (was `listContracts`) | repoint spy to `PurchaseContractManager.listDocuments` |
| `purchaseContractApi.ts` | 38–50, 184–198 | `fetchOnlyDeps`, both getters | delete |

No other module imports `getContract` / `getContractMaterialItem`.

---

## 6. Net effect

- **Deleted:** `fetchOnlyDeps` + two throwaway controller instantiations + two API getters +
  `listDocuments`/`DocumentListQuery`/`DataTableResponse`/`_drawCounter` from the API file
  (+ optionally two doc-action wrappers).
- **Added:** a generic `static listDocuments<T>()` + the four load/list static methods on
  `ServiceManager` (all inherited by every module Manager; all consume `getRootNodeInstId()` /
  `getItemNodeInstId()` for their URLs).
- **Unchanged:** `ServiceBaseController.loadModuleEdit` instance method, controller classes,
  node-id methods (already static).
- Achieves the stated goal (Manager owns the load, fewer moving parts) while deleting *more*
  boilerplate than the original proposal and avoiding the fragile static-controller layer.

---

## 7. Open questions for review

1. **Item-load endpoint mismatch?** The API doc comment (`purchaseContractApi.ts:9,193`) and the
   item controller both use `purchaseContractMaterialItem/loadModuleEditService`. But
   `PurchaseContractManager.getLoadDocItemBaseURL()` (346–348) returns a *different* legacy URL
   `loadModuleViewService.html`. Confirm which endpoint the item edit path should hit before wiring
   `loadDocMatItem` — the plan assumes `loadModuleEditService` (matches current behaviour).
2. **Scope of merge:** include Step 6 (doc-action wrappers) now, or keep this change to the
   two load methods only?
3. **Generalize beyond PurchaseContract?** Since the new methods live on `ServiceManager`, every
   other module's `use***Controller` could drop its own `getXxx`/`fetchOnlyDeps` equivalent the
   same way. Do we roll this out module-by-module, or land PurchaseContract first as the template?
