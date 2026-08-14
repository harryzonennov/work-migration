# Plan: Migrate `docPopConfig` — Document Popover Feature (Remainder)

> **Status:** Planning document. No code changes yet.
> **Created:** 2026-07-06
> **Companion docs:** [`plan-attachment-section-migration.md`](./plan-attachment-section-migration.md), [`plan-doc-item-multiselect-migration.md`](./plan-doc-item-multiselect-migration.md)

---

## 1. What is `docPopConfig`?

`docPopConfig` is a **table-cell configuration** that turns a plain data cell (e.g. a document ID, a material SKU code) into a **clickable capsule** with a leading document-type icon. Clicking the capsule pops up a **quick-view card** showing the referenced document's key fields (id, name, status, dates, responsible-person) — without leaving the current page.

Legacy usage (from `PurchaseContractEditor.js:447` and `PurchaseContractList.js:253`):

```js
{
    fieldName: 'purchaseContractMaterialItemUIModel.refMaterialSKUId',
    docPopConfig: {
        documentType: DocumentConstants.DummyDocumentType.MaterialStockKeepUnit,
        uuidFieldName: 'purchaseContractMaterialItemUIModel.refMaterialSKUUUID'
    }
}
```

The runtime pipeline:

```
column render → detect docPopConfig
  → resolve manager via DocumentManagerFactory.getDocumentManagerDef(documentType)
  → call manager.getDocumentPopoverContent() → returns { fieldMetaList, referenceDate, responsibleByField, targetPage }
  → render capsule <span> + wire click handler
  → on click: fetch record by uuid → populate popover card body → show
```

The capsule renders as a rounded pill: `<span class="popover-docXXX">` with a leading MDI icon (via `formatDocTypeIconClass(documentType)`), the cell display value, and a native Bootstrap Popover triggered on click.

## 2. Current state — audit summary

Both codebases were audited (see conversation history 2026-07-06). Verified facts:

### 2.1 What's ALREADY IMPLEMENTED in `IntelligentUI/src/`

| Piece | File | Lines | State |
|---|---|---|---|
| Popover card component | `components/doc/DocPopoverCard.tsx` | 201 | ✅ Done — renders header + fields + i18n + status icons |
| Popover-wrap logic for **list pages** | `components/page/AsyncEmbeddedListSection.tsx` | 173–229 | ✅ Done — wraps ProTable cells in antd `<Popover>` when `docPopConfig` is present |
| `PopoverCardConfig` interface | `services/ServiceManager.ts` | 60–71 | ✅ Done |
| Manager registry / factory | `services/DocumentManagerFactory.ts` | 421 (getDocumentManagerDef), 125 (getDocPopoverConfig), 315 (formatDocTypeIconClass) | ⚠️ Partial — only `PurchaseContractManager` is imported; the rest are `declare const ... any` stubs |
| Concrete manager | `services/logistics/PurchaseContractManager.ts` | 350–378 (getDocumentPopoverContent), 384–408 (getDocItemPopoverContent) | ✅ Done |
| List page usage | `pages/logistics/purchaseContract/PurchaseContractListController.tsx` | 178 (docPopConfig on `id` column), 261 (`fetchRecord`) | ✅ Done |
| `docPopConfig` on **edit-page material item** | `pages/logistics/purchaseContract/PurchaseContractEditController.tsx` | 581 | ⚠️ Declared but NOT rendered — EditableProTable ignores it |
| `DocumentConstants.DocumentType` / `DummyDocumentType` | `services/DocumentConstants.ts` | 978 lines total, 40+ real + 30+ dummy entries | ✅ Done |
| Type: `ListColumnJson.docPopConfig` | `controllers/ListPageTypes.ts` | 36–50 | ✅ Done |
| Type: `FieldConfig.docPopConfig` | `controllers/PageMetaTypes.ts` | 144 | ⚠️ Declared but comment says "not yet supported" — accurate for edit pages only |
| Type: `ItemsTableColumnJson.docPopConfig` | `controllers/EditPageJsonTypes.ts` | — | ❌ **Not declared** — the edit-page items-table column type has no `docPopConfig` slot |

### 2.2 What is NOT yet done

1. **Edit-page items-table (EditableProTable) does not wire `docPopConfig`.**
   In `AsyncEmbeddedListSection.tsx:96–164` (Mode A — editor page), each column is built from `fieldMetaList` and passed straight into `EditableProTable` with no `docPopConfig` handling. The `PurchaseContractEditController.tsx:581` config is silently ignored.

2. **Only `PurchaseContractManager` is wired into the factory.**
   `getDocumentManagerDef()` at `DocumentManagerFactory.ts:421` has 25+ branches, but every non-PurchaseContract branch references a `declare const XxxManager: any` global — those globals are legacy `.js` files that were never actually imported. So `getDocPopoverConfig(anythingElse)` returns `undefined` and the popover silently no-ops.

3. **DummyDocumentType targets (Material, Employee, LogonUser, Organization, …) have no manager class at all in the new UI.**
   None of `MaterialStockKeepUnitManager`, `EmployeeManager`, `LogonUserManager`, `OrganizationManager` etc. exist as `.ts` files. Legacy has ~27 manager implementations (see the audit table); the new UI has 1.

4. **The capsule visual is missing.**
   The current wrap in `AsyncEmbeddedListSection.tsx:225` is just `<a style={{ cursor: 'pointer' }}>{displayNode}</a>` — a plain underlined link. Legacy renders a **rounded pill with a leading MDI icon** (see `PopDocumentUnion.js:167–170`: `<i class="md md-chat">` prefix on a `.embededTreeSpan` capsule). This is a visual regression from what the user is asking for.

5. **`fetchRecord` is per-controller.**
   Each list controller declares its own `fetchRecord(uuid)` (e.g. `PurchaseContractListController.tsx:261`). This works for a self-reference popover (contract → contract), but a **cross-type** popover (e.g. `MaterialStockKeepUnit` referenced from a `PurchaseContract` items-table) needs the **target manager's** `getModuleView` endpoint, not the source controller's fetchRecord. Currently there is no way to invoke the target manager's fetcher.

6. **SearchResult / other tables not evaluated.** Whether search results tables also need popovers is TBD — plan section 7.5 addresses.

---

## 3. Migration scope

### 3.1 Legacy scope (for reference, from earlier audit)

- 281 `docPopConfig` occurrences in legacy `.js` files
- 27 files declaring `docPopConfig` on fieldMeta
- 27 manager files implementing `getDocumentPopoverContent`
- ~19 DummyDocumentType values in active use
- Core infra: PopupCore.js (146) + PopDocumentUnion.js (183) + DocumentOrderMatPopInfo.js (1,143) + ServiceDataTableFrame docPop rendering (~250 lines) — the new UI has already replaced all of these with `DocPopoverCard.tsx` + antd Popover.

### 3.2 New-UI remaining scope

**Infrastructure (one-time)**
- Edit-page items-table popover wiring (~40 LOC in `AsyncEmbeddedListSection.tsx`)
- `PopDocumentUnion` visual — replace plain `<a>` with `<span class="embededTreeSpan popover-info">` + icon (~15 LOC of component + ~20 LOC SCSS)
- Cross-type fetchRecord — extend manager interface with `fetchRecord(uuid)` static (~10 LOC per manager)
- `ItemsTableColumnJson.docPopConfig` type declaration (~5 LOC)

**Per-doc-type (one manager at a time)**
- Per manager: `getDocumentPopoverContent()` + `getDocItemPopoverContent()` + `fetchRecord()` + register in factory (~60 LOC per manager)
- Add up: ~19 managers × 60 LOC = ~1,100 LOC across the migration lifetime; done incrementally alongside each doc-type Phase A migration.

## 4. Phased plan

### Phase 1 — Infrastructure fixes [~2 hours]

**Goal:** the popover framework is complete for every table on every page, so per-manager work becomes pure configuration.

#### 1.1 Wire `docPopConfig` on EditableProTable (edit-page items-table)

Currently `AsyncEmbeddedListSection.tsx:96–164` (Mode A) builds columns from `fieldMetaList` but ignores `docPopConfig`. Mirror the pattern already used in Mode B (line 185–230):

```tsx
// Inside Mode A, after `dataColumns` is built:
fieldMetaList.forEach((field, idx) => {
    if (!field.docPopConfig) return;
    const { documentType, uuidFieldName } = field.docPopConfig as { documentType: unknown; uuidFieldName: string };
    const popoverConfig = getDocPopoverConfig(documentType);
    if (!popoverConfig) return;
    // Wrap the column at index idx (+ 1 for actions-column offset)
    const col = dataColumns[idx];
    dataColumns[idx] = {
        ...col,
        editable: false,
        render: (_dom, record) => buildPopDocumentUnion(record, uuidFieldName, popoverConfig),
    };
});
```

The `buildPopDocumentUnion()` helper is extracted so Mode A and Mode B share it — mirrors `ServiceDataTable.buildPopDocumentUnion()` from `DataTable.init.js`.

#### 1.2 Add the capsule visual — `PopDocumentUnion` component

Replace the plain-link wrapper with a proper capsule that matches the legacy `PopDocumentUnion` component (`pop-document-union` in `PopDocumentUnion.js`).

The legacy pill uses `class="embededTreeSpan popover-docXXX"` (note spelling: one `d`) with an inner `<i class="md md-chat">` icon and the document ID as text. In the new UI we use a React equivalent with antd `Popover` and the `embededTreeSpan` class name preserved for CSS continuity.

The icon inside the pill in legacy is hardcoded to `md md-chat` (chat bubble) — **not** the document type icon. The document type icon (`DocumentManagerFactory.formatDocTypeIconClass(documentType)`) is used only in the **popover panel header**, passed as `headerIconClass` to `PopupCore`.

```tsx
// Mirrors legacy PopDocumentUnion embedded template:
//   <span class="embededTreeSpan" title="">
//     <i class="md md-chat"></i> {{ documentId }}
//   </span>
function PopDocumentUnion({ documentType, displayValue, uuid, popoverConfig, fetchRecord }: Props) {
    return (
        <Popover
            trigger="click"
            placement="right"
            content={<DocPopoverCard uuid={uuid} config={popoverConfig} fetchRecord={fetchRecord} />}
        >
            <span className="embededTreeSpan popover-info" title="">
                <i className="mdi mdi-comment-outline content-greyblue" />
                {' '}{displayValue}
            </span>
        </Popover>
    );
}
```

SCSS in `styles/overrides.scss` — mirrors `table span.embededTreeSpan` (core.css:2333) and `.popover-info` (components.css:750):

```scss
// Mirrors legacy: table span.embededTreeSpan (core.css:2333)
table span.embededTreeSpan,
.embededTreeSpan {
    border-radius: 5px;
    display: inline-block;
    padding: 0px 8px;
    margin-left: 8px;
    margin-right: 8px;
    text-decoration: none;
    cursor: pointer;
}

// Mirrors legacy: .popover-info (components.css:750)
.popover-info {
    background-color: #cce0f7;
    color: #0854a0 !important;
    min-width: 40px;
}

// Mirrors legacy: .table [class^="popover-"] (components.css:728)
.popover-info:hover {
    background-color: #01053e;
    color: #eff4f9 !important;
}
```

The class name `embededTreeSpan` is preserved exactly from the legacy (one `d`, not two) so that any shared CSS from Minton/legacy that targets it continues to work.

#### 1.3 Cross-type `fetchRecord` — target-manager pattern

Add a static `fetchRecord(uuid): Promise<Record<string, unknown> | undefined>` method to the `ServiceManager` interface. When a table cell references a **different** document type than the page it's on, the wrapper calls `targetManager.fetchRecord(uuid)` — not the source list controller's `fetchRecord`.

```ts
// services/ServiceManager.ts
export interface ServiceManager {
    // …existing…
    /** Fetch a single record by UUID — used for cross-type popovers. */
    fetchRecord?: (uuid: string) => Promise<Record<string, unknown> | undefined>;
}
```

In `AsyncEmbeddedListSection`, prefer `targetManager.fetchRecord` over `lt.fetchRecord`:

```ts
const targetManager = getDocumentManagerDef(documentType);
const fetchFn = targetManager?.fetchRecord ?? lt.fetchRecord;
```

The existing `PurchaseContractListController.fetchRecord` stays working for the same-page case; the new interface enables cross-page navigation.

#### 1.4 Declare `docPopConfig` on `ItemsTableColumnJson`

Currently only `ListColumnJson` has it. Add the same optional field to `ItemsTableColumnJson` in `controllers/EditPageJsonTypes.ts` so the type system knows it's legal on edit-page columns.

**Deliverables:**
- `components/page/AsyncEmbeddedListSection.tsx` — Mode A now wires docPopConfig, extracted `buildPopDocumentUnion` helper
- `components/doc/PopDocumentUnion.tsx` (new file, ~40 LOC) — the `PopDocumentUnion` visual component, mirrors `PopDocumentUnion.js`
- `services/ServiceManager.ts` — `fetchRecord?` added to interface
- `controllers/EditPageJsonTypes.ts` — `docPopConfig?` added to `ItemsTableColumnJson`
- `controllers/PageMetaTypes.ts` — update the outdated "not yet supported" comment
- `styles/overrides.scss` — `.embededTreeSpan` + `.popover-info` block

**Verification:**
- Reload the PurchaseContract editor `合同物料项目` tab. The Material Item's `id` column should now render as a capsule with the material icon. Clicking pops a card. (This will still show empty content until Phase 2's MaterialStockKeepUnitManager stub is in place — Phase 1 just proves the wiring.)
- Reload the PurchaseContract list. The `id` column already worked; verify it now shows the pill visual instead of the plain link.

### Phase 2 — First cross-type manager: `MaterialStockKeepUnitManager` [~1 hour]

**Goal:** prove the cross-type popover end-to-end with the most common target — MaterialStockKeepUnit, which is what every material items-table references.

**Steps:**
1. Create `services/systemResource/MaterialStockKeepUnitManager.ts` — mirror the shape of `PurchaseContractManager.ts` (getModelTitle, getStatusIconArray, getDocumentPopoverContent, getDocItemPopoverContent, fetchRecord, getI18nRootConfig).
2. Populate `getDocumentPopoverContent()`'s fieldMetaList from the legacy `MaterialStockKeepUnitManager.js:1298`.
3. Wire the manager into `DocumentManagerFactory.getDocumentManagerDef()` — remove the `declare const MaterialStockKeepUnitManager: any` stub and import the real one.
4. Add i18n bundle for `materialStockKeepUnit` if not already present.
5. `fetchRecord` uses the existing `/api/materialStockKeepUnit/loadModuleService?uuid=<>` endpoint.

**Verification:** click the Material Item's material capsule in the editor → popover shows real MaterialStockKeepUnit data (id, name, status, unit, etc.).

### Phase 3 — Roll-out to the other document-type managers [incremental, ~1 hour per manager]

For each manager listed below, do the same pattern as Phase 2. Do them incrementally, aligned with each doc-type's Phase A migration — a manager only needs to exist when its docPopConfig site is first encountered.

**Real-document managers (referenced by `DocumentType.*`)** — 20 entries:

| Document type | Legacy manager | Priority |
|---|---|---|
| PURCHASECONTRACT | ✅ Done | — |
| PURCHASEREQUEST | PurchaseRequestManager.js | ⭐ High (Phase 3 of logistics migration) |
| PURCHASEORDER | PurchaseOrderManager.js | ⭐ High |
| PURCHASERETURNORDER | PurchaseReturnOrderManager.js | 🟡 Medium |
| INBOUNDDELIVERY | InboundDeliveryManager.js | ⭐ High |
| OUTBOUNDDELIVERY | OutboundDeliveryManager.js | ⭐ High |
| SALESCONTRACT | SalesContractManager.js | ⭐ High |
| SALESRETURNORDER | SalesReturnOrderManager.js | 🟡 Medium |
| PRODUCTIONORDER | ProductionOrderManager.js | ⭐ High |
| PRODUCTIONPLAN | ProductionPlanManager.js | 🟡 Medium |
| BILLOFMATERIALORDER | BillOfMaterialManager.js | 🟡 Medium |
| BILLOFMATERIALTEMPLATE | BillOfMaterialTemplateManager.js | 🟡 Medium |
| PRODPICKINGORDER | ProdPickingOrderManager.js | 🟡 Medium |
| PRODRETURNORDER | ProdReturnOrderManager.js | 🟢 Low |
| INVENTORY_TRANSFER | InventoryTransferOrderManager.js | 🟡 Medium |
| INVENTORY_CHECKORDER | InventoryCheckOrderManager.js | 🟡 Medium |
| QUALITYINSPECTORDER | QualityInspectOrderManager.js | 🟡 Medium |
| WASTEPROCESSORDER | WasteProcessOrderManager.js | 🟢 Low |
| WAREHOUSESTOREITEM | WarehouseStoreManager.js | 🟡 Medium |
| INQUIRY | InquiryManager.js | 🟡 Medium |

**Dummy-document managers (referenced by `DummyDocumentType.*`)** — 19 entries observed in legacy usage:

| Dummy type | Legacy manager | Priority |
|---|---|---|
| MaterialStockKeepUnit | ✅ Phase 2 | — |
| Material | MaterialManager.js | ⭐ High |
| MaterialType | MaterialTypeManager.js | 🟡 Medium |
| RegisteredProduct | RegisteredProductManager.js | 🟡 Medium |
| Employee | EmployeeManager.js | ⭐ High |
| LogonUser | LogonUserManager.js | ⭐ High |
| Organization | OrganizationManager.js | ⭐ High |
| CorporateCustomer | CorporateCustomerManager.js | ⭐ High |
| CorporateSupplier | CorporateSupplierManager.js | ⭐ High |
| Warehouse | WarehouseManager.js | ⭐ High |
| CalendarTemplate | CalendarTemplateManager.js | 🟢 Low |
| StandardMaterialUnit | StandardMaterialUnitManager.js | 🟢 Low |
| PricingSetting | PricingSettingManager.js | 🟢 Low |
| SystemExecutorSetting | SystemExecutorSettingManager.js | 🟢 Low |
| SerialNumberSetting | SerialNumberSettingManager.js | 🟢 Low |
| SystemCodeValueCollection | SystemCodeValueManager.js | 🟢 Low |
| HostCompany | HostCompanyManager.js | 🟢 Low |
| MessageTemplate | MessageTemplateManager.js | 🟢 Low |
| FinAccountTitle / FinAccount | FinAccountManager.js | 🟡 Medium |

Each row is a small self-contained migration — pick them off as needed.

### Phase 4 — Edge cases + polish [~1 hour, once)

1. **Icon placement in header** — the legacy header row shows the document-type icon at the top of the popover body (`DocumentOrderMatPopInfo.js:97`). Currently `DocPopoverCard` doesn't render a header icon. Add `<i className={formatDocTypeIconClass(config.docType)} />` before the id line.

2. **Long-value truncation** — legacy calls `ServiceStringHelper.handleContentByLength(value, 6)` to ellipsize the pill text at 6 chars. In CSS we already handle this via `text-overflow: ellipsis; max-width: 160px` — verify at real resolutions.

3. **Empty-uuid guard** — if a row's `uuidFieldName` resolves to `null` / empty, render the plain value with no capsule (already handled — see `AsyncEmbeddedListSection.tsx:212`).

4. **Right-click / middle-click** — currently the capsule is a `<span>`, so no native "open in new tab" behavior on the id link. Legacy allowed opening the edit page in a new tab. Add an anchor-inside-span pattern that supports both click-to-popover AND cmd-click-to-open, or expose an "Open" button inside the popover body.

5. **Search-result tables** — a follow-up audit is needed to determine whether search-result tables also carry `docPopConfig` in the legacy. If yes, Mode B of `AsyncEmbeddedListSection` already covers this — verify by loading a search page.

## 5. Risk assessment

| Risk | Likelihood | Mitigation |
|---|---|---|
| The legacy `getDocumentPopoverContent()` on some manager returns doc-specific fields that don't map 1:1 to our `PopoverCardConfig` shape (e.g. custom cell rendering) | Medium | Handle per-manager in Phase 3 — if fields differ, extend `PopoverFieldMeta` with optional `render?` for that manager |
| A `documentType` value used in the wild is not covered by `getDocumentManagerDef()` — silent no-op | Medium | Log a `console.warn` in the factory when returning `undefined`, so unmigrated types show up in dev |
| Cross-type `fetchRecord` endpoints don't match the pattern | Low | Every legacy manager already exposes `getLoadDocumentBaseURL()`; new managers just port that URL |
| Adding new SCSS rules conflicts with Minton/antd tokens | Low | Scoped under `.embededTreeSpan` + `.popover-info` — names are from legacy CSS, no global bleed |
| Removing the plain-link wrapper breaks a page that depended on the underline styling | Low | The `<a>` in the current code is only inside the popover wrapper — no page-wide impact |
| Performance: fetching per popover click on large tables | Very Low | Fetch happens on click, not on render. antd Popover unmounts content on close |

## 6. Estimated effort

| Phase | Effort | Blocking? |
|---|---|---|
| Phase 1 — Infra (wrapper + capsule + `fetchRecord` cross-type + type declarations) | ~2 hours | Yes for Phase 2 |
| Phase 2 — First cross-type manager (MaterialStockKeepUnit) | ~1 hour | Proves the pattern |
| Phase 3 — Roll-out per doc-type manager | ~1 hour each (incremental) | Not blocking anything — each doc-type Phase A can add its own manager |
| Phase 4 — Polish (icon header, right-click open, etc.) | ~1 hour | Optional |

**Total for the "complete framework + first working target" (Phases 1 + 2): ~3 hours.**
**Total including all 27 managers (Phase 3): ~30 hours spread across the doc-type migrations.**

## 7. What this plan explicitly does NOT include

- **A per-doc-type schedule for Phase 3** — those live in each doc-type's Phase A migration plan
- **Bootstrap Popover CSS removal** — the legacy popover was Bootstrap; the new UI already uses antd `Popover`, so nothing to remove
- **Changes to backend endpoints** — every `getLoadDocumentBaseURL()` endpoint already exists (all Phase 1–5 backend migrations are done per MEMORY.md)
- **Changes to `DocumentConstants`** — the constants file is already complete

## 8. Recommendation

Do **Phase 1 + Phase 2** as a single next step (~3 hours). That gives the user:

1. The visual capsule on every table (list + editor)
2. A working popover on both PurchaseContract's own `id` cells AND on Material items in the editor's material tab
3. A pattern others can extend

Phase 3 managers get added opportunistically alongside each doc-type's own migration — no need to batch them.

## 9. Reference: legacy → new mapping (for Phase 1/2 implementers)

| Legacy | New |
|---|---|
| `ServiceDataTableFrame.js:294–354` — docPop rendering | `AsyncEmbeddedListSection.tsx:177–230` (Mode B) — already exists; Mode A needs the same treatment |
| `PopDocumentUnion.js` — capsule + click wire-up | `PopDocumentUnion.tsx` (new file in Phase 1) |
| `PopupCore.js` — Bootstrap popover | antd `<Popover>` |
| `DocumentOrderMatPopInfo.js:97` — content builder | `DocPopoverCard.tsx` |
| `DocumentManagerFactory.getDocumentManagerDef` | `DocumentManagerFactory.ts:421` — same name, needs real imports for non-PurchaseContract types |
| `<Manager>.getDocumentPopoverContent()` returns HTML string | `<Manager>.getDocumentPopoverContent()` returns typed `PopoverCardConfig` |
| `<Manager>.getDocItemPopoverContent()` | same — for line-item variant |
| `ServiceStringHelper.handleContentByLength` | CSS `text-overflow: ellipsis` |
| `formatDocTypeIconClass` | ✅ ported at `DocumentManagerFactory.ts:315` |
