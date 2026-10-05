import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Generates a ***Manager.ts in src/services/{groupId}/ by reading the legacy
 * JS Manager file and producing the correct per-document status icons, action
 * codes, popover fields, and docflow fields — rather than blindly substituting
 * tokens from a reference file.
 */
public class ServiceManagerCopier {

    static final String INTELLIGENT_UI_SERVICES =
            "/Users/I043125/work2/IntelligentUI/src/services/";

    // Subdirectories under legacyUIProjectUrl/js/ to search for the Manager JS.
    static final String[] LEGACY_MANAGER_SEARCH_DIRS = {
        "supplyChain", "production", "salesDistribution", "finance", ""
    };

    // ── helpers ───────────────────────────────────────────────────────────────

    static String toPascal(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static String toCamel(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    // ── legacy JS parsing ─────────────────────────────────────────────────────

    /** Locate the legacy Manager JS file for the given rootNodeInstId. */
    static Path findLegacyManagerJs(String legacyUIProjectUrl, String rootNodeInstId) {
        String fileName = toPascal(rootNodeInstId) + "Manager.js";
        for (String dir : LEGACY_MANAGER_SEARCH_DIRS) {
            Path candidate = dir.isEmpty()
                ? Paths.get(legacyUIProjectUrl, "js", fileName)
                : Paths.get(legacyUIProjectUrl, "js", dir, fileName);
            if (Files.exists(candidate)) return candidate;
        }
        return null;
    }

    /**
     * Parse DOC_ACTION_CODE keys from a legacy JS manager.
     * Returns ordered list of ACTION_XXX keys (e.g. ACTION_APPROVE, ACTION_SUBMIT …).
     */
    static List<String> parseDocActionCodeKeys(String js, String rootPascal) {
        // Match the DOC_ACTION_CODE block: XxxManager.DOC_ACTION_CODE = { ... };
        Pattern blockPat = Pattern.compile(
            Pattern.quote(rootPascal) + "Manager\\.DOC_ACTION_CODE\\s*=\\s*\\{([^}]+)\\}",
            Pattern.DOTALL);
        Matcher blockMatcher = blockPat.matcher(js);
        if (!blockMatcher.find()) return Collections.emptyList();

        String block = blockMatcher.group(1);
        // Each entry: KEY:DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.ACTION_XXX
        Pattern entryPat = Pattern.compile(
            "\\b(\\w+)\\s*:\\s*DocumentConstants\\.StandardProperty\\.SystemDefDocActionCodeProxy\\.(ACTION_\\w+)");
        Matcher entryMatcher = entryPat.matcher(block);
        List<String> proxyKeys = new ArrayList<>();
        while (entryMatcher.find()) {
            proxyKeys.add(entryMatcher.group(2));  // e.g. ACTION_APPROVE
        }
        return proxyKeys;
    }

    /**
     * Parse getStatusIconArray entries from the legacy JS manager.
     * Returns ordered list of status constant paths (e.g. "DocumentConstants.PurchaseReturnOrder.status.INITIAL").
     */
    static List<String[]> parseStatusIconEntries(String js, String rootPascal) {
        // Match the STATIC (non-prototype) getStatusIconArray function body
        // Pattern: XxxManager.getStatusIconArray = function() { ... return [...]; };
        Pattern funcPat = Pattern.compile(
            Pattern.quote(rootPascal) + "Manager\\.getStatusIconArray\\s*=\\s*function\\s*\\([^)]*\\)\\s*\\{(.*?)return\\s*\\[(.+?)\\]\\s*;",
            Pattern.DOTALL);
        Matcher funcMatcher = funcPat.matcher(js);
        if (!funcMatcher.find()) return Collections.emptyList();

        String arrayBody = funcMatcher.group(2);
        // Each entry: { id: DocumentConstants.Xxx.status.YYY, iconClass: DocumentContentProp.statusIcon.ZZZ }
        Pattern entryPat = Pattern.compile(
            "id\\s*:\\s*(DocumentConstants\\.\\w+\\.status\\.\\w+)\\s*,\\s*iconClass\\s*:\\s*(DocumentContentProp\\.statusIcon\\.\\w+)");
        Matcher entryMatcher = entryPat.matcher(arrayBody);
        List<String[]> entries = new ArrayList<>();
        while (entryMatcher.find()) {
            entries.add(new String[]{ entryMatcher.group(1), entryMatcher.group(2) });
        }
        return entries;
    }

    /**
     * Parse extra fieldMetaList entries from getDocumentPopoverContent beyond the standard ones.
     * Returns list of fieldName strings that are not the standard id/name/status/purchaseFrom/purchaseTo fields.
     */
    static List<String[]> parsePopoverExtraRootFields(String js, String rootPascal) {
        Pattern funcPat = Pattern.compile(
            Pattern.quote(rootPascal) + "Manager\\.getDocumentPopoverContent\\s*=\\s*function[^{]*\\{(.+?)(?=^\\w|\\n\\w+Manager\\.)",
            Pattern.DOTALL | Pattern.MULTILINE);
        Matcher funcMatcher = funcPat.matcher(js);
        if (!funcMatcher.find()) return Collections.emptyList();
        String body = funcMatcher.group(1);

        // Extract fieldMetaList array
        Pattern fieldListPat = Pattern.compile("var\\s+fieldMetaList\\s*=\\s*\\[(.+?)\\]\\s*;", Pattern.DOTALL);
        Matcher fieldListMatcher = fieldListPat.matcher(body);
        if (!fieldListMatcher.find()) return Collections.emptyList();

        String fieldListBody = fieldListMatcher.group(1);
        return parseFieldMetaEntries(fieldListBody);
    }

    /**
     * Parse fieldMetaList entries from getDefaultDocFlowFieldConfigure.
     */
    static List<String[]> parseDocFlowFieldMeta(String js, String rootPascal) {
        Pattern funcPat = Pattern.compile(
            Pattern.quote(rootPascal) + "Manager\\.getDefaultDocFlowFieldConfigure\\s*=\\s*function[^{]*\\{(.+?)^\\};",
            Pattern.DOTALL | Pattern.MULTILINE);
        Matcher funcMatcher = funcPat.matcher(js);
        if (!funcMatcher.find()) return Collections.emptyList();
        String body = funcMatcher.group(1);

        Pattern fieldListPat = Pattern.compile("fieldMetaList\\s*:\\s*\\[(.+?)\\]\\s*\\}", Pattern.DOTALL);
        Matcher fieldListMatcher = fieldListPat.matcher(body);
        if (!fieldListMatcher.find()) return Collections.emptyList();

        return parseFieldMetaEntries(fieldListMatcher.group(1));
    }

    /**
     * Parse extra referenceDate fieldNameList from getDocumentPopoverContent.
     */
    static List<String> parsePopoverReferenceDates(String js, String rootPascal) {
        // Find the entire getDocumentPopoverContent function body
        // Strategy: find the function start, then scan for the referenceDate block
        int funcStart = js.indexOf(rootPascal + "Manager.getDocumentPopoverContent = function");
        if (funcStart < 0) return Collections.emptyList();
        // Extract a generous chunk (4000 chars) starting from function declaration
        int end = Math.min(funcStart + 4000, js.length());
        String body = js.substring(funcStart, end);

        Pattern refDatePat = Pattern.compile(
            "referenceDate\\s*:\\s*\\{[^}]*fieldNameList\\s*:\\s*\\[([^\\]]+)\\]", Pattern.DOTALL);
        Matcher refMatcher = refDatePat.matcher(body);
        if (!refMatcher.find()) return Collections.emptyList();

        String dateListBody = refMatcher.group(1);
        List<String> dates = new ArrayList<>();
        Matcher stringMatcher = Pattern.compile("'([^']+)'").matcher(dateListBody);
        while (stringMatcher.find()) dates.add(stringMatcher.group(1));
        return dates;
    }

    /** Helper: parse {fieldName: 'x', labelKey: 'y', ...} entries from a fieldMetaList body. */
    static List<String[]> parseFieldMetaEntries(String body) {
        // Split on }{ boundaries to get individual field objects
        List<String[]> entries = new ArrayList<>();
        // Extract individual { ... } objects
        Pattern objPat = Pattern.compile("\\{([^{}]+)\\}", Pattern.DOTALL);
        Matcher objMatcher = objPat.matcher(body);
        while (objMatcher.find()) {
            String obj = objMatcher.group(1);
            String fieldName = extractStringProp(obj, "fieldName");
            String labelKey  = extractStringProp(obj, "labelKey");
            String fieldKey  = extractStringProp(obj, "fieldKey");
            String iconClassMap = extractStringProp(obj, "iconClassMap");
            if (fieldName != null) {
                entries.add(new String[]{ fieldName, labelKey, fieldKey, iconClassMap });
            }
        }
        return entries;
    }

    static String extractStringProp(String obj, String propName) {
        Pattern p = Pattern.compile("['\"]?" + Pattern.quote(propName) + "['\"]?\\s*:\\s*['\"]([^'\"]+)['\"]");
        Matcher m = p.matcher(obj);
        return m.find() ? m.group(1) : null;
    }

    // ── i18n path resolution ──────────────────────────────────────────────────

    /** Resolve the i18n folder path from the legacy getI18nPath or from groupId. */
    static String resolveI18nFolder(String js, String rootPascal, String groupId) {
        // Try to parse getI18nPath from JS
        Pattern pat = Pattern.compile(
            Pattern.quote(rootPascal) + "Manager\\.getI18nPath\\s*=\\s*function[^{]*\\{[^\"']*[\"']([^\"']+)[\"']");
        Matcher m = pat.matcher(js);
        if (m.find()) return m.group(1).replace("/", "");  // e.g. "supplyChain"
        return groupId;
    }

    // ── DocumentConstants mapping ─────────────────────────────────────────────

    /**
     * Map a DocumentConstants.Xxx.status.KEY reference from the legacy JS
     * to the equivalent TS expression.
     * e.g. "DocumentConstants.PurchaseReturnOrder.status.INITIAL"
     *   -> "DocumentConstants.PurchaseReturnOrder.status.INITIAL"  (pass-through — same in TS)
     */
    static String mapStatusRef(String legacyRef) {
        return legacyRef;  // TS DocumentConstants has the same structure
    }

    /**
     * Map a DocumentContentProp.statusIcon.KEY from legacy to TS.
     * e.g. "DocumentContentProp.statusIcon.INITIAL" -> same (pass-through)
     */
    static String mapIconRef(String legacyRef) {
        return legacyRef;
    }

    /** Given a legacy "DocumentConstants.Xxx.status.KEY", extract the TS document type constant. */
    static String extractDocTypeFromStatusRef(String statusRef) {
        // DocumentConstants.PurchaseReturnOrder.status.INITIAL -> PURCHASERETURNORDER
        Pattern p = Pattern.compile("DocumentConstants\\.([A-Z]\\w+)\\.status");
        Matcher m = p.matcher(statusRef);
        if (m.find()) {
            String className = m.group(1);
            // Map to DocumentConstants.DocumentType.XXX key
            return toUpperCase(className);
        }
        return null;
    }

    static String toUpperCase(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) sb.append(Character.toUpperCase(c));
        return sb.toString();
    }

    // ── TS generation ─────────────────────────────────────────────────────────

    public static void mainEntry(CopierParams params) throws IOException {
        String legacyUrl = (params.legacyUIProjectUrl != null) ? params.legacyUIProjectUrl : "";
        mainEntry(params.rootNodeInstId, params.itemNodeInstId, params.groupId,
                  params.modelCategory, legacyUrl);
    }

    public static void mainEntry(String rootNodeInstId,
                                 String itemNodeInstId,
                                 String groupId,
                                 int modelCategory,
                                 String legacyUIProjectUrl) throws IOException {

        String rootPascal = toPascal(rootNodeInstId);
        String rootCamel  = toCamel(rootNodeInstId);
        String itemPascal = itemNodeInstId != null ? toPascal(itemNodeInstId) : null;
        String itemCamel  = itemNodeInstId != null ? toCamel(itemNodeInstId)  : null;
        boolean hasItem   = itemNodeInstId != null;

        Path targetDir  = Paths.get(INTELLIGENT_UI_SERVICES, groupId);
        Files.createDirectories(targetDir);

        String newFileName = rootPascal + "Manager.ts";
        Path targetFile = targetDir.resolve(newFileName);

        if (Files.exists(targetFile)) {
            System.out.println("Skipping (already exists): " + groupId + "/" + newFileName);
            return;
        }

        // ── Read legacy JS manager ────────────────────────────────────────────
        Path legacyJs = findLegacyManagerJs(legacyUIProjectUrl, rootNodeInstId);
        String jsContent = "";
        if (legacyJs != null && Files.exists(legacyJs)) {
            jsContent = Files.readString(legacyJs, StandardCharsets.UTF_8);
            System.out.println("Reading legacy: " + legacyJs);
        } else {
            System.out.println("WARNING: legacy manager JS not found for " + rootNodeInstId);
        }

        // ── Parse legacy data ─────────────────────────────────────────────────
        List<String> actionCodeKeys      = parseDocActionCodeKeys(jsContent, rootPascal);
        List<String[]> statusIconEntries = parseStatusIconEntries(jsContent, rootPascal);
        List<String[]> docFlowFields     = parseDocFlowFieldMeta(jsContent, rootPascal);
        List<String[]> popoverRootFields = parsePopoverExtraRootFields(jsContent, rootPascal);
        List<String>   referenceDates    = parsePopoverReferenceDates(jsContent, rootPascal);
        String i18nFolder                = resolveI18nFolder(jsContent, rootPascal, groupId);

        // Fallback defaults when legacy parsing yields nothing
        if (actionCodeKeys.isEmpty()) actionCodeKeys = Arrays.asList(
            "ACTION_APPROVE","ACTION_REJECT_APPROVE","ACTION_SUBMIT","ACTION_REVOKE_SUBMIT",
            "ACTION_COUNTAPPROVE","ACTION_DELIVERY_DONE","ACTION_PROCESS_DONE","ACTION_ARCHIVE","ACTION_CANCEL");

        if (statusIconEntries.isEmpty()) {
            // Minimal fallback
            statusIconEntries = Arrays.asList(
                new String[]{"DocumentConstants." + rootPascal + ".status.INITIAL",
                             "DocumentContentProp.statusIcon.INITIAL"},
                new String[]{"DocumentConstants." + rootPascal + ".status.APPROVED",
                             "DocumentContentProp.statusIcon.APPROVED"},
                new String[]{"DocumentConstants." + rootPascal + ".status.SUBMITTED",
                             "DocumentContentProp.statusIcon.SUBMITTED"},
                new String[]{"DocumentConstants." + rootPascal + ".status.REJECT_APPROVAL",
                             "DocumentContentProp.statusIcon.REJECT_APPROVAL"}
            );
        }

        if (referenceDates.isEmpty()) {
            referenceDates = Arrays.asList(
                rootCamel + "UIModel.createdDate",
                rootCamel + "UIModel.updatedDate"
            );
        }

        // ── Derive DocumentType constant name ─────────────────────────────────
        String docTypeKey = toUpperCase(rootPascal);  // e.g. PURCHASERETURNORDER

        // ── Generate TS ───────────────────────────────────────────────────────
        String tsContent = generateManagerTs(
            rootPascal, rootCamel, itemPascal, itemCamel, hasItem,
            groupId, i18nFolder, docTypeKey,
            actionCodeKeys, statusIconEntries,
            referenceDates, popoverRootFields,
            docFlowFields);

        Files.writeString(targetFile, tsContent, StandardCharsets.UTF_8);
        System.out.println("Generated: " + groupId + "/" + newFileName);
    }

    // ── backward-compat overload (no legacyUIProjectUrl) ─────────────────────

    public static void mainEntry(String rootNodeInstId,
                                 String itemNodeInstId,
                                 String groupId,
                                 int modelCategory) throws IOException {
        mainEntry(rootNodeInstId, itemNodeInstId, groupId, modelCategory, "");
    }

    // ── TS template ───────────────────────────────────────────────────────────

    static String generateManagerTs(
            String rootPascal, String rootCamel,
            String itemPascal, String itemCamel,
            boolean hasItem,
            String groupId, String i18nFolder,
            String docTypeKey,
            List<String> actionCodeKeys,
            List<String[]> statusIconEntries,
            List<String> referenceDates,
            List<String[]> popoverRootFields,
            List<String[]> docFlowFields) {

        StringBuilder sb = new StringBuilder();
        String tab = "\t";

        // ── file-header comment ───────────────────────────────────────────────
        sb.append("/**\n");
        sb.append(" * ").append(rootPascal).append("Manager.ts\n");
        sb.append(" *\n");
        sb.append(" * TypeScript port of the legacy `").append(rootPascal).append("Manager.js`.\n");
        sb.append(" * Node ids: `").append(rootCamel).append("`");
        if (hasItem) sb.append(" / `").append(itemCamel).append("`");
        sb.append("\n");
        sb.append(" *\n");
        sb.append(" * ## i18n self-registration\n");
        sb.append(" * Importing this file registers the `").append(rootCamel).append("` namespace plus\n");
        sb.append(" * the shared foundation namespaces (en + zh).\n");
        sb.append(" */\n\n");

        // ── imports ───────────────────────────────────────────────────────────
        sb.append("import i18n from '@/i18n';\n");
        sb.append("import { ServiceModuleConstants, DocumentConstants, DocumentContentProp } from '@/services/DocumentConstants';\n");
        sb.append("import { ServiceManager, type PopoverCardConfig } from '@/services/ServiceManager';\n");
        sb.append("\n");

        // foundation en imports
        sb.append("// ── Foundation namespaces ────────────────────────────────────────────────────\n");
        for (String ns : new String[]{"CommonElements","DocInvolveParty","DocActionNode","DocFlowNode","DocMatItem"}) {
            String varName = "en" + ns;
            sb.append("import ").append(varName)
              .append(" from '@/i18n/locales/en/foundation/").append(ns).append(".json';\n");
        }
        sb.append("\n");
        for (String ns : new String[]{"CommonElements","DocInvolveParty","DocActionNode","DocFlowNode","DocMatItem"}) {
            String varName = "zh" + ns;
            sb.append("import ").append(varName)
              .append(" from '@/i18n/locales/zh/foundation/").append(ns).append(".json';\n");
        }
        sb.append("\n");

        // business namespace — infer JSON path from i18nFolder + rootPascal
        String i18nNs = rootCamel;
        String jsonRelPath = i18nFolder + "/" + rootPascal;
        sb.append("// ── Business namespace ────────────────────────────────────────────────────────\n");
        sb.append("import en").append(rootPascal)
          .append(" from '@/i18n/locales/en/").append(jsonRelPath).append(".json';\n");
        sb.append("import zh").append(rootPascal)
          .append(" from '@/i18n/locales/zh/").append(jsonRelPath).append(".json';\n");
        sb.append("\n");

        // addResourceBundle calls
        sb.append("// Register namespaces on first import (deep merge, no overwrite — HMR-safe).\n");
        for (String ns : new String[]{"CommonElements","DocInvolveParty","DocActionNode","DocFlowNode","DocMatItem"}) {
            String camelNs = toCamel(ns);
            sb.append("i18n.addResourceBundle('en', '").append(camelNs)
              .append("', en").append(ns).append(", true, false);\n");
        }
        sb.append("i18n.addResourceBundle('en', '").append(i18nNs)
          .append("', en").append(rootPascal).append(", true, false);\n");
        sb.append("\n");
        for (String ns : new String[]{"CommonElements","DocInvolveParty","DocActionNode","DocFlowNode","DocMatItem"}) {
            String camelNs = toCamel(ns);
            sb.append("i18n.addResourceBundle('zh', '").append(camelNs)
              .append("', zh").append(ns).append(", true, false);\n");
        }
        sb.append("i18n.addResourceBundle('zh', '").append(i18nNs)
          .append("', zh").append(rootPascal).append(", true, false);\n");
        sb.append("\n\n");

        // ── class ─────────────────────────────────────────────────────────────
        sb.append("export class ").append(rootPascal).append("Manager extends ServiceManager {\n");

        // DOC_ACTION_CODE
        sb.append(tab).append("static readonly DOC_ACTION_CODE = {\n");
        for (String proxyKey : actionCodeKeys) {
            // Convert ACTION_APPROVE -> APPROVE (remove ACTION_ prefix)
            String shortKey = proxyKey.startsWith("ACTION_") ? proxyKey.substring("ACTION_".length()) : proxyKey;
            sb.append(tab).append(tab).append(shortKey)
              .append(": DocumentConstants.StandardProperty.SystemDefDocActionCodeProxy.").append(proxyKey).append(",\n");
        }
        sb.append(tab).append("} as const;\n\n");

        // getStatusURL
        sb.append(tab).append("static getStatusURL(): string {\n");
        sb.append(tab).append(tab).append("return '../").append(rootCamel).append("/getStatusMap.html';\n");
        sb.append(tab).append("}\n\n");

        // getResourceId
        sb.append(tab).append("static getResourceId(): string {\n");
        sb.append(tab).append(tab).append("return ServiceModuleConstants.").append(rootPascal).append(";\n");
        sb.append(tab).append("}\n\n");

        // getDocumentType
        sb.append(tab).append("static getDocumentType(): number {\n");
        sb.append(tab).append(tab).append("return DocumentConstants.DocumentType.").append(docTypeKey).append(";\n");
        sb.append(tab).append("}\n\n");

        // getI18nRootConfig
        sb.append(tab).append("static getI18nRootConfig() {\n");
        sb.append(tab).append(tab).append("return {\n");
        sb.append(tab).append(tab).append(tab).append("primaryNs: '").append(i18nNs).append("',\n");
        sb.append(tab).append(tab).append(tab).append("keyPrefix: '").append(rootCamel).append("',\n");
        sb.append(tab).append(tab).append(tab).append("fallbackNs: 'commonElements',\n");
        sb.append(tab).append(tab).append(tab).append("subNsAliases: {\n");
        sb.append(tab).append(tab).append(tab).append(tab).append("docInvolveParty: 'docInvolveParty',\n");
        sb.append(tab).append(tab).append(tab).append(tab).append("docActionNode: 'docActionNode',\n");
        sb.append(tab).append(tab).append(tab).append(tab).append("docFlowNode: 'docFlowNode',\n");
        sb.append(tab).append(tab).append(tab).append(tab).append("docMatItem: 'docMatItem',\n");
        sb.append(tab).append(tab).append(tab).append("},\n");
        sb.append(tab).append(tab).append("};\n");
        sb.append(tab).append("}\n\n");

        // getI18nItemConfig (only if hasItem)
        if (hasItem) {
            sb.append(tab).append("static getI18nItemConfig() {\n");
            sb.append(tab).append(tab).append("return {\n");
            sb.append(tab).append(tab).append(tab).append("primaryNs: '").append(i18nNs).append("',\n");
            sb.append(tab).append(tab).append(tab).append("keyPrefix: '").append(itemCamel).append("',\n");
            sb.append(tab).append(tab).append(tab).append("fallbackNs: 'commonElements',\n");
            sb.append(tab).append(tab).append(tab).append("subNsAliases: {\n");
            sb.append(tab).append(tab).append(tab).append(tab).append("docInvolveParty: 'docInvolveParty',\n");
            sb.append(tab).append(tab).append(tab).append(tab).append("docActionNode: 'docActionNode',\n");
            sb.append(tab).append(tab).append(tab).append(tab).append("docFlowNode: 'docFlowNode',\n");
            sb.append(tab).append(tab).append(tab).append(tab).append("docMatItem: 'docMatItem',\n");
            sb.append(tab).append(tab).append(tab).append("},\n");
            sb.append(tab).append(tab).append("};\n");
            sb.append(tab).append("}\n\n");
        }

        // getStatusIconArray — from parsed legacy data
        sb.append(tab).append("static getStatusIconArray(): Array<{ id: number; iconClass: string }> {\n");
        sb.append(tab).append(tab).append("return [\n");
        for (String[] entry : statusIconEntries) {
            sb.append(tab).append(tab).append(tab).append("{ id: ").append(entry[0])
              .append(", iconClass: ").append(entry[1]).append(" },\n");
        }
        sb.append(tab).append(tab).append("];\n");
        sb.append(tab).append("}\n\n");

        // getDocumentPopoverContent
        sb.append(tab).append("static getDocumentPopoverContent(): PopoverCardConfig {\n");
        sb.append(tab).append(tab).append("return {\n");
        sb.append(tab).append(tab).append(tab).append("docType: DocumentConstants.DocumentType.").append(docTypeKey).append(",\n");
        sb.append(tab).append(tab).append(tab).append("targetPage: '/").append(groupId).append("/").append(rootCamel).append("',\n");
        sb.append(tab).append(tab).append(tab).append("referenceDate: {\n");
        sb.append(tab).append(tab).append(tab).append(tab).append("fieldNameList: [\n");
        for (String date : referenceDates) {
            sb.append(tab).append(tab).append(tab).append(tab).append(tab)
              .append("'").append(date).append("',\n");
        }
        sb.append(tab).append(tab).append(tab).append(tab).append("],\n");
        sb.append(tab).append(tab).append(tab).append("},\n");
        sb.append(tab).append(tab).append(tab).append("responsibleByField: '").append(rootCamel).append("UIModel.createdByName',\n");
        sb.append(tab).append(tab).append(tab).append("fieldMetaList: [\n");

        // Standard root fields: id, name, status
        sb.append(tab).append(tab).append(tab).append(tab)
          .append("{ fieldName: '").append(rootCamel).append("UIModel.id', labelKey: '").append(rootCamel).append(".id' },\n");
        sb.append(tab).append(tab).append(tab).append(tab)
          .append("{ fieldName: '").append(rootCamel).append("UIModel.name', labelKey: '").append(rootCamel).append(".name' },\n");
        sb.append(tab).append(tab).append(tab).append(tab)
          .append("{\n");
        sb.append(tab).append(tab).append(tab).append(tab).append(tab)
          .append("fieldName: '").append(rootCamel).append("UIModel.statusValue',\n");
        sb.append(tab).append(tab).append(tab).append(tab).append(tab)
          .append("fieldKey: '").append(rootCamel).append("UIModel.status',\n");
        sb.append(tab).append(tab).append(tab).append(tab).append(tab)
          .append("labelKey: '").append(rootCamel).append(".status',\n");
        sb.append(tab).append(tab).append(tab).append(tab).append(tab)
          .append("iconArray: ").append(rootPascal).append("Manager.getStatusIconArray(),\n");
        sb.append(tab).append(tab).append(tab).append(tab)
          .append("},\n");

        // Extra fields from legacy popover (filter out standard ones already emitted)
        Set<String> standardPopoverFieldNames = new HashSet<>(Arrays.asList(
            rootCamel + "UIModel.id", rootCamel + "UIModel.name",
            rootCamel + "UIModel.statusValue", rootCamel + "UIModel.status",
            "purchaseFromSupplierUIModel.partyName", "purchaseFromSupplierUIModel.partyId",
            "purchaseFromSupplierUIModel.contactName", "purchaseToOrgUIModel.partyName",
            "purchaseToOrg.partyName"  // alternate field key form
        ));
        for (String[] field : popoverRootFields) {
            String fieldName = field[0];
            if (standardPopoverFieldNames.contains(fieldName)) continue;
            // Skip purchaseFrom/purchaseTo parties — added below as standard
            if (fieldName.startsWith("purchaseFromSupplier") || fieldName.startsWith("purchaseToOrg")) continue;
            sb.append(tab).append(tab).append(tab).append(tab)
              .append("{ fieldName: '").append(fieldName).append("'");
            if (field[1] != null) sb.append(", labelKey: '").append(field[1]).append("'");
            sb.append(" },\n");
        }

        // Standard party fields
        sb.append(tab).append(tab).append(tab).append(tab)
          .append("{ fieldName: 'purchaseFromSupplierUIModel.partyName', labelKey: 'docInvolveParty.purchaseFromSupplierPartyName' },\n");
        sb.append(tab).append(tab).append(tab).append(tab)
          .append("{ fieldName: 'purchaseFromSupplierUIModel.partyId', labelKey: 'docInvolveParty.purchaseFromSupplierPartyId' },\n");
        sb.append(tab).append(tab).append(tab).append(tab)
          .append("{ fieldName: 'purchaseFromSupplierUIModel.contactName', labelKey: 'docInvolveParty.contactName' },\n");
        sb.append(tab).append(tab).append(tab).append(tab)
          .append("{ fieldName: 'purchaseToOrgUIModel.partyName', labelKey: 'docInvolveParty.purchaseToOrgPartyName' },\n");

        sb.append(tab).append(tab).append(tab).append("],\n");
        sb.append(tab).append(tab).append("};\n");
        sb.append(tab).append("}\n\n");

        // getDocItemPopoverContent (only if hasItem)
        if (hasItem) {
            sb.append(tab).append("static getDocItemPopoverContent(): PopoverCardConfig {\n");
            sb.append(tab).append(tab).append("return {\n");
            sb.append(tab).append(tab).append(tab).append("docType: DocumentConstants.DocumentType.").append(docTypeKey).append(",\n");
            sb.append(tab).append(tab).append(tab).append("targetPage: '/").append(groupId).append("/").append(itemCamel).append("',\n");
            sb.append(tab).append(tab).append(tab).append("referenceDate: {\n");
            sb.append(tab).append(tab).append(tab).append(tab).append("fieldNameList: [\n");
            sb.append(tab).append(tab).append(tab).append(tab).append(tab)
              .append("'").append(itemCamel).append("UIModel.createdDate',\n");
            sb.append(tab).append(tab).append(tab).append(tab).append(tab)
              .append("'").append(itemCamel).append("UIModel.updatedDate',\n");
            sb.append(tab).append(tab).append(tab).append(tab).append("],\n");
            sb.append(tab).append(tab).append(tab).append("},\n");
            sb.append(tab).append(tab).append(tab).append("responsibleByField: '").append(itemCamel).append("UIModel.createdByName',\n");
            sb.append(tab).append(tab).append(tab).append("fieldMetaList: [\n");
            sb.append(tab).append(tab).append(tab).append(tab)
              .append("{ fieldName: '").append(itemCamel).append("UIModel.parentDocId', labelKey: '").append(itemCamel).append(".parentDocId' },\n");
            sb.append(tab).append(tab).append(tab).append(tab)
              .append("{ fieldName: '").append(itemCamel).append("UIModel.parentDocName', labelKey: '").append(itemCamel).append(".parentDocName' },\n");
            sb.append(tab).append(tab).append(tab).append(tab).append("{\n");
            sb.append(tab).append(tab).append(tab).append(tab).append(tab)
              .append("fieldName: '").append(itemCamel).append("UIModel.itemStatusValue',\n");
            sb.append(tab).append(tab).append(tab).append(tab).append(tab)
              .append("fieldKey: '").append(itemCamel).append("UIModel.itemStatus',\n");
            sb.append(tab).append(tab).append(tab).append(tab).append(tab)
              .append("labelKey: '").append(itemCamel).append(".itemStatus',\n");
            sb.append(tab).append(tab).append(tab).append(tab).append(tab)
              .append("iconArray: ").append(rootPascal).append("Manager.getStatusIconArray(),\n");
            sb.append(tab).append(tab).append(tab).append(tab).append("},\n");
            sb.append(tab).append(tab).append(tab).append(tab)
              .append("{ fieldName: '").append(itemCamel).append("UIModel.refMaterialSKUId', labelKey: '").append(itemCamel).append(".refMaterialSKUId' },\n");
            sb.append(tab).append(tab).append(tab).append(tab)
              .append("{ fieldName: '").append(itemCamel).append("UIModel.refMaterialSKUName', labelKey: '").append(itemCamel).append(".refMaterialSKUName' },\n");
            sb.append(tab).append(tab).append(tab).append("],\n");
            sb.append(tab).append(tab).append("};\n");
            sb.append(tab).append("}\n\n");
        }

        // getRootNodeInstId
        sb.append(tab).append("static getRootNodeInstId(): string {\n");
        sb.append(tab).append(tab).append("return '").append(rootCamel).append("';\n");
        sb.append(tab).append("}\n\n");

        // getBasePath
        sb.append(tab).append("static override getBasePath(): string {\n");
        sb.append(tab).append(tab).append("return '/").append(groupId).append("/").append(rootCamel).append("';\n");
        sb.append(tab).append("}\n\n");

        if (hasItem) {
            // getItemNodeInstId
            sb.append(tab).append("static getItemNodeInstId(): string {\n");
            sb.append(tab).append(tab).append("return '").append(itemCamel).append("';\n");
            sb.append(tab).append("}\n\n");

            // getRootNodeSubPath
            sb.append(tab).append("static getRootNodeSubPath(): string {\n");
            sb.append(tab).append(tab).append("return '").append(rootCamel).append("UIModel';\n");
            sb.append(tab).append("}\n\n");

            // getItemNodeSubPath
            sb.append(tab).append("static getItemNodeSubPath(): string {\n");
            sb.append(tab).append(tab).append("return '").append(itemCamel).append("UIModel';\n");
            sb.append(tab).append("}\n\n");

            // getDefaultDocumentItemEditorPage
            sb.append(tab).append("static getDefaultDocumentItemEditorPage(): string {\n");
            sb.append(tab).append(tab).append("return '/").append(groupId).append("/").append(itemCamel).append("';\n");
            sb.append(tab).append("}\n\n");

            // getDefaultDocFlowFieldConfigure — use parsed legacy fields
            sb.append(tab).append("static getDefaultDocFlowFieldConfigure(refUIModel: Record<string, unknown>) {\n");
            sb.append(tab).append(tab).append("return {\n");
            sb.append(tab).append(tab).append(tab).append("labelObject: {} as Record<string, unknown>,\n");
            sb.append(tab).append(tab).append(tab).append("parentContent: refUIModel,\n");
            sb.append(tab).append(tab).append(tab).append("docIdPath: 'id',\n");
            sb.append(tab).append(tab).append(tab).append("labelPath: '").append(itemCamel).append("',\n");
            sb.append(tab).append(tab).append(tab).append("fieldMetaList: [\n");

            if (!docFlowFields.isEmpty()) {
                for (String[] field : docFlowFields) {
                    String fieldName = field[0];
                    String labelKey  = field[1];
                    String fieldKey  = field[2];
                    String iconMap   = field[3];
                    sb.append(tab).append(tab).append(tab).append(tab).append("{\n");
                    sb.append(tab).append(tab).append(tab).append(tab).append(tab)
                      .append("fieldName: '").append(fieldName).append("',\n");
                    if (labelKey != null)
                        sb.append(tab).append(tab).append(tab).append(tab).append(tab)
                          .append("labelKey: '").append(labelKey).append("',\n");
                    if (fieldKey != null)
                        sb.append(tab).append(tab).append(tab).append(tab).append(tab)
                          .append("fieldKey: '").append(fieldKey).append("',\n");
                    if (iconMap != null)
                        sb.append(tab).append(tab).append(tab).append(tab).append(tab)
                          .append("iconClassMap: '").append(iconMap).append("',\n");
                    sb.append(tab).append(tab).append(tab).append(tab).append("},\n");
                }
            } else {
                // Minimal fallback
                sb.append(tab).append(tab).append(tab).append(tab)
                  .append("{ fieldName: 'refMaterialSKUId' },\n");
                sb.append(tab).append(tab).append(tab).append(tab)
                  .append("{ fieldName: 'refMaterialSKUName' },\n");
                sb.append(tab).append(tab).append(tab).append(tab)
                  .append("{ fieldName: 'amountLabel', labelKey: 'amount', fieldKey: 'amount' },\n");
                sb.append(tab).append(tab).append(tab).append(tab).append("{\n");
                sb.append(tab).append(tab).append(tab).append(tab).append(tab)
                  .append("fieldName: 'parentDocStatusValue',\n");
                sb.append(tab).append(tab).append(tab).append(tab).append(tab)
                  .append("labelKey: 'parentDocStatus',\n");
                sb.append(tab).append(tab).append(tab).append(tab).append(tab)
                  .append("fieldKey: 'parentDocStatus',\n");
                sb.append(tab).append(tab).append(tab).append(tab).append(tab)
                  .append("iconClassMap: 'getStatusIconArray',\n");
                sb.append(tab).append(tab).append(tab).append(tab).append("},\n");
            }

            sb.append(tab).append(tab).append(tab).append("],\n");
            sb.append(tab).append(tab).append("};\n");
            sb.append(tab).append("}\n\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

}
