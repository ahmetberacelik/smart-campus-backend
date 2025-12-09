# 🚀 Deployment Hızlı Başlangıç

## Ubuntu VM (138.68.99.35) Deployment

### 1. Sunucuya Bağlan

```bash
ssh root@138.68.99.35
# veya
ssh your-user@138.68.99.35
```

### 2. Gereksinimleri Kontrol Et

```bash
# Docker kontrolü
docker --version
docker-compose --version

# Eğer yoksa kur
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 3. Projeyi Klonla

```bash
cd /opt
sudo git clone https://github.com/your-username/smart-campus-backend.git
cd smart-campus-backend
```

### 4. .env Dosyası Oluştur

```bash
# .env.example'dan kopyala
cp .env.example .env

# Düzenle
nano .env
```

**Önemli .env Ayarları:**

```env
# Database (Mevcut database)
DB_HOST=138.68.99.35
DB_PORT=3306
DB_NAME=smart_campus
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# Service Discovery (Docker network içinde)
AUTH_SERVICE_HOST=auth-service
AUTH_SERVICE_PORT=8081

# Frontend
FRONTEND_URL=http://138.68.99.35:3000
CORS_ALLOWED_ORIGINS=http://138.68.99.35:3000

# JWT (Güçlü bir key!)
JWT_SECRET=your-very-strong-secret-key-minimum-32-characters

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# DigitalOcean Spaces
DO_SPACES_KEY=your-key
DO_SPACES_SECRET=your-secret
DO_SPACES_ENDPOINT=fra1.digitaloceanspaces.com
DO_SPACES_BUCKET=smart-campus
DO_SPACES_REGION=fra1
```

### 5. Firewall Ayarları

```bash
sudo ufw allow 8080/tcp  # API Gateway
sudo ufw allow 8081/tcp  # Auth Service
sudo ufw allow 22/tcp    # SSH
sudo ufw enable
```

### 6. Deployment

```bash
# Script ile
chmod +x deploy.sh
./deploy.sh

# Veya manuel
docker-compose build
docker-compose up -d
```

### 7. Kontrol

```bash
# Health check
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health

# Loglar
docker-compose logs -f
```

### 8. Erişim

- **API Gateway:** http://138.68.99.35:8080
- **Auth Service:** http://138.68.99.35:8081
- **Swagger UI:** http://138.68.99.35:8081/swagger-ui.html

---

## 🔄 Güncelleme

```bash
cd /opt/smart-campus-backend
git pull
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

---

## 🛑 Durdurma

```bash
docker-compose down
```

---

Detaylı dokümantasyon için: [DEPLOYMENT.md](DEPLOYMENT.md)

