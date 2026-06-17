package com.womenconcern.api.auth.controller;


import com.womenconcern.api.auth.dto.*;
import com.womenconcern.api.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {

    private final AuthService authService;

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

    @PreAuthorize("hasRole('ADMIN') or hasRole('EXECUTIVE_DIRECTOR')")
    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<String> resetPassword(@PathVariable String userId) {
        authService.resetPassword(userId);
        return ResponseEntity.ok("Password reset successfully. User must change it at next login.");
    }

    @PostMapping("/{userId}/forgot-password")
    public ResponseEntity<String> forgotPassword(@PathVariable String userId) {
        authService.forgotPassword(userId);
        return ResponseEntity.ok("Password reset email sent to user.");
    }
}