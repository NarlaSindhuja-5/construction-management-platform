package com.e2e.construction.entity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tenders")
public class Tender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tender_number", nullable = false, unique = true, length = 100)
    private String tenderNumber;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "department", nullable = false, length = 150)
    private String department;

    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "estimated_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal estimatedValue;

    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    @Column(name = "closing_date", nullable = false)
    private LocalDate closingDate;

    @Column(name = "eligibility_criteria", columnDefinition = "TEXT", nullable = false)
    private String eligibilityCriteria;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TenderStatus status = TenderStatus.OPEN;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TenderDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TenderApplication> applications = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Tender() {
    }

    public Tender(String tenderNumber, String title, String department, String location,
                  String category, BigDecimal estimatedValue, LocalDate publishedDate,
                  LocalDate closingDate, String eligibilityCriteria, String description,
                  TenderStatus status, User createdBy) {
        this.tenderNumber = tenderNumber;
        this.title = title;
        this.department = department;
        this.location = location;
        this.category = category;
        this.estimatedValue = estimatedValue;
        this.publishedDate = publishedDate;
        this.closingDate = closingDate;
        this.eligibilityCriteria = eligibilityCriteria;
        this.description = description;
        this.status = status != null ? status : TenderStatus.OPEN;
        this.createdBy = createdBy;
    }

    public void addDocument(TenderDocument doc) {
        documents.add(doc);
        doc.setTender(this);
    }

    public void removeDocument(TenderDocument doc) {
        documents.remove(doc);
        doc.setTender(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenderNumber() {
        return tenderNumber;
    }

    public void setTenderNumber(String tenderNumber) {
        this.tenderNumber = tenderNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(LocalDate closingDate) {
        this.closingDate = closingDate;
    }

    public String getEligibilityCriteria() {
        return eligibilityCriteria;
    }

    public void setEligibilityCriteria(String eligibilityCriteria) {
        this.eligibilityCriteria = eligibilityCriteria;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TenderStatus getStatus() {
        return status;
    }

    public void setStatus(TenderStatus status) {
        this.status = status;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public List<TenderDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<TenderDocument> documents) {
        this.documents = documents;
    }

    public List<TenderApplication> getApplications() {
        return applications;
    }

    public void setApplications(List<TenderApplication> applications) {
        this.applications = applications;
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
