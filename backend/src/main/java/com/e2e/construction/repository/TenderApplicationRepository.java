package com.e2e.construction.repository;

import com.e2e.construction.entity.TenderApplication;
import com.e2e.construction.entity.TenderApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenderApplicationRepository extends JpaRepository<TenderApplication, Long> {

    boolean existsByTenderIdAndContractorId(Long tenderId, Long contractorId);

    Optional<TenderApplication> findByTenderIdAndContractorId(Long tenderId, Long contractorId);

    List<TenderApplication> findByContractorIdOrderBySubmissionDateDesc(Long contractorId);

    List<TenderApplication> findByContractorIdAndStatusOrderBySubmissionDateDesc(Long contractorId, TenderApplicationStatus status);

    List<TenderApplication> findByTenderIdOrderBySubmissionDateDesc(Long tenderId);

    List<TenderApplication> findByTenderIdAndStatusOrderBySubmissionDateDesc(Long tenderId, TenderApplicationStatus status);
}
