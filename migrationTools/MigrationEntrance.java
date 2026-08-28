/**
 * Entry point for all migration copier tools.
 * Edit the parameters here and run via run.sh.
 */
public class MigrationEntrance {

    public static int MODEL_CAT_DOCUMENT = 1;
    public static int MODEL_CAT_DUMMY_DOCUMENT = 2;
    public static int MODEL_CAT_SER_ENTITY = 3;


    public static void main(String[] args) throws Exception {
        CopierParams params = new CopierParams(
                "purchaseReturnOrder",
                "outboundItem",
                "logistics",
                MODEL_CAT_DOCUMENT,
                "/Users/I043125/work/ThorSalesDistributionUI/admin/",
                "js/supplyChain/PurchaseReturnOrderList.js"
        );

        System.out.println("=== PageModuleCopier ===");
        PageModuleCopier.mainEntry(params);

        System.out.println("\n=== ServiceManagerCopier ===");
        ServiceManagerCopier.mainEntry(params);

        System.out.println("\n=== I18nCopier ===");
        I18nCopier.mainEntry(params);

        System.out.println("\nAll done.");
    }
}
