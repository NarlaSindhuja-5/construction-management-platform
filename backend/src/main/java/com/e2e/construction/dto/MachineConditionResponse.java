package com.e2e.construction.dto;

import com.e2e.construction.entity.MachineCondition;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MachineConditionResponse {

    private Long id;
    private Long machineId;
    private String machineName;
    private String engine;
    private String engineOil;
    private String coolant;
    private String transmission;
    private String hydraulicSystem;
    private String battery;
    private String brakes;
    private String tyres;
    private String lights;
    private String body;
    private String leakage;
    private String startingCondition;
    private LocalDate inspectionDate;
    private LocalDate lastServiceDate;
    private LocalDate nextServiceDate;
    private Double engineHours;
    private Double odometer;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MachineConditionResponse() {
    }

    public static MachineConditionResponse fromEntity(MachineCondition condition) {
        if (condition == null) {
            return null;
        }

        MachineConditionResponse response = new MachineConditionResponse();
        response.setId(condition.getId());
        if (condition.getMachinery() != null) {
            response.setMachineId(condition.getMachinery().getId());
            response.setMachineName(condition.getMachinery().getName());
        }
        response.setEngine(condition.getEngine());
        response.setEngineOil(condition.getEngineOil());
        response.setCoolant(condition.getCoolant());
        response.setTransmission(condition.getTransmission());
        response.setHydraulicSystem(condition.getHydraulicSystem());
        response.setBattery(condition.getBattery());
        response.setBrakes(condition.getBrakes());
        response.setTyres(condition.getTyres());
        response.setLights(condition.getLights());
        response.setBody(condition.getBody());
        response.setLeakage(condition.getLeakage());
        response.setStartingCondition(condition.getStartingCondition());
        response.setInspectionDate(condition.getInspectionDate());
        response.setLastServiceDate(condition.getLastServiceDate());
        response.setNextServiceDate(condition.getNextServiceDate());
        response.setEngineHours(condition.getEngineHours());
        response.setOdometer(condition.getOdometer());
        response.setRemarks(condition.getRemarks());
        response.setCreatedAt(condition.getCreatedAt());
        response.setUpdatedAt(condition.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMachineId() {
        return machineId;
    }

    public void setMachineId(Long machineId) {
        this.machineId = machineId;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
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
