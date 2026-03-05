package com.example.productreview.service;

import com.example.productreview.dto.NotificationDTO;
import com.example.productreview.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    List<Long> getWishlist(String userId);

    Page<ProductDTO> getWishlistProducts(String userId, Pageable pageable);

    void toggleWishlist(String userId, Long productId);

    List<NotificationDTO> getNotifications(String userId);

    long getUnreadCount(String userId);

    void markAsRead(Long notificationId, String userId);

    void markAllAsRead(String userId);

    void createNotification(String userId, String title, String message, Long productId);

    void deleteNotification(Long notificationId, String userId);

    void deleteAllNotifications(String userId);
}
