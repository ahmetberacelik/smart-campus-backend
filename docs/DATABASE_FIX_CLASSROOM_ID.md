# 🔧 Veritabanı Düzeltme Raporu: course_sections Tablosu

**Tarih:** 16 Aralık 2024  
**Öncelik:** 🔴 Kritik  
**Durum:** Beklemede

---

## 1. Sorun Özeti

`academic-service` backend servisi çalışırken **500 Internal Server Error** hatası alınıyor. Sebebi: `CourseSection` entity'sinde tanımlı `classroom_id` alanı, veritabanındaki `course_sections` tablosunda **mevcut değil**.

---

## 2. Hata Mesajı

```
org.hibernate.exception.SQLGrammarException: 
Unknown column 'cs1_0.classroom_id' in 'field list'

SQL: select cs1_0.id, cs1_0.capacity, cs1_0.classroom_id, cs1_0.course_id, 
     cs1_0.created_at, cs1_0.enrolled_count, cs1_0.instructor_id, 
     cs1_0.schedule_json, cs1_0.section_number, cs1_0.semester, 
     cs1_0.updated_at, cs1_0.year 
from course_sections cs1_0 
where cs1_0.semester=? and cs1_0.year=?
```

---

## 3. Kök Neden Analizi

| Bileşen | Durum |
|---------|-------|
| **Backend Entity** | `CourseSection.java` içinde `classroom` ManyToOne ilişkisi tanımlı ✅ |
| **Database Tablosu** | `course_sections` tablosunda `classroom_id` kolonu **YOK** ❌ |

Backend kodu ile veritabanı şeması arasında **uyumsuzluk** var.

---

## 4.Hatayı çözün

---

## 5. Doğrulama

Migration sonrası tablonun yapısını kontrol edin:

```sql
DESCRIBE course_sections;
```

Beklenen çıktıda `classroom_id` kolonu görünmeli:

```
+----------------+--------------+------+-----+---------+----------------+
| Field          | Type         | Null | Key | Default | Extra          |
+----------------+--------------+------+-----+---------+----------------+
| id             | bigint       | NO   | PRI | NULL    | auto_increment |
| course_id      | bigint       | NO   | MUL | NULL    |                |
| section_number | varchar(10)  | NO   |     | NULL    |                |
| semester       | varchar(20)  | NO   |     | NULL    |                |
| year           | int          | NO   |     | NULL    |                |
| instructor_id  | bigint       | NO   | MUL | NULL    |                |
| classroom_id   | bigint       | YES  | MUL | NULL    |     <-- YENİ   |
| capacity       | int          | NO   |     | 40      |                |
| enrolled_count | int          | NO   |     | 0       |                |
| schedule_json  | json         | YES  |     | NULL    |                |
| created_at     | timestamp    | YES  |     | NULL    |                |
| updated_at     | timestamp    | YES  |     | NULL    |                |
+----------------+--------------+------+-----+---------+----------------+
```

---

## 6. Deploy Sonrası Adımlar

Migration uygulandıktan sonra **backend servisini yeniden başlatın**:

```bash
# Sunucuya SSH ile bağlanın (138.68.99.35)
ssh root@138.68.99.35

# Backend dizinine gidin
cd /path/to/smart-campus-backend

# Academic service container'ını yeniden başlatın
docker-compose restart academic-service

# Logları kontrol edin
docker-compose logs -f academic-service
```

---

## 7. İlgili Backend Dosyaları

Bu değişiklik aşağıdaki backend dosyalarıyla uyumludur:

| Dosya | Açıklama |
|-------|----------|
| `CourseSection.java` | Entity - `@ManyToOne Classroom classroom` ilişkisi |
| `SectionServiceImpl.java` | Section oluştururken classroom set ediliyor |
| `CourseSectionResponse.java` | DTO - `classroomId` ve `classroomName` alanları |
| `CreateSectionRequest.java` | Request DTO - `classroomId` alanı |

---

## 8. Referans: DATABASE_SCHEMA.md Güncellemesi

`DATABASE_SCHEMA.md` dosyasındaki `course_sections` tablo tanımı da güncellenmelidir. Tabloya yeni satır ekleyin:

```markdown
| classroom_id | BIGINT | FK, NULL | Derslik (GPS koordinatları için) |
```

---


**Rapor Sonu**
