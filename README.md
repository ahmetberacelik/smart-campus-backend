# 🎓 Smart Campus Backend

Akıllı Kampüs Ekosistem Yönetim Platformu - Backend Services

## 📋 İçindekiler

- [Proje Hakkında](#-proje-hakkında)
- [Teknoloji Stack](#-teknoloji-stack)
- [Mimari](#-mimari)
- [Gereksinimler](#-gereksinimler)
- [Kurulum](#-kurulum)
- [Çalıştırma](#-çalıştırma)
- [API Dokümantasyonu](#-api-dokümantasyonu)
- [Proje Yapısı](#-proje-yapısı)
- [Environment Variables](#-environment-variables)

---

## 🎯 Proje Hakkında

Smart Campus, bir üniversite kampüsünün günlük operasyonlarını dijitalleştiren kapsamlı bir web uygulamasıdır.

### Part 1 Kapsamı
- ✅ Kullanıcı Kaydı (Öğrenci, Öğretim Üyesi, Admin)
- ✅ JWT Tabanlı Authentication
- ✅ Email Doğrulama
- ✅ Şifre Sıfırlama
- ✅ Profil Yönetimi
- ✅ Profil Fotoğrafı Yükleme

---

## 🛠 Teknoloji Stack

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

---

## 🏗 Mimari

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
                             ▼
                    ┌─────────────────┐
                    │  Auth Service   │
                    │   (Port 8081)   │
                    └────────┬────────┘
                             │
           ┌─────────────────┼─────────────────┐
           ▼                 ▼                 ▼
    ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
    │    MySQL    │   │  DO Spaces  │   │ Gmail SMTP  │
    └─────────────┘   └─────────────┘   └─────────────┘
```

---

## 📌 Gereksinimler

### Lokal Geliştirme
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Production
- DigitalOcean Droplet
- Docker & Docker Compose
- DigitalOcean Spaces (File Storage)

---

## 🚀 Kurulum

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

**Önemli:** Production'da `.env` dosyasında:
- `AUTH_SERVICE_HOST=auth-service` (Docker network içinde)
- `DB_HOST=138.68.99.35` (Mevcut database)
- `FRONTEND_URL=http://138.68.99.35:3000`

### Lokal Geliştirme

### 1. Repository'yi Klonla

```bash
git clone https://github.com/your-username/smart-campus-backend.git
cd smart-campus-backend
```

### 2. Environment Dosyası Oluştur

```bash
cp .env.example .env
# .env dosyasını düzenle ve gerekli değerleri gir
# Lokal için: AUTH_SERVICE_HOST=localhost
```

### 3. Servisleri Başlat

```bash
docker-compose up -d --build
```

---

## 🏃 Çalıştırma

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

# API Gateway'i başlat
cd api-gateway
mvn spring-boot:run

# Auth Service'i başlat (yeni terminal)
cd auth-service
mvn spring-boot:run
```

---

## 📚 API Dokümantasyonu

### Production (138.68.99.35)

| Servis | URL |
|--------|-----|
| **API Gateway** | http://138.68.99.35:8080 |
| **Auth Service** | http://138.68.99.35:8081 |
| **Swagger UI** | http://138.68.99.35:8081/swagger-ui.html |

### Lokal Geliştirme

| Servis | URL |
|--------|-----|
| **API Gateway** | http://localhost:8080 |
| **Auth Service** | http://localhost:8081 |
| **Swagger UI** | http://localhost:8081/swagger-ui.html |

### Temel Endpoints

**Not:** Tüm endpoint'ler **8080 portu** üzerinden API Gateway üzerinden erişilebilir.

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | `/api/v1/auth/register` | Kullanıcı kaydı |
| POST | `/api/v1/auth/login` | Giriş |
| POST | `/api/v1/auth/refresh` | Token yenileme |
| POST | `/api/v1/auth/logout` | Çıkış |
| GET | `/api/v1/users/me` | Profil görüntüleme |
| PUT | `/api/v1/users/me` | Profil güncelleme |

**Örnek:**
```bash
# Production
curl http://138.68.99.35:8080/api/v1/auth/login

# Lokal
curl http://localhost:8080/api/v1/auth/login
```

---

## 📁 Proje Yapısı

```
smart-campus-backend/
├── api-gateway/                 # API Gateway servisi
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   ├── Dockerfile
│   └── pom.xml
│
├── auth-service/                # Authentication servisi
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
│   │   │   │   └── util/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── docs/                        # Dokümantasyon
├── docker-compose.yml           # Docker Compose yapılandırması
├── pom.xml                      # Parent POM
├── .env.example                 # Örnek environment dosyası
├── .gitignore
└── README.md
```

---

## 🔐 Environment Variables

| Değişken | Açıklama | Production | Lokal |
|----------|----------|------------|-------|
| `DB_HOST` | MySQL host | `138.68.99.35` | `localhost` |
| `DB_PORT` | MySQL port | `3306` | `3306` |
| `DB_NAME` | Database adı | `smart_campus` | `smart_campus` |
| `AUTH_SERVICE_HOST` | Auth service host | `auth-service` | `localhost` |
| `AUTH_SERVICE_PORT` | Auth service port | `8081` | `8081` |
| `FRONTEND_URL` | Frontend URL | `http://138.68.99.35:3000` | `http://localhost:3000` |
| `CORS_ALLOWED_ORIGINS` | CORS origins | `http://138.68.99.35:3000` | `http://localhost:3000` |
| `JWT_SECRET` | JWT secret key | Güçlü key! | Güçlü key! |

**Tüm değişkenler için `.env.example` dosyasına bakın.**

**Önemli Notlar:**
- Production'da `AUTH_SERVICE_HOST=auth-service` olmalı (Docker network)
- Lokal'de `AUTH_SERVICE_HOST=localhost` kullanılabilir
- `JWT_SECRET` production'da mutlaka güçlü olmalı (min 32 karakter)

---

## 🚀 Deployment

**Production Deployment (138.68.99.35):**

Detaylı deployment dokümantasyonu:
- [DEPLOYMENT.md](docs/DEPLOYMENT.md) - Detaylı deployment rehberi
- [DEPLOYMENT_QUICK_START.md](docs/DEPLOYMENT_QUICK_START.md) - Hızlı başlangıç

**Hızlı Komutlar:**
```bash
# Deployment script'i çalıştır
./deploy.sh

# Manuel deployment
docker-compose build
docker-compose up -d

# Logları görüntüle
docker-compose logs -f
```

**Production URL'leri:**
- API Gateway: `http://138.68.99.35:8080`
- Auth Service: `http://138.68.99.35:8081`
- Swagger UI: `http://138.68.99.35:8081/swagger-ui.html`

---

## 🔗 İlişkili Repository'ler

| Repository | Açıklama |
|------------|----------|
| [smart-campus-database](https://github.com/your-username/smart-campus-database) | Veritabanı şeması ve Docker setup |
| [smart-campus-frontend](https://github.com/your-username/smart-campus-frontend) | React frontend |

---

## 📄 Lisans

Bu proje **Recep Tayyip Erdoğan Üniversitesi Web ve Mobil Programlama Dersi** kapsamında eğitim amaçlı geliştirilmiştir.

---

<p align="center">
  <b>Smart Campus Backend</b> • Part 1 - Kimlik Doğrulama & Kullanıcı Yönetimi
</p>
