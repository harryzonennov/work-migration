package com.company.IntelligentPlatform.logistics.dto;

import com.company.IntelligentPlatform.logistics.model.InboundDelivery;
import java.time.LocalDateTime;

/**
 * DTO for InboundDelivery create/update requests.
 */
public class InboundDeliveryDto {

    private String name;
    private String client;
    private String refWarehouseUUID;
    private String refWarehouseAreaUUID;
    private double grossPrice;
    private LocalDateTime shippingTime;
    private String shippingPoint;
    private int freightChargeType;
    private double freightCharge;
    private int planCategory;
    private LocalDateTime planExecuteDate;
    private String productionBatchNumber;
    private String purchaseBatchNumber;
    private double grossInboundFee;
    private String note;

    public InboundDelivery toEntity() {
        InboundDelivery delivery = new InboundDelivery();
        applyTo(delivery);
        return delivery;
    }

    public void applyTo(InboundDelivery delivery) {
        if (name != null)                   delivery.setName(name);
        if (client != null)                 delivery.setClient(client);
        if (refWarehouseUUID != null)        delivery.setRefWarehouseUUID(refWarehouseUUID);
        if (refWarehouseAreaUUID != null)    delivery.setRefWarehouseAreaUUID(refWarehouseAreaUUID);
        if (shippingTime != null)            delivery.setShippingTime(shippingTime);
        if (shippingPoint != null)           delivery.setShippingPoint(shippingPoint);
        if (productionBatchNumber != null)   delivery.setProductionBatchNumber(productionBatchNumber);
        if (purchaseBatchNumber != null)     delivery.setPurchaseBatchNumber(purchaseBatchNumber);
        if (planExecuteDate != null)         delivery.setPlanExecuteDate(planExecuteDate);
        if (note != null)                    delivery.setNote(note);
        delivery.setGrossPrice(grossPrice);
        delivery.setFreightChargeType(freightChargeType);
        delivery.setFreightCharge(freightCharge);
        delivery.setPlanCategory(planCategory);
        delivery.setGrossInboundFee(grossInboundFee);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }
    public String getRefWarehouseUUID() { return refWarehouseUUID; }
    public void setRefWarehouseUUID(String refWarehouseUUID) { this.refWarehouseUUID = refWarehouseUUID; }
    public String getRefWarehouseAreaUUID() { return refWarehouseAreaUUID; }
    public void setRefWarehouseAreaUUID(String refWarehouseAreaUUID) { this.refWarehouseAreaUUID = refWarehouseAreaUUID; }
    public double getGrossPrice() { return grossPrice; }
    public void setGrossPrice(double grossPrice) { this.grossPrice = grossPrice; }
    public LocalDateTime getShippingTime() { return shippingTime; }
    public void setShippingTime(LocalDateTime shippingTime) { this.shippingTime = shippingTime; }
    public String getShippingPoint() { return shippingPoint; }
    public void setShippingPoint(String shippingPoint) { this.shippingPoint = shippingPoint; }
    public int getFreightChargeType() { return freightChargeType; }
    public void setFreightChargeType(int freightChargeType) { this.freightChargeType = freightChargeType; }
    public double getFreightCharge() { return freightCharge; }
    public void setFreightCharge(double freightCharge) { this.freightCharge = freightCharge; }
    public int getPlanCategory() { return planCategory; }
    public void setPlanCategory(int planCategory) { this.planCategory = planCategory; }
    public LocalDateTime getPlanExecuteDate() { return planExecuteDate; }
    public void setPlanExecuteDate(LocalDateTime planExecuteDate) { this.planExecuteDate = planExecuteDate; }
    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }
    public String getPurchaseBatchNumber() { return purchaseBatchNumber; }
    public void setPurchaseBatchNumber(String purchaseBatchNumber) { this.purchaseBatchNumber = purchaseBatchNumber; }
    public double getGrossInboundFee() { return grossInboundFee; }
    public void setGrossInboundFee(double grossInboundFee) { this.grossInboundFee = grossInboundFee; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
