package com.smartcampus.auth.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Password hash test ve generator.
 * Bu test'i çalıştırarak seed veriler için doğru hash'i bulabilirsiniz.
 */
class PasswordHashTest {

    @Test
    void generateAndVerifyPasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        
        String password = "password123";
        
        // Yeni hash oluştur
        String newHash = encoder.encode(password);
        
        System.out.println("\n===========================================");
        System.out.println("Password Hash Generator for Seed Data");
        System.out.println("===========================================");
        System.out.println("Password: " + password);
        System.out.println("New BCrypt Hash: " + newHash);
        
        // Mevcut seed hash'i
        String existingHash = "$2a$10$EqKcp1WFKVQISheBxkV8qOEb.OMjSPvKnHJPLAl.pL5aNLwzVy5Aq";
        
        // Test et
        boolean newHashMatches = encoder.matches(password, newHash);
        boolean existingHashMatches = encoder.matches(password, existingHash);
        
        System.out.println("\n--- Verification Results ---");
        System.out.println("New hash matches 'password123': " + (newHashMatches ? "✓ YES" : "✗ NO"));
        System.out.println("Existing seed hash matches 'password123': " + (existingHashMatches ? "✓ YES" : "✗ NO"));
        
        if (!existingHashMatches) {
            System.out.println("\n*** PROBLEM DETECTED ***");
            System.out.println("Existing seed hash does NOT match 'password123'!");
            System.out.println("\nUse this SQL to fix the seed data:");
            System.out.println("UPDATE users SET password_hash = '" + newHash + "';");
        }
        
        System.out.println("===========================================\n");
        
        // Test assertion
        assertTrue(newHashMatches, "New hash should match password");
    }
    
    @Test
    void testMultiplePasswords() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        
        String[] passwords = {"password123", "admin123", "test123", "Password123!"};
        String existingHash = "$2a$10$EqKcp1WFKVQISheBxkV8qOEb.OMjSPvKnHJPLAl.pL5aNLwzVy5Aq";
        
        System.out.println("\n=== Testing which password matches existing hash ===");
        
        for (String pwd : passwords) {
            boolean matches = encoder.matches(pwd, existingHash);
            System.out.println("'" + pwd + "' matches: " + (matches ? "✓ YES" : "✗ NO"));
            if (matches) {
                System.out.println(">>> FOUND! The password is: " + pwd);
            }
        }
        
        System.out.println("===================================================\n");
    }
}

