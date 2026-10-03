package com.e2e.construction.repository;

import com.e2e.construction.entity.MachineryOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MachineryOwnerRepository extends JpaRepository<MachineryOwner, Long> {

    Optional<MachineryOwner> findByUserId(Long userId);

    Optional<MachineryOwner> findByUserEmail(String email);

    boolean existsByUserId(Long userId);
}
