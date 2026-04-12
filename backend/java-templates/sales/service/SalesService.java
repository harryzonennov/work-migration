package com.company.IntelligentPlatform.sales.service;

import com.company.IntelligentPlatform.sales.model.*;
import com.company.IntelligentPlatform.sales.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Replaces: ThorsteinSalesDistribution - SalesContractManager, SalesAreaManager,
 *           SalesForcastManager, SalesReturnOrderManager, SettleOrderManager
 */
@Service
@Transactional
public class SalesService {

    @Autowired private SalesContractRepository salesContractRepository;
    @Autowired private SalesAreaRepository salesAreaRepository;
    @Autowired private SalesForcastRepository salesForcastRepository;
    @Autowired private SalesReturnOrderRepository salesReturnOrderRepository;
    @Autowired private SettleOrderRepository settleOrderRepository;

    // --- SalesContract ---

    public SalesContract createContract(SalesContract contract) {
        contract.setUuid(UUID.randomUUID().toString());
        contract.setStatus(SalesContract.STATUS_INITIAL);
        return salesContractRepository.save(contract);
    }

    @Transactional(readOnly = true)
    public SalesContract getContractByUuid(String uuid) {
        return salesContractRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<SalesContract> getContractsByClient(String client) {
        return salesContractRepository.findByClient(client);
    }

    @Transactional(readOnly = true)
    public List<SalesContract> getContractsByClientAndStatus(String client, int status) {
        return salesContractRepository.findByClientAndStatus(client, status);
    }

    public SalesContract updateContract(SalesContract contract) {
        return salesContractRepository.save(contract);
    }

    public void setContractStatus(String uuid, int status) {
        SalesContract contract = salesContractRepository.findById(uuid).orElseThrow();
        contract.setStatus(status);
        salesContractRepository.save(contract);
    }

    // --- SalesArea ---

    public SalesArea createArea(SalesArea area) {
        area.setUuid(UUID.randomUUID().toString());
        return salesAreaRepository.save(area);
    }

    @Transactional(readOnly = true)
    public SalesArea getAreaByUuid(String uuid) {
        return salesAreaRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<SalesArea> getAreasByClient(String client) {
        return salesAreaRepository.findByClient(client);
    }

    @Transactional(readOnly = true)
    public List<SalesArea> getAreaChildren(String parentAreaUUID) {
        return salesAreaRepository.findByParentAreaUUID(parentAreaUUID);
    }

    public SalesArea updateArea(SalesArea area) {
        return salesAreaRepository.save(area);
    }

    // --- SalesForcast ---

    public SalesForcast createForcast(SalesForcast forcast) {
        forcast.setUuid(UUID.randomUUID().toString());
        forcast.setStatus(SalesForcast.STATUS_INITIAL);
        return salesForcastRepository.save(forcast);
    }

    @Transactional(readOnly = true)
    public SalesForcast getForcastByUuid(String uuid) {
        return salesForcastRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<SalesForcast> getForcastsByClient(String client) {
        return salesForcastRepository.findByClient(client);
    }

    @Transactional(readOnly = true)
    public List<SalesForcast> getForcastsByClientAndStatus(String client, int status) {
        return salesForcastRepository.findByClientAndStatus(client, status);
    }

    public SalesForcast updateForcast(SalesForcast forcast) {
        return salesForcastRepository.save(forcast);
    }

    public void setForcastStatus(String uuid, int status) {
        SalesForcast forcast = salesForcastRepository.findById(uuid).orElseThrow();
        forcast.setStatus(status);
        salesForcastRepository.save(forcast);
    }

    // --- SalesReturnOrder ---

    public SalesReturnOrder createReturnOrder(SalesReturnOrder returnOrder) {
        returnOrder.setUuid(UUID.randomUUID().toString());
        returnOrder.setStatus(SalesReturnOrder.STATUS_INITIAL);
        return salesReturnOrderRepository.save(returnOrder);
    }

    @Transactional(readOnly = true)
    public SalesReturnOrder getReturnOrderByUuid(String uuid) {
        return salesReturnOrderRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<SalesReturnOrder> getReturnOrdersByClient(String client) {
        return salesReturnOrderRepository.findByClient(client);
    }

    @Transactional(readOnly = true)
    public List<SalesReturnOrder> getReturnOrdersByClientAndStatus(String client, int status) {
        return salesReturnOrderRepository.findByClientAndStatus(client, status);
    }

    public SalesReturnOrder updateReturnOrder(SalesReturnOrder returnOrder) {
        return salesReturnOrderRepository.save(returnOrder);
    }

    public void setReturnOrderStatus(String uuid, int status) {
        SalesReturnOrder returnOrder = salesReturnOrderRepository.findById(uuid).orElseThrow();
        returnOrder.setStatus(status);
        salesReturnOrderRepository.save(returnOrder);
    }

    // --- SettleOrder ---

    public SettleOrder createSettleOrder(SettleOrder settleOrder) {
        settleOrder.setUuid(UUID.randomUUID().toString());
        settleOrder.setStatus(SettleOrder.STATUS_INIT);
        return settleOrderRepository.save(settleOrder);
    }

    @Transactional(readOnly = true)
    public SettleOrder getSettleOrderByUuid(String uuid) {
        return settleOrderRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<SettleOrder> getSettleOrdersByClient(String client) {
        return settleOrderRepository.findByClient(client);
    }

    @Transactional(readOnly = true)
    public List<SettleOrder> getSettleOrdersByRefOrder(String refOrderUUID) {
        return settleOrderRepository.findByRefOrderUUID(refOrderUUID);
    }

    public SettleOrder updateSettleOrder(SettleOrder settleOrder) {
        return settleOrderRepository.save(settleOrder);
    }

    public void setSettleOrderStatus(String uuid, int status) {
        SettleOrder settleOrder = settleOrderRepository.findById(uuid).orElseThrow();
        settleOrder.setStatus(status);
        settleOrderRepository.save(settleOrder);
    }
}
