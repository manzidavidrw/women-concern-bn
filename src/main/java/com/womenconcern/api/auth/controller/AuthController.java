package com.womenconcern.api.auth.controller;


import com.womenconcern.api.auth.dto.*;
import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.auth.repository.UserRepository;
import com.womenconcern.api.auth.service.AuthService;
import com.womenconcern.api.exception.ResourceNotFoundException;
import com.womenconcern.api.utils.ApiResponse;
import com.womenconcern.api.utils.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.womenconcern.api.auth.service.IdCardGenerator;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {

    private final AuthService authService;
    private final IdCardGenerator idCardGenerator;

//    @PublicEndpoint
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

//    @PublicEndpoint
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest refreshRequest) {
        return ResponseEntity.ok(authService.refresh(refreshRequest));
    }

//    @PublicEndpoint
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestBody LogoutRequest logoutRequest) {
        return ResponseEntity.ok(authService.logout(logoutRequest));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EXECUTIVE_DIRECTOR')")
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> createUser(
            @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(authService.createUser(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<PasswordDto.ResetPasswordResponse>> resetPassword(
            @RequestBody @Valid PasswordDto.ResetPasswordRequest request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Password reset successfully",
                        authService.resetPassword(request)
                )
        );
    }

    @PostMapping("/{userId}/force-reset-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'EXECUTIVE_DIRECTOR')")
    public ResponseEntity<ApiResponse<PasswordDto.ForceResetPasswordResponse>> forceResetPassword(
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Force reset completed",
                        authService.forceResetPassword(userId)
                )
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<PasswordDto.ForgotPasswordResponse>> forgotPassword(
            @RequestBody @Valid PasswordDto.ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Request processed",
                        authService.forgotPassword(request.email())
                )
        );
    }

    @PutMapping(
            value = "/me",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmployeeProfileResponse> updateMyProfile(
            @ParameterObject @ModelAttribute UpdateProfileForm form,
            @RequestPart(value = "profilePicture", required = false)
            MultipartFile profilePicture,
            @RequestPart(value = "certificates", required = false)
            List<MultipartFile> certificates
    ) {
        User user = AuthUtils.getCurrentUser();
        return ResponseEntity.ok(
                authService.updateMyProfile(user.getId().toString(), form, profilePicture, certificates)
        );
    }
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmployeeProfileResponse> getMyProfile() {
        User user = AuthUtils.getCurrentUser();
        return ResponseEntity.ok(authService.getMyProfile(user.getId().toString()));
    }

    @GetMapping("/profiles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXECUTIVE_DIRECTOR')")
    public ResponseEntity<List<EmployeeProfileResponse>> getAllProfiles() {
        return ResponseEntity.ok(authService.getAllProfiles());
    }
    @GetMapping("/{userId}/id-card")
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "hasRole('EXECUTIVE_DIRECTOR') or " +
                    "#userId == authentication.name"
    )
    public ResponseEntity<byte[]> downloadIdCard(@PathVariable String userId) throws Exception {

        byte[] pdf = authService.generateIdCard(userId);  // ← use service, not repo directly

        // derive filename from the userId — service impl already fetches the user
        String filename = "wc-id-card-" + userId.substring(0, 8) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdf);
    }

}