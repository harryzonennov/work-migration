package com.company.IntelligentPlatform.finance.repository;

import com.company.IntelligentPlatform.finance.model.FinAccountTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Replaces: ThorsteinFinance FinAccountTitleDAO (Hibernate)
 */
@Repository
public interface FinAccountTitleRepository extends JpaRepository<FinAccountTitle, String> {

    List<FinAccountTitle> findByParentAccountTitleUUID(String parentAccountTitleUUID);

    List<FinAccountTitle> findByClient(String client);
}
