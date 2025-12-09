# 🎓 Smart Campus Database

Akıllı Kampüs Yönetim Sistemi'nin veritabanı katmanı. Bu repository, MySQL veritabanı şemasını, Docker yapılandırmasını ve test verilerini içerir.

---

## 📋 İçindekiler

- [Proje Hakkında](#-proje-hakkında)
- [Teknoloji Stack](#-teknoloji-stack)
- [Mimari](#-mimari)
- [Gereksinimler](#-gereksinimler)
- [Kurulum](#-kurulum)
- [Veritabanı Şeması](#-veritabanı-şeması)
- [Test Kullanıcıları](#-test-kullanıcıları)
- [Docker Komutları](#-docker-komutları)
- [Klasör Yapısı](#-klasör-yapısı)
- [İlişkili Repository'ler](#-ilişkili-repositoryler)
- [Dokümantasyon](#-dokümantasyon)

---

## 🎯 Proje Hakkında

Smart Campus, bir üniversite kampüsünün günlük operasyonlarını dijitalleştiren kapsamlı bir web uygulamasıdır. Bu repository, projenin veritabanı katmanını yönetir.

### Part 1 Kapsamı (Kimlik Doğrulama & Kullanıcı Yönetimi)

| Özellik | Açıklama |
|---------|----------|
| **Kullanıcı Kaydı** | Öğrenci, öğretim üyesi ve admin rolleri |
| **Kimlik Doğrulama** | JWT tabanlı authentication |
| **Email Doğrulama** | Token bazlı email verification |
| **Şifre Sıfırlama** | Güvenli password reset akışı |
| **Profil Yönetimi** | Kullanıcı bilgileri CRUD işlemleri |

---

## 🛠 Teknoloji Stack

| Teknoloji | Versiyon | Kullanım Amacı |
|-----------|----------|----------------|
| **MySQL** | 8.0 | İlişkisel veritabanı |
| **Docker** | Latest | Containerization |
| **Docker Compose** | Latest | Multi-container orchestration |
| **phpMyAdmin** | Latest | Veritabanı yönetim arayüzü |

---

## 🏗 Mimari

```
┌─────────────────────────────────────────────────────────────────┐
│                        Docker Host                               │
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

---

## 🔧 Gereksinimler

### Lokal Geliştirme
- Docker Desktop (Windows/Mac) veya Docker Engine (Linux)
- Docker Compose
- Git

### Production (DigitalOcean)
- Ubuntu 22.04 LTS
- Minimum 2GB RAM
- Docker & Docker Compose Plugin

---

## 🚀 Kurulum

### Lokal Geliştirme Ortamı

#### 1. Repository'yi Klonla

```bash
git clone https://github.com/your-username/smart-campus-database.git
cd smart-campus-database
```

#### 2. Environment Dosyası Oluştur

```bash
cd docker
cp .env.example .env
```

> `.env` dosyasını açarak veritabanı şifrelerini ayarlayın.

#### 3. Container'ları Başlat

```bash
docker-compose up -d
```

#### 4. Kurulumu Doğrula

```bash
# Container durumunu kontrol et
docker-compose ps

# MySQL loglarını kontrol et
docker-compose logs mysql
```

Başarılı kurulumda şu mesajları görmelisiniz:
```
Smart Campus Database initialized successfully!
Seed data inserted successfully!
Total Users: 8
Total Students: 5
Total Faculty: 2
Total Departments: 4
```

#### 5. phpMyAdmin'e Eriş

- **URL:** http://localhost:8081
- Veritabanı yapısını ve verileri görsel olarak inceleyebilirsiniz.

---

### Production Ortamı

Detaylı production kurulum talimatları için: [docs/DOCKER_SETUP.md](docs/DOCKER_SETUP.md)

---

## 🗄 Veritabanı Şeması

### Tablo Özeti

| # | Tablo | Açıklama | İlişkiler |
|---|-------|----------|-----------|
| 1 | `departments` | Akademik bölümler | - |
| 2 | `users` | Tüm kullanıcıların temel bilgileri | - |
| 3 | `students` | Öğrenci akademik bilgileri | users, departments |
| 4 | `faculty` | Öğretim üyesi bilgileri | users, departments |
| 5 | `refresh_tokens` | JWT refresh token'ları | users |
| 6 | `email_verification_tokens` | Email doğrulama token'ları | users |
| 7 | `password_reset_tokens` | Şifre sıfırlama token'ları | users |

### ER Diyagramı

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

### Detaylı Şema

Tablo yapıları, veri tipleri ve constraint'ler için: [docs/DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md)

---

## 👥 Test Kullanıcıları

Seed verileri otomatik olarak yüklenir. Test için kullanılabilecek hesaplar:

### Admin
| Email | Rol |
|-------|-----|
| admin@smartcampus.edu.tr | ADMIN |

### Öğretim Üyeleri
| Email | Bölüm | Unvan |
|-------|-------|-------|
| ahmet.yilmaz@smartcampus.edu.tr | Bilgisayar Müh. | Dr. Öğr. Üyesi |
| ayse.demir@smartcampus.edu.tr | Elektrik-Elektronik Müh. | Prof. Dr. |

### Öğrenciler
| Email | Bölüm | Öğrenci No |
|-------|-------|------------|
| ali.kaya@smartcampus.edu.tr | Bilgisayar Müh. | 20210001 |
| zeynep.celik@smartcampus.edu.tr | Bilgisayar Müh. | 20210002 |
| mehmet.ozturk@smartcampus.edu.tr | Elektrik-Elektronik Müh. | 20220001 |
| fatma.sahin@smartcampus.edu.tr | Makine Müh. | 20220002 |
| emre.arslan@smartcampus.edu.tr | İşletme | 20230001 |

### Bölümler
| Kod | Bölüm Adı | Fakülte |
|-----|-----------|---------|
| CENG | Bilgisayar Mühendisliği | Mühendislik Fakültesi |
| EEE | Elektrik-Elektronik Mühendisliği | Mühendislik Fakültesi |
| ME | Makine Mühendisliği | Mühendislik Fakültesi |
| BA | İşletme | İktisadi ve İdari Bilimler Fakültesi |

> **Not:** Tüm test kullanıcılarının şifreleri backend tarafında BCrypt ile hashlenmiş olarak saklanır.

---

## 🐳 Docker Komutları

### Container Yönetimi

```bash
# Tüm servisleri başlat
docker-compose up -d

# Tüm servisleri durdur
docker-compose down

# Servisleri yeniden başlat
docker-compose restart

# Servis durumlarını görüntüle
docker-compose ps

# Logları takip et
docker-compose logs -f

# Belirli servisin logları
docker-compose logs -f mysql
```

### Veritabanı İşlemleri

```bash
# MySQL shell'e bağlan
docker exec -it smart_campus_db mysql -u root -p

# Veritabanını yedekle
docker exec smart_campus_db mysqldump -u root -p smart_campus > backup_$(date +%Y%m%d).sql

# Yedeği geri yükle
docker exec -i smart_campus_db mysql -u root -p smart_campus < backup.sql

# Tablo listesini görüntüle
docker exec smart_campus_db mysql -u root -p -e "USE smart_campus; SHOW TABLES;"
```

### Veritabanını Sıfırlama

```bash
# Volume dahil tüm verileri sil ve yeniden oluştur
docker-compose down -v
docker-compose up -d
```

> ⚠️ Bu işlem tüm verileri siler ve seed data'yı yeniden yükler.

---

## 📁 Klasör Yapısı

```
smart-campus-database/
│
├── 📁 docker/
│   ├── docker-compose.yml      # Docker Compose yapılandırması
│   └── .env                    # Environment variables (git'e eklenmez)
│
├── 📁 migrations/              # Tablo oluşturma SQL dosyaları
│   ├── V1__create_departments.sql
│   ├── V2__create_users.sql
│   ├── V3__create_students.sql
│   ├── V4__create_faculty.sql
│   └── V5__create_auth_tables.sql
│
├── 📁 seeds/                   # Test verisi SQL dosyaları
│   ├── 01_departments.sql
│   ├── 02_users.sql
│   ├── 03_students.sql
│   └── 04_faculty.sql
│
├── 📁 scripts/                 # Birleştirilmiş SQL scriptleri
│   ├── init.sql                # Tüm tabloları oluşturur
│   └── seed.sql                # Tüm test verilerini yükler
│
├── 📁 docs/                    # Dokümantasyon
│   ├── DATABASE_SCHEMA.md      # Veritabanı şema detayları
│   ├── DOCKER_SETUP.md         # Docker kurulum rehberi
│   └── FINAL_PROJECT_ASSIGNMENT.md
│
├── .gitignore                  # Git ignore kuralları
├── .env.example                # Örnek environment dosyası
└── README.md                   # Bu dosya
```

---

## 🔗 İlişkili Repository'ler

| Repository | Açıklama | Teknoloji |
|------------|----------|-----------|
| [smart-campus-backend](https://github.com/your-username/smart-campus-backend) | REST API | Spring Boot |
| [smart-campus-frontend](https://github.com/your-username/smart-campus-frontend) | Web Arayüzü | React |

---

## 📚 Dokümantasyon

| Doküman | Açıklama |
|---------|----------|
| [DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md) | Tablo yapıları, ilişkiler, veri tipleri |
| [DOCKER_SETUP.md](docs/DOCKER_SETUP.md) | Docker kurulum ve yapılandırma rehberi |
| [FINAL_PROJECT_ASSIGNMENT.md](docs/FINAL_PROJECT_ASSIGNMENT.md) | Proje gereksinimleri |

---

## 🔒 Güvenlik Notları

- `.env` dosyası Git'e **asla** eklenmemelidir
- Production ortamında güçlü şifreler kullanın
- MySQL ve phpMyAdmin portları production'da firewall ile kapatılmalıdır
- Düzenli veritabanı yedekleri alın

---

## 📄 Lisans

Bu proje **Recep Tayyip Erdoğan Üniversitesi Web ve Mobil Programlama Dersi** kapsamında eğitim amaçlı geliştirilmiştir.

---

<p align="center">
  <b>Smart Campus Database</b> • Part 1 - Kimlik Doğrulama & Kullanıcı Yönetimi
</p>
