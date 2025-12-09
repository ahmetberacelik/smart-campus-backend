package com.smartcampus.auth.service;

import com.smartcampus.auth.dto.request.*;
import com.smartcampus.auth.dto.response.AuthResponse;
import com.smartcampus.auth.dto.response.TokenResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    TokenResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);

    void verifyEmail(VerifyEmailRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void resendVerificationEmail(String email);
}

