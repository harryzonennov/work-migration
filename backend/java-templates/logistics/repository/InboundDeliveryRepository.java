package com.company.IntelligentPlatform.logistics.repository;

import com.company.IntelligentPlatform.logistics.model.InboundDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InboundDeliveryRepository extends JpaRepository<InboundDelivery, String>,
        JpaSpecificationExecutor<InboundDelivery> {

    List<InboundDelivery> findByClient(String client);
    List<InboundDelivery> findByClientAndStatus(String client, int status);
}
