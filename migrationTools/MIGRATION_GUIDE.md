# UI Resource Migration Guide

This guide walks through migrating a legacy document module to IntelligentUI using the migration tools in this folder.

---

## Step 1 — Copy Service UI Model types

Manually copy the Service UI Model interface(s) from the corresponding legacy `***Content.ts` file in `src/types/`.

- Source reference: `src/types/logistics/InquiryContent.ts` (for `MODEL_CAT_DOCUMENT`)
- Create `src/types/{groupId}/{TargetEntity}Content.ts`
- Rename all type names: `InquiryServiceUIModel` → `{TargetEntity}ServiceUIModel`, `InquiryUIModel` → `{TargetEntity}UIModel`, item types accordingly
- Keep field names identical to the legacy source — do not add or remove fields at this step

---

## Step 2 — Run the migration tools to generate code resources

Edit `MigrationEntrance.java` (lines `rootNodeInstId`, `itemNodeInstId`, `groupId`, `modelCategory`):

```java
String rootNodeInstId = "inboundDelivery";   // camelCase target entity name
String itemNodeInstId = "inboundItem";        // camelCase item entity name, or null if no items
String groupId        = "logistics";          // target module folder name
// modelCategory options:
//   MODEL_CAT_DOCUMENT       — full document with item list (e.g. Inquiry, PurchaseContract)
//   MODEL_CAT_DUMMY_DOCUMENT — simple document without item list (e.g. Material)
//   MODEL_CAT_SER_ENTITY     — service entity without doc workflow (e.g. StandardMaterialUnit)
```

Then run from the `migrationTools` directory:

```bash
./run.sh
```

This generates the following files (skipping any that already exist):

| Generated file | Purpose |
|---|---|
| `src/pages/{groupId}/{entity}/{Entity}ListPage.tsx` | List page shell |
| `src/pages/{groupId}/{entity}/{Entity}ListController.tsx` | List controller class |
| `src/pages/{groupId}/{entity}/use{Entity}ListController.ts` | List controller hook |
| `src/pages/{groupId}/{entity}/{Entity}EditPage.tsx` | Edit page shell |
| `src/pages/{groupId}/{entity}/{Entity}EditController.tsx` | Edit controller class |
| `src/pages/{groupId}/{entity}/use{Entity}EditController.ts` | Edit controller hook |
| `src/pages/{groupId}/{entity}/{Item}Panel.tsx` | Item sub-panel (if itemNodeInstId set) |
| `src/pages/{groupId}/{entity}/{Item}Controller.tsx` | Item controller class (if itemNodeInstId set) |
| `src/pages/{groupId}/{entity}/use{Item}Controller.ts` | Item controller hook (if itemNodeInstId set) |
| `src/services/{groupId}/{Entity}Manager.ts` | Service manager |
| `src/i18n/locales/en/{groupId}/{Entity}.json` | English i18n labels |
| `src/i18n/locales/zh/{groupId}/{Entity}.json` | Chinese i18n labels |

---

## Step 3 — Update edit page sections and fields

Open `{Entity}EditController.tsx` and update:

- Tab/section names to match the target entity's legacy editor (`***Editor.js` → `getDefaultPageMeta()`)
- Field definitions inside each section: field keys, field types, validation rules
- Reference the target entity's `***Content.ts` UIModel fields for the correct field names

---

## Step 4 — Update search content in the List controller

Open `{Entity}ListController.tsx` and update `getDefaultPageMeta()` / search form:

- Search field keys and labels to match the target entity's legacy list (`***List.js`)
- Default search parameter values
- Date range fields, status dropdowns, and any entity-specific filters

---

## Step 5 — Update table column definitions in the List controller

In the same `{Entity}ListController.tsx`, update `getTableConfig()`:

- Column keys, titles, and widths to match the target entity
- Render functions for status tags, date formatting, amounts, etc.
- `rowKey` accessor — ensure it points to the correct UIModel UUID field

---

## Step 6 — Update status and action code configuration

If the target entity has a document workflow, update in `{Entity}EditController.tsx` or the manager:

- Status constants (`INITIAL`, `SUBMITTED`, `APPROVED`, etc.) — verify which statuses apply
- `getActionCodeMatrix()` / action button visibility rules — compare with the legacy `getDefaultPageMeta()` `actionCodeHeader` array
- Any custom status label or colour overrides in `{Entity}Manager.ts`

---

## Step 7 — Check for special methods in each controller

Review each generated controller against its legacy counterpart:

- Open the legacy `***List.js` / `***Editor.js` and scan for methods beyond the standard mixin
- Any method not present in the reference template (`Inquiry*`) will have been dropped — re-add them manually
- Common examples: custom `loadData` hooks, cross-document selection callbacks, computed totals, special save/submit overrides
- Use `// TODO: legacy <methodName>` stubs if the full implementation is deferred
