package com.e2e.construction.service;

import com.e2e.construction.dto.DailyWorkReportImageResponse;
import com.e2e.construction.dto.DailyWorkReportRequest;
import com.e2e.construction.dto.DailyWorkReportResponse;
import com.e2e.construction.entity.DailyWorkReport;
import com.e2e.construction.entity.DailyWorkReportImage;
import com.e2e.construction.entity.Project;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.DailyWorkReportImageRepository;
import com.e2e.construction.repository.DailyWorkReportRepository;
import com.e2e.construction.repository.ProjectRepository;
import com.e2e.construction.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DailyWorkReportService {

    private final DailyWorkReportRepository dailyWorkReportRepository;
    private final DailyWorkReportImageRepository dailyWorkReportImageRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public DailyWorkReportService(
            DailyWorkReportRepository dailyWorkReportRepository,
            DailyWorkReportImageRepository dailyWorkReportImageRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService) {
        this.dailyWorkReportRepository = dailyWorkReportRepository;
        this.dailyWorkReportImageRepository = dailyWorkReportImageRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Create a new Daily Construction Work Report.
     * Prevents duplicate reports for the same project and date unless the user has permission to edit the report.
     */
    @Transactional
    public DailyWorkReportResponse createReport(String userEmail, DailyWorkReportRequest request) {
        User user = getUserByEmail(userEmail);

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        // Verify project authorization for the creating user
        verifyProjectAuthorization(project, user);

        LocalDate reportDate = request.getDate();

        // Check if report already exists for this project and date
        Optional<DailyWorkReport> existingReportOpt = dailyWorkReportRepository.findByProjectIdAndReportDate(project.getId(), reportDate);
        if (existingReportOpt.isPresent()) {
            DailyWorkReport existingReport = existingReportOpt.get();
            // Prevent duplicate reports unless the user has permission to edit the existing report
            if (!canEditReport(existingReport, user)) {
                throw new BadRequestException("A daily work report already exists for project '" + project.getName()
                        + "' on date " + reportDate + ", and you do not have permission to modify it.");
            }

            // User has permission: update existing report to prevent duplicate rows
            updateReportFields(existingReport, request);
            attachImagesFromRequest(existingReport, request.getImages());
            existingReport = dailyWorkReportRepository.save(existingReport);
            return DailyWorkReportResponse.fromEntity(existingReport);
        }

        // Create new daily work report
        DailyWorkReport report = new DailyWorkReport();
        report.setProject(project);
        report.setReportDate(reportDate);
        report.setCreatedBy(user);
        updateReportFields(report, request);
        attachImagesFromRequest(report, request.getImages());

        report = dailyWorkReportRepository.save(report);
        return DailyWorkReportResponse.fromEntity(report);
    }

    /**
     * View a daily work report by ID.
     */
    @Transactional(readOnly = true)
    public DailyWorkReportResponse getReportById(Long id) {
        DailyWorkReport report = dailyWorkReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DailyWorkReport", "id", id));
        return DailyWorkReportResponse.fromEntity(report);
    }

    /**
     * Update an existing daily work report.
     */
    @Transactional
    public DailyWorkReportResponse updateReport(Long id, String userEmail, DailyWorkReportRequest request) {
        User user = getUserByEmail(userEmail);

        DailyWorkReport report = dailyWorkReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DailyWorkReport", "id", id));

        // Verify edit permission
        if (!canEditReport(report, user)) {
            throw new AccessDeniedException("Access denied: You do not have permission to edit this daily work report.");
        }

        // If project or date changed, ensure no duplicate conflict exists
        Long targetProjectId = request.getProjectId() != null ? request.getProjectId() : report.getProject().getId();
        LocalDate targetDate = request.getDate() != null ? request.getDate() : report.getReportDate();

        if (dailyWorkReportRepository.existsByProjectIdAndReportDateAndIdNot(targetProjectId, targetDate, id)) {
            throw new BadRequestException("Another daily work report already exists for project ID "
                    + targetProjectId + " on date " + targetDate);
        }

        if (request.getProjectId() != null && !request.getProjectId().equals(report.getProject().getId())) {
            Project newProject = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));
            verifyProjectAuthorization(newProject, user);
            report.setProject(newProject);
        }

        if (request.getDate() != null) {
            report.setReportDate(request.getDate());
        }

        updateReportFields(report, request);
        attachImagesFromRequest(report, request.getImages());

        report = dailyWorkReportRepository.save(report);
        return DailyWorkReportResponse.fromEntity(report);
    }

    /**
     * List daily work reports with optional filtering by project, date, or date range.
     */
    @Transactional(readOnly = true)
    public List<DailyWorkReportResponse> listReports(Long projectId, LocalDate date, LocalDate startDate, LocalDate endDate) {
        return dailyWorkReportRepository.filterReports(projectId, date, startDate, endDate)
                .stream()
                .map(DailyWorkReportResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Upload multiple site images to an existing daily work report.
     */
    @Transactional
    public List<DailyWorkReportImageResponse> uploadReportImages(Long reportId, String userEmail, List<MultipartFile> files) {
        User user = getUserByEmail(userEmail);

        DailyWorkReport report = dailyWorkReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyWorkReport", "id", reportId));

        if (!canEditReport(report, user)) {
            throw new AccessDeniedException("Access denied: You do not have permission to upload images for this report.");
        }

        if (files == null || files.isEmpty()) {
            throw new BadRequestException("Please select at least one image file to upload.");
        }

        List<DailyWorkReportImageResponse> uploadedResponses = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            FileStorageService.StoredFileInfo storedFileInfo = fileStorageService.storeFile(file);
            String fileUrl = "/api/daily-reports/images/" + storedFileInfo.getStoredFileName();

            DailyWorkReportImage image = new DailyWorkReportImage(
                    report,
                    storedFileInfo.getOriginalFilename(),
                    storedFileInfo.getStoredFileName(),
                    storedFileInfo.getAbsolutePath(),
                    fileUrl,
                    storedFileInfo.getFileSize(),
                    storedFileInfo.getContentType(),
                    "Site photograph for daily report #" + reportId
            );

            report.addImage(image);
            image = dailyWorkReportImageRepository.save(image);
            uploadedResponses.add(DailyWorkReportImageResponse.fromEntity(image));
        }

        return uploadedResponses;
    }

    /**
     * Delete a single image from a daily work report.
     */
    @Transactional
    public void deleteReportImage(Long imageId, String userEmail) {
        User user = getUserByEmail(userEmail);

        DailyWorkReportImage image = dailyWorkReportImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyWorkReportImage", "id", imageId));

        if (!canEditReport(image.getReport(), user)) {
            throw new AccessDeniedException("Access denied: You do not have permission to delete this image.");
        }

        fileStorageService.deleteFile(image.getStoredFileName());
        image.getReport().removeImage(image);
        dailyWorkReportImageRepository.delete(image);
    }

    /**
     * Delete an entire daily work report.
     */
    @Transactional
    public void deleteReport(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);

        DailyWorkReport report = dailyWorkReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DailyWorkReport", "id", id));

        if (!canEditReport(report, user)) {
            throw new AccessDeniedException("Access denied: You do not have permission to delete this report.");
        }

        // Clean up files on disk
        if (report.getImages() != null) {
            for (DailyWorkReportImage img : report.getImages()) {
                fileStorageService.deleteFile(img.getStoredFileName());
            }
        }

        dailyWorkReportRepository.delete(report);
    }

    /**
     * Load image resource for viewing or download.
     */
    public Resource getImageResource(String storedFileName) {
        return fileStorageService.loadFileAsResource(storedFileName);
    }

    // =========================================================================
    // Helper Methods & Authorization
    // =========================================================================

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Checks if a user is authorized to create/manage daily reports for the project.
     * Authorized:
     * - ADMIN: full system access
     * - SITE_MANAGER: site supervisory authorization
     * - CONTRACTOR: must own the project
     */
    private void verifyProjectAuthorization(Project project, User user) {
        String roleName = user.getRole().getName().toUpperCase();
        if ("ADMIN".equals(roleName) || "SITE_MANAGER".equals(roleName)) {
            return;
        }

        if ("CONTRACTOR".equals(roleName)) {
            if (project.getContractor() != null &&
                    project.getContractor().getUser() != null &&
                    project.getContractor().getUser().getId().equals(user.getId())) {
                return;
            }
            throw new AccessDeniedException("Access denied: You are only authorized to file daily reports for your own projects.");
        }

        throw new AccessDeniedException("Access denied: Only authorized contractors or site managers can file daily reports.");
    }

    /**
     * Checks if user has permission to edit the report.
     * Authorized:
     * - ADMIN
     * - SITE_MANAGER
     * - Project's owning CONTRACTOR
     * - The user who originally created the report
     */
    public boolean canEditReport(DailyWorkReport report, User user) {
        String roleName = user.getRole().getName().toUpperCase();
        if ("ADMIN".equals(roleName) || "SITE_MANAGER".equals(roleName)) {
            return true;
        }

        if (report.getCreatedBy() != null && report.getCreatedBy().getId().equals(user.getId())) {
            return true;
        }

        if ("CONTRACTOR".equals(roleName)) {
            Project project = report.getProject();
            return project != null &&
                    project.getContractor() != null &&
                    project.getContractor().getUser() != null &&
                    project.getContractor().getUser().getId().equals(user.getId());
        }

        return false;
    }

    private void updateReportFields(DailyWorkReport report, DailyWorkReportRequest request) {
        report.setWorkDescription(request.getWorkDescription().trim());
        report.setNumberOfWorkers(request.getNumberOfWorkers());
        if (request.getMachineryUsed() != null) {
            report.setMachineryUsed(request.getMachineryUsed().trim());
        }
        if (request.getMaterialsUsed() != null) {
            report.setMaterialsUsed(request.getMaterialsUsed().trim());
        }
        if (request.getQuantityCompleted() != null) {
            report.setQuantityCompleted(request.getQuantityCompleted().trim());
        }
        report.setWorkingHours(request.getWorkingHours());
        report.setProgressPercentage(request.getProgressPercentage());
        report.setExpenses(request.getExpenses());
        if (request.getIssuesOrDelays() != null) {
            report.setIssuesOrDelays(request.getIssuesOrDelays().trim());
        }
        if (request.getRemarks() != null) {
            report.setRemarks(request.getRemarks().trim());
        }
    }

    private void attachImagesFromRequest(DailyWorkReport report, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        for (String url : imageUrls) {
            if (url == null || url.isBlank()) {
                continue;
            }
            String cleanUrl = url.trim();

            boolean alreadyExists = report.getImages().stream()
                    .anyMatch(img -> cleanUrl.equals(img.getFileUrl()) || cleanUrl.equals(img.getStoredFileName()));

            if (!alreadyExists) {
                String fileName = cleanUrl;
                int lastSlash = cleanUrl.lastIndexOf('/');
                if (lastSlash >= 0 && lastSlash < cleanUrl.length() - 1) {
                    fileName = cleanUrl.substring(lastSlash + 1);
                }

                DailyWorkReportImage image = new DailyWorkReportImage();
                image.setReport(report);
                image.setFileName(fileName);
                image.setStoredFileName(fileName);
                image.setFilePath(cleanUrl);
                image.setFileUrl(cleanUrl);
                image.setCaption("Site photograph");

                report.addImage(image);
            }
        }
    }
}
