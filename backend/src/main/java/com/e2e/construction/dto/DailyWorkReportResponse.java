package com.e2e.construction.dto;

import com.e2e.construction.entity.DailyWorkReport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DailyWorkReportResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private Long contractorId;
    private String contractorCompanyName;
    private Long createdById;
    private String createdByName;
    private String createdByRole;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String workDescription;
    private Integer numberOfWorkers;
    private String machineryUsed;
    private String materialsUsed;
    private String quantityCompleted;
    private Double workingHours;
    private Double progressPercentage;
    private BigDecimal expenses;
    private String issuesOrDelays;
    private String remarks;
    private List<DailyWorkReportImageResponse> images;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public DailyWorkReportResponse() {
    }

    public static DailyWorkReportResponse fromEntity(DailyWorkReport report) {
        if (report == null) {
            return null;
        }
        DailyWorkReportResponse res = new DailyWorkReportResponse();
        res.setId(report.getId());
        if (report.getProject() != null) {
            res.setProjectId(report.getProject().getId());
            res.setProjectName(report.getProject().getName());
            if (report.getProject().getContractor() != null) {
                res.setContractorId(report.getProject().getContractor().getId());
                res.setContractorCompanyName(report.getProject().getContractor().getCompanyName());
            }
        }
        if (report.getCreatedBy() != null) {
            res.setCreatedById(report.getCreatedBy().getId());
            res.setCreatedByName(report.getCreatedBy().getFirstName() + " " + report.getCreatedBy().getLastName());
            if (report.getCreatedBy().getRole() != null) {
                res.setCreatedByRole(report.getCreatedBy().getRole().getName());
            }
        }
        res.setDate(report.getReportDate());
        res.setWorkDescription(report.getWorkDescription());
        res.setNumberOfWorkers(report.getNumberOfWorkers());
        res.setMachineryUsed(report.getMachineryUsed());
        res.setMaterialsUsed(report.getMaterialsUsed());
        res.setQuantityCompleted(report.getQuantityCompleted());
        res.setWorkingHours(report.getWorkingHours());
        res.setProgressPercentage(report.getProgressPercentage());
        res.setExpenses(report.getExpenses());
        res.setIssuesOrDelays(report.getIssuesOrDelays());
        res.setRemarks(report.getRemarks());
        res.setCreatedAt(report.getCreatedAt());
        res.setUpdatedAt(report.getUpdatedAt());

        if (report.getImages() != null) {
            res.setImages(report.getImages().stream()
                    .map(DailyWorkReportImageResponse::fromEntity)
                    .collect(Collectors.toList()));
        } else {
            res.setImages(Collections.emptyList());
        }

        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getContractorId() {
        return contractorId;
    }

    public void setContractorId(Long contractorId) {
        this.contractorId = contractorId;
    }

    public String getContractorCompanyName() {
        return contractorCompanyName;
    }

    public void setContractorCompanyName(String contractorCompanyName) {
        this.contractorCompanyName = contractorCompanyName;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getCreatedByRole() {
        return createdByRole;
    }

    public void setCreatedByRole(String createdByRole) {
        this.createdByRole = createdByRole;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }

    public Integer getNumberOfWorkers() {
        return numberOfWorkers;
    }

    public void setNumberOfWorkers(Integer numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }

    public String getMachineryUsed() {
        return machineryUsed;
    }

    public void setMachineryUsed(String machineryUsed) {
        this.machineryUsed = machineryUsed;
    }

    public String getMaterialsUsed() {
        return materialsUsed;
    }

    public void setMaterialsUsed(String materialsUsed) {
        this.materialsUsed = materialsUsed;
    }

    public String getQuantityCompleted() {
        return quantityCompleted;
    }

    public void setQuantityCompleted(String quantityCompleted) {
        this.quantityCompleted = quantityCompleted;
    }

    public Double getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(Double workingHours) {
        this.workingHours = workingHours;
    }

    public Double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public BigDecimal getExpenses() {
        return expenses;
    }

    public void setExpenses(BigDecimal expenses) {
        this.expenses = expenses;
    }

    public String getIssuesOrDelays() {
        return issuesOrDelays;
    }

    public void setIssuesOrDelays(String issuesOrDelays) {
        this.issuesOrDelays = issuesOrDelays;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<DailyWorkReportImageResponse> getImages() {
        return images;
    }

    public void setImages(List<DailyWorkReportImageResponse> images) {
        this.images = images;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
