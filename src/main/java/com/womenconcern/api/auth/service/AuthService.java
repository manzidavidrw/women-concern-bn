package com.womenconcern.api.auth.service;

import com.womenconcern.api.auth.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);

    AuthResponse refresh(RefreshRequest refreshRequest);

    MessageResponse logout(LogoutRequest logoutRequest);

    MessageResponse  createUser(CreateUserRequest request);

    String resetPassword(String userId);

    void forgotPassword(String userId);


    EmployeeProfileResponse updateMyProfile(
            String userId,
            UpdateProfileForm form,
            MultipartFile profilePicture,
            List<MultipartFile> certificates
    );
    EmployeeProfileResponse getMyProfile(String userId);

    List<EmployeeProfileResponse> getAllProfiles();
}
