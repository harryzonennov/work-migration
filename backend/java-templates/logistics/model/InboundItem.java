package com.company.IntelligentPlatform.logistics.model;

import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinLogistics - InboundItem (extends DeliveryItem)
 * Table: InboundItem (schema: logistics)
 *
 * Cross-module ref: refStoreItemUUID → WarehouseStoreItem (same module)
 */
@Entity
@Table(name = "InboundItem", schema = "logistics")
public class InboundItem extends DeliveryItem {

    @Column(name = "inboundFee")
    private double inboundFee;

    @Column(name = "refStoreItemUUID")
    private String refStoreItemUUID;

    public double getInboundFee() { return inboundFee; }
    public void setInboundFee(double inboundFee) { this.inboundFee = inboundFee; }

    public String getRefStoreItemUUID() { return refStoreItemUUID; }
    public void setRefStoreItemUUID(String refStoreItemUUID) { this.refStoreItemUUID = refStoreItemUUID; }
}
