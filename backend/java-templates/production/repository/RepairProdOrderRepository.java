package com.company.IntelligentPlatform.production.repository;

import com.company.IntelligentPlatform.production.model.RepairProdOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RepairProdOrderRepository extends JpaRepository<RepairProdOrder, String>, JpaSpecificationExecutor<RepairProdOrder> {
}
