package com.company.IntelligentPlatform.production.repository;

import com.company.IntelligentPlatform.production.model.ProdPickingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ProdPickingOrderRepository extends JpaRepository<ProdPickingOrder, String>, JpaSpecificationExecutor<ProdPickingOrder> {
    List<ProdPickingOrder> findByStatus(int status);
}
