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

    // ── main ───────────────────────────────────────────────────────────────
    public static void main(String[] args) throws IOException {
        mainEntry("inboundDelivery", "inboundItem", "logistics");
    }

    public static void mainEntry(String rootNodeInstId,
                                 String itemNodeInstId,
                                 String groupId) throws IOException {

        // ── derive pascal / camel variants ─────────────────────────────────
        String rootPascal = toPascal(rootNodeInstId);   // e.g. InboundDelivery
        String rootCamel  = toCamel(rootNodeInstId);    // e.g. inboundDelivery

        String itemPascal = itemNodeInstId != null ? toPascal(itemNodeInstId) : null;
        String itemCamel  = itemNodeInstId != null ? toCamel(itemNodeInstId)  : null;

        // ── source / target dirs ───────────────────────────────────────────
        Path sourceDir = Paths.get(INTELLIGENT_UI_SRC, "logistics", "inquiry");
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
                String newName = renameFile(originalName, rootPascal, rootCamel,
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
                String newContent = replaceContent(content, rootPascal, rootCamel,
                        itemPascal, itemCamel, itemNodeInstId != null);

                Files.writeString(targetFile, newContent, StandardCharsets.UTF_8);
                System.out.println("Copied: " + originalName + " → " + newName);
            }
        }

        System.out.println("\nDone. Output: " + targetDir);
    }

    // ── file rename ────────────────────────────────────────────────────────

    /**
     * Returns the new file name, or null if the file should be skipped
     * (belongs to item tier and itemNodeInstId is null).
     */
    static String renameFile(String name,
                              String rootPascal, String rootCamel,
                              String itemPascal, String itemCamel,
                              boolean hasItem) {
        // hook files: useInquiryMaterialItem* / useInquiry*
        if (name.startsWith("useInquiryMaterialItem")) {
            if (!hasItem) return null;
            return "use" + itemPascal + name.substring("useInquiryMaterialItem".length());
        }
        if (name.startsWith("useInquiry")) {
            return "use" + rootPascal + name.substring("useInquiry".length());
        }

        // class files: InquiryMaterialItem* / Inquiry*
        if (name.startsWith("InquiryMaterialItem")) {
            if (!hasItem) return null;
            return itemPascal + name.substring("InquiryMaterialItem".length());
        }
        if (name.startsWith("Inquiry")) {
            return rootPascal + name.substring("Inquiry".length());
        }

        // file doesn't match any known prefix — copy as-is
        return name;
    }

    // ── content replacement ────────────────────────────────────────────────

    static String replaceContent(String content,
                                  String rootPascal, String rootCamel,
                                  String itemPascal, String itemCamel,
                                  boolean hasItem) {
        // longest tokens first to avoid partial replacement
        if (hasItem) {
            content = content.replace("InquiryMaterialItem", itemPascal);
            content = content.replace("inquiryMaterialItem", itemCamel);
        }
        content = content.replace("Inquiry", rootPascal);
        content = content.replace("inquiry", rootCamel);
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
