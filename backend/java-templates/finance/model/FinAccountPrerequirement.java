package com.company.IntelligentPlatform.finance.model;

import com.company.IntelligentPlatform.common.model.ReferenceNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinFinance - FinAccountPrerequirement.java
 * New table: FinAccountPrerequirement (schema: finance)
 * A pre-condition that must be recorded-done before a FinAccount can proceed.
 */
@Entity
@Table(name = "FinAccountPrerequirement", schema = "finance")
public class FinAccountPrerequirement extends ReferenceNode {

    public static final int RQTYPE_UNLOCK_RECORD = 1;

    @Column(name = "requireType")
    private int requireType;

    public int getRequireType() { return requireType; }
    public void setRequireType(int requireType) { this.requireType = requireType; }
}
