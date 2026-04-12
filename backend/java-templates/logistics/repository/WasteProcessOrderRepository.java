package com.company.IntelligentPlatform.logistics.repository;

import com.company.IntelligentPlatform.logistics.model.WasteProcessOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WasteProcessOrderRepository extends JpaRepository<WasteProcessOrder, String>,
        JpaSpecificationExecutor<WasteProcessOrder> {

    List<WasteProcessOrder> findByClient(String client);
    List<WasteProcessOrder> findByClientAndStatus(String client, int status);
}
