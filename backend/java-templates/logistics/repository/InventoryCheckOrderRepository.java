package com.company.IntelligentPlatform.logistics.repository;

import com.company.IntelligentPlatform.logistics.model.InventoryCheckOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryCheckOrderRepository extends JpaRepository<InventoryCheckOrder, String>,
        JpaSpecificationExecutor<InventoryCheckOrder> {

    List<InventoryCheckOrder> findByClient(String client);
    List<InventoryCheckOrder> findByClientAndStatus(String client, int status);
}
