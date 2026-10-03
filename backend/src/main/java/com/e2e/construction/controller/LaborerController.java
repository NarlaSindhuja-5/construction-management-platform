package com.e2e.construction.controller;

import com.e2e.construction.dto.LaborerAvailabilityRequest;
import com.e2e.construction.dto.LaborerProfileRequest;
import com.e2e.construction.dto.LaborerProfileResponse;
import com.e2e.construction.service.LaborerService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/laborers")
public class LaborerController {

    private final LaborerService laborerService;

    public LaborerController(LaborerService laborerService) {
        this.laborerService = laborerService;
    }

    /**
     * POST /api/laborers/profile
     * Create laborer profile for the authenticated laborer.
     */
    @PostMapping("/profile")
    @PreAuthorize("hasRole('LABORER')")
    public ResponseEntity<LaborerProfileResponse> createProfile(
            Principal principal,
            @Valid @RequestBody LaborerProfileRequest request) {
        LaborerProfileResponse response = laborerService.createProfile(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/laborers/profile
     * View the authenticated laborer's own profile.
     */
    @GetMapping({"/profile", "/profile/me"})
    @PreAuthorize("hasRole('LABORER')")
    public ResponseEntity<LaborerProfileResponse> getMyProfile(Principal principal) {
        LaborerProfileResponse response = laborerService.getMyProfile(principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/laborers/profile
     * Update the authenticated laborer's own profile.
     */
    @PutMapping({"/profile", "/profile/me"})
    @PreAuthorize("hasRole('LABORER')")
    public ResponseEntity<LaborerProfileResponse> updateMyProfile(
            Principal principal,
            @Valid @RequestBody LaborerProfileRequest request) {
        LaborerProfileResponse response = laborerService.updateMyProfile(principal.getName(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/laborers/availability
     * Change the authenticated laborer's availability (AVAILABLE, WORKING, UNAVAILABLE).
     */
    @PatchMapping("/availability")
    @PreAuthorize("hasRole('LABORER')")
    public ResponseEntity<LaborerProfileResponse> updateAvailability(
            Principal principal,
            @Valid @RequestBody LaborerAvailabilityRequest request) {
        LaborerProfileResponse response = laborerService.updateAvailability(principal.getName(), request.getAvailabilityStatus());
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/laborers/availability (Alias for PATCH)
     */
    @PutMapping("/availability")
    @PreAuthorize("hasRole('LABORER')")
    public ResponseEntity<LaborerProfileResponse> putAvailability(
            Principal principal,
            @Valid @RequestBody LaborerAvailabilityRequest request) {
        LaborerProfileResponse response = laborerService.updateAvailability(principal.getName(), request.getAvailabilityStatus());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/laborers/{id}
     * View a laborer profile by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LaborerProfileResponse> getProfileById(@PathVariable Long id) {
        LaborerProfileResponse response = laborerService.getProfileById(id);
        return ResponseEntity.ok(response);
    }
}
