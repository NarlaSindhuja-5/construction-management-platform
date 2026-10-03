package com.e2e.construction.service;

import com.e2e.construction.dto.LaborAssignmentResponse;
import com.e2e.construction.dto.LaborRequestResponse;
import com.e2e.construction.dto.LaborWorkRequestCreateDTO;
import com.e2e.construction.dto.LaborerProfileResponse;
import com.e2e.construction.entity.BookingStatus;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.LaborAssignment;
import com.e2e.construction.entity.LaborAssignmentStatus;
import com.e2e.construction.entity.LaborRequest;
import com.e2e.construction.entity.Laborer;
import com.e2e.construction.entity.LaborerAvailability;
import com.e2e.construction.entity.Project;
import com.e2e.construction.entity.User;
import com.e2e.construction.entity.VerificationStatus;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.LaborAssignmentRepository;
import com.e2e.construction.repository.LaborRequestRepository;
import com.e2e.construction.repository.LaborerRepository;
import com.e2e.construction.repository.ProjectRepository;
import com.e2e.construction.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LaborWorkRequestService {

    private final LaborRequestRepository laborRequestRepository;
    private final LaborAssignmentRepository laborAssignmentRepository;
    private final LaborerRepository laborerRepository;
    private final ContractorRepository contractorRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public LaborWorkRequestService(
            LaborRequestRepository laborRequestRepository,
            LaborAssignmentRepository laborAssignmentRepository,
            LaborerRepository laborerRepository,
            ContractorRepository contractorRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository) {
        this.laborRequestRepository = laborRequestRepository;
        this.laborAssignmentRepository = laborAssignmentRepository;
        this.laborerRepository = laborerRepository;
        this.contractorRepository = contractorRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    /**
     * 1. Search verified laborers by location, skill, experience, availability, and daily wage.
     */
    @Transactional(readOnly = true)
    public List<LaborerProfileResponse> searchVerifiedLaborers(
            String location, String skill, Integer minExperience,
            LaborerAvailability availability, BigDecimal maxDailyWage) {

        List<Laborer> laborers = laborerRepository.searchVerifiedLaborers(
                location, skill, minExperience, availability, maxDailyWage);

        if (laborers == null) {
            return Collections.emptyList();
        }

        return laborers.stream()
                .map(LaborerProfileResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 2. View laborer profile by ID.
     */
    @Transactional(readOnly = true)
    public LaborerProfileResponse getLaborerProfile(Long laborerId) {
        Laborer laborer = laborerRepository.findById(laborerId)
                .orElseThrow(() -> new ResourceNotFoundException("Laborer not found with id: " + laborerId));
        return LaborerProfileResponse.fromEntity(laborer);
    }

    /**
     * 3. Contractor selects project and sends work request to verified laborer.
     * Prevents overlapping assignments and verifies eligibility.
     */
    @Transactional
    public LaborRequestResponse sendWorkRequest(Long laborerId, String contractorEmail, LaborWorkRequestCreateDTO requestDTO) {
        if (laborerId == null && requestDTO.getLaborerId() != null) {
            laborerId = requestDTO.getLaborerId();
        }
        if (laborerId == null) {
            throw new BadRequestException("Laborer ID is required.");
        }

        // 1. Verify contractor
        Contractor contractor = contractorRepository.findByUserEmail(contractorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Contractor profile not found for user: " + contractorEmail));

        // 2. Verify laborer
        final Long finalLaborerId = laborerId;
        Laborer laborer = laborerRepository.findById(finalLaborerId)
                .orElseThrow(() -> new ResourceNotFoundException("Laborer not found with id: " + finalLaborerId));

        if (laborer.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BadRequestException("Cannot send work request: Laborer is not verified.");
        }

        // 3. Verify project ownership
        Project project = projectRepository.findById(requestDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + requestDTO.getProjectId()));

        if (!project.getContractor().getId().equals(contractor.getId())) {
            throw new AccessDeniedException("You can only assign laborers to your own projects.");
        }

        // 4. Validate dates
        LocalDate startDate = requestDTO.getStartDate();
        LocalDate endDate = requestDTO.getEndDate();

        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required.");
        }
        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date.");
        }

        // 5. Prevent overlapping assignments for the laborer
        List<LaborAssignment> activeOverlaps = laborAssignmentRepository.findOverlappingActiveAssignments(
                laborer.getId(), startDate, endDate, null);
        if (!activeOverlaps.isEmpty()) {
            throw new BadRequestException("Laborer already has an active assignment for the selected date range ("
                    + activeOverlaps.get(0).getStartDate() + " to " + activeOverlaps.get(0).getEndDate() + ").");
        }

        List<LaborRequest> acceptedOverlaps = laborRequestRepository.findOverlappingAcceptedRequests(
                laborer.getId(), startDate, endDate, null);
        if (!acceptedOverlaps.isEmpty()) {
            throw new BadRequestException("Laborer already has an accepted work request for overlapping dates.");
        }

        // 6. Calculate total days and costs
        int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal dailyWage = requestDTO.getDailyWage() != null ? requestDTO.getDailyWage() : laborer.getDailyWage();
        if (dailyWage == null || dailyWage.compareTo(BigDecimal.ZERO) <= 0) {
            dailyWage = BigDecimal.valueOf(500.00); // sensible default if unspecified
        }
        BigDecimal totalEstimatedCost = dailyWage.multiply(BigDecimal.valueOf(totalDays));

        // 7. Create LaborRequest
        LaborRequest laborRequest = new LaborRequest(
                contractor,
                laborer,
                project,
                startDate,
                endDate,
                totalDays,
                dailyWage,
                totalEstimatedCost,
                requestDTO.getJobDescription(),
                requestDTO.getContractorNotes()
        );

        LaborRequest saved = laborRequestRepository.save(laborRequest);
        return LaborRequestResponse.fromEntity(saved);
    }

    /**
     * 4. Laborer accepts work request.
     * Actions:
     * - Overlap validation
     * - Set request status to ACCEPTED
     * - Create LaborAssignment
     * - Connect laborer to project
     * - Change laborer availability to WORKING
     */
    @Transactional
    public LaborRequestResponse acceptWorkRequest(Long requestId, String laborerEmail, String notes) {
        Laborer laborer = laborerRepository.findByUserEmail(laborerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Laborer profile not found for user: " + laborerEmail));

        LaborRequest request = laborRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Work request not found with id: " + requestId));

        if (!request.getLaborer().getId().equals(laborer.getId())) {
            throw new AccessDeniedException("You are not authorized to accept this work request.");
        }

        if (request.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Cannot accept request in status: " + request.getStatus());
        }

        // Prevent overlapping assignments
        List<LaborAssignment> activeOverlaps = laborAssignmentRepository.findOverlappingActiveAssignments(
                laborer.getId(), request.getStartDate(), request.getEndDate(), null);
        if (!activeOverlaps.isEmpty()) {
            throw new BadRequestException("Cannot accept request: Laborer already has an active overlapping assignment.");
        }

        List<LaborRequest> acceptedOverlaps = laborRequestRepository.findOverlappingAcceptedRequests(
                laborer.getId(), request.getStartDate(), request.getEndDate(), request.getId());
        if (!acceptedOverlaps.isEmpty()) {
            throw new BadRequestException("Cannot accept request: Laborer already has an accepted overlapping request.");
        }

        // Accept request
        request.setStatus(BookingStatus.ACCEPTED);
        if (notes != null && !notes.isBlank()) {
            request.setLaborerNotes(notes);
        }
        LaborRequest savedRequest = laborRequestRepository.save(request);

        // Create LaborAssignment connecting laborer to project
        LaborAssignment assignment = new LaborAssignment(
                savedRequest,
                laborer,
                savedRequest.getProject(),
                savedRequest.getContractor(),
                savedRequest.getStartDate(),
                savedRequest.getEndDate(),
                savedRequest.getDailyWage(),
                notes
        );
        LaborAssignment savedAssignment = laborAssignmentRepository.save(assignment);

        // Update laborer availability status to WORKING
        laborer.setAvailabilityStatus(LaborerAvailability.WORKING);
        laborerRepository.save(laborer);

        return LaborRequestResponse.fromEntity(savedRequest, savedAssignment.getId());
    }

    /**
     * 5. Laborer rejects work request.
     */
    @Transactional
    public LaborRequestResponse rejectWorkRequest(Long requestId, String laborerEmail, String reason) {
        Laborer laborer = laborerRepository.findByUserEmail(laborerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Laborer profile not found for user: " + laborerEmail));

        LaborRequest request = laborRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Work request not found with id: " + requestId));

        if (!request.getLaborer().getId().equals(laborer.getId())) {
            throw new AccessDeniedException("You are not authorized to reject this work request.");
        }

        if (request.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Cannot reject request in status: " + request.getStatus());
        }

        request.setStatus(BookingStatus.REJECTED);
        request.setRejectionReason(reason);
        request.setLaborerNotes(reason);
        LaborRequest saved = laborRequestRepository.save(request);

        return LaborRequestResponse.fromEntity(saved);
    }

    /**
     * 6. Contractor cancels work request.
     */
    @Transactional
    public LaborRequestResponse cancelWorkRequest(Long requestId, String contractorEmail, String reason) {
        Contractor contractor = contractorRepository.findByUserEmail(contractorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Contractor profile not found for user: " + contractorEmail));

        LaborRequest request = laborRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Work request not found with id: " + requestId));

        if (!request.getContractor().getId().equals(contractor.getId())) {
            throw new AccessDeniedException("You are not authorized to cancel this work request.");
        }

        if (request.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Cannot cancel request in status: " + request.getStatus());
        }

        request.setStatus(BookingStatus.CANCELLED);
        request.setContractorNotes(reason != null ? "Cancelled: " + reason : "Cancelled by contractor");
        LaborRequest saved = laborRequestRepository.save(request);

        return LaborRequestResponse.fromEntity(saved);
    }

    /**
     * 7. Complete labor assignment.
     * Updates assignment and request to COMPLETED, and resets laborer availability to AVAILABLE if no other active assignments exist.
     */
    @Transactional
    public LaborAssignmentResponse completeAssignment(Long assignmentId, String userEmail, String notes) {
        LaborAssignment assignment = laborAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // Check if user is either the assigned contractor or laborer
        boolean isContractor = assignment.getContractor().getUser().getId().equals(user.getId());
        boolean isLaborer = assignment.getLaborer().getUser().getId().equals(user.getId());

        if (!isContractor && !isLaborer) {
            throw new AccessDeniedException("You are not authorized to complete this assignment.");
        }

        if (assignment.getStatus() != LaborAssignmentStatus.ACTIVE) {
            throw new BadRequestException("Cannot complete assignment with status: " + assignment.getStatus());
        }

        assignment.setStatus(LaborAssignmentStatus.COMPLETED);
        assignment.setCompletedAt(LocalDateTime.now());
        if (notes != null && !notes.isBlank()) {
            assignment.setNotes(notes);
        }

        // Update associated request
        LaborRequest request = assignment.getLaborRequest();
        if (request != null) {
            request.setStatus(BookingStatus.COMPLETED);
            laborRequestRepository.save(request);
        }

        LaborAssignment savedAssignment = laborAssignmentRepository.save(assignment);

        // Check remaining active assignments for laborer
        Laborer laborer = assignment.getLaborer();
        List<LaborAssignment> activeRemaining = laborAssignmentRepository
                .findByLaborerIdAndStatusOrderByStartDateDesc(laborer.getId(), LaborAssignmentStatus.ACTIVE);

        boolean hasOtherActive = activeRemaining.stream()
                .anyMatch(a -> !a.getId().equals(assignmentId));

        if (!hasOtherActive) {
            laborer.setAvailabilityStatus(LaborerAvailability.AVAILABLE);
            laborerRepository.save(laborer);
        }

        return LaborAssignmentResponse.fromEntity(savedAssignment);
    }

    /**
     * 8. Laborer views their incoming work requests.
     */
    @Transactional(readOnly = true)
    public List<LaborRequestResponse> getLaborerRequests(String laborerEmail, BookingStatus status) {
        Laborer laborer = laborerRepository.findByUserEmail(laborerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Laborer profile not found for user: " + laborerEmail));

        List<LaborRequest> requests = (status != null)
                ? laborRequestRepository.findByLaborerIdAndStatusOrderByCreatedAtDesc(laborer.getId(), status)
                : laborRequestRepository.findByLaborerIdOrderByCreatedAtDesc(laborer.getId());

        return requests.stream()
                .map(r -> {
                    Optional<LaborAssignment> assignment = laborAssignmentRepository.findByLaborRequestId(r.getId());
                    return LaborRequestResponse.fromEntity(r, assignment.map(LaborAssignment::getId).orElse(null));
                })
                .collect(Collectors.toList());
    }

    /**
     * 9. Contractor views their sent work requests.
     */
    @Transactional(readOnly = true)
    public List<LaborRequestResponse> getContractorRequests(String contractorEmail, BookingStatus status) {
        Contractor contractor = contractorRepository.findByUserEmail(contractorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Contractor profile not found for user: " + contractorEmail));

        List<LaborRequest> requests = (status != null)
                ? laborRequestRepository.findByContractorIdAndStatusOrderByCreatedAtDesc(contractor.getId(), status)
                : laborRequestRepository.findByContractorIdOrderByCreatedAtDesc(contractor.getId());

        return requests.stream()
                .map(r -> {
                    Optional<LaborAssignment> assignment = laborAssignmentRepository.findByLaborRequestId(r.getId());
                    return LaborRequestResponse.fromEntity(r, assignment.map(LaborAssignment::getId).orElse(null));
                })
                .collect(Collectors.toList());
    }

    /**
     * 10. View single request by ID.
     */
    @Transactional(readOnly = true)
    public LaborRequestResponse getRequestById(Long requestId, String userEmail) {
        LaborRequest request = laborRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Work request not found with id: " + requestId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        boolean isContractor = request.getContractor().getUser().getId().equals(user.getId());
        boolean isLaborer = request.getLaborer().getUser().getId().equals(user.getId());

        if (!isContractor && !isLaborer) {
            throw new AccessDeniedException("You are not authorized to view this request.");
        }

        Optional<LaborAssignment> assignment = laborAssignmentRepository.findByLaborRequestId(request.getId());
        return LaborRequestResponse.fromEntity(request, assignment.map(LaborAssignment::getId).orElse(null));
    }

    /**
     * 11. View laborer's assignments.
     */
    @Transactional(readOnly = true)
    public List<LaborAssignmentResponse> getLaborerAssignments(String laborerEmail, LaborAssignmentStatus status) {
        Laborer laborer = laborerRepository.findByUserEmail(laborerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Laborer profile not found for user: " + laborerEmail));

        List<LaborAssignment> assignments = (status != null)
                ? laborAssignmentRepository.findByLaborerIdAndStatusOrderByStartDateDesc(laborer.getId(), status)
                : laborAssignmentRepository.findByLaborerIdOrderByStartDateDesc(laborer.getId());

        return assignments.stream()
                .map(LaborAssignmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 12. View assignments for a specific project.
     */
    @Transactional(readOnly = true)
    public List<LaborAssignmentResponse> getProjectAssignments(Long projectId, String contractorEmail) {
        Contractor contractor = contractorRepository.findByUserEmail(contractorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Contractor profile not found for user: " + contractorEmail));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (!project.getContractor().getId().equals(contractor.getId())) {
            throw new AccessDeniedException("You can only view assignments for your own projects.");
        }

        List<LaborAssignment> assignments = laborAssignmentRepository.findByProjectIdOrderByStartDateDesc(projectId);
        return assignments.stream()
                .map(LaborAssignmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 13. View single assignment by ID.
     */
    @Transactional(readOnly = true)
    public LaborAssignmentResponse getAssignmentById(Long assignmentId, String userEmail) {
        LaborAssignment assignment = laborAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        boolean isContractor = assignment.getContractor().getUser().getId().equals(user.getId());
        boolean isLaborer = assignment.getLaborer().getUser().getId().equals(user.getId());

        if (!isContractor && !isLaborer) {
            throw new AccessDeniedException("You are not authorized to view this assignment.");
        }

        return LaborAssignmentResponse.fromEntity(assignment);
    }
}
