package com.company.IntelligentPlatform.production.dto;

import com.company.IntelligentPlatform.production.model.ProductionOrder;

public class ProductionOrderDto {

    private String uuid;
    private String name;
    private int status;
    private int category;
    private int orderType;
    private String refMaterialSKUUUID;
    private String refBillOfMaterialUUID;
    private double amount;
    private String refUnitUUID;
    private String refWocUUID;
    private String refPlanUUID;
    private String productionBatchNumber;
    private String note;

    public ProductionOrder toEntity() {
        ProductionOrder e = new ProductionOrder();
        if (uuid != null) e.setUuid(uuid);
        if (name != null) e.setName(name);
        e.setStatus(status);
        e.setCategory(category);
        e.setOrderType(orderType);
        if (refMaterialSKUUUID != null) e.setRefMaterialSKUUUID(refMaterialSKUUUID);
        if (refBillOfMaterialUUID != null) e.setRefBillOfMaterialUUID(refBillOfMaterialUUID);
        e.setAmount(amount);
        if (refUnitUUID != null) e.setRefUnitUUID(refUnitUUID);
        if (refWocUUID != null) e.setRefWocUUID(refWocUUID);
        if (refPlanUUID != null) e.setRefPlanUUID(refPlanUUID);
        if (productionBatchNumber != null) e.setProductionBatchNumber(productionBatchNumber);
        if (note != null) e.setNote(note);
        return e;
    }

    public void applyTo(ProductionOrder e) {
        if (name != null) e.setName(name);
        e.setStatus(status);
        e.setCategory(category);
        e.setOrderType(orderType);
        if (refMaterialSKUUUID != null) e.setRefMaterialSKUUUID(refMaterialSKUUUID);
        if (refBillOfMaterialUUID != null) e.setRefBillOfMaterialUUID(refBillOfMaterialUUID);
        e.setAmount(amount);
        if (refUnitUUID != null) e.setRefUnitUUID(refUnitUUID);
        if (refWocUUID != null) e.setRefWocUUID(refWocUUID);
        if (refPlanUUID != null) e.setRefPlanUUID(refPlanUUID);
        if (productionBatchNumber != null) e.setProductionBatchNumber(productionBatchNumber);
        if (note != null) e.setNote(note);
    }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public int getCategory() { return category; }
    public void setCategory(int category) { this.category = category; }
    public int getOrderType() { return orderType; }
    public void setOrderType(int orderType) { this.orderType = orderType; }
    public String getRefMaterialSKUUUID() { return refMaterialSKUUUID; }
    public void setRefMaterialSKUUUID(String refMaterialSKUUUID) { this.refMaterialSKUUUID = refMaterialSKUUUID; }
    public String getRefBillOfMaterialUUID() { return refBillOfMaterialUUID; }
    public void setRefBillOfMaterialUUID(String refBillOfMaterialUUID) { this.refBillOfMaterialUUID = refBillOfMaterialUUID; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getRefUnitUUID() { return refUnitUUID; }
    public void setRefUnitUUID(String refUnitUUID) { this.refUnitUUID = refUnitUUID; }
    public String getRefWocUUID() { return refWocUUID; }
    public void setRefWocUUID(String refWocUUID) { this.refWocUUID = refWocUUID; }
    public String getRefPlanUUID() { return refPlanUUID; }
    public void setRefPlanUUID(String refPlanUUID) { this.refPlanUUID = refPlanUUID; }
    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
