package com.smartcampus.auth.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String uploadFile(MultipartFile file, String folder);

    void deleteFile(String fileUrl);

    String getFileUrl(String key);
}

