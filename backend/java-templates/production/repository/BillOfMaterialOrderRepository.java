package com.company.IntelligentPlatform.production.repository;

import com.company.IntelligentPlatform.production.model.BillOfMaterialOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface BillOfMaterialOrderRepository extends JpaRepository<BillOfMaterialOrder, String>, JpaSpecificationExecutor<BillOfMaterialOrder> {
    List<BillOfMaterialOrder> findByRefMaterialSKUUUID(String refMaterialSKUUUID);
    List<BillOfMaterialOrder> findByStatus(int status);
}
