package com.company.IntelligentPlatform.production.model;

import com.company.IntelligentPlatform.common.model.DocumentContent;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Migrated from: ThorsteinProduction - ProductionPlan (extends DocumentContent)
 * Table: ProductionPlan (schema: production)
 */
@Entity
@Table(name = "ProductionPlan", schema = "production")
public class ProductionPlan extends DocumentContent {

    public static final int STATUS_INITIAL        = 1;
    public static final int STATUS_SUBMITTED      = 2;
    public static final int STATUS_APPROVED       = 3;
    public static final int STATUS_INPRODUCTION   = 4;
    public static final int STATUS_FINISHED       = 5;
    public static final int STATUS_PROCESSDONE    = 100;
    public static final int STATUS_BLOCKED        = 6;
    public static final int STATUS_REJECT_APPROVAL = 305;

    public static final int CATEGORY_MANUAL = 1;
    public static final int CATEGORY_SYSTEM = 2;

    public static final int INIT_TIMEMODE_STARTPREPARE = 1;
    public static final int INIT_TIMEMODE_PLANSTART    = 2;
    public static final int INIT_TIMEMODE_PLANEND      = 3;

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

    @Column(name = "approveBy")
    private String approveBy;

    @Column(name = "approveTime")
    private LocalDateTime approveTime;

    @Column(name = "countApproveBy")
    private String countApproveBy;

    @Column(name = "countApproveTime")
    private LocalDateTime countApproveTime;

    @Column(name = "refMainProdOrderUUID")
    private String refMainProdOrderUUID;

    @Column(name = "initTimeMode")
    private int initTimeMode;

    @Column(name = "productionBatchNumber")
    private String productionBatchNumber;

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
    public String getApproveBy() { return approveBy; }
    public void setApproveBy(String approveBy) { this.approveBy = approveBy; }
    public LocalDateTime getApproveTime() { return approveTime; }
    public void setApproveTime(LocalDateTime approveTime) { this.approveTime = approveTime; }
    public String getCountApproveBy() { return countApproveBy; }
    public void setCountApproveBy(String countApproveBy) { this.countApproveBy = countApproveBy; }
    public LocalDateTime getCountApproveTime() { return countApproveTime; }
    public void setCountApproveTime(LocalDateTime countApproveTime) { this.countApproveTime = countApproveTime; }
    public String getRefMainProdOrderUUID() { return refMainProdOrderUUID; }
    public void setRefMainProdOrderUUID(String refMainProdOrderUUID) { this.refMainProdOrderUUID = refMainProdOrderUUID; }
    public int getInitTimeMode() { return initTimeMode; }
    public void setInitTimeMode(int initTimeMode) { this.initTimeMode = initTimeMode; }
    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }
}
