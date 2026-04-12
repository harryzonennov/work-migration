package com.company.IntelligentPlatform.common.repository;

import com.company.IntelligentPlatform.common.model.LogonUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Replaces: ThorsteinPlatform LogonUserDAO (Hibernate)
 */
@Repository
public interface LogonUserRepository extends JpaRepository<LogonUser, String> {

    Optional<LogonUser> findByName(String name);

    boolean existsByName(String name);
}
