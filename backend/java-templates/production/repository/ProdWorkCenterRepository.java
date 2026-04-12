package com.company.IntelligentPlatform.production.repository;

import com.company.IntelligentPlatform.production.model.ProdWorkCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProdWorkCenterRepository extends JpaRepository<ProdWorkCenter, String> {
    List<ProdWorkCenter> findByParentNodeUUID(String parentNodeUUID);
}
