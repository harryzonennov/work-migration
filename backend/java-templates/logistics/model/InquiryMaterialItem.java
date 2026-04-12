package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.DocMatItemNode;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Migrated from: ThorsteinLogistics - InquiryMaterialItem (extends DocMatItemNode)
 * Table: InquiryMaterialItem (schema: logistics)
 */
@Entity
@Table(name = "InquiryMaterialItem", schema = "logistics")
public class InquiryMaterialItem extends DocMatItemNode {

    @Column(name = "shippingPoint")
    private String shippingPoint;

    @Column(name = "requireShippingTime")
    private LocalDateTime requireShippingTime;

    @Column(name = "itemStatus")
    private int itemStatus;

    @Column(name = "refUnitName")
    private String refUnitName;

    @Column(name = "firstUnitPrice")
    private double firstUnitPrice;

    @Column(name = "firstItemPrice")
    private double firstItemPrice;

    @Column(name = "currencyCode")
    private String currencyCode;

    public String getShippingPoint() { return shippingPoint; }
    public void setShippingPoint(String shippingPoint) { this.shippingPoint = shippingPoint; }

    public LocalDateTime getRequireShippingTime() { return requireShippingTime; }
    public void setRequireShippingTime(LocalDateTime requireShippingTime) { this.requireShippingTime = requireShippingTime; }

    public int getItemStatus() { return itemStatus; }
    public void setItemStatus(int itemStatus) { this.itemStatus = itemStatus; }

    public String getRefUnitName() { return refUnitName; }
    public void setRefUnitName(String refUnitName) { this.refUnitName = refUnitName; }

    public double getFirstUnitPrice() { return firstUnitPrice; }
    public void setFirstUnitPrice(double firstUnitPrice) { this.firstUnitPrice = firstUnitPrice; }

    public double getFirstItemPrice() { return firstItemPrice; }
    public void setFirstItemPrice(double firstItemPrice) { this.firstItemPrice = firstItemPrice; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
}
