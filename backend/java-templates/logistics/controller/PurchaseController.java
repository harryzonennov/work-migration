package com.company.IntelligentPlatform.logistics.controller;

import com.company.IntelligentPlatform.common.response.ApiResponse;
import com.company.IntelligentPlatform.logistics.dto.*;
import com.company.IntelligentPlatform.logistics.model.*;
import com.company.IntelligentPlatform.logistics.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * URL patterns:
 *   /api/v1/logistics/purchaseContracts
 *   /api/v1/logistics/purchaseRequests
 *   /api/v1/logistics/purchaseOrders
 *   /api/v1/logistics/purchaseReturnOrders
 *   /api/v1/logistics/inquiries
 */
@RestController
@RequestMapping("/api/v1/logistics")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    // --- PurchaseContract ---

    @GetMapping("/purchaseContracts/{uuid}")
    public ApiResponse<PurchaseContract> getContract(@PathVariable String uuid) {
        return ApiResponse.success(purchaseService.getContractByUuid(uuid));
    }

    @GetMapping("/purchaseContracts")
    public ApiResponse<List<PurchaseContract>> getContractsByClient(@RequestParam String client) {
        return ApiResponse.success(purchaseService.getContractsByClient(client));
    }

    @PostMapping("/purchaseContracts")
    public ApiResponse<PurchaseContract> createContract(@RequestBody PurchaseContractDto dto) {
        return ApiResponse.success(purchaseService.createContract(dto.toEntity()));
    }

    @PutMapping("/purchaseContracts/{uuid}")
    public ApiResponse<PurchaseContract> updateContract(@PathVariable String uuid,
                                                         @RequestBody PurchaseContractDto dto) {
        PurchaseContract contract = purchaseService.getContractByUuid(uuid);
        dto.applyTo(contract);
        return ApiResponse.success(purchaseService.updateContract(contract));
    }

    @PutMapping("/purchaseContracts/{uuid}/status/{status}")
    public ApiResponse<Void> setContractStatus(@PathVariable String uuid, @PathVariable int status) {
        purchaseService.setContractStatus(uuid, status);
        return ApiResponse.success(null);
    }

    // --- PurchaseRequest ---

    @GetMapping("/purchaseRequests/{uuid}")
    public ApiResponse<PurchaseRequest> getRequest(@PathVariable String uuid) {
        return ApiResponse.success(purchaseService.getRequestByUuid(uuid));
    }

    @GetMapping("/purchaseRequests")
    public ApiResponse<List<PurchaseRequest>> getRequestsByClient(@RequestParam String client) {
        return ApiResponse.success(purchaseService.getRequestsByClient(client));
    }

    @PostMapping("/purchaseRequests")
    public ApiResponse<PurchaseRequest> createRequest(@RequestBody PurchaseRequestDto dto) {
        return ApiResponse.success(purchaseService.createRequest(dto.toEntity()));
    }

    @PutMapping("/purchaseRequests/{uuid}")
    public ApiResponse<PurchaseRequest> updateRequest(@PathVariable String uuid,
                                                       @RequestBody PurchaseRequestDto dto) {
        PurchaseRequest request = purchaseService.getRequestByUuid(uuid);
        dto.applyTo(request);
        return ApiResponse.success(purchaseService.updateRequest(request));
    }

    @PutMapping("/purchaseRequests/{uuid}/status/{status}")
    public ApiResponse<Void> setRequestStatus(@PathVariable String uuid, @PathVariable int status) {
        purchaseService.setRequestStatus(uuid, status);
        return ApiResponse.success(null);
    }

    // --- PurchaseOrder ---

    @GetMapping("/purchaseOrders/{uuid}")
    public ApiResponse<PurchaseOrder> getOrder(@PathVariable String uuid) {
        return ApiResponse.success(purchaseService.getOrderByUuid(uuid));
    }

    @GetMapping("/purchaseOrders")
    public ApiResponse<List<PurchaseOrder>> getOrdersByClient(@RequestParam String client) {
        return ApiResponse.success(purchaseService.getOrdersByClient(client));
    }

    @PostMapping("/purchaseOrders")
    public ApiResponse<PurchaseOrder> createOrder(@RequestBody PurchaseOrderDto dto) {
        return ApiResponse.success(purchaseService.createOrder(dto.toEntity()));
    }

    @PutMapping("/purchaseOrders/{uuid}")
    public ApiResponse<PurchaseOrder> updateOrder(@PathVariable String uuid,
                                                   @RequestBody PurchaseOrderDto dto) {
        PurchaseOrder order = purchaseService.getOrderByUuid(uuid);
        dto.applyTo(order);
        return ApiResponse.success(purchaseService.updateOrder(order));
    }

    @PutMapping("/purchaseOrders/{uuid}/status/{status}")
    public ApiResponse<Void> setOrderStatus(@PathVariable String uuid, @PathVariable int status) {
        purchaseService.setOrderStatus(uuid, status);
        return ApiResponse.success(null);
    }

    // --- PurchaseReturnOrder ---

    @GetMapping("/purchaseReturnOrders/{uuid}")
    public ApiResponse<PurchaseReturnOrder> getReturnOrder(@PathVariable String uuid) {
        return ApiResponse.success(purchaseService.getReturnOrderByUuid(uuid));
    }

    @GetMapping("/purchaseReturnOrders")
    public ApiResponse<List<PurchaseReturnOrder>> getReturnOrdersByClient(@RequestParam String client) {
        return ApiResponse.success(purchaseService.getReturnOrdersByClient(client));
    }

    @PostMapping("/purchaseReturnOrders")
    public ApiResponse<PurchaseReturnOrder> createReturnOrder(@RequestBody PurchaseReturnOrderDto dto) {
        return ApiResponse.success(purchaseService.createReturnOrder(dto.toEntity()));
    }

    @PutMapping("/purchaseReturnOrders/{uuid}")
    public ApiResponse<PurchaseReturnOrder> updateReturnOrder(@PathVariable String uuid,
                                                               @RequestBody PurchaseReturnOrderDto dto) {
        PurchaseReturnOrder returnOrder = purchaseService.getReturnOrderByUuid(uuid);
        dto.applyTo(returnOrder);
        return ApiResponse.success(purchaseService.updateReturnOrder(returnOrder));
    }

    @PutMapping("/purchaseReturnOrders/{uuid}/status/{status}")
    public ApiResponse<Void> setReturnOrderStatus(@PathVariable String uuid, @PathVariable int status) {
        purchaseService.setReturnOrderStatus(uuid, status);
        return ApiResponse.success(null);
    }

    // --- Inquiry ---

    @GetMapping("/inquiries/{uuid}")
    public ApiResponse<Inquiry> getInquiry(@PathVariable String uuid) {
        return ApiResponse.success(purchaseService.getInquiryByUuid(uuid));
    }

    @GetMapping("/inquiries")
    public ApiResponse<List<Inquiry>> getInquiriesByClient(@RequestParam String client) {
        return ApiResponse.success(purchaseService.getInquiriesByClient(client));
    }

    @PostMapping("/inquiries")
    public ApiResponse<Inquiry> createInquiry(@RequestBody InquiryDto dto) {
        return ApiResponse.success(purchaseService.createInquiry(dto.toEntity()));
    }

    @PutMapping("/inquiries/{uuid}")
    public ApiResponse<Inquiry> updateInquiry(@PathVariable String uuid, @RequestBody InquiryDto dto) {
        Inquiry inquiry = purchaseService.getInquiryByUuid(uuid);
        dto.applyTo(inquiry);
        return ApiResponse.success(purchaseService.updateInquiry(inquiry));
    }

    @PutMapping("/inquiries/{uuid}/status/{status}")
    public ApiResponse<Void> setInquiryStatus(@PathVariable String uuid, @PathVariable int status) {
        purchaseService.setInquiryStatus(uuid, status);
        return ApiResponse.success(null);
    }
}
