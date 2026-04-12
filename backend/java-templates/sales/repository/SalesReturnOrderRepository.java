package com.company.IntelligentPlatform.sales.repository;

import com.company.IntelligentPlatform.sales.model.SalesReturnOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SalesReturnOrderRepository extends JpaRepository<SalesReturnOrder, String>,
        JpaSpecificationExecutor<SalesReturnOrder> {

    List<SalesReturnOrder> findByClient(String client);
    List<SalesReturnOrder> findByClientAndStatus(String client, int status);
}
