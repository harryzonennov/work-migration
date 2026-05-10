---
name: compare-component
description: Compare a legacy Vue 2 component class with its new React/TS equivalent, outputting only functional differences (not framework differences).
args: legacyFile legacyClassName legacyLineStart-legacyLineEnd newFile [outputFile]
---

# Compare Component

Generate a differences-only comparison between a legacy Vue 2 component class and its new React/TypeScript equivalent.

## Input

`$ARGUMENTS` = space-separated values:

1. **Legacy file path** — relative to `/Users/I043125/work/ThorSalesDistributionUI/admin/` or absolute
2. **Legacy class name** — e.g. `Select2Field`
3. **Legacy line range** — e.g. `562-891`
4. **New file path** — relative to `/Users/I043125/work2/IntelligentUI/src/` or absolute
5. **(Optional) Output file** — if not provided, the output file is resolved by the lookup in Step 0

Example:
```
/compare-component js/component/basicElements/AsyncControlElement.js Select2Field 562-891 components/control/SelectField.tsx
```

## Steps

### 0. Resolve output file

Before reading anything, determine where to write the output:

1. List all `.md` files under `/Users/I043125/work-migration/docs/ComparasionComponents/`.
2. For each file, search (case-insensitive) for the legacy class name (`$ARGUMENTS[1]`) anywhere in the file content.
3. **If a match is found** — that file is the output file. The existing section for this class will be **replaced in place** (see Step 4).
4. **If no match is found** — use the explicit `$ARGUMENTS[4]` if provided, otherwise fall back to `/Users/I043125/work-migration/docs/ComparasionComponents/InputFields.md`. The new section will be **appended**.

Remember whether the class was found (update mode) or not (append mode) — it controls Step 4.

### 1. Read the exclude list

Read the resolved output file and extract the `*Exclude list*:` line near the top. These patterns must be skipped throughout the comparison.

**Default excludes** (always applied even if not in the file):
- `parentVue→parentController` — rename-only, not a functional difference
- `Vue.extend↔forwardRef framework differences` — component definition syntax, mixin↔hook, $refs↔useImperativeHandle, `Vue.component()` registration
- `Template↔JSX rendering differences` — template vs JSX syntax, CSS framework (Bootstrap→Ant Design)
- `$emit→callback props` — Vue event emission vs React callback props
- `v-model→controlled input` — Vue two-way binding vs React controlled component
- **`$refs`-returning methods** — any method whose entire body is `return vm.$refs[...]` or `return this.$refs[...]` is pure Vue 2 ref plumbing. In React, child handles are exposed via `useImperativeHandle` + `forwardRef`; there is no equivalent imperative accessor method. Always skip.
- **Type-only differences** — e.g. `Object` → `Record<string, unknown>`, `String` → `string`, `[String, Number]` → `string | number`
- **Pure-delegation forwardRef methods** — if a legacy method only delegates to child refs via `ServiceVueUtility.batchExecuteSubRefMethod` (no additional logic), skip it. In the new UI, this delegation is handled by `AsyncField`'s `useImperativeHandle` which iterates `childRefs` and calls each child directly. The intermediate component does not need to re-implement the same pass-through.
- **`initSubComponents` / `inputSubComponents` methods and props** — `initSubComponents` registers child Vue components via `Vue.component()`; `inputSubComponents` / `inputSubComponents***` are the sub-component registry props. Both are legacy framework binding; in React, components are imported directly. Always skip.
- **`initDatePickerConfigure` / `DatePickerInit`** — jQuery datepicker wiring. Replaced entirely by Ant Design `<DatePicker>` / `<ProFormDatePicker>` in the new UI. Always skip.
- **Instance methods inlined into render** — if a legacy instance method (e.g. `getFieldType`, `checkLowFlag`, `checkHighFlag`, `genSubFieldRefId`) is inlined as a local `const`, `useMemo`, or inline expression inside the React render body with identical logic, skip it. Moving callable methods into render scope is a Vue→React structural difference, not a functional one. Only report if the logic itself changed.
- **Legacy-only members not used externally** — if a method or property exists in legacy but is only used internally for framework plumbing (e.g. `initCoreUUID`, `coreUUID`) and has no equivalent in the new UI, skip it. Only report legacy-only members that represent real business logic or are called by external consumers.
- **Instance methods shadowed by a static of the same name** — if a class has both an instance method and a static method with the same name (e.g. `fetchFieldName`), and the instance method is **never called** in the legacy codebase (grep for `vm.methodName` and `this.methodName` returns no results), treat it as internal plumbing and skip it. Only the static version's behaviour matters to callers.

### 2. Read both files

- Read the legacy file at the specified line range
- Read the new file entirely
- Extract all members: **props, data/state, computed, methods, lifecycle hooks, static members, template/rendering**

### 3. Compare and filter

For each member found in either file:

- **Skip** if it matches an exclude-list pattern
- **Skip** if behavior is identical (same logic, same signature, no functional change)
- **Keep** if any of:
  - **New only** — exists in new but not in legacy
  - **Legacy only** — exists in legacy but not in new
  - **Changed return type** — same name but different return
  - **Changed logic** — same name but different behavior
  - **Partial port** — exists in new but with TODOs or missing functionality
  - **Different inheritance** — parent class chain differs

### 4. Write output

Use the section format below. Whether to append or update depends on what Step 0 found:

- **Append mode** (class not previously documented): append the new section at the end of the output file.
- **Update mode** (class already documented in the file): locate the existing section by its `## Class Name:` heading and replace everything from that heading down to (but not including) the next `---` divider or end-of-file, whichever comes first. Preserve all other sections in the file unchanged.

Use this section format:

```markdown
---

## Class Name: LegacyName (legacy) / NewName (new)

**Legacy file**: `relative/path` (lines X–Y)
**New file**: `relative/path`

### [Category Name]

| [Column] | Legacy | New | Status | Notes |
|----------|--------|-----|--------|-------|
| `memberName` | description | description | **Legacy only** | explanation |
```

**Categories** (include only if they have differences):
- **Inheritance** — if parent chain differs
- **Props** — new-only or changed props
- **State** — new-only state variables
- **Computed Properties** — different or missing computed
- **Methods** — different, missing, or partially ported methods
- **Rendering** — new rendering paths (outsideProForm, fallback)
- **TODOs / Not Ported** — table of unresolved gaps with descriptions
- **Resolved** — items confirmed fully covered by the new UI; kept for traceability using strikethrough so they render greyed-out. Use this format:

```markdown
### Resolved

Items confirmed fully covered by the new UI — kept for traceability, no action needed.

| ~~Member~~ | ~~Why covered~~ |
|------------|-----------------|
| ~~`memberName`~~ | ~~One-sentence reason why the legacy gap is fully handled in new UI.~~ |
```

  Only add a row here when you have verified (by reading both files) that the new UI fully handles the case. Never add a row speculatively.

**Status values**: `**Legacy only**`, `**New only**`, `Exists in both`, `**Different**`, `**Not ported**`, `**Partial**`, `~~Resolved~~`

### 5. Update migration log

Append to `/Users/I043125/work-migration/docs/MIGRATION_CONVERSATIONS.md` per the After-Update Summary Rule.

## Rules

- **Differences only** — NEVER include rows where behavior is identical between legacy and new. This includes `Exists in both` rows where the member has been fully migrated with no remaining gap — once a member is equivalent in both, omit it entirely. Exception: items that were previously a gap and are now confirmed fully covered belong in the **Resolved** section, not removed silently.
- **No framework differences** — skip Vue↔React syntax, $emit↔callback, v-model↔controlled, Object↔Record type changes, $refs↔useRef, $nextTick↔setTimeout, $set↔setState
- **Follow the exclude list** — check both the file's exclude list AND the default excludes before outputting any row
- **Do NOT modify** the legacy or new source files — this is a read-only comparison
- **Append or update, never overwrite the whole file** — in append mode add only the new section; in update mode replace only the matching section
- Follow the project's variable naming convention: no 1-2 character variable names
