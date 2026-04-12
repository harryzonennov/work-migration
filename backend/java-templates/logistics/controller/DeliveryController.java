package com.company.IntelligentPlatform.logistics.controller;

import com.company.IntelligentPlatform.common.response.ApiResponse;
import com.company.IntelligentPlatform.logistics.dto.InboundDeliveryDto;
import com.company.IntelligentPlatform.logistics.dto.OutboundDeliveryDto;
import com.company.IntelligentPlatform.logistics.model.InboundDelivery;
import com.company.IntelligentPlatform.logistics.model.InventoryCheckOrder;
import com.company.IntelligentPlatform.logistics.model.InventoryTransferOrder;
import com.company.IntelligentPlatform.logistics.model.OutboundDelivery;
import com.company.IntelligentPlatform.logistics.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * URL pattern: /api/v1/logistics/inboundDeliveries
 *              /api/v1/logistics/outboundDeliveries
 *              /api/v1/logistics/inventoryCheckOrders
 *              /api/v1/logistics/inventoryTransferOrders
 */
@RestController
@RequestMapping("/api/v1/logistics")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    // --- InboundDelivery ---

    @GetMapping("/inboundDeliveries/{uuid}")
    public ApiResponse<InboundDelivery> getInbound(@PathVariable String uuid) {
        return ApiResponse.success(deliveryService.getInboundByUuid(uuid));
    }

    @GetMapping("/inboundDeliveries")
    public ApiResponse<List<InboundDelivery>> getInboundByClient(@RequestParam String client) {
        return ApiResponse.success(deliveryService.getInboundByClient(client));
    }

    @PostMapping("/inboundDeliveries")
    public ApiResponse<InboundDelivery> createInbound(@RequestBody InboundDeliveryDto dto) {
        return ApiResponse.success(deliveryService.createInbound(dto.toEntity()));
    }

    @PutMapping("/inboundDeliveries/{uuid}")
    public ApiResponse<InboundDelivery> updateInbound(@PathVariable String uuid,
                                                       @RequestBody InboundDeliveryDto dto) {
        InboundDelivery delivery = deliveryService.getInboundByUuid(uuid);
        dto.applyTo(delivery);
        return ApiResponse.success(deliveryService.updateInbound(delivery));
    }

    @PutMapping("/inboundDeliveries/{uuid}/status/{status}")
    public ApiResponse<Void> setInboundStatus(@PathVariable String uuid, @PathVariable int status) {
        deliveryService.setInboundStatus(uuid, status);
        return ApiResponse.success(null);
    }

    // --- OutboundDelivery ---

    @GetMapping("/outboundDeliveries/{uuid}")
    public ApiResponse<OutboundDelivery> getOutbound(@PathVariable String uuid) {
        return ApiResponse.success(deliveryService.getOutboundByUuid(uuid));
    }

    @GetMapping("/outboundDeliveries")
    public ApiResponse<List<OutboundDelivery>> getOutboundByClient(@RequestParam String client) {
        return ApiResponse.success(deliveryService.getOutboundByClient(client));
    }

    @PostMapping("/outboundDeliveries")
    public ApiResponse<OutboundDelivery> createOutbound(@RequestBody OutboundDeliveryDto dto) {
        return ApiResponse.success(deliveryService.createOutbound(dto.toEntity()));
    }

    @PutMapping("/outboundDeliveries/{uuid}")
    public ApiResponse<OutboundDelivery> updateOutbound(@PathVariable String uuid,
                                                         @RequestBody OutboundDeliveryDto dto) {
        OutboundDelivery delivery = deliveryService.getOutboundByUuid(uuid);
        dto.applyTo(delivery);
        return ApiResponse.success(deliveryService.updateOutbound(delivery));
    }

    @PutMapping("/outboundDeliveries/{uuid}/status/{status}")
    public ApiResponse<Void> setOutboundStatus(@PathVariable String uuid, @PathVariable int status) {
        deliveryService.setOutboundStatus(uuid, status);
        return ApiResponse.success(null);
    }

    // --- InventoryCheckOrder ---

    @GetMapping("/inventoryCheckOrders/{uuid}")
    public ApiResponse<InventoryCheckOrder> getInventoryCheck(@PathVariable String uuid) {
        return ApiResponse.success(deliveryService.getInventoryCheckByUuid(uuid));
    }

    @GetMapping("/inventoryCheckOrders")
    public ApiResponse<List<InventoryCheckOrder>> getInventoryCheckByClient(@RequestParam String client) {
        return ApiResponse.success(deliveryService.getInventoryCheckByClient(client));
    }

    @PostMapping("/inventoryCheckOrders")
    public ApiResponse<InventoryCheckOrder> createInventoryCheck(@RequestBody InventoryCheckOrder order) {
        return ApiResponse.success(deliveryService.createInventoryCheck(order));
    }

    @PutMapping("/inventoryCheckOrders/{uuid}")
    public ApiResponse<InventoryCheckOrder> updateInventoryCheck(@PathVariable String uuid,
                                                                  @RequestBody InventoryCheckOrder order) {
        InventoryCheckOrder existing = deliveryService.getInventoryCheckByUuid(uuid);
        order.setUuid(existing.getUuid());
        return ApiResponse.success(deliveryService.updateInventoryCheck(order));
    }

    @PutMapping("/inventoryCheckOrders/{uuid}/status/{status}")
    public ApiResponse<Void> setInventoryCheckStatus(@PathVariable String uuid, @PathVariable int status) {
        deliveryService.setInventoryCheckStatus(uuid, status);
        return ApiResponse.success(null);
    }

    // --- InventoryTransferOrder ---

    @GetMapping("/inventoryTransferOrders/{uuid}")
    public ApiResponse<InventoryTransferOrder> getInventoryTransfer(@PathVariable String uuid) {
        return ApiResponse.success(deliveryService.getInventoryTransferByUuid(uuid));
    }

    @GetMapping("/inventoryTransferOrders")
    public ApiResponse<List<InventoryTransferOrder>> getInventoryTransferByClient(@RequestParam String client) {
        return ApiResponse.success(deliveryService.getInventoryTransferByClient(client));
    }

    @PostMapping("/inventoryTransferOrders")
    public ApiResponse<InventoryTransferOrder> createInventoryTransfer(@RequestBody InventoryTransferOrder order) {
        return ApiResponse.success(deliveryService.createInventoryTransfer(order));
    }

    @PutMapping("/inventoryTransferOrders/{uuid}/status/{status}")
    public ApiResponse<Void> setInventoryTransferStatus(@PathVariable String uuid, @PathVariable int status) {
        deliveryService.setInventoryTransferStatus(uuid, status);
        return ApiResponse.success(null);
    }
}
