package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.DocumentContent;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Base class for all delivery documents.
 * Migrated from: ThorsteinLogistics - Delivery (extends DocumentContent)
 *
 * Used by: InboundDelivery, OutboundDelivery, InventoryTransferOrder
 */
@MappedSuperclass
public abstract class Delivery extends DocumentContent {

    public static final int STATUS_INITIAL      = 1;
    public static final int STATUS_SUBMITTED    = 2;
    public static final int STATUS_APPROVED     = 3;
    public static final int STATUS_PROCESSDONE  = 100;
    public static final int STATUS_DELIVERYDONE = 200;
    public static final int STATUS_REJECTED     = 305;
    public static final int STATUS_REVOKE_SUBMIT = 4;

    public static final int FREIGHTCHARGE_TYPE_SENDER   = 1;
    public static final int FREIGHTCHARGE_TYPE_RECEIVER = 2;

    public static final int PLANCATEGORY_NOPLAN = 1;
    public static final int PLANCATEGORY_INPLAN = 2;

    @Column(name = "refWarehouseUUID")
    protected String refWarehouseUUID;

    @Column(name = "refWarehouseAreaUUID")
    protected String refWarehouseAreaUUID;

    @Column(name = "grossPrice")
    protected double grossPrice;

    @Column(name = "shippingTime")
    protected LocalDateTime shippingTime;

    @Column(name = "shippingPoint")
    protected String shippingPoint;

    @Column(name = "freightChargeType")
    protected int freightChargeType;

    @Column(name = "freightCharge")
    protected double freightCharge;

    @Column(name = "planCategory")
    protected int planCategory;

    @Column(name = "planExecuteDate")
    protected LocalDateTime planExecuteDate;

    @Column(name = "productionBatchNumber")
    protected String productionBatchNumber;

    @Column(name = "purchaseBatchNumber")
    protected String purchaseBatchNumber;

    public String getRefWarehouseUUID() { return refWarehouseUUID; }
    public void setRefWarehouseUUID(String refWarehouseUUID) { this.refWarehouseUUID = refWarehouseUUID; }

    public String getRefWarehouseAreaUUID() { return refWarehouseAreaUUID; }
    public void setRefWarehouseAreaUUID(String refWarehouseAreaUUID) { this.refWarehouseAreaUUID = refWarehouseAreaUUID; }

    public double getGrossPrice() { return grossPrice; }
    public void setGrossPrice(double grossPrice) { this.grossPrice = grossPrice; }

    public LocalDateTime getShippingTime() { return shippingTime; }
    public void setShippingTime(LocalDateTime shippingTime) { this.shippingTime = shippingTime; }

    public String getShippingPoint() { return shippingPoint; }
    public void setShippingPoint(String shippingPoint) { this.shippingPoint = shippingPoint; }

    public int getFreightChargeType() { return freightChargeType; }
    public void setFreightChargeType(int freightChargeType) { this.freightChargeType = freightChargeType; }

    public double getFreightCharge() { return freightCharge; }
    public void setFreightCharge(double freightCharge) { this.freightCharge = freightCharge; }

    public int getPlanCategory() { return planCategory; }
    public void setPlanCategory(int planCategory) { this.planCategory = planCategory; }

    public LocalDateTime getPlanExecuteDate() { return planExecuteDate; }
    public void setPlanExecuteDate(LocalDateTime planExecuteDate) { this.planExecuteDate = planExecuteDate; }

    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }

    public String getPurchaseBatchNumber() { return purchaseBatchNumber; }
    public void setPurchaseBatchNumber(String purchaseBatchNumber) { this.purchaseBatchNumber = purchaseBatchNumber; }
}
