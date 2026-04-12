package com.company.IntelligentPlatform.logistics.dto;

import com.company.IntelligentPlatform.logistics.model.PurchaseOrder;

/**
 * DTO for PurchaseOrder create/update requests.
 */
public class PurchaseOrderDto {

    private String name;
    private String client;
    private double grossNetPrice;
    private String refInWarehouseUUID;
    private String refInboundDeliveryUUID;
    private String barcode;
    private double taxRate;
    private String note;

    public PurchaseOrder toEntity() {
        PurchaseOrder order = new PurchaseOrder();
        applyTo(order);
        return order;
    }

    public void applyTo(PurchaseOrder order) {
        if (name != null)                    order.setName(name);
        if (client != null)                  order.setClient(client);
        if (refInWarehouseUUID != null)      order.setRefInWarehouseUUID(refInWarehouseUUID);
        if (refInboundDeliveryUUID != null)  order.setRefInboundDeliveryUUID(refInboundDeliveryUUID);
        if (barcode != null)                 order.setBarcode(barcode);
        if (note != null)                    order.setNote(note);
        order.setGrossNetPrice(grossNetPrice);
        order.setTaxRate(taxRate);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }
    public double getGrossNetPrice() { return grossNetPrice; }
    public void setGrossNetPrice(double grossNetPrice) { this.grossNetPrice = grossNetPrice; }
    public String getRefInWarehouseUUID() { return refInWarehouseUUID; }
    public void setRefInWarehouseUUID(String refInWarehouseUUID) { this.refInWarehouseUUID = refInWarehouseUUID; }
    public String getRefInboundDeliveryUUID() { return refInboundDeliveryUUID; }
    public void setRefInboundDeliveryUUID(String refInboundDeliveryUUID) { this.refInboundDeliveryUUID = refInboundDeliveryUUID; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
