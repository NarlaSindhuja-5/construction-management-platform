package com.e2e.construction.dto;

import com.e2e.construction.entity.Laborer;
import com.e2e.construction.entity.LaborerAvailability;
import com.e2e.construction.entity.VerificationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LaborerProfileResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String profileImage;
    private String location;
    private String city;
    private String state;
    private String skills;
    private Integer yearsOfExperience;
    private BigDecimal dailyWage;
    private String description;
    private LaborerAvailability availabilityStatus;
    private VerificationStatus verificationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LaborerProfileResponse() {
    }

    public static LaborerProfileResponse fromEntity(Laborer laborer) {
        LaborerProfileResponse response = new LaborerProfileResponse();
        response.setId(laborer.getId());
        if (laborer.getUser() != null) {
            response.setUserId(laborer.getUser().getId());
            response.setFullName(laborer.getUser().getFirstName() + " " + laborer.getUser().getLastName());
            response.setEmail(laborer.getUser().getEmail());
            response.setPhone(laborer.getUser().getPhoneNumber());
        }
        response.setProfileImage(laborer.getProfileImage());
        response.setLocation(laborer.getLocation());
        response.setCity(laborer.getCity());
        response.setState(laborer.getState());
        response.setSkills(laborer.getSkills());
        response.setYearsOfExperience(laborer.getYearsOfExperience());
        response.setDailyWage(laborer.getDailyWage());
        response.setDescription(laborer.getDescription());
        response.setAvailabilityStatus(laborer.getAvailabilityStatus());
        response.setVerificationStatus(laborer.getVerificationStatus());
        response.setCreatedAt(laborer.getCreatedAt());
        response.setUpdatedAt(laborer.getUpdatedAt());
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public BigDecimal getDailyWage() {
        return dailyWage;
    }

    public void setDailyWage(BigDecimal dailyWage) {
        this.dailyWage = dailyWage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LaborerAvailability getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(LaborerAvailability availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
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
