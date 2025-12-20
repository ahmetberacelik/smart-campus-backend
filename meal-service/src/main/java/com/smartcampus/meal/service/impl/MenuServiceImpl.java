package com.smartcampus.meal.service.impl;

import com.smartcampus.meal.dto.response.MenuResponse;
import com.smartcampus.meal.entity.Cafeteria;
import com.smartcampus.meal.entity.MealMenu;
import com.smartcampus.meal.repository.CafeteriaRepository;
import com.smartcampus.meal.repository.MealMenuRepository;
import com.smartcampus.meal.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MealMenuRepository menuRepository;
    private final CafeteriaRepository cafeteriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MenuResponse> getMenusForDate(LocalDate date) {
        return menuRepository.findByMenuDateAndIsPublishedTrue(date)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuResponse> getMenusForDateAndCafeteria(LocalDate date, Long cafeteriaId) {
        return menuRepository.findByCafeteriaIdAndMenuDateAndIsPublishedTrue(cafeteriaId, date)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuResponse> getWeeklyMenu(Long cafeteriaId, LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);
        return menuRepository.findWeeklyMenu(cafeteriaId, startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResponse getMenuById(Long menuId) {
        MealMenu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menü bulunamadı"));
        return mapToResponse(menu);
    }

    @Override
    @Transactional
    public MenuResponse createMenu(Long cafeteriaId, LocalDate date, MealMenu.MealType mealType,
                                   String itemsJson, String nutritionJson, BigDecimal price,
                                   boolean isVegan, boolean isVegetarian) {
        Cafeteria cafeteria = cafeteriaRepository.findById(cafeteriaId)
                .orElseThrow(() -> new RuntimeException("Yemekhane bulunamadı"));

        // Aynı tarih ve öğün için menü kontrolü
        if (menuRepository.findByCafeteriaIdAndMenuDateAndMealType(cafeteriaId, date, mealType).isPresent()) {
            throw new RuntimeException("Bu tarih ve öğün için menü zaten var");
        }

        MealMenu menu = MealMenu.builder()
                .cafeteria(cafeteria)
                .menuDate(date)
                .mealType(mealType)
                .itemsJson(itemsJson)
                .nutritionJson(nutritionJson)
                .price(price)
                .isVegan(isVegan)
                .isVegetarian(isVegetarian)
                .isPublished(false)
                .build();

        MealMenu saved = menuRepository.save(menu);
        log.info("Menü oluşturuldu: cafeteriaId={}, date={}, mealType={}", cafeteriaId, date, mealType);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public MenuResponse updateMenu(Long menuId, String itemsJson, String nutritionJson,
                                   BigDecimal price, boolean isVegan, boolean isVegetarian) {
        MealMenu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menü bulunamadı"));

        menu.setItemsJson(itemsJson);
        menu.setNutritionJson(nutritionJson);
        menu.setPrice(price);
        menu.setIsVegan(isVegan);
        menu.setIsVegetarian(isVegetarian);

        MealMenu saved = menuRepository.save(menu);
        log.info("Menü güncellendi: menuId={}", menuId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void publishMenu(Long menuId) {
        MealMenu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menü bulunamadı"));
        menu.setIsPublished(true);
        menuRepository.save(menu);
        log.info("Menü yayınlandı: menuId={}", menuId);
    }

    @Override
    @Transactional
    public void deleteMenu(Long menuId) {
        if (!menuRepository.existsById(menuId)) {
            throw new RuntimeException("Menü bulunamadı");
        }
        menuRepository.deleteById(menuId);
        log.info("Menü silindi: menuId={}", menuId);
    }

    private MenuResponse mapToResponse(MealMenu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .cafeteriaId(menu.getCafeteria().getId())
                .cafeteriaName(menu.getCafeteria().getName())
                .menuDate(menu.getMenuDate())
                .mealType(menu.getMealType())
                .itemsJson(menu.getItemsJson())
                .nutritionJson(menu.getNutritionJson())
                .price(menu.getPrice())
                .isVegan(menu.getIsVegan())
                .isVegetarian(menu.getIsVegetarian())
                .isPublished(menu.getIsPublished())
                .createdAt(menu.getCreatedAt())
                .build();
    }
}
