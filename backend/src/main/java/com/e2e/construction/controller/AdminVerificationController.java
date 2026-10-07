package com.e2e.construction.controller;

import com.e2e.construction.dto.VerificationDecisionRequest;
import com.e2e.construction.dto.VerificationItemResponse;
import com.e2e.construction.dto.VerificationRecordResponse;
import com.e2e.construction.dto.VerificationSummaryStatsResponse;
import com.e2e.construction.entity.VerificationEntityType;
import com.e2e.construction.entity.VerificationStatus;
import com.e2e.construction.service.AdminVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/verifications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVerificationController {

    private final AdminVerificationService adminVerificationService;

    public AdminVerificationController(AdminVerificationService adminVerificationService) {
        this.adminVerificationService = adminVerificationService;
    }

    /**
     * GET /api/admin/verifications
     * Retrieve list of accounts or listings across the 6 entity types for admin review.
     * Supports filtering by entityType, verificationStatus (PENDING, VERIFIED, REJECTED), and keyword search.
     */
    @GetMapping
    public ResponseEntity<List<VerificationItemResponse>> getItemsForVerification(
            @RequestParam(required = false) VerificationEntityType entityType,
            @RequestParam(required = false) VerificationStatus status,
            @RequestParam(required = false) String search) {

        List<VerificationItemResponse> items = adminVerificationService.getItemsForVerification(entityType, status, search);
        return ResponseEntity.ok(items);
    }

    /**
     * GET /api/admin/verifications/{entityType}/{entityId}
     * Retrieve full details of an item (including uploaded images and audit history) for admin inspection before verification.
     */
    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<VerificationItemResponse> getItemForVerification(
            @PathVariable VerificationEntityType entityType,
            @PathVariable Long entityId) {

        VerificationItemResponse item = adminVerificationService.getItemForVerification(entityType, entityId);
        return ResponseEntity.ok(item);
    }

    /**
     * POST /api/admin/verifications/{entityType}/{entityId}
     * Submit verification decision: VERIFY, REJECT, or KEEP PENDING.
     * Records reviewer, verification date, status, and reason for rejection.
     * Does NOT delete rejected records.
     */
    @PostMapping("/{entityType}/{entityId}")
    public ResponseEntity<VerificationItemResponse> submitVerificationDecision(
            @PathVariable VerificationEntityType entityType,
            @PathVariable Long entityId,
            Principal principal,
            @Valid @RequestBody VerificationDecisionRequest request) {

        VerificationItemResponse response = adminVerificationService.submitVerificationDecision(
                entityType, entityId, principal.getName(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/admin/verifications/{entityType}/{entityId}
     * Also supports PUT method for submitting verification decision.
     */
    @PutMapping("/{entityType}/{entityId}")
    public ResponseEntity<VerificationItemResponse> submitVerificationDecisionPut(
            @PathVariable VerificationEntityType entityType,
            @PathVariable Long entityId,
            Principal principal,
            @Valid @RequestBody VerificationDecisionRequest request) {

        return submitVerificationDecision(entityType, entityId, principal, request);
    }

    /**
     * GET /api/admin/verifications/{entityType}/{entityId}/history
     * Retrieve the complete verification audit trail and decision history for an entity.
     */
    @GetMapping("/{entityType}/{entityId}/history")
    public ResponseEntity<List<VerificationRecordResponse>> getVerificationHistory(
            @PathVariable VerificationEntityType entityType,
            @PathVariable Long entityId) {

        List<VerificationRecordResponse> history = adminVerificationService.getVerificationHistory(entityType, entityId);
        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/admin/verifications/summary
     * Retrieve dashboard verification summary statistics.
     */
    @GetMapping("/summary")
    public ResponseEntity<VerificationSummaryStatsResponse> getVerificationStats() {
        VerificationSummaryStatsResponse stats = adminVerificationService.getVerificationStats();
        return ResponseEntity.ok(stats);
    }
}
