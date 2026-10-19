/**
 * Bundles all parameters passed to each ***Copier.mainEntry method.
 */
public class CopierParams {

    public final String rootNodeInstId;
    public final String itemNodeInstId;
    /** Optional 3rd-level sub-item node instance id (e.g. "qualityInspectPropertyItem"). Null if no sub-item. */
    public final String subItemNodeInstId;
    public final String groupId;
    public final int    modelCategory;
    public final String legacyUIProjectUrl;
    public final String legacyRootNodeListControllerUrl;

    /** Full constructor including optional sub-item tier. */
    public CopierParams(String rootNodeInstId,
                        String itemNodeInstId,
                        String subItemNodeInstId,
                        String groupId,
                        int    modelCategory,
                        String legacyUIProjectUrl,
                        String legacyRootNodeListControllerUrl) {
        this.rootNodeInstId                   = rootNodeInstId;
        this.itemNodeInstId                   = itemNodeInstId;
        this.subItemNodeInstId                = subItemNodeInstId;
        this.groupId                          = groupId;
        this.modelCategory                    = modelCategory;
        this.legacyUIProjectUrl               = legacyUIProjectUrl;
        this.legacyRootNodeListControllerUrl  = legacyRootNodeListControllerUrl;
    }

    /** Backward-compatible constructor — no sub-item tier. */
    public CopierParams(String rootNodeInstId,
                        String itemNodeInstId,
                        String groupId,
                        int    modelCategory,
                        String legacyUIProjectUrl,
                        String legacyRootNodeListControllerUrl) {
        this(rootNodeInstId, itemNodeInstId, null, groupId, modelCategory,
             legacyUIProjectUrl, legacyRootNodeListControllerUrl);
    }
}
