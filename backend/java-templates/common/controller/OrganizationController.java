package com.company.IntelligentPlatform.common.controller;

import com.company.IntelligentPlatform.common.dto.OrganizationDto;
import com.company.IntelligentPlatform.common.model.Organization;
import com.company.IntelligentPlatform.common.service.OrganizationService;
import com.company.IntelligentPlatform.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Replaces: ThorsteinPlatform OrganizationController (SEEditorController subclass)
 * URL pattern: /api/v1/platform/organizations
 */
@RestController
@RequestMapping("/api/v1/platform/organizations")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @GetMapping("/{uuid}")
    public ApiResponse<Organization> get(@PathVariable String uuid) {
        return ApiResponse.success(organizationService.getByUuid(uuid));
    }

    @GetMapping
    public ApiResponse<List<Organization>> getByClient(@RequestParam String client) {
        return ApiResponse.success(organizationService.getByClient(client));
    }

    @GetMapping("/{uuid}/children")
    public ApiResponse<List<Organization>> getChildren(@PathVariable String uuid) {
        return ApiResponse.success(organizationService.getChildren(uuid));
    }

    @PostMapping
    public ApiResponse<Organization> create(@RequestBody OrganizationDto dto) {
        Organization org = dto.toEntity();
        return ApiResponse.success(organizationService.create(org));
    }

    @PutMapping("/{uuid}")
    public ApiResponse<Organization> update(@PathVariable String uuid, @RequestBody OrganizationDto dto) {
        Organization org = organizationService.getByUuid(uuid);
        dto.applyTo(org);
        return ApiResponse.success(organizationService.update(org));
    }
}
