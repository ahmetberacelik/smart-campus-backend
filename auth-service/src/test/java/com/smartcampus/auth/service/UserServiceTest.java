package com.smartcampus.auth.service;

import com.smartcampus.auth.dto.request.ChangePasswordRequest;
import com.smartcampus.auth.dto.request.UpdateProfileRequest;
import com.smartcampus.auth.dto.response.PageResponse;
import com.smartcampus.auth.dto.response.UserResponse;
import com.smartcampus.auth.entity.Role;
import com.smartcampus.auth.entity.User;
import com.smartcampus.auth.exception.BadRequestException;
import com.smartcampus.auth.exception.ResourceNotFoundException;
import com.smartcampus.auth.repository.UserRepository;
import com.smartcampus.auth.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    // Test verileri
    private User testUser;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@smartcampus.edu.tr")
                .passwordHash("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("5551234567")
                .role(Role.STUDENT)
                .isVerified(true)
                .isActive(true)
                .build();

        testUser2 = User.builder()
                .id(2L)
                .email("test2@smartcampus.edu.tr")
                .passwordHash("hashedPassword2")
                .firstName("Test2")
                .lastName("User2")
                .role(Role.FACULTY)
                .isVerified(true)
                .isActive(true)
                .build();
    }

    // ==================== GET CURRENT USER TESTS ====================

    @Nested
    @DisplayName("Get Current User Tests")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Mevcut kullanıcıyı getirmeli")
        void getCurrentUser_Success() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            UserResponse response = userService.getCurrentUser(1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
            assertThat(response.getFirstName()).isEqualTo(testUser.getFirstName());
            assertThat(response.getLastName()).isEqualTo(testUser.getLastName());
        }

        @Test
        @DisplayName("Kullanıcı bulunamazsa hata vermeli")
        void getCurrentUser_NotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getCurrentUser(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ==================== UPDATE PROFILE TESTS ====================

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Profil güncelleme başarılı olmalı")
        void updateProfile_Success() {
            // Given
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("Updated")
                    .lastName("Name")
                    .phoneNumber("5559999999")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UserResponse response = userService.updateProfile(1L, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getFirstName()).isEqualTo("Updated");
            assertThat(response.getLastName()).isEqualTo("Name");
            assertThat(response.getPhoneNumber()).isEqualTo("5559999999");

            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Sadece ad güncellenebilmeli")
        void updateProfile_OnlyFirstName() {
            // Given
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("OnlyFirstName")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UserResponse response = userService.updateProfile(1L, request);

            // Then
            assertThat(response.getFirstName()).isEqualTo("OnlyFirstName");
            assertThat(response.getLastName()).isEqualTo("User"); // Değişmemeli
        }

        @Test
        @DisplayName("Sadece soyad güncellenebilmeli")
        void updateProfile_OnlyLastName() {
            // Given
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .lastName("OnlyLastName")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UserResponse response = userService.updateProfile(1L, request);

            // Then
            assertThat(response.getFirstName()).isEqualTo("Test"); // Değişmemeli
            assertThat(response.getLastName()).isEqualTo("OnlyLastName");
        }

        @Test
        @DisplayName("Kullanıcı bulunamazsa hata vermeli")
        void updateProfile_UserNotFound() {
            // Given
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("Updated")
                    .build();

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.updateProfile(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Şifre değiştirme başarılı olmalı")
        void changePassword_Success() {
            // Given
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("Test123!")
                    .newPassword("NewPass123!")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("Test123!", testUser.getPasswordHash())).thenReturn(true);
            when(passwordEncoder.matches("NewPass123!", testUser.getPasswordHash())).thenReturn(false);
            when(passwordEncoder.encode("NewPass123!")).thenReturn("newHashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.changePassword(1L, request);

            // Then
            verify(userRepository).save(testUser);
            verify(passwordEncoder).encode("NewPass123!");
        }

        @Test
        @DisplayName("Mevcut şifre yanlışsa hata vermeli")
        void changePassword_WrongCurrentPassword() {
            // Given
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("WrongPassword")
                    .newPassword("NewPass123!")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("WrongPassword", testUser.getPasswordHash())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.changePassword(1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Mevcut şifre hatalı");
        }

        @Test
        @DisplayName("Yeni şifre mevcut şifre ile aynıysa hata vermeli")
        void changePassword_SamePassword() {
            // Given
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("Test123!")
                    .newPassword("Test123!")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("Test123!", testUser.getPasswordHash())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.changePassword(1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Yeni şifre mevcut şifre ile aynı olamaz");
        }
    }

    // ==================== UPLOAD PROFILE PICTURE TESTS ====================

    @Nested
    @DisplayName("Upload Profile Picture Tests")
    class UploadProfilePictureTests {

        @Test
        @DisplayName("Profil fotoğrafı yükleme başarılı olmalı")
        void uploadProfilePicture_Success() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(fileStorageService.uploadFile(any(MultipartFile.class), eq("profile-pictures")))
                    .thenReturn("https://cdn.smartcampus.edu.tr/profile-pictures/test.jpg");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            String result = userService.uploadProfilePicture(1L, file);

            // Then
            assertThat(result).isEqualTo("https://cdn.smartcampus.edu.tr/profile-pictures/test.jpg");
            verify(fileStorageService).uploadFile(any(MultipartFile.class), eq("profile-pictures"));
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Eski profil fotoğrafı silinmeli")
        void uploadProfilePicture_DeleteOldPicture() {
            // Given
            testUser.setProfilePicture("https://cdn.smartcampus.edu.tr/profile-pictures/old.jpg");

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "new.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            doNothing().when(fileStorageService).deleteFile(anyString());
            when(fileStorageService.uploadFile(any(MultipartFile.class), eq("profile-pictures")))
                    .thenReturn("https://cdn.smartcampus.edu.tr/profile-pictures/new.jpg");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.uploadProfilePicture(1L, file);

            // Then
            verify(fileStorageService).deleteFile("https://cdn.smartcampus.edu.tr/profile-pictures/old.jpg");
        }

        @Test
        @DisplayName("Dosya seçilmezse hata vermeli")
        void uploadProfilePicture_NoFile() {
            // Given
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file",
                    "",
                    "image/jpeg",
                    new byte[0]
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.uploadProfilePicture(1L, emptyFile))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Dosya seçilmedi");
        }

        @Test
        @DisplayName("Dosya boyutu 5MB'dan büyükse hata vermeli")
        void uploadProfilePicture_FileTooLarge() {
            // Given
            byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
            MockMultipartFile largeFile = new MockMultipartFile(
                    "file",
                    "large.jpg",
                    "image/jpeg",
                    largeContent
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.uploadProfilePicture(1L, largeFile))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Dosya boyutu 5MB'dan büyük olamaz");
        }

        @Test
        @DisplayName("Geçersiz dosya tipi için hata vermeli")
        void uploadProfilePicture_InvalidFileType() {
            // Given
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "file",
                    "test.pdf",
                    "application/pdf",
                    "test content".getBytes()
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.uploadProfilePicture(1L, invalidFile))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Sadece JPG, JPEG ve PNG formatları desteklenir");
        }

        @Test
        @DisplayName("PNG dosyası yüklenebilmeli")
        void uploadProfilePicture_PngFile() {
            // Given
            MockMultipartFile pngFile = new MockMultipartFile(
                    "file",
                    "test.png",
                    "image/png",
                    "test image content".getBytes()
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(fileStorageService.uploadFile(any(MultipartFile.class), eq("profile-pictures")))
                    .thenReturn("https://cdn.smartcampus.edu.tr/profile-pictures/test.png");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            String result = userService.uploadProfilePicture(1L, pngFile);

            // Then
            assertThat(result).contains("test.png");
        }
    }

    // ==================== DELETE PROFILE PICTURE TESTS ====================

    @Nested
    @DisplayName("Delete Profile Picture Tests")
    class DeleteProfilePictureTests {

        @Test
        @DisplayName("Profil fotoğrafı silme başarılı olmalı")
        void deleteProfilePicture_Success() {
            // Given
            testUser.setProfilePicture("https://cdn.smartcampus.edu.tr/profile-pictures/test.jpg");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            doNothing().when(fileStorageService).deleteFile(anyString());
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.deleteProfilePicture(1L);

            // Then
            verify(fileStorageService).deleteFile("https://cdn.smartcampus.edu.tr/profile-pictures/test.jpg");
            verify(userRepository).save(testUser);
            assertThat(testUser.getProfilePicture()).isNull();
        }

        @Test
        @DisplayName("Profil fotoğrafı yoksa sessizce geçmeli")
        void deleteProfilePicture_NoPicture() {
            // Given
            testUser.setProfilePicture(null);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            userService.deleteProfilePicture(1L);

            // Then
            verify(fileStorageService, never()).deleteFile(anyString());
            verify(userRepository, never()).save(any());
        }
    }

    // ==================== GET ALL USERS TESTS ====================

    @Nested
    @DisplayName("Get All Users Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Tüm kullanıcıları getirmeli")
        void getAllUsers_Success() {
            // Given
            List<User> users = Arrays.asList(testUser, testUser2);
            Page<User> userPage = new PageImpl<>(users);
            Pageable pageable = PageRequest.of(0, 10);

            when(userRepository.findAllActive(pageable)).thenReturn(userPage);

            // When
            PageResponse<UserResponse> response = userService.getAllUsers(pageable, null, null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("Role'e göre filtrelemeli")
        void getAllUsers_FilterByRole() {
            // Given
            List<User> students = Arrays.asList(testUser);
            Page<User> userPage = new PageImpl<>(students);
            Pageable pageable = PageRequest.of(0, 10);

            when(userRepository.findAllByRole(Role.STUDENT, pageable)).thenReturn(userPage);

            // When
            PageResponse<UserResponse> response = userService.getAllUsers(pageable, null, Role.STUDENT);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getRole()).isEqualTo(Role.STUDENT);
        }

        @Test
        @DisplayName("Arama ile kullanıcı bulmalı")
        void getAllUsers_Search() {
            // Given
            List<User> users = Arrays.asList(testUser);
            Page<User> userPage = new PageImpl<>(users);
            Pageable pageable = PageRequest.of(0, 10);

            when(userRepository.searchUsers("Test", pageable)).thenReturn(userPage);

            // When
            PageResponse<UserResponse> response = userService.getAllUsers(pageable, "Test", null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Arama ve role ile filtrelemeli")
        void getAllUsers_SearchAndRole() {
            // Given
            List<User> users = Arrays.asList(testUser);
            Page<User> userPage = new PageImpl<>(users);
            Pageable pageable = PageRequest.of(0, 10);

            when(userRepository.searchUsersByRole("Test", Role.STUDENT, pageable)).thenReturn(userPage);

            // When
            PageResponse<UserResponse> response = userService.getAllUsers(pageable, "Test", Role.STUDENT);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }
    }

    // ==================== GET USER BY ID TESTS ====================

    @Nested
    @DisplayName("Get User By ID Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("ID ile kullanıcı getirmeli")
        void getUserById_Success() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            UserResponse response = userService.getUserById(1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Kullanıcı bulunamazsa hata vermeli")
        void getUserById_NotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getUserById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}


