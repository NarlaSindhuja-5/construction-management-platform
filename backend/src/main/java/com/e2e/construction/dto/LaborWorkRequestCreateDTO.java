package com.e2e.construction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LaborWorkRequestCreateDTO {

    private Long laborerId;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private BigDecimal dailyWage;

    @NotBlank(message = "Job description is required")
    private String jobDescription;

    private String contractorNotes;

    public LaborWorkRequestCreateDTO() {
    }

    public LaborWorkRequestCreateDTO(Long laborerId, Long projectId, LocalDate startDate,
                                    LocalDate endDate, BigDecimal dailyWage,
                                    String jobDescription, String contractorNotes) {
        this.laborerId = laborerId;
        this.projectId = projectId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dailyWage = dailyWage;
        this.jobDescription = jobDescription;
        this.contractorNotes = contractorNotes;
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

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getContractorNotes() {
        return contractorNotes;
    }

    public void setContractorNotes(String contractorNotes) {
        this.contractorNotes = contractorNotes;
    }
}
