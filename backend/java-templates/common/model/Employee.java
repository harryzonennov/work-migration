package com.company.IntelligentPlatform.common.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Migrated from: ThorsteinPlatform - Employee.java
 * Old table: Employee (single shared DB)
 * New table: Employee (schema: platform)
 * Hierarchy: ServiceEntityNode → Account → IndividualAccount → Employee
 * Note: IndividualAccount is a thin layer (adds nothing beyond mobile) so Employee extends Account directly.
 */
@Entity
@Table(name = "Employee", schema = "platform")
public class Employee extends Account {

    public static final int STATUS_INIT     = 1;
    public static final int STATUS_ACTIVE   = 305;
    public static final int STATUS_ARCHIVED = 980;

    public static final int OPERATETYPE_INTERNAL  = 1;
    public static final int OPERATETYPE_CONTRACT  = 2;
    public static final int OPERATETYPE_TEMP      = 3;
    public static final int OPERATETYPE_ATTACHED  = 4;

    public static final int WORKROLE_CEO                = 1;
    public static final int WORKROLE_GENERAL_MAN        = 2;
    public static final int WORKROLE_VICE_MAN           = 3;
    public static final int WORKROLE_DEP_MAN            = 4;
    public static final int WORKROLE_PROJ_MAN           = 5;
    public static final int WORKROLE_ACCOUNTANT         = 6;
    public static final int WORKROLE_CASHIER            = 7;
    public static final int WORKROLE_LOGISTICS_OPERATOR = 8;
    public static final int WORKROLE_MARKET_OPERATOR    = 9;
    public static final int WORKROLE_DRIVER             = 10;
    public static final int WORKROLE_PORTER             = 11;

    public static final int JOBLEVEL_JUNIOR = 1;
    public static final int JOBLEVEL_MID    = 2;
    public static final int JOBLEVEL_SENIOR = 3;
    public static final int JOBLEVEL_EXPERT = 4;

    public static final int GENDER_MALE   = 1;
    public static final int GENDER_FEMALE = 2;

    @Column(name = "status")
    private int status;

    @Column(name = "operateType")
    private int operateType;

    @Column(name = "identification")
    private String identification;

    @Column(name = "workRole")
    private int workRole;

    @Column(name = "jobLevel")
    private int jobLevel;

    @Column(name = "boardDate")
    private LocalDate boardDate;

    @Column(name = "birthDate")
    private LocalDate birthDate;

    @Column(name = "age")
    private int age;

    @Column(name = "gender")
    private int gender;

    @Column(name = "driverLicenseNumber")
    private String driverLicenseNumber;

    @Column(name = "driverLicenseType")
    private int driverLicenseType;

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public int getOperateType() { return operateType; }
    public void setOperateType(int operateType) { this.operateType = operateType; }

    public String getIdentification() { return identification; }
    public void setIdentification(String identification) { this.identification = identification; }

    public int getWorkRole() { return workRole; }
    public void setWorkRole(int workRole) { this.workRole = workRole; }

    public int getJobLevel() { return jobLevel; }
    public void setJobLevel(int jobLevel) { this.jobLevel = jobLevel; }

    public LocalDate getBoardDate() { return boardDate; }
    public void setBoardDate(LocalDate boardDate) { this.boardDate = boardDate; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public int getGender() { return gender; }
    public void setGender(int gender) { this.gender = gender; }

    public String getDriverLicenseNumber() { return driverLicenseNumber; }
    public void setDriverLicenseNumber(String driverLicenseNumber) { this.driverLicenseNumber = driverLicenseNumber; }

    public int getDriverLicenseType() { return driverLicenseType; }
    public void setDriverLicenseType(int driverLicenseType) { this.driverLicenseType = driverLicenseType; }
}
