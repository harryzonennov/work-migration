# searchTableService — Legacy Protocol & New UI Migration Guide

## Overview

`searchTableService` is the backend endpoint used by ALL list pages to fetch paginated, filtered data.
It follows the **jQuery DataTables server-side processing protocol** — a specific request/response contract
that differs from standard REST pagination. This document explains the full flow, the exact wire format,
and how to migrate correctly to the new React UI.

---

## 1. Full Legacy Flow

```
User clicks Search
      ↓
PurchaseContractList.js: vm.searchModule()
      ↓
ServiceListControlHelper mixin: getPageRef().searchModule()
      ↓
ServiceDataTableFrame.vue: vm.datatable.datatable.ajax.reload()
      ↓
jQuery DataTables fires AJAX:
  POST ../purchaseContract/searchTableService.html
  body: buildSearchData(d)    ← d = DataTables internal params object
      ↓
PurchaseContractListController.java: searchTableService(@RequestBody String request)
      ↓
serviceBasicUtilityController.searchTableServiceWrapper(...)
      ↓
SEListController.searchDataTableCore(request, ...)
      ↓
Parses: draw, start, length from root, searchModel from root.content
Calls: searchCore(searchModel) → rawList
Slices: rawList.subList(start, start+length)
Returns: DataTableResponseData JSON (no wrapper envelope)
```

---

## 2. Request Body (POST JSON)

The body is built by `buildSearchData(d)` in `ServiceListControlHelper`:

```javascript
buildSearchData: function(d) {
    d.content = vm.searchContent;   // merged into the DataTables `d` object
    return JSON.stringify(d);
}
```

`d` is the DataTables internal params object. The final JSON sent to the server looks like:

```json
{
  "draw": 3,
  "start": 0,
  "length": 15,
  "content": {
    "headerModel": {
      "uuid": "",
      "id": "",
      "name": "",
      "status": "",
      "priorityCode": "",
      "productionBatchNumber": ""
    },
    "createdUpdateModel": { ... },
    "purchaseFromSupplier": {
      "uuid": "", "status": "", "name": "", "id": "",
      "telephone": "", "mobile": "", "address": "", "accountType": "",
      "countryName": "", "streetName": "", "houseNumber": "", "townZone": "",
      "subArea": "", "stateName": "", "cityName": "",
      "contactName": "", "contactId": "", "qqNumber": "", "weiXinID": ""
    },
    "purchaseToOrg": { ... same shape as purchaseFromSupplier ... },
    "prevDoc":       { "docFlowDirection": "", "targetDocType": "", "targetDocMatItemUUID": "", "targetDocName": "", "targetDocId": "", "targetDocStatus": "" },
    "prevProfDoc":   { ... },
    "nextProfDoc":   { ... },
    "nextDoc":       { ... },
    "reservedByDoc": { ... },
    "submittedBy":   { "executedByUserId": "", "executedByUserName": "", "executionTimeHigh": "", "executionTimeLow": "" },
    "approvedBy":    { ... same as submittedBy ... },
    "deliveryDoneBy":{ ... same as submittedBy ... },
    "signDateLow":  null,
    "signDateHigh": null,
    "itemMaterialSKU": { "refMaterialSKUId": "", "refMaterialSKUName": "", "serialId": "", "packageStandard": "", "traceMode": "", "traceStatus": "", "supplyType": "", "refMaterialTypeUUID": "", "refMaterialTypeId": "", "refMaterialTypeName": "" }
  }
}
```

**Key points:**
- `draw`, `start`, `length` are **top-level** — NOT inside `content`
- `content` contains the search filters — it maps to `PurchaseContractSearchModel` on the backend
- `start` = row offset (e.g. page 2 with 15 rows/page → `start=15`)
- `length` = page size; `-1` means "all records"
- `signDateLow` / `signDateHigh` are **top-level inside `content`** — NOT inside `headerModel`
- `purchaseFromSupplier` / `purchaseToOrg` use `contactName` for supplier name search — NOT `partyName` (`partyName` is a response-side field on `InvolvePartyUIModel`, not a search field on `AccountSearchSubModel`)

---

## 3. Backend Parsing (`SEListController.searchDataTableCore`)

```java
JsonNode root = mapper.readTree(request);
SEUIComModel searchModel = parseRequestToSearchModel(request, searchModelClass);
// parseRequestToSearchModel reads: root.path("content") → searchModel

int start  = root.path("start").asInt();   // row offset
int draw   = root.path("draw").asInt();    // echo value
int length = root.path("length").asInt();  // page size
int pageEnd = start + length;

DataTableRequestData req = new DataTableRequestData(searchModel, start, length, pageEnd, 0);
BSearchResponse response = searchCore.apply(req);

// Slices the result list: rawList.subList(start, min(pageEnd, total))
```

The backend does **NOT** use page numbers — it uses `start` (offset) and `length` (count).

---

## 4. Response Body (NOT wrapped in `{result, content}`)

**Critical difference:** `searchTableService` responses are **NOT** wrapped in the standard
`{ result: 'success', content: ... }` envelope. They return the DataTables format directly:

```json
{
  "draw": 3,
  "recordsTotal": 142,
  "recordsFiltered": 142,
  "data": [
    {
      "purchaseContractUIModel": { "uuid": "...", "id": "PC-001", "name": "...", "status": 2, ... },
      "purchaseFromSupplierUIModel": { "partyName": "Supplier Co.", "contactName": "..." },
      "purchaseToOrgUIModel": { "partyName": "Buying Org", ... }
    },
    ...
  ]
}
```

| Field | Type | Meaning |
|---|---|---|
| `draw` | int | Echo of the request `draw` value — DataTables uses this to discard stale responses |
| `recordsTotal` | int | Total records in the database (before filtering) |
| `recordsFiltered` | int | Records after applying search filters (usually same as `recordsTotal` here) |
| `data` | array | Page of `PurchaseContractServiceUIModel` objects |

---

## 5. The Bug in the Current New UI

**Current `purchaseContractApi.ts`:**

```typescript
const raw = await apiPost<any>('purchaseContract/searchTableService', query);
// apiPost calls unwrap() which does:  return (json.content ?? json) as T
```

**Problem 1 — Wrong response unwrapping:**
`apiClient.unwrap()` tries to read `json.content`, but `searchTableService` returns no `content` wrapper.
`json.content` is `undefined`, so it falls back to `json` (the whole response) — this part happens to work.
BUT — it then tries to read `raw.data`, `raw.dataList`, `raw.rows` to find the array.
The backend returns `data` as the array key, so `raw.data` should work — **unless** the backend is also
not receiving the right request body.

**Problem 2 — Wrong request body (root cause):**
The backend expects `{ draw, start, length, content: { ...filters } }` at the top level.
But `listContracts(query)` sends the ProTable params directly:
```typescript
{ id: '', name: '', status: undefined, current: 1, pageSize: 10 }
```
The backend reads `root.path("start")` → 0 (missing → defaults to 0)
The backend reads `root.path("length")` → 0 (missing → defaults to 0, means **return 0 rows**)
The backend reads `root.path("content")` → empty object → no filters applied

When `length = 0`, the backend's slice logic returns an empty list every time.

---

## 6. Migration Fix

Update `purchaseContractApi.ts` to send the correct DataTables protocol:

```typescript
export interface ContractSearchFilters {
  headerModel?: {
    id?: string;
    name?: string;
    status?: number;
  };
  purchaseFromSupplier?: { partyName?: string; partyId?: string };
  purchaseToOrg?: { partyName?: string; partyId?: string };
  // ... other filter sub-models
}

export interface DataTableRequest {
  draw: number;
  start: number;    // row offset = (page - 1) * pageSize
  length: number;   // page size
  content: ContractSearchFilters;
}

export interface DataTableResponse<T> {
  draw: number;
  recordsTotal: number;
  recordsFiltered: number;
  data: T[];
}

let _drawCounter = 0;

export async function listContracts(
  query: ContractListQuery = {}
): Promise<PagedResponse<PurchaseContractServiceUIModel>> {
  const { current = 1, pageSize = 15, ...filters } = query;
  const start = (current - 1) * pageSize;

  const body: DataTableRequest = {
    draw: ++_drawCounter,
    start,
    length: pageSize,
    content: {
      headerModel: {
        id: filters.id,
        name: filters.name,
        status: filters.status,
      },
      purchaseFromSupplier: filters.supplierName
        ? { partyName: filters.supplierName }
        : undefined,
    },
  };

  // searchTableService does NOT return the standard { result, content } envelope
  // — call fetch directly and parse the DataTables response shape
  const res = await fetch('/api/purchaseContract/searchTableService', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
    credentials: 'include',
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  const raw: DataTableResponse<PurchaseContractServiceUIModel> = await res.json();

  return {
    data: raw.data ?? [],
    total: raw.recordsTotal ?? 0,
    current,
    pageSize,
  };
}
```

**Why bypass `apiPost`:** `apiPost` calls `unwrap()` which checks for `json.result === 'error'`
and expects a `content` wrapper. `searchTableService` sends neither — parsing the response directly
is cleaner than adding special cases to the shared client.

---

## 7. ProTable ↔ DataTables Param Mapping

| ProTable param (React) | DataTables / backend param | Notes |
|---|---|---|
| `current` (page number, 1-based) | `start` = `(current-1) * pageSize` | Convert page → offset |
| `pageSize` | `length` | Direct mapping |
| Search field values | `content.headerModel.*` | Nest under `content` |
| Supplier filter | `content.purchaseFromSupplier.partyName` | Party sub-model |
| Org filter | `content.purchaseToOrg.partyName` | Party sub-model |
| Date range `signDateLow/High` | `content.headerModel.signDateLow/signDateHigh` | ISO strings |
| — | `draw` | Increment per request; echo in response used by DataTables to discard stale |

---

## 8. `searchModuleService` vs `searchTableService` — Difference

| Endpoint | Protocol | Pagination | When to use |
|---|---|---|---|
| `searchModuleService` | Standard `{ result, content }` envelope | Returns ALL matching records (no paging) | Cross-doc selectors, popups, small datasets |
| `searchTableService` | DataTables protocol (`draw/start/length`) | Server-side paging via `start`+`length` | Main list pages — large datasets |

The new UI list pages should **always** call `searchTableService`, not `searchModuleService`.

---

## 9. Pattern for All Future Modules

Every `[entity]Api.ts` `list[Entity]()` function must:

1. Convert ProTable `{ current, pageSize }` → DataTables `{ draw, start, length }`
2. Nest all search filters under `content: { ... }`
3. Call `fetch` directly (NOT `apiPost`) to avoid the `content` unwrap
4. Parse the `{ draw, recordsTotal, recordsFiltered, data }` response shape
5. Return `{ data, total: recordsTotal, current, pageSize }`

Template:
```typescript
async function list[Entity](query = {}) {
  const { current = 1, pageSize = 15, ...filters } = query;
  const body = {
    draw: ++drawCounter,
    start: (current - 1) * pageSize,
    length: pageSize,
    content: buildSearchContent(filters),  // maps filter fields → SearchModel shape
  };
  const res = await fetch(`/api/[entity]/searchTableService`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
    credentials: 'include',
  });
  const raw = await res.json();
  return { data: raw.data ?? [], total: raw.recordsTotal ?? 0, current, pageSize };
}
```

---

## Files Referenced

| File | Role |
|---|---|
| `admin/js/DataTable.init.js` | `ServiceDataTable.buildWithServer()` — jQuery DataTables AJAX config |
| `admin/js/component/ServiceDataTableFrame.js` | `loadModuleList()` — wires `buildSearchData` as `fnAjaxData` |
| `admin/js/supplyChain/PurchaseContractList.js` | `buildSearchData()`, `searchContent` structure |
| `admin/js/component/basicElements/ServiceUiController.js` | `buildSearchData()` base implementation |
| `ThorsteinPlatform/.../SEListController.java` | `searchDataTableCore()` — parses `draw/start/length/content` |
| `ThorsteinPlatform/.../DataTableResponseData.java` | Response shape constants: `draw`, `recordsTotal`, `recordsFiltered`, `data` |
| `ThorsteinPlatform/.../DataTableRequestData.java` | Request model: `start`, `length`, `pageEnd` |
| `IntelligentUI/src/api/purchaseContractApi.ts` | Current (broken) implementation — needs fix per Section 6 |
