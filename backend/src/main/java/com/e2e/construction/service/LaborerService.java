package com.e2e.construction.service;

import com.e2e.construction.dto.LaborerProfileRequest;
import com.e2e.construction.dto.LaborerProfileResponse;
import com.e2e.construction.entity.Laborer;
import com.e2e.construction.entity.LaborerAvailability;
import com.e2e.construction.entity.User;
import com.e2e.construction.entity.VerificationStatus;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.LaborerRepository;
import com.e2e.construction.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LaborerService {

    private final LaborerRepository laborerRepository;
    private final UserRepository userRepository;

    public LaborerService(LaborerRepository laborerRepository, UserRepository userRepository) {
        this.laborerRepository = laborerRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create laborer profile for the authenticated user.
     */
    @Transactional
    public LaborerProfileResponse createProfile(String userEmail, LaborerProfileRequest request) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        // Enforce that user has LABORER role
        if (!user.getRole().getName().equalsIgnoreCase("LABORER")) {
            throw new BadRequestException("Only users with role LABORER can create a laborer profile. Current role: " + user.getRole().getName());
        }

        // Prevent duplicate profile creation
        if (laborerRepository.existsByUserId(user.getId())) {
            throw new BadRequestException("Laborer profile already exists for this account. Use PUT /api/laborers/profile to update it.");
        }

        Laborer laborer = new Laborer();
        laborer.setUser(user);
        laborer.setLocation(request.getLocation().trim());
        laborer.setCity(request.getCity() != null ? request.getCity().trim() : null);
        laborer.setState(request.getState() != null ? request.getState().trim() : null);
        laborer.setSkills(request.getSkills().trim());
        laborer.setYearsOfExperience(request.getYearsOfExperience() != null ? request.getYearsOfExperience() : 0);
        laborer.setDailyWage(request.getDailyWage());
        laborer.setDescription(request.getDescription());
        laborer.setProfileImage(request.getProfileImage());
        laborer.setAvailabilityStatus(request.getAvailabilityStatus() != null ? request.getAvailabilityStatus() : LaborerAvailability.AVAILABLE);
        laborer.setVerificationStatus(VerificationStatus.PENDING);

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhoneNumber(request.getPhone().trim());
            userRepository.save(user);
        }

        laborer = laborerRepository.save(laborer);
        return LaborerProfileResponse.fromEntity(laborer);
    }

    /**
     * View the authenticated laborer's own profile.
     */
    @Transactional(readOnly = true)
    public LaborerProfileResponse getMyProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Laborer laborer = laborerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Laborer profile not found. Please create your profile first."));

        return LaborerProfileResponse.fromEntity(laborer);
    }

    /**
     * Update the authenticated laborer's own profile.
     * Enforces ownership: only modifies the record matching authenticated user's ID.
     */
    @Transactional
    public LaborerProfileResponse updateMyProfile(String userEmail, LaborerProfileRequest request) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Laborer laborer = laborerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Laborer profile not found. Please create your profile first."));

        laborer.setLocation(request.getLocation().trim());
        if (request.getCity() != null) laborer.setCity(request.getCity().trim());
        if (request.getState() != null) laborer.setState(request.getState().trim());
        laborer.setSkills(request.getSkills().trim());
        if (request.getYearsOfExperience() != null) laborer.setYearsOfExperience(request.getYearsOfExperience());
        if (request.getDailyWage() != null) laborer.setDailyWage(request.getDailyWage());
        laborer.setDescription(request.getDescription());
        if (request.getProfileImage() != null) laborer.setProfileImage(request.getProfileImage().trim());
        if (request.getAvailabilityStatus() != null) laborer.setAvailabilityStatus(request.getAvailabilityStatus());

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhoneNumber(request.getPhone().trim());
            userRepository.save(user);
        }

        laborer = laborerRepository.save(laborer);
        return LaborerProfileResponse.fromEntity(laborer);
    }

    /**
     * Update the authenticated laborer's availability status (AVAILABLE, WORKING, UNAVAILABLE).
     */
    @Transactional
    public LaborerProfileResponse updateAvailability(String userEmail, LaborerAvailability availabilityStatus) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Laborer laborer = laborerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Laborer profile not found. Please create your profile first."));

        laborer.setAvailabilityStatus(availabilityStatus);
        laborer = laborerRepository.save(laborer);
        return LaborerProfileResponse.fromEntity(laborer);
    }

    /**
     * View laborer profile by ID.
     */
    @Transactional(readOnly = true)
    public LaborerProfileResponse getProfileById(Long id) {
        Laborer laborer = laborerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Laborer", "id", id));

        return LaborerProfileResponse.fromEntity(laborer);
    }
}
