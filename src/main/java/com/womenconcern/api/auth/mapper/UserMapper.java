package com.womenconcern.api.auth.mapper;

import com.womenconcern.api.auth.dto.UserDto;
import com.womenconcern.api.auth.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto.LinkedUser mapToLinkedUser(User user) {
        if (user == null) {
            return null;
        }

        return new UserDto.LinkedUser(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }

    public static UserDto.FullUser mapToFullUser(User user) {
        if (user == null) {
            return null;
        }

        return new UserDto.FullUser(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getJobTitle(),
                user.getAddress(),
                user.getProfilePictureUrl(),
                user.getProfilePictureId(),
                user.getNationalId(),
                user.getEmergencyContact(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getCertificates(),
                user.getJoinedAt(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}