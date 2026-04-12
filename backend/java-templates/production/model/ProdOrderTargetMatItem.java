package com.company.IntelligentPlatform.production.model;

import com.company.IntelligentPlatform.common.model.DocMatItemNode;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Migrated from: ThorsteinProduction - ProdOrderTargetMatItem (extends DocMatItemNode)
 * Table: ProdOrderTargetMatItem (schema: production)
 *
 * Represents a target output material item of a production order.
 */
@Entity
@Table(name = "ProdOrderTargetMatItem", schema = "production")
public class ProdOrderTargetMatItem extends DocMatItemNode {

    public static final int ITEM_STATUS_INITIAL   = 1;
    public static final int ITEM_STATUS_INPROCESS = 2;
    public static final int ITEM_STATUS_FINISHED  = 3;

    @Column(name = "itemStatus")
    private int itemStatus;

    @Column(name = "processIndex")
    private int processIndex;

    @Column(name = "refSerialId")
    private String refSerialId;

    @Column(name = "refUnitUUID")
    private String refUnitUUID;

    @Column(name = "planAmount")
    private double planAmount;

    @Column(name = "actualAmount")
    private double actualAmount;

    @Column(name = "scrapAmount")
    private double scrapAmount;

    @Column(name = "unitCost")
    private double unitCost;

    @Column(name = "grossCost")
    private double grossCost;

    @Column(name = "refWarehouseUUID")
    private String refWarehouseUUID;

    @Column(name = "refWarehouseAreaUUID")
    private String refWarehouseAreaUUID;

    @Column(name = "productionBatchNumber")
    private String productionBatchNumber;

    @Column(name = "productionDate")
    private LocalDate productionDate;

    public int getItemStatus() { return itemStatus; }
    public void setItemStatus(int itemStatus) { this.itemStatus = itemStatus; }
    public int getProcessIndex() { return processIndex; }
    public void setProcessIndex(int processIndex) { this.processIndex = processIndex; }
    public String getRefSerialId() { return refSerialId; }
    public void setRefSerialId(String refSerialId) { this.refSerialId = refSerialId; }
    public String getRefUnitUUID() { return refUnitUUID; }
    public void setRefUnitUUID(String refUnitUUID) { this.refUnitUUID = refUnitUUID; }
    public double getPlanAmount() { return planAmount; }
    public void setPlanAmount(double planAmount) { this.planAmount = planAmount; }
    public double getActualAmount() { return actualAmount; }
    public void setActualAmount(double actualAmount) { this.actualAmount = actualAmount; }
    public double getScrapAmount() { return scrapAmount; }
    public void setScrapAmount(double scrapAmount) { this.scrapAmount = scrapAmount; }
    public double getUnitCost() { return unitCost; }
    public void setUnitCost(double unitCost) { this.unitCost = unitCost; }
    public double getGrossCost() { return grossCost; }
    public void setGrossCost(double grossCost) { this.grossCost = grossCost; }
    public String getRefWarehouseUUID() { return refWarehouseUUID; }
    public void setRefWarehouseUUID(String refWarehouseUUID) { this.refWarehouseUUID = refWarehouseUUID; }
    public String getRefWarehouseAreaUUID() { return refWarehouseAreaUUID; }
    public void setRefWarehouseAreaUUID(String refWarehouseAreaUUID) { this.refWarehouseAreaUUID = refWarehouseAreaUUID; }
    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }
    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }
}
