package com.company.IntelligentPlatform.logistics.repository;

import com.company.IntelligentPlatform.logistics.model.PurchaseReturnOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseReturnOrderRepository extends JpaRepository<PurchaseReturnOrder, String>,
        JpaSpecificationExecutor<PurchaseReturnOrder> {

    List<PurchaseReturnOrder> findByClient(String client);
    List<PurchaseReturnOrder> findByClientAndStatus(String client, int status);
}
