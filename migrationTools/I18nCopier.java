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

    static final String RefDocRootNodeInstId = "inquiry";

    static final String RefDocItemNodeInstId = "inquiryMaterialItem";

    static final String RefDocPath = "supplyChain";

    static final String RefDummyDocRootNodeInstId = "material";

    static final String RefDummyDocItemNodeInstId = "materialUnit";

    static final String RefDummyDocPath = "coreFunction";

    static final String RefSERootNodeInstId = "standardMaterialUnit";

    static final String RefSEPath = "coreFunction";

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

    public static void mainEntry(CopierParams params) throws IOException {
        mainEntry(params.rootNodeInstId, params.itemNodeInstId, params.groupId, params.modelCategory);
    }

    public static void mainEntry(String rootNodeInstId,
                                 String itemNodeInstId,
                                 String groupId,
                                 int modelCategory) throws IOException {

        RefConfig ref = resolveRef(modelCategory);

        String rootPascal = toPascal(rootNodeInstId);
        String rootCamel  = toCamel(rootNodeInstId);
        String itemPascal = itemNodeInstId != null ? toPascal(itemNodeInstId) : null;
        String itemCamel  = itemNodeInstId != null ? toCamel(itemNodeInstId)  : null;

        for (String lang : LANGUAGES) {
            Path sourceFile = Paths.get(INTELLIGENT_UI_I18N, lang, ref.refPath, toPascal(ref.refRootNodeInstId) + ".json");
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
            String newContent = replaceContent(content, ref, rootPascal, rootCamel,
                    itemPascal, itemCamel, itemNodeInstId != null);

            Files.writeString(targetFile, newContent, StandardCharsets.UTF_8);
            System.out.println("Copied: " + lang + "/" + ref.refPath + "/" + toPascal(ref.refRootNodeInstId) + ".json → "
                    + lang + "/" + groupId + "/" + newFileName);
        }
    }

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

    static String toPascal(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static String toCamel(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
