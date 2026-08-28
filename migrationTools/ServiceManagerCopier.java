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

        Path sourceFile = Paths.get(INTELLIGENT_UI_SERVICES, ref.refPath, toPascal(ref.refRootNodeInstId) + "Manager.ts");
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
        String newContent = replaceContent(content, ref, rootPascal, rootCamel,
                itemPascal, itemCamel, itemNodeInstId != null);

        Files.writeString(targetFile, newContent, StandardCharsets.UTF_8);
        System.out.println("Copied: " + toPascal(ref.refRootNodeInstId) + "Manager.ts → " + groupId + "/" + newFileName);
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
