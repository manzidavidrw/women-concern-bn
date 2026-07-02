package com.womenconcern.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordDto {

    // ─── Requests ───────────────────────────────────────

    public record ForgotPasswordRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email
    ) {}

    public record ResetPasswordRequest(
            @NotBlank(message = "Token is required")
            String token,

            @NotBlank(message = "New password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            String newPassword,

            @NotBlank(message = "Confirm password is required")
            String confirmPassword
    ) {}

    // ─── Responses ──────────────────────────────────────

    public record ForgotPasswordResponse(
            String message
    ) {}

    public record ResetPasswordResponse(
            String message
    ) {}

    public record ForceResetPasswordResponse(
            String message,
            String email
    ) {}
}