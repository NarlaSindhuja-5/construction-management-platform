package com.e2e.construction.repository;

import com.e2e.construction.entity.Tender;
import com.e2e.construction.entity.TenderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TenderRepository extends JpaRepository<Tender, Long> {

    Optional<Tender> findByTenderNumber(String tenderNumber);

    boolean existsByTenderNumber(String tenderNumber);

    boolean existsByTenderNumberAndIdNot(String tenderNumber, Long id);

    @Query("SELECT t FROM Tender t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:category IS NULL OR LOWER(t.category) = LOWER(:category)) AND " +
            "(:department IS NULL OR LOWER(t.department) LIKE LOWER(CONCAT('%', :department, '%'))) AND " +
            "(:location IS NULL OR LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:minEstimatedValue IS NULL OR t.estimatedValue >= :minEstimatedValue) AND " +
            "(:maxEstimatedValue IS NULL OR t.estimatedValue <= :maxEstimatedValue) AND " +
            "(:closingAfter IS NULL OR t.closingDate >= :closingAfter) AND " +
            "(:closingBefore IS NULL OR t.closingDate <= :closingBefore) AND " +
            "(:search IS NULL OR (" +
            "   LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "   LOWER(t.tenderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "   LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "   LOWER(t.department) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "   LOWER(t.location) LIKE LOWER(CONCAT('%', :search, '%'))" +
            ")) " +
            "ORDER BY t.closingDate ASC, t.id DESC")
    List<Tender> searchAndFilterTenders(
            @Param("search") String search,
            @Param("status") TenderStatus status,
            @Param("category") String category,
            @Param("department") String department,
            @Param("location") String location,
            @Param("minEstimatedValue") BigDecimal minEstimatedValue,
            @Param("maxEstimatedValue") BigDecimal maxEstimatedValue,
            @Param("closingAfter") LocalDate closingAfter,
            @Param("closingBefore") LocalDate closingBefore
    );
}
