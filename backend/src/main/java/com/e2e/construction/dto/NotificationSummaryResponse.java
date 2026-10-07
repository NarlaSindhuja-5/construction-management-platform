package com.e2e.construction.dto;

public class NotificationSummaryResponse {

    private long unreadCount;
    private long totalCount;

    public NotificationSummaryResponse() {
    }

    public NotificationSummaryResponse(long unreadCount, long totalCount) {
        this.unreadCount = unreadCount;
        this.totalCount = totalCount;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
