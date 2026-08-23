import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Copies InquiryManager.ts from src/services/logistics/ to
 * src/services/{groupId}/, renaming the file and replacing all
 * Inquiry / inquiry / InquiryMaterialItem / inquiryMaterialItem tokens.
 */
public class ServiceManagerCopier {

    static final String INTELLIGENT_UI_SERVICES =
            "/Users/I043125/work2/IntelligentUI/src/services/";

    public static void mainEntry(String rootNodeInstId,
                                 String itemNodeInstId,
                                 String groupId) throws IOException {

        String rootPascal = toPascal(rootNodeInstId);
        String rootCamel  = toCamel(rootNodeInstId);
        String itemPascal = itemNodeInstId != null ? toPascal(itemNodeInstId) : null;
        String itemCamel  = itemNodeInstId != null ? toCamel(itemNodeInstId)  : null;

        Path sourceFile = Paths.get(INTELLIGENT_UI_SERVICES, "logistics", "InquiryManager.ts");
        Path targetDir  = Paths.get(INTELLIGENT_UI_SERVICES, groupId);

        if (!Files.exists(sourceFile)) {
            throw new IllegalStateException("Source file not found: " + sourceFile);
        }
        Files.createDirectories(targetDir);

        String newFileName = rootPascal + "Manager.ts";
        Path targetFile = targetDir.resolve(newFileName);

        if (Files.exists(targetFile)) {
            System.out.println("Skipping (already exists): " + groupId + "/" + newFileName);
            return;
        }
        String content = Files.readString(sourceFile, StandardCharsets.UTF_8);
        String newContent = replaceContent(content, rootPascal, rootCamel,
                itemPascal, itemCamel, itemNodeInstId != null);

        Files.writeString(targetFile, newContent, StandardCharsets.UTF_8);
        System.out.println("Copied: InquiryManager.ts → " + groupId + "/" + newFileName);
    }

    static String replaceContent(String content,
                                  String rootPascal, String rootCamel,
                                  String itemPascal, String itemCamel,
                                  boolean hasItem) {
        if (hasItem) {
            content = content.replace("InquiryMaterialItem", itemPascal);
            content = content.replace("inquiryMaterialItem", itemCamel);
        }
        content = content.replace("Inquiry", rootPascal);
        content = content.replace("inquiry", rootCamel);
        return content;
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
