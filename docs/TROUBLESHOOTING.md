# 🔧 Sorun Giderme Rehberi

## Gmail SMTP Bağlantı Hatası

### Hata Mesajı
```
org.eclipse.angus.mail.util.MailConnectException: Couldn't connect to host, port: smtp.gmail.com, 587; timeout 5000
Caused by: java.net.SocketTimeoutException: Connect timed out
```

### Nedenler

1. **Firewall Kısıtlaması**: DigitalOcean sunucusundan Gmail SMTP portuna (587) çıkış yapılamıyor
2. **Network Timeout**: Container'dan dışarıya çıkış yapılamıyor
3. **Mail Health Check**: Spring Boot Actuator'un mail health check'i sürekli çalışıyor

### Çözümler

#### 1. Mail Health Check'i Devre Dışı Bırak (Hızlı Çözüm)

`application.properties` dosyasında:
```properties
management.health.mail.enabled=false
```

Bu değişiklik yapıldı. Container'ı yeniden başlatın:
```bash
docker-compose restart auth-service
```

#### 2. Firewall Kontrolü

Sunucudan Gmail SMTP'ye erişimi test edin:
```bash
# Container içinden test
docker exec -it smart_campus_auth telnet smtp.gmail.com 587

# Sunucudan direkt test
telnet smtp.gmail.com 587
```

Eğer bağlantı kurulamıyorsa, firewall'u kontrol edin:
```bash
# UFW durumunu kontrol et
sudo ufw status

# Gerekirse 587 portunu aç (ama bu genellikle gerekmez, çıkış portu)
# UFW genellikle çıkış trafiğini engellemez
```

#### 3. Network Bağlantısını Kontrol Et

```bash
# DNS çözümlemesi
docker exec -it smart_campus_auth nslookup smtp.gmail.com

# Ping testi
docker exec -it smart_campus_auth ping -c 3 smtp.gmail.com
```

#### 4. SMTP Timeout Değerlerini Artır

`application.properties` dosyasında timeout değerleri artırıldı:
```properties
spring.mail.properties.mail.smtp.connectiontimeout=30000
spring.mail.properties.mail.smtp.timeout=30000
spring.mail.properties.mail.smtp.writetimeout=30000
```

#### 5. DigitalOcean Firewall Kontrolü

DigitalOcean Dashboard'dan:
1. **Networking** > **Firewalls** bölümüne gidin
2. Sunucunuza bağlı firewall'u kontrol edin
3. **Outbound Rules** bölümünde SMTP portlarının açık olduğundan emin olun:
   - Port 587 (SMTP)
   - Port 465 (SMTP SSL)

### Notlar

- **Mail Health Check**: Production'da mail health check genellikle devre dışı bırakılır çünkü:
  - Network gecikmeleri olabilir
  - Firewall kısıtlamaları olabilir
  - Gereksiz log spam'i oluşturur

- **Email Fonksiyonelliği**: Mail health check devre dışı olsa bile, email gönderme fonksiyonelliği çalışmaya devam eder. Sadece health check endpoint'i mail durumunu kontrol etmez.

- **Alternatif Çözüm**: Eğer Gmail SMTP sürekli sorun çıkarıyorsa, SendGrid, Mailgun gibi alternatif email servisleri kullanılabilir.

---

## Container Başlatma Sorunları

### Container Sürekli Restart Oluyor

```bash
# Logları kontrol et
docker logs smart_campus_auth

# Container durumunu kontrol et
docker ps -a

# Health check sonuçlarını kontrol et
docker inspect smart_campus_auth | grep -A 10 Health
```

### Database Bağlantı Hatası

```bash
# Database'in erişilebilir olduğunu kontrol et
docker exec -it smart_campus_auth telnet 138.68.99.35 3306

# .env dosyasındaki database ayarlarını kontrol et
cat .env | grep DB_
```

---

## API Gateway Sorunları

### Gateway Servislere Erişemiyor

```bash
# Gateway loglarını kontrol et
docker logs smart_campus_gateway

# Network'ü kontrol et
docker network inspect smart_campus_network

# Gateway'den auth-service'e erişimi test et
docker exec -it smart_campus_gateway wget -O- http://auth-service:8081/actuator/health
```

---

## Performans Sorunları

### Yavaş Response Süreleri

```bash
# Container resource kullanımını kontrol et
docker stats

# Disk kullanımını kontrol et
df -h

# Memory kullanımını kontrol et
free -h
```

---

## Log Analizi

### Önemli Log Dosyaları

```bash
# Auth Service logları
docker logs smart_campus_auth --tail 100

# API Gateway logları
docker logs smart_campus_gateway --tail 100

# Tüm container logları
docker-compose logs --tail 100
```

---

**Son Güncelleme:** 9 Aralık 2025

