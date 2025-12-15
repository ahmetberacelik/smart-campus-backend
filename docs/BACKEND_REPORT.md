<initial_code>
# Smart Campus Backend - Teknik Rapor

## 1. Genel Bakış

Bu doküman, Akıllı Kampüs Yönetim Sistemi'nin backend mimarisini, teknoloji kararlarını ve Part 1 & Part 2 kapsamındaki geliştirme planını içerir.

| Özellik | Değer |
|---------|-------|
| **Proje Adı** | Smart Campus Backend |
| **Mimari** | Mikroservis |
| **Part** | Part 2 - Akademik Yönetim ve GPS Yoklama |
| **Teslim Tarihi** | 15 Aralık 2025 |

---

## 2. Teknoloji Stack

### 2.1 Core Teknolojiler

| Teknoloji | Versiyon | Açıklama |
|-----------|----------|----------|
| **Java** | 17 (LTS) | Programlama dili |
| **Spring Boot** | 3.2.x | Backend framework |
| **Maven** | Latest | Build tool ve dependency yönetimi |
| **MySQL** | 8.0 | İlişkisel veritabanı |

### 2.2 Spring Ekosistemi

| Modül | Kullanım Amacı |
|-------|----------------|
| **Spring Web** | REST API geliştirme |
| **Spring Security** | Authentication & Authorization |
| **Spring Data JPA** | Veritabanı işlemleri |
| **Spring Cloud Gateway** | API Gateway |
| **Spring Mail** | Email gönderimi |
| **Spring Validation** | Input validation |

### 2.3 Ek Kütüphaneler

| Kütüphane | Kullanım Amacı |
|-----------|----------------|
| **JWT (jjwt)** | Token tabanlı authentication |
| **BCrypt** | Şifre hashleme |
| **Lombok** | Boilerplate kod azaltma |
| **AWS S3 SDK** | DigitalOcean Spaces entegrasyonu |
| **Springdoc OpenAPI** | API dokümantasyonu (Swagger) |
| **PDFBox/iText** | PDF oluşturma (Transkript) |
| **QRGen/ZXing** | QR kod oluşturma ve okuma |

---

## 3. Mimari Yapı

### 3.1 Mikroservis Mimarisi

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENTS                                  │
│              (Web Browser, Mobile App, etc.)                     │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API GATEWAY                                 │
│                 (Spring Cloud Gateway)                           │
│                      Port: 8080                                  │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       SERVICES                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                   AUTH SERVICE                           │   │
│  │                    Port: 8081                            │   │
│  │                                                          │   │
│  │  • User Registration                                     │   │
│  │  • Login / Logout                                        │   │
│  │  • JWT Token Management                                  │   │
│  │  • Email Verification                                    │   │
│  │  • Password Reset                                        │   │
│  │  • Profile Management                                    │   │
│  │  • Profile Picture Upload                                │   │
│  └─────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                 ACADEMIC SERVICE                         │   │
│  │                    Port: 8082                            │   │
│  │                                                          │   │
│  │  • Course Management                                     │   │
│  │  • Section Management                                    │   │
│  │  • Enrollment (Ders Kayıt)                               │   │
│  │  • Grade Management                                      │   │
│  │  • Transcript Generation                                 │   │
│  │  • Prerequisite Checking                                 │   │
│  └─────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                ATTENDANCE SERVICE                        │   │
│  │                    Port: 8083                            │   │
│  │                                                          │   │
│  │  • GPS-Based Attendance                                  │   │
│  │  • QR Code Attendance                                    │   │
│  │  • Spoofing Detection                                    │   │
│  │  • Excuse Management                                     │   │
│  │  • Attendance Reports                                    │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────┬───────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│     MySQL       │  │   DO Spaces     │  │   Gmail SMTP    │
│   Database      │  │  File Storage   │  │  Email Service  │
│   Port: 3306    │  │                 │  │                 │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

### 3.2 Part 2 Servis Yapısı

Part 2 için ek servisler eklendi:

| Servis | Port | Sorumluluk |
|--------|------|------------|
| **api-gateway** | 8080 | Request routing, load balancing |
| **auth-service** | 8081 | Authentication, User Management |
| **academic-service** | 8082 | Course, Enrollment, Grade Management |
| **attendance-service** | 8083 | GPS Attendance, QR Code, Excuse |

### 3.3 Service Discovery

Service Discovery (Eureka/Consul) **kullanılmayacaktır**. Servisler arası iletişim Docker Compose network üzerinden sağlanacaktır.

```yaml
# Docker network üzerinden servis erişimi
auth-service: http://auth-service:8081
academic-service: http://academic-service:8082
attendance-service: http://attendance-service:8083
mysql: jdbc:mysql://smart_campus_db:3306
```

---

## 4. API Gateway

### 4.1 Yapılandırma

Spring Cloud Gateway, tüm isteklerin tek giriş noktası olarak görev yapacaktır.

| Özellik | Değer |
|---------|-------|
| **Framework** | Spring Cloud Gateway |
| **Port** | 8080 |
| **Görevler** | Routing, CORS, Rate Limiting |

### 4.2 Route Yapısı (Part 1 + Part 2)

```
/api/v1/auth/**         →  auth-service:8081
/api/v1/users/**        →  auth-service:8081
/api/v1/departments/**  →  auth-service:8081
/api/v1/courses/**      →  academic-service:8082
/api/v1/sections/**     →  academic-service:8082
/api/v1/enrollments/**  →  academic-service:8082
/api/v1/grades/**       →  academic-service:8082
/api/v1/attendance/**   →  attendance-service:8083
/api/v1/classrooms/**   →  attendance-service:8083
```

---

## 5. Veritabanı

### 5.1 Bağlantı Bilgileri

| Özellik | Değer |
|---------|-------|
| **DBMS** | MySQL 8.0 |
| **Host** | smart_campus_db (Docker network) / 138.68.99.35 (Lokal) |
| **Port** | 3306 |
| **Database** | smart_campus |
| **Charset** | UTF8MB4 |

### 5.2 Part 1 Tabloları

| Tablo | Açıklama |
|-------|----------|
| `departments` | Akademik bölümler |
| `users` | Tüm kullanıcıların temel bilgileri |
| `students` | Öğrenci akademik bilgileri |
| `faculty` | Öğretim üyesi bilgileri |
| `refresh_tokens` | JWT refresh token'ları |
| `email_verification_tokens` | Email doğrulama token'ları |
| `password_reset_tokens` | Şifre sıfırlama token'ları |

### 5.3 Part 2 Tabloları

| Tablo | Açıklama |
|-------|----------|
| `courses` | Ders bilgileri (kod, ad, kredi, ECTS) |
| `course_prerequisites` | Ders önkoşulları (self-join) |
| `course_sections` | Ders bölümleri (şube, dönem, öğretim üyesi) |
| `enrollments` | Öğrenci ders kayıtları |
| `classrooms` | Derslik bilgileri (GPS koordinatları dahil) |
| `attendance_sessions` | Yoklama oturumları |
| `attendance_records` | Yoklama kayıtları |
| `excuse_requests` | Mazeret başvuruları |

### 5.4 Migration Yönetimi

Database şeması **manuel olarak** yönetilecektir. Migration dosyaları database reposundaki `init.sql` ile kontrol edilir.

> **Not:** Flyway kullanılmayacaktır.

---

## 6. Authentication & Security

### 6.1 JWT Yapılandırması

| Token Tipi | Süre | Kullanım |
|------------|------|----------|
| **Access Token** | 15 dakika | API isteklerinde Authorization header |
| **Refresh Token** | 7 gün | Access token yenileme |

### 6.2 Şifre Güvenliği

| Özellik | Değer |
|---------|-------|
| **Algoritma** | BCrypt |
| **Salt Rounds** | 10 |
| **Minimum Uzunluk** | 8 karakter |
| **Gereksinimler** | Büyük harf, küçük harf, rakam |

### 6.3 Rol Tabanlı Erişim (RBAC)

| Rol | Açıklama |
|-----|----------|
| `STUDENT` | Öğrenci kullanıcılar |
| `FACULTY` | Öğretim üyeleri |
| `ADMIN` | Sistem yöneticileri |

---

## 7. File Storage

### 7.1 DigitalOcean Spaces

| Özellik | Değer |
|---------|-------|
| **Servis** | DigitalOcean Spaces |
| **Protokol** | S3 Compatible API |
| **Region** | Frankfurt (fra1) |
| **Kullanım** | Profil fotoğrafları, mazeret belgeleri, syllabus PDF'leri |

### 7.2 Entegrasyon

AWS S3 SDK kullanılarak DigitalOcean Spaces'e bağlanılacaktır.

```
Upload Flow:
User → Backend API → DigitalOcean Spaces → CDN URL döner
```

---

## 8. Email Servisi

### 8.1 Gmail SMTP

| Özellik | Değer |
|---------|-------|
| **Servis** | Gmail SMTP |
| **Host** | smtp.gmail.com |
| **Port** | 587 |
| **Encryption** | TLS |
| **Authentication** | App Password |

### 8.2 Email Kullanım Alanları (Part 1 + Part 2)

- Email doğrulama
- Şifre sıfırlama
- Hoş geldin emaili
- Ders kayıt onayı
- Devamsızlık uyarıları
- Not bildirimleri

---

## 9. Deployment

### 9.1 Sunucu Bilgileri

| Özellik | Değer |
|---------|-------|
| **Provider** | DigitalOcean |
| **Sunucu Tipi** | Droplet (VM) |
| **IP Adresi** | 138.68.99.35 |
| **OS** | Ubuntu 22.04 LTS |

### 9.2 Docker Yapısı

Tüm servisler Docker container olarak çalışacaktır.

```
┌─────────────────────────────────────────────────────────────────┐
│                    DigitalOcean Droplet                          │
│                      138.68.99.35                                │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                  Docker Network                            │  │
│  │                                                            │  │
│  │   ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │  │
│  │   │ api-gateway │  │auth-service │  │ smart_campus_db │  │  │
│  │   │   :8080     │  │   :8081     │  │     :3306       │  │  │
│  │   └─────────────┘  └─────────────┘  └─────────────────┘  │  │
│  │                                                            │  │
│  │   ┌─────────────────┐  ┌─────────────────────────────┐   │  │
│  │   │academic-service │  │   attendance-service        │   │  │
│  │   │     :8082       │  │        :8083                │   │  │
│  │   └─────────────────┘  └─────────────────────────────┘   │  │
│  │                                                            │  │
│  │   ┌─────────────────┐                                     │  │
│  │   │   phpmyadmin    │                                     │  │
│  │   │     :8084       │                                     │  │
│  │   └─────────────────┘                                     │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 9.3 Port Yapılandırması

| Servis | Internal Port | External Port |
|--------|----------------|----------------|
| API Gateway | 8080 | 8080 |
| Auth Service | 8081 | - (internal) |
| Academic Service | 8082 | - (internal) |
| Attendance Service | 8083 | - (internal) |
| MySQL | 3306 | - (internal) |
| phpMyAdmin | 8084 | 8084 (dev only) |

---

## 10. Part 1 - API Endpoints

### 10.1 Authentication Endpoints

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | `/api/v1/auth/register` | Kullanıcı kaydı |
| POST | `/api/v1/auth/verify-email` | Email doğrulama |
| POST | `/api/v1/auth/login` | Kullanıcı girişi |
| POST | `/api/v1/auth/refresh` | Token yenileme |
| POST | `/api/v1/auth/logout` | Çıkış yapma |
| POST | `/api/v1/auth/forgot-password` | Şifre sıfırlama isteği |
| POST | `/api/v1/auth/reset-password` | Şifre sıfırlama |
| POST | `/api/v1/auth/resend-verification` | Doğrulama emaili tekrar gönder |

### 10.2 User Management Endpoints

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/api/v1/users/me` | Profil görüntüleme |
| PUT | `/api/v1/users/me` | Profil güncelleme |
| POST | `/api/v1/users/me/change-password` | Şifre değiştirme |
| POST | `/api/v1/users/me/profile-picture` | Profil fotoğrafı yükleme |
| DELETE | `/api/v1/users/me/profile-picture` | Profil fotoğrafı silme |
| GET | `/api/v1/users` | Kullanıcı listesi (Admin) |
| GET | `/api/v1/users/{id}` | Kullanıcı detayı (Admin) |

### 10.3 Department Endpoints

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/api/v1/departments` | Bölüm listesi |
| GET | `/api/v1/departments/{id}` | Bölüm detayı |
| GET | `/api/v1/departments/code/{code}` | Bölüm detayı (kod ile) |

---

## 11. Part 2 - API Endpoints

### 11.1 Course Management Endpoints

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/api/v1/courses` | Ders listesi (pagination, filtering, search) |
| GET | `/api/v1/courses/{id}` | Ders detayları (önkoşullar dahil) |
| POST | `/api/v1/courses` | Ders oluşturma (Admin) |
| PUT | `/api/v1/courses/{id}` | Ders güncelleme (Admin) |
| DELETE | `/api/v1/courses/{id}` | Ders silme - soft delete (Admin) |

### 11.2 Course Section Endpoints

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/api/v1/sections` | Section listesi (filtering by semester, instructor) |
| GET | `/api/v1/sections/{id}` | Section detayları |
| POST | `/api/v1/sections` | Section oluşturma (Admin) |
| PUT | `/api/v1/sections/{id}` | Section güncelleme (Admin) |

### 11.3 Enrollment Endpoints

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | `/api/v1/enrollments` | Derse kayıt olma (Student) |
| DELETE | `/api/v1/enrollments/{id}` | Dersi bırakma (Student) |
| GET | `/api/v1/enrollments/my-courses` | Kayıtlı derslerim (Student) |
| GET | `/api/v1/enrollments/students/{sectionId}` | Dersin öğrenci listesi (Faculty) |

### 11.4 Grade Endpoints

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/api/v1/grades/my-grades` | Notlarım (Student) |
| GET | `/api/v1/grades/transcript` | Transkript JSON (Student) |
| GET | `/api/v1/grades/transcript/pdf` | Transkript PDF (Student) |
| POST | `/api/v1/grades` | Not girişi (Faculty) |

### 11.5 Attendance Session Endpoints (Faculty)

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | `/api/v1/attendance/sessions` | Yoklama oturumu açma |
| GET | `/api/v1/attendance/sessions/{id}` | Oturum detayları |
| PUT | `/api/v1/attendance/sessions/{id}/close` | Oturumu kapatma |
| GET | `/api/v1/attendance/sessions/my-sessions` | Benim oturumlarım |
| GET | `/api/v1/attendance/report/{sectionId}` | Yoklama raporu |

### 11.6 Attendance Check-in Endpoints (Student)

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | `/api/v1/attendance/sessions/{id}/checkin` | GPS ile yoklama verme |
| POST | `/api/v1/attendance/sessions/{id}/checkin-qr` | QR kod ile yoklama verme |
| GET | `/api/v1/attendance/my-attendance` | Yoklama durumum |

### 11.7 Excuse Request Endpoints

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | `/api/v1/attendance/excuse-requests` | Mazeret bildirme (Student) |
| GET | `/api/v1/attendance/excuse-requests` | Mazeret listesi (Faculty) |
| PUT | `/api/v1/attendance/excuse-requests/{id}/approve` | Mazeret onaylama (Faculty) |
| PUT | `/api/v1/attendance/excuse-requests/{id}/reject` | Mazeret reddetme (Faculty) |

### 11.8 Classroom Endpoints

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/api/v1/classrooms` | Derslik listesi |
| GET | `/api/v1/classrooms/{id}` | Derslik detayı |

---

## 12. Part 2 - Business Logic ve Algoritmalar

### 12.1 Önkoşul Kontrolü (Prerequisite Checking)

Recursive (DFS) algoritma ile tüm önkoşul zinciri kontrol edilir:

```java
public boolean checkPrerequisites(Long courseId, Long studentId) {
    List<Course> prerequisites = courseRepository.findPrerequisites(courseId);
    for (Course prereq : prerequisites) {
        if (!hasCompletedCourse(studentId, prereq.getId())) {
            return false;
        }
        // Recursive check
        if (!checkPrerequisites(prereq.getId(), studentId)) {
            return false;
        }
    }
    return true;
}
```

### 12.2 Ders Programı Çakışma Kontrolü (Schedule Conflict Detection)

Time overlap detection algoritması:

```java
public boolean hasScheduleConflict(Schedule schedule1, Schedule schedule2) {
    for (DaySchedule day1 : schedule1.getDays()) {
        DaySchedule day2 = schedule2.getDay(day1.getDay());
        if (day2 != null) {
            for (TimeSlot slot1 : day1.getSlots()) {
                for (TimeSlot slot2 : day2.getSlots()) {
                    if (overlaps(slot1, slot2)) {
                        return true;
                    }
                }
            }
        }
    }
    return false;
}

private boolean overlaps(TimeSlot slot1, TimeSlot slot2) {
    return slot1.getStart().isBefore(slot2.getEnd()) 
        && slot2.getStart().isBefore(slot1.getEnd());
}
```

### 12.3 Kapasite Kontrolü (Atomic Update)

Row-level locking ile atomic kapasite güncelleme:

```sql
UPDATE course_sections 
SET enrolled_count = enrolled_count + 1 
WHERE id = ? AND enrolled_count < capacity;
-- Affected rows = 0 ise kapasite dolu
```

### 12.4 Haversine Formülü (GPS Mesafe Hesaplama)

İki GPS koordinatı arasındaki mesafeyi metre cinsinden hesaplar:

```java
public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final int R = 6371000; // Dünya yarıçapı (metre)
    
    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    
    double c = 2 * Math.asin(Math.sqrt(a));
    
    return R * c; // metre cinsinden mesafe
}
```

### 12.5 GPS Spoofing Tespiti

Çoklu kontrol ile sahte konum tespiti:

1. **IP Kontrolü**: Kampüs IP aralığı kontrolü
2. **Velocity Check**: Önceki konumdan impossible travel tespiti
3. **Accuracy Check**: GPS doğruluk değeri kontrolü (<50m)
4. **Mock Location Flag**: Cihazın mock location ayarı kontrolü

```java
public SpoofingResult detectSpoofing(Long studentId, double lat, double lon, double accuracy) {
    // 1. IP kontrolü
    if (!isFromCampusNetwork(request.getRemoteAddr())) {
        return new SpoofingResult(true, "OUTSIDE_CAMPUS_NETWORK");
    }
    
    // 2. Velocity check
    AttendanceRecord lastRecord = getLastRecord(studentId);
    if (lastRecord != null) {
        double distance = calculateDistance(lastRecord.getLat(), lastRecord.getLon(), lat, lon);
        long timeDiff = Duration.between(lastRecord.getTime(), Instant.now()).getSeconds();
        double maxPossibleDistance = timeDiff * MAX_WALKING_SPEED; // 5 m/s
        
        if (distance > maxPossibleDistance) {
            return new SpoofingResult(true, "IMPOSSIBLE_TRAVEL");
        }
    }
    
    // 3. Accuracy kontrolü
    if (accuracy > 50) {
        return new SpoofingResult(true, "LOW_GPS_ACCURACY");
    }
    
    return new SpoofingResult(false, null);
}
```

### 12.6 Harf Notu Hesaplama (Grade Calculation)

Otomatik harf notu ve grade point hesaplama:

```java
public GradeResult calculateGrade(double midterm, double finalGrade) {
    double average = (midterm * 0.4) + (finalGrade * 0.6);
    
    String letterGrade;
    double gradePoint;
    
    if (average >= 90) { letterGrade = "AA"; gradePoint = 4.0; }
    else if (average >= 85) { letterGrade = "BA"; gradePoint = 3.5; }
    else if (average >= 80) { letterGrade = "BB"; gradePoint = 3.0; }
    else if (average >= 75) { letterGrade = "CB"; gradePoint = 2.5; }
    else if (average >= 70) { letterGrade = "CC"; gradePoint = 2.0; }
    else if (average >= 65) { letterGrade = "DC"; gradePoint = 1.5; }
    else if (average >= 60) { letterGrade = "DD"; gradePoint = 1.0; }
    else { letterGrade = "FF"; gradePoint = 0.0; }
    
    return new GradeResult(average, letterGrade, gradePoint);
}
```

---

## 13. Proje Yapısı

### 13.1 Repository Yapısı

```
smart-campus-backend/
├── api-gateway/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/smartcampus/gateway/
│   │       └── resources/
│   ├── Dockerfile
│   └── pom.xml
│
├── auth-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/smartcampus/auth/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── academic-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/smartcampus/academic/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── exception/
│   │   │   │   ├── repository/
│   │   │   │   ├── service/
│   │   │   │   └── AcademicServiceApplication.java
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── attendance-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/smartcampus/attendance/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── exception/
│   │   │   │   ├── repository/
│   │   │   │   ├── service/
│   │   │   │   ├── util/
│   │   │   │   └── AttendanceServiceApplication.java
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── docs/
│   ├── BACKEND_REPORT.md
│   ├── DATABASE_SCHEMA.md
│   ├── API_DOCUMENTATION.md
│   └── ...
│
├── docker-compose.yml
├── pom.xml (parent)
└── README.md
```

### 13.2 Package Yapısı (academic-service)

```
com.smartcampus.academic
├── config/           # Configuration classes
├── controller/       # REST controllers
│   ├── CourseController.java
│   ├── SectionController.java
│   ├── EnrollmentController.java
│   └── GradeController.java
├── dto/              # Data Transfer Objects
│   ├── request/
│   └── response/
├── entity/           # JPA entities
│   ├── Course.java
│   ├── CourseSection.java
│   ├── Enrollment.java
│   └── CoursePrerequisite.java
├── exception/        # Custom exceptions
├── repository/       # JPA repositories
├── service/          # Business logic
│   ├── CourseService.java
│   ├── EnrollmentService.java
│   ├── GradeService.java
│   ├── PrerequisiteService.java
│   └── ScheduleConflictService.java
└── util/             # Utility classes
```

### 13.3 Package Yapısı (attendance-service)

```
com.smartcampus.attendance
├── config/           # Configuration classes
├── controller/       # REST controllers
│   ├── AttendanceSessionController.java
│   ├── CheckInController.java
│   ├── ExcuseRequestController.java
│   └── ClassroomController.java
├── dto/              # Data Transfer Objects
├── entity/           # JPA entities
│   ├── AttendanceSession.java
│   ├── AttendanceRecord.java
│   ├── ExcuseRequest.java
│   └── Classroom.java
├── exception/        # Custom exceptions
├── repository/       # JPA repositories
├── service/          # Business logic
│   ├── AttendanceSessionService.java
│   ├── CheckInService.java
│   ├── ExcuseRequestService.java
│   ├── SpoofingDetectionService.java
│   └── QrCodeService.java
└── util/             # Utility classes
    ├── HaversineCalculator.java
    └── QrCodeGenerator.java
```

---

## 14. Environment Variables

### 14.1 Auth Service

```properties
# Database
DB_HOST=smart_campus_db (Docker) / 138.68.99.35 (Lokal)
DB_PORT=3306
DB_NAME=smart_campus
DB_USERNAME=root
DB_PASSWORD=****

# JWT
JWT_SECRET=****
JWT_ACCESS_EXPIRATION=900000      # 15 minutes in ms
JWT_REFRESH_EXPIRATION=604800000  # 7 days in ms

# Email (Gmail SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=****@gmail.com
MAIL_PASSWORD=****              # App Password

# DigitalOcean Spaces
DO_SPACES_KEY=****
DO_SPACES_SECRET=****
DO_SPACES_ENDPOINT=https://fra1.digitaloceanspaces.com
DO_SPACES_BUCKET=smart-campus
DO_SPACES_REGION=fra1

# Frontend
FRONTEND_URL=http://localhost:3000
CORS_ALLOWED_ORIGINS=http://localhost:3000

# Service Discovery
AUTH_SERVICE_HOST=localhost (Lokal) / auth-service (Docker)
AUTH_SERVICE_PORT=8081
```

### 14.2 Academic Service

```properties
# Database
DB_HOST=smart_campus_db
DB_PORT=3306
DB_NAME=smart_campus
DB_USERNAME=root
DB_PASSWORD=****

# JWT (shared secret)
JWT_SECRET=****

# Service URLs
AUTH_SERVICE_URL=http://auth-service:8081

# Academic Settings
DROP_PERIOD_WEEKS=4
```

### 14.3 Attendance Service

```properties
# Database
DB_HOST=smart_campus_db
DB_PORT=3306
DB_NAME=smart_campus
DB_USERNAME=root
DB_PASSWORD=****

# JWT (shared secret)
JWT_SECRET=****

# GPS Settings
DEFAULT_GEOFENCE_RADIUS=15     # metre
MAX_GPS_ACCURACY=50            # metre
QR_CODE_REFRESH_INTERVAL=5000  # ms

# Spoofing Detection
CAMPUS_IP_RANGE=138.68.0.0/16
MAX_WALKING_SPEED=5.0          # m/s

# Attendance Thresholds
WARNING_ABSENCE_RATE=20        # %
CRITICAL_ABSENCE_RATE=30       # %
```

---

## 15. API Dokümantasyonu

### 15.1 Swagger UI

Swagger UI üzerinden API dokümantasyonuna erişilebilir:

| Servis | URL |
|--------|-----|
| Auth Service | http://localhost:8081/swagger-ui.html |
| Academic Service | http://localhost:8082/swagger-ui.html |
| Attendance Service | http://localhost:8083/swagger-ui.html |
| API Docs (JSON) | http://localhost:808X/api-docs |

### 15.2 Dokümantasyon Özellikleri

- ✅ Tüm endpoint'ler dokümante edildi
- ✅ Request/Response örnekleri
- ✅ Authentication gereksinimleri belirtildi
- ✅ Validation kuralları açıklandı
- ✅ Error response'ları tanımlandı
- ✅ Algoritma açıklamaları (Haversine, Prerequisite Checking)

---

## 16. Tamamlanan Özellikler

### 16.1 Part 1 - Authentication & User Management

- ✅ Kullanıcı kaydı (Öğrenci, Öğretim Üyesi)
- ✅ Email doğrulama sistemi
- ✅ JWT tabanlı login/logout
- ✅ Refresh token mekanizması
- ✅ Şifre sıfırlama (forgot password)
- ✅ Şifre değiştirme
- ✅ Profil görüntüleme ve güncelleme
- ✅ Profil fotoğrafı yükleme (DigitalOcean Spaces)
- ✅ Admin kullanıcı listesi
- ✅ Role-based access control (RBAC)

### 16.2 Part 2 - Academic Management

- ⬜ Ders kataloğu (CRUD)
- ⬜ Ders bölümleri (Section) yönetimi
- ⬜ Derse kayıt olma (Enrollment)
- ⬜ Önkoşul kontrolü (Recursive prerequisite checking)
- ⬜ Çakışma kontrolü (Schedule conflict detection)
- ⬜ Kapasite kontrolü (Atomic increment)
- ⬜ Dersi bırakma (Drop period kontrolü)
- ⬜ Not görüntüleme (Öğrenci)
- ⬜ Not girişi (Öğretim üyesi)
- ⬜ Transkript görüntüleme (JSON)
- ⬜ Transkript PDF indirme
- ⬜ GPA/CGPA hesaplama

### 16.3 Part 2 - GPS Attendance

- ⬜ Yoklama oturumu açma (Öğretim üyesi)
- ⬜ GPS koordinatları ile yoklama
- ⬜ Haversine mesafe hesaplama
- ⬜ Geofencing radius kontrolü
- ⬜ QR kod ile yoklama (backup)
- ⬜ GPS spoofing tespiti
- ⬜ Yoklama durumu görüntüleme (Öğrenci)
- ⬜ Yoklama raporları (Öğretim üyesi)
- ⬜ Mazeret bildirme ve onaylama
- ⬜ Devamsızlık uyarıları

--- 

## 17. Sonraki Adımlar

### Part 2 Geliştirme Planı (TAMAMLANDI)

1. ✅ Course, Section, Enrollment entity'leri
2. ✅ Attendance, Classroom entity'leri
3. ✅ PrerequisiteService implementasyonu
4. ✅ ScheduleConflictService implementasyonu
5. ✅ AttendanceService implementasyonu
6. ✅ HaversineCalculator implementasyonu
7. ✅ SpoofingDetectionService implementasyonu
8. ✅ QrCodeService implementasyonu
9. ✅ PDF generation (Transkript)
10. ✅ Scheduled Jobs (Devamsızlık uyarıları)

### Part 3 Geliştirme Planı

1. ⬜ Meal Reservation System
2. ⬜ Event Management
3. ⬜ Course Scheduling (CSP Algorithm)

---

// ... rest of code ...
</initial_code>