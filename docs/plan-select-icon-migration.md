# Plan: Migrate per-option icons to Select dropdowns

> **Status:** Planning document. No code changes yet.
> **Created:** 2026-07-02
> **Companion docs:** [`plan-icon-color-migration.md`](./plan-icon-color-migration.md), [`MINTON_ICON_REFERENCE.md`](./MINTON_ICON_REFERENCE.md)

---

## 1. What the user reported

Legacy IntelligentUI's select dropdowns show a small icon next to each
option — e.g. the Purchase Contract "status" select shows a badge-colored
`✎ 已审核`, the "priority" select shows `↓ Low / ↕ Middle / ↑ High` etc.
Icons are per-option and match the icons already registered in the
status/priority icon arrays.

In the new UI, the same dropdowns render just the text — no per-option
icons. Colors from Phase A's `.content-*` utilities aren't reaching the
dropdown either, because the icon `<i>` element never gets inserted
into the option rendering.

## 2. Diagnosis — how legacy does it

Investigated `ThorSalesDistributionUI/admin/js/`:

### 2.1 Rendering pipeline (jQuery + select2)

`DocumentManagerFactory.js:997-1024` shows the pattern:

```js
var _formatStatus = function(status) {
    if (documentManager && documentManager.getStatusIconArray) {
        var $element = ServiceUtilityHelper.formatSelectWithIcon(
            status,
            documentManager.getStatusIconArray(),
            true
        );
        return $element;
    }
};
$(oSelectElement).select2({
    data: resultList,
    templateResult: _formatStatus,        // ← icon on option in dropdown
    templateSelection: _formatStatus,     // ← icon on selected pill in input
});
```

`ServiceHttpRequestHelper.js:1080-1109` shows `formatSelectWithIcon`:

```js
formatSelectWithIcon = function (selectOption, selectOptionMap, backDirection) {
    var selectOptionMapUnion = ServiceCollectionsHelper.filterArray(
        selectOption.id, 'id', selectOptionMap
    );
    if (selectOptionMapUnion && selectOptionMapUnion.iconClass) {
        var iconElement = document.createElement("i");
        iconElement.setAttribute("class", selectOptionMapUnion.iconClass);
        // Build <span>[text][icon]</span> or <span>[icon][text]</span>
        // based on backDirection flag
    }
    return $element;
};
```

### 2.2 Data source — icon arrays

- **Status:** `documentManager.getStatusIconArray()` returns
  `[{id: STATUS_APPROVED, iconClass: 'mdi mdi-spellcheck content-peach-red'}, ...]`
- **Priority:** `SystemStandrdMetadataProxy.getDefPriorityCodeIconArray()`
  returns `[{id: LOW, iconClass: 'fa fa-angle-down content-green'}, ...]`
- Both arrays live in the ~120 icon strings that got substituted during
  Phase B of the icon+color migration — so the CLASS strings are
  already correct. The bug is that they never get rendered next to
  option text.

### 2.3 select2 API summary

Legacy select2 (jQuery) has two icon-rendering slots:
- `templateResult(option)` → returns HTML/element to render each dropdown option
- `templateSelection(option)` → returns HTML/element to render the selected value inside the input

Both are per-option callbacks receiving `{id, text, ...}`. Legacy uses
the same function for both.

## 3. Diagnosis — new UI gap

### 3.1 Rendering pipeline (antd/Pro)

`src/components/control/SelectField.tsx:300-313`:

```tsx
return (
    <ProFormSelect
        key={comSubFieldRefId}
        name={fieldNameOrPath}
        label={fieldLabel}
        width={fieldWidth}
        rules={fieldRules}
        fieldProps={{
            ...fillStyle,
            disabled: isDisabled,
            onChange,
            options: resolvedOptions,   // ← [{label, value}] only, no icon
        } as any}
    />
);
```

`SelectField` state:

```tsx
const [selectOptions, setSelectOptions] = useState<
    { label: string; value: string | number }[]
>([]);
```

**Root cause of the missing icons:** the option shape carries only
`{label, value}`. Legacy's `iconClass` field is discarded during
transformation. antd's `<Select>` (which `ProFormSelect` wraps) supports
per-option custom rendering via `optionRender` and `labelRender` props —
but they're never wired up.

### 3.2 The transformation that drops the icon

`services/ServiceUtilityHelper.ts:206`:

```ts
interface SelectOptionItem {
    id: unknown;
    text: string;
    // no iconClass here
}
```

The `SelectOptionItem` interface — used by `loadMetaRequest` and its
callers — doesn't have `iconClass`. Even if a backend returned one, it
gets stripped.

Meanwhile the icon data lives in a **separate array** on the manager:

```ts
// DocumentManagerFactory.ts:73
export interface DocTypeIconEntry {
    id: unknown;
    iconClass: string;
}
```

Legacy join happens at render time via `filterArray` — matching
`selectOption.id` against `statusIconArray[].id`. New UI never does
this join.

### 3.3 What's ALREADY working (and what isn't)

I confirmed with the earlier icon+color migration:
- The `iconClass` strings in the arrays are correct — Phase B fixed them
  to Minton MDI equivalents
- The `.content-*` color classes exist — Phase A defined them
- The `statusIconArray` / `priorityCodeIconArray` are exposed via
  `documentManager.getStatusIconArray()` and
  `SystemStandrdMetadataProxy.getDefPriorityCodeIconArray()` — the
  arrays reach the runtime

The last mile — **actually rendering `<i class="mdi mdi-... content-...">`
next to each option** — is the missing piece.

## 4. Strategic options

### Option A — Wire `optionRender` in `SelectField`

Add per-option icon rendering to `SelectField.tsx` via antd's
`optionRender` and `labelRender` props.

Data flow:

```
Legacy iconArray → SelectField adds `data.iconClass` to each option →
optionRender(option) → <span><i class={option.iconClass}/> {option.label}</span>
```

Requires:
1. Extending the option shape from `{label, value}` to
   `{label, value, iconClass?}`
2. Passing the manager's icon array into `SelectField` (via `fieldMeta` or
   a prop)
3. Adding `optionRender` and `labelRender` props to the
   `ProFormSelect fieldProps`

**Pros:**
- Single point of change — `SelectField.tsx` is used by all form Selects
- No changes to data files or controllers
- Matches legacy behavior exactly (dropdown + selected value both show icons)

**Cons:**
- Need to pass icon array through props / context somehow
- The current icon-array lookup (`filterArray(option.id, 'id', iconArray)`)
  runs on every render — trivial perf cost but should memoize

### Option B — Pre-transform options to include `iconClass`

At the point where options are loaded (`loadMetaRequest` or the callers),
join the icon array into the option list up front:

```ts
resultList: [{id: 1, text: '已审核', iconClass: 'mdi mdi-spellcheck ...'}]
```

Then `optionRender` reads `option.iconClass` directly.

**Pros:**
- Selects don't need to know about icon arrays at render time
- Icon array join is one-time cost per fetch, not per render

**Cons:**
- More places to change (every callsite that builds options)
- `SelectOptionItem` interface changes → ripple through 10+ files
- Legacy pattern was render-time join — deviating from that could hide
  bugs

### Option C — Do both: extend option shape AND wire optionRender

Data files start returning `{id, text, iconClass?}` where the manager
already knows the icon; `SelectField` handles `optionRender` when
`option.iconClass` is present.

**Pros:**
- Options are self-contained (no external icon array lookup at render)
- SelectField logic is dead simple
- Callers that don't care about icons don't need to change

**Cons:**
- Both changes need to ship together
- More total files touched

## 5. Recommended path

**Option A first, then optimize to Option C only if perf shows up as an issue.**

Reasoning:
- Option A is the smallest surgical change — one file (`SelectField.tsx`)
  gets `optionRender`, plus one hook to pass the icon array through
- The icon-array lookup is O(n) per render but n is tiny (typically <10
  options per select) and antd already re-renders the whole dropdown on
  any state change — no measurable overhead
- Option C is the "right" long-term but changes 10+ files. If Option A
  works fine, Option C is premature optimization

## 6. Phased plan

### Phase A — Wire `optionRender` in SelectField ✅ DONE (2026-07-02)

#### A.1 Extend option shape internally

`SelectField.tsx`:
```tsx
const [selectOptions, setSelectOptions] = useState<
    { label: string; value: string | number; iconClass?: string }[]
>([]);
```

#### A.2 Accept iconArray via fieldMeta

`FieldMeta` already carries `settings` (see `types/FieldMeta.ts`). Add
`settings.iconArray?: DocTypeIconEntry[]` — an optional array-of-icons
descriptor.

When the controller wiring calls the select's loader, it can pass:
```ts
settings: {
    getMetaDataUrl: '/api/.../status',
    iconArray: purchaseContractManager.getStatusIconArray(),
}
```

#### A.3 Populate iconClass on option-load

Where options come in (in the `_loadMetaRequestCore` result callback or
a wrapper), if `settings.iconArray` is present, join it into each
option:

```ts
const iconMap = new Map(iconArray.map(i => [i.id, i.iconClass]));
setSelectOptions(resultList.map(o => ({
    label: o.text,
    value: o.id,
    iconClass: iconMap.get(o.id),
})));
```

#### A.4 Wire `optionRender` and `labelRender`

```tsx
<ProFormSelect
    ...
    fieldProps={{
        ...fillStyle,
        disabled: isDisabled,
        onChange,
        options: resolvedOptions,
        optionRender: (option: any) => (
            <span>
                {option.data?.iconClass && (
                    <i className={option.data.iconClass} style={{ marginRight: 6 }} />
                )}
                {option.label}
            </span>
        ),
        labelRender: (props: any) => {
            const opt = resolvedOptions?.find(o => o.value === props.value);
            return (
                <span>
                    {opt?.iconClass && (
                        <i className={opt.iconClass} style={{ marginRight: 6 }} />
                    )}
                    {opt?.label ?? props.label}
                </span>
            );
        },
    } as any}
/>
```

Note: antd v5's `optionRender` receives `{label, value, data}` where
`data` is the full option object. `labelRender` receives just `{label, value}`
so we look up by value.

#### A.5 Feed the icon arrays from managers

Controllers that build `SearchFieldConfig` or `EditFieldConfig` for
status/priority fields need to include the icon array in `settings`:

Example for a status field in `PurchaseContractListController`:
```ts
{
    fieldName: 'status',
    fieldType: 'select',
    settings: {
        getMetaDataUrl: purchaseContractManager.getStatusURL(),
        iconArray: purchaseContractManager.getStatusIconArray(),
    },
}
```

**Effort estimate for A.5:** need to find every controller that
declares a status/priority field and add the `iconArray` to its
settings. Grep suggests ~5-10 sites.

### Phase B — Same treatment for search-panel selects [~30 min]

The `SearchPanel` uses its own field config — verify the same
`optionRender` wire-up applies there too. May need to lift the icon-
render pattern into a shared helper.

### Phase C — Verification

- `/logistics/purchaseContract` list page — Status filter dropdown
  shows icons per option (INITIAL / SUBMITTED / APPROVED / etc.)
- `/logistics/purchaseContract/:uuid/edit` — Status field shows the
  matching icon next to the currently-selected value
- Priority select on any doc — Low/Middle/High show ↓/↕/↑ icons
- Icon colors work (from Phase A of icon+color migration)

## 7. Files that will change (rough estimate)

| File | Change |
|---|---|
| `src/components/control/SelectField.tsx` | Add `optionRender` / `labelRender`, extend option shape |
| `src/services/ServiceUtilityHelper.ts` | Extend `SelectOptionItem` interface with optional `iconClass` |
| `src/services/DocumentManagerFactory.ts` or new util | Export a helper `mergeIconArrayIntoOptions(options, iconArray)` used by SelectField |
| Controllers using status/priority selects (~5-10 files) | Add `settings.iconArray` to relevant field configs |
| `src/components/SearchPanel.tsx` | Add same `optionRender` if used |

**Effort:** ~1.5-2 hours total.

## 8. Risk assessment

| Risk | Likelihood | Mitigation |
|---|---|---|
| antd version mismatch — `optionRender` prop may differ | Low | antd v5 supports it; user is on antd v5. Verify signature in dev. |
| Icon array not available at render (loaded async) | Medium | Icon arrays are usually synchronous manager methods — verify no async loading needed. If async, add loading state. |
| Custom rendering breaks a11y | Low | Include `aria-label` on the wrapper span. antd handles keyboard navigation regardless. |
| Some selects have options with no icon | Certain | The `option.data?.iconClass &&` guard renders nothing when absent. |

## 9. Open questions (before executing)

1. **Rendering position** — legacy uses `backDirection: true` for
   status (icon AFTER text), but priority uses `backDirection: false`
   (icon BEFORE text). Should the new UI mirror this per-select, or
   standardize on one position?
2. **Search-panel selects** — should Phase A cover both edit-form and
   search-panel selects, or just edit form first?
3. **Fallback for options without icons** — some selects may have
   options where only some entries have `iconClass`. Should the
   render function pad the icon slot with a placeholder span for
   alignment, or let the text left-align?

## 10. Recommendation

Proceed with **Phase A (SelectField + one worked example)**, then verify
visually, then decide whether Phase B (search panel) is needed based on
what you see.

Expected outcome: after Phase A ships, Status and Priority selects on
document editors render with per-option icons matching legacy
appearance. Colors work through Phase A's `.content-*` utility layer.
