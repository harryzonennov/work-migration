import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Copies a logistics page module (e.g. inquiry) to a new module folder,
 * renaming files and replacing content tokens accordingly.
 *
 * File-naming convention observed in src/pages/logistics/inquiry:
 *   Inquiry*          → {RootNodeInstId}*          (PascalCase prefix)
 *   useInquiry*       → use{RootNodeInstId}*        (camelCase hook prefix)
 *   InquiryMaterialItem* → {ItemNodeInstId}*        (PascalCase prefix)
 *   useInquiryMaterialItem* → use{ItemNodeInstId}*  (camelCase hook prefix)
 *
 * Content replacements (longest/most-specific first to avoid partial hits):
 *   InquiryMaterialItem → {ItemNodeInstId_pascal}
 *   inquiryMaterialItem → {itemNodeInstId_camel}
 *   Inquiry             → {RootNodeInstId_pascal}
 *   inquiry             → {rootNodeInstId_camel}
 */
public class PageModuleCopier {

    static final String INTELLIGENT_UI_SRC =
            "/Users/I043125/work2/IntelligentUI/src/pages/";

    static final String RefDocRootNodeInstId = "inquiry";

    static final String RefDocItemNodeInstId = "inquiryMaterialItem";

    static final String RefDocPath = "logistics";

    static final String RefDummyDocRootNodeInstId = "material";

    static final String RefDummyDocItemNodeInstId = "materialUnit";

    static final String RefDummyDocPath = "platform";

    static final String RefSERootNodeInstId = "standardMaterialUnit";

    static final String RefSEPath = "platform";

    static class RefConfig {
        final String refRootNodeInstId;
        final String refItemNodeInstId;
        final String refPath;
        RefConfig(String refRootNodeInstId, String refItemNodeInstId, String refPath) {
            this.refRootNodeInstId = refRootNodeInstId;
            this.refItemNodeInstId = refItemNodeInstId;
            this.refPath = refPath;
        }
    }

    static RefConfig resolveRef(int modelCategory) {
        if (modelCategory == MigrationEntrance.MODEL_CAT_DUMMY_DOCUMENT)
            return new RefConfig(RefDummyDocRootNodeInstId, RefDummyDocItemNodeInstId, RefDummyDocPath);
        if (modelCategory == MigrationEntrance.MODEL_CAT_SER_ENTITY)
            return new RefConfig(RefSERootNodeInstId, null, RefSEPath);
        return new RefConfig(RefDocRootNodeInstId, RefDocItemNodeInstId, RefDocPath);
    }

    public static void mainEntry(String rootNodeInstId,
                                 String itemNodeInstId,
                                 String groupId,
                                 int modelCategory) throws IOException {

        RefConfig ref = resolveRef(modelCategory);

        // ── derive pascal / camel variants ─────────────────────────────────
        String rootPascal = toPascal(rootNodeInstId);   // e.g. InboundDelivery
        String rootCamel  = toCamel(rootNodeInstId);    // e.g. inboundDelivery

        String itemPascal = itemNodeInstId != null ? toPascal(itemNodeInstId) : null;
        String itemCamel  = itemNodeInstId != null ? toCamel(itemNodeInstId)  : null;

        // ── source / target dirs ───────────────────────────────────────────
        Path sourceDir = Paths.get(INTELLIGENT_UI_SRC, ref.refPath, ref.refRootNodeInstId);
        Path targetDir = Paths.get(INTELLIGENT_UI_SRC, groupId, rootCamel);

        if (!Files.exists(sourceDir)) {
            throw new IllegalStateException("Source directory not found: " + sourceDir);
        }
        Files.createDirectories(targetDir);

        // ── iterate source files ───────────────────────────────────────────
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            for (Path srcFile : stream) {
                if (!Files.isRegularFile(srcFile)) continue;

                String originalName = srcFile.getFileName().toString();
                String newName = renameFile(originalName, ref, rootPascal,
                        itemPascal, itemCamel, itemNodeInstId != null);

                if (newName == null) {
                    // itemNodeInstId is null and this file belongs to the item — skip
                    System.out.println("Skipping (itemNodeInstId is null): " + originalName);
                    continue;
                }

                Path targetFile = targetDir.resolve(newName);
                if (Files.exists(targetFile)) {
                    System.out.println("Skipping (already exists): " + newName);
                    continue;
                }
                String content = Files.readString(srcFile, StandardCharsets.UTF_8);
                String newContent = replaceContent(content, ref, rootPascal, rootCamel,
                        itemPascal, itemCamel, itemNodeInstId != null);

                Files.writeString(targetFile, newContent, StandardCharsets.UTF_8);
                System.out.println("Copied: " + originalName + " → " + newName);
            }
        }

        System.out.println("\nDone. Output: " + targetDir);
    }

    public static void mainEntry(CopierParams params) throws IOException {
        mainEntry(params.rootNodeInstId, params.itemNodeInstId, params.groupId, params.modelCategory);
        ListControllerMigrator.migrate(params);
    }

    // ── file rename ────────────────────────────────────────────────────────

    /**
     * Returns the new file name, or null if the file should be skipped
     * (belongs to item tier and itemNodeInstId is null).
     */
    static String renameFile(String name,
                              RefConfig ref,
                              String rootPascal,
                              String itemPascal, String itemCamel,
                              boolean hasItem) {
        String refItemPascal = ref.refItemNodeInstId != null ? toPascal(ref.refItemNodeInstId) : null;
        String refRootPascal = toPascal(ref.refRootNodeInstId);
        String useRefRoot = "use" + refRootPascal;

        if (refItemPascal != null) {
            String useRefItem = "use" + refItemPascal;
            if (name.startsWith(useRefItem)) {
                if (!hasItem) return null;
                return "use" + itemPascal + name.substring(useRefItem.length());
            }
        }
        if (name.startsWith(useRefRoot)) {
            return "use" + rootPascal + name.substring(useRefRoot.length());
        }

        if (refItemPascal != null && name.startsWith(refItemPascal)) {
            if (!hasItem) return null;
            return itemPascal + name.substring(refItemPascal.length());
        }
        if (name.startsWith(refRootPascal)) {
            return rootPascal + name.substring(refRootPascal.length());
        }

        return name;
    }

    // ── content replacement ────────────────────────────────────────────────

    static String replaceContent(String content,
                                  RefConfig ref,
                                  String rootPascal, String rootCamel,
                                  String itemPascal, String itemCamel,
                                  boolean hasItem) {
        if (hasItem && ref.refItemNodeInstId != null) {
            content = content.replace(toPascal(ref.refItemNodeInstId), itemPascal);
            content = content.replace(ref.refItemNodeInstId, itemCamel);
        }
        content = content.replace(toPascal(ref.refRootNodeInstId), rootPascal);
        content = content.replace(ref.refRootNodeInstId, rootCamel);
        return content;
    }

    // ── case helpers ───────────────────────────────────────────────────────

    /** "inboundDelivery" → "InboundDelivery" */
    static String toPascal(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) return camelCase;
        return Character.toUpperCase(camelCase.charAt(0)) + camelCase.substring(1);
    }

    /** "InboundDelivery" → "inboundDelivery" (already camel → unchanged) */
    static String toCamel(String input) {
        if (input == null || input.isEmpty()) return input;
        return Character.toLowerCase(input.charAt(0)) + input.substring(1);
    }

}
