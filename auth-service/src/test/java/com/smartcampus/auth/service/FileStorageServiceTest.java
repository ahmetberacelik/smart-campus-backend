package com.smartcampus.auth.service;

import com.smartcampus.auth.exception.BadRequestException;
import com.smartcampus.auth.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileStorageService Unit Tests")
class FileStorageServiceTest {

    private FileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageServiceImpl();
        ReflectionTestUtils.setField(fileStorageService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(fileStorageService, "region", "fra1");
    }

    @Nested
    @DisplayName("Get File URL Tests")
    class GetFileUrlTests {

        @Test
        @DisplayName("Should generate correct file URL")
        void shouldGenerateCorrectFileUrl() {
            String key = "profile-pictures/abc123.jpg";

            String url = fileStorageService.getFileUrl(key);

            assertEquals("https://test-bucket.fra1.digitaloceanspaces.com/profile-pictures/abc123.jpg", url);
        }

        @Test
        @DisplayName("Should handle simple key")
        void shouldHandleSimpleKey() {
            String key = "image.png";

            String url = fileStorageService.getFileUrl(key);

            assertEquals("https://test-bucket.fra1.digitaloceanspaces.com/image.png", url);
        }
    }

    @Nested
    @DisplayName("Upload File Tests")
    class UploadFileTests {

        @Test
        @DisplayName("Should throw exception when s3 client not initialized")
        void shouldThrowExceptionWhenS3ClientNotInitialized() {
            MultipartFile mockFile = mock(MultipartFile.class);

            assertThrows(BadRequestException.class, 
                    () -> fileStorageService.uploadFile(mockFile, "profile-pictures"));
        }
    }

    @Nested
    @DisplayName("Delete File Tests")
    class DeleteFileTests {

        @Test
        @DisplayName("Should not throw when s3 client not initialized")
        void shouldNotThrowWhenS3ClientNotInitialized() {
            assertDoesNotThrow(() -> 
                    fileStorageService.deleteFile("https://test-bucket.fra1.digitaloceanspaces.com/test.jpg"));
        }

        @Test
        @DisplayName("Should handle null file url")
        void shouldHandleNullFileUrl() {
            assertDoesNotThrow(() -> fileStorageService.deleteFile(null));
        }

        @Test
        @DisplayName("Should handle empty file url")
        void shouldHandleEmptyFileUrl() {
            assertDoesNotThrow(() -> fileStorageService.deleteFile(""));
        }
    }

    @Nested
    @DisplayName("Init Tests")
    class InitTests {

        @Test
        @DisplayName("Should not throw when credentials empty")
        void shouldNotThrowWhenCredentialsEmpty() {
            FileStorageServiceImpl service = new FileStorageServiceImpl();
            ReflectionTestUtils.setField(service, "spacesKey", "");
            ReflectionTestUtils.setField(service, "spacesSecret", "");
            ReflectionTestUtils.setField(service, "spacesEndpoint", "https://test.digitaloceanspaces.com");
            ReflectionTestUtils.setField(service, "bucketName", "test-bucket");
            ReflectionTestUtils.setField(service, "region", "fra1");

            assertDoesNotThrow(service::init);
        }

        @Test
        @DisplayName("Should handle invalid endpoint gracefully")
        void shouldHandleInvalidEndpointGracefully() {
            FileStorageServiceImpl service = new FileStorageServiceImpl();
            ReflectionTestUtils.setField(service, "spacesKey", "test-key");
            ReflectionTestUtils.setField(service, "spacesSecret", "test-secret");
            ReflectionTestUtils.setField(service, "spacesEndpoint", "invalid-url");
            ReflectionTestUtils.setField(service, "bucketName", "test-bucket");
            ReflectionTestUtils.setField(service, "region", "fra1");

            assertDoesNotThrow(service::init);
        }
    }
}
