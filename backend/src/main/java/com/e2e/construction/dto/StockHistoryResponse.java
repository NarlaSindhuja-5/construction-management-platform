package com.e2e.construction.dto;

import com.e2e.construction.entity.MaterialStockHistory;
import com.e2e.construction.entity.StockChangeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockHistoryResponse {

    private Long id;
    private Long materialId;
    private String materialName;
    private BigDecimal previousQuantity;
    private BigDecimal changeQuantity;
    private BigDecimal newQuantity;
    private StockChangeType changeType;
    private Long referenceId;
    private String notes;
    private String recordedBy;
    private LocalDateTime createdAt;

    public StockHistoryResponse() {
    }

    public static StockHistoryResponse fromEntity(MaterialStockHistory history) {
        if (history == null) return null;

        StockHistoryResponse response = new StockHistoryResponse();
        response.setId(history.getId());
        if (history.getMaterial() != null) {
            response.setMaterialId(history.getMaterial().getId());
            response.setMaterialName(history.getMaterial().getName());
        }
        response.setPreviousQuantity(history.getPreviousQuantity());
        response.setChangeQuantity(history.getChangeQuantity());
        response.setNewQuantity(history.getNewQuantity());
        response.setChangeType(history.getChangeType());
        response.setReferenceId(history.getReferenceId());
        response.setNotes(history.getNotes());
        response.setRecordedBy(history.getRecordedBy());
        response.setCreatedAt(history.getCreatedAt());

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getPreviousQuantity() {
        return previousQuantity;
    }

    public void setPreviousQuantity(BigDecimal previousQuantity) {
        this.previousQuantity = previousQuantity;
    }

    public BigDecimal getChangeQuantity() {
        return changeQuantity;
    }

    public void setChangeQuantity(BigDecimal changeQuantity) {
        this.changeQuantity = changeQuantity;
    }

    public BigDecimal getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(BigDecimal newQuantity) {
        this.newQuantity = newQuantity;
    }

    public StockChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(StockChangeType changeType) {
        this.changeType = changeType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
