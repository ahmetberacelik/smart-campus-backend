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

### 1. Repository'yi Klonla

```bash
git clone https://github.com/your-username/smart-campus-backend.git
cd smart-campus-backend
```

### 2. Environment Dosyası Oluştur

```bash
cp .env.example .env
# .env dosyasını düzenle ve gerekli değerleri gir
```

### 3. Docker Network Oluştur (İlk kurulumda)

```bash
docker network create smart_campus_network
```

### 4. Servisleri Başlat

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

Swagger UI üzerinden API dokümantasyonuna erişebilirsiniz:

| Servis | URL |
|--------|-----|
| Auth Service | http://localhost:8081/swagger-ui.html |

### Temel Endpoints

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | `/api/v1/auth/register` | Kullanıcı kaydı |
| POST | `/api/v1/auth/login` | Giriş |
| POST | `/api/v1/auth/refresh` | Token yenileme |
| POST | `/api/v1/auth/logout` | Çıkış |
| GET | `/api/v1/users/me` | Profil görüntüleme |
| PUT | `/api/v1/users/me` | Profil güncelleme |

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

| Değişken | Açıklama | Örnek |
|----------|----------|-------|
| `DB_HOST` | MySQL host | smart_campus_db |
| `DB_PORT` | MySQL port | 3306 |
| `DB_NAME` | Database adı | smart_campus |
| `DB_USERNAME` | Database kullanıcı | root |
| `DB_PASSWORD` | Database şifre | *** |
| `JWT_SECRET` | JWT secret key | *** |
| `MAIL_USERNAME` | Gmail adresi | ***@gmail.com |
| `MAIL_PASSWORD` | Gmail App Password | *** |
| `DO_SPACES_KEY` | DO Spaces key | *** |
| `DO_SPACES_SECRET` | DO Spaces secret | *** |

Tüm değişkenler için `.env.example` dosyasına bakın.

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
