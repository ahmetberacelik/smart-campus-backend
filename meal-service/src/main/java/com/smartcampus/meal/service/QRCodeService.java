package com.smartcampus.meal.service;

public interface QRCodeService {
    
    /**
     * Benzersiz QR kod üret
     */
    String generateUniqueCode();
    
    /**
     * QR kod'u Base64 PNG image olarak dön
     */
    String generateQRCodeImage(String content, int width, int height);
    
    /**
     * QR kod geçerli mi kontrol et
     */
    boolean validateQRCode(String qrCode);
}
