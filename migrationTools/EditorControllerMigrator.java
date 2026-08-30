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
            String body = convertActionCodeMatrixToTypeScript(acmBlock, params.rootNodeInstId);
            System.out.println("\tgetActionCodeMatrix() {\n\t\treturn " + body + ";\n\t}");
        } else {
            System.out.println("(not found)");
        }

        Map<String, String> customMethods = extractCustomMethods(legacy);
        System.out.println("\n=== Custom methods (" + customMethods.size() + ") ===");
        for (Map.Entry<String, String> entry : customMethods.entrySet()) {
            System.out.println("\n-- " + entry.getKey() + " --");
            System.out.println(convertCustomMethodToTypeScript(entry.getKey(), entry.getValue(), params.rootNodeInstId));
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
        String acmBlock = extractGetActionCodeMatrixBlock(legacy);
        if (acmBlock != null) {
            String tsAcm = convertActionCodeMatrixToTypeScript(acmBlock, params.rootNodeInstId);
            String patchedAcm = patchGetActionCodeMatrix(patched, tsAcm);
            if (patchedAcm != null) {
                patched = patchedAcm;
                System.out.println("EditorControllerMigrator: patched getActionCodeMatrix in " + targetFile.getFileName());
            } else {
                System.out.println("EditorControllerMigrator: getActionCodeMatrix not found in target, skipping patch.");
            }
            Files.writeString(targetFile, patched, StandardCharsets.UTF_8);
        } else {
            System.out.println("EditorControllerMigrator: getActionCodeMatrix not found in legacy file.");
        }

        // 3. append custom methods
        Map<String, String> customMethods = extractCustomMethods(legacy);
        if (!customMethods.isEmpty()) {
            Map<String, String> converted = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : customMethods.entrySet()) {
                converted.put(entry.getKey(),
                        convertCustomMethodToTypeScript(entry.getKey(), entry.getValue(), params.rootNodeInstId));
            }
            String withCustom = appendCustomMethods(patched, converted);
            if (!withCustom.equals(patched)) {
                patched = withCustom;
                Files.writeString(targetFile, patched, StandardCharsets.UTF_8);
                System.out.println("EditorControllerMigrator: appended custom methods to " + targetFile.getFileName());
            }
        }
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
    //   vm.$refs.multiSelectFactory    →  this.extraDeps.multiSelectFactory
    //   DocumentItemMultiSelectFactory.USE_CASE.X  →  DocumentItemMultiSelectFactory_USE_CASE.X
    //   URL '../moduleName/methodName.html'  →  'moduleName/methodName'
    //   vm.xxx  →  this.xxx

    static String convertActionCodeMatrixToTypeScript(String jsBlock, String rootNodeInstId) {
        String ts = jsBlock;

        // vm.content.*UIModel.uuid → this.getBaseUUID()
        ts = ts.replaceAll("\\bvm\\.content\\.\\w+UIModel\\.uuid\\b", "this.getBaseUUID()");

        // vm.$refs.multiSelectFactory → this.extraDeps.multiSelectFactory
        ts = ts.replace("vm.$refs.multiSelectFactory", "this.extraDeps.multiSelectFactory");

        // DocumentItemMultiSelectFactory.USE_CASE.X → DocumentItemMultiSelectFactory_USE_CASE.X
        ts = ts.replace("DocumentItemMultiSelectFactory.USE_CASE.", "DocumentItemMultiSelectFactory_USE_CASE.");

        // URL: '../moduleName/methodName.html' → 'moduleName/methodName'
        ts = ts.replaceAll("'\\.\\./(\\w+)/(\\w+)\\.html'", "'$1/$2'");

        // Remaining vm.xxx → this.xxx
        ts = ts.replaceAll("\\bvm\\.", "this.");

        return ts;
    }

    // ── patch getActionCodeMatrix() body in the generated EditController ───────

    static String patchGetActionCodeMatrix(String content, String tsBlock) {
        Pattern pattern = Pattern.compile(
                "(getActionCodeMatrix\\(\\)[^{]*\\{[\\s\\n]*return\\s*)\\{.*?\\}(;?\\s*\\})",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) return null;

        String replacement = matcher.group(1) + tsBlock + ";" + matcher.group(2);
        return content.substring(0, matcher.start())
                + replacement
                + content.substring(matcher.end());
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

        // processButtonMeta: remove formatClass wrapper string refs (keep as-is for now)
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

        // minWidth string → keep as-is (already correct for EMBEDLIST columns)

        // Add documentType as first key
        ts = ts.replaceFirst("\\{", "{\n\t\t\tdocumentType: '" + toPascal(rootNodeInstId) + "',");

        return ts;
    }

    // ── patch the generated EditController's getDefaultPageMeta body ──────────

    static String patchGetDefaultPageMeta(String content, String tsBlock, String rootPascal) {
        Pattern pattern = Pattern.compile(
                "(protected getDefaultPageMeta\\(\\)[^{]*\\{[\\s\\n]*return\\s*)\\{.*?\\}(;?\\s*\\})",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) return null;

        String replacement = matcher.group(1) + tsBlock + ";" + matcher.group(2);
        return content.substring(0, matcher.start())
                + replacement
                + content.substring(matcher.end());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /** Methods provided by the base class — never migrate these. */
    static final Set<String> FRAMEWORK_METHODS = new HashSet<>(Arrays.asList(
        "initSubComponentsController", "getServiceManager", "getPrefixURL",
        "getEditPageURL", "getBaseUUID", "getStatus", "setModuleToUI",
        "getDefaultPageMeta", "getActionCodeMatrix",
        "getUploadItemExcelBaseUrl", "getDownloadItemExcelBaseUrl",
        "setI18nCallback", "getResourceId", "created", "mounted",
        "beforeDestroy", "destroyed"
    ));

    /**
     * Extract all non-framework methods from a legacy Vue methods block.
     * Returns ordered map of methodName → raw JS function body (the { ... } block only).
     */
    static Map<String, String> extractCustomMethods(String src) {
        Map<String, String> result = new LinkedHashMap<>();
        // Match top-level Vue methods block entries: exactly 8 spaces indent.
        // Nested callbacks like fnExecutionDone are at 12+ spaces — excluded.
        Pattern methodPat = Pattern.compile(
            "(?m)^        (\\w+):\\s*function\\s*\\([^)]*\\)\\s*\\{");
        Matcher m = methodPat.matcher(src);
        while (m.find()) {
            String name = m.group(1);
            if (FRAMEWORK_METHODS.contains(name)) continue;
            // Extract the brace block starting at the { of the function
            int braceStart = m.end() - 1; // position of the opening {
            String body = extractBraceBlock(src, braceStart);
            if (body != null) {
                result.put(name, body);
            }
        }
        return result;
    }

    /**
     * Convert a single legacy JS method body to a TypeScript arrow method.
     * Applies the same vm/URL/USE_CASE/multiSelectFactory conversions as
     * convertActionCodeMatrixToTypeScript, plus:
     *   - window.location.href = "XxxList.html"  →  this.navigateToList()
     *   - fnExecutionDone: function(...) { ... }.bind(this)  →  fnExecutionDone: (...) => { ... }
     *   - var xxx = ...  →  const xxx = ...
     *   - vm.label.xxx  →  this.getLabel('xxx')
     *   - .bind(this) at end of callbacks → drop
     */
    static String convertCustomMethodToTypeScript(String name, String body, String rootNodeInstId) {
        String ts = body;

        // vm.content.*UIModel.uuid → this.getBaseUUID()
        ts = ts.replaceAll("\\bvm\\.content\\.\\w+UIModel\\.uuid\\b", "this.getBaseUUID()");

        // vm.$refs.multiSelectFactory → this.extraDeps.multiSelectFactory
        ts = ts.replace("vm.$refs.multiSelectFactory", "this.extraDeps.multiSelectFactory");

        // DocumentItemMultiSelectFactory.USE_CASE.X → DocumentItemMultiSelectFactory_USE_CASE.X
        ts = ts.replace("DocumentItemMultiSelectFactory.USE_CASE.", "DocumentItemMultiSelectFactory_USE_CASE.");

        // URL: '../moduleName/methodName.html' → 'moduleName/methodName'
        ts = ts.replaceAll("'\\.\\./(\\w+)/(\\w+)\\.html'", "'$1/$2'");

        // window.location.href = "XxxList.html" → this.navigateToList()
        ts = ts.replaceAll("window\\.location\\.href\\s*=\\s*[\"'][^\"']+[\"'];?", "this.navigateToList();");

        // fnExecutionDone: function(param) { ... }.bind(this) → fnExecutionDone: (param) => { ... }
        ts = ts.replaceAll("(fnExecutionDone:\\s*)function\\s*\\(([^)]*)\\)\\s*(\\{)",
                "$1($2) => $3");
        ts = ts.replaceAll("\\}\\.bind\\(this\\)", "}");
        ts = ts.replaceAll("\\}\\.bind\\(vm\\)", "}");

        // var → const
        ts = ts.replaceAll("(?m)^([ \\t]*)var\\b", "$1const");

        // vm.label.xxx → this.getLabel('xxx')  (no direct equivalent — mark with TODO)
        ts = ts.replaceAll("\\bvm\\.label\\.(\\w+)\\b", "/* TODO: i18n */ this.getLabel('$1')");

        // $.Notification.notify(...) → // TODO: show success toast
        ts = ts.replaceAll("(?m)[ \\t]*\\$\\.Notification\\.notify\\([^;]+\\);?\\n?",
                "\t\t\t// TODO: show success toast\n");

        // ServiceMessageBarHelper.removeMessageBar → drop
        ts = ts.replaceAll("(?m)[ \\t]*ServiceMessageBarHelper\\.removeMessageBar\\([^;]+\\);?\\n?", "");

        // Remaining vm.xxx → this.xxx
        ts = ts.replaceAll("\\bvm\\.", "this.");

        // Strip "var vm = this;" / "const vm = this;" — all vm. refs already replaced
        ts = ts.replaceAll("(?m)^[ \\t]*(?:var|const)\\s+vm\\s*=\\s*this;\\s*\\n", "");

        // Wrap as arrow method:  name = (): void => { <body lines> };
        // Strip outer braces from body, re-indent content
        String inner = ts.substring(1, ts.length() - 1); // strip { }
        // Remove leading/trailing blank lines
        inner = inner.replaceAll("^\\n+", "").replaceAll("\\n+$", "");

        return "\t" + name + " = (): void => {\n"
                + inner + "\n"
                + "\t};";
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
