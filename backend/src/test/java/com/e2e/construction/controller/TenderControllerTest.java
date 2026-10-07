package com.e2e.construction.controller;

import com.e2e.construction.dto.TenderApplicationRequest;
import com.e2e.construction.dto.TenderApplicationReviewRequest;
import com.e2e.construction.dto.TenderRequest;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.Role;
import com.e2e.construction.entity.Tender;
import com.e2e.construction.entity.TenderApplication;
import com.e2e.construction.entity.TenderApplicationStatus;
import com.e2e.construction.entity.TenderStatus;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.GlobalExceptionHandler;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.TenderApplicationDocumentRepository;
import com.e2e.construction.repository.TenderApplicationRepository;
import com.e2e.construction.repository.TenderDocumentRepository;
import com.e2e.construction.repository.TenderRepository;
import com.e2e.construction.repository.UserRepository;
import com.e2e.construction.service.FileStorageService;
import com.e2e.construction.service.TenderService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TenderControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private TenderRepository tenderRepository;

    @Mock
    private TenderDocumentRepository tenderDocumentRepository;

    @Mock
    private TenderApplicationRepository tenderApplicationRepository;

    @Mock
    private TenderApplicationDocumentRepository tenderApplicationDocumentRepository;

    @Mock
    private ContractorRepository contractorRepository;

    @Mock
    private UserRepository userRepository;

    private FileStorageService fileStorageService;
    private TenderService tenderService;
    private TenderController tenderController;

    private Principal adminPrincipal;
    private Principal contractorPrincipal;
    private User adminUser;
    private User contractorUser;
    private Contractor contractor;
    private Tender sampleTender;
    private TenderApplication sampleApplication;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService("target/test-uploads-tenders-ctrl");
        tenderService = new TenderService(
                tenderRepository,
                tenderDocumentRepository,
                tenderApplicationRepository,
                tenderApplicationDocumentRepository,
                contractorRepository,
                userRepository,
                fileStorageService
        );
        tenderController = new TenderController(tenderService);

        mockMvc = MockMvcBuilders.standaloneSetup(tenderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        adminPrincipal = () -> "admin@example.com";
        contractorPrincipal = () -> "contractor@example.com";

        Role adminRole = new Role("ADMIN", "Admin role");
        Role contractorRole = new Role("CONTRACTOR", "Contractor role");

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(adminRole);

        contractorUser = new User();
        contractorUser.setId(2L);
        contractorUser.setEmail("contractor@example.com");
        contractorUser.setFirstName("Bob");
        contractorUser.setLastName("Builder");
        contractorUser.setRole(contractorRole);

        contractor = new Contractor();
        contractor.setId(10L);
        contractor.setUser(contractorUser);
        contractor.setCompanyName("Apex Infrastructure Ltd");

        sampleTender = new Tender(
                "TND-2026-001",
                "Metro Elevated Viaduct Construction",
                "Public Works Department",
                "Metro City",
                "Civil Infrastructure",
                new BigDecimal("50000000.00"),
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 11, 30),
                "Class-A Civil Contractor license",
                "Elevated viaduct construction",
                TenderStatus.OPEN,
                adminUser
        );
        sampleTender.setId(100L);

        sampleApplication = new TenderApplication(
                sampleTender,
                contractor,
                new BigDecimal("48000000.00"),
                365,
                "Commercial bid submission",
                LocalDate.of(2026, 10, 6)
        );
        sampleApplication.setId(500L);
    }

    @Test
    @DisplayName("POST /api/tenders creates tender as Admin")
    void createTender_success() throws Exception {
        TenderRequest request = new TenderRequest(
                "TND-2026-002",
                "Expressway Construction",
                "Highway Dept",
                "State Zone",
                "Roads",
                new BigDecimal("30000000.00"),
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 12, 1),
                "Criteria",
                "Description of highway",
                TenderStatus.OPEN,
                null
        );

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(tenderRepository.existsByTenderNumber("TND-2026-002")).thenReturn(false);
        when(tenderRepository.save(any(Tender.class))).thenAnswer(invocation -> {
            Tender t = invocation.getArgument(0);
            t.setId(102L);
            return t;
        });

        mockMvc.perform(post("/api/tenders")
                        .principal(adminPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(102)))
                .andExpect(jsonPath("$.tenderNumber", is("TND-2026-002")))
                .andExpect(jsonPath("$.status", is("OPEN")));
    }

    @Test
    @DisplayName("GET /api/tenders searches and filters tenders")
    void searchAndFilterTenders_success() throws Exception {
        when(tenderRepository.searchAndFilterTenders(
                eq("Metro"), eq(TenderStatus.OPEN), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(sampleTender));

        mockMvc.perform(get("/api/tenders")
                        .param("search", "Metro")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tenderNumber", is("TND-2026-001")));
    }

    @Test
    @DisplayName("GET /api/tenders/{id} views tender by ID")
    void getTenderById_success() throws Exception {
        when(tenderRepository.findById(100L)).thenReturn(Optional.of(sampleTender));

        mockMvc.perform(get("/api/tenders/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.title", is("Metro Elevated Viaduct Construction")));
    }

    @Test
    @DisplayName("POST /api/tenders/{id}/apply submits bid application as Contractor")
    void applyForTender_success() throws Exception {
        TenderApplicationRequest request = new TenderApplicationRequest(
                new BigDecimal("48000000.00"),
                365,
                "Official bid submission",
                null
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(2L)).thenReturn(Optional.of(contractor));
        when(tenderRepository.findById(100L)).thenReturn(Optional.of(sampleTender));
        when(tenderApplicationRepository.existsByTenderIdAndContractorId(100L, 10L)).thenReturn(false);
        when(tenderApplicationRepository.save(any(TenderApplication.class))).thenAnswer(invocation -> {
            TenderApplication app = invocation.getArgument(0);
            app.setId(500L);
            return app;
        });

        mockMvc.perform(post("/api/tenders/100/apply")
                        .principal(contractorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(500)))
                .andExpect(jsonPath("$.tenderNumber", is("TND-2026-001")))
                .andExpect(jsonPath("$.status", is("SUBMITTED")))
                .andExpect(jsonPath("$.bidAmount", is(48000000.00)));
    }

    @Test
    @DisplayName("GET /api/tenders/applications/my-applications tracks contractor applications")
    void getMyApplications_success() throws Exception {
        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(2L)).thenReturn(Optional.of(contractor));
        when(tenderApplicationRepository.findByContractorIdOrderBySubmissionDateDesc(10L))
                .thenReturn(List.of(sampleApplication));

        mockMvc.perform(get("/api/tenders/applications/my-applications")
                        .principal(contractorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(500)))
                .andExpect(jsonPath("$[0].status", is("SUBMITTED")));
    }

    @Test
    @DisplayName("PATCH /api/tenders/applications/{id}/status reviews application as Admin")
    void reviewApplication_success() throws Exception {
        TenderApplicationReviewRequest reviewRequest = new TenderApplicationReviewRequest(
                TenderApplicationStatus.ACCEPTED,
                "Approved after technical review"
        );

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(tenderApplicationRepository.findById(500L)).thenReturn(Optional.of(sampleApplication));
        when(tenderApplicationRepository.save(any(TenderApplication.class))).thenReturn(sampleApplication);

        mockMvc.perform(patch("/api/tenders/applications/500/status")
                        .principal(adminPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACCEPTED")))
                .andExpect(jsonPath("$.reviewerRemarks", is("Approved after technical review")));
    }

    @Test
    @DisplayName("POST /api/tenders/{id}/documents uploads official documents as Admin")
    void uploadTenderDocuments_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "notice.pdf", "application/pdf", "dummy pdf notice".getBytes()
        );

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(tenderRepository.findById(100L)).thenReturn(Optional.of(sampleTender));
        when(tenderDocumentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(multipart("/api/tenders/100/documents")
                        .file(file)
                        .principal(adminPrincipal))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fileName", is("notice.pdf")));
    }

    @Test
    @DisplayName("DELETE /api/tenders/{id} deletes tender as Admin")
    void deleteTender_success() throws Exception {
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(tenderRepository.findById(100L)).thenReturn(Optional.of(sampleTender));

        mockMvc.perform(delete("/api/tenders/100")
                        .principal(adminPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Tender deleted successfully")))
                .andExpect(jsonPath("$.tenderId", is("100")));
    }
}
