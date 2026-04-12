package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.DocumentContent;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinLogistics - PurchaseOrder (extends DocumentContent)
 * Table: PurchaseOrder (schema: logistics)
 *
 * Cross-module refs (stored as UUID String, no FK):
 *   refInWarehouseUUID → Warehouse
 *   refInboundDeliveryUUID → InboundDelivery (same module)
 */
@Entity
@Table(name = "PurchaseOrder", schema = "logistics")
public class PurchaseOrder extends DocumentContent {

    public static final int STATUS_INITIAL   = 1;
    public static final int STATUS_INSETTLE  = 2;
    public static final int STATUS_INDELIVERY = 3;
    public static final int STATUS_FINISHED  = 4;
    public static final int STATUS_CANCELED  = 5;

    @Column(name = "grossNetPrice")
    private double grossNetPrice;

    @Column(name = "refInWarehouseUUID")
    private String refInWarehouseUUID;

    @Column(name = "refInboundDeliveryUUID")
    private String refInboundDeliveryUUID;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "taxRate")
    private double taxRate;

    public double getGrossNetPrice() { return grossNetPrice; }
    public void setGrossNetPrice(double grossNetPrice) { this.grossNetPrice = grossNetPrice; }

    public String getRefInWarehouseUUID() { return refInWarehouseUUID; }
    public void setRefInWarehouseUUID(String refInWarehouseUUID) { this.refInWarehouseUUID = refInWarehouseUUID; }

    public String getRefInboundDeliveryUUID() { return refInboundDeliveryUUID; }
    public void setRefInboundDeliveryUUID(String refInboundDeliveryUUID) { this.refInboundDeliveryUUID = refInboundDeliveryUUID; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }
}
