package com.smartcampus.meal.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public static ResourceNotFoundException menu(Long id) {
        return new ResourceNotFoundException("Menü bulunamadı: " + id);
    }
    
    public static ResourceNotFoundException cafeteria(Long id) {
        return new ResourceNotFoundException("Yemekhane bulunamadı: " + id);
    }
    
    public static ResourceNotFoundException reservation(Long id) {
        return new ResourceNotFoundException("Rezervasyon bulunamadı: " + id);
    }
    
    public static ResourceNotFoundException wallet(Long userId) {
        return new ResourceNotFoundException("Cüzdan bulunamadı: userId=" + userId);
    }
}
