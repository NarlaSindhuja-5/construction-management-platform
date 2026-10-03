package com.e2e.construction.repository;

import com.e2e.construction.entity.MaterialOrder;
import com.e2e.construction.entity.MaterialOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialOrderRepository extends JpaRepository<MaterialOrder, Long> {

    Optional<MaterialOrder> findByOrderNumber(String orderNumber);

    List<MaterialOrder> findByContractorIdOrderByCreatedAtDesc(Long contractorId);

    List<MaterialOrder> findByContractorIdAndStatusOrderByCreatedAtDesc(Long contractorId, MaterialOrderStatus status);

    List<MaterialOrder> findBySupplierIdOrderByCreatedAtDesc(Long supplierId);

    List<MaterialOrder> findBySupplierIdAndStatusOrderByCreatedAtDesc(Long supplierId, MaterialOrderStatus status);

    List<MaterialOrder> findByMaterialIdOrderByCreatedAtDesc(Long materialId);
}
