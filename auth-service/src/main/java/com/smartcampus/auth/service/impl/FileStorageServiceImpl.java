package com.smartcampus.auth.service.impl;

import com.smartcampus.auth.exception.BadRequestException;
import com.smartcampus.auth.service.FileStorageService;
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
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${digitalocean.spaces.key}")
    private String spacesKey;

    @Value("${digitalocean.spaces.secret}")
    private String spacesSecret;

    @Value("${digitalocean.spaces.endpoint}")
    private String spacesEndpoint;

    @Value("${digitalocean.spaces.bucket}")
    private String bucketName;

    @Value("${digitalocean.spaces.region}")
    private String region;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        if (StringUtils.hasText(spacesKey) && StringUtils.hasText(spacesSecret)) {
            try {
                AwsBasicCredentials credentials = AwsBasicCredentials.create(spacesKey, spacesSecret);

                this.s3Client = S3Client.builder()
                        .endpointOverride(URI.create(spacesEndpoint))
                        .credentialsProvider(StaticCredentialsProvider.create(credentials))
                        .region(Region.of(region))
                        .build();

                log.info("DigitalOcean Spaces client initialized successfully");
            } catch (Exception e) {
                log.warn("Failed to initialize DigitalOcean Spaces client: {}", e.getMessage());
            }
        } else {
            log.warn("DigitalOcean Spaces credentials not configured");
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (s3Client == null) {
            throw new BadRequestException("Dosya yükleme servisi yapılandırılmamış", "FILE_SERVICE_NOT_CONFIGURED");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String key = folder + "/" + UUID.randomUUID().toString() + extension;

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .acl("public-read")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

            String fileUrl = getFileUrl(key);
            log.info("File uploaded successfully: {}", fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("Failed to upload file", e);
            throw new BadRequestException("Dosya yüklenirken hata oluştu", "FILE_UPLOAD_FAILED");
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (s3Client == null) {
            log.warn("S3 client not initialized, skipping delete");
            return;
        }

        try {
            String key = extractKeyFromUrl(fileUrl);
            
            if (key != null) {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

                s3Client.deleteObject(deleteRequest);
                log.info("File deleted successfully: {}", key);
            }
        } catch (Exception e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }

    @Override
    public String getFileUrl(String key) {
        // DigitalOcean Spaces URL formatı: https://{bucket}.{region}.digitaloceanspaces.com/{key}
        return String.format("https://%s.%s.digitaloceanspaces.com/%s", bucketName, region, key);
    }

    private String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }

        // URL'den key'i çıkar
        String baseUrl = String.format("https://%s.%s.digitaloceanspaces.com/", bucketName, region);
        if (fileUrl.startsWith(baseUrl)) {
            return fileUrl.substring(baseUrl.length());
        }

        return null;
    }
}

