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
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tender_applications",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_contractor_tender", columnNames = {"contractor_id", "tender_id"})
        }
)
public class TenderApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tender_id", nullable = false)
    private Tender tender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contractor_id", nullable = false)
    private Contractor contractor;

    @Column(name = "bid_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal bidAmount;

    @Column(name = "proposed_duration_days")
    private Integer proposedDurationDays;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TenderApplicationStatus status = TenderApplicationStatus.SUBMITTED;

    @Column(name = "submission_date", nullable = false)
    private LocalDate submissionDate;

    @Column(name = "reviewer_remarks", columnDefinition = "TEXT")
    private String reviewerRemarks;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TenderApplicationDocument> documents = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public TenderApplication() {
    }

    public TenderApplication(Tender tender, Contractor contractor, BigDecimal bidAmount,
                             Integer proposedDurationDays, String coverLetter,
                             LocalDate submissionDate) {
        this.tender = tender;
        this.contractor = contractor;
        this.bidAmount = bidAmount;
        this.proposedDurationDays = proposedDurationDays;
        this.coverLetter = coverLetter;
        this.status = TenderApplicationStatus.SUBMITTED;
        this.submissionDate = submissionDate != null ? submissionDate : LocalDate.now();
    }

    public void addDocument(TenderApplicationDocument doc) {
        documents.add(doc);
        doc.setApplication(this);
    }

    public void removeDocument(TenderApplicationDocument doc) {
        documents.remove(doc);
        doc.setApplication(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tender getTender() {
        return tender;
    }

    public void setTender(Tender tender) {
        this.tender = tender;
    }

    public Contractor getContractor() {
        return contractor;
    }

    public void setContractor(Contractor contractor) {
        this.contractor = contractor;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }

    public Integer getProposedDurationDays() {
        return proposedDurationDays;
    }

    public void setProposedDurationDays(Integer proposedDurationDays) {
        this.proposedDurationDays = proposedDurationDays;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public TenderApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(TenderApplicationStatus status) {
        this.status = status;
    }

    public LocalDate getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDate submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getReviewerRemarks() {
        return reviewerRemarks;
    }

    public void setReviewerRemarks(String reviewerRemarks) {
        this.reviewerRemarks = reviewerRemarks;
    }

    public List<TenderApplicationDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<TenderApplicationDocument> documents) {
        this.documents = documents;
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
