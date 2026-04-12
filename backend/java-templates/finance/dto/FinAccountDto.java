package com.company.IntelligentPlatform.finance.dto;

import com.company.IntelligentPlatform.finance.model.FinAccount;
import java.time.LocalDateTime;

/**
 * DTO for FinAccount create/update requests.
 */
public class FinAccountDto {

    private String name;
    private String client;
    private int documentType;
    private double amount;
    private String accountTitleUUID;
    private String accountObject;
    private String accountantUUID;
    private String cashierUUID;
    private String refDocumentUUID;
    private String refAccountObjectUUID;
    private String execOrgUUID;
    private int paymentType;
    private String currencyCode;
    private double exchangeRate;
    private int adjustDirection;
    private double adjustAmount;
    private LocalDateTime financeTime;
    private String note;

    public FinAccount toEntity() {
        FinAccount account = new FinAccount();
        applyTo(account);
        return account;
    }

    public void applyTo(FinAccount account) {
        if (name != null)                 account.setName(name);
        if (client != null)               account.setClient(client);
        if (accountTitleUUID != null)     account.setAccountTitleUUID(accountTitleUUID);
        if (accountObject != null)        account.setAccountObject(accountObject);
        if (accountantUUID != null)       account.setAccountantUUID(accountantUUID);
        if (cashierUUID != null)          account.setCashierUUID(cashierUUID);
        if (refDocumentUUID != null)      account.setRefDocumentUUID(refDocumentUUID);
        if (refAccountObjectUUID != null) account.setRefAccountObjectUUID(refAccountObjectUUID);
        if (execOrgUUID != null)          account.setExecOrgUUID(execOrgUUID);
        if (currencyCode != null)         account.setCurrencyCode(currencyCode);
        if (financeTime != null)          account.setFinanceTime(financeTime);
        if (note != null)                 account.setNote(note);
        account.setDocumentType(documentType);
        account.setAmount(amount);
        account.setPaymentType(paymentType);
        account.setExchangeRate(exchangeRate);
        account.setAdjustDirection(adjustDirection);
        account.setAdjustAmount(adjustAmount);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }

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

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public double getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(double exchangeRate) { this.exchangeRate = exchangeRate; }

    public int getAdjustDirection() { return adjustDirection; }
    public void setAdjustDirection(int adjustDirection) { this.adjustDirection = adjustDirection; }

    public double getAdjustAmount() { return adjustAmount; }
    public void setAdjustAmount(double adjustAmount) { this.adjustAmount = adjustAmount; }

    public LocalDateTime getFinanceTime() { return financeTime; }
    public void setFinanceTime(LocalDateTime financeTime) { this.financeTime = financeTime; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
