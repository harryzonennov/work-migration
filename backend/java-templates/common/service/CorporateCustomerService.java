package com.company.IntelligentPlatform.common.service;

import com.company.IntelligentPlatform.common.model.CorporateCustomer;
import com.company.IntelligentPlatform.common.repository.CorporateCustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Replaces: ThorsteinPlatform CorporateCustomerManager (ServiceEntityManager subclass)
 */
@Service
@Transactional
public class CorporateCustomerService {

    @Autowired
    private CorporateCustomerRepository corporateCustomerRepository;

    public CorporateCustomer create(CorporateCustomer customer) {
        customer.setUuid(UUID.randomUUID().toString());
        customer.setStatus(CorporateCustomer.STATUS_INITIAL);
        customer.setAccountType(CorporateCustomer.ACCOUNTTYPE_COR_CUSTOMER);
        return corporateCustomerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public CorporateCustomer getByUuid(String uuid) {
        return corporateCustomerRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<CorporateCustomer> getByClient(String client) {
        return corporateCustomerRepository.findByClient(client);
    }

    public CorporateCustomer update(CorporateCustomer customer) {
        return corporateCustomerRepository.save(customer);
    }

    public void setStatus(String uuid, int status) {
        CorporateCustomer customer = corporateCustomerRepository.findById(uuid).orElseThrow();
        customer.setStatus(status);
        corporateCustomerRepository.save(customer);
    }
}
