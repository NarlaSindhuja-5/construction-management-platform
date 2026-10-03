package com.e2e.construction.repository;

import com.e2e.construction.entity.MachineryMaintenance;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MachineryMaintenanceRepository extends JpaRepository<MachineryMaintenance, Long> {

    List<MachineryMaintenance> findByMachineryId(Long machineryId, Sort sort);

    List<MachineryMaintenance> findByMachineryIdOrderByMaintenanceDateDescIdDesc(Long machineryId);

    List<MachineryMaintenance> findByMachineryIdOrderByMaintenanceDateAscIdAsc(Long machineryId);
}
