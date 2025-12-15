package com.smartcampus.attendance.exception;

public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super(message, "ACCESS_DENIED");
    }

    public ForbiddenException(String message, String errorCode) {
        super(message, errorCode);
    }

    public ForbiddenException(String message, String errorCode, Object details) {
        super(message, errorCode, details);
    }
}
