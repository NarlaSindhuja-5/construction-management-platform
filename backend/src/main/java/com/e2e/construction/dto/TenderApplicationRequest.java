package com.e2e.construction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class TenderApplicationRequest {

    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than zero")
    private BigDecimal bidAmount;

    @Min(value = 1, message = "Proposed duration must be at least 1 day")
    private Integer proposedDurationDays;

    private String coverLetter;

    private List<String> documents;

    public TenderApplicationRequest() {
    }

    public TenderApplicationRequest(BigDecimal bidAmount, Integer proposedDurationDays,
                                  String coverLetter, List<String> documents) {
        this.bidAmount = bidAmount;
        this.proposedDurationDays = proposedDurationDays;
        this.coverLetter = coverLetter;
        this.documents = documents;
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

    public List<String> getDocuments() {
        return documents;
    }

    public void setDocuments(List<String> documents) {
        this.documents = documents;
    }
}
