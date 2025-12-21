# Meal Service API Test Rehberi

Bu belge Swagger üzerinden meal-service API'lerini test etmek için örnek istekler içerir.

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

## 📋 Menu Controller

### Yemekhaneleri Listele (Public)
```
GET /api/v1/meals/cafeterias
```
Auth: ❌ Gerekmiyor

### Bugünkü Menüler (Public)
```
GET /api/v1/meals/menus/today
GET /api/v1/meals/menus/today?cafeteriaId=1
```
Auth: ❌ Gerekmiyor

### Belirli Tarihteki Menüler (Public)
```
GET /api/v1/meals/menus/date/2025-12-21?cafeteriaId=1
```
Auth: ❌ Gerekmiyor

### Haftalık Menü (Public)
```
GET /api/v1/meals/menus/weekly?cafeteriaId=1&startDate=2025-12-21
```
Auth: ❌ Gerekmiyor

### Menü Detayı (Public)
```
GET /api/v1/meals/menus/1
```
Auth: ❌ Gerekmiyor

---

## 💰 Wallet Controller

### Cüzdan Bilgisi
```
GET /api/v1/wallet
```
Auth: ✅ Gerekli

### Bakiye Sorgula
```
GET /api/v1/wallet/balance
```
Auth: ✅ Gerekli

### Para Yükle
```
POST /api/v1/wallet/topup
Content-Type: application/json

{
  "amount": 100.00,
  "paymentMethod": "CREDIT_CARD",
  "paymentReference": "TEST-PAY-001"
}
```
Auth: ✅ Gerekli

### İşlem Geçmişi
```
GET /api/v1/wallet/transactions?page=0&size=10
```
Auth: ✅ Gerekli

### Burs Durumu
```
GET /api/v1/wallet/scholarship
```
Auth: ✅ Gerekli

---

## 🍽️ Reservation Controller

### Rezervasyon Oluştur
```
POST /api/v1/meals/reservations
Content-Type: application/json

{
  "menuId": 1,
  "cafeteriaId": 1,
  "reservationDate": "2025-12-21",
  "mealType": "LUNCH",
  "useScholarship": false
}
```
Auth: ✅ Gerekli

### Yaklaşan Rezervasyonlarım
```
GET /api/v1/meals/reservations/upcoming
```
Auth: ✅ Gerekli

### Rezervasyon Detayı
```
GET /api/v1/meals/reservations/1
```
Auth: ✅ Gerekli

### Rezervasyonlarım (Paginated)
```
GET /api/v1/meals/reservations?page=0&size=10
```
Auth: ✅ Gerekli

### Rezervasyon İptal
```
DELETE /api/v1/meals/reservations/1
```
Auth: ✅ Gerekli

### QR Kod ile Rezervasyon Sorgula (Staff)
```
GET /api/v1/meals/reservations/qr/{qrCode}
```
Auth: ✅ Gerekli (ADMIN/FACULTY)

### Rezervasyon Kullan (Staff)
```
POST /api/v1/meals/reservations/use/{qrCode}
```
Auth: ✅ Gerekli (ADMIN/FACULTY)

---

## 📊 Test Senaryoları

### 1. Temel Akış (Yeni Kullanıcı)
1. `GET /api/v1/wallet` → Cüzdan oluşturur (bakiye: 0)
2. `POST /api/v1/wallet/topup` → 100 TL yükle
3. `GET /api/v1/meals/menus/today` → Menüleri gör
4. `POST /api/v1/meals/reservations` → Rezervasyon yap
5. `GET /api/v1/meals/reservations/upcoming` → Rezervasyonu kontrol et

### 2. Burs Kullanımı
1. Database'de wallet tablosunda `is_scholarship = TRUE` yapın
2. `GET /api/v1/wallet/scholarship` → `true` dönmeli
3. Rezervasyon oluştururken `"useScholarship": true` gönderin
4. Bakiyeden para düşmez, burs kullanılır

### 3. Staff QR Tarama
1. Kullanıcı rezervasyon yapar → QR kod alır
2. Staff `GET /api/v1/meals/reservations/qr/{qrCode}` ile sorgular
3. Staff `POST /api/v1/meals/reservations/use/{qrCode}` ile kullanır

---

## ⚠️ Sık Karşılaşılan Hatalar

| Hata | Sebep | Çözüm |
|------|-------|-------|
| 401 Unauthorized | Token yok/geçersiz | Swagger'da Authorize yapın |
| "Yetersiz bakiye" | Cüzdanda para yok | Önce topup yapın |
| "Menü yayınlanmamış" | is_published = false | DB'de menüyü yayınlayın |
| "Zaten rezervasyonunuz var" | Aynı gün/öğün | Farklı tarih deneyin |

---

## 🧪 Test Verileri

Database'deki örnek veriler:

**Yemekhaneler:** Ana Yemekhane (id: 1), Mühendislik Kafeteryası (id: 2)

**Menüler:** 2025-12-21 haftası için BREAKFAST/LUNCH/DINNER menüleri

**Kullanıcılar:**
- Admin: `admin@smartcampus.edu.tr` / `Admin123!`
- Öğrenci: `john.doe@smartcampus.edu.tr` / `Student123!`
