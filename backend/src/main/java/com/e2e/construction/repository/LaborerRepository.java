package com.e2e.construction.repository;

import com.e2e.construction.entity.Laborer;
import com.e2e.construction.entity.LaborerAvailability;
import com.e2e.construction.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface LaborerRepository extends JpaRepository<Laborer, Long> {

    Optional<Laborer> findByUserId(Long userId);

    Optional<Laborer> findByUserEmail(String email);

    boolean existsByUserId(Long userId);

    List<Laborer> findByAvailabilityStatus(LaborerAvailability availabilityStatus);

    List<Laborer> findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus status);

    List<Laborer> findAllByOrderByCreatedAtDesc();

    @Query("SELECT l FROM Laborer l WHERE l.verificationStatus = com.e2e.construction.entity.VerificationStatus.VERIFIED AND " +
            "(:location IS NULL OR LOWER(l.location) LIKE LOWER(CONCAT('%', :location, '%')) " +
            " OR LOWER(l.city) LIKE LOWER(CONCAT('%', :location, '%')) " +
            " OR LOWER(l.state) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:skill IS NULL OR LOWER(l.skills) LIKE LOWER(CONCAT('%', :skill, '%'))) AND " +
            "(:minExperience IS NULL OR l.yearsOfExperience >= :minExperience) AND " +
            "(:availability IS NULL OR l.availabilityStatus = :availability) AND " +
            "(:maxDailyWage IS NULL OR l.dailyWage <= :maxDailyWage) " +
            "ORDER BY l.createdAt DESC")
    List<Laborer> searchVerifiedLaborers(
            @Param("location") String location,
            @Param("skill") String skill,
            @Param("minExperience") Integer minExperience,
            @Param("availability") LaborerAvailability availability,
            @Param("maxDailyWage") BigDecimal maxDailyWage);
}
