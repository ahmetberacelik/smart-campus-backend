package com.smartcampus.auth.service;

public interface EmailService {

    void sendVerificationEmail(String to, String name, String token);

    void sendPasswordResetEmail(String to, String name, String token);

    void sendWelcomeEmail(String to, String name);

    /**
     * Bildirim email'i gönder (Notification System)
     * 
     * @param to       alıcı email
     * @param name     alıcı ismi
     * @param subject  email konusu
     * @param title    bildirim başlığı
     * @param message  bildirim mesajı
     * @param category bildirim kategorisi (ACADEMIC, ATTENDANCE, vb.)
     */
    void sendNotificationEmail(String to, String name, String subject, String title, String message, String category);
}
