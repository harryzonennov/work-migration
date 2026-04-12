package com.company.IntelligentPlatform.production.repository;

import com.company.IntelligentPlatform.production.model.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, String>, JpaSpecificationExecutor<ProductionOrder> {
    List<ProductionOrder> findByStatus(int status);
    List<ProductionOrder> findByRefMaterialSKUUUID(String refMaterialSKUUUID);
}
