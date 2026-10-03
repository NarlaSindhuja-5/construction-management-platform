package com.e2e.construction.dto;

import com.e2e.construction.entity.MaterialCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class MaterialRequest {

    @NotBlank(message = "Material name is required")
    private String name;

    @NotNull(message = "Material category is required")
    private MaterialCategory category;

    private String gradeSpecification;

    @NotBlank(message = "Unit of measurement is required")
    private String unit;

    @NotNull(message = "Available quantity is required")
    @DecimalMin(value = "0.00", message = "Available quantity cannot be negative")
    private BigDecimal availableQuantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    private BigDecimal price;

    @NotBlank(message = "Location is required")
    private String location;

    private String description;

    public MaterialRequest() {
    }

    public MaterialRequest(String name, MaterialCategory category, String gradeSpecification,
                           String unit, BigDecimal availableQuantity, BigDecimal price,
                           String location, String description) {
        this.name = name;
        this.category = category;
        this.gradeSpecification = gradeSpecification;
        this.unit = unit;
        this.availableQuantity = availableQuantity;
        this.price = price;
        this.location = location;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MaterialCategory getCategory() {
        return category;
    }

    public void setCategory(MaterialCategory category) {
        this.category = category;
    }

    public String getGradeSpecification() {
        return gradeSpecification;
    }

    public void setGradeSpecification(String gradeSpecification) {
        this.gradeSpecification = gradeSpecification;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(BigDecimal availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
