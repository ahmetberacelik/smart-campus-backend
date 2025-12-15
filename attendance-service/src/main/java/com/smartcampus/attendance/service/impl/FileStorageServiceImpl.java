package com.smartcampus.attendance.service.impl;

import com.smartcampus.attendance.exception.BadRequestException;
import com.smartcampus.attendance.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${do.spaces.key:}")
    private String spacesKey;

    @Value("${do.spaces.secret:}")
    private String spacesSecret;

    @Value("${do.spaces.endpoint:https://fra1.digitaloceanspaces.com}")
    private String spacesEndpoint;

    @Value("${do.spaces.bucket:smart-campus}")
    private String spacesBucket;

    @Value("${do.spaces.region:fra1}")
    private String spacesRegion;

    private S3Client s3Client;
    private boolean isConfigured = false;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "application/pdf"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @PostConstruct
    public void init() {
        if (StringUtils.hasText(spacesKey) && StringUtils.hasText(spacesSecret)) {
            try {
                AwsBasicCredentials credentials = AwsBasicCredentials.create(spacesKey, spacesSecret);
                this.s3Client = S3Client.builder()
                        .endpointOverride(URI.create(spacesEndpoint))
                        .region(Region.of(spacesRegion))
                        .credentialsProvider(StaticCredentialsProvider.create(credentials))
                        .build();
                this.isConfigured = true;
                log.info("DigitalOcean Spaces configured successfully");
            } catch (Exception e) {
                log.warn("Failed to configure DigitalOcean Spaces: {}", e.getMessage());
            }
        } else {
            log.warn("DigitalOcean Spaces credentials not configured");
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (!isConfigured) {
            throw new BadRequestException("Dosya yükleme servisi yapılandırılmamış", "FILE_SERVICE_NOT_CONFIGURED");
        }

        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String fileName = folder + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(spacesBucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .acl("public-read")
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            return String.format("%s/%s/%s", spacesEndpoint.replace("https://", "https://" + spacesBucket + "."), "", fileName);
        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new BadRequestException("Dosya yüklenirken hata oluştu", "FILE_UPLOAD_FAILED");
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (!isConfigured || fileUrl == null) {
            return;
        }

        try {
            String key = fileUrl.substring(fileUrl.lastIndexOf(spacesBucket + "/") + spacesBucket.length() + 1);
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(spacesBucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Dosya seçilmedi", "FILE_REQUIRED");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Dosya boyutu 10MB'dan büyük olamaz", "FILE_TOO_LARGE");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Sadece JPG, JPEG, PNG ve PDF formatları desteklenir", "INVALID_FILE_TYPE");
        }
    }
}
