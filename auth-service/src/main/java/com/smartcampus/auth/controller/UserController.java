package com.smartcampus.auth.controller;

import com.smartcampus.auth.dto.request.ChangePasswordRequest;
import com.smartcampus.auth.dto.request.UpdateProfileRequest;
import com.smartcampus.auth.dto.response.ApiResponse;
import com.smartcampus.auth.dto.response.PageResponse;
import com.smartcampus.auth.dto.response.UserResponse;
import com.smartcampus.auth.entity.Role;
import com.smartcampus.auth.security.CurrentUser;
import com.smartcampus.auth.security.CustomUserDetails;
import com.smartcampus.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Kullanıcı yönetimi işlemleri")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Profil görüntüleme", description = "Giriş yapmış kullanıcının profilini getirir")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @CurrentUser CustomUserDetails currentUser
    ) {
        UserResponse response = userService.getCurrentUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    @Operation(summary = "Profil güncelleme", description = "Giriş yapmış kullanıcının profilini günceller")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserResponse response = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profil güncellendi", response));
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Şifre değiştirme", description = "Giriş yapmış kullanıcının şifresini değiştirir")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Şifre başarıyla değiştirildi"));
    }

    @PostMapping(value = "/me/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Profil fotoğrafı yükleme", description = "Profil fotoğrafı yükler (JPG, PNG, max 5MB)")
    public ResponseEntity<ApiResponse<String>> uploadProfilePicture(
            @CurrentUser CustomUserDetails currentUser,
            @RequestParam("file") MultipartFile file
    ) {
        String fileUrl = userService.uploadProfilePicture(currentUser.getId(), file);
        return ResponseEntity.ok(ApiResponse.success("Profil fotoğrafı yüklendi", fileUrl));
    }

    @DeleteMapping("/me/profile-picture")
    @Operation(summary = "Profil fotoğrafı silme", description = "Profil fotoğrafını siler")
    public ResponseEntity<ApiResponse<Void>> deleteProfilePicture(
            @CurrentUser CustomUserDetails currentUser
    ) {
        userService.deleteProfilePicture(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Profil fotoğrafı silindi"));
    }

    // Admin endpoints

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kullanıcı listesi (Admin)", description = "Tüm kullanıcıları listeler")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<UserResponse> response = userService.getAllUsers(pageable, search, role);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kullanıcı detayı (Admin)", description = "Belirtilen kullanıcının detaylarını getirir")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id
    ) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

