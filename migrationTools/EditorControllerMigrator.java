import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
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
            System.out.println(convertEditorPageMetaToTypeScript(block, params.rootNodeInstId));
        } else {
            System.out.println("(not found)");
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
    //   Add:  documentType: '{RootPascal}'
    //   vm.xxx → this.xxx   |   vm: vm → vm: this
    //   AsyncSection.sectionCategory.X → SectionCategory.X
    //   AbsInput.FIELDTYPE.Select2   → (handled via fieldType: 'select')
    //   AbsInput.FIELDTYPE.TextArea  → (handled via fieldType: 'textarea')
    //   datetime: true → fieldType: 'date'  (approximation — manual review still needed)
    //   Tab keys: titleLabelKey → tabTitle (prefixed with entity i18n key)
    //             titleIcon     → tabIcon
    //   Section: drop sectionId, titleLabelKey, titleHelpKey, titleIcon,
    //                 updatedByUidPath, updatedByNamePath, updatedDatePath, disabled,
    //                 labelPath, prefixLabel, helpConfigureList, editBlock, pageOnly (kept),
    //                 customerRequired, accountObjectType (kept)
    //   processButtonMeta: drop placeholder category line

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

        // Drop placeholder in processButtonMeta
        ts = ts.replaceAll("(?s)[ \\t]*placeholder:\\s*\\{[^}]*\\},?\\n", "");

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
        ts = dropKey(ts, "disabled");
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
