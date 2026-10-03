package com.e2e.construction.service;

import com.e2e.construction.dto.MachineConditionRequest;
import com.e2e.construction.dto.MachineConditionResponse;
import com.e2e.construction.entity.MachineCondition;
import com.e2e.construction.entity.Machinery;
import com.e2e.construction.entity.MachineryOwner;
import com.e2e.construction.entity.Role;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.MachineConditionRepository;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MachineConditionServiceTest {

    @Mock
    private MachineConditionRepository machineConditionRepository;

    @Mock
    private MachineryRepository machineryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MachineConditionService machineConditionService;

    private User ownerUser;
    private User adminUser;
    private User otherUser;
    private MachineryOwner machineryOwner;
    private Machinery machine;
    private MachineCondition baselineCondition;

    @BeforeEach
    void setUp() {
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

        otherUser = new User();
        otherUser.setId(20L);
        otherUser.setEmail("other@example.com");
        otherUser.setRole(contractorRole);

        machineryOwner = new MachineryOwner();
        machineryOwner.setId(5L);
        machineryOwner.setUser(ownerUser);
        machineryOwner.setCompanyName("Alex Heavy Equipment");

        machine = new Machinery();
        machine.setId(1L);
        machine.setName("Caterpillar 320D Excavator");
        machine.setOwner(machineryOwner);

        baselineCondition = new MachineCondition();
        baselineCondition.setId(100L);
        baselineCondition.setMachinery(machine);
        baselineCondition.setEngine("GOOD");
        baselineCondition.setEngineOil("NORMAL");
        baselineCondition.setCoolant("NORMAL");
        baselineCondition.setTransmission("GOOD");
        baselineCondition.setHydraulicSystem("GOOD");
        baselineCondition.setBattery("GOOD");
        baselineCondition.setBrakes("GOOD");
        baselineCondition.setTyres("80%");
        baselineCondition.setLights("GOOD");
        baselineCondition.setBody("GOOD");
        baselineCondition.setLeakage("NO");
        baselineCondition.setStartingCondition("GOOD");
        baselineCondition.setInspectionDate(LocalDate.now());
    }

    @Test
    @DisplayName("Create report succeeds when performed by machine owner")
    void testCreateReport_byOwner_success() {
        MachineConditionRequest request = new MachineConditionRequest(
                "GOOD", "NORMAL", "NORMAL", "GOOD", "GOOD", "EXCELLENT",
                "GOOD", "80%", "GOOD", "GOOD", "NO", "GOOD",
                LocalDate.of(2026, 10, 3), LocalDate.of(2026, 9, 15), LocalDate.of(2026, 12, 15),
                1250.5, 45000.0, "Smooth operation"
        );

        when(machineryRepository.findById(1L)).thenReturn(Optional.of(machine));
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(machineConditionRepository.save(any(MachineCondition.class))).thenAnswer(invocation -> {
            MachineCondition c = invocation.getArgument(0);
            c.setId(101L);
            return c;
        });

        MachineConditionResponse response = machineConditionService.createReport(1L, "owner@example.com", request);

        assertNotNull(response);
        assertEquals("GOOD", response.getEngine());
        assertEquals("NORMAL", response.getEngineOil());
        assertEquals("80%", response.getTyres());
        assertEquals("NO", response.getLeakage());
        assertEquals(1250.5, response.getEngineHours());
        assertEquals(1L, response.getMachineId());
    }

    @Test
    @DisplayName("Create report succeeds when performed by Admin")
    void testCreateReport_byAdmin_success() {
        MachineConditionRequest request = new MachineConditionRequest(
                "EXCELLENT", "NORMAL", "NORMAL", "EXCELLENT", "EXCELLENT", "EXCELLENT",
                "EXCELLENT", "100%", "EXCELLENT", "EXCELLENT", "NO", "EXCELLENT",
                LocalDate.of(2026, 10, 3), null, null, null, null, "Admin certified"
        );

        when(machineryRepository.findById(1L)).thenReturn(Optional.of(machine));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(machineConditionRepository.save(any(MachineCondition.class))).thenAnswer(invocation -> {
            MachineCondition c = invocation.getArgument(0);
            c.setId(102L);
            return c;
        });

        MachineConditionResponse response = machineConditionService.createReport(1L, "admin@example.com", request);

        assertNotNull(response);
        assertEquals("EXCELLENT", response.getEngine());
        assertEquals("100%", response.getTyres());
    }

    @Test
    @DisplayName("Create report fails with AccessDeniedException when user is neither owner nor admin")
    void testCreateReport_unauthorizedUser_throwsAccessDenied() {
        MachineConditionRequest request = new MachineConditionRequest();
        request.setEngine("GOOD");

        when(machineryRepository.findById(1L)).thenReturn(Optional.of(machine));
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () ->
                machineConditionService.createReport(1L, "other@example.com", request));
    }

    @Test
    @DisplayName("Update report succeeds for owner")
    void testUpdateReport_byOwner_success() {
        MachineConditionRequest updateRequest = new MachineConditionRequest();
        updateRequest.setTyres("60%");
        updateRequest.setEngine("NEEDS_SERVICE");
        updateRequest.setRemarks("Tyres worn down to 60%, needs service");

        when(machineConditionRepository.findById(100L)).thenReturn(Optional.of(baselineCondition));
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(machineConditionRepository.save(any(MachineCondition.class))).thenAnswer(inv -> inv.getArgument(0));

        MachineConditionResponse response = machineConditionService.updateReport(100L, "owner@example.com", updateRequest);

        assertNotNull(response);
        assertEquals("60%", response.getTyres());
        assertEquals("NEEDS_SERVICE", response.getEngine());
        assertEquals("Tyres worn down to 60%, needs service", response.getRemarks());
    }

    @Test
    @DisplayName("Update report fails for unauthorized user")
    void testUpdateReport_unauthorizedUser_throwsAccessDenied() {
        MachineConditionRequest updateRequest = new MachineConditionRequest();

        when(machineConditionRepository.findById(100L)).thenReturn(Optional.of(baselineCondition));
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () ->
                machineConditionService.updateReport(100L, "other@example.com", updateRequest));
    }

    @Test
    @DisplayName("Get latest condition auto-creates baseline if no record exists yet (ensures every machine has a condition record)")
    void testGetLatestCondition_autoCreatesBaselineWhenEmpty() {
        when(machineryRepository.findById(1L)).thenReturn(Optional.of(machine));
        when(machineConditionRepository.findFirstByMachineryIdOrderByInspectionDateDescCreatedAtDesc(1L))
                .thenReturn(Optional.empty());
        when(machineConditionRepository.save(any(MachineCondition.class))).thenAnswer(inv -> {
            MachineCondition c = inv.getArgument(0);
            c.setId(999L);
            return c;
        });

        MachineConditionResponse response = machineConditionService.getLatestCondition(1L);

        assertNotNull(response);
        assertEquals("GOOD", response.getEngine());
        assertEquals("100%", response.getTyres());
        assertEquals("NO", response.getLeakage());
        verify(machineConditionRepository).save(any(MachineCondition.class));
    }

    @Test
    @DisplayName("Get condition history returns full list of past inspection records")
    void testGetConditionHistory_success() {
        MachineCondition condition2 = new MachineCondition();
        condition2.setId(102L);
        condition2.setMachinery(machine);
        condition2.setEngine("EXCELLENT");
        condition2.setTyres("100%");

        when(machineryRepository.findById(1L)).thenReturn(Optional.of(machine));
        when(machineConditionRepository.findByMachineryIdOrderByInspectionDateDescCreatedAtDesc(1L))
                .thenReturn(List.of(condition2, baselineCondition));

        List<MachineConditionResponse> history = machineConditionService.getConditionHistory(1L);

        assertNotNull(history);
        assertEquals(2, history.size());
        assertEquals("EXCELLENT", history.get(0).getEngine());
        assertEquals("GOOD", history.get(1).getEngine());
    }
}
