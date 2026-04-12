package com.company.IntelligentPlatform.sales.model;

import com.company.IntelligentPlatform.common.model.DocumentContent;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Migrated from: ThorsteinSalesDistribution - SalesForcast (extends DocumentContent)
 * Table: SalesForcast (schema: sales)
 *
 * Note: name preserved as "Forcast" (original typo in source).
 */
@Entity
@Table(name = "SalesForcast", schema = "sales")
public class SalesForcast extends DocumentContent {

    public static final int STATUS_INITIAL        = 1;
    public static final int STATUS_SUBMITTED      = 2;
    public static final int STATUS_APPROVED       = 3;
    public static final int STATUS_INPLAN         = 4;
    public static final int STATUS_DONE           = 5;
    public static final int STATUS_REJECT_APPROVAL = 305;
    public static final int STATUS_ARCHIVED       = 400;

    @Column(name = "grossPrice")
    private double grossPrice;

    @Column(name = "grossPriceDisplay")
    private double grossPriceDisplay;

    @Column(name = "currencyCode")
    private String currencyCode;

    @Column(name = "planExecutionDate")
    private LocalDate planExecutionDate;

    @Column(name = "productionBatchNumber")
    private String productionBatchNumber;

    public double getGrossPrice() { return grossPrice; }
    public void setGrossPrice(double grossPrice) { this.grossPrice = grossPrice; }

    public double getGrossPriceDisplay() { return grossPriceDisplay; }
    public void setGrossPriceDisplay(double grossPriceDisplay) { this.grossPriceDisplay = grossPriceDisplay; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public LocalDate getPlanExecutionDate() { return planExecutionDate; }
    public void setPlanExecutionDate(LocalDate planExecutionDate) { this.planExecutionDate = planExecutionDate; }

    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }
}
