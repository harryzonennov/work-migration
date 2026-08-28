import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.regex.*;

/**
 * Migrates the generated *ListController.tsx by:
 *  1. Patching `readonly searchContent` from legacy `data.searchContent`
 *  2. Patching `getDefaultPageMeta()` from legacy `getDefaultPageMeta` function
 *
 * Invoked by PageModuleCopier after the file copy loop.
 */
public class ListControllerMigrator {

    static final String INTELLIGENT_UI_PAGES =
            "/Users/I043125/work2/IntelligentUI/src/pages/";

    static final String DEFAULT_LEGACY_PROJECT_URL =
            "/Users/I043125/work/ThorSalesDistributionUI/admin/";

    static final String DEFAULT_LEGACY_LIST_CONTROLLER_URL =
            "js/supplyChain/PurchaseRequestList.js";

    // ── standalone entry point (dry-run: prints converted content, no file writes) ──

    public static void main(String[] args) throws IOException {
        CopierParams params = new CopierParams(
                "purchaseRequest", "purchaseRequestMaterialItem", "logistics",
                MigrationEntrance.MODEL_CAT_DOCUMENT,
                DEFAULT_LEGACY_PROJECT_URL,
                DEFAULT_LEGACY_LIST_CONTROLLER_URL
        );
        previewMigration(params);
    }

    public static void previewMigration(CopierParams params) throws IOException {
        Path legacyFile = Paths.get(params.legacyUIProjectUrl + params.legacyRootNodeListControllerUrl);
        if (!Files.exists(legacyFile)) {
            System.out.println("Legacy file not found: " + legacyFile);
            return;
        }
        String legacy = Files.readString(legacyFile, StandardCharsets.UTF_8);

        String searchBlock = extractSearchContentBlock(legacy);
        System.out.println("=== searchContent ===");
        if (searchBlock != null) {
            System.out.println(searchBlock.replace("ServiceUIControllerConstants.", "ServiceUIConstants."));
        } else {
            System.out.println("(not found)");
        }

        String pageMetaBlock = extractGetDefaultPageMetaBlock(legacy);
        System.out.println("\n=== getDefaultPageMeta ===");
        if (pageMetaBlock != null) {
            System.out.println(convertPageMetaToTypeScript(pageMetaBlock, params.rootNodeInstId));
        } else {
            System.out.println("(not found)");
        }
    }

    // ── migrate (applies patches to target file) ──────────────────────────────

    public static void migrate(CopierParams params) throws IOException {
        if (params.legacyUIProjectUrl == null || params.legacyRootNodeListControllerUrl == null) {
            System.out.println("ListControllerMigrator: no legacy URL supplied, skipping.");
            return;
        }

        String rootPascal = toPascal(params.rootNodeInstId);

        Path legacyFile = Paths.get(params.legacyUIProjectUrl + params.legacyRootNodeListControllerUrl);
        if (!Files.exists(legacyFile)) {
            System.out.println("ListControllerMigrator: legacy file not found, skipping — " + legacyFile);
            return;
        }

        String legacy = Files.readString(legacyFile, StandardCharsets.UTF_8);

        Path targetFile = Paths.get(INTELLIGENT_UI_PAGES,
                params.groupId, params.rootNodeInstId, rootPascal + "ListController.tsx");

        if (!Files.exists(targetFile)) {
            System.out.println("ListControllerMigrator: target file not found, skipping — " + targetFile);
            return;
        }

        String content = Files.readString(targetFile, StandardCharsets.UTF_8);

        // 1. patch searchContent
        String searchBlock = extractSearchContentBlock(legacy);
        if (searchBlock != null) {
            String patched = patchSearchContent(content,
                    searchBlock.replace("ServiceUIControllerConstants.", "ServiceUIConstants."));
            if (patched != null) {
                content = patched;
                System.out.println("ListControllerMigrator: patched searchContent in " + targetFile.getFileName());
            } else {
                System.out.println("ListControllerMigrator: searchContent property not found, skipping patch.");
            }
        } else {
            System.out.println("ListControllerMigrator: searchContent block not found in legacy file.");
        }

        // 2. patch getDefaultPageMeta
        String legacyPageMeta = extractGetDefaultPageMetaBlock(legacy);
        if (legacyPageMeta != null) {
            String tsPageMeta = convertPageMetaToTypeScript(legacyPageMeta, params.rootNodeInstId);
            String patched = patchGetDefaultPageMeta(content, tsPageMeta);
            if (patched != null) {
                content = patched;
                System.out.println("ListControllerMigrator: patched getDefaultPageMeta in " + targetFile.getFileName());
            } else {
                System.out.println("ListControllerMigrator: getDefaultPageMeta not found in target, skipping patch.");
            }
        } else {
            System.out.println("ListControllerMigrator: getDefaultPageMeta not found in legacy file.");
        }

        Files.writeString(targetFile, content, StandardCharsets.UTF_8);
    }

    // ── extract raw JS searchContent block ────────────────────────────────────

    static String extractSearchContentBlock(String src) {
        int start = src.indexOf("searchContent:");
        if (start < 0) return null;
        int braceOpen = src.indexOf('{', start);
        if (braceOpen < 0) return null;
        return extractBraceBlock(src, braceOpen);
    }

    // ── extract raw JS getDefaultPageMeta return block ────────────────────────

    static String extractGetDefaultPageMetaBlock(String src) {
        int fnStart = src.indexOf("getDefaultPageMeta:");
        if (fnStart < 0) return null;
        // find the `return {` inside the function
        int returnIdx = src.indexOf("return {", fnStart);
        if (returnIdx < 0) return null;
        int braceOpen = src.indexOf('{', returnIdx);
        if (braceOpen < 0) return null;
        return extractBraceBlock(src, braceOpen);
    }

    static String extractBraceBlock(String src, int braceOpen) {
        int depth = 0;
        for (int i = braceOpen; i < src.length(); i++) {
            char c = src.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return src.substring(braceOpen, i + 1);
            }
        }
        return null;
    }

    // ── convert legacy getDefaultPageMeta object to TS ───────────────────────
    //
    // Rules applied:
    //   - Drop top-level keys: pageId, pageCategory, labelObject, parentVue,
    //     parentContent, serviceManager, coreModelId, i18nPath, helpDocumentName
    //   - vm.xxx  →  this.xxx
    //   - AsyncSection.sectionCategory.SEARCH  →  SectionCategory.SEARCH
    //   - AsyncSection.sectionCategory.EMBEDLIST  →  SectionCategory.EMBEDLIST
    //   - ServiceListControlHelper.extendDocSearchTabFieldMeta  →
    //       ServiceListController.extendDocSearchTabFieldMeta
    //   - SEARCH section: replace sectionId with 'basic', add parentContentPath: ''
    //     drop targetTableSectionId, titleLabelKey, titleHelpKey
    //   - EMBEDLIST section: drop titleLabelKey, titleIcon, url, editModule, scrollX, label
    //   - uuid-only field (no labelKey): add labelKey: 'id', hidden: true
    //   - minWidth: 'Npx'  →  width: N (numeric)
    //   - SystemStandrdMetadataProxy.getDefaultPrirityCodeIconArray()  →
    //       getDefaultPrirityCodeIconArray()

    static String convertPageMetaToTypeScript(String jsBlock, String rootNodeInstId) {
        String ts = jsBlock;

        // vm.xxx → this.xxx  AND  vm: vm → vm: this
        ts = ts.replaceAll("\\bvm\\.", "this.");
        ts = ts.replaceAll("\\bvm:\\s*vm\\b", "vm: this");

        // Drop metadata-only top-level keys (whole line)
        ts = dropTopLevelKey(ts, "pageId");
        ts = dropTopLevelKey(ts, "pageCategory");
        ts = dropTopLevelKey(ts, "labelObject");
        ts = dropTopLevelKey(ts, "parentVue");
        ts = dropTopLevelKey(ts, "parentContent");
        ts = dropTopLevelKey(ts, "serviceManager");
        ts = dropTopLevelKey(ts, "coreModelId");
        ts = dropTopLevelKey(ts, "i18nPath");
        // helpDocumentName spans multiple lines as an array — drop the whole array value
        ts = ts.replaceAll("(?s)[ \\t]*helpDocumentName:\\s*\\[[^\\]]*\\],?\\n", "");

        // AsyncSection.sectionCategory → SectionCategory
        ts = ts.replace("AsyncSection.sectionCategory.SEARCH", "SectionCategory.SEARCH");
        ts = ts.replace("AsyncSection.sectionCategory.EMBEDLIST", "SectionCategory.EMBEDLIST");

        // ServiceListControlHelper → ServiceListController
        ts = ts.replace("ServiceListControlHelper.extendDocSearchTabFieldMeta",
                "ServiceListController.extendDocSearchTabFieldMeta");

        // SEARCH section: replace sectionId value with 'basic', add parentContentPath, drop noise keys
        ts = ts.replaceAll("sectionId:\\s*'[^']*Section'", "sectionId: 'basic'");
        ts = ts.replace("sectionId: 'basic'",
                "sectionId: 'basic',\n\t\t\t\tparentContentPath: ''");
        ts = dropSectionKey(ts, "targetTableSectionId");
        ts = dropSectionKey(ts, "titleLabelKey");
        ts = dropSectionKey(ts, "titleHelpKey");

        // EMBEDLIST section: drop noise keys
        ts = dropSectionKey(ts, "titleIcon");
        ts = dropSectionKey(ts, "url");
        ts = dropSectionKey(ts, "editModule");
        ts = dropSectionKey(ts, "scrollX");
        ts = dropSectionKey(ts, "label");

        // uuid-only fieldMeta (no labelKey on same or following line): add hidden: true, labelKey: 'id'
        ts = ts.replaceAll(
                "(\\{\\s*\n\\s*fieldName:\\s*'[^']*\\.uuid',\\s*\n)(\\s*\\})",
                "$1\t\t\t\t\tlabelKey: 'id',\n\t\t\t\t\thidden: true,\n$2");

        // minWidth: 'Npx' → width: N
        ts = ts.replaceAll("minWidth:\\s*'(\\d+)px'", "width: $1");

        // SystemStandrdMetadataProxy.getDefaultPrirityCodeIconArray()
        ts = ts.replace("SystemStandrdMetadataProxy.getDefaultPrirityCodeIconArray()",
                "getDefaultPrirityCodeIconArray()");

        return ts;
    }

    // drop a simple key: value, line from a JS/TS object
    static String dropTopLevelKey(String src, String key) {
        return src.replaceAll("(?m)^[ \\t]*" + key + ":\\s*[^,\\n]+,?\\n", "");
    }

    static String dropSectionKey(String src, String key) {
        return src.replaceAll("(?m)[ \\t]*" + key + ":\\s*[^,\\n]+,?\\n", "");
    }

    // ── patch readonly searchContent in the generated ListController ─────────

    static String patchSearchContent(String content, String tsBlock) {
        Pattern pattern = Pattern.compile(
                "(readonly searchContent:\\s*Record<string,\\s*unknown>\\s*=\\s*)\\{[^;]*\\};",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) return null;

        String replacement = matcher.group(1) + tsBlock + ";";
        return content.substring(0, matcher.start())
                + replacement
                + content.substring(matcher.end());
    }

    // ── patch getDefaultPageMeta() body in the generated ListController ───────

    static String patchGetDefaultPageMeta(String content, String tsBlock) {
        // Match:  protected getDefaultPageMeta() { return { ... }; }
        Pattern pattern = Pattern.compile(
                "(protected getDefaultPageMeta\\(\\)\\s*\\{[\\s\\n]*return\\s*)\\{.*?\\}(;?\\s*\\})",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) return null;

        String replacement = matcher.group(1) + tsBlock + ";" + matcher.group(2);
        return content.substring(0, matcher.start())
                + replacement
                + content.substring(matcher.end());
    }

    static String toPascal(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}

