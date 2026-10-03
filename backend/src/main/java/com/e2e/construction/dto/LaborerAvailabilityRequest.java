package com.e2e.construction.dto;

import com.e2e.construction.entity.LaborerAvailability;
import jakarta.validation.constraints.NotNull;

public class LaborerAvailabilityRequest {

    @NotNull(message = "Availability status is required. Allowed values: AVAILABLE, WORKING, UNAVAILABLE")
    private LaborerAvailability availabilityStatus;

    public LaborerAvailabilityRequest() {
    }

    public LaborerAvailabilityRequest(LaborerAvailability availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public LaborerAvailability getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(LaborerAvailability availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }
}
