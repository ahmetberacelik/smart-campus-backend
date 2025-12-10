# 📧 SendGrid Email Servisi Kurulum Rehberi

## Neden SendGrid?

Gmail SMTP'ye DigitalOcean'dan erişim engellenmiş durumda. SendGrid, production ortamlarında yaygın kullanılan, güvenilir bir email servisidir ve DigitalOcean'dan erişilebilir.

## Adım 1: SendGrid Hesabı Oluşturma

1. **SendGrid Web Sitesine Gidin:**
   - https://sendgrid.com
   - "Start for Free" butonuna tıklayın

2. **Hesap Oluşturun:**
   - Email adresinizi girin
   - Şifre oluşturun
   - Hesap bilgilerinizi doldurun

3. **Email Doğrulama:**
   - Gelen email'i kontrol edin
   - Email'i doğrulayın

## Adım 2: API Key Oluşturma

1. **SendGrid Dashboard'a Giriş Yapın**

2. **API Keys Bölümüne Gidin:**
   - Sol menüden **Settings** > **API Keys** seçin
   - Veya direkt: https://app.sendgrid.com/settings/api_keys

3. **Yeni API Key Oluşturun:**
   - "Create API Key" butonuna tıklayın
   - **API Key Name:** `smart-campus-smtp`
   - **API Key Permissions:** 
     - "Full Access" seçin (veya sadece "Mail Send" yeterli)
   - "Create & View" butonuna tıklayın

4. **API Key'i Kopyalayın:**
   - ⚠️ **ÖNEMLİ:** API Key sadece bir kez gösterilir!
   - API Key'i güvenli bir yere kaydedin
   - Format: `SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

## Adım 3: Backend Konfigürasyonu

### 3.1 `.env` Dosyasını Güncelleyin

Production sunucuda `.env` dosyasını düzenleyin:

```bash
# Email Configuration (SendGrid SMTP)
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=<SENDGRID_API_KEY_BURAYA>
```

**Örnek:**
```bash
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=SG.abc123xyz789...
```

### 3.2 `application.properties` Dosyasını Güncelleyin

`auth-service/src/main/resources/application.properties` dosyasında email konfigürasyonunu güncelleyin:

```properties
# -----------------------------------------------------
# Email Configuration (SendGrid SMTP)
# -----------------------------------------------------
spring.mail.host=${MAIL_HOST}
spring.mail.port=${MAIL_PORT}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
# SendGrid Port 587 için STARTTLS kullanılır
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.ssl.enable=false
spring.mail.properties.mail.transport.protocol=smtp
# Timeout değerleri
spring.mail.properties.mail.smtp.connectiontimeout=30000
spring.mail.properties.mail.smtp.timeout=30000
spring.mail.properties.mail.smtp.writetimeout=30000
```

### 3.3 Kodları GitHub'a Push Edin

```bash
# Local'de
git add auth-service/src/main/resources/application.properties
git commit -m "Update email configuration for SendGrid"
git push
```

### 3.4 Production'da Güncelleyin

```bash
# Production sunucuda
cd /opt/smart-campus/smart-campus-backend
git pull

# .env dosyasını düzenleyin (yukarıdaki SendGrid ayarlarıyla)
nano .env

# Container'ı yeniden build ve başlat
docker-compose build --no-cache auth-service
docker-compose up -d
```

## Adım 4: Test Etme

### 4.1 Network Bağlantısını Test Edin

```bash
# SendGrid SMTP'ye erişimi test et
nc -zv smtp.sendgrid.net 587

# Container'dan test
docker exec -it smart_campus_auth nc -zv smtp.sendgrid.net 587
```

**Beklenen Sonuç:**
```
smtp.sendgrid.net [IP] 587 (smtp) open
```

### 4.2 Email Gönderimini Test Edin

1. **Swagger UI'den Test:**
   - http://138.68.99.35:8080/swagger-ui.html
   - `/api/auth/register` endpoint'ini kullanarak yeni kullanıcı kaydedin
   - Email'in gelip gelmediğini kontrol edin

2. **Logları Kontrol Edin:**
   ```bash
   docker logs smart_campus_auth --tail 50
   ```

**Başarılı Email Gönderimi:**
```
INFO  c.s.a.service.impl.EmailServiceImpl - Verification email sent to: user@example.com
```

**Başarısız Email Gönderimi:**
```
ERROR o.s.a.i.SimpleAsyncUncaughtExceptionHandler - Unexpected exception occurred
```

## SendGrid Ücretsiz Plan Limitleri

- **100 email/gün** (yaklaşık 3,000 email/ay)
- **SMTP API** erişimi
- **Email tracking** (açılma, tıklama istatistikleri)
- **Webhook desteği**

## SendGrid Dashboard Özellikleri

- **Activity Feed:** Gönderilen email'lerin durumunu görüntüleyin
- **Stats:** Email istatistiklerini görüntüleyin
- **Suppressions:** Bounce ve spam şikayetlerini yönetin

## Sorun Giderme

### Email Gönderilemiyor

1. **API Key'i Kontrol Edin:**
   - SendGrid Dashboard > Settings > API Keys
   - API Key'in aktif olduğundan emin olun
   - "Full Access" veya "Mail Send" permission'ı olduğundan emin olun

2. **Network Bağlantısını Kontrol Edin:**
   ```bash
   nc -zv smtp.sendgrid.net 587
   ```

3. **Logları Kontrol Edin:**
   ```bash
   docker logs smart_campus_auth --tail 100 | grep -i mail
   ```

### Rate Limit Hatası

SendGrid ücretsiz planında günlük 100 email limiti vardır. Eğer limit aşılırsa:
- Ertesi gün bekleyin
- Veya ücretli plana geçin

### Email Spam Klasörüne Düşüyor

1. **Sender Authentication:**
   - SendGrid Dashboard > Settings > Sender Authentication
   - Domain veya Single Sender Verification yapın

2. **SPF/DKIM Kayıtları:**
   - Domain'inize SPF ve DKIM kayıtları ekleyin
   - Bu, email deliverability'yi artırır

## Gmail SMTP'ye Geri Dönmek İsterseniz

Eğer ileride Gmail SMTP'ye erişim sağlanırsa, `.env` dosyasını tekrar güncelleyin:

```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

Ve `application.properties` dosyasını Gmail için uygun şekilde güncelleyin.

---

**Son Güncelleme:** 10 Aralık 2025

