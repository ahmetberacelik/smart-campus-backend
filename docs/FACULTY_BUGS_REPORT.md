# 🔧 Öğretim Görevlisi Panel Hata Raporu

## Sorun Özeti

**Tarih:** 16 Aralık 2025  
**Roller:** FACULTY (Öğretim Görevlisi)  
**Etkilenen Sayfalar:**
1. Yoklama Başlat → 400 Bad Request
2. Mazeret İstekleri → 500 Internal Server Error

---

## 🔴 HATA 1: Yoklama Başlatma (400 Bad Request)

### Belirti
```
POST http://138.68.99.35:8080/api/v1/attendance/sessions 400 (Bad Request)
{code: 'VALIDATION_ERROR', message: 'Doğrulama hatası', details: {...}}
```

### Kök Neden

Frontend ile Backend arasında **istek formatı uyumsuzluğu** var.

**Backend Beklentisi** (`CreateSessionRequest.java`):
```java
@NotNull(message = "Section ID zorunludur")
private Long sectionId;

@NotNull(message = "Enlem zorunludur")
private Double latitude;      // ❌ ZORUNLU - Frontend göndermiyor!

@NotNull(message = "Boylam zorunludur")
private Double longitude;     // ❌ ZORUNLU - Frontend göndermiyor!

@Positive(message = "Geofence radius pozitif olmalıdır")
private Integer geofenceRadius;

@Positive(message = "Süre pozitif olmalıdır")
private Integer durationMinutes;
```

**Frontend Gönderiyor**:
```typescript
{
  sectionId: "123",
  date: "2025-12-16",           // ❌ Backend beklemiyor
  startTime: "2025-12-16T09:00:00.000Z",  // ❌ Backend beklemiyor
  endTime: "2025-12-16T09:30:00.000Z",    // ❌ Backend beklemiyor
  geofenceRadius: 15
}
```

### Çözüm Seçenekleri

#### Seçenek A: Backend'i Güncelle (Önerilen)

Backend `CreateSessionRequest.java` dosyasını güncelleyin:

**Dosya:** `attendance-service/src/main/java/com/smartcampus/attendance/dto/request/CreateSessionRequest.java`

```java
package com.smartcampus.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {

    @NotNull(message = "Section ID zorunludur")
    private Long sectionId;

    // GPS koordinatları - Opsiyonel yap (sınıf konumundan al)
    private Double latitude;
    private Double longitude;

    @Positive(message = "Geofence radius pozitif olmalıdır")
    private Integer geofenceRadius;

    @Positive(message = "Süre pozitif olmalıdır")
    private Integer durationMinutes;

    // Frontend'den gelen tarih/saat bilgileri
    private LocalDate sessionDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
```

**Ayrıca `AttendanceServiceImpl.createSession()` metodunu güncelleyin:**

Eğer `latitude` ve `longitude` null gelirse, section'ın bağlı olduğu classroom'un koordinatlarını kullanın:

```java
@Override
@Transactional
public SessionResponse createSession(Long instructorId, CreateSessionRequest request) {
    // ... mevcut validasyonlar ...
    
    // GPS koordinatları null ise classroom'dan al
    Double latitude = request.getLatitude();
    Double longitude = request.getLongitude();
    
    if (latitude == null || longitude == null) {
        // Classroom bilgisini academic-service'den al veya varsayılan koordinat kullan
        // Örnek: Kampüs merkezi koordinatları
        latitude = latitude != null ? latitude : 41.0082; // İstanbul varsayılan
        longitude = longitude != null ? longitude : 28.9784;
    }
    
    // Session oluştur
    AttendanceSession session = AttendanceSession.builder()
            .sectionId(request.getSectionId())
            .instructorId(instructorId)
            .sessionDate(request.getSessionDate() != null ? request.getSessionDate() : LocalDate.now())
            .startTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now())
            .endTime(request.getEndTime())
            .latitude(latitude)
            .longitude(longitude)
            .geofenceRadius(request.getGeofenceRadius() != null ? request.getGeofenceRadius() : 15)
            // ... devamı
            .build();
    
    // ...
}
```

#### Seçenek B: Frontend'i Güncelle

Frontend'de kullanıcının konumunu al ve gönder. (Daha zor, tarayıcı izni gerektirir)

---

## 🔴 HATA 2: Mazeret İstekleri (500 Internal Server Error)

### Belirti
```
GET http://138.68.99.35:8080/api/v1/attendance/excuse-requests?page=0&size=100 500 (Internal Server Error)
{code: 'INTERNAL_ERROR', message: 'Beklenmeyen bir hata oluştu'}
```

### Olası Kök Nedenler

1. **Repository Query Sorunu**: `findByInstructorIdWithFilters` JPQL sorgusu çalışırken hata
2. **Entity İlişki Sorunu**: ExcuseRequest → AttendanceRecord → AttendanceSession zincirinde problem
3. **NullPointerException**: `mapToResponse` metodunda session bilgisi olmadan response oluşturma

### Çözüm

**Dosya:** `attendance-service/src/main/java/com/smartcampus/attendance/service/impl/ExcuseRequestServiceImpl.java`

`getExcuseRequestsForFaculty` metodunu güncelleyin:

```java
@Override
public PageResponse<ExcuseRequestResponse> getExcuseRequestsForFaculty(Long instructorId, Long sectionId,
                                                                        ExcuseStatus status, Pageable pageable) {
    try {
        Page<ExcuseRequest> requests = excuseRequestRepository.findByInstructorIdWithFilters(
                instructorId, sectionId, status, pageable);

        List<ExcuseRequestResponse> content = requests.getContent().stream()
                .map(excuseRequest -> {
                    try {
                        // AttendanceRecord'u bul
                        AttendanceRecord record = attendanceRecordRepository
                                .findById(excuseRequest.getAttendanceRecordId())
                                .orElse(null);
                        
                        if (record != null) {
                            // Session'ı bul
                            AttendanceSession session = sessionRepository
                                    .findById(record.getSessionId())
                                    .orElse(null);
                            
                            if (session != null) {
                                return mapToResponse(excuseRequest, session);
                            }
                        }
                        
                        // Session bulunamazsa basit response döndür
                        return mapToResponse(excuseRequest);
                    } catch (Exception e) {
                        log.error("Mazeret response oluşturulurken hata: {}", e.getMessage());
                        return mapToResponse(excuseRequest);
                    }
                })
                .toList();

        return PageResponse.from(requests, content);
    } catch (Exception e) {
        log.error("Mazeret istekleri getirilirken hata: {}", e.getMessage(), e);
        throw new RuntimeException("Mazeret istekleri yüklenirken bir hata oluştu", e);
    }
}
```

### Alternatif: Repository Query'yi Optimize Et

Eğer query sorunu varsa, daha basit bir yaklaşım deneyin:

**Dosya:** `attendance-service/src/main/java/com/smartcampus/attendance/repository/ExcuseRequestRepository.java`

```java
// Mevcut karmaşık query yerine daha basit yaklaşım
@Query(value = """
    SELECT er.* FROM excuse_requests er
    INNER JOIN attendance_records ar ON er.attendance_record_id = ar.id
    INNER JOIN attendance_sessions s ON ar.session_id = s.id
    WHERE s.instructor_id = :instructorId
    AND (:sectionId IS NULL OR s.section_id = :sectionId)
    AND (:status IS NULL OR er.status = :status)
    ORDER BY er.created_at DESC
    """, nativeQuery = true)
Page<ExcuseRequest> findByInstructorIdWithFiltersNative(
        @Param("instructorId") Long instructorId,
        @Param("sectionId") Long sectionId,
        @Param("status") String status,
        Pageable pageable);
```

---

## 📋 Öncelik Sırası

| Hata | Öncelik | Etki |
|------|---------|------|
| Yoklama Başlatma | 🔴 YÜKSEK | Öğretim görevlisi yoklama başlatamıyor |
| Mazeret İstekleri | 🔴 YÜKSEK | Öğretim görevlisi mazeretleri göremiyor |

---

## 🔍 Debug İçin Backend Logları

Backend'de hata ayıklama için şu komutları çalıştırın:

```bash
# Docker log'larını kontrol et
docker logs attendance-service --tail 100

# Veya canlı izle
docker logs -f attendance-service
```

500 hatası için detaylı stack trace backend loglarında görünecektir.

---

## Test

Düzeltmelerden sonra şu endpoint'leri test edin:

### 1. Yoklama Başlatma
```bash
curl -X POST http://138.68.99.35:8080/api/v1/attendance/sessions \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "sectionId": 98,
    "geofenceRadius": 15,
    "durationMinutes": 30
  }'
```

### 2. Mazeret İstekleri
```bash
curl -X GET "http://138.68.99.35:8080/api/v1/attendance/excuse-requests?page=0&size=10" \
  -H "Authorization: Bearer <TOKEN>"
```

