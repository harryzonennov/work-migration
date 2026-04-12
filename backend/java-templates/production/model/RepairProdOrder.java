package com.company.IntelligentPlatform.production.model;

import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinProduction - RepairProdOrder (extends ProductionOrder)
 * Table: RepairProdOrder (schema: production)
 */
@Entity
@Table(name = "RepairProdOrder", schema = "production")
public class RepairProdOrder extends ProductionOrder {

    public static final int GENITEM_MODE_NO_NEED   = 1;
    public static final int GENITEM_MODE_ADD_MANUAL = 2;
}
