package com.company.IntelligentPlatform.logistics.model;

import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinLogistics - InventoryTransferOrder (extends Delivery)
 * Table: InventoryTransferOrder (schema: logistics)
 *
 * Cross-module refs (stored as UUID String, no FK):
 *   refInboundWarehouseUUID  → Warehouse
 *   refInboundWarehouseAreaUUID → Warehouse area
 *   refInboundDeliveryUUID → InboundDelivery (same module)
 *   refOutboundDeliveryUUID → OutboundDelivery (same module)
 */
@Entity
@Table(name = "InventoryTransferOrder", schema = "logistics")
public class InventoryTransferOrder extends Delivery {

    @Column(name = "grossOutboundFee")
    private double grossOutboundFee;

    @Column(name = "grossStorageFee")
    private double grossStorageFee;

    @Column(name = "refInboundWarehouseUUID")
    private String refInboundWarehouseUUID;

    @Column(name = "refInboundWarehouseAreaUUID")
    private String refInboundWarehouseAreaUUID;

    @Column(name = "refInboundDeliveryUUID")
    private String refInboundDeliveryUUID;

    @Column(name = "refOutboundDeliveryUUID")
    private String refOutboundDeliveryUUID;

    public double getGrossOutboundFee() { return grossOutboundFee; }
    public void setGrossOutboundFee(double grossOutboundFee) { this.grossOutboundFee = grossOutboundFee; }

    public double getGrossStorageFee() { return grossStorageFee; }
    public void setGrossStorageFee(double grossStorageFee) { this.grossStorageFee = grossStorageFee; }

    public String getRefInboundWarehouseUUID() { return refInboundWarehouseUUID; }
    public void setRefInboundWarehouseUUID(String refInboundWarehouseUUID) { this.refInboundWarehouseUUID = refInboundWarehouseUUID; }

    public String getRefInboundWarehouseAreaUUID() { return refInboundWarehouseAreaUUID; }
    public void setRefInboundWarehouseAreaUUID(String refInboundWarehouseAreaUUID) { this.refInboundWarehouseAreaUUID = refInboundWarehouseAreaUUID; }

    public String getRefInboundDeliveryUUID() { return refInboundDeliveryUUID; }
    public void setRefInboundDeliveryUUID(String refInboundDeliveryUUID) { this.refInboundDeliveryUUID = refInboundDeliveryUUID; }

    public String getRefOutboundDeliveryUUID() { return refOutboundDeliveryUUID; }
    public void setRefOutboundDeliveryUUID(String refOutboundDeliveryUUID) { this.refOutboundDeliveryUUID = refOutboundDeliveryUUID; }
}
