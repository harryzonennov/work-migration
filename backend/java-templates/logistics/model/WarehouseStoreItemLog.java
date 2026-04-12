package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.ReferenceNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinLogistics - WarehouseStoreItemLog (extends ReferenceNode)
 * Table: WarehouseStoreItemLog (schema: logistics)
 *
 * Records each change to a WarehouseStoreItem (inbound/outbound/transfer events).
 */
@Entity
@Table(name = "WarehouseStoreItemLog", schema = "logistics")
public class WarehouseStoreItemLog extends ReferenceNode {

    @Column(name = "volume")
    private double volume;

    @Column(name = "weight")
    private double weight;

    @Column(name = "amount")
    private double amount;

    @Column(name = "refUnitUUID")
    private String refUnitUUID;

    @Column(name = "refUnitName")
    private String refUnitName;

    @Column(name = "updatedVolume")
    private double updatedVolume;

    @Column(name = "updatedWeight")
    private double updatedWeight;

    @Column(name = "updatedAmount")
    private double updatedAmount;

    @Column(name = "updatedRefUnitUUID")
    private String updatedRefUnitUUID;

    @Column(name = "updatedRefUnitName")
    private String updatedRefUnitName;

    @Column(name = "documentType")
    private int documentType;

    @Column(name = "documentUUID")
    private String documentUUID;

    @Column(name = "declaredValue")
    private double declaredValue;

    @Column(name = "updatedDeclaredValue")
    private double updatedDeclaredValue;

    @Column(name = "refMaterialSKUUUID")
    private String refMaterialSKUUUID;

    @Column(name = "refMaterialSKUId")
    private String refMaterialSKUId;

    @Column(name = "refMaterialSKUName")
    private String refMaterialSKUName;

    public double getVolume() { return volume; }
    public void setVolume(double volume) { this.volume = volume; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getRefUnitUUID() { return refUnitUUID; }
    public void setRefUnitUUID(String refUnitUUID) { this.refUnitUUID = refUnitUUID; }

    public String getRefUnitName() { return refUnitName; }
    public void setRefUnitName(String refUnitName) { this.refUnitName = refUnitName; }

    public double getUpdatedVolume() { return updatedVolume; }
    public void setUpdatedVolume(double updatedVolume) { this.updatedVolume = updatedVolume; }

    public double getUpdatedWeight() { return updatedWeight; }
    public void setUpdatedWeight(double updatedWeight) { this.updatedWeight = updatedWeight; }

    public double getUpdatedAmount() { return updatedAmount; }
    public void setUpdatedAmount(double updatedAmount) { this.updatedAmount = updatedAmount; }

    public String getUpdatedRefUnitUUID() { return updatedRefUnitUUID; }
    public void setUpdatedRefUnitUUID(String updatedRefUnitUUID) { this.updatedRefUnitUUID = updatedRefUnitUUID; }

    public String getUpdatedRefUnitName() { return updatedRefUnitName; }
    public void setUpdatedRefUnitName(String updatedRefUnitName) { this.updatedRefUnitName = updatedRefUnitName; }

    public int getDocumentType() { return documentType; }
    public void setDocumentType(int documentType) { this.documentType = documentType; }

    public String getDocumentUUID() { return documentUUID; }
    public void setDocumentUUID(String documentUUID) { this.documentUUID = documentUUID; }

    public double getDeclaredValue() { return declaredValue; }
    public void setDeclaredValue(double declaredValue) { this.declaredValue = declaredValue; }

    public double getUpdatedDeclaredValue() { return updatedDeclaredValue; }
    public void setUpdatedDeclaredValue(double updatedDeclaredValue) { this.updatedDeclaredValue = updatedDeclaredValue; }

    public String getRefMaterialSKUUUID() { return refMaterialSKUUUID; }
    public void setRefMaterialSKUUUID(String refMaterialSKUUUID) { this.refMaterialSKUUUID = refMaterialSKUUUID; }

    public String getRefMaterialSKUId() { return refMaterialSKUId; }
    public void setRefMaterialSKUId(String refMaterialSKUId) { this.refMaterialSKUId = refMaterialSKUId; }

    public String getRefMaterialSKUName() { return refMaterialSKUName; }
    public void setRefMaterialSKUName(String refMaterialSKUName) { this.refMaterialSKUName = refMaterialSKUName; }
}
