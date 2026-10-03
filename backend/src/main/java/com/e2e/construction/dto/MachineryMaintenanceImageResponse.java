package com.e2e.construction.dto;

import com.e2e.construction.entity.MachineryMaintenanceImage;

import java.time.LocalDateTime;

public class MachineryMaintenanceImageResponse {

    private Long id;
    private Long maintenanceId;
    private String fileName;
    private String storedFileName;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
    private LocalDateTime createdAt;

    public MachineryMaintenanceImageResponse() {
    }

    public static MachineryMaintenanceImageResponse fromEntity(MachineryMaintenanceImage image) {
        if (image == null) return null;
        MachineryMaintenanceImageResponse response = new MachineryMaintenanceImageResponse();
        response.setId(image.getId());
        if (image.getMaintenance() != null) {
            response.setMaintenanceId(image.getMaintenance().getId());
        }
        response.setFileName(image.getFileName());
        response.setStoredFileName(image.getStoredFileName());
        response.setFileUrl(image.getFileUrl());
        response.setFileSize(image.getFileSize());
        response.setContentType(image.getContentType());
        response.setCreatedAt(image.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(Long maintenanceId) {
        this.maintenanceId = maintenanceId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
