# Input Field Component Comparison: Legacy vs New UI

*Note*: This comparison focuses on **differences only** —
identical members between legacy and new UI are omitted.

**Legacy file**: `admin/js/component/basicElements/AsyncControlElement.js` (lines 164–548)
**New file**: `IntelligentUI/src/components/control/AbsInput.tsx`

*Exclude list*: see skill default excludes (all entries promoted to universal defaults)


---

## Class Name: InputFieldUnion

**Legacy file**: `admin/js/component/basicElements/AsyncControlElement.js` (lines 1227–1517)
**New file**: `IntelligentUI/src/components/control/InputFieldUnion.tsx`

### Props

| Prop | Legacy | New | Status | Notes |
|------|--------|-----|--------|-------|
| `resolvedLabel` | — | `string` | **New only** | Pre-resolved i18n label passed from parent; legacy resolved labels inside the template |
| `namePath` | — | `string \| string[]` | **New only** | ProForm field path for nested data binding; derived variables `fieldNameOrPath` and `fieldRules` are also new and passed to sub-components |

### Computed Properties

| Computed | Legacy | New | Status | Notes |
|----------|--------|-----|--------|-------|
| `comFieldValue` (getter) | Delegates to `fetchFieldValueWrapper({vm})` which checks `fieldMeta.fieldValue` (truthy) before `fetchObjValueByPath` | `fieldValue` useMemo checks `valueCallback` then goes directly to `fetchObjValueByPath`; `fieldMeta.fieldValue` is not checked in the render path | **Different** | `fieldMeta.fieldValue` is honoured in the exported standalone `fetchFieldValueWrapper` but skipped in the component's own render — fields that set `fieldMeta.fieldValue` directly will not reflect it in the rendered input |

### Static Methods

| Method | Legacy | New | Status | Notes |
|--------|--------|-----|--------|-------|
| `InputFieldUnion.setFieldValueWrapper(oSettings)` | Takes `vm`; falls back to `vm.fieldMeta`/`vm.parentContent` when omitted; checks `blockEvent` via `ServiceUtilityHelper.checkEqualsTrue`; emits `vm.$emit('input', newValue)` | No `vm`; `fieldMeta` and `parentContent` must be explicit; `blockEvent` is a plain boolean; fires `onInput?.(newValue)` | **Different** | Migrated 2026-05-09. Legacy resolved field context from the live vm; new version is stateless and requires explicit args |
| `InputFieldUnion.fetchFieldValueWrapper(oSettings)` | `if (fieldMeta.fieldValue)` — truthy check; `0`/`false`/`""` would fall through | `if (fieldMeta.fieldValue !== undefined)` — strict check; correctly returns `0`, `false`, `""` | **Different** | New UI is more correct: falsy-but-set values are returned instead of falling through to `fetchObjValueByPath` |

### TODOs / Not Ported

| Gap | Description |
|-----|-------------|
| `TypeAheadField` / `MultipleValueField` in dispatch map | Neither component is registered in `fieldComponentMap`; those field types fall back silently to `InputField` |

### Resolved

Items confirmed fully covered by the new UI — kept for traceability, no action needed.

| ~~Member~~ | ~~Why covered~~ |
|------------|-----------------|
| ~~`comFieldValue` setter~~ | ~~Was a Vue v-model re-entry point only — `setFieldValueWrapper` already writes `parentContent` directly, making the setter redundant. Covered by `InputFieldUnion.setFieldValueWrapper` in new UI.~~ |
| ~~`valueCallback` in `comFieldValue` getter~~ | ~~Ported 2026-05-10: `fieldValue` useMemo checks `fieldMeta.settings.valueCallback` before falling back to `fetchObjValueByPath`.~~ |
| ~~`getFieldMeta()`~~ | ~~Ported 2026-05-10: exposed via `useImperativeHandle` returning `props.fieldMeta`.~~ |
| ~~`clearValue(oSettings)`~~ | ~~Ported 2026-05-12: added `clearValue: (oSettings) => childRef.current?.clearValue(oSettings)` to `useImperativeHandle`; delegates to `AbsInput.clearValue` which calls `onInput?.(initialValue)`.~~ |
