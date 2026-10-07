package com.e2e.construction.repository;

import com.e2e.construction.entity.DailyWorkReportImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyWorkReportImageRepository extends JpaRepository<DailyWorkReportImage, Long> {

    List<DailyWorkReportImage> findByReportIdOrderByCreatedAtDesc(Long reportId);
}
