package com.company.IntelligentPlatform.logistics.dto;

import com.company.IntelligentPlatform.logistics.model.PurchaseRequest;
import java.time.LocalDate;

/**
 * DTO for PurchaseRequest create/update requests.
 */
public class PurchaseRequestDto {

    private String name;
    private String client;
    private double grossPrice;
    private double grossPriceDisplay;
    private String currencyCode;
    private LocalDate planExecutionDate;
    private String productionBatchNumber;
    private String note;

    public PurchaseRequest toEntity() {
        PurchaseRequest request = new PurchaseRequest();
        applyTo(request);
        return request;
    }

    public void applyTo(PurchaseRequest request) {
        if (name != null)                   request.setName(name);
        if (client != null)                 request.setClient(client);
        if (currencyCode != null)           request.setCurrencyCode(currencyCode);
        if (planExecutionDate != null)      request.setPlanExecutionDate(planExecutionDate);
        if (productionBatchNumber != null)  request.setProductionBatchNumber(productionBatchNumber);
        if (note != null)                   request.setNote(note);
        request.setGrossPrice(grossPrice);
        request.setGrossPriceDisplay(grossPriceDisplay);
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
