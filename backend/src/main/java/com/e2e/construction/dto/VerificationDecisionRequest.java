package com.e2e.construction.dto;

import com.e2e.construction.entity.VerificationStatus;
import jakarta.validation.constraints.NotNull;

public class VerificationDecisionRequest {

    @NotNull(message = "Verification status is required (VERIFIED, REJECTED, PENDING)")
    private VerificationStatus status;

    private String rejectionReason;

    private String remarks;

    public VerificationDecisionRequest() {
    }

    public VerificationDecisionRequest(VerificationStatus status, String rejectionReason, String remarks) {
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.remarks = remarks;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
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
}
