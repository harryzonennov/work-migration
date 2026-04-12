package com.company.IntelligentPlatform.logistics.model;

import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinLogistics - InboundDelivery (extends Delivery)
 * Table: InboundDelivery (schema: logistics)
 */
@Entity
@Table(name = "InboundDelivery", schema = "logistics")
public class InboundDelivery extends Delivery {

    @Column(name = "grossInboundFee")
    private double grossInboundFee;

    public double getGrossInboundFee() { return grossInboundFee; }
    public void setGrossInboundFee(double grossInboundFee) { this.grossInboundFee = grossInboundFee; }
}
