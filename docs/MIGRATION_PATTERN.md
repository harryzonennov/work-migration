# Frontend Migration Pattern
## Vue 2 + jQuery → React 19 + Ant Design (IntelligentUI)

*Derived from the PurchaseContract first-case migration. Use this as the reference for every subsequent module.*

---

## 1. Source File Inventory (per module)

For each module (e.g., `PurchaseContract`), find these files in the old project:

| Old File | Role | New Equivalent |
|---|---|---|
| `[Entity]Manager.js` | Labels, content defaults, URLs, i18n config, action codes | `src/services/[module]/[Entity]Manager.ts` |
| `[Entity]List.js` | List page Vue instance + `getDefaultPageMeta()` | `src/pages/[module]/[entity]/[Entity]ListController.tsx` |
| `[Entity]Editor.js` | Edit page Vue instance + `getDefaultPageMeta()` | `src/pages/[module]/[entity]/[Entity]EditController.tsx` |
| `[Entity]MtrlItemEditor.js` | Sub-item edit page (if exists) | `src/pages/[module]/[entity]/[Entity]ItemController.tsx` |
| `i18n/[module]/[Entity]_en.properties` | Label keys (unicode-escaped Chinese) | `src/i18n/locales/en/[module]/[Entity].json` + `zh/` |
| `i18n/[module]/[Entity]Item_en.properties` | Item label keys | Merge into the same `[Entity].json` under `[entity]Item.*` |

---

## 2. Output File Checklist

A **complete** module migration produces exactly these files:

### Types
- [ ] `src/types/[module]/[Entity]Content.ts`
  - `[Entity]UIModel` — main header fields (exact backend field names)
  - `[Entity]ServiceUIModel` — wrapper: `{ [entity]UIModel, [party]UIModel[], [entity]ItemUIModelList, serviceUIMeta }`
  - `[Entity]ItemUIModel` — line item fields (if applicable)
  - `[Entity]Status` — TypeScript union of all status integer codes from backend

### Constants / Service Manager
- [ ] `src/services/[module]/[Entity]Manager.ts`
  - `[ENTITY]_STATUS` — integer map `{ INITIAL:1, SUBMITTED:2, ... }`
  - `[ENTITY]_DOC_ACTION` — action code strings matching backend
  - `[ENTITY]_STATUS_BADGE` — Ant Design tag colour map per status
  - `getI18nRootConfig()` / `getI18nItemConfig()` — registers i18n bundles on import

### API
- [ ] `src/api/[entity]Api.ts`
  - `list[Entity](query)` → `POST /api/[entity]/searchTableList.html`
  - `get[Entity](uuid)` → `GET /api/[entity]/loadModuleEdit.html`
  - `create[Entity](data)` → `POST /api/[entity]/saveModuleService.html`
  - `update[Entity](data)` → `POST /api/[entity]/saveModuleService.html` (uuid in body)
  - `delete[Entity](uuid)` → `GET /api/[entity]/deleteModule.html`
  - `executeDocAction(uuid, actionCode)` → `POST /api/[entity]/executeDocAction.html`
  - *(item variants)* `get[Entity]Item`, `create[Entity]Item`, `update[Entity]Item`, `delete[Entity]Item`

### i18n
- [ ] `src/i18n/locales/en/[module]/[Entity].json`
- [ ] `src/i18n/locales/zh/[module]/[Entity].json`
  - Required top-level keys: `pageTitle`, `messages`, `toolbar`, `status`, `[entity].*` field labels, `[entity]Item.*` item labels, `form.priorityOptions` (if applicable)
  - Status keys must match integer codes: `"1": "Initial"`, `"2": "Submitted"`, etc.

### Controllers
- [ ] `src/pages/[module]/[entity]/[Entity]ListController.tsx`
  - Extends `ServiceListController`
  - `getDefaultPageMeta()` returns SEARCH + table column definitions
  - `request()` calls real `list[Entity]()` API
  - Status `valueEnum` uses actual integer codes (not invented strings)
- [ ] `src/pages/[module]/[entity]/[Entity]EditController.tsx`
  - Extends `DocumentEditController`
  - `getDefaultPageMeta()` returns tabs → sections → fields
  - `buildInitialValues()` maps `serviceUIModel` → form fields
  - `buildPayload()` maps form fields → `serviceUIModel`
  - `executeAction(actionCode)` calls real `executeDocAction()` API
  - `getStatus()` returns current doc status integer
  - `getDocumentId()` returns display contract/doc number
- [ ] `src/pages/[module]/[entity]/[Entity]ItemController.tsx` *(if applicable)*

### Pages (thin wrappers)
- [ ] `src/pages/[module]/[entity]/[Entity]ListPage.tsx` — renders `<ListPageShell controller={...} />`
- [ ] `src/pages/[module]/[entity]/[Entity]EditPage.tsx` — renders `<WorkflowToolbar>` + `<DocStatusTag>` + `<EditPageShell controller={...} />`
- [ ] `src/pages/[module]/[entity]/[Entity]ItemEditPage.tsx` *(if applicable)*

### Router
- [ ] Routes added to `src/router/index.tsx`
  - List: `/{module}/{entities}` e.g. `/logistics/purchaseContracts`
  - Edit: `/{module}/{entities}/:uuid`
  - Item edit: `/{module}/{entities}/:uuid/items/:itemUuid`

---

## 3. Migration Steps (in order)

```
Step 1 — Read source files
  Read [Entity]Manager.js       → extract: content defaults, action codes, URL prefixes
  Read [Entity]Editor.js        → extract: getDefaultPageMeta(), data fields, lifecycle overrides
  Read [Entity]List.js          → extract: getDefaultPageMeta() for list, search fields
  Read [Entity]_en.properties   → decode unicode escapes → JSON

Step 2 — Catalogue backend endpoints
  From [Entity]Manager.js URLs + old backend controller:
  List all endpoints, HTTP methods, request/response field names
  → flag any that are not yet in [entity]Api.ts

Step 3 — Create types ([Entity]Content.ts)
  Copy exact field names from old [Entity]UIModel.java or content defaults in Manager.js
  Do NOT rename fields — backend uses camelCase as-is

Step 4 — Create service manager ([Entity]Manager.ts)
  Copy status codes, action codes from old Manager.js
  Add i18n bundle registration

Step 5 — Decode + write i18n JSON
  python decode script → en/[module]/[Entity].json + zh/ equivalent
  Verify: status keys, priorityOptions, all form field keys present

Step 6 — Write API file ([entity]Api.ts)
  Implement ALL 6 core functions (list, get, create, update, delete, executeDocAction)
  Map to exact legacy URL patterns

Step 7 — Write list controller
  Translate getDefaultPageMeta() from List.js → new PageMeta format
  Wire request() to real API

Step 8 — Write edit controller
  Translate getDefaultPageMeta() from Editor.js → tabs/sections/fields
  Implement buildInitialValues(), buildPayload(), executeAction()

Step 9 — Write page wrappers
  ListPage.tsx, EditPage.tsx — wire WorkflowToolbar + DocStatusTag

Step 10 — Add routes

Step 11 — Self-check against Output File Checklist above
  Every checkbox must be ticked before the module is "done"
```

---

## 4. Common Mistakes to Avoid

| Mistake | Rule |
|---|---|
| Renaming backend fields | Never — `signDate` stays `signDate`, not `signingDate` |
| Inventing status codes | Read from old Manager.js or backend controller — do not guess |
| Hardcoding i18n strings | Every user-visible string goes in locale JSON, referenced via `t()` |
| Skipping `executeDocAction` | Workflow buttons are useless without real API call — implement it in Step 6 |
| Skipping `exitEditor` call | Call `exitEditor` endpoint on unmount to release pessimistic lock |
| Creating API file but leaving stubs | All 6 core API functions must call real endpoints, not `console.log` |
| Merging item fields into header type | `[Entity]UIModel` ≠ `[Entity]ItemUIModel` — keep separate |
| Using string status in `valueEnum` | Use integer codes: `{ 1: 'Initial' }` not `{ INITIAL: 'Initial' }` |

---

## 5. Self-Check Before Marking "Done"

Run through this before saying a module is complete:

- [ ] All fields in `[Entity]UIModel` match exact backend JSON field names
- [ ] `[entity]Api.ts` has all 6 core functions (list, get, create, update, delete, executeDocAction)
- [ ] No `console.log` or `// TODO` in API or controller files
- [ ] i18n JSON has keys for every field label, every status code, every tab/section title
- [ ] `WorkflowToolbar` is wired in EditPage
- [ ] `DocStatusTag` + document ID displayed in EditPage header
- [ ] Routes registered
- [ ] `exitEditor` called on page unmount

---

## 6. Key Design Decisions (do not revisit)

These were decided during the PurchaseContract case and apply to all modules:

1. **`getDefaultPageMeta()` pattern is preserved** — business controllers define form layout the same way as old JS files. The adapter layer (`ServiceEditController.convertPageMetaToSectionsJson()`) handles the translation. Do not move layout logic into the framework layer.

2. **Status integers, not strings** — the backend sends integer codes. Use integers everywhere. String aliases are only for display via i18n.

3. **`ServiceUIModel` wrapper pattern** — every edit API response wraps the main model: `{ [entity]UIModel, [party]UIModel, [entity]ItemUIModelList, serviceUIMeta }`. Mirror this in types and `buildInitialValues()`.

4. **Party info via `CUSTOMERCONTACT` section** — supplier and buying-org panels use the `SectionCategory.CUSTOMERCONTACT` type. No individual field keys needed in the entity's i18n file — keys come from `DocInvolveParty.json`.

5. **Material items NOT in `content`** — line items are loaded separately via `EditableProTable` / item edit page. They are merged into the payload in `buildPayload()` via `dataSource`.

6. **Pessimistic locking** — `loadModuleEditService` acquires a lock. `exitEditor` must be called on unmount. `loadModuleViewService` is read-only (no lock).

7. **i18n namespace per module** — `[Entity]Manager.ts` calls `i18n.addResourceBundle()` on import. Foundation namespaces (`commonElements`, `docActionNode`, `docInvolveParty`, `docFlowNode`, `docMatItem`) are shared and already registered.
