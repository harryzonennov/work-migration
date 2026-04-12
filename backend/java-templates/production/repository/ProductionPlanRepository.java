package com.company.IntelligentPlatform.production.repository;

import com.company.IntelligentPlatform.production.model.ProductionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, String>, JpaSpecificationExecutor<ProductionPlan> {
    List<ProductionPlan> findByRefMainProdOrderUUID(String refMainProdOrderUUID);
    List<ProductionPlan> findByStatus(int status);
}
