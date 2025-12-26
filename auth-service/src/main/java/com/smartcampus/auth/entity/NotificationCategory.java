package com.smartcampus.auth.entity;

/**
 * Bildirim kategorileri
 */
public enum NotificationCategory {
    ACADEMIC, // Akademik (ders kaydı, not girişi vb.)
    ATTENDANCE, // Yoklama (yoklama açıldı, devamsızlık uyarısı)
    MEAL, // Yemek (rezervasyon, menü güncelleme)
    EVENT, // Etkinlik (kayıt, hatırlatma)
    PAYMENT, // Ödeme (bakiye, işlem)
    SYSTEM // Sistem (bakım, duyuru)
}
