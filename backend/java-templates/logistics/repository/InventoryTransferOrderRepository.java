package com.company.IntelligentPlatform.logistics.repository;

import com.company.IntelligentPlatform.logistics.model.InventoryTransferOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryTransferOrderRepository extends JpaRepository<InventoryTransferOrder, String>,
        JpaSpecificationExecutor<InventoryTransferOrder> {

    List<InventoryTransferOrder> findByClient(String client);
    List<InventoryTransferOrder> findByClientAndStatus(String client, int status);
}
