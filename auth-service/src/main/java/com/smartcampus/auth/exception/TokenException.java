package com.smartcampus.auth.exception;

import org.springframework.http.HttpStatus;

public class TokenException extends BaseException {

    public TokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "TOKEN_ERROR");
    }

    public TokenException(String message, String errorCode) {
        super(message, HttpStatus.UNAUTHORIZED, errorCode);
    }

    public static TokenException expired() {
        return new TokenException("Token süresi dolmuş", "TOKEN_EXPIRED");
    }

    public static TokenException invalid() {
        return new TokenException("Geçersiz token", "TOKEN_INVALID");
    }
}

