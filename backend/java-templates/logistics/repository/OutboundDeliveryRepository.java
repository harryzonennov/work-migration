package com.company.IntelligentPlatform.logistics.repository;

import com.company.IntelligentPlatform.logistics.model.OutboundDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OutboundDeliveryRepository extends JpaRepository<OutboundDelivery, String>,
        JpaSpecificationExecutor<OutboundDelivery> {

    List<OutboundDelivery> findByClient(String client);
    List<OutboundDelivery> findByClientAndStatus(String client, int status);
}
