package com.smartcampus.auth.service;

public interface EmailService {

    void sendVerificationEmail(String to, String name, String token);

    void sendPasswordResetEmail(String to, String name, String token);

    void sendWelcomeEmail(String to, String name);
}

