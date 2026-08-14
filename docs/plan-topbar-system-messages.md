# Plan: Topbar System Message Icons Migration

## Overview

Migrate the legacy `MessageMenu` Vue component (in `admin/js/NavigationPanel.js`) to the new
React UI. This renders 3 colored icon buttons in the topbar — Info (blue), Warn (orange),
Error (red) — each with a dropdown list of system messages relevant to the logged-in user's
roles. The backend endpoint **already exists and is fully implemented** in the new backend.

---

## How it works in the legacy UI

### Data flow

```
on page load
  → GET ../logonUser/loadUserMessageService.html
  → backend: LogonUserEditorController.loadUserMessageService()
      1. Get user's roles
      2. Get RoleMessageCategory entries for those roles
      3. For each MessageTemplate UUID, call messageTemplateManager.executeSearchBatch()
      4. Each template searches the DB for matching documents and returns a count + list
  → response: { content: MessageTemplateResponse[] }

MessageTemplateResponse fields:
  templateUUID, templateId, templateName
  messageTitle          — display title for the dropdown item
  navigationSourceId    — nav item ID used to build the target URL
  actionCode            — appended as ?actionCode=X to target URL
  messageLevelCode      — 1=INFO, 2=WARN, 3=ERROR
  messageLevelCodeValue — human-readable label
  dataNum               — number of documents matching this template
  rawSEList             — array of matching ServiceEntityNode (uuid, id, name)
  documentType, documentTypeValue
```

### Rendering

- 3 `<li>` entries in the topbar, one per level (INFO/WARN/ERROR)
- Each is **hidden** if no messages of that level exist (`displayflag`)
- Icon: level-specific colored icon (INFO=blue bell, WARN=orange remove-circle, ERROR=red cancel)
- Badge: count of messages (`num` = total items across all templates at that level)
- Clicking the icon opens a dropdown listing all `MessageTemplateResponse` items for that level
- Each dropdown item shows `messageTitle` + `dataNum` count badge
- Clicking a dropdown item opens `targetUrl + ?actionCode=X` in a new tab

### Message level codes (from `DocumentConstants.js`)

```
INFO  = 1
WARN  = 2
ERROR = 3
```

---

## Backend status in new project

**Endpoint: `POST /api/v1/logonUser/loadUserMessageService`**  
Controller: `platform/controller/LogonUserEditorController.java` — line 203  
Status: **fully implemented** — identical logic to legacy, returns `{ content: MessageTemplateResponse[] }`

`MessageTemplateResponse` shape (same as legacy):
```java
templateUUID, templateId, templateName
messageTitle, navigationSourceId, actionCode
messageLevelCode (int: 1/2/3), messageLevelCodeValue
dataNum (int), rawSEList (List<ServiceEntityNode>)
documentType, documentTypeValue, messageContent
```

---

## Migration plan

### Files to create

| File | Purpose |
|---|---|
| `src/api/messageApi.ts` | API call to `/logonUser/loadUserMessageService` |
| `src/components/TopbarMessages/index.tsx` | The 3-icon topbar component |
| `src/components/TopbarMessages/types.ts` | TypeScript types for the response |

### Files to modify

| File | Change |
|---|---|
| `src/layouts/MainLayout.tsx` | Add `<TopbarMessages />` inside `.topnav-menu` |
| `src/i18n/locales/en/Menu.json` | Add `notifyMessageTitle`, `warnMessageTitle`, `errorMessageTitle` |
| `src/i18n/locales/zh/Menu.json` | Same keys in Chinese |

---

### Step 1 — Types (`src/components/TopbarMessages/types.ts`)

```ts
export interface MessageTemplateResponse {
    templateUUID: string;
    templateId: string;
    templateName: string;
    messageTitle: string;
    navigationSourceId: string;
    actionCode: string;
    messageLevelCode: number;       // 1=INFO 2=WARN 3=ERROR
    messageLevelCodeValue: string;
    dataNum: number;
    rawSEList: Array<{ uuid: string; id: string; name: string }>;
    documentType: number;
    documentTypeValue: string;
    messageContent: string;
}

export const MESSAGE_LEVEL = { INFO: 1, WARN: 2, ERROR: 3 } as const;

export interface MessageLevel {
    levelCode: number;
    iconClass: string;       // FA icon class
    colorClass: string;      // CSS color class
    titleKey: string;        // i18n key for tooltip
    items: MessageTemplateResponse[];
}
```

---

### Step 2 — API (`src/api/messageApi.ts`)

```ts
import { apiClient } from './apiClient';

export function loadUserMessages(): Promise<MessageTemplateResponse[]> {
    return apiClient
        .post('/api/v1/logonUser/loadUserMessageService')
        .then((res) => res.data?.content ?? []);
}
```

---

### Step 3 — Component (`src/components/TopbarMessages/index.tsx`)

**Behaviour:**
- On mount, call `loadUserMessages()` once
- Group responses by `messageLevelCode` into 3 buckets
- Render one `<li>` per level; hide if bucket is empty
- Icon: `fas fa-bell` (INFO/blue), `fas fa-exclamation-triangle` (WARN/orange), `fas fa-times-circle` (ERROR/red)
- Badge: total `dataNum` summed across all items in that bucket
- Dropdown items: show `messageTitle` + `dataNum` count
- Click item: navigate to the route mapped from `navigationSourceId` (see Step 4)

**Navigation from `navigationSourceId`:** The legacy UI used a separate navigation config to map `navigationSourceId` → URL. In the new SPA, map `navigationSourceId` to a React Router path using a simple lookup table (same approach as nav group defaultPath). Start with the known mapped pages; unknown IDs link to `/` with a console warning.

**Refresh:** No polling in the initial implementation — load once on mount. A manual refresh button inside the dropdown can be added later.

---

### Step 4 — Navigation source ID mapping

The legacy `navigationSourceId` values come from the `MessageTemplate` configuration data
in the DB. These are arbitrary string IDs set by the admin. Add a mapping file:

```ts
// src/components/TopbarMessages/navigationSourceMap.ts
export const NAVIGATION_SOURCE_MAP: Record<string, string> = {
    // populated as MessageTemplates are configured in the new system
    // key = navigationSourceId value from MessageTemplate
    // value = React Router path
    'purchaseContract': '/logistics/purchaseContract',
    'purchaseRequest':  '/logistics/purchaseRequest',
    // add more as needed
};
```

---

### Step 5 — Wire into `MainLayout.tsx`

Add `<TopbarMessages />` inside the `<ul class="topnav-menu float-end">` before the
language switcher `<li>`:

```tsx
<ul className="list-unstyled topnav-menu float-end mb-0">
    <li className="d-none d-lg-block">
        <TopbarMessages />
    </li>
    <li className="d-none d-lg-block">
        <LanguageSwitcher />
    </li>
    ...
```

---

### Step 6 — i18n labels

Add to `en/Menu.json`:
```json
"notifyMessageTitle": "Notifications",
"warnMessageTitle":   "Warnings",
"errorMessageTitle":  "Errors"
```

Add to `zh/Menu.json`:
```json
"notifyMessageTitle": "通知",
"warnMessageTitle":   "警告",
"errorMessageTitle":  "错误"
```

---

## Icon mapping (legacy → new, FA5 solid)

| Level | Legacy (`nmd` font) | New (FA5) | Color |
|---|---|---|---|
| INFO | `nmd nmd-alarm` | `fas fa-bell` | `#5bc0de` (blue) |
| WARN | `nmd nmd-remove-circle` | `fas fa-exclamation-triangle` | `#f0ad4e` (orange) |
| ERROR | `nmd nmd-cancel` | `fas fa-times-circle` | `#d9534f` (red) |

---

## What is NOT in scope for this migration

- The `SidePullRightUnion` component (document-level sub-message matrix in the right sidebar) — that is a separate component tied to individual document pages
- Admin configuration of MessageTemplates and RoleMessageCategories — those are backend-admin tasks
- Polling / WebSocket real-time updates — load-on-mount only for now
