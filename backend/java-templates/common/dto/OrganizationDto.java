package com.company.IntelligentPlatform.common.dto;

import com.company.IntelligentPlatform.common.model.Organization;

/**
 * DTO for Organization create/update requests.
 */
public class OrganizationDto {

    private String name;
    private String client;
    private String parentOrganizationUUID;
    private int organizationFunction;
    private int organType;
    private String address;
    private String telephone;
    private String email;
    private String refFinOrgUUID;
    private String note;

    public Organization toEntity() {
        Organization org = new Organization();
        applyTo(org);
        return org;
    }

    public void applyTo(Organization org) {
        if (name != null)                    org.setName(name);
        if (client != null)                  org.setClient(client);
        if (parentOrganizationUUID != null)  org.setParentOrganizationUUID(parentOrganizationUUID);
        if (address != null)                 org.setAddress(address);
        if (telephone != null)               org.setTelephone(telephone);
        if (email != null)                   org.setEmail(email);
        if (refFinOrgUUID != null)           org.setRefFinOrgUUID(refFinOrgUUID);
        if (note != null)                    org.setNote(note);
        org.setOrganizationFunction(organizationFunction);
        org.setOrganType(organType);
        org.setAccountType(Organization.ACCOUNTTYPE_ORGANIZATION);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }

    public String getParentOrganizationUUID() { return parentOrganizationUUID; }
    public void setParentOrganizationUUID(String parentOrganizationUUID) { this.parentOrganizationUUID = parentOrganizationUUID; }

    public int getOrganizationFunction() { return organizationFunction; }
    public void setOrganizationFunction(int organizationFunction) { this.organizationFunction = organizationFunction; }

    public int getOrganType() { return organType; }
    public void setOrganType(int organType) { this.organType = organType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRefFinOrgUUID() { return refFinOrgUUID; }
    public void setRefFinOrgUUID(String refFinOrgUUID) { this.refFinOrgUUID = refFinOrgUUID; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
