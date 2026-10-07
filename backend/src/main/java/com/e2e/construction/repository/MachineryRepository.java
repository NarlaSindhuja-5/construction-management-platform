package com.e2e.construction.repository;

import com.e2e.construction.entity.Machinery;
import com.e2e.construction.entity.MachineryAvailability;
import com.e2e.construction.entity.MachineryCategory;
import com.e2e.construction.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MachineryRepository extends JpaRepository<Machinery, Long> {

    List<Machinery> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<Machinery> findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus status);

    List<Machinery> findAllByOrderByCreatedAtDesc();

    @Query("SELECT m FROM Machinery m WHERE " +
            "m.verificationStatus = com.e2e.construction.entity.VerificationStatus.VERIFIED AND " +
            "(:category IS NULL OR m.category = :category) AND " +
            "(:availability IS NULL OR m.availabilityStatus = :availability) AND " +
            "(:location IS NULL OR LOWER(m.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:search IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(m.manufacturer) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(m.model) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY m.createdAt DESC")
    List<Machinery> searchAndFilterMachinery(
            @Param("category") MachineryCategory category,
            @Param("availability") MachineryAvailability availability,
            @Param("location") String location,
            @Param("search") String search);

    @Query("SELECT m FROM Machinery m WHERE m.owner.id = :ownerId AND " +
            "(:category IS NULL OR m.category = :category) AND " +
            "(:availability IS NULL OR m.availabilityStatus = :availability) AND " +
            "(:search IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(m.manufacturer) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(m.model) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY m.createdAt DESC")
    List<Machinery> searchAndFilterOwnerMachinery(
            @Param("ownerId") Long ownerId,
            @Param("category") MachineryCategory category,
            @Param("availability") MachineryAvailability availability,
            @Param("search") String search);

    @Query("SELECT m FROM Machinery m WHERE m.verificationStatus = com.e2e.construction.entity.VerificationStatus.VERIFIED AND " +
            "(:category IS NULL OR m.category = :category) AND " +
            "(:availability IS NULL OR m.availabilityStatus = :availability) AND " +
            "(:location IS NULL OR LOWER(m.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:search IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(m.manufacturer) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(m.model) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY m.createdAt DESC")
    List<Machinery> searchVerifiedMachinery(
            @Param("category") MachineryCategory category,
            @Param("availability") MachineryAvailability availability,
            @Param("location") String location,
            @Param("search") String search);
}
