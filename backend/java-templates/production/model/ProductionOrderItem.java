package com.company.IntelligentPlatform.production.model;

import com.company.IntelligentPlatform.common.model.DocMatItemNode;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Migrated from: ThorsteinProduction - ProductionOrderItem (extends DocMatItemNode)
 * Table: ProductionOrderItem (schema: production)
 */
@Entity
@Table(name = "ProductionOrderItem", schema = "production")
public class ProductionOrderItem extends DocMatItemNode {

    public static final int STATUS_INITIAL      = 1;
    public static final int STATUS_INPROCESS    = 2;
    public static final int STATUS_FINISHED     = 3;
    public static final int STATUS_PROCESSDONE  = 100;

    public static final int ITEM_CATEGORY_SEMIFINISHED = 1;
    public static final int ITEM_CATEGORY_COMPONENT    = 2;
    public static final int ITEM_CATEGORY_BYPRODUCT    = 3;

    @Column(name = "itemCategory")
    private int itemCategory;

    @Column(name = "itemStatus")
    private int itemStatus;

    @Column(name = "refUnitUUID")
    private String refUnitUUID;

    @Column(name = "planAmount")
    private double planAmount;

    @Column(name = "actualAmount")
    private double actualAmount;

    @Column(name = "scrapAmount")
    private double scrapAmount;

    @Column(name = "pickAmount")
    private double pickAmount;

    @Column(name = "supplyAmount")
    private double supplyAmount;

    @Column(name = "unitCost")
    private double unitCost;

    @Column(name = "grossCost")
    private double grossCost;

    @Column(name = "refBOMItemUUID")
    private String refBOMItemUUID;

    @Column(name = "refRouteProcessItemUUID")
    private String refRouteProcessItemUUID;

    @Column(name = "refWarehouseUUID")
    private String refWarehouseUUID;

    @Column(name = "refWarehouseAreaUUID")
    private String refWarehouseAreaUUID;

    @Column(name = "productionBatchNumber")
    private String productionBatchNumber;

    @Column(name = "productionDate")
    private LocalDate productionDate;

    @Column(name = "requireDate")
    private LocalDate requireDate;

    public int getItemCategory() { return itemCategory; }
    public void setItemCategory(int itemCategory) { this.itemCategory = itemCategory; }
    public int getItemStatus() { return itemStatus; }
    public void setItemStatus(int itemStatus) { this.itemStatus = itemStatus; }
    public String getRefUnitUUID() { return refUnitUUID; }
    public void setRefUnitUUID(String refUnitUUID) { this.refUnitUUID = refUnitUUID; }
    public double getPlanAmount() { return planAmount; }
    public void setPlanAmount(double planAmount) { this.planAmount = planAmount; }
    public double getActualAmount() { return actualAmount; }
    public void setActualAmount(double actualAmount) { this.actualAmount = actualAmount; }
    public double getScrapAmount() { return scrapAmount; }
    public void setScrapAmount(double scrapAmount) { this.scrapAmount = scrapAmount; }
    public double getPickAmount() { return pickAmount; }
    public void setPickAmount(double pickAmount) { this.pickAmount = pickAmount; }
    public double getSupplyAmount() { return supplyAmount; }
    public void setSupplyAmount(double supplyAmount) { this.supplyAmount = supplyAmount; }
    public double getUnitCost() { return unitCost; }
    public void setUnitCost(double unitCost) { this.unitCost = unitCost; }
    public double getGrossCost() { return grossCost; }
    public void setGrossCost(double grossCost) { this.grossCost = grossCost; }
    public String getRefBOMItemUUID() { return refBOMItemUUID; }
    public void setRefBOMItemUUID(String refBOMItemUUID) { this.refBOMItemUUID = refBOMItemUUID; }
    public String getRefRouteProcessItemUUID() { return refRouteProcessItemUUID; }
    public void setRefRouteProcessItemUUID(String refRouteProcessItemUUID) { this.refRouteProcessItemUUID = refRouteProcessItemUUID; }
    public String getRefWarehouseUUID() { return refWarehouseUUID; }
    public void setRefWarehouseUUID(String refWarehouseUUID) { this.refWarehouseUUID = refWarehouseUUID; }
    public String getRefWarehouseAreaUUID() { return refWarehouseAreaUUID; }
    public void setRefWarehouseAreaUUID(String refWarehouseAreaUUID) { this.refWarehouseAreaUUID = refWarehouseAreaUUID; }
    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }
    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }
    public LocalDate getRequireDate() { return requireDate; }
    public void setRequireDate(LocalDate requireDate) { this.requireDate = requireDate; }
}
