package com.e2e.construction.service;

import com.e2e.construction.dto.BookedDateRangeResponse;
import com.e2e.construction.dto.MachineProfileResponse;
import com.e2e.construction.dto.MachineryBookingRequest;
import com.e2e.construction.dto.MachineryBookingResponse;
import com.e2e.construction.dto.MachineryResponse;
import com.e2e.construction.entity.BookingStatus;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.MachineCondition;
import com.e2e.construction.entity.Machinery;
import com.e2e.construction.entity.MachineryAvailability;
import com.e2e.construction.entity.MachineryBooking;
import com.e2e.construction.entity.MachineryCategory;
import com.e2e.construction.entity.MachineryOwner;
import com.e2e.construction.entity.Role;
import com.e2e.construction.entity.User;
import com.e2e.construction.entity.VerificationStatus;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.MachineConditionRepository;
import com.e2e.construction.repository.MachineryBookingRepository;
import com.e2e.construction.repository.MachineryImageRepository;
import com.e2e.construction.repository.MachineryMaintenanceImageRepository;
import com.e2e.construction.repository.MachineryMaintenanceRepository;
import com.e2e.construction.repository.MachineryOwnerRepository;
import com.e2e.construction.repository.MachineryRepository;
import com.e2e.construction.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MachineryBookingServiceTest {

    @Mock
    private MachineryBookingRepository bookingRepository;

    @Mock
    private MachineryRepository machineryRepository;

    @Mock
    private ContractorRepository contractorRepository;

    @Mock
    private MachineryOwnerRepository machineryOwnerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MachineryImageRepository machineryImageRepository;

    @Mock
    private MachineConditionRepository machineConditionRepository;

    @Mock
    private MachineryMaintenanceRepository maintenanceRepository;

    @Mock
    private MachineryMaintenanceImageRepository maintenanceImageRepository;

    private MachineryBookingService bookingService;

    private User contractorUser;
    private User ownerUser;
    private Contractor contractor;
    private MachineryOwner owner;
    private Machinery verifiedMachine;
    private Machinery unverifiedMachine;

    @BeforeEach
    void setUp() {
        FileStorageService fileStorageService = new FileStorageService("target/test-uploads");
        MachineryImageService machineryImageService = new MachineryImageService(
                machineryRepository, machineryImageRepository, fileStorageService
        );
        MachineConditionService machineConditionService = new MachineConditionService(
                machineConditionRepository, machineryRepository, userRepository
        );
        MachineryMaintenanceService machineryMaintenanceService = new MachineryMaintenanceService(
                maintenanceRepository, maintenanceImageRepository, machineryRepository, userRepository, fileStorageService
        );

        bookingService = new MachineryBookingService(
                bookingRepository,
                machineryRepository,
                contractorRepository,
                machineryOwnerRepository,
                userRepository,
                machineryImageService,
                machineConditionService,
                machineryMaintenanceService
        );

        Role contractorRole = new Role("CONTRACTOR", "Contractor role");
        Role ownerRole = new Role("MACHINERY_OWNER", "Owner role");

        contractorUser = new User();
        contractorUser.setId(10L);
        contractorUser.setEmail("contractor@example.com");
        contractorUser.setFirstName("Bob");
        contractorUser.setLastName("Builder");
        contractorUser.setRole(contractorRole);

        contractor = new Contractor();
        contractor.setId(1L);
        contractor.setUser(contractorUser);
        contractor.setCompanyName("Apex Infra Construction");

        ownerUser = new User();
        ownerUser.setId(20L);
        ownerUser.setEmail("owner@example.com");
        ownerUser.setFirstName("Alex");
        ownerUser.setLastName("Vance");
        ownerUser.setRole(ownerRole);

        owner = new MachineryOwner();
        owner.setId(2L);
        owner.setUser(ownerUser);
        owner.setCompanyName("Titan Fleet Services");

        verifiedMachine = new Machinery();
        verifiedMachine.setId(100L);
        verifiedMachine.setName("CAT 320D Excavator");
        verifiedMachine.setOwner(owner);
        verifiedMachine.setCategory(MachineryCategory.EARTHWORK);
        verifiedMachine.setRentalPricePerDay(new BigDecimal("15000.00"));
        verifiedMachine.setVerificationStatus(VerificationStatus.VERIFIED);
        verifiedMachine.setAvailabilityStatus(MachineryAvailability.AVAILABLE);

        unverifiedMachine = new Machinery();
        unverifiedMachine.setId(200L);
        unverifiedMachine.setName("Unverified Crane");
        unverifiedMachine.setOwner(owner);
        unverifiedMachine.setCategory(MachineryCategory.LIFTING);
        unverifiedMachine.setRentalPricePerDay(new BigDecimal("20000.00"));
        unverifiedMachine.setVerificationStatus(VerificationStatus.PENDING);
        unverifiedMachine.setAvailabilityStatus(MachineryAvailability.AVAILABLE);
    }

    @Test
    @DisplayName("Contractor successfully creates booking request for verified machine")
    void testCreateBookingRequest_success() {
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(6); // 5 days total

        MachineryBookingRequest request = new MachineryBookingRequest(
                start, end, "Highway Roadwork", "Mile 14 East", "Need prompt delivery"
        );

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(10L)).thenReturn(Optional.of(contractor));
        when(machineryRepository.findById(100L)).thenReturn(Optional.of(verifiedMachine));
        when(bookingRepository.findOverlappingAcceptedBookings(100L, start, end, null))
                .thenReturn(Collections.emptyList());

        when(bookingRepository.save(any(MachineryBooking.class))).thenAnswer(inv -> {
            MachineryBooking b = inv.getArgument(0);
            b.setId(501L);
            return b;
        });

        MachineryBookingResponse response = bookingService.createBookingRequest(100L, "contractor@example.com", request);

        assertNotNull(response);
        assertEquals(501L, response.getId());
        assertEquals(100L, response.getMachineId());
        assertEquals(BookingStatus.PENDING, response.getStatus());
        assertEquals(5, response.getTotalDays());
        assertEquals(new BigDecimal("75000.00"), response.getTotalPrice());
    }

    @Test
    @DisplayName("Booking request fails when machine is unverified")
    void testCreateBookingRequest_failsForUnverifiedMachine() {
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(5);

        MachineryBookingRequest request = new MachineryBookingRequest(start, end, "Project", "Loc", "Remarks");

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(10L)).thenReturn(Optional.of(contractor));
        when(machineryRepository.findById(200L)).thenReturn(Optional.of(unverifiedMachine));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                bookingService.createBookingRequest(200L, "contractor@example.com", request));

        assertTrue(ex.getMessage().contains("Only verified machinery is available for booking"));
    }

    @Test
    @DisplayName("Booking request fails if start date is in the past")
    void testCreateBookingRequest_failsForPastDate() {
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(2);

        MachineryBookingRequest request = new MachineryBookingRequest(start, end, "Project", "Loc", "Remarks");

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(10L)).thenReturn(Optional.of(contractor));
        when(machineryRepository.findById(100L)).thenReturn(Optional.of(verifiedMachine));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                bookingService.createBookingRequest(100L, "contractor@example.com", request));

        assertTrue(ex.getMessage().contains("Start date cannot be in the past"));
    }

    @Test
    @DisplayName("Booking request fails if end date is before start date")
    void testCreateBookingRequest_failsWhenEndBeforeStart() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(2);

        MachineryBookingRequest request = new MachineryBookingRequest(start, end, "Project", "Loc", "Remarks");

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(10L)).thenReturn(Optional.of(contractor));
        when(machineryRepository.findById(100L)).thenReturn(Optional.of(verifiedMachine));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                bookingService.createBookingRequest(100L, "contractor@example.com", request));

        assertTrue(ex.getMessage().contains("cannot be before start date"));
    }

    @Test
    @DisplayName("Owner successfully accepts booking when no collision exists")
    void testAcceptBooking_success() {
        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = LocalDate.now().plusDays(15);

        MachineryBooking booking = new MachineryBooking(
                verifiedMachine, contractor, start, end, 6,
                new BigDecimal("15000.00"), new BigDecimal("90000.00"), "Bridge Project", "Zone B", "Notes"
        );
        booking.setId(301L);

        when(bookingRepository.findById(301L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(bookingRepository.findOverlappingAcceptedBookings(100L, start, end, 301L))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(MachineryBooking.class))).thenAnswer(inv -> inv.getArgument(0));

        MachineryBookingResponse response = bookingService.acceptBooking(301L, "owner@example.com", "Confirmed delivery");

        assertNotNull(response);
        assertEquals(BookingStatus.ACCEPTED, response.getStatus());
        assertEquals("Confirmed delivery", response.getOwnerNotes());
    }

    @Test
    @DisplayName("Double booking prevented: Owner cannot accept booking if dates overlap an accepted booking")
    void testAcceptBooking_doubleBookingPrevented() {
        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = LocalDate.now().plusDays(15);

        MachineryBooking targetBooking = new MachineryBooking(
                verifiedMachine, contractor, start, end, 6,
                new BigDecimal("15000.00"), new BigDecimal("90000.00"), "Bridge Project", "Zone B", "Notes"
        );
        targetBooking.setId(302L);

        MachineryBooking existingAcceptedBooking = new MachineryBooking(
                verifiedMachine, contractor,
                LocalDate.now().plusDays(12), LocalDate.now().plusDays(18), 7,
                new BigDecimal("15000.00"), new BigDecimal("105000.00"), "Other Project", "Zone C", "Notes"
        );
        existingAcceptedBooking.setId(299L);
        existingAcceptedBooking.setStatus(BookingStatus.ACCEPTED);

        when(bookingRepository.findById(302L)).thenReturn(Optional.of(targetBooking));
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(bookingRepository.findOverlappingAcceptedBookings(100L, start, end, 302L))
                .thenReturn(List.of(existingAcceptedBooking));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                bookingService.acceptBooking(302L, "owner@example.com", "Accepted"));

        assertTrue(ex.getMessage().contains("Double-booking prevented"));
    }

    @Test
    @DisplayName("Owner rejects booking request with reason")
    void testRejectBooking_success() {
        MachineryBooking booking = new MachineryBooking();
        booking.setId(303L);
        booking.setMachinery(verifiedMachine);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(303L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(bookingRepository.save(any(MachineryBooking.class))).thenAnswer(inv -> inv.getArgument(0));

        MachineryBookingResponse response = bookingService.rejectBooking(303L, "owner@example.com", "Depot maintenance scheduled");

        assertNotNull(response);
        assertEquals(BookingStatus.REJECTED, response.getStatus());
        assertEquals("Depot maintenance scheduled", response.getRejectionReason());
    }

    @Test
    @DisplayName("Contractor cancels their own booking")
    void testCancelBooking_byContractor_success() {
        MachineryBooking booking = new MachineryBooking();
        booking.setId(304L);
        booking.setMachinery(verifiedMachine);
        booking.setContractor(contractor);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(304L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(bookingRepository.save(any(MachineryBooking.class))).thenAnswer(inv -> inv.getArgument(0));

        MachineryBookingResponse response = bookingService.cancelBooking(304L, "contractor@example.com", "Weather delay");

        assertNotNull(response);
        assertEquals(BookingStatus.CANCELLED, response.getStatus());
    }

    @Test
    @DisplayName("Get complete machine profile returns specifications, images, condition, maintenance history, and availability")
    void testGetMachineProfile_success() {
        when(machineryRepository.findById(100L)).thenReturn(Optional.of(verifiedMachine));
        when(machineryRepository.existsById(100L)).thenReturn(true);
        when(machineryImageRepository.findByMachineryIdOrderByCreatedAtDesc(100L)).thenReturn(Collections.emptyList());

        MachineCondition condition = new MachineCondition();
        condition.setMachinery(verifiedMachine);
        condition.setEngine("GOOD");
        condition.setEngineOil("NORMAL");
        condition.setCoolant("NORMAL");
        condition.setTransmission("GOOD");
        condition.setHydraulicSystem("GOOD");
        condition.setBattery("GOOD");
        condition.setBrakes("GOOD");
        condition.setTyres("90%");
        condition.setLights("GOOD");
        condition.setBody("GOOD");
        condition.setLeakage("NO");
        condition.setStartingCondition("GOOD");
        condition.setInspectionDate(LocalDate.now());

        when(machineConditionRepository.findFirstByMachineryIdOrderByInspectionDateDescCreatedAtDesc(100L))
                .thenReturn(Optional.of(condition));

        when(maintenanceRepository.findByMachineryIdOrderByMaintenanceDateDescIdDesc(100L))
                .thenReturn(Collections.emptyList());

        when(bookingRepository.findByMachineryIdAndStatusAndEndDateGreaterThanEqualOrderByStartDateAsc(
                eq(100L), eq(BookingStatus.ACCEPTED), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        MachineProfileResponse profile = bookingService.getMachineProfile(100L);

        assertNotNull(profile);
        assertNotNull(profile.getSpecifications());
        assertEquals("CAT 320D Excavator", profile.getSpecifications().getName());
        assertEquals("GOOD", profile.getCondition().getEngine());
        assertEquals("90%", profile.getCondition().getTyres());
        assertEquals(MachineryAvailability.AVAILABLE, profile.getAvailabilityStatus());
    }

    @Test
    @DisplayName("Search verified machinery queries only VERIFIED machines")
    void testSearchVerifiedMachinery_success() {
        when(machineryRepository.searchVerifiedMachinery(null, null, null, null))
                .thenReturn(List.of(verifiedMachine));

        List<MachineryResponse> results = bookingService.searchVerifiedMachinery(null, null, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(VerificationStatus.VERIFIED, results.get(0).getVerificationStatus());
    }

    @Test
    @DisplayName("Overlapping scenario: Partial overlap at start of existing accepted booking is rejected")
    void testOverlapScenario_partialOverlapStart_fails() {
        // Existing accepted: Nov 10 - Nov 20
        // New request: Nov 05 - Nov 12 (overlaps Nov 10-12)
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(12);

        MachineryBooking existing = new MachineryBooking();
        existing.setId(88L);
        existing.setStartDate(LocalDate.now().plusDays(10));
        existing.setEndDate(LocalDate.now().plusDays(20));
        existing.setStatus(BookingStatus.ACCEPTED);

        MachineryBookingRequest request = new MachineryBookingRequest(start, end, "Overlap Start", "Loc", "Remarks");

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(10L)).thenReturn(Optional.of(contractor));
        when(machineryRepository.findById(100L)).thenReturn(Optional.of(verifiedMachine));
        when(bookingRepository.findOverlappingAcceptedBookings(100L, start, end, null))
                .thenReturn(List.of(existing));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                bookingService.createBookingRequest(100L, "contractor@example.com", request));

        assertTrue(ex.getMessage().contains("Conflicts with an accepted booking"));
    }

    @Test
    @DisplayName("Overlapping scenario: Partial overlap at end of existing accepted booking is rejected")
    void testOverlapScenario_partialOverlapEnd_fails() {
        // Existing accepted: Nov 10 - Nov 20
        // New request: Nov 18 - Nov 25 (overlaps Nov 18-20)
        LocalDate start = LocalDate.now().plusDays(18);
        LocalDate end = LocalDate.now().plusDays(25);

        MachineryBooking existing = new MachineryBooking();
        existing.setId(89L);
        existing.setStartDate(LocalDate.now().plusDays(10));
        existing.setEndDate(LocalDate.now().plusDays(20));
        existing.setStatus(BookingStatus.ACCEPTED);

        MachineryBookingRequest request = new MachineryBookingRequest(start, end, "Overlap End", "Loc", "Remarks");

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(10L)).thenReturn(Optional.of(contractor));
        when(machineryRepository.findById(100L)).thenReturn(Optional.of(verifiedMachine));
        when(bookingRepository.findOverlappingAcceptedBookings(100L, start, end, null))
                .thenReturn(List.of(existing));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                bookingService.createBookingRequest(100L, "contractor@example.com", request));

        assertTrue(ex.getMessage().contains("Conflicts with an accepted booking"));
    }

    @Test
    @DisplayName("Overlapping scenario: Enclosing date range over existing accepted booking is rejected")
    void testOverlapScenario_enclosure_fails() {
        // Existing accepted: Nov 10 - Nov 15
        // New request: Nov 05 - Nov 20 (completely encloses Nov 10-15)
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(20);

        MachineryBooking existing = new MachineryBooking();
        existing.setId(90L);
        existing.setStartDate(LocalDate.now().plusDays(10));
        existing.setEndDate(LocalDate.now().plusDays(15));
        existing.setStatus(BookingStatus.ACCEPTED);

        MachineryBookingRequest request = new MachineryBookingRequest(start, end, "Enclosing", "Loc", "Remarks");

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(10L)).thenReturn(Optional.of(contractor));
        when(machineryRepository.findById(100L)).thenReturn(Optional.of(verifiedMachine));
        when(bookingRepository.findOverlappingAcceptedBookings(100L, start, end, null))
                .thenReturn(List.of(existing));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                bookingService.createBookingRequest(100L, "contractor@example.com", request));

        assertTrue(ex.getMessage().contains("Conflicts with an accepted booking"));
    }

    @Test
    @DisplayName("Non-overlapping booking request succeeds")
    void testOverlapScenario_nonOverlapping_success() {
        // Existing accepted: Nov 10 - Nov 15
        // New request: Nov 16 - Nov 20 (no overlap)
        LocalDate start = LocalDate.now().plusDays(16);
        LocalDate end = LocalDate.now().plusDays(20);

        MachineryBookingRequest request = new MachineryBookingRequest(start, end, "Non Overlapping", "Loc", "Remarks");

        when(userRepository.findByEmail("contractor@example.com")).thenReturn(Optional.of(contractorUser));
        when(contractorRepository.findByUserId(10L)).thenReturn(Optional.of(contractor));
        when(machineryRepository.findById(100L)).thenReturn(Optional.of(verifiedMachine));
        when(bookingRepository.findOverlappingAcceptedBookings(100L, start, end, null))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(MachineryBooking.class))).thenAnswer(inv -> {
            MachineryBooking b = inv.getArgument(0);
            b.setId(999L);
            return b;
        });

        MachineryBookingResponse response = bookingService.createBookingRequest(100L, "contractor@example.com", request);

        assertNotNull(response);
        assertEquals(BookingStatus.PENDING, response.getStatus());
        assertEquals(5, response.getTotalDays());
    }
}
