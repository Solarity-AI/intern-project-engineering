package com.solarityai.productreview.service;

import com.solarityai.backendfw.query.model.PageRequestDto;
import com.solarityai.productreview.dto.AppNotificationDto;
import com.solarityai.productreview.dto.NotificationCreateDto;
import com.solarityai.productreview.dto.ProductDto;
import com.solarityai.productreview.dto.UnreadCountDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface UserService {

    // Wishlist operations
    List<UUID> getWishlistProductIds(String userId);

    Page<ProductDto> getWishlistProducts(String userId, PageRequestDto pageRequest);

    void toggleWishlistItem(String userId, UUID productId);

    // Notification operations
    List<AppNotificationDto> getNotifications(String userId);

    UnreadCountDto getUnreadNotificationCount(String userId);

    void markNotificationAsRead(UUID notificationId);

    void markAllNotificationsAsRead(String userId);

    AppNotificationDto createNotification(String userId, NotificationCreateDto dto);

    void deleteNotification(UUID notificationId);

    void deleteAllNotifications(String userId);
}
