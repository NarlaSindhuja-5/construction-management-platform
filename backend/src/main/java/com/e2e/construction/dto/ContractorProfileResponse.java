package com.e2e.construction.dto;

import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.VerificationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ContractorProfileResponse {

    private Long id;
    private Long userId;
    private String contractorName;
    private String companyName;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String companyDescription;
    private Integer yearsOfExperience;
    private String profileImage;
    private String licenseNumber;
    private String specialization;
    private VerificationStatus verificationStatus;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ContractorProfileResponse() {
    }

    public static ContractorProfileResponse fromEntity(Contractor contractor) {
        ContractorProfileResponse response = new ContractorProfileResponse();
        response.setId(contractor.getId());
        if (contractor.getUser() != null) {
            response.setUserId(contractor.getUser().getId());
            response.setContractorName(contractor.getUser().getFirstName() + " " + contractor.getUser().getLastName());
            response.setEmail(contractor.getUser().getEmail());
            response.setPhone(contractor.getUser().getPhoneNumber());
        }
        response.setCompanyName(contractor.getCompanyName());
        response.setCompanyDescription(contractor.getCompanyDescription());
        response.setAddress(contractor.getAddress());
        response.setCity(contractor.getCity());
        response.setState(contractor.getState());
        response.setPostalCode(contractor.getPostalCode());
        response.setYearsOfExperience(contractor.getYearsOfExperience());
        response.setProfileImage(contractor.getProfileImage());
        response.setLicenseNumber(contractor.getLicenseNumber());
        response.setSpecialization(contractor.getSpecialization());
        response.setVerificationStatus(contractor.getVerificationStatus());
        response.setRating(contractor.getRating());
        response.setCreatedAt(contractor.getCreatedAt());
        response.setUpdatedAt(contractor.getUpdatedAt());
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

    public String getContractorName() {
        return contractorName;
    }

    public void setContractorName(String contractorName) {
        this.contractorName = contractorName;
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

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
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
