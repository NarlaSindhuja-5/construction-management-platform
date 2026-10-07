package com.e2e.construction.dto;

import com.e2e.construction.entity.Tender;
import com.e2e.construction.entity.TenderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TenderResponse {

    private Long id;
    private String tenderNumber;
    private String title;
    private String department;
    private String location;
    private String category;
    private BigDecimal estimatedValue;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishedDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate closingDate;

    private String eligibilityCriteria;
    private String description;
    private TenderStatus status;

    private Long createdById;
    private String createdByName;

    private int applicationCount;
    private boolean isExpired;

    private List<TenderDocumentResponse> documents;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public TenderResponse() {
    }

    public static TenderResponse fromEntity(Tender tender) {
        if (tender == null) {
            return null;
        }

        TenderResponse res = new TenderResponse();
        res.setId(tender.getId());
        res.setTenderNumber(tender.getTenderNumber());
        res.setTitle(tender.getTitle());
        res.setDepartment(tender.getDepartment());
        res.setLocation(tender.getLocation());
        res.setCategory(tender.getCategory());
        res.setEstimatedValue(tender.getEstimatedValue());
        res.setPublishedDate(tender.getPublishedDate());
        res.setClosingDate(tender.getClosingDate());
        res.setEligibilityCriteria(tender.getEligibilityCriteria());
        res.setDescription(tender.getDescription());
        res.setStatus(tender.getStatus());

        if (tender.getCreatedBy() != null) {
            res.setCreatedById(tender.getCreatedBy().getId());
            res.setCreatedByName(tender.getCreatedBy().getFirstName() + " " + tender.getCreatedBy().getLastName());
        }

        res.setApplicationCount(tender.getApplications() != null ? tender.getApplications().size() : 0);
        res.setExpired(LocalDate.now().isAfter(tender.getClosingDate()));

        if (tender.getDocuments() != null) {
            res.setDocuments(tender.getDocuments().stream()
                    .map(TenderDocumentResponse::fromEntity)
                    .collect(Collectors.toList()));
        } else {
            res.setDocuments(Collections.emptyList());
        }

        res.setCreatedAt(tender.getCreatedAt());
        res.setUpdatedAt(tender.getUpdatedAt());

        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public int getApplicationCount() {
        return applicationCount;
    }

    public void setApplicationCount(int applicationCount) {
        this.applicationCount = applicationCount;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public List<TenderDocumentResponse> getDocuments() {
        return documents;
    }

    public void setDocuments(List<TenderDocumentResponse> documents) {
        this.documents = documents;
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
