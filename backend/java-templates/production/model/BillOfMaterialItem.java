package com.company.IntelligentPlatform.production.model;

import com.company.IntelligentPlatform.common.model.DocMatItemNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinProduction - BillOfMaterialItem (extends DocMatItemNode)
 * Table: BillOfMaterialItem (schema: production)
 */
@Entity
@Table(name = "BillOfMaterialItem", schema = "production")
public class BillOfMaterialItem extends DocMatItemNode {

    public static final int ITEM_CATEGORY_SEMIFINISHED = 1;
    public static final int ITEM_CATEGORY_COMPONENT    = 2;
    public static final int ITEM_CATEGORY_BYPRODUCT    = 3;
    public static final int ITEM_CATEGORY_PHANTOM      = 4;

    @Column(name = "layer")
    private int layer;

    @Column(name = "refParentItemUUID")
    private String refParentItemUUID;

    @Column(name = "itemCategory")
    private int itemCategory;

    @Column(name = "leadTimeOffset")
    private double leadTimeOffset;

    @Column(name = "theoLossRate")
    private double theoLossRate;

    @Column(name = "refSubBOMUUID")
    private String refSubBOMUUID;

    @Column(name = "refRouteProcessItemUUID")
    private String refRouteProcessItemUUID;

    @Column(name = "refWocUUID")
    private String refWocUUID;

    public int getLayer() { return layer; }
    public void setLayer(int layer) { this.layer = layer; }
    public String getRefParentItemUUID() { return refParentItemUUID; }
    public void setRefParentItemUUID(String refParentItemUUID) { this.refParentItemUUID = refParentItemUUID; }
    public int getItemCategory() { return itemCategory; }
    public void setItemCategory(int itemCategory) { this.itemCategory = itemCategory; }
    public double getLeadTimeOffset() { return leadTimeOffset; }
    public void setLeadTimeOffset(double leadTimeOffset) { this.leadTimeOffset = leadTimeOffset; }
    public double getTheoLossRate() { return theoLossRate; }
    public void setTheoLossRate(double theoLossRate) { this.theoLossRate = theoLossRate; }
    public String getRefSubBOMUUID() { return refSubBOMUUID; }
    public void setRefSubBOMUUID(String refSubBOMUUID) { this.refSubBOMUUID = refSubBOMUUID; }
    public String getRefRouteProcessItemUUID() { return refRouteProcessItemUUID; }
    public void setRefRouteProcessItemUUID(String refRouteProcessItemUUID) { this.refRouteProcessItemUUID = refRouteProcessItemUUID; }
    public String getRefWocUUID() { return refWocUUID; }
    public void setRefWocUUID(String refWocUUID) { this.refWocUUID = refWocUUID; }
}
