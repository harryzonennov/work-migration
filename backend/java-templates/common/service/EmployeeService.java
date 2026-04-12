package com.company.IntelligentPlatform.common.service;

import com.company.IntelligentPlatform.common.model.Employee;
import com.company.IntelligentPlatform.common.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Replaces: ThorsteinPlatform EmployeeManager (ServiceEntityManager subclass)
 */
@Service
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public Employee create(Employee employee) {
        employee.setUuid(UUID.randomUUID().toString());
        employee.setStatus(Employee.STATUS_INIT);
        employee.setAccountType(Employee.ACCOUNTTYPE_EMPLOYEE);
        return employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public Employee getByUuid(String uuid) {
        return employeeRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Employee> getByClient(String client) {
        return employeeRepository.findByClient(client);
    }

    public Employee update(Employee employee) {
        return employeeRepository.save(employee);
    }

    public void setStatus(String uuid, int status) {
        Employee employee = employeeRepository.findById(uuid).orElseThrow();
        employee.setStatus(status);
        employeeRepository.save(employee);
    }
}
