package com.e2e.construction.dto;

import com.e2e.construction.entity.MachineryAvailability;

import java.util.ArrayList;
import java.util.List;

public class MachineProfileResponse {

    private MachineryResponse specifications;
    private List<MachineryImageResponse> images = new ArrayList<>();
    private MachineConditionResponse condition;
    private List<MachineryMaintenanceResponse> maintenanceHistory = new ArrayList<>();
    private MachineryAvailability availabilityStatus;
    private List<BookedDateRangeResponse> bookedDateRanges = new ArrayList<>();

    public MachineProfileResponse() {
    }

    public MachineProfileResponse(
            MachineryResponse specifications,
            List<MachineryImageResponse> images,
            MachineConditionResponse condition,
            List<MachineryMaintenanceResponse> maintenanceHistory,
            MachineryAvailability availabilityStatus,
            List<BookedDateRangeResponse> bookedDateRanges) {
        this.specifications = specifications;
        this.images = images != null ? images : new ArrayList<>();
        this.condition = condition;
        this.maintenanceHistory = maintenanceHistory != null ? maintenanceHistory : new ArrayList<>();
        this.availabilityStatus = availabilityStatus;
        this.bookedDateRanges = bookedDateRanges != null ? bookedDateRanges : new ArrayList<>();
    }

    public MachineryResponse getSpecifications() {
        return specifications;
    }

    public void setSpecifications(MachineryResponse specifications) {
        this.specifications = specifications;
    }

    public List<MachineryImageResponse> getImages() {
        return images;
    }

    public void setImages(List<MachineryImageResponse> images) {
        this.images = images;
    }

    public MachineConditionResponse getCondition() {
        return condition;
    }

    public void setCondition(MachineConditionResponse condition) {
        this.condition = condition;
    }

    public List<MachineryMaintenanceResponse> getMaintenanceHistory() {
        return maintenanceHistory;
    }

    public void setMaintenanceHistory(List<MachineryMaintenanceResponse> maintenanceHistory) {
        this.maintenanceHistory = maintenanceHistory;
    }

    public MachineryAvailability getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(MachineryAvailability availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public List<BookedDateRangeResponse> getBookedDateRanges() {
        return bookedDateRanges;
    }

    public void setBookedDateRanges(List<BookedDateRangeResponse> bookedDateRanges) {
        this.bookedDateRanges = bookedDateRanges;
    }
}
