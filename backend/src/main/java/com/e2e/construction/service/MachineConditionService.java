package com.e2e.construction.service;

import com.e2e.construction.dto.MachineConditionRequest;
import com.e2e.construction.dto.MachineConditionResponse;
import com.e2e.construction.entity.MachineCondition;
import com.e2e.construction.entity.Machinery;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.MachineConditionRepository;
import com.e2e.construction.repository.MachineryRepository;
import com.e2e.construction.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MachineConditionService {

    private final MachineConditionRepository machineConditionRepository;
    private final MachineryRepository machineryRepository;
    private final UserRepository userRepository;

    public MachineConditionService(
            MachineConditionRepository machineConditionRepository,
            MachineryRepository machineryRepository,
            UserRepository userRepository) {
        this.machineConditionRepository = machineConditionRepository;
        this.machineryRepository = machineryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create an initial baseline condition report for a machine.
     * Ensures every machine has a condition record.
     */
    @Transactional
    public MachineCondition createDefaultInitialCondition(Machinery machine) {
        MachineCondition condition = new MachineCondition();
        condition.setMachinery(machine);
        condition.setEngine("GOOD");
        condition.setEngineOil("NORMAL");
        condition.setCoolant("NORMAL");
        condition.setTransmission("GOOD");
        condition.setHydraulicSystem("GOOD");
        condition.setBattery("GOOD");
        condition.setBrakes("GOOD");
        condition.setTyres("100%");
        condition.setLights("GOOD");
        condition.setBody("GOOD");
        condition.setLeakage("NO");
        condition.setStartingCondition("GOOD");
        condition.setInspectionDate(LocalDate.now());
        condition.setRemarks("Baseline machine inspection recorded upon registration.");
        return machineConditionRepository.save(condition);
    }

    /**
     * Create a new condition report for a machine.
     * Only the machine owner or authorized admin can create/update condition information.
     */
    @Transactional
    public MachineConditionResponse createReport(Long machineId, String userEmail, MachineConditionRequest request) {
        Machinery machine = machineryRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machinery", "id", machineId));

        verifyOwnerOrAdmin(machine, userEmail);

        MachineCondition condition = new MachineCondition();
        condition.setMachinery(machine);
        applyRequestToEntity(condition, request);

        condition = machineConditionRepository.save(condition);
        return MachineConditionResponse.fromEntity(condition);
    }

    /**
     * Update an existing condition report by condition ID.
     * Only the machine owner or authorized admin can update condition information.
     */
    @Transactional
    public MachineConditionResponse updateReport(Long conditionId, String userEmail, MachineConditionRequest request) {
        MachineCondition condition = machineConditionRepository.findById(conditionId)
                .orElseThrow(() -> new ResourceNotFoundException("MachineCondition", "id", conditionId));

        verifyOwnerOrAdmin(condition.getMachinery(), userEmail);

        applyRequestToEntity(condition, request);

        condition = machineConditionRepository.save(condition);
        return MachineConditionResponse.fromEntity(condition);
    }

    /**
     * Update the latest condition report of a machine, or create one if none exists.
     * Only the machine owner or authorized admin can update condition information.
     */
    @Transactional
    public MachineConditionResponse updateLatestCondition(Long machineId, String userEmail, MachineConditionRequest request) {
        Machinery machine = machineryRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machinery", "id", machineId));

        verifyOwnerOrAdmin(machine, userEmail);

        MachineCondition condition = machineConditionRepository
                .findFirstByMachineryIdOrderByInspectionDateDescCreatedAtDesc(machineId)
                .orElseGet(() -> {
                    MachineCondition newCond = new MachineCondition();
                    newCond.setMachinery(machine);
                    return newCond;
                });

        applyRequestToEntity(condition, request);
        condition = machineConditionRepository.save(condition);
        return MachineConditionResponse.fromEntity(condition);
    }

    /**
     * View the current/latest condition report for a machine.
     * If no record exists yet, a default initial record is created to ensure every machine has a condition record.
     */
    @Transactional
    public MachineConditionResponse getLatestCondition(Long machineId) {
        Machinery machine = machineryRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machinery", "id", machineId));

        MachineCondition condition = machineConditionRepository
                .findFirstByMachineryIdOrderByInspectionDateDescCreatedAtDesc(machineId)
                .orElseGet(() -> createDefaultInitialCondition(machine));

        return MachineConditionResponse.fromEntity(condition);
    }

    /**
     * View the full condition history for a machine, ordered by inspection date and creation timestamp descending.
     */
    @Transactional
    public List<MachineConditionResponse> getConditionHistory(Long machineId) {
        Machinery machine = machineryRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machinery", "id", machineId));

        List<MachineCondition> list = machineConditionRepository
                .findByMachineryIdOrderByInspectionDateDescCreatedAtDesc(machineId);

        if (list.isEmpty()) {
            MachineCondition initial = createDefaultInitialCondition(machine);
            list = List.of(initial);
        }

        return list.stream()
                .map(MachineConditionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * View a specific condition report by its ID.
     */
    @Transactional(readOnly = true)
    public MachineConditionResponse getConditionById(Long conditionId) {
        MachineCondition condition = machineConditionRepository.findById(conditionId)
                .orElseThrow(() -> new ResourceNotFoundException("MachineCondition", "id", conditionId));
        return MachineConditionResponse.fromEntity(condition);
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
            throw new AccessDeniedException("Access denied: Only the machine owner or an authorized administrator can update condition information.");
        }
    }

    private void applyRequestToEntity(MachineCondition condition, MachineConditionRequest request) {
        if (request.getEngine() != null) condition.setEngine(request.getEngine().trim());
        if (request.getEngineOil() != null) condition.setEngineOil(request.getEngineOil().trim());
        if (request.getCoolant() != null) condition.setCoolant(request.getCoolant().trim());
        if (request.getTransmission() != null) condition.setTransmission(request.getTransmission().trim());
        if (request.getHydraulicSystem() != null) condition.setHydraulicSystem(request.getHydraulicSystem().trim());
        if (request.getBattery() != null) condition.setBattery(request.getBattery().trim());
        if (request.getBrakes() != null) condition.setBrakes(request.getBrakes().trim());
        if (request.getTyres() != null) condition.setTyres(request.getTyres().trim());
        if (request.getLights() != null) condition.setLights(request.getLights().trim());
        if (request.getBody() != null) condition.setBody(request.getBody().trim());
        if (request.getLeakage() != null) condition.setLeakage(request.getLeakage().trim());
        if (request.getStartingCondition() != null) condition.setStartingCondition(request.getStartingCondition().trim());
        if (request.getInspectionDate() != null) {
            condition.setInspectionDate(request.getInspectionDate());
        } else if (condition.getInspectionDate() == null) {
            condition.setInspectionDate(LocalDate.now());
        }
        if (request.getLastServiceDate() != null) condition.setLastServiceDate(request.getLastServiceDate());
        if (request.getNextServiceDate() != null) condition.setNextServiceDate(request.getNextServiceDate());
        if (request.getEngineHours() != null) condition.setEngineHours(request.getEngineHours());
        if (request.getOdometer() != null) condition.setOdometer(request.getOdometer());
        if (request.getRemarks() != null) condition.setRemarks(request.getRemarks().trim());
    }
}
