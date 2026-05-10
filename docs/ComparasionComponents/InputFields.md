# Input Field Component Comparison: Legacy vs New UI

*Note*: This comparison focuses on **differences only** —
identical members between legacy and new UI are omitted.

**Legacy file**: `admin/js/component/basicElements/AsyncControlElement.js` (lines 164–548)
**New file**: `IntelligentUI/src/components/control/AbsInput.tsx`

*Exclude list*: see skill default excludes (all entries promoted to universal defaults)

---

## Class Name: AbsInput

**Legacy file**: `admin/js/component/basicElements/AsyncControlElement.js` (lines 164–548)
**New file**: `IntelligentUI/src/components/control/AbsInput.tsx`

### Props

| Prop | Legacy | New | Status | Notes |
|------|--------|-----|--------|-------|
| `onInput` | — | `(value: unknown) => void` | **New only** | Replaces `$emit('input')`; React callback prop for value changes |
| `onUpdatePrice` | — | `(event) => void` | **New only** | Replaces `$emit('updatePrice')`; React callback prop for price updates |
| `onChange` | — | `(value: unknown) => void` | **New only** | Alternative change callback for child field components |
| `inputClass` | — | `string` | **New only** | CSS class override for the rendered input element |
| `lowFieldName` | — | `string` | **New only** | Range field: name of the low-bound field |
| `highFieldName` | — | `string` | **New only** | Range field: name of the high-bound field |
| `resolvedLabel` | — | `string` | **New only** | Pre-resolved i18n label passed from `InputFieldUnion` |
| `namePath` | — | `string \| string[]` | **New only** | ProForm field path for nested data binding |

### Methods

| Method | Legacy | New | Status | Notes |
|--------|--------|-----|--------|-------|
| `getFieldName()` | `fieldMeta.fieldName` (no fallback) | `fieldMeta?.fieldName ?? fieldMeta?.fieldKey` | **Different** | New version adds `fieldKey` fallback when `fieldName` is absent |
| `getFieldKey()` | `fieldMeta.fieldKey` (no fallback) | `fieldMeta?.fieldKey ?? fieldMeta?.fieldName` | **Different** | New version adds `fieldName` fallback when `fieldKey` is absent |
| `addHandler(handler, trigger)` | `(handler, trigger)` — handler first, trigger second | `(trigger, handler)` — trigger first, handler second | **Different** | Parameter order is swapped; callers in `ServiceFieldMetaUtility.ts` still pass `(handler, trigger)`, so the new hook receives the arguments in the wrong slots |
| `clearValue(oSettings)` | `(oSettings.initValue) ? oSettings.initValue : undefined` — truthy check; `0` or `""` falls through to `undefined` | `oSettings?.initValue !== undefined ? oSettings.initValue : undefined` — strict check | **Different** | New version correctly passes falsy-but-set values (e.g. `0`) as the initial value |
| `checkValidateInput(oSettings)` | Returns result array when validation fails; implicit `undefined` return when not required and no `validType` | Always returns `unknown[]`; returns `[]` when not required | **Different** | Legacy can return `undefined`; new always returns an array — callers that check for falsy result will behave differently |
| `getHandlerListByTrigger(trigger)` | Returns handler list from `meta[handlerListPath]` | — | **Legacy only** | New UI accesses handler lists directly inside `addHandler` and `disableHandler`; no public getter exposed |
| `setHandlerListByTrigger(newList, trigger)` | Sets `meta[handlerListPath]` via `$set` | — | **Legacy only** | New UI writes via `setMeta` inside `addHandler`; no public setter exposed |
| `executeWatchHandlerList()` | Passes `$http` to `executeWatchHandlerUnion` | Does not pass `$http` | **Different** | `$http` was Vue-axios binding; new UI omits it — watch handlers that depend on `$http` will not receive it |

### Exported Interfaces

| Interface | Status | Notes |
|-----------|--------|-------|
| `FieldHandle` | **New only** | Ref handle: `getFieldMeta`, `getFieldName`, `postUpdate`, `loadMetaData`, `updateConfig`, `checkValidateSave`, `checkValidateSubmit` — `clearValue` is in `UseAbsInputReturn` but **not** in `FieldHandle` |
| `AbsInputProps` | **New only** | Consolidated props type for all field components |
| `UseAbsInputReturn` | **New only** | Return type of `useAbsInput` hook |

### Static Properties

| Property | Legacy | New | Status | Notes |
|----------|--------|-----|--------|-------|
| `FIELDTYPE` new entries | — | `Text`, `Select`, `Date`, `Number`, `Textarea`, `Upload` | **New only** | Additional field type strings added for React migration; legacy only had `Input`, `Select2`, `TextArea`, `TypeAhead`, `ModalSelect2`, `MultipleValue`, `MessageTitle` |

### TODOs / Not Ported

| Gap | Description |
|-----|-------------|
| `addHandler` parameter order | New `useAbsInput.addHandler(trigger, handler)` is reversed vs legacy `(handler, trigger)` and all current callers in `ServiceFieldMetaUtility.ts` |
| `FieldHandle` missing `clearValue` | `clearValue` exists in `UseAbsInputReturn` and is implemented, but is not declared on the `FieldHandle` interface — callers holding a `FieldHandle` ref cannot invoke `clearValue` |
| `executeWatchHandlerList` missing `$http` | Watch handlers that call HTTP services via `$http` will not receive it; those handler types are not yet migrated |

### Resolved

Items confirmed fully covered by the new UI — kept for traceability, no action needed.

| ~~Member~~ | ~~Why covered~~ |
|------------|-----------------|
| ~~`checkHandlerEnable` instance method~~ | ~~Inlined into `executeWatchHandlerList` in new UI; identical logic, just not a separate method.~~ |

---

## Class Name: InputField

**Legacy file**: `admin/js/component/basicElements/AsyncControlElement.js` (lines 553–556)
**New file**: `IntelligentUI/src/components/control/InputField.tsx`

### Rendering

| Aspect | Legacy | New | Status | Notes |
|--------|--------|-----|--------|-------|
| Disabled state | `<input :disabled="comDisabledFlag">` | `<ProFormText readonly={isDisabled}>` | **Different** | Legacy uses HTML `disabled` attribute (field unfocusable, excluded from form submit); new uses `readonly` (field focusable, value still submits) |
| Value binding | `v-model="fieldValue"` — reactive two-way binding; updates instantly on user edit | `initialValue={fieldValue}` on `<ProFormText>` — one-time prop at mount; ProForm owns the live value via `<ProForm initialValues>` | **Different** | `initialValue` on an individual ProFormText is a mount-time hint. Live value tracking relies on `<ProForm initialValues>` at the page level |

### TODOs / Not Ported

| Gap | Description |
|-----|-------------|
| `clearValue` not in `useImperativeHandle` | `useAbsInput` implements `clearValue`; `InputField.useImperativeHandle` does not expose it — callers holding a `FieldHandle` ref cannot invoke `clearValue` on a text input |

---

## Class Name: Select2Field (legacy) / SelectField (new)

**Legacy file**: `admin/js/component/basicElements/AsyncControlElement.js` (lines 562–891)
**New file**: `IntelligentUI/src/components/control/SelectField.tsx`

### Methods

| Method | Legacy | New | Status | Notes |
|--------|--------|-----|--------|-------|
| `getFieldKey()` | Checks `settings.uuidField` first; falls back to `fieldMeta.fieldKey ?? fieldMeta.fieldName` | Not overridden — inherits `useAbsInput.getFieldKey()` which returns `fieldMeta?.fieldKey ?? fieldMeta?.fieldName` only | **Different** | `settings.uuidField` override is ignored in new UI; select fields that specify a separate UUID key field will use the wrong field name |
| `checkFieldMetaConfig()` | Raises `ServiceExceptionHelper` exception if `settings` is missing | `console.warn` if `settings` is missing | **Different** | Severity downgraded from exception to warning; callers that caught the exception will not detect the misconfiguration |
| `clearValue(oSettings)` | Clears `fieldValue` + `parentContent[fieldKey]` + calls `clearDefSearchSelectElements` on DOM | Not in `useImperativeHandle` — `useAbsInput.clearValue` fires `onInput?.(initialValue)` only | **Different** | New UI does not clear `parentContent[fieldKey]` (the UUID key field); the stale UUID remains in `parentContent` after clear. Also missing from `FieldHandle` ref |
| `loadMetaData()` | Calls `loadMetaRequest` or `loadModelMetaWrapper` via `$http`; supports `fnSetInitKey`, `fnSetInitKeyForInvalid`, `formatMeta`, `formatMetaCallback`, `requestData`, `method`, `multiple`, `processEmptyCallback`, `errorHandle` | Calls `loadMetaRequestForSelect` with `idField`, `textField`, `listSubPath`, `excludeKeyList`, `addEmptyFlag`, `processSelectOptions`; no `$http` | **Partial** | Missing options: `fnSetInitKey`/`fnSetInitKeyForInvalid` (callbacks for setting selected key after load), `formatMeta`/`formatMetaCallback` (service-method hooks), `requestData`, `method`, `multiple`, `processEmptyCallback`, `errorHandle` |
| `loadModelMetaWrapper(oSettings)` | Handles `excludeExist` filtering (builds `processSelectOptions` closure); routes to `loadMetaWithCustomReq` when `settings.customMeta` is true | — | **Not ported** | `excludeExist` filtering logic and `customMeta` routing not implemented; `loadMetaRequestForSelect` handles standard cases only |
| `setDefaultKeyHandler(result)` | Writes selected key back to `parentContent` via `InputFieldUnion.setFieldValueWrapper` on select2 change event | — | **Legacy only** | Select2-specific key-change callback; in new UI `ProFormSelect` writes value into ProForm state, and `AsyncPage.onValuesChange` syncs it to `parentContent` |
| `setDefaultInitKeyHandler(initValue)` | Writes initial key value to `parentContent` via `InputFieldUnion.setFieldValueWrapper` on select2 init | — | **Legacy only** | Select2-specific init-key callback; new UI uses `initialValue` prop on `<ProFormSelect>` |
| `getInitKeyHandlerWrapper(oSettings)` | Returns `oSettings.fnSetInitKey` or falls back to `setDefaultInitKeyHandler` | — | **Legacy only** | Handler resolver for select2 init; not needed in React |

### TODOs / Not Ported

| Gap | Description |
|-----|-------------|
| `settings.uuidField` key override | `getFieldKey()` not overridden — select fields using a separate UUID key field (via `settings.uuidField`) will resolve the wrong field name |
| `clearValue` missing from `useImperativeHandle` | `SelectField` overrides `clearValue` with extra logic (also clears `parentContent[fieldKey]`), but `useImperativeHandle` does not expose any `clearValue` — the extra parentContent clear is also lost |
| `loadMetaData` missing callbacks | `fnSetInitKey`, `fnSetInitKeyForInvalid`, `formatMeta`, `formatMetaCallback` not wired; select fields that use these hooks will not apply post-load transformations or set the initial selected key |
| `loadModelMetaWrapper` / `excludeExist` | Exclude-exist filtering (remove already-selected items from the option list) not ported |

---

## Class Name: TextAreaField

**Legacy file**: `admin/js/component/basicElements/AsyncControlElement.js` (lines 1136–1167)
**New file**: `IntelligentUI/src/components/control/TextAreaField.tsx`

### Props

| Prop | Legacy | New | Status | Notes |
|------|--------|-----|--------|-------|
| `rowNumber` | `[String, Number]` | `string \| number` | Exists in both | No functional change |
| `collapseAble` | `[String, Boolean]` | `string \| boolean` | Exists in both | No functional change |
| `ChildFieldProps` | — | `outsideProForm`, `fieldValue`, `fieldLabel`, `fieldNameOrPath`, `fieldRules`, `fillStyle`, `isDisabled`, `comSubFieldRefId` | **New only** | Dispatched from `InputFieldUnion` |

### Computed Properties

| Computed | Legacy | New | Status | Notes |
|----------|--------|-----|--------|-------|
| `comRowNumber` | `rowNumber ?? 5` | `rowNumber ?? 5` | Exists in both | No functional change |
| `comLabelMultiValueSearchComment` | `labelObject['multiValueSearchComment']` | — | **Legacy only** | Label lookup not ported; not used in current pages |

### Rendering

| Aspect | Legacy | New | Status | Notes |
|--------|--------|-----|--------|-------|
| Outside-form mode | — | `outsideProForm` → standalone `<Input.TextArea>` with `<label>` | **New only** | Supports rendering outside ProForm context |
| Fallback path | — | Renders native `<textarea>` for legacy AbsInputProps path | **New only** | Backward-compatible path |
| Row source | `fieldMeta.rows` is not used; uses `rowNumber` prop only | ChildFieldProps path reads `fieldMeta.rows`; fallback path uses `rowNumber` prop | **Different** | New UI has two sources for row count depending on render path |

### Methods

| Method | Legacy | New | Status | Notes |
|--------|--------|-----|--------|-------|
| onChange handler | `v-model` two-way binding | `onChange` callback in ChildFieldProps path; TODO stub in fallback path | **Different** | Fallback path onChange is not yet wired |

---

## Class Name: ModalSelect2Field (legacy) / ModalSelectField (new)

**Legacy file**: `admin/js/component/basicElements/AsyncControlElement.js` (lines 1024–1130)
**New file**: `IntelligentUI/src/components/control/ModalSelectField.tsx`

### Inheritance

| Aspect | Legacy | New | Notes |
|--------|--------|-----|-------|
| Parent | `mixins: [Select2Field]` — inherits all Select2Field + AbsInput methods | Standalone component with `AbsInputProps` only | **Different**: legacy chains Select2Field → AbsInput; new does not extend SelectField, reimplements subset locally |

### Methods

| Method | Legacy | New | Status | Notes |
|--------|--------|-----|--------|-------|
| `initSubComponentsModal()` | Registers `MaterialTypeSelect`, `MaterialSkuSelect`, `MessageTemplateSelect`, `WarehouseSelect` via `Vue.component()` | — | **Legacy only** | Vue dynamic component registration; not needed in React (direct imports) |
| `checkFieldMetaConfig()` | Raises `ServiceExceptionHelper` if missing `settings` or `settings.uuidField` | `console.warn` if missing settings or uuidField | Exists in both | **Simplified**: exception → warning |
| `selectSelector()` | `this.$refs.refCoreSelect.initSelectModal()` | Calls `coreSelectRef.current?.initSelectModal()` | Exists in both | Same logic via React ref |
| `setToValue(objectValue)` | Calls `InputFieldUnion.setFieldValueWrapper` + `setValue` + `setSelect2ModelValue` or `setValueCallback` | Calls `onInput` + `setValueCallback`; `setSelect2ModelValue` is TODO | Exists in both | **Partial**: `setSelect2ModelValue` fieldList wiring not yet ported |
| `getModelSelectorType()` | Returns `settings.selectorType` or `ServiceModelSelector.SELECTOR_TYPE.MaterialSkuSelect` | Returns `settings.selectorType` or `'material-sku-select'` string literal | Exists in both | **Hardcoded fallback** instead of constant reference |

### Rendering

| Aspect | Legacy | New | Status | Notes |
|--------|--------|-----|--------|-------|
| Modal selector | `<service-model-selector :is="getModelSelectorType()">` dynamic component | Commented out TODO | **Not ported** | ServiceModelSelector dynamic component not yet migrated |
| Select element | `<select>` with `v-model` | `<select>` with `onChange` handler | Exists in both | Same structure |

### TODOs in New UI

| TODO | Description |
|------|-------------|
| `ServiceModelSelector` | Dynamic component rendering by `selectorType` not yet migrated |
| `ServiceUtilityHelper.setSelect2ModelValue` | Field list value mapping from selected object to parentContent |
| `InputFieldUnion.setFieldValueWrapper` | Declared but not wired |

---

## Class Name: ModalSelect2Ele (legacy) / ModalSelectEle (new)

**Legacy file**: `admin/js/component/basicElements/AsyncControlElement.js` (lines 1933–1988)
**New file**: `IntelligentUI/src/components/control/ModalSelectEle.tsx`

### Methods

| Method | Legacy | New | Status | Notes |
|--------|--------|-----|--------|-------|
| `checkFieldMetaConfig()` | Raises `ServiceExceptionHelper` if missing `fieldMeta.popupConfigure` | `console.warn` if missing | Exists in both | **Simplified**: exception → warning |
| `getFieldPopupConfigure()` | Adds default `postAction` slot (HTML string) and `postActionMethod` to `fieldMeta.popupConfigure` | Same logic, returns new object instead of mutating | Exists in both | New UI uses spread `{...config}` instead of mutation |
| `postActionMethod()` | `this.getCoreInputField().selectSelector()` | `coreInputRef.current?.selectSelector()` | Exists in both | Same logic via React ref |
| `getPostActionSlot()` | Returns HTML string with icon markup | Same HTML string | Exists in both | **Note**: raw HTML string returned; new UI doesn't render it (no `dangerouslySetInnerHTML`) |

### Rendering

| Aspect | Legacy | New | Status | Notes |
|--------|--------|-----|--------|-------|
| Popup configure usage | `popupConfigure` passed to template for popup modal rendering | `getFieldPopupConfigure()` computed but not connected to any rendered element | **Not wired** | New UI delegates to `<AbsInputEle>` without passing popup config |

---

## Class Name: InputFieldUnion

**Legacy file**: `admin/js/component/basicElements/AsyncControlElement.js` (lines 1227–1513)
**New file**: `IntelligentUI/src/components/control/InputFieldUnion.tsx`

### Props

| Prop | Legacy | New | Status | Notes |
|------|--------|-----|--------|-------|
| `onChange` | — | `(value: unknown) => void` | **New only** | Replaces `$emit('input')` in `setFieldValueWrapper`; legacy emitted on the vm, new passes callback prop |

### Data / State

| Property | Legacy | New | Status | Notes |
|----------|--------|-----|--------|-------|
| `coreUUID` | instance data, set via `initCoreUUID()` calling `genRamdomPostIndex()` | Not a state variable; `comSubFieldRefId` computed inline via `useMemo` with `Math.random()` | **Different** | Legacy stores UUID in reactive data and re-uses it; new generates it once in `useMemo` but it is not stable across re-renders that change `fieldMeta` |

### Computed Properties

| Computed | Legacy | New | Status | Notes |
|----------|--------|-----|--------|-------|
| `comFieldValue` (getter) | Checks `settings.valueCallback()`, then calls `InputFieldUnion.fetchFieldValueWrapper({vm})` | Calls `fetchObjValueByPath` directly, skips `valueCallback` check | **Different** | New UI does not honour `fieldMeta.settings.valueCallback`; that branch is missing |
| `comFieldValue` (setter) | Calls `InputFieldUnion.setFieldValueWrapper({vm, newValue, blockEvent: true})` — writes to `parentContent` and emits `input` only if `blockEvent` is false | Not a setter; value change goes through `onChange` prop | **Different** | Legacy `comFieldValue` setter mutates `parentContent` and optionally suppresses the event; new UI has no equivalent mutation path — `parentContent` is read-only |
| `comSubFieldRefId` | `prefix + fieldType + "-" + genRamdomPostIndex()` — includes `"low-"` or `"high-"` prefix for range fields | `sub-${resolvedFieldType}-${Math.random()...}` — no low/high prefix | **Different** | Range field prefix (`sub-high-`, `sub-low-`) is missing in new UI |

### Methods

| Method | Legacy | New | Status | Notes |
|--------|--------|-----|--------|-------|
| `initSubComponents()` | Registers `AbsInput`, `InputField`, `Select2Field`, `TypeAheadField`, `TextAreaField`, `MultipleValueField`, `ModalSelect2Field` via `Vue.component()` | Not present; direct imports used in `fieldComponentMap` | **Legacy only** | Framework difference in registration, but note `TypeAheadField` and `MultipleValueField` are not in the new component map |
| `initCoreUUID()` | Sets `this.coreUUID = genRamdomPostIndex()` | Not present | **Legacy only** | UUID generation folded into `comSubFieldRefId` `useMemo` |
| `getFieldMeta()` | Returns `vm.fieldMeta` | Not present | **Legacy only** | Simple accessor; callers can use the prop directly in React |
| `fetchFieldName(fieldMeta)` | Instance method: checks `highFieldName` / `lowFieldName` props, then `fieldMeta.fieldName ?? fieldMeta.fieldKey` | Exported standalone function `fetchFieldName(fieldMeta)` — only `fieldMeta.fieldName ?? fieldMeta.fieldKey`, no range-field branch | **Different** | New `fetchFieldName` ignores `lowFieldName` / `highFieldName`; range-field name selection is lost |
| `clearValue(oSettings)` | Delegates via `ServiceVueUtility.batchExecuteSubRefMethod` with `METHOD.CLEAR_VALUE` to child ref | Not present | **Not ported** | No equivalent in new UI; child's `clearValue` cannot be triggered from parent |
| `postUpdate(oSettings)` | Delegates via `batchExecuteSubRefMethod` with `METHOD.POST_UPDATE` | Not present | **Not ported** | No equivalent forwarding mechanism to child component ref |
| `loadMetaData(oSettings)` | Delegates via `batchExecuteSubRefMethod` with `METHOD.LOAD_META_DATA` | Not present | **Not ported** | New UI doesn't expose a `loadMetaData` delegation path on `InputFieldUnion` |
| `checkValidateSave(oSettings)` | Delegates via `batchExecuteSubRefMethod` with `METHOD.CHECK_VALIDATE_SAVE`, returns result array | Not present | **Not ported** | Validation delegation from parent to child field not implemented |
| `checkValidateSubmit(oSettings)` | Delegates via `batchExecuteSubRefMethod` with `METHOD.CHECK_VALIDATE_SUBMIT`, returns result array | Not present | **Not ported** | Same as `checkValidateSave` — not ported |
| `getFieldType(fieldMeta)` | Returns `fieldMeta[AsyncSection.fieldMetaHead.FILED_TYPE]`, defaults to `AbsInput.FIELDTYPE.Input` | Inlined: `(fieldMeta as Record<string, unknown>)?.['fieldType']`, falls back to `fieldComponentMap[AbsInputFIELDTYPE.Input]` | **Different** | New UI reads `'fieldType'` string key directly instead of through `AsyncSection.fieldMetaHead.FILED_TYPE` constant |
| `checkLowFlag()` | Returns `true` if `vm.lowFieldName` truthy | Not present | **Legacy only** | Low/high range flag logic not ported |
| `checkHighFlag()` | Returns `true` if `vm.highFieldName` truthy | Not present | **Legacy only** | Low/high range flag logic not ported |
| `genSubFieldRefId()` | `"sub-"` + optional `"high-"` / `"low-"` + fieldType + random index | Inlined in `useMemo`: `sub-${resolvedFieldType}-${Math.random()...}` (no high/low prefix) | **Different** | Range-field variant IDs (`sub-high-...`, `sub-low-...`) not generated in new UI |
| `getCoreInputField()` | `vm.$refs[vm.comSubFieldRefId]` — returns child component instance | Not present | **Not ported** | No imperative handle forwarded from child; callers cannot call child methods via `InputFieldUnion` ref |

### Static Methods

| Method | Legacy | New | Status | Notes |
|--------|--------|-----|--------|-------|
| `InputFieldUnion.setFieldValueWrapper(oSettings)` | Writes `newValue` to `parentContent` via `ServiceUtilityHelper.setFieldValueWrapper`, then emits `vm.$emit('input', newValue)` unless `blockEvent` is true | Not a static method; mutation removed entirely | **Not ported** | New UI treats `parentContent` as read-only; no equivalent write-back path |
| `InputFieldUnion.fetchFieldName(fieldMeta)` | `fieldMeta.fieldName ?? fieldMeta.fieldKey` (no range-field check) | Exported as standalone `fetchFieldName` function, same logic | Exists in both | Functionally equivalent; range-field check is in the instance method, not the static one |
| `InputFieldUnion.fetchFieldValueWrapper(oSettings)` | Checks `vm.fieldMeta.fieldValue`, then `fetchObjValueByPath(vm.parentContent, fieldName)` | Exported standalone `fetchFieldValueWrapper(fieldMeta, parentContent)`, same logic | Exists in both | Signature changed: no longer takes `{vm}` wrapper object; callers updated |

### Component Map

| Entry | Legacy | New | Status | Notes |
|-------|--------|-----|--------|-------|
| `TypeAheadField` | Registered via `Vue.component("type-ahead-field", TypeAheadField)` | Not in `fieldComponentMap` | **Legacy only** | TypeAheadField component not yet migrated to React |
| `MultipleValueField` | Registered via `Vue.component("multiple-value-field", MultipleValueField)` | Not in `fieldComponentMap` | **Legacy only** | MultipleValueField component not yet migrated to React |
| `ModalSelect2Field` | Registered via `Vue.component("modal-select2-field", ModalSelect2Field)` | Not in `fieldComponentMap` | **Legacy only** | ModalSelectField exists as separate component but is not wired into the `InputFieldUnion` dispatch map |

### TODOs / Not Ported

| Gap | Description |
|-----|-------------|
| `valueCallback` in `comFieldValue` getter | `fieldMeta.settings.valueCallback()` branch skipped in new `fetchFieldValueWrapper` call |
| `parentContent` mutation | `setFieldValueWrapper` write-back to `parentContent` has no equivalent; new UI is fully read-only |
| Range field support (`lowFieldName`/`highFieldName`) | `checkLowFlag`, `checkHighFlag`, `genSubFieldRefId` prefix, and `fetchFieldName` range branch all absent |
| `getCoreInputField()` + delegation methods | `clearValue`, `postUpdate`, `loadMetaData`, `checkValidateSave`, `checkValidateSubmit` not forwarded via `FieldHandle` ref |
| `TypeAheadField` / `MultipleValueField` in dispatch map | Neither component is registered in `fieldComponentMap` |
