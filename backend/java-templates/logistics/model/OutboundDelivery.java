package com.company.IntelligentPlatform.logistics.model;

import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinLogistics - OutboundDelivery (extends Delivery)
 * Table: OutboundDelivery (schema: logistics)
 */
@Entity
@Table(name = "OutboundDelivery", schema = "logistics")
public class OutboundDelivery extends Delivery {

    @Column(name = "grossOutboundFee")
    private double grossOutboundFee;

    @Column(name = "grossStorageFee")
    private double grossStorageFee;

    public double getGrossOutboundFee() { return grossOutboundFee; }
    public void setGrossOutboundFee(double grossOutboundFee) { this.grossOutboundFee = grossOutboundFee; }

    public double getGrossStorageFee() { return grossStorageFee; }
    public void setGrossStorageFee(double grossStorageFee) { this.grossStorageFee = grossStorageFee; }
}
