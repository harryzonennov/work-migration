# HBM XML → JPA Annotation Conversion Guide

## Context
- **Source**: Spring 4 + Hibernate 5, XML-based HBM mappings, MySQL 5.7, single shared database
- **Target**: Spring Boot 3.2 + Hibernate 6, JPA annotations, MySQL 8, single database with schemas
- **Table naming**: table name = Java class name (camelCase) — preserved from old design
- **Column naming**: column name = Java field name (camelCase) — preserved from old design
- **PK**: String UUID, assigned by Manager layer before save — preserved from old design
- **Module separation**: MySQL schemas (`platform`, `finance`, `logistics`, `sales`, `production`)

---

## Required Dependencies (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

> Remove any explicit `hibernate-core` dependency — Spring Boot 3.2 manages it.

---

## Conversion Pattern (1-to-1 Mapping)

### Old: `LogonUser.hbm.xml` (real example from ThorsteinPlatform)
```xml
<hibernate-mapping>
    <class name="platform.foundation.Model.Common.LogonUser" table="LogonUser">
        <id name="uuid" type="java.lang.String">
            <column name="uuid" />
            <generator class="assigned" />
        </id>
        <property name="client" type="java.lang.String"><column name="client" /></property>
        <property name="createdBy" type="java.lang.String"><column name="createdBy" /></property>
        <property name="createdTime" type="java.util.Date"><column name="createdTime" /></property>
        <property name="status" type="int"><column name="status" /></property>
        <property name="password" type="java.lang.String"><column name="password" /></property>
    </class>
</hibernate-mapping>
```

### New: `LogonUser.java` (annotated entity)
```java
package com.company.IntelligentPlatform.common.model;

import jakarta.persistence.*;

@Entity
@Table(name = "LogonUser", schema = "platform")  // table name = class name, schema = module
public class LogonUser extends ServiceEntityNode {  // 16 base fields in ServiceEntityNode

    @Column(name = "status")
    private int status;

    @Column(name = "password")
    private String password;   // BCrypt — old code used MD5

    // getters and setters
}
```

---

## Conversion Rules

| HBM XML | JPA Annotation | Notes |
|---|---|---|
| `<class name="Foo" table="Foo">` | `@Entity` + `@Table(name="Foo", schema="module")` | table name = class name, no rename |
| `<id>` with `generator class="assigned"` | `@Id` + `@Column(name="uuid")` | UUID assigned by Manager layer before save — not generated in entity |
| `<property name="foo" column="foo">` | `@Column(name="foo")` — or omit if field name matches | column name = field name, no rename |
| `type="java.lang.String"` | `String` | |
| `type="int"` | `int` or `Integer` | |
| `type="double"` | `double` or `Double` | |
| `type="boolean"` | `boolean` or `Boolean` | |
| `type="java.util.Date"` | `LocalDateTime` or `LocalDate` | Migrate from java.util.Date |
| `type="binary"` | `@Lob byte[]` | For file content fields |
| `type="text"` | `@Column(columnDefinition = "TEXT")` | |

---

## Package & Class Naming Convention

| Old Project | New Package | New Schema |
|---|---|---|
| ThorsteinPlatform | `com.company.IntelligentPlatform.common` | `platform` |
| ThorsteinFinance | `com.company.IntelligentPlatform.finance` | `finance` |
| ThorsteinLogistics | `com.company.IntelligentPlatform.logistics` | `logistics` |
| ThorsteinSalesDistribution | `com.company.IntelligentPlatform.sales` | `sales` |
| ThorsteinProduction | `com.company.IntelligentPlatform.production` | `production` |

---

## Entity Class Hierarchy

All entities extend one of two `@MappedSuperclass` bases depending on their type:

```
ServiceEntityNode  (@MappedSuperclass)
│   16 base fields shared by ALL entities:
│   uuid (String PK), client, id, name, parentNodeUUID, rootNodeUUID,
│   nodeLevel, serviceEntityName, nodeName, createdBy, createdTime,
│   lastUpdateBy, lastUpdateTime, nodeSpecifyType, note,
│   resEmployeeUUID, resOrgUUID
│
├── LogonUser                  → extend ServiceEntityNode directly
├── Role                       → extend ServiceEntityNode directly
├── FinanceDocument            → extend ServiceEntityNode directly
│
├── ReferenceNode  (@MappedSuperclass)
│       Adds: refUUID, refSEName, refNodeName, refPackageName
│       │
│       └── DocMatItemNode  (@MappedSuperclass)
│               Adds: reservation fields, doc chain item links,
│                     material/price fields (amount, itemPrice, unitPrice, ...)
│               │
│               └── SalesContractMaterialItem  → extend DocMatItemNode
│
└── DocumentContent  (@MappedSuperclass, extends ServiceEntityNode)
        Additional document root fields:
        status, priorityCode, documentCategoryType,
        prevProfDocType, prevProfDocUUID, prevDocType, prevDocUUID,
        nextProfDocType, nextProfDocUUID, nextDocType, nextDocUUID
        │
        ├── SalesContract      → extend DocumentContent
        ├── PurchaseContract   → extend DocumentContent
        └── ProductionOrder    → extend DocumentContent
```

**Rules for choosing superclass:**
- Is a document root node (has `status`, `priorityCode`, doc chain links at document level)? → extend `DocumentContent`
- Is a line-item/material node referencing a parent document? → extend `DocMatItemNode`
- Otherwise → extend `ServiceEntityNode` directly

---

## Cross-Module Reference Rule

Old projects used UUID string references across tables (no DB-level FK between modules).
**Preserve this pattern** — store cross-module refs as plain `String` UUID fields:

```java
// References an entity in another schema — UUID only, no @ManyToOne, no FK
@Column(name = "refFinAccountUUID")
private String refFinAccountUUID;   // resolved via service layer if needed
```

---

## Step-by-Step Conversion Checklist

For each HBM file:
- [ ] Keep table name = class name (no renaming needed)
- [ ] Add `@Entity` + `@Table(name = "ClassName", schema = "moduleschema")`
- [ ] Convert `<id generator="assigned">` → `@Id @Column(name="uuid")` — UUID is set by Manager layer, not in the entity
- [ ] Determine superclass:
  - Has `status`, `priorityCode`, doc chain fields at document level → extend `DocumentContent`
  - Is a line-item/material node referencing a parent document → extend `DocMatItemNode`
  - Otherwise → extend `ServiceEntityNode`
- [ ] Do NOT re-declare inherited fields — they are inherited from the superclass chain
- [ ] Convert each entity-specific `<property>` → `@Column(name="fieldName")` (no rename needed)
- [ ] Replace `java.util.Date` with `LocalDateTime` or `LocalDate`
- [ ] Keep `type="binary"` as `@Lob byte[]`
- [ ] Cross-module refs: keep as `String refXxxUUID` field — no `@ManyToOne`
- [ ] Delete the `.hbm.xml` file
- [ ] Remove entry from `hibernate.cfg.xml`
