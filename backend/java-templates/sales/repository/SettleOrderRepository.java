package com.company.IntelligentPlatform.sales.repository;

import com.company.IntelligentPlatform.sales.model.SettleOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SettleOrderRepository extends JpaRepository<SettleOrder, String> {

    List<SettleOrder> findByClient(String client);
    List<SettleOrder> findByRefOrderUUID(String refOrderUUID);
    List<SettleOrder> findByClientAndStatus(String client, int status);
}
