package com.company.IntelligentPlatform.logistics.controller;

import com.company.IntelligentPlatform.common.response.ApiResponse;
import com.company.IntelligentPlatform.logistics.model.WarehouseStore;
import com.company.IntelligentPlatform.logistics.model.WarehouseStoreItem;
import com.company.IntelligentPlatform.logistics.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * URL patterns:
 *   /api/v1/logistics/warehouseStores
 *   /api/v1/logistics/warehouseStoreItems
 */
@RestController
@RequestMapping("/api/v1/logistics")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    // --- WarehouseStore ---

    @GetMapping("/warehouseStores/{uuid}")
    public ApiResponse<WarehouseStore> getStore(@PathVariable String uuid) {
        return ApiResponse.success(warehouseService.getStoreByUuid(uuid));
    }

    @GetMapping("/warehouseStores")
    public ApiResponse<List<WarehouseStore>> getStoresByClient(@RequestParam String client,
                                                                @RequestParam(required = false) String refWarehouseUUID) {
        if (refWarehouseUUID != null) {
            return ApiResponse.success(warehouseService.getStoresByWarehouse(client, refWarehouseUUID));
        }
        return ApiResponse.success(warehouseService.getStoresByClient(client));
    }

    @PostMapping("/warehouseStores")
    public ApiResponse<WarehouseStore> createStore(@RequestBody WarehouseStore store) {
        return ApiResponse.success(warehouseService.createStore(store));
    }

    @PutMapping("/warehouseStores/{uuid}")
    public ApiResponse<WarehouseStore> updateStore(@PathVariable String uuid,
                                                    @RequestBody WarehouseStore store) {
        store.setUuid(uuid);
        return ApiResponse.success(warehouseService.updateStore(store));
    }

    @PutMapping("/warehouseStores/{uuid}/status/{status}")
    public ApiResponse<Void> setStoreStatus(@PathVariable String uuid, @PathVariable int status) {
        warehouseService.setStoreStatus(uuid, status);
        return ApiResponse.success(null);
    }

    // --- WarehouseStoreItem ---

    @GetMapping("/warehouseStoreItems/{uuid}")
    public ApiResponse<WarehouseStoreItem> getStoreItem(@PathVariable String uuid) {
        return ApiResponse.success(warehouseService.getStoreItemByUuid(uuid));
    }

    @GetMapping("/warehouseStoreItems")
    public ApiResponse<List<WarehouseStoreItem>> getStoreItems(
            @RequestParam(required = false) String documentUUID,
            @RequestParam(required = false) String refWarehouseUUID) {
        if (documentUUID != null) {
            return ApiResponse.success(warehouseService.getStoreItemsByDocument(documentUUID));
        }
        if (refWarehouseUUID != null) {
            return ApiResponse.success(warehouseService.getInstockItemsByWarehouse(refWarehouseUUID));
        }
        return ApiResponse.success(List.of());
    }

    @PostMapping("/warehouseStoreItems")
    public ApiResponse<WarehouseStoreItem> createStoreItem(@RequestBody WarehouseStoreItem item) {
        return ApiResponse.success(warehouseService.createStoreItem(item));
    }

    @PutMapping("/warehouseStoreItems/{uuid}")
    public ApiResponse<WarehouseStoreItem> updateStoreItem(@PathVariable String uuid,
                                                            @RequestBody WarehouseStoreItem item) {
        item.setUuid(uuid);
        return ApiResponse.success(warehouseService.updateStoreItem(item));
    }

    @PutMapping("/warehouseStoreItems/{uuid}/archive")
    public ApiResponse<Void> archiveStoreItem(@PathVariable String uuid) {
        warehouseService.archiveStoreItem(uuid);
        return ApiResponse.success(null);
    }
}
