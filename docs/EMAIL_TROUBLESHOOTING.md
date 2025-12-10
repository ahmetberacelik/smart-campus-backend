# 📧 Email Sorun Giderme Rehberi

## Gmail SMTP Bağlantı Sorunu

### Sorun
Container Gmail SMTP sunucusuna (`smtp.gmail.com`) bağlanamıyor. Hem Port 587 hem de Port 465'te `Connect timed out` hatası alınıyor.

### Bu Ne Anlama Geliyor?
Bu, **kesinlikle bir firewall/network kısıtlaması** sorunudur. Container'dan dışarıya SMTP trafiği çıkamıyor.

---

## 🔍 Adım 1: Network Bağlantısını Test Edin

Production sunucuda şu komutları çalıştırın:

```bash
# Container'dan Gmail SMTP'ye erişimi test et (Port 587)
docker exec -it smart_campus_auth nc -zv smtp.gmail.com 587

# Container'dan Gmail SMTP'ye erişimi test et (Port 465)
docker exec -it smart_campus_auth nc -zv smtp.gmail.com 465

# Eğer nc yoksa, telnet ile test et
docker exec -it smart_campus_auth telnet smtp.gmail.com 587
docker exec -it smart_campus_auth telnet smtp.gmail.com 465

# Sunucudan direkt test (container dışından)
nc -zv smtp.gmail.com 587
nc -zv smtp.gmail.com 465
```

### Beklenen Sonuçlar

**✅ Bağlantı Başarılı:**
```
smtp.gmail.com [IP] 587 (smtp) open
```

**❌ Bağlantı Başarısız:**
```
nc: connect to smtp.gmail.com port 587 (tcp) failed: Connection timed out
```

Eğer bağlantı başarısızsa → **Firewall sorunu var**

---

## 🔥 Adım 2: Firewall Kontrolü

### 2.1 UFW (Ubuntu Firewall) Kontrolü

```bash
# UFW durumunu kontrol et
sudo ufw status verbose

# Eğer UFW aktifse ve çıkış trafiğini engelliyorsa:
# UFW genellikle çıkış trafiğini engellemez, ama kontrol edin
sudo ufw status numbered

# Gerekirse çıkış trafiğine izin ver (genellikle zaten açıktır)
sudo ufw default allow outgoing

# SMTP portlarını açıkça izin ver (outgoing)
sudo ufw allow out 587/tcp
sudo ufw allow out 465/tcp

# Kuralları kontrol et
sudo ufw status numbered
```

### 2.2 DigitalOcean Firewall Kontrolü

1. **DigitalOcean Dashboard**'a gidin
2. **Networking** > **Firewalls** bölümüne gidin
3. Sunucunuza bağlı firewall'u bulun
4. **Outbound Rules** sekmesine gidin
5. Şu portların açık olduğundan emin olun:
   - **Port 587** (SMTP STARTTLS)
   - **Port 465** (SMTP SSL)
   - **Port 25** (SMTP - genellikle engellenir)

**Outbound Rule Ekleme:**
- **Type:** Custom
- **Protocol:** TCP
- **Port Range:** 587, 465
- **Destination:** All IPv4, All IPv6

### 2.3 iptables Kontrolü

```bash
# iptables kurallarını kontrol et
sudo iptables -L -n -v

# OUTPUT chain'ini kontrol et
sudo iptables -L OUTPUT -n -v

# Eğer SMTP portları engellenmişse, kural ekle:
sudo iptables -A OUTPUT -p tcp --dport 587 -j ACCEPT
sudo iptables -A OUTPUT -p tcp --dport 465 -j ACCEPT
```

---

## 🔧 Adım 3: Alternatif Çözümler

### Çözüm 1: Email Servisini Geçici Devre Dışı Bırakma

Email gönderme başarısız olsa bile uygulama çalışmaya devam eder. Kullanıcılar kayıt olabilir, sadece email doğrulama linki gönderilemez.

**Manuel Email Doğrulama:**
- Swagger UI üzerinden `/api/auth/verify-email` endpoint'ini kullanarak token ile doğrulama yapılabilir
- Database'den token'ı alıp API'ye gönderebilirsiniz

### Çözüm 2: Alternatif Email Servisleri

Gmail SMTP yerine alternatif email servisleri kullanılabilir:

#### SendGrid (Önerilen)
- **Port:** 587
- **SSL:** STARTTLS
- **Ücretsiz Plan:** 100 email/gün
- **Kurulum:** https://sendgrid.com

`.env` dosyasında:
```bash
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=<SENDGRID_API_KEY>
```

#### Mailgun
- **Port:** 587
- **SSL:** STARTTLS
- **Ücretsiz Plan:** 5,000 email/ay
- **Kurulum:** https://mailgun.com

`.env` dosyasında:
```bash
MAIL_HOST=smtp.mailgun.org
MAIL_PORT=587
MAIL_USERNAME=<MAILGUN_SMTP_USERNAME>
MAIL_PASSWORD=<MAILGUN_SMTP_PASSWORD>
```

#### Amazon SES
- **Port:** 587 veya 465
- **SSL:** STARTTLS veya SSL
- **Ücretsiz Plan:** 62,000 email/ay (EC2'dan)
- **Kurulum:** https://aws.amazon.com/ses/

---

## 🧪 Test Komutları

### Container İçinden Test

```bash
# Container'a gir
docker exec -it smart_campus_auth bash

# DNS çözümlemesi
nslookup smtp.gmail.com

# Ping testi
ping -c 3 smtp.gmail.com

# Port bağlantı testi
nc -zv smtp.gmail.com 587
nc -zv smtp.gmail.com 465

# SSL bağlantı testi
openssl s_client -connect smtp.gmail.com:465 -quiet
```

### Sunucudan Direkt Test

```bash
# DNS çözümlemesi
nslookup smtp.gmail.com

# Port bağlantı testi
nc -zv smtp.gmail.com 587
nc -zv smtp.gmail.com 465

# Traceroute (network yolunu gösterir)
traceroute smtp.gmail.com
```

---

## 📝 Log Analizi

### Başarılı Email Gönderimi
```
INFO  c.s.a.service.impl.EmailServiceImpl - Verification email sent to: user@example.com
```

### Başarısız Email Gönderimi
```
ERROR o.s.a.i.SimpleAsyncUncaughtExceptionHandler - Unexpected exception occurred
org.springframework.mail.MailSendException: Mail server connection failed
Caused by: java.net.SocketTimeoutException: Connect timed out
```

---

## ✅ Hızlı Kontrol Listesi

- [ ] Container'dan Gmail SMTP'ye network erişimi var mı? (`nc -zv smtp.gmail.com 587`)
- [ ] UFW aktif mi ve çıkış trafiğini engelliyor mu?
- [ ] DigitalOcean Firewall'da outbound rules doğru mu?
- [ ] iptables kuralları SMTP portlarını engelliyor mu?
- [ ] Port 465 ve 587 her ikisi de test edildi mi?
- [ ] Alternatif email servisi (SendGrid/Mailgun) denendi mi?

---

## 🆘 Hala Çalışmıyorsa

1. **Network testi sonuçlarını paylaşın** - Hangi komutlar başarısız oldu?
2. **Firewall loglarını kontrol edin:**
   ```bash
   sudo journalctl -u ufw
   sudo dmesg | grep -i firewall
   ```
3. **DigitalOcean Support'a başvurun** - Firewall kurallarını kontrol etmelerini isteyin
4. **Alternatif email servisi kullanın** - SendGrid veya Mailgun

---

**Son Güncelleme:** 10 Aralık 2025

