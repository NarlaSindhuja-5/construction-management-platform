package com.e2e.construction.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "daily_work_reports",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_project_report_date", columnNames = {"project_id", "report_date"})
        }
)
public class DailyWorkReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "work_description", columnDefinition = "TEXT", nullable = false)
    private String workDescription;

    @Column(name = "number_of_workers", nullable = false)
    private Integer numberOfWorkers;

    @Column(name = "machinery_used", columnDefinition = "TEXT")
    private String machineryUsed;

    @Column(name = "materials_used", columnDefinition = "TEXT")
    private String materialsUsed;

    @Column(name = "quantity_completed", length = 255)
    private String quantityCompleted;

    @Column(name = "working_hours")
    private Double workingHours;

    @Column(name = "progress_percentage")
    private Double progressPercentage;

    @Column(name = "expenses", precision = 12, scale = 2)
    private BigDecimal expenses;

    @Column(name = "issues_or_delays", columnDefinition = "TEXT")
    private String issuesOrDelays;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyWorkReportImage> images = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public DailyWorkReport() {
    }

    public DailyWorkReport(Project project, LocalDate reportDate, String workDescription,
                           Integer numberOfWorkers, String machineryUsed, String materialsUsed,
                           String quantityCompleted, Double workingHours, Double progressPercentage,
                           BigDecimal expenses, String issuesOrDelays, String remarks, User createdBy) {
        this.project = project;
        this.reportDate = reportDate;
        this.workDescription = workDescription;
        this.numberOfWorkers = numberOfWorkers;
        this.machineryUsed = machineryUsed;
        this.materialsUsed = materialsUsed;
        this.quantityCompleted = quantityCompleted;
        this.workingHours = workingHours;
        this.progressPercentage = progressPercentage;
        this.expenses = expenses;
        this.issuesOrDelays = issuesOrDelays;
        this.remarks = remarks;
        this.createdBy = createdBy;
    }

    public void addImage(DailyWorkReportImage image) {
        images.add(image);
        image.setReport(this);
    }

    public void removeImage(DailyWorkReportImage image) {
        images.remove(image);
        image.setReport(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }

    public Integer getNumberOfWorkers() {
        return numberOfWorkers;
    }

    public void setNumberOfWorkers(Integer numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }

    public String getMachineryUsed() {
        return machineryUsed;
    }

    public void setMachineryUsed(String machineryUsed) {
        this.machineryUsed = machineryUsed;
    }

    public String getMaterialsUsed() {
        return materialsUsed;
    }

    public void setMaterialsUsed(String materialsUsed) {
        this.materialsUsed = materialsUsed;
    }

    public String getQuantityCompleted() {
        return quantityCompleted;
    }

    public void setQuantityCompleted(String quantityCompleted) {
        this.quantityCompleted = quantityCompleted;
    }

    public Double getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(Double workingHours) {
        this.workingHours = workingHours;
    }

    public Double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public BigDecimal getExpenses() {
        return expenses;
    }

    public void setExpenses(BigDecimal expenses) {
        this.expenses = expenses;
    }

    public String getIssuesOrDelays() {
        return issuesOrDelays;
    }

    public void setIssuesOrDelays(String issuesOrDelays) {
        this.issuesOrDelays = issuesOrDelays;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public List<DailyWorkReportImage> getImages() {
        return images;
    }

    public void setImages(List<DailyWorkReportImage> images) {
        this.images = images;
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
