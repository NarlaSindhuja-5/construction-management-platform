package com.e2e.construction.repository;

import com.e2e.construction.entity.TenderApplicationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenderApplicationDocumentRepository extends JpaRepository<TenderApplicationDocument, Long> {

    List<TenderApplicationDocument> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);
}
