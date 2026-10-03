package com.e2e.construction.dto;

import com.e2e.construction.entity.BookingStatus;
import com.e2e.construction.entity.LaborRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LaborRequestResponse {

    private Long id;

    // Contractor Info
    private Long contractorId;
    private String contractorName;
    private String contractorCompanyName;
    private String contractorPhone;
    private String contractorEmail;

    // Laborer Info
    private Long laborerId;
    private String laborerName;
    private String laborerPhone;
    private String laborerEmail;
    private String laborerSkills;
    private String laborerLocation;
    private Integer laborerExperience;
    private BigDecimal laborerDailyWage;

    // Project Info
    private Long projectId;
    private String projectName;
    private String projectLocation;
    private String projectType;

    // Request Details
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private BigDecimal dailyWage;
    private BigDecimal totalEstimatedCost;
    private String jobDescription;
    private BookingStatus status;
    private String contractorNotes;
    private String laborerNotes;
    private String rejectionReason;

    // Assignment Reference (if accepted)
    private Long assignmentId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LaborRequestResponse() {
    }

    public static LaborRequestResponse fromEntity(LaborRequest request) {
        return fromEntity(request, null);
    }

    public static LaborRequestResponse fromEntity(LaborRequest request, Long assignmentId) {
        if (request == null) return null;

        LaborRequestResponse response = new LaborRequestResponse();
        response.setId(request.getId());

        if (request.getContractor() != null) {
            response.setContractorId(request.getContractor().getId());
            response.setContractorCompanyName(request.getContractor().getCompanyName());
            if (request.getContractor().getUser() != null) {
                response.setContractorName(request.getContractor().getUser().getFirstName() + " " +
                        request.getContractor().getUser().getLastName());
                response.setContractorEmail(request.getContractor().getUser().getEmail());
                response.setContractorPhone(request.getContractor().getUser().getPhoneNumber());
            }
        }

        if (request.getLaborer() != null) {
            response.setLaborerId(request.getLaborer().getId());
            response.setLaborerSkills(request.getLaborer().getSkills());
            response.setLaborerLocation(request.getLaborer().getLocation());
            response.setLaborerExperience(request.getLaborer().getYearsOfExperience());
            response.setLaborerDailyWage(request.getLaborer().getDailyWage());
            if (request.getLaborer().getUser() != null) {
                response.setLaborerName(request.getLaborer().getUser().getFirstName() + " " +
                        request.getLaborer().getUser().getLastName());
                response.setLaborerEmail(request.getLaborer().getUser().getEmail());
                response.setLaborerPhone(request.getLaborer().getUser().getPhoneNumber());
            }
        }

        if (request.getProject() != null) {
            response.setProjectId(request.getProject().getId());
            response.setProjectName(request.getProject().getName());
            response.setProjectLocation(request.getProject().getLocation());
            if (request.getProject().getProjectType() != null) {
                response.setProjectType(request.getProject().getProjectType().name());
            }
        }

        response.setStartDate(request.getStartDate());
        response.setEndDate(request.getEndDate());
        response.setTotalDays(request.getTotalDays());
        response.setDailyWage(request.getDailyWage());
        response.setTotalEstimatedCost(request.getTotalEstimatedCost());
        response.setJobDescription(request.getJobDescription());
        response.setStatus(request.getStatus());
        response.setContractorNotes(request.getContractorNotes());
        response.setLaborerNotes(request.getLaborerNotes());
        response.setRejectionReason(request.getRejectionReason());
        response.setAssignmentId(assignmentId);
        response.setCreatedAt(request.getCreatedAt());
        response.setUpdatedAt(request.getUpdatedAt());

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContractorId() {
        return contractorId;
    }

    public void setContractorId(Long contractorId) {
        this.contractorId = contractorId;
    }

    public String getContractorName() {
        return contractorName;
    }

    public void setContractorName(String contractorName) {
        this.contractorName = contractorName;
    }

    public String getContractorCompanyName() {
        return contractorCompanyName;
    }

    public void setContractorCompanyName(String contractorCompanyName) {
        this.contractorCompanyName = contractorCompanyName;
    }

    public String getContractorPhone() {
        return contractorPhone;
    }

    public void setContractorPhone(String contractorPhone) {
        this.contractorPhone = contractorPhone;
    }

    public String getContractorEmail() {
        return contractorEmail;
    }

    public void setContractorEmail(String contractorEmail) {
        this.contractorEmail = contractorEmail;
    }

    public Long getLaborerId() {
        return laborerId;
    }

    public void setLaborerId(Long laborerId) {
        this.laborerId = laborerId;
    }

    public String getLaborerName() {
        return laborerName;
    }

    public void setLaborerName(String laborerName) {
        this.laborerName = laborerName;
    }

    public String getLaborerPhone() {
        return laborerPhone;
    }

    public void setLaborerPhone(String laborerPhone) {
        this.laborerPhone = laborerPhone;
    }

    public String getLaborerEmail() {
        return laborerEmail;
    }

    public void setLaborerEmail(String laborerEmail) {
        this.laborerEmail = laborerEmail;
    }

    public String getLaborerSkills() {
        return laborerSkills;
    }

    public void setLaborerSkills(String laborerSkills) {
        this.laborerSkills = laborerSkills;
    }

    public String getLaborerLocation() {
        return laborerLocation;
    }

    public void setLaborerLocation(String laborerLocation) {
        this.laborerLocation = laborerLocation;
    }

    public Integer getLaborerExperience() {
        return laborerExperience;
    }

    public void setLaborerExperience(Integer laborerExperience) {
        this.laborerExperience = laborerExperience;
    }

    public BigDecimal getLaborerDailyWage() {
        return laborerDailyWage;
    }

    public void setLaborerDailyWage(BigDecimal laborerDailyWage) {
        this.laborerDailyWage = laborerDailyWage;
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

    public String getProjectLocation() {
        return projectLocation;
    }

    public void setProjectLocation(String projectLocation) {
        this.projectLocation = projectLocation;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    public BigDecimal getDailyWage() {
        return dailyWage;
    }

    public void setDailyWage(BigDecimal dailyWage) {
        this.dailyWage = dailyWage;
    }

    public BigDecimal getTotalEstimatedCost() {
        return totalEstimatedCost;
    }

    public void setTotalEstimatedCost(BigDecimal totalEstimatedCost) {
        this.totalEstimatedCost = totalEstimatedCost;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getContractorNotes() {
        return contractorNotes;
    }

    public void setContractorNotes(String contractorNotes) {
        this.contractorNotes = contractorNotes;
    }

    public String getLaborerNotes() {
        return laborerNotes;
    }

    public void setLaborerNotes(String laborerNotes) {
        this.laborerNotes = laborerNotes;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
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
