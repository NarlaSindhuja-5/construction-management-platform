package com.e2e.construction.dto;

import com.e2e.construction.entity.AttendanceStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class BatchLaborAttendanceItem {

    @NotNull(message = "Laborer ID is required")
    private Long laborerId;

    @NotNull(message = "Attendance status is required. Allowed values: PRESENT, ABSENT, HALF_DAY, OVERTIME")
    private AttendanceStatus status;

    @DecimalMin(value = "0.0", message = "Working hours must be non-negative")
    private Double workingHours;

    private String remarks;

    public BatchLaborAttendanceItem() {
    }

    public BatchLaborAttendanceItem(Long laborerId, AttendanceStatus status, Double workingHours, String remarks) {
        this.laborerId = laborerId;
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
