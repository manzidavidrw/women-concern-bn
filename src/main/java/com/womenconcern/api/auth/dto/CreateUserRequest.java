package com.womenconcern.api.auth.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String employeeId;
    private String role;
}
