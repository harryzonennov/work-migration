
# Architecture Context - IntelligentPlatform

## Project Location
- **Java source root**: `/Users/I043125/work2/IntelligentPlatform/src/main/java/com/company/IntelligentPlatform/`
- **Resources**: `/Users/I043125/work2/IntelligentPlatform/src/main/resources/`
- **Templates (reference copies)**: `/Users/I043125/work-migration/backend/java-templates/`

## Package Structure
```
com.company.IntelligentPlatform/
├── common/              # From ThorsteinPlatform
│   ├── config/
│   ├── security/
│   ├── exception/
│   ├── response/
│   └── model/
│       ├── ServiceEntityNode.java    # @MappedSuperclass — 16 base fields (uuid, client, ...)
│       ├── ReferenceNode.java        # @MappedSuperclass — extends ServiceEntityNode, adds ref fields
│       ├── DocMatItemNode.java       # @MappedSuperclass — extends ReferenceNode, adds item/price fields
│       ├── DocumentContent.java      # @MappedSuperclass — extends ServiceEntityNode, adds doc fields
│       ├── LogonUser.java            # extends ServiceEntityNode
│       └── Role.java                 # extends ServiceEntityNode
├── finance/             # From ThorsteinFinance
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   │   └── FinanceDocument.java      # extends ServiceEntityNode
│   └── dto/
├── logistics/           # From ThorsteinLogistics
│   └── model/
│       └── PurchaseContract.java     # extends DocumentContent
├── sales/               # From ThorsteinSalesDistribution
│   └── model/
│       ├── SalesContract.java        # extends DocumentContent
│       └── SalesContractMaterialItem.java  # extends DocMatItemNode
└── production/          # From ThorsteinProduction
    └── model/
        └── ProductionOrder.java      # extends DocumentContent
```

## API URL Pattern
- Auth:       /api/v1/auth/**
- Finance:    /api/v1/finance/**
- Logistics:  /api/v1/logistics/**
- Sales:      /api/v1/sales/**
- Production: /api/v1/production/**

## DB Schema Separation
- Schema: platform      (users, roles, auth — from ThorsteinPlatform)
- Schema: finance     (finance documents, accounts — from ThorsteinFinance)
- Schema: logistics   (purchase, delivery, warehouse — from ThorsteinLogistics)
- Schema: sales       (contracts, orders — from ThorsteinSalesDistribution)
- Schema: production  (production orders, BOM, plans — from ThorsteinProduction)
- Table name = Java class name (camelCase, preserved from old design)
- Column name = Java field name (camelCase, preserved from old design)

## Entity Class Hierarchy

```
ServiceEntityNode  (@MappedSuperclass — common/model)
│   16 base fields: uuid (String PK), client, id, name, parentNodeUUID,
│   rootNodeUUID, nodeLevel, serviceEntityName, nodeName, createdBy,
│   createdTime, lastUpdateBy, lastUpdateTime, nodeSpecifyType,
│   note, resEmployeeUUID, resOrgUUID
│
├── LogonUser                  (schema: platform)
├── Role                       (schema: platform)
├── FinanceDocument            (schema: finance)
├── SalesContractMaterialItem  (schema: sales)
│
└── DocumentContent  (@MappedSuperclass — common/model)
        Adds: status, priorityCode, documentCategoryType,
              prevProfDocType/UUID, prevDocType/UUID,
              nextProfDocType/UUID, nextDocType/UUID
        │
        ├── SalesContract      (schema: sales)
        ├── PurchaseContract   (schema: logistics)
        └── ProductionOrder    (schema: production)
```

## Cross-Module Reference Rule
- No `@ManyToOne` or DB FK across schemas
- Cross-module references stored as plain `String` UUID fields (e.g. `refFinAccountUUID`)
- Resolved via service layer when needed



## Standard Response Format
```json
{
  "success": true,
  "message": "Success",
  "data": {},
  "timestamp": "2024-01-01T00:00:00"
}
```

## Security Rules
- JWT Token authentication
- Role-based: ADMIN, FINANCE, LOGISTICS, SALES, PRODUCTION
- Each module has its own roles
