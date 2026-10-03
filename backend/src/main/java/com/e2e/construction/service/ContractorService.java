package com.e2e.construction.service;

import com.e2e.construction.dto.ContractorProfileRequest;
import com.e2e.construction.dto.ContractorProfileResponse;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.User;
import com.e2e.construction.entity.VerificationStatus;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ContractorService {

    private final ContractorRepository contractorRepository;
    private final UserRepository userRepository;

    public ContractorService(ContractorRepository contractorRepository, UserRepository userRepository) {
        this.contractorRepository = contractorRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create contractor profile for the authenticated user.
     */
    @Transactional
    public ContractorProfileResponse createProfile(String userEmail, ContractorProfileRequest request) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        // Enforce that user has CONTRACTOR role
        if (!user.getRole().getName().equalsIgnoreCase("CONTRACTOR")) {
            throw new BadRequestException("Only users with role CONTRACTOR can create a contractor profile. Current role: " + user.getRole().getName());
        }

        // Prevent duplicate profile creation for same user
        if (contractorRepository.existsByUserId(user.getId())) {
            throw new BadRequestException("Contractor profile already exists for this account. Use PUT /api/contractors/profile to update it.");
        }

        Contractor contractor = new Contractor();
        contractor.setUser(user);
        contractor.setCompanyName(request.getCompanyName().trim());
        contractor.setCompanyDescription(request.getCompanyDescription());
        contractor.setAddress(request.getAddress().trim());
        contractor.setCity(request.getCity().trim());
        contractor.setState(request.getState().trim());
        contractor.setPostalCode(request.getPostalCode() != null ? request.getPostalCode().trim() : null);
        contractor.setYearsOfExperience(request.getYearsOfExperience() != null ? request.getYearsOfExperience() : 0);
        contractor.setProfileImage(request.getProfileImage());
        contractor.setLicenseNumber(request.getLicenseNumber() != null ? request.getLicenseNumber().trim() : null);
        contractor.setSpecialization(request.getSpecialization() != null ? request.getSpecialization().trim() : null);
        contractor.setVerificationStatus(VerificationStatus.PENDING);
        contractor.setRating(BigDecimal.ZERO);

        // Update phone if provided
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhoneNumber(request.getPhone().trim());
            userRepository.save(user);
        }

        contractor = contractorRepository.save(contractor);
        return ContractorProfileResponse.fromEntity(contractor);
    }

    /**
     * Retrieve the authenticated contractor's own profile.
     */
    @Transactional(readOnly = true)
    public ContractorProfileResponse getMyProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Contractor contractor = contractorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Contractor profile not found. Please create your profile first."));

        return ContractorProfileResponse.fromEntity(contractor);
    }

    /**
     * Update the authenticated contractor's own profile.
     * Enforces ownership: only modifies the record matching the authenticated user's ID.
     */
    @Transactional
    public ContractorProfileResponse updateMyProfile(String userEmail, ContractorProfileRequest request) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Contractor contractor = contractorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Contractor profile not found. Please create your profile first."));

        contractor.setCompanyName(request.getCompanyName().trim());
        contractor.setCompanyDescription(request.getCompanyDescription());
        contractor.setAddress(request.getAddress().trim());
        contractor.setCity(request.getCity().trim());
        contractor.setState(request.getState().trim());

        if (request.getPostalCode() != null) {
            contractor.setPostalCode(request.getPostalCode().trim());
        }
        if (request.getYearsOfExperience() != null) {
            contractor.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getProfileImage() != null) {
            contractor.setProfileImage(request.getProfileImage().trim());
        }
        if (request.getLicenseNumber() != null) {
            contractor.setLicenseNumber(request.getLicenseNumber().trim());
        }
        if (request.getSpecialization() != null) {
            contractor.setSpecialization(request.getSpecialization().trim());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhoneNumber(request.getPhone().trim());
            userRepository.save(user);
        }

        contractor = contractorRepository.save(contractor);
        return ContractorProfileResponse.fromEntity(contractor);
    }

    /**
     * Get contractor profile by ID (e.g., for public review or client view).
     */
    @Transactional(readOnly = true)
    public ContractorProfileResponse getProfileById(Long id) {
        Contractor contractor = contractorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contractor", "id", id));

        return ContractorProfileResponse.fromEntity(contractor);
    }
}
