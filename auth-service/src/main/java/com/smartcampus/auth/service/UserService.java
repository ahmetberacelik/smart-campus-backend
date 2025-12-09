package com.smartcampus.auth.service;

import com.smartcampus.auth.dto.request.ChangePasswordRequest;
import com.smartcampus.auth.dto.request.UpdateProfileRequest;
import com.smartcampus.auth.dto.response.PageResponse;
import com.smartcampus.auth.dto.response.UserResponse;
import com.smartcampus.auth.entity.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserResponse getCurrentUser(Long userId);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    String uploadProfilePicture(Long userId, MultipartFile file);

    void deleteProfilePicture(Long userId);

    PageResponse<UserResponse> getAllUsers(Pageable pageable, String search, Role role);

    UserResponse getUserById(Long id);
}

