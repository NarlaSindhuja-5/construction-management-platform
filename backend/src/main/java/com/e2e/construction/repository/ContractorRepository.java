package com.e2e.construction.repository;

import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractorRepository extends JpaRepository<Contractor, Long> {

    Optional<Contractor> findByUserId(Long userId);

    Optional<Contractor> findByUserEmail(String email);

    boolean existsByUserId(Long userId);

    List<Contractor> findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus status);

    List<Contractor> findAllByOrderByCreatedAtDesc();

    @Query("SELECT c FROM Contractor c WHERE c.verificationStatus = com.e2e.construction.entity.VerificationStatus.VERIFIED AND " +
            "(:city IS NULL OR LOWER(c.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
            "(:specialization IS NULL OR LOWER(c.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))) " +
            "ORDER BY c.rating DESC, c.createdAt DESC")
    List<Contractor> searchVerifiedContractors(
            @Param("city") String city,
            @Param("specialization") String specialization);
}
