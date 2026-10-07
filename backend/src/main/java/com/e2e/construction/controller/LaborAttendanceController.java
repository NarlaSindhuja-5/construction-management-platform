package com.e2e.construction.controller;

import com.e2e.construction.dto.BatchLaborAttendanceRequest;
import com.e2e.construction.dto.LaborAttendanceRequest;
import com.e2e.construction.dto.LaborAttendanceResponse;
import com.e2e.construction.dto.LaborAttendanceSummaryResponse;
import com.e2e.construction.entity.AttendanceStatus;
import com.e2e.construction.service.LaborAttendanceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/labor-attendance")
public class LaborAttendanceController {

    private final LaborAttendanceService laborAttendanceService;

    public LaborAttendanceController(LaborAttendanceService laborAttendanceService) {
        this.laborAttendanceService = laborAttendanceService;
    }

    /**
     * POST /api/labor-attendance
     * Record single labor attendance for a project.
     * Accessible by authorized contractors, site managers, and admins.
     * Prevents duplicate attendance for the same laborer, project, and date.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CONTRACTOR', 'SITE_MANAGER', 'ADMIN')")
    public ResponseEntity<LaborAttendanceResponse> recordAttendance(
            Principal principal,
            @Valid @RequestBody LaborAttendanceRequest request) {

        LaborAttendanceResponse response = laborAttendanceService.recordAttendance(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/labor-attendance/batch
     * Record batch labor attendance for multiple laborers on a project for a specific date.
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('CONTRACTOR', 'SITE_MANAGER', 'ADMIN')")
    public ResponseEntity<List<LaborAttendanceResponse>> recordBatchAttendance(
            Principal principal,
            @Valid @RequestBody BatchLaborAttendanceRequest request) {

        List<LaborAttendanceResponse> responses = laborAttendanceService.recordBatchAttendance(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * GET /api/labor-attendance/{id}
     * View attendance record by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LaborAttendanceResponse> getAttendanceById(
            @PathVariable Long id,
            Principal principal) {

        LaborAttendanceResponse response = laborAttendanceService.getAttendanceById(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/labor-attendance/{id}
     * Update an existing attendance record.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONTRACTOR', 'SITE_MANAGER', 'ADMIN')")
    public ResponseEntity<LaborAttendanceResponse> updateAttendance(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody LaborAttendanceRequest request) {

        LaborAttendanceResponse response = laborAttendanceService.updateAttendance(id, principal.getName(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/labor-attendance
     * View attendance history with filtering:
     * - projectId
     * - laborerId
     * - date (exact YYYY-MM-DD)
     * - startDate & endDate (range)
     * - status (PRESENT, ABSENT, HALF_DAY, OVERTIME)
     */
    @GetMapping
    public ResponseEntity<List<LaborAttendanceResponse>> getAttendanceHistory(
            Principal principal,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long laborerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) AttendanceStatus status) {

        List<LaborAttendanceResponse> history = laborAttendanceService.getAttendanceHistory(
                principal.getName(),
                projectId,
                laborerId,
                date,
                startDate,
                endDate,
                status
        );
        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/labor-attendance/project/{projectId}
     * Convenience endpoint: View attendance history for a specific project.
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<LaborAttendanceResponse>> getProjectAttendanceHistory(
            @PathVariable Long projectId,
            Principal principal,
            @RequestParam(required = false) Long laborerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) AttendanceStatus status) {

        List<LaborAttendanceResponse> history = laborAttendanceService.getAttendanceHistory(
                principal.getName(),
                projectId,
                laborerId,
                date,
                startDate,
                endDate,
                status
        );
        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/labor-attendance/project/{projectId}/summary
     * Get attendance summary statistics (total, present, absent, half-day, overtime, total hours).
     */
    @GetMapping("/project/{projectId}/summary")
    public ResponseEntity<LaborAttendanceSummaryResponse> getAttendanceSummary(
            @PathVariable Long projectId,
            Principal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LaborAttendanceSummaryResponse summary = laborAttendanceService.getAttendanceSummary(
                principal.getName(),
                projectId,
                date,
                startDate,
                endDate
        );
        return ResponseEntity.ok(summary);
    }

    /**
     * DELETE /api/labor-attendance/{id}
     * Delete an attendance record.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONTRACTOR', 'SITE_MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> deleteAttendance(
            @PathVariable Long id,
            Principal principal) {

        laborAttendanceService.deleteAttendance(id, principal.getName());
        return ResponseEntity.ok(Map.of(
                "message", "Labor attendance record deleted successfully",
                "attendanceId", id.toString()
        ));
    }
}
