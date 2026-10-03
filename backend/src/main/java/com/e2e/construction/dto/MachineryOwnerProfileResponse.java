package com.e2e.construction.dto;

import com.e2e.construction.entity.MachineryOwner;
import com.e2e.construction.entity.VerificationStatus;

import java.time.LocalDateTime;

public class MachineryOwnerProfileResponse {

    private Long id;
    private Long userId;
    private String ownerName;
    private String companyName;
    private String phone;
    private String email;
    private String location;
    private String address;
    private String city;
    private String state;
    private String description;
    private String profileImage;
    private String taxId;
    private VerificationStatus verificationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MachineryOwnerProfileResponse() {
    }

    public static MachineryOwnerProfileResponse fromEntity(MachineryOwner owner) {
        MachineryOwnerProfileResponse response = new MachineryOwnerProfileResponse();
        response.setId(owner.getId());
        if (owner.getUser() != null) {
            response.setUserId(owner.getUser().getId());
            response.setOwnerName(owner.getUser().getFirstName() + " " + owner.getUser().getLastName());
            response.setEmail(owner.getUser().getEmail());
            response.setPhone(owner.getUser().getPhoneNumber());
        }
        response.setCompanyName(owner.getCompanyName());
        response.setTaxId(owner.getTaxId());
        response.setLocation(owner.getLocation());
        response.setAddress(owner.getAddress());
        response.setCity(owner.getCity());
        response.setState(owner.getState());
        response.setDescription(owner.getDescription());
        response.setProfileImage(owner.getProfileImage());
        response.setVerificationStatus(owner.getVerificationStatus());
        response.setCreatedAt(owner.getCreatedAt());
        response.setUpdatedAt(owner.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
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
