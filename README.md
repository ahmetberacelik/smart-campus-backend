# Smart Campus Backend

Akıllı Kampüs Ekosistem Yönetim Platformu - Backend Services

## İçindekiler

- [Proje Hakkında](#proje-hakkında)
- [Teknoloji Stack](#teknoloji-stack)
- [Mimari](#mimari)
- [Gereksinimler](#gereksinimler)
- [Kurulum](#kurulum)
- [Çalıştırma](#çalıştırma)
- [API Dokümantasyonu](#api-dokümantasyonu)
- [Proje Yapısı](#proje-yapısı)
- [Environment Variables](#environment-variables)

---

## Proje Hakkında

Smart Campus, bir üniversite kampüsünün günlük operasyonlarını dijitalleştiren kapsamlı bir web uygulamasıdır.

### Part 1 Kapsamı - Authentication & User Management
- Kullanıcı Kaydı (Öğrenci, Öğretim Üyesi, Admin)
- JWT Tabanlı Authentication
- Email Doğrulama
- Şifre Sıfırlama
- Profil Yönetimi
- Profil Fotoğrafı Yükleme

### Part 2 Kapsamı - Academic Management & GPS Attendance
- Ders Yönetimi (CRUD, önkoşul sistemi)
- Ders Kayıt Sistemi (kapasite kontrolü, çakışma kontrolü)
- Not Yönetimi ve Transkript (JSON + PDF)
- GPS Tabanlı Yoklama Sistemi
- QR Kod ile Yoklama (backup)
- GPS Spoofing Tespiti
- Mazeret Yönetimi
- Devamsızlık Uyarı Sistemi (Scheduled Job)

---

## Teknoloji Stack

| Teknoloji | Versiyon | Açıklama |
|-----------|----------|----------|
| Java | 17 | Programlama dili |
| Spring Boot | 3.2.x | Backend framework |
| Spring Cloud Gateway | - | API Gateway |
| Spring Security | 6.x | Authentication & Authorization |
| Spring Data JPA | - | ORM |
| MySQL | 8.0 | Veritabanı |
| JWT | - | Token tabanlı auth |
| Docker | - | Containerization |
| Maven | - | Build tool |
| iText7 | 7.2.5 | PDF oluşturma |

---

## Mimari

```
                    ┌─────────────────┐
                    │     Clients     │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │   API Gateway   │
                    │    (Port 8080)  │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌───────────────┐   ┌───────────────┐   ┌─────────────────┐
│  Auth Service │   │   Academic    │   │   Attendance    │
│  (Port 8081)  │   │    Service    │   │     Service     │
│               │   │  (Port 8082)  │   │   (Port 8083)   │
└───────┬───────┘   └───────┬───────┘   └────────┬────────┘
        │                   │                    │
        └───────────────────┼────────────────────┘
                            │
           ┌────────────────┼────────────────┐
           ▼                ▼                ▼
    ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
    │    MySQL    │  │  DO Spaces  │  │ SendGrid    │
    └─────────────┘  └─────────────┘  └─────────────┘
```

### Servisler

| Servis | Port | Sorumluluk |
|--------|------|------------|
| **api-gateway** | 8080 | Request routing, CORS |
| **auth-service** | 8081 | Authentication, User Management |
| **academic-service** | 8082 | Course, Enrollment, Grade Management |
| **attendance-service** | 8083 | GPS Attendance, QR Code, Excuse |

---

## Gereksinimler

### Lokal Geliştirme
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Production
- DigitalOcean Droplet
- Docker & Docker Compose
- DigitalOcean Spaces (File Storage)

---

## Kurulum

### Production Deployment (138.68.99.35)

Detaylı deployment dokümantasyonu için: [DEPLOYMENT.md](docs/DEPLOYMENT.md)

**Hızlı Başlangıç:**
```bash
# 1. Repository'yi klonla
git clone https://github.com/your-username/smart-campus-backend.git
cd smart-campus-backend

# 2. Environment dosyası oluştur
cp .env.example .env
nano .env  # Gerekli değerleri doldur

# 3. Deployment script'ini çalıştır
chmod +x deploy.sh
./deploy.sh
```

### Lokal Geliştirme

```bash
# 1. Repository'yi klonla
git clone https://github.com/your-username/smart-campus-backend.git
cd smart-campus-backend

# 2. Environment dosyası oluştur
cp .env.example .env

# 3. Servisleri başlat
docker-compose up -d --build
```

---

## Çalıştırma

### Docker ile (Önerilen)

```bash
# Tüm servisleri başlat
docker-compose up -d

# Logları görüntüle
docker-compose logs -f

# Servisleri durdur
docker-compose down
```

### Lokal Geliştirme (Maven)

```bash
# Parent projeden tüm modülleri derle
mvn clean install

# Her servisi ayrı terminalde başlat
cd api-gateway && mvn spring-boot:run
cd auth-service && mvn spring-boot:run
cd academic-service && mvn spring-boot:run
cd attendance-service && mvn spring-boot:run
```

---

## API Dokümantasyonu

### Production (138.68.99.35)

| Servis | URL |
|--------|-----|
| **API Gateway** | http://138.68.99.35:8080 |
| **Auth Service Swagger** | http://138.68.99.35:8081/swagger-ui.html |
| **Academic Service Swagger** | http://138.68.99.35:8082/swagger-ui.html |
| **Attendance Service Swagger** | http://138.68.99.35:8083/swagger-ui.html |

### Temel Endpoints

**Authentication (auth-service)**
| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | `/api/v1/auth/register` | Kullanıcı kaydı |
| POST | `/api/v1/auth/login` | Giriş |
| POST | `/api/v1/auth/refresh` | Token yenileme |
| GET | `/api/v1/users/me` | Profil görüntüleme |

**Academic (academic-service)**
| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/api/v1/courses` | Ders listesi |
| POST | `/api/v1/enrollments` | Derse kayıt |
| GET | `/api/v1/enrollments/my-courses` | Kayıtlı derslerim |
| GET | `/api/v1/enrollments/transcript` | Transkript JSON |
| GET | `/api/v1/enrollments/transcript/pdf` | Transkript PDF |

**Attendance (attendance-service)**
| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | `/api/v1/attendance/sessions` | Yoklama oturumu aç |
| POST | `/api/v1/attendance/sessions/{id}/checkin` | GPS ile yoklama ver |
| GET | `/api/v1/attendance/my-attendance` | Yoklama durumum |

---

## Proje Yapısı

```
smart-campus-backend/
├── api-gateway/                 # API Gateway servisi
├── auth-service/                # Authentication servisi
├── academic-service/            # Akademik yönetim servisi
│   └── src/main/java/com/smartcampus/academic/
│       ├── controller/          # REST controllers
│       ├── service/             # Business logic
│       ├── entity/              # JPA entities
│       └── repository/          # JPA repositories
├── attendance-service/          # Yoklama servisi
│   └── src/main/java/com/smartcampus/attendance/
│       ├── controller/          # REST controllers
│       ├── service/             # Business logic
│       ├── scheduler/           # Scheduled jobs
│       └── util/                # HaversineCalculator, etc.
├── docs/                        # Dokümantasyon
├── docker-compose.yml           # Docker Compose yapılandırması
└── pom.xml                      # Parent POM
```

---

## Environment Variables

| Değişken | Açıklama |
|----------|----------|
| `DB_HOST` | MySQL host |
| `DB_PORT` | MySQL port |
| `DB_NAME` | Database adı |
| `JWT_SECRET` | JWT secret key |
| `SENDGRID_API_KEY` | SendGrid API key |
| `DO_SPACES_KEY` | DigitalOcean Spaces key |
| `FRONTEND_URL` | Frontend URL |

**Tüm değişkenler için `.env.example` dosyasına bakın.**

---

## İlişkili Repository'ler

| Repository | Açıklama |
|------------|----------|
| [smart-campus-database](https://github.com/your-username/smart-campus-database) | Veritabanı şeması |
| [smart-campus-frontend](https://github.com/your-username/smart-campus-frontend) | React frontend |

---

## Lisans

Bu proje **Recep Tayyip Erdoğan Üniversitesi Web ve Mobil Programlama Dersi** kapsamında eğitim amaçlı geliştirilmiştir.

---

**Smart Campus Backend** - Part 1 & Part 2 Tamamlandı