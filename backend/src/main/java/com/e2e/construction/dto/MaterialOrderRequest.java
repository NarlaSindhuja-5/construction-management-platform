package com.e2e.construction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MaterialOrderRequest {

    private Long materialId;

    private Long projectId;

    @NotNull(message = "Order quantity is required")
    @DecimalMin(value = "0.01", message = "Order quantity must be greater than zero")
    private BigDecimal quantity;

    private String deliveryAddress;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate requestedDeliveryDate;

    private String contractorNotes;

    public MaterialOrderRequest() {
    }

    public MaterialOrderRequest(Long materialId, Long projectId, BigDecimal quantity,
                                String deliveryAddress, LocalDate requestedDeliveryDate,
                                String contractorNotes) {
        this.materialId = materialId;
        this.projectId = projectId;
        this.quantity = quantity;
        this.deliveryAddress = deliveryAddress;
        this.requestedDeliveryDate = requestedDeliveryDate;
        this.contractorNotes = contractorNotes;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
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

    public String getContractorNotes() {
        return contractorNotes;
    }

    public void setContractorNotes(String contractorNotes) {
        this.contractorNotes = contractorNotes;
    }
}
