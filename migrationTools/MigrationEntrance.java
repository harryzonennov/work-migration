/**
 * Entry point for all migration copier tools.
 * Edit the parameters here and run via run.sh.
 */
public class MigrationEntrance {

    public static int MODEL_CAT_DOCUMENT = 1;
    public static int MODEL_CAT_DUMMY_DOCUMENT = 2;
    public static int MODEL_CAT_SER_ENTITY = 3;


    public static void main(String[] args) throws Exception {
        String rootNodeInstId = "outboundDelivery";
        String itemNodeInstId = "outboundItem";
        String groupId        = "logistics";

        System.out.println("=== PageModuleCopier ===");
        PageModuleCopier.mainEntry(rootNodeInstId, itemNodeInstId, groupId, MODEL_CAT_DOCUMENT);

        System.out.println("\n=== ServiceManagerCopier ===");
        ServiceManagerCopier.mainEntry(rootNodeInstId, itemNodeInstId, groupId, MODEL_CAT_DOCUMENT);

        System.out.println("\n=== I18nCopier ===");
        I18nCopier.mainEntry(rootNodeInstId, itemNodeInstId, groupId, MODEL_CAT_DOCUMENT);

        System.out.println("\nAll done.");
    }
}
