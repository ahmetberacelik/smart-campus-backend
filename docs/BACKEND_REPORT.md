# Smart Campus Backend - Teknik Rapor

## 1. Genel Bakış

Bu doküman, Akıllı Kampüs Yönetim Sistemi'nin backend mimarisini, teknoloji kararlarını ve Part 1 kapsamındaki geliştirme planını içerir.

| Özellik | Değer |
|---------|-------|
| **Proje Adı** | Smart Campus Backend |
| **Mimari** | Mikroservis |
| **Part** | Part 1 - Kimlik Doğrulama ve Kullanıcı Yönetimi |
| **Teslim Tarihi** | 8 Aralık 2025 |

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

### 3.2 Part 1 Servis Yapısı

Part 1 için **tek servis** yaklaşımı benimsenmiştir:

| Servis | Port | Sorumluluk |
|--------|------|------------|
| **api-gateway** | 8080 | Request routing, load balancing |
| **auth-service** | 8081 | Authentication, User Management |

### 3.3 Service Discovery

Service Discovery (Eureka/Consul) **kullanılmayacaktır**. Servisler arası iletişim Docker Compose network üzerinden sağlanacaktır.

```yaml
# Docker network üzerinden servis erişimi
auth-service: http://auth-service:8081
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

### 4.2 Route Yapısı (Part 1)

```
/api/v1/auth/**     →  auth-service:8081
/api/v1/users/**    →  auth-service:8081
/api/v1/departments/** → auth-service:8081
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

### 5.3 Migration Yönetimi

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
| **Kullanım** | Profil fotoğrafları |

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

### 8.2 Email Kullanım Alanları (Part 1)

- Email doğrulama
- Şifre sıfırlama
- Hoş geldin emaili

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
│  │   ┌─────────────────┐                                     │  │
│  │   │   phpmyadmin    │                                     │  │
│  │   │     :8082       │                                     │  │
│  │   └─────────────────┘                                     │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 9.3 Port Yapılandırması

| Servis | Internal Port | External Port |
|--------|---------------|---------------|
| API Gateway | 8080 | 8080 |
| Auth Service | 8081 | - (internal) |
| MySQL | 3306 | - (internal) |
| phpMyAdmin | 8082 | 8082 (dev only) |

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

## 11. Proje Yapısı

### 11.1 Repository Yapısı

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
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── exception/
│   │   │   │   ├── repository/
│   │   │   │   ├── security/
│   │   │   │   ├── service/
│   │   │   │   └── AuthServiceApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── docs/
│   ├── BACKEND_REPORT.md
│   ├── DATABASE_SCHEMA.md
│   └── ...
│
├── docker-compose.yml
├── pom.xml (parent)
└── README.md
```

### 11.2 Package Yapısı (auth-service)

```
com.smartcampus.auth
├── config/           # Configuration classes
├── controller/       # REST controllers
├── dto/              # Data Transfer Objects
│   ├── request/
│   └── response/
├── entity/           # JPA entities
├── exception/        # Custom exceptions
├── repository/       # JPA repositories
├── security/         # Security configurations
│   ├── jwt/
│   └── filter/
├── service/          # Business logic
│   └── impl/
└── util/             # Utility classes
```

---

## 12. Environment Variables

### 12.1 Auth Service

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

---

## 13. API Dokümantasyonu

### 13.1 Swagger UI

Swagger UI üzerinden API dokümantasyonuna erişilebilir:

| Servis | URL |
|--------|-----|
| Auth Service | http://localhost:8081/swagger-ui.html |
| API Docs (JSON) | http://localhost:8081/api-docs |

### 13.2 Dokümantasyon Özellikleri

- ✅ Tüm endpoint'ler dokümante edildi
- ✅ Request/Response örnekleri
- ✅ Authentication gereksinimleri belirtildi
- ✅ Validation kuralları açıklandı
- ✅ Error response'ları tanımlandı

---

## 14. Tamamlanan Özellikler (Part 1)

### 14.1 Authentication

- ✅ Kullanıcı kaydı (Öğrenci, Öğretim Üyesi)
- ✅ Email doğrulama sistemi
- ✅ JWT tabanlı login/logout
- ✅ Refresh token mekanizması
- ✅ Şifre sıfırlama (forgot password)
- ✅ Şifre değiştirme

### 14.2 User Management

- ✅ Profil görüntüleme
- ✅ Profil güncelleme
- ✅ Profil fotoğrafı yükleme (DigitalOcean Spaces)
- ✅ Profil fotoğrafı silme
- ✅ Admin kullanıcı listesi
- ✅ Kullanıcı arama ve filtreleme

### 14.3 Security

- ✅ JWT token tabanlı authentication
- ✅ Role-based access control (RBAC)
- ✅ Password encryption (BCrypt)
- ✅ CORS yapılandırması
- ✅ Input validation
- ✅ Exception handling

### 14.4 Email Service

- ✅ Email doğrulama emaili
- ✅ Şifre sıfırlama emaili
- ✅ Hoş geldin emaili
- ✅ HTML email template'leri

### 14.5 File Storage

- ✅ DigitalOcean Spaces entegrasyonu
- ✅ Profil fotoğrafı yükleme
- ✅ Dosya silme
- ✅ Dosya validasyonu (format, boyut)

---

## 15. Sonraki Adımlar

### Part 2 Geliştirme Planı

1. ⬜ Academic Management modülü
2. ⬜ GPS-Based Attendance System
3. ⬜ Course enrollment logic
4. ⬜ Grade management
5. ⬜ Transcript PDF generation

---

## 16. Referanslar

| Doküman | Açıklama |
|---------|----------|
| [FINAL_PROJECT_ASSIGNMENT.md](./FINAL_PROJECT_ASSIGNMENT.md) | Proje gereksinimleri |
| [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) | Veritabanı şeması |
| [DATABASE_DOCKER_SETUP.md](./DATABASE_DOCKER_SETUP.md) | Database Docker kurulumu |

---

**Hazırlayan:** Smart Campus Backend Team  
**Tarih:** Aralık 2025  
**Versiyon:** 1.0

