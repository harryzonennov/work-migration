# Migration Conversations

A running log of technical questions, discussions, and decisions raised during the migration.
Each entry is a self-contained Q&A or open discussion item.

---

## Project Reference

| Role | Path | Tech |
|---|---|---|
| **Legacy backend — Platform** | `/Users/I043125/work/ThorsteinPlatform/` | Spring 4 + Hibernate 5, Java source root: `src/main/java/platform/` |
| **Legacy backend — Finance** | `/Users/I043125/work/ThorsteinFinance/` | Spring 4 + Hibernate 5, Java source root: `src/main/java/net/thorstein/finance/` |
| **Legacy backend — Logistics** | `/Users/I043125/work/ThorsteinLogistics/` | Spring 4 + Hibernate 5, Java source root: `src/main/java/net/thorstein/logistics/` |
| **Legacy backend — SalesDistribution** | `/Users/I043125/work/ThorsteinSalesDistribution/` | Spring 4 + Hibernate 5, Java source root: `src/main/java/net/thorstein/salesDistribution/` |
| **Legacy backend — Production** | `/Users/I043125/work/ThorsteinProduction/` | Spring 4 + Hibernate 5, Java source root: `src/main/java/net/thorstein/production/` |
| **Legacy UI** | `/Users/I043125/work/ThorSalesDistributionUI/` | jQuery + Vue 2, admin root: `admin/`, JS modules: `admin/js/`, i18n: `admin/i18n/` |
| **New backend** | `/Users/I043125/work2/IntelligentPlatform/` | Spring Boot 3.2 + Hibernate 6 + Java 17, Java source root: `src/main/java/com/company/IntelligentPlatform/` |
| **New UI** | `/Users/I043125/work2/IntelligentUI/` | React 18 + Vite 5 + TypeScript + Ant Design Pro, src root: `src/` |

---

## Conversations

---

### C1

**Topic:** When I need to ask Claude to do checking or updating across the migration projects, how can I automatically grant enough permission to all relevant projects without being interrupted with approval prompts every time? And after updates are done automatically, just provide a summary of what changed — I should not have to type commands manually each time.

**Answer:**

Claude Code's permission system is controlled by `settings.local.json` files placed in the
`.claude/` folder at each project root. Entries in the `"allow"` array are pre-approved and
never prompt for confirmation. The key is to use **broad glob patterns** rather than
listing individual commands.

**How to configure each project**

Each project needs its own `.claude/settings.local.json`. The effective set of permissions
for any session is determined by whichever project root Claude is launched from — so when
working across multiple projects, the migration workspace (`work-migration`) is the right
place to put the broadest grant, since it is the session root most commonly used.

The minimal, safe grant that covers all read/write/search/build operations for this migration:

```json
{
  "permissions": {
    "allow": [
      "Read(**)",
      "Glob(**)",
      "Bash(find:*)",
      "Bash(grep:*)",
      "Bash(git:*)",
      "Bash(ls:*)",
      "Bash(cat:*)",
      "Bash(wc:*)",
      "Bash(head:*)",
      "Bash(sed:*)",
      "Bash(python3:*)",
      "Bash(mvn:*)",
      "Bash(npm:*)",
      "Bash(cp:*)",
      "Bash(mkdir:*)"
    ]
  }
}
```

Write this file to each project root that Claude needs to touch autonomously:

| Project | File to create/update |
|---|---|
| Migration workspace | `/Users/I043125/work-migration/.claude/settings.local.json` |
| New UI | `/Users/I043125/work2/IntelligentUI/.claude/settings.local.json` |
| New backend | `/Users/I043125/work2/IntelligentPlatform/.claude/settings.local.json` |

The legacy projects (`/Users/I043125/work/Thorstein*/`) are read-only reference sources —
add `Read(/Users/I043125/work/**)` and `Bash(grep:*)` only; no write permissions needed there.

**Why not put everything in one file?**

`settings.local.json` is scoped to the directory it lives in. When Claude is launched inside
`/Users/I043125/work2/IntelligentUI/`, only that project's file applies. So each project
needs its own grant. The `work-migration` workspace file also applies when Claude is run from
there and accesses sibling directories via absolute paths.

**The "summarise after done" workflow — guaranteed via CLAUDE.md**

Relying on prompt phrasing alone is fragile — you would need to repeat the instruction every
session. The reliable approach is to put the rule in each project's `CLAUDE.md` so it applies
automatically at every session start without any prompt from you.

The following instruction has been added to the `CLAUDE.md` of all three active projects
(`IntelligentUI`, `IntelligentPlatform`, `java-templates`):

```
## After-Update Summary Rule

After completing any task that modifies, creates, or deletes files, you MUST append a summary
entry to /Users/I043125/work-migration/docs/MIGRATION_CONVERSATIONS.md under a new
#### Update log — <YYYY-MM-DD> sub-heading inside the relevant conversation, OR as a
standalone ### Update — <topic> (<YYYY-MM-DD>) entry if it is not tied to a specific
conversation. The entry must list:

- Files modified  — path + one-line description of what changed
- Files created   — path + purpose
- Files deleted   — path + reason

Only skip this step if the task was pure read/research with no file changes.
```

With this in place, the update list is written into this conversation log automatically —
no special phrasing required from you.

**Remaining interruptions after this setup**

Even with broad `allow` rules, Claude will still pause before:
- Destructive operations (`rm`, `git reset --hard`, `DROP TABLE`)
- Pushing to remote git (`git push`)
- Any command that affects systems outside the local filesystem

This is intentional safety behaviour and cannot be bypassed via `settings.local.json`.
For those operations, a single confirmation per session is the minimum.

**Reference:** Claude Code permission docs — `settings.local.json` reference:
https://docs.anthropic.com/en/docs/claude-code/settings

---

### C2

**Topic:** How are document action buttons (Submit, Approve, etc.) dynamically rendered in the legacy UI via `placeholder: { category: DOC_ACTION_BTN }` in `getDefaultPageMeta()`?

**Answer:**

The placeholder is a two-phase runtime expansion mechanism.

**Phase 1 — backend fetch (on component `created`)**

`SerDocumentControlHelper.initDocActionConfigureList()` GETs `getDocActionConfigureListURL`
(e.g. `purchaseContract/getDocActionConfigureList`). The backend returns an array of
`DocActionConfigure` objects — one per possible workflow action:

```json
[
  { "actionCode": 2, "targetStatus": 2,   "preStatusList": [1, 6], "authorActionCode": "edit"     },
  { "actionCode": 5, "targetStatus": 1,   "preStatusList": [2],    "authorActionCode": "edit"     },
  { "actionCode": 2, "targetStatus": 3,   "preStatusList": [2],    "authorActionCode": "auditDoc" },
  { "actionCode": 6, "targetStatus": 6,   "preStatusList": [2],    "authorActionCode": "auditDoc" },
  { "actionCode": 3, "targetStatus": 1,   "preStatusList": [3],    "authorActionCode": "auditDoc" },
  { "actionCode": 5, "targetStatus": 200, "preStatusList": [3],    "authorActionCode": "edit"     },
  { "actionCode": 6, "targetStatus": 100, "preStatusList": [3,200],"authorActionCode": "edit"     }
]
```

`preStatusList` — document statuses in which the button is visible.
`authorActionCode` — authorization key checked against the logged-in user's permissions.

**Phase 2 — button generation (after promise resolves)**

`ProcessButtonArray.genDocActionButtonMeta()` cross-references the backend list with
the controller's `getActionCodeMatrix()`, which maps human-readable action headers to integer
action codes:

```js
// PurchaseContractEditor.js
{
  submit:        { actionCode: PurchaseContractActionNode.DOC_ACTION_SUBMIT },
  revokeSubmit:  { actionCode: PurchaseContractActionNode.DOC_ACTION_REVOKE_SUBMIT },
  approve:       { actionCode: PurchaseContractActionNode.DOC_ACTION_APPROVE },
  rejectApprove: { actionCode: PurchaseContractActionNode.DOC_ACTION_REJECT_APPROVE },
  countApprove:  { actionCode: PurchaseContractActionNode.DOC_ACTION_COUNTAPPROVE },
  processDone:   { actionCode: PurchaseContractActionNode.DOC_ACTION_PROCESS_DONE },
  deliveryDone:  { actionCode: PurchaseContractActionNode.DOC_ACTION_DELIVERY_DONE,
                   docItemMultiSelectConfig: { ... } },
}
```

For each configure item, the framework: reverse-looks up the action header by `actionCode`,
generates a `formatClass` function (checks `currentStatus ∈ preStatusList` + user auth on
every render), and generates a `callback` function (`executeDocActionCore(header)`).

**New UI equivalent:**

The entire mechanism is replaced by `<WorkflowToolbar>` — a static React component that
computes visible actions from a hardcoded `status → actionCodes` map (mirrors `preStatusList`).
No call to `getDocActionConfigureList` is made; visibility is computed client-side from
`DOC_STATUS` constants. `controller.executeAction(actionCode)` is the single callback.

See `DRAFT_MIGRATION_CONTRACT_PURCHASE_CONTRACT.md § Workflow Action Buttons Pattern` for
the full pipeline diagram and action code table.

---

### C2

**Topic:** How are page elements (tabs, sections, fields) rendered from `getDefaultPageMeta()` — the method returns JSON data, not React elements?

**Answer:**

The edit page layout is driven entirely by the JSON descriptor returned from
`getDefaultPageMeta()`. The page component itself (`PurchaseContractEditPage.tsx`) renders
only a loading gate and `<EditPageShell>` — no tabs, sections, or fields are hard-coded there.

The descriptor goes through four layers before reaching the DOM:

```
1. getDefaultPageMeta()   → PageMeta { tabMetaList, processButtonMeta }
                             Legacy format. Plain objects. i18n keys unresolved.

2. convertPageMetaToSectionsJson()  → EditSectionConfigJson[]
                             Still JSON-serialisable. Keys are *Key strings.
                             Multi-tab: each section gets tab:'basic', tabLabelKey:'...'
                             Single-tab: tab/tabLabelKey omitted → flat card-stack.
                             CUSTOMERCONTACT section → { sectionCategory, fields:[] }
                             EMBEDLIST section       → single items-table field

3. resolveSection()       → EditSectionConfig[]
                             i18n keys → translated strings via i18n.t()
                             iconKey   → React node via ICON_REGISTRY
                             getMetaDataUrl → request: () => loadMetaRequestForSelect(url)

4. EditPageShell          → <ProForm> + <Tabs> + ProFormText / ProFormSelect / ...
                             hasTabs = sections.some(s => s.tab !== undefined)
                             CUSTOMERCONTACT → <InvolvePartySection>
                             items-table     → <EditableProTable>
```

`parentContentPath` on a `SectionConfig` causes all bare field names to be prefixed,
producing nested array paths that ProForm uses to read/write the API envelope:

```
parentContentPath: 'purchaseContractUIModel' + fieldName: 'signDate'
  → name: ['purchaseContractUIModel', 'signDate']
```

See `DRAFT_MIGRATION_CONTRACT_PURCHASE_CONTRACT.md § Page Rendering Pipeline` for the full
layer diagram, tab/flat layout rule, and specialized section category table.

---

### C3

**Topic:** How do backend-driven select options (status, priority dropdowns) work — from `getMetaDataUrl` in `getDefaultPageMeta()` through to the rendered `<Select>`?

**Answer:**

The pipeline mirrors the legacy `AsyncControlElement.loadMetaData()` pattern but uses React/SWR
instead of jQuery. Key points:

1. The controller declares `readonly getStatusURL = 'purchaseContract/getStatusMap'` and
   references it in `getDefaultPageMeta()` via `settings: { getMetaDataUrl: this.getStatusURL }`.

2. `editDescriptorResolver.resolveField()` converts `getMetaDataUrl` into a `request` function:
   `request: () => loadMetaRequestForSelect(url)`.

3. `ProFormSelect` calls `request()` automatically on mount via SWR — no `useEffect` or
   `useState` needed. This mirrors `loadMetaData()` firing on mount in the legacy system.

4. `loadMetaRequestForSelect` (in `ServiceUtilityHelper.ts`) is the single canonical utility
   for all backend-driven selects. It accepts `string | SelectMetaOptions` and handles:
   `{id,text}` mapping, `{content:[...]}` envelope unwrap, model-object mapping
   (`idField`/`textField`/`listSubPath`), `filteredKeyList`, `excludeKeyList`, `addEmptyFlag`.

5. **Spring Boot 3 breaking change:** `.html` suffix URLs (e.g. `getStatusMap.html`) return
   404. All `getMetaDataUrl` values must be written without `.html`.

See `DRAFT_MIGRATION_CONTRACT_PURCHASE_CONTRACT.md § Select Metadata Pattern` for the full
pipeline diagram, feature table, and usage examples.

---

### C4

**Topic:** Where should the `test/` folder live — inside `src/` or at the project root? And should it mirror `src/`'s subdirectory structure?

**Answer:**

Place `test/` at the project root, parallel to `src/`. Mirror `src/`'s subdirectory structure
exactly so that every test file sits at the same relative path as the source file it tests:

```
src/services/ServiceUtilityHelper.ts
→ test/services/loadMetaRequestForSelect.test.ts

src/components/page/EditPageShell.tsx
→ test/components/editPageShell.test.tsx

src/pages/logistics/purchaseContract/PurchaseContractEditPage.tsx
→ test/pages/logistics/purchaseContract/purchaseContractEditPage.test.tsx
```

Rationale: root-level `test/` is the convention for Vite/Vitest projects (matches where
`vite.config.ts` lives, avoids polluting `src/` with non-production files, consistent with
how `vitest` discovers test files by default). Update `vite.config.ts` `setupFiles` to
`['test/setup.ts']` when moving.

---

### C5

**Topic:** Why do `getStatusMap` and `getPriorityMap` backend endpoints return 404 in the new backend when the legacy UI used `.html` suffix URLs?

**Answer:**

Spring Boot 3 / Spring MVC 6 **removed suffix pattern matching**, which was enabled by default
in Spring 4 via `<mvc:annotation-driven>`. In Spring 4, a controller mapped to `/getStatusMap`
also matched `/getStatusMap.html`, `/getStatusMap.json`, etc. In Spring Boot 3 that suffix
behaviour is gone entirely — only the exact path matches.

Fix: strip `.html` from all `get*URL` properties on the controller class. The correct values
for PurchaseContract are:

```ts
readonly getStatusURL      = 'purchaseContract/getStatusMap';
readonly getPriorityCodeURL = 'purchaseContract/getPriorityMap';
```

This applies to **every module** migrated from the legacy system — all `getMetaDataUrl` values,
`getDocActionConfigureListURL`, `executeDocActionURL`, and any other URL property must be
written without `.html`.

---

### C6

**Topic:** How should the migrated `AsyncSection` / `AsyncPage` tsx components be wired into
the PurchaseContract Edit and List pages, replacing the current direct rendering inside
`EditPageShell`? What is skipped, why, and what gaps must be recorded?

**Plan (implementation approach — agreed before coding):**

---

#### Background

The legacy Vue 2 UI renders every page through a hierarchy:

```
AsyncPage
  └─ AsyncSectionFactory  (routes by sectionCategory integer)
       ├─ AsyncEditSection          (EDIT=1)
       ├─ AsyncSearchSection        (SEARCH=4)
       ├─ AsyncEmbeddedListSection  (EMBEDLIST=5)
       ├─ AsyncAttachmentSection    (ATTACHMENT=6)
       └─ AsyncCustomerContactSection (CUSTOMERCONTACT=7)
```

All of these have been **mechanically converted to tsx** and live in
`src/components/page/`. The conversion kept prop shapes and component
structure intact but left Vue 2-only internals as stubs or `// TODO`
comments.

The current new-UI rendering path is completely separate:
`PurchaseContractEditPage` → `EditPageShell` → ProForm / Ant Design Pro
components. `EditPageShell` is a clean, fully working React implementation.
The goal is NOT to discard it, but to **wire the AsyncSection layer on top**
so that the section-dispatch chain (`AsyncPage` → `AsyncSectionFactory` →
`AsyncEditSection` → `AsyncEditUnion` → `AsyncField`) is exercised and
preserved for future modules.

---

#### PageMeta format bridge

`getDefaultPageMeta()` (on `PurchaseContractEditController`) returns a
`PageMeta` object whose structure already mirrors what `AsyncPage` expects:

| `PageMeta` field | `AsyncPage` consumes |
|---|---|
| `tabMetaList[].tabId` | `tabMeta.tabId` |
| `tabMetaList[].tabTitle` | `tabMeta.titleLabelKey` → resolved via `labelObject` |
| `tabMetaList[].sectionMetaList[]` | `tabMeta.sectionMetaList` |
| `sectionMeta.sectionCategory` | routed by `AsyncSectionFactory` |
| `sectionMeta.parentContentPath` | `fetchObjValueByPath(parentContent, path)` |
| `sectionMeta.fieldMetaList` | consumed by `getSectionFieldMetaList()` |
| `sectionMeta.sectionColClass` | `groupRowSectionList()` parses `col-md-N` |

The `PageMeta` object just needs to be passed as the `pageMeta` prop, with
`parentContent` set to the loaded record and `labelObject` set to the
resolved i18n map.

---

#### What works vs. what is a stub

| Component | Status | Decision |
|---|---|---|
| `AsyncPage` | Renders Bootstrap tab layout; `genRamdomPostIndex` falls back to `Math.random()` — acceptable | **Use** |
| `AsyncSectionFactory` | Routes by `sectionCategory` correctly | **Use** |
| `AsyncEditSection` | Portlet + collapse + embedded tabs; `AsyncEditUnion` renders fields | **Use** |
| `AsyncSearchSection` | Thin wrapper around `AsyncEditUnion` | **Use** (list page) |
| `AsyncEmbeddedListSection` | Renders a placeholder `<div>` — `ServiceDataTableFrame` not migrated | **Skip (stub)** — record gap G1 |
| `AsyncAttachmentSection` | Renders placeholder | **Skip (stub)** — record gap G2 |
| `AsyncCustomerContactSection` | Renders placeholder + `console.warn` | **Skip (stub)** — the working `<InvolvePartySection>` must remain the runtime impl; record gap G3 |
| `AsyncEditUnion` | Has bug: calls `AsyncSection.getSectionFieldMetaList()` via undeclared global instead of named import | **Fix before use** — step 1 below |
| `AsyncField` / `AsyncForeField` | Field-level rendering stubs | Use as-is for now (legacy field rendering is a separate migration concern) |
| `genRamdomPostIndex` | Declared as `declare const` in every file but never provided | Leave the `Math.random()` fallback in place — no separate wiring needed |

---

#### Gaps to record

| ID | Gap | Affected component | Consequence |
|---|---|---|---|
| G1 | `ServiceDataTableFrame` not migrated | `AsyncEmbeddedListSection` | Contract items table section renders a placeholder div; keep `EditableProTable` in `EditPageShell` as the working implementation |
| G2 | Attachment upload section is a stub | `AsyncAttachmentSection` | Upload field renders placeholder; file upload remains in `EditPageShell` |
| G3 | `AsyncCustomerContactSection.initLoadControl` not wired | `AsyncCustomerContactSection` | Party-selector placeholder only; `<InvolvePartySection>` remains the working impl |
| G4 | `AsyncPage` process-button-array is `// TODO` | `AsyncPage` | Workflow toolbar (Submit / Approve etc.) is not rendered by `AsyncPage`; stays in `PurchaseContractEditPage` header bar |
| G5 | `AsyncPage` tab navigation uses Bootstrap `data-toggle="tab"` | `AsyncPage` | No actual tab-switching without Bootstrap JS; `EditPageShell` uses Ant Design `<Tabs>` which does work |

---

#### Ordered implementation steps

**Step 1 — Fix `AsyncEditUnion.tsx` import bug**

Replace the `declare const AsyncSection: any` global reference with a
proper named import:

```ts
// Remove:
declare const AsyncSection: any;
// Add:
import { getSectionFieldMetaList } from './AsyncSection';
// Change:
AsyncSection.getSectionFieldMetaList(sectionMeta)
// To:
getSectionFieldMetaList(sectionMeta)
```

**Step 2 — Verify `AsyncPage` compiles cleanly after step 1**

No changes to `AsyncPage.tsx` or `AsyncSectionFactory.tsx` are needed.
The `genRamdomPostIndex` fallback (`Math.random().toString(36)`) is already
in place in both files.

**Step 3 — Assess gap impact on PurchaseContract edit page**

The edit page has three tabs:
- `basic` — EDIT sections (work) + two CUSTOMERCONTACT sections (G3 — stub)
- `details` — EDIT section (works) + ATTACHMENT section (G2 — stub)
- `items` — EMBEDLIST section (G1 — stub)

Conclusion: `AsyncPage` can be wired for tabs and EDIT sections. The stub
sections render harmless placeholder divs. The working field rendering
(`ProForm` inputs) lives in `AsyncField`/`AsyncForeField` which are already
imported by `AsyncEditUnion` — those need to be assessed separately as a
follow-on task.

**Step 4 — Wire `AsyncPage` into `PurchaseContractEditPage`**

Create a `pageMeta` object from the controller's `getDefaultPageMeta()`
output and the loaded record, then render `<AsyncPage pageMeta={pageMeta} />`
alongside (or instead of) `<EditPageShell>`.

Strategy: **render both in parallel initially** so the existing ProForm
rendering keeps working while the AsyncPage layer is verified. Switch to
AsyncPage-only once field rendering through `AsyncField` is confirmed.

**Step 5 — Record gaps G1–G5 in this conversation**

Write the gap table as a permanent record so future developers know which
stubs need migration work.

**Step 6 — List page**

`PurchaseContractListPage` uses `<ListPageShell>`. The AsyncSection
equivalent for list pages is `AsyncSearchSection` (SEARCH=4). Wire
similarly after the edit page is confirmed.

---

#### What is NOT changing

- `EditPageShell.tsx` — kept as the working ProForm renderer; not deleted
- `WorkflowToolbar` in the page header — stays; `AsyncPage` process-button-array is a stub (G4)
- `InvolvePartySection` — stays as the working party-selector; `AsyncCustomerContactSection` is a stub (G3)
- `EditableProTable` for contract items — stays; `AsyncEmbeddedListSection` is a stub (G1)

**Answer:** (plan confirmed — implementation proceeds after this entry)

---

### C7

**Topic:** Since `AsyncEditSection` depends on `AsyncField` and its children
(`AsyncForeField`, `AbsInputEle`, `InputFieldUnion`), should those leaf
components be migrated first before wiring `AsyncPage` into the editor?

**Answer:**

Yes. The leaf components are the single bottleneck — wiring `AsyncPage` on
top without fixing them results in the entire section chain rendering either
nothing or crashing.

**Current state of the field rendering chain**

```
AsyncPage
  └─ AsyncSectionFactory
       └─ AsyncEditSection
            └─ AsyncEditUnion
                 ├─ AsyncForeField → AbsInputEle → InputFieldUnion
                 └─ AsyncField     → AbsInputEle → InputFieldUnion
```

| Component | Problem |
|---|---|
| `AsyncEditUnion` | Calls `AsyncSection.getSectionFieldMetaList()` via undeclared global — throws at runtime |
| `AbsInputEle` | `useState(() => genRamdomPostIndex())` — no fallback guard — **crashes** when `genRamdomPostIndex` is not defined |
| `InputFieldUnion` | Calls `ServiceUtilityHelper.fetchObjValueByPath` as an undeclared global instead of the named import |
| `InputFieldUnion` | All field types render as `<input type="text">` — no select / date / digit / textarea yet |

**Revised implementation order**

1. **Fix `AbsInputEle` crash** — add `?? Math.random().toString(36)` guard to
   the `useState` initialiser (mirrors the pattern already in place in
   `AsyncPage`, `AsyncSectionFactory`, `AsyncEditUnion`)

2. **Fix `InputFieldUnion` import** — replace the `declare const ServiceUtilityHelper`
   global with the named import already available at
   `@/services/ServiceUtilityHelper`

3. **Implement field type registry in `InputFieldUnion`** — replace the
   `<input type="text">` stub with a `switch` on `fieldMeta.fieldType` that
   routes to Ant Design Pro components. The mapping is already known from
   `EditPageShell.tsx`'s `renderField()`:

   | `fieldType` value | Target component |
   |---|---|
   | (none / `'text'`) | `ProFormText` |
   | `'select'` | `ProFormSelect` (+ `request` for backend-driven options) |
   | `'date'` | `ProFormDatePicker` |
   | `'number'` | `ProFormDigit` |
   | `'textarea'` | `ProFormTextArea` |
   | `'upload'` | `ProForm.Item` + `Upload` |

4. **Fix `AsyncEditUnion` import** — replace `AsyncSection.getSectionFieldMetaList`
   global with named import `{ getSectionFieldMetaList }` from `'./AsyncSection'`

5. **Wire `AsyncPage` into `PurchaseContractEditPage`** — once steps 1–4 are
   done the full chain is exercised end-to-end

Steps 1–4 are the "migrate AsyncField children first" work. Step 5 is the
integration. This order ensures every wiring problem is found at the field
level (small, isolated files) before the tab/section layout is added on top.

**Answer:** Migrate leaf components first (steps 1–4), then wire `AsyncPage` (step 5).

---

#### Update log — 2026-04-12

- **Files modified** — `docs/MIGRATION_CONVERSATIONS.md`: C1 answer corrected to replace fragile prompt-phrasing advice with the CLAUDE.md-based guarantee; update log section added
- **Files modified** — `/Users/I043125/work2/IntelligentUI/CLAUDE.md`: added `## After-Update Summary Rule` section
- **Files modified** — `/Users/I043125/work2/IntelligentPlatform/CLAUDE.md`: added `## After-Update Summary Rule` section
- **Files modified** — `/Users/I043125/work-migration/backend/java-templates/CLAUDE.md`: added `## After-Update Summary Rule` section
- **Files modified** — `/Users/I043125/work-migration/.claude/settings.local.json`: replaced 80+ accumulated one-off commands with 15 clean glob patterns
- **Files modified** — `/Users/I043125/work2/IntelligentPlatform/.claude/settings.local.json`: same replacement
- **Files modified** — `/Users/I043125/work2/IntelligentUI/.claude/settings.local.json`: replaced single `mcp__ide__getDiagnostics` entry with 15-rule set

---

#### Update log — 2026-04-13

- **Files modified** — `/Users/I043125/work-migration/docs/MIGRATION_CONVERSATIONS.md`: added C6 implementation plan for AsyncSection wiring (gap table G1–G5, ordered steps, PageMeta bridge); added C7 with revised leaf-first order and fieldType registry table
- **Files modified** — `/Users/I043125/work2/IntelligentUI/src/components/control/AbsInputEle.tsx`: added `?? Math.random().toString(36)` guard to `useState` initialiser (step 1); added `style` prop to `AbsInputEleProps` to satisfy `AsyncField`/`AsyncForeField` callers
- **Files modified** — `/Users/I043125/work2/IntelligentUI/src/components/control/InputFieldUnion.tsx`: replaced `declare const ServiceUtilityHelper` global with named import from `@/services/ServiceUtilityHelper`; added `?? Math.random()` guard to `genRamdomPostIndex` call; replaced `<input type="text">` stub with full field-type registry routing `text/select/date/number/textarea/upload` → Ant Design Pro components (step 2 + 3)
- **Files modified** — `/Users/I043125/work2/IntelligentUI/src/components/page/AsyncEditUnion.tsx`: replaced `declare const AsyncSection` global with named import `{ getSectionFieldMetaList }` from `'./AsyncSection'`; imported `FieldMeta` type for correct typing (step 4)

---

#### Update log — 2026-04-13 (C7 step 5)

- **Files modified** — `/Users/I043125/work2/IntelligentUI/src/controllers/ServiceEditController.ts`: added `import i18n`; added `buildAsyncPageMeta()` method — bridges `getDefaultPageMeta()` PageMeta format to the shape `AsyncPage` expects by adding `parentContent` (loaded record), `labelObject` (pre-translated tab titles), `titleLabelKey` (AsyncPage's name for `tabTitle`), and `sectionId` on each section
- **Files modified** — `/Users/I043125/work2/IntelligentUI/src/pages/logistics/purchaseContract/PurchaseContractEditPage.tsx`: imported `AsyncPage`; added `controller.buildAsyncPageMeta()` call; rendered `<AsyncPage pageMeta={asyncPageMeta} />` below `<EditPageShell>` so the full AsyncSection chain is exercised in parallel with the working ProForm renderer

---

#### Update log — 2026-04-13 (genRamdomPostIndex runtime fix)

- **Files modified** — `/Users/I043125/work2/IntelligentUI/src/services/ServiceUtilityHelper.ts`: added `genUUID()` and `genRamdomPostIndex()` as named exports — direct port of `genUUID()` / `genRamdomPostIndex()` from legacy `Commons.js`
- **Files modified (32 files)** — all tsx files under `src/components/control/` and `src/components/page/` that referenced `genRamdomPostIndex`: removed `declare const genRamdomPostIndex` stub; added `import { genRamdomPostIndex } from '@/services/ServiceUtilityHelper'`; cleaned up `?? Math.random().toString(36)` fallback guards (now unnecessary)

---

#### Update log — 2026-04-13 (ServiceUtilityHelper runtime fix)

- **Files modified** — `/Users/I043125/work2/IntelligentUI/src/services/ServiceUtilityHelper.ts`: no change needed — `fetchObjValueByPath`, `getDomElement`, `formatSelectWithIcon`, `extendObject` were already exported named functions
- **Files modified (10 tsx/ts files)** — removed `declare const ServiceUtilityHelper` stubs; replaced `ServiceUtilityHelper.xxx()` dot-notation calls with direct named function imports from `@/services/ServiceUtilityHelper`:
  - `AbsInput.tsx` → `import { getDomElement }`
  - `AbsInputEle.tsx`, `RefControl.tsx`, `LabelEle.tsx`, `PortletHeadEle.tsx`, `ControlPortletHead.tsx` → `import { fetchObjValueByPath }`; added `as string | undefined` / `as Record<string, unknown>` casts where return type narrowing was needed
  - `PurchaseContractManager.ts` → `import { formatSelectWithIcon, extendObject }`; added `as any` on legacy `formatSelectWithIcon` status arg; added `as Record<string, unknown>` on `extendObject` oSettings arg; added `!` non-null assertions on `targetSettings` usages
- **Files modified** — `LabelEle.tsx`: added `children?: React.ReactNode` to `LabelEleProps` (pre-existing error unmasked when `declare const ServiceUtilityHelper: any` was removed from `TextAreaEle.tsx`)

---

### C8

**Topic:** Phase B — verify `AsyncPage` → `AsyncEditSection` → `InputFieldUnion` renders
fields correctly in the Purchase Contract editor, and what blockers exist.

**Findings:**

Three concrete blockers prevent `AsyncPage` from rendering ProForm fields correctly
even though `InputFieldUnion` itself is complete:

**Blocker 1 — Nested `<form>` breaks ProForm context**

`AsyncField` renders each field via `AbsInputEle`, which wraps `InputFieldUnion` in
Bootstrap HTML including its own `<form action="#">` element:

```
AsyncField → AbsInputEle → <div col-md-4> <form> <div form-group> → InputFieldUnion
```

ProForm components (`ProFormText`, `ProFormSelect`, etc.) must be direct children
of a `<ProForm>` — nesting them inside a separate `<form>` element breaks the form
context entirely. The inputs render visually but are disconnected from the ProForm
instance; values are not read or written.

**Blocker 2 — Field labels are raw i18n keys**

`getSectionFieldMetaList(sectionMeta)` returns the raw `fieldMetaList` from
`getDefaultPageMeta()`. Each entry has `fieldMeta.fieldLabel` unset — labels are
stored as i18n keys like `'purchaseContract:purchaseContract.name'` in
`sectionMeta.sectionTitle` etc. `InputFieldUnion` reads `fieldMeta.fieldLabel`
directly, so all labels render as `undefined`.

**Blocker 3 — ProForm namepath vs. bare field name**

`AsyncSectionFactory` correctly extracts `sectionParentContent` via
`fetchObjValueByPath(parentContent, parentContentPath)` — so `InputFieldUnion`
receives the right sub-object (e.g. `purchaseContractUIModel`). However ProForm
needs the full namepath `['purchaseContractUIModel', 'signDate']` to read/write
`initialValues` correctly. `InputFieldUnion` only passes the bare field name
`'signDate'` as `name`, so ProForm cannot find the value in `initialValues`.

**Root cause summary:**

`AsyncField` → `AbsInputEle` is a Bootstrap/jQuery field rendering chain.
`InputFieldUnion` is a ProForm rendering chain. Putting ProForm components inside
the Bootstrap chain (via `AbsInputEle`) creates a structural mismatch.
The correct fix is to **bypass `AbsInputEle` entirely** for ProForm rendering and
call `InputFieldUnion` directly from `AsyncField`, supplying the resolved label
and namepath.

**Revised Phase B implementation:**

1. **Add `resolvedLabel` and `namePath` props to `InputFieldUnion`** — when these
   are provided they take priority over `fieldMeta.fieldLabel` / `fetchFieldName()`,
   allowing the caller to supply already-translated labels and full ProForm namepaths

2. **Modify `AsyncField`** — instead of routing through `AbsInputEle`, call
   `InputFieldUnion` directly for each field in `comSectionFieldMetaList`, passing:
   - `resolvedLabel`: resolved from `labelObject` via `fetchObjValueByPath` using the
     field's `labelKey` or `fieldName`
   - `namePath`: `[sectionMeta.parentContentPath, fieldMeta.fieldName]` so ProForm
     can read/write `initialValues` correctly

3. **Wrap `AsyncEditSection`'s `AsyncEditUnion` output in a `<ProForm>`** — or
   confirm that `AsyncPage` sits inside the `<ProForm>` from `EditPageShell` (it
   does, because `AsyncPage` is rendered as a child of the same page that has
   `<EditPageShell>` with its `<ProForm>`). So no separate ProForm wrapper needed —
   `AsyncPage` just needs to be inside the existing ProForm tree.

**Implementation (2026-04-12):**

Changes made to fix the three blockers:

| File | Change |
|---|---|
| `ServiceEditController.ts` `buildAsyncPageMeta()` | Pre-resolves every `fieldMeta.fieldLabel` via `i18nConfig` + `i18n.t()` so translated labels flow into `fieldMeta.fieldLabel` |
| `InputFieldUnion.tsx` | Added `resolvedLabel?: string` and `namePath?: string \| string[]` props; `fieldLabel` = `resolvedLabel ?? fieldMeta.fieldLabel`; `name` = `namePath ?? fieldName` on all ProForm components |
| `AsyncField.tsx` | Added `parentContentPath?: string` prop; when present, bypasses `AbsInputEle` entirely and renders `<InputFieldUnion>` directly with `namePath=[parentContentPath, fieldName]` and `resolvedLabel` from `fieldMeta.fieldLabel` (pre-resolved by `buildAsyncPageMeta`) |
| `AsyncEditUnion.tsx` | Extracts `sectionMeta.parentContentPath` and passes it as `parentContentPath` to `<AsyncField>` |

**Result:** `AsyncPage` → `AsyncEditSection` → `AsyncEditUnion` → `AsyncField` →
`InputFieldUnion` now renders Ant Design Pro form fields with translated labels and
correct ProForm namepaths that match `initialValues` structure (e.g.
`['purchaseContractUIModel', 'signDate']`). Build succeeds.

---

### C9

**Topic:** Process Button Rendering — Migration Plan for Editor and List Pages

The current new UI renders process buttons in two completely different ways:
- **List pages:** `AsyncPage` reads `pageMeta.processButtonMeta` + `processButtonGroupArray` and renders Ant `<Button>` / `<Dropdown>` directly.
- **Editor pages:** `AsyncEditorPage` reads `pageMeta.toolbarButtons` (a separate pre-resolved array) and renders its own `<Button>` loop + `extraToolbar` for workflow actions.
- **WorkflowToolbar:** A hardcoded React component that renders doc-action buttons (Submit, Approve, etc.) based on a status→buttons visibility matrix, completely bypassing the `processButtonMeta` pipeline.

This is **wrong**. In the legacy UI, both editor and list pages use the **same** process button rendering pipeline:

```
Controller.getDefaultPageMeta()
  → pageMeta.processButtonMeta     (flat buttons: save, exit, search, placeholder, etc.)
  → pageMeta.processButtonGroupArray (dropdown button groups: New ▾ → New Module / New From Inquiry)
       ↓
AsyncPage.initProcessButtonFromPageMeta(oSettings)
  → processProcessButtonMeta()       — resolves string callbacks to functions
  → processProcessButtonGroupArray() — resolves group labels
  → $refs.processButtonArray.convertButtonMetaToArray()
       ↓
ProcessButtonArray.convertButtonMetaToArray()
  → For each button: convertButtonMetaCore() — resolves label, icon, disabled, callback
  → For PLACEHOLDER (category: DOC_ACTION_BTN): genDocActionProcessButtonMeta()
    → Cross-references actionCodeList (from backend) with actionCodeMatrix (from controller)
    → Generates runtime buttons: Submit, Approve, Reject, DeliveryDone, etc.
    → Each button's formatClass checks currentStatus ∈ preStatusList
    → Each button's callback calls executeDocActionCore(actionCodeHeader)
       ↓
ProcessButtonArray template renders:
  → cache.buttonMetaArray → <EmbeddedProcessButtonCore> for each flat button
  → cache.buttonGroupArray → <DropdownButtonCore> + <ItemProcessButton> for each group
```

**Both editor and list pages** use this exact same pipeline. The only difference is what `processButtonMeta` contains:

| Page Type | processButtonMeta | processButtonGroupArray |
|-----------|-------------------|------------------------|
| **Editor** (PurchaseContractEditor) | `save: {callback:'saveModule', formatClass:'displayForEdit'}`, `placeholder: {category: DOC_ACTION_BTN}`, `exit: {callback:'exitModule'}` | (none) |
| **List** (PurchaseContractList) | `search: {callback: vm.searchModule}`, `newModule: {callback: vm.newModule}` | `[{button:{id:'newModule',...}, buttonGroup:[{callback:vm.newModule},{callback:vm.newFromInquiry},{callback:vm.newFromRequest}]}]` |

The `placeholder: {category: DOC_ACTION_BTN}` entry on editor pages is **expanded at runtime** into the workflow action buttons (Submit, Approve, etc.) by `ProcessButtonArray.genDocActionProcessButtonMeta()`. This is how the legacy "WorkflowToolbar" works — it's not a separate component but part of the same process button pipeline.

---

#### Migration Plan

**1. Migrate the legacy process button components to TypeScript, keeping all methods/properties:**

| Legacy JS | New TS | Purpose |
|-----------|--------|---------|
| `ProcessButtonArray.js` | `src/components/control/ProcessButtonArray.ts` | Main component: `convertButtonMetaToArray()`, `genDocActionProcessButtonMeta()`, template renders `cache.buttonMetaArray` + `cache.buttonGroupArray` |
| `EmbeddedProcessButtonCore` (inside ProcessButtonArray.js) | `src/components/control/EmbeddedProcessButtonCore.tsx` | Individual button: renders `<Button>` with icon, label, formatClass visibility, callback onClick |
| `DropdownButtonCore` (inside ProcessButtonArray.js) | Inline or merged into ProcessButtonArray | Dropdown trigger button with arrow icon |
| `ItemProcessButton` (inside ProcessButtonArray.js) | Inline or merged into ProcessButtonArray | Dropdown menu item |
| `ProcessButtonConstants` | `src/components/control/ProcessButtonConstants.ts` | `placeholderCategory: { DOC_ACTION_BTN: 1 }` |

At the **deepest layer**, `EmbeddedProcessButtonCore` renders an Ant Design `<Button>`. The `DropdownButtonCore` + `ItemProcessButton` combination maps to Ant Design `<Dropdown>` + `<Menu>`. But the outer orchestration (`ProcessButtonArray.convertButtonMetaToArray`, placeholder expansion, formatClass/callback resolution) must be kept identical to legacy.

**2. Implement `AsyncPage.initProcessButtonFromPageMeta()` in `AsyncPage.tsx`:**

This is the **starting point** for process button rendering — called by the controller after i18n is loaded. Currently missing in the new UI. Must be added as an imperative method on AsyncPage (or as a hook/effect that runs when `pageMeta` is available).

The method:
1. Calls `processProcessButtonMeta(pageMeta, pageMeta.processButtonMeta)` — resolves string callback names to actual functions via `getVueMethod()`
2. Calls `processProcessButtonGroupArray(pageMeta, pageMeta.processButtonGroupArray)` — resolves group labels
3. Calls `ProcessButtonArray.convertButtonMetaToArray({ processButtonMetaArray, actionCodeList, actionCodeMatrix, parentVue, labelObject })` — converts metadata to rendered button arrays, expanding DOC_ACTION_BTN placeholders

**3. Remove the current ad-hoc process button rendering:**

| Current (wrong) | Action |
|-----------------|--------|
| `AsyncPage.tsx` reads `processButtonMeta`/`processButtonGroupArray` and renders `<Button>`/`<Dropdown>` inline | **Remove** — replace with `<ProcessButtonArray>` component |
| `AsyncEditorPage.tsx` reads `pageMeta.toolbarButtons` and renders its own button loop | **Remove** — editor buttons come from `processButtonMeta` via `ProcessButtonArray` like legacy |
| `ServiceEditController.getProcessButtons()` / `convertProcessButtonsToJson()` that produces `toolbarButtons` | **Remove** — no longer needed; `processButtonMeta` goes directly to `ProcessButtonArray` |
| `WorkflowToolbar.tsx` — hardcoded status→action mapping | **Remove** after `ProcessButtonArray` + DOC_ACTION_BTN placeholder expansion is working |

**4. The `extraToolbar` prop on `AsyncPage` becomes unnecessary** once `ProcessButtonArray` handles all buttons (both operational like Save/Cancel and workflow like Submit/Approve). The `renderToolbar` prop also becomes unnecessary — `ProcessButtonArray` is rendered by `AsyncPage.initProcessButtonFromPageMeta()` in the same position for both editor and list pages.

---

#### Key Legacy Methods to Preserve

**`AsyncPage.initProcessButtonFromPageMeta(oSettings)`** (AsyncPageElement.js L4173):
- Entry point called by controller
- Receives `actionConfigurePromise` (resolves to `actionCodeList` from backend) and `actionCodeMatrix` (from `getActionCodeMatrix()`)
- Calls `processProcessButtonMeta()`, `processProcessButtonGroupArray()`, then `$refs.processButtonArray.convertButtonMetaToArray()`

**`ProcessButtonArray.convertButtonMetaToArray(oSettings)`** (ProcessButtonArray.js L460):
- Iterates `processButtonMetaArray`
- For each entry: calls `convertButtonMetaCore()` to resolve label/icon/callback
- For `placeholder` entries with `category: DOC_ACTION_BTN`: calls `genDocActionProcessButtonMeta()`
- Stores results in `cache.buttonMetaArray` and `cache.buttonGroupArray`

**`ProcessButtonArray.genDocActionProcessButtonMeta(oSettings)`** (ProcessButtonArray.js L500):
- Takes `actionCodeList` (from backend) and `actionCodeMatrix` (from controller)
- For each action code: generates a button with `formatClass = () => displayForActionCodeCore(...)` and `callback = () => executeDocActionCore(header)`
- These are the runtime Submit/Approve/Reject/DeliveryDone buttons

**`EmbeddedProcessButtonCore`** (ProcessButtonArray.js):
- Renders a single `<button>` with icon, label, onClick=callback, visibility controlled by formatClass
- In the new UI: this wraps an Ant Design `<Button>`

---

#### ProcessButtonArray Data Flow (postButtonMetaArray)

`ProcessButtonArray` has two rendering sources:
1. **`cache.buttonMetaArray`** — flat buttons from `processButtonMeta` (converted via `convertButtonMetaToArray`)
2. **`cache.buttonGroupArray`** — dropdown groups from `processButtonGroupArray`

Additionally, `EmbeddedProcessButtonArray` (used in search sections) accepts a **`postButtonMetaArray`** prop — this is a secondary source of button groups rendered after the main buttons. It handles both single buttons (`postButtonMeta.button.label` exists) and dropdown groups (`postButtonMeta.buttonGroup` exists).

---

#### Legacy File Locations

| File | Lines | Purpose |
|------|-------|---------|
| `admin/js/component/basicElements/AsyncPageElement.js` L4173-4280 | ~107 | `initProcessButtonFromPageMeta()`, `processProcessButtonMeta()`, `processProcessButtonGroupArray()` |
| `admin/js/component/basicElements/ProcessButtonArray.js` | ~550 | Full component: `ButtonCore`, `EmbeddedProcessButtonCore`, `DropdownButtonCore`, `ItemProcessButton`, `ProcessButtonArray`, `ProcessButtonConstants` |
| `admin/js/component/basicElements/EmbeddedProcessButtonArray.js` | ~120 | Section-level button array with `postButtonMetaArray` |

---

**Status:** Plan only — implementation not started. The current `WorkflowToolbar` + `toolbarButtons` + inline `<Button>` rendering continues to work as-is until this migration is executed.

---

### C10

**Topic:** DocumentItemMultiSelect Framework Migration — from legacy jQuery/Vue 2 modal to React/Ant Design

**Background:**

The `DocumentItemMultiSelect` is the cross-document item selection framework. When a user clicks a workflow action like "Delivery Done" on a Purchase Contract, a modal opens allowing them to select material items from the source document to generate a new target document (e.g. Inbound Delivery). This framework has 3 layers and 28 legacy files (~5500 lines total):

```
Layer 1: Factory + Constants
  DocumentItemMultiSelectFactory.js (229 lines) — maps targetDocType → MultiSelect subclass
  USE_CASE enum: CROSS_DOC_CREATION(1), MERGE_DOC(2), SPLIT_DOC(3), etc.

Layer 2: Base components
 .js (1196 lines) — modal lifecycle, 6-step init, source/target selection, confirm→POST
  SrcSelectInputUnion.js (648 lines) — source document selector, maps srcDocType → subclass

Layer 3: Document-specific subclasses (26 files)
  12 MultiSelect subclasses: PurchaseContract, InboundDelivery, OutboundDelivery, etc.
  14 SrcSelectInput subclasses: PurchaseContract, InboundDelivery, PurchaseRequest, etc.
```

**Legacy data flow:**
```
User clicks "Delivery Done" on PurchaseContract editor
  → executeDocActionCore('deliveryDone')
  → detects docItemMultiSelectConfig → executeDocItemSelectWrapper()
  → DocumentItemMultiSelectFactory.initBatchSelection(config)
  → Creates InboundDeliveryMultiSelect (based on targetDocType)
  → initBatchSelectionTemplate() (6-step init)
  → Bootstrap Modal opens (75% width)
  → User selects source doc → loads material items → multi-select items → confirm
  → POST selected UUIDs to inboundDelivery/generateNextDocBatch
  → Backend creates InboundDelivery with selected items
```

**Migration approach — two phases:**

**Phase 1: TypeScript class porting (COMPLETED)**

All 3 layers ported to TypeScript classes in `src/components/doc/`:

| Legacy JS file | New TS file | Status |
|---|---|---|
| `DocumentItemMultiSelectFactory.js` | `DocumentItemMultiSelectFactory.ts` | Done — USE_CASE, getModelIdByTargetDocType, subclass registry |
| `DocumentItemMultiSelect.js` | `DocumentItemMultiSelect.ts` | Done — full lifecycle, HTTP calls wired to apiPost/apiGet |
| `SrcSelectInputUnion.js` | `SrcSelectInputUnion.ts` | Done — getValue, genTargetUrl, createByDocType factory |
| `PurchaseContractMultiSelect.js` | `supplyChain/PurchaseContractMultiSelect.ts` | Done |
| `InboundDeliveryMultiSelect.js` | `supplyChain/InboundDeliveryMultiSelect.ts` | Done — warehouse fields, custom confirmToGenerate |
| `PurchaseContractSelectInput.js` | `supplyChain/PurchaseContractSelectInput.ts` | Done — genTargetUrl per useCase |
| `InboundDeliverySelectInput.js` | `supplyChain/InboundDeliverySelectInput.ts` | Done |

Migration rules applied:
- `Vue.extend` → TypeScript class
- `data()` → class properties
- `methods` → methods, `computed` → getters
- `static` methods → `static` methods on class
- jQuery/Select2/Bootstrap calls → replaced with apiPost/apiGet or React callbacks
- Template HTML → separate React TSX component

**Phase 2: React rendering shell + wiring (COMPLETED 2026-04-17)**

Created `DocItemSelectionModal.tsx` and wired `executeDocItemSelectWrapper`:

| Component | What it does |
|---|---|
| `DocItemSelectionModal.tsx` (NEW) | Ant Design Modal wrapping the TS class layer: Descriptions for src/target doc info, Table with rowSelection for item picking, Confirm button |
| `DocumentItemMultiSelectFactory.ts` | Added `createChildByTargetDocType()` — static registry mapping targetDocType → subclass constructor; replaced `filterChildComponents()` TODO |
| `SrcSelectInputUnion.ts` | Added `createByDocType()` — static factory method mapping sourceDocType → subclass constructor |
| `DocumentItemMultiSelect.ts` | Wired 3 HTTP stubs: `loadSrcDataWrapper()` → apiPost, `genTargetDocFromMultipleSelection()` → apiPost, `loadTargetDocument()` → apiGet. Added React callback hooks: `onSrcItemsLoaded`, `onModalClose`, `onGenerationDone`, `getSelectedUUIDsFn` |
| `ServiceEditController.ts` | Added `openDocItemSelectionModal` to `ServiceEditControllerDeps`; replaced TODO in `executeDocItemSelectWrapper` with callback invocation |
| `usePurchaseContractEditController.ts` | Added `multiSelectConfig` state; passed `setMultiSelectConfig` as `openDocItemSelectionModal` into deps |
| `PurchaseContractEditPage.tsx` | Renders `<DocItemSelectionModal>` alongside `<AsyncEditorPage>` |

**Wiring architecture:**
```
PurchaseContractEditPage.tsx
  useState<multiSelectConfig>          ← React state controls modal visibility
  │
  ├── AsyncEditorPage                  ← existing editor
  │     └── ProcessButtonArray
  │           └── "Delivery Done" button onClick
  │                 → controller.executeDocActionCore('deliveryDone')
  │                 → detects docItemMultiSelectConfig
  │                 → controller.executeDocItemSelectWrapper(oSettings)
  │                 → deps.openDocItemSelectionModal(config)  ← sets React state
  │
  └── DocItemSelectionModal            ← NEW
        config={multiSelectConfig}
        │
        ├── useEffect: factory.initBatchSelection(config)
        │     → creates InboundDeliveryMultiSelect instance
        │     → hooks React callbacks into instance
        │     → instance.initBatchSelectionTemplate() (6-step init)
        │     → loads source items via apiPost
        │
        ├── <Table rowSelection>       ← user picks items
        │
        └── Confirm button
              → multiSelect.confirmToGenerate()
              → apiPost to genTargetDocBatch endpoint
              → onDone → refreshEditView
```

**Remaining work (Phase C — not started):**

10 more MultiSelect subclasses + 12 more SrcSelectInput subclasses need porting for other document types. Each is ~100-200 lines of mostly config. Mechanical once the pattern is proven:

| MultiSelect subclass | SrcSelectInput subclass |
|---|---|
| OutboundDeliveryMultiSelect | OutboundDeliverySelectInput |
| InventoryTransferOrderMultiSelect | InventoryTransferOrderSelectInput |
| PurchaseRequestMultiSelect | PurchaseRequestSelectInput |
| PurchaseReturnMultiSelect | PurchaseReturnOrderSelectInput |
| InventoryCheckOrderMultiSelect | WarehouseStoreItemSelectInput |
| WarehouseStoreMultiSelect | QualityInspectOrderSelectInput |
| WasteProcessOrderMultiSelect | WasteProcessOrderSelectInput |
| SalesContractMultiSelect | SalesContractSelectInput |
| SalesReturnOrderMultiSelect | SalesReturnOrderSelectInput |
| SerExtendPageSettingMultiSelect | InquirySelectInput, SalesForcastSelectInput, SerExtendPageSettingSelectInput |

For each new document type: (1) port the subclass .ts file, (2) add an entry to the registry in `DocumentItemMultiSelectFactory.createChildByTargetDocType()` and `SrcSelectInputUnion.createByDocType()`.

---

#### Update log — 2026-04-17

- **Files created** — `/Users/I043125/work2/IntelligentUI/src/components/doc/DocItemSelectionModal.tsx` — React modal component wrapping DocumentItemMultiSelect framework
- **Files modified** — `/Users/I043125/work2/IntelligentUI/src/components/doc/DocumentItemMultiSelectFactory.ts` — added subclass registry (`createChildByTargetDocType`), lazy imports, implemented `filterChildComponents`, `getActiveChild`, changed `initBatchSelection` return type
- **Files modified** — `/Users/I043125/work2/IntelligentUI/src/components/doc/SrcSelectInputUnion.ts` — added `createByDocType()` static factory method
- **Files modified** — `/Users/I043125/work2/IntelligentUI/src/components/doc/DocumentItemMultiSelect.ts` — wired 3 HTTP stubs to apiPost/apiGet, added React callback hooks (onSrcItemsLoaded, onModalClose, onGenerationDone, getSelectedUUIDsFn), updated `setSrcModelId()` to instantiate real subclass, updated `_getSelectedUUIDs()` to delegate to callback
- **Files modified** — `/Users/I043125/work2/IntelligentUI/src/controllers/ServiceEditController.ts` — added `openDocItemSelectionModal` to `ServiceEditControllerDeps`, replaced TODO in `executeDocItemSelectWrapper` with callback invocation
- **Files modified** — `/Users/I043125/work2/IntelligentUI/src/pages/logistics/purchaseContract/usePurchaseContractEditController.ts` — added `multiSelectConfig` state, passed as `openDocItemSelectionModal` into deps, returned from hook
- **Files modified** — `/Users/I043125/work2/IntelligentUI/src/pages/logistics/purchaseContract/PurchaseContractEditPage.tsx` — renders `<DocItemSelectionModal>` alongside `<AsyncEditorPage>`
