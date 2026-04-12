package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.DocMatItemNode;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Base class for all delivery line items.
 * Migrated from: ThorsteinLogistics - DeliveryItem (extends DocMatItemNode)
 *
 * Used by: InboundItem, OutboundItem, InventoryTransferItem
 */
@MappedSuperclass
public abstract class DeliveryItem extends DocMatItemNode {

    @Column(name = "volume")
    protected double volume;

    @Column(name = "weight")
    protected double weight;

    @Column(name = "actualAmount")
    protected double actualAmount;

    @Column(name = "actualVolume")
    protected double actualVolume;

    @Column(name = "actualWeight")
    protected double actualWeight;

    @Column(name = "declaredValue")
    protected double declaredValue;

    @Column(name = "refMaterialSKUName")
    protected String refMaterialSKUName;

    @Column(name = "refMaterialSKUId")
    protected String refMaterialSKUId;

    @Column(name = "productionBatchNumber")
    protected String productionBatchNumber;

    @Column(name = "productionDate")
    protected LocalDate productionDate;

    @Column(name = "refUnitName")
    protected String refUnitName;

    @Column(name = "refUnitNodeInstID")
    protected String refUnitNodeInstID;

    @Column(name = "refWarehouseAreaUUID")
    protected String refWarehouseAreaUUID;

    @Column(name = "refShelfNumberID")
    protected String refShelfNumberID;

    @Column(name = "producerName")
    protected String producerName;

    @Column(name = "itemPriceNoTax")
    protected double itemPriceNoTax;

    @Column(name = "unitPriceNoTax")
    protected double unitPriceNoTax;

    @Column(name = "taxRate")
    protected double taxRate;

    @Column(name = "packageStandard")
    protected String packageStandard;

    @Column(name = "currencyCode")
    protected String currencyCode;

    public double getVolume() { return volume; }
    public void setVolume(double volume) { this.volume = volume; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getActualAmount() { return actualAmount; }
    public void setActualAmount(double actualAmount) { this.actualAmount = actualAmount; }

    public double getActualVolume() { return actualVolume; }
    public void setActualVolume(double actualVolume) { this.actualVolume = actualVolume; }

    public double getActualWeight() { return actualWeight; }
    public void setActualWeight(double actualWeight) { this.actualWeight = actualWeight; }

    public double getDeclaredValue() { return declaredValue; }
    public void setDeclaredValue(double declaredValue) { this.declaredValue = declaredValue; }

    public String getRefMaterialSKUName() { return refMaterialSKUName; }
    public void setRefMaterialSKUName(String refMaterialSKUName) { this.refMaterialSKUName = refMaterialSKUName; }

    public String getRefMaterialSKUId() { return refMaterialSKUId; }
    public void setRefMaterialSKUId(String refMaterialSKUId) { this.refMaterialSKUId = refMaterialSKUId; }

    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }

    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }

    public String getRefUnitName() { return refUnitName; }
    public void setRefUnitName(String refUnitName) { this.refUnitName = refUnitName; }

    public String getRefUnitNodeInstID() { return refUnitNodeInstID; }
    public void setRefUnitNodeInstID(String refUnitNodeInstID) { this.refUnitNodeInstID = refUnitNodeInstID; }

    public String getRefWarehouseAreaUUID() { return refWarehouseAreaUUID; }
    public void setRefWarehouseAreaUUID(String refWarehouseAreaUUID) { this.refWarehouseAreaUUID = refWarehouseAreaUUID; }

    public String getRefShelfNumberID() { return refShelfNumberID; }
    public void setRefShelfNumberID(String refShelfNumberID) { this.refShelfNumberID = refShelfNumberID; }

    public String getProducerName() { return producerName; }
    public void setProducerName(String producerName) { this.producerName = producerName; }

    public double getItemPriceNoTax() { return itemPriceNoTax; }
    public void setItemPriceNoTax(double itemPriceNoTax) { this.itemPriceNoTax = itemPriceNoTax; }

    public double getUnitPriceNoTax() { return unitPriceNoTax; }
    public void setUnitPriceNoTax(double unitPriceNoTax) { this.unitPriceNoTax = unitPriceNoTax; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }

    public String getPackageStandard() { return packageStandard; }
    public void setPackageStandard(String packageStandard) { this.packageStandard = packageStandard; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
}
