# Dummy Document UI Migration Guide

Migrate a legacy **dummy document** — a backend `ServiceEntityNode` (NOT a `DocumentContent` subclass) that is treated as a document in the UI layer because it has status, action codes, and an item-level node.

**Reference implementation:** `Material` (+ MaterialUnit item).  
**Other examples:** `CorporateSupplier`, `MaterialStockKeepUnit`, `RegisteredProduct`.  
**Audience:** Developers and AI agents — all steps are concrete and executable.

---

## What makes a "dummy document" different from a standard document

| Aspect | Standard Document | Dummy Document |
|---|---|---|
| Backend base class | `DocumentContent` | `ServiceEntityNode` (no doc inheritance) |
| `DocumentType` value | Numeric integer (e.g. `34`) | **String** — `DummyDocumentType.Material = 'Material'` |
| Factory lookup | `Number(docType)` comparison | **Direct string equality** comparison |
| Status endpoint | `getStatusMap` | May differ — e.g. `getStatus` (check backend) |
| `getDocumentType()` | Returns the numeric type | Returns `NaN`; add `getDummyDocumentType()` for the string |
| Involve-party sections | Usually has supplier/org | Usually **none** — supplier is a flat scalar field |
| Lifecycle actions | deliver/receive | master-data: **ACTIVE / REINIT / ARCHIVE** |
| DocumentConstants | `DocumentType.<NAME>` block | `DummyDocumentType.<NAME>` + `<Name>.status` block |
| UI framework used | `DocumentEditController` | **Same** `DocumentEditController` — dummy docs ride the same framework |

The framework was deliberately built to support this (see `ServiceEditController.ts` comment: *"dummy document editors (e.g. CorporateSupplier, Material)"*).

---

## 0. Before you start

### Confirm `DocumentConstants.ts` has the entries

```ts
// These must exist before you start — no edits to DocumentConstants needed if they do
ServiceModuleConstants.<YourEntity>  // e.g. 'Material'
DummyDocumentType.<YourEntity>       // = ServiceModuleConstants.<YourEntity>  (a string)
<YourEntity>.status { INIT, APPROVED, ACTIVE, ARCHIVED, ... }
```

If missing, add them to `DocumentConstants.ts` following the existing pattern.

### Confirm backend endpoints under `/api/<rootNodeInstId>/`

Same checklist as standard documents, but **check the status endpoint name carefully**:

| Endpoint | Note |
|---|---|
| `searchTableService` | list |
| `loadModuleEditService` | editor load |
| `saveModuleService` | editor save |
| `getDocActionConfigureList` | workflow buttons |
| `executeDocAction` | workflow actions |
| `getStatus` OR `getStatusMap` | **check which one the backend exposes** |
| `newModuleService` | create mode |
| item `loadModuleEditService` | item editor |
| item `saveModuleService` | item editor |

### Confirm `DocumentManagerFactory.ts` has a stub

Look for `declare const <YourEntity>Manager: any;` — it likely exists. You will replace it with the real import in Step 7.

---

## 1. Create the TypeScript types

**File:** `src/types/platform/<YourEntity>Content.ts`  
**Folder:** `src/types/platform/` (platform entities, not logistics)  
**Mirrors:** `src/types/platform/MaterialContent.ts`

```ts
import type { DocumentUIModel } from './DocumentUIModel';
import type { DocMatItemUIModel } from './DocMatItemUIModel';
// Note: NO DocInvolvePartyUIModel import — dummy docs usually have no involve-party

// Root model
export interface <YourEntity>UIModel extends DocumentUIModel {
    // Add fields from the backend <YourEntity>UIModel.java
    // Example: status, operationMode, category, supplier scalar fields
    status?: number;
    statusValue?: string;
    mainSupplierName?: string;     // flat scalar, NOT an involve-party section
    // ...
}

// Item model (the item node under this entity)
export interface <YourEntity>UnitUIModel extends DocMatItemUIModel {
    unitName?: string;
    ratioToStandard?: number;
    // ... item-specific fields
}

// Item wrapper
export interface <YourEntity>UnitServiceUIModel {
    <yourEntity>UnitUIModel: <YourEntity>UnitUIModel;
    <yourEntity>UnitAttachmentUIModelList?: unknown[];
}

// Action node model
export interface <YourEntity>ActionNodeUIModel {
    docActionCode?: number;
    executionTime?: string;
    executedByUserName?: string;
    note?: string;
    [key: string]: unknown;
}

// Top-level ServiceUIModel wrapper
export interface <YourEntity>ServiceUIModel {
    <yourEntity>UIModel: <YourEntity>UIModel;
    // NO purchaseToOrgUIModel / purchaseFromSupplierUIModel (no involve-party)
    // Action nodes matching the backend ServiceUIModel fields:
    approvedBy?: <YourEntity>ActionNodeUIModel;
    activeBy?: <YourEntity>ActionNodeUIModel;
    submittedBy?: <YourEntity>ActionNodeUIModel;
    archivedBy?: <YourEntity>ActionNodeUIModel;
    // ...
    <yourEntity>UnitUIModelList: <YourEntity>UnitServiceUIModel[];
    <yourEntity>AttachmentUIModelList: unknown[];
    serviceUIMeta: Record<string, unknown>;
}
```

---

## 2. Create i18n locale files

**Files:**
- `src/i18n/locales/en/coreFunction/<YourEntity>.json`  ← note `coreFunction/`, not `supplyChain/`
- `src/i18n/locales/zh/coreFunction/<YourEntity>.json`

**Decode from legacy:** `admin/i18n/coreFunction/<YourEntity>_en.properties`

Same structure as the standard document guide, with your entity's field names. The `coreFunction` subfolder is new — create it if it doesn't exist.

---

## 3. Create the Manager

**File:** `src/services/platform/<YourEntity>Manager.ts`  
**Folder:** `src/services/platform/` (platform entities, not logistics)  
**Mirrors:** `src/services/platform/MaterialManager.ts`

```ts
export class <YourEntity>Manager extends ServiceManager {

    // ── CRITICAL DIFFERENCE 1: two type methods ──────────────────────────────
    // getDocumentType() returns NaN to satisfy the ServiceManager interface
    // (which requires a number). Callers needing the actual type use getDummyDocumentType().
    static getDocumentType(): number {
        return NaN;
    }

    static getDummyDocumentType(): string {
        return DocumentConstants.DummyDocumentType.<YourEntity>;
    }

    // ── CRITICAL DIFFERENCE 2: action codes for master-data lifecycle ─────────
    // Dummy docs typically use ACTIVE/REINIT/ARCHIVE instead of deliver/process-done
    static readonly DOC_ACTION_CODE = {
        APPROVE:        DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.ACTION_APPROVE,
        REJECT_APPROVE: DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.ACTION_REJECT_APPROVE,
        SUBMIT:         DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.ACTION_SUBMIT,
        REVOKE_SUBMIT:  DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.ACTION_REVOKE_SUBMIT,
        COUNTAPPROVE:   DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.ACTION_COUNTAPPROVE,
        REINIT:         DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.ACTION_REINIT,
        ACTIVE:         DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.ACTION_ACTIVE,
        ARCHIVE:        DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.ACTION_ARCHIVE,
    } as const;

    // ── CRITICAL DIFFERENCE 3: status URL may be 'getStatus', not 'getStatusMap' ──
    static getStatusURL(): string {
        return '<yourEntity>/getStatus';  // ← verify against backend controller
    }

    // Node inst ids
    static getRootNodeInstId(): string { return '<yourEntity>'; }
    static getItemNodeInstId(): string { return '<yourEntity>Unit'; }

    // Identity
    static getResourceId(): string { return ServiceModuleConstants.<YourEntity>; }

    // i18n — note: NO 'docInvolveParty' if entity has no involve-party sections
    static getI18nRootConfig() {
        return {
            primaryNs: '<yourEntity>',
            keyPrefix: '<yourEntity>',
            fallbackNs: 'commonElements',
            subNsAliases: {
                // omit docInvolveParty if no involve-party sections
                docActionNode: 'docActionNode',
                docFlowNode:   'docFlowNode',
                docMatItem:    'docMatItem',
            },
        };
    }

    static getI18nItemConfig() {
        return {
            primaryNs: '<yourEntity>',
            keyPrefix: '<yourEntity>Unit',
            fallbackNs: 'commonElements',
            subNsAliases: { /* same as root */ },
        };
    }

    // Status icons — use DocumentConstants.<YourEntity>.status (not DocumentType)
    static getStatusIconArray() {
        return [
            { id: DocumentConstants.<YourEntity>.status.INIT,     iconClass: DocumentContentProp.statusIcon.INITIAL },
            { id: DocumentConstants.<YourEntity>.status.SUBMITTED, iconClass: DocumentContentProp.statusIcon.SUBMITTED },
            { id: DocumentConstants.<YourEntity>.status.APPROVED,  iconClass: DocumentContentProp.statusIcon.APPROVED },
            { id: DocumentConstants.<YourEntity>.status.ACTIVE,    iconClass: DocumentContentProp.statusIcon.ACTIVE },
            { id: DocumentConstants.<YourEntity>.status.ARCHIVED,  iconClass: DocumentContentProp.statusIcon.ARCHIVED },
        ];
    }

    // i18n registration — in coreFunction/, no docInvolveParty
    // (add at top of file, runs on first import)
}

// i18n side-effect registration (top of file):
import en<YourEntity> from '@/i18n/locales/en/coreFunction/<YourEntity>.json';
import zh<YourEntity> from '@/i18n/locales/zh/coreFunction/<YourEntity>.json';
i18n.addResourceBundle('en', '<yourEntity>', en<YourEntity>, true, false);
i18n.addResourceBundle('zh', '<yourEntity>', zh<YourEntity>, true, false);
// + foundation namespaces (commonElements, docActionNode, docFlowNode, docMatItem)
// NOTE: NO docInvolveParty if entity has no involve-party sections
```

---

## 4–6. Create pages (same structure as standard document)

**Pages directory:** `src/pages/platform/<yourEntity>/`

The List, Editor, Item editor, and Panel files follow the **exact same structure** as the standard document guide (Steps 4–6). The only differences are:

> **Step 4a overrides (list controller):**
>
> 1. **No local `*ListItem` interface** — use `<YourEntity>ServiceUIModel` from `@/types/platform/<YourEntity>Content` as the generic parameter (same as the editor uses). The list and editor receive the same wrapper shape from the backend.
>    ```ts
>    import type { <YourEntity>ServiceUIModel } from '@/types/platform/<YourEntity>Content';
>    export class <YourEntity>ListController extends ServiceListController<<YourEntity>ServiceUIModel, SearchParams> {
>    ```
>
> 2. **`request()` must have try/catch** — wrap `listDocuments` and call `pushErrorMessageBar` on error (see the pre-flight section below for the correct pattern).

### Differences in `<YourEntity>EditController.tsx`

**No CUSTOMERCONTACT sections** — if the entity has no involve-party, omit the supplier/org sections entirely. Supplier is a flat field:

```ts
// STANDARD DOC: has two CUSTOMERCONTACT sections
{ sectionCategory: SectionCategory.CUSTOMERCONTACT, parentContentPath: 'purchaseFromSupplierUIModel', ... }

// DUMMY DOC: supplier is just a readonly text field inside the EDIT section
{ fieldName: 'mainSupplierName', colWidth: 'lg', readonly: true }
```

**Different action codes in `getActionCodeMatrix()`:**
```ts
getActionCodeMatrix() {
    return {
        submit:        { actionCode: <YourEntity>Manager.DOC_ACTION_CODE.SUBMIT },
        revokeSubmit:  { actionCode: <YourEntity>Manager.DOC_ACTION_CODE.REVOKE_SUBMIT },
        approve:       { actionCode: <YourEntity>Manager.DOC_ACTION_CODE.APPROVE },
        rejectApprove: { actionCode: <YourEntity>Manager.DOC_ACTION_CODE.REJECT_APPROVE },
        reInit:        { actionCode: <YourEntity>Manager.DOC_ACTION_CODE.REINIT },
        active:        { actionCode: <YourEntity>Manager.DOC_ACTION_CODE.ACTIVE },
        archive:       { actionCode: <YourEntity>Manager.DOC_ACTION_CODE.ARCHIVE },
    };
}
```

**Status field uses `getStatus` not `getStatusMap`:**
```ts
{ fieldName: 'status', fieldType: 'select', readonly: true,
  settings: { getMetaDataUrl: this.getStatusURL } }  // getStatusURL = '<yourEntity>/getStatus'
```

**getDocumentType() note:** The controller's `documentType` string in `getDefaultPageMeta()` should be the entity name (e.g. `'Material'`), not a numeric type. The framework uses this for logging/identification only.

### Route paths use `platform/` prefix

```tsx
// List
{ path: 'platform/<yourEntity>', element: <<YourEntity>ListPage /> }
// Create
{ path: 'platform/<yourEntity>/new', element: <<YourEntity>EditPage processMode={PROCESSMODE_NEW} /> }
// Edit
{ path: 'platform/<yourEntity>/:uuid/edit', element: <<YourEntity>EditPage processMode={PROCESSMODE_EDIT} /> }
// Item edit
{ path: 'platform/<yourEntity>Unit/:uuid/edit', element: <<YourEntity>UnitEditPage /> }
```

---

## 7. Register in shared files (4 edits)

### 7a. `src/router/index.tsx`
Same as standard document — add 3 imports + 4 routes (with `platform/` prefix in paths).

### 7b. `src/router/menuConfig.ts`
Add under a "Master Data" or system-resource group (not under procurement):
```ts
{ key: '<yourEntity>-list', label: t('<yourEntity>List'), path: '/platform/<yourEntity>' }
```

### 7c. `src/i18n/locales/en/Menu.json` + `zh/Menu.json`
```json
"<yourEntity>List": "..."
```

### 7d. `src/services/DocumentManagerFactory.ts` — CRITICAL DIFFERENCE from standard docs

**Step 1:** Replace the `declare const` stub with the real import:
```ts
// Remove this line:
declare const <YourEntity>Manager: any;

// Add this import:
import { <YourEntity>Manager } from '@/services/platform/<YourEntity>Manager';
```

**Step 2:** Add to the class-return function (`getDocumentManagerDef`).  
The comparison uses **string equality** (`===`), NOT `Number(docType)`:
```ts
// ⚠️ DUMMY DOC uses DummyDocumentType (string), not DocumentType (number)
// ⚠️ Use direct === comparison, NOT n === Number(docType)
if (docType === DocumentConstants.DummyDocumentType.<YourEntity>)
    return <YourEntity>Manager as unknown as DocumentManagerInstance;
```

**Step 3:** Add to the instance-cache function (`getDocumentManager`):
```ts
if (docType === DocumentConstants.DummyDocumentType.<YourEntity>)
    return pushCache(docType, new <YourEntity>Manager() as unknown as DocumentManagerInstance);
```

> **Why string equality?** `DummyDocumentType.Material = ServiceModuleConstants.Material = 'Material'` (a string). The standard doc lookup does `const n = Number(docType)` which gives `NaN` for a string. Dummy types must use `docType ===` directly.

---

## 8. Verify

```bash
cd IntelligentUI
npx tsc --noEmit 2>&1 | grep -i "<yourEntity>"  # should be empty
```

Then run the app and check the browser network tab — confirm `searchTableService` returns data, not a backend exception. See the pre-flight searchContent check below.

---

## Pre-flight: verify searchContent against backend SearchModel

**Always read `<YourEntity>SearchModel.java` before writing `searchContent`.**

The searchContent shape must match the backend SearchModel **exactly**. Sending unknown fields causes `UnrecognizedPropertyException`.

| Entity type | searchContent shape | `extendDocSearchTabFieldMeta`? |
|---|---|---|
| Standard document (DocumentContent) | Sub-objects: `headerModel`, `createdUpdateModel`, action-node models | ✅ Yes — generates correct `headerModel.*` field paths |
| Dummy document with involve-party | Sub-objects for party models; flat for entity-specific fields | ✅ Partial — use for the base tabs, add entity fields in `headerPostMetaList` |
| Plain ServiceEntity (no workflow, no party) | **Flat fields only** | ❌ No — use a plain flat `embeddedTabMetaList` with flat `fieldName` values |

Also wrap `request()` in try/catch so backend errors show in the message bar rather than silently failing:
```ts
request = async (params) => {
    try {
        const result = await Manager.listDocuments({ content, ... });
        return { data: result.data, success: true, total: result.total };
    } catch (err) {
        pushErrorMessageBar(err instanceof Error ? err.message : String(err), { context: 'list-error' });
        return { data: [], success: false, total: 0 };
    }
};
```

---

## Decision checklist before starting

Answer these questions from the legacy source — they determine every structural difference:

| Question | Answer source | Impact |
|---|---|---|
| Does the entity have involve-party (supplier/org) sections? | `*Editor.js` — look for `CUSTOMERCONTACT` or `involveParty` sections | No → omit CUSTOMERCONTACT sections, supplier is a flat field |
| What is the status endpoint name? | Backend `*EditorController.java` `@RequestMapping` | `getStatus` or `getStatusMap` — set `getStatusURL()` accordingly |
| Which action codes does the entity support? | Legacy `*Manager.js` `DOC_ACTION_CODE` block | Use ACTIVE/REINIT/ARCHIVE for master data; DELIVERY_DONE/PROCESS_DONE for operational |
| Does the entity's status block exist in `DocumentConstants.ts`? | `DocumentConstants.ts` around line 480 | If absent, add it; if present, use `DocumentConstants.<Entity>.status.*` |
| Is there a `declare const <Entity>Manager: any` stub in DocumentManagerFactory? | `DocumentManagerFactory.ts` lines 30–70 | Replace with the real import in Step 7d |

---

## Summary: differences from the standard document migration

| Step | Standard Document | Dummy Document |
|---|---|---|
| Types folder | `src/types/logistics/` | `src/types/platform/` |
| Manager folder | `src/services/logistics/` | `src/services/platform/` |
| Pages folder | `src/pages/logistics/` | `src/pages/platform/` |
| i18n folder | `src/i18n/locales/*/supplyChain/` | `src/i18n/locales/*/coreFunction/` |
| `getDocumentType()` | Returns numeric type | Returns `NaN`; add `getDummyDocumentType()` |
| Factory lookup | `n === DocumentConstants.DocumentType.X` | `docType === DocumentConstants.DummyDocumentType.X` |
| Status endpoint | `getStatusMap` | `getStatus` (verify per entity) |
| Involve-party | Usually YES | Usually NO |
| Action codes | submit/approve/deliver | ACTIVE/REINIT/ARCHIVE (master-data) |
| i18n: docInvolveParty | YES | NO (if no involve-party sections) |
| Route prefix | `logistics/` | `platform/` |
| Menu group | procurement | master data / system resource |

**File count is identical:** 16 new files + 4 shared edits + 0 backend changes.
