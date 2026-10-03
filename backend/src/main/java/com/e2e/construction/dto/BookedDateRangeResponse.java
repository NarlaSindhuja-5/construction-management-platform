package com.e2e.construction.dto;

import com.e2e.construction.entity.BookingStatus;

import java.time.LocalDate;

public class BookedDateRangeResponse {

    private Long bookingId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BookingStatus status;

    public BookedDateRangeResponse() {
    }

    public BookedDateRangeResponse(Long bookingId, LocalDate startDate, LocalDate endDate, BookingStatus status) {
        this.bookingId = bookingId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
