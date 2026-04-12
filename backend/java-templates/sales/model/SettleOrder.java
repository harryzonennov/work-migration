package com.company.IntelligentPlatform.sales.model;

import com.company.IntelligentPlatform.common.model.DocumentContent;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Migrated from: ThorsteinSalesDistribution - SettleOrder (extends DocumentContent)
 * Table: SettleOrder (schema: sales)
 *
 * Tracks settlement of sales or return orders.
 * refOrderUUID/refOrderType point to SalesContract or SalesReturnOrder (same module).
 */
@Entity
@Table(name = "SettleOrder", schema = "sales")
public class SettleOrder extends DocumentContent {

    public static final int STATUS_INIT               = 1;
    public static final int STATUS_PARTICALLY_SETTLE  = 2;
    public static final int STATUS_FULLY_SETTLE       = 3;

    @Column(name = "refOrderUUID")
    private String refOrderUUID;

    @Column(name = "refOrderType")
    private int refOrderType;

    @Column(name = "executionDate")
    private LocalDate executionDate;

    @Column(name = "curGrossSettlePrice")
    private double curGrossSettlePrice;

    public String getRefOrderUUID() { return refOrderUUID; }
    public void setRefOrderUUID(String refOrderUUID) { this.refOrderUUID = refOrderUUID; }

    public int getRefOrderType() { return refOrderType; }
    public void setRefOrderType(int refOrderType) { this.refOrderType = refOrderType; }

    public LocalDate getExecutionDate() { return executionDate; }
    public void setExecutionDate(LocalDate executionDate) { this.executionDate = executionDate; }

    public double getCurGrossSettlePrice() { return curGrossSettlePrice; }
    public void setCurGrossSettlePrice(double curGrossSettlePrice) { this.curGrossSettlePrice = curGrossSettlePrice; }
}
