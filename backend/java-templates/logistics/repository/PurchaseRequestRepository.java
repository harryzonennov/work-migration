package com.company.IntelligentPlatform.logistics.repository;

import com.company.IntelligentPlatform.logistics.model.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, String>,
        JpaSpecificationExecutor<PurchaseRequest> {

    List<PurchaseRequest> findByClient(String client);
    List<PurchaseRequest> findByClientAndStatus(String client, int status);
}
