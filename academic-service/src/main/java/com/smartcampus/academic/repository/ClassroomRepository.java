package com.smartcampus.academic.repository;

import com.smartcampus.academic.entity.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    List<Classroom> findByBuilding(String building);

    Page<Classroom> findByBuilding(String building, Pageable pageable);

    Optional<Classroom> findByBuildingAndRoomNumber(String building, String roomNumber);

    List<Classroom> findByIsActiveTrue();

    Page<Classroom> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT DISTINCT c.building FROM Classroom c WHERE c.isActive = true ORDER BY c.building")
    List<String> findDistinctBuildings();

    @Query("SELECT c FROM Classroom c WHERE c.capacity >= :minCapacity AND c.isActive = true")
    List<Classroom> findByMinCapacity(@Param("minCapacity") Integer minCapacity);

    @Query("SELECT c FROM Classroom c WHERE " +
            "LOWER(c.building) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Classroom> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
