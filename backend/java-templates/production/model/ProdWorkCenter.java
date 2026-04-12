package com.company.IntelligentPlatform.production.model;

import com.company.IntelligentPlatform.common.model.ServiceEntityNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinProduction - ProdWorkCenter (extends Organization/ServiceEntityNode)
 * Table: ProdWorkCenter (schema: production)
 *
 * Note: In the original, ProdWorkCenter extended Organization (a platform entity).
 * In the unified project it extends ServiceEntityNode directly to avoid cross-schema inheritance.
 */
@Entity
@Table(name = "ProdWorkCenter", schema = "production")
public class ProdWorkCenter extends ServiceEntityNode {

    public static final int CAPACITY_CAL_TYPE_MACHINE  = 1;
    public static final int CAPACITY_CAL_TYPE_PERSON   = 2;
    public static final int CAPACITY_CAL_TYPE_COMBINED = 3;

    @Column(name = "keyWorkCenterFlag")
    private int keyWorkCenterFlag;

    @Column(name = "refCostCenterUUID")
    private String refCostCenterUUID;

    @Column(name = "usageNote", length = 1000)
    private String usageNote;

    @Column(name = "refGroupUUID")
    private String refGroupUUID;

    @Column(name = "capacityCalculateType")
    private int capacityCalculateType;

    public int getKeyWorkCenterFlag() { return keyWorkCenterFlag; }
    public void setKeyWorkCenterFlag(int keyWorkCenterFlag) { this.keyWorkCenterFlag = keyWorkCenterFlag; }
    public String getRefCostCenterUUID() { return refCostCenterUUID; }
    public void setRefCostCenterUUID(String refCostCenterUUID) { this.refCostCenterUUID = refCostCenterUUID; }
    public String getUsageNote() { return usageNote; }
    public void setUsageNote(String usageNote) { this.usageNote = usageNote; }
    public String getRefGroupUUID() { return refGroupUUID; }
    public void setRefGroupUUID(String refGroupUUID) { this.refGroupUUID = refGroupUUID; }
    public int getCapacityCalculateType() { return capacityCalculateType; }
    public void setCapacityCalculateType(int capacityCalculateType) { this.capacityCalculateType = capacityCalculateType; }
}
