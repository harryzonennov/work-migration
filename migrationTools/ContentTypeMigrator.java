import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Generates a ***Content.ts file in IntelligentUI/src/types/{groupId}/
 * by reading the root ***ServiceUIModel.java from the backend project and
 * recursively traversing every referenced UIModel / ServiceUIModel class.
 *
 * Usage:
 *   java ContentTypeMigrator
 *   -- or call migrate(rootJavaPath, groupId) from another class --
 *
 * Input:  path to a *ServiceUIModel.java (absolute)
 * Output: IntelligentUI/src/types/{groupId}/{RootName}Content.ts
 */
public class ContentTypeMigrator {

    // ── constants ─────────────────────────────────────────────────────────────

    static final String INTELLIGENT_UI_TYPES =
            "/Users/I043125/work2/IntelligentUI/src/types/";

    static final String BACKEND_JAVA_SRC =
            "/Users/I043125/work2/IntelligentPlatform/src/main/java/";

    /** Default input for standalone dry-run */
    static final String DEFAULT_ROOT_JAVA =
            "/Users/I043125/work2/IntelligentPlatform/src/main/java/"
            + "com/company/IntelligentPlatform/logistics/dto/PurchaseContractServiceUIModel.java";

    static final String DEFAULT_GROUP_ID = "logistics";

    // ── Known base classes → TS platform import paths ─────────────────────────
    // Key = simple Java class name, Value = TS import path relative to types/
    static final Map<String, String> BASE_CLASS_TS_IMPORT = new LinkedHashMap<>();
    static {
        BASE_CLASS_TS_IMPORT.put("DocumentUIModel",       "../platform/DocumentUIModel");
        BASE_CLASS_TS_IMPORT.put("DocMatItemUIModel",     "../platform/DocMatItemUIModel");
        BASE_CLASS_TS_IMPORT.put("DocInvolvePartyUIModel","../platform/DocInvolvePartyUIModel");
        BASE_CLASS_TS_IMPORT.put("DocActionNodeUIModel",  "../platform/DocActionNodeUIModel");
        BASE_CLASS_TS_IMPORT.put("DocAttachmentNodeUIModel","../platform/DocAttachmentNodeUIModel");
        BASE_CLASS_TS_IMPORT.put("SEUIComModel",          "../platform/SeUiComModel");
        BASE_CLASS_TS_IMPORT.put("SeUiComModel",          "../platform/SeUiComModel");
        // ServiceUIModule fields (uuid etc) are on SeUiComModel — treat as SeUiComModel
        BASE_CLASS_TS_IMPORT.put("ServiceUIModule",       "../platform/SeUiComModel");
    }

    // ── TS interface name used for each base class ────────────────────────────
    static final Map<String, String> BASE_CLASS_TS_INTERFACE = new LinkedHashMap<>();
    static {
        BASE_CLASS_TS_INTERFACE.put("DocumentUIModel",        "DocumentUIModel");
        BASE_CLASS_TS_INTERFACE.put("DocMatItemUIModel",      "DocMatItemUIModel");
        BASE_CLASS_TS_INTERFACE.put("DocInvolvePartyUIModel", "DocInvolvePartyUIModel");
        BASE_CLASS_TS_INTERFACE.put("DocActionNodeUIModel",   "DocActionNodeUIModel");
        BASE_CLASS_TS_INTERFACE.put("DocAttachmentNodeUIModel","DocAttachmentNodeUIModel");
        BASE_CLASS_TS_INTERFACE.put("SEUIComModel",           "SeUiComModel");
        BASE_CLASS_TS_INTERFACE.put("SeUiComModel",           "SeUiComModel");
        BASE_CLASS_TS_INTERFACE.put("ServiceUIModule",        "SeUiComModel");
    }

    // ── Model for a parsed Java field ─────────────────────────────────────────

    static class FieldInfo {
        String name;
        String javaType;   // raw Java type, e.g. "String", "int", "PurchaseContractUIModel"
        boolean isList;    // true if declared as List<X>
        String listItemType; // the X in List<X>, if isList

        FieldInfo(String name, String javaType, boolean isList, String listItemType) {
            this.name = name;
            this.javaType = javaType;
            this.isList = isList;
            this.listItemType = listItemType;
        }
    }

    // ── Model for a parsed Java class ─────────────────────────────────────────

    static class ClassInfo {
        String simpleName;
        String superClass;  // simple name of superclass, or null
        List<FieldInfo> fields = new ArrayList<>();
        boolean isServiceUIModule; // extends ServiceUIModule
    }

    // ── Entry points ─────────────────────────────────────────────────────────

    public static void main(String[] args) throws IOException {
        previewMigration(DEFAULT_ROOT_JAVA, DEFAULT_GROUP_ID);
    }

    public static void previewMigration(String rootJavaPath, String groupId) throws IOException {
        String output = generate(rootJavaPath, groupId);
        System.out.println(output);
    }

    public static void migrate(String rootJavaPath, String groupId) throws IOException {
        Path rootFile = Paths.get(rootJavaPath);
        if (!Files.exists(rootFile)) {
            System.out.println("ContentTypeMigrator: root file not found — " + rootJavaPath);
            return;
        }

        String rootClassName = rootFile.getFileName().toString().replace(".java", "");
        // Derive Content file name: PurchaseContractServiceUIModel → PurchaseContractContent
        String contentName = rootClassName.replace("ServiceUIModel", "Content");

        Path targetDir = Paths.get(INTELLIGENT_UI_TYPES, groupId);
        Files.createDirectories(targetDir);
        Path targetFile = targetDir.resolve(contentName + ".ts");

        if (Files.exists(targetFile)) {
            System.out.println("ContentTypeMigrator: target already exists, skipping — " + targetFile);
            return;
        }

        String output = generate(rootJavaPath, groupId);
        Files.writeString(targetFile, output, StandardCharsets.UTF_8);
        System.out.println("ContentTypeMigrator: wrote " + targetFile);
    }

    // ── Core generation ───────────────────────────────────────────────────────

    static String generate(String rootJavaPath, String groupId) throws IOException {
        Path rootFile = Paths.get(rootJavaPath);
        String rootDir = rootFile.getParent().toString();

        // Parse the root class and all dependencies
        Map<String, ClassInfo> parsed = new LinkedHashMap<>();
        parseClassRecursive(rootFile.getFileName().toString().replace(".java", ""),
                rootDir, parsed);

        String rootClassName = rootFile.getFileName().toString().replace(".java", "");
        ClassInfo rootClass = parsed.get(rootClassName);
        if (rootClass == null) {
            return "// ERROR: could not parse " + rootClassName;
        }

        // Collect which base TS imports are needed
        Set<String> neededImports = new LinkedHashSet<>();

        // Build interface definitions in emission order:
        // leaf UIModels first, then ServiceUIModels, then the root
        List<String> interfaceBlocks = new ArrayList<>();

        // Determine emission order: BFS from root, emit leaves first
        List<String> emitOrder = new ArrayList<>();
        Set<String> emitSeen = new LinkedHashSet<>();
        collectEmitOrder(rootClassName, parsed, emitOrder, emitSeen);

        for (String className : emitOrder) {
            ClassInfo info = parsed.get(className);
            if (info == null) continue;
            boolean isRoot = className.equals(rootClassName);
            String block = buildInterfaceBlock(className, info, parsed, neededImports, isRoot);
            if (block != null) {
                interfaceBlocks.add(block);
            }
        }

        // Build import lines
        StringBuilder sb = new StringBuilder();
        for (String importPath : neededImports) {
            // Determine TS interface name from path
            String iface = importPath.substring(importPath.lastIndexOf('/') + 1);
            sb.append("import type { ").append(iface).append(" } from '")
              .append(importPath).append("';\n");
        }
        if (!neededImports.isEmpty()) sb.append("\n");

        for (String block : interfaceBlocks) {
            sb.append(block).append("\n");
        }

        return sb.toString();
    }

    // ── Build one TS interface block ──────────────────────────────────────────

    static String buildInterfaceBlock(String className, ClassInfo info,
            Map<String, ClassInfo> allParsed, Set<String> neededImports,
            boolean isRoot) {

        StringBuilder sb = new StringBuilder();

        // Determine extends clause
        String extendsClause = "";
        String superClass = info.superClass;
        if (superClass != null) {
            if (BASE_CLASS_TS_INTERFACE.containsKey(superClass)
                    && !superClass.equals("ServiceUIModule")) {
                String tsIface = BASE_CLASS_TS_INTERFACE.get(superClass);
                String importPath = BASE_CLASS_TS_IMPORT.get(superClass);
                neededImports.add(importPath);
                extendsClause = " extends " + tsIface;
            } else if (allParsed.containsKey(superClass)) {
                extendsClause = " extends " + superClass;
            }
        }

        List<FieldInfo> fields = info.fields;

        if (info.isServiceUIModule) {
            // ServiceUIModel → plain interface with typed properties
            sb.append("export interface ").append(className).append(" {\n");
            for (FieldInfo f : fields) {
                String tsType = toTsType(f, allParsed, neededImports);
                // ActionNode fields are optional — they're null before any workflow action fires.
                // All other domain fields (main UIModel, party UIModel, item lists) are required.
                boolean isActionNode = !f.isList && isActionNodeType(f.javaType, allParsed);
                String sep = isActionNode ? "?: " : ": ";
                sb.append("\t").append(f.name).append(sep).append(tsType).append(";\n");
            }
            // serviceUIMeta only on the root ServiceUIModel, not on nested item wrappers
            if (isRoot) {
                sb.append("\tserviceUIMeta: Record<string, unknown>;\n");
            }
            sb.append("}");
        } else {
            // Regular UIModel → emit as interface with possible extends
            boolean hasOwnFields = !fields.isEmpty();
            if (!hasOwnFields && !extendsClause.isEmpty()) {
                // Empty body — emit as single-line alias
                sb.append("export interface ").append(className)
                  .append(extendsClause).append(" {}");
            } else {
                sb.append("export interface ").append(className)
                  .append(extendsClause).append(" {\n");
                for (FieldInfo f : fields) {
                    String tsType = toTsType(f, allParsed, neededImports);
                    sb.append("\t").append(f.name).append("?: ").append(tsType).append(";\n");
                }
                sb.append("}");
            }
        }

        return sb.toString();
    }

    // ── Collect emission order (leaves first, root last) ─────────────────────

    static void collectEmitOrder(String className, Map<String, ClassInfo> parsed,
            List<String> order, Set<String> seen) {
        if (seen.contains(className)) return;
        seen.add(className);

        ClassInfo info = parsed.get(className);
        if (info == null) return;

        // Recurse into field types first
        for (FieldInfo f : info.fields) {
            String dep = f.isList ? f.listItemType : f.javaType;
            if (dep != null && parsed.containsKey(dep)) {
                collectEmitOrder(dep, parsed, order, seen);
            }
        }

        order.add(className);
    }

    // ── Java type → TS type string ────────────────────────────────────────────

    static String toTsType(FieldInfo f, Map<String, ClassInfo> allParsed,
            Set<String> neededImports) {
        if (f.isList) {
            String item = f.listItemType;
            if (allParsed.containsKey(item)) {
                return item + "[]";
            }
            return javaScalarToTs(item) + "[]";
        }
        // Check if it's a known domain class
        if (allParsed.containsKey(f.javaType)) {
            return f.javaType;
        }
        // Check if it's a base platform class
        if (BASE_CLASS_TS_INTERFACE.containsKey(f.javaType)) {
            neededImports.add(BASE_CLASS_TS_IMPORT.get(f.javaType));
            return BASE_CLASS_TS_INTERFACE.get(f.javaType);
        }
        return javaScalarToTs(f.javaType);
    }

    static String javaScalarToTs(String javaType) {
        switch (javaType) {
            case "String":   return "string";
            case "int":
            case "long":
            case "float":
            case "double":
            case "Integer":
            case "Long":
            case "Float":
            case "Double":   return "number";
            case "boolean":
            case "Boolean":  return "boolean";
            case "byte[]":   return "unknown[]";
            default:         return "unknown";
        }
    }

    // ── Parse a Java class file into ClassInfo ────────────────────────────────

    static void parseClassRecursive(String className, String dir,
            Map<String, ClassInfo> out) throws IOException {

        if (out.containsKey(className)) return;
        if (BASE_CLASS_TS_IMPORT.containsKey(className)) return;

        // Locate the .java file — search the same dir first, then the whole backend src
        Path file = findJavaFile(className, dir);
        if (file == null) {
            // Can't find it — skip silently
            return;
        }

        String src = Files.readString(file, StandardCharsets.UTF_8);
        ClassInfo info = parseClassSource(className, src);
        out.put(className, info);

        // Recurse into field types
        for (FieldInfo f : info.fields) {
            String dep = f.isList ? f.listItemType : f.javaType;
            if (dep != null && !BASE_CLASS_TS_IMPORT.containsKey(dep)
                    && !isScalar(dep) && !out.containsKey(dep)) {
                parseClassRecursive(dep, dir, out);
            }
        }

        // Recurse into superclass if it's a domain class (not a platform base)
        if (info.superClass != null && !BASE_CLASS_TS_IMPORT.containsKey(info.superClass)
                && !out.containsKey(info.superClass)) {
            parseClassRecursive(info.superClass, dir, out);
        }
    }

    static ClassInfo parseClassSource(String className, String src) {
        ClassInfo info = new ClassInfo();
        info.simpleName = className;

        // Extract superclass from: class Foo extends Bar {
        Pattern classDecl = Pattern.compile(
            "(?:public\\s+)?class\\s+" + Pattern.quote(className)
            + "(?:\\s+extends\\s+(\\w+))?");
        Matcher cm = classDecl.matcher(src);
        if (cm.find()) {
            info.superClass = cm.group(1); // may be null
        }

        info.isServiceUIModule = "ServiceUIModule".equals(info.superClass);

        // Extract declared fields: protected/private Type name;
        // Handle both simple types and List<X>
        Pattern fieldPat = Pattern.compile(
            "(?:protected|private)\\s+"
            + "(?:(List)<([\\w<>]+)>|(\\w+(?:\\[\\])?))"
            + "\\s+(\\w+)\\s*(?:=|;)");
        Matcher fm = fieldPat.matcher(src);
        Set<String> seen = new LinkedHashSet<>();
        while (fm.find()) {
            boolean isList   = fm.group(1) != null;
            String listItem  = fm.group(2);
            String scalarType = fm.group(3);
            String name      = fm.group(4);

            // Skip static fields (they'd have been matched by accident — check context)
            // We match "protected/private" so statics already excluded

            if (!seen.add(name)) continue; // deduplicate

            if (isList) {
                info.fields.add(new FieldInfo(name, "List", true, listItem));
            } else {
                info.fields.add(new FieldInfo(name, scalarType, false, null));
            }
        }

        return info;
    }

    // ── File search ───────────────────────────────────────────────────────────

    static Path findJavaFile(String className, String preferredDir) throws IOException {
        // 1. Same dir
        Path candidate = Paths.get(preferredDir, className + ".java");
        if (Files.exists(candidate)) return candidate;

        // 2. Walk the entire backend src tree
        Path[] found = {null};
        Path srcRoot = Paths.get(BACKEND_JAVA_SRC);
        if (Files.exists(srcRoot)) {
            try {
                Files.walk(srcRoot)
                     .filter(p -> p.getFileName().toString().equals(className + ".java"))
                     .findFirst()
                     .ifPresent(p -> found[0] = p);
            } catch (IOException e) {
                // ignore
            }
        }
        return found[0];
    }

    // Returns true if the Java type (or its superclass chain) is/extends DocActionNodeUIModel
    static boolean isActionNodeType(String javaType, Map<String, ClassInfo> allParsed) {
        if (javaType == null) return false;
        if ("DocActionNodeUIModel".equals(javaType)) return true;
        ClassInfo info = allParsed.get(javaType);
        if (info == null) return false;
        return isActionNodeType(info.superClass, allParsed);
    }

    static boolean isScalar(String type) {
        switch (type) {
            case "String": case "int": case "long": case "float": case "double":
            case "boolean": case "Integer": case "Long": case "Float": case "Double":
            case "Boolean": case "byte[]": case "Object":
                return true;
            default:
                return false;
        }
    }

    static String toPascal(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
