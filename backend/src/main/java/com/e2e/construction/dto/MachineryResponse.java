package com.e2e.construction.dto;

import com.e2e.construction.entity.Machinery;
import com.e2e.construction.entity.MachineryAvailability;
import com.e2e.construction.entity.MachineryCategory;
import com.e2e.construction.entity.VerificationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MachineryResponse {

    private Long id;
    private Long ownerId;
    private String ownerName;
    private String companyName;
    private String name;
    private MachineryCategory category;
    private String manufacturer;
    private String model;
    private Integer manufacturingYear;
    private String capacity;
    private String fuelType;
    private String transmission;
    private String location;
    private BigDecimal rentalPricePerDay;
    private String description;
    private MachineryAvailability availabilityStatus;
    private VerificationStatus verificationStatus;
    private MachineConditionResponse condition;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MachineryResponse() {
    }

    public static MachineryResponse fromEntity(Machinery machinery) {
        return fromEntity(machinery, null);
    }

    public static MachineryResponse fromEntity(Machinery machinery, MachineConditionResponse condition) {
        MachineryResponse response = new MachineryResponse();
        response.setId(machinery.getId());
        if (machinery.getOwner() != null) {
            response.setOwnerId(machinery.getOwner().getId());
            response.setCompanyName(machinery.getOwner().getCompanyName());
            if (machinery.getOwner().getUser() != null) {
                response.setOwnerName(machinery.getOwner().getUser().getFirstName() + " " + machinery.getOwner().getUser().getLastName());
            }
        }
        response.setName(machinery.getName());
        response.setCategory(machinery.getCategory());
        response.setManufacturer(machinery.getManufacturer());
        response.setModel(machinery.getModel());
        response.setManufacturingYear(machinery.getManufacturingYear());
        response.setCapacity(machinery.getCapacity());
        response.setFuelType(machinery.getFuelType());
        response.setTransmission(machinery.getTransmission());
        response.setLocation(machinery.getLocation());
        response.setRentalPricePerDay(machinery.getRentalPricePerDay());
        response.setDescription(machinery.getDescription());
        response.setAvailabilityStatus(machinery.getAvailabilityStatus());
        response.setVerificationStatus(machinery.getVerificationStatus());
        response.setCondition(condition);
        response.setCreatedAt(machinery.getCreatedAt());
        response.setUpdatedAt(machinery.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MachineryCategory getCategory() {
        return category;
    }

    public void setCategory(MachineryCategory category) {
        this.category = category;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getManufacturingYear() {
        return manufacturingYear;
    }

    public void setManufacturingYear(Integer manufacturingYear) {
        this.manufacturingYear = manufacturingYear;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getRentalPricePerDay() {
        return rentalPricePerDay;
    }

    public void setRentalPricePerDay(BigDecimal rentalPricePerDay) {
        this.rentalPricePerDay = rentalPricePerDay;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MachineryAvailability getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(MachineryAvailability availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public MachineConditionResponse getCondition() {
        return condition;
    }

    public void setCondition(MachineConditionResponse condition) {
        this.condition = condition;
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
