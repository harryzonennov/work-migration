package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.DocMatItemNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Migrated from: ThorsteinLogistics - WarehouseStoreItem (extends DocMatItemNode)
 * Table: WarehouseStoreItem (schema: logistics)
 */
@Entity
@Table(name = "WarehouseStoreItem", schema = "logistics")
public class WarehouseStoreItem extends DocMatItemNode {

    public static final int STATUS_INSTOCK = 1;
    public static final int STATUS_ARCHIVE = 2;

    @Column(name = "productionDate")
    private LocalDate productionDate;

    @Column(name = "productionBatchNumber")
    private String productionBatchNumber;

    @Column(name = "refUnitName")
    private String refUnitName;

    @Column(name = "inboundDate")
    private LocalDateTime inboundDate;

    @Column(name = "outboundDate")
    private LocalDateTime outboundDate;

    @Column(name = "refWarehouseUUID")
    private String refWarehouseUUID;

    @Column(name = "refWarehouseAreaUUID")
    private String refWarehouseAreaUUID;

    @Column(name = "refShelfNumberId")
    private String refShelfNumberId;

    @Column(name = "volume")
    private double volume;

    @Column(name = "weight")
    private double weight;

    @Column(name = "declaredValue")
    private double declaredValue;

    @Column(name = "refMaterialTemplateUUID")
    private String refMaterialTemplateUUID;

    @Column(name = "refMaterialSKUId")
    private String refMaterialSKUId;

    @Column(name = "refMaterialSKUName")
    private String refMaterialSKUName;

    @Column(name = "packageStandard")
    private String packageStandard;

    @Column(name = "productionPlace")
    private String productionPlace;

    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }

    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }

    public String getRefUnitName() { return refUnitName; }
    public void setRefUnitName(String refUnitName) { this.refUnitName = refUnitName; }

    public LocalDateTime getInboundDate() { return inboundDate; }
    public void setInboundDate(LocalDateTime inboundDate) { this.inboundDate = inboundDate; }

    public LocalDateTime getOutboundDate() { return outboundDate; }
    public void setOutboundDate(LocalDateTime outboundDate) { this.outboundDate = outboundDate; }

    public String getRefWarehouseUUID() { return refWarehouseUUID; }
    public void setRefWarehouseUUID(String refWarehouseUUID) { this.refWarehouseUUID = refWarehouseUUID; }

    public String getRefWarehouseAreaUUID() { return refWarehouseAreaUUID; }
    public void setRefWarehouseAreaUUID(String refWarehouseAreaUUID) { this.refWarehouseAreaUUID = refWarehouseAreaUUID; }

    public String getRefShelfNumberId() { return refShelfNumberId; }
    public void setRefShelfNumberId(String refShelfNumberId) { this.refShelfNumberId = refShelfNumberId; }

    public double getVolume() { return volume; }
    public void setVolume(double volume) { this.volume = volume; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getDeclaredValue() { return declaredValue; }
    public void setDeclaredValue(double declaredValue) { this.declaredValue = declaredValue; }

    public String getRefMaterialTemplateUUID() { return refMaterialTemplateUUID; }
    public void setRefMaterialTemplateUUID(String refMaterialTemplateUUID) { this.refMaterialTemplateUUID = refMaterialTemplateUUID; }

    public String getRefMaterialSKUId() { return refMaterialSKUId; }
    public void setRefMaterialSKUId(String refMaterialSKUId) { this.refMaterialSKUId = refMaterialSKUId; }

    public String getRefMaterialSKUName() { return refMaterialSKUName; }
    public void setRefMaterialSKUName(String refMaterialSKUName) { this.refMaterialSKUName = refMaterialSKUName; }

    public String getPackageStandard() { return packageStandard; }
    public void setPackageStandard(String packageStandard) { this.packageStandard = packageStandard; }

    public String getProductionPlace() { return productionPlace; }
    public void setProductionPlace(String productionPlace) { this.productionPlace = productionPlace; }
}
