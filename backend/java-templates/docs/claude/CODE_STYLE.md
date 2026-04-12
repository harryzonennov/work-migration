# Java Code Style Guide
## Derived from the 5 legacy projects (ThorsteinPlatform, ThorsteinFinance, ThorsteinLogistics, ThorsteinSalesDistribution, ThorsteinProduction)

This style MUST be followed exactly when generating or modifying any Java code in IntelligentPlatform.

---

## 1. Entity / Model Classes

### Field declarations
- Each field is on its own line, with **one blank line between every field**.
- Fields use `protected` visibility (not `private`).
- No annotations on the same line as a field unless it is a column-length annotation directly above it.

```java
// CORRECT
protected String refMaterialSKUUUID;

protected double amount;

protected String refUnitUUID;

protected int category;

// WRONG — no blank lines between fields
protected String refMaterialSKUUUID;
protected double amount;
protected String refUnitUUID;
```

### Constants
- Each constant is on its own line, with **one blank line between every constant**.
- Use `public static final int` (not enums).
- Constants related to the same concept may be grouped with a Javadoc comment above the group, followed by a blank line before the next group.

```java
// CORRECT
public static final int STATUS_INITIAL = 1;

public static final int STATUS_APPROVED = 2;

public static final int STATUS_INPRODUCTION = 3;

public static final int CATEGORY_MANUAL = 1;

public static final int CATEGORY_SYSTEM = 2;
```

### Constructor
- Entity classes have a **no-arg constructor** that sets default field values.
- One blank line between the last field and the constructor.
- One blank line between the constructor and the first getter.

```java
// CORRECT
public ProductionPlan() {
    this.nodeName = NODENAME;
    this.serviceEntityName = SENAME;
    this.status = STATUS_INITIAL;
    this.category = CATEGORY_SYSTEM;
}
```

### Getters and Setters
- Each getter/setter pair is **always multi-line** — never collapsed to a single line.
- The getter body is on its own line, indented with a tab.
- The setter body is on its own line, indented with a tab.
- **One blank line between every getter/setter method** (including between the getter and its paired setter).
- Getters use `return fieldName;` — NOT `return this.fieldName;` (no `this.` prefix in returns).
- Setters use `this.fieldName = fieldName;`.

```java
// CORRECT
public String getRefMaterialSKUUUID() {
    return refMaterialSKUUUID;
}

public void setRefMaterialSKUUUID(String refMaterialSKUUUID) {
    this.refMaterialSKUUUID = refMaterialSKUUUID;
}

public double getAmount() {
    return amount;
}

public void setAmount(double amount) {
    this.amount = amount;
}

// WRONG — collapsed single-line getters/setters
public String getRefMaterialSKUUUID() { return refMaterialSKUUUID; }
public void setRefMaterialSKUUUID(String v) { this.refMaterialSKUUUID = v; }

// WRONG — no blank line between methods
public String getRefMaterialSKUUUID() {
    return refMaterialSKUUUID;
}
public void setRefMaterialSKUUUID(String refMaterialSKUUUID) {
    this.refMaterialSKUUUID = refMaterialSKUUUID;
}
```

### Long setter parameter wrapping
- If a setter parameter makes the line exceed ~100 characters, wrap the parameter list onto the next line with double-indent (two tabs).

```java
// CORRECT — for long types
public void setFinAccountMaterialItemList(
        List<FinAccountMaterialItemServiceModel> finAccountMaterialItemList) {
    this.finAccountMaterialItemList = finAccountMaterialItemList;
}
```

### Indentation
- **Tabs**, not spaces.
- Class body: one tab.
- Method body: two tabs.

---

## 2. Service / Manager Classes

- Class-level `@Service` annotation.
- Dependencies injected with `@Autowired` on a **separate line above the field**.
- One blank line between each `@Autowired` field.
- Public methods have one blank line between them.
- Constants (METHOD_* strings) declared at the top of the class, each on its own line with a blank line between them.

```java
@Autowired
protected ProductionPlanDAO productionPlanDAO;

@Autowired
protected BillOfMaterialOrderDAO billOfMaterialOrderDAO;
```

---

## 3. Controller Classes

- Class-level annotations: `@Controller` (or `@RestController`), `@RequestMapping`, each on its own line.
- `@Autowired` fields: each dependency on its own block (annotation + field), with one blank line between dependencies.
- Methods start with the `@RequestMapping` annotation on its own line.
- Javadoc `/** ... */` comments above methods that need explanation, blank line after the comment before the annotation.
- Method bodies: try/catch blocks with each `catch` clause on its own line.
- Multi-line method calls: continuation argument aligned under the opening parenthesis or double-indented.

```java
@Autowired
protected FinAccountManager finAccountManager;

@Autowired
protected LogonUserManager logonUserManager;

/**
 * pre-check if the edit object list could be locked
 */
@RequestMapping(value = "/preLock", produces = "text/html;charset=UTF-8")
public @ResponseBody String preLock(String uuid) {
    try {
        ...
    } catch (ServiceEntityConfigureException e) {
        return lockObjectManager.genJSONLockCheckOtherIssue(e.getMessage());
    } catch (LogonInfoException e) {
        return e.generateSimpleErrorJSON();
    }
}
```

---

## 4. Import Ordering

- `java.*` imports first.
- Third-party library imports (org.spring*, net.sf.*) second.
- Internal project imports (`net.thorstein.*`, `platform.*`, `com.company.*`) last.
- One blank line between each group.
- No wildcard imports except within the same package group when many classes are needed.

---

## 5. Class-Level Javadoc

- Classes that are auto-generated or have a specific author use a Javadoc block:

```java
/**
 * Logic Manager CLASS FOR Service Entity [ProductionPlan]
 *
 * @author ZhangHang
 * @date Sun Jan 03 21:00:34 CST 2016
 */
```

- Plain entity/model classes do not require a Javadoc comment unless they have meaningful domain notes.

---

## 6. What NOT to do

- **No Lombok** (`@Data`, `@Getter`, `@Setter`, `@Builder`, etc.) — all getters/setters written manually.
- **No single-line getter/setter methods** — always multi-line.
- **No blank line omissions between fields** — every field separated by one blank line.
- **No blank line omissions between methods** — every method separated by one blank line.
- **No `private` fields** on entity classes — use `protected`.
- **No `this.` prefix in getter return statements** — `return fieldName;` not `return this.fieldName;`.
- **No constructor injection in entities** — entities use no-arg constructors with field defaults.
