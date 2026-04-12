package com.company.IntelligentPlatform.common.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Migrated from: ThorsteinPlatform - CorporateCustomer.java
 * Old table: CorporateCustomer (single shared DB)
 * New table: CorporateCustomer (schema: platform)
 * Hierarchy: ServiceEntityNode → Account → CorporateAccount → CorporateCustomer
 */
@Entity
@Table(name = "CorporateCustomer", schema = "platform")
public class CorporateCustomer extends CorporateAccount {

    public static final int STATUS_INITIAL  = 1;
    public static final int STATUS_INUSE    = 305;
    public static final int STATUS_ARCHIVED = 980;

    public static final int CUSTOMERTYPE_STANDARD    = 1;
    public static final int CUSTOMERTYPE_DISTRIBUTOR = 2;
    public static final int CUSTOMERTYPE_DEALER      = 3;
    public static final int CUSTOMERTYPE_SUPPLIER    = 4;
    public static final int CUSTOMERTYPE_OTHERS      = 5;

    public static final int SUB_DISTRTYPE_LEVE1 = 1;
    public static final int SUB_DISTRTYPE_LEVE2 = 2;
    public static final int SUB_DISTRTYPE_LEVE3 = 3;

    @Column(name = "status")
    private int status;

    @Column(name = "customerType")
    private int customerType;

    @Column(name = "customerLevel")
    private int customerLevel;

    @Column(name = "subDistributorType")
    private int subDistributorType;

    @Column(name = "baseCityUUID")
    private String baseCityUUID;

    // Cross-module ref — UUID only, no FK
    @Column(name = "refSalesAreaUUID")
    private String refSalesAreaUUID;

    @Column(name = "weiboID")
    private String weiboID;

    @Column(name = "weiXinID")
    private String weiXinID;

    @Column(name = "faceBookID")
    private String faceBookID;

    @Column(name = "systemDefault")
    private boolean systemDefault;

    @Column(name = "retireReason")
    private String retireReason;

    @Column(name = "retireDate")
    private LocalDate retireDate;

    @Column(name = "launchReason")
    private String launchReason;

    @Column(name = "launchDate")
    private LocalDate launchDate;

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public int getCustomerType() { return customerType; }
    public void setCustomerType(int customerType) { this.customerType = customerType; }

    public int getCustomerLevel() { return customerLevel; }
    public void setCustomerLevel(int customerLevel) { this.customerLevel = customerLevel; }

    public int getSubDistributorType() { return subDistributorType; }
    public void setSubDistributorType(int subDistributorType) { this.subDistributorType = subDistributorType; }

    public String getBaseCityUUID() { return baseCityUUID; }
    public void setBaseCityUUID(String baseCityUUID) { this.baseCityUUID = baseCityUUID; }

    public String getRefSalesAreaUUID() { return refSalesAreaUUID; }
    public void setRefSalesAreaUUID(String refSalesAreaUUID) { this.refSalesAreaUUID = refSalesAreaUUID; }

    public String getWeiboID() { return weiboID; }
    public void setWeiboID(String weiboID) { this.weiboID = weiboID; }

    public String getWeiXinID() { return weiXinID; }
    public void setWeiXinID(String weiXinID) { this.weiXinID = weiXinID; }

    public String getFaceBookID() { return faceBookID; }
    public void setFaceBookID(String faceBookID) { this.faceBookID = faceBookID; }

    public boolean getSystemDefault() { return systemDefault; }
    public void setSystemDefault(boolean systemDefault) { this.systemDefault = systemDefault; }

    public String getRetireReason() { return retireReason; }
    public void setRetireReason(String retireReason) { this.retireReason = retireReason; }

    public LocalDate getRetireDate() { return retireDate; }
    public void setRetireDate(LocalDate retireDate) { this.retireDate = retireDate; }

    public String getLaunchReason() { return launchReason; }
    public void setLaunchReason(String launchReason) { this.launchReason = launchReason; }

    public LocalDate getLaunchDate() { return launchDate; }
    public void setLaunchDate(LocalDate launchDate) { this.launchDate = launchDate; }
}
