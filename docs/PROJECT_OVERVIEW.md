# 📋 Smart Campus - Proje Genel Bakış

**Ders:** Web ve Mobil Programlama  
**Öğretim Üyesi:** Dr. Öğretim Üyesi Mehmet Sevri  
**Dönem:** Güz 2024-2025  
**Proje Türü:** Grup Projesi (4 kişi)  
**Part:** Part 1 - Kimlik Doğrulama ve Kullanıcı Yönetimi  
**Teslim Tarihi:** 10 Aralık 2025

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

**Backend:**
- ✅ Kullanıcı kaydı (Öğrenci, Öğretim Üyesi, Admin)
- ✅ JWT tabanlı authentication sistemi
- ✅ Email doğrulama mekanizması
- ✅ Şifre sıfırlama akışı
- ✅ Profil yönetimi (CRUD işlemleri)
- ✅ Profil fotoğrafı yükleme ve yönetimi
- ✅ Role-based access control (RBAC)
- ✅ Refresh token mekanizması

**Frontend:**
- ✅ Login sayfası (form validation, error handling)
- ✅ Register sayfası (öğrenci/öğretim üyesi kaydı, bölüm seçimi)
- ✅ Email doğrulama sayfası (path ve query parameter desteği)
- ✅ Şifre sıfırlama sayfaları (forgot password, reset password)
- ✅ Dashboard sayfası (role-based content)
- ✅ Profil sayfası (görüntüleme, güncelleme, fotoğraf yükleme)
- ✅ Protected routes (authentication guard)
- ✅ Role-based navigation (Navbar, Sidebar)
- ✅ Reusable component library (Button, TextInput, Select, LoadingSpinner)
- ✅ State management (AuthContext)
- ✅ API entegrasyonu (Axios, interceptors, token refresh)
- ✅ Form validation (React Hook Form + Yup)
- ✅ Responsive design (mobile-first)
- ✅ Turkuaz tema
- ✅ Docker containerization

**Database:**
- ✅ MySQL 8.0 veritabanı yapılandırması
- ✅ Docker Compose ile containerization
- ✅ 7 tablo oluşturuldu (departments, users, students, faculty, auth tables)
- ✅ Migration dosyaları (V1-V5)
- ✅ Seed data (test kullanıcıları: 8 kullanıcı, 4 bölüm)
- ✅ Otomatik initialization (init.sql + seed.sql)
- ✅ phpMyAdmin entegrasyonu
- ✅ Production deployment (DigitalOcean)
- ✅ Firewall güvenliği yapılandırması
- ✅ Veritabanı dokümantasyonu (DATABASE_SCHEMA.md, DOCKER_SETUP.md)

---

## 👥 Grup Üyeleri ve Görev Dağılımı

| Üye | Rol | Sorumluluklar |
|-----|-----|---------------|
| **Ahmet Bera Çelik** | Proje Yöneticisi & Backend Geliştirici | • Proje yönetimi ve koordinasyon<br>• Backend mimarisi ve geliştirme<br>• API tasarımı ve implementasyonu<br>• Docker ve deployment yönetimi<br>• Teknik dokümantasyon |
| **Tuğba Nur Uygun** | Frontend Geliştirici | • React frontend geliştirme<br>• UI/UX tasarımı<br>• State management<br>• Form validasyonları |
| **Öznur Beyazpınar** | Frontend Geliştirici | • React frontend geliştirme<br>• Component geliştirme<br>• Routing ve navigation<br>• API entegrasyonu |
| **Furkan Kapucu** | Database Geliştirici ve Tester | • Veritabanı tasarımı ve implementasyonu<br>• Database migration'ları<br>• Unit ve integration testleri<br>• Test coverage raporları |

### İletişim ve İşbirliği

- **GitHub Repositories:**
  - [smart-campus-backend](https://github.com/ahmetberacelik/smart-campus-backend.git) - Backend repository
  - [smart-campus-frontend](https://github.com/ahmetberacelik/smart-campus-frontend.git) - Frontend repository
  - [smart-campus-database](https://github.com/ahmetberacelik/smart-campus-database.git) - Database repository
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

### Frontend

| Teknoloji | Versiyon | Kullanım Amacı |
|-----------|----------|----------------|
| **React** | 18.2.0 | UI framework |
| **TypeScript** | 5.3.2 | Type-safe JavaScript |
| **Vite** | 5.0.7 | Build tool ve development server |
| **React Router** | 6.20.0 | Client-side routing |
| **Axios** | 1.6.2 | HTTP client |
| **React Hook Form** | 7.48.2 | Form yönetimi |
| **Yup** | 1.3.3 | Form validation |
| **React Query** | 3.39.3 | Server state management (gelecekte kullanılacak) |
| **React Toastify** | 9.1.3 | Toast notifications |
| **MSW** | 2.0.8 | Mock Service Worker (development için) |
| **Leaflet** | 1.9.4 | Harita görselleştirme (Part 2 için) |
| **Recharts** | 2.10.3 | Chart görselleştirme (Part 4 için) |
| **QRCode.react** | 3.1.0 | QR kod oluşturma (Part 3 için) |
| **Docker** | Latest | Containerization |
| **Docker Compose** | Latest | Multi-container orchestration |
| **Nginx** | Alpine | Production static file serving |

### Frontend Ekosistemi

#### Core Libraries

- **React 18**: Modern React hooks, concurrent features
- **TypeScript**: Type safety, better IDE support, refactoring
- **Vite**: Fast HMR (Hot Module Replacement), optimized builds
- **React Router v6**: Declarative routing, nested routes, protected routes

#### Form Management

- **React Hook Form**: Performant form library, minimal re-renders
- **Yup**: Schema-based validation, async validation support
- **@hookform/resolvers**: Yup integration with React Hook Form

#### State Management

- **Context API**: Global state (AuthContext)
- **React Query**: Server state caching (kurulu, Part 2+ için kullanılacak)
- **LocalStorage**: Token ve user data persistence

#### HTTP Client

- **Axios**: Promise-based HTTP client
- **Interceptors**: Automatic token injection, error handling, token refresh
- **Request/Response transformation**: Backend field mapping (firstName/lastName ↔ name)

#### Development Tools

- **MSW (Mock Service Worker)**: API mocking for development
- **Vitest**: Unit testing framework
- **React Testing Library**: Component testing utilities
- **ESLint**: Code linting
- **TypeScript**: Static type checking

#### UI/UX Libraries

- **React Toastify**: Toast notifications (success, error, info)
- **Custom CSS**: Turkuaz tema, CSS variables, responsive design
- **Leaflet**: Interactive maps (GPS attendance için)
- **Recharts**: Data visualization (analytics için)

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

## 🎨 Frontend

### Frontend Teknoloji Stack

| Teknoloji | Versiyon | Kullanım Amacı |
|-----------|----------|----------------|
| **React** | 18.2.0 | UI framework |
| **TypeScript** | 5.3.2 | Type-safe JavaScript |
| **Vite** | 5.0.7 | Build tool ve development server |
| **React Router** | 6.20.0 | Client-side routing |
| **Axios** | 1.6.2 | HTTP client |
| **React Hook Form** | 7.48.2 | Form yönetimi |
| **Yup** | 1.3.3 | Form validation |
| **React Query** | 3.39.3 | Server state management (kurulu, gelecekte kullanılacak) |
| **React Toastify** | 9.1.3 | Toast notifications |
| **MSW** | 2.0.8 | Mock Service Worker (development için) |
| **Leaflet** | 1.9.4 | Harita görselleştirme (Part 2 için) |
| **Recharts** | 2.10.3 | Chart görselleştirme (Part 4 için) |
| **QRCode.react** | 3.1.0 | QR kod oluşturma (Part 3 için) |
| **Docker** | Latest | Containerization |
| **Docker Compose** | Latest | Multi-container orchestration |
| **Nginx** | Alpine | Production static file serving |

### Frontend Ekosistemi

#### Core Libraries

- **React 18**: Modern React hooks, concurrent features
- **TypeScript**: Type safety, better IDE support, refactoring
- **Vite**: Fast HMR (Hot Module Replacement), optimized builds
- **React Router v6**: Declarative routing, nested routes, protected routes

#### Form Management

- **React Hook Form**: Performant form library, minimal re-renders
- **Yup**: Schema-based validation, async validation support
- **@hookform/resolvers**: Yup integration with React Hook Form

#### State Management

- **Context API**: Global state (AuthContext)
- **React Query**: Server state caching (kurulu, Part 2+ için kullanılacak)
- **LocalStorage**: Token ve user data persistence

#### HTTP Client

- **Axios**: Promise-based HTTP client
- **Interceptors**: Automatic token injection, error handling, token refresh
- **Request/Response transformation**: Backend field mapping (firstName/lastName ↔ name)

#### Development Tools

- **MSW (Mock Service Worker)**: API mocking for development
- **Vitest**: Unit testing framework
- **React Testing Library**: Component testing utilities
- **ESLint**: Code linting
- **TypeScript**: Static type checking

#### UI/UX Libraries

- **React Toastify**: Toast notifications (success, error, info)
- **Custom CSS**: Turkuaz tema, CSS variables, responsive design
- **Leaflet**: Interactive maps (GPS attendance için)
- **Recharts**: Data visualization (analytics için)

---

## 🏗 Frontend Mimari

### Component-Based Architecture

Frontend, **component-based architecture** prensiplerine göre geliştirilmiştir:

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER BROWSER                              │
│                    (Chrome, Firefox, Safari)                      │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      React Application                           │
│                    (Port 3000 - Dev/Prod)                        │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              App Component (Root)                        │   │
│  │  • React Router                                          │   │
│  │  • QueryClient Provider                                  │   │
│  │  • AuthProvider (Context)                                │   │
│  │  • ToastContainer                                         │   │
│  └─────────────────────┬───────────────────────────────────┘   │
│                        │                                         │
│        ┌───────────────┴───────────────┐                        │
│        │                               │                        │
│        ▼                               ▼                        │
│  ┌─────────────┐              ┌─────────────┐                 │
│  │   Public    │              │  Protected  │                 │
│  │   Routes    │              │   Routes     │                 │
│  │             │              │              │                 │
│  │ • Login     │              │ • Dashboard │                 │
│  │ • Register  │              │ • Profile    │                 │
│  │ • Forgot    │              │ • (Future)   │                 │
│  │   Password  │              │   Pages      │                 │
│  └─────────────┘              └──────┬───────┘                 │
│                                      │                          │
│                                      ▼                          │
│                            ┌─────────────────┐                 │
│                            │  Layout         │                 │
│                            │  • Navbar       │                 │
│                            │  • Sidebar      │                 │
│                            │  • Main Content │                 │
│                            └─────────────────┘                 │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Client Layer                           │
│  • Axios Instance                                               │
│  • Request Interceptors (Token injection)                       │
│  • Response Interceptors (Error handling, Token refresh)        │
│  • Service Functions (authService, userService, etc.)            │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Backend API                                 │
│              http://138.68.99.35:8080/api/v1                    │
└─────────────────────────────────────────────────────────────────┘
```

### Katmanlı Mimari (Layered Architecture)

Frontend uygulaması, **katmanlı mimari** prensiplerine göre organize edilmiştir:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  • Pages (LoginPage, RegisterPage, DashboardPage, etc.)      │
│  • Components (Button, TextInput, Select, etc.)              │
│  • Layout Components (Navbar, Sidebar)                       │
│  • Styling (CSS Modules, Theme Variables)                    │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    State Management Layer                    │
│  • Context API (AuthContext)                                 │
│  • LocalStorage (Token persistence)                         │
│  • React Query (Server state - gelecekte)                   │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                            │
│  • API Services (authService, userService, etc.)            │
│  • API Client (Axios instance + interceptors)              │
│  • Mock Services (MSW handlers - development)               │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Configuration Layer                      │
│  • API Configuration (endpoints, base URL)                  │
│  • Type Definitions (TypeScript interfaces)                 │
│  • Environment Variables (.env)                             │
└─────────────────────────────────────────────────────────────┘
```

### Frontend Proje Yapısı

```
smart-campus-frontend/
├── public/
│   ├── mockServiceWorker.js          # MSW service worker
│   └── index.html                    # HTML template
│
├── src/
│   ├── components/                   # React bileşenleri
│   │   ├── common/                   # Ortak bileşenler
│   │   │   ├── Button.tsx            # Turkuaz temalı buton
│   │   │   ├── Button.css
│   │   │   ├── TextInput.tsx         # Form input bileşeni
│   │   │   ├── TextInput.css
│   │   │   ├── Select.tsx            # Dropdown select
│   │   │   ├── Select.css
│   │   │   ├── LoadingSpinner.tsx    # Yükleme göstergesi
│   │   │   ├── LoadingSpinner.css
│   │   │   └── ProtectedRoute.tsx   # Route guard
│   │   └── layout/                   # Layout bileşenleri
│   │       ├── Navbar.tsx            # Üst navigasyon
│   │       ├── Navbar.css
│   │       ├── Sidebar.tsx           # Yan menü (role-based)
│   │       └── Sidebar.css
│   │
│   ├── pages/                        # Sayfa bileşenleri
│   │   ├── auth/                     # Authentication sayfaları
│   │   │   ├── LoginPage.tsx
│   │   │   ├── RegisterPage.tsx
│   │   │   ├── EmailVerificationPage.tsx
│   │   │   ├── ForgotPasswordPage.tsx
│   │   │   ├── ResetPasswordPage.tsx
│   │   │   └── AuthPages.css
│   │   ├── DashboardPage.tsx
│   │   ├── DashboardPage.css
│   │   ├── ProfilePage.tsx
│   │   ├── ProfilePage.css
│   │   ├── NotFoundPage.tsx
│   │   └── NotFoundPage.css
│   │
│   ├── context/                      # React Context
│   │   └── AuthContext.tsx          # Authentication state yönetimi
│   │
│   ├── services/                     # API servisleri
│   │   ├── api/                      # Gerçek API çağrıları
│   │   │   ├── client.ts             # Axios instance + interceptors
│   │   │   ├── auth.service.ts       # Authentication servisleri
│   │   │   ├── user.service.ts       # User servisleri
│   │   │   ├── department.service.ts # Department servisleri
│   │   │   └── index.ts              # Export barrel
│   │   └── mocks/                    # MSW mock servisleri
│   │       ├── browser.ts            # MSW browser setup
│   │       ├── handlers.ts           # Mock request handlers
│   │       └── data.ts               # Mock data
│   │
│   ├── config/                       # Konfigürasyon
│   │   └── api.config.ts             # API endpoint tanımları
│   │
│   ├── types/                        # TypeScript type tanımları
│   │   └── api.types.ts             # API response types
│   │
│   ├── styles/                       # Global stiller
│   │   └── theme.css                 # CSS variables (Turkuaz tema)
│   │
│   ├── App.tsx                       # Ana uygulama bileşeni
│   ├── App.css
│   ├── main.tsx                      # Entry point
│   ├── index.css                    # Global CSS
│   └── vite-env.d.ts                 # Vite environment types
│
├── docs/                             # Dokümantasyon
│   ├── API_DOCUMENTATION.md
│   ├── SETUP.md
│   └── ...
│
├── Dockerfile                        # Multi-stage Docker build
├── docker-compose.yml                # Docker Compose yapılandırması
├── nginx.conf                        # Nginx konfigürasyonu
├── .dockerignore                    # Docker ignore dosyası
├── .env.example                     # Örnek environment dosyası
├── vite.config.ts                   # Vite konfigürasyonu
├── tsconfig.json                    # TypeScript konfigürasyonu
├── package.json                     # NPM dependencies
└── README.md
```

### Frontend Sayfalar ve Özellikler

#### 1. Authentication Sayfaları

**Login Page** (`/login`)
- Email ve şifre ile giriş
- "Beni hatırla" checkbox
- "Şifremi unuttum" linki
- Email doğrulama hatası durumunda "Doğrulama emaili tekrar gönder" butonu
- Form validation (Yup schema)
- Error handling ve toast notifications

**Register Page** (`/register`)
- Öğrenci ve öğretim üyesi kaydı
- Ad soyad, email, şifre, şifre tekrar
- Kullanıcı tipi seçimi (Student/Faculty)
- Öğrenci numarası / Personel numarası (role-based)
- Bölüm seçimi (backend'den dinamik çekiliyor)
- Şifre güçlülük kontrolü (min 8 karakter, büyük harf, rakam)
- Kullanım şartları checkbox
- Form validation (Yup schema)

**Email Verification Page** (`/verify-email/:token` veya `/verify-email?token=xxx`)
- Path parameter veya query parameter desteği
- Token doğrulama
- Başarı/hata durumu gösterimi
- Otomatik login sayfasına yönlendirme (3 saniye)

**Forgot Password Page** (`/forgot-password`)
- Email input
- Şifre sıfırlama linki gönderme
- Başarı mesajı gösterimi

**Reset Password Page** (`/reset-password/:token` veya `/reset-password?token=xxx`)
- Path parameter veya query parameter desteği
- Yeni şifre ve şifre tekrar input'ları
- Şifre güçlülük kontrolü
- Başarı durumunda otomatik login sayfasına yönlendirme

#### 2. Protected Sayfalar

**Dashboard Page** (`/dashboard`)
- Role-based içerik (Student/Faculty/Admin)
- Hoş geldin mesajı
- Kullanıcı adı gösterimi
- Placeholder kartlar (Part 2+ için)

**Profile Page** (`/profile`)
- Profil bilgileri görüntüleme
- Profil güncelleme formu (ad soyad, telefon)
- Profil fotoğrafı yükleme/değiştirme
- Email adresi (read-only)
- Kullanıcı tipi ve numara bilgileri
- Form validation

**NotFound Page** (`/404`)
- 404 error sayfası
- "Ana Sayfaya Dön" butonu

### Frontend Component'ler

#### Common Components

**Button**
- Variants: primary, secondary, outline, danger, ghost
- Sizes: sm, md, lg
- Loading state
- Full width option
- Turkuaz tema

**TextInput**
- Label, error, helper text desteği
- Left/right icon desteği
- Full width option
- Validation error gösterimi

**Select**
- Label, error, helper text desteği
- Placeholder desteği
- Full width option
- Validation error gösterimi

**LoadingSpinner**
- Sizes: sm, md, lg
- Full screen option
- Turkuaz tema

**ProtectedRoute**
- Authentication guard
- Role-based access control
- Loading state
- Redirect to login if not authenticated

#### Layout Components

**Navbar**
- Logo ve başlık
- Kullanıcı bilgileri (ad, rol)
- Profil fotoğrafı veya placeholder
- Dropdown menü (Profil, Çıkış)
- Responsive design

**Sidebar**
- Role-based navigation
- Active route highlighting
- Icon + label gösterimi
- Responsive design (mobile'da horizontal scroll)

### Frontend State Management

#### AuthContext

**Özellikler:**
- User state yönetimi
- Authentication state (isAuthenticated, isLoading)
- Login, register, logout fonksiyonları
- User update fonksiyonu
- Token yönetimi (localStorage)
- Auto token refresh (interceptor'da)

**State:**
```typescript
{
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (credentials) => Promise<void>;
  register: (data) => Promise<void>;
  logout: () => Promise<void>;
  updateUser: (data) => Promise<void>;
  refreshUser: () => Promise<void>;
}
```

#### Token Management

- **Access Token**: localStorage'da saklanır, 15 dakika geçerli
- **Refresh Token**: localStorage'da saklanır, 7 gün geçerli
- **Auto Refresh**: Axios interceptor'da 401 hatası durumunda otomatik token yenileme
- **Logout**: Tüm token'lar temizlenir

### Frontend API Entegrasyonu

#### API Client (Axios)

**Özellikler:**
- Base URL konfigürasyonu (environment variable'dan)
- Request interceptor: Otomatik token injection
- Response interceptor: Error handling, token refresh
- Standart error response formatı
- Timeout yönetimi (30 saniye)

**Token Refresh Flow:**
```
1. API isteği yapılır
2. 401 Unauthorized hatası alınır
3. Refresh token ile yeni access token alınır
4. Orijinal istek yeni token ile tekrar gönderilir
5. Başarısız olursa logout yapılır
```

#### API Services

**authService**
- `register()` - Kullanıcı kaydı
- `login()` - Giriş yapma
- `logout()` - Çıkış yapma
- `verifyEmail()` - Email doğrulama
- `resendVerificationEmail()` - Doğrulama emaili tekrar gönder
- `forgotPassword()` - Şifre sıfırlama isteği
- `resetPassword()` - Şifre sıfırlama
- `refreshToken()` - Token yenileme

**userService**
- `getMe()` - Profil görüntüleme
- `updateMe()` - Profil güncelleme
- `uploadProfilePicture()` - Profil fotoğrafı yükleme
- `getUsers()` - Kullanıcı listesi (Admin)

**departmentService**
- `getDepartments()` - Bölüm listesi
- `getDepartmentById()` - Bölüm detayı (ID ile)
- `getDepartmentByCode()` - Bölüm detayı (kod ile)

#### Backend Field Mapping

Frontend ve backend arasında field mapping yapılıyor:

| Frontend | Backend | Dönüşüm |
|----------|---------|---------|
| `name` | `firstName + lastName` | Split/join |
| `phone` | `phoneNumber` | Direct mapping |
| `profilePictureUrl` | `profilePicture` | Direct mapping |
| `role` (lowercase) | `role` (uppercase) | Case normalization |

### Frontend Routing

#### Route Yapısı

**Public Routes:**
- `/login` - Giriş sayfası
- `/register` - Kayıt sayfası
- `/verify-email/:token` - Email doğrulama (path param)
- `/verify-email` - Email doğrulama (query param)
- `/forgot-password` - Şifre sıfırlama isteği
- `/reset-password/:token` - Şifre sıfırlama (path param)
- `/reset-password` - Şifre sıfırlama (query param)

**Protected Routes:**
- `/dashboard` - Ana sayfa (role-based content)
- `/profile` - Profil sayfası

**Default Route:**
- `/` - Dashboard'a yönlendirme

**404 Route:**
- `*` - NotFoundPage

#### Protected Route Guard

- `ProtectedRoute` component'i ile route koruması
- Authentication kontrolü
- Role-based access control (gelecekte)
- Loading state gösterimi
- Redirect to login if not authenticated

### Frontend Styling

#### Turkuaz Tema

**Renk Paleti:**
- Primary: `#40e0d0` (Turkuaz)
- Primary Dark: `#2eb8a8`
- Primary Light: `#6ee6d8`
- Primary Lightest: `#cef8f4`

**CSS Variables:**
- Renkler (primary, success, error, warning, info)
- Arka plan renkleri
- Metin renkleri
- Border renkleri
- Shadow'lar
- Border radius
- Spacing
- Typography
- Transitions

**Responsive Design:**
- Mobile-first yaklaşım
- Breakpoint: 768px
- Navbar: Mobile'da kullanıcı bilgileri gizlenir
- Sidebar: Mobile'da horizontal scroll
- Form'lar: Mobile'da full width

### Frontend Testing

#### Test Stratejisi

- **Component Tests**: React Testing Library ile component testleri (planlanıyor)
- **Integration Tests**: User flow testleri (planlanıyor)
- **E2E Tests**: Cypress/Playwright (bonus, planlanıyor)

#### Test Araçları

- **Vitest**: Test framework (Vite ile entegre)
- **React Testing Library**: Component testing utilities
- **MSW**: API mocking (development'ta kullanılıyor)

#### Test Coverage Hedefi

- **Frontend**: Minimum %75 code coverage (yönerge gereksinimi)

### Frontend Deployment

#### Development Environment

- **Local Development**: `npm run dev` (Vite dev server, port 3000)
- **Hot Module Replacement**: Kod değişikliklerinde anında güncelleme
- **Mock API**: MSW ile backend olmadan geliştirme

#### Production Environment

- **Server**: DigitalOcean Droplet (Ubuntu 22.04)
- **IP Address**: 138.68.99.35
- **Port**: 3000
- **Build Tool**: Vite
- **Web Server**: Nginx (Alpine)

#### Deployment Yöntemi

- **Docker Compose**: Containerize edilmiş frontend
- **Multi-stage Dockerfile**: Optimize edilmiş production build
- **Nginx**: Static file serving, SPA routing
- **Environment Variables**: `.env` dosyası ile konfigürasyon
- **Health Checks**: Container sağlık kontrolü

#### Build Process

```
1. npm ci (dependency installation)
2. Environment variables inject (build-time)
3. npm run build (TypeScript compile + Vite build)
4. Output: dist/ klasörü
5. Nginx ile serve edilir
```

#### Deployment URL'leri

- **Frontend**: `http://138.68.99.35:3000`
- **API Gateway**: `http://138.68.99.35:8080/api/v1`

### Frontend Design Patterns

#### 1. Component Composition
- Küçük, yeniden kullanılabilir component'ler
- Props ile data flow
- Children pattern

#### 2. Custom Hooks Pattern
- `useAuth()` hook (AuthContext'ten)
- Gelecekte daha fazla custom hook eklenecek

#### 3. Service Layer Pattern
- API çağrıları service fonksiyonlarında toplanmış
- Business logic service katmanında
- Component'ler sadece UI logic ile ilgilenir

#### 4. Error Boundary Pattern
- Gelecekte error boundary eklenecek
- Global error handling

#### 5. Route Guard Pattern
- `ProtectedRoute` component ile route koruması
- Authentication ve authorization kontrolü

### Frontend Güvenlik

#### Authentication Flow

```
1. Kullanıcı kaydı
   └─> Register API çağrısı
   └─> Access token + Refresh token alınır
   └─> Token'lar localStorage'a kaydedilir
   └─> User state güncellenir
   └─> Email doğrulama sayfasına yönlendirme

2. Email doğrulama
   └─> Token URL'den alınır (path veya query param)
   └─> Verify Email API çağrısı
   └─> Başarılı ise login sayfasına yönlendirme

3. Login
   └─> Login API çağrısı
   └─> Access token + Refresh token alınır
   └─> Token'lar localStorage'a kaydedilir
   └─> User state güncellenir
   └─> Dashboard'a yönlendirme

4. Token yenileme (otomatik)
   └─> 401 hatası alındığında
   └─> Refresh token ile yeni access token alınır
   └─> Orijinal istek tekrar gönderilir

5. Logout
   └─> Logout API çağrısı
   └─> Token'lar localStorage'dan silinir
   └─> User state temizlenir
   └─> Login sayfasına yönlendirme
```

#### Input Validation

- **Client-side**: Yup schema validation
- **Form validation**: React Hook Form ile entegre
- **Real-time validation**: Kullanıcı input'u sırasında
- **Error messages**: Türkçe, kullanıcı dostu mesajlar

#### XSS Prevention

- React otomatik olarak XSS'e karşı korumalı (JSX escaping)
- Input sanitization (gelecekte eklenebilir)

#### CORS

- Backend'de CORS yapılandırması
- Frontend'den backend'e güvenli istekler

---

## 📊 Frontend İstatistikleri

### Kod Metrikleri

- **Toplam TypeScript/TSX Dosyası**: ~40+ dosya
- **Page Components**: 7 (Login, Register, EmailVerification, ForgotPassword, ResetPassword, Dashboard, Profile, NotFound)
- **Common Components**: 5 (Button, TextInput, Select, LoadingSpinner, ProtectedRoute)
- **Layout Components**: 2 (Navbar, Sidebar)
- **Service Functions**: 3 (authService, userService, departmentService)
- **Context Providers**: 1 (AuthContext)

### Component Metrikleri

- **Reusable Components**: 5
- **Page Components**: 7
- **Layout Components**: 2
- **Total Components**: 14+

### API Service Metrikleri

- **Authentication Endpoints**: 8 (register, login, logout, refresh, verify-email, resend-verification, forgot-password, reset-password)
- **User Management Endpoints**: 4 (getMe, updateMe, uploadProfilePicture, getUsers)
- **Department Endpoints**: 3 (getDepartments, getDepartmentById, getDepartmentByCode)
- **Total Service Functions**: 15+

### Route Metrikleri

- **Public Routes**: 7
- **Protected Routes**: 2
- **Total Routes**: 9

---

## 🔄 Frontend Geliştirme Süreci

### Part 1 Tamamlanan Özellikler

1. ✅ **Proje Yapısı**: React + TypeScript + Vite kurulumu
2. ✅ **Routing**: React Router v6 ile client-side routing
3. ✅ **Authentication Pages**: Login, Register, Email Verification, Password Reset
4. ✅ **Protected Pages**: Dashboard, Profile
5. ✅ **State Management**: AuthContext ile global state yönetimi
6. ✅ **API Integration**: Axios ile backend entegrasyonu
7. ✅ **Form Management**: React Hook Form + Yup validation
8. ✅ **UI Components**: Reusable component library
9. ✅ **Styling**: Turkuaz tema, responsive design
10. ✅ **Mock API**: MSW ile development desteği
11. ✅ **Docker**: Containerization ve deployment
12. ✅ **TypeScript**: Type safety ve code quality

---

## 🔄 Database Geliştirme Süreci

### Part 1 Tamamlanan Özellikler

1. ✅ **Proje Yapısı**: Docker Compose ile containerization
2. ✅ **Veritabanı Şeması**: 7 tablo tasarımı ve implementasyonu
3. ✅ **Migration Dosyaları**: Version-controlled SQL migration'lar
4. ✅ **Seed Data**: Test verileri (8 kullanıcı, 4 bölüm)
5. ✅ **Otomatik Initialization**: Container başlatıldığında otomatik setup
6. ✅ **phpMyAdmin**: Veritabanı yönetim arayüzü
7. ✅ **Production Deployment**: DigitalOcean'a deploy
8. ✅ **Güvenlik**: Firewall yapılandırması ve port koruması
9. ✅ **Dokümantasyon**: DATABASE_SCHEMA.md ve DOCKER_SETUP.md
10. ✅ **Backend Entegrasyonu**: Spring Boot ile bağlantı hazır

### Database Geliştirme Prensipleri

- **Normalization**: 3NF minimum
- **Indexing**: Performance için gerekli alanlara index
- **Constraints**: UNIQUE, NOT NULL, FOREIGN KEY constraints
- **Soft Delete**: deleted_at timestamp pattern
- **Version Control**: Migration dosyaları ile schema versioning

### Frontend Geliştirme Prensipleri

- **Component-Based**: Küçük, yeniden kullanılabilir component'ler
- **Type Safety**: TypeScript ile tip güvenliği
- **Clean Code**: Okunabilir ve maintainable kod
- **User Experience**: Kullanıcı dostu arayüz, loading states, error handling
- **Responsive Design**: Mobile-first yaklaşım
- **Accessibility**: ARIA labels, keyboard navigation (gelecekte iyileştirilecek)
- **Performance**: Code splitting, lazy loading (gelecekte eklenecek)

---

## 🗄 Database

### Database Teknoloji Stack

| Teknoloji | Versiyon | Kullanım Amacı |
|-----------|----------|----------------|
| **MySQL** | 8.0 | İlişkisel veritabanı |
| **Docker** | Latest | Containerization |
| **Docker Compose** | Latest | Multi-container orchestration |
| **phpMyAdmin** | Latest | Veritabanı yönetim arayüzü |

### Database Mimari

```
┌─────────────────────────────────────────────────────────────────┐
│                    Docker Host                                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                  smart_campus_network                      │  │
│  │                                                            │  │
│  │   ┌─────────────────────┐      ┌─────────────────────┐   │  │
│  │   │   smart_campus_db   │      │  smart_campus_      │   │  │
│  │   │       (MySQL)       │◄─────│     phpmyadmin      │   │  │
│  │   │                     │      │                     │   │  │
│  │   │   • 7 Tablo         │      │   • Web UI          │   │  │
│  │   │   • UTF8MB4         │      │   • Port 8081       │   │  │
│  │   │   • Port 3306       │      │                     │   │  │
│  │   └──────────┬──────────┘      └─────────────────────┘   │  │
│  │              │                                            │  │
│  │              ▼                                            │  │
│  │   ┌─────────────────────┐                                │  │
│  │   │     mysql_data      │                                │  │
│  │   │  (Persistent Vol.)  │                                │  │
│  │   └─────────────────────┘                                │  │
│  └───────────────────────────────────────────────────────────┘  │
│                              │                                   │
│                              ▼                                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Backend (Spring Boot)                   │  │
│  │              jdbc:mysql://smart_campus_db:3306             │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Database Proje Yapısı

```
smart-campus-database/
├── docker/
│   ├── docker-compose.yml      # Docker Compose yapılandırması
│   └── .env                     # Environment variables (git'e eklenmez)
├── migrations/
│   ├── V1__create_departments.sql
│   ├── V2__create_users.sql
│   ├── V3__create_students.sql
│   ├── V4__create_faculty.sql
│   └── V5__create_auth_tables.sql
├── seeds/
│   ├── 01_departments.sql
│   ├── 02_users.sql
│   ├── 03_students.sql
│   └── 04_faculty.sql
├── scripts/
│   ├── init.sql                # Başlangıç script'i (tüm tablolar)
│   └── seed.sql                # Test verileri script'i
├── docs/
│   ├── DATABASE_SCHEMA.md       # Veritabanı şema detayları
│   ├── DOCKER_SETUP.md          # Docker kurulum rehberi
│   └── FINAL_PROJECT_ASSIGNMENT.md
├── .gitignore
├── .env.example                 # Örnek environment dosyası
└── README.md
```

### Veritabanı Şeması (Part 1)

#### Tablo Özeti

| # | Tablo | Açıklama | İlişkiler |
|---|-------|----------|-----------|
| 1 | `departments` | Akademik bölümler | - |
| 2 | `users` | Tüm kullanıcıların temel bilgileri | - |
| 3 | `students` | Öğrenci akademik bilgileri | users, departments |
| 4 | `faculty` | Öğretim üyesi bilgileri | users, departments |
| 5 | `refresh_tokens` | JWT refresh token'ları | users |
| 6 | `email_verification_tokens` | Email doğrulama token'ları | users |
| 7 | `password_reset_tokens` | Şifre sıfırlama token'ları | users |

#### ER Diyagramı

```
┌─────────────────┐       ┌─────────────────┐
│   departments   │       │     users       │
├─────────────────┤       ├─────────────────┤
│ id (PK)         │       │ id (PK)         │
│ name            │       │ email (UK)      │
│ code (UK)       │       │ password_hash   │
│ faculty_name    │       │ first_name      │
│ created_at      │       │ last_name       │
└────────┬────────┘       │ phone_number    │
         │                │ profile_picture │
         │                │ role (ENUM)     │
         │                │ is_verified     │
         │                │ is_active       │
         │                │ created_at      │
         │                │ updated_at      │
         │                │ deleted_at      │
         │                └────────┬────────┘
         │                         │
    ┌────┴────┐              ┌─────┴─────┐
    │         │              │           │
    ▼         ▼              ▼           ▼
┌─────────┐ ┌─────────┐  ┌─────────┐ ┌─────────┐ ┌─────────┐
│students │ │ faculty │  │refresh_ │ │email_   │ │password_│
│         │ │         │  │tokens   │ │verif_   │ │reset_   │
├─────────┤ ├─────────┤  ├─────────┤ │tokens   │ │tokens   │
│id (PK)  │ │id (PK)  │  │id (PK)  │ ├─────────┤ ├─────────┤
│user_id  │ │user_id  │  │user_id  │ │id (PK)  │ │id (PK)  │
│(FK,UK)  │ │(FK,UK)  │  │(FK)     │ │user_id  │ │user_id  │
│dept_id  │ │dept_id  │  │token    │ │(FK)     │ │(FK)     │
│(FK)     │ │(FK)     │  │(UK)     │ │token    │ │token    │
│student_ │ │employee_│  │expiry_  │ │(UK)     │ │(UK)     │
│number   │ │number   │  │date     │ │expiry_  │ │expiry_  │
│(UK)     │ │(UK)     │  └─────────┘ │date     │ │date     │
│gpa      │ │title    │              └─────────┘ └─────────┘
│cgpa     │ │office_  │
└─────────┘ │location │
            └─────────┘
```

### Database Özellikleri

#### Normalizasyon

- **3NF (Third Normal Form)**: Tüm tablolar normalizasyon kurallarına uygun
- **Foreign Keys**: CASCADE ve RESTRICT uygun kullanımı
- **Indexes**: Performance için gerekli alanlara index (email, role, student_number, vb.)
- **Constraints**: CHECK, UNIQUE, NOT NULL constraints

#### Veri Tipleri

- **BIGINT**: Primary key'ler ve foreign key'ler
- **VARCHAR**: String alanlar (email, name, vb.)
- **ENUM**: Role alanı (STUDENT, FACULTY, ADMIN)
- **DECIMAL**: GPA ve CGPA (3,2 format)
- **TIMESTAMP**: Zaman damgaları (created_at, updated_at, deleted_at)
- **TINYINT(1)**: Boolean değerler (is_verified, is_active)

#### Güvenlik

- **Soft Delete**: `deleted_at` timestamp ile soft delete pattern
- **Password Hashing**: Backend'de BCrypt ile hashleme
- **Token Expiry**: Email verification (24 saat), password reset (15 dakika)
- **Unique Constraints**: Email, student_number, employee_number unique

### Database Migration Stratejisi

#### Manuel SQL Migration

Database repository'si için **manuel SQL dosyaları** kullanılmaktadır:

- **Migration Dosyaları**: `migrations/V1__*.sql` formatında
- **Sıralı Çalıştırma**: Docker container başlatıldığında otomatik çalışır
- **Version Control**: Her migration versiyon numarası ile işaretlenmiş

#### Init Scripts

- **init.sql**: Tüm tabloları oluşturur (Docker entrypoint'te otomatik çalışır)
- **seed.sql**: Test verilerini yükler (Docker entrypoint'te otomatik çalışır)

> **Not:** Backend repository'sinde Flyway kullanılacak (ileride entegre edilecek).

### Database Seed Data

#### Bölümler (4 adet)

| Kod | Bölüm Adı | Fakülte |
|-----|-----------|---------|
| CENG | Bilgisayar Mühendisliği | Mühendislik Fakültesi |
| EEE | Elektrik-Elektronik Mühendisliği | Mühendislik Fakültesi |
| ME | Makine Mühendisliği | Mühendislik Fakültesi |
| BA | İşletme | İktisadi ve İdari Bilimler Fakültesi |

#### Test Kullanıcıları (8 adet)

- **1 Admin**: admin@smartcampus.edu.tr
- **2 Öğretim Üyesi**: ahmet.yilmaz@smartcampus.edu.tr, ayse.demir@smartcampus.edu.tr
- **5 Öğrenci**: ali.kaya, zeynep.celik, mehmet.ozturk, fatma.sahin, emre.arslan

> **Not:** Tüm test kullanıcılarının şifreleri backend tarafında BCrypt ile hashlenmiş olarak saklanır.

### Database Deployment

#### Production Environment

- **Server**: DigitalOcean Droplet (Ubuntu 22.04)
- **IP Address**: 138.68.99.35
- **Database**: MySQL 8.0 (Docker container)
- **Port**: 3306 (internal only - firewall ile korumalı)
- **phpMyAdmin**: Port 8081 (internal only - firewall ile korumalı)

#### Deployment Yöntemi

- **Docker Compose**: MySQL ve phpMyAdmin containerize edilmiş
- **Volume Persistence**: `mysql_data` volume ile veri kalıcılığı
- **Automatic Initialization**: Container başlatıldığında otomatik tablo oluşturma ve seed data yükleme
- **Health Checks**: Container sağlık kontrolü
- **Firewall**: MySQL ve phpMyAdmin portları dışarıya kapalı (güvenlik)

#### Deployment URL'leri

- **MySQL**: `smart_campus_db:3306` (Docker internal network)
- **phpMyAdmin**: `http://138.68.99.35:8081` (sadece internal - firewall kapalı)

### Database Yönetim

#### Container Komutları

```bash
# Container'ları başlat
docker compose up -d

# MySQL'e bağlan
docker exec -it smart_campus_db mysql -u root -p

# Veritabanını yedekle
docker exec smart_campus_db mysqldump -u root -p smart_campus > backup.sql

# Seed verilerini yükle (manuel)
docker exec -i smart_campus_db mysql -u root -p smart_campus < scripts/seed.sql
```

#### Veritabanını Sıfırlama

```bash
# Volume dahil tüm verileri sil ve yeniden oluştur
docker compose down -v
docker compose up -d
```

> ⚠️ Bu işlem tüm verileri siler ve seed data'yı yeniden yükler.

### Database Güvenlik

#### Production Güvenlik Kuralları

| Kural | Açıklama |
|-------|----------|
| **Güçlü Şifreler** | En az 16 karakter, büyük/küçük harf, rakam, özel karakter |
| **Firewall** | MySQL (3306) ve phpMyAdmin (8081) portları dışarıya kapalı |
| **.env Koruması** | .env dosyası asla Git'e eklenmemeli |
| **SSH Key** | Sunucuya şifre yerine SSH key ile bağlanın |
| **Düzenli Yedekleme** | Veritabanını düzenli olarak yedekleyin |

#### Port Güvenliği

```
✅ Port 22   → SSH (açık)
✅ Port 80   → HTTP/Frontend (açık)
✅ Port 443  → HTTPS (açık)
🔒 Port 3306 → MySQL (kapalı - sadece Docker internal)
🔒 Port 8081 → phpMyAdmin (kapalı - sadece Docker internal)
```

### Database İstatistikleri

#### Tablo Metrikleri

- **Toplam Tablo**: 7 tablo
- **Index Sayısı**: 15+ index (performance için)
- **Foreign Key Sayısı**: 6 foreign key relationship
- **Unique Constraint**: 5 unique constraint

#### Veri Metrikleri

- **Toplam Kullanıcı**: 8 (1 admin + 2 faculty + 5 student)
- **Toplam Bölüm**: 4
- **Seed Data**: Otomatik yükleniyor

### Database Dokümantasyon

- **DATABASE_SCHEMA.md**: Tablo yapıları, ilişkiler, veri tipleri
- **DOCKER_SETUP.md**: Docker kurulum ve yapılandırma rehberi
- **README.md**: Proje genel bilgileri ve kurulum talimatları

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

## 📊 Database İstatistikleri

### Tablo Metrikleri

- **Toplam Tablo**: 7 tablo
- **Index Sayısı**: 15+ index (performance için)
- **Foreign Key Sayısı**: 6 foreign key relationship
- **Unique Constraint**: 5 unique constraint

### Veri Metrikleri

- **Toplam Kullanıcı**: 8 (1 admin + 2 faculty + 5 student)
- **Toplam Bölüm**: 4
- **Seed Data**: Otomatik yükleniyor

### Migration Metrikleri

- **Migration Dosyaları**: 5 (V1-V5)
- **Seed Dosyaları**: 4 (01-04)
- **Init Scripts**: 2 (init.sql, seed.sql)

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
- Frontend kısmı **Part 1** kapsamında tamamlanmıştır.
- Database kısmı **Part 1** kapsamında tamamlanmıştır.
- Production deployment başarıyla tamamlanmıştır (138.68.99.35).
- Tüm API endpoint'leri Swagger UI üzerinden test edilebilir.
- Frontend, Backend ve Database entegrasyonu başarıyla tamamlanmıştır.

---

**Son Güncelleme:** 10 Aralık 2025  
**Hazırlayan:** 
- **Ahmet Bera Çelik** (Backend Geliştirici & Proje Yöneticisi) - Backend ve Database bölümleri
- **Tuğba Nur Uygun & Öznur Beyazpınar** (Frontend Geliştiriciler) - Frontend bölümü
- **Furkan Kapucu** (Database Geliştirici ve Tester) - Database bölümü

