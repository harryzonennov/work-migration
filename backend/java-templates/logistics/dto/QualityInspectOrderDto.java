package com.company.IntelligentPlatform.logistics.dto;

import com.company.IntelligentPlatform.logistics.model.QualityInspectOrder;
import java.time.LocalDate;

/**
 * DTO for QualityInspectOrder create/update requests.
 */
public class QualityInspectOrderDto {

    private String name;
    private String client;
    private int inspectType;
    private int reservedDocType;
    private String reservedDocUUID;
    private int category;
    private double grossPrice;
    private String checkResult;
    private String refWarehouseUUID;
    private String refWarehouseAreaUUID;
    private String purchaseBatchNumber;
    private String productionBatchNumber;
    private String note;

    public QualityInspectOrder toEntity() {
        QualityInspectOrder order = new QualityInspectOrder();
        applyTo(order);
        return order;
    }

    public void applyTo(QualityInspectOrder order) {
        if (name != null)                   order.setName(name);
        if (client != null)                 order.setClient(client);
        if (reservedDocUUID != null)        order.setReservedDocUUID(reservedDocUUID);
        if (checkResult != null)            order.setCheckResult(checkResult);
        if (refWarehouseUUID != null)       order.setRefWarehouseUUID(refWarehouseUUID);
        if (refWarehouseAreaUUID != null)   order.setRefWarehouseAreaUUID(refWarehouseAreaUUID);
        if (purchaseBatchNumber != null)    order.setPurchaseBatchNumber(purchaseBatchNumber);
        if (productionBatchNumber != null)  order.setProductionBatchNumber(productionBatchNumber);
        if (note != null)                   order.setNote(note);
        order.setInspectType(inspectType);
        order.setReservedDocType(reservedDocType);
        order.setCategory(category);
        order.setGrossPrice(grossPrice);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }
    public int getInspectType() { return inspectType; }
    public void setInspectType(int inspectType) { this.inspectType = inspectType; }
    public int getReservedDocType() { return reservedDocType; }
    public void setReservedDocType(int reservedDocType) { this.reservedDocType = reservedDocType; }
    public String getReservedDocUUID() { return reservedDocUUID; }
    public void setReservedDocUUID(String reservedDocUUID) { this.reservedDocUUID = reservedDocUUID; }
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
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
