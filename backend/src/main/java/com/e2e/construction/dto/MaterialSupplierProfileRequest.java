package com.e2e.construction.dto;

import com.e2e.construction.entity.MaterialSupplier;
import com.e2e.construction.entity.VerificationStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class MaterialSupplierProfileRequest {

    @NotBlank(message = "Store / Company name is required")
    private String storeName;

    private String gstOrTaxNumber;

    private String contactPhone;

    @NotBlank(message = "Warehouse address is required")
    private String warehouseAddress;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    private Boolean deliveryAvailable = true;

    private String description;

    public MaterialSupplierProfileRequest() {
    }

    public MaterialSupplierProfileRequest(String storeName, String gstOrTaxNumber, String contactPhone,
                                         String warehouseAddress, String city, String state,
                                         Boolean deliveryAvailable, String description) {
        this.storeName = storeName;
        this.gstOrTaxNumber = gstOrTaxNumber;
        this.contactPhone = contactPhone;
        this.warehouseAddress = warehouseAddress;
        this.city = city;
        this.state = state;
        this.deliveryAvailable = deliveryAvailable != null ? deliveryAvailable : true;
        this.description = description;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getGstOrTaxNumber() {
        return gstOrTaxNumber;
    }

    public void setGstOrTaxNumber(String gstOrTaxNumber) {
        this.gstOrTaxNumber = gstOrTaxNumber;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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
}
