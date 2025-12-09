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

## 10. Versiyon Geçmişi

| Versiyon | Tarih | Değişiklikler |
|----------|-------|---------------|
| 1.0 | 2025-12-09 | İlk versiyon - Part 1 endpoint'leri |

---

**Hazırlayan:** Smart Campus Backend Team  
**Son Güncelleme:** 9 Aralık 2025  
**API Versiyonu:** v1

