package com.company.IntelligentPlatform.sales.model;

import com.company.IntelligentPlatform.common.model.ServiceEntityNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinSalesDistribution - SalesArea (extends ServiceEntityNode)
 * Table: SalesArea (schema: sales)
 *
 * Hierarchical sales territory structure (up to 4 levels).
 */
@Entity
@Table(name = "SalesArea", schema = "sales")
public class SalesArea extends ServiceEntityNode {

    public static final int LEVEL1 = 1;
    public static final int LEVEL2 = 2;
    public static final int LEVEL3 = 3;
    public static final int LEVEL4 = 4;

    public static final int CONVTYPE_STANDALONE         = 1;
    public static final int CONVTYPE_COMBINE_FROMROOT   = 2;

    @Column(name = "parentAreaUUID")
    private String parentAreaUUID;

    @Column(name = "rootAreaUUID")
    private String rootAreaUUID;

    @Column(name = "level")
    private int level;

    @Column(name = "convLabelType")
    private int convLabelType;

    public String getParentAreaUUID() { return parentAreaUUID; }
    public void setParentAreaUUID(String parentAreaUUID) { this.parentAreaUUID = parentAreaUUID; }

    public String getRootAreaUUID() { return rootAreaUUID; }
    public void setRootAreaUUID(String rootAreaUUID) { this.rootAreaUUID = rootAreaUUID; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getConvLabelType() { return convLabelType; }
    public void setConvLabelType(int convLabelType) { this.convLabelType = convLabelType; }
}
