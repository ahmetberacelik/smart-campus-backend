package com.smartcampus.attendance.exception;

public class BadRequestException extends BaseException {

    public BadRequestException(String message, String errorCode) {
        super(message, errorCode);
    }

    public BadRequestException(String message, String errorCode, Object details) {
        super(message, errorCode, details);
    }
}
