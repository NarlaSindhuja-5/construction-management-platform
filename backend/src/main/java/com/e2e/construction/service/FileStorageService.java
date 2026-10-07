package com.e2e.construction.service;

import com.e2e.construction.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/zip",
            "application/x-zip-compressed",
            "text/plain"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".webp",
            ".pdf",
            ".doc",
            ".docx",
            ".zip",
            ".txt"
    );

    // 10 MB maximum file size limit
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir:uploads/machinery}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the upload directory at " + this.fileStorageLocation, ex);
        }
    }

    /**
     * Validates and securely saves an uploaded image file to disk.
     */
    public StoredFileInfo storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        // 1. Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum permitted limit of 10MB (actual: " + (file.getSize() / (1024 * 1024)) + "MB)");
        }

        // 2. Validate MIME content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Invalid file format: '" + contentType + "'. Only JPEG, JPG, PNG, and WEBP images are allowed.");
        }

        // 3. Validate and sanitize filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg");
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Invalid path sequence in filename: " + originalFilename);
        }

        String extension = "";
        int extIndex = originalFilename.lastIndexOf('.');
        if (extIndex > 0) {
            extension = originalFilename.substring(extIndex).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Invalid file extension: '" + extension + "'. Allowed: " + ALLOWED_EXTENSIONS);
        }

        // 4. Generate secure unique filename on disk
        String storedFileName = UUID.randomUUID().toString() + extension;
        Path targetLocation = this.fileStorageLocation.resolve(storedFileName).normalize();

        // Prevent path traversal outside upload dir
        if (!targetLocation.startsWith(this.fileStorageLocation)) {
            throw new BadRequestException("Cannot store file outside directory: " + storedFileName);
        }

        // 5. Copy file to target location
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file " + originalFilename + ". Please try again.", ex);
        }

        return new StoredFileInfo(
                originalFilename,
                storedFileName,
                targetLocation.toString(),
                file.getSize(),
                contentType
        );
    }

    /**
     * Loads a file from disk as a Resource for streaming/downloading.
     */
    public Resource loadFileAsResource(String storedFileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(storedFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BadRequestException("File not found or not readable: " + storedFileName);
            }
        } catch (MalformedURLException ex) {
            throw new BadRequestException("File not found: " + storedFileName, ex);
        }
    }

    /**
     * Deletes a file from disk.
     */
    public boolean deleteFile(String storedFileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(storedFileName).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            return false;
        }
    }

    public static class StoredFileInfo {
        private final String originalFilename;
        private final String storedFileName;
        private final String absolutePath;
        private final long fileSize;
        private final String contentType;

        public StoredFileInfo(String originalFilename, String storedFileName, String absolutePath, long fileSize, String contentType) {
            this.originalFilename = originalFilename;
            this.storedFileName = storedFileName;
            this.absolutePath = absolutePath;
            this.fileSize = fileSize;
            this.contentType = contentType;
        }

        public String getOriginalFilename() {
            return originalFilename;
        }

        public String getStoredFileName() {
            return storedFileName;
        }

        public String getAbsolutePath() {
            return absolutePath;
        }

        public long getFileSize() {
            return fileSize;
        }

        public String getContentType() {
            return contentType;
        }
    }
}
