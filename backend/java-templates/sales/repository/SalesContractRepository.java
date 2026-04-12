package com.company.IntelligentPlatform.sales.repository;

import com.company.IntelligentPlatform.sales.model.SalesContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SalesContractRepository extends JpaRepository<SalesContract, String>,
        JpaSpecificationExecutor<SalesContract> {

    List<SalesContract> findByClient(String client);
    List<SalesContract> findByClientAndStatus(String client, int status);
}
