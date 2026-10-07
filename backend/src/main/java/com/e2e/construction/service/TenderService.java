package com.e2e.construction.service;

import com.e2e.construction.dto.TenderApplicationDocumentResponse;
import com.e2e.construction.dto.TenderApplicationRequest;
import com.e2e.construction.dto.TenderApplicationResponse;
import com.e2e.construction.dto.TenderApplicationReviewRequest;
import com.e2e.construction.dto.TenderDocumentResponse;
import com.e2e.construction.dto.TenderRequest;
import com.e2e.construction.dto.TenderResponse;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.Tender;
import com.e2e.construction.entity.TenderApplication;
import com.e2e.construction.entity.TenderApplicationDocument;
import com.e2e.construction.entity.TenderApplicationStatus;
import com.e2e.construction.entity.TenderDocument;
import com.e2e.construction.entity.TenderStatus;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.TenderApplicationDocumentRepository;
import com.e2e.construction.repository.TenderApplicationRepository;
import com.e2e.construction.repository.TenderDocumentRepository;
import com.e2e.construction.repository.TenderRepository;
import com.e2e.construction.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TenderService {

    private final TenderRepository tenderRepository;
    private final TenderDocumentRepository tenderDocumentRepository;
    private final TenderApplicationRepository tenderApplicationRepository;
    private final TenderApplicationDocumentRepository tenderApplicationDocumentRepository;
    private final ContractorRepository contractorRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public TenderService(
            TenderRepository tenderRepository,
            TenderDocumentRepository tenderDocumentRepository,
            TenderApplicationRepository tenderApplicationRepository,
            TenderApplicationDocumentRepository tenderApplicationDocumentRepository,
            ContractorRepository contractorRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService) {
        this.tenderRepository = tenderRepository;
        this.tenderDocumentRepository = tenderDocumentRepository;
        this.tenderApplicationRepository = tenderApplicationRepository;
        this.tenderApplicationDocumentRepository = tenderApplicationDocumentRepository;
        this.contractorRepository = contractorRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    // =========================================================================
    // Admin: Tender Creation & Management
    // =========================================================================

    /**
     * Create a new tender. Only ADMIN can create tenders.
     */
    @Transactional
    public TenderResponse createTender(String adminEmail, TenderRequest request) {
        User admin = getUserByEmail(adminEmail);
        verifyAdmin(admin);

        String tenderNum = request.getTenderNumber().trim();
        if (tenderRepository.existsByTenderNumber(tenderNum)) {
            throw new BadRequestException("Tender number is already in use: " + tenderNum);
        }

        if (request.getClosingDate().isBefore(request.getPublishedDate())) {
            throw new BadRequestException("Closing date cannot be before published date.");
        }

        TenderStatus initialStatus = request.getStatus() != null ? request.getStatus() : TenderStatus.OPEN;
        if (LocalDate.now().isAfter(request.getClosingDate()) && initialStatus == TenderStatus.OPEN) {
            initialStatus = TenderStatus.CLOSED;
        }

        Tender tender = new Tender(
                tenderNum,
                request.getTitle().trim(),
                request.getDepartment().trim(),
                request.getLocation().trim(),
                request.getCategory().trim(),
                request.getEstimatedValue(),
                request.getPublishedDate(),
                request.getClosingDate(),
                request.getEligibilityCriteria().trim(),
                request.getDescription().trim(),
                initialStatus,
                admin
        );

        // Attach document URLs if provided in request body
        attachTenderDocumentsFromUrls(tender, request.getDocuments());

        tender = tenderRepository.save(tender);
        return TenderResponse.fromEntity(tender);
    }

    /**
     * Update an existing tender. Only ADMIN can update tenders.
     */
    @Transactional
    public TenderResponse updateTender(Long id, String adminEmail, TenderRequest request) {
        User admin = getUserByEmail(adminEmail);
        verifyAdmin(admin);

        Tender tender = tenderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tender", "id", id));

        String targetTenderNum = request.getTenderNumber().trim();
        if (tenderRepository.existsByTenderNumberAndIdNot(targetTenderNum, id)) {
            throw new BadRequestException("Another tender is already registered with tender number: " + targetTenderNum);
        }

        if (request.getClosingDate().isBefore(request.getPublishedDate())) {
            throw new BadRequestException("Closing date cannot be before published date.");
        }

        tender.setTenderNumber(targetTenderNum);
        tender.setTitle(request.getTitle().trim());
        tender.setDepartment(request.getDepartment().trim());
        tender.setLocation(request.getLocation().trim());
        tender.setCategory(request.getCategory().trim());
        tender.setEstimatedValue(request.getEstimatedValue());
        tender.setPublishedDate(request.getPublishedDate());
        tender.setClosingDate(request.getClosingDate());
        tender.setEligibilityCriteria(request.getEligibilityCriteria().trim());
        tender.setDescription(request.getDescription().trim());

        if (request.getStatus() != null) {
            tender.setStatus(request.getStatus());
        }

        attachTenderDocumentsFromUrls(tender, request.getDocuments());

        tender = tenderRepository.save(tender);
        return TenderResponse.fromEntity(tender);
    }

    /**
     * Upload actual document files (PDF, DOCX, etc.) for a tender notice.
     */
    @Transactional
    public List<TenderDocumentResponse> uploadTenderDocuments(
            Long tenderId,
            String adminEmail,
            List<MultipartFile> files,
            String documentType) {

        User admin = getUserByEmail(adminEmail);
        verifyAdmin(admin);

        Tender tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new ResourceNotFoundException("Tender", "id", tenderId));

        if (files == null || files.isEmpty()) {
            throw new BadRequestException("Please select at least one document to upload.");
        }

        String resolvedType = (documentType != null && !documentType.isBlank()) ? documentType.trim() : "NOTICE";
        List<TenderDocumentResponse> uploaded = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            FileStorageService.StoredFileInfo info = fileStorageService.storeFile(file);
            String fileUrl = "/api/tenders/documents/" + info.getStoredFileName();

            TenderDocument doc = new TenderDocument(
                    tender,
                    info.getOriginalFilename(),
                    info.getStoredFileName(),
                    info.getAbsolutePath(),
                    fileUrl,
                    info.getFileSize(),
                    info.getContentType(),
                    resolvedType
            );

            tender.addDocument(doc);
            doc = tenderDocumentRepository.save(doc);
            uploaded.add(TenderDocumentResponse.fromEntity(doc));
        }

        return uploaded;
    }

    /**
     * Delete a tender and its associated files. Only ADMIN.
     */
    @Transactional
    public void deleteTender(Long id, String adminEmail) {
        User admin = getUserByEmail(adminEmail);
        verifyAdmin(admin);

        Tender tender = tenderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tender", "id", id));

        // Clean up files on disk
        if (tender.getDocuments() != null) {
            for (TenderDocument doc : tender.getDocuments()) {
                fileStorageService.deleteFile(doc.getStoredFileName());
            }
        }

        tenderRepository.delete(tender);
    }

    // =========================================================================
    // Search, Filter, and View Tenders (Public / Contractor)
    // =========================================================================

    /**
     * Search and filter tenders with multi-criteria filtering:
     * - keyword search
     * - status
     * - category
     * - department
     * - location
     * - estimated value range
     * - closing date range
     */
    @Transactional(readOnly = true)
    public List<TenderResponse> searchAndFilterTenders(
            String search,
            TenderStatus status,
            String category,
            String department,
            String location,
            BigDecimal minEstimatedValue,
            BigDecimal maxEstimatedValue,
            LocalDate closingAfter,
            LocalDate closingBefore) {

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanCategory = (category != null && !category.isBlank()) ? category.trim() : null;
        String cleanDepartment = (department != null && !department.isBlank()) ? department.trim() : null;
        String cleanLocation = (location != null && !location.isBlank()) ? location.trim() : null;

        return tenderRepository.searchAndFilterTenders(
                cleanSearch,
                status,
                cleanCategory,
                cleanDepartment,
                cleanLocation,
                minEstimatedValue,
                maxEstimatedValue,
                closingAfter,
                closingBefore
        ).stream()
                .map(TenderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * View tender details by ID.
     */
    @Transactional
    public TenderResponse getTenderById(Long id) {
        Tender tender = tenderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tender", "id", id));

        // Auto-close if past closing date and still OPEN
        if (LocalDate.now().isAfter(tender.getClosingDate()) && tender.getStatus() == TenderStatus.OPEN) {
            tender.setStatus(TenderStatus.CLOSED);
            tender = tenderRepository.save(tender);
        }

        return TenderResponse.fromEntity(tender);
    }

    /**
     * View tender details by unique tender number.
     */
    @Transactional
    public TenderResponse getTenderByNumber(String tenderNumber) {
        Tender tender = tenderRepository.findByTenderNumber(tenderNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Tender", "tenderNumber", tenderNumber));

        if (LocalDate.now().isAfter(tender.getClosingDate()) && tender.getStatus() == TenderStatus.OPEN) {
            tender.setStatus(TenderStatus.CLOSED);
            tender = tenderRepository.save(tender);
        }

        return TenderResponse.fromEntity(tender);
    }

    // =========================================================================
    // Contractor: Apply for Tenders & Upload Documents
    // =========================================================================

    /**
     * Apply for a tender. Contractors can submit bids and proposals.
     * PREVENTS applications after closing date.
     */
    @Transactional
    public TenderApplicationResponse applyForTender(
            Long tenderId,
            String contractorEmail,
            TenderApplicationRequest request) {

        User user = getUserByEmail(contractorEmail);
        Contractor contractor = contractorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Please create your Contractor Profile before bidding for tenders."));

        Tender tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new ResourceNotFoundException("Tender", "id", tenderId));

        LocalDate today = LocalDate.now();

        // 1. PREVENT applications after the closing date
        if (today.isAfter(tender.getClosingDate())) {
            if (tender.getStatus() == TenderStatus.OPEN) {
                tender.setStatus(TenderStatus.CLOSED);
                tenderRepository.save(tender);
            }
            throw new BadRequestException("Applications are closed for this tender. The closing date was "
                    + tender.getClosingDate() + ".");
        }

        // 2. Prevent applications if tender status is not OPEN
        if (tender.getStatus() != TenderStatus.OPEN) {
            throw new BadRequestException("Cannot apply for this tender. Current status is " + tender.getStatus()
                    + ". Only OPEN tenders accept applications.");
        }

        // 3. Prevent duplicate applications by the same contractor
        if (tenderApplicationRepository.existsByTenderIdAndContractorId(tender.getId(), contractor.getId())) {
            throw new BadRequestException("You have already submitted an application for tender "
                    + tender.getTenderNumber() + ". Duplicate applications are not permitted.");
        }

        TenderApplication application = new TenderApplication(
                tender,
                contractor,
                request.getBidAmount(),
                request.getProposedDurationDays(),
                request.getCoverLetter() != null ? request.getCoverLetter().trim() : null,
                today
        );

        attachApplicationDocumentsFromUrls(application, request.getDocuments());

        application = tenderApplicationRepository.save(application);
        return TenderApplicationResponse.fromEntity(application);
    }

    /**
     * Upload actual qualification / proposal documents for a submitted tender application.
     */
    @Transactional
    public List<TenderApplicationDocumentResponse> uploadApplicationDocuments(
            Long applicationId,
            String contractorEmail,
            List<MultipartFile> files,
            String documentType) {

        User user = getUserByEmail(contractorEmail);
        Contractor contractor = contractorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Contractor profile not found."));

        TenderApplication application = tenderApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("TenderApplication", "id", applicationId));

        // Strict ownership check
        if (!application.getContractor().getId().equals(contractor.getId())) {
            throw new AccessDeniedException("Access denied: You can only upload documents for your own application.");
        }

        // Prevent uploads if tender has already closed
        if (LocalDate.now().isAfter(application.getTender().getClosingDate())) {
            throw new BadRequestException("Document submissions are closed. Tender closing date was "
                    + application.getTender().getClosingDate() + ".");
        }

        if (files == null || files.isEmpty()) {
            throw new BadRequestException("Please select at least one document to upload.");
        }

        String resolvedType = (documentType != null && !documentType.isBlank()) ? documentType.trim() : "BID_DOCUMENT";
        List<TenderApplicationDocumentResponse> uploaded = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            FileStorageService.StoredFileInfo info = fileStorageService.storeFile(file);
            String fileUrl = "/api/tenders/applications/documents/" + info.getStoredFileName();

            TenderApplicationDocument doc = new TenderApplicationDocument(
                    application,
                    info.getOriginalFilename(),
                    info.getStoredFileName(),
                    info.getAbsolutePath(),
                    fileUrl,
                    info.getFileSize(),
                    info.getContentType(),
                    resolvedType
            );

            application.addDocument(doc);
            doc = tenderApplicationDocumentRepository.save(doc);
            uploaded.add(TenderApplicationDocumentResponse.fromEntity(doc));
        }

        return uploaded;
    }

    // =========================================================================
    // Track Application Status
    // =========================================================================

    /**
     * Track all applications submitted by the authenticated contractor.
     */
    @Transactional(readOnly = true)
    public List<TenderApplicationResponse> getMyApplications(String contractorEmail, TenderApplicationStatus status) {
        User user = getUserByEmail(contractorEmail);
        Contractor contractor = contractorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Contractor profile not found."));

        List<TenderApplication> apps;
        if (status != null) {
            apps = tenderApplicationRepository.findByContractorIdAndStatusOrderBySubmissionDateDesc(contractor.getId(), status);
        } else {
            apps = tenderApplicationRepository.findByContractorIdOrderBySubmissionDateDesc(contractor.getId());
        }

        return apps.stream()
                .map(TenderApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Track a specific application by ID. Accessible by applying contractor or admin.
     */
    @Transactional(readOnly = true)
    public TenderApplicationResponse getApplicationById(Long applicationId, String userEmail) {
        User user = getUserByEmail(userEmail);
        String roleName = user.getRole().getName().toUpperCase();

        TenderApplication application = tenderApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("TenderApplication", "id", applicationId));

        if ("CONTRACTOR".equals(roleName)) {
            Contractor contractor = contractorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("Contractor profile not found."));
            if (!application.getContractor().getId().equals(contractor.getId())) {
                throw new AccessDeniedException("Access denied: You can only track your own tender applications.");
            }
        } else if (!"ADMIN".equals(roleName)) {
            throw new AccessDeniedException("Access denied: You do not have permission to view this application.");
        }

        return TenderApplicationResponse.fromEntity(application);
    }

    /**
     * Admin view: List all applications submitted for a tender.
     */
    @Transactional(readOnly = true)
    public List<TenderApplicationResponse> getTenderApplications(Long tenderId, String adminEmail) {
        User admin = getUserByEmail(adminEmail);
        verifyAdmin(admin);

        if (!tenderRepository.existsById(tenderId)) {
            throw new ResourceNotFoundException("Tender", "id", tenderId);
        }

        return tenderApplicationRepository.findByTenderIdOrderBySubmissionDateDesc(tenderId)
                .stream()
                .map(TenderApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Admin view: Review and update application status (UNDER_REVIEW, ACCEPTED, REJECTED).
     */
    @Transactional
    public TenderApplicationResponse reviewApplication(
            Long applicationId,
            String adminEmail,
            TenderApplicationReviewRequest request) {

        User admin = getUserByEmail(adminEmail);
        verifyAdmin(admin);

        TenderApplication application = tenderApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("TenderApplication", "id", applicationId));

        application.setStatus(request.getStatus());
        if (request.getReviewerRemarks() != null) {
            application.setReviewerRemarks(request.getReviewerRemarks().trim());
        }

        application = tenderApplicationRepository.save(application);
        return TenderApplicationResponse.fromEntity(application);
    }

    /**
     * Load document resource for downloading or viewing.
     */
    public Resource getDocumentResource(String storedFileName) {
        return fileStorageService.loadFileAsResource(storedFileName);
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void verifyAdmin(User user) {
        if (!"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new AccessDeniedException("Access denied: Only system administrators can perform this action.");
        }
    }

    private void attachTenderDocumentsFromUrls(Tender tender, List<String> docUrls) {
        if (docUrls == null || docUrls.isEmpty()) {
            return;
        }

        for (String url : docUrls) {
            if (url == null || url.isBlank()) {
                continue;
            }
            String cleanUrl = url.trim();

            boolean exists = tender.getDocuments().stream()
                    .anyMatch(d -> cleanUrl.equals(d.getFileUrl()) || cleanUrl.equals(d.getStoredFileName()));

            if (!exists) {
                String fileName = cleanUrl;
                int lastSlash = cleanUrl.lastIndexOf('/');
                if (lastSlash >= 0 && lastSlash < cleanUrl.length() - 1) {
                    fileName = cleanUrl.substring(lastSlash + 1);
                }

                TenderDocument doc = new TenderDocument(
                        tender,
                        fileName,
                        fileName,
                        cleanUrl,
                        cleanUrl,
                        null,
                        "application/pdf",
                        "NOTICE"
                );
                tender.addDocument(doc);
            }
        }
    }

    private void attachApplicationDocumentsFromUrls(TenderApplication application, List<String> docUrls) {
        if (docUrls == null || docUrls.isEmpty()) {
            return;
        }

        for (String url : docUrls) {
            if (url == null || url.isBlank()) {
                continue;
            }
            String cleanUrl = url.trim();

            boolean exists = application.getDocuments().stream()
                    .anyMatch(d -> cleanUrl.equals(d.getFileUrl()) || cleanUrl.equals(d.getStoredFileName()));

            if (!exists) {
                String fileName = cleanUrl;
                int lastSlash = cleanUrl.lastIndexOf('/');
                if (lastSlash >= 0 && lastSlash < cleanUrl.length() - 1) {
                    fileName = cleanUrl.substring(lastSlash + 1);
                }

                TenderApplicationDocument doc = new TenderApplicationDocument(
                        application,
                        fileName,
                        fileName,
                        cleanUrl,
                        cleanUrl,
                        null,
                        "application/pdf",
                        "BID_DOCUMENT"
                );
                application.addDocument(doc);
            }
        }
    }
}
