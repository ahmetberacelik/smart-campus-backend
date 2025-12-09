package com.smartcampus.auth.service.impl;

import com.smartcampus.auth.dto.request.ChangePasswordRequest;
import com.smartcampus.auth.dto.request.UpdateProfileRequest;
import com.smartcampus.auth.dto.response.PageResponse;
import com.smartcampus.auth.dto.response.UserResponse;
import com.smartcampus.auth.entity.Role;
import com.smartcampus.auth.entity.User;
import com.smartcampus.auth.exception.BadRequestException;
import com.smartcampus.auth.exception.ResourceNotFoundException;
import com.smartcampus.auth.repository.UserRepository;
import com.smartcampus.auth.service.FileStorageService;
import com.smartcampus.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = findUserById(userId);
        return UserResponse.fromUser(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        if (StringUtils.hasText(request.getFirstName())) {
            user.setFirstName(request.getFirstName());
        }

        if (StringUtils.hasText(request.getLastName())) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        user = userRepository.save(user);
        log.info("Profile updated for user: {}", user.getEmail());

        return UserResponse.fromUser(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUserById(userId);

        // Mevcut şifre kontrolü
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Mevcut şifre hatalı", "INVALID_CURRENT_PASSWORD");
        }

        // Yeni şifre aynı olmamalı
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Yeni şifre mevcut şifre ile aynı olamaz", "SAME_PASSWORD");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public String uploadProfilePicture(Long userId, MultipartFile file) {
        User user = findUserById(userId);

        // Dosya validasyonu
        validateImageFile(file);

        // Eski profil fotoğrafını sil
        if (StringUtils.hasText(user.getProfilePicture())) {
            try {
                fileStorageService.deleteFile(user.getProfilePicture());
            } catch (Exception e) {
                log.error("Failed to delete old profile picture", e);
            }
        }

        // Yeni dosyayı yükle
        String fileUrl = fileStorageService.uploadFile(file, "profile-pictures");

        user.setProfilePicture(fileUrl);
        userRepository.save(user);

        log.info("Profile picture uploaded for user: {}", user.getEmail());

        return fileUrl;
    }

    @Override
    @Transactional
    public void deleteProfilePicture(Long userId) {
        User user = findUserById(userId);

        if (StringUtils.hasText(user.getProfilePicture())) {
            try {
                fileStorageService.deleteFile(user.getProfilePicture());
            } catch (Exception e) {
                log.error("Failed to delete profile picture", e);
            }

            user.setProfilePicture(null);
            userRepository.save(user);

            log.info("Profile picture deleted for user: {}", user.getEmail());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(Pageable pageable, String search, Role role) {
        Page<User> userPage;

        if (StringUtils.hasText(search) && role != null) {
            userPage = userRepository.searchUsersByRole(search, role, pageable);
        } else if (StringUtils.hasText(search)) {
            userPage = userRepository.searchUsers(search, pageable);
        } else if (role != null) {
            userPage = userRepository.findAllByRole(role, pageable);
        } else {
            userPage = userRepository.findAllActive(pageable);
        }

        Page<UserResponse> responsePage = userPage.map(UserResponse::fromUser);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserById(id);
        return UserResponse.fromUser(user);
    }

    // Helper methods

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", userId));
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Dosya seçilmedi", "FILE_REQUIRED");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Dosya boyutu 5MB'dan büyük olamaz", "FILE_TOO_LARGE");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException(
                    "Sadece JPG, JPEG ve PNG formatları desteklenir",
                    "INVALID_FILE_TYPE"
            );
        }
    }
}

