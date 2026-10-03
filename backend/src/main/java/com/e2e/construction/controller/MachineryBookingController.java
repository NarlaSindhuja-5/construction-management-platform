package com.e2e.construction.controller;

import com.e2e.construction.dto.BookedDateRangeResponse;
import com.e2e.construction.dto.MachineProfileResponse;
import com.e2e.construction.dto.MachineryBookingRequest;
import com.e2e.construction.dto.MachineryBookingResponse;
import com.e2e.construction.dto.MachineryResponse;
import com.e2e.construction.entity.BookingStatus;
import com.e2e.construction.entity.MachineryAvailability;
import com.e2e.construction.entity.MachineryCategory;
import com.e2e.construction.service.MachineryBookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/machinery")
public class MachineryBookingController {

    private final MachineryBookingService bookingService;

    public MachineryBookingController(MachineryBookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * 1. Search verified machinery.
     * GET /api/machinery/verified
     */
    @GetMapping("/verified")
    public ResponseEntity<List<MachineryResponse>> searchVerifiedMachinery(
            @RequestParam(required = false) MachineryCategory category,
            @RequestParam(required = false) MachineryAvailability availability,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String search) {
        List<MachineryResponse> response = bookingService.searchVerifiedMachinery(category, availability, location, search);
        return ResponseEntity.ok(response);
    }

    /**
     * 2-7. Open a machine profile:
     * - View machine images
     * - View specifications
     * - View condition
     * - View maintenance history
     * - View availability
     * GET /api/machinery/{id}/profile
     */
    @GetMapping("/{id}/profile")
    public ResponseEntity<MachineProfileResponse> getMachineProfile(@PathVariable Long id) {
        MachineProfileResponse response = bookingService.getMachineProfile(id);
        return ResponseEntity.ok(response);
    }

    /**
     * View availability & booked date calendar.
     * GET /api/machinery/{id}/availability
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<List<BookedDateRangeResponse>> getMachineAvailability(@PathVariable Long id) {
        List<BookedDateRangeResponse> response = bookingService.getMachineBookedRanges(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 8-10. Send booking request by authenticated contractor.
     * POST /api/machinery/{machineId}/bookings
     */
    @PostMapping({ "/{machineId}/bookings", "/{machineId}/book" })
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<MachineryBookingResponse> createBookingRequest(
            @PathVariable Long machineId,
            Principal principal,
            @Valid @RequestBody MachineryBookingRequest request) {

        MachineryBookingResponse response = bookingService.createBookingRequest(
                machineId,
                principal.getName(),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Contractor views their own booking requests / bookings.
     * GET /api/machinery/bookings/my-bookings
     */
    @GetMapping("/bookings/my-bookings")
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<List<MachineryBookingResponse>> getMyBookings(
            Principal principal,
            @RequestParam(required = false) BookingStatus status) {

        List<MachineryBookingResponse> response = bookingService.getContractorBookings(principal.getName(), status);
        return ResponseEntity.ok(response);
    }

    /**
     * Machinery owner views incoming booking requests (PENDING).
     * GET /api/machinery/bookings/owner-requests
     */
    @GetMapping({ "/bookings/owner-requests", "/bookings/requests" })
    @PreAuthorize("hasRole('MACHINERY_OWNER')")
    public ResponseEntity<List<MachineryBookingResponse>> getOwnerBookingRequests(Principal principal) {
        List<MachineryBookingResponse> response = bookingService.getOwnerBookingRequests(principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Machinery owner views current active bookings (ACCEPTED).
     * GET /api/machinery/bookings/owner-current
     */
    @GetMapping("/bookings/owner-current")
    @PreAuthorize("hasRole('MACHINERY_OWNER')")
    public ResponseEntity<List<MachineryBookingResponse>> getOwnerCurrentBookings(Principal principal) {
        List<MachineryBookingResponse> response = bookingService.getOwnerCurrentBookings(principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Machinery owner views full booking history.
     * GET /api/machinery/bookings/owner-history
     */
    @GetMapping({ "/bookings/owner-history", "/bookings/owner" })
    @PreAuthorize("hasRole('MACHINERY_OWNER')")
    public ResponseEntity<List<MachineryBookingResponse>> getOwnerBookingHistory(
            Principal principal,
            @RequestParam(required = false) BookingStatus status) {

        List<MachineryBookingResponse> response = bookingService.getOwnerBookingHistory(principal.getName(), status);
        return ResponseEntity.ok(response);
    }

    /**
     * View single booking details by ID.
     * GET /api/machinery/bookings/{id}
     */
    @GetMapping("/bookings/{id}")
    public ResponseEntity<MachineryBookingResponse> getBookingById(
            @PathVariable Long id,
            Principal principal) {

        MachineryBookingResponse response = bookingService.getBookingById(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Machinery owner or admin accepts booking.
     * Enforces STRICT double-booking prevention under transaction.
     * PUT /api/machinery/bookings/{id}/accept
     */
    @PutMapping("/bookings/{id}/accept")
    @PreAuthorize("hasAnyRole('MACHINERY_OWNER', 'ADMIN')")
    public ResponseEntity<MachineryBookingResponse> acceptBooking(
            @PathVariable Long id,
            Principal principal,
            @RequestBody(required = false) Map<String, String> body) {

        String notes = body != null ? body.get("notes") : null;
        MachineryBookingResponse response = bookingService.acceptBooking(id, principal.getName(), notes);
        return ResponseEntity.ok(response);
    }

    /**
     * Machinery owner or admin rejects booking.
     * PUT /api/machinery/bookings/{id}/reject
     */
    @PutMapping("/bookings/{id}/reject")
    @PreAuthorize("hasAnyRole('MACHINERY_OWNER', 'ADMIN')")
    public ResponseEntity<MachineryBookingResponse> rejectBooking(
            @PathVariable Long id,
            Principal principal,
            @RequestBody(required = false) Map<String, String> body) {

        String reason = body != null ? body.get("rejectionReason") : null;
        MachineryBookingResponse response = bookingService.rejectBooking(id, principal.getName(), reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel booking. Either contractor or owner/admin can cancel.
     * PUT /api/machinery/bookings/{id}/cancel
     */
    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<MachineryBookingResponse> cancelBooking(
            @PathVariable Long id,
            Principal principal,
            @RequestBody(required = false) Map<String, String> body) {

        String reason = body != null ? body.get("reason") : null;
        MachineryBookingResponse response = bookingService.cancelBooking(id, principal.getName(), reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark booking as completed.
     * PUT /api/machinery/bookings/{id}/complete
     */
    @PutMapping("/bookings/{id}/complete")
    @PreAuthorize("hasAnyRole('MACHINERY_OWNER', 'ADMIN')")
    public ResponseEntity<MachineryBookingResponse> completeBooking(
            @PathVariable Long id,
            Principal principal) {

        MachineryBookingResponse response = bookingService.completeBooking(id, principal.getName());
        return ResponseEntity.ok(response);
    }
}
