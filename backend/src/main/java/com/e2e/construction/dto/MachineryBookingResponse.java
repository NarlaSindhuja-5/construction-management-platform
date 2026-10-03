package com.e2e.construction.dto;

import com.e2e.construction.entity.BookingStatus;
import com.e2e.construction.entity.MachineryBooking;
import com.e2e.construction.entity.MachineryCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MachineryBookingResponse {

    private Long id;
    private Long machineId;
    private String machineName;
    private MachineryCategory machineCategory;
    private String machineLocation;

    private Long contractorId;
    private String contractorName;
    private String contractorCompanyName;
    private String contractorEmail;
    private String contractorPhone;

    private Long ownerId;
    private String ownerName;
    private String ownerCompanyName;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private BigDecimal dailyPrice;
    private BigDecimal totalPrice;
    private BookingStatus status;

    private String projectName;
    private String deliveryLocation;
    private String remarks;
    private String ownerNotes;
    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MachineryBookingResponse() {
    }

    public static MachineryBookingResponse fromEntity(MachineryBooking booking) {
        if (booking == null) return null;

        MachineryBookingResponse response = new MachineryBookingResponse();
        response.setId(booking.getId());

        if (booking.getMachinery() != null) {
            response.setMachineId(booking.getMachinery().getId());
            response.setMachineName(booking.getMachinery().getName());
            response.setMachineCategory(booking.getMachinery().getCategory());
            response.setMachineLocation(booking.getMachinery().getLocation());

            if (booking.getMachinery().getOwner() != null) {
                response.setOwnerId(booking.getMachinery().getOwner().getId());
                response.setOwnerCompanyName(booking.getMachinery().getOwner().getCompanyName());
                if (booking.getMachinery().getOwner().getUser() != null) {
                    response.setOwnerName(booking.getMachinery().getOwner().getUser().getFirstName() + " " +
                            booking.getMachinery().getOwner().getUser().getLastName());
                }
            }
        }

        if (booking.getContractor() != null) {
            response.setContractorId(booking.getContractor().getId());
            response.setContractorCompanyName(booking.getContractor().getCompanyName());
            if (booking.getContractor().getUser() != null) {
                response.setContractorName(booking.getContractor().getUser().getFirstName() + " " +
                        booking.getContractor().getUser().getLastName());
                response.setContractorEmail(booking.getContractor().getUser().getEmail());
                response.setContractorPhone(booking.getContractor().getUser().getPhoneNumber());
            }
        }

        response.setStartDate(booking.getStartDate());
        response.setEndDate(booking.getEndDate());
        response.setTotalDays(booking.getTotalDays());
        response.setDailyPrice(booking.getDailyPrice());
        response.setTotalPrice(booking.getTotalPrice());
        response.setStatus(booking.getStatus());
        response.setProjectName(booking.getProjectName());
        response.setDeliveryLocation(booking.getDeliveryLocation());
        response.setRemarks(booking.getRemarks());
        response.setOwnerNotes(booking.getOwnerNotes());
        response.setRejectionReason(booking.getRejectionReason());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMachineId() {
        return machineId;
    }

    public void setMachineId(Long machineId) {
        this.machineId = machineId;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public MachineryCategory getMachineCategory() {
        return machineCategory;
    }

    public void setMachineCategory(MachineryCategory machineCategory) {
        this.machineCategory = machineCategory;
    }

    public String getMachineLocation() {
        return machineLocation;
    }

    public void setMachineLocation(String machineLocation) {
        this.machineLocation = machineLocation;
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

    public String getContractorEmail() {
        return contractorEmail;
    }

    public void setContractorEmail(String contractorEmail) {
        this.contractorEmail = contractorEmail;
    }

    public String getContractorPhone() {
        return contractorPhone;
    }

    public void setContractorPhone(String contractorPhone) {
        this.contractorPhone = contractorPhone;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerCompanyName() {
        return ownerCompanyName;
    }

    public void setOwnerCompanyName(String ownerCompanyName) {
        this.ownerCompanyName = ownerCompanyName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    public BigDecimal getDailyPrice() {
        return dailyPrice;
    }

    public void setDailyPrice(BigDecimal dailyPrice) {
        this.dailyPrice = dailyPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getOwnerNotes() {
        return ownerNotes;
    }

    public void setOwnerNotes(String ownerNotes) {
        this.ownerNotes = ownerNotes;
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
