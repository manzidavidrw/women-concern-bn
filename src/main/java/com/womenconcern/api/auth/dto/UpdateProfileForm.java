package com.womenconcern.api.auth.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateProfileForm {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String emergencyContact;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;
}