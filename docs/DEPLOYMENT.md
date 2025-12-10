# Smart Campus Backend - Deployment Dokümantasyonu

## 🚀 Production Deployment

Bu dokümantasyon, Smart Campus Backend'in Ubuntu VM (138.68.99.35) üzerine deployment'ı için hazırlanmıştır.

---

## 📋 Ön Gereksinimler

### 1. Sunucu Gereksinimleri

- **OS:** Ubuntu 20.04+ veya 22.04+
- **RAM:** Minimum 2GB (Önerilen: 4GB+)
- **Disk:** Minimum 20GB boş alan
- **Network:** 138.68.99.35 IP adresine erişim

### 2. Yazılım Gereksinimleri

```bash
# Docker kurulumu
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Docker Compose kurulumu
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Git kurulumu (eğer yoksa)
sudo apt update
sudo apt install -y git
```

### 3. Firewall Ayarları

```bash
# Gerekli portları aç
sudo ufw allow 8080/tcp  # API Gateway
sudo ufw allow 8081/tcp  # Auth Service
sudo ufw allow 22/tcp    # SSH
sudo ufw enable
```

---

## 🔧 Deployment Adımları

### 1. Repository'yi Klonla

```bash
cd /opt
sudo git clone https://github.com/your-username/smart-campus-backend.git
cd smart-campus-backend
```

### 2. Environment Dosyası Oluştur

```bash
# .env.example'dan .env oluştur
cp .env.example .env

# .env dosyasını düzenle
nano .env
```

**Önemli .env Ayarları:**

```env
# Database (Mevcut database'e bağlanacak)
DB_HOST=138.68.99.35
DB_PORT=3306
DB_NAME=smart_campus
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# Service Discovery (Docker network içinde)
AUTH_SERVICE_HOST=auth-service
AUTH_SERVICE_PORT=8081

# Frontend URL (Production)
FRONTEND_URL=http://138.68.99.35:3000

# CORS (Production için spesifik origin'ler)
CORS_ALLOWED_ORIGINS=http://138.68.99.35:3000,http://localhost:3000

# JWT Secret (Güçlü bir secret key kullanın!)
JWT_SECRET=your-very-strong-secret-key-here

# Email (Gmail SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# DigitalOcean Spaces
DO_SPACES_KEY=your-spaces-key
DO_SPACES_SECRET=your-spaces-secret
DO_SPACES_ENDPOINT=fra1.digitaloceanspaces.com
DO_SPACES_BUCKET=smart-campus
DO_SPACES_REGION=fra1
```

### 3. Deployment Script'ini Çalıştır

```bash
# Script'e execute yetkisi ver
chmod +x deploy.sh

# Deployment'ı başlat
./deploy.sh
```

**Manuel Deployment (Script kullanmadan):**

```bash
# Container'ları build et
docker-compose build

# Container'ları başlat
docker-compose up -d

# Logları kontrol et
docker-compose logs -f
```

### 4. Health Check

```bash
# API Gateway health check
curl http://localhost:8080/actuator/health

# Auth Service health check
curl http://localhost:8081/actuator/health
```

---

## 🌐 Erişim URL'leri

### Production Endpoints

| Servis | URL | Açıklama |
|--------|-----|----------|
| **API Gateway** | `http://138.68.99.35:8080` | Tüm API'ler buradan erişilebilir |
| **Auth Service** | `http://138.68.99.35:8081` | Direkt servis erişimi |
| **Swagger UI** | `http://138.68.99.35:8081/swagger-ui.html` | API Dokümantasyonu |

### API Endpoints

Tüm API'ler **8080 portu** üzerinden erişilebilir:

```
http://138.68.99.35:8080/api/v1/auth/register
http://138.68.99.35:8080/api/v1/auth/login
http://138.68.99.35:8080/api/v1/users/me
http://138.68.99.35:8080/api/v1/departments
```

---

## 🔍 Monitoring ve Loglar

### Container Logları

```bash
# Tüm loglar
docker-compose logs -f

# Sadece API Gateway
docker-compose logs -f api-gateway

# Sadece Auth Service
docker-compose logs -f auth-service
```

### Container Durumu

```bash
# Container'ların durumunu kontrol et
docker-compose ps

# Container istatistikleri
docker stats
```

### Health Check

```bash
# API Gateway health
curl http://138.68.99.35:8080/actuator/health

# Auth Service health
curl http://138.68.99.35:8081/actuator/health
```

---

## 🔄 Güncelleme (Update)

### Kod Güncellemesi

```bash
# Yeni kodu çek
git pull origin main

# Container'ları yeniden build et ve başlat
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### Sadece Restart

```bash
# Container'ları yeniden başlat
docker-compose restart
```

---

## 🛑 Durdurma

```bash
# Container'ları durdur (veriler korunur)
docker-compose stop

# Container'ları durdur ve sil
docker-compose down

# Container'ları durdur, sil ve image'ları temizle
docker-compose down --rmi all
```

---

## 🐛 Sorun Giderme

### Container'lar Başlamıyor

```bash
# Logları kontrol et
docker-compose logs

# Container'ların durumunu kontrol et
docker-compose ps

# Network'ü kontrol et
docker network ls
docker network inspect smart_campus_network
```

### Database Bağlantı Hatası

```bash
# .env dosyasındaki database ayarlarını kontrol et
cat .env | grep DB_

# Database'in erişilebilir olduğunu kontrol et
telnet 138.68.99.35 3306
```

### Port Çakışması

```bash
# Port kullanımını kontrol et
sudo netstat -tulpn | grep 8080
sudo netstat -tulpn | grep 8081

# Eğer port kullanılıyorsa, docker-compose.yml'de port değiştir
```

### CORS Hatası

```bash
# .env dosyasındaki CORS_ALLOWED_ORIGINS'i kontrol et
# Frontend URL'ini eklediğinizden emin olun
```

---

## 🔐 Güvenlik Önerileri

### 1. Firewall

```bash
# Sadece gerekli portları aç
sudo ufw allow 8080/tcp
sudo ufw allow 8081/tcp
sudo ufw enable
```

### 2. SSL/TLS (Nginx Reverse Proxy)

Production'da Nginx reverse proxy ile SSL sertifikası kullanılması önerilir:

```nginx
server {
    listen 80;
    server_name api.smartcampus.edu.tr;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 3. Environment Variables

- `.env` dosyasını asla Git'e commit etmeyin
- Production'da güçlü secret key'ler kullanın
- Database şifrelerini güvenli tutun

---

## 📊 Performans İzleme

### Resource Kullanımı

```bash
# Container resource kullanımı
docker stats

# Disk kullanımı
df -h

# Memory kullanımı
free -h
```

### Log Rotation

Docker log'larının büyümesini önlemek için:

```yaml
# docker-compose.yml'e ekle
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

---

## 📞 Destek

Sorun yaşarsanız:

1. Logları kontrol edin: `docker-compose logs -f`
2. Health check yapın: `curl http://138.68.99.35:8080/actuator/health`
3. Container durumunu kontrol edin: `docker-compose ps`

---

**Son Güncelleme:** 9 Aralık 2025  
**Deployment IP:** 138.68.99.35



