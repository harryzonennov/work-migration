# i18n System Analysis: ThorSalesDistributionUI → IntelligentServiceUI Migration

## Overview

The old UI uses a **jQuery i18n properties** plugin with `.properties` files containing unicode-escaped Chinese and English strings. The system loads translations at runtime in the Vue `mounted()` hook, then reflectively populates a shared `label` object that Vue templates bind to.

---

## Key Functions

### `getI18nRootConfig()` — on Service Manager (e.g., `EmployeeManager.js:194`)

Defined on each `***Manager.js` file. Returns the configuration object describing which `.properties` files to load for a **List** page:

```javascript
EmployeeManager.getI18nRootConfig = function () {
    return {
        i18nPath: 'coreFunction/',          // subfolder under I18N_ROOTPATH
        mainName: 'Employee',               // loads Employee_xx.properties
        modelId: 'Employee',
        coreModelId: 'Employee',
        labelObjectParent: EmployeeManager.label,
        labelObjectMainPath: 'employee',    // populated label path: label.employee.*
        configList: [
            { name: 'EmployeeOrg',    subLabelPath: 'employeeOrg' },
            { name: 'EmpLogonUser',   subLabelPath: 'empLogonUser' },
            { actionNodePath: 'actionNode' }   // special: loads action button labels
        ]
    };
};
```

Each Manager also has `getI18nItemConfig()` — same shape, used for **Editor** pages.

---

### `getI18nConfig()` — on Controller mixin (`ServiceUiController.js:364`)

Base implementation that bridges the Vue component to its Manager's config:

```javascript
getI18nConfig: function () {
    var serviceManager = this.getServiceManager();
    if (serviceManager && serviceManager.getI18nRootConfig) {
        return serviceManager.getI18nRootConfig();
    }
},
```

Overridden in:
- **List mixin** (`line 2367`): calls `serviceManager.getI18nItemConfig()` and merges in `DocMatItemNode` for item sub-tables
- **Editor/Item mixin** (`line 2406`): same pattern, calls `getI18nItemConfig()`

---

## Complete i18n Load Flow

```
1. Page HTML includes:
   <script src="js/jquery.i18n.properties.min.js"></script>

2. Vue created() hook
   └── getPageMeta()  ← populates pageMeta from getDefaultPageMeta()

3. Vue mounted() hook
   └── setI18nProperties(setI18nCallback)

4. setI18nProperties()
   └── setDefI18nProperties()
       ├── setNodeI18nPropertiesRightBar()   ← loads right-bar/sidebar labels
       └── setNodeI18nPropertiesByConfig()   ← main i18n load

5. setNodeI18nPropertiesByConfig()           (ServiceUiController.js:145)
   ├── getI18nConfig()                       ← returns Manager.getI18nRootConfig()
   ├── if !i18nConfig.labelObject → assign vm.label
   └── ServiceUtilityHelper.setNodeI18nPropertiesByConfig({ i18nConfig, fnCallback })

6. ServiceUtilityHelper.setNodeI18nPropertiesByConfig()  (ServiceHttpRequestHelper.js:4036)
   ├── checkAndBuildLabelObject(i18nConfig)  ← creates label sub-objects if missing
   ├── setI18nCommonReflective(label, $.i18n.prop)  ← pre-populate from already-loaded props
   ├── Appends mainName to configList
   ├── For each configList entry, attaches a callback:
   │     callback: () => setI18nReflective(label, $.i18n.prop, true)
   └── setI18nPropertiesWrapper(targetLabelConfig)

7. setI18nPropertiesWrapper()                (ServiceHttpRequestHelper.js:4527)
   ├── getCommonI18n()  →  jQuery.i18n.properties({
   │       name: 'ComElements',
   │       path: I18N_ROOTPATH + 'foundation/',
   │       mode: 'map', language: getLan(), cache: true
   │   })
   └── for each config entry:
         jQuery.i18n.properties({
             name: 'Employee',           // or 'EmployeeOrg', etc.
             path: I18N_ROOTPATH + 'coreFunction/',
             mode: 'map',
             language: getLan(),         // from sessionStorage or navigator.language
             cache: true,
             callback: entry.callback
         })
         ← file loaded: Employee_zh_CN.properties  (or _en, _zh_TW, etc.)

8. Each callback fires after its .properties file loads:
   └── setI18nReflective(label, $.i18n.prop)
       └── traverseObjProperties(label, (obj) => {
               label[obj.key] = $.i18n.prop(obj.key)
               // nested: label[parent][child] = $.i18n.prop('parent.child')
           })

9. label object now fully populated:
   label.employee.name  = "姓名"   (or "Name" if en)
   label.employee.id    = "工号"
   ...

10. Vue templates re-render:
    <span>{{ label.employee.name }}</span>  →  "姓名"
```

---

## Helper Functions

| Function | File | Purpose |
|---|---|---|
| `getI18nRootPath()` | `Commons.js:89` | Returns `I18N_ROOTPATH` global — base URL for all .properties files |
| `getCommonI18nPath()` | `Commons.js:227` | Returns `"foundation/"` — path for common labels (ComElements) |
| `getLan()` | `Commons.js:98` | Returns language code from `sessionStorage('lanCode')` or `navigator.language` |
| `getCommonI18n()` | `Commons.js:231` | Loads `ComElements.properties` (shared cross-module labels) |
| `setI18nReflective()` | `ServiceHttpRequestHelper.js:3983` | Walks label object, calls `$.i18n.prop(key)` for each field |
| `setI18nReflectiveEmbedObject()` | `ServiceHttpRequestHelper.js:3990` | Same, but for nested objects using `"parent.child"` key format |

---

## .properties File Naming Convention

jQuery i18n appends the language code automatically:

```
Employee.properties          ← fallback (default)
Employee_en.properties       ← English
Employee_zh_CN.properties    ← Simplified Chinese
Employee_zh_TW.properties    ← Traditional Chinese
```

Content (note: `_en.properties` actually contains unicode-escaped Chinese):

```properties
# Employee_en.properties
employee.name=\u59d3\u540d
employee.id=\u5de5\u53f7
employee.department=\u90e8\u95e8
```

Decode: `\u59d3\u540d` → `姓名`

---

## `configList` Special Entry Types

Inside `getI18nRootConfig().configList`, entries can be:

| Entry shape | Effect |
|---|---|
| `{ name: 'Foo', subLabelPath: 'foo' }` | Loads `Foo.properties`, populates `label.foo.*` |
| `{ name: 'Foo' }` (no subLabelPath) | Loads `Foo.properties`, populates `label.*` at root |
| `{ path: 'foundation/', name: 'DocMatItemNode' }` | Loads from a different subfolder |
| `{ actionNodePath: 'actionNode' }` | Loads action-button labels into `label.actionNode.*` |
| `{ docInvolvePartyPath: 'party' }` | Loads involve-party labels into `label.party.*` |

---

## Migration Mapping: Old → New (Vue 3 + vue-i18n)

| Old concept | New equivalent |
|---|---|
| `getI18nRootConfig()` on Manager | `src/locales/zh-CN/[module]/[Entity].json` + `en/` counterpart |
| `getI18nConfig()` on controller | `useI18n()` from `vue-i18n` in composable/view |
| `jQuery.i18n.properties({...})` | Static JSON imported at build time via vue-i18n |
| `$.i18n.prop('employee.name')` | `t('employee.name')` |
| `label.employee.name` in template | `{{ t('employee.name') }}` or `t('employee.name')` in `<script setup>` |
| `getLan()` from sessionStorage | `locale.value` from `useI18n()`, backed by Pinia store |
| `ComElements.properties` (shared) | `src/locales/zh-CN/common.json` |
| `configList[].actionNodePath` | Action labels included directly in entity's locale JSON under `action.*` |
| `configList[].subLabelPath` | Nested keys within the same JSON file, or a separate imported JSON |

### New JSON Structure Pattern

Old `.properties` key `employee.name` becomes:

**`src/locales/zh-CN/platform/Employee.json`**
```json
{
  "employee": {
    "name": "姓名",
    "id": "工号",
    "department": "部门"
  },
  "employeeOrg": {
    "orgName": "所属组织"
  },
  "action": {
    "submit": "提交",
    "approve": "审核通过"
  }
}
```

**`src/locales/en/platform/Employee.json`**
```json
{
  "employee": {
    "name": "Name",
    "id": "Employee ID",
    "department": "Department"
  }
}
```

### vue-i18n Registration (already done in `src/locales/index.ts`)

```typescript
import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'   // aggregates all module JSON files
import en from './en'

export const i18n = createI18n({
  locale: 'zh-CN',
  fallbackLocale: 'en',
  messages: { 'zh-CN': zhCN, en }
})
```

### Usage in Views

```vue
<script setup lang="ts">
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
</script>

<template>
  <el-table-column :label="t('employee.name')" prop="name" />
</template>
```

---

## Unicode Decode Script (for .properties → JSON)

```python
import json

def decode_properties(path: str) -> dict:
    result = {}
    with open(path, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith('#') or '=' not in line:
                continue
            k, v = line.split('=', 1)
            # _en.properties uses unicode escapes (\uXXXX) for Chinese
            result[k.strip()] = v.strip().encode('utf-8').decode('unicode_escape')
    return result

# Example usage:
data = decode_properties('Employee_en.properties')
with open('Employee.json', 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)
```

> **Note:** The `.properties` files named `_en` actually contain Chinese unicode escapes. The `_zh_CN` files may contain raw Chinese characters directly or also be unicode-escaped. Always decode with `.decode('unicode_escape')`.

---

## Files Referenced

| File | Purpose |
|---|---|
| `admin/js/component/basicElements/ServiceUiController.js` | Base mixin: `getI18nConfig`, `setI18nProperties`, `setNodeI18nPropertiesByConfig` |
| `admin/js/ServiceHttpRequestHelper.js` | Core utilities: `setNodeI18nPropertiesByConfig`, `setI18nPropertiesWrapper`, `setI18nReflective` |
| `admin/js/Commons.js` | `getI18nRootPath`, `getLan`, `getCommonI18n` |
| `admin/js/***Manager.js` | Per-entity: `getI18nRootConfig`, `getI18nItemConfig`, `label` object |
| `admin/js/***List.js` | Calls List mixin; `getDefaultPageMeta` sets `i18nPath` |
| `admin/js/***Editor.js` | Calls Editor mixin; same pattern |
| `admin/i18n/[module]/[Entity]_en.properties` | Translation source files (unicode-escaped) |
| `admin/js/jquery.i18n.properties.min.js` | jQuery i18n plugin (not needed in new project) |
