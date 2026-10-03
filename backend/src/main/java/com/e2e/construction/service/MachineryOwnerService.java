package com.e2e.construction.service;

import com.e2e.construction.dto.MachineryOwnerProfileRequest;
import com.e2e.construction.dto.MachineryOwnerProfileResponse;
import com.e2e.construction.entity.MachineryOwner;
import com.e2e.construction.entity.User;
import com.e2e.construction.entity.VerificationStatus;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.MachineryOwnerRepository;
import com.e2e.construction.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MachineryOwnerService {

    private final MachineryOwnerRepository machineryOwnerRepository;
    private final UserRepository userRepository;

    public MachineryOwnerService(MachineryOwnerRepository machineryOwnerRepository, UserRepository userRepository) {
        this.machineryOwnerRepository = machineryOwnerRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create machinery owner profile for the authenticated user.
     */
    @Transactional
    public MachineryOwnerProfileResponse createProfile(String userEmail, MachineryOwnerProfileRequest request) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        // Enforce that user has MACHINERY_OWNER role
        if (!user.getRole().getName().equalsIgnoreCase("MACHINERY_OWNER")) {
            throw new BadRequestException("Only users with role MACHINERY_OWNER can create a machinery owner profile. Current role: " + user.getRole().getName());
        }

        // Prevent duplicate profile creation
        if (machineryOwnerRepository.existsByUserId(user.getId())) {
            throw new BadRequestException("Machinery owner profile already exists for this account. Use PUT /api/machinery-owners/profile to update it.");
        }

        MachineryOwner owner = new MachineryOwner();
        owner.setUser(user);
        owner.setCompanyName(request.getCompanyName().trim());
        owner.setTaxId(request.getTaxId() != null ? request.getTaxId().trim() : null);
        owner.setLocation(request.getLocation().trim());
        owner.setAddress(request.getAddress().trim());
        owner.setCity(request.getCity() != null ? request.getCity().trim() : null);
        owner.setState(request.getState() != null ? request.getState().trim() : null);
        owner.setDescription(request.getDescription());
        owner.setProfileImage(request.getProfileImage());
        owner.setVerificationStatus(VerificationStatus.PENDING);

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhoneNumber(request.getPhone().trim());
            userRepository.save(user);
        }

        owner = machineryOwnerRepository.save(owner);
        return MachineryOwnerProfileResponse.fromEntity(owner);
    }

    /**
     * View the authenticated machinery owner's own profile.
     */
    @Transactional(readOnly = true)
    public MachineryOwnerProfileResponse getMyProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        MachineryOwner owner = machineryOwnerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Machinery owner profile not found. Please create your profile first."));

        return MachineryOwnerProfileResponse.fromEntity(owner);
    }

    /**
     * Update the authenticated machinery owner's own profile.
     * Enforces ownership: only modifies the record matching authenticated user's ID.
     */
    @Transactional
    public MachineryOwnerProfileResponse updateMyProfile(String userEmail, MachineryOwnerProfileRequest request) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        MachineryOwner owner = machineryOwnerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Machinery owner profile not found. Please create your profile first."));

        owner.setCompanyName(request.getCompanyName().trim());
        if (request.getTaxId() != null) owner.setTaxId(request.getTaxId().trim());
        owner.setLocation(request.getLocation().trim());
        owner.setAddress(request.getAddress().trim());
        if (request.getCity() != null) owner.setCity(request.getCity().trim());
        if (request.getState() != null) owner.setState(request.getState().trim());
        owner.setDescription(request.getDescription());
        if (request.getProfileImage() != null) owner.setProfileImage(request.getProfileImage().trim());

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhoneNumber(request.getPhone().trim());
            userRepository.save(user);
        }

        owner = machineryOwnerRepository.save(owner);
        return MachineryOwnerProfileResponse.fromEntity(owner);
    }

    /**
     * View machinery owner profile by ID.
     */
    @Transactional(readOnly = true)
    public MachineryOwnerProfileResponse getProfileById(Long id) {
        MachineryOwner owner = machineryOwnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MachineryOwner", "id", id));

        return MachineryOwnerProfileResponse.fromEntity(owner);
    }
}
