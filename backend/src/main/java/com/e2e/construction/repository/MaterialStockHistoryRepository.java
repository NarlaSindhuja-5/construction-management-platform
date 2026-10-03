package com.e2e.construction.repository;

import com.e2e.construction.entity.MaterialStockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialStockHistoryRepository extends JpaRepository<MaterialStockHistory, Long> {

    List<MaterialStockHistory> findByMaterialIdOrderByCreatedAtDesc(Long materialId);
}
