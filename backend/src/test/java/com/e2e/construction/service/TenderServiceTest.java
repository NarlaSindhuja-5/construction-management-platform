package com.e2e.construction.service;

import com.e2e.construction.dto.TenderApplicationRequest;
import com.e2e.construction.dto.TenderApplicationResponse;
import com.e2e.construction.dto.TenderApplicationReviewRequest;
import com.e2e.construction.dto.TenderRequest;
import com.e2e.construction.dto.TenderResponse;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.Role;
import com.e2e.construction.entity.Tender;
import com.e2e.construction.entity.TenderApplication;
import com.e2e.construction.entity.TenderApplicationDocument;
import com.e2e.construction.entity.TenderApplicationStatus;
import com.e2e.construction.entity.TenderStatus;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.TenderApplicationDocumentRepository;
import com.e2e.construction.repository.TenderApplicationRepository;
import com.e2e.construction.repository.TenderDocumentRepository;
import com.e2e.construction.repository.TenderRepository;
import com.e2e.construction.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class TenderServiceTest {

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

    private User adminUser;
    private User contractorUser;
    private Contractor contractor;
    private Tender sampleTender;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService("target/test-uploads-tenders");
        tenderService = new TenderService(
                tenderRepository,
                tenderDocumentRepository,
                tenderApplicationRepository,
                tenderApplicationDocumentRepository,
                contractorRepository,
                userRepository,
                fileStorageService
        );

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
                "Class-A Civil Contractor license, Min 5 yrs experience",
                "Construction of 12km elevated metro viaduct including piers and pre-stressed girders.",
                TenderStatus.OPEN,
                adminUser
        );
        sampleTender.setId(100L);
    }

    @Test
    @DisplayName("Admin creates a tender successfully")
    void createTender_asAdmin_success() {
        TenderRequest request = new TenderRequest(
                "TND-2026-002",
                "Highway Bridge Expansion",
                "National Highways Authority",
                "Highway Zone 4",
                "Bridges & Highways",
                new BigDecimal("25000000.00"),
                LocalDate.of(2026, 10, 5),
                LocalDate.of(2026, 12, 15),
                "ISO 9001, Min 3 major bridge projects completed",
                "Widening of bridge from 2 to 4 lanes over river.",
                TenderStatus.OPEN,
                List.of("https://example.com/rfp-bridge.pdf")
        );

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(tenderRepository.existsByTenderNumber("TND-2026-002")).thenReturn(false);
        when(tenderRepository.save(any(Tender.class))).thenAnswer(invocation -> {
            Tender t = invocation.getArgument(0);
            t.setId(101L);
            return t;
        });

        TenderResponse response = tenderService.createTender("admin@example.com", request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("TND-2026-002", response.getTenderNumber());
        assertEquals("Highway Bridge Expansion", response.getTitle());
        assertEquals(TenderStatus.OPEN, response.getStatus());
        assertEquals(1, response.getDocuments().size());
        verify(tenderRepository).save(any(Tender.class));
    }

    @Test
    @DisplayName("Non-admin cannot create tender")
    void createTender_nonAdmin_throwsAccessDenied() {
        TenderRequest request = new TenderRequest();
        request.setTenderNumber("TND-FAIL");

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));

        assertThrows(AccessDeniedException.class, () ->
                tenderService.createTender("contractor@example.com", request));

        verify(tenderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Duplicate tender number is rejected")
    void createTender_duplicateNumber_throwsBadRequest() {
        TenderRequest request = new TenderRequest(
                "TND-2026-001",
                "Duplicate Tender",
                "Dept",
                "City",
                "Category",
                new BigDecimal("1000.00"),
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                "Criteria",
                "Desc",
                TenderStatus.OPEN,
                null
        );

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(tenderRepository.existsByTenderNumber("TND-2026-001")).thenReturn(true);

        assertThrows(BadRequestException.class, () ->
                tenderService.createTender("admin@example.com", request));
    }

    @Test
    @DisplayName("Tender with closing date before published date is rejected")
    void createTender_closingDateBeforePublished_throwsBadRequest() {
        TenderRequest request = new TenderRequest(
                "TND-INVALID-DATES",
                "Invalid Dates Tender",
                "Dept",
                "City",
                "Category",
                new BigDecimal("1000.00"),
                LocalDate.of(2026, 10, 20),
                LocalDate.of(2026, 10, 10), // Closing before published
                "Criteria",
                "Desc",
                TenderStatus.OPEN,
                null
        );

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(tenderRepository.existsByTenderNumber("TND-INVALID-DATES")).thenReturn(false);

        assertThrows(BadRequestException.class, () ->
                tenderService.createTender("admin@example.com", request));
    }

    @Test
    @DisplayName("Contractor applies for an OPEN tender successfully")
    void applyForTender_success() {
        TenderApplicationRequest request = new TenderApplicationRequest(
                new BigDecimal("48500000.00"),
                365,
                "Apex Infrastructure is pleased to submit our commercial and technical bid.",
                List.of("https://example.com/company-profile.pdf")
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(2L)).thenReturn(Optional.of(contractor));
        when(tenderRepository.findById(100L)).thenReturn(Optional.of(sampleTender));
        when(tenderApplicationRepository.existsByTenderIdAndContractorId(100L, 10L)).thenReturn(false);
        when(tenderApplicationRepository.save(any(TenderApplication.class))).thenAnswer(invocation -> {
            TenderApplication app = invocation.getArgument(0);
            app.setId(50L);
            return app;
        });

        TenderApplicationResponse response = tenderService.applyForTender(100L, "contractor@example.com", request);

        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals("TND-2026-001", response.getTenderNumber());
        assertEquals("Apex Infrastructure Ltd", response.getContractorCompanyName());
        assertEquals(new BigDecimal("48500000.00"), response.getBidAmount());
        assertEquals(TenderApplicationStatus.SUBMITTED, response.getStatus());
        assertEquals(1, response.getDocuments().size());
    }

    @Test
    @DisplayName("Prevent applications after the closing date")
    void applyForTender_pastClosingDate_throwsBadRequest() {
        // Expired tender
        sampleTender.setClosingDate(LocalDate.now().minusDays(1));

        TenderApplicationRequest request = new TenderApplicationRequest(
                new BigDecimal("45000000.00"), 300, "Late bid", null
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(2L)).thenReturn(Optional.of(contractor));
        when(tenderRepository.findById(100L)).thenReturn(Optional.of(sampleTender));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                tenderService.applyForTender(100L, "contractor@example.com", request));

        assertNotNull(ex.getMessage());
        assertEquals(TenderStatus.CLOSED, sampleTender.getStatus());
        verify(tenderApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Prevent applications when tender status is not OPEN")
    void applyForTender_statusCancelled_throwsBadRequest() {
        sampleTender.setStatus(TenderStatus.CANCELLED);

        TenderApplicationRequest request = new TenderApplicationRequest(
                new BigDecimal("45000000.00"), 300, "Bid on cancelled", null
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(2L)).thenReturn(Optional.of(contractor));
        when(tenderRepository.findById(100L)).thenReturn(Optional.of(sampleTender));

        assertThrows(BadRequestException.class, () ->
                tenderService.applyForTender(100L, "contractor@example.com", request));
    }

    @Test
    @DisplayName("Prevent duplicate applications by the same contractor for the same tender")
    void applyForTender_duplicateBid_throwsBadRequest() {
        TenderApplicationRequest request = new TenderApplicationRequest(
                new BigDecimal("45000000.00"), 300, "Second bid", null
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(2L)).thenReturn(Optional.of(contractor));
        when(tenderRepository.findById(100L)).thenReturn(Optional.of(sampleTender));
        when(tenderApplicationRepository.existsByTenderIdAndContractorId(100L, 10L)).thenReturn(true);

        assertThrows(BadRequestException.class, () ->
                tenderService.applyForTender(100L, "contractor@example.com", request));
    }

    @Test
    @DisplayName("Contractor uploads application documents")
    void uploadApplicationDocuments_success() {
        TenderApplication app = new TenderApplication(
                sampleTender, contractor, new BigDecimal("48000000.00"), 300, "Cover", LocalDate.now()
        );
        app.setId(50L);

        MockMultipartFile file = new MockMultipartFile(
                "files", "technical_bid.pdf", "application/pdf", "dummy pdf content".getBytes()
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(2L)).thenReturn(Optional.of(contractor));
        when(tenderApplicationRepository.findById(50L)).thenReturn(Optional.of(app));
        when(tenderApplicationDocumentRepository.save(any(TenderApplicationDocument.class)))
                .thenAnswer(i -> i.getArgument(0));

        var uploadedDocs = tenderService.uploadApplicationDocuments(
                50L, "contractor@example.com", List.of(file), "TECHNICAL_PROPOSAL"
        );

        assertNotNull(uploadedDocs);
        assertEquals(1, uploadedDocs.size());
        assertEquals("technical_bid.pdf", uploadedDocs.get(0).getFileName());
        assertEquals("TECHNICAL_PROPOSAL", uploadedDocs.get(0).getDocumentType());
    }

    @Test
    @DisplayName("Contractor tracks submitted applications")
    void getMyApplications_success() {
        TenderApplication app = new TenderApplication(
                sampleTender, contractor, new BigDecimal("48000000.00"), 300, "Cover", LocalDate.now()
        );
        app.setId(50L);

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(2L)).thenReturn(Optional.of(contractor));
        when(tenderApplicationRepository.findByContractorIdOrderBySubmissionDateDesc(10L))
                .thenReturn(List.of(app));

        List<TenderApplicationResponse> myApps = tenderService.getMyApplications("contractor@example.com", null);

        assertNotNull(myApps);
        assertEquals(1, myApps.size());
        assertEquals(50L, myApps.get(0).getId());
        assertEquals("TND-2026-001", myApps.get(0).getTenderNumber());
        assertEquals(TenderApplicationStatus.SUBMITTED, myApps.get(0).getStatus());
    }

    @Test
    @DisplayName("Admin reviews application status")
    void reviewApplication_asAdmin_success() {
        TenderApplication app = new TenderApplication(
                sampleTender, contractor, new BigDecimal("48000000.00"), 300, "Cover", LocalDate.now()
        );
        app.setId(50L);

        TenderApplicationReviewRequest reviewRequest = new TenderApplicationReviewRequest(
                TenderApplicationStatus.ACCEPTED,
                "Bid meets technical specs and offers lowest competitive price."
        );

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(tenderApplicationRepository.findById(50L)).thenReturn(Optional.of(app));
        when(tenderApplicationRepository.save(app)).thenReturn(app);

        TenderApplicationResponse response = tenderService.reviewApplication(50L, "admin@example.com", reviewRequest);

        assertNotNull(response);
        assertEquals(TenderApplicationStatus.ACCEPTED, response.getStatus());
        assertEquals("Bid meets technical specs and offers lowest competitive price.", response.getReviewerRemarks());
    }

    @Test
    @DisplayName("Search and filter tenders with multi-criteria filters")
    void searchAndFilterTenders_success() {
        when(tenderRepository.searchAndFilterTenders(
                "Metro", TenderStatus.OPEN, "Civil Infrastructure", null, null, null, null, null, null
        )).thenReturn(List.of(sampleTender));

        List<TenderResponse> results = tenderService.searchAndFilterTenders(
                "Metro", TenderStatus.OPEN, "Civil Infrastructure", null, null, null, null, null, null
        );

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("TND-2026-001", results.get(0).getTenderNumber());
    }
}
