package com.company.IntelligentPlatform.logistics.repository;

import com.company.IntelligentPlatform.logistics.model.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, String>,
        JpaSpecificationExecutor<Inquiry> {

    List<Inquiry> findByClient(String client);
    List<Inquiry> findByClientAndStatus(String client, int status);
}
