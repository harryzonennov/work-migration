package com.company.IntelligentPlatform.sales.repository;

import com.company.IntelligentPlatform.sales.model.SalesArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SalesAreaRepository extends JpaRepository<SalesArea, String> {

    List<SalesArea> findByClient(String client);
    List<SalesArea> findByParentAreaUUID(String parentAreaUUID);
    List<SalesArea> findByClientAndLevel(String client, int level);
}
