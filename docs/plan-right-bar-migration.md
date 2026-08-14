# Plan: Migrate the legacy "Right Bar" panel to IntelligentUI

> **Status:** Phase A ✅ DONE · Phase B framework ✅ DONE · Phase B content + Phase C in progress
> **Created:** 2026-06-30 · **Last updated:** 2026-07-03
> **Companion docs:** [`plan-bootstrap-integration.md`](./plan-bootstrap-integration.md), [`MINTON_CUSTOMIZATION_GUIDE.md`](./MINTON_CUSTOMIZATION_GUIDE.md), [`MIGRATION_CONVERSATIONS.md`](./MIGRATION_CONVERSATIONS.md)

---

## 1. What is the Right Bar?

A toggleable side panel that appears on the right edge of every document editor page.
Two tabs in the primary variant (`RightBarTimeline`, used by all AsyncPage-based document editors):

| Tab | Purpose | Data source |
|---|---|---|
| **Tab 1 — Action Log** (操作日志) | Chronological feed of every workflow action taken on the document (submit, approve, reject, etc.), plus any pending workflow tasks. | Backend: `GET /api/v1/<module>/<docType>/getDocActionNodeList?uuid=<uuid>` — already exists in `IntelligentPlatform` for every doc type. |
| **Tab 2 — Instructions** (说明书) | Inline field-level help: one section per field/group, with plain text paragraphs and status-code explanations. | Static JSON files: `i18n/supplyChain/PurchaseContractHelpDocument_en.json` etc. Already exists in the legacy UI. No backend call. |

---

## 2. Legacy pipeline — how it actually works

> Research completed 2026-07-03. Full findings in MIGRATION_CONVERSATIONS.md.

### 2.1 Tab 1 — Action Log data pipeline

```
PurchaseContractEditor.js  →  getDefaultPageMeta() sets:
  pageMeta.getDocActionNodeListURL = '../purchaseContract/getDocActionNodeList.html'

AsyncPage.initHelpDocumentWithAction(uuid)
  └─ ServiceRightBarPanelHelper.initHelpDocumentWithAction(oSettings)
       └─ ActionCodeTab.loadActionList({ actionCodeListUrl, uuid })
            ├─ GET  <getDocActionNodeListURL>?uuid=<uuid>
            │       → { content: [DocActionNodeUIModel, ...] }
            │
            └─ POST ../serviceFlowRuntime/getInvolveTaskList.html
                    body: { documentType, uuid }
                    → { content: [pendingTask, ...] }
                    (prepends pending task as synthetic first entry if present)

Result: vm.$set(cache.helpDocument, 'actionCodeList', mergedList)
```

**`DocActionNodeUIModel` fields** (already defined in `IntelligentPlatform`):
`processIndex`, `docActionCode`, `docActionCodeLabel`, `executionTime`,
`executedByUUID`, `executedByUserName`, `executedByUserId`,
`documentType`, `documentTypeValue`, `documentId`, `documentName`,
`documentStatus`, `documentStatusValue`, `refDocMatItemUUID`, `refDocumentUUID`, `flatNodeSwitch`

**Backend endpoint already exists** in `IntelligentPlatform` at:
```
GET /purchaseContract/getDocActionNodeList?uuid=<uuid>
```
(line 95 of `PurchaseContractEditorController.java` — pattern repeated for all doc types).

The legacy `serviceFlowRuntime/getInvolveTaskList` is workflow-engine specific — **not needed** in IntelligentPlatform at this stage.

### 2.2 Tab 2 — Instructions data pipeline

```
PurchaseContractEditor.js → getDefaultPageMeta() sets:
  pageMeta.i18nPath           = 'supplyChain/'
  pageMeta.helpDocumentName   = ['PurchaseContractHelpDocument',
                                  'PurchaseContractMaterialItemHelpDocument']

AsyncPage.initHelpDocumentWithAction(uuid)
  └─ ServiceRightBarPanelHelper._initHelpDocumentCore(oSettings)
       └─ DocumentLineTab.loadI18nDocument({ name, path, language })
            └─ $.getJSON('i18n/supplyChain/PurchaseContractHelpDocument_en.json')
               $.getJSON('i18n/supplyChain/PurchaseContractMaterialItemHelpDocument_en.json')
               (merged via ServiceUtilityHelper.mergeToObject)

Result: vm.$set(cache.helpDocument, 'documentList',
          DocumentLineTab.mergeToFieldDocument(rawJson, label, selectMeta))
```

**Static JSON shape** (one file per doc type, already exists for all legacy doc types):
```json
{
  "fieldConfiguration": {
    "purchaseContract.status": {
      "metaParas": { "1": { "text": "初始状态" }, "299": { "text": "已提交审核" }, ... }
    },
    "purchaseContract.signDate": {
      "paras": [{ "text": "合同签订日期" }]
    },
    "purchaseContract.purchaseContractSection": {
      "paras": [{ "text": "采购合同用于记录..." }, { "text": "包含..." }]
    }
  }
}
```

**`metaParas`** = status-code explanations (keyed by code value `"1"`, `"2"`, `"299"`, etc.)
**`paras`** = plain text paragraphs for a field or section

### 2.3 Fields with `helpKey` (drives selectMeta resolution for `metaParas`)

In the legacy, fields that have `metaParas` in the JSON also need their status-code labels resolved
(e.g. code `1` → `"草稿"`, `299` → `"已提交"`). The legacy does this via `getMetaDataUrl` calls
per field. In IntelligentPlatform, status code maps are already available via existing endpoints
(e.g. `GET /purchaseContract/getStatusMap`).

---

## 3. The gap — what's missing in IntelligentUI

| Gap | Description |
|---|---|
| **Tab 1 service layer** | No API call to `getDocActionNodeList` — `ActionLogTab.tsx` is a stub |
| **Tab 1 TS types** | `ActionLogItem` (mirrors `DocActionNodeUIModel`) not defined |
| **Tab 1 rendering** | `ActionLogTab.tsx` renders only `<Empty>` placeholder |
| **Tab 2 JSON files** | Legacy JSON files exist but not ported to `src/i18n/` structure |
| **Tab 2 service layer** | No loader for the help JSON files |
| **Tab 2 content wiring** | No editor page calls `setHelpList()` |
| **Tab 2 `metaParas` resolution** | Status labels need to be resolved from existing map endpoints |
| **`activeKey` / scroll-to** | No equivalent of `setActiveKey()` → section highlight + scroll |

---

## 4. Revised Phased Plan

### Phase A — Chrome ✅ DONE (2026-07-02)

Toggle button, drawer shell, two placeholder tabs. No data. Already shipped.

---

### Phase B — Tab 2: Instructions ← **CURRENT PHASE**

Two sub-steps:

#### B.1 — Port the static help JSON files

The legacy JSON files already exist. Convert them to the new `src/i18n/` structure.

**Source:** `/Users/I043125/work/ThorSalesDistributionUI/admin/i18n/<module>/`
**Target:** `/Users/I043125/work2/IntelligentUI/src/i18n/help/<module>/`

File naming: keep the same base name, drop the `_en` / `_zh` suffix — store as
`PurchaseContractHelpDocument.json` (the UI picks content for the active language, fallback to zh).

These files need **no structural change** — they are already clean JSON with `fieldConfiguration`.

**For PurchaseContract (worked example):**
- `PurchaseContractHelpDocument_en.json` → `src/i18n/help/supplyChain/PurchaseContractHelpDocument.json`
- `PurchaseContractMaterialItemHelpDocument_en.json` → `src/i18n/help/supplyChain/PurchaseContractMaterialItemHelpDocument.json`

#### B.2 — `HelpDocumentService` — static JSON loader

New file: `src/services/HelpDocumentService.ts`

```ts
// src/services/HelpDocumentService.ts

export interface HelpParagraph {
  text: string;
}

export interface HelpFieldConfig {
  paras?: HelpParagraph[];
  metaParas?: Record<string, HelpParagraph>;  // keyed by status code string
}

export interface HelpDocument {
  fieldConfiguration: Record<string, HelpFieldConfig>;
}

export async function loadHelpDocument(names: string | string[], module: string): Promise<HelpDocument> {
  const nameList = Array.isArray(names) ? names : [names];
  const docs = await Promise.all(
    nameList.map(name =>
      fetch(`/i18n/help/${module}/${name}.json`).then(r => r.json() as Promise<HelpDocument>)
    )
  );
  // merge fieldConfiguration objects (same as legacy mergeToObject)
  return docs.reduce((merged, doc) => ({
    fieldConfiguration: { ...merged.fieldConfiguration, ...doc.fieldConfiguration }
  }));
}
```

#### B.3 — `buildHelpList()` — transforms JSON into `ElementHelpItem[]`

Add to `HelpDocumentService.ts`:

```ts
import type { ElementHelpItem } from '@/layouts/RightSideBar/types';

// labelObject: the i18n t() function resolved for the doc's namespace
// statusLabelMap: keyed by helpKey → Record<code, label>
//   e.g. { 'purchaseContract.status': { '1': '草稿', '299': '已提交' } }
export function buildHelpList(
  doc: HelpDocument,
  labelObject: (key: string) => string,
  statusLabelMap: Record<string, Record<string, string>> = {}
): ElementHelpItem[] {
  return Object.entries(doc.fieldConfiguration).map(([key, config]) => {
    const paragraphs = [];

    if (config.paras) {
      for (const para of config.paras) {
        paragraphs.push({ body: para.text });
      }
    }

    if (config.metaParas) {
      const labelMap = statusLabelMap[key] ?? {};
      for (const [code, para] of Object.entries(config.metaParas)) {
        paragraphs.push({
          statusLabel: labelMap[code] ?? code,
          body: para.text,
        });
      }
    }

    return {
      elementId: key,
      title: labelObject(key),
      paragraphs,
    };
  });
}
```

#### B.4 — Wire into `PurchaseContractEditPage.tsx` (worked example)

```tsx
import { useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useRightBarContent } from '@/layouts/RightSideBar/RightBarContext';
import { loadHelpDocument, buildHelpList } from '@/services/HelpDocumentService';

// Inside the component:
const { setHelpList } = useRightBarContent();
const { t } = useTranslation('supplyChain/PurchaseContract');

useEffect(() => {
  loadHelpDocument(
    ['PurchaseContractHelpDocument', 'PurchaseContractMaterialItemHelpDocument'],
    'supplyChain'
  ).then(doc => {
    setHelpList(buildHelpList(doc, key => t(key)));
  });
  return () => setHelpList([]);
}, []);
```

**For status-code labels:** pass the status map as `statusLabelMap` if you want code → readable label.
For now, passing an empty map renders raw codes — that's acceptable for Phase B.1 delivery;
status label resolution is B.5.

#### B.5 — Status label resolution for `metaParas` (optional, can follow B.4)

Call the existing `GET /purchaseContract/getStatusMap` endpoint and pass the result
into `buildHelpList` as `statusLabelMap['purchaseContract.status']`. One `useEffect` fetch.

---

### Phase C — Tab 1: Action Log ← **next after B**

The backend endpoint `GET /purchaseContract/getDocActionNodeList?uuid=<uuid>` already exists.
No Java work needed — this was the only blocker, and it's cleared.

#### C.1 — Add `ActionLogItem` type to `RightSideBar/types.ts`

```ts
export interface ActionLogItem {
  processIndex: number;
  docActionCode: number;
  docActionCodeLabel: string;
  executionTime: string;
  executedByUUID?: string;
  executedByUserName?: string;
  executedByUserId?: string;
  documentType?: number;
  documentTypeValue?: string;
  documentId?: string;
  documentName?: string;
  note?: string;
}
```

#### C.2 — `ActionLogService.ts` — API caller

```ts
// src/services/ActionLogService.ts
import type { ActionLogItem } from '@/layouts/RightSideBar/types';

export async function fetchActionLog(module: string, docType: string, uuid: string): Promise<ActionLogItem[]> {
  const response = await fetch(`/api/v1/${module}/${docType}/getDocActionNodeList?uuid=${uuid}`);
  const json = await response.json();
  return json.content as ActionLogItem[];
}
```

#### C.3 — Extend `RightBarContext` with action log state

```ts
// In RightBarContext.tsx — add alongside helpList:
docContext: { module: string; docType: string; uuid: string } | null;
setDocContext: (ctx: { module: string; docType: string; uuid: string } | null) => void;
```

Editor pages call `setDocContext({ module: 'logistics', docType: 'purchaseContract', uuid })`.
`ActionLogTab` reads `docContext` and fetches when it changes.

#### C.4 — `ActionLogTab.tsx` — real rendering

Replaces the `<Empty>` stub. Fetches from `ActionLogService`, renders using Minton's `.timeline-3`
markup (same structure as `InstructionsTab`, but keyed by `executionTime` + `docActionCodeLabel`):

```tsx
const ActionLogTab: React.FC = () => {
  const { docContext } = useRightBarContent();
  const [actionLog, setActionLog] = useState<ActionLogItem[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!docContext?.uuid) return;
    setLoading(true);
    fetchActionLog(docContext.module, docContext.docType, docContext.uuid)
      .then(setActionLog)
      .finally(() => setLoading(false));
  }, [docContext?.uuid]);

  if (!docContext?.uuid) return <Empty description="Open a document to see its action log" />;
  if (loading) return <Spin />;
  if (!actionLog.length) return <Empty description="No actions recorded yet" />;

  return (
    <div className="timeline-3">
      {actionLog.map((item, index) => (
        <div className="time-item" key={index}>
          <div className="item-info">
            <div className="item-head p-l-10">
              <label className="popItem-label">{item.docActionCodeLabel}</label>
              <label className="popItem-label" style={{ float: 'right' }}>
                <i className="mdi mdi-restore content-orange" style={{ marginRight: 4 }} />
                {item.executionTime}
              </label>
            </div>
            {item.executedByUserName && (
              <div className="para">
                <div className="p-l-5">
                  <i className="mdi mdi-account-outline content-lightblue" style={{ marginRight: 4 }} />
                  {item.executedByUserId} {item.executedByUserName}
                </div>
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  );
};
```

#### C.5 — Wire `PurchaseContractEditPage.tsx`

```tsx
const { setDocContext } = useRightBarContent();

// After document loads (where uuid is known):
useEffect(() => {
  if (uuid) setDocContext({ module: 'logistics', docType: 'purchaseContract', uuid });
  return () => setDocContext(null);
}, [uuid]);
```

---

### Phase D — `activeKey` / scroll-to (optional polish)

When the user opens the right bar while a field is focused, the matching instructions
section should scroll into view and get a highlight class. This requires:
- A third piece of state in `RightBarContext`: `activeKey: string`
- Editor pages call `setActiveKey(fieldKey)` on field focus
- `InstructionsTab` adds `item-active` CSS class to the matching `id="x_anc${key}"` element
- `RightBarTemplate.openSideBar(tab, key)` equivalent: programmatically switch tab + scroll

Low priority — defer until Phases B and C are verified working.

---

## 5. File inventory

### New files to create

| File | Purpose |
|---|---|
| `src/services/HelpDocumentService.ts` | Load + transform static help JSON into `ElementHelpItem[]` |
| `src/services/ActionLogService.ts` | Fetch action log from backend |
| `src/i18n/help/supplyChain/PurchaseContractHelpDocument.json` | Ported from legacy |
| `src/i18n/help/supplyChain/PurchaseContractMaterialItemHelpDocument.json` | Ported from legacy |

### Files to modify

| File | Change |
|---|---|
| `src/layouts/RightSideBar/types.ts` | Add `ActionLogItem` |
| `src/layouts/RightSideBar/RightBarContext.tsx` | Add `docContext` + `setDocContext` |
| `src/layouts/RightSideBar/ActionLogTab.tsx` | Replace stub with real fetch + render |
| `src/pages/logistics/purchaseContract/PurchaseContractEditPage.tsx` | Wire `setHelpList` + `setDocContext` |

---

## 6. Execution order

```
B.1  Port PurchaseContract help JSON files (10 min)
B.2  Write HelpDocumentService.ts (30 min)
B.3  Wire PurchaseContractEditPage → setHelpList (20 min)
B.4  Verify Instructions tab shows real content

C.1  Add ActionLogItem to types.ts (5 min)
C.2  Write ActionLogService.ts (20 min)
C.3  Extend RightBarContext with docContext (15 min)
C.4  Rewrite ActionLogTab.tsx (30 min)
C.5  Wire PurchaseContractEditPage → setDocContext (15 min)
C.6  Verify Action Log tab shows real data
```

Total: ~2.5 hours for both tabs fully wired for PurchaseContract.
Roll out to other doc types: ~15 min each (copy pattern, adjust names/keys).
