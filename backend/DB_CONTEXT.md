# Database Migration Context

## Old Database Info
- MySQL Version: 5.7
- Single shared database for all 5 projects (one DB, no separation)
- Table names = Java class names (camelCase, e.g. LogonUser, SalesContract, ProductionOrder)
- Column names = Java field names (camelCase, e.g. parentNodeUUID, createdTime)

## New Database
- MySQL Version: 8.x
- Single Database: db_IntelligentService
- Schema management: Flyway
- Table names = Java class names (preserved from old design, camelCase)
- Column names = Java field names (preserved from old design, camelCase)
- Module separation via MySQL schemas (not table name prefixes):
  - Schema: platform      (from ThorsteinPlatform)
  - Schema: finance     (from ThorsteinFinance)
  - Schema: logistics   (from ThorsteinLogistics)
  - Schema: sales       (from ThorsteinSalesDistribution)
  - Schema: production  (from ThorsteinProduction)

## Table Migration Mapping
Note: Old table names = class names (camelCase). New table names = same class names, moved to schemas.

### ThorsteinPlatform → schema: platform
| Old Table                  | New Table (schema.Table)              | Changes                        |
|----------------------------|---------------------------------------|--------------------------------|
| LogonUser                  | platform.LogonUser                      | Password: MD5 → BCrypt         |
| Role                       | platform.Role                           | No change                      |
| UserRole                   | platform.UserRole                       | No change                      |
| LogonUserOrgReference      | platform.LogonUserOrgReference          | No change                      |

### ThorsteinFinance → schema: finance
| Old Table                  | New Table (schema.Table)              | Changes                        |
|----------------------------|---------------------------------------|--------------------------------|
| FinanceDocument            | finance.FinanceDocument               | No change                      |
| FinAccountTitle            | finance.FinAccountTitle               | No change                      |
| FinAccount                 | finance.FinAccount                    | No change                      |

### ThorsteinSalesDistribution → schema: sales
| Old Table                  | New Table (schema.Table)              | Changes                        |
|----------------------------|---------------------------------------|--------------------------------|
| SalesContract              | sales.SalesContract                   | No change                      |
| SalesContractMaterialItem  | sales.SalesContractMaterialItem       | No change                      |
| SalesContractParty         | sales.SalesContractParty              | No change                      |
| SalesArea                  | sales.SalesArea                       | No change                      |

### ThorsteinLogistics → schema: logistics
| Old Table                  | New Table (schema.Table)              | Changes                        |
|----------------------------|---------------------------------------|--------------------------------|
| PurchaseContract           | logistics.PurchaseContract            | No change                      |
| PurchaseOrder              | logistics.PurchaseOrder               | No change                      |
| InboundDelivery            | logistics.InboundDelivery             | No change                      |
| OutboundDelivery           | logistics.OutboundDelivery            | No change                      |

### ThorsteinProduction → schema: production
| Old Table                  | New Table (schema.Table)              | Changes                        |
|----------------------------|---------------------------------------|--------------------------------|
| ProductionOrder            | production.ProductionOrder            | No change                      |
| ProductionPlan             | production.ProductionPlan             | No change                      |
| BillOfMaterialOrder        | production.BillOfMaterialOrder        | No change                      |
| ProdWorkCenter             | production.ProdWorkCenter             | No change                      |

## Flyway Migration Files
- V1__init_schema.sql          (create schemas: platform, finance, logistics, sales, production)
- V2__init_platform.sql          (from ThorsteinPlatform tables)
- V3__init_finance.sql         (from ThorsteinFinance tables)
- V4__init_logistics.sql       (from ThorsteinLogistics tables)
- V5__init_sales.sql           (from ThorsteinSalesDistribution tables)
- V6__init_production.sql      (from ThorsteinProduction tables)
- V7__data_migration.sql       (migrate old data)

## Cross-Module FK Removal
Old: All cross-module relationships referenced by UUID string (no DB FK even in old projects)
New: Same pattern preserved — store refXxxUUID as plain String column, no @ManyToOne across schemas