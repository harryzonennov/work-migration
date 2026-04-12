package com.company.IntelligentPlatform.logistics.model;

import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinLogistics - OutboundItem (extends DeliveryItem)
 * Table: OutboundItem (schema: logistics)
 *
 * Cross-module ref: refStoreItemUUID → WarehouseStoreItem (same module)
 */
@Entity
@Table(name = "OutboundItem", schema = "logistics")
public class OutboundItem extends DeliveryItem {

    @Column(name = "outboundFee")
    private double outboundFee;

    @Column(name = "storageFee")
    private double storageFee;

    @Column(name = "refStoreItemUUID")
    private String refStoreItemUUID;

    @Column(name = "storeDay")
    private int storeDay;

    public double getOutboundFee() { return outboundFee; }
    public void setOutboundFee(double outboundFee) { this.outboundFee = outboundFee; }

    public double getStorageFee() { return storageFee; }
    public void setStorageFee(double storageFee) { this.storageFee = storageFee; }

    public String getRefStoreItemUUID() { return refStoreItemUUID; }
    public void setRefStoreItemUUID(String refStoreItemUUID) { this.refStoreItemUUID = refStoreItemUUID; }

    public int getStoreDay() { return storeDay; }
    public void setStoreDay(int storeDay) { this.storeDay = storeDay; }
}
