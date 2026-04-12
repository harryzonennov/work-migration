package com.company.IntelligentPlatform.logistics.repository;

import com.company.IntelligentPlatform.logistics.model.WarehouseStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WarehouseStoreRepository extends JpaRepository<WarehouseStore, String>,
        JpaSpecificationExecutor<WarehouseStore> {

    List<WarehouseStore> findByClient(String client);
    List<WarehouseStore> findByClientAndStatus(String client, int status);
    List<WarehouseStore> findByClientAndRefWarehouseUUID(String client, String refWarehouseUUID);
}
