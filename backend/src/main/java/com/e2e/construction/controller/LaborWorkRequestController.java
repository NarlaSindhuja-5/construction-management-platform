package com.e2e.construction.controller;

import com.e2e.construction.dto.LaborAssignmentResponse;
import com.e2e.construction.dto.LaborRequestActionDTO;
import com.e2e.construction.dto.LaborRequestResponse;
import com.e2e.construction.dto.LaborWorkRequestCreateDTO;
import com.e2e.construction.dto.LaborerProfileResponse;
import com.e2e.construction.entity.BookingStatus;
import com.e2e.construction.entity.LaborAssignmentStatus;
import com.e2e.construction.entity.LaborerAvailability;
import com.e2e.construction.service.LaborWorkRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
public class LaborWorkRequestController {

    private final LaborWorkRequestService laborWorkRequestService;

    public LaborWorkRequestController(LaborWorkRequestService laborWorkRequestService) {
        this.laborWorkRequestService = laborWorkRequestService;
    }

    /**
     * 1. Search verified laborers by:
     * - Location
     * - Skill
     * - Experience
     * - Availability
     * - Daily wage
     * GET /api/laborers/search
     * GET /api/labor-requests/search-laborers
     */
    @GetMapping({ "/api/laborers/search", "/api/labor-requests/search-laborers" })
    public ResponseEntity<List<LaborerProfileResponse>> searchVerifiedLaborers(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) LaborerAvailability availability,
            @RequestParam(required = false) BigDecimal maxDailyWage) {

        List<LaborerProfileResponse> responses = laborWorkRequestService.searchVerifiedLaborers(
                location, skill, minExperience, availability, maxDailyWage);
        return ResponseEntity.ok(responses);
    }

    /**
     * 2. View laborer profile.
     * GET /api/laborers/{id}/profile
     * GET /api/labor-requests/laborers/{id}/profile
     */
    @GetMapping({ "/api/laborers/{id}/profile", "/api/labor-requests/laborers/{id}/profile" })
    public ResponseEntity<LaborerProfileResponse> getLaborerProfile(@PathVariable Long id) {
        LaborerProfileResponse response = laborWorkRequestService.getLaborerProfile(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. Contractor workflow: Send work request to laborer.
     * POST /api/laborers/{laborerId}/work-requests
     * POST /api/labor-requests
     */
    @PostMapping({ "/api/laborers/{laborerId}/work-requests", "/api/labor-requests" })
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<LaborRequestResponse> sendWorkRequest(
            @PathVariable(required = false) Long laborerId,
            Principal principal,
            @Valid @RequestBody LaborWorkRequestCreateDTO requestDTO) {

        LaborRequestResponse response = laborWorkRequestService.sendWorkRequest(
                laborerId, principal.getName(), requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 4. Laborer receives and views work requests.
     * GET /api/labor-requests/my-requests
     * GET /api/labor-requests/laborer
     */
    @GetMapping({ "/api/labor-requests/my-requests", "/api/labor-requests/laborer" })
    @PreAuthorize("hasRole('LABORER')")
    public ResponseEntity<List<LaborRequestResponse>> getLaborerRequests(
            Principal principal,
            @RequestParam(required = false) BookingStatus status) {

        List<LaborRequestResponse> responses = laborWorkRequestService.getLaborerRequests(principal.getName(), status);
        return ResponseEntity.ok(responses);
    }

    /**
     * 5. Contractor views sent work requests.
     * GET /api/labor-requests/sent
     * GET /api/labor-requests/contractor
     */
    @GetMapping({ "/api/labor-requests/sent", "/api/labor-requests/contractor" })
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<List<LaborRequestResponse>> getContractorRequests(
            Principal principal,
            @RequestParam(required = false) BookingStatus status) {

        List<LaborRequestResponse> responses = laborWorkRequestService.getContractorRequests(principal.getName(), status);
        return ResponseEntity.ok(responses);
    }

    /**
     * 6. View single work request by ID.
     * GET /api/labor-requests/{id}
     */
    @GetMapping("/api/labor-requests/{id}")
    public ResponseEntity<LaborRequestResponse> getRequestById(
            @PathVariable Long id,
            Principal principal) {

        LaborRequestResponse response = laborWorkRequestService.getRequestById(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * 7. Laborer accepts request.
     * Creates labor assignment, connects laborer to project, and sets availability to WORKING.
     * Prevents overlapping assignments.
     * PUT /api/labor-requests/{id}/accept
     */
    @PutMapping("/api/labor-requests/{id}/accept")
    @PreAuthorize("hasRole('LABORER')")
    public ResponseEntity<LaborRequestResponse> acceptWorkRequest(
            @PathVariable Long id,
            Principal principal,
            @RequestBody(required = false) LaborRequestActionDTO actionDTO) {

        String notes = actionDTO != null ? actionDTO.getNotes() : null;
        LaborRequestResponse response = laborWorkRequestService.acceptWorkRequest(id, principal.getName(), notes);
        return ResponseEntity.ok(response);
    }

    /**
     * 8. Laborer rejects request.
     * PUT /api/labor-requests/{id}/reject
     */
    @PutMapping("/api/labor-requests/{id}/reject")
    @PreAuthorize("hasRole('LABORER')")
    public ResponseEntity<LaborRequestResponse> rejectWorkRequest(
            @PathVariable Long id,
            Principal principal,
            @RequestBody(required = false) LaborRequestActionDTO actionDTO) {

        String reason = actionDTO != null ? (actionDTO.getReason() != null ? actionDTO.getReason() : actionDTO.getNotes()) : null;
        LaborRequestResponse response = laborWorkRequestService.rejectWorkRequest(id, principal.getName(), reason);
        return ResponseEntity.ok(response);
    }

    /**
     * 9. Contractor cancels request.
     * PUT /api/labor-requests/{id}/cancel
     */
    @PutMapping("/api/labor-requests/{id}/cancel")
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<LaborRequestResponse> cancelWorkRequest(
            @PathVariable Long id,
            Principal principal,
            @RequestBody(required = false) LaborRequestActionDTO actionDTO) {

        String reason = actionDTO != null ? (actionDTO.getReason() != null ? actionDTO.getReason() : actionDTO.getNotes()) : null;
        LaborRequestResponse response = laborWorkRequestService.cancelWorkRequest(id, principal.getName(), reason);
        return ResponseEntity.ok(response);
    }

    /**
     * 10. Laborer views their assignments.
     * GET /api/labor-assignments/my-assignments
     */
    @GetMapping("/api/labor-assignments/my-assignments")
    @PreAuthorize("hasRole('LABORER')")
    public ResponseEntity<List<LaborAssignmentResponse>> getLaborerAssignments(
            Principal principal,
            @RequestParam(required = false) LaborAssignmentStatus status) {

        List<LaborAssignmentResponse> responses = laborWorkRequestService.getLaborerAssignments(principal.getName(), status);
        return ResponseEntity.ok(responses);
    }

    /**
     * 11. Contractor views labor assignments for a project.
     * GET /api/projects/{projectId}/labor-assignments
     * GET /api/labor-assignments/project/{projectId}
     */
    @GetMapping({ "/api/projects/{projectId}/labor-assignments", "/api/labor-assignments/project/{projectId}" })
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<List<LaborAssignmentResponse>> getProjectAssignments(
            @PathVariable Long projectId,
            Principal principal) {

        List<LaborAssignmentResponse> responses = laborWorkRequestService.getProjectAssignments(projectId, principal.getName());
        return ResponseEntity.ok(responses);
    }

    /**
     * 12. View single assignment details by ID.
     * GET /api/labor-assignments/{id}
     */
    @GetMapping("/api/labor-assignments/{id}")
    public ResponseEntity<LaborAssignmentResponse> getAssignmentById(
            @PathVariable Long id,
            Principal principal) {

        LaborAssignmentResponse response = laborWorkRequestService.getAssignmentById(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * 13. Complete labor assignment.
     * PUT /api/labor-assignments/{id}/complete
     */
    @PutMapping("/api/labor-assignments/{id}/complete")
    @PreAuthorize("hasAnyRole('CONTRACTOR', 'LABORER')")
    public ResponseEntity<LaborAssignmentResponse> completeAssignment(
            @PathVariable Long id,
            Principal principal,
            @RequestBody(required = false) LaborRequestActionDTO actionDTO) {

        String notes = actionDTO != null ? actionDTO.getNotes() : null;
        LaborAssignmentResponse response = laborWorkRequestService.completeAssignment(id, principal.getName(), notes);
        return ResponseEntity.ok(response);
    }
}
