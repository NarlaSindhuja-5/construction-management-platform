package com.e2e.construction.dto;

import com.e2e.construction.entity.LaborAssignment;
import com.e2e.construction.entity.LaborAssignmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LaborAssignmentResponse {

    private Long id;
    private Long requestId;

    // Laborer Info
    private Long laborerId;
    private String laborerName;
    private String laborerPhone;
    private String laborerEmail;
    private String laborerSkills;

    // Project Info
    private Long projectId;
    private String projectName;
    private String projectLocation;
    private String projectType;

    // Contractor Info
    private Long contractorId;
    private String contractorName;
    private String contractorCompanyName;
    private String contractorPhone;
    private String contractorEmail;

    // Assignment Details
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal dailyWage;
    private LaborAssignmentStatus status;
    private String notes;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;

    public LaborAssignmentResponse() {
    }

    public static LaborAssignmentResponse fromEntity(LaborAssignment assignment) {
        if (assignment == null) return null;

        LaborAssignmentResponse response = new LaborAssignmentResponse();
        response.setId(assignment.getId());

        if (assignment.getLaborRequest() != null) {
            response.setRequestId(assignment.getLaborRequest().getId());
        }

        if (assignment.getLaborer() != null) {
            response.setLaborerId(assignment.getLaborer().getId());
            response.setLaborerSkills(assignment.getLaborer().getSkills());
            if (assignment.getLaborer().getUser() != null) {
                response.setLaborerName(assignment.getLaborer().getUser().getFirstName() + " " +
                        assignment.getLaborer().getUser().getLastName());
                response.setLaborerPhone(assignment.getLaborer().getUser().getPhoneNumber());
                response.setLaborerEmail(assignment.getLaborer().getUser().getEmail());
            }
        }

        if (assignment.getProject() != null) {
            response.setProjectId(assignment.getProject().getId());
            response.setProjectName(assignment.getProject().getName());
            response.setProjectLocation(assignment.getProject().getLocation());
            if (assignment.getProject().getProjectType() != null) {
                response.setProjectType(assignment.getProject().getProjectType().name());
            }
        }

        if (assignment.getContractor() != null) {
            response.setContractorId(assignment.getContractor().getId());
            response.setContractorCompanyName(assignment.getContractor().getCompanyName());
            if (assignment.getContractor().getUser() != null) {
                response.setContractorName(assignment.getContractor().getUser().getFirstName() + " " +
                        assignment.getContractor().getUser().getLastName());
                response.setContractorPhone(assignment.getContractor().getUser().getPhoneNumber());
                response.setContractorEmail(assignment.getContractor().getUser().getEmail());
            }
        }

        response.setStartDate(assignment.getStartDate());
        response.setEndDate(assignment.getEndDate());
        response.setDailyWage(assignment.getDailyWage());
        response.setStatus(assignment.getStatus());
        response.setNotes(assignment.getNotes());
        response.setAssignedAt(assignment.getAssignedAt());
        response.setCompletedAt(assignment.getCompletedAt());

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
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

    public BigDecimal getDailyWage() {
        return dailyWage;
    }

    public void setDailyWage(BigDecimal dailyWage) {
        this.dailyWage = dailyWage;
    }

    public LaborAssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(LaborAssignmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
