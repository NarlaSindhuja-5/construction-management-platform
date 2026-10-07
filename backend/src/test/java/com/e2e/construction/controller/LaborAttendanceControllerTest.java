package com.e2e.construction.controller;

import com.e2e.construction.dto.BatchLaborAttendanceItem;
import com.e2e.construction.dto.BatchLaborAttendanceRequest;
import com.e2e.construction.dto.LaborAttendanceRequest;
import com.e2e.construction.entity.AttendanceStatus;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.LaborAttendance;
import com.e2e.construction.entity.Laborer;
import com.e2e.construction.entity.Project;
import com.e2e.construction.entity.ProjectType;
import com.e2e.construction.entity.Role;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.GlobalExceptionHandler;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.LaborAttendanceRepository;
import com.e2e.construction.repository.LaborerRepository;
import com.e2e.construction.repository.ProjectRepository;
import com.e2e.construction.repository.UserRepository;
import com.e2e.construction.service.LaborAttendanceService;
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
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LaborAttendanceControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private LaborAttendanceRepository laborAttendanceRepository;

    @Mock
    private LaborerRepository laborerRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ContractorRepository contractorRepository;

    @Mock
    private UserRepository userRepository;

    private LaborAttendanceService laborAttendanceService;
    private LaborAttendanceController laborAttendanceController;

    private Principal principal;
    private User contractorUser;
    private Contractor contractor;
    private Laborer laborer;
    private Project project;
    private LaborAttendance sampleAttendance;

    @BeforeEach
    void setUp() {
        laborAttendanceService = new LaborAttendanceService(
                laborAttendanceRepository,
                laborerRepository,
                projectRepository,
                contractorRepository,
                userRepository
        );
        laborAttendanceController = new LaborAttendanceController(laborAttendanceService);

        mockMvc = MockMvcBuilders.standaloneSetup(laborAttendanceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        principal = () -> "contractor@example.com";

        Role contractorRole = new Role("CONTRACTOR", "Contractor role");

        contractorUser = new User();
        contractorUser.setId(1L);
        contractorUser.setEmail("contractor@example.com");
        contractorUser.setFirstName("John");
        contractorUser.setLastName("Builder");
        contractorUser.setRole(contractorRole);

        contractor = new Contractor();
        contractor.setId(5L);
        contractor.setUser(contractorUser);
        contractor.setCompanyName("BuildTech Inc");

        User laborerUser = new User();
        laborerUser.setId(2L);
        laborerUser.setFirstName("Alex");
        laborerUser.setLastName("Stone");

        laborer = new Laborer();
        laborer.setId(20L);
        laborer.setUser(laborerUser);
        laborer.setSkills("Masonry");
        laborer.setDailyWage(new BigDecimal("120.00"));

        project = new Project();
        project.setId(100L);
        project.setName("Tower Phase 1");
        project.setContractor(contractor);
        project.setProjectType(ProjectType.BUILDING);

        sampleAttendance = new LaborAttendance();
        sampleAttendance.setId(10L);
        sampleAttendance.setLaborer(laborer);
        sampleAttendance.setProject(project);
        sampleAttendance.setAttendanceDate(LocalDate.of(2026, 10, 6));
        sampleAttendance.setStatus(AttendanceStatus.PRESENT);
        sampleAttendance.setWorkingHours(8.0);
        sampleAttendance.setRemarks("On time");
        sampleAttendance.setRecordedBy(contractorUser);
    }

    @Test
    @DisplayName("POST /api/labor-attendance records attendance successfully")
    void recordAttendance_success() throws Exception {
        LaborAttendanceRequest request = new LaborAttendanceRequest(
                20L,
                100L,
                LocalDate.of(2026, 10, 6),
                AttendanceStatus.PRESENT,
                8.0,
                "Foundation works"
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(laborerRepository.findById(20L)).thenReturn(Optional.of(laborer));
        when(laborAttendanceRepository.existsByLaborerIdAndProjectIdAndAttendanceDate(20L, 100L, LocalDate.of(2026, 10, 6)))
                .thenReturn(false);
        when(laborAttendanceRepository.save(any(LaborAttendance.class))).thenAnswer(invocation -> {
            LaborAttendance a = invocation.getArgument(0);
            a.setId(10L);
            return a;
        });

        mockMvc.perform(post("/api/labor-attendance")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.laborerName", is("Alex Stone")))
                .andExpect(jsonPath("$.projectName", is("Tower Phase 1")))
                .andExpect(jsonPath("$.status", is("PRESENT")))
                .andExpect(jsonPath("$.workingHours", is(8.0)));
    }

    @Test
    @DisplayName("POST /api/labor-attendance/batch records multiple attendance records")
    void recordBatchAttendance_success() throws Exception {
        BatchLaborAttendanceRequest request = new BatchLaborAttendanceRequest(
                100L,
                LocalDate.of(2026, 10, 6),
                List.of(
                        new BatchLaborAttendanceItem(20L, AttendanceStatus.PRESENT, 8.0, "Shift complete"),
                        new BatchLaborAttendanceItem(20L, AttendanceStatus.OVERTIME, 10.0, "Extra hours")
                )
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(laborerRepository.findById(20L)).thenReturn(Optional.of(laborer));
        when(laborAttendanceRepository.existsByLaborerIdAndProjectIdAndAttendanceDate(20L, 100L, LocalDate.of(2026, 10, 6)))
                .thenReturn(false);
        when(laborAttendanceRepository.save(any(LaborAttendance.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/api/labor-attendance/batch")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/labor-attendance/{id} views attendance by ID")
    void getAttendanceById_success() throws Exception {
        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(laborAttendanceRepository.findById(10L)).thenReturn(Optional.of(sampleAttendance));

        mockMvc.perform(get("/api/labor-attendance/10")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.status", is("PRESENT")))
                .andExpect(jsonPath("$.date", is("2026-10-06")));
    }

    @Test
    @DisplayName("PUT /api/labor-attendance/{id} updates attendance")
    void updateAttendance_success() throws Exception {
        LaborAttendanceRequest updateRequest = new LaborAttendanceRequest(
                20L,
                100L,
                LocalDate.of(2026, 10, 6),
                AttendanceStatus.HALF_DAY,
                4.0,
                "Half day shift"
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(laborAttendanceRepository.findById(10L)).thenReturn(Optional.of(sampleAttendance));
        when(laborAttendanceRepository.existsByLaborerIdAndProjectIdAndAttendanceDateAndIdNot(20L, 100L, LocalDate.of(2026, 10, 6), 10L))
                .thenReturn(false);
        when(laborAttendanceRepository.save(any(LaborAttendance.class))).thenReturn(sampleAttendance);

        mockMvc.perform(put("/api/labor-attendance/10")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("HALF_DAY")))
                .andExpect(jsonPath("$.workingHours", is(4.0)));
    }

    @Test
    @DisplayName("GET /api/labor-attendance views history with filters")
    void getAttendanceHistory_success() throws Exception {
        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(1L)).thenReturn(Optional.of(contractor));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(laborAttendanceRepository.filterAttendance(eq(100L), eq(5L), eq(20L), eq(LocalDate.of(2026, 10, 6)), any(), any(), eq(AttendanceStatus.PRESENT)))
                .thenReturn(List.of(sampleAttendance));

        mockMvc.perform(get("/api/labor-attendance")
                        .param("projectId", "100")
                        .param("laborerId", "20")
                        .param("date", "2026-10-06")
                        .param("status", "PRESENT")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[0].laborerName", is("Alex Stone")));
    }

    @Test
    @DisplayName("GET /api/labor-attendance/project/{projectId}/summary returns summary metrics")
    void getAttendanceSummary_success() throws Exception {
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(laborAttendanceRepository.filterAttendance(eq(100L), any(), any(), eq(LocalDate.of(2026, 10, 6)), any(), any(), any()))
                .thenReturn(List.of(sampleAttendance));

        mockMvc.perform(get("/api/labor-attendance/project/100/summary")
                        .param("date", "2026-10-06")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId", is(100)))
                .andExpect(jsonPath("$.totalRecords", is(1)))
                .andExpect(jsonPath("$.presentCount", is(1)))
                .andExpect(jsonPath("$.totalWorkingHours", is(8.0)));
    }

    @Test
    @DisplayName("DELETE /api/labor-attendance/{id} deletes attendance record")
    void deleteAttendance_success() throws Exception {
        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(laborAttendanceRepository.findById(10L)).thenReturn(Optional.of(sampleAttendance));

        mockMvc.perform(delete("/api/labor-attendance/10")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Labor attendance record deleted successfully")))
                .andExpect(jsonPath("$.attendanceId", is("10")));
    }
}
