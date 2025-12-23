package com.smartcampus.academic.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Frontend'deki program oluşturma sayfası ile uyumlu basit istek DTO'su.
 * CSP algoritmasının tam implementasyonu henüz yok; bu DTO sadece seçilen
 * bölümlerin (section) bilgisini backend'e iletmek için kullanılır.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateScheduleRequest {

    /**
     * Akademik dönem: FALL, SPRING, SUMMER
     */
    @NotNull(message = "Dönem zorunludur")
    private String semester;

    /**
     * Akademik yıl, örn: 2025
     */
    @NotNull(message = "Yıl zorunludur")
    private Integer year;

    /**
     * Programda yer alacak section ID listesi.
     */
    @NotEmpty(message = "En az bir ders bölümü seçilmelidir")
    private List<Long> sectionIds;
}


