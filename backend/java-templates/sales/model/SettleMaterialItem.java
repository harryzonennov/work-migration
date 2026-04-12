package com.company.IntelligentPlatform.sales.model;

import com.company.IntelligentPlatform.common.model.ReferenceNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinSalesDistribution - SettleMaterialItem (extends ReferenceNode)
 * Table: SettleMaterialItem (schema: sales)
 *
 * Line item within a SettleOrder. refSourceMaterialItemUUID points back
 * to the SalesContractMaterialItem or SalesReturnMaterialItem being settled.
 */
@Entity
@Table(name = "SettleMaterialItem", schema = "sales")
public class SettleMaterialItem extends ReferenceNode {

    @Column(name = "settlePrice")
    private double settlePrice;

    @Column(name = "settleAmount")
    private double settleAmount;

    @Column(name = "currencyCode")
    private String currencyCode;

    @Column(name = "refSourceMaterialItemUUID")
    private String refSourceMaterialItemUUID;

    @Column(name = "itemStatus")
    private int itemStatus;

    public double getSettlePrice() { return settlePrice; }
    public void setSettlePrice(double settlePrice) { this.settlePrice = settlePrice; }

    public double getSettleAmount() { return settleAmount; }
    public void setSettleAmount(double settleAmount) { this.settleAmount = settleAmount; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getRefSourceMaterialItemUUID() { return refSourceMaterialItemUUID; }
    public void setRefSourceMaterialItemUUID(String refSourceMaterialItemUUID) { this.refSourceMaterialItemUUID = refSourceMaterialItemUUID; }

    public int getItemStatus() { return itemStatus; }
    public void setItemStatus(int itemStatus) { this.itemStatus = itemStatus; }
}
