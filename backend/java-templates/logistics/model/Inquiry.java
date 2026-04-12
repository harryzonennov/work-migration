package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.DocumentContent;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Migrated from: ThorsteinLogistics - Inquiry (extends DocumentContent)
 * Table: Inquiry (schema: logistics)
 */
@Entity
@Table(name = "Inquiry", schema = "logistics")
public class Inquiry extends DocumentContent {

    public static final int STATUS_INIT      = 1;
    public static final int STATUS_INPROCESS = 2;
    public static final int STATUS_SUCCESS   = 3;
    public static final int STATUS_FAILURE   = 4;

    @Column(name = "grossPrice")
    private double grossPrice;

    @Column(name = "grossPriceDisplay")
    private double grossPriceDisplay;

    @Column(name = "contractDetails", length = 800)
    private String contractDetails;

    @Column(name = "signDate")
    private LocalDate signDate;

    @Column(name = "requireExecutionDate")
    private LocalDate requireExecutionDate;

    @Column(name = "currencyCode")
    private String currencyCode;

    public double getGrossPrice() { return grossPrice; }
    public void setGrossPrice(double grossPrice) { this.grossPrice = grossPrice; }

    public double getGrossPriceDisplay() { return grossPriceDisplay; }
    public void setGrossPriceDisplay(double grossPriceDisplay) { this.grossPriceDisplay = grossPriceDisplay; }

    public String getContractDetails() { return contractDetails; }
    public void setContractDetails(String contractDetails) { this.contractDetails = contractDetails; }

    public LocalDate getSignDate() { return signDate; }
    public void setSignDate(LocalDate signDate) { this.signDate = signDate; }

    public LocalDate getRequireExecutionDate() { return requireExecutionDate; }
    public void setRequireExecutionDate(LocalDate requireExecutionDate) { this.requireExecutionDate = requireExecutionDate; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
}
