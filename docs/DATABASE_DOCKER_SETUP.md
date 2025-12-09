# Docker Kurulum ve Yapılandırma Rehberi

## 1. Genel Bakış

Bu doküman, Smart Campus Database projesinin Docker ile nasıl yapılandırıldığını ve deploy edildiğini açıklar.

| Özellik | Değer |
|---------|-------|
| **Container Orchestration** | Docker Compose |
| **MySQL Version** | 8.0 |
| **phpMyAdmin** | Latest |
| **Network** | Bridge (smart_campus_network) |

---

## 2. Servis Mimarisi

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Host                          │
│  ┌─────────────────────────────────────────────────┐   │
│  │            smart_campus_network                  │   │
│  │                                                  │   │
│  │   ┌─────────────────┐    ┌─────────────────┐   │   │
│  │   │  smart_campus_db │    │ smart_campus_   │   │   │
│  │   │     (MySQL)      │◄───│   phpmyadmin    │   │   │
│  │   │   Port: 3306     │    │   Port: 8081    │   │   │
│  │   └─────────────────┘    └─────────────────┘   │   │
│  │           │                                      │   │
│  │           ▼                                      │   │
│  │   ┌─────────────────┐                           │   │
│  │   │   mysql_data    │                           │   │
│  │   │    (Volume)     │                           │   │
│  │   └─────────────────┘                           │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## 3. Docker Compose Yapılandırması

### 3.1 Servisler

#### MySQL Servisi
- **Image:** mysql:8.0
- **Container Name:** smart_campus_db
- **Port:** 3306
- **Volume:** mysql_data (persistent storage)
- **Healthcheck:** mysqladmin ping ile container sağlık kontrolü
- **Init Scripts:**
  - `00_init.sql` → Tablo yapılarını oluşturur
  - `01_seed.sql` → Test verilerini yükler

#### phpMyAdmin Servisi
- **Image:** phpmyadmin:latest
- **Container Name:** smart_campus_phpmyadmin
- **Port:** 8081
- **Depends On:** mysql (healthy durumda olmalı)

### 3.2 Network
- **Name:** smart_campus_network
- **Driver:** bridge
- Container'lar arası iletişim bu network üzerinden sağlanır

### 3.3 Volume
- **Name:** mysql_data
- **Driver:** local
- MySQL verileri bu volume'da kalıcı olarak saklanır

---

## 4. Dosya Yapısı

```
smart-campus-database/
├── docker/
│   ├── docker-compose.yml    # Docker Compose yapılandırması
│   └── .env                  # Environment variables (git'e eklenmez)
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
│   ├── init.sql              # Başlangıç script'i (tüm tablolar)
│   └── seed.sql              # Test verileri script'i
└── docs/
    └── ...
```

---

## 5. Kurulum Adımları

### 5.1 Lokal Geliştirme Ortamı

#### Gereksinimler
- Docker Desktop
- Git

#### Adımlar

1. **Repository'yi klonla**
   ```bash
   git clone <repository-url>
   cd smart-campus-database
   ```

2. **Environment dosyası oluştur**
   ```bash
   cd docker
   cp .env.example .env
   # .env dosyasını düzenle ve şifreleri ayarla
   ```

3. **Container'ları başlat**
   ```bash
   docker-compose up -d
   ```

4. **Durumu kontrol et**
   ```bash
   docker-compose ps
   docker-compose logs -f mysql
   ```

5. **phpMyAdmin'e eriş**
   - URL: http://localhost:8081
   - Kullanıcı: root
   - Şifre: .env dosyasındaki değer

### 5.2 Production Ortamı (DigitalOcean)

#### Sunucu Gereksinimleri
- Ubuntu 22.04 LTS
- Minimum 2GB RAM
- Docker & Docker Compose

#### Kurulum Adımları

1. **Sunucuya bağlan**
   ```bash
   ssh root@<sunucu-ip>
   ```

2. **Sistem güncelle ve Docker kur**
   ```bash
   apt update && apt upgrade -y
   curl -fsSL https://get.docker.com | sh
   apt install docker-compose-plugin -y
   ```

3. **Proje klasörü oluştur ve klonla**
   ```bash
   mkdir -p /opt/smart-campus
   cd /opt/smart-campus
   git clone <repository-url>
   cd smart-campus-database/docker
   ```

4. **Production .env dosyası oluştur**
   ```bash
   nano .env
   # Güçlü şifreler kullan!
   ```

5. **Container'ları başlat**
   ```bash
   docker compose up -d
   ```

6. **Firewall yapılandır**
   ```bash
   ufw allow 22      # SSH
   ufw allow 80      # HTTP
   ufw allow 443     # HTTPS
   ufw deny 3306     # MySQL (dışarıdan erişim kapalı)
   ufw deny 8081     # phpMyAdmin (dışarıdan erişim kapalı)
   ufw enable
   ```

---

## 6. Yönetim Komutları

### Container Yönetimi

```bash
# Container'ları başlat
docker compose up -d

# Container'ları durdur
docker compose down

# Container'ları sil (volume dahil)
docker compose down -v

# Durumu görüntüle
docker compose ps

# Logları görüntüle
docker compose logs -f mysql
docker compose logs -f phpmyadmin
```

### MySQL İşlemleri

```bash
# MySQL'e bağlan
docker exec -it smart_campus_db mysql -u root -p

# SQL dosyası çalıştır
docker exec -i smart_campus_db mysql -u root -p < dosya.sql

# Veritabanını yedekle
docker exec smart_campus_db mysqldump -u root -p smart_campus > backup.sql

# Yedeği geri yükle
docker exec -i smart_campus_db mysql -u root -p smart_campus < backup.sql
```

### Veritabanını Sıfırlama

```bash
# Volume'u silerek tamamen sıfırla
docker compose down -v
docker compose up -d

# Bu işlem:
# 1. Tüm verileri siler
# 2. init.sql ile tabloları yeniden oluşturur
# 3. seed.sql ile test verilerini yükler
```

---

## 7. Güvenlik Notları

### Production Ortamı İçin

| Kural | Açıklama |
|-------|----------|
| **Güçlü Şifreler** | En az 16 karakter, büyük/küçük harf, rakam, özel karakter |
| **Firewall** | MySQL (3306) ve phpMyAdmin (8081) portları dışarıya kapalı olmalı |
| **.env Koruması** | .env dosyası asla Git'e eklenmemeli |
| **SSH Key** | Sunucuya şifre yerine SSH key ile bağlanın |
| **Düzenli Yedekleme** | Veritabanını düzenli olarak yedekleyin |

### Port Güvenliği

```
✅ Port 22   → SSH (açık)
✅ Port 80   → HTTP/Frontend (açık)
✅ Port 443  → HTTPS (açık)
🔒 Port 3306 → MySQL (kapalı - sadece Docker internal)
🔒 Port 8081 → phpMyAdmin (kapalı - sadece Docker internal)
```

---

## 8. Backend Entegrasyonu

Backend (Spring Boot) aynı sunucuda Docker network üzerinden MySQL'e bağlanır:

```
┌─────────────────────────────────────────────────────────┐
│                   Docker Network                         │
│                                                          │
│   ┌──────────────┐         ┌──────────────────┐        │
│   │   Backend    │  ────►  │  smart_campus_db │        │
│   │ Spring Boot  │         │      MySQL       │        │
│   └──────────────┘         └──────────────────┘        │
│                                                          │
│   Bağlantı: jdbc:mysql://smart_campus_db:3306/smart_campus
└─────────────────────────────────────────────────────────┘
```

> **Not:** `smart_campus_db` container ismi, aynı Docker network'te hostname olarak kullanılır.

---

## 9. Troubleshooting

### Container Başlamıyor

```bash
# Logları kontrol et
docker compose logs mysql

# Container durumunu kontrol et
docker compose ps

# Yeniden başlat
docker compose restart mysql
```

### MySQL Bağlantı Hatası

```bash
# Container'ın healthy olduğunu kontrol et
docker inspect smart_campus_db | grep -A 5 "Health"

# MySQL'in dinlediğini kontrol et
docker exec smart_campus_db mysqladmin -u root -p status
```

### Volume/Data Sorunları

```bash
# Volume'u kontrol et
docker volume ls
docker volume inspect smart-campus-database_mysql_data

# Tamamen sıfırla (DİKKAT: Tüm veriler silinir!)
docker compose down -v
docker compose up -d
```

---

## 10. Versiyon Geçmişi

| Tarih | Versiyon | Değişiklik |
|-------|----------|------------|
| 2024-12-08 | 1.0.0 | İlk kurulum - Part 1 tabloları |

---

## 11. İlgili Dokümanlar

- [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) - Veritabanı şeması ve tablo yapıları
- [README.md](../README.md) - Proje genel bilgileri

