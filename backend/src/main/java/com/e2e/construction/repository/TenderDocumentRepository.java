package com.e2e.construction.repository;

import com.e2e.construction.entity.TenderDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenderDocumentRepository extends JpaRepository<TenderDocument, Long> {

    List<TenderDocument> findByTenderIdOrderByCreatedAtAsc(Long tenderId);
}
