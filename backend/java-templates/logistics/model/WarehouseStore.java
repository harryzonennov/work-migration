package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.DocumentContent;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinLogistics - WarehouseStore (extends DocumentContent)
 * Table: WarehouseStore (schema: logistics)
 */
@Entity
@Table(name = "WarehouseStore", schema = "logistics")
public class WarehouseStore extends DocumentContent {

    public static final int STATUS_INITIAL        = 1;
    public static final int STATUS_SUBMITTED      = 2;
    public static final int STATUS_APPROVED       = 3;
    public static final int STATUS_INPROCESS      = 4;
    public static final int STATUS_SUCCESS        = 5;
    public static final int STATUS_FAILURE        = 6;
    public static final int STATUS_DELIVERYDONE   = 200;
    public static final int STATUS_PROCESSDONE    = 100;
    public static final int STATUS_REJECT_APPROVAL = 305;
    public static final int STATUS_ARCHIVED       = 400;

    @Column(name = "refWarehouseUUID")
    private String refWarehouseUUID;

    @Column(name = "refWarehouseAreaUUID")
    private String refWarehouseAreaUUID;

    @Column(name = "grossPrice")
    private double grossPrice;

    @Column(name = "grossPriceDisplay")
    private double grossPriceDisplay;

    public String getRefWarehouseUUID() { return refWarehouseUUID; }
    public void setRefWarehouseUUID(String refWarehouseUUID) { this.refWarehouseUUID = refWarehouseUUID; }

    public String getRefWarehouseAreaUUID() { return refWarehouseAreaUUID; }
    public void setRefWarehouseAreaUUID(String refWarehouseAreaUUID) { this.refWarehouseAreaUUID = refWarehouseAreaUUID; }

    public double getGrossPrice() { return grossPrice; }
    public void setGrossPrice(double grossPrice) { this.grossPrice = grossPrice; }

    public double getGrossPriceDisplay() { return grossPriceDisplay; }
    public void setGrossPriceDisplay(double grossPriceDisplay) { this.grossPriceDisplay = grossPriceDisplay; }
}
