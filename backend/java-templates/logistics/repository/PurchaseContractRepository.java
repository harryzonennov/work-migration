package com.company.IntelligentPlatform.logistics.repository;

import com.company.IntelligentPlatform.logistics.model.PurchaseContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseContractRepository extends JpaRepository<PurchaseContract, String>,
        JpaSpecificationExecutor<PurchaseContract> {

    List<PurchaseContract> findByClient(String client);
    List<PurchaseContract> findByClientAndStatus(String client, int status);
}
