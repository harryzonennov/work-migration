package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.ReferenceNode;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Migrated from: ThorsteinLogistics - PurchaseOrderItem (extends ReferenceNode)
 * Table: PurchaseOrderItem (schema: logistics)
 *
 * Cross-module refs:
 *   refFinAccountUUID → FinAccount (finance module)
 *   refInDeliveryItemUUID → InboundItem (same module)
 */
@Entity
@Table(name = "PurchaseOrderItem", schema = "logistics")
public class PurchaseOrderItem extends ReferenceNode {

    public static final int STATUS_INITIAL         = 1;
    public static final int STATUS_DONE            = 2;
    public static final int AVAILABLE_CHECK_INITIAL = 1;
    public static final int AVAILABLE_CHECK_OK     = 2;
    public static final int AVAILABLE_CHECK_ERROR  = 3;

    @Column(name = "itemStatus")
    private int itemStatus;

    @Column(name = "refUnitUUID")
    private String refUnitUUID;

    @Column(name = "amount")
    private double amount;

    @Column(name = "netValue")
    private double netValue;

    @Column(name = "refInWarehouseUUID")
    private String refInWarehouseUUID;

    @Column(name = "refFinAccountUUID")
    private String refFinAccountUUID;

    @Column(name = "refInDeliveryItemUUID")
    private String refInDeliveryItemUUID;

    @Column(name = "availableCheckStatus")
    private int availableCheckStatus;

    @Column(name = "weight")
    private double weight;

    @Column(name = "volume")
    private double volume;

    @Column(name = "producerName")
    private String producerName;

    @Column(name = "productionBatchNumber")
    private String productionBatchNumber;

    @Column(name = "productionDate")
    private LocalDate productionDate;

    public int getItemStatus() { return itemStatus; }
    public void setItemStatus(int itemStatus) { this.itemStatus = itemStatus; }

    public String getRefUnitUUID() { return refUnitUUID; }
    public void setRefUnitUUID(String refUnitUUID) { this.refUnitUUID = refUnitUUID; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getNetValue() { return netValue; }
    public void setNetValue(double netValue) { this.netValue = netValue; }

    public String getRefInWarehouseUUID() { return refInWarehouseUUID; }
    public void setRefInWarehouseUUID(String refInWarehouseUUID) { this.refInWarehouseUUID = refInWarehouseUUID; }

    public String getRefFinAccountUUID() { return refFinAccountUUID; }
    public void setRefFinAccountUUID(String refFinAccountUUID) { this.refFinAccountUUID = refFinAccountUUID; }

    public String getRefInDeliveryItemUUID() { return refInDeliveryItemUUID; }
    public void setRefInDeliveryItemUUID(String refInDeliveryItemUUID) { this.refInDeliveryItemUUID = refInDeliveryItemUUID; }

    public int getAvailableCheckStatus() { return availableCheckStatus; }
    public void setAvailableCheckStatus(int availableCheckStatus) { this.availableCheckStatus = availableCheckStatus; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getVolume() { return volume; }
    public void setVolume(double volume) { this.volume = volume; }

    public String getProducerName() { return producerName; }
    public void setProducerName(String producerName) { this.producerName = producerName; }

    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }

    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }
}
