# Frontend Migration Context

## Source Project: ThorSalesDistributionUI
- Framework: Vue 2.x
- Build Tool: Webpack
- State Management: Vuex
- Router: Vue Router 3
- HTTP Client: Axios
- UI Library: Element UI (Vue 2)
- Auth: Session cookie based

## Target Project: IntelligentServiceUI
- Framework: Vue 3.x
- Build Tool: Vite
- State Management: Pinia (replace Vuex)
- Router: Vue Router 4
- HTTP Client: Axios + composable wrapper
- UI Library: Element Plus (Vue 3)
- Auth: JWT Token (localStorage/httpOnly cookie)
- Language: TypeScript
- CSS: SCSS + Tailwind (optional)

## Key Migration Changes
### Vue 2 → Vue 3
- Options API → Composition API (<script setup>)
- Vuex → Pinia stores
- Vue Router 3 → Vue Router 4
- Element UI → Element Plus
- this.$store → useStore() composable
- this.$router → useRouter() composable
- Filters (removed in Vue 3) → computed/methods

### New Scope (was Sales only, now ALL modules)
- Add Finance pages
- Add Logistics pages
- Add Production pages
- Keep existing Sales pages (migrated)
- Add Admin/Platform pages

## Component Migration Mapping
### Pages to Migrate (Sales)
| Old Page              | New Page                        |
|-----------------------|---------------------------------|
| views/order/List.vue  | views/sales/order/ListView.vue  |
| views/order/Form.vue  | views/sales/order/FormView.vue  |
| views/customer/*.vue  | views/sales/customer/*.vue      |

### New Pages to Create
| Module     | Pages                                  |
|------------|----------------------------------------|
| Finance    | Invoice List/Form, Payment List/Form   |
| Logistics  | Shipment List/Form, Delivery Tracking  |
| Production | Plan List/Form, Material Management    |
| Common     | User Mgmt, Role Mgmt, Dashboard        |