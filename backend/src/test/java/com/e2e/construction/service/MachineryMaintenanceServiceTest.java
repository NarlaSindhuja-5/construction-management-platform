package com.e2e.construction.service;

import com.e2e.construction.dto.MachineryMaintenanceRequest;
import com.e2e.construction.dto.MachineryMaintenanceResponse;
import com.e2e.construction.entity.Machinery;
import com.e2e.construction.entity.MachineryMaintenance;
import com.e2e.construction.entity.MachineryMaintenanceImage;
import com.e2e.construction.entity.MachineryOwner;
import com.e2e.construction.entity.MaintenanceType;
import com.e2e.construction.entity.Role;
import com.e2e.construction.entity.User;
import com.e2e.construction.repository.MachineryMaintenanceImageRepository;
import com.e2e.construction.repository.MachineryMaintenanceRepository;
import com.e2e.construction.repository.MachineryRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MachineryMaintenanceServiceTest {

    @Mock
    private MachineryMaintenanceRepository maintenanceRepository;

    @Mock
    private MachineryMaintenanceImageRepository maintenanceImageRepository;

    @Mock
    private MachineryRepository machineryRepository;

    @Mock
    private UserRepository userRepository;

    private FileStorageService fileStorageService;

    private MachineryMaintenanceService maintenanceService;

    private User ownerUser;
    private User adminUser;
    private User unauthorizedUser;
    private MachineryOwner machineryOwner;
    private Machinery machine;
    private MachineryMaintenance existingMaintenance;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService("target/test-uploads");
        maintenanceService = new MachineryMaintenanceService(
                maintenanceRepository,
                maintenanceImageRepository,
                machineryRepository,
                userRepository,
                fileStorageService
        );
        Role ownerRole = new Role("MACHINERY_OWNER", "Owner role");
        Role adminRole = new Role("ADMIN", "Admin role");
        Role contractorRole = new Role("CONTRACTOR", "Contractor role");

        ownerUser = new User();
        ownerUser.setId(10L);
        ownerUser.setEmail("owner@example.com");
        ownerUser.setRole(ownerRole);

        adminUser = new User();
        adminUser.setId(99L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(adminRole);

        unauthorizedUser = new User();
        unauthorizedUser.setId(20L);
        unauthorizedUser.setEmail("unauthorized@example.com");
        unauthorizedUser.setRole(contractorRole);

        machineryOwner = new MachineryOwner();
        machineryOwner.setId(5L);
        machineryOwner.setUser(ownerUser);
        machineryOwner.setCompanyName("Alex Heavy Equipment");

        machine = new Machinery();
        machine.setId(1L);
        machine.setName("Caterpillar 320D Excavator");
        machine.setOwner(machineryOwner);

        existingMaintenance = new MachineryMaintenance();
        existingMaintenance.setId(50L);
        existingMaintenance.setMachinery(machine);
        existingMaintenance.setMaintenanceDate(LocalDate.of(2026, 9, 20));
        existingMaintenance.setMaintenanceType(MaintenanceType.OIL_CHANGE);
        existingMaintenance.setDescription("Engine and hydraulic oil change.");
        existingMaintenance.setCost(new BigDecimal("12000.00"));
        existingMaintenance.setServiceProvider("Cat Authorized Center");
        existingMaintenance.setEngineHours(1100.0);
    }

    @Test
    @DisplayName("Add maintenance record succeeds when invoked by machine owner")
    void testAddMaintenanceRecord_byOwner_success() {
        MachineryMaintenanceRequest request = new MachineryMaintenanceRequest(
                LocalDate.of(2026, 10, 1),
                MaintenanceType.ENGINE_SERVICE,
                "500-hour overhaul",
                new BigDecimal("25000.00"),
                "Cat Services",
                1250.0,
                LocalDate.of(2027, 4, 1),
                "All good",
                List.of("/images/test.jpg")
        );

        when(machineryRepository.findById(1L)).thenReturn(Optional.of(machine));
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(maintenanceRepository.save(any(MachineryMaintenance.class))).thenAnswer(inv -> {
            MachineryMaintenance m = inv.getArgument(0);
            m.setId(51L);
            return m;
        });

        MachineryMaintenanceResponse response = maintenanceService.addMaintenanceRecord(1L, "owner@example.com", request);

        assertNotNull(response);
        assertEquals(MaintenanceType.ENGINE_SERVICE, response.getMaintenanceType());
        assertEquals(new BigDecimal("25000.00"), response.getCost());
        assertEquals("Cat Services", response.getServiceProvider());
        assertEquals(1L, response.getMachineId());
    }

    @Test
    @DisplayName("Add maintenance record succeeds when invoked by admin")
    void testAddMaintenanceRecord_byAdmin_success() {
        MachineryMaintenanceRequest request = new MachineryMaintenanceRequest(
                LocalDate.of(2026, 10, 1),
                MaintenanceType.GENERAL_SERVICE,
                "Admin inspected",
                new BigDecimal("5000.00"),
                "Authorized Inspector",
                null,
                null,
                null,
                null
        );

        when(machineryRepository.findById(1L)).thenReturn(Optional.of(machine));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(maintenanceRepository.save(any(MachineryMaintenance.class))).thenAnswer(inv -> {
            MachineryMaintenance m = inv.getArgument(0);
            m.setId(52L);
            return m;
        });

        MachineryMaintenanceResponse response = maintenanceService.addMaintenanceRecord(1L, "admin@example.com", request);

        assertNotNull(response);
        assertEquals(MaintenanceType.GENERAL_SERVICE, response.getMaintenanceType());
    }

    @Test
    @DisplayName("Add maintenance record fails with AccessDeniedException for unauthorized user")
    void testAddMaintenanceRecord_unauthorized_throwsAccessDenied() {
        MachineryMaintenanceRequest request = new MachineryMaintenanceRequest();
        when(machineryRepository.findById(1L)).thenReturn(Optional.of(machine));
        when(userRepository.findByEmail("unauthorized@example.com")).thenReturn(Optional.of(unauthorizedUser));

        assertThrows(AccessDeniedException.class, () ->
                maintenanceService.addMaintenanceRecord(1L, "unauthorized@example.com", request));
    }

    @Test
    @DisplayName("Update maintenance record succeeds for owner")
    void testUpdateMaintenanceRecord_byOwner_success() {
        MachineryMaintenanceRequest updateRequest = new MachineryMaintenanceRequest();
        updateRequest.setCost(new BigDecimal("15000.00"));
        updateRequest.setDescription("Updated with additional synthetic oil additives.");

        when(maintenanceRepository.findById(50L)).thenReturn(Optional.of(existingMaintenance));
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(maintenanceRepository.save(any(MachineryMaintenance.class))).thenAnswer(inv -> inv.getArgument(0));

        MachineryMaintenanceResponse response = maintenanceService.updateMaintenanceRecord(50L, "owner@example.com", updateRequest);

        assertNotNull(response);
        assertEquals(new BigDecimal("15000.00"), response.getCost());
        assertEquals("Updated with additional synthetic oil additives.", response.getDescription());
    }

    @Test
    @DisplayName("View maintenance history returns records in chronological order")
    void testGetMaintenanceHistory_success() {
        MachineryMaintenance older = new MachineryMaintenance();
        older.setId(49L);
        older.setMachinery(machine);
        older.setMaintenanceDate(LocalDate.of(2026, 5, 10));
        older.setMaintenanceType(MaintenanceType.BRAKE_SERVICE);

        when(machineryRepository.existsById(1L)).thenReturn(true);
        when(maintenanceRepository.findByMachineryIdOrderByMaintenanceDateDescIdDesc(1L))
                .thenReturn(List.of(existingMaintenance, older));

        List<MachineryMaintenanceResponse> history = maintenanceService.getMaintenanceHistory(1L, "desc");

        assertNotNull(history);
        assertEquals(2, history.size());
        assertEquals(MaintenanceType.OIL_CHANGE, history.get(0).getMaintenanceType());
        assertEquals(MaintenanceType.BRAKE_SERVICE, history.get(1).getMaintenanceType());
    }

    @Test
    @DisplayName("Delete maintenance record deletes entity and cleans up images when owner authorizes")
    void testDeleteMaintenanceRecord_byOwner_success() {
        MachineryMaintenanceImage img = new MachineryMaintenanceImage();
        img.setStoredFileName("maintenance_sample.jpg");
        existingMaintenance.addImage(img);

        when(maintenanceRepository.findById(50L)).thenReturn(Optional.of(existingMaintenance));
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));

        maintenanceService.deleteMaintenanceRecord(50L, "owner@example.com");

        verify(maintenanceRepository).delete(existingMaintenance);
    }

    @Test
    @DisplayName("Delete maintenance record fails for unauthorized user")
    void testDeleteMaintenanceRecord_unauthorized_throwsAccessDenied() {
        when(maintenanceRepository.findById(50L)).thenReturn(Optional.of(existingMaintenance));
        when(userRepository.findByEmail("unauthorized@example.com")).thenReturn(Optional.of(unauthorizedUser));

        assertThrows(AccessDeniedException.class, () ->
                maintenanceService.deleteMaintenanceRecord(50L, "unauthorized@example.com"));
    }
}
