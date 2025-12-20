package com.smartcampus.meal.repository;

import com.smartcampus.meal.entity.MealMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealMenuRepository extends JpaRepository<MealMenu, Long> {
    
    List<MealMenu> findByCafeteriaIdAndMenuDateAndIsPublishedTrue(Long cafeteriaId, LocalDate menuDate);
    
    List<MealMenu> findByMenuDateAndIsPublishedTrue(LocalDate menuDate);
    
    List<MealMenu> findByMenuDateBetweenAndIsPublishedTrue(LocalDate startDate, LocalDate endDate);
    
    Optional<MealMenu> findByCafeteriaIdAndMenuDateAndMealType(
            Long cafeteriaId, LocalDate menuDate, MealMenu.MealType mealType);
    
    @Query("SELECT m FROM MealMenu m WHERE m.cafeteria.id = :cafeteriaId " +
           "AND m.menuDate >= :startDate AND m.menuDate <= :endDate " +
           "AND m.isPublished = true ORDER BY m.menuDate, m.mealType")
    List<MealMenu> findWeeklyMenu(
            @Param("cafeteriaId") Long cafeteriaId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
