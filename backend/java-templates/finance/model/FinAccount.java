package com.company.IntelligentPlatform.finance.model;

import com.company.IntelligentPlatform.common.model.DocumentContent;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Migrated from: ThorsteinFinance - FinAccount.java / FinAccount.hbm.xml
 * New table: FinAccount (schema: finance)
 * Hierarchy: ServiceEntityNode → DocumentContent → FinAccount
 *
 * FinAccount has a tri-status approval workflow (audit → record → verify)
 * separate from the base document status field.
 */
@Entity
@Table(name = "FinAccount", schema = "finance")
public class FinAccount extends DocumentContent {

    // --- tri-step approval status constants ---
    public static final int AUDIT_UNDONE    = 1;
    public static final int AUDIT_DONE      = 2;
    public static final int AUDIT_REJECT    = 3;

    public static final int VERIFIED_UNDONE = 1;
    public static final int VERIFIED_DONE   = 2;

    public static final int RECORDED_UNDONE = 1;
    public static final int RECORDED_DONE   = 2;

    // --- payment type ---
    public static final int PAYMENT_CASH = 1;
    public static final int PAYMENT_BANK = 2;

    // --- adjust direction ---
    public static final int ADJUST_DISCOUNT = 1;
    public static final int ADJUST_INCREASE = 2;

    @Column(name = "documentType")
    private int documentType;

    @Column(name = "amount")
    private double amount;

    @Column(name = "accountTitleUUID")
    private String accountTitleUUID;

    @Column(name = "accountObject")
    private String accountObject;

    // Cross-module refs — UUID only, no FK
    @Column(name = "accountantUUID")
    private String accountantUUID;

    @Column(name = "cashierUUID")
    private String cashierUUID;

    @Column(name = "refDocumentUUID")
    private String refDocumentUUID;

    @Column(name = "refAccountObjectUUID")
    private String refAccountObjectUUID;

    @Column(name = "execOrgUUID")
    private String execOrgUUID;

    @Column(name = "paymentType")
    private int paymentType;

    @Column(name = "financeTime")
    private LocalDateTime financeTime;

    // --- audit step ---
    @Column(name = "auditStatus")
    private int auditStatus;

    @Column(name = "auditLock")
    private boolean auditLock;

    @Column(name = "auditBy")
    private String auditBy;

    @Column(name = "auditTime")
    private LocalDateTime auditTime;

    @Column(name = "auditNote")
    private String auditNote;

    @Column(name = "auditLockMSG")
    private String auditLockMSG;

    // --- record step ---
    @Column(name = "recordStatus")
    private int recordStatus;

    @Column(name = "recordLock")
    private boolean recordLock;

    @Column(name = "recordBy")
    private String recordBy;

    @Column(name = "recordTime")
    private LocalDateTime recordTime;

    @Column(name = "recordNote")
    private String recordNote;

    @Column(name = "recordLockMSG")
    private String recordLockMSG;

    @Column(name = "recordedAmount")
    private double recordedAmount;

    @Column(name = "toRecordAmount")
    private double toRecordAmount;

    // --- verify step ---
    @Column(name = "verifyStatus")
    private int verifyStatus;

    @Column(name = "verifyLock")
    private boolean verifyLock;

    @Column(name = "verifyBy")
    private String verifyBy;

    @Column(name = "verifyTime")
    private LocalDateTime verifyTime;

    @Column(name = "verifyNote")
    private String verifyNote;

    @Column(name = "verifyLockMSG")
    private String verifyLockMSG;

    // --- currency / adjustment ---
    @Column(name = "currencyCode")
    private String currencyCode;

    @Column(name = "exchangeRate")
    private double exchangeRate;

    @Column(name = "amountInSetCurrency")
    private double amountInSetCurrency;

    @Column(name = "recordedAmountInSetCurrency")
    private double recordedAmountInSetCurrency;

    @Column(name = "toRecordAmountInSetCurrency")
    private double toRecordAmountInSetCurrency;

    @Column(name = "adjustDirection")
    private int adjustDirection;

    @Column(name = "adjustAmount")
    private double adjustAmount;

    public int getDocumentType() { return documentType; }
    public void setDocumentType(int documentType) { this.documentType = documentType; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getAccountTitleUUID() { return accountTitleUUID; }
    public void setAccountTitleUUID(String accountTitleUUID) { this.accountTitleUUID = accountTitleUUID; }

    public String getAccountObject() { return accountObject; }
    public void setAccountObject(String accountObject) { this.accountObject = accountObject; }

    public String getAccountantUUID() { return accountantUUID; }
    public void setAccountantUUID(String accountantUUID) { this.accountantUUID = accountantUUID; }

    public String getCashierUUID() { return cashierUUID; }
    public void setCashierUUID(String cashierUUID) { this.cashierUUID = cashierUUID; }

    public String getRefDocumentUUID() { return refDocumentUUID; }
    public void setRefDocumentUUID(String refDocumentUUID) { this.refDocumentUUID = refDocumentUUID; }

    public String getRefAccountObjectUUID() { return refAccountObjectUUID; }
    public void setRefAccountObjectUUID(String refAccountObjectUUID) { this.refAccountObjectUUID = refAccountObjectUUID; }

    public String getExecOrgUUID() { return execOrgUUID; }
    public void setExecOrgUUID(String execOrgUUID) { this.execOrgUUID = execOrgUUID; }

    public int getPaymentType() { return paymentType; }
    public void setPaymentType(int paymentType) { this.paymentType = paymentType; }

    public LocalDateTime getFinanceTime() { return financeTime; }
    public void setFinanceTime(LocalDateTime financeTime) { this.financeTime = financeTime; }

    public int getAuditStatus() { return auditStatus; }
    public void setAuditStatus(int auditStatus) { this.auditStatus = auditStatus; }

    public boolean getAuditLock() { return auditLock; }
    public void setAuditLock(boolean auditLock) { this.auditLock = auditLock; }

    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }

    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }

    public String getAuditNote() { return auditNote; }
    public void setAuditNote(String auditNote) { this.auditNote = auditNote; }

    public String getAuditLockMSG() { return auditLockMSG; }
    public void setAuditLockMSG(String auditLockMSG) { this.auditLockMSG = auditLockMSG; }

    public int getRecordStatus() { return recordStatus; }
    public void setRecordStatus(int recordStatus) { this.recordStatus = recordStatus; }

    public boolean getRecordLock() { return recordLock; }
    public void setRecordLock(boolean recordLock) { this.recordLock = recordLock; }

    public String getRecordBy() { return recordBy; }
    public void setRecordBy(String recordBy) { this.recordBy = recordBy; }

    public LocalDateTime getRecordTime() { return recordTime; }
    public void setRecordTime(LocalDateTime recordTime) { this.recordTime = recordTime; }

    public String getRecordNote() { return recordNote; }
    public void setRecordNote(String recordNote) { this.recordNote = recordNote; }

    public String getRecordLockMSG() { return recordLockMSG; }
    public void setRecordLockMSG(String recordLockMSG) { this.recordLockMSG = recordLockMSG; }

    public double getRecordedAmount() { return recordedAmount; }
    public void setRecordedAmount(double recordedAmount) { this.recordedAmount = recordedAmount; }

    public double getToRecordAmount() { return toRecordAmount; }
    public void setToRecordAmount(double toRecordAmount) { this.toRecordAmount = toRecordAmount; }

    public int getVerifyStatus() { return verifyStatus; }
    public void setVerifyStatus(int verifyStatus) { this.verifyStatus = verifyStatus; }

    public boolean getVerifyLock() { return verifyLock; }
    public void setVerifyLock(boolean verifyLock) { this.verifyLock = verifyLock; }

    public String getVerifyBy() { return verifyBy; }
    public void setVerifyBy(String verifyBy) { this.verifyBy = verifyBy; }

    public LocalDateTime getVerifyTime() { return verifyTime; }
    public void setVerifyTime(LocalDateTime verifyTime) { this.verifyTime = verifyTime; }

    public String getVerifyNote() { return verifyNote; }
    public void setVerifyNote(String verifyNote) { this.verifyNote = verifyNote; }

    public String getVerifyLockMSG() { return verifyLockMSG; }
    public void setVerifyLockMSG(String verifyLockMSG) { this.verifyLockMSG = verifyLockMSG; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public double getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(double exchangeRate) { this.exchangeRate = exchangeRate; }

    public double getAmountInSetCurrency() { return amountInSetCurrency; }
    public void setAmountInSetCurrency(double amountInSetCurrency) { this.amountInSetCurrency = amountInSetCurrency; }

    public double getRecordedAmountInSetCurrency() { return recordedAmountInSetCurrency; }
    public void setRecordedAmountInSetCurrency(double v) { this.recordedAmountInSetCurrency = v; }

    public double getToRecordAmountInSetCurrency() { return toRecordAmountInSetCurrency; }
    public void setToRecordAmountInSetCurrency(double v) { this.toRecordAmountInSetCurrency = v; }

    public int getAdjustDirection() { return adjustDirection; }
    public void setAdjustDirection(int adjustDirection) { this.adjustDirection = adjustDirection; }

    public double getAdjustAmount() { return adjustAmount; }
    public void setAdjustAmount(double adjustAmount) { this.adjustAmount = adjustAmount; }
}
