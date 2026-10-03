package com.e2e.construction.repository;

import com.e2e.construction.entity.LaborAssignment;
import com.e2e.construction.entity.LaborAssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LaborAssignmentRepository extends JpaRepository<LaborAssignment, Long> {

    @Query("SELECT a FROM LaborAssignment a WHERE a.laborer.id = :laborerId " +
            "AND a.status = com.e2e.construction.entity.LaborAssignmentStatus.ACTIVE " +
            "AND a.startDate <= :endDate AND a.endDate >= :startDate " +
            "AND (:excludeAssignmentId IS NULL OR a.id <> :excludeAssignmentId)")
    List<LaborAssignment> findOverlappingActiveAssignments(
            @Param("laborerId") Long laborerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeAssignmentId") Long excludeAssignmentId);

    List<LaborAssignment> findByLaborerIdOrderByStartDateDesc(Long laborerId);

    List<LaborAssignment> findByLaborerIdAndStatusOrderByStartDateDesc(Long laborerId, LaborAssignmentStatus status);

    List<LaborAssignment> findByProjectIdOrderByStartDateDesc(Long projectId);

    List<LaborAssignment> findByContractorIdOrderByStartDateDesc(Long contractorId);

    Optional<LaborAssignment> findByLaborRequestId(Long laborRequestId);
}
