package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.DocumentContent;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Migrated from: ThorsteinLogistics - QualityInspectOrder (extends DocumentContent)
 * Table: QualityInspectOrder (schema: logistics)
 */
@Entity
@Table(name = "QualityInspectOrder", schema = "logistics")
public class QualityInspectOrder extends DocumentContent {

    public static final int STATUS_INITIAL    = 1;
    public static final int STATUS_INPROCESS  = 2;
    public static final int STATUS_TESTDONE   = 190;
    public static final int STATUS_PROCESSDONE = 100;

    public static final int CHECKSTATUS_INITIAL  = 1;
    public static final int CHECKSTATUS_PARTPASS = 2;
    public static final int CHECKSTATUS_FULLPASS = 3;
    public static final int CHECKSTATUS_NOTPASS  = 4;

    public static final int CATEGORY_INBOUND    = 1;
    public static final int CATEGORY_OUTBOUND   = 2;
    public static final int CATEGORY_PRODUCTION = 3;

    public static final int INSTYPE_FULLINSPECT = 1;
    public static final int INSTYPE_RAMINSPECT  = 2;

    @Column(name = "inspectType")
    private int inspectType;

    @Column(name = "reservedDocType")
    private int reservedDocType;

    @Column(name = "reservedDocUUID")
    private String reservedDocUUID;

    @Column(name = "checkStatus")
    private int checkStatus;

    @Column(name = "checkDate")
    private LocalDate checkDate;

    @Column(name = "category")
    private int category;

    @Column(name = "grossPrice")
    private double grossPrice;

    @Column(name = "checkResult", length = 3000)
    private String checkResult;

    /** Only relevant for CATEGORY_INBOUND */
    @Column(name = "refWarehouseUUID")
    private String refWarehouseUUID;

    @Column(name = "refWarehouseAreaUUID")
    private String refWarehouseAreaUUID;

    @Column(name = "purchaseBatchNumber")
    private String purchaseBatchNumber;

    @Column(name = "productionBatchNumber")
    private String productionBatchNumber;

    public int getInspectType() { return inspectType; }
    public void setInspectType(int inspectType) { this.inspectType = inspectType; }

    public int getReservedDocType() { return reservedDocType; }
    public void setReservedDocType(int reservedDocType) { this.reservedDocType = reservedDocType; }

    public String getReservedDocUUID() { return reservedDocUUID; }
    public void setReservedDocUUID(String reservedDocUUID) { this.reservedDocUUID = reservedDocUUID; }

    public int getCheckStatus() { return checkStatus; }
    public void setCheckStatus(int checkStatus) { this.checkStatus = checkStatus; }

    public LocalDate getCheckDate() { return checkDate; }
    public void setCheckDate(LocalDate checkDate) { this.checkDate = checkDate; }

    public int getCategory() { return category; }
    public void setCategory(int category) { this.category = category; }

    public double getGrossPrice() { return grossPrice; }
    public void setGrossPrice(double grossPrice) { this.grossPrice = grossPrice; }

    public String getCheckResult() { return checkResult; }
    public void setCheckResult(String checkResult) { this.checkResult = checkResult; }

    public String getRefWarehouseUUID() { return refWarehouseUUID; }
    public void setRefWarehouseUUID(String refWarehouseUUID) { this.refWarehouseUUID = refWarehouseUUID; }

    public String getRefWarehouseAreaUUID() { return refWarehouseAreaUUID; }
    public void setRefWarehouseAreaUUID(String refWarehouseAreaUUID) { this.refWarehouseAreaUUID = refWarehouseAreaUUID; }

    public String getPurchaseBatchNumber() { return purchaseBatchNumber; }
    public void setPurchaseBatchNumber(String purchaseBatchNumber) { this.purchaseBatchNumber = purchaseBatchNumber; }

    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }
}
