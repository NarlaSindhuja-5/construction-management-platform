package com.e2e.construction.service;

import com.e2e.construction.dto.VerificationDecisionRequest;
import com.e2e.construction.dto.VerificationItemResponse;
import com.e2e.construction.dto.VerificationRecordResponse;
import com.e2e.construction.dto.VerificationSummaryStatsResponse;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.Laborer;
import com.e2e.construction.entity.Machinery;
import com.e2e.construction.entity.MachineryImage;
import com.e2e.construction.entity.MachineryOwner;
import com.e2e.construction.entity.Material;
import com.e2e.construction.entity.MaterialSupplier;
import com.e2e.construction.entity.NotificationType;
import com.e2e.construction.entity.User;
import com.e2e.construction.entity.VerificationEntityType;
import com.e2e.construction.entity.VerificationRecord;
import com.e2e.construction.entity.VerificationStatus;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.LaborerRepository;
import com.e2e.construction.repository.MachineryImageRepository;
import com.e2e.construction.repository.MachineryOwnerRepository;
import com.e2e.construction.repository.MachineryRepository;
import com.e2e.construction.repository.MaterialRepository;
import com.e2e.construction.repository.MaterialSupplierRepository;
import com.e2e.construction.repository.UserRepository;
import com.e2e.construction.repository.VerificationRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminVerificationService {

    private static final Logger log = LoggerFactory.getLogger(AdminVerificationService.class);

    private final VerificationRecordRepository verificationRecordRepository;
    private final ContractorRepository contractorRepository;
    private final LaborerRepository laborerRepository;
    private final MachineryOwnerRepository machineryOwnerRepository;
    private final MachineryRepository machineryRepository;
    private final MachineryImageRepository machineryImageRepository;
    private final MaterialSupplierRepository materialSupplierRepository;
    private final MaterialRepository materialRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public AdminVerificationService(
            VerificationRecordRepository verificationRecordRepository,
            ContractorRepository contractorRepository,
            LaborerRepository laborerRepository,
            MachineryOwnerRepository machineryOwnerRepository,
            MachineryRepository machineryRepository,
            MachineryImageRepository machineryImageRepository,
            MaterialSupplierRepository materialSupplierRepository,
            MaterialRepository materialRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.verificationRecordRepository = verificationRecordRepository;
        this.contractorRepository = contractorRepository;
        this.laborerRepository = laborerRepository;
        this.machineryOwnerRepository = machineryOwnerRepository;
        this.machineryRepository = machineryRepository;
        this.machineryImageRepository = machineryImageRepository;
        this.materialSupplierRepository = materialSupplierRepository;
        this.materialRepository = materialRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Retrieve items across the 6 entity types for admin review, with optional filters for entity type, status, and search query.
     */
    @Transactional(readOnly = true)
    public List<VerificationItemResponse> getItemsForVerification(
            VerificationEntityType entityType,
            VerificationStatus status,
            String search) {

        List<VerificationItemResponse> results = new ArrayList<>();

        if (entityType == null || entityType == VerificationEntityType.CONTRACTOR) {
            results.addAll(getContractors(status));
        }
        if (entityType == null || entityType == VerificationEntityType.LABORER) {
            results.addAll(getLaborers(status));
        }
        if (entityType == null || entityType == VerificationEntityType.MACHINERY_OWNER) {
            results.addAll(getMachineryOwners(status));
        }
        if (entityType == null || entityType == VerificationEntityType.MACHINERY) {
            results.addAll(getMachineryList(status));
        }
        if (entityType == null || entityType == VerificationEntityType.MATERIAL_SUPPLIER) {
            results.addAll(getMaterialSuppliers(status));
        }
        if (entityType == null || entityType == VerificationEntityType.MATERIAL) {
            results.addAll(getMaterials(status));
        }

        // Apply keyword search filter if provided
        if (search != null && !search.trim().isEmpty()) {
            String query = search.trim().toLowerCase();
            results = results.stream().filter(item -> {
                boolean matchTitle = item.getTitle() != null && item.getTitle().toLowerCase().contains(query);
                boolean matchSub = item.getSubtitle() != null && item.getSubtitle().toLowerCase().contains(query);
                boolean matchEmail = item.getUserEmail() != null && item.getUserEmail().toLowerCase().contains(query);
                boolean matchLoc = item.getLocation() != null && item.getLocation().toLowerCase().contains(query);
                boolean matchCity = item.getCity() != null && item.getCity().toLowerCase().contains(query);
                return matchTitle || matchSub || matchEmail || matchLoc || matchCity;
            }).collect(Collectors.toList());
        }

        // Sort: PENDING status first, then by createdAt descending
        results.sort((a, b) -> {
            if (a.getVerificationStatus() == VerificationStatus.PENDING && b.getVerificationStatus() != VerificationStatus.PENDING) {
                return -1;
            }
            if (a.getVerificationStatus() != VerificationStatus.PENDING && b.getVerificationStatus() == VerificationStatus.PENDING) {
                return 1;
            }
            if (a.getCreatedAt() != null && b.getCreatedAt() != null) {
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            }
            return 0;
        });

        return results;
    }

    /**
     * Retrieve a specific item with uploaded images, full metadata, and latest verification audit for admin review.
     */
    @Transactional(readOnly = true)
    public VerificationItemResponse getItemForVerification(VerificationEntityType entityType, Long entityId) {
        if (entityType == null || entityId == null) {
            throw new BadRequestException("Entity type and ID are required.");
        }

        return switch (entityType) {
            case CONTRACTOR -> {
                Contractor c = contractorRepository.findById(entityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Contractor not found with ID: " + entityId));
                yield mapContractor(c);
            }
            case LABORER -> {
                Laborer l = laborerRepository.findById(entityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Laborer not found with ID: " + entityId));
                yield mapLaborer(l);
            }
            case MACHINERY_OWNER -> {
                MachineryOwner o = machineryOwnerRepository.findById(entityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Machinery Owner not found with ID: " + entityId));
                yield mapMachineryOwner(o);
            }
            case MACHINERY -> {
                Machinery m = machineryRepository.findById(entityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Machinery not found with ID: " + entityId));
                yield mapMachinery(m);
            }
            case MATERIAL_SUPPLIER -> {
                MaterialSupplier s = materialSupplierRepository.findById(entityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Material Supplier not found with ID: " + entityId));
                yield mapMaterialSupplier(s);
            }
            case MATERIAL -> {
                Material mat = materialRepository.findById(entityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Material not found with ID: " + entityId));
                yield mapMaterial(mat);
            }
        };
    }

    /**
     * Submit verification decision (VERIFY, REJECT, KEEP PENDING).
     * Records reviewer, verification date, status, reason for rejection.
     * Crucially: Does NOT delete rejected records automatically.
     */
    @Transactional
    public VerificationItemResponse submitVerificationDecision(
            VerificationEntityType entityType,
            Long entityId,
            String adminEmail,
            VerificationDecisionRequest request) {

        if (entityType == null || entityId == null) {
            throw new BadRequestException("Entity type and ID are required.");
        }
        if (request.getStatus() == null) {
            throw new BadRequestException("Status is required (VERIFIED, REJECTED, PENDING).");
        }

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found with email: " + adminEmail));

        boolean isAdmin = admin.getRole() != null && "ADMIN".equalsIgnoreCase(admin.getRole().getName());
        if (!isAdmin) {
            throw new AccessDeniedException("Access denied: Only administrators can review and verify accounts or listings.");
        }

        String rejectionReason = request.getRejectionReason();
        if (request.getStatus() == VerificationStatus.REJECTED && (rejectionReason == null || rejectionReason.isBlank())) {
            rejectionReason = "Verification criteria not met.";
        }

        User targetUser;
        String entityTitle;

        switch (entityType) {
            case CONTRACTOR -> {
                Contractor c = contractorRepository.findById(entityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Contractor not found with ID: " + entityId));
                c.setVerificationStatus(request.getStatus());
                contractorRepository.save(c);
                targetUser = c.getUser();
                entityTitle = c.getCompanyName();
            }
            case LABORER -> {
                Laborer l = laborerRepository.findById(entityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Laborer not found with ID: " + entityId));
                l.setVerificationStatus(request.getStatus());
                laborerRepository.save(l);
                targetUser = l.getUser();
                entityTitle = targetUser != null ? targetUser.getFirstName() + " " + targetUser.getLastName() : "Laborer #" + l.getId();
            }
            case MACHINERY_OWNER -> {
                MachineryOwner o = machineryOwnerRepository.findById(entityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Machinery Owner not found with ID: " + entityId));
                o.setVerificationStatus(request.getStatus());
                machineryOwnerRepository.save(o);
                targetUser = o.getUser();
                entityTitle = o.getCompanyName();
            }
            case MACHINERY -> {
                Machinery m = machineryRepository.findById(entityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Machinery not found with ID: " + entityId));
                m.setVerificationStatus(request.getStatus());
                machineryRepository.save(m);
                targetUser = m.getOwner() != null ? m.getOwner().getUser() : null;
                entityTitle = m.getName();
            }
            case MATERIAL_SUPPLIER -> {
                MaterialSupplier s = materialSupplierRepository.findById(entityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Material Supplier not found with ID: " + entityId));
                s.setVerificationStatus(request.getStatus());
                materialSupplierRepository.save(s);
                targetUser = s.getUser();
                entityTitle = s.getStoreName();
            }
            case MATERIAL -> {
                Material mat = materialRepository.findById(entityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Material not found with ID: " + entityId));
                mat.setVerificationStatus(request.getStatus());
                materialRepository.save(mat);
                targetUser = mat.getSupplier() != null ? mat.getSupplier().getUser() : null;
                entityTitle = mat.getName();
            }
            default -> throw new BadRequestException("Unsupported entity type: " + entityType);
        }

        // Record verification audit entry
        LocalDateTime verificationDate = LocalDateTime.now();
        VerificationRecord record = new VerificationRecord(
                entityType,
                entityId,
                admin,
                request.getStatus(),
                verificationDate,
                request.getStatus() == VerificationStatus.REJECTED ? rejectionReason : null,
                request.getRemarks()
        );
        verificationRecordRepository.save(record);
        log.info("Admin {} set verification status of {} (ID: {}) to {}",
                adminEmail, entityType, entityId, request.getStatus());

        // Dispatch notification to user
        if (targetUser != null && notificationService != null) {
            try {
                String typeName = entityType.name().replace('_', ' ').toLowerCase();
                if (request.getStatus() == VerificationStatus.VERIFIED) {
                    notificationService.notifyAdminVerification(targetUser, typeName, true, null);
                } else if (request.getStatus() == VerificationStatus.REJECTED) {
                    notificationService.notifyAdminVerification(targetUser, typeName, false, rejectionReason);
                } else {
                    notificationService.sendNotification(
                            targetUser,
                            "Verification Under Review",
                            String.format("Your %s '%s' verification status is currently set to PENDING pending additional review.", typeName, entityTitle),
                            NotificationType.ADMIN_VERIFICATION,
                            entityId,
                            entityType.name()
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to dispatch verification notification: {}", e.getMessage());
            }
        }

        return getItemForVerification(entityType, entityId);
    }

    /**
     * Retrieve complete audit history of verification decisions for a specific entity.
     */
    @Transactional(readOnly = true)
    public List<VerificationRecordResponse> getVerificationHistory(VerificationEntityType entityType, Long entityId) {
        List<VerificationRecord> records = verificationRecordRepository
                .findByEntityTypeAndEntityIdOrderByVerificationDateDesc(entityType, entityId);

        if (records == null) {
            return Collections.emptyList();
        }

        return records.stream()
                .map(VerificationRecordResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get verification dashboard statistics (counts for pending, verified, rejected, and per-entity breakdown).
     */
    @Transactional(readOnly = true)
    public VerificationSummaryStatsResponse getVerificationStats() {
        long pending = 0;
        long verified = 0;
        long rejected = 0;

        Map<String, Long> pendingByEntity = new HashMap<>();

        for (VerificationEntityType type : VerificationEntityType.values()) {
            long typePending = switch (type) {
                case CONTRACTOR -> contractorRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING).size();
                case LABORER -> laborerRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING).size();
                case MACHINERY_OWNER -> machineryOwnerRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING).size();
                case MACHINERY -> machineryRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING).size();
                case MATERIAL_SUPPLIER -> materialSupplierRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING).size();
                case MATERIAL -> materialRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING).size();
            };

            long typeVerified = switch (type) {
                case CONTRACTOR -> contractorRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.VERIFIED).size();
                case LABORER -> laborerRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.VERIFIED).size();
                case MACHINERY_OWNER -> machineryOwnerRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.VERIFIED).size();
                case MACHINERY -> machineryRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.VERIFIED).size();
                case MATERIAL_SUPPLIER -> materialSupplierRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.VERIFIED).size();
                case MATERIAL -> materialRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.VERIFIED).size();
            };

            long typeRejected = switch (type) {
                case CONTRACTOR -> contractorRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.REJECTED).size();
                case LABORER -> laborerRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.REJECTED).size();
                case MACHINERY_OWNER -> machineryOwnerRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.REJECTED).size();
                case MACHINERY -> machineryRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.REJECTED).size();
                case MATERIAL_SUPPLIER -> materialSupplierRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.REJECTED).size();
                case MATERIAL -> materialRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.REJECTED).size();
            };

            pending += typePending;
            verified += typeVerified;
            rejected += typeRejected;
            pendingByEntity.put(type.name(), typePending);
        }

        long total = pending + verified + rejected;
        VerificationSummaryStatsResponse stats = new VerificationSummaryStatsResponse(pending, verified, rejected, total);
        stats.setPendingByEntityType(pendingByEntity);
        return stats;
    }

    // =========================================================================
    // Private Helpers: Entity to VerificationItemResponse Mapping
    // =========================================================================

    private List<VerificationItemResponse> getContractors(VerificationStatus status) {
        List<Contractor> list = (status != null)
                ? contractorRepository.findByVerificationStatusOrderByCreatedAtDesc(status)
                : contractorRepository.findAllByOrderByCreatedAtDesc();
        return list.stream().map(this::mapContractor).collect(Collectors.toList());
    }

    private VerificationItemResponse mapContractor(Contractor c) {
        VerificationItemResponse item = new VerificationItemResponse();
        item.setEntityId(c.getId());
        item.setEntityType(VerificationEntityType.CONTRACTOR);
        item.setTitle(c.getCompanyName());
        item.setSubtitle(c.getSpecialization() != null ? c.getSpecialization() : "General Contractor");
        item.setVerificationStatus(c.getVerificationStatus());

        if (c.getUser() != null) {
            item.setUserId(c.getUser().getId());
            item.setUserEmail(c.getUser().getEmail());
            item.setUserName(c.getUser().getFirstName() + " " + c.getUser().getLastName());
            item.setPhoneNumber(c.getUser().getPhoneNumber());
        }

        item.setAddress(c.getAddress());
        item.setCity(c.getCity());
        item.setState(c.getState());
        item.setDescription(c.getCompanyDescription());

        if (c.getProfileImage() != null && !c.getProfileImage().isBlank()) {
            item.getImages().add(c.getProfileImage());
        }

        Map<String, Object> details = new HashMap<>();
        details.put("licenseNumber", c.getLicenseNumber());
        details.put("yearsOfExperience", c.getYearsOfExperience());
        details.put("rating", c.getRating());
        details.put("postalCode", c.getPostalCode());
        item.setDetails(details);

        attachLatestVerification(item, VerificationEntityType.CONTRACTOR, c.getId());
        item.setCreatedAt(c.getCreatedAt());
        item.setUpdatedAt(c.getUpdatedAt());
        return item;
    }

    private List<VerificationItemResponse> getLaborers(VerificationStatus status) {
        List<Laborer> list = (status != null)
                ? laborerRepository.findByVerificationStatusOrderByCreatedAtDesc(status)
                : laborerRepository.findAllByOrderByCreatedAtDesc();
        return list.stream().map(this::mapLaborer).collect(Collectors.toList());
    }

    private VerificationItemResponse mapLaborer(Laborer l) {
        VerificationItemResponse item = new VerificationItemResponse();
        item.setEntityId(l.getId());
        item.setEntityType(VerificationEntityType.LABORER);

        if (l.getUser() != null) {
            item.setUserId(l.getUser().getId());
            item.setUserEmail(l.getUser().getEmail());
            item.setUserName(l.getUser().getFirstName() + " " + l.getUser().getLastName());
            item.setTitle(item.getUserName());
            item.setPhoneNumber(l.getUser().getPhoneNumber());
        } else {
            item.setTitle("Laborer #" + l.getId());
        }

        item.setSubtitle(l.getSkills());
        item.setVerificationStatus(l.getVerificationStatus());
        item.setLocation(l.getLocation());
        item.setCity(l.getCity());
        item.setState(l.getState());
        item.setDescription(l.getDescription());

        if (l.getProfileImage() != null && !l.getProfileImage().isBlank()) {
            item.getImages().add(l.getProfileImage());
        }

        Map<String, Object> details = new HashMap<>();
        details.put("skills", l.getSkills());
        details.put("yearsOfExperience", l.getYearsOfExperience());
        details.put("dailyWage", l.getDailyWage());
        details.put("availabilityStatus", l.getAvailabilityStatus());
        item.setDetails(details);

        attachLatestVerification(item, VerificationEntityType.LABORER, l.getId());
        item.setCreatedAt(l.getCreatedAt());
        item.setUpdatedAt(l.getUpdatedAt());
        return item;
    }

    private List<VerificationItemResponse> getMachineryOwners(VerificationStatus status) {
        List<MachineryOwner> list = (status != null)
                ? machineryOwnerRepository.findByVerificationStatusOrderByCreatedAtDesc(status)
                : machineryOwnerRepository.findAllByOrderByCreatedAtDesc();
        return list.stream().map(this::mapMachineryOwner).collect(Collectors.toList());
    }

    private VerificationItemResponse mapMachineryOwner(MachineryOwner o) {
        VerificationItemResponse item = new VerificationItemResponse();
        item.setEntityId(o.getId());
        item.setEntityType(VerificationEntityType.MACHINERY_OWNER);
        item.setTitle(o.getCompanyName());
        item.setSubtitle(o.getTaxId() != null ? "Tax ID: " + o.getTaxId() : "Machinery Fleet Owner");
        item.setVerificationStatus(o.getVerificationStatus());

        if (o.getUser() != null) {
            item.setUserId(o.getUser().getId());
            item.setUserEmail(o.getUser().getEmail());
            item.setUserName(o.getUser().getFirstName() + " " + o.getUser().getLastName());
            item.setPhoneNumber(o.getUser().getPhoneNumber());
        }

        item.setLocation(o.getLocation());
        item.setAddress(o.getAddress());
        item.setCity(o.getCity());
        item.setState(o.getState());
        item.setDescription(o.getDescription());

        if (o.getProfileImage() != null && !o.getProfileImage().isBlank()) {
            item.getImages().add(o.getProfileImage());
        }

        Map<String, Object> details = new HashMap<>();
        details.put("taxId", o.getTaxId());
        item.setDetails(details);

        attachLatestVerification(item, VerificationEntityType.MACHINERY_OWNER, o.getId());
        item.setCreatedAt(o.getCreatedAt());
        item.setUpdatedAt(o.getUpdatedAt());
        return item;
    }

    private List<VerificationItemResponse> getMachineryList(VerificationStatus status) {
        List<Machinery> list = (status != null)
                ? machineryRepository.findByVerificationStatusOrderByCreatedAtDesc(status)
                : machineryRepository.findAllByOrderByCreatedAtDesc();
        return list.stream().map(this::mapMachinery).collect(Collectors.toList());
    }

    private VerificationItemResponse mapMachinery(Machinery m) {
        VerificationItemResponse item = new VerificationItemResponse();
        item.setEntityId(m.getId());
        item.setEntityType(VerificationEntityType.MACHINERY);
        item.setTitle(m.getName());
        item.setSubtitle(m.getManufacturer() + " " + m.getModel());
        item.setVerificationStatus(m.getVerificationStatus());

        if (m.getOwner() != null && m.getOwner().getUser() != null) {
            item.setUserId(m.getOwner().getUser().getId());
            item.setUserEmail(m.getOwner().getUser().getEmail());
            item.setUserName(m.getOwner().getUser().getFirstName() + " " + m.getOwner().getUser().getLastName());
            item.setPhoneNumber(m.getOwner().getUser().getPhoneNumber());
        }

        item.setLocation(m.getLocation());
        item.setDescription(m.getDescription());

        // Include all uploaded machinery images
        List<MachineryImage> images = machineryImageRepository.findByMachineryIdOrderByCreatedAtDesc(m.getId());
        if (images != null) {
            for (MachineryImage img : images) {
                if (img.getFileUrl() != null && !img.getFileUrl().isBlank()) {
                    item.getImages().add(img.getFileUrl());
                }
            }
        }

        Map<String, Object> details = new HashMap<>();
        details.put("category", m.getCategory());
        details.put("manufacturer", m.getManufacturer());
        details.put("model", m.getModel());
        details.put("manufacturingYear", m.getManufacturingYear());
        details.put("capacity", m.getCapacity());
        details.put("fuelType", m.getFuelType());
        details.put("transmission", m.getTransmission());
        details.put("rentalPricePerDay", m.getRentalPricePerDay());
        details.put("availabilityStatus", m.getAvailabilityStatus());
        if (m.getOwner() != null) {
            details.put("ownerCompanyName", m.getOwner().getCompanyName());
        }
        item.setDetails(details);

        attachLatestVerification(item, VerificationEntityType.MACHINERY, m.getId());
        item.setCreatedAt(m.getCreatedAt());
        item.setUpdatedAt(m.getUpdatedAt());
        return item;
    }

    private List<VerificationItemResponse> getMaterialSuppliers(VerificationStatus status) {
        List<MaterialSupplier> list = (status != null)
                ? materialSupplierRepository.findByVerificationStatusOrderByCreatedAtDesc(status)
                : materialSupplierRepository.findAllByOrderByCreatedAtDesc();
        return list.stream().map(this::mapMaterialSupplier).collect(Collectors.toList());
    }

    private VerificationItemResponse mapMaterialSupplier(MaterialSupplier s) {
        VerificationItemResponse item = new VerificationItemResponse();
        item.setEntityId(s.getId());
        item.setEntityType(VerificationEntityType.MATERIAL_SUPPLIER);
        item.setTitle(s.getStoreName());
        item.setSubtitle(s.getGstOrTaxNumber() != null ? "GST/Tax: " + s.getGstOrTaxNumber() : "Material Supplier");
        item.setVerificationStatus(s.getVerificationStatus());

        if (s.getUser() != null) {
            item.setUserId(s.getUser().getId());
            item.setUserEmail(s.getUser().getEmail());
            item.setUserName(s.getUser().getFirstName() + " " + s.getUser().getLastName());
            item.setPhoneNumber(s.getContactPhone() != null ? s.getContactPhone() : s.getUser().getPhoneNumber());
        }

        item.setAddress(s.getWarehouseAddress());
        item.setCity(s.getCity());
        item.setState(s.getState());
        item.setDescription(s.getDescription());

        Map<String, Object> details = new HashMap<>();
        details.put("gstOrTaxNumber", s.getGstOrTaxNumber());
        details.put("deliveryAvailable", s.getDeliveryAvailable());
        details.put("contactPhone", s.getContactPhone());
        item.setDetails(details);

        attachLatestVerification(item, VerificationEntityType.MATERIAL_SUPPLIER, s.getId());
        item.setCreatedAt(s.getCreatedAt());
        item.setUpdatedAt(s.getUpdatedAt());
        return item;
    }

    private List<VerificationItemResponse> getMaterials(VerificationStatus status) {
        List<Material> list = (status != null)
                ? materialRepository.findByVerificationStatusOrderByCreatedAtDesc(status)
                : materialRepository.findAllByOrderByCreatedAtDesc();
        return list.stream().map(this::mapMaterial).collect(Collectors.toList());
    }

    private VerificationItemResponse mapMaterial(Material mat) {
        VerificationItemResponse item = new VerificationItemResponse();
        item.setEntityId(mat.getId());
        item.setEntityType(VerificationEntityType.MATERIAL);
        item.setTitle(mat.getName());
        item.setSubtitle(mat.getGradeSpecification() != null ? mat.getGradeSpecification() : mat.getCategory().name());
        item.setVerificationStatus(mat.getVerificationStatus());

        if (mat.getSupplier() != null && mat.getSupplier().getUser() != null) {
            item.setUserId(mat.getSupplier().getUser().getId());
            item.setUserEmail(mat.getSupplier().getUser().getEmail());
            item.setUserName(mat.getSupplier().getUser().getFirstName() + " " + mat.getSupplier().getUser().getLastName());
            item.setPhoneNumber(mat.getSupplier().getContactPhone() != null ? mat.getSupplier().getContactPhone() : mat.getSupplier().getUser().getPhoneNumber());
        }

        item.setLocation(mat.getLocation());
        item.setDescription(mat.getDescription());

        Map<String, Object> details = new HashMap<>();
        details.put("category", mat.getCategory());
        details.put("gradeSpecification", mat.getGradeSpecification());
        details.put("unit", mat.getUnit());
        details.put("availableQuantity", mat.getAvailableQuantity());
        details.put("price", mat.getPrice());
        details.put("availability", mat.getAvailability());
        if (mat.getSupplier() != null) {
            details.put("supplierStoreName", mat.getSupplier().getStoreName());
        }
        item.setDetails(details);

        attachLatestVerification(item, VerificationEntityType.MATERIAL, mat.getId());
        item.setCreatedAt(mat.getCreatedAt());
        item.setUpdatedAt(mat.getUpdatedAt());
        return item;
    }

    private void attachLatestVerification(VerificationItemResponse item, VerificationEntityType entityType, Long entityId) {
        Optional<VerificationRecord> latest = verificationRecordRepository
                .findFirstByEntityTypeAndEntityIdOrderByVerificationDateDesc(entityType, entityId);
        latest.ifPresent(record -> item.setLatestVerification(VerificationRecordResponse.fromEntity(record)));
    }
}
