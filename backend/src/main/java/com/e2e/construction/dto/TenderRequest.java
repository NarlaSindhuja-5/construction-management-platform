package com.e2e.construction.dto;

import com.e2e.construction.entity.TenderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TenderRequest {

    @NotBlank(message = "Tender number is required")
    @Size(max = 100, message = "Tender number cannot exceed 100 characters")
    private String tenderNumber;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Department is required")
    @Size(max = 150, message = "Department cannot exceed 150 characters")
    private String department;

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @NotNull(message = "Estimated value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Estimated value must be greater than zero")
    private BigDecimal estimatedValue;

    @NotNull(message = "Published date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishedDate;

    @NotNull(message = "Closing date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate closingDate;

    @NotBlank(message = "Eligibility criteria is required")
    private String eligibilityCriteria;

    @NotBlank(message = "Description is required")
    private String description;

    private TenderStatus status;

    private List<String> documents;

    public TenderRequest() {
    }

    public TenderRequest(String tenderNumber, String title, String department, String location,
                         String category, BigDecimal estimatedValue, LocalDate publishedDate,
                         LocalDate closingDate, String eligibilityCriteria, String description,
                         TenderStatus status, List<String> documents) {
        this.tenderNumber = tenderNumber;
        this.title = title;
        this.department = department;
        this.location = location;
        this.category = category;
        this.estimatedValue = estimatedValue;
        this.publishedDate = publishedDate;
        this.closingDate = closingDate;
        this.eligibilityCriteria = eligibilityCriteria;
        this.description = description;
        this.status = status;
        this.documents = documents;
    }

    public String getTenderNumber() {
        return tenderNumber;
    }

    public void setTenderNumber(String tenderNumber) {
        this.tenderNumber = tenderNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(LocalDate closingDate) {
        this.closingDate = closingDate;
    }

    public String getEligibilityCriteria() {
        return eligibilityCriteria;
    }

    public void setEligibilityCriteria(String eligibilityCriteria) {
        this.eligibilityCriteria = eligibilityCriteria;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TenderStatus getStatus() {
        return status;
    }

    public void setStatus(TenderStatus status) {
        this.status = status;
    }

    public List<String> getDocuments() {
        return documents;
    }

    public void setDocuments(List<String> documents) {
        this.documents = documents;
    }
}
