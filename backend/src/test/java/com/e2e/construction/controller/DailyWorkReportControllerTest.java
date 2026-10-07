package com.e2e.construction.controller;

import com.e2e.construction.dto.DailyWorkReportRequest;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.DailyWorkReport;
import com.e2e.construction.entity.Project;
import com.e2e.construction.entity.ProjectType;
import com.e2e.construction.entity.Role;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.GlobalExceptionHandler;
import com.e2e.construction.repository.DailyWorkReportImageRepository;
import com.e2e.construction.repository.DailyWorkReportRepository;
import com.e2e.construction.repository.ProjectRepository;
import com.e2e.construction.repository.UserRepository;
import com.e2e.construction.service.DailyWorkReportService;
import com.e2e.construction.service.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DailyWorkReportControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private DailyWorkReportRepository dailyWorkReportRepository;

    @Mock
    private DailyWorkReportImageRepository dailyWorkReportImageRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    private FileStorageService fileStorageService;
    private DailyWorkReportService dailyWorkReportService;
    private DailyWorkReportController dailyWorkReportController;

    private Principal principal;
    private User contractorUser;
    private Contractor contractor;
    private Project project;
    private DailyWorkReport sampleReport;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService("target/test-uploads-ctrl");
        dailyWorkReportService = new DailyWorkReportService(
                dailyWorkReportRepository,
                dailyWorkReportImageRepository,
                projectRepository,
                userRepository,
                fileStorageService
        );
        dailyWorkReportController = new DailyWorkReportController(dailyWorkReportService);

        mockMvc = MockMvcBuilders.standaloneSetup(dailyWorkReportController)
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
        contractor.setCompanyName("BuildTech Inc");
        contractor.setUser(contractorUser);

        project = new Project();
        project.setId(100L);
        project.setName("Tower Phase 1");
        project.setContractor(contractor);
        project.setProjectType(ProjectType.BUILDING);

        sampleReport = new DailyWorkReport();
        sampleReport.setId(10L);
        sampleReport.setProject(project);
        sampleReport.setReportDate(LocalDate.of(2026, 10, 6));
        sampleReport.setWorkDescription("Pillar structural inspection and foundation rebar binding");
        sampleReport.setNumberOfWorkers(20);
        sampleReport.setMachineryUsed("Crane, Mixer");
        sampleReport.setMaterialsUsed("Cement, Sand, Steel");
        sampleReport.setQuantityCompleted("30 sqm");
        sampleReport.setWorkingHours(8.0);
        sampleReport.setProgressPercentage(25.0);
        sampleReport.setExpenses(new BigDecimal("7500.00"));
        sampleReport.setIssuesOrDelays("None");
        sampleReport.setRemarks("On schedule");
        sampleReport.setCreatedBy(contractorUser);
    }

    @Test
    @DisplayName("POST /api/daily-reports creates a report successfully")
    void createReport_success() throws Exception {
        DailyWorkReportRequest request = new DailyWorkReportRequest(
                100L,
                LocalDate.of(2026, 10, 6),
                "Pillar structural inspection and foundation rebar binding",
                20,
                "Crane, Mixer",
                "Cement, Sand, Steel",
                "30 sqm",
                8.0,
                25.0,
                new BigDecimal("7500.00"),
                "None",
                "On schedule",
                List.of("https://example.com/site.jpg")
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(dailyWorkReportRepository.findByProjectIdAndReportDate(100L, LocalDate.of(2026, 10, 6)))
                .thenReturn(Optional.empty());
        when(dailyWorkReportRepository.save(any(DailyWorkReport.class))).thenAnswer(invocation -> {
            DailyWorkReport r = invocation.getArgument(0);
            r.setId(10L);
            return r;
        });

        mockMvc.perform(post("/api/daily-reports")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.projectName", is("Tower Phase 1")))
                .andExpect(jsonPath("$.workDescription", is("Pillar structural inspection and foundation rebar binding")))
                .andExpect(jsonPath("$.numberOfWorkers", is(20)))
                .andExpect(jsonPath("$.progressPercentage", is(25.0)));
    }

    @Test
    @DisplayName("GET /api/daily-reports/{id} views report by ID")
    void getReportById_success() throws Exception {
        when(dailyWorkReportRepository.findById(10L)).thenReturn(Optional.of(sampleReport));

        mockMvc.perform(get("/api/daily-reports/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.projectName", is("Tower Phase 1")))
                .andExpect(jsonPath("$.date", is("2026-10-06")));
    }

    @Test
    @DisplayName("PUT /api/daily-reports/{id} updates report")
    void updateReport_success() throws Exception {
        DailyWorkReportRequest updateRequest = new DailyWorkReportRequest();
        updateRequest.setProjectId(100L);
        updateRequest.setDate(LocalDate.of(2026, 10, 6));
        updateRequest.setWorkDescription("Updated inspection remarks");
        updateRequest.setNumberOfWorkers(22);
        updateRequest.setWorkingHours(8.5);

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(dailyWorkReportRepository.findById(10L)).thenReturn(Optional.of(sampleReport));
        when(dailyWorkReportRepository.existsByProjectIdAndReportDateAndIdNot(100L, sampleReport.getReportDate(), 10L))
                .thenReturn(false);
        when(dailyWorkReportRepository.save(any(DailyWorkReport.class))).thenReturn(sampleReport);

        mockMvc.perform(put("/api/daily-reports/10")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workDescription", is("Updated inspection remarks")))
                .andExpect(jsonPath("$.numberOfWorkers", is(22)));
    }

    @Test
    @DisplayName("GET /api/daily-reports lists reports with filters")
    void listReports_success() throws Exception {
        when(dailyWorkReportRepository.filterReports(eq(100L), eq(LocalDate.of(2026, 10, 6)), any(), any()))
                .thenReturn(List.of(sampleReport));

        mockMvc.perform(get("/api/daily-reports")
                        .param("projectId", "100")
                        .param("date", "2026-10-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[0].projectName", is("Tower Phase 1")));
    }

    @Test
    @DisplayName("GET /api/daily-reports/project/{projectId} filters by project")
    void listReportsByProject_success() throws Exception {
        when(dailyWorkReportRepository.filterReports(eq(100L), any(), any(), any()))
                .thenReturn(List.of(sampleReport));

        mockMvc.perform(get("/api/daily-reports/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].projectId", is(100)));
    }

    @Test
    @DisplayName("POST /api/daily-reports/{id}/images uploads multiple images")
    void uploadReportImages_success() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("files", "site1.jpg", "image/jpeg", "dummy1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "site2.png", "image/png", "dummy2".getBytes());

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(dailyWorkReportRepository.findById(10L)).thenReturn(Optional.of(sampleReport));
        when(dailyWorkReportImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(multipart("/api/daily-reports/10/images")
                        .file(file1)
                        .file(file2)
                        .principal(principal))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].fileName", is("site1.jpg")))
                .andExpect(jsonPath("$[1].fileName", is("site2.png")));
    }

    @Test
    @DisplayName("DELETE /api/daily-reports/{id} deletes report")
    void deleteReport_success() throws Exception {
        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(dailyWorkReportRepository.findById(10L)).thenReturn(Optional.of(sampleReport));

        mockMvc.perform(delete("/api/daily-reports/10")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Daily work report deleted successfully")))
                .andExpect(jsonPath("$.reportId", is("10")));
    }
}
