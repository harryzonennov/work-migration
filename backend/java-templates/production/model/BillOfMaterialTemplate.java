package com.company.IntelligentPlatform.production.model;

import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinProduction - BillOfMaterialTemplate (extends BillOfMaterialOrder)
 * Table: BillOfMaterialTemplate (schema: production)
 *
 * Reusable BOM template that BillOfMaterialOrder instances can reference via refTemplateUUID.
 */
@Entity
@Table(name = "BillOfMaterialTemplate", schema = "production")
public class BillOfMaterialTemplate extends BillOfMaterialOrder {
}
