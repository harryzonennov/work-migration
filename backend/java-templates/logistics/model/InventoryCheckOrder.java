package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.DocumentContent;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinLogistics - InventoryCheckOrder (extends DocumentContent)
 * Table: InventoryCheckOrder (schema: logistics)
 */
@Entity
@Table(name = "InventoryCheckOrder", schema = "logistics")
public class InventoryCheckOrder extends DocumentContent {

    public static final int STATUS_INITIAL      = 1;
    public static final int STATUS_SUBMITTED    = 2;
    public static final int STATUS_APPROVED     = 3;
    public static final int STATUS_PROCESSDONE  = 100;
    public static final int STATUS_DELIVERYDONE = 200;
    public static final int STATUS_REJECTED     = 305;

    @Column(name = "refWarehouseUUID")
    private String refWarehouseUUID;

    @Column(name = "refWarehouseAreaUUID")
    private String refWarehouseAreaUUID;

    @Column(name = "grossUpdateValue")
    private double grossUpdateValue;

    @Column(name = "grossCheckResult")
    private int grossCheckResult;

    public String getRefWarehouseUUID() { return refWarehouseUUID; }
    public void setRefWarehouseUUID(String refWarehouseUUID) { this.refWarehouseUUID = refWarehouseUUID; }

    public String getRefWarehouseAreaUUID() { return refWarehouseAreaUUID; }
    public void setRefWarehouseAreaUUID(String refWarehouseAreaUUID) { this.refWarehouseAreaUUID = refWarehouseAreaUUID; }

    public double getGrossUpdateValue() { return grossUpdateValue; }
    public void setGrossUpdateValue(double grossUpdateValue) { this.grossUpdateValue = grossUpdateValue; }

    public int getGrossCheckResult() { return grossCheckResult; }
    public void setGrossCheckResult(int grossCheckResult) { this.grossCheckResult = grossCheckResult; }
}
