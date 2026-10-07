package com.e2e.construction.dto;

import com.e2e.construction.entity.TenderApplication;
import com.e2e.construction.entity.TenderApplicationStatus;
import com.e2e.construction.entity.TenderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TenderApplicationResponse {

    private Long id;

    // Tender details
    private Long tenderId;
    private String tenderNumber;
    private String tenderTitle;
    private String tenderDepartment;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tenderClosingDate;

    private TenderStatus tenderStatus;

    // Contractor details
    private Long contractorId;
    private String contractorCompanyName;
    private String contractorName;
    private String contractorEmail;
    private String contractorLicense;

    // Application details
    private BigDecimal bidAmount;
    private Integer proposedDurationDays;
    private String coverLetter;
    private TenderApplicationStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate submissionDate;

    private String reviewerRemarks;

    private List<TenderApplicationDocumentResponse> documents;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public TenderApplicationResponse() {
    }

    public static TenderApplicationResponse fromEntity(TenderApplication app) {
        if (app == null) {
            return null;
        }

        TenderApplicationResponse res = new TenderApplicationResponse();
        res.setId(app.getId());

        if (app.getTender() != null) {
            res.setTenderId(app.getTender().getId());
            res.setTenderNumber(app.getTender().getTenderNumber());
            res.setTenderTitle(app.getTender().getTitle());
            res.setTenderDepartment(app.getTender().getDepartment());
            res.setTenderClosingDate(app.getTender().getClosingDate());
            res.setTenderStatus(app.getTender().getStatus());
        }

        if (app.getContractor() != null) {
            res.setContractorId(app.getContractor().getId());
            res.setContractorCompanyName(app.getContractor().getCompanyName());
            res.setContractorLicense(app.getContractor().getLicenseNumber());
            if (app.getContractor().getUser() != null) {
                res.setContractorName(app.getContractor().getUser().getFirstName() + " " + app.getContractor().getUser().getLastName());
                res.setContractorEmail(app.getContractor().getUser().getEmail());
            }
        }

        res.setBidAmount(app.getBidAmount());
        res.setProposedDurationDays(app.getProposedDurationDays());
        res.setCoverLetter(app.getCoverLetter());
        res.setStatus(app.getStatus());
        res.setSubmissionDate(app.getSubmissionDate());
        res.setReviewerRemarks(app.getReviewerRemarks());

        if (app.getDocuments() != null) {
            res.setDocuments(app.getDocuments().stream()
                    .map(TenderApplicationDocumentResponse::fromEntity)
                    .collect(Collectors.toList()));
        } else {
            res.setDocuments(Collections.emptyList());
        }

        res.setCreatedAt(app.getCreatedAt());
        res.setUpdatedAt(app.getUpdatedAt());

        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenderId() {
        return tenderId;
    }

    public void setTenderId(Long tenderId) {
        this.tenderId = tenderId;
    }

    public String getTenderNumber() {
        return tenderNumber;
    }

    public void setTenderNumber(String tenderNumber) {
        this.tenderNumber = tenderNumber;
    }

    public String getTenderTitle() {
        return tenderTitle;
    }

    public void setTenderTitle(String tenderTitle) {
        this.tenderTitle = tenderTitle;
    }

    public String getTenderDepartment() {
        return tenderDepartment;
    }

    public void setTenderDepartment(String tenderDepartment) {
        this.tenderDepartment = tenderDepartment;
    }

    public LocalDate getTenderClosingDate() {
        return tenderClosingDate;
    }

    public void setTenderClosingDate(LocalDate tenderClosingDate) {
        this.tenderClosingDate = tenderClosingDate;
    }

    public TenderStatus getTenderStatus() {
        return tenderStatus;
    }

    public void setTenderStatus(TenderStatus tenderStatus) {
        this.tenderStatus = tenderStatus;
    }

    public Long getContractorId() {
        return contractorId;
    }

    public void setContractorId(Long contractorId) {
        this.contractorId = contractorId;
    }

    public String getContractorCompanyName() {
        return contractorCompanyName;
    }

    public void setContractorCompanyName(String contractorCompanyName) {
        this.contractorCompanyName = contractorCompanyName;
    }

    public String getContractorName() {
        return contractorName;
    }

    public void setContractorName(String contractorName) {
        this.contractorName = contractorName;
    }

    public String getContractorEmail() {
        return contractorEmail;
    }

    public void setContractorEmail(String contractorEmail) {
        this.contractorEmail = contractorEmail;
    }

    public String getContractorLicense() {
        return contractorLicense;
    }

    public void setContractorLicense(String contractorLicense) {
        this.contractorLicense = contractorLicense;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }

    public Integer getProposedDurationDays() {
        return proposedDurationDays;
    }

    public void setProposedDurationDays(Integer proposedDurationDays) {
        this.proposedDurationDays = proposedDurationDays;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public TenderApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(TenderApplicationStatus status) {
        this.status = status;
    }

    public LocalDate getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDate submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getReviewerRemarks() {
        return reviewerRemarks;
    }

    public void setReviewerRemarks(String reviewerRemarks) {
        this.reviewerRemarks = reviewerRemarks;
    }

    public List<TenderApplicationDocumentResponse> getDocuments() {
        return documents;
    }

    public void setDocuments(List<TenderApplicationDocumentResponse> documents) {
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
