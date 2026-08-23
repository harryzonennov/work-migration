import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Copies Inquiry.json from src/i18n/locales/{lang}/supplyChain/ to
 * src/i18n/locales/{lang}/{groupId}/, renaming the file to
 * {RootNodeInstId}.json and replacing all
 * Inquiry / inquiry / InquiryMaterialItem / inquiryMaterialItem tokens.
 *
 * Languages copied: en, zh
 */
public class I18nCopier {

    static final String INTELLIGENT_UI_I18N =
            "/Users/I043125/work2/IntelligentUI/src/i18n/locales/";

    static final String[] LANGUAGES = {"en", "zh"};

    public static void mainEntry(String rootNodeInstId,
                                 String itemNodeInstId,
                                 String groupId) throws IOException {

        String rootPascal = toPascal(rootNodeInstId);
        String rootCamel  = toCamel(rootNodeInstId);
        String itemPascal = itemNodeInstId != null ? toPascal(itemNodeInstId) : null;
        String itemCamel  = itemNodeInstId != null ? toCamel(itemNodeInstId)  : null;

        for (String lang : LANGUAGES) {
            Path sourceFile = Paths.get(INTELLIGENT_UI_I18N, lang, "supplyChain", "Inquiry.json");
            Path targetDir  = Paths.get(INTELLIGENT_UI_I18N, lang, groupId);

            if (!Files.exists(sourceFile)) {
                System.out.println("Skipping (not found): " + sourceFile);
                continue;
            }
            Files.createDirectories(targetDir);

            String newFileName = rootPascal + ".json";
            Path targetFile = targetDir.resolve(newFileName);

            if (Files.exists(targetFile)) {
                System.out.println("Skipping (already exists): " + lang + "/" + groupId + "/" + newFileName);
                continue;
            }
            String content = Files.readString(sourceFile, StandardCharsets.UTF_8);
            String newContent = replaceContent(content, rootPascal, rootCamel,
                    itemPascal, itemCamel, itemNodeInstId != null);

            Files.writeString(targetFile, newContent, StandardCharsets.UTF_8);
            System.out.println("Copied: " + lang + "/supplyChain/Inquiry.json → "
                    + lang + "/" + groupId + "/" + newFileName);
        }
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
