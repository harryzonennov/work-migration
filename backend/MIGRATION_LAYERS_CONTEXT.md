# Migration Layers Context — All 5 Projects

## Module Dependency Order (build order, still applies in new project)

```
ThorsteinPlatform
  └── ThorsteinFinance
        └── ThorsteinLogistics
              └── ThorsteinSalesDistribution
                    └── ThorsteinProduction
```

New project: single Spring Boot app — all modules coexist in one codebase.
Inter-module dependencies become intra-project service/event calls.

---

## Old Three-Layer Architecture → New Mapping

| Old Layer | Old Class | New Equivalent |
|---|---|---|
| Controller | `@Controller` extends `SEEditorController` / `SEBasicController` | `@RestController` |
| LogicManager | `@Service @Transactional` extends `ServiceEntityManager` | `@Service @Transactional` |
| DAO | `HibernateDefaultImpDAO` + one DAO per entity | `JpaRepository<Entity, String>` |
| HBM XML mapping | `[Entity].hbm.xml` | JPA annotations on entity class |
| Spring XML config | `[Module]-spring.xml` | `application.yml` + `@Configuration` |

**Key simplification**: The old DAO layer (56+ Hibernate DAOs) is entirely replaced by Spring Data JPA repositories. No custom DAO code needed for basic CRUD.

---

## Controller Layer Migration

### Old Base Classes → New Pattern

**Old:** `SEBasicController` / `SEEditorController` (Spring MVC, returns view names, session-based auth)
- `@InitBinder` for Date/Integer/Double type conversion
- `LockObjectManager` for concurrent edit locking
- `LogonInfoManager` for current user from session
- `ServiceDropdownListHelper` for dropdown data
- `ServiceMessageHelper` for i18n messages

**New:** Plain `@RestController` with:
```java
@RestController
@RequestMapping("/api/v1/[module]")
public class SalesContractController {

    @Autowired
    private SalesContractService salesContractService;

    @PostMapping("/sales-contracts")
    public ApiResponse<SalesContractDto> create(@RequestBody @Valid SalesContractDto dto) { ... }

    @GetMapping("/sales-contracts/{uuid}")
    public ApiResponse<SalesContractDto> get(@PathVariable String uuid) { ... }
}
```

**Decisions:**
- No base controller class needed — use `@ControllerAdvice` for global exception handling
- Lock management: defer to later (not critical for initial migration)
- Current user: inject from JWT via `@AuthenticationPrincipal`
- Dropdown data: return from dedicated `/api/v1/[module]/options` endpoints
- i18n: Spring Boot MessageSource (defer to later)

### Old `DocUIModelExtensionBuilder` Pattern → DTO Mapping

Old code used a complex fluent builder to configure bidirectional entity↔UI conversion.

**New:** Simple DTO classes with manual mapping (or MapStruct later):
```java
// Entity → DTO: in Service layer
public SalesContractDto toDto(SalesContract entity) { ... }

// DTO → Entity: in Service layer
public SalesContract toEntity(SalesContractDto dto) { ... }
```

No `DocUIModelExtensionBuilder`, `ServiceUIModelExtension`, or `UIModelConfigureMap` needed.

---

## LogicManager (Service) Layer Migration

### Old Base Class: `ServiceEntityManager` (2407 lines)

`ServiceEntityManager` is the core foundation that ALL `***Manager` classes in the 5 old projects extend.
Its methods provide: UUID/admin-data management, CRUD, bind-list diff, archive/delete, hierarchy helpers, and duplicate checking.

**New equivalent: `ServiceEntityService`** at `common/service/ServiceEntityService.java`.
All domain `***Service` classes MUST extend `ServiceEntityService`.

Key method mapping:

| Old `ServiceEntityManager` method | New `ServiceEntityService` equivalent |
|---|---|
| `setAdminData(node, CREATE, user, org)` | `setAdminDataCreate(node, userUUID, orgUUID)` |
| `setAdminData(node, UPDATE, user, org)` | `setAdminDataUpdate(node, userUUID, orgUUID)` |
| `newRootEntityNode(client)` | `prepareRootNode(node, client)` |
| `newEntityNode(parent, nodeName)` | `prepareChildNode(node, parent, nodeName)` |
| `insertSENode(seNode, user, org)` | `insertSENode(repository, node, userUUID, orgUUID)` — assigns UUID + stamps admin data + saves |
| `updateSENode(seNode, backNode, user, org)` | `updateSENode(repository, node, userUUID, orgUUID)` — stamps admin data + saves |
| `deleteSENode(seNode, user, org)` | `deleteSENode(repository, uuid)` |
| `getEntityNodeByUUID(uuid, nodeName, client)` | `getEntityNodeByUUID(repository, uuid)` |
| `getEntityNodeListByKey(client, CLIENT, name)` | `getEntityNodeListByClient(entityClass, client)` |
| `getEntityNodeListByKey(parentUUID, PARENTNODEUUID, name)` | `getEntityNodeListByParentNodeUUID(entityClass, parentUUID)` |
| `getEntityNodeListByKey(rootUUID, ROOTNODEUUID, name)` | `getEntityNodeListByRootNodeUUID(entityClass, rootUUID)` |
| `getEntityNodeListByKey(value, field, name, client)` | `getEntityNodeListByKey(entityClass, fieldName, value, client)` |
| `getEntityNodeListByKeyLastUpdate(client, name, offset, limit)` | `getEntityNodeListByKeyLastUpdate(entityClass, client, offset, limit)` |
| `getRecordNum(name, client)` | `getRecordNum(entityClass, client)` |
| `updateSEBindList(newList, backList, user, org)` | `updateSEBindList(repository, newList, backList, userUUID, orgUUID)` |
| `archiveModule(rootUUID, name, client)` | `archiveModule(entityClass, rootUUID, client)` — prefixes client with "arc_" |
| `restoreArchiveModule(rootUUID, name, client)` | `restoreArchiveModule(entityClass, rootUUID, client)` |
| `admDeleteModule(rootUUID, name, client)` | `admDeleteModule(entityClass, rootUUID, client)` |
| `deleteEntityNodeListByKey(value, field, name, client)` | `deleteEntityNodeListByKey(entityClass, fieldName, value, client)` |
| `checkIDDuplicate(id, name, client, excludeUUID)` | `checkIDDuplicate(entityClass, fieldName, value, client, excludeUUID)` |
| `changeParent(seNode, newParentUUID, level, ...)` | `changeParent(node, newParentNodeUUID, newParentLevel, userUUID, orgUUID)` |

**New Service pattern — all domain services extend ServiceEntityService:**
```java
@Service
@Transactional
public class SalesContractService extends ServiceEntityService {

    @Autowired
    protected SalesContractRepository salesContractRepository;

    public SalesContract createContract(SalesContract contract, String userUUID, String orgUUID) {
        contract.setStatus(SalesContract.STATUS_INITIAL);
        return insertSENode(salesContractRepository, contract, userUUID, orgUUID);
    }

    public SalesContract updateContract(SalesContract contract, String userUUID, String orgUUID) {
        return updateSENode(salesContractRepository, contract, userUUID, orgUUID);
    }

    public void deleteContract(String uuid) {
        deleteSENode(salesContractRepository, uuid);
    }
}
```

**Important**: Methods that were previously in old `***Manager` classes that delegated to `ServiceEntityManager`
are now handled by the base class. Do NOT copy-paste the old create/update/delete infrastructure into each service —
call `insertSENode`, `updateSENode`, `deleteSENode` from the base class instead.

### Old `DocumentContentSpecifier` Pattern

Old code: every document type had a `Specifier` class wiring together manager, UI builder, ID generation, status transitions, and workflow.

**New:** Not needed as a pattern. The responsibilities are split:
- Status transitions → explicit methods in Service class (`submit()`, `approve()`, `reject()`)
- ID generation → UUID in Service (or serial number service if business IDs needed)
- UI conversion → DTO mapping in Service or dedicated mapper
- Workflow → see Flowable decision below

### Old `ConfigureProxy` Pattern

Each entity had a `[Doc]ConfigureProxy.java` holding document type IDs and category codes as constants.

**New:** Keep as simple constants interface or enum:
```java
public interface SalesContractConstants {
    int DOCUMENT_TYPE = 1001;
    int CATEGORY_STANDARD = 1;
}
```

---

## Document Status Machine

Old `DocumentContent` status constants (all modules share these):

| Constant | Value | Meaning |
|---|---|---|
| `STATUS_INITIAL` | 1 | Draft |
| `STATUS_APPROVED` | 2 | Approved |
| `STATUS_DELIVERYDONE` | 4 | Delivery complete |
| `STATUS_PROCESSDONE` | 5 | Process complete |
| `STATUS_SUBMITTED` | 299 | Submitted for approval |
| `STATUS_INPROCESS` | 310 | In approval process |
| `STATUS_ACTIVE` | 305 | Active |
| `STATUS_REVOKE_SUBMIT` | 690 | Submission revoked |
| `STATUS_REJECT_APPROVAL` | 790 | Approval rejected |
| `STATUS_BLOCKED` | 910 | Blocked |
| `STATUS_ARCHIVED` | 980 | Archived |
| `STATUS_CANCELED` | 990 | Canceled |
| `STATUS_DELETED` | 991 | Deleted |

**Migrate as-is:** Add these constants to `DocumentContent.java`. Status transitions are enforced in the Service layer.

---

## Workflow (Flowable BPMN)

Old system used Flowable 6.6.0 for document approval workflows across all modules.

**Decision required — two options:**

| Option | Pros | Cons |
|---|---|---|
| **A: Keep Flowable** | No approval logic rewrite; complex multi-role approval preserved | Heavy dependency; complex Spring Boot 3 integration |
| **B: Simplified status machine** (recommended for initial migration) | Much simpler; status transitions in Service methods; easier to maintain | Multi-role/multi-step approvals need manual coding |

**Recommendation:** Start with Option B (status machine in Service layer) for initial migration. Add Flowable back later if needed for specific workflows.

---

## Cross-Module Integration

### Old approach
Each WAR had its own Spring context and loaded all upstream HBM configs. Cross-module calls were direct Java method calls (all in same JVM per WAR, but each WAR duplicated the upstream classes).

### New approach (modular monolith)
All modules in one Spring Boot app. Cross-module calls are direct `@Autowired` service calls within the same JVM.

**Cross-schema DB references:** Still use plain `String refXxxUUID` fields — no `@ManyToOne` across schemas.

**Finance integration proxies** (e.g. `SalesContractPriceProxy`, `PurchaseContractIncomeProxy`):
→ Migrate as `@Service` classes calling `FinanceService` directly.

**Cross-document conversion** (e.g. Forecast → SalesContract, Plan → ProductionOrder):
→ Migrate `CrossConvertRequest` logic as Service methods.

---

## Complete Entity Inventory by Module

### platform schema (from ThorsteinPlatform)

**foundation/Model/Common/**
- `LogonUser`, `LogonInfo`, `UserRole` — authentication
- `Organization`, `OrganizationFunction`, `HostCompany` — company structure
- `NavigationSystemSetting`, `NavigationGroupSetting`, `NavigationItemSetting` — UI nav
- `CalendarSetting`, `CalendarWorkTimeSetting` — work calendar
- `SerialNumberSetting` — serial ID generation
- `ServiceEntityLogModel`, `ServiceEntityLogItem` — audit log
- `ClientDBConnectProperty` — (not needed in new design)

**foundation/Model/Authorization/**
- `Role`, `ActionCode`, `AuthorizationGroup`, `AuthorizationObject`
- `RoleAuthorization`, `RoleSubAuthorization`

**foundation/Model/Account/**
- `CorporateCustomer`, `IndividualCustomer` — customer master
- `Employee` — employee master
- `CustomerParentOrgReference`, `EmpLogonUserReference`, `EmployeeOrgReference` — ref links

**foundation/Model/DocFlow/** — workflow routing entities (only needed if keeping Flowable)

**coreFunction/Model/Material/** — Material master entities
**coreFunction/Model/Warehouse/** — Warehouse configuration entities

### finance schema (from ThorsteinFinance)
- `FinAccount` + sub-entities (Attachment, MaterialItem, Log, ObjectRef, DocRef, Prerequirement, SettleItem)
- `FinAccountTitle` — chart of accounts hierarchy
- `FinanceDocument` — finance document reference
- System resource: `AppAccountField`, `ResFinAccountFieldSetting`, `ResFinAccountProcessCode`, etc.

### logistics schema (from ThorsteinLogistics)

**Delivery:**
- `InboundDelivery`, `OutboundDelivery`, `InventoryCheckOrder`, `InventoryTransferOrder`
- Each with: MaterialItem, Attachment, ActionNode, Party sub-entities

**Supply Chain:**
- `PurchaseContract`, `PurchaseRequest`, `PurchaseOrder`, `PurchaseReturnOrder`
- `Inquiry`, `QualityInspectOrder`, `WasteProcessOrder`
- Each with standard sub-entities

**Warehouse:**
- `WarehouseStore`, `WarehouseStoreItem`, `WarehouseStoreItemLog`

### sales schema (from ThorsteinSalesDistribution)
- `SalesContract` + sub-entities (MaterialItem, MaterialItemAttachment, Party, ActionNode, Attachment)
- `SalesForcast` + sub-entities
- `SalesReturnOrder` + sub-entities
- `SalesArea` — configuration
- `SettleOrder`, `SettleMaterialItem`, `SettleOrderAttachment` — settlement

### production schema (from ThorsteinProduction)

**BOM:**
- `BillOfMaterialOrder`, `BillOfMaterialTemplate` + sub-entities

**Planning & Execution:**
- `ProductionPlan`, `ProductionOrder`, `RepairProdOrder` + sub-entities
- `ProdJobOrder`, `ProdPickingOrder` — execution
- `ProdWorkCenter`, `ProductionResourceUnion` — resource config

**Process:**
- `ProcessRouteOrder`, `ProcessBOMOrder`, `ProdProcess`

---

## Migration Scope Summary

| Area | Total in old system | Approach |
|---|---|---|
| Entity classes (root) | ~50+ across 5 projects | Migrate with JPA annotations |
| Sub-entity classes (items, attachments, etc.) | ~150+ | Migrate with JPA annotations |
| HBM XML files | ~40+ | Delete after annotation conversion |
| DAO classes | ~60+ | Replace with JpaRepository |
| LogicManager/Service classes | ~100+ | Migrate as `@Service`, drop Specifier pattern |
| Controller classes | ~100+ | Migrate as `@RestController` |
| Flowable BPMN workflows | ~5+ `.bpmn20.xml` | Defer — use status machine first |
| DocUIModelExtensionBuilder | Per document type | Replace with DTO mapping |
| i18n property files | ~100+ | Migrate to Spring MessageSource (defer) |

---

## Recommended Migration Sequence

1. **Phase 1 — Foundation** (platform schema, no business logic)
   - `ServiceEntityNode`, `ReferenceNode`, `DocMatItemNode`, `DocumentContent` (done)
   - `LogonUser`, `Role`, `UserRole` + JWT security config
   - `Organization`, `Employee`, `CorporateCustomer` entities
   - `MaterialMaster` entities (needed by all other modules)

2. **Phase 2 — Finance** (finance schema)
   - `FinAccountTitle`, `FinAccount` + sub-entities
   - `FinanceDocument`

3. **Phase 3 — Logistics** (logistics schema)
   - `PurchaseContract` + supply chain docs
   - `InboundDelivery`, `OutboundDelivery`
   - `WarehouseStore` + inventory

4. **Phase 4 — Sales** (sales schema)
   - `SalesContract` + sub-entities
   - `SalesForcast`, `SalesReturnOrder`

5. **Phase 5 — Production** (production schema)
   - `ProductionOrder`, `ProductionPlan`, `BillOfMaterialOrder`
   - Process and job order entities

**Within each phase, order per entity type:**
1. Entity class (JPA annotations)
2. JpaRepository
3. Service (`@Service`)
4. Controller (`@RestController`)
5. DTO class
