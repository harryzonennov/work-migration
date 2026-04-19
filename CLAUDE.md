# Migration Project — Claude Instructions

## Project
Migrating 5 legacy Spring 4 + Hibernate 5 projects → 1 Spring Boot 3.2 + Hibernate 6 backend (IntelligentPlatform) + 1 React frontend (IntelligentUI).

- **Legacy backend**: `/Users/I043125/work/Thorstein*/`
- **Legacy UI**: `/Users/I043125/work/ThorSalesDistributionUI/admin/`
- **New backend**: `/Users/I043125/work2/IntelligentPlatform/`
- **New UI**: `/Users/I043125/work2/IntelligentUI/`
- **Migration workspace**: `/Users/I043125/work-migration/`

---

## MIGRATION CONTRACT (MANDATORY — NEVER VIOLATE)

### Rule 1: Complete port first, then wire

When migrating a legacy JS class to TypeScript:

1. **Port ALL methods, ALL properties, ALL computed, ALL static members** from the legacy JS file to the new TS file — even if you don't think they're needed for the current task.
2. Convert to TypeScript types/signatures, but **keep the same method names, same parameter names, same structure**.
3. jQuery / Bootstrap / Vue.component calls → replace inline with Ant Design equivalents or mark as `// TODO: legacy <description>` stubs. But the **method must exist** with the same signature.
4. Only after the class is fully ported, write the React component that uses it.

### Rule 2: Never create new methods in migrated classes

- **NEVER add methods that don't exist in the legacy JS source.**
- **NEVER rename methods** — keep the exact legacy name.
- **NEVER add new business logic** to migrated classes.
- The React rendering layer adapts to the existing class API. If data is needed, use existing getters/properties.
- Plain callback properties (like `onSrcItemsLoaded`) set by the React layer are acceptable as adapter hooks — they replace Vue's `$emit` / `$refs` wiring, not business logic.

### Rule 3: Verify before porting

Before writing any method in a migrated TS class, **grep the legacy JS file** to confirm:
- The method exists in the legacy source
- The signature matches
- The logic is faithfully preserved

If the method doesn't exist in the legacy source, **don't add it**.

### Rule 4: One class = one legacy file

Each TS class in `src/components/doc/` maps 1:1 to a legacy JS file:

| New TS file | Legacy JS file | Lines |
|---|---|---|
| `DocumentItemMultiSelectFactory.ts` | `admin/js/component/DocumentItemMultiSelectFactory.js` | 229 |
| `DocumentItemMultiSelect.ts` | `admin/js/component/DocumentItemMultiSelect.js` | 1196 |
| `SrcSelectInputUnion.ts` | `admin/js/component/SrcSelectInputUnion.js` | 648 |
| `supplyChain/PurchaseContractMultiSelect.ts` | `admin/js/supplyChain/component/PurchaseContractMultiSelect.js` | ~80 |
| `supplyChain/InboundDeliveryMultiSelect.ts` | `admin/js/supplyChain/component/InboundDeliveryMultiSelect.js` | ~140 |
| `supplyChain/PurchaseContractSelectInput.ts` | `admin/js/supplyChain/component/PurchaseContractSelectInput.js` | ~90 |
| `supplyChain/InboundDeliverySelectInput.ts` | `admin/js/supplyChain/component/InboundDeliverySelectInput.js` | ~80 |

When porting: open the legacy JS, go top to bottom, port every member. Don't skip.

---

## Vue 2 → TypeScript Conversion Rules

| Vue 2 construct | TypeScript equivalent |
|---|---|
| `Vue.extend({ data() {} })` | Class with properties |
| `methods: { foo() {} }` | Class method `foo()` |
| `computed: { bar() {} }` | Getter `get bar()` |
| `static` methods (attached after `Vue.extend`) | `static` class methods |
| `vm.$set(obj, key, val)` | Plain `obj[key] = val` |
| `vm.$emit('eventName', data)` | Call parent method directly or callback property |
| `vm.$refs.childName` | Stored reference via setter method |
| `vm.$nextTick(fn)` | `setTimeout(fn, 0)` or React `useEffect` |
| `Vue.component('tag-name', Comp)` | Import + use directly |
| Template HTML | Separate `.tsx` component file |

---

## After-Update Summary Rule

After completing any task that modifies, creates, or deletes files, you MUST append a summary
entry to `/Users/I043125/work-migration/docs/MIGRATION_CONVERSATIONS.md` under a new
`#### Update log — <YYYY-MM-DD>` sub-heading inside the relevant conversation, OR as a
standalone `### Update — <topic> (<YYYY-MM-DD>)` entry if it is not tied to a specific
conversation. The entry must list:

- **Files modified** — path + one-line description of what changed
- **Files created** — path + purpose
- **Files deleted** — path + reason

Only skip this step if the task was pure read/research with no file changes.
