package com.company.IntelligentPlatform.common.controller;

import com.company.IntelligentPlatform.common.dto.EmployeeDto;
import com.company.IntelligentPlatform.common.model.Employee;
import com.company.IntelligentPlatform.common.service.EmployeeService;
import com.company.IntelligentPlatform.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Replaces: ThorsteinPlatform EmployeeController (SEEditorController subclass)
 * URL pattern: /api/v1/platform/employees
 */
@RestController
@RequestMapping("/api/v1/platform/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/{uuid}")
    public ApiResponse<Employee> get(@PathVariable String uuid) {
        return ApiResponse.success(employeeService.getByUuid(uuid));
    }

    @GetMapping
    public ApiResponse<List<Employee>> getByClient(@RequestParam String client) {
        return ApiResponse.success(employeeService.getByClient(client));
    }

    @PostMapping
    public ApiResponse<Employee> create(@RequestBody EmployeeDto dto) {
        Employee employee = dto.toEntity();
        return ApiResponse.success(employeeService.create(employee));
    }

    @PutMapping("/{uuid}")
    public ApiResponse<Employee> update(@PathVariable String uuid, @RequestBody EmployeeDto dto) {
        Employee employee = employeeService.getByUuid(uuid);
        dto.applyTo(employee);
        return ApiResponse.success(employeeService.update(employee));
    }

    @PutMapping("/{uuid}/status/{status}")
    public ApiResponse<Void> setStatus(@PathVariable String uuid, @PathVariable int status) {
        employeeService.setStatus(uuid, status);
        return ApiResponse.success(null);
    }
}
