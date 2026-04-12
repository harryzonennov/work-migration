package com.company.IntelligentPlatform.production.model;

import com.company.IntelligentPlatform.common.model.DocumentContent;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Migrated from: ThorsteinProduction - ProductionOrder (extends DocumentContent)
 * Table: ProductionOrder (schema: production)
 */
@Entity
@Table(name = "ProductionOrder", schema = "production")
public class ProductionOrder extends DocumentContent {

    public static final int STATUS_INITIAL        = 1;
    public static final int STATUS_SUBMITTED      = 2;
    public static final int STATUS_APPROVED       = 3;
    public static final int STATUS_INPRODUCTION   = 4;
    public static final int STATUS_FINISHED       = 5;
    public static final int STATUS_PROCESS_DONE   = 100;
    public static final int STATUS_BLOCKED        = 6;
    public static final int STATUS_REJECT_APPROVAL = 305;

    public static final int CATEGORY_MANUAL = 1;
    public static final int CATEGORY_SYSTEM = 2;

    public static final int DONESTATUS_INITIAL     = 1;
    public static final int DONESTATUS_PARTFINISH  = 2;
    public static final int DONESTATUS_FULLFINISH  = 3;

    public static final int ORDERTYPE_STANDARD = 1;
    public static final int ORDERTYPE_REPAIR   = 2;

    @Column(name = "refMaterialSKUUUID")
    private String refMaterialSKUUUID;

    @Column(name = "refBillOfMaterialUUID")
    private String refBillOfMaterialUUID;

    @Column(name = "amount")
    private double amount;

    @Column(name = "refUnitUUID")
    private String refUnitUUID;

    @Column(name = "category")
    private int category;

    @Column(name = "doneStatus")
    private int doneStatus;

    @Column(name = "planStartPrepareDate")
    private LocalDateTime planStartPrepareDate;

    @Column(name = "planStartTime")
    private LocalDateTime planStartTime;

    @Column(name = "actualStartTime")
    private LocalDateTime actualStartTime;

    @Column(name = "planEndTime")
    private LocalDateTime planEndTime;

    @Column(name = "actualEndTime")
    private LocalDateTime actualEndTime;

    @Column(name = "selfLeadTime")
    private double selfLeadTime;

    @Column(name = "comLeadTime")
    private double comLeadTime;

    @Column(name = "refWocUUID")
    private String refWocUUID;

    @Column(name = "refPlanUUID")
    private String refPlanUUID;

    @Column(name = "reservedMatItemUUID")
    private String reservedMatItemUUID;

    @Column(name = "reservedDocType")
    private int reservedDocType;

    @Column(name = "grossCost")
    private double grossCost;

    @Column(name = "grossCostLossRate")
    private double grossCostLossRate;

    @Column(name = "grossCostActual")
    private double grossCostActual;

    @Column(name = "approveBy")
    private String approveBy;

    @Column(name = "approveTime")
    private LocalDateTime approveTime;

    @Column(name = "countApproveBy")
    private String countApproveBy;

    @Column(name = "countApproveTime")
    private LocalDateTime countApproveTime;

    @Column(name = "orderType")
    private int orderType;

    @Column(name = "productionBatchNumber")
    private String productionBatchNumber;

    @Column(name = "genOrderItemMode")
    private int genOrderItemMode;

    public String getRefMaterialSKUUUID() { return refMaterialSKUUUID; }
    public void setRefMaterialSKUUUID(String refMaterialSKUUUID) { this.refMaterialSKUUUID = refMaterialSKUUUID; }
    public String getRefBillOfMaterialUUID() { return refBillOfMaterialUUID; }
    public void setRefBillOfMaterialUUID(String refBillOfMaterialUUID) { this.refBillOfMaterialUUID = refBillOfMaterialUUID; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getRefUnitUUID() { return refUnitUUID; }
    public void setRefUnitUUID(String refUnitUUID) { this.refUnitUUID = refUnitUUID; }
    public int getCategory() { return category; }
    public void setCategory(int category) { this.category = category; }
    public int getDoneStatus() { return doneStatus; }
    public void setDoneStatus(int doneStatus) { this.doneStatus = doneStatus; }
    public LocalDateTime getPlanStartPrepareDate() { return planStartPrepareDate; }
    public void setPlanStartPrepareDate(LocalDateTime planStartPrepareDate) { this.planStartPrepareDate = planStartPrepareDate; }
    public LocalDateTime getPlanStartTime() { return planStartTime; }
    public void setPlanStartTime(LocalDateTime planStartTime) { this.planStartTime = planStartTime; }
    public LocalDateTime getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(LocalDateTime actualStartTime) { this.actualStartTime = actualStartTime; }
    public LocalDateTime getPlanEndTime() { return planEndTime; }
    public void setPlanEndTime(LocalDateTime planEndTime) { this.planEndTime = planEndTime; }
    public LocalDateTime getActualEndTime() { return actualEndTime; }
    public void setActualEndTime(LocalDateTime actualEndTime) { this.actualEndTime = actualEndTime; }
    public double getSelfLeadTime() { return selfLeadTime; }
    public void setSelfLeadTime(double selfLeadTime) { this.selfLeadTime = selfLeadTime; }
    public double getComLeadTime() { return comLeadTime; }
    public void setComLeadTime(double comLeadTime) { this.comLeadTime = comLeadTime; }
    public String getRefWocUUID() { return refWocUUID; }
    public void setRefWocUUID(String refWocUUID) { this.refWocUUID = refWocUUID; }
    public String getRefPlanUUID() { return refPlanUUID; }
    public void setRefPlanUUID(String refPlanUUID) { this.refPlanUUID = refPlanUUID; }
    public String getReservedMatItemUUID() { return reservedMatItemUUID; }
    public void setReservedMatItemUUID(String reservedMatItemUUID) { this.reservedMatItemUUID = reservedMatItemUUID; }
    public int getReservedDocType() { return reservedDocType; }
    public void setReservedDocType(int reservedDocType) { this.reservedDocType = reservedDocType; }
    public double getGrossCost() { return grossCost; }
    public void setGrossCost(double grossCost) { this.grossCost = grossCost; }
    public double getGrossCostLossRate() { return grossCostLossRate; }
    public void setGrossCostLossRate(double grossCostLossRate) { this.grossCostLossRate = grossCostLossRate; }
    public double getGrossCostActual() { return grossCostActual; }
    public void setGrossCostActual(double grossCostActual) { this.grossCostActual = grossCostActual; }
    public String getApproveBy() { return approveBy; }
    public void setApproveBy(String approveBy) { this.approveBy = approveBy; }
    public LocalDateTime getApproveTime() { return approveTime; }
    public void setApproveTime(LocalDateTime approveTime) { this.approveTime = approveTime; }
    public String getCountApproveBy() { return countApproveBy; }
    public void setCountApproveBy(String countApproveBy) { this.countApproveBy = countApproveBy; }
    public LocalDateTime getCountApproveTime() { return countApproveTime; }
    public void setCountApproveTime(LocalDateTime countApproveTime) { this.countApproveTime = countApproveTime; }
    public int getOrderType() { return orderType; }
    public void setOrderType(int orderType) { this.orderType = orderType; }
    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }
    public int getGenOrderItemMode() { return genOrderItemMode; }
    public void setGenOrderItemMode(int genOrderItemMode) { this.genOrderItemMode = genOrderItemMode; }
}
