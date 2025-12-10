# 🔧 SendGrid 403 Forbidden Hatası Çözümü

## Hata Mesajı
```
403 Forbidden from POST https://api.sendgrid.com/v3/mail/send
```

## Nedenler

SendGrid 403 hatası genellikle şu nedenlerden kaynaklanır:

1. **"From" Email Adresi Doğrulanmamış**: SendGrid, gönderen email adresinin doğrulanmış olmasını gerektirir
2. **API Key Permission Sorunu**: API Key'in "Mail Send" permission'ı yok
3. **API Key Geçersiz**: API Key yanlış veya silinmiş

## Çözüm Adımları

### Adım 1: SendGrid "From" Email Adresini Doğrulayın

1. **SendGrid Dashboard'a gidin:**
   - https://app.sendgrid.com

2. **Settings > Sender Authentication bölümüne gidin:**
   - Sol menüden **Settings** > **Sender Authentication** seçin

3. **Single Sender Verification:**
   - "Verify a Single Sender" butonuna tıklayın
   - Email adresinizi girin: `noreply@smartcampus.edu.tr` (veya kullanmak istediğiniz email)
   - Formu doldurun ve "Create" butonuna tıklayın
   - SendGrid size bir doğrulama emaili gönderecek
   - Email'i kontrol edin ve linke tıklayın

4. **Domain Authentication (Önerilen - Production için):**
   - Eğer kendi domain'iniz varsa, domain authentication yapın
   - "Authenticate Your Domain" butonuna tıklayın
   - Domain'inizi girin (örn: `smartcampus.edu.tr`)
   - DNS kayıtlarını ekleyin (SendGrid size verecek)
   - DNS kayıtlarını domain'inize ekledikten sonra "Verify" butonuna tıklayın

### Adım 2: API Key Permission'ını Kontrol Edin

1. **Settings > API Keys bölümüne gidin:**
   - https://app.sendgrid.com/settings/api_keys

2. **API Key'inizi bulun ve "Edit" butonuna tıklayın**

3. **Permission'ları kontrol edin:**
   - "Mail Send" permission'ının aktif olduğundan emin olun
   - Veya "Full Access" seçin

4. **"Update" butonuna tıklayın**

### Adım 3: .env Dosyasını Güncelleyin

Doğrulanmış email adresini `.env` dosyasına ekleyin:

```bash
MAIL_FROM_EMAIL=noreply@smartcampus.edu.tr
```

**ÖNEMLİ:** Bu email adresi SendGrid'de doğrulanmış olmalı!

### Adım 4: Container'ı Yeniden Başlatın

```bash
docker-compose restart auth-service
```

### Adım 5: Test Edin

1. **Swagger UI'den test:**
   - http://138.68.99.35:8080/swagger-ui.html
   - `/api/auth/register` endpoint'ini kullanarak yeni kullanıcı kaydedin

2. **Logları kontrol edin:**
   ```bash
   docker logs smart_campus_auth --tail 50 | grep -i sendgrid
   ```

## Alternatif: Geçici Çözüm

Eğer hemen email doğrulaması yapamıyorsanız:

1. **SendGrid'de test email adresi kullanın:**
   - SendGrid, yeni hesaplarda test email adresi sağlar
   - Dashboard'da "Settings" > "Sender Authentication" bölümünde görebilirsiniz
   - Genellikle format: `noreply@your-sendgrid-domain.com`

2. **Bu test email adresini `.env` dosyasına ekleyin:**
   ```bash
   MAIL_FROM_EMAIL=noreply@your-sendgrid-domain.com
   ```

**Not:** Test email adresi sadece SendGrid dashboard'unda görünen email'lere gönderebilir. Production için mutlaka kendi domain'inizi doğrulamanız gerekir.

## Hata Mesajlarını Kontrol Etme

SendGrid API hata mesajları artık log'larda görünecek:

```bash
docker logs smart_campus_auth --tail 100 | grep -i "SendGrid API error"
```

Hata mesajı size tam olarak neyin yanlış olduğunu söyleyecektir.

## Yaygın Hata Mesajları

### "The from address does not match a verified Sender Identity"
- **Çözüm:** "From" email adresini SendGrid'de doğrulayın

### "Permission denied"
- **Çözüm:** API Key'in "Mail Send" permission'ı olduğundan emin olun

### "Invalid API Key"
- **Çözüm:** API Key'i kontrol edin, yeni bir tane oluşturun

---

**Son Güncelleme:** 10 Aralık 2025

