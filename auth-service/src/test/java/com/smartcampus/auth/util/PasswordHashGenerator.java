package com.smartcampus.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Seed verileri için BCrypt hash oluşturucu.
 * Bu sınıfı çalıştırarak yeni password hash'leri oluşturabilirsiniz.
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        
        // Test şifresi
        String password = "password123";
        
        // Yeni hash oluştur
        String hash = encoder.encode(password);
        
        System.out.println("===========================================");
        System.out.println("Password Hash Generator for Seed Data");
        System.out.println("===========================================");
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("===========================================");
        
        // Doğrulama
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification: " + (matches ? "✓ SUCCESS" : "✗ FAILED"));
        
        // Mevcut seed hash'i ile de test et
        String existingHash = "$2a$10$EqKcp1WFKVQISheBxkV8qOEb.OMjSPvKnHJPLAl.pL5aNLwzVy5Aq";
        boolean existingMatches = encoder.matches(password, existingHash);
        System.out.println("\nExisting seed hash verification:");
        System.out.println("Hash: " + existingHash);
        System.out.println("Matches 'password123': " + (existingMatches ? "✓ YES" : "✗ NO"));
        
        System.out.println("\n===========================================");
        System.out.println("SQL Update Statement:");
        System.out.println("===========================================");
        System.out.println("UPDATE users SET password_hash = '" + hash + "' WHERE email LIKE '%@smartcampus.edu.tr';");
    }
}

