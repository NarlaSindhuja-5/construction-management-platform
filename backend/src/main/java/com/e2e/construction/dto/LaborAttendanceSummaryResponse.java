package com.e2e.construction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class LaborAttendanceSummaryResponse {

    private Long projectId;
    private String projectName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private int totalRecords;
    private int presentCount;
    private int absentCount;
    private int halfDayCount;
    private int overtimeCount;
    private double totalWorkingHours;

    public LaborAttendanceSummaryResponse() {
    }

    public LaborAttendanceSummaryResponse(Long projectId, String projectName, LocalDate date,
                                          LocalDate startDate, LocalDate endDate,
                                          int totalRecords, int presentCount, int absentCount,
                                          int halfDayCount, int overtimeCount, double totalWorkingHours) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.date = date;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalRecords = totalRecords;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.halfDayCount = halfDayCount;
        this.overtimeCount = overtimeCount;
        this.totalWorkingHours = totalWorkingHours;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(int presentCount) {
        this.presentCount = presentCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(int absentCount) {
        this.absentCount = absentCount;
    }

    public int getHalfDayCount() {
        return halfDayCount;
    }

    public void setHalfDayCount(int halfDayCount) {
        this.halfDayCount = halfDayCount;
    }

    public int getOvertimeCount() {
        return overtimeCount;
    }

    public void setOvertimeCount(int overtimeCount) {
        this.overtimeCount = overtimeCount;
    }

    public double getTotalWorkingHours() {
        return totalWorkingHours;
    }

    public void setTotalWorkingHours(double totalWorkingHours) {
        this.totalWorkingHours = totalWorkingHours;
    }
}
