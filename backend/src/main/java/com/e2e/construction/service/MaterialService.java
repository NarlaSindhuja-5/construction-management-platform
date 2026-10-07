package com.e2e.construction.service;

import com.e2e.construction.dto.MaterialOrderRequest;
import com.e2e.construction.dto.MaterialOrderResponse;
import com.e2e.construction.dto.MaterialRequest;
import com.e2e.construction.dto.MaterialResponse;
import com.e2e.construction.dto.MaterialSupplierProfileRequest;
import com.e2e.construction.dto.MaterialSupplierProfileResponse;
import com.e2e.construction.dto.PriceUpdateRequest;
import com.e2e.construction.dto.StockHistoryResponse;
import com.e2e.construction.dto.StockUpdateRequest;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.Material;
import com.e2e.construction.entity.MaterialAvailability;
import com.e2e.construction.entity.MaterialCategory;
import com.e2e.construction.entity.MaterialOrder;
import com.e2e.construction.entity.MaterialOrderStatus;
import com.e2e.construction.entity.MaterialStockHistory;
import com.e2e.construction.entity.MaterialSupplier;
import com.e2e.construction.entity.Project;
import com.e2e.construction.entity.StockChangeType;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.MaterialOrderRepository;
import com.e2e.construction.repository.MaterialRepository;
import com.e2e.construction.repository.MaterialStockHistoryRepository;
import com.e2e.construction.repository.MaterialSupplierRepository;
import com.e2e.construction.repository.ProjectRepository;
import com.e2e.construction.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialStockHistoryRepository materialStockHistoryRepository;
    private final MaterialOrderRepository materialOrderRepository;
    private final MaterialSupplierRepository materialSupplierRepository;
    private final ContractorRepository contractorRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public MaterialService(
            MaterialRepository materialRepository,
            MaterialStockHistoryRepository materialStockHistoryRepository,
            MaterialOrderRepository materialOrderRepository,
            MaterialSupplierRepository materialSupplierRepository,
            ContractorRepository contractorRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository) {
        this.materialRepository = materialRepository;
        this.materialStockHistoryRepository = materialStockHistoryRepository;
        this.materialOrderRepository = materialOrderRepository;
        this.materialSupplierRepository = materialSupplierRepository;
        this.contractorRepository = contractorRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get or create supplier profile for authenticated user.
     */
    @Transactional
    public MaterialSupplier getOrCreateSupplier(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        return materialSupplierRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    String storeName = user.getFirstName() + " " + user.getLastName() + " Materials";
                    MaterialSupplier newSupplier = new MaterialSupplier(
                            user,
                            storeName,
                            null,
                            user.getPhoneNumber(),
                            "Central Depot",
                            "Austin",
                            "TX",
                            true,
                            "Building material merchant and supplier"
                    );
                    return materialSupplierRepository.save(newSupplier);
                });
    }

    /**
     * Save or update supplier profile.
     */
    @Transactional
    public MaterialSupplierProfileResponse saveSupplierProfile(String userEmail, MaterialSupplierProfileRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        MaterialSupplier supplier = materialSupplierRepository.findByUserId(user.getId())
                .orElseGet(() -> new MaterialSupplier(
                        user,
                        request.getStoreName(),
                        request.getGstOrTaxNumber(),
                        request.getContactPhone(),
                        request.getWarehouseAddress(),
                        request.getCity(),
                        request.getState(),
                        request.getDeliveryAvailable(),
                        request.getDescription()
                ));

        supplier.setStoreName(request.getStoreName());
        supplier.setGstOrTaxNumber(request.getGstOrTaxNumber());
        if (request.getContactPhone() != null && !request.getContactPhone().isBlank()) {
            supplier.setContactPhone(request.getContactPhone());
        }
        supplier.setWarehouseAddress(request.getWarehouseAddress());
        supplier.setCity(request.getCity());
        supplier.setState(request.getState());
        if (request.getDeliveryAvailable() != null) {
            supplier.setDeliveryAvailable(request.getDeliveryAvailable());
        }
        supplier.setDescription(request.getDescription());

        MaterialSupplier saved = materialSupplierRepository.save(supplier);
        return MaterialSupplierProfileResponse.fromEntity(saved);
    }

    /**
     * Get supplier profile.
     */
    @Transactional(readOnly = true)
    public MaterialSupplierProfileResponse getSupplierProfile(String userEmail) {
        MaterialSupplier supplier = materialSupplierRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Material supplier profile not found for: " + userEmail));
        return MaterialSupplierProfileResponse.fromEntity(supplier);
    }

    /**
     * 1. Material suppliers can create material listings.
     * Records initial stock history.
     */
    @Transactional
    public MaterialResponse createMaterial(MaterialRequest request, String supplierEmail) {
        MaterialSupplier supplier = getOrCreateSupplier(supplierEmail);

        BigDecimal qty = request.getAvailableQuantity() != null ? request.getAvailableQuantity() : BigDecimal.ZERO;
        Material material = new Material(
                supplier,
                request.getName(),
                request.getCategory(),
                request.getGradeSpecification(),
                request.getUnit(),
                qty,
                request.getPrice(),
                request.getLocation(),
                request.getDescription()
        );

        Material saved = materialRepository.save(material);

        // Record stock history
        MaterialStockHistory history = new MaterialStockHistory(
                saved,
                BigDecimal.ZERO,
                qty,
                qty,
                StockChangeType.INITIAL_STOCK,
                null,
                "Initial stock recorded on listing creation",
                supplierEmail
        );
        materialStockHistoryRepository.save(history);

        return MaterialResponse.fromEntity(saved);
    }

    /**
     * Update material metadata.
     */
    @Transactional
    public MaterialResponse updateMaterial(Long materialId, MaterialRequest request, String supplierEmail) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + materialId));

        if (!material.getSupplier().getUser().getEmail().equals(supplierEmail)) {
            throw new AccessDeniedException("You can only modify your own materials.");
        }

        material.setName(request.getName());
        material.setCategory(request.getCategory());
        material.setGradeSpecification(request.getGradeSpecification());
        material.setUnit(request.getUnit());
        material.setPrice(request.getPrice());
        material.setLocation(request.getLocation());
        material.setDescription(request.getDescription());

        Material saved = materialRepository.save(material);
        return MaterialResponse.fromEntity(saved);
    }

    /**
     * 2. Suppliers can update stock.
     * Creates stock history and recalculates availability status.
     */
    @Transactional
    public MaterialResponse updateStock(Long materialId, StockUpdateRequest request, String supplierEmail) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + materialId));

        if (!material.getSupplier().getUser().getEmail().equals(supplierEmail)) {
            throw new AccessDeniedException("You can only modify your own material stock.");
        }

        BigDecimal previousQty = material.getAvailableQuantity() != null ? material.getAvailableQuantity() : BigDecimal.ZERO;
        BigDecimal newQty;
        BigDecimal changeQty;

        if (Boolean.TRUE.equals(request.getIsDelta())) {
            changeQty = request.getQuantity();
            newQty = previousQty.add(changeQty);
            if (newQty.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Stock reduction cannot result in negative quantity.");
            }
        } else {
            newQty = request.getQuantity();
            if (newQty.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Stock quantity cannot be negative.");
            }
            changeQty = newQty.subtract(previousQty);
        }

        material.setAvailableQuantity(newQty);
        refreshAvailability(material);
        Material saved = materialRepository.save(material);

        StockChangeType changeType = changeQty.compareTo(BigDecimal.ZERO) >= 0
                ? StockChangeType.RESTOCK
                : StockChangeType.MANUAL_ADJUSTMENT;

        String notes = request.getNotes() != null && !request.getNotes().isBlank()
                ? request.getNotes()
                : "Stock updated by supplier";

        MaterialStockHistory history = new MaterialStockHistory(
                saved, previousQty, changeQty, newQty, changeType, null, notes, supplierEmail
        );
        materialStockHistoryRepository.save(history);

        return MaterialResponse.fromEntity(saved);
    }

    /**
     * 3. Suppliers can update price.
     */
    @Transactional
    public MaterialResponse updatePrice(Long materialId, PriceUpdateRequest request, String supplierEmail) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + materialId));

        if (!material.getSupplier().getUser().getEmail().equals(supplierEmail)) {
            throw new AccessDeniedException("You can only update prices for your own materials.");
        }

        BigDecimal oldPrice = material.getPrice();
        material.setPrice(request.getPrice());
        Material saved = materialRepository.save(material);

        String notes = "Price updated from " + oldPrice + " to " + request.getPrice() +
                (request.getNotes() != null ? ": " + request.getNotes() : "");

        MaterialStockHistory history = new MaterialStockHistory(
                saved, material.getAvailableQuantity(), BigDecimal.ZERO, material.getAvailableQuantity(),
                StockChangeType.PRICE_UPDATE, null, notes, supplierEmail
        );
        materialStockHistoryRepository.save(history);

        return MaterialResponse.fromEntity(saved);
    }

    /**
     * 4. Contractors can search and filter materials.
     */
    @Transactional(readOnly = true)
    public List<MaterialResponse> searchMaterials(
            String search, MaterialCategory category, String location,
            BigDecimal maxPrice, MaterialAvailability availability) {

        List<Material> materials = materialRepository.searchMaterials(search, category, location, maxPrice, availability);
        if (materials == null) {
            return Collections.emptyList();
        }
        return materials.stream().map(MaterialResponse::fromEntity).collect(Collectors.toList());
    }

    /**
     * View material details / stock / price.
     */
    @Transactional(readOnly = true)
    public MaterialResponse getMaterialById(Long materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + materialId));
        return MaterialResponse.fromEntity(material);
    }

    /**
     * View stock history for a material.
     */
    @Transactional(readOnly = true)
    public List<StockHistoryResponse> getStockHistory(Long materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + materialId));

        List<MaterialStockHistory> histories = materialStockHistoryRepository.findByMaterialIdOrderByCreatedAtDesc(material.getId());
        return histories.stream().map(StockHistoryResponse::fromEntity).collect(Collectors.toList());
    }

    /**
     * 5. Contractors can request/order material.
     * CRITICAL: Do not allow a contractor to order more than available stock.
     */
    @Transactional
    public MaterialOrderResponse createOrder(Long materialId, MaterialOrderRequest request, String contractorEmail) {
        if (materialId == null && request.getMaterialId() != null) {
            materialId = request.getMaterialId();
        }
        if (materialId == null) {
            throw new BadRequestException("Material ID is required.");
        }

        Contractor contractor = contractorRepository.findByUserEmail(contractorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Contractor profile not found for user: " + contractorEmail));

        final Long finalMaterialId = materialId;
        Material material = materialRepository.findById(finalMaterialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + finalMaterialId));

        // Enforce stock availability check: do not allow ordering more than available stock!
        if (material.getAvailability() == MaterialAvailability.OUT_OF_STOCK || material.getAvailableQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Material is currently out of stock.");
        }

        if (request.getQuantity().compareTo(material.getAvailableQuantity()) > 0) {
            throw new BadRequestException("Order quantity (" + request.getQuantity() + " " + material.getUnit() +
                    ") exceeds available stock (" + material.getAvailableQuantity() + " " + material.getUnit() + ").");
        }

        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));
            if (!project.getContractor().getId().equals(contractor.getId())) {
                throw new AccessDeniedException("You can only link orders to your own projects.");
            }
        }

        BigDecimal unitPrice = material.getPrice();
        BigDecimal totalAmount = unitPrice.multiply(request.getQuantity());

        String orderNumber = "ORD-MAT-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        MaterialOrder order = new MaterialOrder(
                orderNumber,
                contractor,
                material.getSupplier(),
                material,
                project,
                request.getQuantity(),
                material.getUnit(),
                unitPrice,
                totalAmount,
                request.getDeliveryAddress(),
                request.getRequestedDeliveryDate(),
                request.getContractorNotes()
        );

        MaterialOrder saved = materialOrderRepository.save(order);
        return MaterialOrderResponse.fromEntity(saved);
    }

    /**
     * 6. Suppliers can accept order.
     * Deducts stock and records stock history.
     */
    @Transactional
    public MaterialOrderResponse acceptOrder(Long orderId, String supplierEmail, String notes) {
        MaterialOrder order = materialOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Material order not found with id: " + orderId));

        if (!order.getSupplier().getUser().getEmail().equals(supplierEmail)) {
            throw new AccessDeniedException("You are not authorized to accept this order.");
        }

        if (order.getStatus() != MaterialOrderStatus.PENDING) {
            throw new BadRequestException("Cannot accept order with status: " + order.getStatus());
        }

        Material material = order.getMaterial();
        if (material.getAvailableQuantity().compareTo(order.getQuantity()) < 0) {
            throw new BadRequestException("Insufficient available stock to accept order. Available: " +
                    material.getAvailableQuantity() + ", Ordered: " + order.getQuantity());
        }

        // Deduct stock
        BigDecimal previousQty = material.getAvailableQuantity();
        BigDecimal newQty = previousQty.subtract(order.getQuantity());
        material.setAvailableQuantity(newQty);
        refreshAvailability(material);
        materialRepository.save(material);

        // Record stock history deduction
        MaterialStockHistory history = new MaterialStockHistory(
                material,
                previousQty,
                order.getQuantity().negate(),
                newQty,
                StockChangeType.ORDER_DEDUCTION,
                order.getId(),
                "Stock deducted upon order acceptance #" + order.getOrderNumber(),
                supplierEmail
        );
        materialStockHistoryRepository.save(history);

        order.setStatus(MaterialOrderStatus.ACCEPTED);
        if (notes != null && !notes.isBlank()) {
            order.setSupplierNotes(notes);
        }
        MaterialOrder saved = materialOrderRepository.save(order);

        return MaterialOrderResponse.fromEntity(saved);
    }

    /**
     * 7. Suppliers can reject order.
     */
    @Transactional
    public MaterialOrderResponse rejectOrder(Long orderId, String supplierEmail, String reason) {
        MaterialOrder order = materialOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Material order not found with id: " + orderId));

        if (!order.getSupplier().getUser().getEmail().equals(supplierEmail)) {
            throw new AccessDeniedException("You are not authorized to reject this order.");
        }

        if (order.getStatus() != MaterialOrderStatus.PENDING) {
            throw new BadRequestException("Cannot reject order with status: " + order.getStatus());
        }

        order.setStatus(MaterialOrderStatus.REJECTED);
        order.setRejectionReason(reason);
        order.setSupplierNotes(reason);
        MaterialOrder saved = materialOrderRepository.save(order);

        return MaterialOrderResponse.fromEntity(saved);
    }

    /**
     * 8. Contractors can cancel order.
     * If the order was already accepted, restores deducted stock and logs history.
     */
    @Transactional
    public MaterialOrderResponse cancelOrder(Long orderId, String contractorEmail, String reason) {
        MaterialOrder order = materialOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Material order not found with id: " + orderId));

        if (!order.getContractor().getUser().getEmail().equals(contractorEmail)) {
            throw new AccessDeniedException("You are not authorized to cancel this order.");
        }

        if (order.getStatus() == MaterialOrderStatus.COMPLETED ||
                order.getStatus() == MaterialOrderStatus.DELIVERED ||
                order.getStatus() == MaterialOrderStatus.CANCELLED ||
                order.getStatus() == MaterialOrderStatus.REJECTED) {
            throw new BadRequestException("Cannot cancel order in status: " + order.getStatus());
        }

        // If it was accepted and stock had been deducted, restore the stock!
        if (order.getStatus() == MaterialOrderStatus.ACCEPTED) {
            Material material = order.getMaterial();
            BigDecimal prev = material.getAvailableQuantity();
            BigDecimal restored = prev.add(order.getQuantity());
            material.setAvailableQuantity(restored);
            refreshAvailability(material);
            materialRepository.save(material);

            MaterialStockHistory history = new MaterialStockHistory(
                    material,
                    prev,
                    order.getQuantity(),
                    restored,
                    StockChangeType.ORDER_CANCELLED_RESTORE,
                    order.getId(),
                    "Stock restored after order cancellation #" + order.getOrderNumber(),
                    contractorEmail
            );
            materialStockHistoryRepository.save(history);
        }

        order.setStatus(MaterialOrderStatus.CANCELLED);
        order.setContractorNotes(reason != null ? "Cancelled: " + reason : "Cancelled by contractor");
        MaterialOrder saved = materialOrderRepository.save(order);

        return MaterialOrderResponse.fromEntity(saved);
    }

    /**
     * 9. Suppliers can view order history.
     */
    @Transactional(readOnly = true)
    public List<MaterialOrderResponse> getSupplierOrderHistory(String supplierEmail, MaterialOrderStatus status) {
        MaterialSupplier supplier = materialSupplierRepository.findByUserEmail(supplierEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found for user: " + supplierEmail));

        List<MaterialOrder> orders = (status != null)
                ? materialOrderRepository.findBySupplierIdAndStatusOrderByCreatedAtDesc(supplier.getId(), status)
                : materialOrderRepository.findBySupplierIdOrderByCreatedAtDesc(supplier.getId());

        return orders.stream().map(MaterialOrderResponse::fromEntity).collect(Collectors.toList());
    }

    /**
     * Contractors can view their orders.
     */
    @Transactional(readOnly = true)
    public List<MaterialOrderResponse> getContractorOrderHistory(String contractorEmail, MaterialOrderStatus status) {
        Contractor contractor = contractorRepository.findByUserEmail(contractorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Contractor not found for user: " + contractorEmail));

        List<MaterialOrder> orders = (status != null)
                ? materialOrderRepository.findByContractorIdAndStatusOrderByCreatedAtDesc(contractor.getId(), status)
                : materialOrderRepository.findByContractorIdOrderByCreatedAtDesc(contractor.getId());

        return orders.stream().map(MaterialOrderResponse::fromEntity).collect(Collectors.toList());
    }

    /**
     * View single order by ID.
     */
    @Transactional(readOnly = true)
    public MaterialOrderResponse getOrderById(Long orderId, String userEmail) {
        MaterialOrder order = materialOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        boolean isContractor = order.getContractor().getUser().getEmail().equals(userEmail);
        boolean isSupplier = order.getSupplier().getUser().getEmail().equals(userEmail);

        if (!isContractor && !isSupplier) {
            throw new AccessDeniedException("You are not authorized to view this order.");
        }

        return MaterialOrderResponse.fromEntity(order);
    }

    /**
     * Suppliers can view their material listings.
     */
    @Transactional(readOnly = true)
    public List<MaterialResponse> getSupplierMaterials(String supplierEmail) {
        List<Material> materials = materialRepository.findBySupplierUserEmailOrderByCreatedAtDesc(supplierEmail);
        return materials.stream().map(MaterialResponse::fromEntity).collect(Collectors.toList());
    }

    private void refreshAvailability(Material material) {
        BigDecimal qty = material.getAvailableQuantity();
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            material.setAvailability(MaterialAvailability.OUT_OF_STOCK);
        } else if (qty.compareTo(BigDecimal.valueOf(10.00)) <= 0) {
            material.setAvailability(MaterialAvailability.LOW_STOCK);
        } else {
            material.setAvailability(MaterialAvailability.IN_STOCK);
        }
    }
}
