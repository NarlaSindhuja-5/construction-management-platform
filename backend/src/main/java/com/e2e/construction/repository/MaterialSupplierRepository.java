package com.e2e.construction.repository;

import com.e2e.construction.entity.MaterialSupplier;
import com.e2e.construction.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialSupplierRepository extends JpaRepository<MaterialSupplier, Long> {

    Optional<MaterialSupplier> findByUserId(Long userId);

    Optional<MaterialSupplier> findByUserEmail(String email);

    boolean existsByUserId(Long userId);

    List<MaterialSupplier> findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus status);

    List<MaterialSupplier> findAllByOrderByCreatedAtDesc();
}
