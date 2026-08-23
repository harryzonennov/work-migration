/**
 * Entry point for all migration copier tools.
 * Edit the parameters here and run via run.sh.
 */
public class MigrationEntrance {

    public static void main(String[] args) throws Exception {
        String rootNodeInstId = "outboundDelivery";
        String itemNodeInstId = "outboundItem";
        String groupId        = "logistics";

        System.out.println("=== PageModuleCopier ===");
        PageModuleCopier.mainEntry(rootNodeInstId, itemNodeInstId, groupId);

        System.out.println("\n=== ServiceManagerCopier ===");
        ServiceManagerCopier.mainEntry(rootNodeInstId, itemNodeInstId, groupId);

        System.out.println("\n=== I18nCopier ===");
        I18nCopier.mainEntry(rootNodeInstId, itemNodeInstId, groupId);

        System.out.println("\nAll done.");
    }
}
