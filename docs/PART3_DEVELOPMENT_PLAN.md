# Part 3 Geliştirme Planı
## Akıllı Kampüs - Yemek Servisi, Etkinlik ve Çizelgeleme

**Başlangıç Tarihi:** 20 Aralık 2025
**Teslim Tarihi:** 22 Aralık 2025 (Pazar) 23:59
**Ağırlık:** %25

---

## 📋 Genel Bakış

Part 3'te üç ana modül geliştirilecek:

| Modül | Ağırlık | Öncelik |
|-------|---------|---------|
| Meal Reservation System | 30% | P1 |
| Event Management | 25% | P1 |
| Course Scheduling | 25% | P1 |
| Frontend Sayfaları | 12% | P1 |
| Testing | 5% | P2 |
| Dokümantasyon | 3% | P2 |

---

## 🍽️ Modül 1: Meal Reservation System (Yemek Servisi)

### 1.1 Veritabanı Tabloları

```sql
-- Yemekhaneler
cafeterias (id, name, location, capacity)

-- Yemek menüleri
meal_menus (id, cafeteria_id, date, meal_type, items_json, nutrition_json, is_published)

-- Yemek rezervasyonları
meal_reservations (id, user_id, menu_id, cafeteria_id, meal_type, date, amount, qr_code, status, used_at)

-- Cüzdanlar
wallets (id, user_id, balance, currency, is_active)

-- İşlemler
transactions (id, wallet_id, type, amount, balance_after, reference_type, reference_id, description)
```

### 1.2 Backend Endpoints

#### Menü Yönetimi
- `GET /api/v1/meals/menus` - Menü listesi (tarih filtresi)
- `GET /api/v1/meals/menus/:id` - Menü detayı
- `POST /api/v1/meals/menus` - Menü oluşturma (admin)
- `PUT /api/v1/meals/menus/:id` - Menü güncelleme
- `DELETE /api/v1/meals/menus/:id` - Menü silme

#### Rezervasyon
- `POST /api/v1/meals/reservations` - Yemek rezervasyonu
  - Burs/ücretli öğrenci ayrımı
  - Günlük kota kontrolü (burslu: max 2 öğün/gün)
  - Cüzdan bakiye kontrolü (ücretli)
  - Unique QR kod oluşturma
  - Bildirim gönderme
- `DELETE /api/v1/meals/reservations/:id` - Rezervasyon iptali (2 saat öncesi)
- `GET /api/v1/meals/reservations/my-reservations` - Rezervasyonlarım
- `POST /api/v1/meals/reservations/:id/use` - Yemek kullanımı (QR okutma)

#### Cüzdan
- `GET /api/v1/wallet/balance` - Bakiye sorgulama
- `POST /api/v1/wallet/topup` - Para yükleme (ödeme gateway)
- `POST /api/v1/wallet/topup/webhook` - Ödeme callback
- `GET /api/v1/wallet/transactions` - İşlem geçmişi

### 1.3 Frontend Sayfaları

1. **Menu Page** (`/meals/menu`)
   - Takvim görünümü, öğle/akşam menüleri
   - Besin değerleri, vegan/vejetaryen badgeleri
   - "Rezerve Et" butonu

2. **My Reservations** (`/meals/reservations`)
   - QR kod gösterimi (tam ekran)
   - İptal butonu, durum badgeleri

3. **Wallet Page** (`/wallet`)
   - Bakiye, para yükleme, işlem geçmişi

4. **QR Scanner** (`/meals/scan`) - Personel
   - Kamera ile QR okuma
   - Doğrulama ve kullanım onayı

---

## 🎉 Modül 2: Event Management (Etkinlik Yönetimi)

### 2.1 Veritabanı Tabloları

```sql
-- Etkinlikler
events (id, title, description, category, date, start_time, end_time, 
        location, capacity, registered_count, registration_deadline, 
        is_paid, price, status)

-- Etkinlik kayıtları
event_registrations (id, event_id, user_id, registration_date, qr_code, 
                     checked_in, checked_in_at, custom_fields_json)
```

### 2.2 Backend Endpoints

#### Etkinlik CRUD
- `GET /api/v1/events` - Etkinlik listesi (kategori, tarih filtresi)
- `GET /api/v1/events/:id` - Etkinlik detayı
- `POST /api/v1/events` - Etkinlik oluşturma (admin)
- `PUT /api/v1/events/:id` - Etkinlik güncelleme
- `DELETE /api/v1/events/:id` - Etkinlik silme

#### Kayıt İşlemleri
- `POST /api/v1/events/:id/register` - Etkinliğe kayıt
  - Kapasite kontrolü
  - Bekleme listesi (bonus)
  - QR kod oluşturma
  - E-posta bildirimi
- `DELETE /api/v1/events/:eventId/registrations/:regId` - Kayıt iptali
- `GET /api/v1/events/:id/registrations` - Kayıtlı kullanıcılar (yönetici)
- `POST /api/v1/events/:eventId/registrations/:regId/checkin` - QR ile giriş

### 2.3 Frontend Sayfaları

1. **Events Page** (`/events`)
   - Etkinlik kartları, kategori filtreleme
   
2. **Event Detail** (`/events/:id`)
   - Detaylar, kalan kontenjan, kayıt butonu

3. **My Events** (`/my-events`)
   - Kayıtlı etkinlikler, QR kodlar

4. **Event Check-in** (`/events/checkin`) - Yönetici
   - QR tarama, katılımcı listesi

---

## 📅 Modül 3: Course Scheduling (Ders Çizelgeleme)

### 3.1 Veritabanı Tabloları

```sql
-- Ders programları
schedules (id, section_id, day_of_week, start_time, end_time, classroom_id)

-- Derslik rezervasyonları
reservations (id, classroom_id, user_id, date, start_time, end_time, 
              purpose, status, approved_by)
```

### 3.2 Backend Endpoints

#### Otomatik Program Oluşturma
- `POST /api/v1/scheduling/generate` - CSP algoritması ile program oluşturma
  - **Hard Constraints:**
    - Öğretim üyesi çakışması yok
    - Derslik çakışması yok
    - Öğrenci ders çakışması yok
    - Derslik kapasitesi yeterli
  - **Soft Constraints:**
    - Öğretim üyesi tercihleri
    - Öğrenci boşluklarını minimize et
    - Dersleri haftaya eşit dağıt

#### Program Görüntüleme
- `GET /api/v1/scheduling/:scheduleId` - Program görüntüleme
- `GET /api/v1/scheduling/my-schedule` - Benim programım
- `GET /api/v1/scheduling/my-schedule/ical` - iCal export (.ics)

#### Derslik Rezervasyonu
- `POST /api/v1/reservations` - Derslik rezerve etme
- `GET /api/v1/reservations` - Rezervasyon listesi
- `PUT /api/v1/reservations/:id/approve` - Onaylama (admin)
- `PUT /api/v1/reservations/:id/reject` - Reddetme (admin)

### 3.3 Frontend Sayfaları

1. **My Schedule** (`/schedule`)
   - Haftalık takvim görünümü (FullCalendar)
   - iCal export butonu

2. **Generate Schedule** (`/admin/scheduling/generate`) - Admin
   - Dönem/yıl seçimi, program oluşturma

3. **Classroom Reservations** (`/reservations`)
   - Derslik listesi, rezervasyon formu

---

## 🛠️ Geliştirme Aşamaları

### Aşama 1: Veritabanı ve Altyapı (1-2 saat)
- [ ] Yeni tabloların migration'larını oluştur
- [ ] Entity sınıflarını yaz
- [ ] Repository'leri oluştur
- [ ] Seed data ekle

### Aşama 2: Meal Service Backend (3-4 saat)
- [ ] Menu CRUD endpoints
- [ ] Wallet entity ve endpoints
- [ ] Reservation logic (kota, bakiye kontrolü)
- [ ] QR kod oluşturma servisi
- [ ] Yemek kullanımı (QR doğrulama)

### Aşama 3: Event Management Backend (2-3 saat)
- [ ] Event CRUD endpoints
- [ ] Registration logic (kapasite kontrolü)
- [ ] Check-in sistemi
- [ ] E-posta bildirimleri

### Aşama 4: Course Scheduling Backend (3-4 saat)
- [ ] Schedule entity ve CRUD
- [ ] CSP algoritması implementasyonu
- [ ] My schedule endpoint
- [ ] iCal export
- [ ] Classroom reservation

### Aşama 5: Frontend Sayfaları (4-5 saat)
- [ ] Meal pages (menu, reservations, wallet, scanner)
- [ ] Event pages (list, detail, my-events, checkin)
- [ ] Schedule pages (my-schedule, reservations)
- [ ] QR kod bileşenleri

### Aşama 6: Testing & Dokümantasyon (2 saat)
- [ ] Integration testleri
- [ ] API dokümantasyonu
- [ ] Kullanıcı kılavuzu

---

## 🎯 Öncelik Sıralaması

> **ÖNEMLİ:** Zaman kısıtlı olduğundan, aşağıdaki öncelik sırasına göre ilerliyoruz.

### Yüksek Öncelik (Must Have)
1. ✅ Meal Service - Temel rezervasyon sistemi
2. ✅ Event Management - Kayıt sistemi
3. ✅ Course Scheduling - Program görüntüleme

### Orta Öncelik (Should Have)
4. ⚠️ Wallet sistemi (basit bakiye yönetimi)
5. ⚠️ QR kod oluşturma/okuma
6. ⚠️ CSP algoritması (basit versiyon)

### Düşük Öncelik (Nice to Have)
7. 🔄 Ödeme gateway entegrasyonu
8. 🔄 iCal export
9. 🔄 Bekleme listesi

---

## 📁 Yeni Microservice'ler

```
smart-campus-backend/
├── meal-service/           # Yeni microservice
│   ├── src/main/java/
│   │   ├── controller/
│   │   │   ├── MenuController.java
│   │   │   ├── ReservationController.java
│   │   │   └── WalletController.java
│   │   ├── entity/
│   │   │   ├── Cafeteria.java
│   │   │   ├── MealMenu.java
│   │   │   ├── MealReservation.java
│   │   │   ├── Wallet.java
│   │   │   └── Transaction.java
│   │   └── service/
│   └── Dockerfile

├── event-service/          # Yeni microservice
│   ├── src/main/java/
│   │   ├── controller/
│   │   │   ├── EventController.java
│   │   │   └── RegistrationController.java
│   │   ├── entity/
│   │   │   ├── Event.java
│   │   │   └── EventRegistration.java
│   │   └── service/
│   └── Dockerfile

├── scheduling-service/     # Yeni microservice
│   ├── src/main/java/
│   │   ├── controller/
│   │   │   ├── ScheduleController.java
│   │   │   └── ReservationController.java
│   │   ├── entity/
│   │   │   ├── Schedule.java
│   │   │   └── ClassroomReservation.java
│   │   └── service/
│   └── Dockerfile
```

---

## 📝 Sonraki Adım

Kullanıcı onayı sonrasında **Aşama 1: Veritabanı ve Altyapı** ile başlayacağız.

Hangi modülle başlamak istiyorsunuz?
1. 🍽️ Meal Service
2. 🎉 Event Management  
3. 📅 Course Scheduling
