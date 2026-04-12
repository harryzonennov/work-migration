package com.company.IntelligentPlatform.sales.controller;

import com.company.IntelligentPlatform.common.response.ApiResponse;
import com.company.IntelligentPlatform.sales.dto.*;
import com.company.IntelligentPlatform.sales.model.*;
import com.company.IntelligentPlatform.sales.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * URL patterns:
 *   /api/v1/sales/salesContracts
 *   /api/v1/sales/salesAreas
 *   /api/v1/sales/salesForcasts
 *   /api/v1/sales/salesReturnOrders
 *   /api/v1/sales/settleOrders
 */
@RestController
@RequestMapping("/api/v1/sales")
public class SalesController {

    @Autowired
    private SalesService salesService;

    // --- SalesContract ---

    @GetMapping("/salesContracts/{uuid}")
    public ApiResponse<SalesContract> getContract(@PathVariable String uuid) {
        return ApiResponse.success(salesService.getContractByUuid(uuid));
    }

    @GetMapping("/salesContracts")
    public ApiResponse<List<SalesContract>> getContractsByClient(@RequestParam String client) {
        return ApiResponse.success(salesService.getContractsByClient(client));
    }

    @PostMapping("/salesContracts")
    public ApiResponse<SalesContract> createContract(@RequestBody SalesContractDto dto) {
        return ApiResponse.success(salesService.createContract(dto.toEntity()));
    }

    @PutMapping("/salesContracts/{uuid}")
    public ApiResponse<SalesContract> updateContract(@PathVariable String uuid,
                                                      @RequestBody SalesContractDto dto) {
        SalesContract contract = salesService.getContractByUuid(uuid);
        dto.applyTo(contract);
        return ApiResponse.success(salesService.updateContract(contract));
    }

    @PutMapping("/salesContracts/{uuid}/status/{status}")
    public ApiResponse<Void> setContractStatus(@PathVariable String uuid, @PathVariable int status) {
        salesService.setContractStatus(uuid, status);
        return ApiResponse.success(null);
    }

    // --- SalesArea ---

    @GetMapping("/salesAreas/{uuid}")
    public ApiResponse<SalesArea> getArea(@PathVariable String uuid) {
        return ApiResponse.success(salesService.getAreaByUuid(uuid));
    }

    @GetMapping("/salesAreas")
    public ApiResponse<List<SalesArea>> getAreasByClient(@RequestParam String client) {
        return ApiResponse.success(salesService.getAreasByClient(client));
    }

    @GetMapping("/salesAreas/{uuid}/children")
    public ApiResponse<List<SalesArea>> getAreaChildren(@PathVariable String uuid) {
        return ApiResponse.success(salesService.getAreaChildren(uuid));
    }

    @PostMapping("/salesAreas")
    public ApiResponse<SalesArea> createArea(@RequestBody SalesAreaDto dto) {
        return ApiResponse.success(salesService.createArea(dto.toEntity()));
    }

    @PutMapping("/salesAreas/{uuid}")
    public ApiResponse<SalesArea> updateArea(@PathVariable String uuid,
                                              @RequestBody SalesAreaDto dto) {
        SalesArea area = salesService.getAreaByUuid(uuid);
        dto.applyTo(area);
        return ApiResponse.success(salesService.updateArea(area));
    }

    // --- SalesForcast ---

    @GetMapping("/salesForcasts/{uuid}")
    public ApiResponse<SalesForcast> getForcast(@PathVariable String uuid) {
        return ApiResponse.success(salesService.getForcastByUuid(uuid));
    }

    @GetMapping("/salesForcasts")
    public ApiResponse<List<SalesForcast>> getForcastsByClient(@RequestParam String client) {
        return ApiResponse.success(salesService.getForcastsByClient(client));
    }

    @PostMapping("/salesForcasts")
    public ApiResponse<SalesForcast> createForcast(@RequestBody SalesForcastDto dto) {
        return ApiResponse.success(salesService.createForcast(dto.toEntity()));
    }

    @PutMapping("/salesForcasts/{uuid}")
    public ApiResponse<SalesForcast> updateForcast(@PathVariable String uuid,
                                                    @RequestBody SalesForcastDto dto) {
        SalesForcast forcast = salesService.getForcastByUuid(uuid);
        dto.applyTo(forcast);
        return ApiResponse.success(salesService.updateForcast(forcast));
    }

    @PutMapping("/salesForcasts/{uuid}/status/{status}")
    public ApiResponse<Void> setForcastStatus(@PathVariable String uuid, @PathVariable int status) {
        salesService.setForcastStatus(uuid, status);
        return ApiResponse.success(null);
    }

    // --- SalesReturnOrder ---

    @GetMapping("/salesReturnOrders/{uuid}")
    public ApiResponse<SalesReturnOrder> getReturnOrder(@PathVariable String uuid) {
        return ApiResponse.success(salesService.getReturnOrderByUuid(uuid));
    }

    @GetMapping("/salesReturnOrders")
    public ApiResponse<List<SalesReturnOrder>> getReturnOrdersByClient(@RequestParam String client) {
        return ApiResponse.success(salesService.getReturnOrdersByClient(client));
    }

    @PostMapping("/salesReturnOrders")
    public ApiResponse<SalesReturnOrder> createReturnOrder(@RequestBody SalesReturnOrderDto dto) {
        return ApiResponse.success(salesService.createReturnOrder(dto.toEntity()));
    }

    @PutMapping("/salesReturnOrders/{uuid}")
    public ApiResponse<SalesReturnOrder> updateReturnOrder(@PathVariable String uuid,
                                                            @RequestBody SalesReturnOrderDto dto) {
        SalesReturnOrder returnOrder = salesService.getReturnOrderByUuid(uuid);
        dto.applyTo(returnOrder);
        return ApiResponse.success(salesService.updateReturnOrder(returnOrder));
    }

    @PutMapping("/salesReturnOrders/{uuid}/status/{status}")
    public ApiResponse<Void> setReturnOrderStatus(@PathVariable String uuid, @PathVariable int status) {
        salesService.setReturnOrderStatus(uuid, status);
        return ApiResponse.success(null);
    }

    // --- SettleOrder ---

    @GetMapping("/settleOrders/{uuid}")
    public ApiResponse<SettleOrder> getSettleOrder(@PathVariable String uuid) {
        return ApiResponse.success(salesService.getSettleOrderByUuid(uuid));
    }

    @GetMapping("/settleOrders")
    public ApiResponse<List<SettleOrder>> getSettleOrdersByClient(@RequestParam String client,
                                                                   @RequestParam(required = false) String refOrderUUID) {
        if (refOrderUUID != null) {
            return ApiResponse.success(salesService.getSettleOrdersByRefOrder(refOrderUUID));
        }
        return ApiResponse.success(salesService.getSettleOrdersByClient(client));
    }

    @PostMapping("/settleOrders")
    public ApiResponse<SettleOrder> createSettleOrder(@RequestBody SettleOrderDto dto) {
        return ApiResponse.success(salesService.createSettleOrder(dto.toEntity()));
    }

    @PutMapping("/settleOrders/{uuid}")
    public ApiResponse<SettleOrder> updateSettleOrder(@PathVariable String uuid,
                                                       @RequestBody SettleOrderDto dto) {
        SettleOrder settleOrder = salesService.getSettleOrderByUuid(uuid);
        dto.applyTo(settleOrder);
        return ApiResponse.success(salesService.updateSettleOrder(settleOrder));
    }

    @PutMapping("/settleOrders/{uuid}/status/{status}")
    public ApiResponse<Void> setSettleOrderStatus(@PathVariable String uuid, @PathVariable int status) {
        salesService.setSettleOrderStatus(uuid, status);
        return ApiResponse.success(null);
    }
}
