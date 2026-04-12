package com.company.IntelligentPlatform.logistics.controller;

import com.company.IntelligentPlatform.common.response.ApiResponse;
import com.company.IntelligentPlatform.logistics.dto.QualityInspectOrderDto;
import com.company.IntelligentPlatform.logistics.dto.WasteProcessOrderDto;
import com.company.IntelligentPlatform.logistics.model.QualityInspectOrder;
import com.company.IntelligentPlatform.logistics.model.WasteProcessOrder;
import com.company.IntelligentPlatform.logistics.service.QualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * URL patterns:
 *   /api/v1/logistics/qualityInspectOrders
 *   /api/v1/logistics/wasteProcessOrders
 */
@RestController
@RequestMapping("/api/v1/logistics")
public class QualityController {

    @Autowired
    private QualityService qualityService;

    // --- QualityInspectOrder ---

    @GetMapping("/qualityInspectOrders/{uuid}")
    public ApiResponse<QualityInspectOrder> getInspect(@PathVariable String uuid) {
        return ApiResponse.success(qualityService.getInspectByUuid(uuid));
    }

    @GetMapping("/qualityInspectOrders")
    public ApiResponse<List<QualityInspectOrder>> getInspectsByClient(@RequestParam String client) {
        return ApiResponse.success(qualityService.getInspectsByClient(client));
    }

    @PostMapping("/qualityInspectOrders")
    public ApiResponse<QualityInspectOrder> createInspect(@RequestBody QualityInspectOrderDto dto) {
        return ApiResponse.success(qualityService.createInspect(dto.toEntity()));
    }

    @PutMapping("/qualityInspectOrders/{uuid}")
    public ApiResponse<QualityInspectOrder> updateInspect(@PathVariable String uuid,
                                                           @RequestBody QualityInspectOrderDto dto) {
        QualityInspectOrder order = qualityService.getInspectByUuid(uuid);
        dto.applyTo(order);
        return ApiResponse.success(qualityService.updateInspect(order));
    }

    @PutMapping("/qualityInspectOrders/{uuid}/status/{status}")
    public ApiResponse<Void> setInspectStatus(@PathVariable String uuid, @PathVariable int status) {
        qualityService.setInspectStatus(uuid, status);
        return ApiResponse.success(null);
    }

    @PutMapping("/qualityInspectOrders/{uuid}/checkStatus/{checkStatus}")
    public ApiResponse<Void> setInspectCheckStatus(@PathVariable String uuid,
                                                    @PathVariable int checkStatus) {
        qualityService.setInspectCheckStatus(uuid, checkStatus);
        return ApiResponse.success(null);
    }

    // --- WasteProcessOrder ---

    @GetMapping("/wasteProcessOrders/{uuid}")
    public ApiResponse<WasteProcessOrder> getWasteProcess(@PathVariable String uuid) {
        return ApiResponse.success(qualityService.getWasteProcessByUuid(uuid));
    }

    @GetMapping("/wasteProcessOrders")
    public ApiResponse<List<WasteProcessOrder>> getWasteProcessesByClient(@RequestParam String client) {
        return ApiResponse.success(qualityService.getWasteProcessesByClient(client));
    }

    @PostMapping("/wasteProcessOrders")
    public ApiResponse<WasteProcessOrder> createWasteProcess(@RequestBody WasteProcessOrderDto dto) {
        return ApiResponse.success(qualityService.createWasteProcess(dto.toEntity()));
    }

    @PutMapping("/wasteProcessOrders/{uuid}")
    public ApiResponse<WasteProcessOrder> updateWasteProcess(@PathVariable String uuid,
                                                              @RequestBody WasteProcessOrderDto dto) {
        WasteProcessOrder order = qualityService.getWasteProcessByUuid(uuid);
        dto.applyTo(order);
        return ApiResponse.success(qualityService.updateWasteProcess(order));
    }

    @PutMapping("/wasteProcessOrders/{uuid}/status/{status}")
    public ApiResponse<Void> setWasteProcessStatus(@PathVariable String uuid, @PathVariable int status) {
        qualityService.setWasteProcessStatus(uuid, status);
        return ApiResponse.success(null);
    }
}
