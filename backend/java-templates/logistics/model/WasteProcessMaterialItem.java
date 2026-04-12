package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.DocMatItemNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinLogistics - WasteProcessMaterialItem (extends DocMatItemNode)
 * Table: WasteProcessMaterialItem (schema: logistics)
 */
@Entity
@Table(name = "WasteProcessMaterialItem", schema = "logistics")
public class WasteProcessMaterialItem extends DocMatItemNode {

    @Column(name = "storeCheckStatus")
    private int storeCheckStatus;

    @Column(name = "refStoreItemUUID")
    private String refStoreItemUUID;

    public int getStoreCheckStatus() { return storeCheckStatus; }
    public void setStoreCheckStatus(int storeCheckStatus) { this.storeCheckStatus = storeCheckStatus; }

    public String getRefStoreItemUUID() { return refStoreItemUUID; }
    public void setRefStoreItemUUID(String refStoreItemUUID) { this.refStoreItemUUID = refStoreItemUUID; }
}
