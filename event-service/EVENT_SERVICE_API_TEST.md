# Event Service API Test Rehberi

Bu belge Swagger üzerinden event-service API'lerini test etmek için örnek istekler içerir.

## 🔐 Authentication

Önce login olup token alın:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@smartcampus.edu.tr","password":"password123"}'
```

**Swagger'da Authorize:**
1. Sağ üst köşedeki "Authorize" butonuna tıklayın
2. Token'ı yapıştırın: `eyJhbGci...` (Bearer prefix'i yazMAYIN)

---

## 📋 Event Controller

### Yaklaşan Etkinlikler (Public)
```
GET /api/v1/events
GET /api/v1/events/upcoming
```
Auth: ❌ Gerekmiyor

### Etkinlik Detayı (Public)
```
GET /api/v1/events/{id}
GET /api/v1/events/1
```
Auth: ❌ Gerekmiyor

### Kategoriye Göre Etkinlikler (Public)
```
GET /api/v1/events/category/WORKSHOP
GET /api/v1/events/category/CONFERENCE
GET /api/v1/events/category/SEMINAR
GET /api/v1/events/category/SOCIAL
GET /api/v1/events/category/SPORTS
GET /api/v1/events/category/CULTURAL
GET /api/v1/events/category/CAREER
```
Auth: ❌ Gerekmiyor

### Etkinlik Ara (Public)
```
GET /api/v1/events/search?q=yazılım&page=0&size=10
```
Auth: ❌ Gerekmiyor

### Benim Etkinliklerim (Organizatör)
```
GET /api/v1/events/my-events
```
Auth: ✅ Gerekli (FACULTY/ADMIN)

### Etkinlik Oluştur (Faculty/Admin)
```
POST /api/v1/events
Content-Type: application/json

{
  "title": "Spring Boot Workshop",
  "description": "Spring Boot ile microservice geliştirme",
  "category": "WORKSHOP",
  "eventDate": "2025-12-28",
  "startTime": "14:00:00",
  "endTime": "18:00:00",
  "location": "Bilgisayar Merkezi, LAB-2",
  "capacity": 30,
  "registrationDeadline": "2025-12-26T23:59:59",
  "isPaid": true,
  "price": 75.00,
  "imageUrl": null
}
```
Auth: ✅ Gerekli (FACULTY/ADMIN)

### Etkinlik Güncelle
```
PUT /api/v1/events/{id}
Content-Type: application/json

{
  "title": "Spring Boot Workshop - Güncellendi",
  "description": "Spring Boot ile microservice geliştirme - Yeni içerik",
  "category": "WORKSHOP",
  "eventDate": "2025-12-28",
  "startTime": "14:00:00",
  "endTime": "18:00:00",
  "location": "Bilgisayar Merkezi, LAB-2",
  "capacity": 40,
  "registrationDeadline": "2025-12-26T23:59:59",
  "isPaid": true,
  "price": 50.00,
  "imageUrl": null
}
```
Auth: ✅ Gerekli (Organizatör)

### Etkinlik Yayınla
```
POST /api/v1/events/{id}/publish
```
Auth: ✅ Gerekli (Organizatör)

### Etkinlik İptal Et
```
POST /api/v1/events/{id}/cancel
```
Auth: ✅ Gerekli (Organizatör)

### Etkinlik Sil
```
DELETE /api/v1/events/{id}
```
Auth: ✅ Gerekli (ADMIN veya Organizatör - sadece DRAFT)

---

## 🎫 Registration Controller

### Etkinliğe Kayıt Ol
```
POST /api/v1/events/{eventId}/register
Content-Type: application/json

{
  "customFieldsJson": null
}
```
Auth: ✅ Gerekli

### Kayıt İptal
```
DELETE /api/v1/events/{eventId}/register
```
Auth: ✅ Gerekli

### Kayıtlı Olduğum Etkinlikler
```
GET /api/v1/events/my-registrations
```
Auth: ✅ Gerekli

### Etkinlik Katılımcı Listesi (Organizatör)
```
GET /api/v1/events/{eventId}/registrations?page=0&size=10
```
Auth: ✅ Gerekli (FACULTY/ADMIN - Organizatör)

### Etkinlik İstatistikleri (Organizatör)
```
GET /api/v1/events/{eventId}/stats
```
Auth: ✅ Gerekli (FACULTY/ADMIN)

Response:
```json
{
  "success": true,
  "data": {
    "registeredCount": 25,
    "checkedInCount": 18
  }
}
```

### QR Kod ile Kayıt Sorgula (Staff)
```
GET /api/v1/events/registration/qr/{qrCode}
GET /api/v1/events/registration/qr/EVT-ABC12345
```
Auth: ✅ Gerekli (FACULTY/ADMIN)

### Check-in Yap (Staff)
```
POST /api/v1/events/check-in/{qrCode}
POST /api/v1/events/check-in/EVT-ABC12345
```
Auth: ✅ Gerekli (FACULTY/ADMIN)

---

## 📊 Test Senaryoları

### 1. Etkinlik Oluşturma ve Yayınlama (Organizatör)
1. `POST /api/v1/events` → Etkinlik oluştur (DRAFT status)
2. `POST /api/v1/events/{id}/publish` → Yayınla (PUBLISHED status)
3. `GET /api/v1/events/{id}` → Kontrol et

### 2. Kayıt Akışı (Öğrenci)
1. `GET /api/v1/events/upcoming` → Etkinlikleri listele
2. `GET /api/v1/events/{id}` → Detay gör
3. `POST /api/v1/events/{id}/register` → Kayıt ol → QR kod al
4. `GET /api/v1/events/my-registrations` → Kayıtlarımı gör

### 3. Waitlist Senaryosu
1. Kapasite 2 olan etkinlik oluştur
2. 2 kullanıcı kayıt olsun → REGISTERED
3. 3. kullanıcı kayıt olsun → WAITLIST (waitlistPosition: 1)
4. 1. kayıt iptal → 3. kullanıcı otomatik REGISTERED olur

### 4. Check-in Akışı (Staff)
1. `GET /api/v1/events/registration/qr/{qrCode}` → QR sorgula
2. `POST /api/v1/events/check-in/{qrCode}` → Check-in yap
3. `GET /api/v1/events/{id}/stats` → İstatistik kontrol

---

## ⚠️ Sık Karşılaşılan Hatalar

| Hata | Sebep | Çözüm |
|------|-------|-------|
| 401 Unauthorized | Token yok/geçersiz | Swagger'da Authorize yapın |
| "Kayıt dönemi kapalı" | Deadline geçmiş veya DRAFT | Etkinlik PUBLISHED olmalı |
| "Bu etkinliğe zaten kayıtlısınız" | Çift kayıt | Farklı etkinlik deneyin |
| "Bu etkinliği düzenleme yetkiniz yok" | Organizatör değil | Kendi etkinliğinizi düzenleyin |
| "Bu kayıt ile giriş yapılamaz" | Zaten check-in veya iptal | Status kontrol edin |

---

## 🏷️ Event Categories

| Kategori | Açıklama |
|----------|----------|
| CONFERENCE | Konferanslar |
| WORKSHOP | Atölye çalışmaları |
| SEMINAR | Seminerler |
| SOCIAL | Sosyal etkinlikler |
| SPORTS | Spor etkinlikleri |
| CULTURAL | Kültürel etkinlikler |
| CAREER | Kariyer etkinlikleri |

---

## 🔄 Event Status Akışı

```
DRAFT → PUBLISHED → COMPLETED
           ↓
       CANCELLED
```

---

## 🧪 Test Verileri

Database'deki örnek veriler (seeds):

**Etkinlikler:**
- Yazılım Kariyer Günleri 2025 (CAREER, PUBLISHED)
- React.js Workshop (WORKSHOP, PUBLISHED, Ücretli: 50 TL)
- Yapay Zeka ve Gelecek Konferansı (CONFERENCE, PUBLISHED)
- Bahar Şenliği 2025 (SOCIAL, PUBLISHED)
- Fakülteler Arası Futbol Turnuvası (SPORTS, DRAFT)

**Kullanıcılar:**
- Admin: `admin@smartcampus.edu.tr` / `Admin123!`
- Öğrenci: `john.doe@smartcampus.edu.tr` / `Student123!`
