package com.company.IntelligentPlatform.finance.model;

import com.company.IntelligentPlatform.common.model.ReferenceNode;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Migrated from: ThorsteinFinance - FinAccountSettleItem.java
 * New table: FinAccountSettleItem (schema: finance)
 * Settlement record for a FinAccount against an account object.
 */
@Entity
@Table(name = "FinAccountSettleItem", schema = "finance")
public class FinAccountSettleItem extends ReferenceNode {

    @Column(name = "settleAccountValue")
    private double settleAccountValue;

    @Column(name = "refAccObjectUUID")
    private String refAccObjectUUID;

    @Column(name = "accountType")
    private int accountType;

    @Column(name = "auditStatus")
    private int auditStatus;

    @Column(name = "auditBy")
    private String auditBy;

    @Column(name = "auditTime")
    private LocalDateTime auditTime;

    @Column(name = "auditLockFlag")
    private boolean auditLockFlag;

    @Column(name = "auditLockMSG")
    private String auditLockMSG;

    @Column(name = "auditNote")
    private String auditNote;

    @Column(name = "verifyStatus")
    private int verifyStatus;

    @Column(name = "verifyBy")
    private String verifyBy;

    @Column(name = "verifyTime")
    private LocalDateTime verifyTime;

    @Column(name = "verifyLockFlag")
    private boolean verifyLockFlag;

    @Column(name = "verifyLockMSG")
    private String verifyLockMSG;

    @Column(name = "verifyNote")
    private String verifyNote;

    @Column(name = "recordStatus")
    private int recordStatus;

    @Column(name = "recordBy")
    private String recordBy;

    @Column(name = "recordTime")
    private LocalDateTime recordTime;

    @Column(name = "recordLockFlag")
    private boolean recordLockFlag;

    @Column(name = "recordLockMSG")
    private String recordLockMSG;

    @Column(name = "recordNote")
    private String recordNote;

    public double getSettleAccountValue() { return settleAccountValue; }
    public void setSettleAccountValue(double settleAccountValue) { this.settleAccountValue = settleAccountValue; }

    public String getRefAccObjectUUID() { return refAccObjectUUID; }
    public void setRefAccObjectUUID(String refAccObjectUUID) { this.refAccObjectUUID = refAccObjectUUID; }

    public int getAccountType() { return accountType; }
    public void setAccountType(int accountType) { this.accountType = accountType; }

    public int getAuditStatus() { return auditStatus; }
    public void setAuditStatus(int auditStatus) { this.auditStatus = auditStatus; }

    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }

    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }

    public boolean isAuditLockFlag() { return auditLockFlag; }
    public void setAuditLockFlag(boolean auditLockFlag) { this.auditLockFlag = auditLockFlag; }

    public String getAuditLockMSG() { return auditLockMSG; }
    public void setAuditLockMSG(String auditLockMSG) { this.auditLockMSG = auditLockMSG; }

    public String getAuditNote() { return auditNote; }
    public void setAuditNote(String auditNote) { this.auditNote = auditNote; }

    public int getVerifyStatus() { return verifyStatus; }
    public void setVerifyStatus(int verifyStatus) { this.verifyStatus = verifyStatus; }

    public String getVerifyBy() { return verifyBy; }
    public void setVerifyBy(String verifyBy) { this.verifyBy = verifyBy; }

    public LocalDateTime getVerifyTime() { return verifyTime; }
    public void setVerifyTime(LocalDateTime verifyTime) { this.verifyTime = verifyTime; }

    public boolean isVerifyLockFlag() { return verifyLockFlag; }
    public void setVerifyLockFlag(boolean verifyLockFlag) { this.verifyLockFlag = verifyLockFlag; }

    public String getVerifyLockMSG() { return verifyLockMSG; }
    public void setVerifyLockMSG(String verifyLockMSG) { this.verifyLockMSG = verifyLockMSG; }

    public String getVerifyNote() { return verifyNote; }
    public void setVerifyNote(String verifyNote) { this.verifyNote = verifyNote; }

    public int getRecordStatus() { return recordStatus; }
    public void setRecordStatus(int recordStatus) { this.recordStatus = recordStatus; }

    public String getRecordBy() { return recordBy; }
    public void setRecordBy(String recordBy) { this.recordBy = recordBy; }

    public LocalDateTime getRecordTime() { return recordTime; }
    public void setRecordTime(LocalDateTime recordTime) { this.recordTime = recordTime; }

    public boolean isRecordLockFlag() { return recordLockFlag; }
    public void setRecordLockFlag(boolean recordLockFlag) { this.recordLockFlag = recordLockFlag; }

    public String getRecordLockMSG() { return recordLockMSG; }
    public void setRecordLockMSG(String recordLockMSG) { this.recordLockMSG = recordLockMSG; }

    public String getRecordNote() { return recordNote; }
    public void setRecordNote(String recordNote) { this.recordNote = recordNote; }
}
