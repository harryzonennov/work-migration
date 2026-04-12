package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.DocumentContent;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinLogistics - PurchaseReturnOrder (extends DocumentContent)
 * Table: PurchaseReturnOrder (schema: logistics)
 */
@Entity
@Table(name = "PurchaseReturnOrder", schema = "logistics")
public class PurchaseReturnOrder extends DocumentContent {

    public static final int STATUS_INITIAL        = 1;
    public static final int STATUS_SUBMITTED      = 2;
    public static final int STATUS_APPROVED       = 3;
    public static final int STATUS_DELIVERYDONE   = 200;
    public static final int STATUS_PROCESSDONE    = 100;
    public static final int STATUS_REJECT_APPROVAL = 305;
    public static final int STATUS_ARCHIVED       = 400;

    @Column(name = "grossPrice")
    private double grossPrice;

    @Column(name = "grossPriceDisplay")
    private double grossPriceDisplay;

    @Column(name = "taxRate")
    private double taxRate;

    @Column(name = "productionBatchNumber")
    private String productionBatchNumber;

    public double getGrossPrice() { return grossPrice; }
    public void setGrossPrice(double grossPrice) { this.grossPrice = grossPrice; }

    public double getGrossPriceDisplay() { return grossPriceDisplay; }
    public void setGrossPriceDisplay(double grossPriceDisplay) { this.grossPriceDisplay = grossPriceDisplay; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }

    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }
}
