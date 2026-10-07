package com.e2e.construction.service;

import com.e2e.construction.dto.AuthResponse;
import com.e2e.construction.dto.LoginRequest;
import com.e2e.construction.dto.RegisterRequest;
import com.e2e.construction.dto.UserProfileResponse;
import com.e2e.construction.entity.Role;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.RoleRepository;
import com.e2e.construction.repository.UserRepository;
import com.e2e.construction.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuthService {

    private static final Set<String> ALLOWED_ROLES = Set.of(
            "ADMIN",
            "CONTRACTOR",
            "SITE_MANAGER",
            "LABORER",
            "MACHINERY_OWNER",
            "MATERIAL_SUPPLIER",
            "CLIENT"
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Registers a new user with BCrypt hashed password and assigned system role.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // 1. Prevent duplicate email & phone registration
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered: " + email);
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty() && userRepository.existsByPhoneNumber(request.getPhoneNumber().trim())) {
            throw new BadRequestException("Phone number is already registered: " + request.getPhoneNumber());
        }

        // 2. Validate and resolve user role
        String roleName = request.getRole().trim().toUpperCase();
        if (roleName.startsWith("ROLE_")) {
            roleName = roleName.substring(5);
        }

        if (!ALLOWED_ROLES.contains(roleName)) {
            throw new BadRequestException("Invalid role: '" + request.getRole() + "'. Allowed roles are: " + ALLOWED_ROLES);
        }

        final String finalRoleName = roleName;
        Role role = roleRepository.findByName(finalRoleName)
                .orElseGet(() -> roleRepository.save(new Role(finalRoleName, finalRoleName + " role")));

        // 3. Hash password using BCrypt
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // 4. Persist user entity
        User user = new User(
                role,
                email,
                passwordHash,
                request.getFirstName().trim(),
                request.getLastName().trim(),
                request.getPhoneNumber().trim()
        );
        user = userRepository.save(user);

        // 5. Generate JWT token
        String token = tokenProvider.generateTokenFromUsername(user.getEmail(), user.getId(), user.getRole().getName());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().getName(),
                "User registered successfully"
        );
    }

    /**
     * Authenticates user credentials and returns JWT token.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // 1. Authenticate credentials via Spring Security AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        // 2. Fetch authenticated user details
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!user.isActive()) {
            throw new BadRequestException("Account is currently deactivated. Please contact support.");
        }

        // 3. Generate JWT
        String token = tokenProvider.generateToken(authentication, user.getId(), user.getRole().getName());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().getName(),
                "Login successful"
        );
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole().getName(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
