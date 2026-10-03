package com.e2e.construction.dto;

public class LaborRequestActionDTO {

    private String notes;
    private String reason;

    public LaborRequestActionDTO() {
    }

    public LaborRequestActionDTO(String notes, String reason) {
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
