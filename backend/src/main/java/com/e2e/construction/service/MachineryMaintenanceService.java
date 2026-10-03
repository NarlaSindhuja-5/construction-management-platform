package com.e2e.construction.service;

import com.e2e.construction.dto.MachineryMaintenanceImageResponse;
import com.e2e.construction.dto.MachineryMaintenanceRequest;
import com.e2e.construction.dto.MachineryMaintenanceResponse;
import com.e2e.construction.entity.Machinery;
import com.e2e.construction.entity.MachineryMaintenance;
import com.e2e.construction.entity.MachineryMaintenanceImage;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.MachineryMaintenanceImageRepository;
import com.e2e.construction.repository.MachineryMaintenanceRepository;
import com.e2e.construction.repository.MachineryRepository;
import com.e2e.construction.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MachineryMaintenanceService {

    private final MachineryMaintenanceRepository maintenanceRepository;
    private final MachineryMaintenanceImageRepository maintenanceImageRepository;
    private final MachineryRepository machineryRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public MachineryMaintenanceService(
            MachineryMaintenanceRepository maintenanceRepository,
            MachineryMaintenanceImageRepository maintenanceImageRepository,
            MachineryRepository machineryRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService) {
        this.maintenanceRepository = maintenanceRepository;
        this.maintenanceImageRepository = maintenanceImageRepository;
        this.machineryRepository = machineryRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Add a new maintenance record for a machine.
     * Only the machine owner and authorized admin can add maintenance records.
     */
    @Transactional
    public MachineryMaintenanceResponse addMaintenanceRecord(Long machineId, String userEmail, MachineryMaintenanceRequest request) {
        Machinery machine = machineryRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machinery", "id", machineId));

        verifyOwnerOrAdmin(machine, userEmail);

        MachineryMaintenance maintenance = new MachineryMaintenance();
        maintenance.setMachinery(machine);
        applyRequestToEntity(maintenance, request);

        maintenance = maintenanceRepository.save(maintenance);

        // Process any image URLs/paths supplied in the request body
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (String imgPath : request.getImages()) {
                if (imgPath != null && !imgPath.trim().isEmpty()) {
                    MachineryMaintenanceImage image = new MachineryMaintenanceImage();
                    image.setMaintenance(maintenance);
                    image.setFileName(imgPath.trim());
                    image.setStoredFileName(imgPath.trim());
                    image.setFilePath(imgPath.trim());
                    image.setFileUrl(imgPath.trim());
                    maintenance.addImage(image);
                    maintenanceImageRepository.save(image);
                }
            }
        }

        return MachineryMaintenanceResponse.fromEntity(maintenance);
    }

    /**
     * Update an existing maintenance record.
     * Only the machine owner and authorized admin can modify maintenance records.
     */
    @Transactional
    public MachineryMaintenanceResponse updateMaintenanceRecord(Long maintenanceId, String userEmail, MachineryMaintenanceRequest request) {
        MachineryMaintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new ResourceNotFoundException("MachineryMaintenance", "id", maintenanceId));

        verifyOwnerOrAdmin(maintenance.getMachinery(), userEmail);

        applyRequestToEntity(maintenance, request);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (String imgPath : request.getImages()) {
                if (imgPath != null && !imgPath.trim().isEmpty()) {
                    boolean alreadyExists = maintenance.getImages().stream()
                            .anyMatch(existing -> existing.getFileUrl().equalsIgnoreCase(imgPath.trim()));
                    if (!alreadyExists) {
                        MachineryMaintenanceImage image = new MachineryMaintenanceImage();
                        image.setMaintenance(maintenance);
                        image.setFileName(imgPath.trim());
                        image.setStoredFileName(imgPath.trim());
                        image.setFilePath(imgPath.trim());
                        image.setFileUrl(imgPath.trim());
                        maintenance.addImage(image);
                        maintenanceImageRepository.save(image);
                    }
                }
            }
        }

        maintenance = maintenanceRepository.save(maintenance);
        return MachineryMaintenanceResponse.fromEntity(maintenance);
    }

    /**
     * View maintenance history in chronological order.
     * Default sort: descending (latest first). If direction is "asc", returns ascending (oldest first).
     */
    @Transactional(readOnly = true)
    public List<MachineryMaintenanceResponse> getMaintenanceHistory(Long machineId, String direction) {
        if (!machineryRepository.existsById(machineId)) {
            throw new ResourceNotFoundException("Machinery", "id", machineId);
        }

        List<MachineryMaintenance> records;
        if ("asc".equalsIgnoreCase(direction)) {
            records = maintenanceRepository.findByMachineryIdOrderByMaintenanceDateAscIdAsc(machineId);
        } else {
            records = maintenanceRepository.findByMachineryIdOrderByMaintenanceDateDescIdDesc(machineId);
        }

        return records.stream()
                .map(MachineryMaintenanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * View a specific maintenance record by its ID.
     */
    @Transactional(readOnly = true)
    public MachineryMaintenanceResponse getMaintenanceRecordById(Long maintenanceId) {
        MachineryMaintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new ResourceNotFoundException("MachineryMaintenance", "id", maintenanceId));
        return MachineryMaintenanceResponse.fromEntity(maintenance);
    }

    /**
     * Delete an incorrect maintenance record where authorized.
     * Only the machine owner and authorized admin can delete maintenance records.
     */
    @Transactional
    public void deleteMaintenanceRecord(Long maintenanceId, String userEmail) {
        MachineryMaintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new ResourceNotFoundException("MachineryMaintenance", "id", maintenanceId));

        verifyOwnerOrAdmin(maintenance.getMachinery(), userEmail);

        // Clean up stored image files from disk
        if (maintenance.getImages() != null) {
            for (MachineryMaintenanceImage img : maintenance.getImages()) {
                if (img.getStoredFileName() != null) {
                    fileStorageService.deleteFile(img.getStoredFileName());
                }
            }
        }

        maintenanceRepository.delete(maintenance);
    }

    /**
     * Upload a real maintenance image or photograph.
     * Only the machine owner and authorized admin can upload maintenance images.
     */
    @Transactional
    public MachineryMaintenanceImageResponse uploadMaintenanceImage(Long maintenanceId, String userEmail, MultipartFile file) {
        MachineryMaintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new ResourceNotFoundException("MachineryMaintenance", "id", maintenanceId));

        verifyOwnerOrAdmin(maintenance.getMachinery(), userEmail);

        FileStorageService.StoredFileInfo stored = fileStorageService.storeFile(file);
        String fileUrl = "/api/machinery/maintenance/images/" + stored.getStoredFileName();

        MachineryMaintenanceImage image = new MachineryMaintenanceImage(
                maintenance,
                stored.getOriginalFilename(),
                stored.getStoredFileName(),
                stored.getAbsolutePath(),
                fileUrl,
                stored.getFileSize(),
                stored.getContentType()
        );

        image = maintenanceImageRepository.save(image);
        maintenance.addImage(image);

        return MachineryMaintenanceImageResponse.fromEntity(image);
    }

    /**
     * Delete a single maintenance image.
     * Only the machine owner and authorized admin can delete maintenance images.
     */
    @Transactional
    public void deleteMaintenanceImage(Long imageId, String userEmail) {
        MachineryMaintenanceImage image = maintenanceImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("MachineryMaintenanceImage", "id", imageId));

        verifyOwnerOrAdmin(image.getMaintenance().getMachinery(), userEmail);

        fileStorageService.deleteFile(image.getStoredFileName());
        maintenanceImageRepository.delete(image);
    }

    /**
     * Load maintenance image resource for streaming / downloading.
     */
    public Resource getMaintenanceImageResource(String storedFileName) {
        return fileStorageService.loadFileAsResource(storedFileName);
    }

    /**
     * Verifies that the user is either the machine's owner or an administrator.
     */
    private void verifyOwnerOrAdmin(Machinery machine, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName());
        boolean isOwner = machine.getOwner() != null &&
                machine.getOwner().getUser() != null &&
                machine.getOwner().getUser().getEmail().equalsIgnoreCase(userEmail.trim());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Access denied: Only the machine owner and authorized administrators can modify maintenance records.");
        }
    }

    private void applyRequestToEntity(MachineryMaintenance maintenance, MachineryMaintenanceRequest request) {
        if (request.getMaintenanceDate() != null) maintenance.setMaintenanceDate(request.getMaintenanceDate());
        if (request.getMaintenanceType() != null) maintenance.setMaintenanceType(request.getMaintenanceType());
        if (request.getDescription() != null) maintenance.setDescription(request.getDescription().trim());
        if (request.getCost() != null) maintenance.setCost(request.getCost());
        if (request.getServiceProvider() != null) maintenance.setServiceProvider(request.getServiceProvider().trim());
        if (request.getEngineHours() != null) maintenance.setEngineHours(request.getEngineHours());
        if (request.getNextServiceDate() != null) maintenance.setNextServiceDate(request.getNextServiceDate());
        if (request.getRemarks() != null) maintenance.setRemarks(request.getRemarks().trim());
    }
}
