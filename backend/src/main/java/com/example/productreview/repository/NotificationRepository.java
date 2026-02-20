package com.example.productreview.repository;

import com.example.productreview.model.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findByUserIdOrderByCreatedAtDesc(String userId);
    long countByUserIdAndIsReadFalse(String userId);

    @Modifying
    @Query("UPDATE AppNotification a SET a.isRead = true WHERE a.userId = :userId AND a.isRead = false")
    int markAllAsReadByUserId(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM AppNotification a WHERE a.userId = :userId")
    int deleteAllByUserId(@Param("userId") String userId);
}
