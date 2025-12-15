package com.smartcampus.attendance.exception;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s bulunamadı: %s = %s", resourceName, fieldName, fieldValue), "RESOURCE_NOT_FOUND");
    }
}
