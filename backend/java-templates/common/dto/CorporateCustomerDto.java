package com.company.IntelligentPlatform.common.dto;

import com.company.IntelligentPlatform.common.model.CorporateCustomer;

/**
 * DTO for CorporateCustomer create/update requests.
 */
public class CorporateCustomerDto {

    private String name;
    private String client;
    private int customerType;
    private String address;
    private String telephone;
    private String email;
    private String taxNumber;
    private String bankAccount;
    private String refSalesAreaUUID;
    private String note;

    public CorporateCustomer toEntity() {
        CorporateCustomer customer = new CorporateCustomer();
        applyTo(customer);
        return customer;
    }

    public void applyTo(CorporateCustomer customer) {
        if (name != null)             customer.setName(name);
        if (client != null)           customer.setClient(client);
        if (address != null)          customer.setAddress(address);
        if (telephone != null)        customer.setTelephone(telephone);
        if (email != null)            customer.setEmail(email);
        if (taxNumber != null)        customer.setTaxNumber(taxNumber);
        if (bankAccount != null)      customer.setBankAccount(bankAccount);
        if (refSalesAreaUUID != null) customer.setRefSalesAreaUUID(refSalesAreaUUID);
        if (note != null)             customer.setNote(note);
        customer.setCustomerType(customerType);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }

    public int getCustomerType() { return customerType; }
    public void setCustomerType(int customerType) { this.customerType = customerType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public String getRefSalesAreaUUID() { return refSalesAreaUUID; }
    public void setRefSalesAreaUUID(String refSalesAreaUUID) { this.refSalesAreaUUID = refSalesAreaUUID; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
