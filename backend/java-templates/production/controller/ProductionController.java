package com.company.IntelligentPlatform.production.controller;

import com.company.IntelligentPlatform.common.response.ApiResponse;
import com.company.IntelligentPlatform.production.dto.*;
import com.company.IntelligentPlatform.production.model.*;
import com.company.IntelligentPlatform.production.service.ProductionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/production")
public class ProductionController {

    private final ProductionService productionService;

    public ProductionController(ProductionService productionService) {
        this.productionService = productionService;
    }

    // ── ProductionOrder ────────────────────────────────────────────────────────

    @GetMapping("/productionOrders")
    public ResponseEntity<ApiResponse<List<ProductionOrder>>> getAllProductionOrders() {
        return ResponseEntity.ok(ApiResponse.success(productionService.getAllProductionOrders()));
    }

    @GetMapping("/productionOrders/{uuid}")
    public ResponseEntity<ApiResponse<ProductionOrder>> getProductionOrder(@PathVariable String uuid) {
        return productionService.getProductionOrderByUuid(uuid)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/productionOrders")
    public ResponseEntity<ApiResponse<ProductionOrder>> createProductionOrder(@RequestBody ProductionOrderDto dto) {
        ProductionOrder saved = productionService.createProductionOrder(dto.toEntity());
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/productionOrders/{uuid}")
    public ResponseEntity<ApiResponse<ProductionOrder>> updateProductionOrder(@PathVariable String uuid, @RequestBody ProductionOrderDto dto) {
        ProductionOrder updated = dto.toEntity();
        return productionService.updateProductionOrder(uuid, updated)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── RepairProdOrder ────────────────────────────────────────────────────────

    @GetMapping("/repairOrders")
    public ResponseEntity<ApiResponse<List<RepairProdOrder>>> getAllRepairOrders() {
        return ResponseEntity.ok(ApiResponse.success(productionService.getAllRepairOrders()));
    }

    @GetMapping("/repairOrders/{uuid}")
    public ResponseEntity<ApiResponse<RepairProdOrder>> getRepairOrder(@PathVariable String uuid) {
        return productionService.getRepairOrderByUuid(uuid)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/repairOrders")
    public ResponseEntity<ApiResponse<RepairProdOrder>> createRepairOrder(@RequestBody ProductionOrderDto dto) {
        RepairProdOrder order = new RepairProdOrder();
        dto.applyTo(order);
        RepairProdOrder saved = productionService.createRepairOrder(order);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/repairOrders/{uuid}")
    public ResponseEntity<ApiResponse<RepairProdOrder>> updateRepairOrder(@PathVariable String uuid, @RequestBody ProductionOrderDto dto) {
        RepairProdOrder order = new RepairProdOrder();
        dto.applyTo(order);
        return productionService.updateRepairOrder(uuid, order)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── ProductionPlan ─────────────────────────────────────────────────────────

    @GetMapping("/productionPlans")
    public ResponseEntity<ApiResponse<List<ProductionPlan>>> getAllProductionPlans() {
        return ResponseEntity.ok(ApiResponse.success(productionService.getAllProductionPlans()));
    }

    @GetMapping("/productionPlans/{uuid}")
    public ResponseEntity<ApiResponse<ProductionPlan>> getProductionPlan(@PathVariable String uuid) {
        return productionService.getProductionPlanByUuid(uuid)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/productionPlans/byOrder/{refMainProdOrderUUID}")
    public ResponseEntity<ApiResponse<List<ProductionPlan>>> getPlansByOrder(@PathVariable String refMainProdOrderUUID) {
        return ResponseEntity.ok(ApiResponse.success(productionService.getPlansByMainProdOrder(refMainProdOrderUUID)));
    }

    @PostMapping("/productionPlans")
    public ResponseEntity<ApiResponse<ProductionPlan>> createProductionPlan(@RequestBody ProductionPlanDto dto) {
        ProductionPlan saved = productionService.createProductionPlan(dto.toEntity());
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/productionPlans/{uuid}")
    public ResponseEntity<ApiResponse<ProductionPlan>> updateProductionPlan(@PathVariable String uuid, @RequestBody ProductionPlanDto dto) {
        return productionService.updateProductionPlan(uuid, dto.toEntity())
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── BillOfMaterialOrder ────────────────────────────────────────────────────

    @GetMapping("/billOfMaterials")
    public ResponseEntity<ApiResponse<List<BillOfMaterialOrder>>> getAllBOMs() {
        return ResponseEntity.ok(ApiResponse.success(productionService.getAllBOMs()));
    }

    @GetMapping("/billOfMaterials/{uuid}")
    public ResponseEntity<ApiResponse<BillOfMaterialOrder>> getBOM(@PathVariable String uuid) {
        return productionService.getBOMByUuid(uuid)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/billOfMaterials")
    public ResponseEntity<ApiResponse<BillOfMaterialOrder>> createBOM(@RequestBody BillOfMaterialOrderDto dto) {
        BillOfMaterialOrder saved = productionService.createBOM(dto.toEntity());
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/billOfMaterials/{uuid}")
    public ResponseEntity<ApiResponse<BillOfMaterialOrder>> updateBOM(@PathVariable String uuid, @RequestBody BillOfMaterialOrderDto dto) {
        return productionService.updateBOM(uuid, dto.toEntity())
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── BillOfMaterialTemplate ─────────────────────────────────────────────────

    @GetMapping("/bomTemplates")
    public ResponseEntity<ApiResponse<List<BillOfMaterialTemplate>>> getAllBOMTemplates() {
        return ResponseEntity.ok(ApiResponse.success(productionService.getAllBOMTemplates()));
    }

    @GetMapping("/bomTemplates/{uuid}")
    public ResponseEntity<ApiResponse<BillOfMaterialTemplate>> getBOMTemplate(@PathVariable String uuid) {
        return productionService.getBOMTemplateByUuid(uuid)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/bomTemplates")
    public ResponseEntity<ApiResponse<BillOfMaterialTemplate>> createBOMTemplate(@RequestBody BillOfMaterialOrderDto dto) {
        BillOfMaterialTemplate template = new BillOfMaterialTemplate();
        dto.applyTo(template);
        BillOfMaterialTemplate saved = productionService.createBOMTemplate(template);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/bomTemplates/{uuid}")
    public ResponseEntity<ApiResponse<BillOfMaterialTemplate>> updateBOMTemplate(@PathVariable String uuid, @RequestBody BillOfMaterialOrderDto dto) {
        BillOfMaterialTemplate template = new BillOfMaterialTemplate();
        dto.applyTo(template);
        return productionService.updateBOMTemplate(uuid, template)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── ProdPickingOrder ───────────────────────────────────────────────────────

    @GetMapping("/pickingOrders")
    public ResponseEntity<ApiResponse<List<ProdPickingOrder>>> getAllPickingOrders() {
        return ResponseEntity.ok(ApiResponse.success(productionService.getAllPickingOrders()));
    }

    @GetMapping("/pickingOrders/{uuid}")
    public ResponseEntity<ApiResponse<ProdPickingOrder>> getPickingOrder(@PathVariable String uuid) {
        return productionService.getPickingOrderByUuid(uuid)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/pickingOrders")
    public ResponseEntity<ApiResponse<ProdPickingOrder>> createPickingOrder(@RequestBody ProdPickingOrderDto dto) {
        ProdPickingOrder saved = productionService.createPickingOrder(dto.toEntity());
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/pickingOrders/{uuid}")
    public ResponseEntity<ApiResponse<ProdPickingOrder>> updatePickingOrder(@PathVariable String uuid, @RequestBody ProdPickingOrderDto dto) {
        return productionService.updatePickingOrder(uuid, dto.toEntity())
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── ProdJobOrder ───────────────────────────────────────────────────────────

    @GetMapping("/jobOrders/byOrder/{refProductionOrderUUID}")
    public ResponseEntity<ApiResponse<List<ProdJobOrder>>> getJobOrdersByOrder(@PathVariable String refProductionOrderUUID) {
        return ResponseEntity.ok(ApiResponse.success(productionService.getJobOrdersByProductionOrder(refProductionOrderUUID)));
    }

    @GetMapping("/jobOrders/{uuid}")
    public ResponseEntity<ApiResponse<ProdJobOrder>> getJobOrder(@PathVariable String uuid) {
        return productionService.getJobOrderByUuid(uuid)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/jobOrders")
    public ResponseEntity<ApiResponse<ProdJobOrder>> createJobOrder(@RequestBody ProdJobOrder jobOrder) {
        ProdJobOrder saved = productionService.createJobOrder(jobOrder);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/jobOrders/{uuid}")
    public ResponseEntity<ApiResponse<ProdJobOrder>> updateJobOrder(@PathVariable String uuid, @RequestBody ProdJobOrder jobOrder) {
        return productionService.updateJobOrder(uuid, jobOrder)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── ProcessRouteOrder ──────────────────────────────────────────────────────

    @GetMapping("/processRoutes")
    public ResponseEntity<ApiResponse<List<ProcessRouteOrder>>> getAllProcessRoutes() {
        return ResponseEntity.ok(ApiResponse.success(productionService.getAllProcessRoutes()));
    }

    @GetMapping("/processRoutes/{uuid}")
    public ResponseEntity<ApiResponse<ProcessRouteOrder>> getProcessRoute(@PathVariable String uuid) {
        return productionService.getProcessRouteByUuid(uuid)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/processRoutes")
    public ResponseEntity<ApiResponse<ProcessRouteOrder>> createProcessRoute(@RequestBody ProcessRouteOrder route) {
        ProcessRouteOrder saved = productionService.createProcessRoute(route);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/processRoutes/{uuid}")
    public ResponseEntity<ApiResponse<ProcessRouteOrder>> updateProcessRoute(@PathVariable String uuid, @RequestBody ProcessRouteOrder route) {
        return productionService.updateProcessRoute(uuid, route)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── ProdWorkCenter ─────────────────────────────────────────────────────────

    @GetMapping("/workCenters")
    public ResponseEntity<ApiResponse<List<ProdWorkCenter>>> getAllWorkCenters() {
        return ResponseEntity.ok(ApiResponse.success(productionService.getAllWorkCenters()));
    }

    @GetMapping("/workCenters/{uuid}")
    public ResponseEntity<ApiResponse<ProdWorkCenter>> getWorkCenter(@PathVariable String uuid) {
        return productionService.getWorkCenterByUuid(uuid)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/workCenters/{uuid}/children")
    public ResponseEntity<ApiResponse<List<ProdWorkCenter>>> getWorkCenterChildren(@PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.success(productionService.getWorkCenterChildren(uuid)));
    }

    @PostMapping("/workCenters")
    public ResponseEntity<ApiResponse<ProdWorkCenter>> createWorkCenter(@RequestBody ProdWorkCenter workCenter) {
        ProdWorkCenter saved = productionService.createWorkCenter(workCenter);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/workCenters/{uuid}")
    public ResponseEntity<ApiResponse<ProdWorkCenter>> updateWorkCenter(@PathVariable String uuid, @RequestBody ProdWorkCenter workCenter) {
        return productionService.updateWorkCenter(uuid, workCenter)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElse(ResponseEntity.notFound().build());
    }
}
