package com.womenconcern.api.auth.dto;

import com.womenconcern.api.auth.enums.Gender;
import com.womenconcern.api.auth.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProfileResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String emergencyContact;
    private List<String> certificates;
    private LocalDate dateOfBirth;
    private LocalDate joinedAt;
    private Gender gender;
    private UserRole role;
    private String jobTitle;
    private String nationalId;
    private String profilePictureUrl;
    private boolean isActive;
}