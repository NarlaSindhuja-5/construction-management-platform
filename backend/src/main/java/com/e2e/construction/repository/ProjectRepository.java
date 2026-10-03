package com.e2e.construction.repository;

import com.e2e.construction.entity.Project;
import com.e2e.construction.entity.ProjectStatus;
import com.e2e.construction.entity.ProjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByContractorIdOrderByCreatedAtDesc(Long contractorId);

    @Query("SELECT p FROM Project p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:projectType IS NULL OR p.projectType = :projectType) AND " +
            "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(p.clientName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(p.city) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(p.location) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY p.createdAt DESC")
    List<Project> searchAndFilterProjects(
            @Param("status") ProjectStatus status,
            @Param("projectType") ProjectType projectType,
            @Param("search") String search);

    @Query("SELECT p FROM Project p WHERE p.contractor.id = :contractorId AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:projectType IS NULL OR p.projectType = :projectType) AND " +
            "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(p.clientName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(p.city) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(p.location) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY p.createdAt DESC")
    List<Project> searchAndFilterContractorProjects(
            @Param("contractorId") Long contractorId,
            @Param("status") ProjectStatus status,
            @Param("projectType") ProjectType projectType,
            @Param("search") String search);
}
