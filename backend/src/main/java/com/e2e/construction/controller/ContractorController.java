package com.e2e.construction.controller;

import com.e2e.construction.dto.ContractorProfileRequest;
import com.e2e.construction.dto.ContractorProfileResponse;
import com.e2e.construction.service.ContractorService;
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

@RestController
@RequestMapping("/api/contractors")
public class ContractorController {

    private final ContractorService contractorService;

    public ContractorController(ContractorService contractorService) {
        this.contractorService = contractorService;
    }

    /**
     * POST /api/contractors/profile
     * Create contractor profile for the authenticated contractor.
     */
    @PostMapping("/profile")
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<ContractorProfileResponse> createProfile(
            Principal principal,
            @Valid @RequestBody ContractorProfileRequest request) {
        ContractorProfileResponse response = contractorService.createProfile(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/contractors/profile
     * View the authenticated contractor's own profile.
     */
    @GetMapping({"/profile", "/profile/me"})
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<ContractorProfileResponse> getMyProfile(Principal principal) {
        ContractorProfileResponse response = contractorService.getMyProfile(principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/contractors/profile
     * Update the authenticated contractor's own profile.
     */
    @PutMapping({"/profile", "/profile/me"})
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<ContractorProfileResponse> updateMyProfile(
            Principal principal,
            @Valid @RequestBody ContractorProfileRequest request) {
        ContractorProfileResponse response = contractorService.updateMyProfile(principal.getName(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/contractors/{id}
     * View a contractor profile by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContractorProfileResponse> getProfileById(@PathVariable Long id) {
        ContractorProfileResponse response = contractorService.getProfileById(id);
        return ResponseEntity.ok(response);
    }
}
