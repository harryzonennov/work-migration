import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Migrates the generated *EditController.tsx and *ItemController.tsx by
 * patching their `getDefaultPageMeta()` body from the legacy Vue JS editor.
 *
 * Invoked by PageModuleCopier after the file copy loop (via CopierParams).
 * Also has a standalone main() for dry-run preview.
 */
public class EditorControllerMigrator {

    static final String INTELLIGENT_UI_PAGES =
            "/Users/I043125/work2/IntelligentUI/src/pages/";

    static final String DEFAULT_LEGACY_PROJECT_URL =
            "/Users/I043125/work/ThorSalesDistributionUI/admin/";

    static final String DEFAULT_LEGACY_EDITOR_URL =
            "js/supplyChain/PurchaseRequestEditor.js";

    // ── standalone dry-run entry point ────────────────────────────────────────

    public static void main(String[] args) throws IOException {
        CopierParams params = new CopierParams(
                "purchaseRequest", "purchaseRequestMaterialItem", "logistics",
                MigrationEntrance.MODEL_CAT_DOCUMENT,
                DEFAULT_LEGACY_PROJECT_URL,
                DEFAULT_LEGACY_EDITOR_URL
        );
        previewMigration(params);
    }

    public static void previewMigration(CopierParams params) throws IOException {
        String legacyEditorUrl = toLegacyEditorUrl(params);
        Path legacyFile = Paths.get(legacyEditorUrl);
        if (!Files.exists(legacyFile)) {
            System.out.println("Legacy editor file not found: " + legacyFile);
            return;
        }
        String legacy = Files.readString(legacyFile, StandardCharsets.UTF_8);

        String block = extractGetDefaultPageMetaBlock(legacy);
        System.out.println("=== " + toPascal(params.rootNodeInstId) + "EditController getDefaultPageMeta ===");
        if (block != null) {
            String body = convertEditorPageMetaToTypeScript(block, params.rootNodeInstId);
            System.out.println("\tprotected getDefaultPageMeta(): PageMeta {\n\t\treturn " + body + ";\n\t}");
        } else {
            System.out.println("(not found)");
        }

        String acmBlock = extractGetActionCodeMatrixBlock(legacy);
        System.out.println("\n=== " + toPascal(params.rootNodeInstId) + "EditController getActionCodeMatrix ===");
        if (acmBlock != null) {
            ConversionResult acmResult = convertActionCodeMatrixToTypeScript(acmBlock, params.rootNodeInstId);
            System.out.println("\tgetActionCodeMatrix() {\n\t\treturn " + reindentBlock(acmResult.ts, "\t\t") + ";\n\t}");
        } else {
            System.out.println("(not found)");
        }

        Map<String, MethodEntry> customMethods = extractCustomMethodEntries(legacy);
        System.out.println("\n=== Custom methods (" + customMethods.size() + ") ===");
        for (Map.Entry<String, MethodEntry> entry : customMethods.entrySet()) {
            System.out.println("\n-- " + entry.getKey() + " --");
            ConversionResult result = convertCustomMethodToTypeScript(entry.getKey(), entry.getValue().params, entry.getValue().body, params.rootNodeInstId);
            System.out.println(result.ts);
        }

        Map<String, String> urlFields = extractDataUrlProperties(legacy);
        System.out.println("\n=== Readonly URL fields (" + urlFields.size() + ") ===");
        for (Map.Entry<String, String> e : urlFields.entrySet()) {
            System.out.println("\treadonly " + e.getKey() + " = " + e.getValue() + ";");
        }
    }

    // ── migrate (applies patches to target files) ─────────────────────────────

    public static void migrate(CopierParams params) throws IOException {
        if (params.legacyUIProjectUrl == null) {
            System.out.println("EditorControllerMigrator: no legacy URL supplied, skipping.");
            return;
        }

        String rootPascal = toPascal(params.rootNodeInstId);
        String legacyEditorUrl = toLegacyEditorUrl(params);
        Path legacyFile = Paths.get(legacyEditorUrl);

        if (!Files.exists(legacyFile)) {
            System.out.println("EditorControllerMigrator: legacy file not found, skipping — " + legacyFile);
            return;
        }

        String legacy = Files.readString(legacyFile, StandardCharsets.UTF_8);
        String block = extractGetDefaultPageMetaBlock(legacy);

        if (block == null) {
            System.out.println("EditorControllerMigrator: getDefaultPageMeta not found in " + legacyFile.getFileName());
            return;
        }

        String tsBlock = convertEditorPageMetaToTypeScript(block, params.rootNodeInstId);
        Path targetFile = Paths.get(INTELLIGENT_UI_PAGES,
                params.groupId, params.rootNodeInstId, rootPascal + "EditController.tsx");

        if (!Files.exists(targetFile)) {
            System.out.println("EditorControllerMigrator: target file not found — " + targetFile);
            return;
        }

        String content = Files.readString(targetFile, StandardCharsets.UTF_8);
        String patched = patchGetDefaultPageMeta(content, tsBlock, rootPascal);
        if (patched == null) {
            System.out.println("EditorControllerMigrator: getDefaultPageMeta not found in " + targetFile.getFileName());
            return;
        }

        Files.writeString(targetFile, patched, StandardCharsets.UTF_8);
        System.out.println("EditorControllerMigrator: patched getDefaultPageMeta in " + targetFile.getFileName());

        // 2. patch getActionCodeMatrix
        List<ImportRegistry.Rule> extraRules = new ArrayList<>();
        String acmBlock = extractGetActionCodeMatrixBlock(legacy);
        if (acmBlock != null) {
            ConversionResult acmResult = convertActionCodeMatrixToTypeScript(acmBlock, params.rootNodeInstId);
            extraRules.addAll(acmResult.imports);
            String patchedAcm = patchGetActionCodeMatrix(patched, acmResult.ts);
            if (patchedAcm != null) {
                patched = patchedAcm;
                System.out.println("EditorControllerMigrator: patched getActionCodeMatrix in " + targetFile.getFileName());
            } else {
                System.out.println("EditorControllerMigrator: getActionCodeMatrix not found in target, skipping patch.");
            }
        } else {
            System.out.println("EditorControllerMigrator: getActionCodeMatrix not found in legacy file.");
        }

        // 3. append custom methods
        Map<String, MethodEntry> customMethods = extractCustomMethodEntries(legacy);
        if (!customMethods.isEmpty()) {
            Map<String, String> converted = new LinkedHashMap<>();
            for (Map.Entry<String, MethodEntry> entry : customMethods.entrySet()) {
                ConversionResult result = convertCustomMethodToTypeScript(
                        entry.getKey(), entry.getValue().params, entry.getValue().body, params.rootNodeInstId);
                converted.put(entry.getKey(), result.ts);
                extraRules.addAll(result.imports);
            }
            String withCustom = appendCustomMethods(patched, converted);
            if (!withCustom.equals(patched)) {
                patched = withCustom;
                System.out.println("EditorControllerMigrator: appended custom methods to " + targetFile.getFileName());
            }
        }

        // 4. inject readonly URL fields from legacy data: block
        Map<String, String> urlFields = extractDataUrlProperties(legacy);
        if (!urlFields.isEmpty()) {
            String withFields = appendReadonlyFields(patched, urlFields);
            if (!withFields.equals(patched)) {
                patched = withFields;
                System.out.println("EditorControllerMigrator: injected readonly URL fields in " + targetFile.getFileName());
            }
        }

        // inject any missing imports — static rules + conditional rules from converters
        String withImportFinal = ImportRegistry.injectAll(patched, extraRules);
        if (!withImportFinal.equals(patched)) {
            patched = withImportFinal;
            System.out.println("EditorControllerMigrator: injected missing imports in " + targetFile.getFileName());
        }
        Files.writeString(targetFile, patched, StandardCharsets.UTF_8);
    }

    // ── derive the legacy editor JS file path from params ────────────────────

    static String toLegacyEditorUrl(CopierParams params) {
        // If a specific legacy editor URL was given (re-using legacyRootNodeListControllerUrl
        // but pointing at the Editor.js), use it; otherwise derive from rootNodeInstId.
        // Convention: the legacy list controller URL has the form js/xxx/YyyList.js
        // We replace List with Editor to get the editor file.
        if (params.legacyRootNodeListControllerUrl != null
                && params.legacyRootNodeListControllerUrl.contains("List.js")) {
            return params.legacyUIProjectUrl
                    + params.legacyRootNodeListControllerUrl.replace("List.js", "Editor.js");
        }
        // Fallback: derive path from rootNodeInstId
        String pascal = toPascal(params.rootNodeInstId);
        return params.legacyUIProjectUrl + "js/supplyChain/" + pascal + "Editor.js";
    }

    // ── derive the legacy item Control JS file path from params ───────────────
    //
    // Item-tier logic lives in {ItemPascal}Control.js (the mixin), not the thin
    // {ItemPascal}Editor.js that just mixes it in. getDefaultPageMeta and custom
    // methods are always in the Control file.
    //
    // Convention: same directory as the root list file.
    //   js/supplyChain/QualityInspectOrderList.js
    //   → js/supplyChain/QualityInspectMatItemControl.js   (item tier)
    //   → js/supplyChain/QualityInspectPropertyItemControl.js  (sub-item tier)

    static String toLegacyItemControlUrl(CopierParams params) {
        if (params.legacyUIProjectUrl == null || params.itemNodeInstId == null) return null;
        String dir = legacyDir(params);
        return params.legacyUIProjectUrl + dir + toPascal(params.itemNodeInstId) + "Control.js";
    }

    static String toLegacySubItemControlUrl(CopierParams params) {
        if (params.legacyUIProjectUrl == null || params.subItemNodeInstId == null) return null;
        String dir = legacyDir(params);
        return params.legacyUIProjectUrl + dir + toPascal(params.subItemNodeInstId) + "Control.js";
    }

    /** Extract the directory prefix from the legacyRootNodeListControllerUrl. */
    static String legacyDir(CopierParams params) {
        if (params.legacyRootNodeListControllerUrl == null) return "js/supplyChain/";
        int lastSlash = params.legacyRootNodeListControllerUrl.lastIndexOf('/');
        return lastSlash >= 0
                ? params.legacyRootNodeListControllerUrl.substring(0, lastSlash + 1)
                : "js/supplyChain/";
    }

    // ── migrate item-tier controller (reads from {Item}Control.js) ───────────
    //
    // Patches getDefaultPageMeta() in the generated {ItemPascal}Controller.tsx
    // from the legacy {ItemPascal}Control.js mixin, then appends custom methods.

    public static void migrateItemController(CopierParams params) throws IOException {
        if (params.legacyUIProjectUrl == null || params.itemNodeInstId == null) {
            System.out.println("EditorControllerMigrator.migrateItemController: skipping (no item or legacyURL).");
            return;
        }

        String itemPascal       = toPascal(params.itemNodeInstId);
        String legacyControlUrl = toLegacyItemControlUrl(params);
        Path   legacyFile       = Paths.get(legacyControlUrl);

        if (!Files.exists(legacyFile)) {
            System.out.println("EditorControllerMigrator.migrateItemController: legacy Control file not found, skipping — " + legacyFile);
            return;
        }

        migrateControllerFile(params, legacyFile, itemPascal + "Controller.tsx",
                params.itemNodeInstId, "migrateItemController");
    }

    // ── migrate sub-item-tier controller (reads from {SubItem}Control.js) ────

    public static void migrateSubItemController(CopierParams params) throws IOException {
        if (params.legacyUIProjectUrl == null || params.subItemNodeInstId == null) {
            System.out.println("EditorControllerMigrator.migrateSubItemController: skipping (no subItem or legacyURL).");
            return;
        }

        String subItemPascal       = toPascal(params.subItemNodeInstId);
        String legacyControlUrl    = toLegacySubItemControlUrl(params);
        Path   legacyFile          = Paths.get(legacyControlUrl);

        if (!Files.exists(legacyFile)) {
            System.out.println("EditorControllerMigrator.migrateSubItemController: legacy Control file not found, skipping — " + legacyFile);
            return;
        }

        migrateControllerFile(params, legacyFile, subItemPascal + "Controller.tsx",
                params.subItemNodeInstId, "migrateSubItemController");
    }

    // ── shared helper: patch a generated controller file from a legacy Control.js ──
    //
    // Steps mirror migrate() for the root controller:
    //   1. Extract + patch getDefaultPageMeta()
    //   2. Append custom methods
    //   3. Inject missing imports

    static void migrateControllerFile(CopierParams params,
                                      Path legacyFile,
                                      String targetFileName,
                                      String nodeInstId,
                                      String callerLabel) throws IOException {

        String rootCamel   = toCamel(params.rootNodeInstId);
        String legacy      = Files.readString(legacyFile, StandardCharsets.UTF_8);
        String block       = extractGetDefaultPageMetaBlock(legacy);

        Path targetFile = Paths.get(INTELLIGENT_UI_PAGES,
                params.groupId, params.rootNodeInstId, targetFileName);

        if (!Files.exists(targetFile)) {
            System.out.println("EditorControllerMigrator." + callerLabel + ": target file not found — " + targetFile);
            return;
        }

        String content = Files.readString(targetFile, StandardCharsets.UTF_8);
        List<ImportRegistry.Rule> extraRules = new ArrayList<>();

        // 1. patch getDefaultPageMeta
        if (block != null) {
            String tsBlock = convertEditorPageMetaToTypeScript(block, nodeInstId);
            String patched = patchGetDefaultPageMeta(content, tsBlock, toPascal(nodeInstId));
            if (patched != null) {
                content = patched;
                System.out.println("EditorControllerMigrator." + callerLabel + ": patched getDefaultPageMeta in " + targetFileName);
            } else {
                System.out.println("EditorControllerMigrator." + callerLabel + ": getDefaultPageMeta not found in " + targetFileName);
            }
        } else {
            System.out.println("EditorControllerMigrator." + callerLabel + ": getDefaultPageMeta not found in " + legacyFile.getFileName());
        }

        // 2. append custom methods
        Map<String, MethodEntry> customMethods = extractCustomMethodEntries(legacy);
        if (!customMethods.isEmpty()) {
            Map<String, String> converted = new LinkedHashMap<>();
            for (Map.Entry<String, MethodEntry> entry : customMethods.entrySet()) {
                ConversionResult result = convertCustomMethodToTypeScript(
                        entry.getKey(), entry.getValue().params, entry.getValue().body, nodeInstId);
                converted.put(entry.getKey(), result.ts);
                extraRules.addAll(result.imports);
            }
            String withCustom = appendCustomMethods(content, converted);
            if (!withCustom.equals(content)) {
                content = withCustom;
                System.out.println("EditorControllerMigrator." + callerLabel + ": appended custom methods to " + targetFileName);
            }
        }

        // 3. inject readonly URL fields from legacy data: block
        Map<String, String> urlFields = extractDataUrlProperties(legacy);
        if (!urlFields.isEmpty()) {
            String withFields = appendReadonlyFields(content, urlFields);
            if (!withFields.equals(content)) {
                content = withFields;
                System.out.println("EditorControllerMigrator." + callerLabel + ": injected readonly URL fields in " + targetFileName);
            }
        }

        // 4. inject missing imports
        String withImports = ImportRegistry.injectAll(content, extraRules);
        if (!withImports.equals(content)) {
            content = withImports;
            System.out.println("EditorControllerMigrator." + callerLabel + ": injected missing imports in " + targetFileName);
        }

        Files.writeString(targetFile, content, StandardCharsets.UTF_8);
    }

    // ── extract the return { ... } block of getDefaultPageMeta ───────────────

    static String extractGetDefaultPageMetaBlock(String src) {
        int fnStart = src.indexOf("getDefaultPageMeta:");
        if (fnStart < 0) return null;
        int returnIdx = src.indexOf("return {", fnStart);
        if (returnIdx < 0) return null;
        int braceOpen = src.indexOf('{', returnIdx);
        if (braceOpen < 0) return null;
        return extractBraceBlock(src, braceOpen);
    }

    // ── extract the return { ... } block of getActionCodeMatrix ──────────────

    static String extractGetActionCodeMatrixBlock(String src) {
        int fnStart = src.indexOf("getActionCodeMatrix:");
        if (fnStart < 0) return null;
        int returnIdx = src.indexOf("return {", fnStart);
        if (returnIdx < 0) return null;
        int braceOpen = src.indexOf('{', returnIdx);
        if (braceOpen < 0) return null;
        return extractBraceBlock(src, braceOpen);
    }

    // ── convert legacy getActionCodeMatrix return block to TypeScript ─────────
    //
    // Rules:
    //   vm.content.{root}UIModel.uuid  →  this.getBaseUUID()
    //   vm.$refs.multiSelectFactory    →  this.deps.multiSelectFactory
    //   DocumentItemMultiSelectFactory.USE_CASE.X  →  DocumentItemMultiSelectFactory_USE_CASE.X
    //   URL '../moduleName/methodName.html'  →  'moduleName/methodName'
    //   vm.label.xxx  →  i18n.t('entityKey:xxx')
    //   vm.xxx  →  this.xxx

    static ConversionResult convertActionCodeMatrixToTypeScript(String jsBlock, String rootNodeInstId) {
        String ts = jsBlock;
        List<ImportRegistry.Rule> rules = new ArrayList<>();

        // vm.content.*UIModel.uuid → this.getBaseUUID()
        ts = ts.replaceAll("\\bvm\\.content\\.\\w+UIModel\\.uuid\\b", "this.getBaseUUID()");

        // vm.content.XxxUIModel.field → this.loadModule()?.XxxUIModel.field
        // (all non-uuid fields on the loaded model, e.g. refWarehouseUUID)
        ts = ts.replaceAll("\\bvm\\.content\\.(\\w+UIModel)\\.(\\w+)\\b", "this.loadModule()?.$1.$2");

        // vm.$refs.multiSelectFactory → this.deps.multiSelectFactory
        ts = ts.replace("vm.$refs.multiSelectFactory", "this.deps.multiSelectFactory");

        // DocumentItemMultiSelectFactory.USE_CASE.X → DocumentItemMultiSelectFactory_USE_CASE.X
        if (ts.contains("DocumentItemMultiSelectFactory.USE_CASE.")) {
            ts = ts.replace("DocumentItemMultiSelectFactory.USE_CASE.", "DocumentItemMultiSelectFactory_USE_CASE.");
            rules.add(ImportRegistry.RULE_USE_CASE);
        }

        // URL: '../moduleName/methodName.html' → 'moduleName/methodName'
        ts = ts.replaceAll("'\\.\\./(\\w+)/(\\w+)\\.html'", "'$1/$2'");

        // vm.label.xxx → i18n.t('entityKey:xxx')
        if (ts.contains("vm.label.")) {
            String entityKey = toCamel(rootNodeInstId);
            ts = ts.replaceAll("\\bvm\\.label\\.(\\w+)\\b", "i18n.t('" + entityKey + ":$1')");
            rules.add(ImportRegistry.RULE_I18N);
        }

        // Remaining vm.xxx → this.xxx
        ts = ts.replaceAll("\\bvm\\.", "this.");

        return new ConversionResult(ts, rules);
    }

    // ── patch getActionCodeMatrix() body in the generated EditController ───────
    //
    // We use a two-pass approach to avoid the \\{.*?\\} non-greedy trap:
    //   1. Regex locates "getActionCodeMatrix() { return " and captures the prefix
    //      up to the opening '{' of the return object, plus the method's closing '}'.
    //   2. extractBraceBlock counts brace depth from the opening '{' to find the
    //      exact span of the return object, however deeply nested it is.
    //   3. We replace only that span, leaving everything else intact.

    static String patchGetActionCodeMatrix(String content, String tsBlock) {
        // Locate "getActionCodeMatrix() { return {"
        Pattern pattern = Pattern.compile(
                "getActionCodeMatrix\\(\\)[^{]*\\{[\\s\\n]*return\\s*(\\{)",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) return null;

        int returnBraceStart = matcher.start(1);
        String returnBlock = extractBraceBlock(content, returnBraceStart);
        if (returnBlock == null) return null;
        int returnBraceEnd = returnBraceStart + returnBlock.length(); // exclusive

        // Everything after the return block up to and including the method's closing '}'
        // (skip optional ';' and whitespace between return-block end and method closing brace)
        String tail = content.substring(returnBraceEnd);
        // tail starts with optional ';' then whitespace then the method's '}'
        // Keep it as-is so we preserve the exact ';' and newline before the method '}'

        return content.substring(0, returnBraceStart)
                + reindentBlock(tsBlock, "\t\t")
                + content.substring(returnBraceEnd);
    }

    /**
     * Strip the common leading whitespace from a brace-delimited block and
     * re-indent it so the opening/closing braces are at {@code baseIndent}
     * and inner lines are at {@code baseIndent + "\t"}.
     */
    static String reindentBlock(String block, String baseIndent) {
        // Split while keeping the surrounding braces on their own virtual lines.
        // The block starts with '{' and ends with '}'; inner content may have
        // leading spaces from the original source.
        String[] lines = block.split("\n", -1);
        // Find the minimum indentation of non-empty inner lines (skip first/last brace lines).
        int minIndent = Integer.MAX_VALUE;
        for (int i = 1; i < lines.length - 1; i++) {
            String line = lines[i];
            if (line.trim().isEmpty()) continue;
            int spaces = 0;
            while (spaces < line.length() && line.charAt(spaces) == ' ') spaces++;
            if (spaces < minIndent) minIndent = spaces;
        }
        if (minIndent == Integer.MAX_VALUE) minIndent = 0;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (i == 0) {
                // opening brace — keep as-is (caller places it inline after "return ")
                sb.append(line.trim());
            } else if (i == lines.length - 1) {
                // closing brace
                sb.append('\n').append(baseIndent).append(line.trim());
            } else {
                // inner line — strip common indent, re-apply baseIndent + one tab
                String stripped = line.length() >= minIndent ? line.substring(minIndent) : line.trim();
                sb.append('\n').append(baseIndent).append('\t').append(stripped);
            }
        }
        return sb.toString();
    }

    // ── auto-import injection ─────────────────────────────────────────────────
    // Delegates to ImportRegistry — add new imports there, not here.

    static String injectImportsIfNeeded(String content) {
        return ImportRegistry.injectAll(content);
    }

    // ── conversion result (ts text + import rules the conversion requires) ────

    static class ConversionResult {
        final String ts;
        final List<ImportRegistry.Rule> imports;
        ConversionResult(String ts, List<ImportRegistry.Rule> imports) {
            this.ts = ts;
            this.imports = imports;
        }
        static ConversionResult of(String ts, ImportRegistry.Rule... rules) {
            return new ConversionResult(ts, Arrays.asList(rules));
        }
        static ConversionResult of(String ts) {
            return new ConversionResult(ts, Collections.emptyList());
        }
    }

    static String extractBraceBlock(String src, int start) {
        int depth = 0;
        for (int i = start; i < src.length(); i++) {
            char c = src.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return src.substring(start, i + 1);
            }
        }
        return null;
    }

    // Convert all popupConfigure: { ... } blocks to docPopConfig: { ... }.
    // Drops: popupLabelClass, documentFunction, popupTitle, popupTitlePath, headerTitle, headerTitlePath.
    // Renames: documentTypePath → docTypeFieldName, targetUidPath → uuidFieldName.
    // Keeps: documentType (static variant).
    static String convertPopupConfigure(String src) {
        StringBuilder result = new StringBuilder();
        int searchFrom = 0;
        while (true) {
            int keyStart = src.indexOf("popupConfigure:", searchFrom);
            if (keyStart < 0) {
                result.append(src, searchFrom, src.length());
                break;
            }
            // Skip if inside a component file (infrastructure) — only convert fieldMeta usage.
            // We detect fieldMeta context by checking that we're inside a getDefaultPageMeta body;
            // simpler: just always convert — infrastructure files don't run through this method.
            result.append(src, searchFrom, keyStart);

            int braceOpen = src.indexOf('{', keyStart);
            if (braceOpen < 0) {
                result.append(src, keyStart, src.length());
                break;
            }
            String blockContent = ListControllerMigrator.extractBraceBlock(src, braceOpen);
            if (blockContent == null) {
                result.append(src, keyStart, src.length());
                break;
            }

            String inner = blockContent.substring(1, blockContent.length() - 1); // strip { }

            // Drop legacy-only keys
            inner = inner.replaceAll("(?m)[ \\t]*popupLabelClass:\\s*[^,\\n]+,?\\n", "");
            inner = inner.replaceAll("(?m)[ \\t]*documentFunction:\\s*[^,\\n]+,?\\n", "");
            inner = inner.replaceAll("(?m)[ \\t]*popupTitle:\\s*[^,\\n]+,?\\n", "");
            inner = inner.replaceAll("(?m)[ \\t]*popupTitlePath:\\s*[^,\\n]+,?\\n", "");
            inner = inner.replaceAll("(?m)[ \\t]*headerTitle:\\s*[^,\\n]+,?\\n", "");
            inner = inner.replaceAll("(?m)[ \\t]*headerTitlePath:\\s*[^,\\n]+,?\\n", "");

            // Rename keys
            inner = inner.replaceAll("documentTypePath:", "docTypeFieldName:");
            inner = inner.replaceAll("targetUidPath:", "uuidFieldName:");

            result.append("docPopConfig: {").append(inner).append("}");
            searchFrom = braceOpen + blockContent.length();
        }
        return result.toString();
    }

    // ── convert legacy editor getDefaultPageMeta object to TypeScript ─────────
    //
    // Rules:
    //   Drop top-level: pageId, pageCategory, labelObject, parentVue, parentContent,
    //                   serviceManager, coreModelId, i18nPath, helpDocumentName,
    //                   getDocActionNodeListURL
    //   Keep: placeholder block in processButtonMeta (DOC_ACTION_BTN placeholder)
    //   Keep: disabled on fieldMetaList entries

    static String convertEditorPageMetaToTypeScript(String jsBlock, String rootNodeInstId) {
        String entityKey = toCamel(rootNodeInstId);
        String ts = jsBlock;

        // vm references
        ts = ts.replaceAll("\\bvm\\.([a-zA-Z])", "this.$1");
        ts = ts.replaceAll("\\bvm:\\s*vm\\b", "vm: this");
        // remaining bare `vm,` references (e.g. in extendDocSearchTabFieldMeta)
        ts = ts.replaceAll("\\bvm\\b", "this");

        // disableNotInInit: the framework resolves this as a string key — never call it as a method
        // this.disableNotInInit()  →  'disableNotInInit'
        // this.disableNotInInit    →  'disableNotInInit'  (when used as a value, e.g. disableFlag:)
        ts = ts.replaceAll("this\\.disableNotInInit\\(\\)", "'disableNotInInit'");
        ts = ts.replaceAll("(disableFlag:\\s*)this\\.disableNotInInit\\b", "$1'disableNotInInit'");

        // helpConfigureListWarehouse: a Vue data property — inline its literal value
        ts = ts.replaceAll("this\\.helpConfigureListWarehouse\\b",
                "[{ helpKey: 'refWarehouseId', fieldKey: 'refWarehouseId' }]");

        // RefControl.Ids.xxx → RefControlIds.xxx  (import injected by ImportRegistry static rule)
        ts = ts.replace("RefControl.Ids.", "RefControlIds.");

        // Drop top-level metadata keys
        ts = dropKey(ts, "pageId");
        ts = dropKey(ts, "pageCategory");
        ts = dropKey(ts, "labelObject");
        ts = dropKey(ts, "parentVue");
        ts = dropKey(ts, "parentContent");
        ts = dropKey(ts, "serviceManager");
        ts = dropKey(ts, "coreModelId");
        ts = dropKey(ts, "i18nPath");
        ts = dropKey(ts, "getDocActionNodeListURL");
        ts = ts.replaceAll("(?s)[ \\t]*helpDocumentName:\\s*\\[[^\\]]*\\],?\\n", "");

        // AsyncSection.sectionCategory → SectionCategory
        ts = ts.replace("AsyncSection.sectionCategory.", "SectionCategory.");

        // fieldType conversions
        ts = ts.replace("AbsInput.FIELDTYPE.Select2", "'select'");
        ts = ts.replace("AbsInput.FIELDTYPE.TextArea", "'textarea'");
        ts = ts.replaceAll("fieldType:\\s*AbsInput\\.FIELDTYPE\\.(\\w+)",
                "fieldType: '$1'");
        // datetime: true → fieldType: 'date'
        ts = ts.replaceAll("(?m)[ \\t]*datetime:\\s*true,?\\n",
                "\t\t\t\t\tfieldType: 'date',\n");

        // processButtonMeta: callback/formatClass are string keys in the new framework
        // this.saveModule → 'saveModule', this.exitModule → 'exitModule', this.displayForEdit → 'displayForEdit'
        ts = ts.replaceAll("(callback:\\s*)this\\.(saveModule|exitModule|cancelModule)\\b", "$1'$2'");
        ts = ts.replaceAll("(formatClass:\\s*)this\\.(displayForEdit|displayForCreate|displayForView)\\b", "$1'$2'");
        // Tab-level: rename titleLabelKey → tabTitle with i18n prefix, titleIcon → tabIcon
        ts = ts.replaceAll("titleLabelKey:\\s*'([^']+)'",
                "tabTitle: '" + entityKey + ":" + entityKey + ".$1'");
        ts = ts.replaceAll("titleIcon:\\s*'([^']+)'",
                "tabIcon: '$1'");

        // Drop section-level noise keys
        ts = dropKey(ts, "sectionId");
        ts = dropKey(ts, "titleHelpKey");
        ts = dropKey(ts, "updatedByUidPath");
        ts = dropKey(ts, "updatedByNamePath");
        ts = dropKey(ts, "updatedDatePath");

        ts = dropKey(ts, "labelPath");
        ts = dropKey(ts, "prefixLabel");
        ts = dropKey(ts, "editBlock");
        ts = dropKey(ts, "scrollX");
        ts = dropKey(ts, "label");
        ts = dropKey(ts, "titleLabelKey");  // any remaining after tab rename
        ts = ts.replaceAll("(?s)[ \\t]*helpConfigureList:\\s*\\[[^\\]]*\\],?\\n", "");

        // this.label.xxx → i18n.t('<entityKey>:<xxx>')
        ts = ts.replaceAll("this\\.label\\.(\\w+)", "i18n.t('" + entityKey + ":$1')");

        // priceConfig: drop legacy auth fields, strip xxxUIModel. prefix from field paths
        // actionCode: this.author.actionCode.PriceInfo, → drop
        ts = ts.replaceAll("(?m)[ \\t]*actionCode:\\s*this\\.author\\.actionCode\\.\\w+,?\\n", "");
        // actionCodePromise: this.initAuthorResourceCheck(), → drop
        ts = ts.replaceAll("(?m)[ \\t]*actionCodePromise:\\s*[^,\\n]+,?\\n", "");
        // priceField: 'xxxUIModel.fieldName' → priceField: 'fieldName'
        ts = ts.replaceAll("(priceField:\\s*')(\\w+UIModel\\.)(\\w+')", "$1$3");
        // priceDisplayField: 'xxxUIModel.fieldName' → priceDisplayField: 'fieldName'
        ts = ts.replaceAll("(priceDisplayField:\\s*')(\\w+UIModel\\.)(\\w+')", "$1$3");

        // fieldMetaList in EMBEDLIST sections: strip ****UIModel. prefix from fieldName, fieldKey, uuidFieldName
        // 'qualityInspectMatItemUIModel.fieldName' → 'fieldName'
        ts = ts.replaceAll("((?:fieldName|fieldKey|uuidFieldName):\\s*')(\\w+UIModel\\.)(\\w+')", "$1$3");

        // EMBEDLIST sections: inject listSubPath before fieldMetaList if not already present.
        // Legacy JS stores field paths under item UIModel keys — the new framework needs
        // listSubPath: XxxManager.getItemNodeSubPath() to resolve the correct sub-path.
        // Strategy: match a run from `{` up to `fieldMetaList:` that contains no `listSubPath`,
        // then prepend listSubPath on its own line before fieldMetaList.
        String managerName = toPascal(rootNodeInstId) + "Manager";
        ts = injectListSubPath(ts, managerName);

        // minWidth string → keep as-is (already correct for EMBEDLIST columns)

        // popupConfigure → docPopConfig
        ts = convertPopupConfigure(ts);

        // callbackTemplate function bodies: cast "${fieldName}" placeholders to unknown
        // when compared to numbers, to suppress TS "string compared to number" warnings.
        // The framework rewrites these via func.toString() + regex before eval — they are
        // never executed as written.
        // Pattern 1: "${x}" op N  →  ("${x}" as unknown as number) op N
        ts = ts.replaceAll("\"\\$\\{([^}]+)\\}\"(\\s*[=!<>]{1,3}\\s*[0-9])", "(\"\\${$1}\" as unknown as number)$2");
        ts = ts.replaceAll("([0-9]\\s*[=!<>]{1,3}\\s*)\"\\$\\{([^}]+)\\}\"", "$1(\"\\${$2}\" as unknown as number)");
        // Pattern 2: "${x}" * 1  →  ("${x}" as unknown as number) * 1
        ts = ts.replaceAll("\"\\$\\{([^}]+)\\}\"(\\s*\\*\\s*1)", "(\"\\${$1}\" as unknown as number)$2");
        // Pattern 3: typed callbackTemplate function parameter: function(content) → function(content: unknown)
        ts = ts.replaceAll("callbackTemplate\\s*:\\s*function\\s*\\((\\w+)\\)", "callbackTemplate: function ($1: unknown)");

        // Add documentType as first key
        ts = ts.replaceFirst("\\{", "{\n\t\t\tdocumentType: '" + toPascal(rootNodeInstId) + "',");

        return ts;
    }

    // ── inject listSubPath before fieldMetaList in EMBEDLIST sections ─────────
    //
    // Scans the TS block for occurrences of `fieldMetaList:` that are inside an
    // EMBEDLIST section (contains "EMBEDLIST") and lack a `listSubPath:`.
    // Inserts `listSubPath: XxxManager.getItemNodeSubPath(),` before `fieldMetaList:`.

    static String injectListSubPath(String ts, String managerName) {
        String marker = "fieldMetaList:";
        int searchFrom = 0;
        StringBuilder result = new StringBuilder();
        while (true) {
            int idx = ts.indexOf(marker, searchFrom);
            if (idx < 0) {
                result.append(ts.substring(searchFrom));
                break;
            }
            // Find the enclosing section object: scan back past nested objects to find
            // the `{` that opens the sectionMeta containing this fieldMetaList.
            // We need the section-level `{` — walk backward tracking brace depth.
            int sectionStart = -1;
            int depth = 0;
            for (int i = idx - 1; i >= 0; i--) {
                char c = ts.charAt(i);
                if (c == '}') depth++;
                else if (c == '{') {
                    if (depth == 0) { sectionStart = i; break; }
                    depth--;
                }
            }
            String section = sectionStart >= 0 ? ts.substring(sectionStart, idx) : "";
            if (!section.contains("EMBEDLIST")) {
                // Not an EMBEDLIST section — leave fieldMetaList unchanged
                result.append(ts, searchFrom, idx + marker.length());
                searchFrom = idx + marker.length();
                continue;
            }
            if (section.contains("listSubPath")) {
                // Already has listSubPath — skip
                result.append(ts, searchFrom, idx + marker.length());
                searchFrom = idx + marker.length();
                continue;
            }
            // Determine indentation of the fieldMetaList: line
            int lineStart = ts.lastIndexOf('\n', idx);
            String indent = "";
            if (lineStart >= 0) {
                int contentStart = lineStart + 1;
                int k = contentStart;
                while (k < idx && (ts.charAt(k) == ' ' || ts.charAt(k) == '\t')) k++;
                indent = ts.substring(contentStart, k);
            }
            result.append(ts, searchFrom, idx);
            result.append("listSubPath: ").append(managerName).append(".getItemNodeSubPath(),\n");
            result.append(indent);
            result.append(marker);
            searchFrom = idx + marker.length();
        }
        return result.toString();
    }

    // ── patch the generated EditController's getDefaultPageMeta body ──────────
    //
    // Same two-pass approach as patchGetActionCodeMatrix: regex finds "return {",
    // extractBraceBlock finds the exact closing brace by depth counting.

    static String patchGetDefaultPageMeta(String content, String tsBlock, String rootPascal) {
        Pattern pattern = Pattern.compile(
                "protected getDefaultPageMeta\\(\\)[^{]*\\{[\\s\\n]*return\\s*(\\{)",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) return null;

        int returnBraceStart = matcher.start(1);
        String returnBlock = extractBraceBlock(content, returnBraceStart);
        if (returnBlock == null) return null;
        int returnBraceEnd = returnBraceStart + returnBlock.length();

        return content.substring(0, returnBraceStart)
                + tsBlock
                + content.substring(returnBraceEnd);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * URL properties in the legacy data: block that are already handled by the
     * base class (DocumentEditController / ServiceEditController) and must not
     * be re-emitted as readonly fields.
     */
    static final Set<String> FRAMEWORK_URL_KEYS = new HashSet<>(Arrays.asList(
        "getActionCodeMapURL", "getDocActionConfigureListURL", "executeDocActionURL",
        "exitURL", "exitModuleURL"
    ));

    /**
     * Extract custom URL readonly fields from the legacy Vue data: { ... } block.
     *
     * Scans for entries of the form:
     *   somethingURL: '../moduleName/methodName.html',
     * that are NOT in FRAMEWORK_URL_KEYS and NOT already declared in the target file.
     *
     * Returns an ordered map of  fieldName → converted URL string value,
     * e.g.  "getCheckStatusURL" → "'qualityInspectOrder/getCheckStatus'"
     */
    static Map<String, String> extractDataUrlProperties(String src) {
        Map<String, String> result = new LinkedHashMap<>();
        // Match entries like:  getCheckStatusURL: '../qualityInspectOrder/getCheckStatus.html',
        Pattern urlPat = Pattern.compile(
            "(?m)^[ \\t]+(\\w+URL):\\s*'\\.\\./(\\w+)/(\\w+)\\.html'");
        Matcher m = urlPat.matcher(src);
        while (m.find()) {
            String fieldName  = m.group(1);
            String moduleName = m.group(2);
            String methodName = m.group(3);
            if (FRAMEWORK_URL_KEYS.contains(fieldName)) continue;
            result.put(fieldName, "'" + moduleName + "/" + methodName + "'");
        }
        return result;
    }

    /**
     * Inject readonly field declarations into the target controller file.
     * Each entry becomes:  \treadonly fieldName = value;\n
     *
     * Inserts after the first line that matches the class declaration
     * (the line containing "extends ...Controller"), or after the opening
     * brace of the class if no extends is found.
     * Skips fields already declared in the target (idempotent).
     */
    static String appendReadonlyFields(String content, Map<String, String> fields) {
        if (fields.isEmpty()) return content;

        StringBuilder toInsert = new StringBuilder();
        for (Map.Entry<String, String> e : fields.entrySet()) {
            String name  = e.getKey();
            String value = e.getValue();
            // Skip if already declared (readonly or otherwise)
            if (content.contains("readonly " + name) || content.contains(name + " =")) continue;
            toInsert.append("\treadonly ").append(name).append(" = ").append(value).append(";\n");
        }
        if (toInsert.length() == 0) return content;

        // Insert after the class-opening line (line containing "export class" or "class ")
        Pattern classLine = Pattern.compile("(?m)^export class [^{]+\\{");
        Matcher m = classLine.matcher(content);
        if (!m.find()) return content;  // fallback: don't modify
        int insertAt = m.end();
        // Skip the newline immediately after '{'
        if (insertAt < content.length() && content.charAt(insertAt) == '\n') insertAt++;

        return content.substring(0, insertAt) + "\n" + toInsert + content.substring(insertAt);
    }

    /** Methods provided by the base class — never migrate these. */
    static final Set<String> FRAMEWORK_METHODS = new HashSet<>(Arrays.asList(
        "initSubComponentsController", "getServiceManager", "getPrefixURL",
        "getEditPageURL", "getBaseUUID", "getStatus", "setModuleToUI",
        "getDefaultPageMeta", "getActionCodeMatrix",
        "getUploadItemExcelBaseUrl", "getDownloadItemExcelBaseUrl",
        "setI18nCallback", "getResourceId", "created", "mounted",
        "beforeDestroy", "destroyed",
        // controller-level getDocumentType() is dead code in new UI
        "getDocumentType",
        // Vue lifecycle / component hooks — never emit as class members
        "data", "watch", "computed", "filters", "components", "mixins"
    ));

    /**
     * Extract all non-framework methods from a legacy Vue methods block.
     * Returns ordered map of methodName → raw JS function body (the { ... } block only).
     */
    /** Holds method name → [paramList, bodyBlock] for extracted custom methods. */
    static class MethodEntry {
        final String params;
        final String body;
        MethodEntry(String params, String body) { this.params = params; this.body = body; }
    }

    static Map<String, MethodEntry> extractCustomMethodEntries(String src) {
        Map<String, MethodEntry> result = new LinkedHashMap<>();
        // Top-level (8-space indent)
        Pattern methodPat = Pattern.compile(
            "(?m)^        (\\w+):\\s*function\\s*\\(([^)]*)\\)\\s*\\{");
        Matcher m = methodPat.matcher(src);
        while (m.find()) {
            String name = m.group(1);
            String params = m.group(2).trim();
            if (FRAMEWORK_METHODS.contains(name)) continue;
            int braceStart = m.end() - 1;
            String body = extractBraceBlock(src, braceStart);
            if (body != null) result.put(name, new MethodEntry(params, body));
        }
        // 12-space-indented fn*** callbacks
        Pattern fnCallbackPat = Pattern.compile(
            "(?m)^            (fn\\w+):\\s*function\\s*\\(([^)]*)\\)\\s*\\{");
        Matcher fnM = fnCallbackPat.matcher(src);
        while (fnM.find()) {
            String name = fnM.group(1);
            String params = fnM.group(2).trim();
            if (FRAMEWORK_METHODS.contains(name) || result.containsKey(name)) continue;
            int braceStart = fnM.end() - 1;
            String body = extractBraceBlock(src, braceStart);
            if (body != null) result.put(name, new MethodEntry(params, body));
        }
        return result;
    }

    static Map<String, String> extractCustomMethods(String src) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, MethodEntry> e : extractCustomMethodEntries(src).entrySet()) {
            result.put(e.getKey(), e.getValue().body);
        }
        return result;
    }

    /**
     * Convert a single legacy JS method body to a TypeScript arrow method.
     * Applies the same vm/URL/USE_CASE/multiSelectFactory conversions as
     * convertActionCodeMatrixToTypeScript, plus:
     *   - window.location.href = "XxxList.html"  →  this.deps.actionRef.current?.reload()
     *   - fnExecutionDone: function(...) { ... }.bind(this)  →  fnExecutionDone: (...) => { ... }
     *   - var xxx = ...  →  const xxx = ...
     *   - vm.label.xxx  →  i18n.t('entityKey:xxx')
     *   - .bind(this) at end of callbacks → drop
     * @param params  comma-separated JS parameter names (may be empty)
     */
    static ConversionResult convertCustomMethodToTypeScript(String name, String params, String body, String rootNodeInstId) {
        String ts = body;
        List<ImportRegistry.Rule> rules = new ArrayList<>();

        // vm.$set(vm.content.XxxUIModel, 'field', value) → two-line assignment
        // This pattern is used by fnSetWarehouse/fnSetWarehouseArea callbacks
        ts = ts.replaceAll(
            "\\bvm\\.\\$set\\(vm\\.content\\.(\\w+UIModel),\\s*'(\\w+)',\\s*([^)]+)\\);?",
            "const _m = this.loadModule();\n                if (_m) _m.$1.$2 = $3;");

        // vm.content.*UIModel.uuid → this.getBaseUUID()
        ts = ts.replaceAll("\\bvm\\.content\\.\\w+UIModel\\.uuid\\b", "this.getBaseUUID()");

        // vm.content.XxxUIModel.field → this.loadModule()?.XxxUIModel.field
        ts = ts.replaceAll("\\bvm\\.content\\.(\\w+UIModel)\\.(\\w+)\\b", "this.loadModule()?.$1.$2");

        // vm.$refs.multiSelectFactory → this.deps.multiSelectFactory
        ts = ts.replace("vm.$refs.multiSelectFactory", "this.deps.multiSelectFactory");

        // DocumentItemMultiSelectFactory.USE_CASE.X → DocumentItemMultiSelectFactory_USE_CASE.X
        if (ts.contains("DocumentItemMultiSelectFactory.USE_CASE.")) {
            ts = ts.replace("DocumentItemMultiSelectFactory.USE_CASE.", "DocumentItemMultiSelectFactory_USE_CASE.");
            rules.add(ImportRegistry.RULE_USE_CASE);
        }

        // URL: '../moduleName/methodName.html' → 'moduleName/methodName'
        ts = ts.replaceAll("'\\.\\./(\\w+)/(\\w+)\\.html'", "'$1/$2'");

        // window.location.href = "XxxList.html" → this.deps.actionRef.current?.reload()
        ts = ts.replaceAll("window\\.location\\.href\\s*=\\s*[\"'][^\"']+[\"'];?", "this.deps.actionRef.current?.reload();");

        // this.navigateToList() → this.deps.actionRef.current?.reload()
        ts = ts.replaceAll("\\bthis\\.navigateToList\\(\\);?", "this.deps.actionRef.current?.reload();");

        // this.deps.multiSelectFactory.initBatchSelection → this.deps.multiSelectFactory?.initBatchSelection
        // multiSelectFactory is optional in deps — always use optional chaining
        ts = ts.replace("this.deps.multiSelectFactory.initBatchSelection",
                "this.deps.multiSelectFactory?.initBatchSelection");

        // fnExecutionDone: function(param) { ... }.bind(this) → fnExecutionDone: (_param: unknown) => { ... }
        // Parameter is renamed to _param: unknown to satisfy TS (unused parameter, but typed)
        ts = ts.replaceAll("(fnExecutionDone:\\s*)function\\s*\\(([^)]*)\\)\\s*(\\{)",
                "$1(_$2: unknown) => $3");
        ts = ts.replaceAll("\\}\\.bind\\(this\\)", "}");
        ts = ts.replaceAll("\\}\\.bind\\(vm\\)", "}");

        // var → const
        ts = ts.replaceAll("(?m)^([ \\t]*)var\\b", "$1const");

        // vm.label.xxx → i18n.t('entityKey:xxx')
        if (ts.contains("vm.label.")) {
            String entityKey = toCamel(rootNodeInstId);
            ts = ts.replaceAll("\\bvm\\.label\\.(\\w+)\\b", "i18n.t('" + entityKey + ":$1')");
            rules.add(ImportRegistry.RULE_I18N);
        }

        // WarehouseStoreManager.initItemSearchModel → WarehouseStoreManager.genInitSearchModel()
        // initItemSearchModel is a shared readonly object; callers that mutate it must use genInitSearchModel()
        if (ts.contains("WarehouseStoreManager.initItemSearchModel")) {
            ts = ts.replace("WarehouseStoreManager.initItemSearchModel",
                    "WarehouseStoreManager.genInitSearchModel()");
            rules.add(ImportRegistry.RULE_WAREHOUSE_STORE_MANAGER);
        }

        // $.Notification.notify(...) → // TODO: show success toast
        ts = ts.replaceAll("(?m)[ \\t]*\\$\\.Notification\\.notify\\([^;]+\\);?\\n?",
                "\t\t\t// TODO: show success toast\n");

        // ServiceMessageBarHelper.removeMessageBar → drop
        ts = ts.replaceAll("(?m)[ \\t]*ServiceMessageBarHelper\\.removeMessageBar\\([^;]+\\);?\\n?", "");

        // Remaining vm.xxx → this.xxx
        ts = ts.replaceAll("\\bvm\\.", "this.");

        // Strip "var vm = this;" / "const vm = this;" — all vm. refs already replaced
        ts = ts.replaceAll("(?m)^[ \\t]*(?:var|const)\\s+vm\\s*=\\s*this;\\s*\\n", "");

        // Wrap as arrow method:  name = (param: string): void => { <body lines> };
        String inner = ts.substring(1, ts.length() - 1);
        inner = inner.replaceAll("^\\n+", "").replaceAll("\\n+$", "");

        // Build typed parameter list: each JS param → "param: string" (string is a safe default)
        String typedParams = "";
        if (params != null && !params.isEmpty()) {
            StringBuilder paramSb = new StringBuilder();
            for (String p : params.split(",")) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) {
                    if (paramSb.length() > 0) paramSb.append(", ");
                    paramSb.append(trimmed).append(": string");
                }
            }
            typedParams = paramSb.toString();
        }

        String methodTs = "\t" + name + " = (" + typedParams + "): void => {\n" + inner + "\n\t};";
        return new ConversionResult(methodTs, rules);
    }

    /**
     * Append converted custom methods before the closing `}` of the class in the target file.
     * Skips any method whose name already appears in the target (idempotent).
     */
    static String appendCustomMethods(String content, Map<String, String> converted) {
        if (converted.isEmpty()) return content;

        StringBuilder toAppend = new StringBuilder();
        for (Map.Entry<String, String> entry : converted.entrySet()) {
            String name = entry.getKey();
            // Skip if method already defined in target
            if (content.contains(name + " =") || content.contains(name + "()")) continue;
            toAppend.append("\n").append(entry.getValue()).append("\n");
        }

        if (toAppend.length() == 0) return content;

        // Insert before the last `}` that closes the class
        int lastBrace = content.lastIndexOf('}');
        if (lastBrace < 0) return content;
        return content.substring(0, lastBrace)
                + toAppend
                + content.substring(lastBrace);
    }

    static String dropKey(String src, String key) {
        return src.replaceAll("(?m)[ \\t]*" + key + ":\\s*[^,\\n{\\[]+,?\\n", "");
    }

    static String toPascal(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static String toCamel(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
