package com.e2e.construction.dto;

import com.e2e.construction.entity.DailyWorkReportImage;

import java.time.LocalDateTime;

public class DailyWorkReportImageResponse {

    private Long id;
    private Long reportId;
    private String fileName;
    private String storedFileName;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
    private String caption;
    private LocalDateTime createdAt;

    public DailyWorkReportImageResponse() {
    }

    public static DailyWorkReportImageResponse fromEntity(DailyWorkReportImage image) {
        if (image == null) {
            return null;
        }
        DailyWorkReportImageResponse response = new DailyWorkReportImageResponse();
        response.setId(image.getId());
        if (image.getReport() != null) {
            response.setReportId(image.getReport().getId());
        }
        response.setFileName(image.getFileName());
        response.setStoredFileName(image.getStoredFileName());
        response.setFileUrl(image.getFileUrl());
        response.setFileSize(image.getFileSize());
        response.setContentType(image.getContentType());
        response.setCaption(image.getCaption());
        response.setCreatedAt(image.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
