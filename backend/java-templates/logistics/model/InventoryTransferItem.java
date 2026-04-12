package com.company.IntelligentPlatform.logistics.model;

import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinLogistics - InventoryTransferItem (extends DeliveryItem)
 * Table: InventoryTransferItem (schema: logistics)
 */
@Entity
@Table(name = "InventoryTransferItem", schema = "logistics")
public class InventoryTransferItem extends DeliveryItem {

    @Column(name = "outboundFee")
    private double outboundFee;

    @Column(name = "storageFee")
    private double storageFee;

    @Column(name = "refOutboundItemUUID")
    private String refOutboundItemUUID;

    @Column(name = "refInboundItemUUID")
    private String refInboundItemUUID;

    @Column(name = "refStoreItemUUID")
    private String refStoreItemUUID;

    @Column(name = "itemStatus")
    private int itemStatus;

    @Column(name = "storeDay")
    private int storeDay;

    public double getOutboundFee() { return outboundFee; }
    public void setOutboundFee(double outboundFee) { this.outboundFee = outboundFee; }

    public double getStorageFee() { return storageFee; }
    public void setStorageFee(double storageFee) { this.storageFee = storageFee; }

    public String getRefOutboundItemUUID() { return refOutboundItemUUID; }
    public void setRefOutboundItemUUID(String refOutboundItemUUID) { this.refOutboundItemUUID = refOutboundItemUUID; }

    public String getRefInboundItemUUID() { return refInboundItemUUID; }
    public void setRefInboundItemUUID(String refInboundItemUUID) { this.refInboundItemUUID = refInboundItemUUID; }

    public String getRefStoreItemUUID() { return refStoreItemUUID; }
    public void setRefStoreItemUUID(String refStoreItemUUID) { this.refStoreItemUUID = refStoreItemUUID; }

    public int getItemStatus() { return itemStatus; }
    public void setItemStatus(int itemStatus) { this.itemStatus = itemStatus; }

    public int getStoreDay() { return storeDay; }
    public void setStoreDay(int storeDay) { this.storeDay = storeDay; }
}
