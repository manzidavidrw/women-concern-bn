package com.womenconcern.api.auth.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class EmployeeProfileResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String role;

    // Employment details
    private LocalDate dateOfBirth;
    private LocalDate joinedAt;
    private String jobTitle;

    // Extra details
    private String nationalId;
    private String address;
    private String emergencyContact;

    // Certificates
    private List<String> certificates;
}
