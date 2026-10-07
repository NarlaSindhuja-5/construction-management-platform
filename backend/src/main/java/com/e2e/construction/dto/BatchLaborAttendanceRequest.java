package com.e2e.construction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class BatchLaborAttendanceRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Attendance date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotEmpty(message = "Attendance records list cannot be empty")
    @Valid
    private List<BatchLaborAttendanceItem> attendances;

    public BatchLaborAttendanceRequest() {
    }

    public BatchLaborAttendanceRequest(Long projectId, LocalDate date, List<BatchLaborAttendanceItem> attendances) {
        this.projectId = projectId;
        this.date = date;
        this.attendances = attendances;
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

    public List<BatchLaborAttendanceItem> getAttendances() {
        return attendances;
    }

    public void setAttendances(List<BatchLaborAttendanceItem> attendances) {
        this.attendances = attendances;
    }
}
