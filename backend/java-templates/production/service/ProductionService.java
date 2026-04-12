package com.company.IntelligentPlatform.production.service;

import com.company.IntelligentPlatform.production.model.*;
import com.company.IntelligentPlatform.production.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProductionService {

    private final ProductionOrderRepository productionOrderRepository;
    private final RepairProdOrderRepository repairProdOrderRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final BillOfMaterialOrderRepository billOfMaterialOrderRepository;
    private final BillOfMaterialTemplateRepository billOfMaterialTemplateRepository;
    private final ProdPickingOrderRepository prodPickingOrderRepository;
    private final ProdJobOrderRepository prodJobOrderRepository;
    private final ProcessRouteOrderRepository processRouteOrderRepository;
    private final ProdWorkCenterRepository prodWorkCenterRepository;

    public ProductionService(
            ProductionOrderRepository productionOrderRepository,
            RepairProdOrderRepository repairProdOrderRepository,
            ProductionPlanRepository productionPlanRepository,
            BillOfMaterialOrderRepository billOfMaterialOrderRepository,
            BillOfMaterialTemplateRepository billOfMaterialTemplateRepository,
            ProdPickingOrderRepository prodPickingOrderRepository,
            ProdJobOrderRepository prodJobOrderRepository,
            ProcessRouteOrderRepository processRouteOrderRepository,
            ProdWorkCenterRepository prodWorkCenterRepository) {
        this.productionOrderRepository = productionOrderRepository;
        this.repairProdOrderRepository = repairProdOrderRepository;
        this.productionPlanRepository = productionPlanRepository;
        this.billOfMaterialOrderRepository = billOfMaterialOrderRepository;
        this.billOfMaterialTemplateRepository = billOfMaterialTemplateRepository;
        this.prodPickingOrderRepository = prodPickingOrderRepository;
        this.prodJobOrderRepository = prodJobOrderRepository;
        this.processRouteOrderRepository = processRouteOrderRepository;
        this.prodWorkCenterRepository = prodWorkCenterRepository;
    }

    // ── ProductionOrder ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProductionOrder> getAllProductionOrders() {
        return productionOrderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ProductionOrder> getProductionOrderByUuid(String uuid) {
        return productionOrderRepository.findById(uuid);
    }

    @Transactional(readOnly = true)
    public List<ProductionOrder> getProductionOrdersByStatus(int status) {
        return productionOrderRepository.findByStatus(status);
    }

    public ProductionOrder createProductionOrder(ProductionOrder order) {
        order.setUuid(UUID.randomUUID().toString());
        order.setStatus(ProductionOrder.STATUS_INITIAL);
        return productionOrderRepository.save(order);
    }

    public Optional<ProductionOrder> updateProductionOrder(String uuid, ProductionOrder updated) {
        return productionOrderRepository.findById(uuid).map(e -> {
            updated.setUuid(uuid);
            return productionOrderRepository.save(updated);
        });
    }

    // ── RepairProdOrder ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RepairProdOrder> getAllRepairOrders() {
        return repairProdOrderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<RepairProdOrder> getRepairOrderByUuid(String uuid) {
        return repairProdOrderRepository.findById(uuid);
    }

    public RepairProdOrder createRepairOrder(RepairProdOrder order) {
        order.setUuid(UUID.randomUUID().toString());
        order.setStatus(ProductionOrder.STATUS_INITIAL);
        return repairProdOrderRepository.save(order);
    }

    public Optional<RepairProdOrder> updateRepairOrder(String uuid, RepairProdOrder updated) {
        return repairProdOrderRepository.findById(uuid).map(e -> {
            updated.setUuid(uuid);
            return repairProdOrderRepository.save(updated);
        });
    }

    // ── ProductionPlan ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProductionPlan> getAllProductionPlans() {
        return productionPlanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ProductionPlan> getProductionPlanByUuid(String uuid) {
        return productionPlanRepository.findById(uuid);
    }

    @Transactional(readOnly = true)
    public List<ProductionPlan> getPlansByMainProdOrder(String refMainProdOrderUUID) {
        return productionPlanRepository.findByRefMainProdOrderUUID(refMainProdOrderUUID);
    }

    public ProductionPlan createProductionPlan(ProductionPlan plan) {
        plan.setUuid(UUID.randomUUID().toString());
        plan.setStatus(ProductionPlan.STATUS_INITIAL);
        return productionPlanRepository.save(plan);
    }

    public Optional<ProductionPlan> updateProductionPlan(String uuid, ProductionPlan updated) {
        return productionPlanRepository.findById(uuid).map(e -> {
            updated.setUuid(uuid);
            return productionPlanRepository.save(updated);
        });
    }

    // ── BillOfMaterialOrder ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BillOfMaterialOrder> getAllBOMs() {
        return billOfMaterialOrderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<BillOfMaterialOrder> getBOMByUuid(String uuid) {
        return billOfMaterialOrderRepository.findById(uuid);
    }

    @Transactional(readOnly = true)
    public List<BillOfMaterialOrder> getBOMsByMaterialSKU(String refMaterialSKUUUID) {
        return billOfMaterialOrderRepository.findByRefMaterialSKUUUID(refMaterialSKUUUID);
    }

    public BillOfMaterialOrder createBOM(BillOfMaterialOrder bom) {
        bom.setUuid(UUID.randomUUID().toString());
        bom.setStatus(BillOfMaterialOrder.STATUS_INITIAL);
        return billOfMaterialOrderRepository.save(bom);
    }

    public Optional<BillOfMaterialOrder> updateBOM(String uuid, BillOfMaterialOrder updated) {
        return billOfMaterialOrderRepository.findById(uuid).map(e -> {
            updated.setUuid(uuid);
            return billOfMaterialOrderRepository.save(updated);
        });
    }

    // ── BillOfMaterialTemplate ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BillOfMaterialTemplate> getAllBOMTemplates() {
        return billOfMaterialTemplateRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<BillOfMaterialTemplate> getBOMTemplateByUuid(String uuid) {
        return billOfMaterialTemplateRepository.findById(uuid);
    }

    public BillOfMaterialTemplate createBOMTemplate(BillOfMaterialTemplate template) {
        template.setUuid(UUID.randomUUID().toString());
        template.setStatus(BillOfMaterialOrder.STATUS_INITIAL);
        return billOfMaterialTemplateRepository.save(template);
    }

    public Optional<BillOfMaterialTemplate> updateBOMTemplate(String uuid, BillOfMaterialTemplate updated) {
        return billOfMaterialTemplateRepository.findById(uuid).map(e -> {
            updated.setUuid(uuid);
            return billOfMaterialTemplateRepository.save(updated);
        });
    }

    // ── ProdPickingOrder ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProdPickingOrder> getAllPickingOrders() {
        return prodPickingOrderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ProdPickingOrder> getPickingOrderByUuid(String uuid) {
        return prodPickingOrderRepository.findById(uuid);
    }

    public ProdPickingOrder createPickingOrder(ProdPickingOrder order) {
        order.setUuid(UUID.randomUUID().toString());
        order.setStatus(ProdPickingOrder.STATUS_INITIAL);
        return prodPickingOrderRepository.save(order);
    }

    public Optional<ProdPickingOrder> updatePickingOrder(String uuid, ProdPickingOrder updated) {
        return prodPickingOrderRepository.findById(uuid).map(e -> {
            updated.setUuid(uuid);
            return prodPickingOrderRepository.save(updated);
        });
    }

    // ── ProdJobOrder ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProdJobOrder> getJobOrdersByProductionOrder(String refProductionOrderUUID) {
        return prodJobOrderRepository.findByRefProductionOrderUUID(refProductionOrderUUID);
    }

    @Transactional(readOnly = true)
    public Optional<ProdJobOrder> getJobOrderByUuid(String uuid) {
        return prodJobOrderRepository.findById(uuid);
    }

    public ProdJobOrder createJobOrder(ProdJobOrder jobOrder) {
        jobOrder.setUuid(UUID.randomUUID().toString());
        jobOrder.setStatus(ProdJobOrder.STATUS_INITIAL);
        return prodJobOrderRepository.save(jobOrder);
    }

    public Optional<ProdJobOrder> updateJobOrder(String uuid, ProdJobOrder updated) {
        return prodJobOrderRepository.findById(uuid).map(e -> {
            updated.setUuid(uuid);
            return prodJobOrderRepository.save(updated);
        });
    }

    // ── ProcessRouteOrder ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProcessRouteOrder> getAllProcessRoutes() {
        return processRouteOrderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ProcessRouteOrder> getProcessRouteByUuid(String uuid) {
        return processRouteOrderRepository.findById(uuid);
    }

    public ProcessRouteOrder createProcessRoute(ProcessRouteOrder route) {
        route.setUuid(UUID.randomUUID().toString());
        route.setStatus(ProcessRouteOrder.STATUS_INITIAL);
        return processRouteOrderRepository.save(route);
    }

    public Optional<ProcessRouteOrder> updateProcessRoute(String uuid, ProcessRouteOrder updated) {
        return processRouteOrderRepository.findById(uuid).map(e -> {
            updated.setUuid(uuid);
            return processRouteOrderRepository.save(updated);
        });
    }

    // ── ProdWorkCenter ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProdWorkCenter> getAllWorkCenters() {
        return prodWorkCenterRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ProdWorkCenter> getWorkCenterByUuid(String uuid) {
        return prodWorkCenterRepository.findById(uuid);
    }

    @Transactional(readOnly = true)
    public List<ProdWorkCenter> getWorkCenterChildren(String parentNodeUUID) {
        return prodWorkCenterRepository.findByParentNodeUUID(parentNodeUUID);
    }

    public ProdWorkCenter createWorkCenter(ProdWorkCenter workCenter) {
        workCenter.setUuid(UUID.randomUUID().toString());
        return prodWorkCenterRepository.save(workCenter);
    }

    public Optional<ProdWorkCenter> updateWorkCenter(String uuid, ProdWorkCenter updated) {
        return prodWorkCenterRepository.findById(uuid).map(e -> {
            updated.setUuid(uuid);
            return prodWorkCenterRepository.save(updated);
        });
    }
}
