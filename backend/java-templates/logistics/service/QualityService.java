package com.company.IntelligentPlatform.logistics.service;

import com.company.IntelligentPlatform.logistics.model.*;
import com.company.IntelligentPlatform.logistics.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Replaces: ThorsteinLogistics - QualityInspectOrderManager, WasteProcessOrderManager
 */
@Service
@Transactional
public class QualityService {

    @Autowired private QualityInspectOrderRepository qualityInspectOrderRepository;
    @Autowired private WasteProcessOrderRepository wasteProcessOrderRepository;

    // --- QualityInspectOrder ---

    public QualityInspectOrder createInspect(QualityInspectOrder order) {
        order.setUuid(UUID.randomUUID().toString());
        order.setStatus(QualityInspectOrder.STATUS_INITIAL);
        order.setCheckStatus(QualityInspectOrder.CHECKSTATUS_INITIAL);
        return qualityInspectOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public QualityInspectOrder getInspectByUuid(String uuid) {
        return qualityInspectOrderRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<QualityInspectOrder> getInspectsByClient(String client) {
        return qualityInspectOrderRepository.findByClient(client);
    }

    @Transactional(readOnly = true)
    public List<QualityInspectOrder> getInspectsByClientAndCategory(String client, int category) {
        return qualityInspectOrderRepository.findByClientAndCategory(client, category);
    }

    public QualityInspectOrder updateInspect(QualityInspectOrder order) {
        return qualityInspectOrderRepository.save(order);
    }

    public void setInspectStatus(String uuid, int status) {
        QualityInspectOrder order = qualityInspectOrderRepository.findById(uuid).orElseThrow();
        order.setStatus(status);
        qualityInspectOrderRepository.save(order);
    }

    public void setInspectCheckStatus(String uuid, int checkStatus) {
        QualityInspectOrder order = qualityInspectOrderRepository.findById(uuid).orElseThrow();
        order.setCheckStatus(checkStatus);
        qualityInspectOrderRepository.save(order);
    }

    // --- WasteProcessOrder ---

    public WasteProcessOrder createWasteProcess(WasteProcessOrder order) {
        order.setUuid(UUID.randomUUID().toString());
        order.setStatus(WasteProcessOrder.STATUS_INITIAL);
        return wasteProcessOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public WasteProcessOrder getWasteProcessByUuid(String uuid) {
        return wasteProcessOrderRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<WasteProcessOrder> getWasteProcessesByClient(String client) {
        return wasteProcessOrderRepository.findByClient(client);
    }

    public WasteProcessOrder updateWasteProcess(WasteProcessOrder order) {
        return wasteProcessOrderRepository.save(order);
    }

    public void setWasteProcessStatus(String uuid, int status) {
        WasteProcessOrder order = wasteProcessOrderRepository.findById(uuid).orElseThrow();
        order.setStatus(status);
        wasteProcessOrderRepository.save(order);
    }
}
