package com.company.IntelligentPlatform.common.model;

import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinPlatform - Account.java
 * @MappedSuperclass — base for all account entities (corporate, individual, employee, organization)
 * Hierarchy: ServiceEntityNode → Account
 */
@MappedSuperclass
public abstract class Account extends ServiceEntityNode {

    public static final int ACCOUNTTYPE_IND_CUSTOMER      = 1;
    public static final int ACCOUNTTYPE_COR_CUSTOMER       = 2;
    public static final int ACCOUNTTYPE_EMPLOYEE           = 3;
    public static final int ACCOUNTTYPE_TRANSIT_PARTNER    = 4;
    public static final int ACCOUNTTYPE_EXTER_DRIVER       = 5;
    public static final int ACCOUNTTYPE_ORGANIZATION       = 6;
    public static final int ACCOUNTTYPE_SUPPLIER           = 7;

    public static final int REGULAR_TYPE_REGULAR = 1;
    public static final int REGULAR_TYPE_TEMP    = 2;

    @Column(name = "accountType")
    protected int accountType;

    @Column(name = "address")
    protected String address;

    @Column(name = "telephone")
    protected String telephone;

    @Column(name = "mobile")
    protected String mobile;

    @Column(name = "fax")
    protected String fax;

    @Column(name = "email")
    protected String email;

    @Column(name = "webPage")
    protected String webPage;

    @Column(name = "postcode")
    protected String postcode;

    @Column(name = "countryName")
    protected String countryName;

    @Column(name = "stateName")
    protected String stateName;

    @Column(name = "cityName")
    protected String cityName;

    @Column(name = "refCityUUID")
    protected String refCityUUID;

    @Column(name = "townZone")
    protected String townZone;

    @Column(name = "subArea")
    protected String subArea;

    @Column(name = "streetName")
    protected String streetName;

    @Column(name = "houseNumber")
    protected String houseNumber;

    @Column(name = "regularType")
    protected int regularType;

    @Column(name = "taxNumber")
    protected String taxNumber;

    @Column(name = "bankAccount")
    protected String bankAccount;

    @Column(name = "depositBank")
    protected String depositBank;

    public int getAccountType() { return accountType; }
    public void setAccountType(int accountType) { this.accountType = accountType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getFax() { return fax; }
    public void setFax(String fax) { this.fax = fax; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWebPage() { return webPage; }
    public void setWebPage(String webPage) { this.webPage = webPage; }

    public String getPostcode() { return postcode; }
    public void setPostcode(String postcode) { this.postcode = postcode; }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public String getStateName() { return stateName; }
    public void setStateName(String stateName) { this.stateName = stateName; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getRefCityUUID() { return refCityUUID; }
    public void setRefCityUUID(String refCityUUID) { this.refCityUUID = refCityUUID; }

    public String getTownZone() { return townZone; }
    public void setTownZone(String townZone) { this.townZone = townZone; }

    public String getSubArea() { return subArea; }
    public void setSubArea(String subArea) { this.subArea = subArea; }

    public String getStreetName() { return streetName; }
    public void setStreetName(String streetName) { this.streetName = streetName; }

    public String getHouseNumber() { return houseNumber; }
    public void setHouseNumber(String houseNumber) { this.houseNumber = houseNumber; }

    public int getRegularType() { return regularType; }
    public void setRegularType(int regularType) { this.regularType = regularType; }

    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public String getDepositBank() { return depositBank; }
    public void setDepositBank(String depositBank) { this.depositBank = depositBank; }
}
