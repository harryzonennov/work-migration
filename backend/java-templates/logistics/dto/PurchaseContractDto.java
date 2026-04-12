package com.company.IntelligentPlatform.logistics.dto;

import com.company.IntelligentPlatform.logistics.model.PurchaseContract;
import java.time.LocalDate;

/**
 * DTO for PurchaseContract create/update requests.
 */
public class PurchaseContractDto {

    private String name;
    private String client;
    private double grossPrice;
    private double grossPriceDisplay;
    private String contractDetails;
    private LocalDate signDate;
    private LocalDate requireExecutionDate;
    private String currencyCode;
    private String refFinAccountUUID;
    private String purchaseBatchNumber;
    private String productionBatchNumber;
    private String note;

    public PurchaseContract toEntity() {
        PurchaseContract contract = new PurchaseContract();
        applyTo(contract);
        return contract;
    }

    public void applyTo(PurchaseContract contract) {
        if (name != null)                   contract.setName(name);
        if (client != null)                 contract.setClient(client);
        if (contractDetails != null)        contract.setContractDetails(contractDetails);
        if (signDate != null)               contract.setSignDate(signDate);
        if (requireExecutionDate != null)   contract.setRequireExecutionDate(requireExecutionDate);
        if (currencyCode != null)           contract.setCurrencyCode(currencyCode);
        if (refFinAccountUUID != null)      contract.setRefFinAccountUUID(refFinAccountUUID);
        if (purchaseBatchNumber != null)    contract.setPurchaseBatchNumber(purchaseBatchNumber);
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
    public String getContractDetails() { return contractDetails; }
    public void setContractDetails(String contractDetails) { this.contractDetails = contractDetails; }
    public LocalDate getSignDate() { return signDate; }
    public void setSignDate(LocalDate signDate) { this.signDate = signDate; }
    public LocalDate getRequireExecutionDate() { return requireExecutionDate; }
    public void setRequireExecutionDate(LocalDate requireExecutionDate) { this.requireExecutionDate = requireExecutionDate; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getRefFinAccountUUID() { return refFinAccountUUID; }
    public void setRefFinAccountUUID(String refFinAccountUUID) { this.refFinAccountUUID = refFinAccountUUID; }
    public String getPurchaseBatchNumber() { return purchaseBatchNumber; }
    public void setPurchaseBatchNumber(String purchaseBatchNumber) { this.purchaseBatchNumber = purchaseBatchNumber; }
    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
