package com.e2e.construction.dto;

import com.e2e.construction.entity.MachineryAvailability;
import com.e2e.construction.entity.MachineryCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class MachineryRequest {

    @NotBlank(message = "Machine name is required")
    @Size(max = 150, message = "Machine name cannot exceed 150 characters")
    private String name;

    @NotNull(message = "Category is required. Allowed: BUILDING, ROAD, BRIDGE, EARTHWORK, CONCRETE, LIFTING, TRANSPORT, OTHER")
    private MachineryCategory category;

    @NotBlank(message = "Manufacturer is required")
    @Size(max = 100, message = "Manufacturer cannot exceed 100 characters")
    private String manufacturer;

    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model cannot exceed 100 characters")
    private String model;

    @NotNull(message = "Manufacturing year is required")
    @Min(value = 1970, message = "Manufacturing year must be 1970 or later")
    @Max(value = 2050, message = "Manufacturing year is invalid")
    private Integer manufacturingYear;

    @NotBlank(message = "Capacity is required")
    @Size(max = 100, message = "Capacity cannot exceed 100 characters")
    private String capacity;

    @NotBlank(message = "Fuel type is required")
    @Size(max = 50, message = "Fuel type cannot exceed 50 characters")
    private String fuelType;

    @NotBlank(message = "Transmission is required")
    @Size(max = 50, message = "Transmission cannot exceed 50 characters")
    private String transmission;

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @NotNull(message = "Rental price per day is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rental price must be greater than zero")
    private BigDecimal rentalPricePerDay;

    @NotBlank(message = "Description is required")
    private String description;

    private MachineryAvailability availabilityStatus;

    public MachineryRequest() {
    }

    public MachineryRequest(String name, MachineryCategory category, String manufacturer, String model, Integer manufacturingYear, String capacity, String fuelType, String transmission, String location, BigDecimal rentalPricePerDay, String description, MachineryAvailability availabilityStatus) {
        this.name = name;
        this.category = category;
        this.manufacturer = manufacturer;
        this.model = model;
        this.manufacturingYear = manufacturingYear;
        this.capacity = capacity;
        this.fuelType = fuelType;
        this.transmission = transmission;
        this.location = location;
        this.rentalPricePerDay = rentalPricePerDay;
        this.description = description;
        this.availabilityStatus = availabilityStatus;
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
}
