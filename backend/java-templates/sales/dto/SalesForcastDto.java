package com.company.IntelligentPlatform.sales.dto;

import com.company.IntelligentPlatform.sales.model.SalesForcast;
import java.time.LocalDate;

/**
 * DTO for SalesForcast create/update requests.
 */
public class SalesForcastDto {

    private String name;
    private String client;
    private double grossPrice;
    private double grossPriceDisplay;
    private String currencyCode;
    private LocalDate planExecutionDate;
    private String productionBatchNumber;
    private String note;

    public SalesForcast toEntity() {
        SalesForcast forcast = new SalesForcast();
        applyTo(forcast);
        return forcast;
    }

    public void applyTo(SalesForcast forcast) {
        if (name != null)                   forcast.setName(name);
        if (client != null)                 forcast.setClient(client);
        if (currencyCode != null)           forcast.setCurrencyCode(currencyCode);
        if (planExecutionDate != null)      forcast.setPlanExecutionDate(planExecutionDate);
        if (productionBatchNumber != null)  forcast.setProductionBatchNumber(productionBatchNumber);
        if (note != null)                   forcast.setNote(note);
        forcast.setGrossPrice(grossPrice);
        forcast.setGrossPriceDisplay(grossPriceDisplay);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }
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
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
