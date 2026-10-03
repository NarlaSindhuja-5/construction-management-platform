package com.e2e.construction.repository;

import com.e2e.construction.entity.Contractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractorRepository extends JpaRepository<Contractor, Long> {

    Optional<Contractor> findByUserId(Long userId);

    Optional<Contractor> findByUserEmail(String email);

    boolean existsByUserId(Long userId);
}
