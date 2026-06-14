package com.womenconcern.api.auth.service;

import com.womenconcern.api.auth.dto.*;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);

    AuthResponse refresh(RefreshRequest refreshRequest);

    MessageResponse logout(LogoutRequest logoutRequest);
}
