package com.smartcampus.academic.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " bulunamadı: " + id, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}
