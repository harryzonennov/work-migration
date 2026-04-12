package com.company.IntelligentPlatform.common.model;

import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinPlatform - LogonUser.hbm.xml
 * Old table: LogonUser (single shared DB)
 * New table: LogonUser (schema: platform)
 */
@Entity
@Table(name = "LogonUser", schema = "platform")
public class LogonUser extends ServiceEntityNode {

    @Column(name = "password")
    private String password;         // NOTE: must be BCrypt — old code used plain/MD5

    @Column(name = "lockUserFlag")
    private int lockUserFlag;

    @Column(name = "tryFailedTimes")
    private int tryFailedTimes;

    @Column(name = "passwordInitFlag")
    private int passwordInitFlag;

    @Column(name = "logonTime")
    private java.time.LocalDateTime logonTime;

    @Column(name = "userType")
    private int userType;

    @Column(name = "passwordNeedFlag")
    private int passwordNeedFlag;

    @Column(name = "initPassword")
    private String initPassword;

    @Column(name = "status")
    private int status;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getLockUserFlag() { return lockUserFlag; }
    public void setLockUserFlag(int lockUserFlag) { this.lockUserFlag = lockUserFlag; }

    public int getTryFailedTimes() { return tryFailedTimes; }
    public void setTryFailedTimes(int tryFailedTimes) { this.tryFailedTimes = tryFailedTimes; }

    public int getPasswordInitFlag() { return passwordInitFlag; }
    public void setPasswordInitFlag(int passwordInitFlag) { this.passwordInitFlag = passwordInitFlag; }

    public java.time.LocalDateTime getLogonTime() { return logonTime; }
    public void setLogonTime(java.time.LocalDateTime logonTime) { this.logonTime = logonTime; }

    public int getUserType() { return userType; }
    public void setUserType(int userType) { this.userType = userType; }

    public int getPasswordNeedFlag() { return passwordNeedFlag; }
    public void setPasswordNeedFlag(int passwordNeedFlag) { this.passwordNeedFlag = passwordNeedFlag; }

    public String getInitPassword() { return initPassword; }
    public void setInitPassword(String initPassword) { this.initPassword = initPassword; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
