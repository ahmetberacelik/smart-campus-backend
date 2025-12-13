package com.smartcampus.auth.security;

import com.smartcampus.auth.exception.TokenException;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET = "dGVzdFNlY3JldEtleUZvckludGVncmF0aW9uVGVzdGluZzEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MA==";
    private static final Long ACCESS_TOKEN_EXPIRATION = 900000L;
    private static final Long REFRESH_TOKEN_EXPIRATION = 604800000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);
    }

    @Nested
    @DisplayName("Generate Access Token Tests")
    class GenerateAccessTokenTests {

        @Test
        @DisplayName("Should generate access token from email")
        void shouldGenerateAccessTokenFromEmail() {
            String email = "test@smartcampus.edu.tr";

            String token = jwtTokenProvider.generateAccessToken(email);

            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertEquals(email, jwtTokenProvider.getEmailFromToken(token));
        }

        @Test
        @DisplayName("Should generate access token from authentication")
        void shouldGenerateAccessTokenFromAuthentication() {
            String email = "test@smartcampus.edu.tr";
            UserDetails userDetails = new User(email, "password",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")));
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            String token = jwtTokenProvider.generateAccessToken(authentication);

            assertNotNull(token);
            assertEquals(email, jwtTokenProvider.getEmailFromToken(token));
        }
    }

    @Nested
    @DisplayName("Generate Refresh Token Tests")
    class GenerateRefreshTokenTests {

        @Test
        @DisplayName("Should generate refresh token")
        void shouldGenerateRefreshToken() {
            String email = "test@smartcampus.edu.tr";

            String token = jwtTokenProvider.generateRefreshToken(email);

            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertEquals(email, jwtTokenProvider.getEmailFromToken(token));
        }
    }

    @Nested
    @DisplayName("Validate Token Tests")
    class ValidateTokenTests {

        @Test
        @DisplayName("Should validate valid token")
        void shouldValidateValidToken() {
            String token = jwtTokenProvider.generateAccessToken("test@smartcampus.edu.tr");

            boolean isValid = jwtTokenProvider.validateToken(token);

            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should throw exception for expired token")
        void shouldThrowExceptionForExpiredToken() {
            JwtTokenProvider expiredTokenProvider = new JwtTokenProvider();
            ReflectionTestUtils.setField(expiredTokenProvider, "jwtSecret", TEST_SECRET);
            ReflectionTestUtils.setField(expiredTokenProvider, "accessTokenExpiration", -1000L);
            ReflectionTestUtils.setField(expiredTokenProvider, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);

            String expiredToken = expiredTokenProvider.generateAccessToken("test@smartcampus.edu.tr");

            assertThrows(TokenException.class, () -> jwtTokenProvider.validateToken(expiredToken));
        }

        @Test
        @DisplayName("Should throw exception for malformed token")
        void shouldThrowExceptionForMalformedToken() {
            String malformedToken = "not.a.valid.token";

            assertThrows(TokenException.class, () -> jwtTokenProvider.validateToken(malformedToken));
        }

        @Test
        @DisplayName("Should throw exception for empty token")
        void shouldThrowExceptionForEmptyToken() {
            assertThrows(TokenException.class, () -> jwtTokenProvider.validateToken(""));
        }
    }

    @Nested
    @DisplayName("Get Email From Token Tests")
    class GetEmailFromTokenTests {

        @Test
        @DisplayName("Should extract email from valid token")
        void shouldExtractEmailFromValidToken() {
            String email = "student@smartcampus.edu.tr";
            String token = jwtTokenProvider.generateAccessToken(email);

            String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

            assertEquals(email, extractedEmail);
        }
    }

    @Nested
    @DisplayName("Expiration Getter Tests")
    class ExpirationGetterTests {

        @Test
        @DisplayName("Should return access token expiration")
        void shouldReturnAccessTokenExpiration() {
            assertEquals(ACCESS_TOKEN_EXPIRATION, jwtTokenProvider.getAccessTokenExpiration());
        }

        @Test
        @DisplayName("Should return refresh token expiration")
        void shouldReturnRefreshTokenExpiration() {
            assertEquals(REFRESH_TOKEN_EXPIRATION, jwtTokenProvider.getRefreshTokenExpiration());
        }
    }
}
