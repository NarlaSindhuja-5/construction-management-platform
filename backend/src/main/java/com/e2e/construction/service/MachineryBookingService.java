package com.e2e.construction.service;

import com.e2e.construction.dto.BookedDateRangeResponse;
import com.e2e.construction.dto.MachineConditionResponse;
import com.e2e.construction.dto.MachineProfileResponse;
import com.e2e.construction.dto.MachineryBookingRequest;
import com.e2e.construction.dto.MachineryBookingResponse;
import com.e2e.construction.dto.MachineryImageResponse;
import com.e2e.construction.dto.MachineryMaintenanceResponse;
import com.e2e.construction.dto.MachineryResponse;
import com.e2e.construction.entity.BookingStatus;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.Machinery;
import com.e2e.construction.entity.MachineryAvailability;
import com.e2e.construction.entity.MachineryBooking;
import com.e2e.construction.entity.MachineryCategory;
import com.e2e.construction.entity.MachineryOwner;
import com.e2e.construction.entity.User;
import com.e2e.construction.entity.VerificationStatus;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.MachineryBookingRepository;
import com.e2e.construction.repository.MachineryOwnerRepository;
import com.e2e.construction.repository.MachineryRepository;
import com.e2e.construction.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MachineryBookingService {

    private final MachineryBookingRepository bookingRepository;
    private final MachineryRepository machineryRepository;
    private final ContractorRepository contractorRepository;
    private final MachineryOwnerRepository machineryOwnerRepository;
    private final UserRepository userRepository;
    private final MachineryImageService machineryImageService;
    private final MachineConditionService machineConditionService;
    private final MachineryMaintenanceService machineryMaintenanceService;

    public MachineryBookingService(
            MachineryBookingRepository bookingRepository,
            MachineryRepository machineryRepository,
            ContractorRepository contractorRepository,
            MachineryOwnerRepository machineryOwnerRepository,
            UserRepository userRepository,
            MachineryImageService machineryImageService,
            MachineConditionService machineConditionService,
            MachineryMaintenanceService machineryMaintenanceService) {
        this.bookingRepository = bookingRepository;
        this.machineryRepository = machineryRepository;
        this.contractorRepository = contractorRepository;
        this.machineryOwnerRepository = machineryOwnerRepository;
        this.userRepository = userRepository;
        this.machineryImageService = machineryImageService;
        this.machineConditionService = machineConditionService;
        this.machineryMaintenanceService = machineryMaintenanceService;
    }

    /**
     * 1. Search verified machinery available for public booking.
     */
    @Transactional(readOnly = true)
    public List<MachineryResponse> searchVerifiedMachinery(
            MachineryCategory category,
            MachineryAvailability availability,
            String location,
            String search) {
        return machineryRepository.searchVerifiedMachinery(category, availability, location, search)
                .stream()
                .map(MachineryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 2-7. Open a complete machine profile:
     * - Specifications
     * - Images
     * - Condition report
     * - Maintenance history
     * - Availability & booked date calendar
     */
    @Transactional
    public MachineProfileResponse getMachineProfile(Long machineId) {
        Machinery machine = machineryRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machinery", "id", machineId));

        MachineryResponse specs = MachineryResponse.fromEntity(machine);
        List<MachineryImageResponse> images = machineryImageService.getMachineImages(machineId);
        MachineConditionResponse condition = machineConditionService.getLatestCondition(machineId);
        List<MachineryMaintenanceResponse> maintenanceHistory = machineryMaintenanceService.getMaintenanceHistory(machineId, "desc");

        List<BookedDateRangeResponse> bookedRanges = bookingRepository
                .findByMachineryIdAndStatusAndEndDateGreaterThanEqualOrderByStartDateAsc(
                        machineId,
                        BookingStatus.ACCEPTED,
                        LocalDate.now()
                ).stream()
                .map(b -> new BookedDateRangeResponse(b.getId(), b.getStartDate(), b.getEndDate(), b.getStatus()))
                .collect(Collectors.toList());

        return new MachineProfileResponse(specs, images, condition, maintenanceHistory, machine.getAvailabilityStatus(), bookedRanges);
    }

    /**
     * View availability calendar and booked date ranges for a machine.
     */
    @Transactional(readOnly = true)
    public List<BookedDateRangeResponse> getMachineBookedRanges(Long machineId) {
        if (!machineryRepository.existsById(machineId)) {
            throw new ResourceNotFoundException("Machinery", "id", machineId);
        }

        return bookingRepository.findByMachineryIdAndStatusAndEndDateGreaterThanEqualOrderByStartDateAsc(
                machineId,
                BookingStatus.ACCEPTED,
                LocalDate.now()
        ).stream()
        .map(b -> new BookedDateRangeResponse(b.getId(), b.getStartDate(), b.getEndDate(), b.getStatus()))
        .collect(Collectors.toList());
    }

    /**
     * 8-10. Send booking request by authenticated contractor.
     * Prevents overlapping bookings with already accepted dates.
     */
    @Transactional
    public MachineryBookingResponse createBookingRequest(
            Long machineId,
            String contractorEmail,
            MachineryBookingRequest request) {

        // Validate Contractor identity
        User user = userRepository.findByEmail(contractorEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", contractorEmail));

        Contractor contractor = contractorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Please create your Contractor Profile before requesting machine bookings. Use POST /api/contractors/profile."));

        Machinery machine = machineryRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machinery", "id", machineId));

        // Requirement: Only verified machinery should be available for public booking
        if (machine.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BadRequestException("Only verified machinery is available for booking. Current verification status: " + machine.getVerificationStatus());
        }

        // Date validation
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (startDate == null || endDate == null) {
            throw new BadRequestException("Both start date and end date are required.");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date cannot be in the past. (Requested: " + startDate + ")");
        }

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date (" + endDate + ") cannot be before start date (" + startDate + ").");
        }

        // Prevent double booking at request time
        List<MachineryBooking> overlappingAccepted = bookingRepository.findOverlappingAcceptedBookings(
                machineId, startDate, endDate, null
        );

        if (!overlappingAccepted.isEmpty()) {
            MachineryBooking conflict = overlappingAccepted.get(0);
            throw new BadRequestException(String.format(
                    "Machine is unavailable for requested dates [%s to %s]. Conflicts with an accepted booking from %s to %s.",
                    startDate, endDate, conflict.getStartDate(), conflict.getEndDate()
            ));
        }

        // Price and duration calculation
        int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal dailyPrice = machine.getRentalPricePerDay();
        BigDecimal totalPrice = dailyPrice.multiply(BigDecimal.valueOf(totalDays));

        MachineryBooking booking = new MachineryBooking(
                machine,
                contractor,
                startDate,
                endDate,
                totalDays,
                dailyPrice,
                totalPrice,
                request.getProjectName(),
                request.getDeliveryLocation(),
                request.getRemarks()
        );

        booking = bookingRepository.save(booking);
        return MachineryBookingResponse.fromEntity(booking);
    }

    /**
     * Accept a booking request.
     * Enforces STRICT double-booking prevention under database transaction.
     */
    @Transactional
    public MachineryBookingResponse acceptBooking(Long bookingId, String userEmail, String notes) {
        MachineryBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("MachineryBooking", "id", bookingId));

        verifyOwnerOrAdmin(booking.getMachinery(), userEmail);

        if (booking.getStatus() == BookingStatus.ACCEPTED) {
            return MachineryBookingResponse.fromEntity(booking);
        }

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Cannot accept a booking that is currently " + booking.getStatus());
        }

        // CRITICAL: Prevent double booking. A machine must not have two accepted bookings for overlapping dates.
        List<MachineryBooking> overlapping = bookingRepository.findOverlappingAcceptedBookings(
                booking.getMachinery().getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getId()
        );

        if (!overlapping.isEmpty()) {
            MachineryBooking conflict = overlapping.get(0);
            throw new BadRequestException(String.format(
                    "Double-booking prevented: Machine already has an accepted booking (ID: %d) from %s to %s for overlapping dates.",
                    conflict.getId(), conflict.getStartDate(), conflict.getEndDate()
            ));
        }

        booking.setStatus(BookingStatus.ACCEPTED);
        if (notes != null && !notes.trim().isEmpty()) {
            booking.setOwnerNotes(notes.trim());
        }

        // Update machine status if today is in booking range
        Machinery machine = booking.getMachinery();
        LocalDate today = LocalDate.now();
        if (!today.isBefore(booking.getStartDate()) && !today.isAfter(booking.getEndDate())) {
            machine.setAvailabilityStatus(MachineryAvailability.BOOKED);
            machineryRepository.save(machine);
        }

        booking = bookingRepository.save(booking);
        return MachineryBookingResponse.fromEntity(booking);
    }

    /**
     * Reject a booking request.
     */
    @Transactional
    public MachineryBookingResponse rejectBooking(Long bookingId, String userEmail, String rejectionReason) {
        MachineryBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("MachineryBooking", "id", bookingId));

        verifyOwnerOrAdmin(booking.getMachinery(), userEmail);

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Cannot reject an already completed booking.");
        }

        booking.setStatus(BookingStatus.REJECTED);
        if (rejectionReason != null && !rejectionReason.trim().isEmpty()) {
            booking.setRejectionReason(rejectionReason.trim());
        }

        booking = bookingRepository.save(booking);
        return MachineryBookingResponse.fromEntity(booking);
    }

    /**
     * Cancel a booking. Either the contractor or owner/admin can cancel.
     */
    @Transactional
    public MachineryBookingResponse cancelBooking(Long bookingId, String userEmail, String reason) {
        MachineryBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("MachineryBooking", "id", bookingId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName());
        boolean isOwner = booking.getMachinery().getOwner() != null &&
                booking.getMachinery().getOwner().getUser() != null &&
                booking.getMachinery().getOwner().getUser().getEmail().equalsIgnoreCase(userEmail.trim());
        boolean isContractor = booking.getContractor() != null &&
                booking.getContractor().getUser() != null &&
                booking.getContractor().getUser().getEmail().equalsIgnoreCase(userEmail.trim());

        if (!isAdmin && !isOwner && !isContractor) {
            throw new AccessDeniedException("Access denied: You do not have permission to cancel this booking.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        if (reason != null && !reason.trim().isEmpty()) {
            booking.setRemarks((booking.getRemarks() != null ? booking.getRemarks() + " | Cancellation Reason: " : "Cancellation Reason: ") + reason.trim());
        }

        // If machine was BOOKED, set back to AVAILABLE if no other active booking
        Machinery machine = booking.getMachinery();
        List<MachineryBooking> activeAccepted = bookingRepository.findOverlappingAcceptedBookings(
                machine.getId(), LocalDate.now(), LocalDate.now(), booking.getId()
        );
        if (activeAccepted.isEmpty() && machine.getAvailabilityStatus() == MachineryAvailability.BOOKED) {
            machine.setAvailabilityStatus(MachineryAvailability.AVAILABLE);
            machineryRepository.save(machine);
        }

        booking = bookingRepository.save(booking);
        return MachineryBookingResponse.fromEntity(booking);
    }

    /**
     * Mark booking completed.
     */
    @Transactional
    public MachineryBookingResponse completeBooking(Long bookingId, String userEmail) {
        MachineryBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("MachineryBooking", "id", bookingId));

        verifyOwnerOrAdmin(booking.getMachinery(), userEmail);

        booking.setStatus(BookingStatus.COMPLETED);

        Machinery machine = booking.getMachinery();
        machine.setAvailabilityStatus(MachineryAvailability.AVAILABLE);
        machineryRepository.save(machine);

        booking = bookingRepository.save(booking);
        return MachineryBookingResponse.fromEntity(booking);
    }

    /**
     * View booking requests for machinery owner (PENDING).
     */
    @Transactional(readOnly = true)
    public List<MachineryBookingResponse> getOwnerBookingRequests(String userEmail) {
        MachineryOwner owner = getOwnerByUserEmail(userEmail);
        return bookingRepository.findByMachineryOwnerIdAndStatusOrderByCreatedAtDesc(owner.getId(), BookingStatus.PENDING)
                .stream()
                .map(MachineryBookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * View current active/accepted bookings for machinery owner.
     */
    @Transactional(readOnly = true)
    public List<MachineryBookingResponse> getOwnerCurrentBookings(String userEmail) {
        MachineryOwner owner = getOwnerByUserEmail(userEmail);
        return bookingRepository.findByMachineryOwnerIdAndStatusOrderByCreatedAtDesc(owner.getId(), BookingStatus.ACCEPTED)
                .stream()
                .map(MachineryBookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * View full booking history for machinery owner.
     */
    @Transactional(readOnly = true)
    public List<MachineryBookingResponse> getOwnerBookingHistory(String userEmail, BookingStatus status) {
        MachineryOwner owner = getOwnerByUserEmail(userEmail);
        List<MachineryBooking> list;
        if (status != null) {
            list = bookingRepository.findByMachineryOwnerIdAndStatusOrderByCreatedAtDesc(owner.getId(), status);
        } else {
            list = bookingRepository.findByMachineryOwnerIdOrderByCreatedAtDesc(owner.getId());
        }
        return list.stream().map(MachineryBookingResponse::fromEntity).collect(Collectors.toList());
    }

    /**
     * View contractor's own bookings.
     */
    @Transactional(readOnly = true)
    public List<MachineryBookingResponse> getContractorBookings(String userEmail, BookingStatus status) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Contractor contractor = contractorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Contractor profile not found."));

        List<MachineryBooking> list;
        if (status != null) {
            list = bookingRepository.findByContractorIdAndStatusOrderByCreatedAtDesc(contractor.getId(), status);
        } else {
            list = bookingRepository.findByContractorIdOrderByCreatedAtDesc(contractor.getId());
        }

        return list.stream().map(MachineryBookingResponse::fromEntity).collect(Collectors.toList());
    }

    /**
     * View booking details by ID.
     */
    @Transactional(readOnly = true)
    public MachineryBookingResponse getBookingById(Long bookingId, String userEmail) {
        MachineryBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("MachineryBooking", "id", bookingId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName());
        boolean isOwner = booking.getMachinery().getOwner() != null &&
                booking.getMachinery().getOwner().getUser() != null &&
                booking.getMachinery().getOwner().getUser().getEmail().equalsIgnoreCase(userEmail.trim());
        boolean isContractor = booking.getContractor() != null &&
                booking.getContractor().getUser() != null &&
                booking.getContractor().getUser().getEmail().equalsIgnoreCase(userEmail.trim());

        if (!isAdmin && !isOwner && !isContractor) {
            throw new AccessDeniedException("Access denied: You do not have permission to view this booking.");
        }

        return MachineryBookingResponse.fromEntity(booking);
    }

    private MachineryOwner getOwnerByUserEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        return machineryOwnerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Machinery owner profile not found for user: " + userEmail));
    }

    private void verifyOwnerOrAdmin(Machinery machine, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName());
        boolean isOwner = machine.getOwner() != null &&
                machine.getOwner().getUser() != null &&
                machine.getOwner().getUser().getEmail().equalsIgnoreCase(userEmail.trim());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Access denied: Only the machine owner or authorized administrator can manage this booking.");
        }
    }
}
