package com.e2e.construction.controller;

import com.e2e.construction.dto.MachineryOwnerProfileRequest;
import com.e2e.construction.dto.MachineryOwnerProfileResponse;
import com.e2e.construction.service.MachineryOwnerService;
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
@RequestMapping("/api/machinery-owners")
public class MachineryOwnerController {

    private final MachineryOwnerService machineryOwnerService;

    public MachineryOwnerController(MachineryOwnerService machineryOwnerService) {
        this.machineryOwnerService = machineryOwnerService;
    }

    /**
     * POST /api/machinery-owners/profile
     * Create machinery owner profile for the authenticated machinery owner.
     */
    @PostMapping("/profile")
    @PreAuthorize("hasRole('MACHINERY_OWNER')")
    public ResponseEntity<MachineryOwnerProfileResponse> createProfile(
            Principal principal,
            @Valid @RequestBody MachineryOwnerProfileRequest request) {
        MachineryOwnerProfileResponse response = machineryOwnerService.createProfile(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/machinery-owners/profile
     * View the authenticated machinery owner's own profile.
     */
    @GetMapping({"/profile", "/profile/me"})
    @PreAuthorize("hasRole('MACHINERY_OWNER')")
    public ResponseEntity<MachineryOwnerProfileResponse> getMyProfile(Principal principal) {
        MachineryOwnerProfileResponse response = machineryOwnerService.getMyProfile(principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/machinery-owners/profile
     * Update the authenticated machinery owner's own profile.
     */
    @PutMapping({"/profile", "/profile/me"})
    @PreAuthorize("hasRole('MACHINERY_OWNER')")
    public ResponseEntity<MachineryOwnerProfileResponse> updateMyProfile(
            Principal principal,
            @Valid @RequestBody MachineryOwnerProfileRequest request) {
        MachineryOwnerProfileResponse response = machineryOwnerService.updateMyProfile(principal.getName(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/machinery-owners/{id}
     * View a machinery owner profile by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MachineryOwnerProfileResponse> getProfileById(@PathVariable Long id) {
        MachineryOwnerProfileResponse response = machineryOwnerService.getProfileById(id);
        return ResponseEntity.ok(response);
    }
}
