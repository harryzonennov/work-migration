package com.company.IntelligentPlatform.common.model;

import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinPlatform - LogonUserOrgReference.java
 * Old table: LogonUserOrgReference (single shared DB)
 * New table: LogonUserOrgReference (schema: platform)
 * Hierarchy: ServiceEntityNode → ReferenceNode → LogonUserOrgReference
 * Links a LogonUser to an Organization with a work role (refUUID = Organization.uuid)
 */
@Entity
@Table(name = "LogonUserOrgReference", schema = "platform")
public class LogonUserOrgReference extends ReferenceNode {

    public static final int ORGROLE_NOR_USER        = 1;
    public static final int ORGROLE_BOARD_CHAIRMAN  = 2;
    public static final int ORGROLE_ORG_MAN         = 3;
    public static final int ORGROLE_ORG_VICEMAN     = 4;
    public static final int ORGROLE_FIN_ACC         = 5;
    public static final int ORGROLE_FIN_CASH        = 6;
    public static final int ORGROLE_QE_INST         = 7;

    @Column(name = "workRole")
    private int workRole;

    public int getWorkRole() { return workRole; }
    public void setWorkRole(int workRole) { this.workRole = workRole; }
}
