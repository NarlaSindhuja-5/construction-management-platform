package com.e2e.construction.service;

import com.e2e.construction.dto.BatchLaborAttendanceItem;
import com.e2e.construction.dto.BatchLaborAttendanceRequest;
import com.e2e.construction.dto.LaborAttendanceRequest;
import com.e2e.construction.dto.LaborAttendanceResponse;
import com.e2e.construction.dto.LaborAttendanceSummaryResponse;
import com.e2e.construction.entity.AttendanceStatus;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.LaborAttendance;
import com.e2e.construction.entity.Laborer;
import com.e2e.construction.entity.Project;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.LaborAttendanceRepository;
import com.e2e.construction.repository.LaborerRepository;
import com.e2e.construction.repository.ProjectRepository;
import com.e2e.construction.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LaborAttendanceService {

    private final LaborAttendanceRepository laborAttendanceRepository;
    private final LaborerRepository laborerRepository;
    private final ProjectRepository projectRepository;
    private final ContractorRepository contractorRepository;
    private final UserRepository userRepository;

    public LaborAttendanceService(
            LaborAttendanceRepository laborAttendanceRepository,
            LaborerRepository laborerRepository,
            ProjectRepository projectRepository,
            ContractorRepository contractorRepository,
            UserRepository userRepository) {
        this.laborAttendanceRepository = laborAttendanceRepository;
        this.laborerRepository = laborerRepository;
        this.projectRepository = projectRepository;
        this.contractorRepository = contractorRepository;
        this.userRepository = userRepository;
    }

    /**
     * Record single labor attendance for a project.
     * Prevents duplicate attendance for the same laborer, project, and date.
     */
    @Transactional
    public LaborAttendanceResponse recordAttendance(String userEmail, LaborAttendanceRequest request) {
        User user = getUserByEmail(userEmail);

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        verifyProjectAuthorization(project, user);

        Laborer laborer = laborerRepository.findById(request.getLaborerId())
                .orElseThrow(() -> new ResourceNotFoundException("Laborer", "id", request.getLaborerId()));

        LocalDate date = request.getDate();

        // Prevent duplicate attendance for the same laborer, project, and date
        if (laborAttendanceRepository.existsByLaborerIdAndProjectIdAndAttendanceDate(laborer.getId(), project.getId(), date)) {
            String laborerName = laborer.getUser() != null
                    ? laborer.getUser().getFirstName() + " " + laborer.getUser().getLastName()
                    : "ID " + laborer.getId();
            throw new BadRequestException("Duplicate attendance: Attendance already recorded for laborer "
                    + laborerName + " on project '" + project.getName() + "' for date " + date);
        }

        Double resolvedWorkingHours = resolveWorkingHours(request.getStatus(), request.getWorkingHours());

        LaborAttendance attendance = new LaborAttendance(
                laborer,
                project,
                date,
                resolvedWorkingHours,
                request.getStatus(),
                request.getRemarks() != null ? request.getRemarks().trim() : null,
                user
        );

        attendance = laborAttendanceRepository.save(attendance);
        return LaborAttendanceResponse.fromEntity(attendance);
    }

    /**
     * Record batch labor attendance for multiple laborers on a project for a specific date.
     * Prevents duplicate attendance for any laborer.
     */
    @Transactional
    public List<LaborAttendanceResponse> recordBatchAttendance(String userEmail, BatchLaborAttendanceRequest request) {
        User user = getUserByEmail(userEmail);

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        verifyProjectAuthorization(project, user);

        LocalDate date = request.getDate();
        List<LaborAttendanceResponse> savedResponses = new ArrayList<>();

        for (BatchLaborAttendanceItem item : request.getAttendances()) {
            Laborer laborer = laborerRepository.findById(item.getLaborerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Laborer", "id", item.getLaborerId()));

            // Check duplicate
            if (laborAttendanceRepository.existsByLaborerIdAndProjectIdAndAttendanceDate(laborer.getId(), project.getId(), date)) {
                String laborerName = laborer.getUser() != null
                        ? laborer.getUser().getFirstName() + " " + laborer.getUser().getLastName()
                        : "ID " + laborer.getId();
                throw new BadRequestException("Duplicate attendance in batch: Attendance already recorded for laborer "
                        + laborerName + " on project '" + project.getName() + "' for date " + date);
            }

            Double hours = resolveWorkingHours(item.getStatus(), item.getWorkingHours());

            LaborAttendance attendance = new LaborAttendance(
                    laborer,
                    project,
                    date,
                    hours,
                    item.getStatus(),
                    item.getRemarks() != null ? item.getRemarks().trim() : null,
                    user
            );

            attendance = laborAttendanceRepository.save(attendance);
            savedResponses.add(LaborAttendanceResponse.fromEntity(attendance));
        }

        return savedResponses;
    }

    /**
     * View attendance record by ID.
     */
    @Transactional(readOnly = true)
    public LaborAttendanceResponse getAttendanceById(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);

        LaborAttendance attendance = laborAttendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LaborAttendance", "id", id));

        verifyViewAuthorization(attendance, user);

        return LaborAttendanceResponse.fromEntity(attendance);
    }

    /**
     * Update an existing attendance record.
     */
    @Transactional
    public LaborAttendanceResponse updateAttendance(Long id, String userEmail, LaborAttendanceRequest request) {
        User user = getUserByEmail(userEmail);

        LaborAttendance attendance = laborAttendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LaborAttendance", "id", id));

        verifyProjectAuthorization(attendance.getProject(), user);

        Long targetLaborerId = request.getLaborerId() != null ? request.getLaborerId() : attendance.getLaborer().getId();
        Long targetProjectId = request.getProjectId() != null ? request.getProjectId() : attendance.getProject().getId();
        LocalDate targetDate = request.getDate() != null ? request.getDate() : attendance.getAttendanceDate();

        // Verify duplicate if laborer, project, or date changed
        if (laborAttendanceRepository.existsByLaborerIdAndProjectIdAndAttendanceDateAndIdNot(targetLaborerId, targetProjectId, targetDate, id)) {
            throw new BadRequestException("Another attendance record already exists for laborer ID "
                    + targetLaborerId + " on project ID " + targetProjectId + " for date " + targetDate);
        }

        if (request.getLaborerId() != null && !request.getLaborerId().equals(attendance.getLaborer().getId())) {
            Laborer newLaborer = laborerRepository.findById(request.getLaborerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Laborer", "id", request.getLaborerId()));
            attendance.setLaborer(newLaborer);
        }

        if (request.getProjectId() != null && !request.getProjectId().equals(attendance.getProject().getId())) {
            Project newProject = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));
            verifyProjectAuthorization(newProject, user);
            attendance.setProject(newProject);
        }

        if (request.getDate() != null) {
            attendance.setAttendanceDate(request.getDate());
        }

        if (request.getStatus() != null) {
            attendance.setStatus(request.getStatus());
        }

        attendance.setWorkingHours(resolveWorkingHours(attendance.getStatus(), request.getWorkingHours()));

        if (request.getRemarks() != null) {
            attendance.setRemarks(request.getRemarks().trim());
        }

        attendance = laborAttendanceRepository.save(attendance);
        return LaborAttendanceResponse.fromEntity(attendance);
    }

    /**
     * View attendance history with filtering options.
     * Authorized contractors can view attendance history for their projects.
     */
    @Transactional(readOnly = true)
    public List<LaborAttendanceResponse> getAttendanceHistory(
            String userEmail,
            Long projectId,
            Long laborerId,
            LocalDate date,
            LocalDate startDate,
            LocalDate endDate,
            AttendanceStatus status) {

        User user = getUserByEmail(userEmail);
        String roleName = user.getRole().getName().toUpperCase();

        Long contractorFilterId = null;

        // Contractors can only view attendance for their own projects
        if ("CONTRACTOR".equals(roleName)) {
            Optional<Contractor> contractorOpt = contractorRepository.findByUserId(user.getId());
            if (contractorOpt.isEmpty()) {
                return List.of();
            }
            Contractor contractor = contractorOpt.get();
            contractorFilterId = contractor.getId();

            if (projectId != null) {
                Project project = projectRepository.findById(projectId)
                        .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
                if (!project.getContractor().getId().equals(contractor.getId())) {
                    throw new AccessDeniedException("Access denied: You can only view attendance for your own projects.");
                }
            }
        } else if ("LABORER".equals(roleName)) {
            // Laborers can only view their own attendance
            Optional<Laborer> laborerOpt = laborerRepository.findByUserId(user.getId());
            if (laborerOpt.isEmpty()) {
                return List.of();
            }
            laborerId = laborerOpt.get().getId();
        } else if (!"ADMIN".equals(roleName) && !"SITE_MANAGER".equals(roleName)) {
            throw new AccessDeniedException("Access denied: You do not have permission to view attendance history.");
        }

        return laborAttendanceRepository.filterAttendance(projectId, contractorFilterId, laborerId, date, startDate, endDate, status)
                .stream()
                .map(LaborAttendanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get attendance summary statistics for a project and date/range.
     */
    @Transactional(readOnly = true)
    public LaborAttendanceSummaryResponse getAttendanceSummary(
            String userEmail,
            Long projectId,
            LocalDate date,
            LocalDate startDate,
            LocalDate endDate) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        User user = getUserByEmail(userEmail);
        String roleName = user.getRole().getName().toUpperCase();

        if ("CONTRACTOR".equals(roleName)) {
            if (project.getContractor() == null ||
                    project.getContractor().getUser() == null ||
                    !project.getContractor().getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("Access denied: You can only view attendance summaries for your own projects.");
            }
        } else if (!"ADMIN".equals(roleName) && !"SITE_MANAGER".equals(roleName)) {
            throw new AccessDeniedException("Access denied: You do not have permission to view attendance summaries.");
        }

        List<LaborAttendance> records = laborAttendanceRepository.filterAttendance(
                projectId, null, null, date, startDate, endDate, null
        );

        int total = records.size();
        int present = 0;
        int absent = 0;
        int halfDay = 0;
        int overtime = 0;
        double totalHours = 0.0;

        for (LaborAttendance r : records) {
            totalHours += r.getWorkingHours() != null ? r.getWorkingHours() : 0.0;
            switch (r.getStatus()) {
                case PRESENT -> present++;
                case ABSENT -> absent++;
                case HALF_DAY -> halfDay++;
                case OVERTIME -> overtime++;
            }
        }

        return new LaborAttendanceSummaryResponse(
                project.getId(),
                project.getName(),
                date,
                startDate,
                endDate,
                total,
                present,
                absent,
                halfDay,
                overtime,
                Math.round(totalHours * 100.0) / 100.0
        );
    }

    /**
     * Delete an attendance record.
     */
    @Transactional
    public void deleteAttendance(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);

        LaborAttendance attendance = laborAttendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LaborAttendance", "id", id));

        verifyProjectAuthorization(attendance.getProject(), user);

        laborAttendanceRepository.delete(attendance);
    }

    // =========================================================================
    // Helper Methods & Authorization
    // =========================================================================

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

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
            throw new AccessDeniedException("Access denied: You can only record attendance for your own projects.");
        }

        throw new AccessDeniedException("Access denied: Only authorized contractors or site managers can manage labor attendance.");
    }

    private void verifyViewAuthorization(LaborAttendance attendance, User user) {
        String roleName = user.getRole().getName().toUpperCase();

        if ("ADMIN".equals(roleName) || "SITE_MANAGER".equals(roleName)) {
            return;
        }

        if ("CONTRACTOR".equals(roleName)) {
            Project project = attendance.getProject();
            if (project.getContractor() != null &&
                    project.getContractor().getUser() != null &&
                    project.getContractor().getUser().getId().equals(user.getId())) {
                return;
            }
            throw new AccessDeniedException("Access denied: You can only view attendance for your own projects.");
        }

        if ("LABORER".equals(roleName)) {
            if (attendance.getLaborer() != null &&
                    attendance.getLaborer().getUser() != null &&
                    attendance.getLaborer().getUser().getId().equals(user.getId())) {
                return;
            }
            throw new AccessDeniedException("Access denied: You can only view your own attendance records.");
        }

        throw new AccessDeniedException("Access denied: You do not have permission to view this attendance record.");
    }

    private Double resolveWorkingHours(AttendanceStatus status, Double suppliedHours) {
        if (suppliedHours != null && suppliedHours >= 0.0) {
            return suppliedHours;
        }

        return switch (status) {
            case PRESENT -> 8.0;
            case HALF_DAY -> 4.0;
            case ABSENT -> 0.0;
            case OVERTIME -> 10.0;
        };
    }
}
