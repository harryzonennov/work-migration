package com.company.IntelligentPlatform.logistics.repository;

import com.company.IntelligentPlatform.logistics.model.QualityInspectOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QualityInspectOrderRepository extends JpaRepository<QualityInspectOrder, String>,
        JpaSpecificationExecutor<QualityInspectOrder> {

    List<QualityInspectOrder> findByClient(String client);
    List<QualityInspectOrder> findByClientAndStatus(String client, int status);
    List<QualityInspectOrder> findByClientAndCategory(String client, int category);
}
