package com.e2e.construction.controller;

import com.e2e.construction.dto.MachineryRequest;
import com.e2e.construction.dto.MachineryResponse;
import com.e2e.construction.entity.MachineryAvailability;
import com.e2e.construction.entity.MachineryCategory;
import com.e2e.construction.service.MachineryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import java.util.Map;

@RestController
@RequestMapping("/api/machinery")
public class MachineryController {

    private final MachineryService machineryService;

    public MachineryController(MachineryService machineryService) {
        this.machineryService = machineryService;
    }

    /**
     * POST /api/machinery
     * Create machinery listing. Only authenticated MACHINERY_OWNER users can create listings.
     */
    @PostMapping
    @PreAuthorize("hasRole('MACHINERY_OWNER')")
    public ResponseEntity<MachineryResponse> createMachine(
            Principal principal,
            @Valid @RequestBody MachineryRequest request) {
        MachineryResponse response = machineryService.createMachine(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/machinery/{id}
     * View machine by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MachineryResponse> getMachineById(@PathVariable Long id) {
        MachineryResponse response = machineryService.getMachineById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/machinery/{id}
     * Update machine. A machinery owner can modify only their own machines.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MACHINERY_OWNER')")
    public ResponseEntity<MachineryResponse> updateMachine(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody MachineryRequest request) {
        MachineryResponse response = machineryService.updateMachine(id, principal.getName(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/machinery/{id}
     * Delete machine. A machinery owner can delete only their own machines.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MACHINERY_OWNER')")
    public ResponseEntity<Map<String, String>> deleteMachine(
            @PathVariable Long id,
            Principal principal) {
        machineryService.deleteMachine(id, principal.getName());
        return ResponseEntity.ok(Map.of(
                "message", "Machinery listing deleted successfully",
                "machineId", id.toString()
        ));
    }

    /**
     * GET /api/machinery/my-machines
     * List authenticated machinery owner's machines with optional search and filters.
     */
    @GetMapping("/my-machines")
    @PreAuthorize("hasRole('MACHINERY_OWNER')")
    public ResponseEntity<List<MachineryResponse>> getMyMachines(
            Principal principal,
            @RequestParam(required = false) MachineryCategory category,
            @RequestParam(required = false) MachineryAvailability availability,
            @RequestParam(required = false) String search) {
        List<MachineryResponse> responses = machineryService.getMyMachines(principal.getName(), category, availability, search);
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /api/machinery
     * Search and filter machines by category, availability, location, or keyword search.
     */
    @GetMapping({ "", "/search" })
    public ResponseEntity<List<MachineryResponse>> searchMachines(
            @RequestParam(required = false) MachineryCategory category,
            @RequestParam(required = false) MachineryAvailability availability,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String search) {
        List<MachineryResponse> responses = machineryService.searchAndFilterMachines(category, availability, location, search);
        return ResponseEntity.ok(responses);
    }
}
