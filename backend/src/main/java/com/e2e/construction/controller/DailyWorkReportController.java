package com.e2e.construction.controller;

import com.e2e.construction.dto.DailyWorkReportImageResponse;
import com.e2e.construction.dto.DailyWorkReportRequest;
import com.e2e.construction.dto.DailyWorkReportResponse;
import com.e2e.construction.service.DailyWorkReportService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/daily-reports", "/api/daily-work-reports"})
public class DailyWorkReportController {

    private final DailyWorkReportService dailyWorkReportService;

    public DailyWorkReportController(DailyWorkReportService dailyWorkReportService) {
        this.dailyWorkReportService = dailyWorkReportService;
    }

    /**
     * POST /api/daily-reports
     * Create a new daily work report for a project.
     * Only authorized contractor or site manager (or admin) can create reports.
     * Prevents duplicate reports for the same project and date unless the user has permission to edit the report.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CONTRACTOR', 'SITE_MANAGER', 'ADMIN')")
    public ResponseEntity<DailyWorkReportResponse> createReport(
            Principal principal,
            @Valid @RequestBody DailyWorkReportRequest request) {

        DailyWorkReportResponse response = dailyWorkReportService.createReport(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/daily-reports/{id}
     * View report by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DailyWorkReportResponse> getReportById(@PathVariable Long id) {
        DailyWorkReportResponse response = dailyWorkReportService.getReportById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/daily-reports/{id}
     * Update an existing daily work report.
     * Only authorized contractor, site manager, or admin allowed.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONTRACTOR', 'SITE_MANAGER', 'ADMIN')")
    public ResponseEntity<DailyWorkReportResponse> updateReport(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody DailyWorkReportRequest request) {

        DailyWorkReportResponse response = dailyWorkReportService.updateReport(id, principal.getName(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/daily-reports
     * List reports with optional filters:
     * - Filter by project (?projectId=...)
     * - Filter by exact date (?date=YYYY-MM-DD)
     * - Filter by date range (?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD)
     */
    @GetMapping
    public ResponseEntity<List<DailyWorkReportResponse>> listReports(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<DailyWorkReportResponse> reports = dailyWorkReportService.listReports(projectId, date, startDate, endDate);
        return ResponseEntity.ok(reports);
    }

    /**
     * GET /api/daily-reports/project/{projectId}
     * Convenience endpoint: List reports filtered by project.
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<DailyWorkReportResponse>> listReportsByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<DailyWorkReportResponse> reports = dailyWorkReportService.listReports(projectId, date, startDate, endDate);
        return ResponseEntity.ok(reports);
    }

    /**
     * POST /api/daily-reports/{id}/images
     * Upload one or multiple site images for a daily report.
     */
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CONTRACTOR', 'SITE_MANAGER', 'ADMIN')")
    public ResponseEntity<List<DailyWorkReportImageResponse>> uploadReportImages(
            @PathVariable Long id,
            Principal principal,
            @RequestParam("files") List<MultipartFile> files) {

        List<DailyWorkReportImageResponse> response = dailyWorkReportService.uploadReportImages(id, principal.getName(), files);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/daily-reports/images/{storedFileName}
     * Stream / view an uploaded site image.
     */
    @GetMapping("/images/{storedFileName:.+}")
    public ResponseEntity<Resource> viewReportImage(@PathVariable String storedFileName) {
        Resource resource = dailyWorkReportService.getImageResource(storedFileName);

        String contentType = "application/octet-stream";
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
        } catch (IOException ignored) {
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * DELETE /api/daily-reports/images/{imageId}
     * Delete a single image from a report.
     */
    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasAnyRole('CONTRACTOR', 'SITE_MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> deleteReportImage(
            @PathVariable Long imageId,
            Principal principal) {

        dailyWorkReportService.deleteReportImage(imageId, principal.getName());
        return ResponseEntity.ok(Map.of(
                "message", "Daily report image deleted successfully",
                "imageId", imageId.toString()
        ));
    }

    /**
     * DELETE /api/daily-reports/{id}
     * Delete a daily work report.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONTRACTOR', 'SITE_MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> deleteReport(
            @PathVariable Long id,
            Principal principal) {

        dailyWorkReportService.deleteReport(id, principal.getName());
        return ResponseEntity.ok(Map.of(
                "message", "Daily work report deleted successfully",
                "reportId", id.toString()
        ));
    }
}
