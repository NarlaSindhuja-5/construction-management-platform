package com.e2e.construction.dto;

import com.e2e.construction.entity.MaintenanceType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MachineryMaintenanceRequest {

    @NotNull(message = "Maintenance date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate maintenanceDate;

    @NotNull(message = "Maintenance type is required (ENGINE_SERVICE, OIL_CHANGE, BRAKE_SERVICE, TYRE_REPLACEMENT, HYDRAULIC_SERVICE, ELECTRICAL_SERVICE, GENERAL_SERVICE, OTHER)")
    private MaintenanceType maintenanceType;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Cost cannot be negative")
    private BigDecimal cost;

    @NotBlank(message = "Service provider is required")
    private String serviceProvider;

    private Double engineHours;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextServiceDate;

    private String remarks;

    private List<String> images;

    public MachineryMaintenanceRequest() {
    }

    public MachineryMaintenanceRequest(LocalDate maintenanceDate, MaintenanceType maintenanceType,
                                     String description, BigDecimal cost, String serviceProvider,
                                     Double engineHours, LocalDate nextServiceDate, String remarks,
                                     List<String> images) {
        this.maintenanceDate = maintenanceDate;
        this.maintenanceType = maintenanceType;
        this.description = description;
        this.cost = cost;
        this.serviceProvider = serviceProvider;
        this.engineHours = engineHours;
        this.nextServiceDate = nextServiceDate;
        this.remarks = remarks;
        this.images = images;
    }

    public LocalDate getMaintenanceDate() {
        return maintenanceDate;
    }

    public void setMaintenanceDate(LocalDate maintenanceDate) {
        this.maintenanceDate = maintenanceDate;
    }

    public MaintenanceType getMaintenanceType() {
        return maintenanceType;
    }

    public void setMaintenanceType(MaintenanceType maintenanceType) {
        this.maintenanceType = maintenanceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(String serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public Double getEngineHours() {
        return engineHours;
    }

    public void setEngineHours(Double engineHours) {
        this.engineHours = engineHours;
    }

    public LocalDate getNextServiceDate() {
        return nextServiceDate;
    }

    public void setNextServiceDate(LocalDate nextServiceDate) {
        this.nextServiceDate = nextServiceDate;
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
