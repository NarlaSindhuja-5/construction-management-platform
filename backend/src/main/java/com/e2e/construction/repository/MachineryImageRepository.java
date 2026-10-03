package com.e2e.construction.repository;

import com.e2e.construction.entity.MachineryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MachineryImageRepository extends JpaRepository<MachineryImage, Long> {

    List<MachineryImage> findByMachineryIdOrderByCreatedAtDesc(Long machineryId);

    Optional<MachineryImage> findByIdAndMachineryId(Long id, Long machineryId);
}
