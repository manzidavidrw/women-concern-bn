package com.womenconcern.api.auth.service.impl;

import com.womenconcern.api.auth.dto.*;
import com.womenconcern.api.auth.entity.RefreshToken;
import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.auth.enums.UserRole;
import com.womenconcern.api.auth.repository.RefreshTokenRepository;
import com.womenconcern.api.auth.repository.UserRepository;
import com.womenconcern.api.auth.service.AuthService;
import com.womenconcern.api.exception.BadRequestException;
import com.womenconcern.api.exception.ConflictException;
import com.womenconcern.api.exception.ResourceNotFoundException;
import com.womenconcern.api.exception.UnauthorizedException;
import com.womenconcern.api.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository         userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService             jwtService;
    private final PasswordEncoder        passwordEncoder;
    private final AuthenticationManager  authenticationManager;
    private final EmailService           emailService;

    /** Refresh-token TTL: 7 days */
    private static final long REFRESH_TTL_SECONDS = 7L * 24 * 60 * 60;

    // ── Login ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is disabled. Contact your administrator.");
        }

        // Revoke previous sessions (single-device policy; remove if multi-device needed)
        refreshTokenRepository.revokeAllByUser(user);

        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirySeconds())
                .userId(user.getId().toString())
                .email(user.getEmail())
                .role(user.getRole().name())
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    // ── Refresh ───────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        RefreshToken stored = refreshTokenRepository.findByToken(req.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (stored.isRevoked() || stored.isExpired()) {
            throw new UnauthorizedException("Refresh token has expired or been revoked. Please log in again.");
        }

        User user = stored.getUser();

        // Rotate: revoke the old one, issue a new pair
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String newAccessToken  = jwtService.generateAccessToken(user);
        String newRefreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirySeconds())
                .userId(user.getId().toString())
                .email(user.getEmail())
                .role(user.getRole().name())
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    // ── Logout ────────────────────────────────────────────────────

    @Override
    @Transactional
    public MessageResponse logout(LogoutRequest req) {
        refreshTokenRepository.findByToken(req.getRefreshToken())
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
        return new MessageResponse("Logged out successfully");
    }

    // ── Create user ───────────────────────────────────────────────

    @Override
    @Transactional
    public MessageResponse createUser(CreateUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("A user with email " + req.getEmail() + " already exists.");
        }

        UserRole role;
        try {
            role = UserRole.valueOf(req.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown role: " + req.getRole() +
                    ". Valid values: ADMIN, EXECUTIVE_DIRECTOR, MANAGER, STAFF");
        }

        // Generate a temporary password; user must change it on first login
        String tempPassword = generateTempPassword();

        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .phoneNumber(req.getPhoneNumber())
                .role(role)
                .joinedAt(req.getJoinedAt())
                .isActive(true)
                .mustChangePassword(true)
                .build();

        userRepository.save(user);

        // Send welcome email with temp password
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName(), tempPassword);
            log.info("Welcome email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.warn("User created but welcome email failed for {}: {}", user.getEmail(), e.getMessage());
        }

        return new MessageResponse(
                "User account created for " + req.getEmail() +
                        " with role " + role.name() + ". A welcome email with login credentials has been sent."
        );
    }

    // ── Reset password (admin-triggered) ─────────────────────────

    @Override
    @Transactional
    public String resetPassword(String userId) {
        User user = findUserById(userId);
        String newPassword = generateTempPassword();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUser(user);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), newPassword);
        } catch (Exception e) {
            log.warn("Password reset email failed for {}: {}", user.getEmail(), e.getMessage());
        }

        return "Password reset successfully for " + user.getEmail();
    }

    // ── Forgot password (self-service) ────────────────────────────

    @Override
    @Transactional
    public void forgotPassword(String userId) {
        User user = findUserById(userId);
        String newPassword = generateTempPassword();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUser(user);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), newPassword);
    }

    // ── Profile ───────────────────────────────────────────────────

    @Override
    @Transactional
    public EmployeeProfileResponse updateMyProfile(String userId, UpdateProfileRequest req) {
        User user = findUserById(userId);

        if (req.getFirstName()        != null) user.setFirstName(req.getFirstName());
        if (req.getLastName()         != null) user.setLastName(req.getLastName());
        if (req.getPhoneNumber()      != null) user.setPhoneNumber(req.getPhoneNumber());
        if (req.getAddress()          != null) user.setAddress(req.getAddress());
        if (req.getEmergencyContact() != null) user.setEmergencyContact(req.getEmergencyContact());
        if (req.getDateOfBirth()      != null) user.setDateOfBirth(req.getDateOfBirth());
        if (req.getCertificates()     != null) {
            user.setCertificates(String.join(",", req.getCertificates()));
        }

        return toProfileResponse(userRepository.save(user));
    }

    @Override
    public EmployeeProfileResponse getMyProfile(String userId) {
        return toProfileResponse(findUserById(userId));
    }

    @Override
    public List<EmployeeProfileResponse> getAllProfiles() {
        return userRepository.findAll().stream()
                .map(this::toProfileResponse)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private User findUserById(String userId) {
        try {
            UUID uuid = UUID.fromString(userId);
            return userRepository.findById(uuid)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid user ID format: " + userId);
        }
    }

    private String createRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString() + "-" + UUID.randomUUID();
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(Instant.now().plusSeconds(REFRESH_TTL_SECONDS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(rt);
        return tokenValue;
    }

    private String generateTempPassword() {
        // 8 random chars — readable but not guessable
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    private EmployeeProfileResponse toProfileResponse(User user) {
        EmployeeProfileResponse resp = new EmployeeProfileResponse();
        resp.setEmail(user.getEmail());
        resp.setFirstName(user.getFirstName());
        resp.setLastName(user.getLastName());
        resp.setPhoneNumber(user.getPhoneNumber());
        resp.setRole(user.getRole().name());
        resp.setJobTitle(user.getJobTitle());
        resp.setAddress(user.getAddress());
        resp.setNationalId(user.getNationalId());
        resp.setEmergencyContact(user.getEmergencyContact());
        resp.setDateOfBirth(user.getDateOfBirth());
        resp.setJoinedAt(user.getJoinedAt());

        String certs = user.getCertificates();
        resp.setCertificates(
                (certs == null || certs.isBlank())
                        ? List.of()
                        : Arrays.asList(certs.split(","))
        );
        return resp;
    }
}
