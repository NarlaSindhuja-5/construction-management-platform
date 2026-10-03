package com.e2e.construction.controller;

import com.e2e.construction.dto.MachineryMaintenanceImageResponse;
import com.e2e.construction.dto.MachineryMaintenanceRequest;
import com.e2e.construction.dto.MachineryMaintenanceResponse;
import com.e2e.construction.service.MachineryMaintenanceService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/machinery")
public class MachineryMaintenanceController {

    private final MachineryMaintenanceService maintenanceService;

    public MachineryMaintenanceController(MachineryMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    /**
     * POST /api/machinery/{machineId}/maintenance
     * Add a new maintenance record. Only the machine owner and authorized admin allowed.
     */
    @PostMapping({ "/{machineId}/maintenance", "/{machineId}/maintenance-records" })
    @PreAuthorize("hasAnyRole('MACHINERY_OWNER', 'ADMIN')")
    public ResponseEntity<MachineryMaintenanceResponse> addMaintenanceRecord(
            @PathVariable Long machineId,
            Principal principal,
            @Valid @RequestBody MachineryMaintenanceRequest request) {

        MachineryMaintenanceResponse response = maintenanceService.addMaintenanceRecord(
                machineId,
                principal.getName(),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/machinery/maintenance/{maintenanceId}
     * Update an existing maintenance record. Only the machine owner and authorized admin allowed.
     */
    @PutMapping({ "/maintenance/{maintenanceId}", "/{machineId}/maintenance/{maintenanceId}" })
    @PreAuthorize("hasAnyRole('MACHINERY_OWNER', 'ADMIN')")
    public ResponseEntity<MachineryMaintenanceResponse> updateMaintenanceRecord(
            @PathVariable Long maintenanceId,
            Principal principal,
            @Valid @RequestBody MachineryMaintenanceRequest request) {

        MachineryMaintenanceResponse response = maintenanceService.updateMaintenanceRecord(
                maintenanceId,
                principal.getName(),
                request
        );
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/machinery/{machineId}/maintenance (or /history)
     * View maintenance history in chronological order.
     * Optional 'sort' param: "asc" for oldest-to-newest, "desc" (default) for newest-to-oldest.
     */
    @GetMapping({ "/{machineId}/maintenance", "/{machineId}/maintenance/history" })
    public ResponseEntity<List<MachineryMaintenanceResponse>> viewMaintenanceHistory(
            @PathVariable Long machineId,
            @RequestParam(required = false, defaultValue = "desc") String sort) {

        List<MachineryMaintenanceResponse> response = maintenanceService.getMaintenanceHistory(machineId, sort);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/machinery/maintenance/{maintenanceId}
     * View specific maintenance record by ID.
     */
    @GetMapping("/maintenance/{maintenanceId}")
    public ResponseEntity<MachineryMaintenanceResponse> viewMaintenanceRecordById(@PathVariable Long maintenanceId) {
        MachineryMaintenanceResponse response = maintenanceService.getMaintenanceRecordById(maintenanceId);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/machinery/maintenance/{maintenanceId}
     * Delete an incorrect maintenance record where authorized. Only machine owner or admin allowed.
     */
    @DeleteMapping({ "/maintenance/{maintenanceId}", "/{machineId}/maintenance/{maintenanceId}" })
    @PreAuthorize("hasAnyRole('MACHINERY_OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> deleteMaintenanceRecord(
            @PathVariable Long maintenanceId,
            Principal principal) {

        maintenanceService.deleteMaintenanceRecord(maintenanceId, principal.getName());
        return ResponseEntity.ok(Map.of(
                "message", "Maintenance record deleted successfully",
                "maintenanceId", maintenanceId.toString()
        ));
    }

    /**
     * POST /api/machinery/maintenance/{maintenanceId}/images
     * Upload an image for a maintenance event. Only machine owner or admin allowed.
     */
    @PostMapping(value = "/maintenance/{maintenanceId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('MACHINERY_OWNER', 'ADMIN')")
    public ResponseEntity<MachineryMaintenanceImageResponse> uploadMaintenanceImage(
            @PathVariable Long maintenanceId,
            Principal principal,
            @RequestParam("file") MultipartFile file) {

        MachineryMaintenanceImageResponse response = maintenanceService.uploadMaintenanceImage(
                maintenanceId,
                principal.getName(),
                file
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/machinery/maintenance/images/{storedFileName}
     * Stream or download maintenance image.
     */
    @GetMapping("/maintenance/images/{storedFileName:.+}")
    public ResponseEntity<Resource> viewMaintenanceImage(@PathVariable String storedFileName) {
        Resource resource = maintenanceService.getMaintenanceImageResource(storedFileName);

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
     * DELETE /api/machinery/maintenance/images/{imageId}
     * Delete an image from a maintenance record. Only machine owner or admin allowed.
     */
    @DeleteMapping("/maintenance/images/{imageId}")
    @PreAuthorize("hasAnyRole('MACHINERY_OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> deleteMaintenanceImage(
            @PathVariable Long imageId,
            Principal principal) {

        maintenanceService.deleteMaintenanceImage(imageId, principal.getName());
        return ResponseEntity.ok(Map.of(
                "message", "Maintenance image deleted successfully",
                "imageId", imageId.toString()
        ));
    }
}
