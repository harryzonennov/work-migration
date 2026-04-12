package com.company.IntelligentPlatform.logistics.repository;

import com.company.IntelligentPlatform.logistics.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String>,
        JpaSpecificationExecutor<PurchaseOrder> {

    List<PurchaseOrder> findByClient(String client);
    List<PurchaseOrder> findByClientAndStatus(String client, int status);
}
