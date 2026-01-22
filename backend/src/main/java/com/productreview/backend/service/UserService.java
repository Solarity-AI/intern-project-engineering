package com.productreview.backend.service;

import com.productreview.backend.dto.ProductDTO;
import com.productreview.backend.entity.AppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    // Wishlist operations
    List<Long> getWishlist(String userId);

    Page<ProductDTO> getWishlistProducts(String userId, Pageable pageable);

    void toggleWishlist(String userId, Long productId);

    // Notification operations
    List<AppNotification> getNotifications(String userId);

    long getUnreadCount(String userId);

    void markAsRead(Long notificationId);

    void markAllAsRead(String userId);

    void createNotification(String userId, String title, String message, Long productId);

    void deleteNotification(Long notificationId);

    void deleteAllNotifications(String userId);
}
