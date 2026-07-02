package com.womenconcern.api.auth.controller;

import com.womenconcern.api.auth.dto.UserDto;
import com.womenconcern.api.auth.enums.Gender;
import com.womenconcern.api.auth.enums.UserRole;
import com.womenconcern.api.auth.service.UserService;
import com.womenconcern.api.utils.ApiResponse;
import com.womenconcern.api.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'EXECUTIVE_DIRECTOR')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserDto.FullUser>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        UserDto.UserFilter filter = new UserDto.UserFilter(
                search,
                active,
                gender,
                role
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Users retrieved successfully",
                        userService.getAllUsers(filter, pageable)
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto.FullUser>> getUserById(@PathVariable UUID id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "User found",
                        userService.getUserById(id)
                )
        );
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable UUID id) {

        userService.activateUser(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "User activated successfully",
                        null
                )
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {

        userService.deactivateUser(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "User deactivated successfully",
                        null
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {

        userService.deleteUser(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "User deleted successfully",
                        null
                )
        );
    }
}