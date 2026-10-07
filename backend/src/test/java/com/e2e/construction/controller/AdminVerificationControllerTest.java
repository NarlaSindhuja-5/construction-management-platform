package com.e2e.construction.controller;

import com.e2e.construction.dto.VerificationDecisionRequest;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.Role;
import com.e2e.construction.entity.User;
import com.e2e.construction.entity.VerificationEntityType;
import com.e2e.construction.entity.VerificationRecord;
import com.e2e.construction.entity.VerificationStatus;
import com.e2e.construction.exception.GlobalExceptionHandler;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.LaborerRepository;
import com.e2e.construction.repository.MachineryImageRepository;
import com.e2e.construction.repository.MachineryOwnerRepository;
import com.e2e.construction.repository.MachineryRepository;
import com.e2e.construction.repository.MaterialRepository;
import com.e2e.construction.repository.MaterialSupplierRepository;
import com.e2e.construction.repository.NotificationRepository;
import com.e2e.construction.repository.UserRepository;
import com.e2e.construction.repository.VerificationRecordRepository;
import com.e2e.construction.service.AdminVerificationService;
import com.e2e.construction.service.NotificationService;
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

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminVerificationControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private VerificationRecordRepository verificationRecordRepository;
    @Mock
    private ContractorRepository contractorRepository;
    @Mock
    private LaborerRepository laborerRepository;
    @Mock
    private MachineryOwnerRepository machineryOwnerRepository;
    @Mock
    private MachineryRepository machineryRepository;
    @Mock
    private MachineryImageRepository machineryImageRepository;
    @Mock
    private MaterialSupplierRepository materialSupplierRepository;
    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;
    private AdminVerificationService adminVerificationService;
    private AdminVerificationController adminVerificationController;

    private Principal principal;
    private User adminUser;
    private Contractor sampleContractor;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userRepository);

        adminVerificationService = new AdminVerificationService(
                verificationRecordRepository,
                contractorRepository,
                laborerRepository,
                machineryOwnerRepository,
                machineryRepository,
                machineryImageRepository,
                materialSupplierRepository,
                materialRepository,
                userRepository,
                notificationService
        );

        adminVerificationController = new AdminVerificationController(adminVerificationService);

        mockMvc = MockMvcBuilders.standaloneSetup(adminVerificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Role adminRole = new Role("ADMIN", "Admin role");
        Role contractorRole = new Role("CONTRACTOR", "Contractor role");

        adminUser = new User(adminRole, "admin@test.com", "hash", "Super", "Admin", "1111111111");
        adminUser.setId(1L);

        User contractorUser = new User(contractorRole, "contractor@test.com", "hash", "Bob", "Builder", "2222222222");
        contractorUser.setId(2L);

        sampleContractor = new Contractor(contractorUser, "Bob's Mega Builds", "100 Industry Blvd", "Austin", "TX", "Commercial", 8, "http://example.com/logo.jpg");
        sampleContractor.setId(10L);
        sampleContractor.setVerificationStatus(VerificationStatus.PENDING);
        sampleContractor.setCreatedAt(LocalDateTime.now());

        principal = () -> "admin@test.com";
    }

    @Test
    @DisplayName("GET /api/admin/verifications - should return list of items to review")
    void testGetItemsForVerification() throws Exception {
        when(contractorRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING))
                .thenReturn(List.of(sampleContractor));
        when(laborerRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(machineryOwnerRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(machineryRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(materialSupplierRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(materialRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/verifications")
                        .param("status", "PENDING")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].entityId", is(10)))
                .andExpect(jsonPath("$[0].title", is("Bob's Mega Builds")))
                .andExpect(jsonPath("$[0].entityType", is("CONTRACTOR")))
                .andExpect(jsonPath("$[0].verificationStatus", is("PENDING")))
                .andExpect(jsonPath("$[0].images[0]", is("http://example.com/logo.jpg")));
    }

    @Test
    @DisplayName("GET /api/admin/verifications/{entityType}/{entityId} - should return single item with full metadata")
    void testGetItemForVerification() throws Exception {
        when(contractorRepository.findById(10L)).thenReturn(Optional.of(sampleContractor));

        mockMvc.perform(get("/api/admin/verifications/CONTRACTOR/10")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId", is(10)))
                .andExpect(jsonPath("$.entityType", is("CONTRACTOR")))
                .andExpect(jsonPath("$.title", is("Bob's Mega Builds")))
                .andExpect(jsonPath("$.details.yearsOfExperience", is(8)))
                .andExpect(jsonPath("$.images[0]", is("http://example.com/logo.jpg")));
    }

    @Test
    @DisplayName("POST /api/admin/verifications/{entityType}/{entityId} - should verify item and record audit")
    void testSubmitVerificationDecision_Post() throws Exception {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(contractorRepository.findById(10L)).thenReturn(Optional.of(sampleContractor));
        when(contractorRepository.save(any(Contractor.class))).thenAnswer(inv -> inv.getArgument(0));

        VerificationDecisionRequest request = new VerificationDecisionRequest(
                VerificationStatus.VERIFIED, null, "Approved by admin"
        );

        mockMvc.perform(post("/api/admin/verifications/CONTRACTOR/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId", is(10)))
                .andExpect(jsonPath("$.verificationStatus", is("VERIFIED")));
    }

    @Test
    @DisplayName("PUT /api/admin/verifications/{entityType}/{entityId} - should reject item with reason")
    void testSubmitVerificationDecision_Put() throws Exception {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(contractorRepository.findById(10L)).thenReturn(Optional.of(sampleContractor));
        when(contractorRepository.save(any(Contractor.class))).thenAnswer(inv -> inv.getArgument(0));

        VerificationDecisionRequest request = new VerificationDecisionRequest(
                VerificationStatus.REJECTED, "Invalid commercial license", "Please update"
        );

        mockMvc.perform(put("/api/admin/verifications/CONTRACTOR/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId", is(10)))
                .andExpect(jsonPath("$.verificationStatus", is("REJECTED")));
    }

    @Test
    @DisplayName("GET /api/admin/verifications/{entityType}/{entityId}/history - should return audit history")
    void testGetVerificationHistory() throws Exception {
        VerificationRecord record = new VerificationRecord(
                VerificationEntityType.CONTRACTOR, 10L, adminUser, VerificationStatus.VERIFIED,
                LocalDateTime.now(), null, "Initial pass"
        );
        record.setId(100L);

        when(verificationRecordRepository.findByEntityTypeAndEntityIdOrderByVerificationDateDesc(VerificationEntityType.CONTRACTOR, 10L))
                .thenReturn(List.of(record));

        mockMvc.perform(get("/api/admin/verifications/CONTRACTOR/10/history")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(100)))
                .andExpect(jsonPath("$[0].status", is("VERIFIED")))
                .andExpect(jsonPath("$[0].reviewerEmail", is("admin@test.com")));
    }

    @Test
    @DisplayName("GET /api/admin/verifications/summary - should return verification counts")
    void testGetVerificationStats() throws Exception {
        when(contractorRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING)).thenReturn(List.of(sampleContractor));
        when(contractorRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.VERIFIED)).thenReturn(Collections.emptyList());
        when(contractorRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.REJECTED)).thenReturn(Collections.emptyList());

        when(laborerRepository.findByVerificationStatusOrderByCreatedAtDesc(any())).thenReturn(Collections.emptyList());
        when(machineryOwnerRepository.findByVerificationStatusOrderByCreatedAtDesc(any())).thenReturn(Collections.emptyList());
        when(machineryRepository.findByVerificationStatusOrderByCreatedAtDesc(any())).thenReturn(Collections.emptyList());
        when(materialSupplierRepository.findByVerificationStatusOrderByCreatedAtDesc(any())).thenReturn(Collections.emptyList());
        when(materialRepository.findByVerificationStatusOrderByCreatedAtDesc(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/verifications/summary")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingCount", is(1)))
                .andExpect(jsonPath("$.pendingByEntityType.CONTRACTOR", is(1)));
    }
}
