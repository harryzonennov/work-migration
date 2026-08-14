# Pure ServiceEntity UI Migration Guide

Migrate a legacy entity that is a plain **`ServiceEntityNode`** with no status lifecycle, no
workflow action codes, and no item-level child node.

**Reference implementation:** `StandardMaterialUnit`  
**Other examples:** Any lookup / master-data entity whose backend class extends `ServiceEntityNode`
directly (not `DocumentContent`, not a dummy-doc with status).  
**Audience:** Developers and AI agents — all steps are concrete and executable.

---

## What makes a "pure ServiceEntity" different

| Aspect | Standard Document | Dummy Document | Pure ServiceEntity |
|---|---|---|---|
| Backend base class | `DocumentContent` | `ServiceEntityNode` | `ServiceEntityNode` |
| Has status / workflow | Yes | Yes | **No** |
| Has item-level child node | Yes | Yes | **No** |
| Has involve-party sections | Usually yes | Usually no | **No** |
| `DocumentType` value | Numeric integer | String (`DummyDocumentType`) | String (`DummyDocumentType`) |
| Factory lookup | `Number(docType)` | `docType ===` string | `docType ===` string |
| Edit controller base class | `DocumentEditController` | `DocumentEditController` | **`ServiceEditController`** |
| Edit hook | `useDocumentEditController` | `useDocumentEditController` | **`useServiceEntityEditController`** |
| Edit page component | `DocumentEditPage` | `DocumentEditPage` | **`AsyncEditorPage` directly** |
| Process buttons | exit + DOC_ACTION_BTN + save | exit + DOC_ACTION_BTN + save | **exit + save only** |
| `getActionCodeMatrix()` | workflow codes | lifecycle codes | **returns `{}`** |
| Item data source / Panel | Yes | Yes | **No** |
| Doc-action configure list | Yes | Yes | **No** |
| SearchContent shape | Sub-objects (`headerModel`, party models…) | Mixed | **Flat fields only** |

The key structural consequence: the pure ServiceEntity is the **thinnest** possible migration.
There is no item tab, no right-bar action log, no workflow, no attachment section (unless the
entity explicitly has one). The ServiceUIModel wrapper has a single root UIModel field.

---

## 0. Before you start

### Confirm `DocumentConstants.ts` has the entries

```ts
ServiceModuleConstants.<YourEntity>   // e.g. 'StandardMaterialUnit'
DummyDocumentType.<YourEntity>        // = ServiceModuleConstants.<YourEntity>  (a string)
// Optional: enum constants for any dropdown fields
<YourEntity>.<fieldCategory> { VALUE1: 1, VALUE2: 2, ... }
```

If missing, add them following the existing pattern in `DocumentConstants.ts`.

### Confirm backend endpoints under `/api/<rootNodeInstId>/`

| Endpoint | Required |
|---|---|
| `searchTableService` | ✅ list page |
| `newModuleService` | ✅ create mode |
| `loadModuleEditService` | ✅ editor load |
| `saveModuleService` | ✅ editor save |
| `get<FieldName>Map` (one per enum dropdown) | ✅ for each select field |
| `loadModuleListService` | ✅ if any field uses a self-referencing FK lookup |

No `getDocActionConfigureList`, no `executeDocAction`, no `getStatusMap` — this entity has none.

### Confirm `DocumentManagerFactory.ts` has a stub

Look for `declare const <YourEntity>Manager: any;` — it likely exists.  
You will replace it with the real import in Step 6.

### Read the legacy source files

Before writing any code, open the legacy JS files and collect:

- `getRootNodeInstId()` → the URL prefix string (e.g. `'standardMaterialUnit'`)
- The `data.searchContent` object → the flat field names the backend SearchModel accepts
- The `getDefaultPageMeta()` field lists from the legacy `*Editor.js` file
- What enum dropdown endpoints exist (look for `getMetaDataUrl` in the legacy pageMeta)

---

## 1. Create the TypeScript types

**File:** `src/types/platform/<YourEntity>Content.ts`  
**Mirrors:** `src/types/platform/StandardMaterialUnitContent.ts`

```ts
/**
 * Root UIModel — mirrors the backend <YourEntity>UIModel.java fields.
 * No status, no docFlow, no involve-party.
 */
export interface <YourEntity>UIModel {
    uuid?: string;
    id?: string;
    name?: string;
    note?: string;
    // Add entity-specific fields from the backend UIModel.java.
    // For enum fields, include both the numeric code and the resolved text value:
    someCategory?: number;
    someCategoryValue?: string;
    // For FK lookup fields, include the stored UUID and the display-only fields:
    refOtherEntityUUID?: string;
    refOtherEntityId?: string;
    refOtherEntityName?: string;
}

/**
 * Top-level ServiceUIModel wrapper returned by loadModuleEditService.
 * Pure ServiceEntity: only one root model field, no item lists, no action nodes.
 */
export interface <YourEntity>ServiceUIModel {
    <yourEntity>UIModel: <YourEntity>UIModel;
    serviceUIMeta: Record<string, unknown>;
}
```

**Key rules:**
- Root model key = `<yourEntity>UIModel` (camelCase class name, lowercase first letter)
- No item list fields, no action node fields, no involve-party fields
- All field names must **exactly** match the Java UIModel field names

---

## 2. Create i18n locale files

**Files:**
- `src/i18n/locales/en/coreFunction/<YourEntity>.json`
- `src/i18n/locales/zh/coreFunction/<YourEntity>.json`

**Decode from legacy:** `admin/i18n/coreFunction/<YourEntity>_en.properties`

```python
# Decode script (run in admin/i18n/coreFunction/)
with open('<YourEntity>_en.properties', 'r', encoding='utf-8') as f:
    content = f.read()
result = {}
for line in content.splitlines():
    if not line.strip() or line.startswith('#') or '=' not in line:
        continue
    k, v = line.split('=', 1)
    result[k.strip()] = v.encode('utf-8').decode('unicode_escape').strip()
```

Note: `_en.properties` files contain Chinese unicode escapes, despite the `_en` suffix.

**Minimal JSON structure:**
```json
{
  "pageTitle": "...",
  "messages": {
    "unitCreated": "...",
    "unitUpdated": "..."
  },
  "<yourEntity>": {
    "id": "...",
    "name": "...",
    "<yourEntity>Section": "...",
    "someCategory": "...",
    "refOtherEntityId": "..."
  },
  "form": {
    "validation": {
      "idRequired": "...",
      "nameRequired": "..."
    }
  }
}
```

No `status` block, no `<yourEntity>MaterialItem` block needed.

---

## 3. Create the Manager

**File:** `src/services/platform/<YourEntity>Manager.ts`  
**Mirrors:** `src/services/platform/StandardMaterialUnitManager.ts`

```ts
import i18n from '@/i18n';
import { ServiceModuleConstants, DocumentConstants } from '@/services/DocumentConstants';
import { ServiceManager, type PopoverCardConfig } from '@/services/ServiceManager';

// Foundation: only commonElements is needed — no docActionNode, no docInvolveParty,
// no docFlowNode, no docMatItem for a pure ServiceEntity with no workflow or items.
import enCommonElements from '@/i18n/locales/en/foundation/CommonElements.json';
import zhCommonElements from '@/i18n/locales/zh/foundation/CommonElements.json';
import en<YourEntity> from '@/i18n/locales/en/coreFunction/<YourEntity>.json';
import zh<YourEntity> from '@/i18n/locales/zh/coreFunction/<YourEntity>.json';

// Register on first import (side-effect)
i18n.addResourceBundle('en', 'commonElements', enCommonElements, true, false);
i18n.addResourceBundle('en', '<yourEntity>', en<YourEntity>, true, false);
i18n.addResourceBundle('zh', 'commonElements', zhCommonElements, true, false);
i18n.addResourceBundle('zh', '<yourEntity>', zh<YourEntity>, true, false);

export class <YourEntity>Manager extends ServiceManager {

    static getResourceId(): string {
        return ServiceModuleConstants.<YourEntity>;
    }

    /**
     * Pure ServiceEntityNode — no numeric DocumentType.
     * Returns NaN to satisfy the ServiceManager interface.
     * Callers needing the string identity use DummyDocumentType.<YourEntity>.
     */
    static getDocumentType(): number {
        return NaN;
    }

    /** Must match the backend @RequestMapping base path exactly. */
    static getRootNodeInstId(): string {
        return '<yourEntity>';
    }

    /**
     * No item-level node. Returning empty string so accidental calls are safe.
     */
    static getItemNodeInstId(): string {
        return '';
    }

    /** i18n config for label resolution in controllers. */
    static getI18nRootConfig() {
        return {
            primaryNs: '<yourEntity>',
            keyPrefix: '<yourEntity>',
            fallbackNs: 'commonElements',
            subNsAliases: {},  // no docInvolveParty, docActionNode, docFlowNode, docMatItem
        };
    }

    // ── Enum dropdown URLs ────────────────────────────────────────────────────
    // One static method per select field in the editor. The backend serves these
    // as simple id/text option lists from the entity's own controller.

    static getSomeCategoryMapURL(): string {
        return '<yourEntity>/getSomeCategoryMap';
    }

    // If a field uses a self-referencing FK lookup (entity references another of its own kind):
    static getRefEntityListURL(): string {
        return '<yourEntity>/loadModuleListService';
    }

    // ── Popover content ───────────────────────────────────────────────────────

    static getDocumentPopoverContent(): PopoverCardConfig {
        return {
            docType: DocumentConstants.DummyDocumentType.<YourEntity>,
            targetPage: '/platform/<yourEntity>',
            fieldMetaList: [
                { fieldName: '<yourEntity>UIModel.id',   labelKey: '<yourEntity>.id' },
                { fieldName: '<yourEntity>UIModel.name', labelKey: '<yourEntity>.name' },
                // Add 2-3 identifying fields
            ],
        };
    }
}
```

**What to omit vs. dummy-document Manager:**
- No `DOC_ACTION_CODE` block
- No `getStatusIconArray()` / `formatStatusIconClass()`
- No `getStatusLabelMap()`
- No `getStatusURL()`
- No `getDummyDocumentType()` (the string type is already in `DummyDocumentType`)
- No `getDocActionConfigureList()` / `executeDocAction()`
- `subNsAliases` is an empty object `{}`

---

## 4. Create pages directory and List files (3 files)

Create directory: `src/pages/platform/<yourEntity>/`

### 4a. `<YourEntity>ListController.tsx`

```ts
import React from 'react';
import type { ServiceListControllerDeps } from '@/controllers/ServiceListController';
import type { ListColumnJson, ListPageConfig } from '@/controllers/ListPageTypes';
import { ServiceListController } from '@/controllers/ServiceListController';
import { pushErrorMessageBar } from '@/controllers/messageBarStore';
import type { I18nConfig } from '@/controllers/ServiceBaseController';
import i18n from '@/i18n';
import { <YourEntity>Manager } from '@/services/platform/<YourEntity>Manager';
import type { <YourEntity>UIModel } from '@/types/platform/<YourEntity>Content';
import { SectionCategory } from '@/components/page/AsyncSection';
import { DocumentConstants } from '@/services/DocumentConstants';

export interface <YourEntity>SearchParams {
    [key: string]: unknown;
    current?: number;
    pageSize?: number;
}

export class <YourEntity>ListController extends ServiceListController<<YourEntity>UIModel, <YourEntity>SearchParams> {

    /**
     * FLAT fields only — must match the backend <YourEntity>SearchModel.java field names exactly.
     * Do NOT use headerModel/createdUpdateModel sub-objects — this entity's SearchModel
     * does not extend ServiceDocumentSearchModel. Sending unknown fields causes
     * UnrecognizedPropertyException on the backend.
     *
     * Derive this list by reading <YourEntity>SearchModel.java directly.
     */
    readonly searchContent: Record<string, unknown> = {
        id: '',
        name: '',
        // one entry per searchable field in the backend SearchModel
    };

    constructor(deps: ServiceListControllerDeps<<YourEntity>UIModel>) {
        super(deps);
    }

    protected getRowId(row: <YourEntity>UIModel): React.Key {
        return (row.uuid as string) ?? (row.id as string);
    }

    getBasePath(): string { return '/platform/<yourEntity>'; }

    protected getI18nConfig(): I18nConfig {
        return <YourEntity>Manager.getI18nRootConfig();
    }

    protected getServiceManager() {
        return <YourEntity>Manager;
    }

    protected getSearchContent(): Record<string, unknown> {
        return this.searchContent;
    }

    getTableConfig(): ListPageConfig<<YourEntity>UIModel> {
        return {
            headerTitle: i18n.t('<yourEntity>:pageTitle'),
            rowKey: (row) => (row.uuid as string) ?? (row.id as string),
            pagination: { pageSize: 10, showSizeChanger: true },
            search: { labelWidth: 'auto', filterType: 'query' },
            rowSelection: true,
        };
    }

    protected getDefaultPageMeta() {
        return {
            processButtonMeta: {
                search: { callback: this.searchModule },
                newModule: { callback: this.newModule },
            },
            sectionMetaList: [
                {
                    sectionId: 'basic',
                    sectionCategory: SectionCategory.SEARCH,
                    parentContentPath: '',
                    // Flat search tab — do NOT use extendDocSearchTabFieldMeta here.
                    // That helper injects headerModel.id/name which this entity's backend does not accept.
                    embeddedTabMetaList: [
                        {
                            tabId: 'basicSection',
                            titleLabelKey: 'commonElements:basicSection',
                            fieldMetaList: [
                                { fieldName: 'id',   labelKey: 'id' },
                                { fieldName: 'name', labelKey: 'name' },
                                // one entry per searchContent key; add settings for dropdown fields:
                                {
                                    fieldName: 'someCategory',
                                    labelKey: 'someCategory',
                                    settings: { getMetaDataUrl: <YourEntity>Manager.getSomeCategoryMapURL() },
                                },
                            ],
                        },
                    ],
                },
                {
                    sectionId: '<yourEntity>List',
                    sectionCategory: SectionCategory.EMBEDLIST,
                    fieldMetaList: [
                        // Backend returns <YourEntity>UIModel fields DIRECTLY (no wrapper prefix).
                        // Runtime shape: { uuid, id, name, someCategoryValue, ... }
                        { fieldName: 'uuid', labelKey: 'id', hidden: true },
                        {
                            fieldName: 'name',
                            labelKey: 'name',
                            width: 200,
                            docPopConfig: {
                                documentType: DocumentConstants.DummyDocumentType.<YourEntity>,
                                uuidFieldName: 'uuid',
                            },
                        },
                        { fieldName: 'id',   labelKey: 'id',   width: 160, copyable: true },
                        { fieldName: 'someCategoryValue', labelKey: 'someCategory', width: 120 },
                        // add more columns as needed
                    ] as ListColumnJson[],
                },
            ],
        };
    }

    request = async (params: <YourEntity>SearchParams & { current?: number; pageSize?: number }) => {
        const content = Object.keys(this.panelParams).length ? this.panelParams : this.searchContent;
        try {
            const result = await <YourEntity>Manager.listDocuments<<YourEntity>UIModel>({
                content,
                current: params.current,
                pageSize: params.pageSize,
            });
            return { data: result.data, success: true, total: result.total };
        } catch (err) {
            pushErrorMessageBar(err instanceof Error ? err.message : String(err), { context: '<yourEntity>-list-error' });
            return { data: [], success: false, total: 0 };
        }
    };
}
```

### 4b. `use<YourEntity>ListController.ts`

```ts
import '@/services/platform/<YourEntity>Manager'; // registers i18n namespaces
import type { <YourEntity>UIModel } from '@/types/platform/<YourEntity>Content';
import { useDocumentListController } from '@/composables/useDocumentListController';
import { <YourEntity>ListController } from './<YourEntity>ListController';

export function use<YourEntity>ListController() {
    return useDocumentListController<<YourEntity>UIModel, <YourEntity>ListController>({
        buildController: (deps) => new <YourEntity>ListController(deps),
    });
}
```

### 4c. `<YourEntity>ListPage.tsx`

```tsx
import React from 'react';
import { AsyncListPage } from '@/components/page/AsyncListPage';
import { use<YourEntity>ListController } from './use<YourEntity>ListController';

const <YourEntity>ListPage: React.FC = () => {
    const { controller } = use<YourEntity>ListController();
    return <AsyncListPage pageMeta={controller.buildAsyncListPageMeta()} />;
};

export default <YourEntity>ListPage;
```

---

## 5. Create the Editor files (3 files)

### 5a. `<YourEntity>EditController.tsx`

```ts
import type { <YourEntity>ServiceUIModel, <YourEntity>UIModel } from '@/types/platform/<YourEntity>Content';
import { ServiceEditController, type ServiceEditControllerDeps, type I18nConfig } from '@/controllers/ServiceEditController';
import { PROCESSMODE_EDIT } from '@/services/Commons';
import type { PageMeta } from '@/controllers/PageMetaTypes';
import i18n from '@/i18n';
import { <YourEntity>Manager } from '@/services/platform/<YourEntity>Manager';
import { SectionCategory } from '@/components/page/AsyncSection';

export type <YourEntity>FormValues = <YourEntity>UIModel & Record<string, unknown>;

export interface <YourEntity>EditControllerDeps extends ServiceEditControllerDeps {
    serviceUIModel?: <YourEntity>ServiceUIModel;
}

/**
 * Extends ServiceEditController DIRECTLY (NOT DocumentEditController) because this is
 * a plain CRUD ServiceEntityNode:
 * - No workflow action codes
 * - No item-level node / no editable items table
 * - No involve-party sections
 * - No doc-action configure list fetch
 * Process buttons: exit + save only.
 */
export class <YourEntity>EditController extends ServiceEditController<<YourEntity>ServiceUIModel, <YourEntity>FormValues> {
    private readonly extraDeps: <YourEntity>EditControllerDeps;

    // Enum dropdown URLs — served from the backend controller
    readonly getSomeCategoryMapURL = <YourEntity>Manager.getSomeCategoryMapURL();

    constructor(deps: <YourEntity>EditControllerDeps) {
        super(deps);
        this.extraDeps = deps;
    }

    protected loadModule(): <YourEntity>ServiceUIModel | undefined {
        if (this.processMode === PROCESSMODE_EDIT) return this.extraDeps.serviceUIModel;
        return undefined;
    }

    /**
     * Default values for create mode.
     * Pre-select sensible defaults from DocumentConstants.<YourEntity>.<fieldCategory> if applicable.
     */
    protected buildCreateDefaults(): Partial<<YourEntity>FormValues> {
        return {
            <yourEntity>UIModel: {} as <YourEntity>FormValues['<yourEntity>UIModel'],
        };
    }

    protected buildPayload(values: <YourEntity>FormValues): unknown {
        const merged = super.buildPayload(values) as Record<string, unknown>;
        return { ...merged, serviceUIMeta: {} };
    }

    protected getBasePath(): string { return '/platform/<yourEntity>'; }
    protected getI18nConfig(): I18nConfig { return <YourEntity>Manager.getI18nRootConfig(); }
    protected getServiceManager() { return <YourEntity>Manager; }

    protected getCreateSuccessMessage(): string {
        return i18n.t('<yourEntity>:messages.created');
    }

    protected getUpdateSuccessMessage(): string {
        return i18n.t('<yourEntity>:messages.updated');
    }

    getBaseUUID(): string | undefined {
        return this.loadModule()?.<yourEntity>UIModel.uuid;
    }

    /**
     * No workflow — return empty so the base class skips workflow button rendering.
     */
    getActionCodeMatrix() { return {}; }

    protected getDefaultPageMeta(): PageMeta {
        return {
            documentType: '<YourEntity>',
            processButtonMeta: {
                // No DOC_ACTION_BTN placeholder — pure CRUD, no workflow
                exit: { callback: 'exitModule' },
                save: { formatClass: 'displayForEdit', callback: 'saveModule' },
            },
            tabMetaList: [
                {
                    tabId: 'basic',
                    tabTitle: '<yourEntity>:<yourEntity>.<yourEntity>Section',
                    tabIcon: 'mdi mdi-<icon> content-portlet-title',
                    sectionMetaList: [
                        {
                            sectionTitle: '<yourEntity>:<yourEntity>.<yourEntity>Section',
                            sectionIconClass: 'mdi mdi-<icon> content-portlet-title',
                            parentContentPath: '<yourEntity>UIModel',
                            sectionCategory: SectionCategory.EDIT,
                            fieldMetaList: [
                                // Dropdown fields first (classification selects), then identity fields
                                {
                                    fieldName: 'someCategory',
                                    fieldType: 'select',
                                    helpKey: '<yourEntity>.someCategory',
                                    settings: { getMetaDataUrl: this.getSomeCategoryMapURL },
                                    colWidth: 'sm',
                                },
                                {
                                    fieldName: 'id',
                                    required: true,
                                    requiredMessage: '<yourEntity>:form.validation.idRequired',
                                    colWidth: 'md',
                                },
                                {
                                    fieldName: 'name',
                                    required: true,
                                    requiredMessage: '<yourEntity>:form.validation.nameRequired',
                                    colWidth: 'md',
                                },
                                { fieldName: 'note', fieldType: 'textarea', rows: 5 },
                            ],
                        },
                        // If there is a FK lookup section (entity references itself or another entity):
                        {
                            sectionTitle: '<yourEntity>:<yourEntity>.refEntitySection',
                            sectionIconClass: 'mdi mdi-link content-portlet-title',
                            parentContentPath: '<yourEntity>UIModel',
                            sectionCategory: SectionCategory.EDIT,
                            fieldMetaList: [
                                {
                                    // The select stores the UUID of the referenced entity.
                                    // idField: 'uuid' — value stored; textField: 'id' — label shown.
                                    fieldName: 'refOtherEntityId',
                                    fieldType: 'select',
                                    settings: {
                                        getMetaDataUrl: <YourEntity>Manager.getRefEntityListURL(),
                                        idField: 'uuid',
                                        textField: 'id',
                                    },
                                    colWidth: 'md',
                                },
                                {
                                    fieldName: 'refOtherEntityName',
                                    readonly: true,
                                    colWidth: 'md',
                                },
                                {
                                    fieldName: 'conversionFactor',
                                    fieldType: 'number',
                                    precision: 6,
                                    min: 0,
                                    colWidth: 'sm',
                                },
                            ],
                        },
                    ],
                },
            ],
        };
    }
}
```

### 5b. `use<YourEntity>EditController.ts`

```ts
import type { <YourEntity>ServiceUIModel } from '@/types/platform/<YourEntity>Content';
import { useServiceEntityEditController } from '@/composables/useServiceEntityEditController';
import { <YourEntity>EditController } from './<YourEntity>EditController';
import { <YourEntity>Manager } from '@/services/platform/<YourEntity>Manager';

/**
 * Uses useServiceEntityEditController DIRECTLY — not useDocumentEditController —
 * because this is a pure CRUD entity:
 * - No loadActionConfigureList (no workflow buttons)
 * - No extractItems (no item-level data source)
 * - No DocumentEditPage (no item panel slot needed)
 */
export function use<YourEntity>EditController() {
    return useServiceEntityEditController<
        <YourEntity>ServiceUIModel,
        <YourEntity>EditController
    >({
        loadModule: (uuid) =>
            <YourEntity>Manager.loadDocument<<YourEntity>ServiceUIModel>(uuid),
        buildController: ({ processMode, uuid, navigate, serviceUIModel }) =>
            new <YourEntity>EditController({ processMode, uuid, navigate, serviceUIModel }),
    });
}
```

### 5c. `<YourEntity>EditPage.tsx`

```tsx
import React, { useRef, useEffect } from 'react';
import type { ProFormInstance } from '@ant-design/pro-components';
import { AsyncEditorPage } from '@/components/page/AsyncEditorPage';
import { use<YourEntity>EditController } from './use<YourEntity>EditController';
import type { EditPageProps } from '@/components/page/DocumentEditPage';

/**
 * Uses AsyncEditorPage directly — NOT DocumentEditPage — because this is a pure CRUD
 * entity with no item panel slot, no doc-action modal, no multi-select modal.
 */
const <YourEntity>EditPage: React.FC<EditPageProps> = () => {
    const { controller, loading } = use<YourEntity>EditController();
    const formRef = useRef<ProFormInstance>(undefined);

    // Give the controller access to formRef so saveModule() can trigger submission.
    useEffect(() => {
        (controller as { formRef?: typeof formRef }).formRef = formRef;
    }, [controller, formRef]);

    const asyncPageMeta = !loading ? controller.buildAsyncPageMeta() : undefined;
    const asyncInitialValues = !loading ? controller.buildInitialValues() as Record<string, unknown> : undefined;

    return (
        <AsyncEditorPage
            loading={loading}
            pageMeta={asyncPageMeta}
            initialValues={asyncInitialValues}
            formRef={formRef}
            onFinish={controller.handleFinish as (values: Record<string, unknown>) => Promise<boolean>}
        />
    );
};

export default <YourEntity>EditPage;
```

---

## 6. Register in shared files (4 edits)

### 6a. `src/router/index.tsx` — add imports + 2 routes

```tsx
import <YourEntity>ListPage from '@/pages/platform/<yourEntity>/<YourEntity>ListPage';
import <YourEntity>EditPage from '@/pages/platform/<yourEntity>/<YourEntity>EditPage';

// Inside the router children array:
{ path: 'platform/<yourEntity>', element: <<YourEntity>ListPage /> },
{ path: 'platform/<yourEntity>/new', element: <<YourEntity>EditPage processMode={PROCESSMODE_NEW} /> },
{ path: 'platform/<yourEntity>/:uuid/edit', element: <<YourEntity>EditPage processMode={PROCESSMODE_EDIT} /> },
```

Note: only 2 imports and 3 routes (no item-edit page or panel).

### 6b. `src/router/menuConfig.ts` — add a menu entry

Add under the appropriate group (master data / system resource):
```ts
{ key: '<yourEntity>-list', label: t('<yourEntity>List'), path: '/platform/<yourEntity>' }
```

### 6c. `src/i18n/locales/en/Menu.json` + `zh/Menu.json` — add keys

```json
"<yourEntity>List": "..."
```

### 6d. `src/services/DocumentManagerFactory.ts` — replace stub with real import

**Step 1:** Replace the `declare const` stub with the real import:
```ts
// Remove:
declare const <YourEntity>Manager: any;

// Add:
import { <YourEntity>Manager } from '@/services/platform/<YourEntity>Manager';
```

**Step 2:** Add to the class-return function (`getDocumentManagerDef`, ~line 428).  
Use **string equality** (`===`), NOT `Number(docType)`:
```ts
// Pure ServiceEntity uses DummyDocumentType (string), not DocumentType (number)
if (docType === DocumentConstants.DummyDocumentType.<YourEntity>)
    return <YourEntity>Manager as unknown as DocumentManagerInstance;
```

**Step 3:** Add to the instance-cache function (`getDocumentManager`, ~line 503):
```ts
if (docType === DocumentConstants.DummyDocumentType.<YourEntity>)
    return pushCache(docType, new <YourEntity>Manager() as unknown as DocumentManagerInstance);
```

---

## 7. Verify

```bash
cd IntelligentUI
npx tsc --noEmit 2>&1 | grep -i "<yourEntity>"  # should be empty
```

Then run the app and test:
1. List page loads — open the browser network tab and confirm `searchTableService` succeeds
2. New record creates and saves
3. Existing record loads with all fields populated
4. Enum dropdowns load their options correctly
5. FK lookup select loads its option list and the readonly name field populates on selection

> **If `searchTableService` returns a backend exception:** the most likely cause is that
> `searchContent` contains a field name the backend `<YourEntity>SearchModel.java` doesn't know.
> Read `SearchModel.java` directly and match the fields exactly.

---

## Pre-flight: searchContent must be flat

Pure ServiceEntity backends have a **flat** `SearchModel` with no sub-objects. This is the most
common mistake when migrating from a standard document (which uses `headerModel`,
`createdUpdateModel`, etc.).

| Entity type | searchContent shape | Use `extendDocSearchTabFieldMeta`? |
|---|---|---|
| Standard document | Sub-objects: `headerModel`, `createdUpdateModel`, party models | ✅ Yes |
| Dummy document with workflow | Mixed: doc-action models, party models, flat fields | ✅ Partial |
| **Pure ServiceEntity** | **Flat fields only** — one key per SearchModel field | ❌ **No** |

**Check:** open `<YourEntity>SearchModel.java`, list all declared fields. Your `searchContent`
object must have **exactly those keys** (no more, no fewer).

The `fieldName` values in `embeddedTabMetaList` must also be **flat** (`'id'`, `'name'`,
`'someCategory'`) — not `'headerModel.id'` or `'<yourEntity>UIModel.id'`.

---

## Summary: differences from the other migration patterns

| Step | Standard Document | Dummy Document | Pure ServiceEntity |
|---|---|---|---|
| Types folder | `src/types/logistics/` | `src/types/platform/` | `src/types/platform/` |
| Manager folder | `src/services/logistics/` | `src/services/platform/` | `src/services/platform/` |
| Pages folder | `src/pages/logistics/` | `src/pages/platform/` | `src/pages/platform/` |
| i18n folder | `*/supplyChain/` | `*/coreFunction/` | `*/coreFunction/` |
| `getDocumentType()` | Numeric type | `NaN` + `getDummyDocumentType()` | `NaN` (no `getDummyDocumentType()` needed) |
| Factory lookup | `n === DocumentConstants.DocumentType.X` | `docType === DummyDocumentType.X` | `docType === DummyDocumentType.X` |
| `DOC_ACTION_CODE` | Yes | Yes (lifecycle) | **No** |
| `getStatusIconArray()` | Yes | Yes | **No** |
| Edit controller base | `DocumentEditController` | `DocumentEditController` | **`ServiceEditController`** |
| Edit hook | `useDocumentEditController` | `useDocumentEditController` | **`useServiceEntityEditController`** |
| Edit page component | `DocumentEditPage` | `DocumentEditPage` | **`AsyncEditorPage` directly** |
| `getActionCodeMatrix()` | workflow codes | lifecycle codes | **`return {}`** |
| `subNsAliases` in i18nConfig | docInvolveParty + others | docActionNode + others | **`{}`** (empty) |
| Route prefix | `logistics/` | `platform/` | `platform/` |
| Item page + panel files | Yes (4 files) | Yes (4 files) | **No** |
| Total new files | **16** | **16** | **10** |
| Shared files to edit | **4** | **4** | **4** |
| Backend changes | 0 | 0 | 0 |

**File count: 10 new files + 4 shared edits.**

| # | File | Purpose |
|---|---|---|
| 1 | `src/types/platform/<YourEntity>Content.ts` | TypeScript types |
| 2 | `src/i18n/locales/en/coreFunction/<YourEntity>.json` | English labels |
| 3 | `src/i18n/locales/zh/coreFunction/<YourEntity>.json` | Chinese labels |
| 4 | `src/services/platform/<YourEntity>Manager.ts` | API + i18n + config |
| 5 | `src/pages/platform/<yourEntity>/<YourEntity>ListController.tsx` | List controller |
| 6 | `src/pages/platform/<yourEntity>/use<YourEntity>ListController.ts` | List hook |
| 7 | `src/pages/platform/<yourEntity>/<YourEntity>ListPage.tsx` | List page |
| 8 | `src/pages/platform/<yourEntity>/<YourEntity>EditController.tsx` | Edit controller |
| 9 | `src/pages/platform/<yourEntity>/use<YourEntity>EditController.ts` | Edit hook |
| 10 | `src/pages/platform/<yourEntity>/<YourEntity>EditPage.tsx` | Edit page |

