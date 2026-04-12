package com.company.IntelligentPlatform.common.dto;

import com.company.IntelligentPlatform.common.model.Employee;
import java.time.LocalDate;

/**
 * DTO for Employee create/update requests.
 */
public class EmployeeDto {

    private String name;
    private String client;
    private int workRole;
    private int operateType;
    private int gender;
    private String identification;
    private String mobile;
    private String email;
    private LocalDate boardDate;
    private String resOrgUUID;
    private String note;

    public Employee toEntity() {
        Employee employee = new Employee();
        applyTo(employee);
        return employee;
    }

    public void applyTo(Employee employee) {
        if (name != null)           employee.setName(name);
        if (client != null)         employee.setClient(client);
        if (identification != null) employee.setIdentification(identification);
        if (mobile != null)         employee.setMobile(mobile);
        if (email != null)          employee.setEmail(email);
        if (boardDate != null)      employee.setBoardDate(boardDate);
        if (resOrgUUID != null)     employee.setResOrgUUID(resOrgUUID);
        if (note != null)           employee.setNote(note);
        employee.setWorkRole(workRole);
        employee.setOperateType(operateType);
        employee.setGender(gender);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }

    public int getWorkRole() { return workRole; }
    public void setWorkRole(int workRole) { this.workRole = workRole; }

    public int getOperateType() { return operateType; }
    public void setOperateType(int operateType) { this.operateType = operateType; }

    public int getGender() { return gender; }
    public void setGender(int gender) { this.gender = gender; }

    public String getIdentification() { return identification; }
    public void setIdentification(String identification) { this.identification = identification; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getBoardDate() { return boardDate; }
    public void setBoardDate(LocalDate boardDate) { this.boardDate = boardDate; }

    public String getResOrgUUID() { return resOrgUUID; }
    public void setResOrgUUID(String resOrgUUID) { this.resOrgUUID = resOrgUUID; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
