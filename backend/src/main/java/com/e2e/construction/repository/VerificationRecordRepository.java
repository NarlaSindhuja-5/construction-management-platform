package com.e2e.construction.repository;

import com.e2e.construction.entity.VerificationEntityType;
import com.e2e.construction.entity.VerificationRecord;
import com.e2e.construction.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRecordRepository extends JpaRepository<VerificationRecord, Long> {

    List<VerificationRecord> findByEntityTypeAndEntityIdOrderByVerificationDateDesc(
            VerificationEntityType entityType, Long entityId);

    Optional<VerificationRecord> findFirstByEntityTypeAndEntityIdOrderByVerificationDateDesc(
            VerificationEntityType entityType, Long entityId);

    List<VerificationRecord> findByStatusOrderByVerificationDateDesc(VerificationStatus status);

    List<VerificationRecord> findByReviewerIdOrderByVerificationDateDesc(Long reviewerId);

    long countByStatus(VerificationStatus status);

    long countByEntityTypeAndStatus(VerificationEntityType entityType, VerificationStatus status);
}
