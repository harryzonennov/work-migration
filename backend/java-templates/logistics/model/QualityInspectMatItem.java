package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.DocMatItemNode;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Migrated from: ThorsteinLogistics - QualityInspectMatItem (extends DocMatItemNode)
 * Table: QualityInspectMatItem (schema: logistics)
 */
@Entity
@Table(name = "QualityInspectMatItem", schema = "logistics")
public class QualityInspectMatItem extends DocMatItemNode {

    @Column(name = "itemInspectType")
    private int itemInspectType;

    @Column(name = "productionBatchNumber")
    private String productionBatchNumber;

    @Column(name = "itemCheckStatus")
    private int itemCheckStatus;

    @Column(name = "checkDate")
    private LocalDate checkDate;

    @Column(name = "checkTimes")
    private int checkTimes;

    @Column(name = "itemCheckResult", length = 3000)
    private String itemCheckResult;

    @Column(name = "sampleRate")
    private double sampleRate;

    @Column(name = "sampleAmount")
    private double sampleAmount;

    @Column(name = "sampleUnitUUID")
    private String sampleUnitUUID;

    @Column(name = "refWarehouseAreaUUID")
    private String refWarehouseAreaUUID;

    @Column(name = "failAmount")
    private double failAmount;

    @Column(name = "failRefUnitUUID")
    private String failRefUnitUUID;

    @Column(name = "refWasteWarehouseUUID")
    private String refWasteWarehouseUUID;

    @Column(name = "refWasteWareAreaUUID")
    private String refWasteWareAreaUUID;

    public int getItemInspectType() { return itemInspectType; }
    public void setItemInspectType(int itemInspectType) { this.itemInspectType = itemInspectType; }

    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }

    public int getItemCheckStatus() { return itemCheckStatus; }
    public void setItemCheckStatus(int itemCheckStatus) { this.itemCheckStatus = itemCheckStatus; }

    public LocalDate getCheckDate() { return checkDate; }
    public void setCheckDate(LocalDate checkDate) { this.checkDate = checkDate; }

    public int getCheckTimes() { return checkTimes; }
    public void setCheckTimes(int checkTimes) { this.checkTimes = checkTimes; }

    public String getItemCheckResult() { return itemCheckResult; }
    public void setItemCheckResult(String itemCheckResult) { this.itemCheckResult = itemCheckResult; }

    public double getSampleRate() { return sampleRate; }
    public void setSampleRate(double sampleRate) { this.sampleRate = sampleRate; }

    public double getSampleAmount() { return sampleAmount; }
    public void setSampleAmount(double sampleAmount) { this.sampleAmount = sampleAmount; }

    public String getSampleUnitUUID() { return sampleUnitUUID; }
    public void setSampleUnitUUID(String sampleUnitUUID) { this.sampleUnitUUID = sampleUnitUUID; }

    public String getRefWarehouseAreaUUID() { return refWarehouseAreaUUID; }
    public void setRefWarehouseAreaUUID(String refWarehouseAreaUUID) { this.refWarehouseAreaUUID = refWarehouseAreaUUID; }

    public double getFailAmount() { return failAmount; }
    public void setFailAmount(double failAmount) { this.failAmount = failAmount; }

    public String getFailRefUnitUUID() { return failRefUnitUUID; }
    public void setFailRefUnitUUID(String failRefUnitUUID) { this.failRefUnitUUID = failRefUnitUUID; }

    public String getRefWasteWarehouseUUID() { return refWasteWarehouseUUID; }
    public void setRefWasteWarehouseUUID(String refWasteWarehouseUUID) { this.refWasteWarehouseUUID = refWasteWarehouseUUID; }

    public String getRefWasteWareAreaUUID() { return refWasteWareAreaUUID; }
    public void setRefWasteWareAreaUUID(String refWasteWareAreaUUID) { this.refWasteWareAreaUUID = refWasteWareAreaUUID; }
}
