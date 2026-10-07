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
import com.e2e.construction.entity.ProjectType;
import com.e2e.construction.entity.Role;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.LaborAttendanceRepository;
import com.e2e.construction.repository.LaborerRepository;
import com.e2e.construction.repository.ProjectRepository;
import com.e2e.construction.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class LaborAttendanceServiceTest {

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

    @InjectMocks
    private LaborAttendanceService laborAttendanceService;

    private User contractorUser;
    private User siteManagerUser;
    private User otherContractorUser;
    private User laborerUser;
    private Contractor contractor;
    private Laborer laborer;
    private Project project;
    private LaborAttendance sampleAttendance;

    @BeforeEach
    void setUp() {
        Role contractorRole = new Role("CONTRACTOR", "Contractor role");
        Role siteManagerRole = new Role("SITE_MANAGER", "Site Manager role");
        Role laborerRole = new Role("LABORER", "Laborer role");

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

        laborerUser = new User();
        laborerUser.setId(4L);
        laborerUser.setEmail("laborer@example.com");
        laborerUser.setFirstName("Mike");
        laborerUser.setLastName("Mason");
        laborerUser.setPhoneNumber("+1234567890");
        laborerUser.setRole(laborerRole);

        contractor = new Contractor();
        contractor.setId(10L);
        contractor.setUser(contractorUser);
        contractor.setCompanyName("Acme Construction Corp");

        laborer = new Laborer();
        laborer.setId(20L);
        laborer.setUser(laborerUser);
        laborer.setSkills("Masonry, Bricklaying");
        laborer.setDailyWage(new BigDecimal("150.00"));

        project = new Project();
        project.setId(100L);
        project.setName("Skyline Tower Project");
        project.setContractor(contractor);
        project.setProjectType(ProjectType.BUILDING);

        sampleAttendance = new LaborAttendance();
        sampleAttendance.setId(1L);
        sampleAttendance.setLaborer(laborer);
        sampleAttendance.setProject(project);
        sampleAttendance.setAttendanceDate(LocalDate.of(2026, 10, 6));
        sampleAttendance.setStatus(AttendanceStatus.PRESENT);
        sampleAttendance.setWorkingHours(8.0);
        sampleAttendance.setRemarks("On time");
        sampleAttendance.setRecordedBy(contractorUser);
    }

    @Test
    @DisplayName("Contractor records labor attendance successfully")
    void recordAttendance_asContractor_success() {
        LaborAttendanceRequest request = new LaborAttendanceRequest(
                20L,
                100L,
                LocalDate.of(2026, 10, 6),
                AttendanceStatus.PRESENT,
                8.0,
                "Worked on pillar foundation"
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(laborerRepository.findById(20L)).thenReturn(Optional.of(laborer));
        when(laborAttendanceRepository.existsByLaborerIdAndProjectIdAndAttendanceDate(20L, 100L, LocalDate.of(2026, 10, 6)))
                .thenReturn(false);
        when(laborAttendanceRepository.save(any(LaborAttendance.class))).thenAnswer(invocation -> {
            LaborAttendance a = invocation.getArgument(0);
            a.setId(101L);
            return a;
        });

        LaborAttendanceResponse response = laborAttendanceService.recordAttendance("contractor@example.com", request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals(20L, response.getLaborerId());
        assertEquals("Mike Mason", response.getLaborerName());
        assertEquals(100L, response.getProjectId());
        assertEquals(AttendanceStatus.PRESENT, response.getStatus());
        assertEquals(8.0, response.getWorkingHours());
        verify(laborAttendanceRepository).save(any(LaborAttendance.class));
    }

    @Test
    @DisplayName("Site manager records labor attendance successfully with overtime")
    void recordAttendance_asSiteManager_overtime_success() {
        LaborAttendanceRequest request = new LaborAttendanceRequest(
                20L,
                100L,
                LocalDate.of(2026, 10, 6),
                AttendanceStatus.OVERTIME,
                11.5,
                "Overtime concrete casting"
        );

        when(userRepository.findByEmail("sitemanager@example.com")).thenReturn(Optional.of(siteManagerUser));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(laborerRepository.findById(20L)).thenReturn(Optional.of(laborer));
        when(laborAttendanceRepository.existsByLaborerIdAndProjectIdAndAttendanceDate(20L, 100L, LocalDate.of(2026, 10, 6)))
                .thenReturn(false);
        when(laborAttendanceRepository.save(any(LaborAttendance.class))).thenAnswer(invocation -> {
            LaborAttendance a = invocation.getArgument(0);
            a.setId(102L);
            return a;
        });

        LaborAttendanceResponse response = laborAttendanceService.recordAttendance("sitemanager@example.com", request);

        assertNotNull(response);
        assertEquals(102L, response.getId());
        assertEquals(AttendanceStatus.OVERTIME, response.getStatus());
        assertEquals(11.5, response.getWorkingHours());
    }

    @Test
    @DisplayName("Prevent duplicate attendance for the same laborer, project, and date")
    void recordAttendance_duplicate_throwsBadRequest() {
        LaborAttendanceRequest request = new LaborAttendanceRequest(
                20L,
                100L,
                LocalDate.of(2026, 10, 6),
                AttendanceStatus.PRESENT,
                8.0,
                "Duplicate entry attempt"
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(laborerRepository.findById(20L)).thenReturn(Optional.of(laborer));
        when(laborAttendanceRepository.existsByLaborerIdAndProjectIdAndAttendanceDate(20L, 100L, LocalDate.of(2026, 10, 6)))
                .thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                laborAttendanceService.recordAttendance("contractor@example.com", request));

        assertNotNull(ex.getMessage());
        verify(laborAttendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Unauthorized contractor cannot record attendance for another contractor's project")
    void recordAttendance_unauthorizedContractor_throwsAccessDenied() {
        LaborAttendanceRequest request = new LaborAttendanceRequest(
                20L,
                100L,
                LocalDate.of(2026, 10, 6),
                AttendanceStatus.PRESENT,
                8.0,
                null
        );

        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherContractorUser));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));

        assertThrows(AccessDeniedException.class, () ->
                laborAttendanceService.recordAttendance("other@example.com", request));

        verify(laborAttendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Record batch labor attendance successfully")
    void recordBatchAttendance_success() {
        BatchLaborAttendanceRequest request = new BatchLaborAttendanceRequest(
                100L,
                LocalDate.of(2026, 10, 6),
                List.of(
                        new BatchLaborAttendanceItem(20L, AttendanceStatus.PRESENT, 8.0, "Full shift"),
                        new BatchLaborAttendanceItem(21L, AttendanceStatus.HALF_DAY, 4.0, "Morning only")
                )
        );

        Laborer laborer2 = new Laborer();
        laborer2.setId(21L);
        laborer2.setUser(laborerUser);

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(laborerRepository.findById(20L)).thenReturn(Optional.of(laborer));
        when(laborerRepository.findById(21L)).thenReturn(Optional.of(laborer2));
        when(laborAttendanceRepository.existsByLaborerIdAndProjectIdAndAttendanceDate(20L, 100L, LocalDate.of(2026, 10, 6)))
                .thenReturn(false);
        when(laborAttendanceRepository.existsByLaborerIdAndProjectIdAndAttendanceDate(21L, 100L, LocalDate.of(2026, 10, 6)))
                .thenReturn(false);
        when(laborAttendanceRepository.save(any(LaborAttendance.class))).thenAnswer(i -> i.getArgument(0));

        List<LaborAttendanceResponse> responses = laborAttendanceService.recordBatchAttendance("contractor@example.com", request);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(AttendanceStatus.PRESENT, responses.get(0).getStatus());
        assertEquals(AttendanceStatus.HALF_DAY, responses.get(1).getStatus());
    }

    @Test
    @DisplayName("Update existing attendance record successfully")
    void updateAttendance_success() {
        LaborAttendanceRequest updateRequest = new LaborAttendanceRequest(
                20L,
                100L,
                LocalDate.of(2026, 10, 6),
                AttendanceStatus.HALF_DAY,
                4.0,
                "Left early due to rain"
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(laborAttendanceRepository.findById(1L)).thenReturn(Optional.of(sampleAttendance));
        when(laborAttendanceRepository.existsByLaborerIdAndProjectIdAndAttendanceDateAndIdNot(20L, 100L, LocalDate.of(2026, 10, 6), 1L))
                .thenReturn(false);
        when(laborAttendanceRepository.save(any(LaborAttendance.class))).thenReturn(sampleAttendance);

        LaborAttendanceResponse response = laborAttendanceService.updateAttendance(1L, "contractor@example.com", updateRequest);

        assertNotNull(response);
        assertEquals(AttendanceStatus.HALF_DAY, response.getStatus());
        assertEquals(4.0, response.getWorkingHours());
    }

    @Test
    @DisplayName("Authorized contractor views attendance history")
    void getAttendanceHistory_asContractor_success() {
        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(1L)).thenReturn(Optional.of(contractor));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(laborAttendanceRepository.filterAttendance(100L, 10L, null, LocalDate.of(2026, 10, 6), null, null, AttendanceStatus.PRESENT))
                .thenReturn(List.of(sampleAttendance));

        List<LaborAttendanceResponse> history = laborAttendanceService.getAttendanceHistory(
                "contractor@example.com",
                100L,
                null,
                LocalDate.of(2026, 10, 6),
                null,
                null,
                AttendanceStatus.PRESENT
        );

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(100L, history.get(0).getProjectId());
    }

    @Test
    @DisplayName("Calculate attendance summary statistics correctly")
    void getAttendanceSummary_success() {
        LaborAttendance attPresent = new LaborAttendance(laborer, project, LocalDate.of(2026, 10, 6), 8.0, AttendanceStatus.PRESENT, "", contractorUser);
        LaborAttendance attHalf = new LaborAttendance(laborer, project, LocalDate.of(2026, 10, 6), 4.0, AttendanceStatus.HALF_DAY, "", contractorUser);
        LaborAttendance attOvertime = new LaborAttendance(laborer, project, LocalDate.of(2026, 10, 6), 11.0, AttendanceStatus.OVERTIME, "", contractorUser);
        LaborAttendance attAbsent = new LaborAttendance(laborer, project, LocalDate.of(2026, 10, 6), 0.0, AttendanceStatus.ABSENT, "", contractorUser);

        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(laborAttendanceRepository.filterAttendance(100L, null, null, LocalDate.of(2026, 10, 6), null, null, null))
                .thenReturn(List.of(attPresent, attHalf, attOvertime, attAbsent));

        LaborAttendanceSummaryResponse summary = laborAttendanceService.getAttendanceSummary(
                "contractor@example.com",
                100L,
                LocalDate.of(2026, 10, 6),
                null,
                null
        );

        assertNotNull(summary);
        assertEquals(4, summary.getTotalRecords());
        assertEquals(1, summary.getPresentCount());
        assertEquals(1, summary.getHalfDayCount());
        assertEquals(1, summary.getOvertimeCount());
        assertEquals(1, summary.getAbsentCount());
        assertEquals(23.0, summary.getTotalWorkingHours());
    }

    @Test
    @DisplayName("Delete attendance record removes it successfully")
    void deleteAttendance_success() {
        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(laborAttendanceRepository.findById(1L)).thenReturn(Optional.of(sampleAttendance));

        laborAttendanceService.deleteAttendance(1L, "contractor@example.com");

        verify(laborAttendanceRepository).delete(sampleAttendance);
    }
}
