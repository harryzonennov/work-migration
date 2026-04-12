package com.company.IntelligentPlatform.sales.dto;

import com.company.IntelligentPlatform.sales.model.SalesContract;
import java.time.LocalDate;

/**
 * DTO for SalesContract create/update requests.
 */
public class SalesContractDto {

    private String name;
    private String client;
    private double grossPrice;
    private double grossPriceDisplay;
    private String currencyCode;
    private String contractDetails;
    private LocalDate signDate;
    private LocalDate requireExecutionDate;
    private LocalDate planExecutionDate;
    private String refFinAccountUUID;
    private String productionBatchNumber;
    private String note;

    public SalesContract toEntity() {
        SalesContract contract = new SalesContract();
        applyTo(contract);
        return contract;
    }

    public void applyTo(SalesContract contract) {
        if (name != null)                   contract.setName(name);
        if (client != null)                 contract.setClient(client);
        if (currencyCode != null)           contract.setCurrencyCode(currencyCode);
        if (contractDetails != null)        contract.setContractDetails(contractDetails);
        if (signDate != null)               contract.setSignDate(signDate);
        if (requireExecutionDate != null)   contract.setRequireExecutionDate(requireExecutionDate);
        if (planExecutionDate != null)      contract.setPlanExecutionDate(planExecutionDate);
        if (refFinAccountUUID != null)      contract.setRefFinAccountUUID(refFinAccountUUID);
        if (productionBatchNumber != null)  contract.setProductionBatchNumber(productionBatchNumber);
        if (note != null)                   contract.setNote(note);
        contract.setGrossPrice(grossPrice);
        contract.setGrossPriceDisplay(grossPriceDisplay);
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
    public String getContractDetails() { return contractDetails; }
    public void setContractDetails(String contractDetails) { this.contractDetails = contractDetails; }
    public LocalDate getSignDate() { return signDate; }
    public void setSignDate(LocalDate signDate) { this.signDate = signDate; }
    public LocalDate getRequireExecutionDate() { return requireExecutionDate; }
    public void setRequireExecutionDate(LocalDate requireExecutionDate) { this.requireExecutionDate = requireExecutionDate; }
    public LocalDate getPlanExecutionDate() { return planExecutionDate; }
    public void setPlanExecutionDate(LocalDate planExecutionDate) { this.planExecutionDate = planExecutionDate; }
    public String getRefFinAccountUUID() { return refFinAccountUUID; }
    public void setRefFinAccountUUID(String refFinAccountUUID) { this.refFinAccountUUID = refFinAccountUUID; }
    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
