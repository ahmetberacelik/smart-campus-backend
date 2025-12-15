# Smart Campus Backend - API Dokümantasyonu

## 1. Genel Bilgiler

### 1.1 Base URL

**Production (Önerilen):**
```
API Gateway: http://138.68.99.35:8080
Auth Service (Direkt): http://138.68.99.35:8081
Swagger UI: http://138.68.99.35:8081/swagger-ui.html
```

**Lokal Geliştirme:**
```
API Gateway: http://localhost:8080
Auth Service (Direkt): http://localhost:8081
Swagger UI: http://localhost:8081/swagger-ui.html
```

**Not:** Tüm API endpoint'leri **8080 portu** üzerinden API Gateway üzerinden erişilebilir.

### 1.2 API Versiyonu

**Version:** v1  
**Base Path:** `/api/v1`

### 1.3 Response Formatı

Tüm API response'ları standart bir format kullanır:

**Başarılı Response:**
```json
{
  "success": true,
  "message": "İşlem başarılı",
  "data": { ... }
}
```

**Hata Response:**
```json
{
  "success": false,
  "message": "Hata mesajı",
  "error": {
    "code": "ERROR_CODE",
    "details": { ... }
  }
}
```

### 1.4 Authentication

Çoğu endpoint JWT token gerektirir. Token'ı header'da gönderin:

```
Authorization: Bearer {access_token}
```

### 1.5 HTTP Status Kodları

| Kod | Açıklama |
|-----|----------|
| 200 | Başarılı |
| 201 | Oluşturuldu |
| 204 | İçerik yok |
| 400 | Hatalı istek |
| 401 | Yetkisiz erişim |
| 403 | Yasak |
| 404 | Bulunamadı |
| 409 | Çakışma |
| 500 | Sunucu hatası |

---

## 2. Authentication Endpoints

### 2.1 Kullanıcı Kaydı

**Endpoint:** `POST /api/v1/auth/register`

**Açıklama:** Yeni öğrenci veya öğretim üyesi kaydı oluşturur. Kayıt sonrası email doğrulama linki gönderilir.

**Authentication:** Gerekli değil

**Request Body:**
```json
{
  "email": "ahmet.kaya@smartcampus.edu.tr",
  "password": "SecurePass123",
  "firstName": "Ahmet",
  "lastName": "Kaya",
  "phoneNumber": "05551234567",
  "role": "STUDENT",
  "departmentId": 1,
  "studentNumber": "20210001"
}
```

**Öğrenci için zorunlu alanlar:**
- `email` (String, email formatında)
- `password` (String, min 8 karakter, büyük harf + küçük harf + rakam)
- `firstName` (String, max 50 karakter)
- `lastName` (String, max 50 karakter)
- `role` (Enum: STUDENT, FACULTY)
- `departmentId` (Long)
- `studentNumber` (String, unique)

**Öğretim Üyesi için zorunlu alanlar:**
- `email` (String, email formatında)
- `password` (String, min 8 karakter, büyük harf + küçük harf + rakam)
- `firstName` (String, max 50 karakter)
- `lastName` (String, max 50 karakter)
- `role` (Enum: FACULTY)
- `departmentId` (Long)
- `employeeNumber` (String, unique)
- `title` (String, örn: "Dr. Öğr. Üyesi", "Prof. Dr.")

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Kayıt başarılı. Lütfen email adresinizi doğrulayın.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "tokenType": "Bearer",
    "expiresIn": 900000,
    "user": {
      "id": 1,
      "email": "ahmet.kaya@smartcampus.edu.tr",
      "firstName": "Ahmet",
      "lastName": "Kaya",
      "phoneNumber": "05551234567",
      "profilePicture": null,
      "role": "STUDENT",
      "isVerified": false,
      "createdAt": "2025-12-09T10:00:00",
      "studentInfo": {
        "studentNumber": "20210001",
        "departmentId": 1,
        "departmentName": "Bilgisayar Mühendisliği",
        "gpa": 0.0,
        "cgpa": 0.0
      }
    }
  }
}
```

**Hata Response'ları:**

**400 Bad Request - Email zaten kayıtlı (doğrulanmış):**
```json
{
  "success": false,
  "message": "Bu email adresi zaten kayıtlı",
  "error": {
    "code": "EMAIL_ALREADY_EXISTS"
  }
}
```

**400 Bad Request - Email kayıtlı ama doğrulanmamış:**
```json
{
  "success": false,
  "message": "Bu email adresi kayıtlı fakat doğrulanmamış. Yeni doğrulama emaili gönderildi.",
  "error": {
    "code": "EMAIL_NOT_VERIFIED"
  }
}
```

**400 Bad Request - Validation hatası:**
```json
{
  "success": false,
  "message": "Doğrulama hatası",
  "error": {
    "code": "VALIDATION_ERROR",
    "details": {
      "email": "Geçerli bir email adresi giriniz",
      "password": "Şifre en az bir büyük harf, bir küçük harf ve bir rakam içermelidir"
    }
  }
}
```

---

### 2.2 Kullanıcı Girişi

**Endpoint:** `POST /api/v1/auth/login`

**Açıklama:** Email ve şifre ile giriş yapar. Access token ve refresh token döner.

**Authentication:** Gerekli değil

**Request Body:**
```json
{
  "email": "ahmet.kaya@smartcampus.edu.tr",
  "password": "SecurePass123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Giriş başarılı",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "tokenType": "Bearer",
    "expiresIn": 900000,
    "user": {
      "id": 1,
      "email": "ahmet.kaya@smartcampus.edu.tr",
      "firstName": "Ahmet",
      "lastName": "Kaya",
      "phoneNumber": "05551234567",
      "profilePicture": "https://fra1.digitaloceanspaces.com/...",
      "role": "STUDENT",
      "isVerified": true,
      "createdAt": "2025-12-09T10:00:00",
      "studentInfo": {
        "studentNumber": "20210001",
        "departmentId": 1,
        "departmentName": "Bilgisayar Mühendisliği",
        "gpa": 3.75,
        "cgpa": 3.68
      }
    }
  }
}
```

**Hata Response'ları:**

**401 Unauthorized - Yanlış email/şifre:**
```json
{
  "success": false,
  "message": "Email veya şifre hatalı",
  "error": {
    "code": "INVALID_CREDENTIALS"
  }
}
```

**401 Unauthorized - Hesap devre dışı:**
```json
{
  "success": false,
  "message": "Hesabınız devre dışı bırakılmış",
  "error": {
    "code": "ACCOUNT_DISABLED"
  }
}
```

---

### 2.3 Token Yenileme

**Endpoint:** `POST /api/v1/auth/refresh`

**Açıklama:** Refresh token kullanarak yeni access token ve refresh token alır. Her refresh işleminde yeni token'lar oluşturulur.

**Authentication:** Gerekli değil

**Request Body:**
```json
{
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Token yenilendi",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "tokenType": "Bearer",
    "expiresIn": 900000
  }
}
```

**Hata Response'ları:**

**401 Unauthorized - Geçersiz token:**
```json
{
  "success": false,
  "message": "Geçersiz token",
  "error": {
    "code": "TOKEN_INVALID"
  }
}
```

**401 Unauthorized - Token süresi dolmuş:**
```json
{
  "success": false,
  "message": "Token süresi dolmuş",
  "error": {
    "code": "TOKEN_EXPIRED"
  }
}
```

---

### 2.4 Çıkış Yapma

**Endpoint:** `POST /api/v1/auth/logout`

**Açıklama:** Refresh token'ı geçersiz kılar. Sadece o cihazın token'ı silinir.

**Authentication:** Gerekli değil

**Request Body:**
```json
{
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Çıkış başarılı"
}
```

---

### 2.5 Email Doğrulama

**Endpoint:** `POST /api/v1/auth/verify-email`

**Açıklama:** Email doğrulama token'ı ile hesabı aktifleştirir. Token kayıt sonrası gönderilen email'de bulunur.

**Authentication:** Gerekli değil

**Request Body:**
```json
{
  "token": "9f1b4f5c-b164-4e9f-ac4f-8cff031b2a9f"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Email başarıyla doğrulandı"
}
```

**Hata Response'ları:**

**400 Bad Request - Geçersiz token:**
```json
{
  "success": false,
  "message": "Geçersiz doğrulama linki",
  "error": {
    "code": "INVALID_TOKEN"
  }
}
```

**400 Bad Request - Token süresi dolmuş:**
```json
{
  "success": false,
  "message": "Doğrulama linki süresi dolmuş",
  "error": {
    "code": "TOKEN_EXPIRED"
  }
}
```

---

### 2.6 Şifre Sıfırlama İsteği

**Endpoint:** `POST /api/v1/auth/forgot-password`

**Açıklama:** Şifre sıfırlama linki gönderir. Email kayıtlı değilse bile aynı mesaj döner (güvenlik için).

**Authentication:** Gerekli değil

**Request Body:**
```json
{
  "email": "ahmet.kaya@smartcampus.edu.tr"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Eğer bu email adresi kayıtlı ise şifre sıfırlama linki gönderildi"
}
```

---

### 2.7 Şifre Sıfırlama

**Endpoint:** `POST /api/v1/auth/reset-password`

**Açıklama:** Token ile yeni şifre belirler. Şifre değişikliği sonrası tüm refresh token'lar geçersiz kılınır.

**Authentication:** Gerekli değil

**Request Body:**
```json
{
  "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "newPassword": "NewSecurePass123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Şifre başarıyla değiştirildi"
}
```

**Hata Response'ları:**

**400 Bad Request - Geçersiz token:**
```json
{
  "success": false,
  "message": "Geçersiz şifre sıfırlama linki",
  "error": {
    "code": "INVALID_TOKEN"
  }
}
```

**400 Bad Request - Token süresi dolmuş:**
```json
{
  "success": false,
  "message": "Şifre sıfırlama linki süresi dolmuş",
  "error": {
    "code": "TOKEN_EXPIRED"
  }
}
```

---

### 2.8 Doğrulama Emaili Tekrar Gönder

**Endpoint:** `POST /api/v1/auth/resend-verification`

**Açıklama:** Doğrulama emailini tekrar gönderir. Email kayıtlı ama doğrulanmamış olmalıdır.

**Authentication:** Gerekli değil

**Query Parameters:**
- `email` (String, required) - Email adresi

**Request Example:**
```
POST /api/v1/auth/resend-verification?email=ahmet.kaya@smartcampus.edu.tr
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Doğrulama emaili gönderildi"
}
```

**Hata Response'ları:**

**400 Bad Request - Email zaten doğrulanmış:**
```json
{
  "success": false,
  "message": "Email zaten doğrulanmış",
  "error": {
    "code": "ALREADY_VERIFIED"
  }
}
```

**404 Not Found - Email bulunamadı:**
```json
{
  "success": false,
  "message": "Kullanıcı bulunamadı: email",
  "error": {
    "code": "RESOURCE_NOT_FOUND"
  }
}
```

---

## 3. User Management Endpoints

### 3.1 Profil Görüntüleme

**Endpoint:** `GET /api/v1/users/me`

**Açıklama:** Giriş yapmış kullanıcının profil bilgilerini getirir.

**Authentication:** Gerekli (JWT)

**Request Headers:**
```
Authorization: Bearer {access_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "ahmet.kaya@smartcampus.edu.tr",
    "firstName": "Ahmet",
    "lastName": "Kaya",
    "phoneNumber": "05551234567",
    "profilePicture": "https://fra1.digitaloceanspaces.com/smart-campus/profile-pictures/...",
    "role": "STUDENT",
    "isVerified": true,
    "createdAt": "2025-12-09T10:00:00",
    "studentInfo": {
      "studentNumber": "20210001",
      "departmentId": 1,
      "departmentName": "Bilgisayar Mühendisliği",
      "gpa": 3.75,
      "cgpa": 3.68
    }
  }
}
```

**Öğretim Üyesi için örnek:**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "email": "ayse.demir@smartcampus.edu.tr",
    "firstName": "Ayşe",
    "lastName": "Demir",
    "phoneNumber": "05559876543",
    "profilePicture": null,
    "role": "FACULTY",
    "isVerified": true,
    "createdAt": "2025-12-09T10:00:00",
    "facultyInfo": {
      "employeeNumber": "EMP001",
      "title": "Prof. Dr.",
      "departmentId": 2,
      "departmentName": "Elektrik-Elektronik Mühendisliği"
    }
  }
}
```

**Hata Response'ları:**

**401 Unauthorized - Token eksik/geçersiz:**
```json
{
  "success": false,
  "message": "Kimlik doğrulama gerekli",
  "error": {
    "code": "UNAUTHORIZED"
  }
}
```

---

### 3.2 Profil Güncelleme

**Endpoint:** `PUT /api/v1/users/me`

**Açıklama:** Giriş yapmış kullanıcının profil bilgilerini günceller.

**Authentication:** Gerekli (JWT)

**Request Headers:**
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "Ahmet",
  "lastName": "Kaya",
  "phoneNumber": "05551234567"
}
```

**Not:** Tüm alanlar opsiyoneldir. Sadece güncellenmek istenen alanlar gönderilir.

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Profil güncellendi",
  "data": {
    "id": 1,
    "email": "ahmet.kaya@smartcampus.edu.tr",
    "firstName": "Ahmet",
    "lastName": "Kaya",
    "phoneNumber": "05551234567",
    "profilePicture": null,
    "role": "STUDENT",
    "isVerified": true,
    "createdAt": "2025-12-09T10:00:00",
    "studentInfo": {
      "studentNumber": "20210001",
      "departmentId": 1,
      "departmentName": "Bilgisayar Mühendisliği",
      "gpa": 3.75,
      "cgpa": 3.68
    }
  }
}
```

---

### 3.3 Şifre Değiştirme

**Endpoint:** `POST /api/v1/users/me/change-password`

**Açıklama:** Giriş yapmış kullanıcının şifresini değiştirir. Mevcut şifre kontrolü yapılır.

**Authentication:** Gerekli (JWT)

**Request Headers:**
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "currentPassword": "OldSecurePass123",
  "newPassword": "NewSecurePass456"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Şifre başarıyla değiştirildi"
}
```

**Hata Response'ları:**

**400 Bad Request - Mevcut şifre hatalı:**
```json
{
  "success": false,
  "message": "Mevcut şifre hatalı",
  "error": {
    "code": "INVALID_CURRENT_PASSWORD"
  }
}
```

**400 Bad Request - Yeni şifre mevcut şifre ile aynı:**
```json
{
  "success": false,
  "message": "Yeni şifre mevcut şifre ile aynı olamaz",
  "error": {
    "code": "SAME_PASSWORD"
  }
}
```

---

### 3.4 Profil Fotoğrafı Yükleme

**Endpoint:** `POST /api/v1/users/me/profile-picture`

**Açıklama:** Profil fotoğrafı yükler. DigitalOcean Spaces'e kaydedilir. Eski fotoğraf varsa otomatik silinir.

**Authentication:** Gerekli (JWT)

**Request Headers:**
```
Authorization: Bearer {access_token}
Content-Type: multipart/form-data
```

**Request Body (Form Data):**
- `file` (File, required) - JPG, JPEG veya PNG formatında, maksimum 5MB

**cURL Örneği:**
```bash
curl -X POST http://localhost:8081/api/v1/users/me/profile-picture \
  -H "Authorization: Bearer {access_token}" \
  -F "file=@/path/to/image.jpg"
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Profil fotoğrafı yüklendi",
  "data": "https://fra1.digitaloceanspaces.com/smart-campus/profile-pictures/uuid.jpg"
}
```

**Hata Response'ları:**

**400 Bad Request - Dosya seçilmedi:**
```json
{
  "success": false,
  "message": "Dosya seçilmedi",
  "error": {
    "code": "FILE_REQUIRED"
  }
}
```

**400 Bad Request - Dosya boyutu çok büyük:**
```json
{
  "success": false,
  "message": "Dosya boyutu 5MB'dan büyük olamaz",
  "error": {
    "code": "FILE_TOO_LARGE"
  }
}
```

**400 Bad Request - Geçersiz dosya formatı:**
```json
{
  "success": false,
  "message": "Sadece JPG, JPEG ve PNG formatları desteklenir",
  "error": {
    "code": "INVALID_FILE_TYPE"
  }
}
```

---

### 3.5 Profil Fotoğrafı Silme

**Endpoint:** `DELETE /api/v1/users/me/profile-picture`

**Açıklama:** Profil fotoğrafını siler.

**Authentication:** Gerekli (JWT)

**Request Headers:**
```
Authorization: Bearer {access_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Profil fotoğrafı silindi"
}
```

---

### 3.6 Kullanıcı Listesi (Admin)

**Endpoint:** `GET /api/v1/users`

**Açıklama:** Tüm kullanıcıları listeler. Sadece admin erişebilir. Pagination, arama ve filtreleme destekler.

**Authentication:** Gerekli (JWT - ADMIN rolü)

**Authorization:** ADMIN

**Request Headers:**
```
Authorization: Bearer {access_token}
```

**Query Parameters:**
- `page` (Integer, default: 0) - Sayfa numarası (0-indexed)
- `size` (Integer, default: 10) - Sayfa başına kayıt sayısı
- `sortBy` (String, default: "createdAt") - Sıralama alanı
- `sortDir` (String, default: "desc") - Sıralama yönü (asc/desc)
- `search` (String, optional) - Arama terimi (ad, soyad, email)
- `role` (Enum, optional) - Rol filtresi (STUDENT, FACULTY, ADMIN)

**Request Example:**
```
GET /api/v1/users?page=0&size=10&sortBy=createdAt&sortDir=desc&search=ahmet&role=STUDENT
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "email": "ahmet.kaya@smartcampus.edu.tr",
        "firstName": "Ahmet",
        "lastName": "Kaya",
        "phoneNumber": "05551234567",
        "profilePicture": null,
        "role": "STUDENT",
        "isVerified": true,
        "createdAt": "2025-12-09T10:00:00",
        "studentInfo": {
          "studentNumber": "20210001",
          "departmentId": 1,
          "departmentName": "Bilgisayar Mühendisliği",
          "gpa": 3.75,
          "cgpa": 3.68
        }
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false
  }
}
```

**Hata Response'ları:**

**403 Forbidden - Admin değil:**
```json
{
  "success": false,
  "message": "Bu işlem için yetkiniz yok",
  "error": {
    "code": "ACCESS_DENIED"
  }
}
```

---

### 3.7 Kullanıcı Detayı (Admin)

**Endpoint:** `GET /api/v1/users/{id}`

**Açıklama:** Belirtilen kullanıcının detaylarını getirir. Sadece admin erişebilir.

**Authentication:** Gerekli (JWT - ADMIN rolü)

**Authorization:** ADMIN

**Request Headers:**
```
Authorization: Bearer {access_token}
```

**Path Parameters:**
- `id` (Long, required) - Kullanıcı ID'si

**Request Example:**
```
GET /api/v1/users/1
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "ahmet.kaya@smartcampus.edu.tr",
    "firstName": "Ahmet",
    "lastName": "Kaya",
    "phoneNumber": "05551234567",
    "profilePicture": null,
    "role": "STUDENT",
    "isVerified": true,
    "createdAt": "2025-12-09T10:00:00",
    "studentInfo": {
      "studentNumber": "20210001",
      "departmentId": 1,
      "departmentName": "Bilgisayar Mühendisliği",
      "gpa": 3.75,
      "cgpa": 3.68
    }
  }
}
```

**Hata Response'ları:**

**404 Not Found - Kullanıcı bulunamadı:**
```json
{
  "success": false,
  "message": "Kullanıcı bulunamadı: id = 999",
  "error": {
    "code": "RESOURCE_NOT_FOUND"
  }
}
```

---

## 4. Department Endpoints

### 4.1 Bölüm Listesi

**Endpoint:** `GET /api/v1/departments`

**Açıklama:** Tüm bölümleri listeler. Public endpoint'tir, authentication gerekmez.

**Authentication:** Gerekli değil

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Bilgisayar Mühendisliği",
      "code": "CENG",
      "facultyName": "Mühendislik Fakültesi"
    },
    {
      "id": 2,
      "name": "Elektrik-Elektronik Mühendisliği",
      "code": "EEE",
      "facultyName": "Mühendislik Fakültesi"
    },
    {
      "id": 3,
      "name": "Makine Mühendisliği",
      "code": "ME",
      "facultyName": "Mühendislik Fakültesi"
    },
    {
      "id": 4,
      "name": "İşletme",
      "code": "BA",
      "facultyName": "İktisadi ve İdari Bilimler Fakültesi"
    }
  ]
}
```

---

### 4.2 Bölüm Detayı (ID ile)

**Endpoint:** `GET /api/v1/departments/{id}`

**Açıklama:** Belirtilen bölümün detaylarını getirir.

**Authentication:** Gerekli değil

**Path Parameters:**
- `id` (Long, required) - Bölüm ID'si

**Request Example:**
```
GET /api/v1/departments/1
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Bilgisayar Mühendisliği",
    "code": "CENG",
    "facultyName": "Mühendislik Fakültesi"
  }
}
```

**Hata Response'ları:**

**404 Not Found - Bölüm bulunamadı:**
```json
{
  "success": false,
  "message": "Bölüm bulunamadı: id = 999",
  "error": {
    "code": "RESOURCE_NOT_FOUND"
  }
}
```

---

### 4.3 Bölüm Detayı (Kod ile)

**Endpoint:** `GET /api/v1/departments/code/{code}`

**Açıklama:** Bölüm koduna göre detayları getirir.

**Authentication:** Gerekli değil

**Path Parameters:**
- `code` (String, required) - Bölüm kodu (örn: CENG, EEE)

**Request Example:**
```
GET /api/v1/departments/code/CENG
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Bilgisayar Mühendisliği",
    "code": "CENG",
    "facultyName": "Mühendislik Fakültesi"
  }
}
```

**Hata Response'ları:**

**404 Not Found - Bölüm bulunamadı:**
```json
{
  "success": false,
  "message": "Bölüm bulunamadı: code = INVALID",
  "error": {
    "code": "RESOURCE_NOT_FOUND"
  }
}
```

---

## 5. Error Codes

### 5.1 Genel Hata Kodları

| Kod | Açıklama |
|-----|----------|
| `VALIDATION_ERROR` | Doğrulama hatası |
| `UNAUTHORIZED` | Kimlik doğrulama gerekli |
| `INVALID_CREDENTIALS` | Email veya şifre hatalı |
| `ACCOUNT_DISABLED` | Hesap devre dışı |
| `ACCESS_DENIED` | Bu işlem için yetki yok |
| `RESOURCE_NOT_FOUND` | Kaynak bulunamadı |
| `CONFLICT` | Çakışma (örn: email zaten kayıtlı) |
| `TOKEN_INVALID` | Geçersiz token |
| `TOKEN_EXPIRED` | Token süresi dolmuş |
| `INTERNAL_ERROR` | Sunucu hatası |

### 5.2 Özel Hata Kodları

| Kod | Açıklama |
|-----|----------|
| `EMAIL_ALREADY_EXISTS` | Email zaten kayıtlı |
| `EMAIL_NOT_VERIFIED` | Email doğrulanmamış |
| `STUDENT_NUMBER_EXISTS` | Öğrenci numarası zaten kayıtlı |
| `EMPLOYEE_NUMBER_EXISTS` | Sicil numarası zaten kayıtlı |
| `STUDENT_NUMBER_REQUIRED` | Öğrenci numarası zorunlu |
| `EMPLOYEE_NUMBER_REQUIRED` | Sicil numarası zorunlu |
| `TITLE_REQUIRED` | Unvan zorunlu |
| `ADMIN_REGISTRATION_NOT_ALLOWED` | Admin kaydı yapılamaz |
| `ALREADY_VERIFIED` | Email zaten doğrulanmış |
| `INVALID_CURRENT_PASSWORD` | Mevcut şifre hatalı |
| `SAME_PASSWORD` | Yeni şifre mevcut şifre ile aynı |
| `FILE_REQUIRED` | Dosya seçilmedi |
| `FILE_TOO_LARGE` | Dosya boyutu çok büyük |
| `INVALID_FILE_TYPE` | Geçersiz dosya formatı |
| `FILE_SERVICE_NOT_CONFIGURED` | Dosya yükleme servisi yapılandırılmamış |
| `FILE_UPLOAD_FAILED` | Dosya yüklenirken hata oluştu |
| `EMAIL_SEND_FAILED` | Email gönderilemedi |

---

## 6. Authentication Flow

### 6.1 Kayıt ve Email Doğrulama Akışı

```
1. POST /api/v1/auth/register
   ↓
2. Email doğrulama linki gönderilir
   ↓
3. Kullanıcı email'deki linke tıklar
   ↓
4. Frontend token'ı alır ve POST /api/v1/auth/verify-email'e gönderir
   ↓
5. Email doğrulandı, hesap aktif
```

### 6.2 Login ve Token Yenileme Akışı

```
1. POST /api/v1/auth/login
   ↓
2. Access Token (15 dk) + Refresh Token (7 gün) alınır
   ↓
3. Access Token ile API istekleri yapılır
   ↓
4. Access Token süresi dolduğunda:
   ↓
5. POST /api/v1/auth/refresh ile yeni token'lar alınır
   ↓
6. Yeni Access Token ile devam edilir
```

### 6.3 Şifre Sıfırlama Akışı

```
1. POST /api/v1/auth/forgot-password
   ↓
2. Şifre sıfırlama linki gönderilir
   ↓
3. Kullanıcı email'deki linke tıklar
   ↓
4. Frontend token'ı alır ve POST /api/v1/auth/reset-password'e gönderir
   ↓
5. Yeni şifre belirlenir, tüm refresh token'lar geçersiz kılınır
```

---

## 7. Örnek Kullanım Senaryoları

### 7.1 Öğrenci Kaydı ve Giriş

**Production:**
```bash
# 1. Kayıt
curl -X POST http://138.68.99.35:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ahmet.kaya@smartcampus.edu.tr",
    "password": "SecurePass123",
    "firstName": "Ahmet",
    "lastName": "Kaya",
    "phoneNumber": "05551234567",
    "role": "STUDENT",
    "departmentId": 1,
    "studentNumber": "20210001"
  }'

# 2. Email doğrulama (token email'den alınır)
curl -X POST http://138.68.99.35:8080/api/v1/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "token": "9f1b4f5c-b164-4e9f-ac4f-8cff031b2a9f"
  }'

# 3. Giriş
curl -X POST http://138.68.99.35:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ahmet.kaya@smartcampus.edu.tr",
    "password": "SecurePass123"
  }'

# 4. Profil görüntüleme
curl -X GET http://138.68.99.35:8080/api/v1/users/me \
  -H "Authorization: Bearer {access_token}"
```

**Lokal Geliştirme:**
```bash
# 1. Kayıt
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ahmet.kaya@smartcampus.edu.tr",
    "password": "SecurePass123",
    "firstName": "Ahmet",
    "lastName": "Kaya",
    "phoneNumber": "05551234567",
    "role": "STUDENT",
    "departmentId": 1,
    "studentNumber": "20210001"
  }'

# 2. Email doğrulama
curl -X POST http://localhost:8080/api/v1/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "token": "9f1b4f5c-b164-4e9f-ac4f-8cff031b2a9f"
  }'

# 3. Giriş
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ahmet.kaya@smartcampus.edu.tr",
    "password": "SecurePass123"
  }'

# 4. Profil görüntüleme
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer {access_token}"
```

### 7.2 Öğretim Üyesi Kaydı

**Production:**
```bash
curl -X POST http://138.68.99.35:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ayse.demir@smartcampus.edu.tr",
    "password": "SecurePass123",
    "firstName": "Ayşe",
    "lastName": "Demir",
    "phoneNumber": "05559876543",
    "role": "FACULTY",
    "departmentId": 2,
    "employeeNumber": "EMP001",
    "title": "Prof. Dr."
  }'
```

### 7.3 Token Yenileme

**Production:**
```bash
curl -X POST http://138.68.99.35:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  }'
```

### 7.4 Profil Fotoğrafı Yükleme

**Production:**
```bash
curl -X POST http://138.68.99.35:8080/api/v1/users/me/profile-picture \
  -H "Authorization: Bearer {access_token}" \
  -F "file=@/path/to/profile.jpg"
```

---

## 8. Swagger UI

Detaylı API dokümantasyonu ve interaktif test için Swagger UI kullanılabilir:

**Production:** `http://138.68.99.35:8081/swagger-ui.html`  
**Lokal:** `http://localhost:8081/swagger-ui.html`

Swagger UI'da:
- Tüm endpoint'ler listelenir
- Request/Response örnekleri görüntülenir
- Doğrudan API test edilebilir
- Authentication token'ı girebilirsiniz

---

## 9. Notlar

### 9.1 Token Süreleri

- **Access Token:** 15 dakika
- **Refresh Token:** 7 gün
- **Email Verification Token:** 24 saat
- **Password Reset Token:** 1 saat

### 9.2 Şifre Gereksinimleri

- Minimum 8 karakter
- En az 1 büyük harf
- En az 1 küçük harf
- En az 1 rakam
- Özel karakter zorunlu değil

### 9.3 Dosya Yükleme

- **Maksimum boyut:** 5MB
- **Desteklenen formatlar:** JPG, JPEG, PNG
- **Storage:** DigitalOcean Spaces
- **CDN URL:** Otomatik olarak döner

### 9.4 Pagination

- Default sayfa boyutu: 10
- Sayfa numaralandırması: 0-indexed
- Sıralama: `sortBy` ve `sortDir` parametreleri ile

---

## 10. Part 2 - Academic Management Endpoints

### 10.1 Ders Yönetimi (Courses)

#### 10.1.1 Ders Listesi

**Endpoint:** `GET /api/v1/courses`

**Açıklama:** Tüm dersleri listeler. Pagination, filtreleme ve arama destekler.

**Authentication:** Gerekli değil

**Query Parameters:**
- `page` (Integer, default: 0) - Sayfa numarası
- `size` (Integer, default: 10) - Sayfa başına kayıt
- `sortBy` (String, default: "code") - Sıralama alanı
- `sortDir` (String, default: "asc") - Sıralama yönü
- `search` (String, optional) - Ders kodu veya adı ile arama
- `departmentId` (Long, optional) - Bölüm filtresi

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "code": "CSE101",
        "name": "Programlamaya Giriş",
        "description": "Temel programlama kavramları...",
        "credits": 4,
        "ects": 6,
        "syllabusUrl": "https://...",
        "departmentId": 1,
        "departmentName": "Bilgisayar Mühendisliği",
        "prerequisites": [
          {
            "id": 2,
            "code": "MAT101",
            "name": "Matematik I"
          }
        ]
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 50,
    "totalPages": 5
  }
}
```

---

#### 10.1.2 Ders Detayı

**Endpoint:** `GET /api/v1/courses/{id}`

**Açıklama:** Belirtilen dersin detaylarını ve önkoşullarını getirir.

**Authentication:** Gerekli değil

**Path Parameters:**
- `id` (Long, required) - Ders ID'si

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "code": "CSE101",
    "name": "Programlamaya Giriş",
    "description": "Temel programlama kavramları, algoritmalar ve veri yapıları...",
    "credits": 4,
    "ects": 6,
    "syllabusUrl": "https://storage.smartcampus.edu.tr/syllabus/cse101.pdf",
    "departmentId": 1,
    "departmentName": "Bilgisayar Mühendisliği",
    "prerequisites": [
      {
        "id": 2,
        "code": "MAT101",
        "name": "Matematik I"
      }
    ],
    "sections": [
      {
        "id": 1,
        "sectionNumber": "01",
        "semester": "FALL",
        "year": 2025,
        "instructorId": 5,
        "instructorName": "Prof. Dr. Ayşe Demir",
        "capacity": 40,
        "enrolledCount": 35,
        "schedule": {
          "monday": ["09:00-10:50"],
          "wednesday": ["09:00-10:50"]
        },
        "classroomId": 1,
        "classroomName": "A-101"
      }
    ]
  }
}
```

---

#### 10.1.3 Ders Oluşturma (Admin)

**Endpoint:** `POST /api/v1/courses`

**Açıklama:** Yeni ders oluşturur. Sadece admin erişebilir.

**Authentication:** Gerekli (JWT - ADMIN rolü)

**Request Body:**
```json
{
  "code": "CSE102",
  "name": "Veri Yapıları",
  "description": "Temel veri yapıları ve algoritmalar...",
  "credits": 4,
  "ects": 6,
  "syllabusUrl": "https://...",
  "departmentId": 1,
  "prerequisiteIds": [1]
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Ders başarıyla oluşturuldu",
  "data": {
    "id": 2,
    "code": "CSE102",
    "name": "Veri Yapıları"
  }
}
```

---

#### 10.1.4 Ders Güncelleme (Admin)

**Endpoint:** `PUT /api/v1/courses/{id}`

**Açıklama:** Mevcut dersi günceller. Sadece admin erişebilir.

**Authentication:** Gerekli (JWT - ADMIN rolü)

**Request Body:**
```json
{
  "name": "Veri Yapıları ve Algoritmalar",
  "description": "Güncellenmiş açıklama...",
  "credits": 5,
  "ects": 7,
  "prerequisiteIds": [1, 3]
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Ders başarıyla güncellendi",
  "data": { ... }
}
```

---

#### 10.1.5 Ders Silme (Admin)

**Endpoint:** `DELETE /api/v1/courses/{id}`

**Açıklama:** Dersi siler (soft delete). Sadece admin erişebilir.

**Authentication:** Gerekli (JWT - ADMIN rolü)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Ders başarıyla silindi"
}
```

---

### 10.2 Ders Bölümleri (Course Sections)

#### 10.2.1 Section Listesi

**Endpoint:** `GET /api/v1/sections`

**Açıklama:** Ders bölümlerini listeler.

**Authentication:** Gerekli (JWT)

**Query Parameters:**
- `courseId` (Long, optional) - Ders filtresi
- `instructorId` (Long, optional) - Öğretim üyesi filtresi
- `semester` (Enum, optional) - Dönem filtresi (FALL, SPRING, SUMMER)
- `year` (Integer, optional) - Yıl filtresi

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "courseId": 1,
        "courseCode": "CSE101",
        "courseName": "Programlamaya Giriş",
        "sectionNumber": "01",
        "semester": "FALL",
        "year": 2025,
        "instructorId": 5,
        "instructorName": "Prof. Dr. Ayşe Demir",
        "capacity": 40,
        "enrolledCount": 35,
        "schedule": {
          "monday": ["09:00-10:50"],
          "wednesday": ["09:00-10:50"]
        },
        "classroomId": 1,
        "classroomName": "A-101"
      }
    ]
  }
}
```

---

#### 10.2.2 Section Detayı

**Endpoint:** `GET /api/v1/sections/{id}`

**Açıklama:** Belirtilen section'ın detaylarını getirir.

**Authentication:** Gerekli (JWT)

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "courseId": 1,
    "courseCode": "CSE101",
    "courseName": "Programlamaya Giriş",
    "sectionNumber": "01",
    "semester": "FALL",
    "year": 2025,
    "instructorId": 5,
    "instructorName": "Prof. Dr. Ayşe Demir",
    "capacity": 40,
    "enrolledCount": 35,
    "schedule": {
      "monday": ["09:00-10:50"],
      "wednesday": ["09:00-10:50"]
    },
    "classroomId": 1,
    "classroomName": "A-101",
    "classroom": {
      "building": "A Blok",
      "roomNumber": "101",
      "latitude": 41.0082,
      "longitude": 29.0186
    }
  }
}
```

---

#### 10.2.3 Section Oluşturma (Admin)

**Endpoint:** `POST /api/v1/sections`

**Açıklama:** Yeni ders bölümü oluşturur.

**Authentication:** Gerekli (JWT - ADMIN rolü)

**Request Body:**
```json
{
  "courseId": 1,
  "sectionNumber": "02",
  "semester": "FALL",
  "year": 2025,
  "instructorId": 6,
  "capacity": 35,
  "classroomId": 2,
  "schedule": {
    "tuesday": ["13:00-14:50"],
    "thursday": ["13:00-14:50"]
  }
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Section başarıyla oluşturuldu",
  "data": { ... }
}
```

---

#### 10.2.4 Section Güncelleme (Admin)

**Endpoint:** `PUT /api/v1/sections/{id}`

**Açıklama:** Section bilgilerini günceller.

**Authentication:** Gerekli (JWT - ADMIN rolü)

**Request Body:**
```json
{
  "capacity": 40,
  "classroomId": 3,
  "schedule": {
    "monday": ["11:00-12:50"],
    "wednesday": ["11:00-12:50"]
  }
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Section başarıyla güncellendi",
  "data": { ... }
}
```

---

### 10.3 Ders Kayıt (Enrollments)

#### 10.3.1 Derse Kayıt Olma (Student)

**Endpoint:** `POST /api/v1/enrollments`

**Açıklama:** Öğrenci derse kayıt olur. Önkoşul kontrolü, çakışma kontrolü ve kapasite kontrolü yapılır.

**Authentication:** Gerekli (JWT - STUDENT rolü)

**Request Body:**
```json
{
  "sectionId": 1
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Derse kayıt başarılı",
  "data": {
    "id": 1,
    "studentId": 10,
    "sectionId": 1,
    "courseCode": "CSE101",
    "courseName": "Programlamaya Giriş",
    "sectionNumber": "01",
    "status": "ENROLLED",
    "enrollmentDate": "2025-12-15T10:30:00"
  }
}
```

**Hata Response'ları:**

**400 Bad Request - Önkoşul sağlanmadı:**
```json
{
  "success": false,
  "message": "Önkoşul dersleri tamamlanmamış",
  "error": {
    "code": "PREREQUISITE_NOT_MET",
    "details": {
      "missingPrerequisites": [
        {
          "code": "MAT101",
          "name": "Matematik I"
        }
      ]
    }
  }
}
```

**409 Conflict - Ders programı çakışması:**
```json
{
  "success": false,
  "message": "Ders programı çakışması tespit edildi",
  "error": {
    "code": "SCHEDULE_CONFLICT",
    "details": {
      "conflictingCourse": {
        "code": "CSE102",
        "name": "Veri Yapıları",
        "conflictDay": "monday",
        "conflictTime": "09:00-10:50"
      }
    }
  }
}
```

**400 Bad Request - Kapasite dolu:**
```json
{
  "success": false,
  "message": "Ders kapasitesi dolu",
  "error": {
    "code": "SECTION_FULL"
  }
}
```

---

#### 10.3.2 Dersi Bırakma (Student)

**Endpoint:** `DELETE /api/v1/enrollments/{id}`

**Açıklama:** Derse kaydı iptal eder. Drop period (ilk 4 hafta) kontrolü yapılır.

**Authentication:** Gerekli (JWT - STUDENT rolü)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Ders bırakma işlemi başarılı"
}
```

**Hata Response:**

**400 Bad Request - Drop period geçti:**
```json
{
  "success": false,
  "message": "Ders bırakma süresi geçmiş",
  "error": {
    "code": "DROP_PERIOD_EXPIRED"
  }
}
```

---

#### 10.3.3 Kayıtlı Derslerim (Student)

**Endpoint:** `GET /api/v1/enrollments/my-courses`

**Açıklama:** Öğrencinin kayıtlı olduğu dersleri listeler.

**Authentication:** Gerekli (JWT - STUDENT rolü)

**Query Parameters:**
- `semester` (Enum, optional) - Dönem filtresi
- `year` (Integer, optional) - Yıl filtresi

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "enrollmentId": 1,
      "courseId": 1,
      "courseCode": "CSE101",
      "courseName": "Programlamaya Giriş",
      "sectionId": 1,
      "sectionNumber": "01",
      "credits": 4,
      "instructorName": "Prof. Dr. Ayşe Demir",
      "schedule": {
        "monday": ["09:00-10:50"],
        "wednesday": ["09:00-10:50"]
      },
      "classroomName": "A-101",
      "status": "ENROLLED",
      "midtermGrade": null,
      "finalGrade": null,
      "letterGrade": null,
      "attendancePercentage": 85.5,
      "attendanceStatus": "OK"
    }
  ]
}
```

---

#### 10.3.4 Dersin Öğrenci Listesi (Faculty)

**Endpoint:** `GET /api/v1/enrollments/students/{sectionId}`

**Açıklama:** Belirtilen section'a kayıtlı öğrencileri listeler." Sadece dersin öğretim üyesi veya admin erişebilir.

**Authentication:** Gerekli (JWT - FACULTY veya ADMIN rolü)

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "enrollmentId": 1,
      "studentId": 10,
      "studentNumber": "20210001",
      "firstName": "Ahmet",
      "lastName": "Kaya",
      "email": "ahmet.kaya@smartcampus.edu.tr",
      "status": "ENROLLED",
      "midtermGrade": 75.5,
      "finalGrade": null,
      "letterGrade": null,
      "attendancePercentage": 90.0
    }
  ]
}
```

---

### 10.4 Not Yönetimi (Grades)

#### 10.4.1 Notlarım (Student)

**Endpoint:** `GET /api/v1/grades/my-grades`

**Açıklama:** Öğrencinin tüm notlarını getirir.

**Authentication:** Gerekli (JWT - STUDENT rolü)

**Query Parameters:**
- `semester` (Enum, optional) - Dönem filtresi
- `year` (Integer, optional) - Yıl filtresi

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "currentSemester": {
      "semester": "FALL",
      "year": 2025,
      "courses": [
        {
          "courseCode": "CSE101",
          "courseName": "Programlamaya Giriş",
          "credits": 4,
          "midtermGrade": 75.5,
          "finalGrade": 82.0,
          "letterGrade": "BB",
          "gradePoint": 3.0
        }
      ],
      "gpa": 3.25,
      "totalCredits": 18
    },
    "cgpa": 3.18,
    "totalEarnedCredits": 72
  }
}
```

---

#### 10.4.2 Transkript (Student)

**Endpoint:** `GET /api/v1/grades/transcript`

**Açıklama:** Öğrencinin transkriptini JSON formatında getirir.

**Authentication:** Gerekli (JWT - STUDENT rolü)

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "student": {
      "studentNumber": "20210001",
      "firstName": "Ahmet",
      "lastName": "Kaya",
      "department": "Bilgisayar Mühendisliği",
      "faculty": "Mühendislik Fakültesi"
    },
    "semesters": [
      {
        "semester": "FALL",
        "year": 2024,
        "courses": [
          {
            "code": "MAT101",
            "name": "Matematik I",
            "credits": 4,
            "letterGrade": "AA",
            "gradePoint": 4.0
          }
        ],
        "gpa": 3.50,
        "totalCredits": 20
      }
    ],
    "cgpa": 3.18,
    "totalEarnedCredits": 72,
    "generatedAt": "2025-12-15T10:30:00"
  }
}
```

---

#### 10.4.3 Transkript PDF (Student)

**Endpoint:** `GET /api/v1/grades/transcript/pdf`

**Açıklama:** Öğrencinin transkriptini PDF olarak indirir.

**Authentication:** Gerekli (JWT - STUDENT rolü)

**Response (200 OK):**
- Content-Type: application/pdf
- Content-Disposition: attachment; filename="transcript_20210001.pdf"

---

#### 10.4.4 Not Girişi (Faculty)

**Endpoint:** `POST /api/v1/grades`

**Açıklama:** Öğretim üyesi not girişi yapar. Harf notu ve grade point otomatik hesaplanır.

**Authentication:** Gerekli (JWT - FACULTY rolü)

**Request Body:**
```json
{
  "enrollmentId": 1,
  "midtermGrade": 75.5,
  "finalGrade": 82.0
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Not başarıyla kaydedildi",
  "data": {
    "enrollmentId": 1,
    "studentNumber": "20210001",
    "midtermGrade": 75.5,
    "finalGrade": 82.0,
    "average": 79.9,
    "letterGrade": "BB",
    "gradePoint": 3.0
  }
}
```

**Not:** Harf notu hesaplama:
- AA: 90-100 (4.0)
- BA: 85-89 (3.5)
- BB: 80-84 (3.0)
- CB: 75-79 (2.5)
- CC: 70-74 (2.0)
- DC: 65-69 (1.5)
- DD: 60-64 (1.0)
- FF: 0-59 (0.0)

---

## 11. Part 2 - GPS Attendance Endpoints

### 11.1 Yoklama Oturumları (Sessions)

#### 11.1.1 Yoklama Oturumu Açma (Faculty)

**Endpoint:** `POST /api/v1/attendance/sessions`

**Açıklama:** Öğretim üyesi yoklama oturumu açar. QR kod ve GPS koordinatları otomatik oluşturulur.

**Authentication:** Gerekli (JWT - FACULTY rolü)

**Request Body:**
```json
{
  "sectionId": 1,
  "geofenceRadius": 15,
  "durationMinutes": 30
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Yoklama oturumu başlatıldı",
  "data": {
    "id": 1,
    "sectionId": 1,
    "courseCode": "CSE101",
    "courseName": "Programlamaya Giriş",
    "sectionNumber": "01",
    "date": "2025-12-15",
    "startTime": "09:00:00",
    "endTime": "09:30:00",
    "latitude": 41.0082,
    "longitude": 29.0186,
    "geofenceRadius": 15,
    "qrCode": "eyJzZXNzaW9uSWQiOjEsInRva2VuIjoiYWJjMTIzIn0=",
    "qrCodeUrl": "https://api.qrserver.com/v1/create-qr-code/?data=...",
    "status": "ACTIVE",
    "enrolledCount": 35,
    "presentCount": 0
  }
}
```

---

#### 11.1.2 Oturum Detayları

**Endpoint:** `GET /api/v1/attendance/sessions/{id}`

**Açıklama:** Yoklama oturumunun detaylarını getirir.

**Authentication:** Gerekli (JWT - FACULTY veya STUDENT rolü)

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "sectionId": 1,
    "courseCode": "CSE101",
    "courseName": "Programlamaya Giriş",
    "sectionNumber": "01",
    "instructorName": "Prof. Dr. Ayşe Demir",
    "date": "2025-12-15",
    "startTime": "09:00:00",
    "endTime": "09:30:00",
    "latitude": 41.0082,
    "longitude": 29.0186,
    "geofenceRadius": 15,
    "status": "ACTIVE",
    "qrCode": "eyJzZXNzaW9uSWQiOjEsInRva2VuIjoiYWJjMTIzIn0=",
    "enrolledCount": 35,
    "presentCount": 28,
    "classroomName": "A-101"
  }
}
```

---

#### 11.1.3 Oturumu Kapatma (Faculty)

**Endpoint:** `PUT /api/v1/attendance/sessions/{id}/close`

**Açıklama:** Aktif yoklama oturumunu kapatır.

**Authentication:** Gerekli (JWT - FACULTY rolü)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Yoklama oturumu kapatıldı",
  "data": {
    "id": 1,
    "status": "CLOSED",
    "presentCount": 32,
    "absentCount": 3
  }
}
```

---

#### 11.1.4 Benim Oturumlarım (Faculty)

**Endpoint:** `GET /api/v1/attendance/sessions/my-sessions`

**Açıklama:** Öğretim üyesinin açtığı yoklama oturumlarını listeler.

**Authentication:** Gerekli (JWT - FACULTY rolü)

**Query Parameters:**
- `sectionId` (Long, optional) - Section filtresi
- `startDate` (Date, optional) - Başlangıç tarihi
- `endDate` (Date, optional) - Bitiş tarihi
- `status` (Enum, optional) - Durum filtresi (ACTIVE, CLOSED)

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "sectionId": 1,
        "courseCode": "CSE101",
        "date": "2025-12-15",
        "startTime": "09:00:00",
        "status": "CLOSED",
        "presentCount": 32,
        "enrolledCount": 35,
        "attendanceRate": 91.4
      }
    ]
  }
}
```

---

#### 11.1.5 Yoklama Raporu (Faculty)

**Endpoint:** `GET /api/v1/attendance/report/{sectionId}`

**Açıklama:** Belirtilen section için yoklama raporunu getirir.

**Authentication:** Gerekli (JWT - FACULTY rolü)

**Query Parameters:**
- `startDate` (Date, optional) - Başlangıç tarihi
- `endDate` (Date, optional) - Bitiş tarihi

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "sectionId": 1,
    "courseCode": "CSE101",
    "courseName": "Programlamaya Giriş",
    "totalSessions": 14,
    "students": [
      {
        "studentId": 10,
        "studentNumber": "20210001",
        "firstName": "Ahmet",
        "lastName": "Kaya",
        "presentCount": 12,
        "absentCount": 2,
        "excusedCount": 1,
        "attendancePercentage": 92.8,
        "status": "OK",
        "isFlagged": false
      },
      {
        "studentId": 11,
        "studentNumber": "20210002",
        "firstName": "Mehmet",
        "lastName": "Yılmaz",
        "presentCount": 8,
        "absentCount": 6,
        "excusedCount": 0,
        "attendancePercentage": 57.1,
        "status": "CRITICAL",
        "isFlagged": true,
        "flagReason": "GPS_SPOOFING_SUSPECTED"
      }
    ]
  }
}
```

---

### 11.2 Yoklama Verme (Check-in)

#### 11.2.1 GPS ile Yoklama Verme (Student)

**Endpoint:** `POST /api/v1/attendance/sessions/{id}/checkin`

**Açıklama:** Öğrenci GPS koordinatları ile yoklama verir. Haversine formülü ile mesafe hesaplanır. GPS spoofing kontrolü yapılır.

**Authentication:** Gerekli (JWT - STUDENT rolü)

**Request Body:**
```json
{
  "latitude": 41.0083,
  "longitude": 29.0187,
  "accuracy": 5.0
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Yoklama başarıyla verildi",
  "data": {
    "sessionId": 1,
    "checkInTime": "2025-12-15T09:05:23",
    "distance": 12.5,
    "status": "PRESENT"
  }
}
```

**Hata Response'ları:**

**400 Bad Request - Oturum aktif değil:**
```json
{
  "success": false,
  "message": "Yoklama oturumu aktif değil",
  "error": {
    "code": "SESSION_NOT_ACTIVE"
  }
}
```

**400 Bad Request - Konum dışında:**
```json
{
  "success": false,
  "message": "Derslik konumunun dışındasınız",
  "error": {
    "code": "OUT_OF_GEOFENCE",
    "details": {
      "distance": 45.2,
      "allowedRadius": 15,
      "message": "Derslikten 45.2 metre uzaktasınız. Maksimum mesafe: 15 metre"
    }
  }
}
```

**400 Bad Request - Zaten yoklama verilmiş:**
```json
{
  "success": false,
  "message": "Bu oturuma zaten yoklama verdiniz",
  "error": {
    "code": "ALREADY_CHECKED_IN"
  }
}
```

**403 Forbidden - GPS spoofing tespit edildi:**
```json
{
  "success": false,
  "message": "Şüpheli konum verisi tespit edildi",
  "error": {
    "code": "GPS_SPOOFING_DETECTED",
    "details": {
      "reason": "IMPOSSIBLE_TRAVEL",
      "message": "Son konumunuzdan bu konuma belirtilen sürede ulaşmanız mümkün değil"
    }
  }
}
```

---

#### 11.2.2 QR Kod ile Yoklama Verme (Student)

**Endpoint:** `POST /api/v1/attendance/sessions/{id}/checkin-qr`

**Açıklama:** Öğrenci QR kod ve GPS koordinatları ile yoklama verir. QR kod 5 saniyede bir yenilenir.

**Authentication:** Gerekli (JWT - STUDENT rolü)

**Request Body:**
```json
{
  "qrCode": "eyJzZXNzaW9uSWQiOjEsInRva2VuIjoiYWJjMTIzIn0=",
  "latitude": 41.0083,
  "longitude": 29.0187,
  "accuracy": 5.0
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Yoklama başarıyla verildi",
  "data": {
    "sessionId": 1,
    "checkInTime": "2025-12-15T09:05:23",
    "method": "QR_CODE",
    "status": "PRESENT"
  }
}
```

---

#### 11.2.3 Yoklama Durumum (Student)

**Endpoint:** `GET /api/v1/attendance/my-attendance`

**Açıklama:** Öğrencinin tüm dersler için yoklama durumunu getirir.

**Authentication:** Gerekli (JWT - STUDENT rolü)

**Query Parameters:**
- `semester` (Enum, optional) - Dönem filtresi
- `year` (Integer, optional) - Yıl filtresi

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "courseCode": "CSE101",
      "courseName": "Programlamaya Giriş",
      "sectionNumber": "01",
      "totalSessions": 14,
      "presentCount": 12,
      "absentCount": 2,
      "excusedCount": 1,
      "attendancePercentage": 92.8,
      "status": "OK",
      "sessions": [
        {
          "sessionId": 1,
          "date": "2025-12-15",
          "status": "PRESENT",
          "checkInTime": "09:05:23"
        },
        {
          "sessionId": 2,
          "date": "2025-12-17",
          "status": "ABSENT",
          "excuseStatus": "APPROVED"
        }
      ]
    }
  ]
}
```

**Not:** Yoklama durumları:
- `OK`: Devamsızlık %20'nin altında
- `WARNING`: Devamsızlık %20-%30 arasında
- `CRITICAL`: Devamsızlık %30'un üzerinde

---

### 11.3 Mazeret Yönetimi (Excuse Requests)

#### 11.3.1 Mazeret Bildirme (Student)

**Endpoint:** `POST /api/v1/attendance/excuse-requests`

**Açıklama:** Öğrenci mazeret bildirimi yapar. Belge yüklenebilir.

**Authentication:** Gerekli (JWT - STUDENT rolü)

**Request Headers:**
```
Content-Type: multipart/form-data
```

**Request Body (Form Data):**
- `sessionId` (Long, required) - Yoklama oturumu ID'si
- `reason` (String, required) - Mazeret açıklaması
- `document` (File, optional) - Mazeret belgesi (PDF, JPG, PNG)

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Mazeret başvurusu alındı",
  "data": {
    "id": 1,
    "sessionId": 5,
    "courseCode": "CSE101",
    "date": "2025-12-13",
    "reason": "Sağlık raporu nedeniyle...",
    "documentUrl": "https://...",
    "status": "PENDING",
    "createdAt": "2025-12-15T10:30:00"
  }
}
```

---

#### 11.3.2 Mazeret Listesi (Faculty)

**Endpoint:** `GET /api/v1/attendance/excuse-requests`

**Açıklama:** Öğretim üyesinin derslerine ait mazeret başvurularını listeler.

**Authentication:** Gerekli (JWT - FACULTY rolü)

**Query Parameters:**
- `sectionId` (Long, optional) - Section filtresi
- `status` (Enum, optional) - Durum filtresi (PENDING, APPROVED, REJECTED)

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "studentId": 10,
        "studentNumber": "20210001",
        "studentName": "Ahmet Kaya",
        "sessionId": 5,
        "courseCode": "CSE101",
        "date": "2025-12-13",
        "reason": "Sağlık raporu nedeniyle...",
        "documentUrl": "https://...",
        "status": "PENDING",
        "createdAt": "2025-12-15T10:30:00"
      }
    ]
  }
}
```

---

#### 11.3.3 Mazeret Onaylama (Faculty)

**Endpoint:** `PUT /api/v1/attendance/excuse-requests/{id}/approve`

**Açıklama:** Mazeret başvurusunu onaylar.

**Authentication:** Gerekli (JWT - FACULTY rolü)

**Request Body:**
```json
{
  "notes": "Sağlık raporu geçerli kabul edildi"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Mazeret onaylandı",
  "data": {
    "id": 1,
    "status": "APPROVED",
    "reviewedAt": "2025-12-15T14:30:00",
    "notes": "Sağlık raporu geçerli kabul edildi"
  }
}
```

---

#### 11.3.4 Mazeret Reddetme (Faculty)

**Endpoint:** `PUT /api/v1/attendance/excuse-requests/{id}/reject`

**Açıklama:** Mazeret başvurusunu reddeder.

**Authentication:** Gerekli (JWT - FACULTY rolü)

**Request Body:**
```json
{
  "notes": "Belge geçersiz"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Mazeret reddedildi",
  "data": {
    "id": 1,
    "status": "REJECTED",
    "reviewedAt": "2025-12-15T14:30:00",
    "notes": "Belge geçersiz"
  }
}
```

---

### 11.4 Derslik Yönetimi (Classrooms)

#### 11.4.1 Derslik Listesi

**Endpoint:** `GET /api/v1/classrooms`

**Açıklama:** Tüm derslikleri listeler.

**Authentication:** Gerekli (JWT)

**Query Parameters:**
- `building` (String, optional) - Bina filtresi
- `minCapacity` (Integer, optional) - Minimum kapasite

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "building": "A Blok",
      "roomNumber": "101",
      "capacity": 50,
      "latitude": 41.0082,
      "longitude": 29.0186,
      "features": ["projector", "whiteboard", "air_conditioning"]
    }
  ]
}
```

---

#### 11.4.2 Derslik Detayı

**Endpoint:** `GET /api/v1/classrooms/{id}`

**Açıklama:** Belirtilen dersliğin detaylarını getirir.

**Authentication:** Gerekli (JWT)

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "building": "A Blok",
    "roomNumber": "101",
    "capacity": 50,
    "latitude": 41.0082,
    "longitude": 29.0186,
    "features": ["projector", "whiteboard", "air_conditioning"]
  }
}
```

---

## 12. Part 2 - Error Codes (Akademik & Yoklama)

### 12.1 Academic Management Hata Kodları

| Kod | Açıklama |
|-----|----------|
| `COURSE_NOT_FOUND` | Ders bulunamadı |
| `SECTION_NOT_FOUND` | Section bulunamadı |
| `ENROLLMENT_NOT_FOUND` | Kayıt bulunamadı |
| `PREREQUISITE_NOT_MET` | Önkoşul sağlanmadı |
| `SCHEDULE_CONFLICT` | Ders programı çakışması |
| `SECTION_FULL` | Ders kapasitesi dolu |
| `ALREADY_ENROLLED` | Zaten kayıtlı |
| `DROP_PERIOD_EXPIRED` | Ders bırakma süresi geçmiş |
| `NOT_INSTRUCTOR_OF_SECTION` | Bu dersin öğretim üyesi değilsiniz |
| `INVALID_GRADE` | Geçersiz not değeri |

### 12.2 Attendance Hata Kodları

| Kod | Açıklama |
|-----|----------|
| `SESSION_NOT_FOUND` | Yoklama oturumu bulunamadı |
| `SESSION_NOT_ACTIVE` | Yoklama oturumu aktif değil |
| `SESSION_EXPIRED` | Yoklama oturumu süresi dolmuş |
| `ALREADY_CHECKED_IN` | Zaten yoklama verilmiş |
| `OUT_OF_GEOFENCE` | Derslik konumunun dışında |
| `GPS_SPOOFING_DETECTED` | GPS spoofing tespit edildi |
| `INVALID_QR_CODE` | Geçersiz QR kod |
| `QR_CODE_EXPIRED` | QR kod süresi dolmuş |
| `NOT_ENROLLED_IN_SECTION` | Bu derse kayıtlı değilsiniz |
| `EXCUSE_NOT_FOUND` | Mazeret başvurusu bulunamadı |
| `EXCUSE_ALREADY_REVIEWED` | Mazeret zaten değerlendirilmiş |
| `CLASSROOM_NOT_FOUND` | Derslik bulunamadı |

---

## 13. Part 2 - Algoritma Açıklamaları

### 13.1 Haversine Formülü (GPS Mesafe Hesaplama)

İki GPS koordinatı arasındaki mesafeyi metre cinsinden hesaplar:

```
distance = 2 * R * arcsin(sqrt(sin²((lat2-lat1)/2) + cos(lat1) * cos(lat2) * sin²((lon2-lon1)/2)))

R = 6371000 (Dünya yarıçapı, metre)
lat1, lat2 = Enlem (radyan)
lon1, lon2 = Boylam (radyan)
```

**Örnek:**
- Derslik: 41.0082°N, 29.0186°E
- Öğrenci: 41.0083°N, 29.0187°E
- Mesafe: ~12.5 metre

---

### 13.2 Önkoşul Kontrolü (Prerequisite Checking)

Recursive (DFS) algoritma ile tüm önkoşul zinciri kontrol edilir:

```
function checkPrerequisites(courseId, studentId):
    prerequisites = getPrerequisites(courseId)
    for each prereq in prerequisites:
        if not hasCompletedCourse(studentId, prereq.id):
            return false
        if not checkPrerequisites(prereq.id, studentId):
            return false
    return true
```

---

### 13.3 Ders Programı Çakışma Kontrolü

İki zaman diliminin çakışıp çakışmadığını kontrol eder:

```
function hasTimeOverlap(schedule1, schedule2):
    for each day in schedule1:
        if day exists in schedule2:
            for each slot1 in schedule1[day]:
                for each slot2 in schedule2[day]:
                    if overlaps(slot1, slot2):
                        return true
    return false

function overlaps(slot1, slot2):
    return slot1.start < slot2.end AND slot2.start < slot1.end
```

---

### 13.4 GPS Spoofing Tespiti

Çoklu kontrol ile sahte konum tespiti:

1. **IP Kontrolü**: Kampüs IP aralığı kontrolü
2. **Velocity Check**: Önceki konumdan impossible travel tespiti
3. **Accuracy Check**: GPS doğruluk değeri kontrolü (<50m)
4. **Mock Location Flag**: Cihazın mock location ayarı kontrolü

```
function detectSpoofing(studentId, latitude, longitude, accuracy):
    // IP kontrolü
    if not isFromCampusNetwork(request.ip):
        return {flagged: true, reason: "OUTSIDE_CAMPUS_NETWORK"}
    
    // Önceki konum kontrolü
    lastRecord = getLastAttendanceRecord(studentId)
    if lastRecord:
        timeDiff = now - lastRecord.time
        distance = haversine(lastRecord.lat, lastRecord.lon, latitude, longitude)
        maxPossibleDistance = timeDiff.seconds * MAX_WALKING_SPEED
        if distance > maxPossibleDistance:
            return {flagged: true, reason: "IMPOSSIBLE_TRAVEL"}
    
    // Accuracy kontrolü
    if accuracy > 50:
        return {flagged: true, reason: "LOW_GPS_ACCURACY"}
    
    return {flagged: false}
```

---

## 14. Versiyon Geçmişi

| Versiyon | Tarih | Değişiklikler |
|----------|-------|---------------|
| 1.0 | 2025-12-09 | İlk versiyon - Part 1 endpoint'leri |
| 2.0 | 2025-12-15 | Part 2 - Academic Management & GPS Attendance endpoint'leri eklendi |

---

**Hazırlayan:** Smart Campus Backend Team  
**Son Güncelleme:** 15 Aralık 2025  
**API Versiyonu:** v1