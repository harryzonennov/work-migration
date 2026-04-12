package com.company.IntelligentPlatform.production.model;

import com.company.IntelligentPlatform.common.model.ServiceEntityNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinProduction - ProductionResourceUnion (extends ServiceEntityNode)
 * Table: ProductionResourceUnion (schema: production)
 *
 * Represents a resource (machine, person, tool) attached to a work center.
 */
@Entity
@Table(name = "ProductionResourceUnion", schema = "production")
public class ProductionResourceUnion extends ServiceEntityNode {

    public static final int RESOURCE_TYPE_MACHINE = 1;
    public static final int RESOURCE_TYPE_PERSON  = 2;
    public static final int RESOURCE_TYPE_TOOL    = 3;

    @Column(name = "utilizationRate")
    private double utilizationRate;

    @Column(name = "efficiency")
    private double efficiency;

    @Column(name = "resourceType")
    private int resourceType;

    @Column(name = "dailyShift")
    private double dailyShift;

    @Column(name = "costInhour")
    private double costInhour;

    public double getUtilizationRate() { return utilizationRate; }
    public void setUtilizationRate(double utilizationRate) { this.utilizationRate = utilizationRate; }
    public double getEfficiency() { return efficiency; }
    public void setEfficiency(double efficiency) { this.efficiency = efficiency; }
    public int getResourceType() { return resourceType; }
    public void setResourceType(int resourceType) { this.resourceType = resourceType; }
    public double getDailyShift() { return dailyShift; }
    public void setDailyShift(double dailyShift) { this.dailyShift = dailyShift; }
    public double getCostInhour() { return costInhour; }
    public void setCostInhour(double costInhour) { this.costInhour = costInhour; }
}
