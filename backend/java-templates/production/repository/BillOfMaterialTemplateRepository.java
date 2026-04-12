package com.company.IntelligentPlatform.production.repository;

import com.company.IntelligentPlatform.production.model.BillOfMaterialTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BillOfMaterialTemplateRepository extends JpaRepository<BillOfMaterialTemplate, String>, JpaSpecificationExecutor<BillOfMaterialTemplate> {
}
