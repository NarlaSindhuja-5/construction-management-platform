package com.e2e.construction.controller;

import com.e2e.construction.dto.MaterialOrderActionRequest;
import com.e2e.construction.dto.MaterialOrderRequest;
import com.e2e.construction.dto.MaterialOrderResponse;
import com.e2e.construction.dto.MaterialRequest;
import com.e2e.construction.dto.MaterialResponse;
import com.e2e.construction.dto.MaterialSupplierProfileRequest;
import com.e2e.construction.dto.MaterialSupplierProfileResponse;
import com.e2e.construction.dto.PriceUpdateRequest;
import com.e2e.construction.dto.StockHistoryResponse;
import com.e2e.construction.dto.StockUpdateRequest;
import com.e2e.construction.entity.MaterialAvailability;
import com.e2e.construction.entity.MaterialCategory;
import com.e2e.construction.entity.MaterialOrderStatus;
import com.e2e.construction.service.MaterialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    // =========================================================================
    // 1. MATERIAL SUPPLIER PROFILE
    // =========================================================================

    @PostMapping({ "/api/material-suppliers/profile", "/api/materials/supplier/profile" })
    @PreAuthorize("hasAnyRole('MATERIAL_SUPPLIER', 'ADMIN')")
    public ResponseEntity<MaterialSupplierProfileResponse> saveSupplierProfile(
            Principal principal,
            @Valid @RequestBody MaterialSupplierProfileRequest request) {
        MaterialSupplierProfileResponse response = materialService.saveSupplierProfile(principal.getName(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping({ "/api/material-suppliers/profile", "/api/materials/supplier/profile" })
    @PreAuthorize("hasAnyRole('MATERIAL_SUPPLIER', 'ADMIN')")
    public ResponseEntity<MaterialSupplierProfileResponse> getSupplierProfile(Principal principal) {
        MaterialSupplierProfileResponse response = materialService.getSupplierProfile(principal.getName());
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // 2. MATERIAL LISTINGS & CATALOG (SUPPLIER)
    // =========================================================================

    @PostMapping("/api/materials")
    @PreAuthorize("hasAnyRole('MATERIAL_SUPPLIER', 'ADMIN')")
    public ResponseEntity<MaterialResponse> createMaterial(
            Principal principal,
            @Valid @RequestBody MaterialRequest request) {
        MaterialResponse response = materialService.createMaterial(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/materials/{id}")
    @PreAuthorize("hasAnyRole('MATERIAL_SUPPLIER', 'ADMIN')")
    public ResponseEntity<MaterialResponse> updateMaterial(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody MaterialRequest request) {
        MaterialResponse response = materialService.updateMaterial(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping({ "/api/materials/{id}/stock", "/api/materials/{id}/update-stock" })
    @PreAuthorize("hasAnyRole('MATERIAL_SUPPLIER', 'ADMIN')")
    public ResponseEntity<MaterialResponse> updateStock(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody StockUpdateRequest request) {
        MaterialResponse response = materialService.updateStock(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/materials/{id}/stock")
    @PreAuthorize("hasAnyRole('MATERIAL_SUPPLIER', 'ADMIN')")
    public ResponseEntity<MaterialResponse> putStock(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody StockUpdateRequest request) {
        MaterialResponse response = materialService.updateStock(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping({ "/api/materials/{id}/price", "/api/materials/{id}/update-price" })
    @PreAuthorize("hasAnyRole('MATERIAL_SUPPLIER', 'ADMIN')")
    public ResponseEntity<MaterialResponse> updatePrice(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody PriceUpdateRequest request) {
        MaterialResponse response = materialService.updatePrice(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/materials/{id}/price")
    @PreAuthorize("hasAnyRole('MATERIAL_SUPPLIER', 'ADMIN')")
    public ResponseEntity<MaterialResponse> putPrice(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody PriceUpdateRequest request) {
        MaterialResponse response = materialService.updatePrice(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/materials/my-materials")
    @PreAuthorize("hasAnyRole('MATERIAL_SUPPLIER', 'ADMIN')")
    public ResponseEntity<List<MaterialResponse>> getMyMaterials(Principal principal) {
        List<MaterialResponse> responses = materialService.getSupplierMaterials(principal.getName());
        return ResponseEntity.ok(responses);
    }

    // =========================================================================
    // 3. SEARCH & VIEW (CONTRACTOR / PUBLIC AUTHENTICATED)
    // =========================================================================

    @GetMapping({ "/api/materials", "/api/materials/search" })
    public ResponseEntity<List<MaterialResponse>> searchMaterials(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) MaterialCategory category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) MaterialAvailability availability) {

        List<MaterialResponse> responses = materialService.searchMaterials(search, category, location, maxPrice, availability);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/api/materials/{id}")
    public ResponseEntity<MaterialResponse> getMaterialById(@PathVariable Long id) {
        MaterialResponse response = materialService.getMaterialById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/materials/{id}/stock-history")
    public ResponseEntity<List<StockHistoryResponse>> getStockHistory(@PathVariable Long id) {
        List<StockHistoryResponse> history = materialService.getStockHistory(id);
        return ResponseEntity.ok(history);
    }

    // =========================================================================
    // 4. ORDERS (CONTRACTOR & SUPPLIER WORKFLOW)
    // =========================================================================

    @PostMapping({ "/api/materials/{materialId}/orders", "/api/material-orders" })
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<MaterialOrderResponse> createOrder(
            @PathVariable(required = false) Long materialId,
            Principal principal,
            @Valid @RequestBody MaterialOrderRequest request) {

        MaterialOrderResponse response = materialService.createOrder(materialId, request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/material-orders/my-orders")
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<List<MaterialOrderResponse>> getContractorOrders(
            Principal principal,
            @RequestParam(required = false) MaterialOrderStatus status) {

        List<MaterialOrderResponse> responses = materialService.getContractorOrderHistory(principal.getName(), status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping({ "/api/material-orders/supplier-orders", "/api/material-orders/incoming" })
    @PreAuthorize("hasAnyRole('MATERIAL_SUPPLIER', 'ADMIN')")
    public ResponseEntity<List<MaterialOrderResponse>> getSupplierOrders(
            Principal principal,
            @RequestParam(required = false) MaterialOrderStatus status) {

        List<MaterialOrderResponse> responses = materialService.getSupplierOrderHistory(principal.getName(), status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/api/material-orders/{id}")
    public ResponseEntity<MaterialOrderResponse> getOrderById(
            @PathVariable Long id,
            Principal principal) {

        MaterialOrderResponse response = materialService.getOrderById(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/material-orders/{id}/accept")
    @PreAuthorize("hasAnyRole('MATERIAL_SUPPLIER', 'ADMIN')")
    public ResponseEntity<MaterialOrderResponse> acceptOrder(
            @PathVariable Long id,
            Principal principal,
            @RequestBody(required = false) MaterialOrderActionRequest actionRequest) {

        String notes = actionRequest != null ? actionRequest.getNotes() : null;
        MaterialOrderResponse response = materialService.acceptOrder(id, principal.getName(), notes);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/material-orders/{id}/reject")
    @PreAuthorize("hasAnyRole('MATERIAL_SUPPLIER', 'ADMIN')")
    public ResponseEntity<MaterialOrderResponse> rejectOrder(
            @PathVariable Long id,
            Principal principal,
            @RequestBody(required = false) MaterialOrderActionRequest actionRequest) {

        String reason = actionRequest != null
                ? (actionRequest.getReason() != null ? actionRequest.getReason() : actionRequest.getNotes())
                : null;
        MaterialOrderResponse response = materialService.rejectOrder(id, principal.getName(), reason);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/material-orders/{id}/cancel")
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<MaterialOrderResponse> cancelOrder(
            @PathVariable Long id,
            Principal principal,
            @RequestBody(required = false) MaterialOrderActionRequest actionRequest) {

        String reason = actionRequest != null
                ? (actionRequest.getReason() != null ? actionRequest.getReason() : actionRequest.getNotes())
                : null;
        MaterialOrderResponse response = materialService.cancelOrder(id, principal.getName(), reason);
        return ResponseEntity.ok(response);
    }
}
