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

---

### Update — Fix select fields with getMetaDataUrl not rendering in DocumentItemMultiSelect (2026-04-20)

#### Problem
Select-type fields configured with `getMetaDataUrl` in their `settings` (e.g. `getMetaDataUrl: this.getDocumentTypeURL`) were rendering as empty `<Select>` components in the DocumentItemMultiSelect modal. The same field configuration worked correctly in AsyncPage.

#### Root Cause
`InputFieldUnion.tsx` has two code paths:
1. **Inside ProForm** (AsyncPage) — uses `ProFormSelect` with a `request` prop that calls `loadMetaRequestForSelect(getMetaDataUrl)`, which fetches and populates options automatically.
2. **Outside ProForm** (DocumentItemMultiSelect modal) — rendered a plain `<Select>` but never passed `options` or fetched from `getMetaDataUrl`. The URL was extracted from settings but unused.

#### Fix
Extracted the outside-ProForm rendering into a dedicated `OutsideProFormField` component with a `useEffect` that calls `loadMetaRequestForSelect()` when `getMetaDataUrl` is present in field settings. Passes through extended settings (`idField`, `textField`, `listSubPath`, `excludeKeyList`, `addEmptyFlag`) to the fetch function. Also supports static `options` as fallback.

#### Update log — 2026-04-20

- **Files modified** — `/Users/I043125/work2/IntelligentUI/src/components/control/InputFieldUnion.tsx` — added `OutsideProFormField` sub-component that fetches select options from `getMetaDataUrl` via `useEffect` + `loadMetaRequestForSelect`; replaced inline outside-ProForm rendering with this component; added `useEffect` import and `SelectMetaOptions` type import
- **Files modified** — `/Users/I043125/work2/IntelligentUI/src/pages/logistics/purchaseContract/PurchaseContractEditPage.tsx` — renders `<DocItemSelectionModal>` alongside `<AsyncEditorPage>`

---

### C11

**Topic:** `namePath` in InputFieldUnion — what it means, why it exists, and how it replaces the legacy `$refs` + `batchExecuteSubRefMethod` chain

---

#### What is `namePath`?

`namePath` is a prop on the new React `InputFieldUnion` component (`InputFieldUnion.tsx:37`). It is a **ProForm field path** — an array of strings that tells Ant Design Pro's `<ProForm>` how to locate the field's value in the form state.

For example, if a PurchaseContract editor has a section with `parentContentPath: 'purchaseContractUIModel'` and a field `fieldName: 'signDate'`, then `namePath` becomes `['purchaseContractUIModel', 'signDate']`. ProForm uses this path to bind the field's value, validation, and submission automatically.

**When `namePath` is present** → the field is inside a `<ProForm>` context → use `ProFormSelect`, `ProFormText`, etc.
**When `namePath` is absent** → the field is standalone (e.g. inside a modal with no ProForm) → use plain Ant Design `<Select>`, `<Input>`, etc.

This concept has **no direct equivalent in the legacy UI**. The legacy `InputFieldUnion` was a thin pass-through that always rendered the same way regardless of context.

---

#### Why does `namePath` exist? (the problem it solves)

In React + Ant Design Pro, `ProFormSelect` and other Pro components **require** a parent `<ProForm>` context to function. Without it, they render a dash (`-`) instead of an input. But some places in the migrated code — specifically `DocumentItemMultiSelect`'s modal — render fields **outside** any `<ProForm>`. The `namePath` prop acts as the signal to distinguish these two contexts.

---

#### Legacy: no `namePath` needed — the `$refs` chain was context-agnostic

In the legacy Vue 2 code, `InputFieldUnion` (`AsyncControlElement.js:1227-1455`) was a dynamic-component wrapper. Its entire template was:

```js
// AsyncControlElement.js:1450-1454
template:
  '<abs-input :is="getFieldType(fieldMeta)" ... :ref="comSubFieldRefId"
     :settings="fieldMeta.settings" v-model:value="comFieldValue">'
```

`:is="getFieldType(fieldMeta)"` resolved to the actual child component (e.g. `Select2Field`). The child was registered as a Vue `$ref` via `:ref="comSubFieldRefId"`.

**Select options loading was triggered imperatively — the same way everywhere:**

```
parent.postUpdate()
  → batchExecuteSubRefMethod({ methodName: 'postUpdate' })    // walks all child $refs
    → InputFieldUnion.postUpdate()                             // AsyncControlElement.js:1348
      → batchExecuteSubRefMethod({ methodName: 'postUpdate' }) // walks its child $refs
        → Select2Field.postUpdate()                            // AsyncControlElement.js:760
          → vm.updateConfig()                                  // line 767
            → vm.initSelectConfigure()                         // line 782
            → vm.$nextTick → vm.loadMetaData()                 // line 783-784
              → checks settings.getMetaDataUrl                 // line 803
              → ServiceUtilityHelper.loadMetaRequest(...)       // line 811
```

This chain worked identically whether the parent was `AsyncPage` (`AsyncPageElement.js:93-101`) or `DocumentItemMultiSelect` (`DocumentItemMultiSelect.js:572-580`). Both called `postUpdate()` → `batchExecuteSubRefMethod`, which walked the `$refs` tree and eventually reached `Select2Field.loadMetaData()` where `getMetaDataUrl` was consumed.

**Key insight:** The legacy code had no "inside form vs. outside form" distinction. `Select2Field` was a standalone jQuery Select2 widget that worked anywhere — it didn't need a parent form context. The `$refs` traversal was the universal trigger mechanism.

---

#### New React: two paths, split by `namePath`

React has no `$refs` tree traversal. The imperative `postUpdate → batchExecuteSubRefMethod` chain cannot exist. Instead, the new code uses **declarative lifecycle** — but the lifecycle mechanism differs depending on whether a `<ProForm>` context exists:

##### Path 1: AsyncPage (inside ProForm) — `namePath` is set

```
Editor Controller descriptor (e.g. PurchaseContractEditController.tsx:327):
  parentContentPath: 'purchaseContractUIModel'

→ AsyncEditUnion.tsx:61  extracts parentContentPath from sectionMeta
→ AsyncEditUnion.tsx:95  passes parentContentPath to AsyncField
→ AsyncField.tsx:183     if (parentContentPath !== undefined) — enters ProForm path
→ AsyncField.tsx:193     builds namePath: ['purchaseContractUIModel', 'signDate']
→ AsyncField.tsx:203     <InputFieldUnion namePath={namePath} />
→ InputFieldUnion.tsx:179  outsideProForm = !namePath → false
→ InputFieldUnion.tsx:213-237  renders <ProFormSelect request={() => loadMetaRequestForSelect(url)} />
    → ProFormSelect automatically calls request() on mount (Ant Design Pro lifecycle)
```

##### Path 2: DocumentItemMultiSelect modal (outside ProForm) — `namePath` is absent

```
DocumentItemMultiSelect.tsx:1084-1088  getDefaultSectionMeta():
  { sectionId: '...', sectionCategory: 'EDIT', fieldMetaList: [...] }
  ← NO parentContentPath key

→ AsyncEditUnion.tsx:61  parentContentPath = sectionMeta?.['parentContentPath'] → undefined
→ AsyncEditUnion.tsx:95  passes undefined to AsyncField
→ AsyncField.tsx:183     if (parentContentPath !== undefined) — SKIPS ProForm path
→ AsyncField.tsx:213+    falls to legacy Bootstrap rendering path → AbsInputEle
→ InputFieldUnion receives NO namePath prop
→ InputFieldUnion.tsx:179  outsideProForm = !namePath → true
→ InputFieldUnion.tsx:181-191  renders <OutsideProFormField />
→ OutsideProFormField useEffect (line 74-83):
    loadMetaRequestForSelect(metaOpts).then(setSelectOptions)
    → fires on mount when getMetaDataUrl is present (React useEffect lifecycle)
```

---

#### Summary table

| Aspect | Legacy (Vue 2) | New AsyncPage (ProForm) | New DocItemMultiSelect (modal) |
|---|---|---|---|
| **Equivalent of `namePath`** | None — not needed | `namePath` prop set by `AsyncField.tsx:193-203` | `namePath` absent — not passed |
| **Where the split happens** | No split — same code path everywhere | `AsyncField.tsx:183`: `if (parentContentPath !== undefined)` | Same line — evaluates to `false` |
| **Select component** | `Select2Field` (jQuery Select2 widget) | `ProFormSelect` (Ant Design Pro) | Plain `<Select>` (Ant Design) |
| **Trigger for loading options** | Imperative: `postUpdate()` → `batchExecuteSubRefMethod` → `Select2Field.loadMetaData()` (`AsyncControlElement.js:760-829`) | Declarative: `ProFormSelect.request` prop auto-fires on mount (`InputFieldUnion.tsx:217-218`) | Declarative: `useEffect([getMetaDataUrl])` auto-fires on mount (`InputFieldUnion.tsx:74-83`) |
| **Where `getMetaDataUrl` is consumed** | `Select2Field.loadMetaData()` at `AsyncControlElement.js:803` | `InputFieldUnion.tsx:217`: `() => loadMetaRequestForSelect(getMetaDataUrl)` | `OutsideProFormField` useEffect at `InputFieldUnion.tsx:76-82` |
| **Context requirement** | None — Select2 works anywhere | Requires parent `<ProForm>` context | Works standalone — no form context needed |

---

#### Key file references

| File | Line(s) | What |
|---|---|---|
| `InputFieldUnion.tsx` | 37 | `namePath` prop definition |
| `InputFieldUnion.tsx` | 179 | `const outsideProForm = !namePath` — the fork point |
| `InputFieldUnion.tsx` | 74-83 | `OutsideProFormField` useEffect — fetches options outside ProForm |
| `InputFieldUnion.tsx` | 213-237 | ProFormSelect with `request` prop — inside ProForm |
| `AsyncField.tsx` | 183 | `if (parentContentPath !== undefined)` — gate for ProForm path |
| `AsyncField.tsx` | 193-195 | Builds `namePath` from `parentContentPath` + `fieldName` |
| `AsyncEditUnion.tsx` | 61 | Extracts `parentContentPath` from `sectionMeta` |
| `PurchaseContractEditController.tsx` | 327 | Example: `parentContentPath: 'purchaseContractUIModel'` |
| `DocumentItemMultiSelect.tsx` | 1084-1088 | `getDefaultSectionMeta()` — no `parentContentPath` |
| Legacy `AsyncControlElement.js` | 1227-1455 | Legacy `InputFieldUnion` — thin pass-through, no routing |
| Legacy `AsyncControlElement.js` | 760-829 | `Select2Field.postUpdate()` → `updateConfig()` → `loadMetaData()` |
| Legacy `AsyncControlElement.js` | 1348-1356 | `InputFieldUnion.postUpdate()` → `batchExecuteSubRefMethod` |
| Legacy `AsyncPageElement.js` | 93-101 | `AsyncPage.postUpdate()` → `batchExecuteSubRefMethod` |
| Legacy `DocumentItemMultiSelect.js` | 572-580 | `DocumentItemMultiSelect.postUpdate()` → `batchExecuteSubRefMethod` |
| Legacy `DocumentItemMultiSelect.js` | 385-388 | `vm.$nextTick → vm.postUpdate()` — the call site |

---

### C12

**Topic:** How to invoke a method (e.g. `postUpdate`) on all children components in React — replacing the legacy `batchExecuteSubRefMethod` pattern

---

#### Background: the legacy pattern

In the legacy Vue 2 code, `batchExecuteSubRefMethod` walks the `$refs` tree and calls a named method on every child. For example, after data loads, the parent calls `this.postUpdate()`, which triggers `batchExecuteSubRefMethod({ methodName: 'postUpdate' })`, and every child Vue component that has a `postUpdate()` method gets it invoked. This is used for `postUpdate`, `loadMetaData`, `clearValue`, `checkValidateSave`, `checkValidateSubmit`, etc.

React has no `$refs` tree traversal. Components are declarative — a parent doesn't normally call methods on its children. To replicate the legacy pattern, we evaluated three options and chose **Option 2: `useImperativeHandle` + `forwardRef`**.

---

#### The chosen approach: `useImperativeHandle` + `forwardRef`

This is the React mechanism that most closely matches the legacy `batchExecuteSubRefMethod` pattern. Each child component exposes named methods via `useImperativeHandle`, and the parent collects references to call them imperatively.

##### Example code

```tsx
// ── Child: AbsInputEle ──────────────────────────────────────────────────

// 1. Define the handle interface — the methods the parent can call
interface AbsInputEleHandle {
  postUpdate(): void;
  loadMetaData(): void;
  clearValue(): void;
  checkValidateSave(): boolean;
}

// 2. The inner component receives props AND a forwarded ref
const AbsInputEleInner = (props: AbsInputEleProps, ref: React.Ref<AbsInputEleHandle>) => {

  // 3. useImperativeHandle attaches methods to the ref
  useImperativeHandle(ref, () => ({
    postUpdate()       { /* e.g. reload select options */ },
    loadMetaData()     { /* e.g. fetch from getMetaDataUrl */ },
    clearValue()       { /* e.g. reset field state */ },
    checkValidateSave(){ /* e.g. return validation result */ return true; },
  }));

  return <InputFieldUnion ... />;
};

// 4. forwardRef wraps the inner component so it can receive a ref from its parent
const AbsInputEle = forwardRef(AbsInputEleInner);


// ── Parent: AsyncField ──────────────────────────────────────────────────

// 5. Parent stores a Map of child refs (keyed by index or field name)
const childRefs = useRef<Map<string, AbsInputEleHandle>>(new Map());

// 6. Each child gets a callback ref that registers itself into the Map
{visibleFields.map((fm, i) => (
  <AbsInputEle
    ref={el => { if (el) childRefs.current.set(String(i), el); }}
    ...
  />
))}

// 7. The parent's postUpdate iterates all children — mirrors batchExecuteSubRefMethod
function postUpdate() {
  childRefs.current.forEach(child => child.postUpdate());
}

// 8. For methods with return values (e.g. checkValidateSave):
function checkValidateSave(): boolean[] {
  const results: boolean[] = [];
  childRefs.current.forEach(child => results.push(child.checkValidateSave()));
  return results;
}
```

---

#### Q&A — Detailed explanations

---

##### Q1: What does `useRef<Map<string, ChildHandle>>(new Map())` mean? Does `useRef` collect HTML elements with `ref=` attributes?

**No — `useRef` is more general than that.**

`useRef` creates a **mutable container** that persists across React re-renders. The `.current` property holds whatever value you put in it. It does NOT cause re-renders when changed.

Common uses:
1. **Hold a DOM element** — `useRef<HTMLDivElement>(null)` → `ref={divRef}` → `divRef.current` is the `<div>` DOM node
2. **Hold a component handle** — `useRef<AbsInputEleHandle>(null)` → `ref={childRef}` → `childRef.current` is the object returned by `useImperativeHandle`
3. **Hold any mutable value** — `useRef<number>(0)` → a counter that doesn't trigger re-renders

In our case, `useRef<Map<string, AbsInputEleHandle>>(new Map())` creates a persistent `Map` that stores **child component handles** (not DOM elements). Each child's `useImperativeHandle` defines what methods are available on that handle. The `Map` lets us store multiple children keyed by index.

**Comparison with legacy:**

| Legacy Vue 2 | React `useRef` |
|---|---|
| `vm.$refs.childName` → the child Vue component instance | `childRef.current` → the handle object from `useImperativeHandle` |
| Access any method/data on the child | Access only methods explicitly exposed via `useImperativeHandle` |
| Automatic via `:ref="name"` in template | Manual via `ref={callback}` or `ref={refObject}` in JSX |

---

##### Q2: Explain `<AbsInputEle ref={el => { if (el) childRefs.current.set(i, el); }} />` in detail

This is a **callback ref** — instead of passing a ref object, you pass a function. React calls this function:
- With the component handle (or DOM element) when the component **mounts**
- With `null` when the component **unmounts**

Breaking it down:

```tsx
<AbsInputEle
  ref={el => {                              // el = the AbsInputEleHandle object, or null on unmount
    if (el)                                  // guard: only register on mount, not on unmount
      childRefs.current.set(String(i), el);  // store handle in the Map, keyed by index
  }}
/>
```

Step by step:
1. React renders `<AbsInputEle>` and calls `forwardRef` → the `ref` reaches the inner component
2. `useImperativeHandle(ref, () => ({ postUpdate, loadMetaData, ... }))` attaches methods to the ref
3. React calls our callback with `el` = the handle object `{ postUpdate, loadMetaData, ... }`
4. We store it: `childRefs.current.set('0', el)`
5. Later, the parent can call `childRefs.current.get('0')?.postUpdate()`

**Comparison with legacy:**

| Legacy | React |
|---|---|
| `<select2-field :ref="comSubFieldRefId">` | `<AbsInputEle ref={el => { ... }}>` |
| `vm.$refs[comSubFieldRefId]` → child component | `childRefs.current.get(key)` → child handle |
| Automatic registration by Vue | Manual registration via callback ref |

---

##### Q3: What does `.current` mean?

`useRef()` returns an object with a single property: `{ current: initialValue }`.

- `childRefs` is the ref container object (immutable — same object across all renders)
- `childRefs.current` is the **mutable value inside** (the `Map` we stored)

Think of it like a box: `useRef` creates the box, `.current` is what's inside the box. You can change what's inside without React knowing or re-rendering.

```tsx
const childRefs = useRef<Map<string, Handle>>(new Map());
// childRefs         → { current: Map {} }    — the box (never changes)
// childRefs.current → Map {}                 — the contents (mutable)
// childRefs.current.set('0', handle)         — put something in
// childRefs.current.get('0')?.postUpdate()   — use it later
```

**Why `.current` and not just the value directly?**
React needs the outer object to stay the same reference across renders so it can pass it to child components. The inner `.current` is the escape hatch for mutation.

---

##### Q4: What does `const AbsInputEleInner` mean? Is it an additional component to declare inside AbsInputEle?

**Yes — it's the actual implementation, and `AbsInputEle` becomes its `forwardRef` wrapper.**

`forwardRef` requires a function with signature `(props, ref) => JSX`. A normal React component only receives `(props) => JSX` — it can't see the `ref`. `forwardRef` bridges this gap.

The pattern:

```tsx
// Step 1: Write the real component — it receives (props, ref) instead of just (props)
const AbsInputEleInner = (props: AbsInputEleProps, ref: React.Ref<AbsInputEleHandle>) => {
  useImperativeHandle(ref, () => ({
    postUpdate() { ... },
  }));
  return <div>...</div>;
};

// Step 2: Wrap with forwardRef — this lets parents pass ref={...} to it
const AbsInputEle = forwardRef(AbsInputEleInner);
```

You **replace** the existing `AbsInputEle` component definition. The `Inner` suffix is just a convention — it's not a separate public component. Only `AbsInputEle` is exported and used by other files.

**Before (current code):**
```tsx
const AbsInputEle: React.FC<AbsInputEleProps> = (props) => { ... };
export default AbsInputEle;
```

**After (with forwardRef):**
```tsx
const AbsInputEleInner = (props: AbsInputEleProps, ref: React.Ref<AbsInputEleHandle>) => { ... };
const AbsInputEle = forwardRef(AbsInputEleInner);
export default AbsInputEle;
```

The external API is the same — callers still write `<AbsInputEle .../>`. The only difference is that callers can now **optionally** pass a `ref` to get the handle.

---

##### Q5: Using Option 2 universally — detailed Cons analysis

**Decision: Use `useImperativeHandle` + `forwardRef` as the universal pattern for all imperative child method calls.**

**Rationale:** One consistent pattern covers both fire-and-forget (`postUpdate`, `loadMetaData`, `clearValue`) and return-value methods (`checkValidateSave`, `checkValidateSubmit`). This matches the legacy architecture 1:1 and avoids mixing two different patterns.

**Detailed Cons (and mitigations):**

| Con | Severity | Detail | Mitigation |
|---|---|---|---|
| **More boilerplate per component** | Medium | Every component that needs to be called by a parent must: (1) define a `Handle` interface, (2) use `forwardRef`, (3) call `useImperativeHandle`. Legacy did this implicitly via Vue mixins. | Define a shared `AsyncFieldHandle` interface once; components only implement the methods they need. |
| **Goes against React's declarative grain** | Low | React's philosophy is "props down, events up". Imperative method calls on children are considered an escape hatch, not the primary pattern. Linters and style guides may flag it. | This is a migration project — the legacy architecture is inherently imperative. Fighting it would require redesigning the entire field lifecycle. `useImperativeHandle` exists precisely for this use case. |
| **Ref management complexity** | Medium | Parent must maintain a `Map` (or array) of child refs. When children are dynamically added/removed (e.g. conditional fields, list items), the Map must be kept in sync. Stale refs can cause silent no-ops. | Use callback refs with cleanup: `ref={el => { if (el) map.set(key, el); else map.delete(key); }}`. This handles mount/unmount automatically. |
| **Cannot use React.memo effectively** | Low | `forwardRef` components can still be memoized via `React.memo(forwardRef(...))`, but the callback ref `ref={el => ...}` creates a new function on each render, which can defeat memoization of the child. | Extract callback refs to `useCallback` if performance becomes an issue. Unlikely to matter for form fields. |
| **Testing is slightly harder** | Low | Unit tests must use `React.createRef()` or `renderHook` to access the imperative handle, rather than just checking rendered output. | Standard pattern — React Testing Library supports `act()` + ref access. |
| **Tight coupling between parent and child** | Medium | Parent knows the child's method names. If a child renames or removes a method, TypeScript catches it at compile time (via the Handle interface), but the coupling is still tighter than pure prop-based communication. | The Handle interface is the contract — same as the legacy method names. TypeScript enforces it. |
| **No built-in batching / ordering guarantee** | Low | `Map.forEach` iterates in insertion order, which matches DOM order for statically rendered lists. But for dynamically reordered children, the iteration order may not match visual order. | Use a key that reflects the desired order (e.g. field index). For our use case, iteration order doesn't matter — `postUpdate` on field A is independent of field B. |

**Overall assessment:** The cons are manageable and well-mitigated by TypeScript's type system. For a migration project where the legacy architecture is fundamentally imperative, Option 2 is the pragmatic choice. The boilerplate is a one-time cost per component, and the pattern is consistent and predictable.

---

#### Summary: legacy → React mapping for imperative child calls

| Legacy Vue 2 | React (Option 2) |
|---|---|
| `:ref="comSubFieldRefId"` on child in template | `ref={el => map.set(key, el)}` callback ref in JSX |
| `vm.$refs[refId]` to access child | `childRefs.current.get(key)` to access handle |
| `batchExecuteSubRefMethod({ methodName })` | `childRefs.current.forEach(c => c[methodName]())` |
| Method exists on child via Vue mixin (`AbsInput`) | Method exposed via `useImperativeHandle` + `Handle` interface |
| `:is="getFieldType()"` dynamic component | if/else chain in `InputFieldUnion.tsx:194-309` |
| `vm.$set(vm, 'fieldValue', val)` reactive mutation | `useState` setter or form context |

#### Key file references

| File | Line(s) | What |
|---|---|---|
| `AbsInputEle.tsx` | (to be modified) | Will add `forwardRef` + `useImperativeHandle` with `AbsInputEleHandle` |
| `AsyncField.tsx` | 232+ | Where `<AbsInputEle>` is rendered — will add `ref` callback and `childRefs` Map |
| `InputFieldUnion.tsx` | 194-309 | Field type routing (replaces legacy `:is="getFieldType()"` dynamic component) |
| Legacy `AsyncControlElement.js` | 1348-1356 | `InputFieldUnion.postUpdate()` → `batchExecuteSubRefMethod` |
| Legacy `AsyncControlElement.js` | 760-785 | `Select2Field.postUpdate()` → `updateConfig()` → `loadMetaData()` |

---

##### Q6: What does `visibleFields` mean and what is `fm`?

This relates to the code example in the Option 2 pattern:

```tsx
const visibleFields = rawFieldMetaList.filter(fm => checkRenderField(fm) && checkStandardField(fm));

{visibleFields.map((fm, i) => (
  <AbsInputEle
    ref={el => { if (el) childRefs.current.set(String(i), el); }}
    fieldMeta={fm}
    ...
  />
))}
```

**`rawFieldMetaList`** is the flat array of field descriptor objects passed into `AsyncField` via the `fieldMetaList` prop (`AsyncField.tsx:50,65-68`). Each element describes one form field — its name, type, label, settings, validation rules, etc. This array comes from the section configuration defined in the editor controller.

For example, in `PurchaseContractEditController.tsx:329-369`, a section's `fieldMetaList` looks like:

```ts
fieldMetaList: [
  { fieldName: 'id', readonly: true, required: true },
  { fieldName: 'name', required: true, colWidth: 'lg' },
  { fieldName: 'status', fieldType: 'select', settings: { getMetaDataUrl: this.getStatusURL }, readonly: true },
  { fieldName: 'signDate', fieldType: 'date' },
  { fieldName: 'priorityCode', fieldType: 'select', settings: { getMetaDataUrl: this.getPriorityCodeURL } },
  { fieldName: 'note', fieldType: 'textarea', rows: 5 },
]
```

Each object in this array is a **field meta** (`FieldMeta` type from `AbsInput.tsx`). It declares *what* to render, not *how* — the rendering component (`InputFieldUnion` or `AbsInputEle`) reads these properties to decide which Ant Design component to use.

**`visibleFields`** is the filtered subset of `rawFieldMetaList` — only fields that should be rendered and are "standard" (i.e. not hidden, not a `refControl`). The filtering is done at `AsyncField.tsx:184`:

```tsx
const visibleFields = rawFieldMetaList.filter(fm => checkRenderField(fm) && checkStandardField(fm));
```

Where:
- `checkRenderField(fm)` (`AsyncField.tsx:70-74`) — returns `false` if `fm.hidden === true`
- `checkStandardField(fm)` (`AsyncField.tsx:81-83`) — returns `true` if the field has a `fieldName` and is not a `refControl`

**`fm`** is the loop variable — short for **"field meta"**. It is one `FieldMeta` object from the filtered list. In `.map((fm, i) => ...)`:
- `fm` = the field descriptor object (e.g. `{ fieldName: 'status', fieldType: 'select', settings: { getMetaDataUrl: '...' } }`)
- `i` = the numeric index in the filtered array (0, 1, 2, ...)

**`fm` is passed as the `fieldMeta` prop to `AbsInputEle`**, which then passes it down to `InputFieldUnion`, which reads `fm.fieldType` to decide whether to render a `ProFormText`, `ProFormSelect`, `ProFormDatePicker`, etc.

**Comparison with legacy:**

| Legacy Vue 2 | New React |
|---|---|
| `v-for="(fieldMeta, index) in fieldMetaList"` (`AsyncControlElement.js:3185`) | `visibleFields.map((fm, i) => ...)` (`AsyncField.tsx:190`) |
| `fieldMeta` = one field descriptor object | `fm` = same, just a shorter variable name |
| `index` = loop index | `i` = same |
| No pre-filtering in template; `v-show`/`v-if` hides fields inline | Pre-filtered via `.filter()` before rendering |

**Data flow for one field:**

```
Editor Controller (e.g. PurchaseContractEditController.tsx:329)
  defines fieldMetaList: [ { fieldName: 'status', fieldType: 'select', ... }, ... ]

→ AsyncEditSection receives fieldMetaList via sectionMeta
→ AsyncEditUnion passes it to AsyncField as fieldMetaList prop
→ AsyncField.tsx:65-68  rawFieldMetaList = fieldMetaList as FieldMeta[]
→ AsyncField.tsx:184    visibleFields = rawFieldMetaList.filter(...)
→ AsyncField.tsx:190    visibleFields.map((fm, i) => ...)
→ fm = { fieldName: 'status', fieldType: 'select', settings: { getMetaDataUrl: '...' } }
→ passed as fieldMeta={fm} to InputFieldUnion
→ InputFieldUnion reads fm.fieldType → 'select' → renders <ProFormSelect>

---

### Update — InputFieldUnion Component Map Refactoring (2026-04-20)

Refactored `InputFieldUnion` from a monolithic if/else chain to a Component Map pattern, mirroring the legacy Vue 2 `:is="getFieldType(fieldMeta)"` dynamic dispatch architecture.

**Architecture change:**
- `InputFieldUnion` is now a thin wrapper that delegates to `AbsInputDispatcher`
- `AbsInputDispatcher` (in `AbsInput.tsx`) pre-computes `ChildFieldProps` and routes to child components via `FIELD_COMPONENT_MAP[fieldType]`
- Each child component handles both `outsideProForm` (plain Ant Design) and ProForm rendering paths
- Lazy `require()` in `getFieldComponentMap()` avoids circular imports at module load time

**Files modified:**
- `src/components/control/AbsInput.tsx` — added `ChildFieldProps` interface, `LegacyAbsInput` alias, `AbsInputDispatcher` component with `FIELD_COMPONENT_MAP`, `getFieldComponentMap()` lazy loader; widened `AbsInputProps.disabled` type; default export changed to `AbsInputDispatcher`
- `src/components/control/InputFieldUnion.tsx` — simplified from 314 lines to ~45 lines; removed `OutsideProFormField` and if/else chain; now delegates to `<AbsInputDispatcher>`. Keeps `fetchFieldName`, `fetchFieldValueWrapper`, `InputFieldUnionProps` exports
- `src/components/control/InputField.tsx` — updated to accept `ChildFieldProps | AbsInputProps`; ProForm path renders `<ProFormText>`, outside path renders `<Input>`, legacy path renders `<LegacyAbsInput>`
- `src/components/control/Select2Field.tsx` — updated to accept `ChildFieldProps | Select2FieldProps`; ProForm path renders `<ProFormSelect>` with `request` prop, outside path renders `<Select>` with `useEffect` for `loadMetaRequestForSelect`; keeps legacy `<select>` HTML path
- `src/components/control/TextAreaField.tsx` — updated to accept `ChildFieldProps | TextAreaFieldProps`; ProForm path renders `<ProFormTextArea>`, outside path renders `<Input.TextArea>`; keeps legacy `<textarea>` HTML path; fixed invalid `type` attribute on `<textarea>`

**Files created:**
- `src/components/control/DateField.tsx` — new component for date fields; outsideProForm → `<DatePicker>`, ProForm → `<ProFormDatePicker>`
- `src/components/control/NumberField.tsx` — new component for number fields; outsideProForm → `<InputNumber>`, ProForm → `<ProFormDigit>` with precision/min/max/addonAfter
- `src/components/control/UploadField.tsx` — new component for upload fields; outsideProForm → `<Upload>`, ProForm → `<ProForm.Item>` + `<Upload>`

**No changes needed:**
- `AsyncField.tsx` — still renders `<InputFieldUnion>` or `<AbsInputEle>` as before
- `AbsInputEle.tsx` — still imports `InputFieldUnion` default export
- `TypeAheadField.tsx`, `MultipleValueField.tsx`, `ModalSelect2Field.tsx`, `MessageTitleField.tsx` — unchanged, future phase

#### Update log — 2026-04-23

Merged `AbsInputBase` into `InputField` and cleaned up `AbsInput.tsx`:

- **Files modified:**
  - `src/components/control/AbsInput.tsx` — removed `AbsInputBase` component, removed `AbsInputDispatcher`/`AbsInputDispatcherProps` (merged into `AbsInput`/`AbsInputProps`), removed `LegacyAbsInput` alias, removed legacy global declares no longer needed. File is now purely: types + constants + component map dispatcher (~180 lines, down from ~290)
  - `src/components/control/InputField.tsx` — absorbed the legacy HTML `<input>` rendering path from `AbsInputBase` (datepicker wiring, updatePrice handler, coreUUID). Now handles 3 paths: ChildFieldProps+outsideProForm → `<Input>`, ChildFieldProps+ProForm → `<ProFormText>`, AbsInputProps (legacy) → `<input>`

#### Update log — 2026-04-23 (restructure to mirror Vue 2 mixin architecture)

Restructured field component hierarchy to match the legacy Vue 2 mixin pattern:
- `AbsInput` = base component with shared state/methods (like Vue mixin), renders `<input>`
- `InputField` = inherits from AbsInput via `useAbsInput()` hook (like `mixins: [AbsInput]`)
- `InputFieldUnion` = dispatches to correct field component via component map (like `:is="getFieldType()"`)

**Key pattern: `useAbsInput()` custom hook replaces Vue 2 `mixins: [AbsInput]`**
- Provides: `coreUUID`, `meta`/`setMeta`, `comInputId`, `comDisabledFlag`, `handleChange`
- Plus all legacy methods: `getFieldMeta`, `getFieldName`, `blockUpdatePrice`, `enableUpdatePrice`, `checkUpdatePrice`, `clearValue`, `postUpdate`, `loadMetaData`, `updateConfig`, `executeWatchHandlerList`, `checkValidateSave`, `checkValidateSubmit`, etc.
- Each child field component calls `useAbsInput(props)` to inherit shared behavior

**Files modified:**
- `src/components/control/AbsInput.tsx` — now contains: types (`FieldMeta`, `PageMeta`, `AbsInputProps`, `ChildFieldProps`), `useAbsInput()` custom hook with all shared state/methods, `AbsInput` component rendering `<input>`, `AbsInput.FIELDTYPE` static constant. Dispatcher logic REMOVED (moved to InputFieldUnion)
- `src/components/control/InputFieldUnion.tsx` — now contains: `getFieldComponentMap()` lazy loader, `ChildFieldProps` pre-computation, component dispatch via map lookup. Static utilities `fetchFieldName`/`fetchFieldValueWrapper` kept
- `src/components/control/InputField.tsx` — simplified: calls `useAbsInput()`, ChildFieldProps dual path (outsideProForm/ProForm), legacy path delegates to `<AbsInput>`
- `src/components/control/Select2Field.tsx` — calls `useAbsInput()` for shared state; removed duplicated `coreUUID`/`meta`/`comDisabledFlag`; keeps Select2-specific `comSelectedId`, `getSelect2Settings`, `handleChange`
- `src/components/control/TextAreaField.tsx` — calls `useAbsInput()` for shared state; removed duplicated `comDisabledFlag`; keeps textarea-specific `comRowNumber`

#### Update log — 2026-04-23

**Rename Select2Field → SelectField + add loadMetaData override**

**Files modified:**
- `src/components/control/SelectField.tsx` — renamed from Select2Field.tsx; all internal names updated (`Select2Field` → `SelectField`, `Select2FieldProps` → `SelectFieldProps`); added select-specific `loadMetaData()` override that reads settings from `absInput.meta`, builds `SelectMetaOptions`, and calls `loadMetaRequestForSelect` to populate `selectOptions` state. Also added ported helper methods: `getSelect2Settings()`, `getFieldKey()`, `getFieldKeyValue()`, `checkFieldMetaConfig()`. The outsideProForm useEffect now calls `loadMetaData()` instead of inlining the logic.
- `src/components/control/InputFieldUnion.tsx` — updated lazy require from `./Select2Field` → `./SelectField`, variable `Select2Field` → `SelectField`
- `src/components/control/ModalSelect2Field.tsx` — updated import from `./Select2Field` → `./SelectField`, updated comments
- `src/components/control/AbsInput.tsx` — updated comment: `Select2Field` → `SelectField`

**Files deleted:**
- `src/components/control/Select2Field.tsx` — replaced by SelectField.tsx

#### Update log — 2026-04-23 (2)

**Remove `initDatePickerConfigure` from useAbsInput — replaced by Ant Design DatePicker**

The legacy `initDatePickerConfigure()` wired up jQuery datepicker via `ServiceDataPickerHelper.initDatePicker` with manual DOM element references and a callback to push values back into the model. In the new UI, `DateField.tsx` uses Ant Design's `<DatePicker>` and `<ProFormDatePicker>` which handle value binding natively through React's `onChange` prop — no manual DOM wiring needed.

**Files modified:**
- `src/components/control/AbsInput.tsx` — removed `initDatePickerConfigure()` function, its mounted `useEffect`, its type in `UseAbsInputReturn`, and its return entry. Removed now-unused imports: `getDomElement`, `declare ServiceDataPickerHelper`, `declare InputFieldUnion`. Removed unused `parentContent` destructure.
- `src/components/control/ModalSelect2Field.tsx` — removed unused `import SelectField` (was never used, inherited from before rename)

#### Update log — 2026-04-23 (3)

**Normalize variable naming + ESLint `id-length` rule + rename `getSelect2Settings` → `getSelectSettings`**

Added ESLint `id-length` rule (min: 3, exceptions: i/j/k/e/_/id) to enforce descriptive variable names project-wide. Fixed all short variable names in control components.

**Files modified:**
- `eslint.config.js` — added `'id-length': ['warn', { min: 3, exceptions: ['i', 'j', 'k', 'e', '_', 'id'] }]`
- `src/components/control/SelectField.tsx` — renamed `getSelect2Settings` → `getSelectSettings`; `s` → `settings`, `pc` → `parentContent`, `val` → `selectedValue`, `o` → `option` (in .map callbacks)
- `src/components/control/InputFieldUnion.tsx` — `ft` → `resolvedFieldType`, `map` → `componentMap`
- `src/components/control/AsyncField.tsx` — `c` → `colClassValue`, `fn` → `fieldName`
- `src/components/control/NumberField.tsx` — `val` → `numericValue`

#### Update log — 2026-04-23 (4)

**Rename Select2Ele → SelectEle**

**Files created:**
- `src/components/control/SelectEle.tsx` — renamed from Select2Ele.tsx; internal name `Select2Ele` → `SelectEle`

**Files deleted:**
- `src/components/control/Select2Ele.tsx` — replaced by SelectEle.tsx

#### Update log — 2026-04-23 (5)

**Rename ModalSelect2Ele → ModalSelectEle, ModalSelect2Field → ModalSelectField**

**Files created:**
- `src/components/control/ModalSelectEle.tsx` — renamed from ModalSelect2Ele.tsx; `ModalSelect2Ele` → `ModalSelectEle`, `cfg` → `config`
- `src/components/control/ModalSelectField.tsx` — renamed from ModalSelect2Field.tsx; `ModalSelect2Field` → `ModalSelectField`, `ModalSelect2FieldProps` → `ModalSelectFieldProps`, `newVal` → `newValue`

**Files deleted:**
- `src/components/control/ModalSelect2Ele.tsx` — replaced by ModalSelectEle.tsx
- `src/components/control/ModalSelect2Field.tsx` — replaced by ModalSelectField.tsx

#### Update log — 2026-04-23 (6)

**Wire TODOs in AbsInputEle.tsx + create `/wire-todos` skill**

Audited all TODO/declare entries in AbsInputEle.tsx:

| Line | Reference | Status | Action |
|------|-----------|--------|--------|
| 11 | `declare AsyncPage` | Not used in file body | Removed |
| 12 | `declare ServiceCollectionsHelper` | Not used in file body | Removed |
| 13 | `declare ServiceVueUtility` | Class not in TS | Kept |
| 14 | `declare ServicePriceUtility` | Class not in TS | Kept |
| 56 | `InputFieldUnion.fetchFieldValueWrapper` | Method exists in InputFieldUnion.tsx | Wired: imported + replaced TODO |
| 62 | `ServicePriceUtility.calculatePriceLeftIcon` | Class not in TS | Updated TODO comment |
| 86 | `parseColSize / updateColSize` | Methods exist in ServiceUtilityHelper.ts | Wired: imported + implemented legacy logic |

**Files modified:**
- `src/components/control/AbsInputEle.tsx` — wired `fetchFieldValueWrapper`, `parseColSize`, `updateColSize`; removed unused declares (`AsyncPage`, `ServiceCollectionsHelper`); updated `calculatePriceLeftIcon` TODO to note class not yet in TS

**Files created:**
- `.claude/skills/wire-todos/SKILL.md` — reusable skill: audit TODO/needs-wiring in a file, wire what exists, update comments for partial, keep unchanged if class missing

#### Update log — 2026-04-24

**/wire-todos AsyncField.tsx**

| Line | Reference | Status | Action |
|------|-----------|--------|--------|
| 11 | `declare AsyncPage` | Class exists (AsyncPage.tsx) | Removed declare; imported `getValueOrFunction` |
| 12 | `declare AsyncSection` | Class exists (AsyncSection.tsx) | Removed declare; imported `FieldMetaHead` |
| 13 | `declare ServiceVueUtility` | Not used in file body | Removed unused declare |
| 14 | `declare MaterialStockKeepUnitManager` | Class not in TS | Kept declare + updated TODO |
| 15 | `declare AbsInput` | Not used in file body | Removed unused declare |
| 71 | `AsyncPage.getValueOrFunction / checkEqualsTrue` | Both exist in TS | Wired in `checkRenderField` and `checkDisplayField` |
| 90 | `AsyncSection.fieldMetaHead.FILED_TYPE` | `FieldMetaHead` exported from AsyncSection.tsx | Wired: use `FieldMetaHead.FILED_TYPE` constant |
| 170 | `MaterialStockKeepUnitManager.calculatePrice` | Class not in TS | Updated TODO comment |

**Files modified:**
- `src/components/control/AsyncField.tsx` — wired `getValueOrFunction`, `checkEqualsTrue`, `FieldMetaHead.FILED_TYPE`; removed 4 unused declares; updated TODO comments for unwireable references

#### Update log — 2026-04-23

Ported legacy `postUpdate` and `loadMetaData` instance methods from `AsyncControlElement.js` into the React `AsyncField` component:

- Converted `AsyncField` from `React.FC` to `forwardRef<AsyncFieldHandle, AsyncFieldProps>` to expose instance methods via ref
- Added `AsyncFieldHandle` interface with `postUpdate` and `loadMetaData` signatures
- Added `postUpdate()` — merges settings with `AsyncPageMethod.POST_UPDATE`, downstream calls left as TODO stubs (`ServiceVueUtility.batchExecuteSubRefMethod`, `ServiceFieldMetaUtility.processFieldMetaWatchList`)
- Added `loadMetaData()` — merges settings with `AsyncPageMethod.LOAD_META_DATA`, downstream call left as TODO stub
- Wired `useImperativeHandle` to expose both methods on the component ref
- Removed legacy raw JavaScript block (lines 112-142) that was pasted into the file but never syntactically valid
- Cleaned up unused imports (`useEffect`, `useRef`), prefixed unused vars (`_mergedSettings`, `_getElementType`, `_controlAreaArray`, `_expandAreaConfig`)
- Fixed `forwardRef` closing bracket (`});` instead of `};`)

**Files modified:**
- `src/components/control/AsyncField.tsx` — forwardRef conversion, `postUpdate`/`loadMetaData` methods, removed raw legacy JS block, cleaned unused imports/vars

#### Update log — 2026-04-24

Wired `forwardRef` + `useImperativeHandle` through the entire field component chain, implementing the React equivalent of legacy `batchExecuteSubRefMethod`. This enables parent components (AsyncField) to call `postUpdate()`, `loadMetaData()`, and `updateConfig()` on all child field components via refs.

**Call chain:** `AsyncField` → `AbsInputEle` → `InputFieldUnion` → leaf field component (InputField / SelectField / TextAreaField / DateField / NumberField / UploadField)

**Files modified:**
- `src/components/control/AbsInput.tsx` — added `FieldHandle` interface export (postUpdate, loadMetaData, updateConfig signatures)
- `src/components/control/InputField.tsx` — wrapped in `forwardRef<FieldHandle>`, added `useImperativeHandle` delegating to `useAbsInput` stubs
- `src/components/control/SelectField.tsx` — wrapped in `forwardRef<FieldHandle>`, added `useImperativeHandle` with real `postUpdate` → `updateConfig` → `loadMetaData` chain (matches legacy Select2Field), `updateConfig` merges fieldMeta/settings then calls `loadMetaData`
- `src/components/control/TextAreaField.tsx` — wrapped in `forwardRef<FieldHandle>`, added `useImperativeHandle` delegating to `useAbsInput` stubs
- `src/components/control/DateField.tsx` — wrapped in `forwardRef<FieldHandle>`, no-op stubs (matches legacy base AbsInput)
- `src/components/control/NumberField.tsx` — wrapped in `forwardRef<FieldHandle>`, no-op stubs
- `src/components/control/UploadField.tsx` — wrapped in `forwardRef<FieldHandle>`, no-op stubs
- `src/components/control/InputFieldUnion.tsx` — wrapped in `forwardRef<FieldHandle>`, passes `ref` through to dynamically dispatched child component
- `src/components/control/AbsInputEle.tsx` — wrapped in `forwardRef<FieldHandle>`, collects child `InputFieldUnion` refs via Map, delegates `postUpdate`/`loadMetaData`/`updateConfig` to all children
- `src/components/control/AsyncField.tsx` — added `updateConfig` to `AsyncFieldHandle`, collects child refs (AbsInputEle + InputFieldUnion), replaced TODO stubs with ref iteration for `postUpdate`/`loadMetaData`/`updateConfig`

#### Update log — 2026-04-24 (upper-half postUpdate chain)

Wired the upper half of the `postUpdate` cascade: from `PurchaseContractEditPage` → `AsyncEditorPage` → `AsyncPage` → `AsyncSectionFactory` → `AsyncEditSection` → `AsyncEditUnion` → `AsyncField`.

After this change, calling `pageRef.current?.postUpdate()` in the edit page triggers the full cascade down to every leaf field (SelectField, InputField, etc.). This is the React equivalent of the legacy `getPageRef().postUpdate()` → `batchExecuteSubRefMethod` tree walk.

**Files modified:**
- `src/components/page/AsyncEditUnion.tsx` — wrapped in `forwardRef<FieldHandle>`, holds `AsyncFieldHandle` ref, `useImperativeHandle` delegates `postUpdate`/`loadMetaData`/`updateConfig` to `AsyncField` child
- `src/components/page/AsyncEditSection.tsx` — wrapped in `forwardRef<FieldHandle>`, collects refs from main `AsyncEditUnion` + embedded tab unions via Map, delegates to all children
- `src/components/page/AsyncSectionFactory.tsx` — wrapped in `forwardRef<FieldHandle>`, passes `ref` through to `AsyncEditSection` (EDIT sections only)
- `src/components/page/AsyncPage.tsx` — wrapped in `forwardRef<AsyncPageHandle>`, collects refs from all `AsyncSectionFactory` children (both tab and non-tab sections) via Map, exports `AsyncPageHandle` type
- `src/components/page/AsyncEditorPage.tsx` — wrapped in `forwardRef<AsyncPageHandle>`, passes `ref` through to `AsyncPage`, re-exports `AsyncPageHandle`
- `src/pages/logistics/purchaseContract/PurchaseContractEditPage.tsx` — added `pageRef: useRef<AsyncPageHandle>`, passes to `<AsyncEditorPage ref={pageRef}>`, added `useEffect` that calls `pageRef.current?.postUpdate()` after `loading` becomes false (equivalent of legacy `postUpdateUIModelBasic()` → `getPageRef().postUpdate()`)

#### Update log — 2026-04-24 (wire-todos: AbsInput.tsx)

Audited and resolved TODO/declare entries in `AbsInput.tsx`.

**Files modified:**
- `src/components/control/AbsInput.tsx` — removed unused `declare const AsyncPage: any`, wired `clearValue` (calls `onInput` with initValue), confirmed `postUpdate`/`loadMetaData`/`updateConfig` as intentional no-ops matching legacy base class, wired `addHandler` using `ServiceCollectionsHelper.mergeList`, wired `disableHandler`/`enableWatchHandler` using `ServiceCollectionsHelper.filterArray`, updated TODO comments for `executeWatchHandlerList` (needs ServiceFieldMetaUtility, not in TS), `checkValidateSave`/`checkValidateSubmit`/`checkValidateInput` (needs ServiceValidatorHelper, not in TS), `equalsTrueByVueMethod` (method not in TS)

| Line | Reference | Status | Action |
|------|-----------|--------|--------|
| 6 | `AsyncPage` declare | Class exists but unused in code body | Removed declare |
| 5 | `// TODO: legacy globals` | No globals left | Removed |
| 177 | `AsyncPage.equalsTrueByVueMethod` | Method not in TS | Updated TODO |
| 214 | `clearValue` | Logic matches `onInput?.(initialValue)` | Wired |
| 218 | `postUpdate` | Legacy is empty no-op | Wired (intentional no-op) |
| 223 | `loadMetaData` | Legacy is empty no-op | Wired (intentional no-op) |
| 228 | `updateConfig` | Legacy is empty no-op | Wired (intentional no-op) |
| 233 | `executeWatchHandlerList` | Needs ServiceFieldMetaUtility (not in TS) | Updated TODO |
| 237 | `addHandler` | ServiceCollectionsHelper.mergeList exists | Wired |
| 240 | `disableHandler` | ServiceCollectionsHelper.filterArray exists | Wired |
| 243 | `enableWatchHandler` | ServiceCollectionsHelper.filterArray exists | Wired |
| 249 | `checkValidateSave` | Needs ServiceValidatorHelper (not in TS) | Updated TODO |
| 253 | `checkValidateSubmit` | Needs ServiceValidatorHelper (not in TS) | Updated TODO |
| 259 | `checkValidateInput` | Needs ServiceValidatorHelper (not in TS) | Updated TODO |

---

### C14 — `outsideProForm`: What It Is, When It Triggers, and How It Works (2026-04-24)

#### Question

What is the `outsideProForm` flag in `ChildFieldProps`, how is its value determined, and how do field components use it?

#### Answer

`outsideProForm` is a boolean on the `ChildFieldProps` interface (`AbsInput.tsx:73`) that tells each leaf field component whether it's rendering **inside** a `<ProForm>` context or **outside** one. This single flag switches every field component between two completely different rendering paths.

##### How the value is determined

The chain starts from `sectionMeta.parentContentPath`, set in each editor controller's `getDefaultPageMeta()`:

```
AsyncEditUnion  reads  sectionMeta.parentContentPath
    ↓
AsyncField      checks if parentContentPath !== undefined
    ↓
    ├── YES → ProForm path: renders InputFieldUnion directly with namePath=[parentContentPath, fieldName]
    │         InputFieldUnion sets outsideProForm = false (because namePath is provided)
    │
    └── NO  → Legacy path: renders AbsInputEle → InputFieldUnion WITHOUT namePath
              InputFieldUnion sets outsideProForm = true (because namePath is absent)
```

The critical line in `InputFieldUnion.tsx:102`:
```ts
const outsideProForm = !namePath;
```

##### Two rendering paths in each field component

Every field component (`InputField`, `SelectField`, `TextAreaField`, `DateField`, `NumberField`, `UploadField`) has an identical branching pattern:

```ts
if ('outsideProForm' in props) {
    const { outsideProForm, ... } = props as ChildFieldProps;

    if (outsideProForm) {
        // Path A: Standalone — plain Ant Design component in a manual wrapper div
        return (
            <div className="ant-form-item">
                <label>{fieldLabel}</label>
                <Input value={...} onChange={...} />  // or Select, DatePicker, etc.
            </div>
        );
    }

    // Path B: ProForm — Ant Design Pro form component with validation/layout
    return (
        <ProFormText name={fieldNameOrPath} label={fieldLabel} rules={fieldRules} ... />
    );
}
```

| Aspect | `outsideProForm = false` (ProForm) | `outsideProForm = true` (Standalone) |
|--------|-------------------------------------|--------------------------------------|
| **Trigger** | `parentContentPath` is set (including `''`) | `parentContentPath` is `undefined` |
| **Parent context** | Inside `<ProForm>` from AsyncPage | No form context (AbsInputEle path) |
| **Components used** | `ProFormText`, `ProFormSelect`, `ProFormDatePicker`, `ProFormDigit`, `ProFormTextArea` | `Input`, `Select`, `DatePicker`, `InputNumber`, `Input.TextArea` |
| **Validation** | ProForm `rules` prop applied | No validation |
| **Form `name` path** | `['purchaseContractUIModel', 'signDate']` | Not applicable |
| **Value binding** | Managed by ProForm (from `initialValues`) | Manual `value` + `onChange` props |
| **Layout** | ProForm handles label/field alignment | Manual `<div>` + `<label>` wrapper |

##### When each path is used

**ProForm path** (`outsideProForm = false`):
- Standard editor page sections — every section in `getDefaultPageMeta()` that sets `parentContentPath` (e.g. `'purchaseContractUIModel'`, `'purchaseToOrgUIModel'`).
- This is the primary rendering path for all modern edit pages.

**Standalone path** (`outsideProForm = true`):
- Fields rendered inside modals (e.g. `DocumentItemMultiSelect`) that have no parent `<ProForm>`.
- Fields rendered via the legacy `AbsInputEle` code path when `parentContentPath` is not configured on the section.
- Any context where a `<ProForm>` wrapper is absent.

##### Why this design exists

The legacy Vue 2 UI rendered all fields via AbsInputEle using standalone inputs. The React migration introduced ProForm for structured form handling (validation, layout, nested namepaths). Rather than forcing a complete rewrite of all rendering paths, the `outsideProForm` flag allows both modes to coexist:

1. New edit pages use ProForm (fields inside `<ProForm>` → `parentContentPath` set → `outsideProForm = false`)
2. Modals and legacy contexts use standalone inputs (no `<ProForm>` → `parentContentPath` absent → `outsideProForm = true`)

This dual-mode design means the same field component (e.g. `SelectField`) works correctly in both contexts without separate component variants.

##### Key files

- `AbsInput.tsx:73` — `ChildFieldProps` interface with `outsideProForm: boolean`
- `InputFieldUnion.tsx:102` — `const outsideProForm = !namePath;` (the decision point)
- `AsyncField.tsx:204-242` — branches on `parentContentPath !== undefined` to choose ProForm vs AbsInputEle path
- `AsyncEditUnion.tsx:61` — reads `parentContentPath` from `sectionMeta`
- Each field component: `InputField.tsx:19`, `SelectField.tsx:124`, `TextAreaField.tsx:25`, `DateField.tsx:25`, `NumberField.tsx:30`, `UploadField.tsx:26` — the `if (outsideProForm)` branch

##### Why modals must use `outsideProForm = true` (not a ProForm limitation per se)

The reason modals cannot use ProForm fields is **not an HTML limitation of `<dialog>`** — it's a **React Context portaling issue** specific to Ant Design's `<Modal>`.

**The problem:** Ant Design `<Modal>` renders its content via `ReactDOM.createPortal()` into `document.body`. This moves the modal's DOM subtree **outside** the DOM hierarchy where the parent `<ProForm>` lives:

```
DOM tree:
  <div id="root">
    <AsyncPage>
      <ProForm>              ← ProForm context provider lives here
        <Tabs>
          <AsyncSectionFactory>
            ...fields...     ← ProForm fields work here (inside context)
          </AsyncSectionFactory>
        </Tabs>
      </ProForm>
    </AsyncPage>
  </div>

  <div class="ant-modal-root">   ← portaled to document.body by Ant Design Modal
    <DocumentItemMultiSelectModal>
      <AsyncEditUnion>
        <AsyncField>
          <InputFieldUnion>
            <SelectField>     ← ProFormSelect would FAIL here (no ProForm context ancestor)
          </InputFieldUnion>
        </AsyncField>
      </AsyncEditUnion>
    </DocumentItemMultiSelectModal>
  </div>
```

Because portaled content is outside the `<ProForm>` React Context tree:
- `ProFormSelect`, `ProFormText`, etc. cannot find their parent `FormInstance` — they rely on Ant Design's `Form.useFormInstance()` which traverses the React Context chain
- ProForm validation, `name` path binding, and `initialValues` all break silently

**The solution:** Modal section configs intentionally omit `parentContentPath` (`DocumentItemMultiSelect.getDefaultSectionMeta()` returns no `parentContentPath`), which triggers the standalone path: `AsyncField → AbsInputEle → InputFieldUnion(no namePath) → outsideProForm = true → plain `<Select>`, `<Input>`, etc.`

This is not a workaround — it's the correct architecture. Modal fields don't participate in form submission; they collect selection data that's applied back to the parent form via imperative callbacks (e.g. `onSrcItemsLoaded`, `refreshEditView`).

**Could a modal use its own `<ProForm>`?** Theoretically yes — a modal could wrap its content in a separate `<ProForm>` instance. But the `DocumentItemMultiSelect` modal doesn't need form-level validation or submission — it's a selection UI that writes directly to `parentContent`. Adding a nested ProForm would add complexity without benefit.

#### Update log — 2026-04-24 (equalsTrueByControllerMethod)

Migrated `AsyncPage.equalsTrueByVueMethod` from legacy `AsyncPageElement.js:5012-5037` to new UI as `equalsTrueByControllerMethod`.

**Files modified:**
- `src/components/page/AsyncPage.tsx` — added `equalsTrueByControllerMethod` export (renamed from `equalsTrueByVueMethod`), imported `checkEqualsTrue` from ServiceUtilityHelper; uses `getControllerMethod` for string method name lookup, left TODO for `getServiceMethod` (not yet in TS)

#### Update log — 2026-04-24 (getVueMethod → getControllerMethod rename)

Removed all `getVueMethod` exports and aliases. All call sites now use `getControllerMethod` directly.

**Files modified:**
- `src/components/page/AsyncPage.tsx` — removed deprecated `getVueMethod` wrapper export
- `src/components/page/AsyncPageHelpers.ts` — removed deprecated `getVueMethod` alias export
- `src/components/page/AsyncUnion.tsx` — replaced local `getVueMethod` wrapper with `resolveControllerMethod` using imported `getControllerMethod`, replaced `executeVueMethod` with `executeControllerMethod`, replaced `AsyncPage.equalsTrueByVueMethod` calls with imported `equalsTrueByControllerMethod`, removed unused `declare const AsyncPage: any`
- `src/components/control/PanelProcessButtonArray.tsx` — updated TODO comment to reference `getControllerMethod` instead of `AsyncPage.getVueMethod`

#### Update log — 2026-04-24 (extract FieldMeta & PageMeta from AbsInput.tsx)

Extracted `FieldMeta` and `PageMeta` interfaces from `AbsInput.tsx` into dedicated files.

**Files created:**
- `src/components/control/FieldMeta.ts` — `FieldMeta` interface (moved from AbsInput.tsx)
- `src/components/control/PageMeta.ts` — `PageMeta` interface (moved from AbsInput.tsx)

**Files modified:**
- `src/components/control/AbsInput.tsx` — removed inline `FieldMeta`/`PageMeta` definitions, added `import type` + `export type` re-exports for backward compatibility
- `src/components/page/AsyncEditUnion.tsx` — updated import: `FieldMeta` from `../control/FieldMeta`
- `src/components/control/AsyncField.tsx` — split import: `FieldMeta` from `./FieldMeta`, `FieldHandle` from `./AbsInput`
- `src/components/control/AsyncForeField.tsx` — updated import: `FieldMeta` from `./FieldMeta`
- `src/components/control/AbsInputEle.tsx` — split import: `FieldMeta`/`PageMeta` from new files, `FieldHandle` from `./AbsInput`
- `src/components/control/InputFieldUnion.tsx` — split import: `FieldMeta`/`PageMeta` from new files, rest from `./AbsInput`
- `src/components/control/ModalSelectEle.tsx` — updated import: `FieldMeta` from `./FieldMeta`
- `src/components/control/TypeAheadEle.tsx` — updated import: `FieldMeta` from `./FieldMeta`
- `src/components/control/MessageTitleEle.tsx` — updated import: `FieldMeta` from `./FieldMeta`

#### Update log — 2026-04-24 (ServiceValidatorHelper migration)

Migrated `ServiceValidatorHelper` from legacy `ServiceHttpRequestHelper.js` lines 8140-8479 to TypeScript.

**Files created:**
- `src/services/ServiceValidatorHelper.ts` — constants (`DEF_VALID_TYPE`, `VALID_VALUE_TYPE`, `label`), interfaces (`ValidType`, `ValidateCheckConfig`, `ValidateCheckSettings`), pure logic methods (`mapValidToValueType`, `defaultValidateCheckArray`, `getDefValidateCheckArray`, `defaultValidateCheck`, `_checkLogicCore`, `_getNumberValue`). Uses `checkEmptyValue` and `checkHTTPResponseCode` from ServiceUtilityHelper. DOM/jQuery methods (`throwErrorMessageWrap`, `_clearMessageWrap`, `_conbineErrorMessage`, `_getI18nCommonMap`, `_getValueContainer`, `_getValueContainerUnion`) left as TODO stubs — legacy uses jQuery toggleClass + ServiceMessageBarHelper, which are replaced by Ant Design form rules in the new UI.

#### Update log — 2026-04-24 (wire-todos: AbsInput.tsx round 2)

Second pass of wire-todos on `AbsInput.tsx` after `ServiceValidatorHelper` and `equalsTrueByControllerMethod` were migrated.

**Files modified:**
- `src/components/control/AbsInput.tsx` — wired `comDisabledFlag` with `equalsTrueByControllerMethod` (replaces TODO for `AsyncPage.equalsTrueByVueMethod`), wired `checkValidateInput` with `ServiceValidatorHelper.getDefValidateCheckArray` + `DEF_VALID_TYPE.NON_EMPTY` + `checkEqualsTrue`, removed TODO comments for `checkValidateSave`/`checkValidateSubmit`/`checkValidateInput`

| Line | Reference | Status | Action |
|------|-----------|--------|--------|
| 159 | `AsyncPage.equalsTrueByVueMethod` | Now `equalsTrueByControllerMethod` in AsyncPage.tsx | **Wired** |
| 215 | `ServiceFieldMetaUtility.executeWatchHandlerUnion` | Class not in TS | Kept TODO unchanged |
| 261 | `ServiceValidatorHelper` in checkValidateSave | Now exists in ServiceValidatorHelper.ts | **Wired** (delegates to checkValidateInput) |
| 267 | `ServiceValidatorHelper` in checkValidateSubmit | Now exists in ServiceValidatorHelper.ts | **Wired** (delegates to checkValidateInput) |
| 273 | `ServiceValidatorHelper.getDefValidateCheckArray` | Now exists in ServiceValidatorHelper.ts | **Wired** with full legacy logic |

---

### Update — Move postUpdate-after-load to AsyncEditorPage + Migrate ServiceFieldMetaUtility (2026-04-24)

#### Part 1: Centralize postUpdate-after-load lifecycle in AsyncEditorPage

In the legacy UI, `ServiceEditorControlHelper.postUpdateUIModelBasic()` (ServiceUiController.js:1706) cascades `getPageRef().postUpdate()` after data loads, which triggers all select fields to fetch their metadata options. This was duplicated inline in `PurchaseContractEditPage.tsx` as a `useEffect` and was missing entirely from `PurchaseContractItemEditPage.tsx`.

Moved the lifecycle into `AsyncEditorPage` via a new `loading` prop — when it transitions from `true` → `false`, the component auto-triggers `postUpdate()` on its inner `AsyncPage`.

#### Part 2: Migrate ServiceFieldMetaUtility (AsyncControlElement.js L3365-3832)

Ported the field-level watch/handler framework to TypeScript. This powers `setAutoValue`, `updateFieldConfig`, `watchCheckValue`, and `submitCheckValue` — reactive field behaviors declared in `getDefaultPageMeta()`. All 16 static methods/constants ported faithfully. Replaced legacy `$http` with `apiGet`/`apiPost` from `apiClient.ts`. `filterInputInsList` adapted from Vue `$children` traversal to React ref-based lookup via `fieldRefMap`.

**Files modified:**
- `IntelligentUI/src/components/page/AsyncEditorPage.tsx` — added `loading` prop, `useEffect` for auto-`postUpdate()`, `Spin` loading guard, `useImperativeHandle` forwarding
- `IntelligentUI/src/pages/logistics/purchaseContract/PurchaseContractEditPage.tsx` — removed inline `useEffect`/`Spin` loading logic, pass `loading` prop to `AsyncEditorPage`
- `IntelligentUI/src/pages/logistics/purchaseContract/PurchaseContractItemEditPage.tsx` — removed `Spin` loading guard, pass `loading` prop to `AsyncEditorPage` (fixes missing `postUpdate` bug)

**Files created:**
- `IntelligentUI/src/services/ServiceFieldMetaUtility.ts` — TS migration of legacy `ServiceFieldMetaUtility` (field watch/handler framework)

#### Update log — 2026-04-24 (wire-todos: AbsInput.tsx)

Third pass of wire-todos on `AbsInput.tsx` after `ServiceFieldMetaUtility` was migrated to TypeScript.

**Files modified:**
- `src/components/control/AbsInput.tsx` — wired `executeWatchHandlerList` with `ServiceFieldMetaUtility.checkHandlerEnable` + `ServiceFieldMetaUtility.executeWatchHandlerUnion` (replaces TODO), added import for `ServiceFieldMetaUtility`
- `src/services/ServiceFieldMetaUtility.ts` — fixed two TypeScript errors (`callbackBody` typing, `enableFlag === false` comparison)

| Line | Reference | Status | Action |
|------|-----------|--------|--------|
| 223 | `ServiceFieldMetaUtility.executeWatchHandlerUnion` | Class+method now exist in ServiceFieldMetaUtility.ts | **Wired** |
