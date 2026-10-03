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
import com.e2e.construction.entity.ProjectType;
import com.e2e.construction.entity.Role;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LaborWorkRequestServiceTest {

    @Mock
    private LaborRequestRepository laborRequestRepository;

    @Mock
    private LaborAssignmentRepository laborAssignmentRepository;

    @Mock
    private LaborerRepository laborerRepository;

    @Mock
    private ContractorRepository contractorRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    private LaborWorkRequestService service;

    private User contractorUser;
    private User laborerUser;
    private Contractor contractor;
    private Laborer verifiedLaborer;
    private Laborer unverifiedLaborer;
    private Project project;

    @BeforeEach
    void setUp() {
        service = new LaborWorkRequestService(
                laborRequestRepository,
                laborAssignmentRepository,
                laborerRepository,
                contractorRepository,
                projectRepository,
                userRepository
        );

        // Contractor User & Entity
        Role contractorRole = new Role("CONTRACTOR", "Contractor role");
        contractorUser = new User();
        contractorUser.setId(10L);
        contractorUser.setEmail("contractor@example.com");
        contractorUser.setFirstName("Bob");
        contractorUser.setLastName("Builder");
        contractorUser.setPhoneNumber("+1-555-1001");
        contractorUser.setRole(contractorRole);

        contractor = new Contractor();
        contractor.setId(100L);
        contractor.setUser(contractorUser);
        contractor.setCompanyName("Apex Builders Inc");
        contractor.setLicenseNumber("LIC-9988");
        contractor.setAddress("Austin, TX");
        contractor.setCity("Austin");
        contractor.setState("TX");

        // Project
        project = new Project();
        project.setId(500L);
        project.setContractor(contractor);
        project.setName("Downtown Tower");
        project.setProjectType(ProjectType.COMMERCIAL);
        project.setClientName("Apex Real Estate");
        project.setLocation("400 Congress Ave");
        project.setCity("Austin");
        project.setState("TX");
        project.setStartDate(LocalDate.now().plusDays(1));
        project.setExpectedCompletionDate(LocalDate.now().plusMonths(6));
        project.setBudget(BigDecimal.valueOf(1500000.00));
        project.setDescription("30-story commercial tower");

        // Laborer User & Entity
        Role laborerRole = new Role("LABORER", "Laborer role");
        laborerUser = new User();
        laborerUser.setId(20L);
        laborerUser.setEmail("laborer.john@example.com");
        laborerUser.setFirstName("John");
        laborerUser.setLastName("Doe");
        laborerUser.setPhoneNumber("+1-555-2002");
        laborerUser.setRole(laborerRole);

        verifiedLaborer = new Laborer();
        verifiedLaborer.setId(200L);
        verifiedLaborer.setUser(laborerUser);
        verifiedLaborer.setLocation("Austin, TX");
        verifiedLaborer.setCity("Austin");
        verifiedLaborer.setState("TX");
        verifiedLaborer.setSkills("Masonry, Carpentry");
        verifiedLaborer.setYearsOfExperience(5);
        verifiedLaborer.setDailyWage(BigDecimal.valueOf(250.00));
        verifiedLaborer.setDescription("Experienced mason and framer");
        verifiedLaborer.setVerificationStatus(VerificationStatus.VERIFIED);
        verifiedLaborer.setAvailabilityStatus(LaborerAvailability.AVAILABLE);

        unverifiedLaborer = new Laborer();
        unverifiedLaborer.setId(201L);
        unverifiedLaborer.setUser(laborerUser);
        unverifiedLaborer.setLocation("Dallas, TX");
        unverifiedLaborer.setCity("Dallas");
        unverifiedLaborer.setState("TX");
        unverifiedLaborer.setSkills("Plumbing");
        unverifiedLaborer.setYearsOfExperience(2);
        unverifiedLaborer.setDailyWage(BigDecimal.valueOf(180.00));
        unverifiedLaborer.setVerificationStatus(VerificationStatus.PENDING);
        unverifiedLaborer.setAvailabilityStatus(LaborerAvailability.AVAILABLE);
    }

    @Test
    @DisplayName("Search verified laborers by filters returns matching laborers")
    void testSearchVerifiedLaborers() {
        when(laborerRepository.searchVerifiedLaborers(
                eq("Austin"), eq("Masonry"), eq(3), eq(LaborerAvailability.AVAILABLE), eq(BigDecimal.valueOf(300.00))))
                .thenReturn(List.of(verifiedLaborer));

        List<LaborerProfileResponse> result = service.searchVerifiedLaborers(
                "Austin", "Masonry", 3, LaborerAvailability.AVAILABLE, BigDecimal.valueOf(300.00));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getFullName());
        assertEquals(BigDecimal.valueOf(250.00), result.get(0).getDailyWage());
        assertEquals(VerificationStatus.VERIFIED, result.get(0).getVerificationStatus());
    }

    @Test
    @DisplayName("View laborer profile by ID succeeds")
    void testGetLaborerProfileSuccess() {
        when(laborerRepository.findById(200L)).thenReturn(Optional.of(verifiedLaborer));

        LaborerProfileResponse profile = service.getLaborerProfile(200L);

        assertNotNull(profile);
        assertEquals(200L, profile.getId());
        assertEquals("John Doe", profile.getFullName());
    }

    @Test
    @DisplayName("View laborer profile fails when ID not found")
    void testGetLaborerProfileNotFound() {
        when(laborerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getLaborerProfile(999L));
    }

    @Test
    @DisplayName("Contractor sends work request successfully")
    void testSendWorkRequestSuccess() {
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(6); // 5 days

        LaborWorkRequestCreateDTO dto = new LaborWorkRequestCreateDTO();
        dto.setProjectId(500L);
        dto.setStartDate(start);
        dto.setEndDate(end);
        dto.setJobDescription("Foundation masonry and brick laying");
        dto.setContractorNotes("Safety boots and hard hat required");

        when(contractorRepository.findByUserEmail(contractorUser.getEmail())).thenReturn(Optional.of(contractor));
        when(laborerRepository.findById(200L)).thenReturn(Optional.of(verifiedLaborer));
        when(projectRepository.findById(500L)).thenReturn(Optional.of(project));
        when(laborAssignmentRepository.findOverlappingActiveAssignments(200L, start, end, null))
                .thenReturn(Collections.emptyList());
        when(laborRequestRepository.findOverlappingAcceptedRequests(200L, start, end, null))
                .thenReturn(Collections.emptyList());
        when(laborRequestRepository.save(any(LaborRequest.class))).thenAnswer(invocation -> {
            LaborRequest req = invocation.getArgument(0);
            req.setId(1000L);
            return req;
        });

        LaborRequestResponse response = service.sendWorkRequest(200L, contractorUser.getEmail(), dto);

        assertNotNull(response);
        assertEquals(1000L, response.getId());
        assertEquals(500L, response.getProjectId());
        assertEquals(200L, response.getLaborerId());
        assertEquals(100L, response.getContractorId());
        assertEquals(5, response.getTotalDays());
        assertEquals(BigDecimal.valueOf(250.00), response.getDailyWage());
        assertEquals(BigDecimal.valueOf(1250.00), response.getTotalEstimatedCost());
        assertEquals(BookingStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("Contractor cannot send work request to unverified laborer")
    void testSendWorkRequestFailsUnverifiedLaborer() {
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(4);

        LaborWorkRequestCreateDTO dto = new LaborWorkRequestCreateDTO();
        dto.setProjectId(500L);
        dto.setStartDate(start);
        dto.setEndDate(end);
        dto.setJobDescription("General plumbing");

        when(contractorRepository.findByUserEmail(contractorUser.getEmail())).thenReturn(Optional.of(contractor));
        when(laborerRepository.findById(201L)).thenReturn(Optional.of(unverifiedLaborer));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.sendWorkRequest(201L, contractorUser.getEmail(), dto));
        assertTrue(ex.getMessage().contains("Laborer is not verified"));
    }

    @Test
    @DisplayName("Contractor cannot assign laborer to a project owned by another contractor")
    void testSendWorkRequestFailsForeignProject() {
        Contractor otherContractor = new Contractor();
        otherContractor.setId(999L);

        Project foreignProject = new Project();
        foreignProject.setId(777L);
        foreignProject.setContractor(otherContractor);
        foreignProject.setName("Foreign Project");

        LaborWorkRequestCreateDTO dto = new LaborWorkRequestCreateDTO();
        dto.setProjectId(777L);
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(5));
        dto.setJobDescription("Carpentry work");

        when(contractorRepository.findByUserEmail(contractorUser.getEmail())).thenReturn(Optional.of(contractor));
        when(laborerRepository.findById(200L)).thenReturn(Optional.of(verifiedLaborer));
        when(projectRepository.findById(777L)).thenReturn(Optional.of(foreignProject));

        assertThrows(AccessDeniedException.class, () ->
                service.sendWorkRequest(200L, contractorUser.getEmail(), dto));
    }

    @Test
    @DisplayName("Contractor cannot send work request with invalid date range")
    void testSendWorkRequestFailsInvalidDates() {
        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = LocalDate.now().plusDays(5); // end before start

        LaborWorkRequestCreateDTO dto = new LaborWorkRequestCreateDTO();
        dto.setProjectId(500L);
        dto.setStartDate(start);
        dto.setEndDate(end);
        dto.setJobDescription("Masonry");

        when(contractorRepository.findByUserEmail(contractorUser.getEmail())).thenReturn(Optional.of(contractor));
        when(laborerRepository.findById(200L)).thenReturn(Optional.of(verifiedLaborer));
        when(projectRepository.findById(500L)).thenReturn(Optional.of(project));

        assertThrows(BadRequestException.class, () ->
                service.sendWorkRequest(200L, contractorUser.getEmail(), dto));
    }

    @Test
    @DisplayName("Prevent overlapping work request if laborer already has active assignment")
    void testSendWorkRequestFailsOverlappingAssignment() {
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(10);

        LaborAssignment existingAssignment = new LaborAssignment();
        existingAssignment.setId(88L);
        existingAssignment.setStartDate(LocalDate.now().plusDays(1));
        existingAssignment.setEndDate(LocalDate.now().plusDays(5));
        existingAssignment.setStatus(LaborAssignmentStatus.ACTIVE);

        LaborWorkRequestCreateDTO dto = new LaborWorkRequestCreateDTO();
        dto.setProjectId(500L);
        dto.setStartDate(start);
        dto.setEndDate(end);
        dto.setJobDescription("Masonry");

        when(contractorRepository.findByUserEmail(contractorUser.getEmail())).thenReturn(Optional.of(contractor));
        when(laborerRepository.findById(200L)).thenReturn(Optional.of(verifiedLaborer));
        when(projectRepository.findById(500L)).thenReturn(Optional.of(project));
        when(laborAssignmentRepository.findOverlappingActiveAssignments(200L, start, end, null))
                .thenReturn(List.of(existingAssignment));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.sendWorkRequest(200L, contractorUser.getEmail(), dto));
        assertTrue(ex.getMessage().contains("active assignment for the selected date range"));
    }

    @Test
    @DisplayName("Laborer accepts work request: creates assignment, connects to project, updates availability to WORKING")
    void testAcceptWorkRequestSuccess() {
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(6);

        LaborRequest request = new LaborRequest(
                contractor, verifiedLaborer, project,
                start, end, 5, BigDecimal.valueOf(250.00), BigDecimal.valueOf(1250.00),
                "Masonry works", "Wear safety gear"
        );
        request.setId(5000L);
        request.setStatus(BookingStatus.PENDING);

        when(laborerRepository.findByUserEmail(laborerUser.getEmail())).thenReturn(Optional.of(verifiedLaborer));
        when(laborRequestRepository.findById(5000L)).thenReturn(Optional.of(request));
        when(laborAssignmentRepository.findOverlappingActiveAssignments(200L, start, end, null))
                .thenReturn(Collections.emptyList());
        when(laborRequestRepository.findOverlappingAcceptedRequests(200L, start, end, 5000L))
                .thenReturn(Collections.emptyList());
        when(laborRequestRepository.save(any(LaborRequest.class))).thenAnswer(i -> i.getArgument(0));
        when(laborAssignmentRepository.save(any(LaborAssignment.class))).thenAnswer(invocation -> {
            LaborAssignment assignment = invocation.getArgument(0);
            assignment.setId(7000L);
            return assignment;
        });

        LaborRequestResponse response = service.acceptWorkRequest(5000L, laborerUser.getEmail(), "Confirmed. Ready to start.");

        assertNotNull(response);
        assertEquals(BookingStatus.ACCEPTED, response.getStatus());
        assertEquals(7000L, response.getAssignmentId());
        assertEquals("Confirmed. Ready to start.", response.getLaborerNotes());

        // Verify laborer availability changed to WORKING
        assertEquals(LaborerAvailability.WORKING, verifiedLaborer.getAvailabilityStatus());
        verify(laborerRepository).save(verifiedLaborer);

        // Verify labor assignment saved with connection to project
        verify(laborAssignmentRepository).save(argThat(assignment ->
                assignment.getProject().getId().equals(500L) &&
                assignment.getLaborer().getId().equals(200L) &&
                assignment.getStatus() == LaborAssignmentStatus.ACTIVE &&
                assignment.getStartDate().equals(start)
        ));
    }

    @Test
    @DisplayName("Prevent overlapping assignment acceptance (double booking)")
    void testAcceptWorkRequestFailsOverlappingCollision() {
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(6);

        LaborRequest request = new LaborRequest(
                contractor, verifiedLaborer, project,
                start, end, 5, BigDecimal.valueOf(250.00), BigDecimal.valueOf(1250.00),
                "Masonry works", null
        );
        request.setId(5000L);
        request.setStatus(BookingStatus.PENDING);

        LaborAssignment activeCollision = new LaborAssignment();
        activeCollision.setId(999L);
        activeCollision.setStatus(LaborAssignmentStatus.ACTIVE);

        when(laborerRepository.findByUserEmail(laborerUser.getEmail())).thenReturn(Optional.of(verifiedLaborer));
        when(laborRequestRepository.findById(5000L)).thenReturn(Optional.of(request));
        when(laborAssignmentRepository.findOverlappingActiveAssignments(200L, start, end, null))
                .thenReturn(List.of(activeCollision));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.acceptWorkRequest(5000L, laborerUser.getEmail(), "Accepting"));
        assertTrue(ex.getMessage().contains("overlapping"));
        verify(laborAssignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Laborer rejects work request with reason")
    void testRejectWorkRequestSuccess() {
        LaborRequest request = new LaborRequest(
                contractor, verifiedLaborer, project,
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(5), 4,
                BigDecimal.valueOf(250.00), BigDecimal.valueOf(1000.00),
                "Masonry", null
        );
        request.setId(5001L);
        request.setStatus(BookingStatus.PENDING);

        when(laborerRepository.findByUserEmail(laborerUser.getEmail())).thenReturn(Optional.of(verifiedLaborer));
        when(laborRequestRepository.findById(5001L)).thenReturn(Optional.of(request));
        when(laborRequestRepository.save(any(LaborRequest.class))).thenAnswer(i -> i.getArgument(0));

        LaborRequestResponse response = service.rejectWorkRequest(5001L, laborerUser.getEmail(), "Unavailable on those dates.");

        assertNotNull(response);
        assertEquals(BookingStatus.REJECTED, response.getStatus());
        assertEquals("Unavailable on those dates.", response.getRejectionReason());
        verify(laborAssignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Contractor cancels pending work request")
    void testCancelWorkRequestSuccess() {
        LaborRequest request = new LaborRequest(
                contractor, verifiedLaborer, project,
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(5), 4,
                BigDecimal.valueOf(250.00), BigDecimal.valueOf(1000.00),
                "Masonry", null
        );
        request.setId(5002L);
        request.setStatus(BookingStatus.PENDING);

        when(contractorRepository.findByUserEmail(contractorUser.getEmail())).thenReturn(Optional.of(contractor));
        when(laborRequestRepository.findById(5002L)).thenReturn(Optional.of(request));
        when(laborRequestRepository.save(any(LaborRequest.class))).thenAnswer(i -> i.getArgument(0));

        LaborRequestResponse response = service.cancelWorkRequest(5002L, contractorUser.getEmail(), "Project postponed");

        assertNotNull(response);
        assertEquals(BookingStatus.CANCELLED, response.getStatus());
    }

    @Test
    @DisplayName("Complete assignment: sets COMPLETED, completes request, restores laborer availability to AVAILABLE")
    void testCompleteAssignmentSuccess() {
        verifiedLaborer.setAvailabilityStatus(LaborerAvailability.WORKING);

        LaborRequest request = new LaborRequest(
                contractor, verifiedLaborer, project,
                LocalDate.now().minusDays(5), LocalDate.now().minusDays(1), 5,
                BigDecimal.valueOf(250.00), BigDecimal.valueOf(1250.00),
                "Masonry", null
        );
        request.setId(5003L);
        request.setStatus(BookingStatus.ACCEPTED);

        LaborAssignment assignment = new LaborAssignment(
                request, verifiedLaborer, project, contractor,
                LocalDate.now().minusDays(5), LocalDate.now().minusDays(1),
                BigDecimal.valueOf(250.00), null
        );
        assignment.setId(8000L);
        assignment.setStatus(LaborAssignmentStatus.ACTIVE);

        when(laborAssignmentRepository.findById(8000L)).thenReturn(Optional.of(assignment));
        when(userRepository.findByEmail(contractorUser.getEmail())).thenReturn(Optional.of(contractorUser));
        when(laborAssignmentRepository.save(any(LaborAssignment.class))).thenAnswer(i -> i.getArgument(0));
        when(laborAssignmentRepository.findByLaborerIdAndStatusOrderByStartDateDesc(200L, LaborAssignmentStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        LaborAssignmentResponse response = service.completeAssignment(8000L, contractorUser.getEmail(), "Great job done on time");

        assertNotNull(response);
        assertEquals(LaborAssignmentStatus.COMPLETED, response.getStatus());
        assertEquals(BookingStatus.COMPLETED, request.getStatus());
        assertEquals(LaborerAvailability.AVAILABLE, verifiedLaborer.getAvailabilityStatus());
        verify(laborerRepository).save(verifiedLaborer);
    }
}
