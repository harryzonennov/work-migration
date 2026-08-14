# Plan: Migrate `pageHeaderConfig` Breadcrumb Navigation to IntelligentUI

> **Status:** Planning document — no code yet.
> **Created:** 2026-07-07
> **Companion docs:** [`MIGRATION_CONVERSATIONS.md`](./MIGRATION_CONVERSATIONS.md), [`plan-attachment-section-migration.md`](./plan-attachment-section-migration.md)

---

## 1. What is the breadcrumb?

Item-level editor pages (`PurchaseContractMaterialItemEditor`, `InboundItemEditor`, `SalesContractMaterialItemEditor`, etc.) show a breadcrumb at the top of the page linking each parent document in the chain.

Runtime example on `PurchaseContractMaterialItemEditor`:
```
采购合同:PU202503100001 / 采购物料:BC008A03 |
```

Each segment is a clickable `<a>` linking to the parent editor page. The last (current) segment is styled with `class="active"`.

## 2. The legacy pipeline in one picture

```
getDefaultPageMeta().pageHeaderConfig  ─┐
                                        │  static array of segment descriptors
                                        │  [{ nodeInstId, baseEditUrl, targetTab, pageTitlePath, pageTitleVarPath, active? }, …]
                                        ▼
ServiceEditorControlHelper.postUpdateUIModelBasic()
  → vm.checkPageHeader()                    ─── truthy check
  → vm.getPageHeaderModelList()
      → refPageHeader.initPageHeader({uuid, baseUUID, pageHeaderListUrl, fnPageHeaderModel})

PageHeaderUnion.initPageHeader()
  → POST <getPageHeaderModelListURL>
     body: { uuid, baseUUID }
     response: [{ nodeInstId, uuid, headerName, index }, …]
  → for each backend segment: vm.fnPageHeaderModel(model)
      → match against pageHeaderConfig by nodeInstId
      → build pageLink = genCommonEditURL(baseEditUrl, model.uuid, targetTab)
      → build pageTitle = label[pageTitlePath] + ':' + content[pageTitleVarPath]
      → set liClass = 'active' when config.active === true

Vue template renders:
  <ol class="breadcrumb pull-left">
    <li v-for="seg in list" :class="seg.liClass">
      <a :href="seg.pageLink">{{ seg.pageTitle }}</a>
    </li>
  </ol>
  <h4 class="page-title">|</h4>
```

## 3. What the backend supplies

Endpoint: `POST /<itemNodeName>/getPageHeaderModelList.html`

Request body:
```json
{ "uuid": "<current item UUID>", "baseUUID": "<parent doc UUID>" }
```

Response `content`:
```json
[
  { "nodeInstId": "purchaseContract",              "uuid": "d0…", "headerName": "…", "index": 0 },
  { "nodeInstId": "purchaseContractMaterialItem",  "uuid": "e4…", "headerName": "…", "index": 1 }
]
```

**Backend status:** All the item-level `*ItemEditorController` classes on `IntelligentPlatform` need a corresponding `getPageHeaderModelList` route. Some may already exist (legacy carried over) — verify per doc type.

## 4. Gaps in the new UI

| Layer | Gap |
|---|---|
| Types | `pageHeaderConfig` not in `PageMeta` |
| Component | No React equivalent of `PageHeaderUnion` |
| Editor page | `AsyncEditorPage` / `AsyncItemEditor` have no breadcrumb slot |
| Wiring | No controller triggers a `getPageHeaderModelList` fetch |
| Routing | Legacy `baseEditUrl` values are `.html` filenames — new UI needs SPA routes |

## 5. Phased plan

### Phase A — Framework [~2 hours]

Ship the infrastructure so any item editor can opt in with pure controller config.

#### A.1 — Type extension

Add to `PageMetaTypes.ts`:

```ts
// Mirrors legacy pageHeaderConfig from ServiceUiController.js:2069
export interface PageHeaderConfigSegment {
    /** Matches backend response nodeInstId — e.g. 'purchaseContract' */
    nodeInstId: string;
    /** SPA route pattern for the parent editor. Supports :uuid substitution.
     *  Legacy used HTML filenames like "PurchaseContractEditor.html" — this
     *  version uses React Router paths like "/logistics/purchaseContract/:uuid/edit". */
    baseEditUrl: string;
    /** Optional tab key — appended as ?tab= query param on the parent link. */
    targetTab?: string;
    /** Dot-path into `labelObject` — the segment title prefix (e.g. '采购合同'). */
    pageTitlePath: string;
    /** Dot-path into loaded record — the segment title variable (e.g. 'PU202503100001'). */
    pageTitleVarPath: string;
    /** Marks this segment as the current active page — renders <li class="active"> */
    active?: boolean;
}

// Extend PageMeta:
export interface PageMeta {
    // …existing fields…
    /**
     * Breadcrumb segment descriptors for item-level editor pages.
     * When set, AsyncItemEditor renders a <PageHeaderBreadcrumb> above the tabs.
     * Mirrors legacy pageHeaderConfig on getDefaultPageMeta() return value.
     */
    pageHeaderConfig?: PageHeaderConfigSegment[];
    /** POST URL that returns the list of parent-doc segments with their UUIDs.
     *  Mirrors legacy vm.getPageHeaderModelListURL data property. */
    getPageHeaderModelListURL?: string;
}
```

#### A.2 — `PageHeaderBreadcrumb.tsx` — new React component

File: `src/components/page/PageHeaderBreadcrumb.tsx`

Responsibilities:
1. POST to `getPageHeaderModelListURL` with `{ uuid, baseUUID }` on mount / uuid change
2. Match each returned segment against `pageHeaderConfig` by `nodeInstId`
3. Build display title as `labelObject[pageTitlePath] + ':' + record[pageTitleVarPath]`
4. Build link as `baseEditUrl` with `:uuid` substituted + optional `?tab=` query
5. Render the exact legacy markup: `<ol class="breadcrumb pull-left">` with `<li class="active">` on the current segment, and a trailing `<h4 class="page-title">|</h4>`

```tsx
// src/components/page/PageHeaderBreadcrumb.tsx
import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchObjValueByPath } from '@/services/ServiceUtilityHelper';
import type { PageHeaderConfigSegment } from '@/controllers/PageMetaTypes';

interface PageHeaderModel {
    nodeInstId: string;
    uuid: string;
    headerName?: string;
    index?: number;
}

interface RenderedSegment {
    pageLink: string;
    pageTitle: string;
    active?: boolean;
}

interface Props {
    pageHeaderConfig: PageHeaderConfigSegment[];
    getPageHeaderModelListURL: string;
    uuid?: string;
    baseUUID?: string;
    labelObject?: Record<string, unknown>;
    record?: Record<string, unknown>;
}

const PageHeaderBreadcrumb: React.FC<Props> = ({
    pageHeaderConfig,
    getPageHeaderModelListURL,
    uuid,
    baseUUID,
    labelObject,
    record,
}) => {
    const [models, setModels] = useState<PageHeaderModel[]>([]);

    useEffect(() => {
        if (!uuid) return;
        fetch(`/api${getPageHeaderModelListURL.startsWith('/') ? '' : '/'}${getPageHeaderModelListURL}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ uuid, baseUUID }),
        })
            .then(r => r.json())
            .then(json => setModels((json.content ?? []) as PageHeaderModel[]))
            .catch(() => setModels([]));
    }, [uuid, baseUUID, getPageHeaderModelListURL]);

    const segments: RenderedSegment[] = models
        .map(model => {
            const cfg = pageHeaderConfig.find(c => c.nodeInstId === model.nodeInstId);
            if (!cfg) return null;
            const titlePrefix = labelObject && cfg.pageTitlePath
                ? fetchObjValueByPath(labelObject, cfg.pageTitlePath) as string | undefined
                : undefined;
            const titleVar = record && cfg.pageTitleVarPath
                ? fetchObjValueByPath(record, cfg.pageTitleVarPath) as string | undefined
                : undefined;
            const displayVar = titleVar ?? model.headerName ?? '';
            const pageTitle = displayVar ? `${titlePrefix ?? ''}:${displayVar}` : (titlePrefix ?? '');
            const routePath = cfg.baseEditUrl.replace(':uuid', model.uuid);
            const pageLink = cfg.targetTab ? `${routePath}?tab=${cfg.targetTab}` : routePath;
            return { pageLink, pageTitle, active: cfg.active };
        })
        .filter((s): s is RenderedSegment => s !== null);

    if (!segments.length) return null;

    return (
        <div className="row">
            <div className="col-sm-12">
                <div className="page-title-box">
                    <ol className="breadcrumb pull-left">
                        {segments.map((seg, i) => (
                            <li key={i} className={seg.active ? 'active' : ''}>
                                {seg.active
                                    ? <span>{seg.pageTitle}</span>
                                    : <Link to={seg.pageLink}>{seg.pageTitle}</Link>}
                            </li>
                        ))}
                    </ol>
                    <h4 className="page-title">|</h4>
                </div>
            </div>
        </div>
    );
};

export default PageHeaderBreadcrumb;
```

#### A.3 — Wire into `AsyncEditorPage`

Modify `AsyncEditorPage.tsx` — before the `<AsyncPage>` render, when `pageMeta?.pageHeaderConfig` is set, render `<PageHeaderBreadcrumb>`:

```tsx
{pageMeta?.pageHeaderConfig && pageMeta?.getPageHeaderModelListURL && (
    <PageHeaderBreadcrumb
        pageHeaderConfig={pageMeta.pageHeaderConfig}
        getPageHeaderModelListURL={pageMeta.getPageHeaderModelListURL}
        uuid={pageMeta.parentContent?.[?].uuid}       // current item UUID
        baseUUID={pageMeta.parentContent?.[?].parentDocumentUUID}  // parent doc UUID
        labelObject={pageMeta.labelObject}
        record={pageMeta.parentContent}
    />
)}
<AsyncPage pageMeta={pageMeta} … />
```

Exact uuid/baseUUID paths depend on each ServiceUIModel shape — surface them via a controller method `getBreadcrumbContext()` returning `{ uuid, baseUUID }` so the page doesn't need to know the shape.

#### A.4 — SCSS

The legacy breadcrumb uses Bootstrap 3 default styles which we already have in the Minton bundle. Verify `.breadcrumb`, `.breadcrumb > li + li:before`, `.breadcrumb > .active` render correctly. If not, add to `overrides.scss`:

```scss
// Mirrors Bootstrap 3 breadcrumb defaults for the page header
.page-title-box .breadcrumb {
    padding: 0;
    margin: 0;
    background: transparent;
    list-style: none;
    display: inline-block;

    > li { display: inline-block; }
    > li + li:before {
        content: "/";
        padding: 0 6px;
        color: #ccc;
    }
    > .active { color: #98a6ad; }
    a { color: #0854a1; }
}
.page-title-box .page-title {
    display: inline-block;
    margin: 0 0 0 6px;
    color: #98a6ad;
}
```

---

### Phase B — First worked example: PurchaseContractMaterialItemEditor [~30 min]

#### B.1 — Verify backend endpoint

Check `IntelligentPlatform` for `PurchaseContractMaterialItemEditorController.getPageHeaderModelList()`:
- Should exist based on legacy patterns
- If missing: quick Java-side add — return `[{ nodeInstId: "purchaseContract", uuid: parentUUID }, { nodeInstId: "purchaseContractMaterialItem", uuid: itemUUID }]`

#### B.2 — Wire in the controller

In `PurchaseContractMaterialItemController.tsx` `getDefaultPageMeta()`:

```ts
{
    getPageHeaderModelListURL: 'purchaseContractMaterialItem/getPageHeaderModelList',
    pageHeaderConfig: [
        {
            nodeInstId:       'purchaseContract',
            baseEditUrl:      '/logistics/purchaseContract/:uuid/edit',
            targetTab:        'items',   // scroll to material items tab
            pageTitlePath:    'purchaseContractPageTitle',
            pageTitleVarPath: 'parentDocId',
        },
        {
            active:           true,
            nodeInstId:       'purchaseContractMaterialItem',
            baseEditUrl:      '/logistics/purchaseContract/:baseUUID/item/:uuid/edit',
            pageTitlePath:    'pageHeaderTitle',
            pageTitleVarPath: 'refMaterialSKUId',
        },
    ],
    // …rest of the tabMetaList…
}
```

**Note on paths:** `pageTitleVarPath` values are top-level paths on the loaded record (which is `PurchaseContractMaterialItemUIModel` directly — not wrapped). The legacy `vm.content` object was the flat item UIModel, matching this shape exactly.

#### B.3 — i18n keys

Add to `src/i18n/locales/en/logistics/PurchaseContract.json` and `zh/`:
```json
"purchaseContractPageTitle":    "采购合同",  // "Purchase Contract"
"pageHeaderTitle":              "采购物料"   // "Purchase Material"
```

(The exact keys already exist in the legacy `.properties` files — port them.)

#### B.4 — Verify

- Open `/logistics/purchaseContract/<contractUUID>/item/<itemUUID>/edit`
- Breadcrumb reads `采购合同:PU202503100001 / 采购物料:BC008A03 |`
- Click `采购合同:PU202503100001` → navigates to the parent contract editor, `items` tab active

---

### Phase C — Roll out to remaining item editors [~15 min each]

Repeat the Phase B pattern for each item-editor page. Full list:

**Supply chain**
- InboundItemEditor
- OutboundItemEditor  
- InventoryTransferItemEditor
- InventoryCheckItemEditor
- InquiryMaterialItemEditor
- PurchaseRequestMaterialItemEditor
- PurchaseReturnMaterialItemEditor
- QualityInspectMatItemEditor
- QualityInspectPropertyItemEditor
- WarehouseStoreItemEditor
- WarehouseStoreItemLogEditor
- WasteProcessMaterialItemEditor

**Sales distribution**
- SalesContractMaterialItemEditor
- SalesReturnMaterialItemEditor
- SalesForcastMaterialItemEditor

**System resource**
- NavigationItemSettingEditor
- ServiceDocActConfigureItemEditor

Each roll-out:
1. Verify backend endpoint exists (or add it)
2. Copy the two-segment `pageHeaderConfig` template, adjust `nodeInstId`, `baseEditUrl`, i18n paths
3. Add the two i18n keys to that manager's locale files

---

### Phase D — Non-item editors [optional, defer]

The legacy also uses `pageHeaderConfig` on ~30 non-item single-record editors (system settings, sub-configs, etc.). These follow the same pattern with different route shapes. Migrate on demand — same framework applies without changes.

---

## 6. Files to create / modify

| File | Change | Phase |
|---|---|---|
| `src/controllers/PageMetaTypes.ts` | Add `PageHeaderConfigSegment` type + `pageHeaderConfig?` + `getPageHeaderModelListURL?` on `PageMeta` | A.1 |
| `src/components/page/PageHeaderBreadcrumb.tsx` | New component (~80 LOC) | A.2 |
| `src/components/page/AsyncEditorPage.tsx` | Insert `<PageHeaderBreadcrumb>` above `<AsyncPage>` when config present | A.3 |
| `src/styles/overrides.scss` | Verify/add `.page-title-box .breadcrumb` styles | A.4 |
| `src/controllers/ServiceItemEditController.ts` (or similar) | Add optional `getBreadcrumbContext()` method returning `{ uuid, baseUUID }` | A.3 |
| `PurchaseContractMaterialItemController.tsx` | Add `pageHeaderConfig` + `getPageHeaderModelListURL` in `getDefaultPageMeta()` | B.2 |
| `src/i18n/locales/*/logistics/PurchaseContract.json` | Add i18n keys | B.3 |
| Each remaining `*ItemController.tsx` | Same as B.2 | C |

---

## 7. Risks

| Risk | Mitigation |
|---|---|
| Backend `getPageHeaderModelList` endpoints don't exist for all item types | Add per-type as Phase B/C proceeds; treat missing 404 as graceful degradation (breadcrumb hides) |
| Route path convention differs across modules | Standardise on `/<module>/<parentType>/:baseUUID/item/:uuid/edit` — mirrors existing new-UI patterns |
| Legacy `targetTab` values (numeric indices) don't map to new UI's string tab keys | Manually translate per doc type — small enumerable set |
| Item pages currently use `AsyncEditorPage` not `AsyncItemEditor` | Wire the breadcrumb into `AsyncEditorPage` (universal) — cleaner than reviving the stub `AsyncItemEditor` |
| Title interpolation `label + ':' + var` returns literal `undefined:undefined` when paths miss | The component guards with `??` fallbacks and hides the segment entirely if title resolves to empty |

---

## 8. Estimated effort

| Phase | Effort |
|---|---|
| A — framework | ~2 hours |
| B — first worked example (PurchaseContract) | ~30 min |
| C — remaining 15 item editors | ~15 min each = ~4 hours total |
| D — non-item editors | Defer / on-demand |

Total for full item-editor coverage: ~6.5 hours.
