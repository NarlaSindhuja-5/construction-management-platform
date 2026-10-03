package com.e2e.construction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "machine_conditions")
public class MachineCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "machinery_id", nullable = false)
    private Machinery machinery;

    @Column(name = "engine", nullable = false, length = 50)
    private String engine;

    @Column(name = "engine_oil", nullable = false, length = 50)
    private String engineOil;

    @Column(name = "coolant", nullable = false, length = 50)
    private String coolant;

    @Column(name = "transmission", nullable = false, length = 50)
    private String transmission;

    @Column(name = "hydraulic_system", nullable = false, length = 50)
    private String hydraulicSystem;

    @Column(name = "battery", nullable = false, length = 50)
    private String battery;

    @Column(name = "brakes", nullable = false, length = 50)
    private String brakes;

    @Column(name = "tyres", nullable = false, length = 50)
    private String tyres;

    @Column(name = "lights", nullable = false, length = 50)
    private String lights;

    @Column(name = "body", nullable = false, length = 50)
    private String body;

    @Column(name = "leakage", nullable = false, length = 50)
    private String leakage;

    @Column(name = "starting_condition", nullable = false, length = 50)
    private String startingCondition;

    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @Column(name = "next_service_date")
    private LocalDate nextServiceDate;

    @Column(name = "engine_hours")
    private Double engineHours;

    @Column(name = "odometer")
    private Double odometer;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public MachineCondition() {
    }

    public MachineCondition(Machinery machinery, String engine, String engineOil, String coolant,
                            String transmission, String hydraulicSystem, String battery,
                            String brakes, String tyres, String lights, String body,
                            String leakage, String startingCondition, LocalDate inspectionDate) {
        this.machinery = machinery;
        this.engine = engine;
        this.engineOil = engineOil;
        this.coolant = coolant;
        this.transmission = transmission;
        this.hydraulicSystem = hydraulicSystem;
        this.battery = battery;
        this.brakes = brakes;
        this.tyres = tyres;
        this.lights = lights;
        this.body = body;
        this.leakage = leakage;
        this.startingCondition = startingCondition;
        this.inspectionDate = inspectionDate != null ? inspectionDate : LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Machinery getMachinery() {
        return machinery;
    }

    public void setMachinery(Machinery machinery) {
        this.machinery = machinery;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getEngineOil() {
        return engineOil;
    }

    public void setEngineOil(String engineOil) {
        this.engineOil = engineOil;
    }

    public String getCoolant() {
        return coolant;
    }

    public void setCoolant(String coolant) {
        this.coolant = coolant;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getHydraulicSystem() {
        return hydraulicSystem;
    }

    public void setHydraulicSystem(String hydraulicSystem) {
        this.hydraulicSystem = hydraulicSystem;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getBrakes() {
        return brakes;
    }

    public void setBrakes(String brakes) {
        this.brakes = brakes;
    }

    public String getTyres() {
        return tyres;
    }

    public void setTyres(String tyres) {
        this.tyres = tyres;
    }

    public String getLights() {
        return lights;
    }

    public void setLights(String lights) {
        this.lights = lights;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getLeakage() {
        return leakage;
    }

    public void setLeakage(String leakage) {
        this.leakage = leakage;
    }

    public String getStartingCondition() {
        return startingCondition;
    }

    public void setStartingCondition(String startingCondition) {
        this.startingCondition = startingCondition;
    }

    public LocalDate getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(LocalDate inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public LocalDate getLastServiceDate() {
        return lastServiceDate;
    }

    public void setLastServiceDate(LocalDate lastServiceDate) {
        this.lastServiceDate = lastServiceDate;
    }

    public LocalDate getNextServiceDate() {
        return nextServiceDate;
    }

    public void setNextServiceDate(LocalDate nextServiceDate) {
        this.nextServiceDate = nextServiceDate;
    }

    public Double getEngineHours() {
        return engineHours;
    }

    public void setEngineHours(Double engineHours) {
        this.engineHours = engineHours;
    }

    public Double getOdometer() {
        return odometer;
    }

    public void setOdometer(Double odometer) {
        this.odometer = odometer;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
