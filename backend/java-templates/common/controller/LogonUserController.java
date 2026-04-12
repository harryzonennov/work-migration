package com.company.IntelligentPlatform.common.controller;

import com.company.IntelligentPlatform.common.dto.LogonUserDto;
import com.company.IntelligentPlatform.common.model.LogonUser;
import com.company.IntelligentPlatform.common.service.LogonUserService;
import com.company.IntelligentPlatform.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Replaces: ThorsteinPlatform LogonUserController (SEEditorController subclass)
 * URL pattern: /api/v1/platform/logonUsers
 */
@RestController
@RequestMapping("/api/v1/platform/logonUsers")
public class LogonUserController {

    @Autowired
    private LogonUserService logonUserService;

    @GetMapping("/{uuid}")
    public ApiResponse<LogonUser> get(@PathVariable String uuid) {
        return ApiResponse.success(logonUserService.getByUuid(uuid));
    }

    @GetMapping
    public ApiResponse<List<LogonUser>> getAll() {
        return ApiResponse.success(logonUserService.getAll());
    }

    @PostMapping
    public ApiResponse<LogonUser> create(@RequestBody LogonUserDto dto) {
        LogonUser user = dto.toEntity();
        return ApiResponse.success(logonUserService.create(user));
    }

    @PutMapping("/{uuid}")
    public ApiResponse<LogonUser> update(@PathVariable String uuid, @RequestBody LogonUserDto dto) {
        LogonUser user = logonUserService.getByUuid(uuid);
        dto.applyTo(user);
        return ApiResponse.success(logonUserService.update(user));
    }

    @PutMapping("/{uuid}/status/{status}")
    public ApiResponse<Void> setStatus(@PathVariable String uuid, @PathVariable int status) {
        logonUserService.setStatus(uuid, status);
        return ApiResponse.success(null);
    }
}
