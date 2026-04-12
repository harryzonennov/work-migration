package com.company.IntelligentPlatform.sales.model;

import com.company.IntelligentPlatform.common.model.DocMatItemNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinSalesDistribution - SalesReturnMaterialItem (extends DocMatItemNode)
 * Table: SalesReturnMaterialItem (schema: sales)
 *
 * Cross-module ref: refFinAccountUUID → finance schema
 */
@Entity
@Table(name = "SalesReturnMaterialItem", schema = "sales")
public class SalesReturnMaterialItem extends DocMatItemNode {

    public static final int STATUS_INITIAL           = 1;
    public static final int STATUS_DONE              = 2;
    public static final int AVAILABLE_CHECK_INITIAL  = 1;
    public static final int AVAILABLE_CHECK_OK       = 2;
    public static final int AVAILABLE_CHECK_ERROR    = 3;

    @Column(name = "refFinAccountUUID")
    private String refFinAccountUUID;

    @Column(name = "refDocItemUUID")
    private String refDocItemUUID;

    @Column(name = "refDocItemType")
    private int refDocItemType;

    public String getRefFinAccountUUID() { return refFinAccountUUID; }
    public void setRefFinAccountUUID(String refFinAccountUUID) { this.refFinAccountUUID = refFinAccountUUID; }

    public String getRefDocItemUUID() { return refDocItemUUID; }
    public void setRefDocItemUUID(String refDocItemUUID) { this.refDocItemUUID = refDocItemUUID; }

    public int getRefDocItemType() { return refDocItemType; }
    public void setRefDocItemType(int refDocItemType) { this.refDocItemType = refDocItemType; }
}
