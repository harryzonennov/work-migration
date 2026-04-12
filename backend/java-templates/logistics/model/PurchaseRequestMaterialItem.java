package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.DocMatItemNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinLogistics - PurchaseRequestMaterialItem (extends DocMatItemNode)
 * Table: PurchaseRequestMaterialItem (schema: logistics)
 */
@Entity
@Table(name = "PurchaseRequestMaterialItem", schema = "logistics")
public class PurchaseRequestMaterialItem extends DocMatItemNode {

    @Column(name = "itemStatus")
    private int itemStatus;

    public int getItemStatus() { return itemStatus; }
    public void setItemStatus(int itemStatus) { this.itemStatus = itemStatus; }
}
