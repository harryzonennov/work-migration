package com.company.IntelligentPlatform.common.dto;

import com.company.IntelligentPlatform.common.model.LogonUser;

/**
 * DTO for LogonUser create/update requests.
 * Replaces old ServiceUIModel pattern from ThorsteinPlatform.
 */
public class LogonUserDto {

    private String name;
    private String client;
    private String password;
    private int userType;
    private int passwordNeedFlag;
    private String resEmployeeUUID;
    private String resOrgUUID;
    private String note;

    public LogonUser toEntity() {
        LogonUser user = new LogonUser();
        applyTo(user);
        return user;
    }

    public void applyTo(LogonUser user) {
        if (name != null)             user.setName(name);
        if (client != null)           user.setClient(client);
        if (password != null)         user.setPassword(password);
        if (resEmployeeUUID != null)  user.setResEmployeeUUID(resEmployeeUUID);
        if (resOrgUUID != null)       user.setResOrgUUID(resOrgUUID);
        if (note != null)             user.setNote(note);
        user.setUserType(userType);
        user.setPasswordNeedFlag(passwordNeedFlag);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getUserType() { return userType; }
    public void setUserType(int userType) { this.userType = userType; }

    public int getPasswordNeedFlag() { return passwordNeedFlag; }
    public void setPasswordNeedFlag(int passwordNeedFlag) { this.passwordNeedFlag = passwordNeedFlag; }

    public String getResEmployeeUUID() { return resEmployeeUUID; }
    public void setResEmployeeUUID(String resEmployeeUUID) { this.resEmployeeUUID = resEmployeeUUID; }

    public String getResOrgUUID() { return resOrgUUID; }
    public void setResOrgUUID(String resOrgUUID) { this.resOrgUUID = resOrgUUID; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
