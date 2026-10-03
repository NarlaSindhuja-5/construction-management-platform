package com.e2e.construction.dto;

import com.e2e.construction.entity.MaterialSupplier;
import com.e2e.construction.entity.VerificationStatus;

import java.time.LocalDateTime;

public class MaterialSupplierProfileResponse {

    private Long id;
    private Long userId;
    private String storeName;
    private String ownerName;
    private String email;
    private String contactPhone;
    private String gstOrTaxNumber;
    private String warehouseAddress;
    private String city;
    private String state;
    private Boolean deliveryAvailable;
    private String description;
    private VerificationStatus verificationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MaterialSupplierProfileResponse() {
    }

    public static MaterialSupplierProfileResponse fromEntity(MaterialSupplier supplier) {
        if (supplier == null) return null;

        MaterialSupplierProfileResponse response = new MaterialSupplierProfileResponse();
        response.setId(supplier.getId());
        response.setStoreName(supplier.getStoreName());
        response.setGstOrTaxNumber(supplier.getGstOrTaxNumber());
        response.setContactPhone(supplier.getContactPhone());
        response.setWarehouseAddress(supplier.getWarehouseAddress());
        response.setCity(supplier.getCity());
        response.setState(supplier.getState());
        response.setDeliveryAvailable(supplier.getDeliveryAvailable());
        response.setDescription(supplier.getDescription());
        response.setVerificationStatus(supplier.getVerificationStatus());
        response.setCreatedAt(supplier.getCreatedAt());
        response.setUpdatedAt(supplier.getUpdatedAt());

        if (supplier.getUser() != null) {
            response.setUserId(supplier.getUser().getId());
            response.setOwnerName(supplier.getUser().getFirstName() + " " + supplier.getUser().getLastName());
            response.setEmail(supplier.getUser().getEmail());
            if (response.getContactPhone() == null || response.getContactPhone().isBlank()) {
                response.setContactPhone(supplier.getUser().getPhoneNumber());
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getGstOrTaxNumber() {
        return gstOrTaxNumber;
    }

    public void setGstOrTaxNumber(String gstOrTaxNumber) {
        this.gstOrTaxNumber = gstOrTaxNumber;
    }

    public String getWarehouseAddress() {
        return warehouseAddress;
    }

    public void setWarehouseAddress(String warehouseAddress) {
        this.warehouseAddress = warehouseAddress;
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

    public Boolean getDeliveryAvailable() {
        return deliveryAvailable;
    }

    public void setDeliveryAvailable(Boolean deliveryAvailable) {
        this.deliveryAvailable = deliveryAvailable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
