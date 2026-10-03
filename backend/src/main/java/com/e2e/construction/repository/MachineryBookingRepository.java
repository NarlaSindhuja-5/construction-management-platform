package com.e2e.construction.repository;

import com.e2e.construction.entity.BookingStatus;
import com.e2e.construction.entity.MachineryBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MachineryBookingRepository extends JpaRepository<MachineryBooking, Long> {

    @Query("SELECT b FROM MachineryBooking b WHERE b.machinery.id = :machineryId " +
            "AND b.status = com.e2e.construction.entity.BookingStatus.ACCEPTED " +
            "AND b.startDate <= :endDate AND b.endDate >= :startDate " +
            "AND (:excludeBookingId IS NULL OR b.id <> :excludeBookingId)")
    List<MachineryBooking> findOverlappingAcceptedBookings(
            @Param("machineryId") Long machineryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeBookingId") Long excludeBookingId);

    List<MachineryBooking> findByContractorIdOrderByCreatedAtDesc(Long contractorId);

    List<MachineryBooking> findByContractorIdAndStatusOrderByCreatedAtDesc(Long contractorId, BookingStatus status);

    List<MachineryBooking> findByMachineryOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<MachineryBooking> findByMachineryOwnerIdAndStatusOrderByCreatedAtDesc(Long ownerId, BookingStatus status);

    List<MachineryBooking> findByMachineryIdOrderByStartDateDesc(Long machineryId);

    List<MachineryBooking> findByMachineryIdAndStatusOrderByStartDateAsc(Long machineryId, BookingStatus status);

    List<MachineryBooking> findByMachineryIdAndStatusAndEndDateGreaterThanEqualOrderByStartDateAsc(
            Long machineryId, BookingStatus status, LocalDate minEndDate);
}
