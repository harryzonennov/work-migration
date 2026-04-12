package com.company.IntelligentPlatform.common.model;

import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinPlatform - DocMatItemNode.java
 *
 * Document material item node — the base class for all line-item nodes
 * that belong to a document (e.g. SalesContractMaterialItem).
 *
 * Hierarchy: ServiceEntityNode → ReferenceNode → DocMatItemNode
 */
@MappedSuperclass
public abstract class DocMatItemNode extends ReferenceNode {

    // Reservation fields
    @Column(name = "reservedMatItemUUID")
    protected String reservedMatItemUUID;

    @Column(name = "reservedDocType")
    protected int reservedDocType;

    @Column(name = "reservedDocMatItemArrayUUID")
    protected String reservedDocMatItemArrayUUID;

    @Column(name = "reserveTargetMatItemUUID")
    protected String reserveTargetMatItemUUID;

    @Column(name = "reserveTargetDocType")
    protected int reserveTargetDocType;

    @Column(name = "itemStatus")
    protected int itemStatus;

    @Column(name = "reserveTargetDocMatItemArrayUUID")
    protected String reserveTargetDocMatItemArrayUUID;

    // Document chain item links (item-level, not document-level)
    @Column(name = "prevDocType")
    protected int prevDocType;

    @Column(name = "prevDocMatItemUUID")
    protected String prevDocMatItemUUID;

    @Column(name = "prevDocMatItemArrayUUID")
    protected String prevDocMatItemArrayUUID;

    @Column(name = "nextDocType")
    protected int nextDocType;

    @Column(name = "nextDocMatItemUUID")
    protected String nextDocMatItemUUID;

    @Column(name = "nextDocMatItemArrayUUID")
    protected String nextDocMatItemArrayUUID;

    // Material and price fields
    @Column(name = "amount")
    protected double amount;

    @Column(name = "refUnitUUID")
    protected String refUnitUUID;

    @Column(name = "refMaterialSKUUUID")
    protected String refMaterialSKUUUID;

    @Column(name = "itemPrice")
    protected double itemPrice;

    @Column(name = "unitPrice")
    protected double unitPrice;

    @Column(name = "itemPriceDisplay")
    protected double itemPriceDisplay;

    @Column(name = "unitPriceDisplay")
    protected double unitPriceDisplay;

    @Column(name = "currencyCode")
    protected String currencyCode;

    @Column(name = "refFinMatItemUUID")
    protected String refFinMatItemUUID;

    @Column(name = "productionBatchNumber")
    protected String productionBatchNumber;

    @Column(name = "purchaseBatchNumber")
    protected String purchaseBatchNumber;

    @Column(name = "materialStatus")
    protected int materialStatus;

    @Column(name = "homeDocumentType")
    protected int homeDocumentType;

    // Professional document chain links (item-level)
    @Column(name = "prevProfDocType")
    protected int prevProfDocType;

    @Column(name = "prevProfDocMatItemUUID")
    protected String prevProfDocMatItemUUID;

    @Column(name = "prevProfDocMatItemArrayUUID")
    protected String prevProfDocMatItemArrayUUID;

    @Column(name = "nextProfDocType")
    protected int nextProfDocType;

    @Column(name = "nextProfDocMatItemUUID")
    protected String nextProfDocMatItemUUID;

    @Column(name = "nextProfDocMatItemArrayUUID")
    protected String nextProfDocMatItemArrayUUID;

    public String getReservedMatItemUUID() { return reservedMatItemUUID; }
    public void setReservedMatItemUUID(String reservedMatItemUUID) { this.reservedMatItemUUID = reservedMatItemUUID; }

    public int getReservedDocType() { return reservedDocType; }
    public void setReservedDocType(int reservedDocType) { this.reservedDocType = reservedDocType; }

    public String getReservedDocMatItemArrayUUID() { return reservedDocMatItemArrayUUID; }
    public void setReservedDocMatItemArrayUUID(String reservedDocMatItemArrayUUID) { this.reservedDocMatItemArrayUUID = reservedDocMatItemArrayUUID; }

    public String getReserveTargetMatItemUUID() { return reserveTargetMatItemUUID; }
    public void setReserveTargetMatItemUUID(String reserveTargetMatItemUUID) { this.reserveTargetMatItemUUID = reserveTargetMatItemUUID; }

    public int getReserveTargetDocType() { return reserveTargetDocType; }
    public void setReserveTargetDocType(int reserveTargetDocType) { this.reserveTargetDocType = reserveTargetDocType; }

    public int getItemStatus() { return itemStatus; }
    public void setItemStatus(int itemStatus) { this.itemStatus = itemStatus; }

    public String getReserveTargetDocMatItemArrayUUID() { return reserveTargetDocMatItemArrayUUID; }
    public void setReserveTargetDocMatItemArrayUUID(String v) { this.reserveTargetDocMatItemArrayUUID = v; }

    public int getPrevDocType() { return prevDocType; }
    public void setPrevDocType(int prevDocType) { this.prevDocType = prevDocType; }

    public String getPrevDocMatItemUUID() { return prevDocMatItemUUID; }
    public void setPrevDocMatItemUUID(String prevDocMatItemUUID) { this.prevDocMatItemUUID = prevDocMatItemUUID; }

    public String getPrevDocMatItemArrayUUID() { return prevDocMatItemArrayUUID; }
    public void setPrevDocMatItemArrayUUID(String prevDocMatItemArrayUUID) { this.prevDocMatItemArrayUUID = prevDocMatItemArrayUUID; }

    public int getNextDocType() { return nextDocType; }
    public void setNextDocType(int nextDocType) { this.nextDocType = nextDocType; }

    public String getNextDocMatItemUUID() { return nextDocMatItemUUID; }
    public void setNextDocMatItemUUID(String nextDocMatItemUUID) { this.nextDocMatItemUUID = nextDocMatItemUUID; }

    public String getNextDocMatItemArrayUUID() { return nextDocMatItemArrayUUID; }
    public void setNextDocMatItemArrayUUID(String nextDocMatItemArrayUUID) { this.nextDocMatItemArrayUUID = nextDocMatItemArrayUUID; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getRefUnitUUID() { return refUnitUUID; }
    public void setRefUnitUUID(String refUnitUUID) { this.refUnitUUID = refUnitUUID; }

    public String getRefMaterialSKUUUID() { return refMaterialSKUUUID; }
    public void setRefMaterialSKUUUID(String refMaterialSKUUUID) { this.refMaterialSKUUUID = refMaterialSKUUUID; }

    public double getItemPrice() { return itemPrice; }
    public void setItemPrice(double itemPrice) { this.itemPrice = itemPrice; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getItemPriceDisplay() { return itemPriceDisplay; }
    public void setItemPriceDisplay(double itemPriceDisplay) { this.itemPriceDisplay = itemPriceDisplay; }

    public double getUnitPriceDisplay() { return unitPriceDisplay; }
    public void setUnitPriceDisplay(double unitPriceDisplay) { this.unitPriceDisplay = unitPriceDisplay; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getRefFinMatItemUUID() { return refFinMatItemUUID; }
    public void setRefFinMatItemUUID(String refFinMatItemUUID) { this.refFinMatItemUUID = refFinMatItemUUID; }

    public String getProductionBatchNumber() { return productionBatchNumber; }
    public void setProductionBatchNumber(String productionBatchNumber) { this.productionBatchNumber = productionBatchNumber; }

    public String getPurchaseBatchNumber() { return purchaseBatchNumber; }
    public void setPurchaseBatchNumber(String purchaseBatchNumber) { this.purchaseBatchNumber = purchaseBatchNumber; }

    public int getMaterialStatus() { return materialStatus; }
    public void setMaterialStatus(int materialStatus) { this.materialStatus = materialStatus; }

    public int getHomeDocumentType() { return homeDocumentType; }
    public void setHomeDocumentType(int homeDocumentType) { this.homeDocumentType = homeDocumentType; }

    public int getPrevProfDocType() { return prevProfDocType; }
    public void setPrevProfDocType(int prevProfDocType) { this.prevProfDocType = prevProfDocType; }

    public String getPrevProfDocMatItemUUID() { return prevProfDocMatItemUUID; }
    public void setPrevProfDocMatItemUUID(String prevProfDocMatItemUUID) { this.prevProfDocMatItemUUID = prevProfDocMatItemUUID; }

    public String getPrevProfDocMatItemArrayUUID() { return prevProfDocMatItemArrayUUID; }
    public void setPrevProfDocMatItemArrayUUID(String prevProfDocMatItemArrayUUID) { this.prevProfDocMatItemArrayUUID = prevProfDocMatItemArrayUUID; }

    public int getNextProfDocType() { return nextProfDocType; }
    public void setNextProfDocType(int nextProfDocType) { this.nextProfDocType = nextProfDocType; }

    public String getNextProfDocMatItemUUID() { return nextProfDocMatItemUUID; }
    public void setNextProfDocMatItemUUID(String nextProfDocMatItemUUID) { this.nextProfDocMatItemUUID = nextProfDocMatItemUUID; }

    public String getNextProfDocMatItemArrayUUID() { return nextProfDocMatItemArrayUUID; }
    public void setNextProfDocMatItemArrayUUID(String nextProfDocMatItemArrayUUID) { this.nextProfDocMatItemArrayUUID = nextProfDocMatItemArrayUUID; }
}
