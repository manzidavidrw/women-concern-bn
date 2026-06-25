package com.womenconcern.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;   // Required

    @NotBlank(message = "First name is required")
    private String firstName;  // Required

    private String lastName;

    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 digits")
    private String phoneNumber;  // Optional but validated if present

    @NotBlank(message = "Hire date is required")
    private LocalDate JoinedAt;

    @NotBlank(message = "Role is required")
    private String role;   // Required
}
