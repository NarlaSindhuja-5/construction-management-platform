package com.e2e.construction.repository;

import com.e2e.construction.entity.AttendanceStatus;
import com.e2e.construction.entity.LaborAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LaborAttendanceRepository extends JpaRepository<LaborAttendance, Long> {

    Optional<LaborAttendance> findByLaborerIdAndProjectIdAndAttendanceDate(Long laborerId, Long projectId, LocalDate attendanceDate);

    boolean existsByLaborerIdAndProjectIdAndAttendanceDate(Long laborerId, Long projectId, LocalDate attendanceDate);

    boolean existsByLaborerIdAndProjectIdAndAttendanceDateAndIdNot(Long laborerId, Long projectId, LocalDate attendanceDate, Long id);

    List<LaborAttendance> findByProjectIdOrderByAttendanceDateDesc(Long projectId);

    List<LaborAttendance> findByLaborerIdOrderByAttendanceDateDesc(Long laborerId);

    @Query("SELECT a FROM LaborAttendance a WHERE " +
            "(:projectId IS NULL OR a.project.id = :projectId) AND " +
            "(:contractorId IS NULL OR a.project.contractor.id = :contractorId) AND " +
            "(:laborerId IS NULL OR a.laborer.id = :laborerId) AND " +
            "(:date IS NULL OR a.attendanceDate = :date) AND " +
            "(:startDate IS NULL OR a.attendanceDate >= :startDate) AND " +
            "(:endDate IS NULL OR a.attendanceDate <= :endDate) AND " +
            "(:status IS NULL OR a.status = :status) " +
            "ORDER BY a.attendanceDate DESC, a.id DESC")
    List<LaborAttendance> filterAttendance(
            @Param("projectId") Long projectId,
            @Param("contractorId") Long contractorId,
            @Param("laborerId") Long laborerId,
            @Param("date") LocalDate date,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") AttendanceStatus status
    );
}
