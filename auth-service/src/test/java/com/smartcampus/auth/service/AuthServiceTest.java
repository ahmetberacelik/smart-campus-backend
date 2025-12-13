package com.smartcampus.auth.service;

import com.smartcampus.auth.dto.request.*;
import com.smartcampus.auth.dto.response.AuthResponse;
import com.smartcampus.auth.dto.response.TokenResponse;
import com.smartcampus.auth.entity.*;
import com.smartcampus.auth.exception.*;
import com.smartcampus.auth.repository.*;
import com.smartcampus.auth.security.JwtTokenProvider;
import com.smartcampus.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    // Test verileri
    private User testUser;
    private Department testDepartment;
    private RegisterRequest studentRegisterRequest;
    private RegisterRequest facultyRegisterRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Test Department
        testDepartment = Department.builder()
                .id(1L)
                .name("Bilgisayar Mühendisliği")
                .code("CSE")
                .build();

        // Test User
        testUser = User.builder()
                .id(1L)
                .email("test@smartcampus.edu.tr")
                .passwordHash("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.STUDENT)
                .isVerified(true)
                .isActive(true)
                .build();

        // Student Register Request
        studentRegisterRequest = RegisterRequest.builder()
                .email("student@smartcampus.edu.tr")
                .password("Test123!")
                .firstName("Öğrenci")
                .lastName("Test")
                .role(Role.STUDENT)
                .departmentId(1L)
                .studentNumber("20210001")
                .build();

        // Faculty Register Request
        facultyRegisterRequest = RegisterRequest.builder()
                .email("faculty@smartcampus.edu.tr")
                .password("Test123!")
                .firstName("Öğretim")
                .lastName("Üyesi")
                .role(Role.FACULTY)
                .departmentId(1L)
                .employeeNumber("EMP001")
                .title("Dr. Öğr. Üyesi")
                .build();

        // Login Request
        loginRequest = LoginRequest.builder()
                .email("test@smartcampus.edu.tr")
                .password("Test123!")
                .build();
    }

    // ==================== REGISTER TESTS ====================

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Öğrenci kaydı başarılı olmalı")
        void register_StudentSuccess() {
            // Given
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));
            when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });
            when(studentRepository.existsByStudentNumber(anyString())).thenReturn(false);
            when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(emailVerificationTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtTokenProvider.generateAccessToken(anyString())).thenReturn("accessToken");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyString());

            // When
            AuthResponse response = authService.register(studentRegisterRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("accessToken");
            assertThat(response.getUser().getEmail()).isEqualTo(studentRegisterRequest.getEmail());
            assertThat(response.getUser().getRole()).isEqualTo(Role.STUDENT);

            verify(userRepository).save(any(User.class));
            verify(studentRepository).save(any(Student.class));
            verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Öğretim üyesi kaydı başarılı olmalı")
        void register_FacultySuccess() {
            // Given
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));
            when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });
            when(facultyRepository.existsByEmployeeNumber(anyString())).thenReturn(false);
            when(facultyRepository.save(any(Faculty.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(emailVerificationTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtTokenProvider.generateAccessToken(anyString())).thenReturn("accessToken");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyString());

            // When
            AuthResponse response = authService.register(facultyRegisterRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUser().getRole()).isEqualTo(Role.FACULTY);

            verify(facultyRepository).save(any(Faculty.class));
        }

        @Test
        @DisplayName("Admin kaydı engellenmeli")
        void register_AdminNotAllowed() {
            // Given
            RegisterRequest adminRequest = RegisterRequest.builder()
                    .email("admin@smartcampus.edu.tr")
                    .password("Test123!")
                    .firstName("Admin")
                    .lastName("Test")
                    .role(Role.ADMIN)
                    .departmentId(1L)
                    .build();

            // When & Then
            assertThatThrownBy(() -> authService.register(adminRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Admin kaydı yapılamaz");
        }

        @Test
        @DisplayName("Email zaten kayıtlı ve doğrulanmış ise hata vermeli")
        void register_EmailAlreadyExistsAndVerified() {
            // Given
            testUser.setIsVerified(true);
            when(userRepository.existsByEmail(studentRegisterRequest.getEmail())).thenReturn(true);
            when(userRepository.findByEmail(studentRegisterRequest.getEmail())).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> authService.register(studentRegisterRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Bu email adresi zaten kayıtlı");
        }

        @Test
        @DisplayName("Öğrenci numarası zorunlu olmalı")
        void register_StudentNumberRequired() {
            // Given
            studentRegisterRequest.setStudentNumber(null);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));

            // When & Then
            assertThatThrownBy(() -> authService.register(studentRegisterRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Öğrenci numarası zorunludur");
        }

        @Test
        @DisplayName("Sicil numarası zorunlu olmalı")
        void register_EmployeeNumberRequired() {
            // Given
            facultyRegisterRequest.setEmployeeNumber(null);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));

            // When & Then
            assertThatThrownBy(() -> authService.register(facultyRegisterRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Sicil numarası zorunludur");
        }

        @Test
        @DisplayName("Unvan zorunlu olmalı")
        void register_TitleRequired() {
            // Given
            facultyRegisterRequest.setTitle(null);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));

            // When & Then
            assertThatThrownBy(() -> authService.register(facultyRegisterRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Unvan zorunludur");
        }

        @Test
        @DisplayName("Bölüm bulunamazsa hata vermeli")
        void register_DepartmentNotFound() {
            // Given
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.register(studentRegisterRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Öğrenci numarası zaten varsa hata vermeli")
        void register_StudentNumberAlreadyExists() {
            // Given
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));
            when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });
            when(studentRepository.existsByStudentNumber(anyString())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(studentRegisterRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Bu öğrenci numarası zaten kayıtlı");
        }
    }

    // ==================== LOGIN TESTS ====================

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Giriş başarılı olmalı")
        void login_Success() {
            // Given
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
            when(jwtTokenProvider.generateAccessToken(authentication)).thenReturn("accessToken");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            AuthResponse response = authService.login(loginRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("accessToken");
            assertThat(response.getRefreshToken()).isNotNull();
            assertThat(response.getUser().getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Hatalı şifre ile giriş başarısız olmalı")
        void login_BadCredentials() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // When & Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("Devre dışı hesap ile giriş engellenmeli")
        void login_AccountDisabled() {
            // Given
            testUser.setIsActive(false);
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Hesabınız devre dışı bırakılmış");
        }
    }

    // ==================== REFRESH TOKEN TESTS ====================

    @Nested
    @DisplayName("Refresh Token Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Token yenileme başarılı olmalı")
        void refreshToken_Success() {
            // Given
            RefreshToken refreshToken = RefreshToken.builder()
                    .id(1L)
                    .token("valid-refresh-token")
                    .user(testUser)
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .build();

            RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

            when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(refreshToken));
            doNothing().when(refreshTokenRepository).delete(refreshToken);
            when(jwtTokenProvider.generateAccessToken(testUser.getEmail())).thenReturn("newAccessToken");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            TokenResponse response = authService.refreshToken(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
            assertThat(response.getRefreshToken()).isNotNull();

            verify(refreshTokenRepository).delete(refreshToken);
        }

        @Test
        @DisplayName("Geçersiz refresh token ile hata vermeli")
        void refreshToken_InvalidToken() {
            // Given
            RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");
            when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(TokenException.class);
        }

        @Test
        @DisplayName("Süresi dolmuş refresh token ile hata vermeli")
        void refreshToken_ExpiredToken() {
            // Given
            RefreshToken expiredToken = RefreshToken.builder()
                    .id(1L)
                    .token("expired-token")
                    .user(testUser)
                    .expiryDate(LocalDateTime.now().minusDays(1)) // Süresi dolmuş
                    .build();

            RefreshTokenRequest request = new RefreshTokenRequest("expired-token");
            when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

            // When & Then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(TokenException.class);

            verify(refreshTokenRepository).delete(expiredToken);
        }
    }

    // ==================== LOGOUT TESTS ====================

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Çıkış başarılı olmalı")
        void logout_Success() {
            // Given
            String refreshToken = "valid-refresh-token";
            doNothing().when(refreshTokenRepository).deleteByToken(refreshToken);

            // When
            authService.logout(refreshToken);

            // Then
            verify(refreshTokenRepository).deleteByToken(refreshToken);
        }
    }

    // ==================== VERIFY EMAIL TESTS ====================

    @Nested
    @DisplayName("Verify Email Tests")
    class VerifyEmailTests {

        @Test
        @DisplayName("Email doğrulama başarılı olmalı")
        void verifyEmail_Success() {
            // Given
            EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                    .id(1L)
                    .token("valid-verification-token")
                    .user(testUser)
                    .expiryDate(LocalDateTime.now().plusHours(24))
                    .build();

            VerifyEmailRequest request = new VerifyEmailRequest("valid-verification-token");

            when(emailVerificationTokenRepository.findByToken("valid-verification-token"))
                    .thenReturn(Optional.of(verificationToken));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            doNothing().when(emailVerificationTokenRepository).deleteAllByUser(testUser);
            doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

            // When
            authService.verifyEmail(request);

            // Then
            verify(userRepository).save(testUser);
            assertThat(testUser.getIsVerified()).isTrue();
        }

        @Test
        @DisplayName("Geçersiz doğrulama token'ı ile hata vermeli")
        void verifyEmail_InvalidToken() {
            // Given
            VerifyEmailRequest request = new VerifyEmailRequest("invalid-token");
            when(emailVerificationTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.verifyEmail(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Geçersiz doğrulama linki");
        }

        @Test
        @DisplayName("Süresi dolmuş doğrulama token'ı ile hata vermeli")
        void verifyEmail_ExpiredToken() {
            // Given
            EmailVerificationToken expiredToken = EmailVerificationToken.builder()
                    .id(1L)
                    .token("expired-token")
                    .user(testUser)
                    .expiryDate(LocalDateTime.now().minusHours(1))
                    .build();

            VerifyEmailRequest request = new VerifyEmailRequest("expired-token");
            when(emailVerificationTokenRepository.findByToken("expired-token"))
                    .thenReturn(Optional.of(expiredToken));

            // When & Then
            assertThatThrownBy(() -> authService.verifyEmail(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Doğrulama linki süresi dolmuş");

            verify(emailVerificationTokenRepository).delete(expiredToken);
        }
    }

    // ==================== FORGOT PASSWORD TESTS ====================

    @Nested
    @DisplayName("Forgot Password Tests")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Şifre sıfırlama isteği başarılı olmalı")
        void forgotPassword_Success() {
            // Given
            ForgotPasswordRequest request = new ForgotPasswordRequest("test@smartcampus.edu.tr");

            when(userRepository.findByEmail("test@smartcampus.edu.tr")).thenReturn(Optional.of(testUser));
            doNothing().when(passwordResetTokenRepository).deleteAllByUser(testUser);
            when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

            // When
            authService.forgotPassword(request);

            // Then
            verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
            verify(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Var olmayan email için sessizce işlem yapmalı")
        void forgotPassword_NonExistentEmail() {
            // Given
            ForgotPasswordRequest request = new ForgotPasswordRequest("nonexistent@smartcampus.edu.tr");
            when(userRepository.findByEmail("nonexistent@smartcampus.edu.tr")).thenReturn(Optional.empty());

            // When
            authService.forgotPassword(request);

            // Then - Güvenlik için sessizce geç
            verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Devre dışı hesap için email göndermemeli")
        void forgotPassword_InactiveAccount() {
            // Given
            testUser.setIsActive(false);
            ForgotPasswordRequest request = new ForgotPasswordRequest("test@smartcampus.edu.tr");
            when(userRepository.findByEmail("test@smartcampus.edu.tr")).thenReturn(Optional.of(testUser));

            // When
            authService.forgotPassword(request);

            // Then
            verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
        }
    }

    // ==================== RESET PASSWORD TESTS ====================

    @Nested
    @DisplayName("Reset Password Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Şifre sıfırlama başarılı olmalı")
        void resetPassword_Success() {
            // Given
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .id(1L)
                    .token("valid-reset-token")
                    .user(testUser)
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .build();

            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("valid-reset-token")
                    .newPassword("NewPass123!")
                    .build();

            when(passwordResetTokenRepository.findByToken("valid-reset-token"))
                    .thenReturn(Optional.of(resetToken));
            when(passwordEncoder.encode("NewPass123!")).thenReturn("hashedNewPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            doNothing().when(refreshTokenRepository).deleteAllByUser(testUser);
            doNothing().when(passwordResetTokenRepository).deleteAllByUser(testUser);

            // When
            authService.resetPassword(request);

            // Then
            verify(userRepository).save(testUser);
            verify(refreshTokenRepository).deleteAllByUser(testUser);
            verify(passwordResetTokenRepository).deleteAllByUser(testUser);
        }

        @Test
        @DisplayName("Geçersiz reset token ile hata vermeli")
        void resetPassword_InvalidToken() {
            // Given
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("invalid-token")
                    .newPassword("NewPass123!")
                    .build();

            when(passwordResetTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Geçersiz şifre sıfırlama linki");
        }

        @Test
        @DisplayName("Süresi dolmuş reset token ile hata vermeli")
        void resetPassword_ExpiredToken() {
            // Given
            PasswordResetToken expiredToken = PasswordResetToken.builder()
                    .id(1L)
                    .token("expired-token")
                    .user(testUser)
                    .expiryDate(LocalDateTime.now().minusHours(1))
                    .build();

            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("expired-token")
                    .newPassword("NewPass123!")
                    .build();

            when(passwordResetTokenRepository.findByToken("expired-token"))
                    .thenReturn(Optional.of(expiredToken));

            // When & Then
            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Şifre sıfırlama linki süresi dolmuş");

            verify(passwordResetTokenRepository).delete(expiredToken);
        }
    }

    // ==================== RESEND VERIFICATION EMAIL TESTS ====================

    @Nested
    @DisplayName("Resend Verification Email Tests")
    class ResendVerificationEmailTests {

        @Test
        @DisplayName("Doğrulama emaili tekrar göndermeli")
        void resendVerificationEmail_Success() {
            // Given
            testUser.setIsVerified(false);
            when(userRepository.findByEmail("test@smartcampus.edu.tr")).thenReturn(Optional.of(testUser));
            doNothing().when(emailVerificationTokenRepository).deleteAllByUser(testUser);
            when(emailVerificationTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyString());

            // When
            authService.resendVerificationEmail("test@smartcampus.edu.tr");

            // Then
            verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Zaten doğrulanmış email için hata vermeli")
        void resendVerificationEmail_AlreadyVerified() {
            // Given
            testUser.setIsVerified(true);
            when(userRepository.findByEmail("test@smartcampus.edu.tr")).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> authService.resendVerificationEmail("test@smartcampus.edu.tr"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Email zaten doğrulanmış");
        }

        @Test
        @DisplayName("Var olmayan email için hata vermeli")
        void resendVerificationEmail_UserNotFound() {
            // Given
            when(userRepository.findByEmail("nonexistent@smartcampus.edu.tr")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.resendVerificationEmail("nonexistent@smartcampus.edu.tr"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}


