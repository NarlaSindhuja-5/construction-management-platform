package com.e2e.construction.repository;

import com.e2e.construction.entity.MachineryMaintenanceImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MachineryMaintenanceImageRepository extends JpaRepository<MachineryMaintenanceImage, Long> {

    List<MachineryMaintenanceImage> findByMaintenanceIdOrderByCreatedAtDesc(Long maintenanceId);
}
