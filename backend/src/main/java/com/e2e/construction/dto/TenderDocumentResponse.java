package com.e2e.construction.dto;

import com.e2e.construction.entity.TenderDocument;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class TenderDocumentResponse {

    private Long id;
    private Long tenderId;
    private String fileName;
    private String storedFileName;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
    private String documentType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public TenderDocumentResponse() {
    }

    public static TenderDocumentResponse fromEntity(TenderDocument doc) {
        if (doc == null) {
            return null;
        }
        TenderDocumentResponse res = new TenderDocumentResponse();
        res.setId(doc.getId());
        if (doc.getTender() != null) {
            res.setTenderId(doc.getTender().getId());
        }
        res.setFileName(doc.getFileName());
        res.setStoredFileName(doc.getStoredFileName());
        res.setFileUrl(doc.getFileUrl());
        res.setFileSize(doc.getFileSize());
        res.setContentType(doc.getContentType());
        res.setDocumentType(doc.getDocumentType());
        res.setCreatedAt(doc.getCreatedAt());
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenderId() {
        return tenderId;
    }

    public void setTenderId(Long tenderId) {
        this.tenderId = tenderId;
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

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
