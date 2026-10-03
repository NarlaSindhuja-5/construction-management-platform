package com.e2e.construction.repository;

import com.e2e.construction.entity.BookingStatus;
import com.e2e.construction.entity.LaborRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LaborRequestRepository extends JpaRepository<LaborRequest, Long> {

    @Query("SELECT r FROM LaborRequest r WHERE r.laborer.id = :laborerId " +
            "AND r.status = com.e2e.construction.entity.BookingStatus.ACCEPTED " +
            "AND r.startDate <= :endDate AND r.endDate >= :startDate " +
            "AND (:excludeRequestId IS NULL OR r.id <> :excludeRequestId)")
    List<LaborRequest> findOverlappingAcceptedRequests(
            @Param("laborerId") Long laborerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeRequestId") Long excludeRequestId);

    List<LaborRequest> findByContractorIdOrderByCreatedAtDesc(Long contractorId);

    List<LaborRequest> findByContractorIdAndStatusOrderByCreatedAtDesc(Long contractorId, BookingStatus status);

    List<LaborRequest> findByLaborerIdOrderByCreatedAtDesc(Long laborerId);

    List<LaborRequest> findByLaborerIdAndStatusOrderByCreatedAtDesc(Long laborerId, BookingStatus status);

    List<LaborRequest> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
