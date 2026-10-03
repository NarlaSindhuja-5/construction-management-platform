package com.e2e.construction.repository;

import com.e2e.construction.entity.MachineCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MachineConditionRepository extends JpaRepository<MachineCondition, Long> {

    List<MachineCondition> findByMachineryIdOrderByInspectionDateDescCreatedAtDesc(Long machineryId);

    Optional<MachineCondition> findFirstByMachineryIdOrderByInspectionDateDescCreatedAtDesc(Long machineryId);

    boolean existsByMachineryId(Long machineryId);
}
