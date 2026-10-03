package com.e2e.construction.dto;

import com.e2e.construction.entity.MaterialCategory;
import com.e2e.construction.entity.MaterialOrder;
import com.e2e.construction.entity.MaterialOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MaterialOrderResponse {

    private Long id;
    private String orderNumber;

    // Material details
    private Long materialId;
    private String materialName;
    private MaterialCategory materialCategory;
    private String materialGrade;
    private String materialLocation;

    // Contractor details
    private Long contractorId;
    private String contractorName;
    private String contractorCompanyName;
    private String contractorPhone;
    private String contractorEmail;

    // Supplier details
    private Long supplierId;
    private String supplierStoreName;
    private String supplierContactPhone;
    private String supplierEmail;
    private String supplierLocation;

    // Project details (optional)
    private Long projectId;
    private String projectName;

    // Order metrics
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private LocalDate requestedDeliveryDate;
    private MaterialOrderStatus status;

    // Notes & Remarks
    private String contractorNotes;
    private String supplierNotes;
    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MaterialOrderResponse() {
    }

    public static MaterialOrderResponse fromEntity(MaterialOrder order) {
        if (order == null) return null;

        MaterialOrderResponse response = new MaterialOrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());

        if (order.getMaterial() != null) {
            response.setMaterialId(order.getMaterial().getId());
            response.setMaterialName(order.getMaterial().getName());
            response.setMaterialCategory(order.getMaterial().getCategory());
            response.setMaterialGrade(order.getMaterial().getGradeSpecification());
            response.setMaterialLocation(order.getMaterial().getLocation());
        }

        if (order.getContractor() != null) {
            response.setContractorId(order.getContractor().getId());
            response.setContractorCompanyName(order.getContractor().getCompanyName());
            if (order.getContractor().getUser() != null) {
                response.setContractorName(order.getContractor().getUser().getFirstName() + " " +
                        order.getContractor().getUser().getLastName());
                response.setContractorPhone(order.getContractor().getUser().getPhoneNumber());
                response.setContractorEmail(order.getContractor().getUser().getEmail());
            }
        }

        if (order.getSupplier() != null) {
            response.setSupplierId(order.getSupplier().getId());
            response.setSupplierStoreName(order.getSupplier().getStoreName());
            response.setSupplierContactPhone(order.getSupplier().getContactPhone());
            response.setSupplierLocation(order.getSupplier().getCity() + ", " + order.getSupplier().getState());
            if (order.getSupplier().getUser() != null) {
                response.setSupplierEmail(order.getSupplier().getUser().getEmail());
            }
        }

        if (order.getProject() != null) {
            response.setProjectId(order.getProject().getId());
            response.setProjectName(order.getProject().getName());
        }

        response.setQuantity(order.getQuantity());
        response.setUnit(order.getUnit());
        response.setUnitPrice(order.getUnitPrice());
        response.setTotalAmount(order.getTotalAmount());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setRequestedDeliveryDate(order.getRequestedDeliveryDate());
        response.setStatus(order.getStatus());
        response.setContractorNotes(order.getContractorNotes());
        response.setSupplierNotes(order.getSupplierNotes());
        response.setRejectionReason(order.getRejectionReason());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public MaterialCategory getMaterialCategory() {
        return materialCategory;
    }

    public void setMaterialCategory(MaterialCategory materialCategory) {
        this.materialCategory = materialCategory;
    }

    public String getMaterialGrade() {
        return materialGrade;
    }

    public void setMaterialGrade(String materialGrade) {
        this.materialGrade = materialGrade;
    }

    public String getMaterialLocation() {
        return materialLocation;
    }

    public void setMaterialLocation(String materialLocation) {
        this.materialLocation = materialLocation;
    }

    public Long getContractorId() {
        return contractorId;
    }

    public void setContractorId(Long contractorId) {
        this.contractorId = contractorId;
    }

    public String getContractorName() {
        return contractorName;
    }

    public void setContractorName(String contractorName) {
        this.contractorName = contractorName;
    }

    public String getContractorCompanyName() {
        return contractorCompanyName;
    }

    public void setContractorCompanyName(String contractorCompanyName) {
        this.contractorCompanyName = contractorCompanyName;
    }

    public String getContractorPhone() {
        return contractorPhone;
    }

    public void setContractorPhone(String contractorPhone) {
        this.contractorPhone = contractorPhone;
    }

    public String getContractorEmail() {
        return contractorEmail;
    }

    public void setContractorEmail(String contractorEmail) {
        this.contractorEmail = contractorEmail;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public LocalDate getRequestedDeliveryDate() {
        return requestedDeliveryDate;
    }

    public void setRequestedDeliveryDate(LocalDate requestedDeliveryDate) {
        this.requestedDeliveryDate = requestedDeliveryDate;
    }

    public MaterialOrderStatus getStatus() {
        return status;
    }

    public void setStatus(MaterialOrderStatus status) {
        this.status = status;
    }

    public String getContractorNotes() {
        return contractorNotes;
    }

    public void setContractorNotes(String contractorNotes) {
        this.contractorNotes = contractorNotes;
    }

    public String getSupplierNotes() {
        return supplierNotes;
    }

    public void setSupplierNotes(String supplierNotes) {
        this.supplierNotes = supplierNotes;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
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
