package com.e2e.construction.service;

import com.e2e.construction.dto.MachineryImageResponse;
import com.e2e.construction.entity.Machinery;
import com.e2e.construction.entity.MachineryImage;
import com.e2e.construction.entity.MachineryImageType;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.MachineryImageRepository;
import com.e2e.construction.repository.MachineryRepository;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MachineryImageService {

    private final MachineryRepository machineryRepository;
    private final MachineryImageRepository machineryImageRepository;
    private final FileStorageService fileStorageService;

    public MachineryImageService(
            MachineryRepository machineryRepository,
            MachineryImageRepository machineryImageRepository,
            FileStorageService fileStorageService) {
        this.machineryRepository = machineryRepository;
        this.machineryImageRepository = machineryImageRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Upload an actual photograph for a machine. Only the owner of the machine can upload images.
     */
    @Transactional
    public MachineryImageResponse uploadImage(
            Long machineId,
            String userEmail,
            MultipartFile file,
            MachineryImageType imageType) {

        Machinery machine = machineryRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machinery", "id", machineId));

        // Strict ownership check
        if (!machine.getOwner().getUser().getEmail().equalsIgnoreCase(userEmail.trim())) {
            throw new AccessDeniedException("Access denied: You can only upload images for your own machinery.");
        }

        // Validate, sanitize, and store file on disk
        FileStorageService.StoredFileInfo storedFileInfo = fileStorageService.storeFile(file);

        MachineryImageType resolvedType = imageType != null ? imageType : MachineryImageType.OTHER;
        String fileUrl = "/api/machinery/images/" + storedFileInfo.getStoredFileName();

        MachineryImage image = new MachineryImage();
        image.setMachinery(machine);
        image.setImageType(resolvedType);
        image.setFileName(storedFileInfo.getOriginalFilename());
        image.setStoredFileName(storedFileInfo.getStoredFileName());
        image.setFilePath(storedFileInfo.getAbsolutePath());
        image.setFileUrl(fileUrl);
        image.setFileSize(storedFileInfo.getFileSize());
        image.setContentType(storedFileInfo.getContentType());

        image = machineryImageRepository.save(image);
        return MachineryImageResponse.fromEntity(image);
    }

    /**
     * Retrieve all image metadata records for a given machine.
     */
    @Transactional(readOnly = true)
    public List<MachineryImageResponse> getMachineImages(Long machineId) {
        if (!machineryRepository.existsById(machineId)) {
            throw new ResourceNotFoundException("Machinery", "id", machineId);
        }

        return machineryImageRepository.findByMachineryIdOrderByCreatedAtDesc(machineId)
                .stream()
                .map(MachineryImageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Load image resource for viewing or download.
     */
    public Resource getImageResource(String storedFileName) {
        return fileStorageService.loadFileAsResource(storedFileName);
    }

    /**
     * Delete an image. Only the owner of the machine can delete its images.
     */
    @Transactional
    public void deleteImage(Long imageId, String userEmail) {
        MachineryImage image = machineryImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("MachineryImage", "id", imageId));

        // Strict ownership check
        if (!image.getMachinery().getOwner().getUser().getEmail().equalsIgnoreCase(userEmail.trim())) {
            throw new AccessDeniedException("Access denied: You can only delete images for your own machinery.");
        }

        // Remove from filesystem
        fileStorageService.deleteFile(image.getStoredFileName());

        // Remove from database
        machineryImageRepository.delete(image);
    }
}
