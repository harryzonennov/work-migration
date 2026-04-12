package com.company.IntelligentPlatform.production.repository;

import com.company.IntelligentPlatform.production.model.ProcessRouteOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ProcessRouteOrderRepository extends JpaRepository<ProcessRouteOrder, String>, JpaSpecificationExecutor<ProcessRouteOrder> {
    List<ProcessRouteOrder> findByRefMaterialSKUUUID(String refMaterialSKUUUID);
    List<ProcessRouteOrder> findByStatus(int status);
}
