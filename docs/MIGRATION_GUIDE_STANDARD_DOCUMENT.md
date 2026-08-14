# Standard Document UI Migration Guide

Migrate a legacy **standard document** (e.g. PurchaseOrder, InboundDelivery, SalesContract) from the legacy Vue 2 UI into the new React IntelligentUI project.

**Reference implementation:** `PurchaseRequest` — compare with `PurchaseContract` when in doubt.  
**Audience:** Developers and AI agents — all steps are concrete and executable.

---

## 0. Before you start

### Confirm backend readiness
The backend must expose these endpoints under `/api/<rootNodeInstId>/`:

| Endpoint | Required |
|---|---|
| `searchTableService` | ✅ list page |
| `loadModuleEditService` | ✅ editor load |
| `saveModuleService` | ✅ editor save |
| `getDocActionConfigureList` | ✅ workflow buttons |
| `executeDocAction` | ✅ workflow actions |
| `getStatusMap` | ✅ status select options |
| `getPriorityMap` | ✅ (if the doc has priority) |
| `newModuleService` | ✅ create mode |
| `loadModuleEditService` on item prefix | ✅ item editor |
| `saveModuleService` on item prefix | ✅ item editor |

And verify `DocumentConstants.ts` has entries for the doc type:
- `DocumentType.<YOURDOC>` (numeric, e.g. `PURCHASEREQUEST: 34`)
- `<YourDoc>.status` block (under `DocumentConstants`)

### Gather from legacy source
Before writing any code, read the legacy JS files and collect:
- `getRootNodeInstId()` → the URL prefix string (e.g. `'purchaseRequest'`)
- `getItemNodeInstId()` → the item URL prefix string (e.g. `'purchaseRequestMaterialItem'`)
- `DOC_ACTION_CODE` constants (which actions exist)
- The Vue `data.content` shape → the ServiceUIModel wrapper structure
- `getDefaultPageMeta()` field lists from the Editor JS file

---

## 1. Create the TypeScript types

**File:** `src/types/logistics/<YourDoc>Content.ts`  
**Mirrors:** `src/types/logistics/PurchaseRequestContent.ts`

```ts
import type { DocumentUIModel } from '../platform/DocumentUIModel';
import type { DocMatItemUIModel } from '../platform/DocMatItemUIModel';
import type { DocInvolvePartyUIModel } from '../platform/DocInvolvePartyUIModel';

// Root model — extends DocumentUIModel, adds doc-specific fields
export interface <YourDoc>UIModel extends DocumentUIModel {
    grossPrice?: number;
    // ... add doc-specific fields from the backend <YourDoc>UIModel.java
}

// Involve-party models (if the doc has supplier/org sections)
export interface InvolvePartyUIModel extends DocInvolvePartyUIModel {
    name?: string; // backwards-compat alias
}

// Item model — extends DocMatItemUIModel, adds item-specific fields
export interface <YourDoc>MaterialItemUIModel extends DocMatItemUIModel {
    amount: number;
    unitPrice: number;
    itemStatus?: number;
    itemStatusValue?: string;
    // ... add item-specific fields
}

// Item wrapper (the backend ServiceUIModel for one item)
export interface <YourDoc>MaterialItemServiceUIModel {
    <yourDoc>MaterialItemUIModel: <YourDoc>MaterialItemUIModel;
    <yourDoc>MaterialItemAttachmentUIModelList?: unknown[];
}

// Action node model (for rightBar action logs)
export interface <YourDoc>ActionNodeUIModel {
    docActionCode?: number;
    executionTime?: string;
    executedByUserName?: string;
    note?: string;
    [key: string]: unknown;
}

// Top-level ServiceUIModel wrapper — mirrors vue data.content exactly
export interface <YourDoc>ServiceUIModel {
    <yourDoc>UIModel: <YourDoc>UIModel;
    purchaseToOrgUIModel?: InvolvePartyUIModel;      // if the doc has org
    purchaseFromSupplierUIModel?: InvolvePartyUIModel; // if the doc has supplier
    // Action nodes — one per doc-action type used in the right bar
    submittedBy?: <YourDoc>ActionNodeUIModel;
    approvedBy?: <YourDoc>ActionNodeUIModel;
    // ... other action nodes from the backend ServiceUIModel.java
    <yourDoc>MaterialItemUIModelList: <YourDoc>MaterialItemServiceUIModel[];
    <yourDoc>AttachmentUIModelList: unknown[];
    serviceUIMeta: Record<string, unknown>;
}
```

**Key rules:**
- Root model key = `<yourDoc>UIModel` (camelCase class name with lowercase first letter)
- Item model key = `<yourDoc>MaterialItemUIModel` (this MUST match `getItemUIModelKey()`)
- All field names must exactly match the Java UIModel field names

---

## 2. Create i18n locale files

**Files:**
- `src/i18n/locales/en/supplyChain/<YourDoc>.json`
- `src/i18n/locales/zh/supplyChain/<YourDoc>.json`

**Mirrors:** `src/i18n/locales/en/supplyChain/PurchaseRequest.json`

Decode from the legacy `.properties` files under `admin/i18n/supplyChain/`:

```python
# Decode script (run in admin/i18n/supplyChain/)
with open('<YourDoc>_en.properties', 'r', encoding='utf-8') as f:
    content = f.read()
result = {}
for line in content.splitlines():
    if not line.strip() or line.startswith('#') or '=' not in line:
        continue
    k, v = line.split('=', 1)
    result[k.strip()] = v.encode('utf-8').decode('unicode_escape').strip()
```

**JSON structure to follow:**
```json
{
  "pageTitle": "...",
  "messages": {
    "deleted": "...", "created": "...", "updated": "...",
    "itemCreated": "...", "itemUpdated": "..."
  },
  "<yourDoc>": {
    "id": "...", "name": "...", "status": "...",
    "<yourDoc>Section": "...",
    "<yourDoc>MaterialItemSection": "..."
  },
  "form": { "validation": { "<yourDoc>NameRequired": "..." } },
  "<yourDoc>MaterialItem": {
    "identity": "...", "pricing": "...",
    "status": { "1": "...", "2": "...", "3": "..." },
    "units": { "pcs": "...", "kg": "..." }
  }
}
```

---

## 3. Create the Manager

**File:** `src/services/logistics/<YourDoc>Manager.ts`  
**Mirrors:** `src/services/logistics/PurchaseRequestManager.ts`

```ts
import i18n from '@/i18n';
import { ServiceModuleConstants, DocumentConstants, DocumentContentProp } from '@/services/DocumentConstants';
import { ServiceManager, type PopoverCardConfig } from '@/services/ServiceManager';
import { executeDocActionGeneric, getDocActionConfigureList as getDocActionConfigureListGeneric, type DocActionConfigure } from '@/api/docActionApi';

// 1. Import foundation namespaces (always include these 5 + your business ns)
import enCommonElements from '@/i18n/locales/en/foundation/CommonElements.json';
import enDocInvolveParty from '@/i18n/locales/en/foundation/DocInvolveParty.json'; // omit if no involve-party
import enDocActionNode from '@/i18n/locales/en/foundation/DocActionNode.json';
import enDocFlowNode from '@/i18n/locales/en/foundation/DocFlowNode.json';
import enDocMatItem from '@/i18n/locales/en/foundation/DocMatItem.json';
import en<YourDoc> from '@/i18n/locales/en/supplyChain/<YourDoc>.json';
// ... same for zh

// 2. Register on import (side-effect — fires once when the module is first imported)
i18n.addResourceBundle('en', 'commonElements', enCommonElements, true, false);
i18n.addResourceBundle('en', 'docInvolveParty', enDocInvolveParty, true, false);
i18n.addResourceBundle('en', 'docActionNode', enDocActionNode, true, false);
i18n.addResourceBundle('en', 'docFlowNode', enDocFlowNode, true, false);
i18n.addResourceBundle('en', 'docMatItem', enDocMatItem, true, false);
i18n.addResourceBundle('en', '<yourDoc>', en<YourDoc>, true, false);
// ... same for zh

export class <YourDoc>Manager extends ServiceManager {
    // 3. Action codes — pick from SystemDefDocActionCodeProxy constants
    static readonly DOC_ACTION_CODE = {
        APPROVE:        DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.ACTION_APPROVE,
        REJECT_APPROVE: DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.ACTION_REJECT_APPROVE,
        SUBMIT:         DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.ACTION_SUBMIT,
        REVOKE_SUBMIT:  DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.ACTION_REVOKE_SUBMIT,
        COUNTAPPROVE:   DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.ACTION_COUNTAPPROVE,
        DELIVERY_DONE:  DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.ACTION_DELIVERY_DONE,
        // ... add/remove based on what the doc actually supports
    } as const;

    // 4. Node inst ids — MUST match the backend @RequestMapping paths
    static getRootNodeInstId(): string { return '<yourDoc>'; }
    static getItemNodeInstId(): string { return '<yourDoc>MaterialItem'; }

    // 5. Identity
    static getResourceId(): string { return ServiceModuleConstants.<YourDoc>; }
    static getDocumentType(): number { return DocumentConstants.DocumentType.<YOURDOC>; }

    // 6. Status URL (standard docs use getStatusMap)
    static getStatusURL(): string { return '<yourDoc>/getStatusMap'; }

    // 7. Attachment config
    static getAttachmentConfig() {
        return {
            module: '<yourDoc>',
            loadURL: '<yourDoc>/loadAttachment',
            uploadURL: '<yourDoc>/uploadAttachment',
            uploadTextURL: '<yourDoc>/uploadAttachmentText',
            deleteURL: '<yourDoc>/deleteAttachment',
        };
    }

    // 8. i18n config
    static getI18nRootConfig() {
        return {
            primaryNs: '<yourDoc>',
            keyPrefix: '<yourDoc>',
            fallbackNs: 'commonElements',
            subNsAliases: {
                docInvolveParty: 'docInvolveParty',
                docActionNode: 'docActionNode',
                docFlowNode: 'docFlowNode',
                docMatItem: 'docMatItem',
            },
        };
    }

    static getI18nItemConfig() {
        return {
            primaryNs: '<yourDoc>',
            keyPrefix: '<yourDoc>MaterialItem',
            fallbackNs: 'commonElements',
            subNsAliases: { /* same as root */ },
        };
    }

    // 9. Status icons — must cover every status in DocumentConstants.<YourDoc>.status
    static getStatusIconArray(): Array<{ id: number; iconClass: string }> {
        return [
            { id: DocumentConstants.<YourDoc>.status.INITIAL,    iconClass: DocumentContentProp.statusIcon.INITIAL },
            { id: DocumentConstants.<YourDoc>.status.SUBMITTED,  iconClass: DocumentContentProp.statusIcon.SUBMITTED },
            { id: DocumentConstants.<YourDoc>.status.APPROVED,   iconClass: DocumentContentProp.statusIcon.APPROVED },
            // ... add others from DocumentConstants.<YourDoc>.status
        ];
    }

    static formatStatusIconClass(status: number): string | undefined {
        return <YourDoc>Manager.getStatusIconArray().find(e => e.id === status)?.iconClass;
    }

    // 10. Status label map for right-bar help docs
    static getStatusLabelMap(): Record<string, Record<string, string>> {
        const labels: Record<string, string> = {
            '1': '未开始', '299': '已提交审核', /* ... */
        };
        return {
            '<yourDoc>.status': labels,
            '<yourDoc>MaterialItem.itemStatus': labels,
        };
    }

    // 11. Popover content (for docPopConfig in list columns)
    static getDocumentPopoverContent(): PopoverCardConfig {
        return {
            docType: DocumentConstants.DocumentType.<YOURDOC>,
            targetPage: '/logistics/<yourDoc>',
            fieldMetaList: [
                { fieldName: '<yourDoc>UIModel.id',   labelKey: '<yourDoc>.id' },
                { fieldName: '<yourDoc>UIModel.name', labelKey: '<yourDoc>.name' },
                // ...
            ],
        };
    }

    // 12. Doc actions
    static getDocActionConfigureList(): Promise<DocActionConfigure[]> {
        return getDocActionConfigureListGeneric(`${this.getRootNodeInstId()}/getDocActionConfigureList`);
    }

    static executeDocAction(serviceUIModel: Record<string, unknown>, actionCode: number, extras = {}) {
        return executeDocActionGeneric(`${this.getRootNodeInstId()}/executeDocAction`, serviceUIModel, actionCode, extras);
    }
}
```

---

## 4. Create pages directory and List files (3 files)

Create directory: `src/pages/logistics/<yourDoc>/`

### 4a. `<YourDoc>ListController.tsx`
**Mirrors:** `PurchaseRequestListController.tsx`

> **Do NOT define a local `*ListItem` interface.** Use the real `<YourDoc>ServiceUIModel` from the types file as the generic parameter — the list and editor pages receive the same shape from the backend.

```ts
import type { <YourDoc>ServiceUIModel } from '@/types/logistics/<YourDoc>Content';
import { pushErrorMessageBar } from '@/controllers/messageBarStore';

export class <YourDoc>ListController extends ServiceListController<<YourDoc>ServiceUIModel, SearchParams> {
    readonly getStatusURL = '<yourDoc>/getStatusMap';

    // searchContent — one key per search field/group from the legacy searchContent object
    readonly searchContent: Record<string, unknown> = {
        headerModel: ServiceUIConstants.getDocSearchHeaderModel(),
        createdUpdateModel: ServiceUIConstants.getCreateUpdateSearchModel(),
        purchaseFromSupplier: ServiceUIConstants.getAccountSearchHeaderModel(),
        purchaseToOrg: ServiceUIConstants.getAccountSearchHeaderModel(),
        // doc-flow refs from legacy docFlowIdList
        prevDoc: ServiceUIConstants.getDocFlowSearchHeaderModel(),
        // action log nodes from legacy docActionLogIdList
        submittedBy: ServiceUIConstants.getDocActionNodeSearchModel(),
        approvedBy: ServiceUIConstants.getDocActionNodeSearchModel(),
        itemMaterialSKU: ServiceUIConstants.getItemMaterialSearchModel(),
    };

    protected getRowId(row) { return row.<yourDoc>UIModel.uuid ?? row.<yourDoc>UIModel.id; }
    getRowNavId(row) { return row.<yourDoc>UIModel.uuid ?? row.<yourDoc>UIModel.id; }
    protected getI18nConfig() { return <YourDoc>Manager.getI18nRootConfig(); }
    protected getServiceManager() { return <YourDoc>Manager; }
    protected getSearchContent() { return this.searchContent; }

    getTableConfig() {
        return {
            headerTitle: i18n.t('<yourDoc>:pageTitle'),
            rowKey: (row) => row.<yourDoc>UIModel.uuid ?? row.<yourDoc>UIModel.id,
            pagination: { pageSize: 10, showSizeChanger: true },
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
                    embeddedTabMetaList: ServiceListController.extendDocSearchTabFieldMeta({
                        vm: this,
                        helpKeyPrefix: '<yourDoc>',
                        docActionLogIdList: ['submittedBy', 'approvedBy'],
                        docFlowIdList: ['prevDoc'],
                        docPartyIdList: ['purchaseFromSupplier', 'purchaseToOrg'],
                        headerPostMetaList: [/* date range fields */],
                    }),
                },
                {
                    sectionId: '<yourDoc>List',
                    sectionCategory: SectionCategory.EMBEDLIST,
                    fieldMetaList: [
                        { fieldName: '<yourDoc>UIModel.uuid', labelKey: 'id', hidden: true },
                        { fieldName: '<yourDoc>UIModel.id', labelKey: 'id',
                          docPopConfig: { documentType: DocumentConstants.DocumentType.<YOURDOC>, uuidFieldName: '<yourDoc>UIModel.uuid' } },
                        { fieldName: '<yourDoc>UIModel.name', labelKey: 'name' },
                        { fieldName: '<yourDoc>UIModel.statusValue', fieldKey: '<yourDoc>UIModel.status',
                          labelKey: 'status', iconArray: <YourDoc>Manager.getStatusIconArray() },
                        // ... other columns
                    ] as ListColumnJson[],
                },
            ],
        };
    }

    request = async (params) => {
        const content = Object.keys(this.panelParams).length ? this.panelParams : this.searchContent;
        try {
            const result = await <YourDoc>Manager.listDocuments({ content, current: params.current, pageSize: params.pageSize });
            return { data: result.data, success: true, total: result.total };
        } catch (err) {
            pushErrorMessageBar(err instanceof Error ? err.message : String(err), { context: 'list-error' });
            return { data: [], success: false, total: 0 };
        }
    };

    navigateToNew = () => this.deps.navigate('/logistics/<yourDoc>/new');
    navigateToEdit = (uuid) => this.deps.navigate(`/logistics/<yourDoc>/${uuid}/edit`);
    loadModule = (uuid) => this.loadModuleEdit(uuid);
    newModule = () => this.navigateToNew();
}
```

### 4b. `use<YourDoc>ListController.ts`
```ts
import '@/services/logistics/<YourDoc>Manager';  // triggers i18n registration
export function use<YourDoc>ListController() {
    return useDocumentListController({
        buildController: (deps) => new <YourDoc>ListController(deps),
    });
}
```

### 4c. `<YourDoc>ListPage.tsx`
```tsx
const <YourDoc>ListPage: React.FC = () => {
    const { controller } = use<YourDoc>ListController();
    return <AsyncListPage pageMeta={controller.buildAsyncListPageMeta()} />;
};
export default <YourDoc>ListPage;
```

---

## 5. Create the Editor files (3 files)

### 5a. `<YourDoc>EditController.tsx`
**Mirrors:** `PurchaseRequestEditController.tsx`

```ts
// Deps interface
export interface <YourDoc>EditControllerDeps extends ServiceEditControllerDeps {
    serviceUIModel?: <YourDoc>ServiceUIModel;
    dataSource: <YourDoc>MaterialItemServiceUIModel[];
    setDataSource: React.Dispatch<React.SetStateAction<<YourDoc>MaterialItemServiceUIModel[]>>;
    editableKeys: React.Key[];
    setEditableKeys: React.Dispatch<React.SetStateAction<React.Key[]>>;
    actionCodeList?: Array<{ actionCode: number; [key: string]: unknown }>;
    multiSelectFactory?: import('@/components/doc/DocumentItemMultiSelectFactory').DocumentItemMultiSelectFactory;
}

export class <YourDoc>EditController extends DocumentEditController<<YourDoc>ServiceUIModel, FormValues> {
    private readonly extraDeps: <YourDoc>EditControllerDeps;

    readonly getStatusURL = '<yourDoc>/getStatusMap';
    readonly executeDocActionURL = '<yourDoc>/executeDocAction';
    readonly getDocActionConfigureListURL = '<yourDoc>/getDocActionConfigureList';

    constructor(deps) { super(deps); this.extraDeps = deps; }

    protected loadModule() {
        return this.processMode === PROCESSMODE_EDIT ? this.extraDeps.serviceUIModel : undefined;
    }

    protected buildCreateDefaults() {
        return { <yourDoc>UIModel: {} as FormValues['<yourDoc>UIModel'] };
    }

    protected buildPayload(values) {
        const merged = super.buildPayload(values) as Record<string, unknown>;
        return {
            ...merged,
            <yourDoc>MaterialItemUIModelList: this.extraDeps.dataSource,
            <yourDoc>AttachmentUIModelList: [],
            serviceUIMeta: {},
        };
    }

    protected getBasePath() { return '/logistics/<yourDoc>'; }
    protected getI18nConfig() { return <YourDoc>Manager.getI18nRootConfig(); }
    protected getServiceManager() { return <YourDoc>Manager; }

    getBaseUUID() { return this.loadModule()?.<yourDoc>UIModel.uuid; }

    getActionCodeMatrix() {
        return {
            submit:        { actionCode: <YourDoc>Manager.DOC_ACTION_CODE.SUBMIT },
            revokeSubmit:  { actionCode: <YourDoc>Manager.DOC_ACTION_CODE.REVOKE_SUBMIT },
            approve:       { actionCode: <YourDoc>Manager.DOC_ACTION_CODE.APPROVE },
            rejectApprove: { actionCode: <YourDoc>Manager.DOC_ACTION_CODE.REJECT_APPROVE },
            // ... add all action codes the doc supports
        };
    }

    handleDeleteItem = (itemId) => {
        this.extraDeps.setDataSource(prev =>
            prev.filter(r => r.<yourDoc>MaterialItemUIModel.uuid !== itemId)
        );
    };

    navigateToItem = (itemUuid) => {
        this.deps.navigate(`/logistics/<yourDoc>MaterialItem/${itemUuid}/edit`);
    };

    protected getDefaultPageMeta(): PageMeta {
        return {
            documentType: '<YourDoc>',
            processButtonMeta: {
                exit: { callback: 'exitModule' },
                placeholder: { category: ProcessButtonConstants.placeholderCategory.DOC_ACTION_BTN },
                save: { formatClass: 'displayForEdit', callback: 'saveModule' },
            },
            tabMetaList: [
                {
                    tabId: 'basic',
                    tabTitle: '<yourDoc>:<yourDoc>.<yourDoc>Section',
                    tabIcon: 'mdi mdi-cart content-portlet-title',
                    sectionMetaList: [
                        {
                            sectionTitle: '<yourDoc>:<yourDoc>.<yourDoc>Section',
                            parentContentPath: '<yourDoc>UIModel',
                            sectionCategory: SectionCategory.EDIT,
                            fieldMetaList: [
                                { fieldName: 'id', readonly: true, required: true },
                                { fieldName: 'name', required: true },
                                { fieldName: 'status', fieldType: 'select', readonly: true,
                                  settings: { getMetaDataUrl: this.getStatusURL, iconArray: <YourDoc>Manager.getStatusIconArray() } },
                                // ... add all fields from the legacy editor's basic section
                            ],
                        },
                        // CUSTOMERCONTACT section for supplier (if applicable):
                        {
                            sectionTitle: '<yourDoc>:<yourDoc>.purchaseFromSupplierSection',
                            parentContentPath: 'purchaseFromSupplierUIModel',
                            sectionCategory: SectionCategory.CUSTOMERCONTACT,
                            accountObjectType: DocumentConstants.AccountObject.AccountType.SUPPLIER,
                            fieldMetaList: [],
                        },
                        // Attachment section:
                        {
                            sectionTitle: '<yourDoc>:<yourDoc>.attachmentsSection',
                            parentContentPath: '<yourDoc>AttachmentUIModelList',
                            sectionCategory: SectionCategory.ATTACHMENT,
                            titleIcon: 'mdi mdi-paperclip content-portlet-title',
                            fieldMetaList: [],
                        },
                    ],
                },
                // Items tab:
                {
                    tabId: 'items',
                    tabTitle: '<yourDoc>:<yourDoc>.<yourDoc>MaterialItemSection',
                    tabIcon: 'mdi mdi-texture content-portlet-title',
                    sectionMetaList: [{
                        sectionCategory: SectionCategory.EMBEDLIST,
                        listSubPath: <YourDoc>MaterialItemController.getItemUIModelKey(),
                        parentContentPath: '<yourDoc>MaterialItemUIModelList',
                        refItemName: '<yourDoc>MaterialItemPanel',
                        editModuleFlag: true,
                        editModuleModalFlag: true,
                        embedProcessButtonMeta: { addDisableFlag: 'disableNotInInit', newModuleModalFlag: true },
                        fieldMetaList: [ /* item list columns */ ],
                    }],
                },
            ],
        };
    }
}
```

### 5b. `use<YourDoc>EditController.ts`
```ts
export function use<YourDoc>EditController(processMode: ProcessMode) {
    return useDocumentEditController<
        <YourDoc>ServiceUIModel,
        <YourDoc>MaterialItemServiceUIModel,
        <YourDoc>EditController,
        <YourDoc>MaterialItemPanelHandle
    >(processMode, {
        loadModule: (uuid) => <YourDoc>Manager.loadDocument<<YourDoc>ServiceUIModel>(uuid),
        loadActionConfigureList: () => <YourDoc>Manager.getDocActionConfigureList(),
        extractItems: (r) => r.<yourDoc>MaterialItemUIModelList ?? [],
        buildController: (deps) => new <YourDoc>EditController(deps),
        rightBar: {
            tab1Mode: 'actionLog',
            getRootNodeInstId: () => <YourDoc>Manager.getRootNodeInstId(),
            i18nNamespace: '<yourDoc>',
            helpDocumentName: ['<YourDoc>HelpDocument', '<YourDoc>MaterialItemHelpDocument'],
            i18nPath: 'supplyChain',
            statusLabelMap: <YourDoc>Manager.getStatusLabelMap(),
            helpLoadGate: 'mount',
        },
        materialItemPanelName: '<yourDoc>MaterialItemPanel',
    });
}
```

### 5c. `<YourDoc>EditPage.tsx`
```tsx
const <YourDoc>EditPage: React.FC<EditPageProps> = ({ processMode }) => (
    <DocumentEditPage<...PanelHandle>
        processMode={processMode}
        useController={use<YourDoc>EditController}
        renderItemPanel={(ref, uuid, onSaved) => (
            <<YourDoc>MaterialItemPanel ref={ref} parentUuid={uuid} onSaved={onSaved} />
        )}
    />
);
export default <YourDoc>EditPage;
```

---

## 6. Create the Item editor files (4 files)

### 6a. `<YourDoc>MaterialItemController.tsx`
**Mirrors:** `PurchaseRequestMaterialItemController.tsx`

```ts
export class <YourDoc>MaterialItemController extends DocItemEditController<<YourDoc>MaterialItemUIModel, ItemFormValues> {

    // THIS MUST BE THE EXACT KEY NAME in the wrapper ServiceUIModel
    static getItemUIModelKey(): '<yourDoc>MaterialItemUIModel' {
        return '<yourDoc>MaterialItemUIModel';
    }

    protected loadModule() {
        return (this.deps as Deps).serviceUIModel?.<yourDoc>MaterialItemUIModel;
    }

    getParentUUID() { return this.loadModule()?.parentNodeUUID ?? this.parentUuid; }
    protected getParentEditPath() { return `/logistics/<yourDoc>/${this.getParentUUID()}/edit`; }
    protected getModuleListPath() { return '/logistics/<yourDoc>'; }
    protected getI18nConfig() { return <YourDoc>Manager.getI18nItemConfig(); }
    protected getServiceManager() { return <YourDoc>Manager; }
    getBaseUUID() { return this.loadModule()?.uuid; }
    readonly getDocFlowListURL = '<yourDoc>MaterialItem/getDocFlowList';

    getAttachmentConfig() {
        return {
            module: '<yourDoc>MaterialItem',
            loadURL: '<yourDoc>MaterialItem/loadAttachment',
            uploadURL: '<yourDoc>MaterialItem/uploadAttachment',
            uploadTextURL: '<yourDoc>MaterialItem/uploadAttachmentText',
            deleteURL: '<yourDoc>MaterialItem/deleteAttachment',
        };
    }

    protected getDefaultPageMeta(): PageMeta {
        return {
            documentType: '<YourDoc>MaterialItem',
            pageHeaderConfig: [
                { nodeInstId: '<yourDoc>', baseEditUrl: '/logistics/<yourDoc>/:uuid/edit',
                  targetTab: 'items', pageTitlePath: '<yourDoc>:<yourDoc>MaterialItem.<yourDoc>PageTitle', pageTitleVarPath: 'id' },
                { active: true, nodeInstId: '<yourDoc>MaterialItem',
                  baseEditUrl: '/logistics/<yourDoc>MaterialItem/:uuid/edit',
                  pageTitlePath: '<yourDoc>:<yourDoc>MaterialItem.pageHeaderTitle', pageTitleVarPath: 'refMaterialSKUId' },
            ],
            processButtonMeta: {
                exit: { callback: 'exitModule' },
                save: { formatClass: 'displayForEdit', callback: 'saveModule' },
            },
            tabMetaList: [
                {
                    tabId: 'identity',
                    tabTitle: '<yourDoc>:<yourDoc>MaterialItem.identity',
                    sectionMetaList: [{
                        fieldMetaList: [
                            { fieldName: 'refMaterialSKUId', labelKey: 'materialId' },
                            { fieldName: 'refMaterialSKUName', labelKey: 'materialName' },
                            // ...
                        ],
                    }, {
                        sectionId: 'refDocumentSection',
                        pageOnly: true,
                        sectionCategory: SectionCategory.DOCFLOW,
                        parentContentPath: <YourDoc>MaterialItemController.getItemUIModelKey(),
                        titleLabelKey: 'refDocumentSection',
                    }],
                },
                {
                    tabId: 'pricing',
                    tabTitle: '<yourDoc>:<yourDoc>MaterialItem.pricing',
                    sectionMetaList: [{ fieldMetaList: [
                        { fieldName: 'amount', fieldType: 'number' },
                        { fieldName: 'unitPrice', fieldType: 'number' },
                        { fieldName: 'itemStatus', fieldType: 'select',
                          options: [{ id: 1, text: '...' }, { id: 2, text: '...' }] },
                    ]}],
                },
                {
                    tabId: 'attachment',
                    sectionMetaList: [{
                        pageOnly: true,
                        parentContentPath: '<yourDoc>MaterialItemAttachmentUIModelList',
                        sectionCategory: SectionCategory.ATTACHMENT,
                        fieldMetaList: [],
                    }],
                },
            ],
        };
    }
}
```

### 6b. `use<YourDoc>MaterialItemController.ts`
```ts
export function use<YourDoc>ItemEditController() {
    return useItemEditController<
        <YourDoc>MaterialItemServiceUIModel,
        <YourDoc>MaterialItemController
    >({
        loadModule: (uuid) => <YourDoc>Manager.loadDocMatItem<<YourDoc>MaterialItemServiceUIModel>(uuid),
        buildController: (deps) => new <YourDoc>MaterialItemController(deps),
        rightBar: {
            tab1Mode: 'docFlow',       // ← 'docFlow' for item editors, not 'actionLog'
            getDocType: () => <YourDoc>Manager.getItemNodeInstId(),
            i18nNamespace: '<yourDoc>',
            helpDocumentName: '<YourDoc>MaterialItemHelpDocument',
            i18nPath: 'supplyChain',
            statusLabelMap: <YourDoc>Manager.getStatusLabelMap(),
            getDocFlowListURL: '<yourDoc>MaterialItem/getDocFlowList',
        },
    });
}
```

### 6c. `<YourDoc>ItemEditPage.tsx`
```tsx
const <YourDoc>ItemEditPage: React.FC = () => {
    const { controller, loading } = use<YourDoc>ItemEditController();
    const formRef = useRef<ProFormInstance>(undefined);
    const asyncPageMeta = !loading ? controller.buildAsyncPageMeta() : undefined;
    const asyncInitialValues = !loading ? controller.buildInitialValues() as Record<string, unknown> : undefined;
    return (
        <AsyncEditorPage loading={loading} pageMeta={asyncPageMeta}
            initialValues={asyncInitialValues} formRef={formRef}
            onFinish={controller.handleFinish as (values: Record<string, unknown>) => Promise<boolean>} />
    );
};
export default <YourDoc>ItemEditPage;
```

### 6d. `<YourDoc>MaterialItemPanel.tsx`
```tsx
export type <YourDoc>MaterialItemPanelHandle = EditPanelHandle;

const config: EditPanelConfig<<YourDoc>MaterialItemServiceUIModel> = {
    loadModule: async (uuid, controller) =>
        controller.loadModuleEdit<<YourDoc>MaterialItemServiceUIModel>(uuid),
    buildController: (args) => new <YourDoc>MaterialItemController(args),
    getEditPageURL: ({ uuid }) => `/logistics/<yourDoc>MaterialItem/${uuid}/edit`,
};

const <YourDoc>MaterialItemPanel = forwardRef<...>((props, ref) => (
    <EditPanel<<YourDoc>MaterialItemServiceUIModel> ref={ref} config={config} {...props} />
));
<YourDoc>MaterialItemPanel.displayName = '<YourDoc>MaterialItemPanel';
export { <YourDoc>MaterialItemPanel };
```

---

## 7. Register in shared files (4 edits)

### 7a. `src/router/index.tsx` — add imports + 4 routes
```tsx
import <YourDoc>ListPage from '@/pages/logistics/<yourDoc>/<YourDoc>ListPage';
import <YourDoc>EditPage from '@/pages/logistics/<yourDoc>/<YourDoc>EditPage';
import <YourDoc>ItemEditPage from '@/pages/logistics/<yourDoc>/<YourDoc>ItemEditPage';

// Inside the router children array:
{ path: 'logistics/<yourDoc>', element: <<YourDoc>ListPage /> },
{ path: 'logistics/<yourDoc>/new', element: <<YourDoc>EditPage processMode={PROCESSMODE_NEW} /> },
{ path: 'logistics/<yourDoc>/:uuid/edit', element: <<YourDoc>EditPage processMode={PROCESSMODE_EDIT} /> },
{ path: 'logistics/<yourDoc>MaterialItem/:uuid/edit', element: <<YourDoc>ItemEditPage /> },
```

### 7b. `src/router/menuConfig.ts` — add a menu entry
```ts
{ key: '<yourDoc>-list', label: t('<yourDoc>List'), path: '/logistics/<yourDoc>' }
```

### 7c. `src/i18n/locales/en/Menu.json` + `zh/Menu.json` — add keys
```json
"<yourDoc>List": "... list label ..."
```

### 7d. `src/services/DocumentManagerFactory.ts` — replace stub with real import

Find the line: `declare const <YourDoc>Manager: any;` and replace with:
```ts
import { <YourDoc>Manager } from '@/services/logistics/<YourDoc>Manager';
```

Add to BOTH lookup functions (there are two: one returns the class, one returns an instance):
```ts
// In getDocumentManagerDef (class-return, ~line 428):
if (n === DocumentConstants.DocumentType.<YOURDOC>)
    return <YourDoc>Manager as unknown as DocumentManagerInstance;

// In getDocumentManager (instance-cache, ~line 503):
if (n === DocumentConstants.DocumentType.<YOURDOC>)
    return pushCache(docType, new <YourDoc>Manager() as unknown as DocumentManagerInstance);
```

---

## 8. Verify

```bash
cd IntelligentUI
npx tsc --noEmit 2>&1 | grep -i "<yourDoc>"  # should be empty
```

Then run the app and test:
1. List page loads and Search button triggers the backend **without backend exceptions**
2. New record creates and saves
3. Existing record loads with all fields populated
4. Workflow buttons (Submit/Approve/…) appear and execute
5. Item quick-edit panel opens, saves, and refreshes the parent table
6. Item full-page editor loads and saves

> **⚠️ Before accepting step 1 as passed:** open the browser network tab and confirm the `searchTableService` request succeeds. A `com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException` means `searchContent` has fields the backend `*SearchModel.java` doesn't know. See the pre-flight check below.

---

## Pre-flight: verify searchContent against backend SearchModel

**Always do this before writing `searchContent`.**

Open the backend `<YourDoc>SearchModel.java` and list all its fields. Then check the legacy `<YourDoc>List.js` `data.searchContent` to confirm the shape. The searchContent you write must use **exactly the same field names and nesting** as the backend expects.

| Entity type | Typical searchContent shape | Example |
|---|---|---|
| Standard document (DocumentContent) | Sub-objects: `headerModel`, `createdUpdateModel`, action-node models, party models | `{ headerModel: ServiceUIConstants.getDocSearchHeaderModel(), submittedBy: ServiceUIConstants.getDocActionNodeSearchModel() }` |
| Plain ServiceEntity | **Flat fields only** — no sub-objects | `{ id: '', name: '', unitType: '' }` |

**Standard documents** (PurchaseContract, PurchaseRequest): the backend `SearchModel` extends `ServiceDocumentSearchModel` which accepts `headerModel` + `createdUpdateModel` as sub-objects. Use `ServiceUIConstants.getDocSearchHeaderModel()` and `extendDocSearchTabFieldMeta`.

**Plain ServiceEntity** (StandardMaterialUnit): the backend `SearchModel` has **only flat fields**. Sending `headerModel` causes `UnrecognizedPropertyException`. Use a plain flat object and build `embeddedTabMetaList` with flat `fieldName` values (`'id'`, `'name'` — not `'headerModel.id'`).

Also wrap the list `request()` in try/catch so backend errors show in the message bar:
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

## Summary: file count

| Category | Count |
|---|---|
| New files to create | **16** (1 type + 2 i18n + 1 manager + 3 list + 3 editor + 4 item + 2 cross-doc select if needed) |
| Shared files to edit | **4** (router, menuConfig, 2 Menu.json, DocumentManagerFactory) |
| Backend changes | **0** |
