package com.company.IntelligentPlatform.finance.controller;

import com.company.IntelligentPlatform.common.response.ApiResponse;
import com.company.IntelligentPlatform.finance.model.FinAccountTitle;
import com.company.IntelligentPlatform.finance.service.FinAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Replaces: ThorsteinFinance FinAccountTitleController (SEEditorController subclass)
 * URL pattern: /api/v1/finance/finAccountTitles
 */
@RestController
@RequestMapping("/api/v1/finance/finAccountTitles")
public class FinAccountTitleController {

    @Autowired
    private FinAccountService finAccountService;

    @GetMapping("/{uuid}")
    public ApiResponse<FinAccountTitle> get(@PathVariable String uuid) {
        return ApiResponse.success(finAccountService.getTitleByUuid(uuid));
    }

    @GetMapping
    public ApiResponse<List<FinAccountTitle>> getByClient(@RequestParam String client) {
        return ApiResponse.success(finAccountService.getTitlesByClient(client));
    }

    @GetMapping("/{uuid}/children")
    public ApiResponse<List<FinAccountTitle>> getChildren(@PathVariable String uuid) {
        return ApiResponse.success(finAccountService.getTitleChildren(uuid));
    }

    @PostMapping
    public ApiResponse<FinAccountTitle> create(@RequestBody FinAccountTitle title) {
        return ApiResponse.success(finAccountService.createTitle(title));
    }
}
