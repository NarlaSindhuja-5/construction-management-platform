package com.e2e.construction.dto;

import com.e2e.construction.entity.Material;
import com.e2e.construction.entity.MaterialAvailability;
import com.e2e.construction.entity.MaterialCategory;
import com.e2e.construction.entity.VerificationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MaterialResponse {

    private Long id;
    private String name;
    private MaterialCategory category;
    private String gradeSpecification;
    private String unit;
    private BigDecimal availableQuantity;
    private BigDecimal price;
    private String location;
    private String description;

    // Supplier Info
    private Long supplierId;
    private String supplierStoreName;
    private String supplierContactPhone;
    private String supplierEmail;
    private String supplierLocation;

    private MaterialAvailability availability;
    private VerificationStatus verificationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MaterialResponse() {
    }

    public static MaterialResponse fromEntity(Material material) {
        if (material == null) return null;

        MaterialResponse response = new MaterialResponse();
        response.setId(material.getId());
        response.setName(material.getName());
        response.setCategory(material.getCategory());
        response.setGradeSpecification(material.getGradeSpecification());
        response.setUnit(material.getUnit());
        response.setAvailableQuantity(material.getAvailableQuantity());
        response.setPrice(material.getPrice());
        response.setLocation(material.getLocation());
        response.setDescription(material.getDescription());
        response.setAvailability(material.getAvailability());
        response.setVerificationStatus(material.getVerificationStatus());
        response.setCreatedAt(material.getCreatedAt());
        response.setUpdatedAt(material.getUpdatedAt());

        if (material.getSupplier() != null) {
            response.setSupplierId(material.getSupplier().getId());
            response.setSupplierStoreName(material.getSupplier().getStoreName());
            response.setSupplierContactPhone(material.getSupplier().getContactPhone());
            response.setSupplierLocation(material.getSupplier().getCity() + ", " + material.getSupplier().getState());
            if (material.getSupplier().getUser() != null) {
                response.setSupplierEmail(material.getSupplier().getUser().getEmail());
            }
        }

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierStoreName() {
        return supplierStoreName;
    }

    public void setSupplierStoreName(String supplierStoreName) {
        this.supplierStoreName = supplierStoreName;
    }

    public String getSupplierContactPhone() {
        return supplierContactPhone;
    }

    public void setSupplierContactPhone(String supplierContactPhone) {
        this.supplierContactPhone = supplierContactPhone;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public void setSupplierEmail(String supplierEmail) {
        this.supplierEmail = supplierEmail;
    }

    public String getSupplierLocation() {
        return supplierLocation;
    }

    public void setSupplierLocation(String supplierLocation) {
        this.supplierLocation = supplierLocation;
    }

    public MaterialAvailability getAvailability() {
        return availability;
    }

    public void setAvailability(MaterialAvailability availability) {
        this.availability = availability;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
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
