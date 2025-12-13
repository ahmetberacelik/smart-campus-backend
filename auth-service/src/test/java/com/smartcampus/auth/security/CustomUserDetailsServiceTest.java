package com.smartcampus.auth.security;

import com.smartcampus.auth.entity.Role;
import com.smartcampus.auth.entity.User;
import com.smartcampus.auth.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@smartcampus.edu.tr")
                .passwordHash("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.STUDENT)
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("Load User By Username Tests")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Should load user by email successfully")
        void shouldLoadUserByEmailSuccessfully() {
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(testUser.getEmail());

            assertNotNull(userDetails);
            assertEquals(testUser.getEmail(), userDetails.getUsername());
            assertEquals(testUser.getPasswordHash(), userDetails.getPassword());
            verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        }

        @Test
        @DisplayName("Should throw exception when user not found by email")
        void shouldThrowExceptionWhenUserNotFoundByEmail() {
            String email = "nonexistent@smartcampus.edu.tr";
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class,
                    () -> customUserDetailsService.loadUserByUsername(email));
            verify(userRepository, times(1)).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("Load User By Id Tests")
    class LoadUserByIdTests {

        @Test
        @DisplayName("Should load user by id successfully")
        void shouldLoadUserByIdSuccessfully() {
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

            UserDetails userDetails = customUserDetailsService.loadUserById(testUser.getId());

            assertNotNull(userDetails);
            assertEquals(testUser.getEmail(), userDetails.getUsername());
            verify(userRepository, times(1)).findById(testUser.getId());
        }

        @Test
        @DisplayName("Should throw exception when user not found by id")
        void shouldThrowExceptionWhenUserNotFoundById() {
            Long nonExistentId = 999L;
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class,
                    () -> customUserDetailsService.loadUserById(nonExistentId));
            verify(userRepository, times(1)).findById(nonExistentId);
        }
    }
}