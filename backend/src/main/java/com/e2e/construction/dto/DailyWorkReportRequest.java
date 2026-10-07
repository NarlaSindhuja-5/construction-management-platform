package com.e2e.construction.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DailyWorkReportRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Date is required")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotBlank(message = "Work description is required")
    private String workDescription;

    @NotNull(message = "Number of workers is required")
    @Min(value = 0, message = "Number of workers cannot be negative")
    private Integer numberOfWorkers;

    private String machineryUsed;

    private String materialsUsed;

    private String quantityCompleted;

    @DecimalMin(value = "0.0", message = "Working hours must be non-negative")
    private Double workingHours;

    @DecimalMin(value = "0.0", message = "Progress percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Progress percentage cannot exceed 100")
    private Double progressPercentage;

    @DecimalMin(value = "0.0", message = "Expenses must be non-negative")
    private BigDecimal expenses;

    @JsonAlias({"issuesDelays", "issues", "delays"})
    private String issuesOrDelays;

    private String remarks;

    private List<String> images;

    public DailyWorkReportRequest() {
    }

    public DailyWorkReportRequest(Long projectId, LocalDate date, String workDescription,
                                  Integer numberOfWorkers, String machineryUsed, String materialsUsed,
                                  String quantityCompleted, Double workingHours, Double progressPercentage,
                                  BigDecimal expenses, String issuesOrDelays, String remarks,
                                  List<String> images) {
        this.projectId = projectId;
        this.date = date;
        this.workDescription = workDescription;
        this.numberOfWorkers = numberOfWorkers;
        this.machineryUsed = machineryUsed;
        this.materialsUsed = materialsUsed;
        this.quantityCompleted = quantityCompleted;
        this.workingHours = workingHours;
        this.progressPercentage = progressPercentage;
        this.expenses = expenses;
        this.issuesOrDelays = issuesOrDelays;
        this.remarks = remarks;
        this.images = images;
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

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
