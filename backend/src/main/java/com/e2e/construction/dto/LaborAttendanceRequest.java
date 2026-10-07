package com.e2e.construction.dto;

import com.e2e.construction.entity.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class LaborAttendanceRequest {

    @NotNull(message = "Laborer ID is required")
    private Long laborerId;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Attendance date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull(message = "Attendance status is required. Allowed values: PRESENT, ABSENT, HALF_DAY, OVERTIME")
    private AttendanceStatus status;

    @DecimalMin(value = "0.0", message = "Working hours must be non-negative")
    private Double workingHours;

    private String remarks;

    public LaborAttendanceRequest() {
    }

    public LaborAttendanceRequest(Long laborerId, Long projectId, LocalDate date,
                                  AttendanceStatus status, Double workingHours, String remarks) {
        this.laborerId = laborerId;
        this.projectId = projectId;
        this.date = date;
        this.status = status;
        this.workingHours = workingHours;
        this.remarks = remarks;
    }

    public Long getLaborerId() {
        return laborerId;
    }

    public void setLaborerId(Long laborerId) {
        this.laborerId = laborerId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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
}
