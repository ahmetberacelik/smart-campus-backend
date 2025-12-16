# 🔴 JWT Authentication Sorunu - Backend Düzeltme Raporu

## Sorun Özeti

**Tarih:** 16 Aralık 2025  
**Öncelik:** KRİTİK  
**Etkilenen Servisler:** Academic-Service, Attendance-Service  
**Belirti:** Öğrenci giriş yaptıktan sonra `/my-enrollments`, `/my-grades`, `/my-attendance` gibi rol bazlı endpoint'lere erişmeye çalıştığında 401 Unauthorized hatası alıyor.

---

## Teknik Analiz

### Sorun 1: JWT Secret Key Encoding Uyumsuzluğu

Auth-service ve diğer servisler JWT secret key'i farklı şekillerde decode ediyor.

#### Auth-Service (Token Üreten)
**Dosya:** `auth-service/src/main/java/com/smartcampus/auth/security/JwtTokenProvider.java`

```java
private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);  // ← BASE64 decode
    return Keys.hmacShaKeyFor(keyBytes);
}
```

#### Academic-Service (Token Doğrulayan)
**Dosya:** `academic-service/src/main/java/com/smartcampus/academic/security/JwtTokenProvider.java`

```java
public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));  // ← UTF-8 bytes
}
```

#### Attendance-Service (Token Doğrulayan)
**Dosya:** `attendance-service/src/main/java/com/smartcampus/attendance/security/JwtTokenProvider.java`

```java
public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));  // ← UTF-8 bytes
}
```

**Sonuç:** Farklı encoding yöntemleri farklı secret key'ler üretiyor → Token imza doğrulaması başarısız oluyor → 401 Unauthorized.

---

### Sorun 2: JWT Token'da Role Claim Eksik

Auth-service token oluştururken `role` claim'i eklemiyor.

#### Mevcut Token Oluşturma (Auth-Service)
**Dosya:** `auth-service/src/main/java/com/smartcampus/auth/security/JwtTokenProvider.java`

```java
public String generateAccessToken(String email) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

    return Jwts.builder()
            .subject(email)           // ← Sadece email var
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
}
```

#### Academic-Service Beklentisi
**Dosya:** `academic-service/src/main/java/com/smartcampus/academic/security/JwtTokenProvider.java`

```java
public String getRoleFromToken(String token) {
    Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

    return claims.get("role", String.class);  // ← "role" claim'i arıyor ama YOK!
}
```

**Sonuç:** Token'da role bilgisi olmadığı için `@PreAuthorize("hasRole('STUDENT')")` kontrolü başarısız oluyor.

---

### Sorun 3: JWT Subject Format Uyumsuzluğu

- **Auth-service:** Token subject'ine **email** (String) koyuyor
- **Academic-service:** Token subject'inden **userId** (Long) almaya çalışıyor

#### Auth-Service Token Oluşturma
```java
.subject(email)  // "student@example.com"
```

#### Academic-Service Token Okuma
```java
public Long getUserIdFromToken(String token) {
    // ...
    return Long.parseLong(claims.getSubject());  // ← "student@example.com" parse edilemez!
}
```

**Sonuç:** `NumberFormatException` veya yanlış kullanıcı tanımlama.

---

## Çözüm Adımları

### Adım 1: Auth-Service - Token Oluşturmayı Güncelle

**Dosya:** `auth-service/src/main/java/com/smartcampus/auth/security/JwtTokenProvider.java`

#### Mevcut Kod:
```java
public String generateAccessToken(String email) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

    return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
}
```

#### Yeni Kod:
```java
public String generateAccessToken(Long userId, String email, String role) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

    return Jwts.builder()
            .subject(String.valueOf(userId))   // userId artık subject
            .claim("email", email)             // email ayrı claim olarak
            .claim("role", role)               // role eklendi
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
}
```

#### Eski Metodu Korumak İsterseniz (Overload):
```java
// Eski metod - geriye dönük uyumluluk için
public String generateAccessToken(String email) {
    return generateAccessToken(null, email, null);
}

// Yeni metod - tam bilgi ile
public String generateAccessToken(Long userId, String email, String role) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

    Jwts.JwtBuilder builder = Jwts.builder()
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey());

    // userId varsa subject olarak kullan, yoksa email
    if (userId != null) {
        builder.subject(String.valueOf(userId));
        builder.claim("email", email);
    } else {
        builder.subject(email);
    }

    // Role varsa ekle
    if (role != null) {
        builder.claim("role", role);
    }

    return builder.compact();
}
```

---

### Adım 2: Auth-Service - AuthServiceImpl Güncelle

**Dosya:** `auth-service/src/main/java/com/smartcampus/auth/service/impl/AuthServiceImpl.java`

Token oluşturulurken userId ve role bilgisini de gönder:

```java
// Login metodunda
String accessToken = jwtTokenProvider.generateAccessToken(
    user.getId(),           // userId
    user.getEmail(),        // email
    user.getRole().name()   // role: "STUDENT", "FACULTY", "ADMIN"
);
```

---

### Adım 3: Academic-Service - Key Encoding Düzelt

**Dosya:** `academic-service/src/main/java/com/smartcampus/academic/security/JwtTokenProvider.java`

#### Mevcut Kod:
```java
public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
}
```

#### Yeni Kod:
```java
import io.jsonwebtoken.io.Decoders;

public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
    // Auth-service ile aynı encoding kullan
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
}
```

---

### Adım 4: Attendance-Service - Key Encoding Düzelt

**Dosya:** `attendance-service/src/main/java/com/smartcampus/attendance/security/JwtTokenProvider.java`

#### Mevcut Kod:
```java
public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
}
```

#### Yeni Kod:
```java
import io.jsonwebtoken.io.Decoders;

public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
    // Auth-service ile aynı encoding kullan
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
}
```

---

### Adım 5: JwtAuthenticationFilter Güncelle (Her İki Serviste)

Academic-service ve attendance-service'deki `JwtAuthenticationFilter` sınıflarında role claim'ini düzgün okuduğunuzdan emin olun.

**Örnek Güncelleme:**

```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                 HttpServletResponse response, 
                                 FilterChain filterChain) throws ServletException, IOException {
    String token = getJwtFromRequest(request);

    if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String role = jwtTokenProvider.getRoleFromToken(token);

        // Role'ü Spring Security formatına çevir
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + role)  // "ROLE_STUDENT", "ROLE_FACULTY", etc.
        );

        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(userId, null, authorities);
        
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
}
```

---

## Değiştirilmesi Gereken Dosyalar Özeti

| Servis | Dosya | Değişiklik |
|--------|-------|------------|
| auth-service | `security/JwtTokenProvider.java` | Token'a userId, email, role ekle |
| auth-service | `service/impl/AuthServiceImpl.java` | Token oluştururken userId ve role gönder |
| academic-service | `security/JwtTokenProvider.java` | Key encoding'i BASE64 decode yap |
| academic-service | `security/JwtAuthenticationFilter.java` | Role'ü authorities'e düzgün çevir |
| attendance-service | `security/JwtTokenProvider.java` | Key encoding'i BASE64 decode yap |
| attendance-service | `security/JwtAuthenticationFilter.java` | Role'ü authorities'e düzgün çevir |

---

## Test Senaryoları

Değişikliklerden sonra şu testleri yapın:

### 1. Token İçeriği Kontrolü
Login sonrası dönen token'ı [jwt.io](https://jwt.io) sitesinde decode edin:

**Beklenen Payload:**
```json
{
  "sub": "1",
  "email": "student@example.com",
  "role": "STUDENT",
  "iat": 1734364800,
  "exp": 1734365700
}
```

### 2. Endpoint Testleri

| Endpoint | Beklenen Sonuç |
|----------|----------------|
| `GET /api/v1/enrollments/my-enrollments` | 200 OK (STUDENT) |
| `GET /api/v1/grades/my-grades` | 200 OK (STUDENT) |
| `GET /api/v1/attendance/my-attendance` | 200 OK (STUDENT) |
| `POST /api/v1/attendance/sessions` | 200 OK (FACULTY) |
| `GET /api/v1/sections/my-sections` | 200 OK (FACULTY) |

### 3. Role Bazlı Erişim Testi

- STUDENT rolü ile FACULTY endpoint'ine istek → 403 Forbidden
- FACULTY rolü ile ADMIN endpoint'ine istek → 403 Forbidden

---

## Ek Notlar

### JWT Secret Key Hakkında

Eğer `.env` dosyasındaki `JWT_SECRET` değeri düz string ise ve Base64 encode edilmemişse, iki seçeneğiniz var:

**Seçenek A:** Secret'ı Base64 encode edin:
```bash
echo -n "your-secret-key" | base64
# Sonucu .env'ye koyun
```

**Seçenek B:** Tüm servislerde UTF-8 bytes kullanın (Auth-service'i de değiştirin)

**Öneri:** Seçenek A daha güvenlidir çünkü Base64 encoded secret'lar binary-safe'dir.

---

## Hata Ayıklama İpuçları

Backend loglarında şunları kontrol edin:

```
# Academic-service veya Attendance-service loglarında
JWT validation error: ...
Invalid JWT signature
Expired JWT token
```

Eğer "Invalid JWT signature" görüyorsanız → Key encoding sorunu
Eğer role null dönüyorsa → Token'da role claim eksik

---

## Sonuç

Bu değişiklikler yapıldıktan sonra:
1. Auth-service tutarlı ve bilgi dolu token'lar üretecek
2. Academic-service ve attendance-service bu token'ları doğrulayabilecek
3. `@PreAuthorize` annotation'ları düzgün çalışacak
4. Frontend'de 401 hataları ortadan kalkacak

**Tahmini Düzeltme Süresi:** 30-60 dakika


