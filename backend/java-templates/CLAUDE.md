# IntelligentPlatform — Claude Instructions

## Project
Spring Boot 3.2 + Hibernate 6 + MySQL 8 unified backend.
Migrated from 5 legacy projects: ThorsteinPlatform, ThorsteinFinance, ThorsteinLogistics, ThorsteinSalesDistribution, ThorsteinProduction.

- **Java source root**: `src/main/java/com/company/IntelligentPlatform/`
- **Reference templates**: `/Users/I043125/work-migration/backend/java-templates/`
- **Old projects (read-only reference)**: `/Users/I043125/work/`

---

## MANDATORY: Read before writing any Java code

**`docs/claude/CODE_STYLE.md`** — Java formatting rules derived from the original 5 projects.
The most common mistakes to avoid:
- Every field separated by one blank line
- Every method separated by one blank line
- Getters/setters are always multi-line — NEVER collapsed to one line
- Fields use `protected`, not `private`
- No Lombok

---

## Architecture Rules (NEVER violate)

1. **UUID assigned in Service layer** — `entity.setUuid(UUID.randomUUID().toString())` before save. Never in entity constructor or `@PrePersist`.
2. **No cross-schema FK annotations** — cross-module references are plain `String` UUID fields only. No `@ManyToOne`, no `@JoinColumn` across schemas.
3. **`ddl-auto: validate`** — Flyway owns the schema. Never change to `create`, `create-drop`, or `update`.
4. **Table name = Java class name** (camelCase). **Column name = Java field name** (camelCase). No renaming.
5. **`@Table(schema=...)` required** on every `@Entity`. Schemas: `platform`, `finance`, `logistics`, `sales`, `production`.
6. **URL paths camelCase, no hyphens**: `/api/v1/logistics/purchaseContracts` — never `/purchase-contracts`.

## Module → Schema mapping
| Module package | Schema |
|---|---|
| common, platform | platform |
| finance | finance |
| logistics | logistics |
| sales | sales |
| production | production |

## Entity Inheritance (never extend the wrong base)
- All entities extend `ServiceEntityNode` (directly or via a superclass)
- `ServiceEntityNode` → `DocumentContent` → domain entities (orders, plans, contracts)
- `ServiceEntityNode` → `ReferenceNode` → `DocMatItemNode` → item/line entities
- `@MappedSuperclass` classes do NOT have `@Table` or `@Entity`
- See memory file for full tree

## Layer pattern
- `@RestController` → `@Service @Transactional` → `JpaRepository`
- All `***Service` classes MUST extend `ServiceEntityService` (common/service/ServiceEntityService.java)
- Use `insertSENode(repo, entity, userUUID, orgUUID)` — never call `repo.save()` directly for create
- Use `updateSENode(repo, entity, userUUID, orgUUID)` — never call `repo.save()` directly for update
- Use `deleteSENode(repo, uuid)` for deletes
- `@Transactional(readOnly = true)` on all read methods
- `ResponseEntity<ApiResponse<T>>` return type on all controller methods
- DTO: `toEntity()` for create, `applyTo(entity)` for update; null-check String/Object fields; always set primitives

## Flyway migrations
- Next migration: `V4__...sql`
- Location: `src/main/resources/db/migration/`
- All table DDL uses schema prefix: `CREATE TABLE IF NOT EXISTS \`platform\`.\`tableName\``
