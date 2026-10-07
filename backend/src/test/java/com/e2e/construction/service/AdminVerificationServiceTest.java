package com.e2e.construction.service;

import com.e2e.construction.dto.VerificationDecisionRequest;
import com.e2e.construction.dto.VerificationItemResponse;
import com.e2e.construction.dto.VerificationRecordResponse;
import com.e2e.construction.dto.VerificationSummaryStatsResponse;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.Laborer;
import com.e2e.construction.entity.Machinery;
import com.e2e.construction.entity.MachineryCategory;
import com.e2e.construction.entity.MachineryImage;
import com.e2e.construction.entity.MachineryImageType;
import com.e2e.construction.entity.MachineryOwner;
import com.e2e.construction.entity.Material;
import com.e2e.construction.entity.MaterialCategory;
import com.e2e.construction.entity.MaterialSupplier;
import com.e2e.construction.entity.Role;
import com.e2e.construction.entity.User;
import com.e2e.construction.entity.Notification;
import com.e2e.construction.entity.VerificationEntityType;
import com.e2e.construction.entity.VerificationRecord;
import com.e2e.construction.entity.VerificationStatus;
import com.e2e.construction.exception.ResourceNotFoundException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminVerificationServiceTest {

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
    private AdminVerificationService service;

    private User adminUser;
    private User regularUser;
    private Contractor sampleContractor;
    private Laborer sampleLaborer;
    private MachineryOwner sampleOwner;
    private Machinery sampleMachinery;
    private MaterialSupplier sampleSupplier;
    private Material sampleMaterial;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userRepository);

        service = new AdminVerificationService(
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

        Role adminRole = new Role("ADMIN", "Administrator");
        Role contractorRole = new Role("CONTRACTOR", "Contractor");
        Role laborerRole = new Role("LABORER", "Laborer");
        Role ownerRole = new Role("MACHINERY_OWNER", "Machinery Owner");
        Role supplierRole = new Role("MATERIAL_SUPPLIER", "Material Supplier");

        adminUser = new User(adminRole, "admin@test.com", "hash", "Admin", "User", "1111111111");
        adminUser.setId(1L);

        regularUser = new User(contractorRole, "regular@test.com", "hash", "John", "Doe", "2222222222");
        regularUser.setId(2L);

        sampleContractor = new Contractor(regularUser, "Apex Constructions", "123 Build Rd", "Austin", "TX", "Commercial builder", 10, "http://example.com/contractor.jpg");
        sampleContractor.setId(10L);
        sampleContractor.setVerificationStatus(VerificationStatus.PENDING);
        sampleContractor.setCreatedAt(LocalDateTime.now().minusDays(2));

        User laborerUser = new User(laborerRole, "laborer@test.com", "hash", "Ravi", "Kumar", "3333333333");
        laborerUser.setId(3L);
        sampleLaborer = new Laborer(laborerUser, "http://example.com/laborer.jpg", "Austin", "Austin", "TX", "Masonry", 5, BigDecimal.valueOf(500), "Experienced mason");
        sampleLaborer.setId(20L);
        sampleLaborer.setVerificationStatus(VerificationStatus.PENDING);
        sampleLaborer.setCreatedAt(LocalDateTime.now().minusDays(1));

        User ownerUser = new User(ownerRole, "owner@test.com", "hash", "Bob", "Owner", "4444444444");
        ownerUser.setId(4L);
        sampleOwner = new MachineryOwner(ownerUser, "Heavy Fleet Ltd", "TAX-9988", "Dallas", "456 Fleet Way", "Dallas", "TX", "Equipment rentals", "http://example.com/owner.jpg");
        sampleOwner.setId(30L);
        sampleOwner.setVerificationStatus(VerificationStatus.PENDING);
        sampleOwner.setCreatedAt(LocalDateTime.now().minusDays(3));

        sampleMachinery = new Machinery(sampleOwner, "CAT 320 Excavator", MachineryCategory.EARTHWORK, "Caterpillar", "320D", 2021, "20 Ton", "Diesel", "Hydraulic", "Dallas", BigDecimal.valueOf(1500), "Heavy excavator", null);
        sampleMachinery.setId(40L);
        sampleMachinery.setVerificationStatus(VerificationStatus.PENDING);
        sampleMachinery.setCreatedAt(LocalDateTime.now().minusDays(1));

        User supplierUser = new User(supplierRole, "supplier@test.com", "hash", "Alice", "Store", "5555555555");
        supplierUser.setId(5L);
        sampleSupplier = new MaterialSupplier(supplierUser, "BuildPro Supplies", "GST-7766", "5555555555", "789 Depot Rd", "Houston", "TX", true, "Concrete and steel");
        sampleSupplier.setId(50L);
        sampleSupplier.setVerificationStatus(VerificationStatus.PENDING);
        sampleSupplier.setCreatedAt(LocalDateTime.now().minusDays(2));

        sampleMaterial = new Material(sampleSupplier, "UltraTech 53 Grade Cement", MaterialCategory.CEMENT, "53 Grade", "50kg Bag", BigDecimal.valueOf(100), BigDecimal.valueOf(380), "Houston", "High strength cement");
        sampleMaterial.setId(60L);
        sampleMaterial.setVerificationStatus(VerificationStatus.PENDING);
        sampleMaterial.setCreatedAt(LocalDateTime.now().minusDays(1));
    }

    @Test
    @DisplayName("getItemsForVerification: should return items across all 6 entity types")
    void testGetItemsForVerification_AllTypes() {
        when(contractorRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING))
                .thenReturn(List.of(sampleContractor));
        when(laborerRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING))
                .thenReturn(List.of(sampleLaborer));
        when(machineryOwnerRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING))
                .thenReturn(List.of(sampleOwner));
        when(machineryRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING))
                .thenReturn(List.of(sampleMachinery));
        when(materialSupplierRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING))
                .thenReturn(List.of(sampleSupplier));
        when(materialRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING))
                .thenReturn(List.of(sampleMaterial));

        List<VerificationItemResponse> items = service.getItemsForVerification(null, VerificationStatus.PENDING, null);

        assertThat(items).hasSize(6);
    }

    @Test
    @DisplayName("getItemsForVerification: should filter by specific entity type")
    void testGetItemsForVerification_FilterByType() {
        when(contractorRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING))
                .thenReturn(List.of(sampleContractor));

        List<VerificationItemResponse> items = service.getItemsForVerification(VerificationEntityType.CONTRACTOR, VerificationStatus.PENDING, null);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getEntityType()).isEqualTo(VerificationEntityType.CONTRACTOR);
        assertThat(items.get(0).getTitle()).isEqualTo("Apex Constructions");
    }

    @Test
    @DisplayName("getItemForVerification: should return detailed item including uploaded images")
    void testGetItemForVerification_MachineryWithImages() {
        MachineryImage img1 = new MachineryImage();
        img1.setFileUrl("http://example.com/machinery-front.jpg");
        MachineryImage img2 = new MachineryImage();
        img2.setFileUrl("http://example.com/machinery-side.jpg");

        when(machineryRepository.findById(40L)).thenReturn(Optional.of(sampleMachinery));
        when(machineryImageRepository.findByMachineryIdOrderByCreatedAtDesc(40L)).thenReturn(List.of(img1, img2));

        VerificationItemResponse item = service.getItemForVerification(VerificationEntityType.MACHINERY, 40L);

        assertThat(item.getEntityId()).isEqualTo(40L);
        assertThat(item.getTitle()).isEqualTo("CAT 320 Excavator");
        assertThat(item.getImages()).containsExactly("http://example.com/machinery-front.jpg", "http://example.com/machinery-side.jpg");
        assertThat(item.getDetails()).containsKey("category");
        assertThat(item.getDetails().get("rentalPricePerDay")).isEqualTo(BigDecimal.valueOf(1500));
    }

    @Test
    @DisplayName("submitVerificationDecision: VERIFY action updates status and records reviewer")
    void testSubmitVerificationDecision_Verify() {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(contractorRepository.findById(10L)).thenReturn(Optional.of(sampleContractor));
        when(contractorRepository.save(any(Contractor.class))).thenAnswer(inv -> inv.getArgument(0));

        VerificationDecisionRequest request = new VerificationDecisionRequest(
                VerificationStatus.VERIFIED, null, "All licenses and permits confirmed"
        );

        VerificationItemResponse result = service.submitVerificationDecision(
                VerificationEntityType.CONTRACTOR, 10L, "admin@test.com", request
        );

        assertThat(result.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(sampleContractor.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);

        ArgumentCaptor<VerificationRecord> recordCaptor = ArgumentCaptor.forClass(VerificationRecord.class);
        verify(verificationRecordRepository).save(recordCaptor.capture());
        VerificationRecord savedRecord = recordCaptor.getValue();
        assertThat(savedRecord.getStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(savedRecord.getReviewer().getId()).isEqualTo(1L);
        assertThat(savedRecord.getRemarks()).isEqualTo("All licenses and permits confirmed");

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("submitVerificationDecision: REJECT action records reason and does NOT delete record")
    void testSubmitVerificationDecision_RejectDoesNotDelete() {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(machineryRepository.findById(40L)).thenReturn(Optional.of(sampleMachinery));
        when(machineryRepository.save(any(Machinery.class))).thenAnswer(inv -> inv.getArgument(0));

        VerificationDecisionRequest request = new VerificationDecisionRequest(
                VerificationStatus.REJECTED, "Invalid insurance and fitness certificates", "Please upload updated papers"
        );

        VerificationItemResponse result = service.submitVerificationDecision(
                VerificationEntityType.MACHINERY, 40L, "admin@test.com", request
        );

        assertThat(result.getVerificationStatus()).isEqualTo(VerificationStatus.REJECTED);
        assertThat(sampleMachinery.getVerificationStatus()).isEqualTo(VerificationStatus.REJECTED);

        // Verify entity was NOT deleted
        verify(machineryRepository, never()).delete(any());
        verify(machineryRepository, never()).deleteById(any());

        // Verify audit record was created with rejection reason
        ArgumentCaptor<VerificationRecord> recordCaptor = ArgumentCaptor.forClass(VerificationRecord.class);
        verify(verificationRecordRepository).save(recordCaptor.capture());
        VerificationRecord savedRecord = recordCaptor.getValue();
        assertThat(savedRecord.getStatus()).isEqualTo(VerificationStatus.REJECTED);
        assertThat(savedRecord.getRejectionReason()).isEqualTo("Invalid insurance and fitness certificates");

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("submitVerificationDecision: KEEP PENDING action sets status back to PENDING")
    void testSubmitVerificationDecision_KeepPending() {
        sampleLaborer.setVerificationStatus(VerificationStatus.REJECTED);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(laborerRepository.findById(20L)).thenReturn(Optional.of(sampleLaborer));
        when(laborerRepository.save(any(Laborer.class))).thenAnswer(inv -> inv.getArgument(0));

        VerificationDecisionRequest request = new VerificationDecisionRequest(
                VerificationStatus.PENDING, null, "Under manual audit"
        );

        VerificationItemResponse result = service.submitVerificationDecision(
                VerificationEntityType.LABORER, 20L, "admin@test.com", request
        );

        assertThat(result.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);
        assertThat(sampleLaborer.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);
    }

    @Test
    @DisplayName("submitVerificationDecision: Non-admin throws AccessDeniedException")
    void testSubmitVerificationDecision_NonAdminDenied() {
        when(userRepository.findByEmail("regular@test.com")).thenReturn(Optional.of(regularUser));

        VerificationDecisionRequest request = new VerificationDecisionRequest(
                VerificationStatus.VERIFIED, null, null
        );

        assertThrows(AccessDeniedException.class, () ->
                service.submitVerificationDecision(VerificationEntityType.CONTRACTOR, 10L, "regular@test.com", request)
        );
    }

    @Test
    @DisplayName("getVerificationHistory: returns audit history for entity")
    void testGetVerificationHistory() {
        VerificationRecord r1 = new VerificationRecord(
                VerificationEntityType.CONTRACTOR, 10L, adminUser, VerificationStatus.REJECTED,
                LocalDateTime.now().minusDays(1), "Incomplete license", "Fix license"
        );
        r1.setId(101L);

        when(verificationRecordRepository.findByEntityTypeAndEntityIdOrderByVerificationDateDesc(VerificationEntityType.CONTRACTOR, 10L))
                .thenReturn(List.of(r1));

        List<VerificationRecordResponse> history = service.getVerificationHistory(VerificationEntityType.CONTRACTOR, 10L);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getRejectionReason()).isEqualTo("Incomplete license");
    }

    @Test
    @DisplayName("getVerificationStats: returns dashboard aggregate counts")
    void testGetVerificationStats() {
        when(contractorRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING)).thenReturn(List.of(sampleContractor));
        when(contractorRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.VERIFIED)).thenReturn(Collections.emptyList());
        when(contractorRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.REJECTED)).thenReturn(Collections.emptyList());

        when(laborerRepository.findByVerificationStatusOrderByCreatedAtDesc(any())).thenReturn(Collections.emptyList());
        when(machineryOwnerRepository.findByVerificationStatusOrderByCreatedAtDesc(any())).thenReturn(Collections.emptyList());
        when(machineryRepository.findByVerificationStatusOrderByCreatedAtDesc(any())).thenReturn(Collections.emptyList());
        when(materialSupplierRepository.findByVerificationStatusOrderByCreatedAtDesc(any())).thenReturn(Collections.emptyList());
        when(materialRepository.findByVerificationStatusOrderByCreatedAtDesc(any())).thenReturn(Collections.emptyList());

        VerificationSummaryStatsResponse stats = service.getVerificationStats();

        assertThat(stats.getPendingCount()).isEqualTo(1L);
        assertThat(stats.getPendingByEntityType()).containsEntry("CONTRACTOR", 1L);
    }
}
