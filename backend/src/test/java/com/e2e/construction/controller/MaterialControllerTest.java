package com.e2e.construction.controller;

import com.e2e.construction.dto.MaterialOrderRequest;
import com.e2e.construction.dto.MaterialRequest;
import com.e2e.construction.dto.PriceUpdateRequest;
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
import com.e2e.construction.exception.GlobalExceptionHandler;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.MaterialOrderRepository;
import com.e2e.construction.repository.MaterialRepository;
import com.e2e.construction.repository.MaterialStockHistoryRepository;
import com.e2e.construction.repository.MaterialSupplierRepository;
import com.e2e.construction.repository.ProjectRepository;
import com.e2e.construction.repository.UserRepository;
import com.e2e.construction.service.MaterialService;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MaterialControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

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

    private MaterialService materialService;
    private MaterialController controller;

    private final Principal supplierPrincipal = () -> "supplier@materials.com";
    private final Principal contractorPrincipal = () -> "contractor@builder.com";

    private User supplierUser;
    private User contractorUser;
    private MaterialSupplier supplier;
    private Contractor contractor;
    private Material sandMaterial;

    @BeforeEach
    void setUp() {
        materialService = new MaterialService(
                materialRepository,
                materialStockHistoryRepository,
                materialOrderRepository,
                materialSupplierRepository,
                contractorRepository,
                projectRepository,
                userRepository
        );

        controller = new MaterialController(materialService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Supplier
        Role supplierRole = new Role("MATERIAL_SUPPLIER", "Supplier role");
        supplierUser = new User();
        supplierUser.setId(30L);
        supplierUser.setEmail("supplier@materials.com");
        supplierUser.setFirstName("Sarah");
        supplierUser.setLastName("Stone");
        supplierUser.setRole(supplierRole);

        supplier = new MaterialSupplier();
        supplier.setId(300L);
        supplier.setUser(supplierUser);
        supplier.setStoreName("Apex Quarry Supplies");
        supplier.setCity("Austin");
        supplier.setState("TX");

        // Contractor
        Role contractorRole = new Role("CONTRACTOR", "Contractor role");
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

        // Material (Sand)
        sandMaterial = new Material();
        sandMaterial.setId(1005L);
        sandMaterial.setSupplier(supplier);
        sandMaterial.setName("Coarse River Sand M-Sand");
        sandMaterial.setCategory(MaterialCategory.SAND);
        sandMaterial.setGradeSpecification("Zone II Concrete Sand");
        sandMaterial.setUnit("TON");
        sandMaterial.setAvailableQuantity(BigDecimal.valueOf(250.00));
        sandMaterial.setPrice(BigDecimal.valueOf(1400.00));
        sandMaterial.setLocation("Austin Quarry Site");
        sandMaterial.setAvailability(MaterialAvailability.IN_STOCK);
    }

    @Test
    @DisplayName("Step 1: Supplier creates a new material listing (CEMENT, STEEL, SAND, etc.)")
    void testCreateMaterial() throws Exception {
        MaterialRequest request = new MaterialRequest(
                "Coarse River Sand M-Sand",
                MaterialCategory.SAND,
                "Zone II Concrete Sand",
                "TON",
                BigDecimal.valueOf(250.00),
                BigDecimal.valueOf(1400.00),
                "Austin Quarry Site",
                "Washed graded manufactured sand"
        );

        when(userRepository.findByEmail("supplier@materials.com")).thenReturn(Optional.of(supplierUser));
        when(materialSupplierRepository.findByUserId(30L)).thenReturn(Optional.of(supplier));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> {
            Material m = i.getArgument(0);
            m.setId(1005L);
            return m;
        });

        mockMvc.perform(post("/api/materials")
                        .principal(supplierPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1005)))
                .andExpect(jsonPath("$.name", is("Coarse River Sand M-Sand")))
                .andExpect(jsonPath("$.category", is("SAND")))
                .andExpect(jsonPath("$.availableQuantity", is(250.00)))
                .andExpect(jsonPath("$.price", is(1400.00)));
    }

    @Test
    @DisplayName("Step 2: Supplier updates stock quantity")
    void testUpdateStock() throws Exception {
        StockUpdateRequest request = new StockUpdateRequest(
                BigDecimal.valueOf(100.00),
                true,
                "Quarry crushing output batch #44"
        );

        when(materialRepository.findById(1005L)).thenReturn(Optional.of(sandMaterial));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(patch("/api/materials/1005/stock")
                        .principal(supplierPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1005)))
                .andExpect(jsonPath("$.availableQuantity", is(350.00))); // 250 + 100
    }

    @Test
    @DisplayName("Step 3: Supplier updates material price")
    void testUpdatePrice() throws Exception {
        PriceUpdateRequest request = new PriceUpdateRequest(
                BigDecimal.valueOf(1450.00),
                "Quarterly fuel surcharge adjustment"
        );

        when(materialRepository.findById(1005L)).thenReturn(Optional.of(sandMaterial));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(patch("/api/materials/1005/price")
                        .principal(supplierPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1005)))
                .andExpect(jsonPath("$.price", is(1450.00)));
    }

    @Test
    @DisplayName("Step 4: Contractor searches and filters materials")
    void testSearchMaterials() throws Exception {
        when(materialRepository.searchMaterials(
                eq("Sand"), eq(MaterialCategory.SAND), eq("Austin"), any(BigDecimal.class), eq(MaterialAvailability.IN_STOCK)))
                .thenReturn(List.of(sandMaterial));

        mockMvc.perform(get("/api/materials")
                        .principal(contractorPrincipal)
                        .param("search", "Sand")
                        .param("category", "SAND")
                        .param("location", "Austin")
                        .param("maxPrice", "2000.00")
                        .param("availability", "IN_STOCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Coarse River Sand M-Sand")))
                .andExpect(jsonPath("$[0].availableQuantity", is(250.00)));
    }

    @Test
    @DisplayName("Step 5: Contractor views stock and price for a material")
    void testGetMaterialById() throws Exception {
        when(materialRepository.findById(1005L)).thenReturn(Optional.of(sandMaterial));

        mockMvc.perform(get("/api/materials/1005")
                        .principal(contractorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1005)))
                .andExpect(jsonPath("$.availableQuantity", is(250.00)))
                .andExpect(jsonPath("$.price", is(1400.00)));
    }

    @Test
    @DisplayName("Step 6: View stock history for a material")
    void testGetStockHistory() throws Exception {
        MaterialStockHistory history = new MaterialStockHistory(
                sandMaterial, BigDecimal.ZERO, BigDecimal.valueOf(250.00),
                BigDecimal.valueOf(250.00), StockChangeType.INITIAL_STOCK, null,
                "Initial batch", "supplier@materials.com"
        );
        history.setId(10L);

        when(materialRepository.findById(1005L)).thenReturn(Optional.of(sandMaterial));
        when(materialStockHistoryRepository.findByMaterialIdOrderByCreatedAtDesc(1005L))
                .thenReturn(List.of(history));

        mockMvc.perform(get("/api/materials/1005/stock-history")
                        .principal(contractorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].changeType", is("INITIAL_STOCK")))
                .andExpect(jsonPath("$[0].newQuantity", is(250.00)));
    }

    @Test
    @DisplayName("Step 7: Contractor requests/orders material within available stock")
    void testCreateOrderSuccess() throws Exception {
        MaterialOrderRequest orderRequest = new MaterialOrderRequest(
                1005L,
                null,
                BigDecimal.valueOf(50.00), // ordering 50 TON (within 250 available)
                "Austin Jobsite 4",
                LocalDate.now().plusDays(1),
                "Tipper truck required"
        );

        when(contractorRepository.findByUserEmail("contractor@builder.com")).thenReturn(Optional.of(contractor));
        when(materialRepository.findById(1005L)).thenReturn(Optional.of(sandMaterial));
        when(materialOrderRepository.save(any(MaterialOrder.class))).thenAnswer(i -> {
            MaterialOrder o = i.getArgument(0);
            o.setId(9001L);
            return o;
        });

        mockMvc.perform(post("/api/materials/1005/orders")
                        .principal(contractorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(9001)))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.quantity", is(50.00)))
                .andExpect(jsonPath("$.unitPrice", is(1400.00)))
                .andExpect(jsonPath("$.totalAmount", is(70000.00))); // 50 * 1400
    }

    @Test
    @DisplayName("Step 8: CRITICAL - Contractor cannot order more than available stock (400 Bad Request)")
    void testCreateOrderExceedingStockFails() throws Exception {
        MaterialOrderRequest excessiveRequest = new MaterialOrderRequest(
                1005L,
                null,
                BigDecimal.valueOf(300.00), // 300 TON exceeds 250 available stock!
                "Site",
                LocalDate.now().plusDays(1),
                null
        );

        when(contractorRepository.findByUserEmail("contractor@builder.com")).thenReturn(Optional.of(contractor));
        when(materialRepository.findById(1005L)).thenReturn(Optional.of(sandMaterial));

        mockMvc.perform(post("/api/materials/1005/orders")
                        .principal(contractorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(excessiveRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("exceeds available stock")));
    }

    @Test
    @DisplayName("Step 9: Supplier accepts order: stock deducted and order set to ACCEPTED")
    void testAcceptOrder() throws Exception {
        MaterialOrder order = new MaterialOrder();
        order.setId(9001L);
        order.setOrderNumber("ORD-MAT-9001");
        order.setSupplier(supplier);
        order.setContractor(contractor);
        order.setMaterial(sandMaterial);
        order.setQuantity(BigDecimal.valueOf(50.00));
        order.setStatus(MaterialOrderStatus.PENDING);

        when(materialOrderRepository.findById(9001L)).thenReturn(Optional.of(order));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));
        when(materialOrderRepository.save(any(MaterialOrder.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/api/material-orders/9001/accept")
                        .principal(supplierPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"Dispatching batch at 9 AM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(9001)))
                .andExpect(jsonPath("$.status", is("ACCEPTED")));
    }

    @Test
    @DisplayName("Step 10: Supplier rejects order with reason")
    void testRejectOrder() throws Exception {
        MaterialOrder order = new MaterialOrder();
        order.setId(9002L);
        order.setSupplier(supplier);
        order.setContractor(contractor);
        order.setMaterial(sandMaterial);
        order.setStatus(MaterialOrderStatus.PENDING);

        when(materialOrderRepository.findById(9002L)).thenReturn(Optional.of(order));
        when(materialOrderRepository.save(any(MaterialOrder.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/api/material-orders/9002/reject")
                        .principal(supplierPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Quarry maintenance on scheduled date\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(9002)))
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.rejectionReason", containsString("Quarry maintenance")));
    }

    @Test
    @DisplayName("Step 11: Contractor cancels order")
    void testCancelOrder() throws Exception {
        MaterialOrder order = new MaterialOrder();
        order.setId(9003L);
        order.setContractor(contractor);
        order.setSupplier(supplier);
        order.setMaterial(sandMaterial);
        order.setStatus(MaterialOrderStatus.PENDING);

        when(materialOrderRepository.findById(9003L)).thenReturn(Optional.of(order));
        when(materialOrderRepository.save(any(MaterialOrder.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/api/material-orders/9003/cancel")
                        .principal(contractorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Design revised\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(9003)))
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }
}
