package com.e2e.construction.dto;

import com.e2e.construction.entity.LaborerAvailability;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class LaborerProfileRequest {

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    private String city;

    private String state;

    @NotBlank(message = "Skills are required")
    @Size(max = 255, message = "Skills cannot exceed 255 characters")
    private String skills;

    @NotNull(message = "Years of experience is required")
    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsOfExperience;

    @NotNull(message = "Daily wage is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Daily wage must be greater than zero")
    private BigDecimal dailyWage;

    private String description;

    private String profileImage;

    private LaborerAvailability availabilityStatus;

    private String phone;

    public LaborerProfileRequest() {
    }

    public LaborerProfileRequest(String location, String city, String state, String skills, Integer yearsOfExperience, BigDecimal dailyWage, String description, String profileImage, LaborerAvailability availabilityStatus, String phone) {
        this.location = location;
        this.city = city;
        this.state = state;
        this.skills = skills;
        this.yearsOfExperience = yearsOfExperience;
        this.dailyWage = dailyWage;
        this.description = description;
        this.profileImage = profileImage;
        this.availabilityStatus = availabilityStatus;
        this.phone = phone;
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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public LaborerAvailability getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(LaborerAvailability availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
