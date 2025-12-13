package com.smartcampus.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.auth.dto.request.*;
import com.smartcampus.auth.entity.*;
import com.smartcampus.auth.repository.*;
import com.smartcampus.auth.service.EmailService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    private Department testDepartment;

    @BeforeEach
    void setUp() {
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        testDepartment = Department.builder()
                .name("Bilgisayar Mühendisliği")
                .code("CSE")
                .facultyName("Mühendislik Fakültesi")
                .build();
        testDepartment = departmentRepository.save(testDepartment);
    }

    @Nested
    @DisplayName("POST /api/v1/auth/register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Öğrenci kaydı başarılı olmalı - 201 Created")
        void register_StudentSuccess_Returns201() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email("student@smartcampus.edu.tr")
                    .password("Test123!")
                    .firstName("Test")
                    .lastName("Student")
                    .role(Role.STUDENT)
                    .departmentId(testDepartment.getId())
                    .studentNumber("20210001")
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.user.email").value("student@smartcampus.edu.tr"))
                    .andExpect(jsonPath("$.data.user.role").value("STUDENT"));
        }

        @Test
        @DisplayName("Öğretim üyesi kaydı başarılı olmalı - 201 Created")
        void register_FacultySuccess_Returns201() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email("faculty@smartcampus.edu.tr")
                    .password("Test123!")
                    .firstName("Test")
                    .lastName("Faculty")
                    .role(Role.FACULTY)
                    .departmentId(testDepartment.getId())
                    .employeeNumber("EMP001")
                    .title("Dr. Öğr. Üyesi")
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.user.role").value("FACULTY"));
        }

        @Test
        @DisplayName("Email zaten kayıtlı ise - 409 Conflict")
        void register_EmailAlreadyExists_Returns409() throws Exception {
            User existingUser = User.builder()
                    .email("existing@smartcampus.edu.tr")
                    .passwordHash(passwordEncoder.encode("Test123!"))
                    .firstName("Existing")
                    .lastName("User")
                    .role(Role.STUDENT)
                    .isVerified(true)
                    .isActive(true)
                    .build();
            userRepository.save(existingUser);

            RegisterRequest request = RegisterRequest.builder()
                    .email("existing@smartcampus.edu.tr")
                    .password("Test123!")
                    .firstName("New")
                    .lastName("User")
                    .role(Role.STUDENT)
                    .departmentId(testDepartment.getId())
                    .studentNumber("20210002")
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Geçersiz email formatı - 400 Bad Request")
        void register_InvalidEmail_Returns400() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email("invalid-email")
                    .password("Test123!")
                    .firstName("Test")
                    .lastName("User")
                    .role(Role.STUDENT)
                    .departmentId(testDepartment.getId())
                    .studentNumber("20210003")
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Zayıf şifre - 400 Bad Request")
        void register_WeakPassword_Returns400() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email("test@smartcampus.edu.tr")
                    .password("weak")
                    .firstName("Test")
                    .lastName("User")
                    .role(Role.STUDENT)
                    .departmentId(testDepartment.getId())
                    .studentNumber("20210004")
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Admin kaydı engellenmeli - 400 Bad Request")
        void register_AdminNotAllowed_Returns400() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email("admin@smartcampus.edu.tr")
                    .password("Test123!")
                    .firstName("Admin")
                    .lastName("User")
                    .role(Role.ADMIN)
                    .departmentId(testDepartment.getId())
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login Tests")
    class LoginTests {

        private User testUser;

        @BeforeEach
        void setUp() {
            testUser = User.builder()
                    .email("login@smartcampus.edu.tr")
                    .passwordHash(passwordEncoder.encode("Test123!"))
                    .firstName("Login")
                    .lastName("User")
                    .role(Role.STUDENT)
                    .isVerified(true)
                    .isActive(true)
                    .build();
            userRepository.save(testUser);
        }

        @Test
        @DisplayName("Giriş başarılı olmalı - 200 OK")
        void login_Success_Returns200() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("login@smartcampus.edu.tr")
                    .password("Test123!")
                    .build();

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.user.email").value("login@smartcampus.edu.tr"));
        }

        @Test
        @DisplayName("Yanlış şifre - 401 Unauthorized")
        void login_WrongPassword_Returns401() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("login@smartcampus.edu.tr")
                    .password("WrongPass123!")
                    .build();

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Kayıtlı olmayan email - 401 Unauthorized")
        void login_UserNotFound_Returns401() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("notfound@smartcampus.edu.tr")
                    .password("Test123!")
                    .build();

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Devre dışı hesap - 401 Unauthorized")
        void login_DisabledAccount_Returns401() throws Exception {
            testUser.setIsActive(false);
            userRepository.save(testUser);

            LoginRequest request = LoginRequest.builder()
                    .email("login@smartcampus.edu.tr")
                    .password("Test123!")
                    .build();

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh Tests")
    class RefreshTokenTests {

        private User testUser;
        private RefreshToken validRefreshToken;

        @BeforeEach
        void setUp() {
            testUser = User.builder()
                    .email("refresh@smartcampus.edu.tr")
                    .passwordHash(passwordEncoder.encode("Test123!"))
                    .firstName("Refresh")
                    .lastName("User")
                    .role(Role.STUDENT)
                    .isVerified(true)
                    .isActive(true)
                    .build();
            testUser = userRepository.save(testUser);

            validRefreshToken = RefreshToken.builder()
                    .token(UUID.randomUUID().toString())
                    .user(testUser)
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .build();
            refreshTokenRepository.save(validRefreshToken);
        }

        @Test
        @DisplayName("Token yenileme başarılı - 200 OK")
        void refreshToken_Success_Returns200() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest(validRefreshToken.getToken());

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
        }

        @Test
        @DisplayName("Geçersiz refresh token - 401 Unauthorized")
        void refreshToken_InvalidToken_Returns401() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Süresi dolmuş refresh token - 401 Unauthorized")
        void refreshToken_ExpiredToken_Returns401() throws Exception {
            RefreshToken expiredToken = RefreshToken.builder()
                    .token(UUID.randomUUID().toString())
                    .user(testUser)
                    .expiryDate(LocalDateTime.now().minusDays(1))
                    .build();
            refreshTokenRepository.save(expiredToken);

            RefreshTokenRequest request = new RefreshTokenRequest(expiredToken.getToken());

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Çıkış başarılı - 200 OK")
        void logout_Success_Returns200() throws Exception {
            User testUser = User.builder()
                    .email("logout@smartcampus.edu.tr")
                    .passwordHash(passwordEncoder.encode("Test123!"))
                    .firstName("Logout")
                    .lastName("User")
                    .role(Role.STUDENT)
                    .isVerified(true)
                    .isActive(true)
                    .build();
            testUser = userRepository.save(testUser);

            RefreshToken refreshToken = RefreshToken.builder()
                    .token(UUID.randomUUID().toString())
                    .user(testUser)
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .build();
            refreshTokenRepository.save(refreshToken);

            RefreshTokenRequest request = new RefreshTokenRequest(refreshToken.getToken());

            mockMvc.perform(post("/api/v1/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/verify-email Tests")
    class VerifyEmailTests {

        @Test
        @DisplayName("Email doğrulama başarılı - 200 OK")
        void verifyEmail_Success_Returns200() throws Exception {
            User testUser = User.builder()
                    .email("verify@smartcampus.edu.tr")
                    .passwordHash(passwordEncoder.encode("Test123!"))
                    .firstName("Verify")
                    .lastName("User")
                    .role(Role.STUDENT)
                    .isVerified(false)
                    .isActive(true)
                    .build();
            testUser = userRepository.save(testUser);

            EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                    .token(UUID.randomUUID().toString())
                    .user(testUser)
                    .expiryDate(LocalDateTime.now().plusHours(24))
                    .build();
            emailVerificationTokenRepository.save(verificationToken);

            VerifyEmailRequest request = new VerifyEmailRequest(verificationToken.getToken());

            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Geçersiz doğrulama token - 400 Bad Request")
        void verifyEmail_InvalidToken_Returns400() throws Exception {
            VerifyEmailRequest request = new VerifyEmailRequest("invalid-token");

            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/forgot-password Tests")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Şifre sıfırlama isteği - 200 OK")
        void forgotPassword_Success_Returns200() throws Exception {
            User testUser = User.builder()
                    .email("forgot@smartcampus.edu.tr")
                    .passwordHash(passwordEncoder.encode("Test123!"))
                    .firstName("Forgot")
                    .lastName("User")
                    .role(Role.STUDENT)
                    .isVerified(true)
                    .isActive(true)
                    .build();
            userRepository.save(testUser);

            ForgotPasswordRequest request = new ForgotPasswordRequest("forgot@smartcampus.edu.tr");

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Kayıtlı olmayan email için de 200 OK dönmeli (güvenlik)")
        void forgotPassword_NonExistentEmail_Returns200() throws Exception {
            ForgotPasswordRequest request = new ForgotPasswordRequest("nonexistent@smartcampus.edu.tr");

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/reset-password Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Şifre sıfırlama başarılı - 200 OK")
        void resetPassword_Success_Returns200() throws Exception {
            User testUser = User.builder()
                    .email("reset@smartcampus.edu.tr")
                    .passwordHash(passwordEncoder.encode("Test123!"))
                    .firstName("Reset")
                    .lastName("User")
                    .role(Role.STUDENT)
                    .isVerified(true)
                    .isActive(true)
                    .build();
            testUser = userRepository.save(testUser);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(UUID.randomUUID().toString())
                    .user(testUser)
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token(resetToken.getToken())
                    .newPassword("NewPass123!")
                    .build();

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Geçersiz reset token - 400 Bad Request")
        void resetPassword_InvalidToken_Returns400() throws Exception {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("invalid-token")
                    .newPassword("NewPass123!")
                    .build();

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/resend-verification Tests")
    class ResendVerificationTests {

        @Test
        @DisplayName("Doğrulama emaili tekrar gönderme - 200 OK")
        void resendVerification_Success_Returns200() throws Exception {
            User testUser = User.builder()
                    .email("resend@smartcampus.edu.tr")
                    .passwordHash(passwordEncoder.encode("Test123!"))
                    .firstName("Resend")
                    .lastName("User")
                    .role(Role.STUDENT)
                    .isVerified(false)
                    .isActive(true)
                    .build();
            userRepository.save(testUser);

            mockMvc.perform(post("/api/v1/auth/resend-verification")
                            .param("email", "resend@smartcampus.edu.tr"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Zaten doğrulanmış email - 400 Bad Request")
        void resendVerification_AlreadyVerified_Returns400() throws Exception {
            User testUser = User.builder()
                    .email("verified@smartcampus.edu.tr")
                    .passwordHash(passwordEncoder.encode("Test123!"))
                    .firstName("Verified")
                    .lastName("User")
                    .role(Role.STUDENT)
                    .isVerified(true)
                    .isActive(true)
                    .build();
            userRepository.save(testUser);

            mockMvc.perform(post("/api/v1/auth/resend-verification")
                            .param("email", "verified@smartcampus.edu.tr"))
                    .andExpect(status().isBadRequest());
        }
    }
}
