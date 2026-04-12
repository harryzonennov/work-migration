package com.company.IntelligentPlatform.common.service;

import com.company.IntelligentPlatform.common.model.Organization;
import com.company.IntelligentPlatform.common.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Replaces: ThorsteinPlatform OrganizationManager (ServiceEntityManager subclass)
 */
@Service
@Transactional
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    public Organization create(Organization org) {
        org.setUuid(UUID.randomUUID().toString());
        return organizationRepository.save(org);
    }

    @Transactional(readOnly = true)
    public Organization getByUuid(String uuid) {
        return organizationRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Organization> getByClient(String client) {
        return organizationRepository.findByClient(client);
    }

    @Transactional(readOnly = true)
    public List<Organization> getChildren(String parentOrganizationUUID) {
        return organizationRepository.findByParentOrganizationUUID(parentOrganizationUUID);
    }

    public Organization update(Organization org) {
        return organizationRepository.save(org);
    }
}
