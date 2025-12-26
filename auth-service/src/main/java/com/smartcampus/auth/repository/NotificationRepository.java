package com.smartcampus.auth.repository;

import com.smartcampus.auth.entity.Notification;
import com.smartcampus.auth.entity.NotificationCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Kullanıcının tüm bildirimlerini getir (sayfalı)
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Kullanıcının okunmamış bildirimlerini getir
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // Kullanıcının okunmamış bildirim sayısı
    long countByUserIdAndIsReadFalse(Long userId);

    // Kategoriye göre bildirimler
    Page<Notification> findByUserIdAndCategoryOrderByCreatedAtDesc(
            Long userId, NotificationCategory category, Pageable pageable);

    // Tüm bildirimleri okundu işaretle
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    // Kullanıcının bildirimini sil
    void deleteByIdAndUserId(Long id, Long userId);
}
