package com.company.IntelligentPlatform.production.dto;

import com.company.IntelligentPlatform.production.model.ProductionPlan;

public class ProductionPlanDto {

    private String uuid;
    private String name;
    private int status;
    private int category;
    private String refMaterialSKUUUID;
    private String refBillOfMaterialUUID;
    private double amount;
    private String refUnitUUID;
    private String refMainProdOrderUUID;
    private int initTimeMode;
    private String productionBatchNumber;
    private String note;

    public ProductionPlan toEntity() {
        ProductionPlan e = new ProductionPlan();
        if (uuid != null) e.setUuid(uuid);
        if (name != null) e.setName(name);
        e.setStatus(status);
        e.setCategory(category);
        if (refMaterialSKUUUID != null) e.setRefMaterialSKUUUID(refMaterialSKUUUID);
        if (refBillOfMaterialUUID != null) e.setRefBillOfMaterialUUID(refBillOfMaterialUUID);
        e.setAmount(amount);
        if (refUnitUUID != null) e.setRefUnitUUID(refUnitUUID);
        if (refMainProdOrderUUID != null) e.setRefMainProdOrderUUID(refMainProdOrderUUID);
        e.setInitTimeMode(initTimeMode);
        if (productionBatchNumber != null) e.setProductionBatchNumber(productionBatchNumber);
        if (note != null) e.setNote(note);
        return e;
    }

    public void applyTo(ProductionPlan e) {
        if (name != null) e.setName(name);
        e.setStatus(status);
        e.setCategory(category);
        if (refMaterialSKUUUID != null) e.setRefMaterialSKUUUID(refMaterialSKUUUID);
        if (refBillOfMaterialUUID != null) e.setRefBillOfMaterialUUID(refBillOfMaterialUUID);
        e.setAmount(amount);
        if (refUnitUUID != null) e.setRefUnitUUID(refUnitUUID);
        if (refMainProdOrderUUID != null) e.setRefMainProdOrderUUID(refMainProdOrderUUID);
        e.setInitTimeMode(initTimeMode);
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
    public String getRefMaterialSKUUUID() { return refMaterialSKUUUID; }
    public void setRefMaterialSKUUUID(String refMaterialSKUUUID) { this.refMaterialSKUUUID = refMaterialSKUUUID; }
    public String getRefBillOfMaterialUUID() { return refBillOfMaterialUUID; }
    public void setRefBillOfMaterialUUID(String refBillOfMaterialUUID) { this.refBillOfMaterialUUID = refBillOfMaterialUUID; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getRefUnitUUID() { return refUnitUUID; }
    public void setRefUnitUUID(String refUnitUUID) { this.refUnitUUID = refUnitUUID; }
    public String getRefMainProdOrderUUID() { return refMainProdOrderUUID; }
    public void setRefMainProdOrderUUID(String refMainProdOrderUUID) { this.refMainProdOrderUUID = refMainProdOrderUUID; }
    public int getInitTimeMode() { return initTimeMode; }
    public void setInitTimeMode(int initTimeMode) { this.initTimeMode = initTimeMode; }
    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
