package com.company.IntelligentPlatform.finance.repository;

import com.company.IntelligentPlatform.finance.model.FinAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Replaces: ThorsteinFinance FinAccountDAO (Hibernate)
 */
@Repository
public interface FinAccountRepository extends JpaRepository<FinAccount, String>,
        JpaSpecificationExecutor<FinAccount> {

    List<FinAccount> findByClient(String client);

    List<FinAccount> findByClientAndStatus(String client, int status);

    List<FinAccount> findByAccountTitleUUID(String accountTitleUUID);
}
