package com.smartcampus.auth.controller;

import com.smartcampus.auth.dto.request.*;
import com.smartcampus.auth.dto.response.ApiResponse;
import com.smartcampus.auth.dto.response.AuthResponse;
import com.smartcampus.auth.dto.response.TokenResponse;
import com.smartcampus.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Kimlik doğrulama işlemleri")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Kullanıcı kaydı", description = "Yeni öğrenci veya öğretim üyesi kaydı oluşturur")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kayıt başarılı. Lütfen email adresinizi doğrulayın.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Kullanıcı girişi", description = "Email ve şifre ile giriş yapar")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Giriş başarılı", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Token yenileme", description = "Refresh token ile yeni access token alır")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        TokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token yenilendi", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Çıkış yapma", description = "Refresh token'ı geçersiz kılar")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Çıkış başarılı"));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Email doğrulama", description = "Email doğrulama token'ı ile hesabı aktifleştirir")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Email başarıyla doğrulandı"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Şifre sıfırlama isteği", description = "Şifre sıfırlama linki gönderir")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Eğer bu email adresi kayıtlı ise şifre sıfırlama linki gönderildi"
        ));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Şifre sıfırlama", description = "Token ile yeni şifre belirler")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Şifre başarıyla değiştirildi"));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Doğrulama emaili tekrar gönder", description = "Doğrulama emailini tekrar gönderir")
    public ResponseEntity<ApiResponse<Void>> resendVerificationEmail(
            @RequestParam String email
    ) {
        authService.resendVerificationEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Doğrulama emaili gönderildi"));
    }
}

