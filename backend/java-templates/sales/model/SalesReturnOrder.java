package com.company.IntelligentPlatform.sales.model;

import com.company.IntelligentPlatform.common.model.DocumentContent;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinSalesDistribution - SalesReturnOrder (extends DocumentContent)
 * Table: SalesReturnOrder (schema: sales)
 *
 * Cross-module refs:
 *   refInWarehouseUUID    → logistics/Warehouse
 *   refInboundDeliveryUUID → logistics/InboundDelivery
 */
@Entity
@Table(name = "SalesReturnOrder", schema = "sales")
public class SalesReturnOrder extends DocumentContent {

    public static final int STATUS_INITIAL        = 1;
    public static final int STATUS_SUBMITTED      = 2;
    public static final int STATUS_APPROVED       = 3;
    public static final int STATUS_INDELIVERY     = 4;
    public static final int STATUS_DELIVERY_DONE  = 5;
    public static final int STATUS_PROCESS_DONE   = 6;
    public static final int STATUS_REJECT_APPROVAL = 305;
    public static final int STATUS_ARCHIVED       = 400;

    @Column(name = "grossPrice")
    private double grossPrice;

    @Column(name = "grossPriceDisplay")
    private double grossPriceDisplay;

    @Column(name = "refInWarehouseUUID")
    private String refInWarehouseUUID;

    @Column(name = "refInboundDeliveryUUID")
    private String refInboundDeliveryUUID;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "taxRate")
    private double taxRate;

    @Column(name = "productionBatchNumber")
    private String productionBatchNumber;

    public double getGrossPrice() { return grossPrice; }
    public void setGrossPrice(double grossPrice) { this.grossPrice = grossPrice; }

    public double getGrossPriceDisplay() { return grossPriceDisplay; }
    public void setGrossPriceDisplay(double grossPriceDisplay) { this.grossPriceDisplay = grossPriceDisplay; }

    public String getRefInWarehouseUUID() { return refInWarehouseUUID; }
    public void setRefInWarehouseUUID(String refInWarehouseUUID) { this.refInWarehouseUUID = refInWarehouseUUID; }

    public String getRefInboundDeliveryUUID() { return refInboundDeliveryUUID; }
    public void setRefInboundDeliveryUUID(String refInboundDeliveryUUID) { this.refInboundDeliveryUUID = refInboundDeliveryUUID; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }

    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }
}
