package com.womenconcern.api.auth.service;

import com.womenconcern.api.auth.dto.UserDto;
import com.womenconcern.api.utils.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    UserDto.FullUser getUserById(UUID id);

    PageResponse<UserDto.FullUser> getAllUsers(UserDto.UserFilter filter, Pageable pageable);

    void activateUser(UUID id);

    void deactivateUser(UUID id);

    void deleteUser(UUID id);
}