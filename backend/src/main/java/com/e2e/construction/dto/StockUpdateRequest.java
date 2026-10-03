package com.e2e.construction.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class StockUpdateRequest {

    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;

    private Boolean isDelta = false;

    private String notes;

    public StockUpdateRequest() {
    }

    public StockUpdateRequest(BigDecimal quantity, Boolean isDelta, String notes) {
        this.quantity = quantity;
        this.isDelta = isDelta != null ? isDelta : false;
        this.notes = notes;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Boolean getIsDelta() {
        return isDelta;
    }

    public void setIsDelta(Boolean isDelta) {
        this.isDelta = isDelta;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
