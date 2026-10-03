package com.e2e.construction.service;

import com.e2e.construction.dto.MachineryRequest;
import com.e2e.construction.dto.MachineryResponse;
import com.e2e.construction.entity.Machinery;
import com.e2e.construction.entity.MachineryAvailability;
import com.e2e.construction.entity.MachineryCategory;
import com.e2e.construction.entity.MachineryOwner;
import com.e2e.construction.entity.User;
import com.e2e.construction.entity.VerificationStatus;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.MachineryOwnerRepository;
import com.e2e.construction.repository.MachineryRepository;
import com.e2e.construction.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MachineryService {

    private final MachineryRepository machineryRepository;
    private final MachineryOwnerRepository machineryOwnerRepository;
    private final UserRepository userRepository;
    private final MachineConditionService machineConditionService;

    public MachineryService(
            MachineryRepository machineryRepository,
            MachineryOwnerRepository machineryOwnerRepository,
            UserRepository userRepository,
            MachineConditionService machineConditionService) {
        this.machineryRepository = machineryRepository;
        this.machineryOwnerRepository = machineryOwnerRepository;
        this.userRepository = userRepository;
        this.machineConditionService = machineConditionService;
    }

    /**
     * Create a new machinery listing. Only authenticated MACHINERY_OWNER users can create listings.
     */
    @Transactional
    public MachineryResponse createMachine(String userEmail, MachineryRequest request) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        // Enforce MACHINERY_OWNER role
        if (!user.getRole().getName().equalsIgnoreCase("MACHINERY_OWNER")) {
            throw new BadRequestException("Only authenticated MACHINERY_OWNER users can create machinery listings. Current role: " + user.getRole().getName());
        }

        // Resolve MachineryOwner profile
        MachineryOwner owner = machineryOwnerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Please create your Machinery Owner Profile before listing machines. Use POST /api/machinery-owners/profile."));

        Machinery machine = new Machinery();
        machine.setOwner(owner);
        machine.setName(request.getName().trim());
        machine.setCategory(request.getCategory());
        machine.setManufacturer(request.getManufacturer().trim());
        machine.setModel(request.getModel().trim());
        machine.setManufacturingYear(request.getManufacturingYear());
        machine.setCapacity(request.getCapacity().trim());
        machine.setFuelType(request.getFuelType().trim());
        machine.setTransmission(request.getTransmission().trim());
        machine.setLocation(request.getLocation().trim());
        machine.setRentalPricePerDay(request.getRentalPricePerDay());
        machine.setDescription(request.getDescription().trim());
        machine.setAvailabilityStatus(request.getAvailabilityStatus() != null ? request.getAvailabilityStatus() : MachineryAvailability.AVAILABLE);
        machine.setVerificationStatus(VerificationStatus.PENDING);

        machine = machineryRepository.save(machine);
        var condition = machineConditionService.createDefaultInitialCondition(machine);
        return MachineryResponse.fromEntity(machine, com.e2e.construction.dto.MachineConditionResponse.fromEntity(condition));
    }

    /**
     * View machine by ID.
     */
    @Transactional
    public MachineryResponse getMachineById(Long id) {
        Machinery machine = machineryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machinery", "id", id));

        var condition = machineConditionService.getLatestCondition(id);
        return MachineryResponse.fromEntity(machine, condition);
    }

    /**
     * Update machine. A machinery owner can modify only their own machines.
     */
    @Transactional
    public MachineryResponse updateMachine(Long id, String userEmail, MachineryRequest request) {
        Machinery machine = machineryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machinery", "id", id));

        // Strict ownership check
        if (!machine.getOwner().getUser().getEmail().equalsIgnoreCase(userEmail.trim())) {
            throw new AccessDeniedException("Access denied: You can only modify your own machinery.");
        }

        machine.setName(request.getName().trim());
        machine.setCategory(request.getCategory());
        machine.setManufacturer(request.getManufacturer().trim());
        machine.setModel(request.getModel().trim());
        machine.setManufacturingYear(request.getManufacturingYear());
        machine.setCapacity(request.getCapacity().trim());
        machine.setFuelType(request.getFuelType().trim());
        machine.setTransmission(request.getTransmission().trim());
        machine.setLocation(request.getLocation().trim());
        machine.setRentalPricePerDay(request.getRentalPricePerDay());
        machine.setDescription(request.getDescription().trim());
        if (request.getAvailabilityStatus() != null) {
            machine.setAvailabilityStatus(request.getAvailabilityStatus());
        }

        machine = machineryRepository.save(machine);
        var condition = machineConditionService.getLatestCondition(id);
        return MachineryResponse.fromEntity(machine, condition);
    }

    /**
     * Delete machine. A machinery owner can delete only their own machines.
     */
    @Transactional
    public void deleteMachine(Long id, String userEmail) {
        Machinery machine = machineryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machinery", "id", id));

        // Strict ownership check
        if (!machine.getOwner().getUser().getEmail().equalsIgnoreCase(userEmail.trim())) {
            throw new AccessDeniedException("Access denied: You can only delete your own machinery.");
        }

        if (machine.getAvailabilityStatus() == MachineryAvailability.BOOKED) {
            throw new BadRequestException("Cannot delete machine while it is currently BOOKED.");
        }

        machineryRepository.delete(machine);
    }

    /**
     * List all machines owned by the authenticated machinery owner.
     */
    @Transactional(readOnly = true)
    public List<MachineryResponse> getMyMachines(String userEmail, MachineryCategory category, MachineryAvailability availability, String search) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        return machineryOwnerRepository.findByUserId(user.getId())
                .map(owner -> {
                    String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
                    return machineryRepository.searchAndFilterOwnerMachinery(owner.getId(), category, availability, cleanSearch)
                            .stream()
                            .map(MachineryResponse::fromEntity)
                            .collect(Collectors.toList());
                })
                .orElse(Collections.emptyList());
    }

    /**
     * Search and filter machines across the catalog by category, availability, location, and keywords.
     */
    @Transactional(readOnly = true)
    public List<MachineryResponse> searchAndFilterMachines(MachineryCategory category, MachineryAvailability availability, String location, String search) {
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanLocation = (location != null && !location.isBlank()) ? location.trim() : null;

        return machineryRepository.searchAndFilterMachinery(category, availability, cleanLocation, cleanSearch)
                .stream()
                .map(MachineryResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
