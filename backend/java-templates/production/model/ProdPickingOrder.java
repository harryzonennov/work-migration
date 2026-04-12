package com.company.IntelligentPlatform.production.model;

import com.company.IntelligentPlatform.common.model.DocumentContent;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Migrated from: ThorsteinProduction - ProdPickingOrder (extends DocumentContent)
 * Table: ProdPickingOrder (schema: production)
 */
@Entity
@Table(name = "ProdPickingOrder", schema = "production")
public class ProdPickingOrder extends DocumentContent {

    public static final int STATUS_INITIAL        = 1;
    public static final int STATUS_APPROVED       = 2;
    public static final int STATUS_INPROCESS      = 3;
    public static final int STATUS_DELIVERYDONE   = 200;
    public static final int STATUS_PROCESSDONE    = 100;
    public static final int STATUS_REJECT_APPROVAL = 305;

    public static final int CATEGORY_MANUAL         = 1;
    public static final int CATEGORY_PRODORDER      = 2;
    public static final int CATEGORY_PRODORDERBATCH = 3;

    public static final int PROCESSTYPE_INPLAN    = 1;
    public static final int PROCESSTYPE_REPLENISH = 2;
    public static final int PROCESSTYPE_RETURN    = 3;

    @Column(name = "category")
    private int category;

    @Column(name = "processType")
    private int processType;

    @Column(name = "approveBy")
    private String approveBy;

    @Column(name = "approveDate")
    private LocalDate approveDate;

    @Column(name = "approveType")
    private int approveType;

    @Column(name = "processBy")
    private String processBy;

    @Column(name = "processDate")
    private LocalDate processDate;

    @Column(name = "grossCost")
    private double grossCost;

    public int getCategory() { return category; }
    public void setCategory(int category) { this.category = category; }
    public int getProcessType() { return processType; }
    public void setProcessType(int processType) { this.processType = processType; }
    public String getApproveBy() { return approveBy; }
    public void setApproveBy(String approveBy) { this.approveBy = approveBy; }
    public LocalDate getApproveDate() { return approveDate; }
    public void setApproveDate(LocalDate approveDate) { this.approveDate = approveDate; }
    public int getApproveType() { return approveType; }
    public void setApproveType(int approveType) { this.approveType = approveType; }
    public String getProcessBy() { return processBy; }
    public void setProcessBy(String processBy) { this.processBy = processBy; }
    public LocalDate getProcessDate() { return processDate; }
    public void setProcessDate(LocalDate processDate) { this.processDate = processDate; }
    public double getGrossCost() { return grossCost; }
    public void setGrossCost(double grossCost) { this.grossCost = grossCost; }
}
