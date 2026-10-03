package com.e2e.construction.dto;

import com.e2e.construction.entity.ProjectStatus;
import com.e2e.construction.entity.ProjectType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 200, message = "Project name cannot exceed 200 characters")
    private String name;

    @NotNull(message = "Project type is required. Allowed values: BUILDING, ROAD, BRIDGE, INDUSTRIAL, RESIDENTIAL, COMMERCIAL, GOVERNMENT, OTHER")
    private ProjectType projectType;

    private String clientName;

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    private LocalDate startDate;

    private LocalDate expectedCompletionDate;

    @NotNull(message = "Budget is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than zero")
    private BigDecimal budget;

    @NotBlank(message = "Description is required")
    private String description;

    private ProjectStatus status;

    @Min(value = 0, message = "Progress percentage cannot be less than 0")
    @Max(value = 100, message = "Progress percentage cannot exceed 100")
    private Integer progressPercentage;

    public ProjectRequest() {
    }

    public ProjectRequest(String name, ProjectType projectType, String clientName, String location, String city, String state, LocalDate startDate, LocalDate expectedCompletionDate, BigDecimal budget, String description, ProjectStatus status, Integer progressPercentage) {
        this.name = name;
        this.projectType = projectType;
        this.clientName = clientName;
        this.location = location;
        this.city = city;
        this.state = state;
        this.startDate = startDate;
        this.expectedCompletionDate = expectedCompletionDate;
        this.budget = budget;
        this.description = description;
        this.status = status;
        this.progressPercentage = progressPercentage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProjectType getProjectType() {
        return projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getExpectedCompletionDate() {
        return expectedCompletionDate;
    }

    public void setExpectedCompletionDate(LocalDate expectedCompletionDate) {
        this.expectedCompletionDate = expectedCompletionDate;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
}
