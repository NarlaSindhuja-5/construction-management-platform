package com.e2e.construction.controller;

import com.e2e.construction.dto.MachineConditionRequest;
import com.e2e.construction.dto.MachineConditionResponse;
import com.e2e.construction.service.MachineConditionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/machinery")
public class MachineConditionController {

    private final MachineConditionService machineConditionService;

    public MachineConditionController(MachineConditionService machineConditionService) {
        this.machineConditionService = machineConditionService;
    }

    /**
     * POST /api/machinery/{machineId}/condition (or /conditions)
     * Create a new condition report for a machine.
     * Only the machine owner or authorized admin can create condition information.
     */
    @PostMapping({ "/{machineId}/condition", "/{machineId}/conditions" })
    @PreAuthorize("hasAnyRole('MACHINERY_OWNER', 'ADMIN')")
    public ResponseEntity<MachineConditionResponse> createConditionReport(
            @PathVariable Long machineId,
            Principal principal,
            @Valid @RequestBody MachineConditionRequest request) {

        MachineConditionResponse response = machineConditionService.createReport(
                machineId,
                principal.getName(),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/machinery/conditions/{conditionId}
     * Update an existing condition report by ID.
     * Only the machine owner or authorized admin can update condition information.
     */
    @PutMapping({ "/conditions/{conditionId}", "/{machineId}/conditions/{conditionId}" })
    @PreAuthorize("hasAnyRole('MACHINERY_OWNER', 'ADMIN')")
    public ResponseEntity<MachineConditionResponse> updateConditionReport(
            @PathVariable Long conditionId,
            Principal principal,
            @Valid @RequestBody MachineConditionRequest request) {

        MachineConditionResponse response = machineConditionService.updateReport(
                conditionId,
                principal.getName(),
                request
        );
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/machinery/{machineId}/condition
     * Update current condition report for the machine.
     * Only the machine owner or authorized admin can update condition information.
     */
    @PutMapping("/{machineId}/condition")
    @PreAuthorize("hasAnyRole('MACHINERY_OWNER', 'ADMIN')")
    public ResponseEntity<MachineConditionResponse> updateMachineCondition(
            @PathVariable Long machineId,
            Principal principal,
            @Valid @RequestBody MachineConditionRequest request) {

        MachineConditionResponse response = machineConditionService.updateLatestCondition(
                machineId,
                principal.getName(),
                request
        );
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/machinery/{machineId}/condition
     * View the current/latest condition report of a machine.
     */
    @GetMapping("/{machineId}/condition")
    public ResponseEntity<MachineConditionResponse> viewConditionReport(@PathVariable Long machineId) {
        MachineConditionResponse response = machineConditionService.getLatestCondition(machineId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/machinery/conditions/{conditionId}
     * View a specific condition report by ID.
     */
    @GetMapping("/conditions/{conditionId}")
    public ResponseEntity<MachineConditionResponse> viewConditionReportById(@PathVariable Long conditionId) {
        MachineConditionResponse response = machineConditionService.getConditionById(conditionId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/machinery/{machineId}/conditions/history (or /condition/history or /conditions)
     * View full condition report history for a machine.
     */
    @GetMapping({ "/{machineId}/conditions/history", "/{machineId}/condition/history", "/{machineId}/conditions" })
    public ResponseEntity<List<MachineConditionResponse>> viewMachineConditionHistory(@PathVariable Long machineId) {
        List<MachineConditionResponse> response = machineConditionService.getConditionHistory(machineId);
        return ResponseEntity.ok(response);
    }
}
