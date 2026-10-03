package com.e2e.construction.dto;

public class MaterialOrderActionRequest {

    private String notes;
    private String reason;

    public MaterialOrderActionRequest() {
    }

    public MaterialOrderActionRequest(String notes, String reason) {
        this.notes = notes;
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
