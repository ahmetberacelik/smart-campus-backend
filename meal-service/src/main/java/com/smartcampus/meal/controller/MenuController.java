package com.smartcampus.meal.controller;

import com.smartcampus.meal.dto.response.ApiResponse;
import com.smartcampus.meal.dto.response.MenuResponse;
import com.smartcampus.meal.entity.Cafeteria;
import com.smartcampus.meal.repository.CafeteriaRepository;
import com.smartcampus.meal.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/meals")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final CafeteriaRepository cafeteriaRepository;

    /**
     * Tüm yemekhaneleri listele
     */
    @GetMapping("/cafeterias")
    public ResponseEntity<ApiResponse<List<Cafeteria>>> getAllCafeterias() {
        List<Cafeteria> cafeterias = cafeteriaRepository.findByIsActiveTrue();
        return ResponseEntity.ok(ApiResponse.success(cafeterias));
    }

    /**
     * Yemekhane detayı
     */
    @GetMapping("/cafeterias/{id}")
    public ResponseEntity<ApiResponse<Cafeteria>> getCafeteria(@PathVariable Long id) {
        Cafeteria cafeteria = cafeteriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Yemekhane bulunamadı"));
        return ResponseEntity.ok(ApiResponse.success(cafeteria));
    }

    /**
     * Belirli tarih için menüleri getir
     */
    @GetMapping("/menus")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMenusForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long cafeteriaId) {
        
        List<MenuResponse> menus;
        if (cafeteriaId != null) {
            menus = menuService.getMenusForDateAndCafeteria(date, cafeteriaId);
        } else {
            menus = menuService.getMenusForDate(date);
        }
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    /**
     * Haftalık menü
     */
    @GetMapping("/menus/weekly")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getWeeklyMenu(
            @RequestParam Long cafeteriaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        List<MenuResponse> menus = menuService.getWeeklyMenu(cafeteriaId, startDate);
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    /**
     * Menü detayı
     */
    @GetMapping("/menus/{id}")
    public ResponseEntity<ApiResponse<MenuResponse>> getMenuById(@PathVariable Long id) {
        MenuResponse menu = menuService.getMenuById(id);
        return ResponseEntity.ok(ApiResponse.success(menu));
    }

    /**
     * Bugünkü menüleri getir
     */
    @GetMapping("/menus/today")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getTodayMenus(
            @RequestParam(required = false) Long cafeteriaId) {
        
        List<MenuResponse> menus;
        if (cafeteriaId != null) {
            menus = menuService.getMenusForDateAndCafeteria(LocalDate.now(), cafeteriaId);
        } else {
            menus = menuService.getMenusForDate(LocalDate.now());
        }
        return ResponseEntity.ok(ApiResponse.success(menus));
    }
}
