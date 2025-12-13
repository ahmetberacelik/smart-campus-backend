# 📋 Smart Campus - Proje Genel Bakış

**Ders:** Web ve Mobil Programlama  
**Öğretim Üyesi:** Dr. Öğretim Üyesi Mehmet Sevri  
**Dönem:** Güz 2024-2025  
**Proje Türü:** Grup Projesi (4 kişi)  
**Part:** Part 1 - Kimlik Doğrulama ve Kullanıcı Yönetimi  
**Teslim Tarihi:** 8 Aralık 2025

---

## 📖 Proje Tanımı

**Smart Campus**, bir üniversite kampüsünün günlük operasyonlarını dijitalleştiren ve optimize eden kapsamlı bir web uygulamasıdır. Bu proje, öğrencilerin gerçek dünya senaryolarına uygun, ölçeklenebilir ve modern web teknolojileri kullanarak profesyonel bir uygulama geliştirme deneyimi kazanmalarını amaçlamaktadır.

### Proje Kapsamı

Smart Campus platformu, aşağıdaki ana modülleri içermektedir:

- ✅ **Authentication & User Management** (Part 1 - Tamamlandı)
- 🔄 **Academic Management** (Part 2 - Planlanıyor)
- 🔄 **GPS-Based Attendance** (Part 2 - Planlanıyor)
- 🔄 **Course Scheduling** (Part 3 - Planlanıyor)
- 🔄 **Meal Reservation System** (Part 3 - Planlanıyor)
- 🔄 **Event Management** (Part 3 - Planlanıyor)
- 🔄 **Notification System** (Part 4 - Planlanıyor)
- 🔄 **Analytics & Reporting** (Part 4 - Planlanıyor)

### Part 1 Kapsamı

Bu aşamada projenin temelini oluşturan **Kimlik Doğrulama ve Kullanıcı Yönetimi** modülü tamamlanmıştır:

- ✅ Kullanıcı kaydı (Öğrenci, Öğretim Üyesi, Admin)
- ✅ JWT tabanlı authentication sistemi
- ✅ Email doğrulama mekanizması
- ✅ Şifre sıfırlama akışı
- ✅ Profil yönetimi (CRUD işlemleri)
- ✅ Profil fotoğrafı yükleme ve yönetimi
- ✅ Role-based access control (RBAC)
- ✅ Refresh token mekanizması

---

## 👥 Grup Üyeleri ve Görev Dağılımı

| Üye | Rol | Sorumluluklar |
|-----|-----|---------------|
| **Ahmet Bera Çelik** | Proje Yöneticisi & Backend Geliştirici | • Proje yönetimi ve koordinasyon<br>• Backend mimarisi ve geliştirme<br>• API tasarımı ve implementasyonu<br>• Docker ve deployment yönetimi<br>• Teknik dokümantasyon |
| **Tuğba Nur Uygun** | Frontend Geliştirici | • React frontend geliştirme<br>• UI/UX tasarımı<br>• State management<br>• Form validasyonları |
| **Öznur Beyazpınar** | Frontend Geliştirici | • React frontend geliştirme<br>• Component geliştirme<br>• Routing ve navigation<br>• API entegrasyonu |
| **Furkan Kapucu** | Test & Database Geliştirici | • Veritabanı tasarımı ve implementasyonu<br>• Unit ve integration testleri<br>• Test coverage raporları<br>• Database migration'ları |

### İletişim ve İşbirliği

- **GitHub Repository:** [smart-campus-backend](https://github.com/your-username/smart-campus-backend)
- **İletişim Platformu:** Discord / Slack
- **Proje Yönetimi:** GitHub Issues & Projects
- **Code Review:** Pull Request workflow

---

## 🛠 Teknoloji Stack

### Backend

| Teknoloji | Versiyon | Kullanım Amacı |
|-----------|----------|----------------|
| **Java** | 17 (LTS) | Programlama dili |
| **Spring Boot** | 3.2.0 | Backend framework |
| **Spring Cloud Gateway** | 2023.0.0 | API Gateway (routing, CORS, load balancing) |
| **Spring Security** | 6.x | Authentication & Authorization |
| **Spring Data JPA** | - | ORM ve veritabanı işlemleri |
| **Spring WebFlux** | - | Reactive HTTP client (SendGrid API) |
| **MySQL** | 8.0 | İlişkisel veritabanı |
| **JWT (jjwt)** | 0.12.3 | Token tabanlı authentication |
| **BCrypt** | - | Şifre hashleme (Spring Security içinde) |
| **Lombok** | - | Boilerplate kod azaltma |
| **AWS S3 SDK** | 2.21.29 | DigitalOcean Spaces entegrasyonu (dosya yükleme) |
| **Springdoc OpenAPI** | 2.3.0 | API dokümantasyonu (Swagger UI) |
| **Maven** | Latest | Build tool ve dependency yönetimi |
| **Docker** | Latest | Containerization |
| **Docker Compose** | Latest | Multi-container orchestration |

### Backend Ekosistemi

#### Spring Modülleri

- **Spring Web**: RESTful API geliştirme
- **Spring Security**: JWT tabanlı güvenlik, role-based access control
- **Spring Data JPA**: Repository pattern, otomatik query generation
- **Spring Cloud Gateway**: API routing, CORS yönetimi, request forwarding
- **Spring Mail**: Email gönderimi (SMTP)
- **Spring Validation**: Input validation ve error handling
- **Spring WebFlux**: Reactive HTTP client (SendGrid HTTP API)

#### Güvenlik

- **JWT Authentication**: Access token (15 dakika) ve refresh token (7 gün)
- **BCrypt Password Hashing**: Minimum 10 salt rounds
- **Role-Based Access Control (RBAC)**: Student, Faculty, Admin rolleri
- **CORS Configuration**: Frontend ile güvenli iletişim
- **Input Validation**: Request DTO'larında `@Valid` annotation'ları

#### Dış Servisler

- **SendGrid HTTP API**: Email gönderimi (production)
- **DigitalOcean Spaces**: Profil fotoğrafları için object storage (S3-compatible)
- **MySQL Database**: Merkezi veritabanı (production: 138.68.99.35)

---

## 🏗 Backend Mimari

### Mikroservis Mimarisi

Smart Campus backend'i **mikroservis mimarisi** kullanarak geliştirilmiştir. Bu mimari, servislerin bağımsız olarak geliştirilmesini, test edilmesini ve deploy edilmesini sağlar.

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENTS                                   │
│              (Web Browser, Mobile App, etc.)                     │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway                                 │
│                    (Port 8080)                                   │
│  • Request Routing                                              │
│  • CORS Management                                               │
│  • Load Balancing                                                │
│  • Request/Response Transformation                               │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
        ┌─────────────────────┴─────────────────────┐
        │                                             │
        ▼                                             ▼
┌──────────────────┐                      ┌──────────────────┐
│  Auth Service    │                      │  (Future)        │
│  (Port 8081)     │                      │  Other Services  │
│                  │                      │                  │
│  • Authentication│                      │  • Academic      │
│  • User Mgmt     │                      │  • Attendance    │
│  • Email Service │                      │  • Meal          │
│  • File Storage  │                      │  • Event         │
└────────┬─────────┘                      └──────────────────┘
         │
         ├─────────────────┬─────────────────┬─────────────────┐
         ▼                 ▼                 ▼                 ▼
    ┌─────────┐      ┌──────────┐      ┌──────────┐      ┌──────────┐
    │  MySQL  │      │DO Spaces │      │ SendGrid │      │  (Future)│
    │Database │      │  (S3)    │      │   API    │      │ Services │
    └─────────┘      └──────────┘      └──────────┘      └──────────┘
```

### Katmanlı Mimari (Layered Architecture)

Her mikroservis, **katmanlı mimari** prensiplerine göre organize edilmiştir:

```
┌─────────────────────────────────────────────────────────────┐
│                    Controller Layer                          │
│  • REST Endpoints                                            │
│  • Request/Response Mapping                                  │
│  • Input Validation                                          │
│  • Error Handling                                            │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                            │
│  • Business Logic                                            │
│  • Transaction Management                                    │
│  • Service Orchestration                                     │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Repository Layer                         │
│  • Data Access                                               │
│  • Database Queries                                          │
│  • Entity Management                                         │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Database Layer                          │
│  • MySQL Database                                            │
│  • Tables & Relationships                                    │
│  • Indexes & Constraints                                     │
└─────────────────────────────────────────────────────────────┘
```

### Backend Proje Yapısı

```
smart-campus-backend/
├── api-gateway/                          # API Gateway Servisi
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/smartcampus/gateway/
│   │   │   │   ├── config/
│   │   │   │   │   └── CorsConfig.java
│   │   │   │   └── GatewayApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── auth-service/                         # Authentication Servisi
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/smartcampus/auth/
│   │   │   │   ├── config/               # Konfigürasyon sınıfları
│   │   │   │   │   ├── AsyncConfig.java
│   │   │   │   │   ├── CorsConfig.java
│   │   │   │   │   ├── OpenApiConfig.java
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   └── WebClientConfig.java
│   │   │   │   │
│   │   │   │   ├── controller/          # REST Controller'lar
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── DepartmentController.java
│   │   │   │   │   └── UserController.java
│   │   │   │   │
│   │   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   │   ├── request/
│   │   │   │   │   │   ├── ChangePasswordRequest.java
│   │   │   │   │   │   ├── ForgotPasswordRequest.java
│   │   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   │   ├── ResetPasswordRequest.java
│   │   │   │   │   │   ├── UpdateProfileRequest.java
│   │   │   │   │   │   └── VerifyEmailRequest.java
│   │   │   │   │   └── response/
│   │   │   │   │       ├── ApiResponse.java
│   │   │   │   │       ├── AuthResponse.java
│   │   │   │   │       ├── DepartmentResponse.java
│   │   │   │   │       ├── PageResponse.java
│   │   │   │   │       ├── TokenResponse.java
│   │   │   │   │       └── UserResponse.java
│   │   │   │   │
│   │   │   │   ├── entity/              # JPA Entity'ler
│   │   │   │   │   ├── Department.java
│   │   │   │   │   ├── EmailVerificationToken.java
│   │   │   │   │   ├── Faculty.java
│   │   │   │   │   ├── PasswordResetToken.java
│   │   │   │   │   ├── RefreshToken.java
│   │   │   │   │   ├── Role.java
│   │   │   │   │   ├── Student.java
│   │   │   │   │   └── User.java
│   │   │   │   │
│   │   │   │   ├── exception/            # Exception Handling
│   │   │   │   │   ├── BadRequestException.java
│   │   │   │   │   ├── BaseException.java
│   │   │   │   │   ├── ConflictException.java
│   │   │   │   │   ├── ForbiddenException.java
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   │   ├── TokenException.java
│   │   │   │   │   └── UnauthorizedException.java
│   │   │   │   │
│   │   │   │   ├── repository/          # Spring Data JPA Repository'ler
│   │   │   │   │   ├── DepartmentRepository.java
│   │   │   │   │   ├── EmailVerificationTokenRepository.java
│   │   │   │   │   ├── FacultyRepository.java
│   │   │   │   │   ├── PasswordResetTokenRepository.java
│   │   │   │   │   ├── RefreshTokenRepository.java
│   │   │   │   │   ├── StudentRepository.java
│   │   │   │   │   └── UserRepository.java
│   │   │   │   │
│   │   │   │   ├── security/            # Security Konfigürasyonu
│   │   │   │   │   ├── CurrentUser.java
│   │   │   │   │   ├── CustomUserDetails.java
│   │   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   │   ├── JwtAuthenticationEntryPoint.java
│   │   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   │   └── JwtTokenProvider.java
│   │   │   │   │
│   │   │   │   ├── service/             # Business Logic
│   │   │   │   │   ├── impl/
│   │   │   │   │   │   ├── AuthServiceImpl.java
│   │   │   │   │   │   ├── DepartmentServiceImpl.java
│   │   │   │   │   │   ├── EmailServiceImpl.java
│   │   │   │   │   │   ├── FileStorageServiceImpl.java
│   │   │   │   │   │   └── UserServiceImpl.java
│   │   │   │   │   ├── AuthService.java
│   │   │   │   │   ├── DepartmentService.java
│   │   │   │   │   ├── EmailService.java
│   │   │   │   │   ├── FileStorageService.java
│   │   │   │   │   └── UserService.java
│   │   │   │   │
│   │   │   │   └── util/                # Utility Sınıfları
│   │   │   │
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   │
│   │   └── test/                        # Test Dosyaları
│   │       ├── java/com/smartcampus/auth/
│   │       │   ├── service/
│   │       │   │   ├── AuthServiceTest.java
│   │       │   │   └── UserServiceTest.java
│   │       │   └── resources/
│   │       │       └── application-test.properties
│   │
│   ├── Dockerfile
│   └── pom.xml
│
├── docs/                                # Dokümantasyon
│   ├── API_DOCUMENTATION.md
│   ├── DEPLOYMENT.md
│   ├── PROJECT_OVERVIEW.md
│   └── ...
│
├── docker-compose.yml                   # Docker Compose yapılandırması
├── pom.xml                              # Parent POM
├── .env.example                         # Örnek environment dosyası
├── .gitignore
└── README.md
```

### Backend Servis Detayları

#### 1. API Gateway

**Amaç:** Tüm client isteklerinin tek bir noktadan yönetilmesi

**Özellikler:**
- Request routing (auth-service'e yönlendirme)
- CORS yönetimi (frontend ile güvenli iletişim)
- Load balancing (gelecekte birden fazla instance için)
- Request/response transformation

**Port:** 8080

**Konfigürasyon:**
- `application.properties` içinde route tanımlamaları
- CORS allowed origins environment variable'dan okunur

#### 2. Auth Service

**Amaç:** Kimlik doğrulama ve kullanıcı yönetimi

**Özellikler:**
- Kullanıcı kaydı (Student, Faculty)
- JWT tabanlı authentication
- Email doğrulama
- Şifre sıfırlama
- Profil yönetimi
- Profil fotoğrafı yükleme
- Role-based access control

**Port:** 8081

**API Endpoints:**
- `POST /api/v1/auth/register` - Kullanıcı kaydı
- `POST /api/v1/auth/login` - Giriş
- `POST /api/v1/auth/refresh` - Token yenileme
- `POST /api/v1/auth/logout` - Çıkış
- `POST /api/v1/auth/verify-email` - Email doğrulama
- `POST /api/v1/auth/forgot-password` - Şifre sıfırlama isteği
- `POST /api/v1/auth/reset-password` - Şifre sıfırlama
- `POST /api/v1/auth/resend-verification` - Doğrulama emaili tekrar gönder
- `GET /api/v1/users/me` - Profil görüntüleme
- `PUT /api/v1/users/me` - Profil güncelleme
- `POST /api/v1/users/me/change-password` - Şifre değiştirme
- `POST /api/v1/users/me/profile-picture` - Profil fotoğrafı yükleme
- `DELETE /api/v1/users/me/profile-picture` - Profil fotoğrafı silme
- `GET /api/v1/users` - Kullanıcı listesi (Admin)
- `GET /api/v1/users/{id}` - Kullanıcı detayı (Admin)
- `GET /api/v1/departments` - Bölüm listesi
- `GET /api/v1/departments/{id}` - Bölüm detayı

**Swagger UI:** `http://localhost:8081/swagger-ui.html`

### Backend Design Patterns

#### 1. Repository Pattern
- Spring Data JPA repository'ler ile veritabanı işlemleri
- Custom query metodları
- Pagination ve sorting desteği

#### 2. Service Layer Pattern
- Business logic'in service katmanında toplanması
- Transaction yönetimi (`@Transactional`)
- Interface ve implementation ayrımı

#### 3. DTO Pattern
- Request ve Response DTO'ları ile API kontratı
- Entity'lerin direkt expose edilmemesi
- Validation annotation'ları

#### 4. Exception Handling Pattern
- Global exception handler (`@ControllerAdvice`)
- Custom exception sınıfları
- Standart error response formatı

#### 5. Security Pattern
- JWT token tabanlı authentication
- Filter chain ile request interception
- Role-based access control (RBAC)

### Backend Güvenlik

#### Authentication Flow

```
1. Kullanıcı kaydı
   └─> Email doğrulama token'ı oluşturulur
   └─> Email gönderilir
   └─> Access token + Refresh token döner

2. Email doğrulama
   └─> Token validate edilir
   └─> User.isVerified = true
   └─> Hoş geldin emaili gönderilir

3. Login
   └─> Email/password doğrulanır
   └─> Access token (15 dk) + Refresh token (7 gün) döner

4. Token yenileme
   └─> Refresh token validate edilir
   └─> Yeni access token + refresh token döner

5. Logout
   └─> Refresh token silinir
```

#### Authorization

- **Student**: Kendi profilini görüntüleyip güncelleyebilir
- **Faculty**: Kendi profilini görüntüleyip güncelleyebilir
- **Admin**: Tüm kullanıcıları görüntüleyip yönetebilir

#### Password Security

- BCrypt ile hashleme (minimum 10 salt rounds)
- Şifre güçlülük kontrolü (min 8 karakter, büyük harf, küçük harf, rakam)
- Şifre sıfırlama token'ları 1 saat geçerli
- Email doğrulama token'ları 24 saat geçerli

### Backend Testing

#### Test Stratejisi

- **Unit Tests**: Service katmanı business logic testleri
- **Integration Tests**: Controller katmanı API endpoint testleri (şimdilik durduruldu)
- **Test Coverage**: JaCoCo ile coverage raporu

#### Test Araçları

- **JUnit 5**: Test framework
- **Mockito**: Mocking framework
- **Spring Boot Test**: Integration test desteği
- **H2 Database**: In-memory test database
- **JaCoCo**: Code coverage tool

#### Test Coverage Hedefi

- **Backend**: Minimum %85 code coverage (yönerge gereksinimi)

#### Test Dosyaları

```
auth-service/src/test/
├── java/com/smartcampus/auth/
│   ├── service/
│   │   ├── AuthServiceTest.java      (~20 test)
│   │   └── UserServiceTest.java      (~15 test)
│   └── resources/
│       └── application-test.properties
```

### Backend Deployment

#### Production Environment

- **Server**: DigitalOcean Droplet (Ubuntu 22.04)
- **IP Address**: 138.68.99.35
- **Database**: MySQL 8.0 (aynı sunucuda)
- **File Storage**: DigitalOcean Spaces (S3-compatible)
- **Email Service**: SendGrid HTTP API

#### Deployment Yöntemi

- **Docker Compose**: Tüm servisler containerize edilmiş
- **Multi-stage Dockerfile**: Optimize edilmiş image'ler
- **Environment Variables**: `.env` dosyası ile konfigürasyon
- **Health Checks**: Container sağlık kontrolü

#### Deployment URL'leri

- **API Gateway**: `http://138.68.99.35:8080`
- **Auth Service**: `http://138.68.99.35:8081`
- **Swagger UI**: `http://138.68.99.35:8081/swagger-ui.html`

### Backend API Dokümantasyonu

- **Swagger/OpenAPI**: Otomatik API dokümantasyonu
- **Endpoint'ler**: Tüm endpoint'ler dokümante edilmiş
- **Request/Response Örnekleri**: Her endpoint için örnekler
- **Authentication**: Bearer token ile korumalı endpoint'ler işaretlenmiş

---

## 📊 Backend İstatistikleri

### Kod Metrikleri

- **Toplam Java Dosyası**: ~50+ sınıf
- **Service Sınıfları**: 5 (Auth, User, Email, FileStorage, Department)
- **Controller Sınıfları**: 3 (Auth, User, Department)
- **Entity Sınıfları**: 8 (User, Student, Faculty, Department, Token'lar)
- **Repository Sınıfları**: 7
- **DTO Sınıfları**: 12+ (Request/Response)

### Test Metrikleri

- **Unit Test Sayısı**: ~35 test
- **Test Coverage**: Hedef %85+
- **Test Dosyaları**: 2 (AuthServiceTest, UserServiceTest)

### API Endpoint Sayısı

- **Authentication Endpoints**: 8
- **User Management Endpoints**: 7
- **Department Endpoints**: 2
- **Toplam**: 17 endpoint

---

## 🔄 Backend Geliştirme Süreci

### Part 1 Tamamlanan Özellikler

1. ✅ **Proje Yapısı**: Mikroservis mimarisi kuruldu
2. ✅ **API Gateway**: Routing ve CORS yönetimi
3. ✅ **Auth Service**: Tam fonksiyonel authentication servisi
4. ✅ **Database Schema**: Part 1 için gerekli tablolar
5. ✅ **Security**: JWT authentication ve RBAC
6. ✅ **Email Service**: SendGrid HTTP API entegrasyonu
7. ✅ **File Storage**: DigitalOcean Spaces entegrasyonu
8. ✅ **API Documentation**: Swagger/OpenAPI
9. ✅ **Testing**: Unit testler ve coverage raporu
10. ✅ **Deployment**: Production ortamına deploy edildi

### Backend Geliştirme Prensipleri

- **Clean Code**: Okunabilir ve maintainable kod
- **SOLID Principles**: Object-oriented design prensipleri
- **RESTful API**: Standart REST API tasarımı
- **Error Handling**: Merkezi exception handling
- **Security First**: Güvenlik öncelikli geliştirme
- **Documentation**: Kod ve API dokümantasyonu
- **Testing**: Test-driven development yaklaşımı

---

## 📝 Notlar

- Backend kısmı **Part 1** kapsamında tamamlanmıştır.
- Frontend ve Database kısımları ilgili geliştiriciler tarafından eklenecektir.
- Production deployment başarıyla tamamlanmıştır (138.68.99.35).
- Tüm API endpoint'leri Swagger UI üzerinden test edilebilir.

---

**Son Güncelleme:** 10 Aralık 2025  
**Hazırlayan:** Ahmet Bera Çelik (Backend Geliştirici & Proje Yöneticisi)


