package com.company.IntelligentPlatform.production.model;

import com.company.IntelligentPlatform.common.model.DocMatItemNode;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Migrated from: ThorsteinProduction - ProdPickingRefMaterialItem (extends DocMatItemNode)
 * Table: ProdPickingRefMaterialItem (schema: production)
 *
 * Tracks picking of reference materials for a production picking order.
 */
@Entity
@Table(name = "ProdPickingRefMaterialItem", schema = "production")
public class ProdPickingRefMaterialItem extends DocMatItemNode {

    public static final int ITEM_STATUS_INITIAL    = 1;
    public static final int ITEM_STATUS_INPROCESS  = 2;
    public static final int ITEM_STATUS_FINISHED   = 3;

    @Column(name = "itemStatus")
    private int itemStatus;

    @Column(name = "refUnitUUID")
    private String refUnitUUID;

    @Column(name = "planAmount")
    private double planAmount;

    @Column(name = "actualAmount")
    private double actualAmount;

    @Column(name = "returnAmount")
    private double returnAmount;

    @Column(name = "remainAmount")
    private double remainAmount;

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

    @Column(name = "refStoreItemUUID")
    private String refStoreItemUUID;

    @Column(name = "productionBatchNumber")
    private String productionBatchNumber;

    @Column(name = "productionDate")
    private LocalDate productionDate;

    public int getItemStatus() { return itemStatus; }
    public void setItemStatus(int itemStatus) { this.itemStatus = itemStatus; }
    public String getRefUnitUUID() { return refUnitUUID; }
    public void setRefUnitUUID(String refUnitUUID) { this.refUnitUUID = refUnitUUID; }
    public double getPlanAmount() { return planAmount; }
    public void setPlanAmount(double planAmount) { this.planAmount = planAmount; }
    public double getActualAmount() { return actualAmount; }
    public void setActualAmount(double actualAmount) { this.actualAmount = actualAmount; }
    public double getReturnAmount() { return returnAmount; }
    public void setReturnAmount(double returnAmount) { this.returnAmount = returnAmount; }
    public double getRemainAmount() { return remainAmount; }
    public void setRemainAmount(double remainAmount) { this.remainAmount = remainAmount; }
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
    public String getRefStoreItemUUID() { return refStoreItemUUID; }
    public void setRefStoreItemUUID(String refStoreItemUUID) { this.refStoreItemUUID = refStoreItemUUID; }
    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }
    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }
}
