package com.e2e.construction.dto;

import com.e2e.construction.entity.MachineryImage;
import com.e2e.construction.entity.MachineryImageType;

import java.time.LocalDateTime;

public class MachineryImageResponse {

    private Long id;
    private Long machineId;
    private String machineName;
    private MachineryImageType imageType;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
    private LocalDateTime uploadDate;

    public MachineryImageResponse() {
    }

    public static MachineryImageResponse fromEntity(MachineryImage image) {
        MachineryImageResponse response = new MachineryImageResponse();
        response.setId(image.getId());
        if (image.getMachinery() != null) {
            response.setMachineId(image.getMachinery().getId());
            response.setMachineName(image.getMachinery().getName());
        }
        response.setImageType(image.getImageType());
        response.setFileName(image.getFileName());
        response.setFileUrl(image.getFileUrl());
        response.setFileSize(image.getFileSize());
        response.setContentType(image.getContentType());
        response.setUploadDate(image.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMachineId() {
        return machineId;
    }

    public void setMachineId(Long machineId) {
        this.machineId = machineId;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public MachineryImageType getImageType() {
        return imageType;
    }

    public void setImageType(MachineryImageType imageType) {
        this.imageType = imageType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}
