# Akıllı Kampüs Yönetim Sistemi - Veritabanı Şeması

## 1. Genel Bakış

Bu doküman, Akıllı Kampüs Yönetim Sistemi'nin **Part 1 (Kimlik Doğrulama ve Kullanıcı Yönetimi)** ve **Part 2 (Akademik Yönetim ve GPS Yoklama)** kapsamındaki veritabanı şemasını, tablo yapılarını ve ilişkilerini içerir.

| Özellik | Değer |
|---------|-------|
| **Veritabanı Yönetim Sistemi** | MySQL 8.0+ |
| **Tasarım Prensibi** | 3NF (Third Normal Form) |
| **Toplam Tablo Sayısı** | 15 |

---

## 2. ER Diyagramı

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         VERİTABANI ŞEMA DİYAGRAMI                           │
│                      Part 1 + Part 2 (Akademik + Yoklama)                   │
└─────────────────────────────────────────────────────────────────────────────┘

                              ┌─────────────────────┐
                              │    departments      │
                              ├─────────────────────┤
                              │ id (PK)             │
                              │ name                │
                              │ code (UK)           │
                              │ faculty_name        │
                              └──────────┬──────────┘
                                         │
           ┌─────────────────────────────┼─────────────────────────────┐
           │                             │                             │
           ▼                             ▼                             ▼
    ┌─────────────┐              ┌─────────────┐              ┌─────────────┐
    │  students   │              │   faculty   │              │   courses   │
    ├─────────────┤              ├─────────────┤              ├─────────────┤
    │ id (PK)     │              │ id (PK)     │              │ id (PK)     │
    │ user_id(FK) │              │ user_id(FK) │              │ code (UK)   │
    │ dept_id(FK) │              │ dept_id(FK) │              │ name        │
    │ student_no  │              │ employee_no │              │ credits     │
    │ gpa, cgpa   │              │ title       │              │ ects        │
    └──────┬──────┘              └──────┬──────┘              └──────┬──────┘
           │                            │                            │
           │                            │                            │
           ▼                            │                            ▼
    ┌─────────────┐                     │                   ┌──────────────────┐
    │ enrollments │                     │                   │ course_sections  │
    ├─────────────┤                     │                   ├──────────────────┤
    │ id (PK)     │                     │                   │ id (PK)          │
    │ student_id  │◄────────────────────┤                   │ course_id (FK)   │
    │ section_id  │                     │                   │ instructor_id(FK)│──┘
    │ status      │                     └──────────────────►│ semester, year   │
    │ grades      │                                         │ capacity         │
    └──────┬──────┘                                         └────────┬─────────┘
           │                                                         │
           │                                                         ▼
           │                                              ┌────────────────────┐
           │                                              │attendance_sessions │
           │                                              ├────────────────────┤
           │                                              │ id (PK)            │
           │                                              │ section_id (FK)    │
           │                                              │ instructor_id (FK) │
           │                                              │ classroom_id (FK)  │
           │                                              │ date, times        │
           │                                              │ lat, lon, radius   │
           │                                              │ qr_code            │
           │                                              └─────────┬──────────┘
           │                                                        │
           ▼                                                        ▼
    ┌──────────────────┐                              ┌────────────────────────┐
    │ attendance_      │                              │   attendance_records   │
    │ records          │◄─────────────────────────────┤                        │
    └──────────────────┘                              ├────────────────────────┤
           │                                          │ id (PK)                │
           │                                          │ session_id (FK)        │
           │                                          │ student_id (FK)        │
           ▼                                          │ check_in_time          │
    ┌──────────────────┐                              │ lat, lon, distance     │
    │ excuse_requests  │                              │ is_flagged             │
    ├──────────────────┤                              └────────────────────────┘
    │ id (PK)          │
    │ student_id (FK)  │
    │ session_id (FK)  │
    │ reason, doc_url  │
    │ status           │
    └──────────────────┘


    ┌──────────────────┐              ┌──────────────────────────┐
    │   classrooms     │              │  course_prerequisites    │
    ├──────────────────┤              ├──────────────────────────┤
    │ id (PK)          │              │ course_id (FK)           │
    │ building         │              │ prerequisite_id (FK)     │
    │ room_number      │              │ (Composite PK)           │
    │ capacity         │              └──────────────────────────┘
    │ latitude         │
    │ longitude        │
    │ features_json    │
    └──────────────────┘

                           ┌─────────────────────┐
                           │       users         │
                           ├─────────────────────┤
                           │ id (PK)             │
                           │ email (UK)          │
                           │ password_hash       │
                           │ role (ENUM)         │
                           │ ...                 │
                           └──────────┬──────────┘
                                      │
              ┌───────────────────────┼───────────────────────┐
              │                       │                       │
              ▼                       ▼                       ▼
    ┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
    │ refresh_tokens   │   │email_verification│   │password_reset_   │
    │                  │   │_tokens           │   │tokens            │
    └──────────────────┘   └──────────────────┘   └──────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                           İLİŞKİ ÖZETİ                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│  departments ──(1:N)──> students, faculty, courses                         │
│  users ──(1:1)──> students, faculty                                        │
│  courses ──(1:N)──> course_sections                                        │
│  courses ──(N:M)──> courses (prerequisites - self-referencing)             │
│  course_sections ──(1:N)──> enrollments, attendance_sessions               │
│  students ──(1:N)──> enrollments, attendance_records, excuse_requests      │
│  faculty ──(1:N)──> course_sections (instructor)                           │
│  classrooms ──(1:N)──> attendance_sessions                                 │
│  attendance_sessions ──(1:N)──> attendance_records, excuse_requests        │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Part 1 Tabloları (Kimlik Doğrulama ve Kullanıcı Yönetimi)

### 3.1. departments (Bölümler)

Üniversitedeki akademik bölümleri tutar.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
|-----------|-----------|--------------|----------|
| id | BIGINT | PK, Auto Increment | Benzersiz bölüm ID'si |
| name | VARCHAR(100) | NOT NULL | Bölüm adı |
| code | VARCHAR(10) | UNIQUE, NOT NULL | Bölüm kodu (Örn: CENG) |
| faculty_name | VARCHAR(100) | NOT NULL | Fakülte adı |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Kayıt tarihi |

---

### 3.2. users (Kullanıcılar)

Sisteme giriş yapacak tüm kullanıcıların temel kimlik bilgilerini tutar.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
|-----------|-----------|--------------|----------|
| id | BIGINT | PK, Auto Increment | Benzersiz kullanıcı ID'si |
| email | VARCHAR(150) | UNIQUE, NOT NULL, INDEX | E-posta adresi |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt ile hashlenmiş şifre |
| first_name | VARCHAR(50) | NOT NULL | Ad |
| last_name | VARCHAR(50) | NOT NULL | Soyad |
| phone_number | VARCHAR(20) | NULL | Telefon numarası |
| profile_picture | VARCHAR(255) | NULL | Profil fotoğrafı URL |
| role | ENUM | NOT NULL | 'STUDENT', 'FACULTY', 'ADMIN' |
| is_verified | TINYINT(1) | DEFAULT 0 | Email doğrulandı mı? |
| is_active | TINYINT(1) | DEFAULT 1 | Hesap aktif mi? |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Kayıt tarihi |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Güncelleme tarihi |
| deleted_at | TIMESTAMP | NULL | Soft delete |

---

### 3.3. students (Öğrenciler)

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
|-----------|-----------|--------------|----------|
| id | BIGINT | PK, Auto Increment | Tablo ID'si |
| user_id | BIGINT | FK, UNIQUE, NOT NULL | users tablosuna referans |
| department_id | BIGINT | FK, NOT NULL | departments tablosuna referans |
| student_number | VARCHAR(20) | UNIQUE, NOT NULL | Okul numarası |
| gpa | DECIMAL(3,2) | DEFAULT 0.00 | Dönem ortalaması |
| cgpa | DECIMAL(3,2) | DEFAULT 0.00 | Genel not ortalaması |

---

### 3.4. faculty (Öğretim Üyeleri)

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
|-----------|-----------|--------------|----------|
| id | BIGINT | PK, Auto Increment | Tablo ID'si |
| user_id | BIGINT | FK, UNIQUE, NOT NULL | users tablosuna referans |
| department_id | BIGINT | FK, NOT NULL | departments tablosuna referans |
| employee_number | VARCHAR(20) | UNIQUE, NOT NULL | Sicil numarası |
| title | VARCHAR(50) | NOT NULL | Unvan (Dr., Prof. vb.) |

---

### 3.5. Auth Tabloları

#### refresh_tokens

| Sütun Adı | Veri Tipi | Açıklama |
|-----------|-----------|----------|
| id | BIGINT | PK |
| user_id | BIGINT | FK -> users |
| token | VARCHAR(255) | Unique token string |
| expiry_date | TIMESTAMP | Geçerlilik süresi |
| created_at | TIMESTAMP | Oluşturma tarihi |

#### email_verification_tokens

| Sütun Adı | Veri Tipi | Açıklama |
|-----------|-----------|----------|
| id | BIGINT | PK |
| user_id | BIGINT | FK -> users |
| token | VARCHAR(255) | Unique token |
| expiry_date | TIMESTAMP | Geçerlilik süresi |

#### password_reset_tokens

| Sütun Adı | Veri Tipi | Açıklama |
|-----------|-----------|----------|
| id | BIGINT | PK |
| user_id | BIGINT | FK -> users |
| token | VARCHAR(255) | Unique token |
| expiry_date | TIMESTAMP | Geçerlilik süresi |

---

## 4. Part 2 Tabloları (Akademik Yönetim ve GPS Yoklama)

### 4.1. courses (Dersler)

Üniversitedeki derslerin temel bilgilerini tutar.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
|-----------|-----------|--------------|----------|
| id | BIGINT | PK, Auto Increment | Benzersiz ders ID'si |
| code | VARCHAR(20) | UNIQUE, NOT NULL | Ders kodu (Örn: CENG301) |
| name | VARCHAR(150) | NOT NULL | Ders adı |
| description | TEXT | NULL | Ders açıklaması |
| credits | INT | NOT NULL, DEFAULT 3 | Kredi sayısı |
| ects | INT | NOT NULL, DEFAULT 5 | ECTS kredisi |
| department_id | BIGINT | FK, NOT NULL | Bağlı olduğu bölüm |
| syllabus_url | VARCHAR(255) | NULL | Ders izlencesi dosya URL'i |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Oluşturma tarihi |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Güncelleme tarihi |

---

### 4.2. course_prerequisites (Ön Koşul Dersleri)

Derslerin ön koşullarını tutar (N:M self-referencing ilişki).

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
|-----------|-----------|--------------|----------|
| course_id | BIGINT | FK, NOT NULL | Ana ders |
| prerequisite_id | BIGINT | FK, NOT NULL | Ön koşul ders |
| | | PRIMARY KEY (course_id, prerequisite_id) | Composite PK |

**İş Kuralı:** Öğrenci bir derse kayıt olmak için tüm ön koşul dersleri başarıyla tamamlamış olmalı. Recursive kontrol gerekli (BFS/DFS).

---

### 4.3. course_sections (Ders Bölümleri)

Bir dersin dönemsel açılan bölümlerini (section) tutar.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
|-----------|-----------|--------------|----------|
| id | BIGINT | PK, Auto Increment | Benzersiz section ID |
| course_id | BIGINT | FK, NOT NULL | Hangi derse ait |
| section_number | VARCHAR(10) | NOT NULL | Bölüm numarası (Örn: 01, 02) |
| semester | ENUM | NOT NULL | 'FALL', 'SPRING', 'SUMMER' |
| year | INT | NOT NULL | Akademik yıl (Örn: 2024) |
| instructor_id | BIGINT | FK, NOT NULL | Dersi veren öğretim üyesi |
| classroom_id | BIGINT | FK, NULL | Derslik (GPS koordinatları için) |
| capacity | INT | NOT NULL, DEFAULT 40 | Kontenjan |
| enrolled_count | INT | NOT NULL, DEFAULT 0 | Kayıtlı öğrenci sayısı |
| schedule_json | JSON | NULL | Ders programı (gün, saat, derslik) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Oluşturma tarihi |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Güncelleme tarihi |
| | | UNIQUE (course_id, section_number, semester, year) | Benzersizlik |

**schedule_json örneği:**
```json
{
  "slots": [
    {"day": "MONDAY", "start": "09:00", "end": "10:50", "classroom_id": 1},
    {"day": "WEDNESDAY", "start": "09:00", "end": "10:50", "classroom_id": 1}
  ]
}
```

---

### 4.4. enrollments (Ders Kayıtları)

Öğrencilerin derse kayıt bilgilerini ve notlarını tutar.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
|-----------|-----------|--------------|----------|
| id | BIGINT | PK, Auto Increment | Kayıt ID |
| student_id | BIGINT | FK, NOT NULL | Öğrenci (students tablosu) |
| section_id | BIGINT | FK, NOT NULL | Ders bölümü |
| status | ENUM | NOT NULL, DEFAULT 'ENROLLED' | 'ENROLLED', 'DROPPED', 'COMPLETED', 'FAILED' |
| enrollment_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Kayıt tarihi |
| midterm_grade | DECIMAL(5,2) | NULL | Vize notu (0-100) |
| final_grade | DECIMAL(5,2) | NULL | Final notu (0-100) |
| homework_grade | DECIMAL(5,2) | NULL | Ödev/Proje notu (0-100) |
| letter_grade | VARCHAR(2) | NULL | Harf notu (AA, BA, BB, vb.) |
| grade_point | DECIMAL(3,2) | NULL | 4.0 üzerinden not (4.00, 3.50 vb.) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Oluşturma tarihi |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Güncelleme tarihi |
| | | UNIQUE (student_id, section_id) | Aynı section'a tekrar kayıt engellenir |

**Not Hesaplama Kuralı:**
- Vize %30, Final %50, Ödev %20
- AA: 90-100 (4.00), BA: 85-89 (3.50), BB: 80-84 (3.00), CB: 75-79 (2.50), CC: 70-74 (2.00), DC: 65-69 (1.50), DD: 60-64 (1.00), FF: <60 (0.00)

---

### 4.5. classrooms (Derslikler)

Kampüsteki derslik ve laboratuvarların bilgilerini tutar. GPS koordinatları yoklama için kullanılır.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
|-----------|-----------|--------------|----------|
| id | BIGINT | PK, Auto Increment | Derslik ID |
| building | VARCHAR(50) | NOT NULL | Bina adı (Örn: Mühendislik A) |
| room_number | VARCHAR(20) | NOT NULL | Oda numarası (Örn: A-201) |
| capacity | INT | NOT NULL | Kapasite |
| latitude | DECIMAL(10, 8) | NOT NULL | GPS enlem koordinatı |
| longitude | DECIMAL(11, 8) | NOT NULL | GPS boylam koordinatı |
| features_json | JSON | NULL | Derslik özellikleri |
| is_active | TINYINT(1) | DEFAULT 1 | Aktif mi? |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Oluşturma tarihi |
| | | UNIQUE (building, room_number) | Benzersizlik |

**features_json örneği:**
```json
{
  "hasProjector": true,
  "hasComputers": false,
  "hasWhiteboard": true,
  "type": "LECTURE_HALL"
}
```

---

### 4.6. attendance_sessions (Yoklama Oturumları)

Öğretim üyesinin açtığı yoklama oturumlarını tutar.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
|-----------|-----------|--------------|----------|
| id | BIGINT | PK, Auto Increment | Oturum ID |
| section_id | BIGINT | FK, NOT NULL | Hangi ders bölümü |
| instructor_id | BIGINT | FK, NOT NULL | Yoklamayı açan öğretim üyesi |
| classroom_id | BIGINT | FK, NULL | Derslik (GPS koordinatları için) |
| date | DATE | NOT NULL | Yoklama tarihi |
| start_time | TIME | NOT NULL | Başlangıç saati |
| end_time | TIME | NULL | Bitiş saati |
| latitude | DECIMAL(10, 8) | NOT NULL | Yoklama merkez GPS enlem |
| longitude | DECIMAL(11, 8) | NOT NULL | Yoklama merkez GPS boylam |
| geofence_radius | INT | DEFAULT 15 | Geçerli yarıçap (metre) |
| qr_code | VARCHAR(255) | UNIQUE, NULL | QR kod (5 saniyede yenilenir) |
| qr_expiry | TIMESTAMP | NULL | QR kod geçerlilik süresi |
| status | ENUM | DEFAULT 'ACTIVE' | 'ACTIVE', 'CLOSED' |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Oluşturma tarihi |

---

### 4.7. attendance_records (Yoklama Kayıtları)

Öğrencilerin yoklama verme kayıtlarını tutar.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
|-----------|-----------|--------------|----------|
| id | BIGINT | PK, Auto Increment | Kayıt ID |
| session_id | BIGINT | FK, NOT NULL | Yoklama oturumu |
| student_id | BIGINT | FK, NOT NULL | Öğrenci |
| check_in_time | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Yoklama verme zamanı |
| latitude | DECIMAL(10, 8) | NOT NULL | Öğrencinin GPS enlemi |
| longitude | DECIMAL(11, 8) | NOT NULL | Öğrencinin GPS boylamı |
| distance_from_center | DECIMAL(10, 2) | NOT NULL | Merkeze uzaklık (metre) |
| ip_address | VARCHAR(45) | NULL | Öğrencinin IP adresi |
| is_flagged | TINYINT(1) | DEFAULT 0 | Şüpheli mi? |
| flag_reason | VARCHAR(255) | NULL | İşaretleme sebebi |
| check_in_method | ENUM | DEFAULT 'GPS' | 'GPS', 'QR_CODE', 'MANUAL' |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Oluşturma tarihi |
| | | UNIQUE (session_id, student_id) | Aynı oturuma tekrar yoklama engellenir |

**Haversine Formula (Mesafe Hesaplama):**
```
distance = 2 * R * asin(sqrt(sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlon/2)))
R = 6371000 metre (Dünya yarıçapı)
```

**Spoofing Detection Kuralları:**
- `is_flagged = 1` ve `flag_reason` şu durumlarda set edilir:
  - Mesafe > geofence_radius + 5m
  - IP adresi kampüs dışı
  - Önceki konumdan impossible travel (>100km/h hız)
  - Mock location flag tespit edildi

---

### 4.8. excuse_requests (Mazeret Talepleri)

Öğrencilerin devamsızlık mazeret taleplerini tutar.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
|-----------|-----------|--------------|----------|
| id | BIGINT | PK, Auto Increment | Talep ID |
| student_id | BIGINT | FK, NOT NULL | Mazeret sahibi öğrenci |
| session_id | BIGINT | FK, NOT NULL | İlgili yoklama oturumu |
| reason | TEXT | NOT NULL | Mazeret açıklaması |
| document_url | VARCHAR(255) | NULL | Belge dosyası URL'i |
| status | ENUM | DEFAULT 'PENDING' | 'PENDING', 'APPROVED', 'REJECTED' |
| reviewed_by | BIGINT | FK, NULL | Onaylayan/reddeden öğretim üyesi |
| reviewed_at | TIMESTAMP | NULL | İnceleme tarihi |
| reviewer_notes | TEXT | NULL | İnceleme notları |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Oluşturma tarihi |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Güncelleme tarihi |

---

## 5. SQL Kurulum Scripti (MySQL)

### Part 1 Tabloları

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

### Part 2 Tabloları

```sql
-- Dersler Tablosu
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    credits INT NOT NULL DEFAULT 3,
    ects INT NOT NULL DEFAULT 5,
    department_id BIGINT NOT NULL,
    syllabus_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id),
    INDEX idx_code (code),
    INDEX idx_department (department_id)
);

-- Ön Koşul Dersleri Tablosu
CREATE TABLE course_prerequisites (
    course_id BIGINT NOT NULL,
    prerequisite_id BIGINT NOT NULL,
    PRIMARY KEY (course_id, prerequisite_id),
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (prerequisite_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Ders Bölümleri Tablosu
CREATE TABLE course_sections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    section_number VARCHAR(10) NOT NULL,
    semester ENUM('FALL', 'SPRING', 'SUMMER') NOT NULL,
    year INT NOT NULL,
    instructor_id BIGINT NOT NULL,
    capacity INT NOT NULL DEFAULT 40,
    enrolled_count INT NOT NULL DEFAULT 0,
    schedule_json JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (instructor_id) REFERENCES faculty(id),
    UNIQUE KEY uk_section (course_id, section_number, semester, year),
    INDEX idx_semester_year (semester, year),
    INDEX idx_instructor (instructor_id)
);

-- Ders Kayıtları Tablosu
CREATE TABLE enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    section_id BIGINT NOT NULL,
    status ENUM('ENROLLED', 'DROPPED', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'ENROLLED',
    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    midterm_grade DECIMAL(5,2),
    final_grade DECIMAL(5,2),
    homework_grade DECIMAL(5,2),
    letter_grade VARCHAR(2),
    grade_point DECIMAL(3,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (section_id) REFERENCES course_sections(id) ON DELETE CASCADE,
    UNIQUE KEY uk_enrollment (student_id, section_id),
    INDEX idx_student (student_id),
    INDEX idx_section (section_id),
    INDEX idx_status (status)
);

-- Derslikler Tablosu
CREATE TABLE classrooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    building VARCHAR(50) NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    capacity INT NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    features_json JSON,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_room (building, room_number),
    INDEX idx_building (building)
);

-- Yoklama Oturumları Tablosu
CREATE TABLE attendance_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id BIGINT NOT NULL,
    instructor_id BIGINT NOT NULL,
    classroom_id BIGINT,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    geofence_radius INT DEFAULT 15,
    qr_code VARCHAR(255) UNIQUE,
    qr_expiry TIMESTAMP NULL,
    status ENUM('ACTIVE', 'CLOSED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (section_id) REFERENCES course_sections(id) ON DELETE CASCADE,
    FOREIGN KEY (instructor_id) REFERENCES faculty(id),
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id),
    INDEX idx_section_date (section_id, date),
    INDEX idx_status (status)
);

-- Yoklama Kayıtları Tablosu
CREATE TABLE attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    check_in_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    distance_from_center DECIMAL(10, 2) NOT NULL,
    ip_address VARCHAR(45),
    is_flagged TINYINT(1) DEFAULT 0,
    flag_reason VARCHAR(255),
    check_in_method ENUM('GPS', 'QR_CODE', 'MANUAL') DEFAULT 'GPS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    UNIQUE KEY uk_attendance (session_id, student_id),
    INDEX idx_student (student_id),
    INDEX idx_flagged (is_flagged)
);

-- Mazeret Talepleri Tablosu
CREATE TABLE excuse_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    document_url VARCHAR(255),
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP NULL,
    reviewer_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES faculty(id),
    INDEX idx_student (student_id),
    INDEX idx_status (status)
);
```

---

## 6. Tablo Özet Listesi

| # | Tablo Adı | Part | Açıklama |
|---|-----------|------|----------|
| 1 | departments | Part 1 | Akademik bölümler |
| 2 | users | Part 1 | Tüm kullanıcılar |
| 3 | students | Part 1 | Öğrenci detayları |
| 4 | faculty | Part 1 | Öğretim üyesi detayları |
| 5 | refresh_tokens | Part 1 | JWT refresh token |
| 6 | email_verification_tokens | Part 1 | Email doğrulama |
| 7 | password_reset_tokens | Part 1 | Şifre sıfırlama |
| 8 | courses | Part 2 | Dersler |
| 9 | course_prerequisites | Part 2 | Ön koşul dersleri |
| 10 | course_sections | Part 2 | Ders bölümleri |
| 11 | enrollments | Part 2 | Ders kayıtları |
| 12 | classrooms | Part 2 | Derslikler (GPS) |
| 13 | attendance_sessions | Part 2 | Yoklama oturumları |
| 14 | attendance_records | Part 2 | Yoklama kayıtları |
| 15 | excuse_requests | Part 2 | Mazeret talepleri |