package com.company.IntelligentPlatform.sales.dto;

import com.company.IntelligentPlatform.sales.model.SalesReturnOrder;

/**
 * DTO for SalesReturnOrder create/update requests.
 */
public class SalesReturnOrderDto {

    private String name;
    private String client;
    private double grossPrice;
    private double grossPriceDisplay;
    private String refInWarehouseUUID;
    private String refInboundDeliveryUUID;
    private String barcode;
    private double taxRate;
    private String productionBatchNumber;
    private String note;

    public SalesReturnOrder toEntity() {
        SalesReturnOrder returnOrder = new SalesReturnOrder();
        applyTo(returnOrder);
        return returnOrder;
    }

    public void applyTo(SalesReturnOrder returnOrder) {
        if (name != null)                    returnOrder.setName(name);
        if (client != null)                  returnOrder.setClient(client);
        if (refInWarehouseUUID != null)      returnOrder.setRefInWarehouseUUID(refInWarehouseUUID);
        if (refInboundDeliveryUUID != null)  returnOrder.setRefInboundDeliveryUUID(refInboundDeliveryUUID);
        if (barcode != null)                 returnOrder.setBarcode(barcode);
        if (productionBatchNumber != null)   returnOrder.setProductionBatchNumber(productionBatchNumber);
        if (note != null)                    returnOrder.setNote(note);
        returnOrder.setGrossPrice(grossPrice);
        returnOrder.setGrossPriceDisplay(grossPriceDisplay);
        returnOrder.setTaxRate(taxRate);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }
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
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
