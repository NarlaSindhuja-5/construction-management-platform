package com.e2e.construction.repository;

import com.e2e.construction.entity.DailyWorkReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyWorkReportRepository extends JpaRepository<DailyWorkReport, Long> {

    Optional<DailyWorkReport> findByProjectIdAndReportDate(Long projectId, LocalDate reportDate);

    boolean existsByProjectIdAndReportDate(Long projectId, LocalDate reportDate);

    boolean existsByProjectIdAndReportDateAndIdNot(Long projectId, LocalDate reportDate, Long id);

    List<DailyWorkReport> findByProjectIdOrderByReportDateDesc(Long projectId);

    List<DailyWorkReport> findByReportDateOrderByCreatedAtDesc(LocalDate reportDate);

    @Query("SELECT r FROM DailyWorkReport r " +
            "WHERE (:projectId IS NULL OR r.project.id = :projectId) " +
            "AND (:date IS NULL OR r.reportDate = :date) " +
            "AND (:startDate IS NULL OR r.reportDate >= :startDate) " +
            "AND (:endDate IS NULL OR r.reportDate <= :endDate) " +
            "ORDER BY r.reportDate DESC, r.id DESC")
    List<DailyWorkReport> filterReports(
            @Param("projectId") Long projectId,
            @Param("date") LocalDate date,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
