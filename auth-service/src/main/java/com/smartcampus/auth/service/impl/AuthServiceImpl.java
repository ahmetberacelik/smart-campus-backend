package com.smartcampus.auth.service.impl;

import com.smartcampus.auth.dto.request.*;
import com.smartcampus.auth.dto.response.AuthResponse;
import com.smartcampus.auth.dto.response.TokenResponse;
import com.smartcampus.auth.dto.response.UserResponse;
import com.smartcampus.auth.entity.*;
import com.smartcampus.auth.exception.*;
import com.smartcampus.auth.repository.*;
import com.smartcampus.auth.security.JwtTokenProvider;
import com.smartcampus.auth.service.AuthService;
import com.smartcampus.auth.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Admin kayıt kontrolü
        if (request.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin kaydı yapılamaz", "ADMIN_REGISTRATION_NOT_ALLOWED");
        }

        // Email kontrolü
        if (userRepository.existsByEmail(request.getEmail())) {
            User existingUser = userRepository.findByEmail(request.getEmail())
                    .orElseThrow();
            
            if (existingUser.getIsVerified()) {
                throw new ConflictException("Bu email adresi zaten kayıtlı", "EMAIL_ALREADY_EXISTS");
            } else {
                // Doğrulanmamış kullanıcı - yeni doğrulama emaili gönder
                resendVerificationEmail(request.getEmail());
                throw new BadRequestException(
                        "Bu email adresi kayıtlı fakat doğrulanmamış. Yeni doğrulama emaili gönderildi.",
                        "EMAIL_NOT_VERIFIED"
                );
            }
        }

        // Department kontrolü
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Bölüm", "id", request.getDepartmentId()));

        // Role bazlı validasyon
        validateRoleSpecificFields(request);

        // User oluştur
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .isVerified(false)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // Role bazlı detay oluştur
        if (request.getRole() == Role.STUDENT) {
            if (studentRepository.existsByStudentNumber(request.getStudentNumber())) {
                throw new ConflictException("Bu öğrenci numarası zaten kayıtlı", "STUDENT_NUMBER_EXISTS");
            }
            
            Student student = Student.builder()
                    .user(user)
                    .department(department)
                    .studentNumber(request.getStudentNumber())
                    .build();
            studentRepository.save(student);
            user.setStudent(student);
        } else if (request.getRole() == Role.FACULTY) {
            if (facultyRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
                throw new ConflictException("Bu sicil numarası zaten kayıtlı", "EMPLOYEE_NUMBER_EXISTS");
            }
            
            Faculty faculty = Faculty.builder()
                    .user(user)
                    .department(department)
                    .employeeNumber(request.getEmployeeNumber())
                    .title(request.getTitle())
                    .build();
            facultyRepository.save(faculty);
            user.setFaculty(faculty);
        }

        // Email doğrulama token'ı oluştur
        String verificationToken = generateVerificationToken(user);

        // Doğrulama emaili gönder
        try {
            emailService.sendVerificationEmail(
                    user.getEmail(),
                    user.getFirstName(),
                    verificationToken
            );
        } catch (Exception e) {
            log.error("Failed to send verification email", e);
        }

        // Token'lar oluştur (doğrulama bekliyor durumunda da login yapabilsin)
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = createRefreshToken(user);

        log.info("User registered: {}", user.getEmail());

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiration(),
                UserResponse.fromUser(user)
        );
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Authentication
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email veya şifre hatalı"));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Hesabınız devre dışı bırakılmış", "ACCOUNT_DISABLED");
        }

        // Token'lar oluştur
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = createRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiration(),
                UserResponse.fromUser(user)
        );
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> TokenException.invalid());

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw TokenException.expired();
        }

        User user = refreshToken.getUser();

        // Eski token'ı sil
        refreshTokenRepository.delete(refreshToken);

        // Yeni token'lar oluştur
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String newRefreshToken = createRefreshToken(user);

        log.info("Token refreshed for user: {}", user.getEmail());

        return TokenResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtTokenProvider.getAccessTokenExpiration()
        );
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
        log.info("User logged out");
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Geçersiz doğrulama linki", "INVALID_TOKEN"));

        if (verificationToken.isExpired()) {
            emailVerificationTokenRepository.delete(verificationToken);
            throw new BadRequestException("Doğrulama linki süresi dolmuş", "TOKEN_EXPIRED");
        }

        User user = verificationToken.getUser();
        user.setIsVerified(true);
        userRepository.save(user);

        // Token'ı sil
        emailVerificationTokenRepository.deleteAllByUser(user);

        // Hoş geldin emaili gönder
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        } catch (Exception e) {
            log.error("Failed to send welcome email", e);
        }

        log.info("Email verified for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        // Güvenlik için kullanıcı bulunamasa bile aynı mesajı ver
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", request.getEmail());
            return;
        }

        if (!user.getIsActive()) {
            log.warn("Password reset requested for disabled account: {}", request.getEmail());
            return;
        }

        // Eski token'ları sil
        passwordResetTokenRepository.deleteAllByUser(user);

        // Yeni token oluştur
        String resetToken = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .user(user)
                .token(resetToken)
                .expiryDate(LocalDateTime.now().plusHours(1)) // 1 saat geçerli
                .build();
        passwordResetTokenRepository.save(passwordResetToken);

        // Email gönder
        try {
            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getFirstName(),
                    resetToken
            );
        } catch (Exception e) {
            log.error("Failed to send password reset email", e);
        }

        log.info("Password reset email sent to: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Geçersiz şifre sıfırlama linki", "INVALID_TOKEN"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new BadRequestException("Şifre sıfırlama linki süresi dolmuş", "TOKEN_EXPIRED");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Tüm refresh token'ları sil (güvenlik için)
        refreshTokenRepository.deleteAllByUser(user);

        // Reset token'ı sil
        passwordResetTokenRepository.deleteAllByUser(user);

        log.info("Password reset for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "email", email));

        if (user.getIsVerified()) {
            throw new BadRequestException("Email zaten doğrulanmış", "ALREADY_VERIFIED");
        }

        // Eski token'ları sil
        emailVerificationTokenRepository.deleteAllByUser(user);

        // Yeni token oluştur ve email gönder
        String verificationToken = generateVerificationToken(user);

        try {
            emailService.sendVerificationEmail(
                    user.getEmail(),
                    user.getFirstName(),
                    verificationToken
            );
        } catch (Exception e) {
            log.error("Failed to send verification email", e);
            throw new BadRequestException("Email gönderilemedi", "EMAIL_SEND_FAILED");
        }

        log.info("Verification email resent to: {}", user.getEmail());
    }

    // Helper methods

    private void validateRoleSpecificFields(RegisterRequest request) {
        if (request.getRole() == Role.STUDENT) {
            if (request.getStudentNumber() == null || request.getStudentNumber().isBlank()) {
                throw new BadRequestException("Öğrenci numarası zorunludur", "STUDENT_NUMBER_REQUIRED");
            }
        } else if (request.getRole() == Role.FACULTY) {
            if (request.getEmployeeNumber() == null || request.getEmployeeNumber().isBlank()) {
                throw new BadRequestException("Sicil numarası zorunludur", "EMPLOYEE_NUMBER_REQUIRED");
            }
            if (request.getTitle() == null || request.getTitle().isBlank()) {
                throw new BadRequestException("Unvan zorunludur", "TITLE_REQUIRED");
            }
        }
    }

    private String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusHours(24)) // 24 saat geçerli
                .build();
        emailVerificationTokenRepository.save(verificationToken);
        return token;
    }

    private String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusDays(7)) // 7 gün geçerli
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }
}

