# Akıllı Kampüs Yönetim Sistemi - Veritabanı Şeması

## 1. Genel Bakış

Bu doküman, Akıllı Kampüs Yönetim Sistemi'nin **Part 1 (Kimlik Doğrulama ve Kullanıcı Yönetimi)** kapsamındaki veritabanı şemasını, tablo yapılarını ve ilişkilerini içerir.

| Özellik | Değer |
|---------|-------|
| **Veritabanı Yönetim Sistemi** | MySQL 8.0+ |
| **Tasarım Prensibi** | 3NF (Third Normal Form) |

---

## 2. ER Diyagramı

> **Not:** Aşağıdaki diyagram metinsel temsildir. Draw.io veya benzeri bir araçla görselleştirilip buraya görseli eklenecektir.

```mermaid
erDiagram
    DEPARTMENTS ||--o{ STUDENTS : "has"
    DEPARTMENTS ||--o{ FACULTY : "has"
    USERS ||--|| STUDENTS : "is a"
    USERS ||--|| FACULTY : "is a"
    USERS ||--o{ REFRESH_TOKENS : "owns"
    USERS ||--o{ EMAIL_VERIFICATIONS : "has"
    USERS ||--o{ PASSWORD_RESETS : "requests"

    USERS {
        bigint id PK
        varchar email UK
        varchar password_hash
        enum role
        boolean is_verified
        boolean is_active
        timestamp created_at
    }

    STUDENTS {
        bigint id PK
        bigint user_id FK_UK
        varchar student_number UK
        bigint department_id FK
        decimal gpa
    }

    FACULTY {
        bigint id PK
        bigint user_id FK_UK
        varchar employee_number UK
        bigint department_id FK
        varchar title
    }

    DEPARTMENTS {
        bigint id PK
        varchar name
        varchar code UK
        varchar faculty_name
    }
```

---

## 3. Tablo Detayları

### 3.1. departments (Bölümler)

Üniversitedeki akademik bölümleri tutar. Öğrenciler ve öğretim üyeleri bir bölüme bağlı olmak zorundadır.

| Sütun Adı | Veri Tipi | Kısıtlamalar (Constraints) | Açıklama |
|-----------|-----------|---------------------------|----------|
| id | BIGINT | PK, Auto Increment | Benzersiz bölüm ID'si |
| name | VARCHAR(100) | NOT NULL | Bölüm adı (Örn: Bilgisayar Mühendisliği) |
| code | VARCHAR(10) | UNIQUE, NOT NULL | Bölüm kodu (Örn: CENG) |
| faculty_name | VARCHAR(100) | NOT NULL | Bağlı olduğu fakülte adı (Örn: Mühendislik Fakültesi) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Kayıt tarihi |

---

### 3.2. users (Kullanıcılar)

Sisteme giriş yapacak tüm aktörlerin (Öğrenci, Akademisyen, Admin) temel kimlik bilgilerini tutar.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
|-----------|-----------|--------------|----------|
| id | BIGINT | PK, Auto Increment | Benzersiz kullanıcı ID'si |
| email | VARCHAR(150) | UNIQUE, NOT NULL, INDEX | Kullanıcı e-posta adresi (Giriş için) |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt ile hashlenmiş şifre |
| first_name | VARCHAR(50) | NOT NULL | Ad |
| last_name | VARCHAR(50) | NOT NULL | Soyad |
| phone_number | VARCHAR(20) | NULL | Telefon numarası |
| profile_picture | VARCHAR(255) | NULL | Profil fotoğrafı dosya yolu/URL |
| role | ENUM | NOT NULL | 'STUDENT', 'FACULTY', 'ADMIN' |
| is_verified | TINYINT(1) | DEFAULT 0 | Email doğrulaması yapıldı mı? |
| is_active | TINYINT(1) | DEFAULT 1 | Hesap aktif mi? (Soft delete kontrolü) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Kayıt olma tarihi |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Son güncelleme tarihi |
| deleted_at | TIMESTAMP | NULL | Soft delete zaman damgası |

---

### 3.3. students (Öğrenciler)

`users` tablosunun uzantısıdır. Sadece öğrenci rolüne sahip kullanıcıların akademik bilgilerini tutar.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
|-----------|-----------|--------------|----------|
| id | BIGINT | PK, Auto Increment | Tablo ID'si |
| user_id | BIGINT | FK, UNIQUE, NOT NULL | users tablosuna referans |
| department_id | BIGINT | FK, NOT NULL | departments tablosuna referans |
| student_number | VARCHAR(20) | UNIQUE, NOT NULL | Okul numarası (Örn: 20245501) |
| gpa | DECIMAL(3,2) | DEFAULT 0.00 | Dönem ortalaması |
| cgpa | DECIMAL(3,2) | DEFAULT 0.00 | Genel not ortalaması |

---

### 3.4. faculty (Öğretim Üyeleri)

`users` tablosunun uzantısıdır. Akademik personelin bilgilerini tutar.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
|-----------|-----------|--------------|----------|
| id | BIGINT | PK, Auto Increment | Tablo ID'si |
| user_id | BIGINT | FK, UNIQUE, NOT NULL | users tablosuna referans |
| department_id | BIGINT | FK, NOT NULL | departments tablosuna referans |
| employee_number | VARCHAR(20) | UNIQUE, NOT NULL | Sicil numarası |
| title | VARCHAR(50) | NOT NULL | Unvan (Dr., Prof., Arş. Gör. vb.) |

---

### 3.5. Auth & Security Tabloları

#### refresh_tokens

JWT mimarisinde Access Token süresi dolduğunda oturumu yenilemek için kullanılır.

| Sütun Adı | Veri Tipi | Açıklama |
|-----------|-----------|----------|
| id | BIGINT | PK |
| user_id | BIGINT | Token sahibi (users tablosuna FK) |
| token | VARCHAR(255) | Refresh token string (Unique) |
| expiry_date | TIMESTAMP | Token geçerlilik süresi |

---

#### email_verification_tokens

Kayıt sonrası email doğrulama linki için oluşturulan geçici tokenlar.

| Sütun Adı | Veri Tipi | Açıklama |
|-----------|-----------|----------|
| id | BIGINT | PK |
| user_id | BIGINT | Doğrulanacak kullanıcı |
| token | VARCHAR(255) | URL'de giden token |
| expiry_date | TIMESTAMP | Tokenın geçerlilik süresi (Örn: 24 saat) |

---

#### password_reset_tokens

Şifremi unuttum akışı için kullanılır.

| Sütun Adı | Veri Tipi | Açıklama |
|-----------|-----------|----------|
| id | BIGINT | PK |
| user_id | BIGINT | Şifresini sıfırlayan kullanıcı |
| token | VARCHAR(255) | Reset linkindeki token |
| expiry_date | TIMESTAMP | Geçerlilik süresi (Örn: 15 dk) |

---

## 4. SQL Kurulum Scripti (MySQL)

Aşağıdaki SQL komutları veritabanı şemasını oluşturmak için kullanılır.

```sql
-- Departmanlar Tablosu
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL UNIQUE,
    faculty_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Kullanıcılar Tablosu
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    profile_picture VARCHAR(255),
    role ENUM('STUDENT', 'FACULTY', 'ADMIN') NOT NULL,
    is_verified TINYINT(1) DEFAULT 0,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    INDEX idx_email (email)
);

-- Öğrenciler Tablosu
CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    department_id BIGINT NOT NULL,
    student_number VARCHAR(20) NOT NULL UNIQUE,
    gpa DECIMAL(3,2) DEFAULT 0.00,
    cgpa DECIMAL(3,2) DEFAULT 0.00,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- Öğretim Üyeleri Tablosu
CREATE TABLE faculty (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    department_id BIGINT NOT NULL,
    employee_number VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- Refresh Token Tablosu
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Email Doğrulama Tokenları
CREATE TABLE email_verification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Şifre Sıfırlama Tokenları
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

