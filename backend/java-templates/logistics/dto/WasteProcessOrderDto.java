package com.company.IntelligentPlatform.logistics.dto;

import com.company.IntelligentPlatform.logistics.model.WasteProcessOrder;

/**
 * DTO for WasteProcessOrder create/update requests.
 */
public class WasteProcessOrderDto {

    private String name;
    private String client;
    private double grossPrice;
    private double grossPriceDisplay;
    private String currencyCode;
    private int processType;
    private String note;

    public WasteProcessOrder toEntity() {
        WasteProcessOrder order = new WasteProcessOrder();
        applyTo(order);
        return order;
    }

    public void applyTo(WasteProcessOrder order) {
        if (name != null)         order.setName(name);
        if (client != null)       order.setClient(client);
        if (currencyCode != null) order.setCurrencyCode(currencyCode);
        if (note != null)         order.setNote(note);
        order.setGrossPrice(grossPrice);
        order.setGrossPriceDisplay(grossPriceDisplay);
        order.setProcessType(processType);
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
    public int getProcessType() { return processType; }
    public void setProcessType(int processType) { this.processType = processType; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
