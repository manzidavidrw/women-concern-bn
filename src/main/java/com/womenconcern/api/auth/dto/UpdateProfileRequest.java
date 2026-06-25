package com.womenconcern.api.auth.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String emergencyContact;
    private List<String> certificates;
    private LocalDate dateOfBirth;
}
