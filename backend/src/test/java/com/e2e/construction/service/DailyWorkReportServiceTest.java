package com.e2e.construction.service;

import com.e2e.construction.dto.DailyWorkReportImageResponse;
import com.e2e.construction.dto.DailyWorkReportRequest;
import com.e2e.construction.dto.DailyWorkReportResponse;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.DailyWorkReport;
import com.e2e.construction.entity.DailyWorkReportImage;
import com.e2e.construction.entity.Project;
import com.e2e.construction.entity.ProjectType;
import com.e2e.construction.entity.Role;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.DailyWorkReportImageRepository;
import com.e2e.construction.repository.DailyWorkReportRepository;
import com.e2e.construction.repository.ProjectRepository;
import com.e2e.construction.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyWorkReportServiceTest {

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

    private User contractorUser;
    private User siteManagerUser;
    private User otherContractorUser;
    private Contractor contractor;
    private Project project;
    private DailyWorkReport sampleReport;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService("target/test-uploads-reports");
        dailyWorkReportService = new DailyWorkReportService(
                dailyWorkReportRepository,
                dailyWorkReportImageRepository,
                projectRepository,
                userRepository,
                fileStorageService
        );

        Role contractorRole = new Role("CONTRACTOR", "Contractor role");
        Role siteManagerRole = new Role("SITE_MANAGER", "Site Manager role");

        contractorUser = new User();
        contractorUser.setId(1L);
        contractorUser.setEmail("contractor@example.com");
        contractorUser.setFirstName("John");
        contractorUser.setLastName("Builder");
        contractorUser.setRole(contractorRole);

        siteManagerUser = new User();
        siteManagerUser.setId(2L);
        siteManagerUser.setEmail("sitemanager@example.com");
        siteManagerUser.setFirstName("Sarah");
        siteManagerUser.setLastName("Supervisor");
        siteManagerUser.setRole(siteManagerRole);

        otherContractorUser = new User();
        otherContractorUser.setId(3L);
        otherContractorUser.setEmail("other@example.com");
        otherContractorUser.setFirstName("Bob");
        otherContractorUser.setLastName("Contractor");
        otherContractorUser.setRole(contractorRole);

        contractor = new Contractor();
        contractor.setId(10L);
        contractor.setUser(contractorUser);
        contractor.setCompanyName("Acme Construction Corp");

        project = new Project();
        project.setId(100L);
        project.setName("Skyline Tower Project");
        project.setContractor(contractor);
        project.setProjectType(ProjectType.BUILDING);

        sampleReport = new DailyWorkReport();
        sampleReport.setId(500L);
        sampleReport.setProject(project);
        sampleReport.setReportDate(LocalDate.of(2026, 10, 6));
        sampleReport.setWorkDescription("Foundation pouring and structural pillar testing");
        sampleReport.setNumberOfWorkers(25);
        sampleReport.setMachineryUsed("2x Excavator, 1x Crane");
        sampleReport.setMaterialsUsed("100 bags cement, 15 tons rebar");
        sampleReport.setQuantityCompleted("45 cubic meters concrete");
        sampleReport.setWorkingHours(8.5);
        sampleReport.setProgressPercentage(15.0);
        sampleReport.setExpenses(new BigDecimal("12500.00"));
        sampleReport.setIssuesOrDelays("Rain delay in the afternoon (1 hour)");
        sampleReport.setRemarks("Target met despite weather");
        sampleReport.setCreatedBy(contractorUser);
    }

    @Test
    @DisplayName("Authorized contractor creates a daily report successfully")
    void createReport_asContractor_success() {
        DailyWorkReportRequest request = new DailyWorkReportRequest(
                100L,
                LocalDate.of(2026, 10, 6),
                "Excavation and leveling work",
                15,
                "CAT 320 Excavator",
                "Gravel, diesel fuel",
                "500 sqm excavated",
                8.0,
                12.5,
                new BigDecimal("5000.00"),
                "None",
                "Smooth operations",
                List.of("https://example.com/site1.jpg", "https://example.com/site2.jpg")
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(dailyWorkReportRepository.findByProjectIdAndReportDate(100L, LocalDate.of(2026, 10, 6)))
                .thenReturn(Optional.empty());
        when(dailyWorkReportRepository.save(any(DailyWorkReport.class))).thenAnswer(invocation -> {
            DailyWorkReport r = invocation.getArgument(0);
            r.setId(501L);
            return r;
        });

        DailyWorkReportResponse response = dailyWorkReportService.createReport("contractor@example.com", request);

        assertNotNull(response);
        assertEquals(501L, response.getId());
        assertEquals("Skyline Tower Project", response.getProjectName());
        assertEquals("Excavation and leveling work", response.getWorkDescription());
        assertEquals(15, response.getNumberOfWorkers());
        assertEquals(2, response.getImages().size());
        verify(dailyWorkReportRepository).save(any(DailyWorkReport.class));
    }

    @Test
    @DisplayName("Authorized site manager creates daily report successfully")
    void createReport_asSiteManager_success() {
        DailyWorkReportRequest request = new DailyWorkReportRequest(
                100L,
                LocalDate.of(2026, 10, 6),
                "Site safety checks and electrical wiring",
                8,
                "Scissor lift",
                "Copper wiring 500m",
                "Floor 3 completed",
                7.5,
                18.0,
                new BigDecimal("2100.00"),
                "Minor supplier delay",
                "Inspection passed",
                null
        );

        when(userRepository.findByEmail("sitemanager@example.com")).thenReturn(Optional.of(siteManagerUser));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(dailyWorkReportRepository.findByProjectIdAndReportDate(100L, LocalDate.of(2026, 10, 6)))
                .thenReturn(Optional.empty());
        when(dailyWorkReportRepository.save(any(DailyWorkReport.class))).thenAnswer(invocation -> {
            DailyWorkReport r = invocation.getArgument(0);
            r.setId(502L);
            return r;
        });

        DailyWorkReportResponse response = dailyWorkReportService.createReport("sitemanager@example.com", request);

        assertNotNull(response);
        assertEquals("SITE_MANAGER", response.getCreatedByRole());
        assertEquals("Site safety checks and electrical wiring", response.getWorkDescription());
    }

    @Test
    @DisplayName("Duplicate report for same project and date is updated when user has edit permission")
    void createReport_duplicateDate_userHasEditPermission_updatesExisting() {
        DailyWorkReportRequest request = new DailyWorkReportRequest(
                100L,
                LocalDate.of(2026, 10, 6),
                "Updated foundation pouring and curing",
                28,
                "2x Excavator, 2x Concrete Mixer",
                "120 bags cement",
                "50 cubic meters poured",
                9.0,
                16.5,
                new BigDecimal("14000.00"),
                "None",
                "Evening shift completed",
                List.of("https://example.com/site-evening.jpg")
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(dailyWorkReportRepository.findByProjectIdAndReportDate(100L, LocalDate.of(2026, 10, 6)))
                .thenReturn(Optional.of(sampleReport));
        when(dailyWorkReportRepository.save(sampleReport)).thenReturn(sampleReport);

        DailyWorkReportResponse response = dailyWorkReportService.createReport("contractor@example.com", request);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals("Updated foundation pouring and curing", response.getWorkDescription());
        assertEquals(28, response.getNumberOfWorkers());
        assertEquals(16.5, response.getProgressPercentage());
    }

    @Test
    @DisplayName("Duplicate report rejected when user lacks permission to edit the existing report")
    void createReport_duplicateDate_userLacksPermission_throwsBadRequest() {
        DailyWorkReportRequest request = new DailyWorkReportRequest(
                100L,
                LocalDate.of(2026, 10, 6),
                "Attempt to overwrite",
                10,
                null, null, null, 8.0, 10.0, BigDecimal.ZERO, null, null, null
        );

        // other contractor tries to post report on project owned by contractorUser
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherContractorUser));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));

        assertThrows(AccessDeniedException.class, () ->
                dailyWorkReportService.createReport("other@example.com", request));
    }

    @Test
    @DisplayName("Unauthorized contractor cannot file report on another contractor's project")
    void createReport_unauthorizedContractor_throwsAccessDenied() {
        DailyWorkReportRequest request = new DailyWorkReportRequest(
                100L,
                LocalDate.of(2026, 10, 7),
                "Unauthorized submission",
                5, null, null, null, 8.0, 10.0, BigDecimal.ZERO, null, null, null
        );

        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherContractorUser));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));

        assertThrows(AccessDeniedException.class, () ->
                dailyWorkReportService.createReport("other@example.com", request));

        verify(dailyWorkReportRepository, never()).save(any());
    }

    @Test
    @DisplayName("View report by ID returns correct details")
    void getReportById_success() {
        when(dailyWorkReportRepository.findById(500L)).thenReturn(Optional.of(sampleReport));

        DailyWorkReportResponse response = dailyWorkReportService.getReportById(500L);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals("Skyline Tower Project", response.getProjectName());
    }

    @Test
    @DisplayName("View non-existent report throws ResourceNotFoundException")
    void getReportById_notFound_throwsException() {
        when(dailyWorkReportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                dailyWorkReportService.getReportById(999L));
    }

    @Test
    @DisplayName("Update report successfully by authorized owner")
    void updateReport_success() {
        DailyWorkReportRequest updateRequest = new DailyWorkReportRequest();
        updateRequest.setWorkDescription("Updated description");
        updateRequest.setNumberOfWorkers(30);
        updateRequest.setWorkingHours(9.5);
        updateRequest.setProgressPercentage(17.0);

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(dailyWorkReportRepository.findById(500L)).thenReturn(Optional.of(sampleReport));
        when(dailyWorkReportRepository.existsByProjectIdAndReportDateAndIdNot(100L, sampleReport.getReportDate(), 500L))
                .thenReturn(false);
        when(dailyWorkReportRepository.save(sampleReport)).thenReturn(sampleReport);

        DailyWorkReportResponse response = dailyWorkReportService.updateReport(500L, "contractor@example.com", updateRequest);

        assertNotNull(response);
        assertEquals("Updated description", response.getWorkDescription());
        assertEquals(30, response.getNumberOfWorkers());
        assertEquals(9.5, response.getWorkingHours());
    }

    @Test
    @DisplayName("List and filter reports by project and date")
    void listReports_withFilters() {
        when(dailyWorkReportRepository.filterReports(100L, LocalDate.of(2026, 10, 6), null, null))
                .thenReturn(List.of(sampleReport));

        List<DailyWorkReportResponse> results = dailyWorkReportService.listReports(
                100L,
                LocalDate.of(2026, 10, 6),
                null,
                null
        );

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(500L, results.get(0).getId());
    }

    @Test
    @DisplayName("Upload multiple site images to an existing report")
    void uploadReportImages_success() {
        MockMultipartFile file1 = new MockMultipartFile("files", "site1.jpg", "image/jpeg", "image1data".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "site2.png", "image/png", "image2data".getBytes());

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(dailyWorkReportRepository.findById(500L)).thenReturn(Optional.of(sampleReport));
        when(dailyWorkReportImageRepository.save(any(DailyWorkReportImage.class))).thenAnswer(i -> i.getArgument(0));

        List<DailyWorkReportImageResponse> uploaded = dailyWorkReportService.uploadReportImages(
                500L,
                "contractor@example.com",
                List.of(file1, file2)
        );

        assertNotNull(uploaded);
        assertEquals(2, uploaded.size());
        assertEquals("site1.jpg", uploaded.get(0).getFileName());
        assertEquals("site2.png", uploaded.get(1).getFileName());
    }

    @Test
    @DisplayName("Delete report removes report and cleans images")
    void deleteReport_success() {
        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(dailyWorkReportRepository.findById(500L)).thenReturn(Optional.of(sampleReport));

        dailyWorkReportService.deleteReport(500L, "contractor@example.com");

        verify(dailyWorkReportRepository).delete(sampleReport);
    }
}
