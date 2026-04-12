package com.company.IntelligentPlatform.production.model;

import com.company.IntelligentPlatform.common.model.DocumentContent;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinProduction - BillOfMaterialOrder (extends DocumentContent)
 * Table: BillOfMaterialOrder (schema: production)
 */
@Entity
@Table(name = "BillOfMaterialOrder", schema = "production")
public class BillOfMaterialOrder extends DocumentContent {

    public static final int STATUS_INITIAL  = 1;
    public static final int STATUS_INUSE    = 2;
    public static final int STATUS_RETIRED  = 3;

    public static final int LEAD_CAL_MODE_MAT     = 1;
    public static final int LEAD_CAL_MODE_PROCESS = 2;

    @Column(name = "refMaterialSKUUUID")
    private String refMaterialSKUUUID;

    @Column(name = "amount")
    private double amount;

    @Column(name = "refUnitUUID")
    private String refUnitUUID;

    @Column(name = "itemCategory")
    private int itemCategory;

    @Column(name = "refRouteOrderUUID")
    private String refRouteOrderUUID;

    @Column(name = "leadTimeCalMode")
    private int leadTimeCalMode;

    @Column(name = "refWocUUID")
    private String refWocUUID;

    @Column(name = "versionNumber")
    private int versionNumber;

    @Column(name = "patchNumber")
    private int patchNumber;

    @Column(name = "refTemplateUUID")
    private String refTemplateUUID;

    public String getRefMaterialSKUUUID() { return refMaterialSKUUUID; }
    public void setRefMaterialSKUUUID(String refMaterialSKUUUID) { this.refMaterialSKUUUID = refMaterialSKUUUID; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getRefUnitUUID() { return refUnitUUID; }
    public void setRefUnitUUID(String refUnitUUID) { this.refUnitUUID = refUnitUUID; }
    public int getItemCategory() { return itemCategory; }
    public void setItemCategory(int itemCategory) { this.itemCategory = itemCategory; }
    public String getRefRouteOrderUUID() { return refRouteOrderUUID; }
    public void setRefRouteOrderUUID(String refRouteOrderUUID) { this.refRouteOrderUUID = refRouteOrderUUID; }
    public int getLeadTimeCalMode() { return leadTimeCalMode; }
    public void setLeadTimeCalMode(int leadTimeCalMode) { this.leadTimeCalMode = leadTimeCalMode; }
    public String getRefWocUUID() { return refWocUUID; }
    public void setRefWocUUID(String refWocUUID) { this.refWocUUID = refWocUUID; }
    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }
    public int getPatchNumber() { return patchNumber; }
    public void setPatchNumber(int patchNumber) { this.patchNumber = patchNumber; }
    public String getRefTemplateUUID() { return refTemplateUUID; }
    public void setRefTemplateUUID(String refTemplateUUID) { this.refTemplateUUID = refTemplateUUID; }
}
