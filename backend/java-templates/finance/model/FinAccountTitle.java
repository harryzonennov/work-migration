package com.company.IntelligentPlatform.finance.model;

import com.company.IntelligentPlatform.common.model.ServiceEntityNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinFinance - FinAccountTitle.java
 * New table: FinAccountTitle (schema: finance)
 * Hierarchy: ServiceEntityNode → FinAccountTitle
 * Chart-of-accounts hierarchy node — uses parentAccountTitleUUID for tree structure.
 */
@Entity
@Table(name = "FinAccountTitle", schema = "finance")
public class FinAccountTitle extends ServiceEntityNode {

    public static final int FIN_ACCOUNTTYPE_DEBIT  = 1;
    public static final int FIN_ACCOUNTTYPE_CREDIT = 2;

    public static final int CATEGORY_GENERAL    = 1;
    public static final int CATEGORY_SECONDARY  = 2;
    public static final int CATEGORY_SUBSIDIARY = 3;
    public static final int CATEGORY_FOURTH     = 4;
    public static final int CATEGORY_FIFTH      = 5;

    public static final int ORIGINALTYPE_STANDARD = 1;
    public static final int ORIGINALTYPE_CUST     = 2;

    public static final int SETTLETYPE_GENERAL = 1;
    public static final int SETTLETYPE_MONTHLY = 2;

    public static final int GENTYPE_NONE               = 1;
    public static final int GENTYPE_VEHICLERUNORDER    = 2;
    public static final int GENTYPE_VEHICLERUNCONTRACT = 3;
    public static final int GENTYPE_BOOKINGNOTE        = 4;
    public static final int GENTYPE_VOUCHER            = 5;

    @Column(name = "finAccountType")
    private int finAccountType;

    @Column(name = "generateType")
    private int generateType;

    @Column(name = "category")
    private int category;

    @Column(name = "parentAccountTitleUUID")
    private String parentAccountTitleUUID;

    @Column(name = "rootAccountTitleUUID")
    private String rootAccountTitleUUID;

    @Column(name = "originalType")
    private int originalType;

    @Column(name = "settleType")
    private int settleType;

    public int getFinAccountType() { return finAccountType; }
    public void setFinAccountType(int finAccountType) { this.finAccountType = finAccountType; }

    public int getGenerateType() { return generateType; }
    public void setGenerateType(int generateType) { this.generateType = generateType; }

    public int getCategory() { return category; }
    public void setCategory(int category) { this.category = category; }

    public String getParentAccountTitleUUID() { return parentAccountTitleUUID; }
    public void setParentAccountTitleUUID(String parentAccountTitleUUID) { this.parentAccountTitleUUID = parentAccountTitleUUID; }

    public String getRootAccountTitleUUID() { return rootAccountTitleUUID; }
    public void setRootAccountTitleUUID(String rootAccountTitleUUID) { this.rootAccountTitleUUID = rootAccountTitleUUID; }

    public int getOriginalType() { return originalType; }
    public void setOriginalType(int originalType) { this.originalType = originalType; }

    public int getSettleType() { return settleType; }
    public void setSettleType(int settleType) { this.settleType = settleType; }
}
