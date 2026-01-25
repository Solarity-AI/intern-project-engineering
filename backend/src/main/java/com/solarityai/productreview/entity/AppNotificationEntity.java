package com.solarityai.productreview.entity;

import com.solarityai.backendfw.foundation.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class AppNotificationEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "notification_created_at")
    private Instant notificationCreatedAt;

    @Column(name = "product_id")
    private UUID productId;

    @PrePersist
    protected void onCreate() {
        if (notificationCreatedAt == null) {
            notificationCreatedAt = Instant.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }
}
