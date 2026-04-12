package com.company.IntelligentPlatform.common.model;

import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinPlatform - CorporateAccount.java
 * @MappedSuperclass — base for corporate-type accounts (CorporateCustomer, Organization)
 * Hierarchy: ServiceEntityNode → Account → CorporateAccount
 */
@MappedSuperclass
public abstract class CorporateAccount extends Account {

    @Column(name = "fullName")
    protected String fullName;

    @Column(name = "addressOnMap")
    protected String addressOnMap;

    @Column(name = "longitude")
    protected String longitude;

    @Column(name = "latitude")
    protected String latitude;

    @Column(name = "contactMobileNumber")
    protected String contactMobileNumber;

    @Column(name = "tags")
    protected String tags;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAddressOnMap() { return addressOnMap; }
    public void setAddressOnMap(String addressOnMap) { this.addressOnMap = addressOnMap; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }

    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getContactMobileNumber() { return contactMobileNumber; }
    public void setContactMobileNumber(String contactMobileNumber) { this.contactMobileNumber = contactMobileNumber; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
