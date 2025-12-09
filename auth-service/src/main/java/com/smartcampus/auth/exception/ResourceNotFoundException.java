package com.smartcampus.auth.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s bulunamadı: %s = %s", resource, field, value),
              HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}

