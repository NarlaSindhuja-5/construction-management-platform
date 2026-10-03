package com.e2e.construction.service;

import com.e2e.construction.dto.MaterialOrderRequest;
import com.e2e.construction.dto.MaterialOrderResponse;
import com.e2e.construction.dto.MaterialRequest;
import com.e2e.construction.dto.MaterialResponse;
import com.e2e.construction.dto.PriceUpdateRequest;
import com.e2e.construction.dto.StockHistoryResponse;
import com.e2e.construction.dto.StockUpdateRequest;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.Material;
import com.e2e.construction.entity.MaterialAvailability;
import com.e2e.construction.entity.MaterialCategory;
import com.e2e.construction.entity.MaterialOrder;
import com.e2e.construction.entity.MaterialOrderStatus;
import com.e2e.construction.entity.MaterialStockHistory;
import com.e2e.construction.entity.MaterialSupplier;
import com.e2e.construction.entity.Project;
import com.e2e.construction.entity.Role;
import com.e2e.construction.entity.StockChangeType;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.MaterialOrderRepository;
import com.e2e.construction.repository.MaterialRepository;
import com.e2e.construction.repository.MaterialStockHistoryRepository;
import com.e2e.construction.repository.MaterialSupplierRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MaterialStockHistoryRepository materialStockHistoryRepository;

    @Mock
    private MaterialOrderRepository materialOrderRepository;

    @Mock
    private MaterialSupplierRepository materialSupplierRepository;

    @Mock
    private ContractorRepository contractorRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    private MaterialService service;

    private User supplierUser;
    private User contractorUser;
    private MaterialSupplier supplier;
    private Contractor contractor;
    private Material cementMaterial;
    private Project project;

    @BeforeEach
    void setUp() {
        service = new MaterialService(
                materialRepository,
                materialStockHistoryRepository,
                materialOrderRepository,
                materialSupplierRepository,
                contractorRepository,
                projectRepository,
                userRepository
        );

        // Supplier User & Entity
        Role supplierRole = new Role("MATERIAL_SUPPLIER", "Material Supplier");
        supplierUser = new User();
        supplierUser.setId(30L);
        supplierUser.setEmail("supplier@materials.com");
        supplierUser.setFirstName("Sarah");
        supplierUser.setLastName("Stone");
        supplierUser.setPhoneNumber("+1-555-3003");
        supplierUser.setRole(supplierRole);

        supplier = new MaterialSupplier();
        supplier.setId(300L);
        supplier.setUser(supplierUser);
        supplier.setStoreName("Apex Aggregate & Cement Supply");
        supplier.setCity("Austin");
        supplier.setState("TX");
        supplier.setWarehouseAddress("800 Industrial Blvd");

        // Contractor User & Entity
        Role contractorRole = new Role("CONTRACTOR", "Contractor");
        contractorUser = new User();
        contractorUser.setId(10L);
        contractorUser.setEmail("contractor@builder.com");
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
        project.setContractor(contractor);
        project.setName("Commercial Hub");

        // Material (Cement)
        cementMaterial = new Material();
        cementMaterial.setId(1001L);
        cementMaterial.setSupplier(supplier);
        cementMaterial.setName("UltraTech 53 Grade OPC Cement");
        cementMaterial.setCategory(MaterialCategory.CEMENT);
        cementMaterial.setGradeSpecification("OPC 53");
        cementMaterial.setUnit("BAG");
        cementMaterial.setAvailableQuantity(BigDecimal.valueOf(500.00));
        cementMaterial.setPrice(BigDecimal.valueOf(380.00));
        cementMaterial.setLocation("Austin, TX - Depot 1");
        cementMaterial.setDescription("High early strength 53 grade cement");
        cementMaterial.setAvailability(MaterialAvailability.IN_STOCK);
    }

    @Test
    @DisplayName("Supplier creates material listing and initializes stock history")
    void testCreateMaterialSuccess() {
        MaterialRequest request = new MaterialRequest(
                "TMT Steel Fe 500D",
                MaterialCategory.STEEL,
                "Fe 500D - 12mm",
                "TON",
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(62000.00),
                "Austin Depot 2",
                "High tensile reinforcement steel bars"
        );

        when(userRepository.findByEmail("supplier@materials.com")).thenReturn(Optional.of(supplierUser));
        when(materialSupplierRepository.findByUserId(30L)).thenReturn(Optional.of(supplier));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> {
            Material m = invocation.getArgument(0);
            m.setId(1002L);
            return m;
        });

        MaterialResponse response = service.createMaterial(request, "supplier@materials.com");

        assertNotNull(response);
        assertEquals(1002L, response.getId());
        assertEquals("TMT Steel Fe 500D", response.getName());
        assertEquals(MaterialCategory.STEEL, response.getCategory());
        assertEquals(BigDecimal.valueOf(100.00), response.getAvailableQuantity());
        assertEquals(MaterialAvailability.IN_STOCK, response.getAvailability());

        // Verify stock history logged INITIAL_STOCK
        verify(materialStockHistoryRepository).save(argThat(h ->
                h.getChangeType() == StockChangeType.INITIAL_STOCK &&
                h.getNewQuantity().compareTo(BigDecimal.valueOf(100.00)) == 0 &&
                h.getPreviousQuantity().compareTo(BigDecimal.ZERO) == 0
        ));
    }

    @Test
    @DisplayName("Supplier updates stock and creates stock history record")
    void testUpdateStockSuccess() {
        StockUpdateRequest request = new StockUpdateRequest(
                BigDecimal.valueOf(200.00),
                true, // delta (+200)
                "Received fresh shipment from kiln"
        );

        when(materialRepository.findById(1001L)).thenReturn(Optional.of(cementMaterial));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));

        MaterialResponse response = service.updateStock(1001L, request, "supplier@materials.com");

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(700.00), response.getAvailableQuantity()); // 500 + 200
        assertEquals(MaterialAvailability.IN_STOCK, response.getAvailability());

        verify(materialStockHistoryRepository).save(argThat(h ->
                h.getChangeType() == StockChangeType.RESTOCK &&
                h.getPreviousQuantity().compareTo(BigDecimal.valueOf(500.00)) == 0 &&
                h.getNewQuantity().compareTo(BigDecimal.valueOf(700.00)) == 0
        ));
    }

    @Test
    @DisplayName("Supplier updates price and records price update history")
    void testUpdatePriceSuccess() {
        PriceUpdateRequest request = new PriceUpdateRequest(
                BigDecimal.valueOf(395.00),
                "Freight cost increase adjustment"
        );

        when(materialRepository.findById(1001L)).thenReturn(Optional.of(cementMaterial));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));

        MaterialResponse response = service.updatePrice(1001L, request, "supplier@materials.com");

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(395.00), response.getPrice());

        verify(materialStockHistoryRepository).save(argThat(h ->
                h.getChangeType() == StockChangeType.PRICE_UPDATE &&
                h.getNotes().contains("Freight cost increase adjustment")
        ));
    }

    @Test
    @DisplayName("Search materials by category, location, maxPrice, and availability")
    void testSearchMaterials() {
        when(materialRepository.searchMaterials(
                eq("Cement"), eq(MaterialCategory.CEMENT), eq("Austin"), eq(BigDecimal.valueOf(400.00)), eq(MaterialAvailability.IN_STOCK)))
                .thenReturn(List.of(cementMaterial));

        List<MaterialResponse> results = service.searchMaterials(
                "Cement", MaterialCategory.CEMENT, "Austin", BigDecimal.valueOf(400.00), MaterialAvailability.IN_STOCK);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("UltraTech 53 Grade OPC Cement", results.get(0).getName());
        assertEquals(BigDecimal.valueOf(380.00), results.get(0).getPrice());
    }

    @Test
    @DisplayName("Contractor orders material within available stock successfully")
    void testCreateOrderSuccess() {
        MaterialOrderRequest request = new MaterialOrderRequest(
                1001L,
                500L,
                BigDecimal.valueOf(100.00), // within 500 stock
                "400 Congress Ave, Austin",
                LocalDate.now().plusDays(2),
                "Unload at Gate 2"
        );

        when(contractorRepository.findByUserEmail("contractor@builder.com")).thenReturn(Optional.of(contractor));
        when(materialRepository.findById(1001L)).thenReturn(Optional.of(cementMaterial));
        when(projectRepository.findById(500L)).thenReturn(Optional.of(project));
        when(materialOrderRepository.save(any(MaterialOrder.class))).thenAnswer(invocation -> {
            MaterialOrder order = invocation.getArgument(0);
            order.setId(5001L);
            return order;
        });

        MaterialOrderResponse response = service.createOrder(1001L, request, "contractor@builder.com");

        assertNotNull(response);
        assertEquals(5001L, response.getId());
        assertEquals(MaterialOrderStatus.PENDING, response.getStatus());
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(response.getQuantity()));
        assertEquals(0, BigDecimal.valueOf(380.00).compareTo(response.getUnitPrice()));
        assertEquals(0, BigDecimal.valueOf(38000.00).compareTo(response.getTotalAmount())); // 100 * 380
    }

    @Test
    @DisplayName("CRITICAL: Contractor cannot order more than available stock (400 Bad Request)")
    void testCreateOrderFailsExceedingStock() {
        MaterialOrderRequest request = new MaterialOrderRequest(
                1001L,
                500L,
                BigDecimal.valueOf(600.00), // exceeds available 500.00 stock!
                "Austin Site",
                LocalDate.now().plusDays(2),
                "Bulk order"
        );

        when(contractorRepository.findByUserEmail("contractor@builder.com")).thenReturn(Optional.of(contractor));
        when(materialRepository.findById(1001L)).thenReturn(Optional.of(cementMaterial));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.createOrder(1001L, request, "contractor@builder.com"));

        assertTrue(ex.getMessage().contains("exceeds available stock"));
        verify(materialOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Supplier accepts order: Deducts stock and records ORDER_DEDUCTION history")
    void testAcceptOrderSuccess() {
        MaterialOrder order = new MaterialOrder(
                "ORD-MAT-12345",
                contractor,
                supplier,
                cementMaterial,
                project,
                BigDecimal.valueOf(100.00),
                "BAG",
                BigDecimal.valueOf(380.00),
                BigDecimal.valueOf(38000.00),
                "Austin Site",
                LocalDate.now().plusDays(2),
                "Gate 2 delivery"
        );
        order.setId(5002L);
        order.setStatus(MaterialOrderStatus.PENDING);

        when(materialOrderRepository.findById(5002L)).thenReturn(Optional.of(order));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));
        when(materialOrderRepository.save(any(MaterialOrder.class))).thenAnswer(i -> i.getArgument(0));

        MaterialOrderResponse response = service.acceptOrder(5002L, "supplier@materials.com", "Order accepted. Loading scheduled.");

        assertNotNull(response);
        assertEquals(MaterialOrderStatus.ACCEPTED, response.getStatus());
        assertEquals("Order accepted. Loading scheduled.", response.getSupplierNotes());

        // Verify stock deducted: 500 - 100 = 400
        assertEquals(BigDecimal.valueOf(400.00), cementMaterial.getAvailableQuantity());
        verify(materialRepository).save(cementMaterial);

        // Verify stock history logged ORDER_DEDUCTION
        verify(materialStockHistoryRepository).save(argThat(h ->
                h.getChangeType() == StockChangeType.ORDER_DEDUCTION &&
                h.getPreviousQuantity().compareTo(BigDecimal.valueOf(500.00)) == 0 &&
                h.getNewQuantity().compareTo(BigDecimal.valueOf(400.00)) == 0 &&
                h.getChangeQuantity().compareTo(BigDecimal.valueOf(-100.00)) == 0
        ));
    }

    @Test
    @DisplayName("Supplier accepts order depleting all stock: sets OUT_OF_STOCK")
    void testAcceptOrderDepletingStock() {
        cementMaterial.setAvailableQuantity(BigDecimal.valueOf(100.00)); // only 100 left

        MaterialOrder order = new MaterialOrder(
                "ORD-MAT-99999",
                contractor,
                supplier,
                cementMaterial,
                null,
                BigDecimal.valueOf(100.00),
                "BAG",
                BigDecimal.valueOf(380.00),
                BigDecimal.valueOf(38000.00),
                "Site",
                null,
                null
        );
        order.setId(5003L);
        order.setStatus(MaterialOrderStatus.PENDING);

        when(materialOrderRepository.findById(5003L)).thenReturn(Optional.of(order));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));
        when(materialOrderRepository.save(any(MaterialOrder.class))).thenAnswer(i -> i.getArgument(0));

        service.acceptOrder(5003L, "supplier@materials.com", null);

        assertEquals(0, BigDecimal.ZERO.compareTo(cementMaterial.getAvailableQuantity()));
        assertEquals(MaterialAvailability.OUT_OF_STOCK, cementMaterial.getAvailability());
    }

    @Test
    @DisplayName("Supplier rejects order with reason")
    void testRejectOrderSuccess() {
        MaterialOrder order = new MaterialOrder();
        order.setId(5004L);
        order.setSupplier(supplier);
        order.setMaterial(cementMaterial);
        order.setStatus(MaterialOrderStatus.PENDING);

        when(materialOrderRepository.findById(5004L)).thenReturn(Optional.of(order));
        when(materialOrderRepository.save(any(MaterialOrder.class))).thenAnswer(i -> i.getArgument(0));

        MaterialOrderResponse response = service.rejectOrder(5004L, "supplier@materials.com", "Delivery truck unavailable for specified date");

        assertNotNull(response);
        assertEquals(MaterialOrderStatus.REJECTED, response.getStatus());
        assertEquals("Delivery truck unavailable for specified date", response.getRejectionReason());
        // Verify stock was NOT deducted
        assertEquals(BigDecimal.valueOf(500.00), cementMaterial.getAvailableQuantity());
    }

    @Test
    @DisplayName("Contractor cancels accepted order: restores deducted stock and logs history")
    void testCancelAcceptedOrderRestoresStock() {
        cementMaterial.setAvailableQuantity(BigDecimal.valueOf(400.00)); // stock was already deducted by 100

        MaterialOrder order = new MaterialOrder();
        order.setId(5005L);
        order.setOrderNumber("ORD-MAT-CANCEL-01");
        order.setContractor(contractor);
        order.setSupplier(supplier);
        order.setMaterial(cementMaterial);
        order.setQuantity(BigDecimal.valueOf(100.00));
        order.setStatus(MaterialOrderStatus.ACCEPTED);

        when(materialOrderRepository.findById(5005L)).thenReturn(Optional.of(order));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));
        when(materialOrderRepository.save(any(MaterialOrder.class))).thenAnswer(i -> i.getArgument(0));

        MaterialOrderResponse response = service.cancelOrder(5005L, "contractor@builder.com", "Site pour delayed");

        assertNotNull(response);
        assertEquals(MaterialOrderStatus.CANCELLED, response.getStatus());

        // Verify stock restored: 400 + 100 = 500
        assertEquals(BigDecimal.valueOf(500.00), cementMaterial.getAvailableQuantity());
        verify(materialRepository).save(cementMaterial);

        // Verify stock history logged ORDER_CANCELLED_RESTORE
        verify(materialStockHistoryRepository).save(argThat(h ->
                h.getChangeType() == StockChangeType.ORDER_CANCELLED_RESTORE &&
                h.getPreviousQuantity().compareTo(BigDecimal.valueOf(400.00)) == 0 &&
                h.getNewQuantity().compareTo(BigDecimal.valueOf(500.00)) == 0
        ));
    }

    @Test
    @DisplayName("View stock history for a material returns entries")
    void testGetStockHistorySuccess() {
        MaterialStockHistory h1 = new MaterialStockHistory(
                cementMaterial, BigDecimal.ZERO, BigDecimal.valueOf(500.00),
                BigDecimal.valueOf(500.00), StockChangeType.INITIAL_STOCK, null,
                "Initial batch", "supplier@materials.com"
        );
        h1.setId(1L);

        when(materialRepository.findById(1001L)).thenReturn(Optional.of(cementMaterial));
        when(materialStockHistoryRepository.findByMaterialIdOrderByCreatedAtDesc(1001L))
                .thenReturn(List.of(h1));

        List<StockHistoryResponse> history = service.getStockHistory(1001L);

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(StockChangeType.INITIAL_STOCK, history.get(0).getChangeType());
        assertEquals(BigDecimal.valueOf(500.00), history.get(0).getNewQuantity());
    }
}
