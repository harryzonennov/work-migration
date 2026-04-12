package com.company.IntelligentPlatform.common.repository;

import com.company.IntelligentPlatform.common.model.CorporateCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Replaces: ThorsteinPlatform CorporateCustomerDAO (Hibernate)
 */
@Repository
public interface CorporateCustomerRepository extends JpaRepository<CorporateCustomer, String>,
        JpaSpecificationExecutor<CorporateCustomer> {

    List<CorporateCustomer> findByClient(String client);

    List<CorporateCustomer> findByStatus(int status);
}
