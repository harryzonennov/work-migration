package com.company.IntelligentPlatform.logistics.service;

import com.company.IntelligentPlatform.logistics.model.*;
import com.company.IntelligentPlatform.logistics.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Replaces: ThorsteinLogistics - InboundDeliveryManager, OutboundDeliveryManager,
 *           InventoryCheckOrderManager, InventoryTransferOrderManager
 */
@Service
@Transactional
public class DeliveryService {

    @Autowired private InboundDeliveryRepository inboundDeliveryRepository;
    @Autowired private OutboundDeliveryRepository outboundDeliveryRepository;
    @Autowired private InventoryCheckOrderRepository inventoryCheckOrderRepository;
    @Autowired private InventoryTransferOrderRepository inventoryTransferOrderRepository;

    // --- InboundDelivery ---

    public InboundDelivery createInbound(InboundDelivery delivery) {
        delivery.setUuid(UUID.randomUUID().toString());
        delivery.setStatus(InboundDelivery.STATUS_INITIAL);
        return inboundDeliveryRepository.save(delivery);
    }

    @Transactional(readOnly = true)
    public InboundDelivery getInboundByUuid(String uuid) {
        return inboundDeliveryRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<InboundDelivery> getInboundByClient(String client) {
        return inboundDeliveryRepository.findByClient(client);
    }

    @Transactional(readOnly = true)
    public List<InboundDelivery> getInboundByClientAndStatus(String client, int status) {
        return inboundDeliveryRepository.findByClientAndStatus(client, status);
    }

    public InboundDelivery updateInbound(InboundDelivery delivery) {
        return inboundDeliveryRepository.save(delivery);
    }

    public void setInboundStatus(String uuid, int status) {
        InboundDelivery delivery = inboundDeliveryRepository.findById(uuid).orElseThrow();
        delivery.setStatus(status);
        inboundDeliveryRepository.save(delivery);
    }

    // --- OutboundDelivery ---

    public OutboundDelivery createOutbound(OutboundDelivery delivery) {
        delivery.setUuid(UUID.randomUUID().toString());
        delivery.setStatus(OutboundDelivery.STATUS_INITIAL);
        return outboundDeliveryRepository.save(delivery);
    }

    @Transactional(readOnly = true)
    public OutboundDelivery getOutboundByUuid(String uuid) {
        return outboundDeliveryRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<OutboundDelivery> getOutboundByClient(String client) {
        return outboundDeliveryRepository.findByClient(client);
    }

    @Transactional(readOnly = true)
    public List<OutboundDelivery> getOutboundByClientAndStatus(String client, int status) {
        return outboundDeliveryRepository.findByClientAndStatus(client, status);
    }

    public OutboundDelivery updateOutbound(OutboundDelivery delivery) {
        return outboundDeliveryRepository.save(delivery);
    }

    public void setOutboundStatus(String uuid, int status) {
        OutboundDelivery delivery = outboundDeliveryRepository.findById(uuid).orElseThrow();
        delivery.setStatus(status);
        outboundDeliveryRepository.save(delivery);
    }

    // --- InventoryCheckOrder ---

    public InventoryCheckOrder createInventoryCheck(InventoryCheckOrder order) {
        order.setUuid(UUID.randomUUID().toString());
        order.setStatus(InventoryCheckOrder.STATUS_INITIAL);
        return inventoryCheckOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public InventoryCheckOrder getInventoryCheckByUuid(String uuid) {
        return inventoryCheckOrderRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<InventoryCheckOrder> getInventoryCheckByClient(String client) {
        return inventoryCheckOrderRepository.findByClient(client);
    }

    public InventoryCheckOrder updateInventoryCheck(InventoryCheckOrder order) {
        return inventoryCheckOrderRepository.save(order);
    }

    public void setInventoryCheckStatus(String uuid, int status) {
        InventoryCheckOrder order = inventoryCheckOrderRepository.findById(uuid).orElseThrow();
        order.setStatus(status);
        inventoryCheckOrderRepository.save(order);
    }

    // --- InventoryTransferOrder ---

    public InventoryTransferOrder createInventoryTransfer(InventoryTransferOrder order) {
        order.setUuid(UUID.randomUUID().toString());
        order.setStatus(InventoryTransferOrder.STATUS_INITIAL);
        return inventoryTransferOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public InventoryTransferOrder getInventoryTransferByUuid(String uuid) {
        return inventoryTransferOrderRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<InventoryTransferOrder> getInventoryTransferByClient(String client) {
        return inventoryTransferOrderRepository.findByClient(client);
    }

    public InventoryTransferOrder updateInventoryTransfer(InventoryTransferOrder order) {
        return inventoryTransferOrderRepository.save(order);
    }

    public void setInventoryTransferStatus(String uuid, int status) {
        InventoryTransferOrder order = inventoryTransferOrderRepository.findById(uuid).orElseThrow();
        order.setStatus(status);
        inventoryTransferOrderRepository.save(order);
    }
}
