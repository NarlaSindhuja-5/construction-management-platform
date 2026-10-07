package com.e2e.construction.dto;

import com.e2e.construction.entity.VerificationEntityType;
import com.e2e.construction.entity.VerificationRecord;
import com.e2e.construction.entity.VerificationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class VerificationRecordResponse {

    private Long id;
    private VerificationEntityType entityType;
    private Long entityId;
    private Long reviewerId;
    private String reviewerEmail;
    private String reviewerName;
    private VerificationStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verificationDate;

    private String rejectionReason;
    private String remarks;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public VerificationRecordResponse() {
    }

    public static VerificationRecordResponse fromEntity(VerificationRecord record) {
        if (record == null) {
            return null;
        }

        VerificationRecordResponse response = new VerificationRecordResponse();
        response.setId(record.getId());
        response.setEntityType(record.getEntityType());
        response.setEntityId(record.getEntityId());

        if (record.getReviewer() != null) {
            response.setReviewerId(record.getReviewer().getId());
            response.setReviewerEmail(record.getReviewer().getEmail());
            String fullName = "";
            if (record.getReviewer().getFirstName() != null) {
                fullName += record.getReviewer().getFirstName();
            }
            if (record.getReviewer().getLastName() != null) {
                fullName += (fullName.isEmpty() ? "" : " ") + record.getReviewer().getLastName();
            }
            response.setReviewerName(fullName.trim());
        }

        response.setStatus(record.getStatus());
        response.setVerificationDate(record.getVerificationDate());
        response.setRejectionReason(record.getRejectionReason());
        response.setRemarks(record.getRemarks());
        response.setCreatedAt(record.getCreatedAt());

        return response;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VerificationEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(VerificationEntityType entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerEmail() {
        return reviewerEmail;
    }

    public void setReviewerEmail(String reviewerEmail) {
        this.reviewerEmail = reviewerEmail;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
    }

    public LocalDateTime getVerificationDate() {
        return verificationDate;
    }

    public void setVerificationDate(LocalDateTime verificationDate) {
        this.verificationDate = verificationDate;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
