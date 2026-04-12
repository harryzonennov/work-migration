package com.company.IntelligentPlatform.finance.controller;

import com.company.IntelligentPlatform.common.response.ApiResponse;
import com.company.IntelligentPlatform.finance.dto.FinAccountDto;
import com.company.IntelligentPlatform.finance.model.FinAccount;
import com.company.IntelligentPlatform.finance.model.FinAccountTitle;
import com.company.IntelligentPlatform.finance.service.FinAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Replaces: ThorsteinFinance FinAccountController (SEEditorController subclass)
 * URL pattern: /api/v1/finance/finAccounts
 */
@RestController
@RequestMapping("/api/v1/finance/finAccounts")
public class FinAccountController {

    @Autowired
    private FinAccountService finAccountService;

    @GetMapping("/{uuid}")
    public ApiResponse<FinAccount> get(@PathVariable String uuid) {
        return ApiResponse.success(finAccountService.getByUuid(uuid));
    }

    @GetMapping
    public ApiResponse<List<FinAccount>> getByClient(@RequestParam String client) {
        return ApiResponse.success(finAccountService.getByClient(client));
    }

    @PostMapping
    public ApiResponse<FinAccount> create(@RequestBody FinAccountDto dto) {
        return ApiResponse.success(finAccountService.create(dto.toEntity()));
    }

    @PutMapping("/{uuid}")
    public ApiResponse<FinAccount> update(@PathVariable String uuid, @RequestBody FinAccountDto dto) {
        FinAccount account = finAccountService.getByUuid(uuid);
        dto.applyTo(account);
        return ApiResponse.success(finAccountService.update(account));
    }

    @PutMapping("/{uuid}/audit")
    public ApiResponse<Void> audit(@PathVariable String uuid,
                                   @RequestParam String auditBy,
                                   @RequestParam(required = false, defaultValue = "") String auditNote,
                                   @RequestParam boolean approve) {
        finAccountService.audit(uuid, auditBy, auditNote, approve);
        return ApiResponse.success(null);
    }

    @PutMapping("/{uuid}/record")
    public ApiResponse<Void> record(@PathVariable String uuid,
                                    @RequestParam String recordBy,
                                    @RequestParam(required = false, defaultValue = "") String recordNote) {
        finAccountService.record(uuid, recordBy, recordNote);
        return ApiResponse.success(null);
    }

    @PutMapping("/{uuid}/verify")
    public ApiResponse<Void> verify(@PathVariable String uuid,
                                    @RequestParam String verifyBy,
                                    @RequestParam(required = false, defaultValue = "") String verifyNote) {
        finAccountService.verify(uuid, verifyBy, verifyNote);
        return ApiResponse.success(null);
    }
}
