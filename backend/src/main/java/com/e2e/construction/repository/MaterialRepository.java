package com.e2e.construction.repository;

import com.e2e.construction.entity.Material;
import com.e2e.construction.entity.MaterialAvailability;
import com.e2e.construction.entity.MaterialCategory;
import com.e2e.construction.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    @Query("SELECT m FROM Material m WHERE " +
            "m.verificationStatus = com.e2e.construction.entity.VerificationStatus.VERIFIED AND " +
            "(:search IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(m.gradeSpecification) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:category IS NULL OR m.category = :category) AND " +
            "(:location IS NULL OR LOWER(m.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:maxPrice IS NULL OR m.price <= :maxPrice) AND " +
            "(:availability IS NULL OR m.availability = :availability) " +
            "ORDER BY m.createdAt DESC")
    List<Material> searchMaterials(
            @Param("search") String search,
            @Param("category") MaterialCategory category,
            @Param("location") String location,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("availability") MaterialAvailability availability);

    List<Material> findBySupplierIdOrderByCreatedAtDesc(Long supplierId);

    List<Material> findBySupplierUserEmailOrderByCreatedAtDesc(String email);

    List<Material> findByCategoryOrderByCreatedAtDesc(MaterialCategory category);

    List<Material> findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus status);

    List<Material> findAllByOrderByCreatedAtDesc();
}
