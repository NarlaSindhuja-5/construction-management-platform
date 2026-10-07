package com.e2e.construction.dto;

import com.e2e.construction.entity.TenderApplicationStatus;
import jakarta.validation.constraints.NotNull;

public class TenderApplicationReviewRequest {

    @NotNull(message = "Application status is required. Allowed values: SUBMITTED, UNDER_REVIEW, ACCEPTED, REJECTED, WITHDRAWN")
    private TenderApplicationStatus status;

    private String reviewerRemarks;

    public TenderApplicationReviewRequest() {
    }

    public TenderApplicationReviewRequest(TenderApplicationStatus status, String reviewerRemarks) {
        this.status = status;
        this.reviewerRemarks = reviewerRemarks;
    }

    public TenderApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(TenderApplicationStatus status) {
        this.status = status;
    }

    public String getReviewerRemarks() {
        return reviewerRemarks;
    }

    public void setReviewerRemarks(String reviewerRemarks) {
        this.reviewerRemarks = reviewerRemarks;
    }
}
