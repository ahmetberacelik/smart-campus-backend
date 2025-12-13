package com.smartcampus.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.auth.dto.request.ChangePasswordRequest;
import com.smartcampus.auth.dto.request.UpdateProfileRequest;
import com.smartcampus.auth.entity.*;
import com.smartcampus.auth.repository.*;
import com.smartcampus.auth.security.JwtTokenProvider;
import com.smartcampus.auth.service.EmailService;
import com.smartcampus.auth.service.FileStorageService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UserController Integration Tests")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private EmailService emailService;

    private User testUser;
    private User adminUser;
    private String userAccessToken;
    private String adminAccessToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("user@smartcampus.edu.tr")
                .passwordHash(passwordEncoder.encode("Test123!"))
                .firstName("Test")
                .lastName("User")
                .phoneNumber("5551234567")
                .role(Role.STUDENT)
                .isVerified(true)
                .isActive(true)
                .build();
        testUser = userRepository.save(testUser);
        userAccessToken = jwtTokenProvider.generateAccessToken(testUser.getEmail());

        adminUser = User.builder()
                .email("admin@smartcampus.edu.tr")
                .passwordHash(passwordEncoder.encode("Admin123!"))
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .isVerified(true)
                .isActive(true)
                .build();
        adminUser = userRepository.save(adminUser);
        adminAccessToken = jwtTokenProvider.generateAccessToken(adminUser.getEmail());
    }

    @Nested
    @DisplayName("GET /api/v1/users/me Tests")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Profil görüntüleme başarılı - 200 OK")
        void getCurrentUser_Success_Returns200() throws Exception {
            mockMvc.perform(get("/api/v1/users/me")
                            .header("Authorization", "Bearer " + userAccessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("user@smartcampus.edu.tr"))
                    .andExpect(jsonPath("$.data.firstName").value("Test"))
                    .andExpect(jsonPath("$.data.lastName").value("User"))
                    .andExpect(jsonPath("$.data.role").value("STUDENT"));
        }

        @Test
        @DisplayName("Token olmadan erişim - 401 Unauthorized")
        void getCurrentUser_NoToken_Returns401() throws Exception {
            mockMvc.perform(get("/api/v1/users/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Geçersiz token ile erişim - 401 Unauthorized")
        void getCurrentUser_InvalidToken_Returns401() throws Exception {
            mockMvc.perform(get("/api/v1/users/me")
                            .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/users/me Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Profil güncelleme başarılı - 200 OK")
        void updateProfile_Success_Returns200() throws Exception {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("Updated")
                    .lastName("Name")
                    .phoneNumber("5559999999")
                    .build();

            mockMvc.perform(put("/api/v1/users/me")
                            .header("Authorization", "Bearer " + userAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.firstName").value("Updated"))
                    .andExpect(jsonPath("$.data.lastName").value("Name"))
                    .andExpect(jsonPath("$.data.phoneNumber").value("5559999999"));
        }

        @Test
        @DisplayName("Kısmi güncelleme - sadece ad - 200 OK")
        void updateProfile_OnlyFirstName_Returns200() throws Exception {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("OnlyFirst")
                    .build();

            mockMvc.perform(put("/api/v1/users/me")
                            .header("Authorization", "Bearer " + userAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.firstName").value("OnlyFirst"))
                    .andExpect(jsonPath("$.data.lastName").value("User"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/me/change-password Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Şifre değiştirme başarılı - 200 OK")
        void changePassword_Success_Returns200() throws Exception {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("Test123!")
                    .newPassword("NewPass123!")
                    .build();

            mockMvc.perform(post("/api/v1/users/me/change-password")
                            .header("Authorization", "Bearer " + userAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Yanlış mevcut şifre - 400 Bad Request")
        void changePassword_WrongCurrentPassword_Returns400() throws Exception {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("WrongPass123!")
                    .newPassword("NewPass123!")
                    .build();

            mockMvc.perform(post("/api/v1/users/me/change-password")
                            .header("Authorization", "Bearer " + userAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Yeni şifre aynı eski şifre - 400 Bad Request")
        void changePassword_SamePassword_Returns400() throws Exception {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("Test123!")
                    .newPassword("Test123!")
                    .build();

            mockMvc.perform(post("/api/v1/users/me/change-password")
                            .header("Authorization", "Bearer " + userAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/me/profile-picture Tests")
    class UploadProfilePictureTests {

        @Test
        @DisplayName("Profil fotoğrafı yükleme başarılı - 200 OK")
        void uploadProfilePicture_Success_Returns200() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            when(fileStorageService.uploadFile(any(), eq("profile-pictures")))
                    .thenReturn("https://cdn.smartcampus.edu.tr/profile-pictures/test.jpg");

            mockMvc.perform(multipart("/api/v1/users/me/profile-picture")
                            .file(file)
                            .header("Authorization", "Bearer " + userAccessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("https://cdn.smartcampus.edu.tr/profile-pictures/test.jpg"));
        }

        @Test
        @DisplayName("Geçersiz dosya tipi - 400 Bad Request")
        void uploadProfilePicture_InvalidFileType_Returns400() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.pdf",
                    "application/pdf",
                    "test content".getBytes()
            );

            mockMvc.perform(multipart("/api/v1/users/me/profile-picture")
                            .file(file)
                            .header("Authorization", "Bearer " + userAccessToken))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/me/profile-picture Tests")
    class DeleteProfilePictureTests {

        @Test
        @DisplayName("Profil fotoğrafı silme başarılı - 200 OK")
        void deleteProfilePicture_Success_Returns200() throws Exception {
            testUser.setProfilePicture("https://cdn.smartcampus.edu.tr/profile-pictures/old.jpg");
            userRepository.save(testUser);

            doNothing().when(fileStorageService).deleteFile(any());

            mockMvc.perform(delete("/api/v1/users/me/profile-picture")
                            .header("Authorization", "Bearer " + userAccessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users Tests (Admin Only)")
    class GetAllUsersTests {

        @Test
        @DisplayName("Admin kullanıcı listesi alabilir - 200 OK")
        void getAllUsers_AsAdmin_Returns200() throws Exception {
            mockMvc.perform(get("/api/v1/users")
                            .header("Authorization", "Bearer " + adminAccessToken)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("Normal kullanıcı erişemez - 403 Forbidden")
        void getAllUsers_AsStudent_Returns403() throws Exception {
            mockMvc.perform(get("/api/v1/users")
                            .header("Authorization", "Bearer " + userAccessToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Role ile filtreleme - 200 OK")
        void getAllUsers_FilterByRole_Returns200() throws Exception {
            mockMvc.perform(get("/api/v1/users")
                            .header("Authorization", "Bearer " + adminAccessToken)
                            .param("role", "STUDENT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Arama ile kullanıcı bulma - 200 OK")
        void getAllUsers_WithSearch_Returns200() throws Exception {
            mockMvc.perform(get("/api/v1/users")
                            .header("Authorization", "Bearer " + adminAccessToken)
                            .param("search", "Test"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{id} Tests (Admin Only)")
    class GetUserByIdTests {

        @Test
        @DisplayName("Admin kullanıcı detayı alabilir - 200 OK")
        void getUserById_AsAdmin_Returns200() throws Exception {
            mockMvc.perform(get("/api/v1/users/" + testUser.getId())
                            .header("Authorization", "Bearer " + adminAccessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("user@smartcampus.edu.tr"));
        }

        @Test
        @DisplayName("Normal kullanıcı erişemez - 403 Forbidden")
        void getUserById_AsStudent_Returns403() throws Exception {
            mockMvc.perform(get("/api/v1/users/" + adminUser.getId())
                            .header("Authorization", "Bearer " + userAccessToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Olmayan kullanıcı - 404 Not Found")
        void getUserById_NotFound_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/users/99999")
                            .header("Authorization", "Bearer " + adminAccessToken))
                    .andExpect(status().isNotFound());
        }
    }
}