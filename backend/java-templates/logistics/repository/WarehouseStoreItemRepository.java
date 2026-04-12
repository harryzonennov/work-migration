package com.company.IntelligentPlatform.logistics.repository;

import com.company.IntelligentPlatform.logistics.model.WarehouseStoreItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WarehouseStoreItemRepository extends JpaRepository<WarehouseStoreItem, String>,
        JpaSpecificationExecutor<WarehouseStoreItem> {

    List<WarehouseStoreItem> findByDocumentUUID(String documentUUID);
    List<WarehouseStoreItem> findByDocumentUUIDAndItemStatus(String documentUUID, int itemStatus);
    List<WarehouseStoreItem> findByRefWarehouseUUID(String refWarehouseUUID);
    List<WarehouseStoreItem> findByRefWarehouseUUIDAndItemStatus(String refWarehouseUUID, int itemStatus);
}
