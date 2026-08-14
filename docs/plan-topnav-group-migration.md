# Plan: Top-Nav Group Migration

Migrate the legacy 3-button top navigation bar (group switcher) into the new React/Minton UI,
with the left sidebar content changing per active group.

---

## Legacy behaviour (how it works today)

The legacy system has **two navigation layers**:

1. **Top bar** — 3 colored icon buttons, each representing a module group.
2. **Left sidebar** — shows the menu for the currently active group.

Switching groups is a **full page reload**: clicking a top-bar button navigates to the group's
cockpit HTML page (e.g. `SupplyChainCockpit.html`), which loads its own sidebar JSON.

### The 3 groups (from `admin/js/navigation.json`)

| id | Default page | Background color | Icon | Sidebar JSON |
|---|---|---|---|---|
| `logistics` | `SupplyChainCockpit.html` | `background-green` | `nmd-add-shopping-cart` | `navigationLogistics.json` |
| `systemAdmin` | `LogonUserList.html` | `background-darkblue2` | `fa fa-gear` | `navigationSystemAdmin.json` |
| `production` | `ProductionCockpit.html` | `background-blue` | `ion-wrench` | `navigationProduction.json` |

### Sidebar content per group

**Logistics** (9 groups): SupplyChainCockpit dashboard, Material, CorporateSupplier,
PurchaseRequest, PurchaseContract, SalesContract, BidInvitation, WarehouseManagement,
InboundOutbound.

**SystemAdmin** (7 groups): LogonUser, SystemConfigureResource, ServiceFlowModel,
ServiceDocumentSetting, ServiceEntityLog, Material config (MaterialType, StandardMaterialUnit),
FinanceSetting, HostCompany (Organization, Employee, Warehouse).

**Production** (5 groups): ProductionCockpit dashboard, ProductionConfigure,
ProductionPlanManage, ProductionProcess, ProdPickingOrder.

---

## New UI approach

The new UI is a **SPA** (no full page reload). The equivalent of the legacy group switch is:
**swap which `MenuConfigItem[]` tree is passed to `LeftSideBar`** when the active group changes.

The active group is tracked in React state in `MainLayout`. The top-bar buttons are rendered in
the `navbar-custom` topbar — the same place Minton puts its own `navbar-nav`.

Since the new UI has far fewer migrated pages than the legacy, **dummy entries** (greyed-out,
no `path`) are used as placeholders for content not yet migrated.

---

## Implementation plan

### Step 1 — Extend `MenuConfigItem` to support nav-group metadata

**File:** `src/router/menuConfig.ts`

Add an optional `disabled` flag to `MenuConfigItem` for stub entries:
```ts
export interface MenuConfigItem {
    key: string;
    label: string;
    icon?: React.ReactNode;
    path?: string;
    children?: MenuConfigItem[];
    disabled?: boolean;   // ← add: renders as greyed-out, no navigation
}
```

### Step 2 — Define the nav-group config

**File:** `src/router/navGroupConfig.ts` (new file)

```ts
import React from 'react';
import type { MenuConfigItem } from './menuConfig';
import i18n from '@/i18n';

export interface NavGroup {
    id: string;
    iconClass: string;       // CSS icon class (nmd/fa/ion)
    colorClass: string;      // background color CSS class
    defaultPath: string;     // where to navigate when the group button is clicked
    getMenu: () => MenuConfigItem[];
}

export function getNavGroups(): NavGroup[] {
    const t = (key: string) => i18n.t(`menu:${key}`);
    return [
        {
            id: 'logistics',
            iconClass: 'nmd nmd-add-shopping-cart',
            colorClass: 'background-green',
            defaultPath: '/logistics/purchaseContract',
            getMenu: () => [
                {
                    key: 'material',
                    label: t('material'),
                    icon: '…',
                    children: [
                        { key: 'material-list',   label: t('materialList'),   path: '/platform/material' },
                        { key: 'smu-list',        label: t('standardMaterialUnitList'), path: '/platform/standardMaterialUnit' },
                        { key: 'msku-list',       label: t('materialStockKeepUnit'),    disabled: true },
                        { key: 'registered-product', label: t('registeredProduct'),     disabled: true },
                    ],
                },
                {
                    key: 'supplier',
                    label: t('corporateSupplier'),
                    children: [
                        { key: 'corporate-customer',  label: t('corporateCustomer'),  disabled: true },
                        { key: 'corporate-supplier',  label: t('corporateSupplier'),  disabled: true },
                        { key: 'individual-customer', label: t('individualCustomer'), disabled: true },
                    ],
                },
                {
                    key: 'purchase',
                    label: t('purchaseRequest'),
                    children: [
                        { key: 'request-list', label: t('requestList'), path: '/logistics/purchaseRequest' },
                    ],
                },
                {
                    key: 'contract',
                    label: t('purchaseContract'),
                    children: [
                        { key: 'inquiry-list',       label: t('inquiry'),            disabled: true },
                        { key: 'contract-list',      label: t('contractList'),       path: '/logistics/purchaseContract' },
                        { key: 'return-order-list',  label: t('purchaseReturnOrder'), disabled: true },
                    ],
                },
                {
                    key: 'sales',
                    label: t('salesContract'),
                    children: [
                        { key: 'sales-contract-list', label: t('salesContract'),    disabled: true },
                        { key: 'sales-return-list',   label: t('salesReturnOrder'), disabled: true },
                        { key: 'sales-forecast-list', label: t('salesForcast'),     disabled: true },
                    ],
                },
                {
                    key: 'warehouse',
                    label: t('warehouseManagement'),
                    children: [
                        { key: 'warehouse-store',       label: t('warehouseStore'),        disabled: true },
                        { key: 'inventory-check',       label: t('inventoryCheckOrder'),   disabled: true },
                        { key: 'inventory-transfer',    label: t('inventoryTransferOrder'), disabled: true },
                        { key: 'waste-process',         label: t('wasteProcessOrder'),     disabled: true },
                    ],
                },
                {
                    key: 'inbound-outbound',
                    label: t('inboundOutbound'),
                    children: [
                        { key: 'quality-inspect', label: t('qualityInspectOrder'), disabled: true },
                        { key: 'inbound-delivery', label: t('inboundDelivery'),    disabled: true },
                        { key: 'outbound-delivery', label: t('outboundDelivery'),  disabled: true },
                    ],
                },
            ],
        },
        {
            id: 'systemAdmin',
            iconClass: 'fa fa-gear',
            colorClass: 'background-darkblue2',
            defaultPath: '/platform/standardMaterialUnit',
            getMenu: () => [
                {
                    key: 'logon-user',
                    label: t('logonUser'),
                    children: [
                        { key: 'logon-user-list', label: t('logonUser'),  disabled: true },
                        { key: 'role-list',       label: t('role'),       disabled: true },
                    ],
                },
                {
                    key: 'material-config',
                    label: t('materialConfig'),
                    children: [
                        { key: 'smu-admin',      label: t('standardMaterialUnitList'), path: '/platform/standardMaterialUnit' },
                        { key: 'material-admin', label: t('materialList'),             path: '/platform/material' },
                        { key: 'material-type',  label: t('materialType'),             disabled: true },
                    ],
                },
                {
                    key: 'host-company',
                    label: t('hostCompany'),
                    children: [
                        { key: 'org-list',       label: t('organization'), disabled: true },
                        { key: 'employee-list',  label: t('employee'),     disabled: true },
                        { key: 'warehouse-list', label: t('warehouse'),    disabled: true },
                    ],
                },
                {
                    key: 'finance-setting',
                    label: t('financeSetting'),
                    children: [
                        { key: 'fin-account',       label: t('finAccount'),      disabled: true },
                        { key: 'fin-account-title', label: t('finAccountTitle'), disabled: true },
                    ],
                },
            ],
        },
        {
            id: 'production',
            iconClass: 'ion-wrench',
            colorClass: 'background-blue',
            defaultPath: '/',
            getMenu: () => [
                {
                    key: 'prod-configure',
                    label: t('productionConfigure'),
                    children: [
                        { key: 'bom-template',   label: t('billOfMaterialTemplate'), disabled: true },
                        { key: 'work-center',    label: t('prodWorkCenter'),          disabled: true },
                    ],
                },
                {
                    key: 'prod-plan',
                    label: t('productionPlan'),
                    children: [
                        { key: 'prod-plan-list', label: t('productionPlan'), disabled: true },
                    ],
                },
                {
                    key: 'prod-process',
                    label: t('productionProcess'),
                    children: [
                        { key: 'prod-order-list',   label: t('productionOrder'),   disabled: true },
                        { key: 'repair-prod-list',  label: t('repairProdOrder'),   disabled: true },
                        { key: 'quality-prod-list', label: t('qualityInspect'),    disabled: true },
                    ],
                },
                {
                    key: 'prod-picking',
                    label: t('prodPickingOrder'),
                    children: [
                        { key: 'picking-list', label: t('prodPickingOrder'), disabled: true },
                        { key: 'return-list',  label: t('prodReturnOrder'),  disabled: true },
                    ],
                },
            ],
        },
    ];
}
```

### Step 3 — Add `disabled` rendering to `LeftSideBar`

**File:** `src/layouts/LeftSideBar/index.tsx`

When a `MenuConfigItem` has `disabled: true`:
- Render the `<a>` as `<span>` (or `<a>` with `e.preventDefault()`)
- Add `text-muted` + `pe-none` CSS classes so it is visually greyed-out and non-clickable
- No `onClick` handler

### Step 4 — Add nav-group state to `MainLayout`

**File:** `src/layouts/MainLayout.tsx`

```ts
import { getNavGroups } from '@/router/navGroupConfig';

// Inside MainLayout:
const navGroups = React.useMemo(() => getNavGroups(), [i18n.language]);

// Derive active group from current pathname (fallback to first group)
const activeGroupId = React.useMemo(() => {
    for (const group of navGroups) {
        if (group.getMenu().some(item => itemMatchesPath(item, pathname))) {
            return group.id;
        }
    }
    return navGroups[0].id;
}, [navGroups, pathname]);

// Active group's menu tree replaces the flat getMenuConfig() call
const menu = React.useMemo(
    () => navGroups.find(g => g.id === activeGroupId)?.getMenu() ?? [],
    [navGroups, activeGroupId]
);
```

Remove the existing `getMenuConfig()` import and the `menu` useMemo that calls it.

### Step 5 — Render the top-bar group buttons

**File:** `src/layouts/MainLayout.tsx`

Inside `.navbar-custom`, add a `<ul className="nav navbar-nav">` before the existing right-side
`<ul className="topnav-menu float-end">`:

```tsx
{/* Top-bar group switcher — mirrors legacy navigation.json groups */}
<ul className="nav navbar-nav">
    {navGroups.map(group => (
        <li key={group.id} className={activeGroupId === group.id ? 'active' : ''}>
            <a
                className={group.colorClass}
                href="#"
                title={group.id}
                onClick={(e) => {
                    e.preventDefault();
                    navigate(group.defaultPath);
                }}
                style={{ display: 'flex', alignItems: 'center', justifyContent: 'center',
                         width: 40, height: 40, borderRadius: 4 }}
            >
                <i className={group.iconClass} style={{ color: '#fff' }} />
            </a>
        </li>
    ))}
</ul>
```

### Step 6 — Add missing i18n menu keys

**Files:** `src/i18n/locales/en/Menu.json` + `zh/Menu.json`

Add keys for every new sidebar label that doesn't already exist:
`corporateSupplier`, `corporateCustomer`, `individualCustomer`, `inquiry`,
`purchaseReturnOrder`, `salesContract`, `salesReturnOrder`, `salesForcast`,
`warehouseManagement`, `warehouseStore`, `inventoryCheckOrder`, `inventoryTransferOrder`,
`wasteProcessOrder`, `inboundOutbound`, `qualityInspectOrder`, `inboundDelivery`,
`outboundDelivery`, `logonUser`, `role`, `materialConfig`, `materialType`,
`hostCompany`, `organization`, `employee`, `warehouse`, `financeSetting`,
`finAccount`, `finAccountTitle`, `productionConfigure`, `billOfMaterialTemplate`,
`prodWorkCenter`, `productionPlan`, `productionProcess`, `productionOrder`,
`repairProdOrder`, `prodPickingOrder`, `prodReturnOrder`.

---

## File summary

| # | File | Action |
|---|---|---|
| 1 | `src/router/navGroupConfig.ts` | Create — 3-group config with menus |
| 2 | `src/router/menuConfig.ts` | Edit — add `disabled?` to `MenuConfigItem` |
| 3 | `src/layouts/LeftSideBar/index.tsx` | Edit — render disabled items as non-clickable stubs |
| 4 | `src/layouts/MainLayout.tsx` | Edit — group state, active group detection, top-bar buttons, swap menu source |
| 5 | `src/i18n/locales/en/Menu.json` | Edit — add ~30 new label keys |
| 6 | `src/i18n/locales/zh/Menu.json` | Edit — add matching Chinese labels |

**6 files total. 1 new, 5 edits. No backend changes.**

---

## Decisions / constraints

- **No page reload** — group switch is a `navigate()` call to the group's `defaultPath`.
- **Active group is derived from the current URL** — no extra persisted state needed.
- **Dummy entries** use `disabled: true` — they appear in the sidebar but do nothing.
  As pages are migrated, replace `disabled: true` with a real `path`.
- **`getMenuConfig()` is kept** for any caller outside MainLayout that still uses it (e.g. breadcrumb helpers), but `MainLayout` no longer calls it directly.
- The legacy `navigationFinance.json` group is not represented as a top-bar button in the
  legacy `navigation.json` (finance was merged into systemAdmin). No finance top-bar button needed.
