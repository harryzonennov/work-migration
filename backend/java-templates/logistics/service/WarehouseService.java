package com.company.IntelligentPlatform.logistics.service;

import com.company.IntelligentPlatform.logistics.model.*;
import com.company.IntelligentPlatform.logistics.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Replaces: ThorsteinLogistics - WarehouseStoreManager
 */
@Service
@Transactional
public class WarehouseService {

    @Autowired private WarehouseStoreRepository warehouseStoreRepository;
    @Autowired private WarehouseStoreItemRepository warehouseStoreItemRepository;

    // --- WarehouseStore ---

    public WarehouseStore createStore(WarehouseStore store) {
        store.setUuid(UUID.randomUUID().toString());
        store.setStatus(WarehouseStore.STATUS_INITIAL);
        return warehouseStoreRepository.save(store);
    }

    @Transactional(readOnly = true)
    public WarehouseStore getStoreByUuid(String uuid) {
        return warehouseStoreRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<WarehouseStore> getStoresByClient(String client) {
        return warehouseStoreRepository.findByClient(client);
    }

    @Transactional(readOnly = true)
    public List<WarehouseStore> getStoresByWarehouse(String client, String refWarehouseUUID) {
        return warehouseStoreRepository.findByClientAndRefWarehouseUUID(client, refWarehouseUUID);
    }

    public WarehouseStore updateStore(WarehouseStore store) {
        return warehouseStoreRepository.save(store);
    }

    public void setStoreStatus(String uuid, int status) {
        WarehouseStore store = warehouseStoreRepository.findById(uuid).orElseThrow();
        store.setStatus(status);
        warehouseStoreRepository.save(store);
    }

    // --- WarehouseStoreItem ---

    public WarehouseStoreItem createStoreItem(WarehouseStoreItem item) {
        item.setUuid(UUID.randomUUID().toString());
        item.setItemStatus(WarehouseStoreItem.STATUS_INSTOCK);
        return warehouseStoreItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public WarehouseStoreItem getStoreItemByUuid(String uuid) {
        return warehouseStoreItemRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<WarehouseStoreItem> getStoreItemsByDocument(String documentUUID) {
        return warehouseStoreItemRepository.findByDocumentUUID(documentUUID);
    }

    @Transactional(readOnly = true)
    public List<WarehouseStoreItem> getInstockItemsByWarehouse(String refWarehouseUUID) {
        return warehouseStoreItemRepository.findByRefWarehouseUUIDAndItemStatus(
                refWarehouseUUID, WarehouseStoreItem.STATUS_INSTOCK);
    }

    public WarehouseStoreItem updateStoreItem(WarehouseStoreItem item) {
        return warehouseStoreItemRepository.save(item);
    }

    public void archiveStoreItem(String uuid) {
        WarehouseStoreItem item = warehouseStoreItemRepository.findById(uuid).orElseThrow();
        item.setItemStatus(WarehouseStoreItem.STATUS_ARCHIVE);
        warehouseStoreItemRepository.save(item);
    }
}
