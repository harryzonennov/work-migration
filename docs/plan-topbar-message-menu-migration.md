# Migration Plan — Topbar System Message Menu

## What it is

The legacy topbar shows up to 3 icon buttons (INFO / WARN / ERROR) with dropdown menus. Each button appears only when its level has messages. Clicking an item navigates to the affected document.

---

## Legacy implementation summary

### Vue component: `MessageMenu` (`LoadNavigation.js` line 361)

- Lives in the topbar, rendered as a `<message-menu>` custom element.
- On `created`, calls `loadUserMessageService.html` (GET) to fetch the message list, and fetches the navigation item list in parallel.
- Maintains an internal array of 3 buckets — one per level (INFO=1, WARN=2, ERROR=3).
- Each `MessageTemplateResponse` item is sorted into the matching bucket. A bucket is **hidden** (`displayflag = false`) until it has at least one message with a non-empty `rawSEList`.
- Badge shows `num` (count of messages in that level).
- Clicking a dropdown item calls `navigateToTarget(messageResponse)` → opens `messageResponse.navigationUrl` in a new tab.
- No polling — loaded once on mount.
- No dismiss / mark-as-read in the legacy UI.

### Level → icon + color mapping

| Level code | Label | Legacy icon | New icon (FA5) | Color |
|---|---|---|---|---|
| INFO = 1 | 通知 / Notification | `nmd nmd-alarm` | `fas fa-bell` | blue (`content-lightblue`) |
| WARN = 2 | 警告 / Warning | `nmd nmd-remove-circle` | `fas fa-exclamation-triangle` | orange |
| ERROR = 3 | 错误 / Error | `nmd nmd-cancel` | `fas fa-times-circle` | red |

---

## Backend API (already migrated, ready to use)

**Endpoint**: `GET /api/v1/logonUser/loadUserMessageService`  
*(maps to `LogonUserEditorController.loadUserMessageService()` — path prefix `/logonUser`)*

**Response shape** (`content` field is `List<MessageTemplateResponse>`):
```json
{
  "content": [
    {
      "templateUUID": "...",
      "templateId": "WAHOUSE_SAFE_WARN",
      "templateName": "仓储安全警告",
      "messageTitle": "仓储安全库存不足",
      "navigationSourceId": "WAREHOUSE_STORE_LIST",
      "actionCode": "view",
      "documentType": 12,
      "documentTypeValue": "WarehouseStore",
      "messageContent": "...",
      "messageLevelCode": 2,
      "messageLevelCodeValue": "WARN",
      "dataNum": 3,
      "rawSEList": [
        { "uuid": "...", "name": "...", ... }
      ]
    }
  ]
}
```

**Key fields used by the UI:**
- `messageLevelCode` — 1/2/3 → which bucket (INFO/WARN/ERROR)
- `messageTitle` — dropdown item label
- `dataNum` — count shown on the right of each dropdown item
- `rawSEList` — if empty/null, item is suppressed (not shown)
- `navigationSourceId` — used to resolve the target URL (in legacy via a separate navigation list; in new UI, map to a React Router path)
- `actionCode` — appended to the target URL as `?actionCode=...`

---

## Navigation source ID → React Router path mapping

The legacy uses a separate navigation API to resolve `navigationSourceId` to a URL. In the new SPA, we hardcode a lookup table (same pattern as the nav group JSON files):

```ts
// src/services/messageNavigationMap.ts
export const MESSAGE_NAVIGATION_MAP: Record<string, string> = {
  WAREHOUSE_STORE_LIST:         '/logistics/warehouseStore',
  INBOUND_DELIVERY_LIST:        '/logistics/inboundDelivery',
  OUTBOUND_DELIVERY_LIST:       '/logistics/outboundDelivery',
  PURCHASE_CONTRACT_LIST:       '/logistics/purchaseContract',
  PURCHASE_REQUEST_LIST:        '/logistics/purchaseRequest',
  FIN_ACCOUNT_LIST:             '/finance/finAccount',
  // add more as pages are migrated
};
```

When a `navigationSourceId` has no entry in the map (page not yet migrated), suppress the item (don't render it) or show it as disabled.

---

## Migration plan

### Step 1 — API service

Create `src/api/userMessageApi.ts`:

```ts
export interface MessageTemplateResponse {
  templateUUID: string;
  templateId: string;
  templateName: string;
  messageTitle: string;
  navigationSourceId: string;
  actionCode?: string;
  documentType: number;
  documentTypeValue: string;
  messageContent: string;
  messageLevelCode: number;      // 1=INFO, 2=WARN, 3=ERROR
  messageLevelCodeValue: string;
  dataNum: number;
  rawSEList: Array<{ uuid: string; name?: string }>;
}

export interface UserMessageListResponse {
  content: MessageTemplateResponse[];
}

export const userMessageApi = {
  loadUserMessages: (): Promise<UserMessageListResponse> =>
    axiosInstance.get('/logonUser/loadUserMessageService'),
};
```

### Step 2 — Navigation map

Create `src/services/messageNavigationMap.ts` — the `navigationSourceId` → React Router path lookup table (see above). Grows as pages are migrated.

### Step 3 — MessageMenuBucket component

Create `src/layouts/TopBar/MessageMenuBucket.tsx`:

- Props: `level` (1/2/3), `messages: MessageTemplateResponse[]`
- Renders a single Ant Design `Dropdown` button.
- Hidden if `messages` is empty.
- Badge shows `messages.length`.
- Dropdown items: `messageTitle` on the left, `dataNum` count badge on the right.
- Click: resolve path from `messageNavigationMap`, call `navigate(path + '?actionCode=' + actionCode)` or open in new tab.

Icon/color per level:
```ts
const LEVEL_CONFIG = {
  1: { icon: 'fas fa-bell',                  color: '#0073ea', title: 'Notifications' },
  2: { icon: 'fas fa-exclamation-triangle',  color: '#f7b731', title: 'Warnings' },
  3: { icon: 'fas fa-times-circle',          color: '#fc4b6c', title: 'Errors' },
};
```

### Step 4 — TopBar integration

In `MainLayout.tsx`:
- Fetch messages once on mount via `userMessageApi.loadUserMessages()`.
- Split the flat list into 3 buckets by `messageLevelCode` (filter out items with empty `rawSEList`).
- Render `<MessageMenuBucket>` for each level inside `.topnav-menu.float-end`.

```tsx
// Fetch once on mount
const [messages, setMessages] = useState<MessageTemplateResponse[]>([]);
useEffect(() => {
  userMessageApi.loadUserMessages()
    .then(r => setMessages((r.content ?? []).filter(m => m.rawSEList?.length > 0)))
    .catch(() => {});
}, []);

const infoMessages  = messages.filter(m => m.messageLevelCode === 1);
const warnMessages  = messages.filter(m => m.messageLevelCode === 2);
const errorMessages = messages.filter(m => m.messageLevelCode === 3);
```

### Step 5 — i18n labels

Add to `en/Menu.json` and `zh/Menu.json`:
```json
"topbar.infoMessages":  "Notifications",
"topbar.warnMessages":  "Warnings",
"topbar.errorMessages": "Errors"
```

---

## Files to create / modify

| Action | File | Purpose |
|---|---|---|
| Create | `src/api/userMessageApi.ts` | API call + `MessageTemplateResponse` type |
| Create | `src/services/messageNavigationMap.ts` | `navigationSourceId` → path lookup |
| Create | `src/layouts/TopBar/MessageMenuBucket.tsx` | Single level button + dropdown |
| Modify | `src/layouts/MainLayout.tsx` | Fetch messages, split by level, render 3 buckets |
| Modify | `src/i18n/locales/en/Menu.json` | Add 3 topbar label keys |
| Modify | `src/i18n/locales/zh/Menu.json` | Add 3 topbar label keys |

---

## Out of scope (not in legacy, not planned)

- Polling / auto-refresh (legacy loads once on mount)
- Mark as read / dismiss
- Pagination in the dropdown
