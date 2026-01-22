package com.productreview.backend.repository;

import com.productreview.backend.entity.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<AppNotification, Long> {

    List<AppNotification> findByUserIdOrderByCreatedAtDesc(String userId);

    long countByUserIdAndReadFalse(String userId);

    @Modifying
    @Query("UPDATE AppNotification n SET n.read = true WHERE n.userId = :userId")
    void markAllAsReadByUserId(@Param("userId") String userId);

    void deleteByUserId(String userId);
}