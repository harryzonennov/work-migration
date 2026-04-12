package com.company.IntelligentPlatform.logistics.dto;

import com.company.IntelligentPlatform.logistics.model.Inquiry;
import java.time.LocalDate;

/**
 * DTO for Inquiry create/update requests.
 */
public class InquiryDto {

    private String name;
    private String client;
    private double grossPrice;
    private double grossPriceDisplay;
    private String contractDetails;
    private LocalDate signDate;
    private LocalDate requireExecutionDate;
    private String currencyCode;
    private String note;

    public Inquiry toEntity() {
        Inquiry inquiry = new Inquiry();
        applyTo(inquiry);
        return inquiry;
    }

    public void applyTo(Inquiry inquiry) {
        if (name != null)                   inquiry.setName(name);
        if (client != null)                 inquiry.setClient(client);
        if (contractDetails != null)        inquiry.setContractDetails(contractDetails);
        if (signDate != null)               inquiry.setSignDate(signDate);
        if (requireExecutionDate != null)   inquiry.setRequireExecutionDate(requireExecutionDate);
        if (currencyCode != null)           inquiry.setCurrencyCode(currencyCode);
        if (note != null)                   inquiry.setNote(note);
        inquiry.setGrossPrice(grossPrice);
        inquiry.setGrossPriceDisplay(grossPriceDisplay);
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
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
