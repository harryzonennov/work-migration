package com.company.IntelligentPlatform.common.model;

import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinPlatform - Organization.java
 * Old table: Organization (single shared DB)
 * New table: Organization (schema: platform)
 * Hierarchy: ServiceEntityNode → Account → CorporateAccount → Organization
 */
@Entity
@Table(name = "Organization", schema = "platform")
public class Organization extends CorporateAccount {

    public static final int ORGFUNCTION_HOSTCOMPANY  = 1;
    public static final int ORGFUNCTION_SUBSIDIARY   = 2;
    public static final int ORGFUNCTION_SALES_DEPT   = 3;
    public static final int ORGFUNCTION_PROD_DEPT    = 4;
    public static final int ORGFUNCTION_DEV_DEPT     = 5;
    public static final int ORGFUNCTION_TRANSSITE    = 6;
    public static final int ORGFUNCTION_SUPPORT_DEPT = 7;
    public static final int ORGFUNCTION_PURCHASE     = 8;
    public static final int ORGFUNCTION_FINANCE      = 9;

    public static final int ORGAN_TYPE_TRANSSITE = 5;

    @Column(name = "parentOrganizationUUID")
    private String parentOrganizationUUID;

    @Column(name = "organType")
    private int organType;

    @Column(name = "organLevel")
    private int organLevel;

    @Column(name = "organizationFunction")
    private int organizationFunction;

    @Column(name = "refOrganizationFunction")
    private String refOrganizationFunction;

    @Column(name = "mainContactUUID")
    private String mainContactUUID;

    // Cross-module refs — UUID only, no FK
    @Column(name = "refCashierUUID")
    private String refCashierUUID;

    @Column(name = "refAccountantUUID")
    private String refAccountantUUID;

    @Column(name = "refFinOrgUUID")
    private String refFinOrgUUID;

    public String getParentOrganizationUUID() { return parentOrganizationUUID; }
    public void setParentOrganizationUUID(String parentOrganizationUUID) { this.parentOrganizationUUID = parentOrganizationUUID; }

    public int getOrganType() { return organType; }
    public void setOrganType(int organType) { this.organType = organType; }

    public int getOrganLevel() { return organLevel; }
    public void setOrganLevel(int organLevel) { this.organLevel = organLevel; }

    public int getOrganizationFunction() { return organizationFunction; }
    public void setOrganizationFunction(int organizationFunction) { this.organizationFunction = organizationFunction; }

    public String getRefOrganizationFunction() { return refOrganizationFunction; }
    public void setRefOrganizationFunction(String refOrganizationFunction) { this.refOrganizationFunction = refOrganizationFunction; }

    public String getMainContactUUID() { return mainContactUUID; }
    public void setMainContactUUID(String mainContactUUID) { this.mainContactUUID = mainContactUUID; }

    public String getRefCashierUUID() { return refCashierUUID; }
    public void setRefCashierUUID(String refCashierUUID) { this.refCashierUUID = refCashierUUID; }

    public String getRefAccountantUUID() { return refAccountantUUID; }
    public void setRefAccountantUUID(String refAccountantUUID) { this.refAccountantUUID = refAccountantUUID; }

    public String getRefFinOrgUUID() { return refFinOrgUUID; }
    public void setRefFinOrgUUID(String refFinOrgUUID) { this.refFinOrgUUID = refFinOrgUUID; }
}
