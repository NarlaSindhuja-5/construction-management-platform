package com.e2e.construction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class MachineConditionRequest {

    @NotBlank(message = "Engine condition is required (e.g. EXCELLENT, GOOD, FAIR, NEEDS_SERVICE, NOT_AVAILABLE)")
    private String engine;

    @NotBlank(message = "Engine oil condition is required (e.g. NORMAL, GOOD, LOW, NEEDS_CHANGE, NOT_AVAILABLE)")
    private String engineOil;

    @NotBlank(message = "Coolant condition is required (e.g. NORMAL, GOOD, LOW, NEEDS_TOPUP, NOT_AVAILABLE)")
    private String coolant;

    @NotBlank(message = "Transmission condition is required (e.g. EXCELLENT, GOOD, FAIR, NEEDS_SERVICE, NOT_AVAILABLE)")
    private String transmission;

    @NotBlank(message = "Hydraulic system condition is required (e.g. EXCELLENT, GOOD, FAIR, NEEDS_SERVICE, NOT_AVAILABLE)")
    private String hydraulicSystem;

    @NotBlank(message = "Battery condition is required (e.g. EXCELLENT, GOOD, FAIR, NEEDS_SERVICE, NOT_AVAILABLE)")
    private String battery;

    @NotBlank(message = "Brakes condition is required (e.g. EXCELLENT, GOOD, FAIR, NEEDS_SERVICE, NOT_AVAILABLE)")
    private String brakes;

    @NotBlank(message = "Tyres condition is required (e.g. 100%, 80%, 60%, 40%, GOOD)")
    private String tyres;

    @NotBlank(message = "Lights condition is required (e.g. EXCELLENT, GOOD, FAIR, NEEDS_SERVICE, NOT_AVAILABLE)")
    private String lights;

    @NotBlank(message = "Body condition is required (e.g. EXCELLENT, GOOD, FAIR, NEEDS_SERVICE, NOT_AVAILABLE)")
    private String body;

    @NotBlank(message = "Leakage status is required (e.g. NO, NONE, MINOR, MAJOR)")
    private String leakage;

    @NotBlank(message = "Starting condition is required (e.g. EXCELLENT, GOOD, FAIR, NEEDS_SERVICE)")
    private String startingCondition;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate inspectionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastServiceDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextServiceDate;

    private Double engineHours;

    private Double odometer;

    private String remarks;

    public MachineConditionRequest() {
    }

    public MachineConditionRequest(String engine, String engineOil, String coolant, String transmission,
                                 String hydraulicSystem, String battery, String brakes, String tyres,
                                 String lights, String body, String leakage, String startingCondition,
                                 LocalDate inspectionDate, LocalDate lastServiceDate, LocalDate nextServiceDate,
                                 Double engineHours, Double odometer, String remarks) {
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
        this.inspectionDate = inspectionDate;
        this.lastServiceDate = lastServiceDate;
        this.nextServiceDate = nextServiceDate;
        this.engineHours = engineHours;
        this.odometer = odometer;
        this.remarks = remarks;
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
}
