package com.e2e.construction.dto;

import com.e2e.construction.entity.MachineryMaintenance;
import com.e2e.construction.entity.MaintenanceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MachineryMaintenanceResponse {

    private Long id;
    private Long machineId;
    private String machineName;
    private LocalDate maintenanceDate;
    private MaintenanceType maintenanceType;
    private String description;
    private BigDecimal cost;
    private String serviceProvider;
    private Double engineHours;
    private LocalDate nextServiceDate;
    private String remarks;
    private List<String> images = new ArrayList<>();
    private List<MachineryMaintenanceImageResponse> imageDetails = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MachineryMaintenanceResponse() {
    }

    public static MachineryMaintenanceResponse fromEntity(MachineryMaintenance maintenance) {
        if (maintenance == null) {
            return null;
        }

        MachineryMaintenanceResponse response = new MachineryMaintenanceResponse();
        response.setId(maintenance.getId());
        if (maintenance.getMachinery() != null) {
            response.setMachineId(maintenance.getMachinery().getId());
            response.setMachineName(maintenance.getMachinery().getName());
        }
        response.setMaintenanceDate(maintenance.getMaintenanceDate());
        response.setMaintenanceType(maintenance.getMaintenanceType());
        response.setDescription(maintenance.getDescription());
        response.setCost(maintenance.getCost());
        response.setServiceProvider(maintenance.getServiceProvider());
        response.setEngineHours(maintenance.getEngineHours());
        response.setNextServiceDate(maintenance.getNextServiceDate());
        response.setRemarks(maintenance.getRemarks());

        if (maintenance.getImages() != null && !maintenance.getImages().isEmpty()) {
            response.setImages(maintenance.getImages().stream()
                    .map(img -> img.getFileUrl())
                    .collect(Collectors.toList()));
            response.setImageDetails(maintenance.getImages().stream()
                    .map(MachineryMaintenanceImageResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        response.setCreatedAt(maintenance.getCreatedAt());
        response.setUpdatedAt(maintenance.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMachineId() {
        return machineId;
    }

    public void setMachineId(Long machineId) {
        this.machineId = machineId;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
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

    public List<MachineryMaintenanceImageResponse> getImageDetails() {
        return imageDetails;
    }

    public void setImageDetails(List<MachineryMaintenanceImageResponse> imageDetails) {
        this.imageDetails = imageDetails;
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
