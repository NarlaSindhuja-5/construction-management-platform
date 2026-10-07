package com.e2e.construction.dto;

import java.util.HashMap;
import java.util.Map;

public class VerificationSummaryStatsResponse {

    private long pendingCount;
    private long verifiedCount;
    private long rejectedCount;
    private long totalCount;

    private Map<String, Long> pendingByEntityType = new HashMap<>();

    public VerificationSummaryStatsResponse() {
    }

    public VerificationSummaryStatsResponse(long pendingCount, long verifiedCount, long rejectedCount, long totalCount) {
        this.pendingCount = pendingCount;
        this.verifiedCount = verifiedCount;
        this.rejectedCount = rejectedCount;
        this.totalCount = totalCount;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public long getVerifiedCount() {
        return verifiedCount;
    }

    public void setVerifiedCount(long verifiedCount) {
        this.verifiedCount = verifiedCount;
    }

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public Map<String, Long> getPendingByEntityType() {
        return pendingByEntityType;
    }

    public void setPendingByEntityType(Map<String, Long> pendingByEntityType) {
        this.pendingByEntityType = pendingByEntityType != null ? pendingByEntityType : new HashMap<>();
    }
}
