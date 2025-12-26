# Part 4 API Dokümantasyonu - Test Rehberi

## � Servis ve Port Bilgisi

| Servis | Port | Part 4 API'leri |
|--------|------|-----------------|
| **API Gateway** | 8080 | Tüm istekleri buradan yap |
| **Auth Service** | 8081 | Notification API (`/api/v1/notifications/*`) |
| **Academic Service** | 8082 | Analytics API (`/api/v1/analytics/*`) |

---

## �🔐 Ön Koşullar

1. **Servisleri başlat:**
```bash
# Backend root dizininde
docker-compose up -d

# Veya servisleri ayrı ayrı:
cd auth-service && mvn spring-boot:run
cd academic-service && mvn spring-boot:run
```

2. **Admin token al:**
```bash
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@smartcampus.edu.tr",
  "password": "password123"
}
```

> ⚠️ Response'taki `accessToken`'ı tüm isteklerde kullan!

---

## 📬 Notification API Endpoints

**Servis:** Auth Service (Port: 8081)  
**Gateway Üzerinden:** `http://localhost:8080/api/v1/notifications`  
**Direkt:** `http://localhost:8081/api/v1/notifications`  
**Auth:** Bearer Token (Header: `Authorization: Bearer <token>`)

---

### 1. Bildirimleri Listele
```http
GET /api/v1/notifications?page=0&size=20
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "type": "INFO",
        "category": "ACADEMIC",
        "title": "Yeni ders kaydı açıldı",
        "message": "2025 Bahar dönemi...",
        "isRead": false,
        "createdAt": "2025-12-26T10:00:00"
      }
    ],
    "totalElements": 14,
    "totalPages": 1
  }
}
```

---

### 2. Okunmamış Bildirim Sayısı
```http
GET /api/v1/notifications/unread-count
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "unreadCount": 5
  }
}
```

---

### 3. Bildirimi Okundu İşaretle
```http
PUT /api/v1/notifications/{id}/read
Authorization: Bearer <token>
```

**Örnek:** `PUT /api/v1/notifications/1/read`

---

### 4. Tüm Bildirimleri Okundu İşaretle
```http
PUT /api/v1/notifications/mark-all-read
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "message": "Tüm bildirimler okundu olarak işaretlendi",
  "data": {
    "markedCount": 5
  }
}
```

---

### 5. Bildirimi Sil
```http
DELETE /api/v1/notifications/{id}
Authorization: Bearer <token>
```

---

### 6. Bildirim Tercihlerini Getir
```http
GET /api/v1/notifications/preferences
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "emailAcademic": true,
    "emailAttendance": false,
    "emailMeal": true,
    "emailEvent": true,
    "emailPayment": true,
    "emailSystem": true,
    "pushAcademic": true,
    "pushAttendance": true,
    "pushMeal": true,
    "pushEvent": true,
    "pushPayment": true,
    "pushSystem": true,
    "smsAttendance": false,
    "smsPayment": false
  }
}
```

---

### 7. Bildirim Tercihlerini Güncelle
```http
PUT /api/v1/notifications/preferences
Authorization: Bearer <token>
Content-Type: application/json

{
  "emailAcademic": true,
  "emailAttendance": false,
  "emailMeal": true,
  "pushAcademic": true,
  "pushEvent": false
}
```

---

## 📊 Analytics API Endpoints (ADMIN ONLY)

**Servis:** Academic Service (Port: 8082)  
**Gateway Üzerinden:** `http://localhost:8080/api/v1/analytics`  
**Direkt:** `http://localhost:8082/api/v1/analytics`  
**Auth:** Bearer Token (Admin rolü gerekli)

---

### 1. Dashboard İstatistikleri
```http
GET /api/v1/analytics/dashboard
Authorization: Bearer <admin_token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalUsers": 25,
    "totalStudents": 20,
    "totalFaculty": 4,
    "totalAdmins": 1,
    "totalDepartments": 5,
    "totalCourses": 30,
    "totalSections": 45,
    "totalEnrollments": 120,
    "totalAttendanceSessions": 50,
    "averageAttendanceRate": 85.5,
    "totalMealReservationsToday": 15,
    "totalMealReservationsThisMonth": 200,
    "totalEvents": 10,
    "upcomingEvents": 3,
    "totalEventRegistrations": 45,
    "systemHealth": "healthy",
    "lastUpdated": "2025-12-26T16:00:00"
  }
}
```

---

### 2. Akademik İstatistikler
```http
GET /api/v1/analytics/academic
Authorization: Bearer <admin_token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "averageGpa": 2.85,
    "averageCgpa": 2.90,
    "highestGpa": 4.0,
    "lowestGpa": 1.5,
    "passRate": 78.5,
    "failRate": 21.5,
    "studentsAbove3": 8,
    "studentsBetween2And3": 10,
    "studentsBelow2": 2,
    "departmentStats": [...],
    "gradeDistribution": {
      "AA": 12.5,
      "BA": 18.0,
      "BB": 25.0,
      "CB": 15.0,
      "CC": 12.0,
      "DC": 8.0,
      "DD": 5.0,
      "FF": 4.5
    }
  }
}
```

---

### 3. Yoklama İstatistikleri
```http
GET /api/v1/analytics/attendance
Authorization: Bearer <admin_token>
```

---

### 4. Yemek İstatistikleri
```http
GET /api/v1/analytics/meals
Authorization: Bearer <admin_token>
```

---

### 5. Etkinlik İstatistikleri
```http
GET /api/v1/analytics/events
Authorization: Bearer <admin_token>
```

---

## 📥 Export Endpoints (ADMIN ONLY)

### Dashboard Export
```http
GET /api/v1/analytics/export/dashboard/excel
GET /api/v1/analytics/export/dashboard/csv
GET /api/v1/analytics/export/dashboard/pdf
Authorization: Bearer <admin_token>
```

### Academic Export
```http
GET /api/v1/analytics/export/academic/excel
Authorization: Bearer <admin_token>
```

> ⬇️ Bu endpoint'ler dosya indirir (Excel, CSV, PDF)

---

## 🔌 WebSocket Bağlantısı

**Endpoint:** `ws://localhost:8081/ws`

### JavaScript Örneği:
```javascript
const socket = new SockJS('http://localhost:8081/ws');
const stompClient = Stomp.over(socket);

stompClient.connect(
  { Authorization: 'Bearer <token>' },
  () => {
    // Bildirimlere subscribe ol
    stompClient.subscribe('/user/queue/notifications', (message) => {
      const notification = JSON.parse(message.body);
      console.log('Yeni bildirim:', notification);
    });

    // Okunmamış sayı güncellemelerine subscribe ol
    stompClient.subscribe('/user/queue/unread-count', (message) => {
      console.log('Okunmamış sayı:', message.body);
    });
  }
);
```

---

## Swagger UI Linkleri

- **Auth Service:** http://localhost:8081/swagger-ui.html
- **Academic Service:** http://localhost:8082/swagger-ui.html
- **API Gateway:** http://localhost:8080/swagger-ui.html

---

## Test Senaryoları

### Senaryo 1: Bildirim Akışı
1. Login yap → token al
2. `GET /notifications` → mevcut bildirimleri gör
3. `GET /notifications/unread-count` → okunmamış sayısı
4. `PUT /notifications/1/read` → bildirimi okundu yap
5. `PUT /notifications/mark-all-read` → hepsini okundu yap

### Senaryo 2: Tercih Yönetimi
1. `GET /notifications/preferences` → mevcut tercihleri gör
2. `PUT /notifications/preferences` → email tercihlerini güncelle

### Senaryo 3: Analytics (Admin)
1. Admin olarak login yap
2. `GET /analytics/dashboard` → genel istatistikler
3. `GET /analytics/export/dashboard/excel` → Excel indir

---

## ⚠️ Olası Hatalar

| HTTP Status | Anlam |
|-------------|-------|
| 401 | Token eksik veya geçersiz |
| 403 | Yetkisiz (örn: STUDENT analytics'e erişmeye çalışıyor) |
| 404 | Bildirim bulunamadı |
| 500 | Sunucu hatası |
