/**
 * Bundles all parameters passed to each ***Copier.mainEntry method.
 */
public class CopierParams {

    public final String rootNodeInstId;
    public final String itemNodeInstId;
    public final String groupId;
    public final int    modelCategory;
    public final String legacyUIProjectUrl;
    public final String legacyRootNodeListControllerUrl;

    public CopierParams(String rootNodeInstId,
                        String itemNodeInstId,
                        String groupId,
                        int    modelCategory,
                        String legacyUIProjectUrl,
                        String legacyRootNodeListControllerUrl) {
        this.rootNodeInstId                   = rootNodeInstId;
        this.itemNodeInstId                   = itemNodeInstId;
        this.groupId                          = groupId;
        this.modelCategory                    = modelCategory;
        this.legacyUIProjectUrl               = legacyUIProjectUrl;
        this.legacyRootNodeListControllerUrl  = legacyRootNodeListControllerUrl;
    }
}
