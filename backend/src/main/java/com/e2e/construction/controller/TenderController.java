package com.e2e.construction.controller;

import com.e2e.construction.dto.TenderApplicationDocumentResponse;
import com.e2e.construction.dto.TenderApplicationRequest;
import com.e2e.construction.dto.TenderApplicationResponse;
import com.e2e.construction.dto.TenderApplicationReviewRequest;
import com.e2e.construction.dto.TenderDocumentResponse;
import com.e2e.construction.dto.TenderRequest;
import com.e2e.construction.dto.TenderResponse;
import com.e2e.construction.entity.TenderApplicationStatus;
import com.e2e.construction.entity.TenderStatus;
import com.e2e.construction.service.TenderService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenders")
public class TenderController {

    private final TenderService tenderService;

    public TenderController(TenderService tenderService) {
        this.tenderService = tenderService;
    }

    // =========================================================================
    // Admin Tender APIs
    // =========================================================================

    /**
     * POST /api/tenders
     * Create tender. Only ADMIN.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TenderResponse> createTender(
            Principal principal,
            @Valid @RequestBody TenderRequest request) {

        TenderResponse response = tenderService.createTender(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/tenders/{id}
     * Update tender. Only ADMIN.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TenderResponse> updateTender(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody TenderRequest request) {

        TenderResponse response = tenderService.updateTender(id, principal.getName(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/tenders/{id}/documents
     * Upload official tender documents / RFP / specifications. Only ADMIN.
     */
    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TenderDocumentResponse>> uploadTenderDocuments(
            @PathVariable Long id,
            Principal principal,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(required = false, defaultValue = "NOTICE") String documentType) {

        List<TenderDocumentResponse> responses = tenderService.uploadTenderDocuments(
                id,
                principal.getName(),
                files,
                documentType
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * DELETE /api/tenders/{id}
     * Delete tender. Only ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteTender(
            @PathVariable Long id,
            Principal principal) {

        tenderService.deleteTender(id, principal.getName());
        return ResponseEntity.ok(Map.of(
                "message", "Tender deleted successfully",
                "tenderId", id.toString()
        ));
    }

    /**
     * GET /api/tenders/{id}/applications
     * List all submitted applications/bids for a tender. Only ADMIN.
     */
    @GetMapping("/{id}/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TenderApplicationResponse>> getTenderApplications(
            @PathVariable Long id,
            Principal principal) {

        List<TenderApplicationResponse> responses = tenderService.getTenderApplications(id, principal.getName());
        return ResponseEntity.ok(responses);
    }

    /**
     * PATCH /api/tenders/applications/{applicationId}/status
     * Review tender application status (UNDER_REVIEW, ACCEPTED, REJECTED). Only ADMIN.
     */
    @PatchMapping("/applications/{applicationId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TenderApplicationResponse> reviewApplication(
            @PathVariable Long applicationId,
            Principal principal,
            @Valid @RequestBody TenderApplicationReviewRequest reviewRequest) {

        TenderApplicationResponse response = tenderService.reviewApplication(
                applicationId,
                principal.getName(),
                reviewRequest
        );
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // Contractor & Public Tender APIs
    // =========================================================================

    /**
     * GET /api/tenders
     * Search and filter tenders.
     */
    @GetMapping
    public ResponseEntity<List<TenderResponse>> searchAndFilterTenders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TenderStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minEstimatedValue,
            @RequestParam(required = false) BigDecimal maxEstimatedValue,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closingAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closingBefore) {

        List<TenderResponse> tenders = tenderService.searchAndFilterTenders(
                search,
                status,
                category,
                department,
                location,
                minEstimatedValue,
                maxEstimatedValue,
                closingAfter,
                closingBefore
        );
        return ResponseEntity.ok(tenders);
    }

    /**
     * GET /api/tenders/{id}
     * View tender details by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TenderResponse> getTenderById(@PathVariable Long id) {
        TenderResponse tender = tenderService.getTenderById(id);
        return ResponseEntity.ok(tender);
    }

    /**
     * GET /api/tenders/number/{tenderNumber}
     * View tender details by unique tender number.
     */
    @GetMapping("/number/{tenderNumber}")
    public ResponseEntity<TenderResponse> getTenderByNumber(@PathVariable String tenderNumber) {
        TenderResponse tender = tenderService.getTenderByNumber(tenderNumber);
        return ResponseEntity.ok(tender);
    }

    /**
     * POST /api/tenders/{id}/apply
     * Apply for tender. Only authenticated CONTRACTOR.
     * Prevents applications after closing date.
     */
    @PostMapping("/{id}/apply")
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<TenderApplicationResponse> applyForTender(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody TenderApplicationRequest request) {

        TenderApplicationResponse response = tenderService.applyForTender(
                id,
                principal.getName(),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/tenders/applications/{applicationId}/documents
     * Upload qualification / bid documents for a submitted application. Only CONTRACTOR.
     */
    @PostMapping(value = "/applications/{applicationId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<List<TenderApplicationDocumentResponse>> uploadApplicationDocuments(
            @PathVariable Long applicationId,
            Principal principal,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(required = false, defaultValue = "BID_DOCUMENT") String documentType) {

        List<TenderApplicationDocumentResponse> responses = tenderService.uploadApplicationDocuments(
                applicationId,
                principal.getName(),
                files,
                documentType
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * GET /api/tenders/applications/my-applications
     * Track applications submitted by the logged-in contractor.
     */
    @GetMapping("/applications/my-applications")
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<List<TenderApplicationResponse>> getMyApplications(
            Principal principal,
            @RequestParam(required = false) TenderApplicationStatus status) {

        List<TenderApplicationResponse> responses = tenderService.getMyApplications(principal.getName(), status);
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /api/tenders/applications/{applicationId}
     * Track a specific application. Accessible by applying contractor or admin.
     */
    @GetMapping("/applications/{applicationId}")
    @PreAuthorize("hasAnyRole('CONTRACTOR', 'ADMIN')")
    public ResponseEntity<TenderApplicationResponse> getApplicationById(
            @PathVariable Long applicationId,
            Principal principal) {

        TenderApplicationResponse response = tenderService.getApplicationById(applicationId, principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/tenders/documents/{storedFileName}
     * Download / stream tender notice document.
     */
    @GetMapping("/documents/{storedFileName:.+}")
    public ResponseEntity<Resource> downloadTenderDocument(@PathVariable String storedFileName) {
        Resource resource = tenderService.getDocumentResource(storedFileName);

        String contentType = "application/octet-stream";
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
        } catch (IOException ignored) {
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/pdf"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * GET /api/tenders/applications/documents/{storedFileName}
     * Download / stream application document.
     */
    @GetMapping("/applications/documents/{storedFileName:.+}")
    public ResponseEntity<Resource> downloadApplicationDocument(@PathVariable String storedFileName) {
        Resource resource = tenderService.getDocumentResource(storedFileName);

        String contentType = "application/octet-stream";
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
        } catch (IOException ignored) {
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/pdf"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
