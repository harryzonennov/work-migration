package com.company.IntelligentPlatform.logistics.service;

import com.company.IntelligentPlatform.logistics.model.*;
import com.company.IntelligentPlatform.logistics.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Replaces: ThorsteinLogistics - PurchaseContractManager, PurchaseRequestManager,
 *           PurchaseOrderManager, PurchaseReturnOrderManager, InquiryManager
 */
@Service
@Transactional
public class PurchaseService {

    @Autowired private PurchaseContractRepository purchaseContractRepository;
    @Autowired private PurchaseRequestRepository purchaseRequestRepository;
    @Autowired private PurchaseOrderRepository purchaseOrderRepository;
    @Autowired private PurchaseReturnOrderRepository purchaseReturnOrderRepository;
    @Autowired private InquiryRepository inquiryRepository;

    // --- PurchaseContract ---

    public PurchaseContract createContract(PurchaseContract contract) {
        contract.setUuid(UUID.randomUUID().toString());
        contract.setStatus(PurchaseContract.STATUS_INITIAL);
        return purchaseContractRepository.save(contract);
    }

    @Transactional(readOnly = true)
    public PurchaseContract getContractByUuid(String uuid) {
        return purchaseContractRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<PurchaseContract> getContractsByClient(String client) {
        return purchaseContractRepository.findByClient(client);
    }

    @Transactional(readOnly = true)
    public List<PurchaseContract> getContractsByClientAndStatus(String client, int status) {
        return purchaseContractRepository.findByClientAndStatus(client, status);
    }

    public PurchaseContract updateContract(PurchaseContract contract) {
        return purchaseContractRepository.save(contract);
    }

    public void setContractStatus(String uuid, int status) {
        PurchaseContract contract = purchaseContractRepository.findById(uuid).orElseThrow();
        contract.setStatus(status);
        purchaseContractRepository.save(contract);
    }

    // --- PurchaseRequest ---

    public PurchaseRequest createRequest(PurchaseRequest request) {
        request.setUuid(UUID.randomUUID().toString());
        request.setStatus(PurchaseRequest.STATUS_INITIAL);
        return purchaseRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public PurchaseRequest getRequestByUuid(String uuid) {
        return purchaseRequestRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<PurchaseRequest> getRequestsByClient(String client) {
        return purchaseRequestRepository.findByClient(client);
    }

    @Transactional(readOnly = true)
    public List<PurchaseRequest> getRequestsByClientAndStatus(String client, int status) {
        return purchaseRequestRepository.findByClientAndStatus(client, status);
    }

    public PurchaseRequest updateRequest(PurchaseRequest request) {
        return purchaseRequestRepository.save(request);
    }

    public void setRequestStatus(String uuid, int status) {
        PurchaseRequest request = purchaseRequestRepository.findById(uuid).orElseThrow();
        request.setStatus(status);
        purchaseRequestRepository.save(request);
    }

    // --- PurchaseOrder ---

    public PurchaseOrder createOrder(PurchaseOrder order) {
        order.setUuid(UUID.randomUUID().toString());
        order.setStatus(PurchaseOrder.STATUS_INITIAL);
        return purchaseOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getOrderByUuid(String uuid) {
        return purchaseOrderRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getOrdersByClient(String client) {
        return purchaseOrderRepository.findByClient(client);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getOrdersByClientAndStatus(String client, int status) {
        return purchaseOrderRepository.findByClientAndStatus(client, status);
    }

    public PurchaseOrder updateOrder(PurchaseOrder order) {
        return purchaseOrderRepository.save(order);
    }

    public void setOrderStatus(String uuid, int status) {
        PurchaseOrder order = purchaseOrderRepository.findById(uuid).orElseThrow();
        order.setStatus(status);
        purchaseOrderRepository.save(order);
    }

    // --- PurchaseReturnOrder ---

    public PurchaseReturnOrder createReturnOrder(PurchaseReturnOrder returnOrder) {
        returnOrder.setUuid(UUID.randomUUID().toString());
        returnOrder.setStatus(PurchaseReturnOrder.STATUS_INITIAL);
        return purchaseReturnOrderRepository.save(returnOrder);
    }

    @Transactional(readOnly = true)
    public PurchaseReturnOrder getReturnOrderByUuid(String uuid) {
        return purchaseReturnOrderRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<PurchaseReturnOrder> getReturnOrdersByClient(String client) {
        return purchaseReturnOrderRepository.findByClient(client);
    }

    public PurchaseReturnOrder updateReturnOrder(PurchaseReturnOrder returnOrder) {
        return purchaseReturnOrderRepository.save(returnOrder);
    }

    public void setReturnOrderStatus(String uuid, int status) {
        PurchaseReturnOrder returnOrder = purchaseReturnOrderRepository.findById(uuid).orElseThrow();
        returnOrder.setStatus(status);
        purchaseReturnOrderRepository.save(returnOrder);
    }

    // --- Inquiry ---

    public Inquiry createInquiry(Inquiry inquiry) {
        inquiry.setUuid(UUID.randomUUID().toString());
        inquiry.setStatus(Inquiry.STATUS_INIT);
        return inquiryRepository.save(inquiry);
    }

    @Transactional(readOnly = true)
    public Inquiry getInquiryByUuid(String uuid) {
        return inquiryRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Inquiry> getInquiriesByClient(String client) {
        return inquiryRepository.findByClient(client);
    }

    @Transactional(readOnly = true)
    public List<Inquiry> getInquiriesByClientAndStatus(String client, int status) {
        return inquiryRepository.findByClientAndStatus(client, status);
    }

    public Inquiry updateInquiry(Inquiry inquiry) {
        return inquiryRepository.save(inquiry);
    }

    public void setInquiryStatus(String uuid, int status) {
        Inquiry inquiry = inquiryRepository.findById(uuid).orElseThrow();
        inquiry.setStatus(status);
        inquiryRepository.save(inquiry);
    }
}
