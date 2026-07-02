package com.womenconcern.api.auth.dto;

import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.auth.enums.Gender;
import com.womenconcern.api.auth.enums.UserRole;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class UserDto {

     public record LinkedUser(
             UUID id,
             String email,
             String firstName,
             String lastName,
             String phoneNumber,
             UserRole role
     ) {}

     public record FullUser(
             UUID id,
             String email,
             String firstName,
             String lastName,
             String phoneNumber,
             UserRole role,
             String jobTitle,
             String address,
             String profilePictureUrl,
             String profilePictureId,
             String nationalId,
             String emergencyContact,
             LocalDate dateOfBirth,
             Gender gender,
             String certificates,
             LocalDate joinedAt,
             boolean isActive,
             Instant createdAt,
             Instant updatedAt
     ) {}

     public record UserFilter(
             String search,
             Boolean active,
             Gender gender,
             UserRole role
     ) {}

}
