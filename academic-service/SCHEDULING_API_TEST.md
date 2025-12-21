# Scheduling API Test Rehberi

Bu belge Swagger üzerinden schedule ve classroom reservation API'lerini test etmek için örnek istekler içerir.

**Swagger URL:** `http://localhost:8082/swagger-ui.html`

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

## 📅 Schedule Controller

### Tüm Programları Listele (Public)
```
GET /api/v1/schedules
```
Auth: ❌ Gerekmiyor

### Program Detayı (Public)
```
GET /api/v1/schedules/{id}
GET /api/v1/schedules/1
```
Auth: ❌ Gerekmiyor

### Bölüme Göre Programlar (Public)
```
GET /api/v1/schedules/section/{sectionId}
GET /api/v1/schedules/section/1
```
Auth: ❌ Gerekmiyor

### Dersliğe Göre Programlar (Public)
```
GET /api/v1/schedules/classroom/{classroomId}
GET /api/v1/schedules/classroom/1
```
Auth: ❌ Gerekmiyor

### Güne Göre Programlar (Public)
```
GET /api/v1/schedules/day/{dayOfWeek}
GET /api/v1/schedules/day/MONDAY
GET /api/v1/schedules/day/TUESDAY
GET /api/v1/schedules/day/WEDNESDAY
GET /api/v1/schedules/day/THURSDAY
GET /api/v1/schedules/day/FRIDAY
GET /api/v1/schedules/day/SATURDAY
```
Auth: ❌ Gerekmiyor

### Çakışma Kontrolü
```
POST /api/v1/schedules/check-conflict?classroomId=1&dayOfWeek=MONDAY&startTime=09:00&endTime=10:00
```
Auth: ✅ Gerekli

Response:
```json
{
  "success": true,
  "data": {
    "hasConflict": false
  }
}
```

### Program Oluştur (Admin)
```
POST /api/v1/schedules
Content-Type: application/json

{
  "sectionId": 1,
  "dayOfWeek": "MONDAY",
  "startTime": "09:00:00",
  "endTime": "10:30:00",
  "classroomId": 1
}
```
Auth: ✅ Gerekli (ADMIN)

### Program Güncelle (Admin)
```
PUT /api/v1/schedules/{id}
Content-Type: application/json

{
  "sectionId": 1,
  "dayOfWeek": "TUESDAY",
  "startTime": "10:00:00",
  "endTime": "11:30:00",
  "classroomId": 2
}
```
Auth: ✅ Gerekli (ADMIN)

### Program Sil (Admin)
```
DELETE /api/v1/schedules/{id}
```
Auth: ✅ Gerekli (ADMIN)

---

## 🏫 Classroom Reservation Controller

### Rezervasyon Oluştur
```
POST /api/v1/classroom-reservations
Content-Type: application/json

{
  "classroomId": 1,
  "reservationDate": "2025-12-28",
  "startTime": "14:00:00",
  "endTime": "16:00:00",
  "purpose": "Proje toplantısı",
  "notes": "Yazılım ekibi toplantısı"
}
```
Auth: ✅ Gerekli

Response:
```json
{
  "success": true,
  "message": "Rezervasyon oluşturuldu, onay bekleniyor",
  "data": {
    "id": 1,
    "status": "PENDING",
    ...
  }
}
```

### Rezervasyon Detayı
```
GET /api/v1/classroom-reservations/{id}
```
Auth: ✅ Gerekli

### Benim Rezervasyonlarım
```
GET /api/v1/classroom-reservations/my
```
Auth: ✅ Gerekli

### Derslik Rezervasyonları (Belirli Tarih)
```
GET /api/v1/classroom-reservations/classroom/{classroomId}?date=2025-12-28
```
Auth: ✅ Gerekli

### Müsaitlik Kontrolü (Dolu Slotlar)
```
GET /api/v1/classroom-reservations/available?classroomId=1&date=2025-12-28
```
Auth: ✅ Gerekli

### Onay Bekleyen Rezervasyonlar (Admin)
```
GET /api/v1/classroom-reservations/pending?page=0&size=10
```
Auth: ✅ Gerekli (ADMIN)

### Rezervasyon Onayla (Admin)
```
POST /api/v1/classroom-reservations/{id}/approve
```
Auth: ✅ Gerekli (ADMIN)

### Rezervasyon Reddet (Admin)
```
POST /api/v1/classroom-reservations/{id}/reject?reason=Derslik bakımda
```
Auth: ✅ Gerekli (ADMIN)

### Rezervasyon İptal Et
```
DELETE /api/v1/classroom-reservations/{id}
```
Auth: ✅ Gerekli (Kendi rezervasyonu)

---

## 📊 Test Senaryoları

### 1. Program Oluşturma (Admin)
1. Admin olarak login ol
2. `GET /api/v1/sections` ile bölümleri listele → sectionId al
3. `GET /api/v1/classrooms` ile derslikleri listele → classroomId al
4. `POST /api/v1/schedules/check-conflict` ile çakışma kontrol et
5. `POST /api/v1/schedules` ile program oluştur
6. `GET /api/v1/schedules` ile kontrol et

### 2. Rezervasyon Akışı
1. Herhangi bir kullanıcı olarak login ol
2. `GET /api/v1/classroom-reservations/available` ile müsaitlik kontrol et
3. `POST /api/v1/classroom-reservations` ile rezervasyon oluştur → PENDING
4. Admin olarak login ol
5. `GET /api/v1/classroom-reservations/pending` ile bekleyenleri gör
6. `POST /api/v1/classroom-reservations/{id}/approve` ile onayla → APPROVED

### 3. Çakışma Testi
1. Aynı derslik, aynı gün, çakışan saat için iki program oluşturmayı dene
2. Hata almalısın: "Bu derslik ve saatte çakışma var!"

---

## ⚠️ Sık Karşılaşılan Hatalar

| Hata | Sebep | Çözüm |
|------|-------|-------|
| 401 Unauthorized | Token yok/geçersiz | Swagger'da Authorize yapın |
| 403 Forbidden | Yetki yetersiz | Admin hesabıyla deneyin |
| "Bu derslik ve saatte çakışma var!" | Zaman çakışması | Farklı saat/derslik seçin |
| "Bölüm bulunamadı" | Geçersiz sectionId | Mevcut section ID kullanın |
| "Derslik bulunamadı" | Geçersiz classroomId | Mevcut classroom ID kullanın |
| "Bu rezervasyon zaten işlenmiş" | PENDING değil | Sadece PENDING onayla/reddet |

---

## 🗓️ DayOfWeek Değerleri

| Değer | Açıklama |
|-------|----------|
| MONDAY | Pazartesi |
| TUESDAY | Salı |
| WEDNESDAY | Çarşamba |
| THURSDAY | Perşembe |
| FRIDAY | Cuma |
| SATURDAY | Cumartesi |

---

## 🔄 Reservation Status Akışı

```
PENDING ──┬──► APPROVED
          │
          └──► REJECTED

APPROVED ───► CANCELLED (kullanıcı iptal)
PENDING  ───► CANCELLED (kullanıcı iptal)
```

---

## 🧪 Örnek Section ve Classroom ID'leri

Database'deki mevcut verilerle test için:
- **Section ID:** 1-10 (course_sections tablosundan)
- **Classroom ID:** 1-5 (classrooms tablosundan)

Mevcut verileri görmek için:
```
GET /api/v1/sections
GET /api/v1/classrooms
```

---

## 👤 Test Kullanıcıları

| Rol | Email | Şifre |
|-----|-------|-------|
| Admin | admin@smartcampus.edu.tr | Admin123! |
| Faculty | prof.yilmaz@smartcampus.edu.tr | Faculty123! |
| Student | john.doe@smartcampus.edu.tr | Student123! |
