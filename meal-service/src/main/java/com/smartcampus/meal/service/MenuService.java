package com.smartcampus.meal.service;

import com.smartcampus.meal.dto.response.MenuResponse;
import com.smartcampus.meal.entity.MealMenu;

import java.time.LocalDate;
import java.util.List;

public interface MenuService {
    
    List<MenuResponse> getMenusForDate(LocalDate date);
    
    List<MenuResponse> getMenusForDateAndCafeteria(LocalDate date, Long cafeteriaId);
    
    List<MenuResponse> getWeeklyMenu(Long cafeteriaId, LocalDate startDate);
    
    MenuResponse getMenuById(Long menuId);
    
    MenuResponse createMenu(Long cafeteriaId, LocalDate date, MealMenu.MealType mealType,
                           String itemsJson, String nutritionJson, java.math.BigDecimal price,
                           boolean isVegan, boolean isVegetarian);
    
    MenuResponse updateMenu(Long menuId, String itemsJson, String nutritionJson,
                           java.math.BigDecimal price, boolean isVegan, boolean isVegetarian);
    
    void publishMenu(Long menuId);
    
    void deleteMenu(Long menuId);
}
