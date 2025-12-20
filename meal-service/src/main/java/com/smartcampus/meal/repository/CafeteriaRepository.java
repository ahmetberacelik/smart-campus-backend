package com.smartcampus.meal.repository;

import com.smartcampus.meal.entity.Cafeteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CafeteriaRepository extends JpaRepository<Cafeteria, Long> {
    
    List<Cafeteria> findByIsActiveTrue();
    
    List<Cafeteria> findByNameContainingIgnoreCase(String name);
}
