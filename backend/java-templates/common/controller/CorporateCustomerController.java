package com.company.IntelligentPlatform.common.controller;

import com.company.IntelligentPlatform.common.dto.CorporateCustomerDto;
import com.company.IntelligentPlatform.common.model.CorporateCustomer;
import com.company.IntelligentPlatform.common.service.CorporateCustomerService;
import com.company.IntelligentPlatform.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Replaces: ThorsteinPlatform CorporateCustomerController (SEEditorController subclass)
 * URL pattern: /api/v1/platform/corporateCustomers
 */
@RestController
@RequestMapping("/api/v1/platform/corporateCustomers")
public class CorporateCustomerController {

    @Autowired
    private CorporateCustomerService corporateCustomerService;

    @GetMapping("/{uuid}")
    public ApiResponse<CorporateCustomer> get(@PathVariable String uuid) {
        return ApiResponse.success(corporateCustomerService.getByUuid(uuid));
    }

    @GetMapping
    public ApiResponse<List<CorporateCustomer>> getByClient(@RequestParam String client) {
        return ApiResponse.success(corporateCustomerService.getByClient(client));
    }

    @PostMapping
    public ApiResponse<CorporateCustomer> create(@RequestBody CorporateCustomerDto dto) {
        CorporateCustomer customer = dto.toEntity();
        return ApiResponse.success(corporateCustomerService.create(customer));
    }

    @PutMapping("/{uuid}")
    public ApiResponse<CorporateCustomer> update(@PathVariable String uuid, @RequestBody CorporateCustomerDto dto) {
        CorporateCustomer customer = corporateCustomerService.getByUuid(uuid);
        dto.applyTo(customer);
        return ApiResponse.success(corporateCustomerService.update(customer));
    }

    @PutMapping("/{uuid}/status/{status}")
    public ApiResponse<Void> setStatus(@PathVariable String uuid, @PathVariable int status) {
        corporateCustomerService.setStatus(uuid, status);
        return ApiResponse.success(null);
    }
}
