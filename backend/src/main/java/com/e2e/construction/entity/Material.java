package com.e2e.construction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "materials")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id", nullable = false)
    private MaterialSupplier supplier;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private MaterialCategory category;

    @Column(name = "grade_specification", length = 100)
    private String gradeSpecification;

    @Column(name = "unit", nullable = false, length = 50)
    private String unit;

    @Column(name = "available_quantity", precision = 12, scale = 2, nullable = false)
    private BigDecimal availableQuantity = BigDecimal.ZERO;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability", nullable = false, length = 30)
    private MaterialAvailability availability = MaterialAvailability.IN_STOCK;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.VERIFIED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Material() {
    }

    public Material(MaterialSupplier supplier, String name, MaterialCategory category,
                    String gradeSpecification, String unit, BigDecimal availableQuantity,
                    BigDecimal price, String location, String description) {
        this.supplier = supplier;
        this.name = name;
        this.category = category;
        this.gradeSpecification = gradeSpecification;
        this.unit = unit;
        this.availableQuantity = availableQuantity != null ? availableQuantity : BigDecimal.ZERO;
        this.price = price;
        this.location = location;
        this.description = description;
        this.availability = (this.availableQuantity.compareTo(BigDecimal.ZERO) > 0)
                ? MaterialAvailability.IN_STOCK
                : MaterialAvailability.OUT_OF_STOCK;
        this.verificationStatus = VerificationStatus.VERIFIED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MaterialSupplier getSupplier() {
        return supplier;
    }

    public void setSupplier(MaterialSupplier supplier) {
        this.supplier = supplier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MaterialCategory getCategory() {
        return category;
    }

    public void setCategory(MaterialCategory category) {
        this.category = category;
    }

    public String getGradeSpecification() {
        return gradeSpecification;
    }

    public void setGradeSpecification(String gradeSpecification) {
        this.gradeSpecification = gradeSpecification;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(BigDecimal availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MaterialAvailability getAvailability() {
        return availability;
    }

    public void setAvailability(MaterialAvailability availability) {
        this.availability = availability;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
