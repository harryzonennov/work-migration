package com.company.IntelligentPlatform.common.repository;

import com.company.IntelligentPlatform.common.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Replaces: ThorsteinPlatform EmployeeDAO (Hibernate)
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String>,
        JpaSpecificationExecutor<Employee> {

    List<Employee> findByClient(String client);

    List<Employee> findByStatus(int status);
}
