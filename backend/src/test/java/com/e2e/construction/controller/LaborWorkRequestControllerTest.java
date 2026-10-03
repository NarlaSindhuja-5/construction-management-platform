package com.e2e.construction.controller;

import com.e2e.construction.dto.LaborWorkRequestCreateDTO;
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
import com.e2e.construction.exception.GlobalExceptionHandler;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.LaborAssignmentRepository;
import com.e2e.construction.repository.LaborRequestRepository;
import com.e2e.construction.repository.LaborerRepository;
import com.e2e.construction.repository.ProjectRepository;
import com.e2e.construction.repository.UserRepository;
import com.e2e.construction.service.LaborWorkRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LaborWorkRequestControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

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

    private LaborWorkRequestService laborWorkRequestService;
    private LaborWorkRequestController controller;

    // Two different user accounts
    private final Principal contractorPrincipal = () -> "contractor.bob@builder.com";
    private final Principal laborerPrincipal = () -> "laborer.john@trades.com";

    private User contractorUser;
    private User laborerUser;
    private Contractor contractor;
    private Laborer verifiedLaborer;
    private Project project;

    @BeforeEach
    void setUp() {
        laborWorkRequestService = new LaborWorkRequestService(
                laborRequestRepository,
                laborAssignmentRepository,
                laborerRepository,
                contractorRepository,
                projectRepository,
                userRepository
        );

        controller = new LaborWorkRequestController(laborWorkRequestService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Contractor entity
        Role contractorRole = new Role("CONTRACTOR", "Contractor role");
        contractorUser = new User();
        contractorUser.setId(10L);
        contractorUser.setEmail("contractor.bob@builder.com");
        contractorUser.setFirstName("Bob");
        contractorUser.setLastName("Builder");
        contractorUser.setRole(contractorRole);

        contractor = new Contractor();
        contractor.setId(100L);
        contractor.setUser(contractorUser);
        contractor.setCompanyName("Apex Builders Inc");

        // Project
        project = new Project();
        project.setId(500L);
        project.setName("Austin Commercial Center");
        project.setContractor(contractor);
        project.setLocation("Austin Downtown");
        project.setProjectType(ProjectType.COMMERCIAL);

        // Laborer entity
        Role laborerRole = new Role("LABORER", "Laborer role");
        laborerUser = new User();
        laborerUser.setId(20L);
        laborerUser.setEmail("laborer.john@trades.com");
        laborerUser.setFirstName("John");
        laborerUser.setLastName("Doe");
        laborerUser.setRole(laborerRole);

        verifiedLaborer = new Laborer();
        verifiedLaborer.setId(200L);
        verifiedLaborer.setUser(laborerUser);
        verifiedLaborer.setLocation("Austin, TX");
        verifiedLaborer.setSkills("Masonry, Bricklaying");
        verifiedLaborer.setYearsOfExperience(6);
        verifiedLaborer.setDailyWage(BigDecimal.valueOf(280.00));
        verifiedLaborer.setVerificationStatus(VerificationStatus.VERIFIED);
        verifiedLaborer.setAvailabilityStatus(LaborerAvailability.AVAILABLE);
    }

    @Test
    @DisplayName("Step 1: Contractor searches verified laborers by skill, location, availability, experience")
    void testWorkflow_Step1_SearchVerifiedLaborers() throws Exception {
        when(laborerRepository.searchVerifiedLaborers(
                eq("Austin"), eq("Masonry"), eq(5), eq(LaborerAvailability.AVAILABLE), any(BigDecimal.class)))
                .thenReturn(List.of(verifiedLaborer));

        mockMvc.perform(get("/api/laborers/search")
                        .principal(contractorPrincipal)
                        .param("location", "Austin")
                        .param("skill", "Masonry")
                        .param("minExperience", "5")
                        .param("availability", "AVAILABLE")
                        .param("maxDailyWage", "300.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName", is("John Doe")))
                .andExpect(jsonPath("$[0].skills", containsString("Masonry")))
                .andExpect(jsonPath("$[0].verificationStatus", is("VERIFIED")));
    }

    @Test
    @DisplayName("Step 2: Contractor views laborer profile")
    void testWorkflow_Step2_ViewLaborerProfile() throws Exception {
        when(laborerRepository.findById(200L)).thenReturn(Optional.of(verifiedLaborer));

        mockMvc.perform(get("/api/laborers/200/profile")
                        .principal(contractorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(200)))
                .andExpect(jsonPath("$.fullName", is("John Doe")));
    }

    @Test
    @DisplayName("Step 3: Contractor selects project and sends work request to laborer")
    void testWorkflow_Step3_SendWorkRequest() throws Exception {
        LocalDate start = LocalDate.now().plusDays(3);
        LocalDate end = LocalDate.now().plusDays(7); // 5 days

        LaborWorkRequestCreateDTO requestDTO = new LaborWorkRequestCreateDTO();
        requestDTO.setProjectId(500L);
        requestDTO.setStartDate(start);
        requestDTO.setEndDate(end);
        requestDTO.setDailyWage(BigDecimal.valueOf(280.00));
        requestDTO.setJobDescription("Construct exterior stone masonry walls");
        requestDTO.setContractorNotes("Bring OSHA approved PPE");

        when(contractorRepository.findByUserEmail("contractor.bob@builder.com")).thenReturn(Optional.of(contractor));
        when(laborerRepository.findById(200L)).thenReturn(Optional.of(verifiedLaborer));
        when(projectRepository.findById(500L)).thenReturn(Optional.of(project));
        when(laborAssignmentRepository.findOverlappingActiveAssignments(200L, start, end, null))
                .thenReturn(Collections.emptyList());
        when(laborRequestRepository.findOverlappingAcceptedRequests(200L, start, end, null))
                .thenReturn(Collections.emptyList());
        when(laborRequestRepository.save(any(LaborRequest.class))).thenAnswer(invocation -> {
            LaborRequest req = invocation.getArgument(0);
            req.setId(1001L);
            return req;
        });

        mockMvc.perform(post("/api/laborers/200/work-requests")
                        .principal(contractorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1001)))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.totalDays", is(5)))
                .andExpect(jsonPath("$.totalEstimatedCost", is(1400.00)));
    }

    @Test
    @DisplayName("Step 4: Laborer receives and views pending work requests")
    void testWorkflow_Step4_LaborerViewsRequests() throws Exception {
        LaborRequest request = new LaborRequest(
                contractor, verifiedLaborer, project,
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(7),
                5, BigDecimal.valueOf(280.00), BigDecimal.valueOf(1400.00),
                "Masonry works", "PPE required"
        );
        request.setId(1001L);
        request.setStatus(BookingStatus.PENDING);

        when(laborerRepository.findByUserEmail("laborer.john@trades.com")).thenReturn(Optional.of(verifiedLaborer));
        when(laborRequestRepository.findByLaborerIdAndStatusOrderByCreatedAtDesc(200L, BookingStatus.PENDING))
                .thenReturn(List.of(request));
        when(laborAssignmentRepository.findByLaborRequestId(1001L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/labor-requests/my-requests")
                        .principal(laborerPrincipal)
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1001)))
                .andExpect(jsonPath("$[0].status", is("PENDING")));
    }

    @Test
    @DisplayName("Step 5: Laborer accepts work request -> Creates assignment, connects to project, sets WORKING")
    void testWorkflow_Step5_LaborerAcceptsRequest() throws Exception {
        LocalDate start = LocalDate.now().plusDays(3);
        LocalDate end = LocalDate.now().plusDays(7);

        LaborRequest request = new LaborRequest(
                contractor, verifiedLaborer, project,
                start, end, 5, BigDecimal.valueOf(280.00), BigDecimal.valueOf(1400.00),
                "Masonry works", "PPE required"
        );
        request.setId(1001L);
        request.setStatus(BookingStatus.PENDING);

        when(laborerRepository.findByUserEmail("laborer.john@trades.com")).thenReturn(Optional.of(verifiedLaborer));
        when(laborRequestRepository.findById(1001L)).thenReturn(Optional.of(request));
        when(laborAssignmentRepository.findOverlappingActiveAssignments(200L, start, end, null))
                .thenReturn(Collections.emptyList());
        when(laborRequestRepository.findOverlappingAcceptedRequests(200L, start, end, 1001L))
                .thenReturn(Collections.emptyList());
        when(laborRequestRepository.save(any(LaborRequest.class))).thenAnswer(i -> i.getArgument(0));
        when(laborAssignmentRepository.save(any(LaborAssignment.class))).thenAnswer(i -> {
            LaborAssignment a = i.getArgument(0);
            a.setId(7001L);
            return a;
        });

        String payload = "{\"notes\":\"Ready to start on scheduled date.\"}";

        mockMvc.perform(put("/api/labor-requests/1001/accept")
                        .principal(laborerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1001)))
                .andExpect(jsonPath("$.status", is("ACCEPTED")))
                .andExpect(jsonPath("$.assignmentId", is(7001)));
    }

    @Test
    @DisplayName("Step 6: Prevent overlapping assignments: Attempt to book overlapping dates returns 400 Bad Request")
    void testWorkflow_Step6_PreventOverlappingAssignments() throws Exception {
        LocalDate start = LocalDate.now().plusDays(4);
        LocalDate end = LocalDate.now().plusDays(8);

        LaborAssignment existing = new LaborAssignment();
        existing.setId(99L);
        existing.setStartDate(LocalDate.now().plusDays(3));
        existing.setEndDate(LocalDate.now().plusDays(7));
        existing.setStatus(LaborAssignmentStatus.ACTIVE);

        LaborWorkRequestCreateDTO conflictDTO = new LaborWorkRequestCreateDTO();
        conflictDTO.setProjectId(500L);
        conflictDTO.setStartDate(start);
        conflictDTO.setEndDate(end);
        conflictDTO.setJobDescription("Overlapping masonry work");

        when(contractorRepository.findByUserEmail("contractor.bob@builder.com")).thenReturn(Optional.of(contractor));
        when(laborerRepository.findById(200L)).thenReturn(Optional.of(verifiedLaborer));
        when(projectRepository.findById(500L)).thenReturn(Optional.of(project));
        when(laborAssignmentRepository.findOverlappingActiveAssignments(200L, start, end, null))
                .thenReturn(List.of(existing));

        mockMvc.perform(post("/api/laborers/200/work-requests")
                        .principal(contractorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("active assignment for the selected date range")));
    }

    @Test
    @DisplayName("Step 7: Laborer views their active assignments")
    void testWorkflow_Step7_LaborerViewsAssignments() throws Exception {
        LaborRequest request = new LaborRequest();
        request.setId(1001L);

        LaborAssignment assignment = new LaborAssignment();
        assignment.setId(7001L);
        assignment.setLaborRequest(request);
        assignment.setLaborer(verifiedLaborer);
        assignment.setProject(project);
        assignment.setContractor(contractor);
        assignment.setStartDate(LocalDate.now().plusDays(3));
        assignment.setEndDate(LocalDate.now().plusDays(7));
        assignment.setDailyWage(BigDecimal.valueOf(280.00));
        assignment.setStatus(LaborAssignmentStatus.ACTIVE);

        when(laborerRepository.findByUserEmail("laborer.john@trades.com")).thenReturn(Optional.of(verifiedLaborer));
        when(laborAssignmentRepository.findByLaborerIdAndStatusOrderByStartDateDesc(200L, LaborAssignmentStatus.ACTIVE))
                .thenReturn(List.of(assignment));

        mockMvc.perform(get("/api/labor-assignments/my-assignments")
                        .principal(laborerPrincipal)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(7001)))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }

    @Test
    @DisplayName("Step 8: Contractor views labor assignments for their project")
    void testWorkflow_Step8_ContractorViewsProjectAssignments() throws Exception {
        LaborAssignment assignment = new LaborAssignment();
        assignment.setId(7001L);
        assignment.setProject(project);
        assignment.setLaborer(verifiedLaborer);
        assignment.setContractor(contractor);
        assignment.setStatus(LaborAssignmentStatus.ACTIVE);

        when(contractorRepository.findByUserEmail("contractor.bob@builder.com")).thenReturn(Optional.of(contractor));
        when(projectRepository.findById(500L)).thenReturn(Optional.of(project));
        when(laborAssignmentRepository.findByProjectIdOrderByStartDateDesc(500L))
                .thenReturn(List.of(assignment));

        mockMvc.perform(get("/api/projects/500/labor-assignments")
                        .principal(contractorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(7001)))
                .andExpect(jsonPath("$[0].laborerName", is("John Doe")));
    }

    @Test
    @DisplayName("Step 9: Complete assignment -> status becomes COMPLETED and laborer returns to AVAILABLE")
    void testWorkflow_Step9_CompleteAssignment() throws Exception {
        verifiedLaborer.setAvailabilityStatus(LaborerAvailability.WORKING);

        LaborRequest request = new LaborRequest();
        request.setId(1001L);
        request.setStatus(BookingStatus.ACCEPTED);

        LaborAssignment assignment = new LaborAssignment();
        assignment.setId(7001L);
        assignment.setLaborRequest(request);
        assignment.setLaborer(verifiedLaborer);
        assignment.setContractor(contractor);
        assignment.setProject(project);
        assignment.setStatus(LaborAssignmentStatus.ACTIVE);

        when(laborAssignmentRepository.findById(7001L)).thenReturn(Optional.of(assignment));
        when(userRepository.findByEmail("contractor.bob@builder.com")).thenReturn(Optional.of(contractorUser));
        when(laborAssignmentRepository.save(any(LaborAssignment.class))).thenAnswer(i -> i.getArgument(0));
        when(laborAssignmentRepository.findByLaborerIdAndStatusOrderByStartDateDesc(200L, LaborAssignmentStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        String payload = "{\"notes\":\"Project phase finished successfully.\"}";

        mockMvc.perform(put("/api/labor-assignments/7001/complete")
                        .principal(contractorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(7001)))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    @DisplayName("Step 10: Laborer rejects work request")
    void testWorkflow_Step10_LaborerRejectsRequest() throws Exception {
        LaborRequest request = new LaborRequest();
        request.setId(1002L);
        request.setLaborer(verifiedLaborer);
        request.setContractor(contractor);
        request.setProject(project);
        request.setStatus(BookingStatus.PENDING);

        when(laborerRepository.findByUserEmail("laborer.john@trades.com")).thenReturn(Optional.of(verifiedLaborer));
        when(laborRequestRepository.findById(1002L)).thenReturn(Optional.of(request));
        when(laborRequestRepository.save(any(LaborRequest.class))).thenAnswer(i -> i.getArgument(0));

        String payload = "{\"reason\":\"Fully booked with private residential framing.\"}";

        mockMvc.perform(put("/api/labor-requests/1002/reject")
                        .principal(laborerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1002)))
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.rejectionReason", containsString("Fully booked")));
    }

    @Test
    @DisplayName("Step 11: Contractor cancels work request")
    void testWorkflow_Step11_ContractorCancelsRequest() throws Exception {
        LaborRequest request = new LaborRequest();
        request.setId(1003L);
        request.setLaborer(verifiedLaborer);
        request.setContractor(contractor);
        request.setProject(project);
        request.setStatus(BookingStatus.PENDING);

        when(contractorRepository.findByUserEmail("contractor.bob@builder.com")).thenReturn(Optional.of(contractor));
        when(laborRequestRepository.findById(1003L)).thenReturn(Optional.of(request));
        when(laborRequestRepository.save(any(LaborRequest.class))).thenAnswer(i -> i.getArgument(0));

        String payload = "{\"reason\":\"Client delayed start date\"}";

        mockMvc.perform(put("/api/labor-requests/1003/cancel")
                        .principal(contractorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1003)))
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }
}
