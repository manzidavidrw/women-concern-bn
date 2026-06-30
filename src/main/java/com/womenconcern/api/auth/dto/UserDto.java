package com.womenconcern.api.auth.dto;

import com.womenconcern.api.auth.enums.UserRole;

import java.util.UUID;

 public record UserDto (
         UUID id,
         String email,
         String firstName,
         String lastName,
         String phoneNumber,
         UserRole role){}

