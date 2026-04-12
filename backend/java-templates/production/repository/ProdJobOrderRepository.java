package com.company.IntelligentPlatform.production.repository;

import com.company.IntelligentPlatform.production.model.ProdJobOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProdJobOrderRepository extends JpaRepository<ProdJobOrder, String> {
    List<ProdJobOrder> findByRefProductionOrderUUID(String refProductionOrderUUID);
    List<ProdJobOrder> findByRefWorkCenterUUID(String refWorkCenterUUID);
}
