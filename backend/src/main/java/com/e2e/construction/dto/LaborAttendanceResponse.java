package com.e2e.construction.dto;

import com.e2e.construction.entity.AttendanceStatus;
import com.e2e.construction.entity.LaborAttendance;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LaborAttendanceResponse {

    private Long id;
    private Long laborerId;
    private String laborerName;
    private String laborerPhoneNumber;
    private String laborerSkills;
    private BigDecimal laborerDailyWage;

    private Long projectId;
    private String projectName;
    private Long contractorId;
    private String contractorCompanyName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private AttendanceStatus status;
    private Double workingHours;
    private String remarks;

    private Long recordedById;
    private String recordedByName;
    private String recordedByRole;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public LaborAttendanceResponse() {
    }

    public static LaborAttendanceResponse fromEntity(LaborAttendance attendance) {
        if (attendance == null) {
            return null;
        }

        LaborAttendanceResponse response = new LaborAttendanceResponse();
        response.setId(attendance.getId());

        if (attendance.getLaborer() != null) {
            response.setLaborerId(attendance.getLaborer().getId());
            response.setLaborerSkills(attendance.getLaborer().getSkills());
            response.setLaborerDailyWage(attendance.getLaborer().getDailyWage());
            if (attendance.getLaborer().getUser() != null) {
                response.setLaborerName(attendance.getLaborer().getUser().getFirstName() + " " + attendance.getLaborer().getUser().getLastName());
                response.setLaborerPhoneNumber(attendance.getLaborer().getUser().getPhoneNumber());
            }
        }

        if (attendance.getProject() != null) {
            response.setProjectId(attendance.getProject().getId());
            response.setProjectName(attendance.getProject().getName());
            if (attendance.getProject().getContractor() != null) {
                response.setContractorId(attendance.getProject().getContractor().getId());
                response.setContractorCompanyName(attendance.getProject().getContractor().getCompanyName());
            }
        }

        response.setDate(attendance.getAttendanceDate());
        response.setStatus(attendance.getStatus());
        response.setWorkingHours(attendance.getWorkingHours());
        response.setRemarks(attendance.getRemarks());

        if (attendance.getRecordedBy() != null) {
            response.setRecordedById(attendance.getRecordedBy().getId());
            response.setRecordedByName(attendance.getRecordedBy().getFirstName() + " " + attendance.getRecordedBy().getLastName());
            if (attendance.getRecordedBy().getRole() != null) {
                response.setRecordedByRole(attendance.getRecordedBy().getRole().getName());
            }
        }

        response.setCreatedAt(attendance.getCreatedAt());
        response.setUpdatedAt(attendance.getUpdatedAt());

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getLaborerPhoneNumber() {
        return laborerPhoneNumber;
    }

    public void setLaborerPhoneNumber(String laborerPhoneNumber) {
        this.laborerPhoneNumber = laborerPhoneNumber;
    }

    public String getLaborerSkills() {
        return laborerSkills;
    }

    public void setLaborerSkills(String laborerSkills) {
        this.laborerSkills = laborerSkills;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public Double getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(Double workingHours) {
        this.workingHours = workingHours;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Long getRecordedById() {
        return recordedById;
    }

    public void setRecordedById(Long recordedById) {
        this.recordedById = recordedById;
    }

    public String getRecordedByName() {
        return recordedByName;
    }

    public void setRecordedByName(String recordedByName) {
        this.recordedByName = recordedByName;
    }

    public String getRecordedByRole() {
        return recordedByRole;
    }

    public void setRecordedByRole(String recordedByRole) {
        this.recordedByRole = recordedByRole;
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
