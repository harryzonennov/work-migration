package com.company.IntelligentPlatform.common.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Replaces: hibernate.cfg.xml + Spring XML datasource config
 *
 * Spring Boot auto-configures DataSource and EntityManagerFactory
 * from application.yml — this class just registers scan paths.
 */
@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "com.company.IntelligentPlatform")
@EnableJpaRepositories(basePackages = "com.company.IntelligentPlatform")
public class JpaConfig {
    // DataSource, EntityManagerFactory, TransactionManager
    // are all auto-configured by Spring Boot from application.yml
}
